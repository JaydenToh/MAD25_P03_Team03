package np.ict.mad.mad25_p03_team03.utils

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SoundGenerator {
    // 播放指定频率的声音（正弦波）
    suspend fun playTone(frequency: Double, durationMs: Int) = withContext(Dispatchers.Default) {
        val sampleRate = 44100
        val numSamples = durationMs * sampleRate / 1000
        val sample = DoubleArray(numSamples)
        val generatedSnd = ByteArray(2 * numSamples)

        // 生成正弦波数据
        for (i in 0 until numSamples) {
            sample[i] = Math.sin(2 * Math.PI * i / (sampleRate / frequency))
        }

        // 转换成 PCM 16bit byte array
        var idx = 0
        for (dVal in sample) {
            val shortVal = (dVal * 32767).toInt().toShort()
            generatedSnd[idx++] = (shortVal.toInt() and 0x00ff).toByte()
            generatedSnd[idx++] = (shortVal.toInt() ushr 8 and 0x00ff).toByte()
        }

        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(generatedSnd.size)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        audioTrack.write(generatedSnd, 0, generatedSnd.size)
        audioTrack.play()
        // 等待播放完释放
        Thread.sleep(durationMs.toLong())
        audioTrack.release()
    }
}