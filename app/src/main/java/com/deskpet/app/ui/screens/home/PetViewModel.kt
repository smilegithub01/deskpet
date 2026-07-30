package com.deskpet.app.ui.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deskpet.app.DeskPetApplication
import com.deskpet.app.data.model.MoodLevel
import com.deskpet.app.data.model.Pet
import com.deskpet.app.data.model.PetState
import com.deskpet.app.data.model.equippedOutfitIds
import com.deskpet.app.data.repository.PetRepository
import com.deskpet.app.util.PhotoHelper
import com.deskpet.app.util.SoundHelper
import com.deskpet.app.util.SoundType
import com.deskpet.app.util.SpeechHelper
import com.deskpet.app.util.DialogueBank
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.deskpet.app.data.model.InteractionLog
import com.deskpet.app.data.model.InteractionType
import com.deskpet.app.service.EnvApiService
import com.deskpet.app.service.PetMemoryEngine
import com.deskpet.app.util.LunarCalendarHelper

/**
 * Food options shown in the feed bottom-sheet.
 */
data class FoodOption(
    val name: String,
    val emoji: String,
    val hungerGain: Int
)

val foodOptions: List<FoodOption> = listOf(
    FoodOption("小鱼干", "🐟", 10),
    FoodOption("猫粮", "🥣", 15),
    FoodOption("罐头", "🥫", 20),
    FoodOption("草莓", "🍓", 8)
)

/**
 * UI state + intent handling for the Pet Home screen.
 *
 * Delegates persistence to [PetRepository] and owns the transient animation
 * state (current [PetState], food sheet visibility, toast text, heart particles).
 */
class PetViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository: PetRepository =
        getApplication<DeskPetApplication>().repository

    private val interactionLogDao = getApplication<DeskPetApplication>().database.interactionLogDao()
    private val memoryEngine = PetMemoryEngine(
        getApplication<DeskPetApplication>().database,
        repository
    )
    private val envApiService = EnvApiService(getApplication<DeskPetApplication>().database)

    private val _dailyQuote = MutableStateFlow<String?>(null)
    val dailyQuote: StateFlow<String?> = _dailyQuote

    private val _festivalMessage = MutableStateFlow<String?>(null)
    val festivalMessage: StateFlow<String?> = _festivalMessage

    init {
        viewModelScope.launch {
            memoryEngine.generateIfNeeded()
        }
        viewModelScope.launch {
            // Load daily quote
            val cached = envApiService.getCachedQuote()
            if (cached != null) {
                _dailyQuote.value = cached.content
            } else {
                val fetched = envApiService.fetchDailyQuote()
                _dailyQuote.value = fetched?.content
            }

            // Check festival
            val festival = LunarCalendarHelper.getTodayFestival()
            _festivalMessage.value = festival?.petMessage

            // Show quote or festival message as speech bubble (only if no other bubble is showing)
            val envMessage = _festivalMessage.value ?: _dailyQuote.value
            if (envMessage != null) {
                speak(envMessage)
                delay(4000)
                _speechBubble.value = null
            }
        }
    }

    /** Live pet state sourced from the repository. */
    val pet: StateFlow<Pet> = repository.petState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = repository.getPet()
    )

    /** Transient animation state (IDLE / HAPPY / EATING). */
    private val _petState = MutableStateFlow(PetState.IDLE)
    val petState: StateFlow<PetState> = _petState.asStateFlow()

    /** Controls the food bottom-sheet visibility. */
    private val _showFoodSheet = MutableStateFlow(false)
    val showFoodSheet: StateFlow<Boolean> = _showFoodSheet.asStateFlow()

    /** One-shot toast messages. */
    private val _toast = MutableStateFlow<String?>(null)
    val toast: StateFlow<String?> = _toast.asStateFlow()

    /** Floating heart particles overlay (shown briefly after petting). */
    private val _showHearts = MutableStateFlow(false)
    val showHearts: StateFlow<Boolean> = _showHearts.asStateFlow()

    /** Speech bubble text shown above the pet. */
    private val _speechBubble = MutableStateFlow<String?>(null)
    val speechBubble: StateFlow<String?> = _speechBubble.asStateFlow()

    /** Shows a speech bubble and speaks it via TTS if enabled. */
    private fun speak(text: String) {
        _speechBubble.value = text
        if (repository.getSettings().ttsEnabled) {
            SpeechHelper.speak(text)
        }
    }

    /** White flash overlay used by the photo action. */
    private val _flash = MutableStateFlow(false)
    val flash: StateFlow<Boolean> = _flash.asStateFlow()

    // ----------------------------------------------------------- Intents

    /** 抚摸: trigger HAPPY state, mood/intimacy up, hearts + speech bubble. */
    fun onPet() {
        repository.petPet()
        SoundHelper.play(SoundType.PET)
        viewModelScope.launch {
            interactionLogDao.insert(InteractionLog(
                type = InteractionType.PET.name,
                timestamp = System.currentTimeMillis()
            ))
        }
        _petState.value = PetState.HAPPY
        _showHearts.value = true
        speak("好舒服呀～")
        viewModelScope.launch {
            delay(2500)
            _petState.value = PetState.IDLE
            _showHearts.value = false
        }
        viewModelScope.launch {
            delay(2000)
            _speechBubble.value = null
        }
    }

    /** Tapping the pet directly on Home also counts as petting. */
    fun onPetClicked() = onPet()

    /** Opens the food bottom-sheet. */
    fun onOpenFoodSheet() {
        _showFoodSheet.value = true
    }

    /** Dismisses the food bottom-sheet without feeding. */
    fun onDismissFoodSheet() {
        _showFoodSheet.value = false
    }

    /** 喂食: select a food → eating animation → hunger up. */
    fun onFeed(food: FoodOption) {
        _showFoodSheet.value = false
        _petState.value = PetState.EATING
        repository.feedPet(food.name)
        SoundHelper.play(SoundType.FEED)
        viewModelScope.launch {
            interactionLogDao.insert(InteractionLog(
                type = InteractionType.FEED.name,
                timestamp = System.currentTimeMillis(),
                detail = food.name
            ))
        }
        speak("真好吃～")
        viewModelScope.launch {
            delay(2000)
            _petState.value = PetState.IDLE
        }
        viewModelScope.launch {
            delay(1800)
            _speechBubble.value = null
        }
    }

    /** Mood selector: nudges the pet's mood value based on the chosen mood. */
    fun onMoodSelected(mood: MoodLevel) {
        val delta = when (mood) {
            MoodLevel.HAPPY, MoodLevel.EXCITED -> 5
            MoodLevel.CALM -> 2
            MoodLevel.TIRED -> -3
            MoodLevel.SAD -> -5
        }
        repository.updateMood(delta)
        viewModelScope.launch {
            interactionLogDao.insert(InteractionLog(
                type = InteractionType.MOOD_SELECTED.name,
                timestamp = System.currentTimeMillis(),
                detail = mood.name
            ))
        }
        _petState.value = if (delta > 0) PetState.HAPPY else PetState.SLEEPY
        viewModelScope.launch {
            delay(2000)
            _petState.value = PetState.IDLE
        }
    }

    /** 拍照: render pet to a Bitmap, save via FileProvider and offer share. */
    fun onPhoto() {
        val pet = repository.getPet()
        val outfits = pet.equippedOutfitIds(
            getApplication<DeskPetApplication>().repository.getOutfitItems()
        )
        val context = getApplication<Application>()

        viewModelScope.launch {
            interactionLogDao.insert(InteractionLog(
                type = InteractionType.PHOTO.name,
                timestamp = System.currentTimeMillis()
            ))
            val uri = PhotoHelper.captureAndSave(
                context = context,
                petColor = pet.color,
                species = pet.species,
                petState = _petState.value,
                outfits = outfits,
                petName = pet.name
            )

            _flash.value = true
            delay(400)
            _flash.value = false

            if (uri != null) {
                _toast.value = "合影已保存"
                PhotoHelper.launchShare(context, uri)
            } else {
                _toast.value = "保存失败，请重试"
            }
            delay(2000)
            _toast.value = null
        }
    }

    /** Clears the current toast (called after the snackbar is shown). */
    fun onToastShown() {
        _toast.value = null
    }
}
