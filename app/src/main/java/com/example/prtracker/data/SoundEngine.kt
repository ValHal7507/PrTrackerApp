package com.example.prtracker.data

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

object SoundEngine {
    private const val SAMPLE_RATE = 44100
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    var volume: Float = 1f

    fun release() {
        scope.cancel()
    }

    fun playTapExercise() {
        scope.launch {
            val tap1 = sine(880f, 40, 0.3f) { i, n -> 1f - i.toFloat() / n }
            val tap2 = sine(1100f, 30, 0.3f) { i, n -> 1f - i.toFloat() / n }
            playSound(tap1, tap2)
        }
    }

    fun playLogEntry() {
        scope.launch {
            val sweep = sweepGen(600f, 1200f, 120, 0.5f) { i, n ->
                if (i > n * 0.7f) 1f - (i.toFloat() - n * 0.7f) / (n * 0.3f) else 1f
            }
            playSound(sweep)
        }
    }

    fun playNewPR() {
        scope.launch {
            val note1 = sine(523f, 80, 0.8f) { i, n ->
                val attack = (i.toFloat() / (n * 0.125f)).coerceAtMost(1f)
                val decay = if (i > n - 40) 1f - (i.toFloat() - (n - 40)) / 40f else 1f
                attack * decay
            }
            val gap1 = silence(10)
            val note2 = sine(659f, 80, 0.8f) { i, n ->
                val attack = (i.toFloat() / (n * 0.125f)).coerceAtMost(1f)
                val decay = if (i > n - 40) 1f - (i.toFloat() - (n - 40)) / 40f else 1f
                attack * decay
            }
            val gap2 = silence(10)
            val note3 = sine(784f, 150, 0.8f) { i, n ->
                val attack = (i.toFloat() / (n * 0.067f)).coerceAtMost(1f)
                val decay = if (i > n - 40) 1f - (i.toFloat() - (n - 40)) / 40f else 1f
                attack * decay
            }
            val shimmer = sweepGen(1000f, 2000f, 80, 0.3f) { i, n -> 1f - i.toFloat() / n }
            playSound(note1, gap1, note2, gap2, note3, shimmer)
        }
    }

    fun playDeleteEntry() {
        scope.launch {
            val sweep = sweepGen(400f, 200f, 80, 0.25f) { i, n -> 1f - i.toFloat() / n }
            playSound(sweep)
        }
    }

    fun playDeleteExercise() {
        scope.launch {
            val numSamples = (SAMPLE_RATE * 150 / 1000f).toInt()
            val samples = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val t = i.toFloat() / numSamples
                val freq = 200f - 120f * t
                val sinePart = sin(2.0 * PI * freq * i / SAMPLE_RATE).toFloat()
                val period = SAMPLE_RATE.toFloat() / freq
                val squarePart = if ((i % period.toInt()) < period / 2) 1f else -1f
                val value = 0.5f * 32767f * (0.8f * sinePart + 0.2f * squarePart) * (1f - t)
                samples[i] = value.toInt().toShort()
            }
            playSound(samples)
        }
    }

    fun playTogglePin() {
        scope.launch {
            val samples = sine(1800f, 60, 0.4f) { i, n ->
                val t = i.toFloat() / n * 30f
                exp(-t).toFloat()
            }
            playSound(samples)
        }
    }

    fun playToggleHaptic() {
        scope.launch {
            val bip1 = sine(700f, 35, 0.3f) { _, _ -> 1f }
            val gap = silence(25)
            val bip2 = sine(700f, 35, 0.3f) { _, _ -> 1f }
            playSound(bip1, gap, bip2)
        }
    }

    fun playTimerStart() {
        scope.launch {
            val sweep = sweepGen(440f, 880f, 100, 0.4f) { i, n -> i.toFloat() / n }
            playSound(sweep)
        }
    }

    fun playTimerStop() {
        scope.launch {
            val sweep = sweepGen(880f, 440f, 100, 0.4f) { i, n -> 1f - i.toFloat() / n }
            playSound(sweep)
        }
    }

    fun playTimerTargetReached() {
        scope.launch {
            val ping1 = sine(1200f, 50, 0.6f) { i, n -> 1f - i.toFloat() / n }
            val gap = silence(20)
            val ping2 = sine(1200f, 50, 0.6f) { i, n -> 1f - i.toFloat() / n }
            playSound(ping1, gap, ping2)
        }
    }

    fun playCalendarRestDay() {
        scope.launch {
            val samples = sine(523f, 120, 0.35f) { i, n ->
                val t = i.toFloat() / n * 15f
                exp(-t).toFloat()
            }
            playSound(samples)
        }
    }

    fun playNavigation() {
        scope.launch {
            val samples = sine(2000f, 20, 0.15f) { _, _ -> 1f }
            playSound(samples)
        }
    }

    private fun sine(
        freq: Float,
        durationMs: Int,
        amplitude: Float,
        envelope: (Int, Int) -> Float
    ): ShortArray {
        val numSamples = (SAMPLE_RATE * durationMs / 1000f).toInt().coerceAtLeast(1)
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val value = (amplitude * 32767f * sin(2.0 * PI * freq * i / SAMPLE_RATE) * envelope(i, numSamples)).toInt()
            samples[i] = value.coerceIn(-32768, 32767).toShort()
        }
        return samples
    }

    private fun sweepGen(
        startFreq: Float,
        endFreq: Float,
        durationMs: Int,
        amplitude: Float,
        envelope: (Int, Int) -> Float
    ): ShortArray {
        val numSamples = (SAMPLE_RATE * durationMs / 1000f).toInt().coerceAtLeast(1)
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val freq = startFreq + (endFreq - startFreq) * i.toFloat() / numSamples
            val value = (amplitude * 32767f * sin(2.0 * PI * freq * i / SAMPLE_RATE) * envelope(i, numSamples)).toInt()
            samples[i] = value.coerceIn(-32768, 32767).toShort()
        }
        return samples
    }

    private fun silence(durationMs: Int): ShortArray {
        val numSamples = (SAMPLE_RATE * durationMs / 1000f).toInt().coerceAtLeast(0)
        return ShortArray(numSamples)
    }

    private suspend fun playSound(vararg arrays: ShortArray) {
        val totalSize = arrays.sumOf { it.size }
        if (totalSize == 0) return
        val buffer = ShortArray(totalSize)
        var offset = 0
        for (arr in arrays) {
            System.arraycopy(arr, 0, buffer, offset, arr.size)
            offset += arr.size
        }

        if (volume < 1f) {
            for (i in buffer.indices) {
                buffer[i] = (buffer[i].toInt() * volume).toInt().coerceIn(-32768, 32767).toShort()
            }
        }

        val bufferSizeInBytes = buffer.size * 2
        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSizeInBytes)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        try {
            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()
            delay((buffer.size * 1000L) / SAMPLE_RATE + 100)
        } catch (_: Exception) {
        } finally {
            try {
                audioTrack.release()
            } catch (_: Exception) { }
        }
    }
}
