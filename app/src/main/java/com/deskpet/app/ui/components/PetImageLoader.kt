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
     * 检查指定物种+颜色是否有图片资源可用
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
     * 加载并返回已抠除白色背景的宠物图片
     * @return 透明背景的 ImageBitmap，无资源时返回 null（由调用方走矢量 fallback）
     */
    fun loadPetBitmap(context: Context, species: PetSpecies, color: PetColor): ImageBitmap? {
        val key = assetKey(species, color)
        cache[key]?.let { return it }

        val path = "pets/$key.jpg"
        return try {
            val bitmap = context.assets.open(path).use { stream ->
                BitmapFactory.decodeStream(stream)
            } ?: return null
            val keyed = chromaKeyWhite(bitmap)
            // 回收原始 bitmap 释放内存
            if (keyed !== bitmap) bitmap.recycle()
            val result = keyed.asImageBitmap()
            cache[key] = result
            result
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
     * 阈值策略（平滑过渡，避免硬边缘锯齿）：
     * - brightness >= 250（接近纯白）：完全透明
     * - brightness 225~250：线性渐变 alpha（边缘抗锯齿）
     * - brightness < 225（主体与浅色毛发）：完全不透明，保留水彩质感
     */
    private fun chromaKeyWhite(bitmap: Bitmap): Bitmap {
        val result = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val w = result.width
        val h = result.height
        val pixels = IntArray(w * h)
        result.getPixels(pixels, 0, w, 0, 0, w, h)

        val highThreshold = 250
        val lowThreshold = 225
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
