package com.deskpet.app.data.model

/**
 * A wearable outfit item available in the shop / wardrobe.
 */
data class OutfitItem(
    val id: String,
    val category: OutfitCategory,
    val name: String,
    val emoji: String,
    val price: Int = 0,
    val requiredLevel: Int = 1,
    val isOwned: Boolean = false
)

/**
 * Category an [OutfitItem] belongs to.
 */
enum class OutfitCategory(val displayName: String) {
    HEAD("头饰"),
    GLASSES("眼镜"),
    COLLAR("项圈"),
    CLOTHING("服装"),
    TAIL("尾饰"),
    ACCESSORY("随身")
}

/**
 * Builds a map of equipped [OutfitCategory] → outfit id from a [Pet]'s
 * equipped item ids. The [catalogue] is accepted for API symmetry but is no
 * longer used to resolve emojis; callers now pass raw ids that
 * [com.deskpet.app.ui.components.OutfitRenderer] draws as vectors.
 *
 * Used by [com.deskpet.app.ui.components.PetCanvas] to render worn outfits.
 */
fun Pet.equippedOutfitIds(catalogue: List<OutfitItem>): Map<OutfitCategory, String> {
    val result = mutableMapOf<OutfitCategory, String>()
    equippedHead?.let { result[OutfitCategory.HEAD] = it }
    equippedGlasses?.let { result[OutfitCategory.GLASSES] = it }
    equippedCollar?.let { result[OutfitCategory.COLLAR] = it }
    equippedClothing?.let { result[OutfitCategory.CLOTHING] = it }
    equippedTail?.let { result[OutfitCategory.TAIL] = it }
    equippedAccessory?.let { result[OutfitCategory.ACCESSORY] = it }
    return result
}

@Deprecated("Use equippedOutfitIds instead", ReplaceWith("equippedOutfitIds(catalogue)"))
fun Pet.equippedOutfitEmojis(catalogue: List<OutfitItem>): Map<OutfitCategory, String> =
    equippedOutfitIds(catalogue)
