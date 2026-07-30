package com.deskpet.app.data.model

enum class TravelType(val displayName: String, val durationRange: Pair<Long, Long>) {
    SHORT("短途", Pair(30 * 60 * 1000L, 60 * 60 * 1000L)),
    MEDIUM("中途", Pair(2 * 60 * 60 * 1000L, 4 * 60 * 60 * 1000L)),
    LONG("长途", Pair(6 * 60 * 60 * 1000L, 12 * 60 * 60 * 1000L))
}

data class TravelDestination(
    val id: String,
    val name: String,
    val type: TravelType,
    val emoji: String,
    val requiredLevel: Int,
    val requiredOutfit: String? = null,
    val sceneDrawKey: String,
    val postcardTemplates: List<String>,
    val giftDiamondRange: Pair<Int, Int>,
    val giftOutfitChance: Float = 0.05f,
    val giftFurnitureChance: Float = 0.03f
)

val TRAVEL_DESTINATIONS: List<TravelDestination> = listOf(
    TravelDestination("park", "公园", TravelType.SHORT, "🌳", 1, null, "park_scene",
        listOf("在公园遇到了蝴蝶！", "晒太阳好舒服~", "和别的小动物打招呼了"),
        Pair(5, 15)),
    TravelDestination("cafe", "咖啡馆", TravelType.SHORT, "☕", 1, null, "cafe_scene",
        listOf("咖啡好香~", "在咖啡馆睡了个午觉", "店主给了小饼干"),
        Pair(8, 20)),
    TravelDestination("beach", "海边", TravelType.MEDIUM, "🏖️", 5, null, "beach_scene",
        listOf("海风好舒服~", "捡到了贝壳！", "看到了海鸥"),
        Pair(15, 40)),
    TravelDestination("forest", "山林", TravelType.MEDIUM, "🌲", 5, null, "forest_scene",
        listOf("空气好清新！", "看到了小松鼠", "在草地上打滚"),
        Pair(15, 40)),
    TravelDestination("snow", "雪山", TravelType.LONG, "🏔️", 10, "head_beanie", "snow_scene",
        listOf("雪好白好美~", "堆了个小雪人！", "差点滑倒了"),
        Pair(30, 80)),
    TravelDestination("starry", "星空", TravelType.LONG, "✨", 15, null, "starry_scene",
        listOf("星星好漂亮！", "许了个愿望~", "看到了流星"),
        Pair(40, 100))
)
