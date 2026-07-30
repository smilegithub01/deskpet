package com.deskpet.app.service

import com.deskpet.app.data.db.AppDatabase
import com.deskpet.app.data.model.EnvCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class WeatherInfo(
    val temp: Int,
    val text: String,
    val icon: String
)

data class DailyQuote(
    val content: String,
    val source: String
)

/**
 * Unified service for weather (QWeather), daily quote (Hitokoto), and
 * festival (local lunar calendar) data.
 *
 * All API calls fail silently — core functionality is never blocked.
 */
class EnvApiService(private val database: AppDatabase) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val envCacheDao = database.envCacheDao()

    // QWeather API key — set via local.properties/BuildConfig in production
    private val qweatherApiKey: String = ""

    suspend fun fetchWeather(): WeatherInfo? = withContext(Dispatchers.IO) {
        if (qweatherApiKey.isBlank()) return@withContext null
        try {
            // QWeather API would go here when API key is configured
            null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun fetchDailyQuote(): DailyQuote? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://v1.hitokoto.cn")
                .build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val json = JSONObject(response.body?.string() ?: "")
                val quote = DailyQuote(
                    content = json.optString("hitokoto", "每一天都是新的开始~"),
                    source = json.optString("from", "")
                )
                envCacheDao.upsert(EnvCache(
                    key = "daily_quote",
                    value = json.toString(),
                    updatedAt = System.currentTimeMillis()
                ))
                quote
            } else null
        } catch (e: Exception) {
            val cached = envCacheDao.get("daily_quote")
            if (cached != null) {
                try {
                    val json = JSONObject(cached.value)
                    DailyQuote(
                        content = json.optString("hitokoto", "每一天都是新的开始~"),
                        source = json.optString("from", "")
                    )
                } catch (ex: Exception) { null }
            } else null
        }
    }

    suspend fun getCachedQuote(): DailyQuote? {
        val cached = envCacheDao.get("daily_quote") ?: return null
        return try {
            val json = JSONObject(cached.value)
            DailyQuote(
                content = json.optString("hitokoto"),
                source = json.optString("from")
            )
        } catch (e: Exception) { null }
    }

    suspend fun getCachedWeather(): WeatherInfo? {
        val cached = envCacheDao.get("weather") ?: return null
        return try {
            val json = JSONObject(cached.value)
            WeatherInfo(
                temp = json.optInt("temp"),
                text = json.optString("text"),
                icon = json.optString("icon")
            )
        } catch (e: Exception) { null }
    }
}
