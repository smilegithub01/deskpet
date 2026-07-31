package com.deskpet.app.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.deskpet.app.data.model.PetColor
import com.deskpet.app.data.model.PetSpecies

/**
 * 宠物图片资源加载器
 *
 * 行业方案：静态 PNG/JPG 素材 + 代码轻量动效
 * - 从 assets/pets 加载宠物主体图片
 * - 对白色背景做 chroma key 像素级抠除（AI 生成的 JPG 无透明通道）
 * - 缓存处理后的透明 ImageBitmap，避免重复计算
 */
object PetImageLoader {

    private val cache = mutableMapOf<String, ImageBitmap>()

    /**
     * 每个物种的默认颜色（有对应图片素材）
     */
    private val defaultColorFor = mapOf(
        PetSpecies.CAT to PetColor.PINK,
        PetSpecies.DOG to PetColor.PEACH,
        PetSpecies.RABBIT to PetColor.BLUE,
        PetSpecies.HAMSTER to PetColor.MINT
    )

    /**
     * 检查指定物种+颜色是否有精确匹配的图片资源
     */
    fun hasAsset(context: Context, species: PetSpecies, color: PetColor): Boolean {
        val key = assetKey(species, color)
        return try {
            context.assets.open("pets/$key.jpg").use { it.available() > 0 }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 加载宠物图片，支持颜色变体：
     * 1. 优先精确匹配 species_color.jpg
     * 2. 无精确匹配时加载物种默认图（如 cat_pink.jpg），由调用方做 ColorFilter tint 着色
     *
     * @return Pair<bitmap, needsTint> — needsTint=true 表示图片是默认色，需要叠加 colorFilter
     */
    fun loadPetBitmap(context: Context, species: PetSpecies, color: PetColor): Pair<ImageBitmap, Boolean>? {
        val exactKey = assetKey(species, color)

        // 1. 尝试精确匹配
        cache[exactKey]?.let { return it to false }
        if (hasAsset(context, species, color)) {
            return loadAndCache(context, exactKey, false)
        }

        // 2. 回退到物种默认颜色图
        val defaultColor = defaultColorFor[species] ?: return null
        val defaultKey = assetKey(species, defaultColor)
        cache[defaultKey]?.let { return it to (color != defaultColor) }
        if (hasAsset(context, species, defaultColor)) {
            return loadAndCache(context, defaultKey, color != defaultColor)
        }

        return null
    }

    private fun loadAndCache(context: Context, key: String, needsTint: Boolean): Pair<ImageBitmap, Boolean>? {
        return try {
            val bitmap = context.assets.open("pets/$key.jpg").use { stream ->
                BitmapFactory.decodeStream(stream)
            } ?: return null
            val keyed = chromaKeyWhite(bitmap)
            if (keyed !== bitmap) bitmap.recycle()
            val result = keyed.asImageBitmap()
            cache[key] = result
            result to needsTint
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 加载装饰图片（装饰素材应为带透明通道的 PNG）
     */
    fun loadOutfitBitmap(context: Context, outfitId: String): ImageBitmap? {
        cache[outfitId]?.let { return it }
        return try {
            val bitmap = context.assets.open("outfits/$outfitId.png").use { stream ->
                BitmapFactory.decodeStream(stream)
            } ?: return null
            val result = bitmap.asImageBitmap()
            cache[outfitId] = result
            result
        } catch (e: Exception) {
            null
        }
    }

    private fun assetKey(species: PetSpecies, color: PetColor): String =
        "${species.name.lowercase()}_${color.name.lowercase()}"

    /**
     * Chroma key：将白色背景像素转为透明，保留主体。
     *
     * 阈值策略（更激进，适配 AI 生成的水彩图）：
     * - brightness >= 240（接近纯白）：完全透明
     * - brightness 200~240：线性渐变 alpha（边缘抗锯齿，同时确保浅灰背景也被抠除）
     * - brightness < 200（主体与浅色毛发）：完全不透明，保留水彩质感
     */
    private fun chromaKeyWhite(bitmap: Bitmap): Bitmap {
        val result = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val w = result.width
        val h = result.height
        val pixels = IntArray(w * h)
        result.getPixels(pixels, 0, w, 0, 0, w, h)

        val highThreshold = 240
        val lowThreshold = 200
        val range = (highThreshold - lowThreshold).toFloat()

        for (i in pixels.indices) {
            val c = pixels[i]
            val r = (c shr 16) and 0xFF
            val g = (c shr 8) and 0xFF
            val b = c and 0xFF
            val brightness = (r + g + b) / 3

            if (brightness > lowThreshold) {
                val alpha = when {
                    brightness >= highThreshold -> 0
                    else -> {
                        val t = (brightness - lowThreshold) / range
                        (255 * (1f - t)).toInt().coerceIn(0, 255)
                    }
                }
                pixels[i] = (alpha shl 24) or (c and 0x00FFFFFF)
            }
        }
        result.setPixels(pixels, 0, w, 0, 0, w, h)
        return result
    }
}
