package com.deskpet.app.ui.screens.decor

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deskpet.app.DeskPetApplication
import com.deskpet.app.data.model.FurnitureCategory
import com.deskpet.app.data.model.FurnitureItem
import com.deskpet.app.data.model.RoomLayout
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DecorViewModel(application: android.app.Application) : AndroidViewModel(application) {
    private val repository = getApplication<DeskPetApplication>().repository

    val pet by lazy { repository.pet }
    val ownedFurniture by lazy { repository.ownedFurniture }
    val roomLayout by lazy { repository.roomLayout }

    private val _selectedCategory = MutableStateFlow(FurnitureCategory.WALLPAPER)
    val selectedCategory: StateFlow<FurnitureCategory> = _selectedCategory

    private val _toast = MutableStateFlow<String?>(null)
    val toast: StateFlow<String?> = _toast

    private val _selectedSlot = MutableStateFlow<Int?>(null)
    val selectedSlot: StateFlow<Int?> = _selectedSlot

    init {
        viewModelScope.launch {
            repository.loadRoomLayout()
        }
    }

    fun getFurnitureCatalogue(): List<FurnitureItem> = repository.getFurnitureItems()

    fun selectCategory(category: FurnitureCategory) {
        _selectedCategory.value = category
    }

    fun selectSlot(slotIndex: Int) {
        _selectedSlot.value = slotIndex
    }

    fun onTapFurniture(item: FurnitureItem) {
        val pet = pet.value
        val owned = ownedFurniture.value.contains(item.id)
        val layout = roomLayout.value

        viewModelScope.launch {
            if (owned) {
                // Find an available slot for this category
                val slotIndex = findAvailableSlot(item.category, layout)
                if (slotIndex == null) {
                    _toast.value = "该类型的格子已满，请先移除一件"
                } else {
                    repository.placeFurniture(slotIndex, item.id)
                    _toast.value = "已摆放「${item.name}」"
                }
            } else {
                if (pet.level < item.requiredLevel) {
                    _toast.value = "需要 Lv.${item.requiredLevel} 才能解锁"
                } else {
                    val ok = repository.purchaseFurniture(item)
                    _toast.value = if (ok) "购买成功！「${item.name}」已加入仓库" else "钻石不足"
                }
            }
        }
    }

    fun removeFurnitureAt(slotIndex: Int) {
        viewModelScope.launch {
            repository.removeFurniture(slotIndex)
            _toast.value = "已收起"
        }
    }

    private fun findAvailableSlot(category: FurnitureCategory, layout: List<RoomLayout>): Int? {
        val baseSlot = when (category) {
            FurnitureCategory.WALLPAPER -> 0
            FurnitureCategory.FLOOR -> 1
            FurnitureCategory.BED -> 2
            FurnitureCategory.TABLE -> 3
            FurnitureCategory.DECORATION -> 4
            FurnitureCategory.TOY -> 6
        }
        val slotCount = category.slotCount
        val occupiedSlots = layout.map { it.slotIndex }.toSet()
        for (i in 0 until slotCount) {
            if (baseSlot + i !in occupiedSlots) return baseSlot + i
        }
        return null
    }

    fun onToastShown() { _toast.value = null }
}
