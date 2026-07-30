package com.deskpet.app.util

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import kotlin.math.PI
import kotlin.math.sin

/**
 * Lightweight procedural sound effects engine.
 *
 * Generates short, cute "blip" tones on the fly with [AudioTrack] — no audio
 * asset files needed. Each [SoundType] maps to a distinct frequency envelope
 * so petting sounds happy, feeding sounds satisfied, etc.
 *
 * Call [init] once (e.g. from [Application.onCreate]) and [play] from any
 * interaction handler. All calls are offloaded to a background thread.
 */
object SoundHelper {

    private const val SAMPLE_RATE = 44100
    private var enabled = true
    private var initialized = false

    fun init() {
        initialized = true
    }

    fun setEnabled(value: Boolean) {
        enabled = value
    }

    fun isEnabled(): Boolean = enabled

    /**
     * Plays a short procedural tone for the given [type].
     * Safe to call from any thread; returns immediately.
     */
    fun play(type: SoundType) {
        if (!initialized || !enabled) return
        Thread {
            runCatching { playToneSync(type) }
        }.start()
    }

    private fun playToneSync(type: SoundType) {
        val (freqs, durationMs) = when (type) {
            SoundType.PET -> listOf(523.0, 659.0, 784.0) to 180
            SoundType.FEED -> listOf(440.0, 587.0) to 200
            SoundType.TAP_LIGHT -> listOf(880.0) to 60
            SoundType.TAP_HEAVY -> listOf(440.0) to 100
            SoundType.PURCHASE -> listOf(659.0, 880.0, 1047.0) to 220
            SoundType.EQUIP -> listOf(587.0, 784.0) to 120
            SoundType.ERROR -> listOf(220.0, 185.0) to 150
            SoundType.LEVEL_UP -> listOf(523.0, 659.0, 784.0, 1047.0) to 300
            SoundType.GREETING -> listOf(523.0, 659.0) to 250
            SoundType.SLEEP -> listOf(784.0, 523.0) to 350
            SoundType.WAKE -> listOf(523.0, 784.0) to 300
            SoundType.ACHIEVEMENT -> listOf(523.0, 659.0, 784.0, 1047.0) to 400
            SoundType.CHECKIN -> listOf(659.0, 880.0) to 150
            SoundType.TRAVEL_DEPART -> listOf(587.0, 698.0, 880.0) to 200
            SoundType.TRAVEL_RETURN -> listOf(523.0, 659.0, 784.0, 1047.0) to 350
        }

        val numSamples = (SAMPLE_RATE * durationMs / 1000.0).toInt()
        val buffer = ShortArray(numSamples)
        val segmentLen = numSamples / freqs.size

        freqs.forEachIndexed { idx, freq ->
            val start = idx * segmentLen
            val end = if (idx == freqs.size - 1) numSamples else (idx + 1) * segmentLen
            for (i in start until end) {
                val progress = (i - start).toFloat() / segmentLen
                // Envelope: quick attack, exponential decay
                val envelope = (progress * 4.0).coerceAtMost(1.0) *
                    Math.exp(-progress * 3.0)
                val sample = (sin(2.0 * PI * freq * (i - start) / SAMPLE_RATE) * envelope * 0.4 * Short.MAX_VALUE).toInt()
                buffer[i] = sample.toShort()
            }
        }

        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(SAMPLE_RATE)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(buffer.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        audioTrack.write(buffer, 0, buffer.size)
        audioTrack.play()
        // Wait for playback to finish, then release
        Thread.sleep(durationMs.toLong() + 50)
        runCatching { audioTrack.stop() }
        audioTrack.release()
    }
}

/**
 * Types of sound effects available in the app.
 */
enum class SoundType {
    /** 抚摸 pet — happy rising arpeggio */
    PET,
    /** 喂食 feed — satisfied two-note */
    FEED,
    /** 轻点击 tap light — short high blip */
    TAP_LIGHT,
    /** 重点击 tap heavy — short low blip */
    TAP_HEAVY,
    /** 购买 purchase — sparkle arpeggio */
    PURCHASE,
    /** 装备装备 equip — dress-up two-note */
    EQUIP,
    /** 错误 error — low descending */
    ERROR,
    /** 升级 level up — fanfare */
    LEVEL_UP,
    /** 问候 greeting — welcome two-note */
    GREETING,
    /** 睡眠 sleep — descending lull */
    SLEEP,
    /** 醒来 wake — rising wake-up */
    WAKE,
    /** 成就 achievement — fanfare */
    ACHIEVEMENT,
    /** 签到 checkin — two-note chime */
    CHECKIN,
    /** 出发旅行 travel depart — rising send-off */
    TRAVEL_DEPART,
    /** 返回旅行 travel return — fanfare homecoming */
    TRAVEL_RETURN
}
