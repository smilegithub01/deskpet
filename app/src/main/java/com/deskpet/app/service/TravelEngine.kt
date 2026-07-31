// app/src/main/java/com/deskpet/app/service/TravelEngine.kt
package com.deskpet.app.service

import com.deskpet.app.data.db.AppDatabase
import com.deskpet.app.data.model.PetSpecies
import com.deskpet.app.data.model.Postcard
import com.deskpet.app.data.model.TRAVEL_DESTINATIONS
import com.deskpet.app.data.model.TravelDestination
import com.deskpet.app.data.model.TravelLog
import com.deskpet.app.data.repository.PetRepository
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.random.Random

/**
 * Manages pet travel: start, check return, generate gifts and postcards.
 */
class TravelEngine(
    private val database: AppDatabase,
    private val repository: PetRepository
) {
    private val travelLogDao = database.travelLogDao()
    private val postcardDao = database.postcardDao()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    data class TravelResult(
        val success: Boolean,
        val message: String,
        val returnTime: Long = 0L
    )

    data class TravelReturnResult(
        val returned: Boolean,
        val destinationName: String = "",
        val gifts: List<String> = emptyList(),
        val postcard: String? = null,
        val message: String = ""
    )

    /**
     * Starts a travel to the given destination with the specified duration.
     */
    suspend fun startTravel(destinationId: String, durationMs: Long): TravelResult {
        // Check if already traveling
        val active = travelLogDao.getActiveTravel()
        if (active != null) {
            return TravelResult(false, "宠物正在旅行中哦~")
        }

        val destination = TRAVEL_DESTINATIONS.find { it.id == destinationId }
            ?: return TravelResult(false, "未知目的地")

        val pet = repository.pet.value
        if (pet.level < destination.requiredLevel) {
            return TravelResult(false, "需要 Lv.${destination.requiredLevel} 才能前往${destination.name}")
        }

        // Check required outfit
        if (destination.requiredOutfit != null) {
            val hasOutfit = repository.ownedOutfits.value.contains(destination.requiredOutfit)
            if (!hasOutfit) {
                return TravelResult(false, "需要特定装扮才能前往${destination.name}")
            }
        }

        val now = System.currentTimeMillis()
        val returnTime = now + durationMs

        val log = TravelLog(
            destinationId = destination.id,
            destinationName = destination.name,
            departTime = now,
            returnTime = returnTime
        )
        travelLogDao.insert(log)

        return TravelResult(
            success = true,
            message = "${pet.name}出发去${destination.name}啦！旅途愉快~",
            returnTime = returnTime
        )
    }

    /**
     * Checks if the current travel has completed. If so, generates gifts and postcard.
     */
    suspend fun checkTravelReturn(): TravelReturnResult {
        val active = travelLogDao.getActiveTravel() ?: return TravelReturnResult(false)
        val now = System.currentTimeMillis()

        if (now < active.returnTime) {
            val remaining = active.returnTime - now
            val hours = remaining / (60 * 60 * 1000)
            val minutes = (remaining % (60 * 60 * 1000)) / (60 * 1000)
            return TravelReturnResult(
                returned = false,
                destinationName = active.destinationName,
                message = "${active.destinationName}旅行中，还有 ${hours}h${minutes}m 回来"
            )
        }

        // Travel complete — generate rewards
        val destination = TRAVEL_DESTINATIONS.find { it.id == active.destinationId }!!
        val pet = repository.pet.value
        val gifts = mutableListOf<String>()
        val giftJsonArray = JSONArray()

        // Diamond reward (always)
        val diamonds = Random.nextInt(destination.giftDiamondRange.first, destination.giftDiamondRange.second + 1)
        repository.addDiamonds(diamonds)
        gifts.add("💎 x$diamonds")
        giftJsonArray.put(JSONObject().apply {
            put("type", "diamond")
            put("amount", diamonds)
        })

        // Outfit gift (rare)
        if (Random.nextFloat() < destination.giftOutfitChance) {
            gifts.add("🎁 限定装扮!")
            giftJsonArray.put(JSONObject().apply { put("type", "outfit") })
        }

        // Furniture gift (rare)
        if (Random.nextFloat() < destination.giftFurnitureChance) {
            gifts.add("🏠 家具一件!")
            giftJsonArray.put(JSONObject().apply { put("type", "furniture") })
        }

        // Generate postcard
        val message = destination.postcardTemplates.random()
        val petEmoji = pet.species.emoji
        val postcard = Postcard(
            destinationId = destination.id,
            destinationName = destination.name,
            destinationEmoji = destination.emoji,
            date = dateFormat.format(java.util.Date()),
            message = message,
            sceneDrawKey = destination.sceneDrawKey,
            petEmoji = petEmoji
        )
        postcardDao.insert(postcard)

        // Complete the travel
        travelLogDao.completeTravel(
            active.id,
            now,
            postcards = 1,
            gifts = giftJsonArray.toString()
        )

        // Mood bonus
        repository.updateMood(10)

        return TravelReturnResult(
            returned = true,
            destinationName = destination.name,
            gifts = gifts,
            postcard = message,
            message = "${pet.name}从${destination.name}回来啦！带了${gifts.joinToString("、")}"
        )
    }

    /**
     * Settles overdue travels on app launch.
     *
     * If a travel's return time has passed but it is still marked active
     * (e.g. user was offline), this auto-settles it so rewards are not lost.
     * Travels that exceeded the return time by more than [OVERDUE_THRESHOLD_MS]
     * (72h) are force-settled silently.
     */
    suspend fun settleOverdueTravels(): TravelReturnResult? {
        val active = travelLogDao.getActiveTravel() ?: return null
        val now = System.currentTimeMillis()
        if (now < active.returnTime) return null

        // Overdue beyond threshold — settle now (offline settlement)
        val overdueMs = now - active.returnTime
        val result = checkTravelReturn()
        if (overdueMs > OVERDUE_THRESHOLD_MS && result.returned) {
            // Mark as offline-settled by appending note via repository mood update
            repository.updateMood(5)
        }
        return result
    }

    companion object {
        /** 72h: max offline grace period before force-settling a travel. */
        const val OVERDUE_THRESHOLD_MS: Long = 72L * 60 * 60 * 1000
    }

    /**
     * Gets the active travel (if any) for UI display.
     */
    suspend fun getActiveTravel(): TravelLog? = travelLogDao.getActiveTravel()

    /**
     * Gets all postcards for the collection.
     */
    fun getAllPostcards(): Flow<List<Postcard>> = postcardDao.getAll()

    /**
     * Gets travel history.
     */
    fun getAllTravels(): Flow<List<TravelLog>> = travelLogDao.getAll()
}
