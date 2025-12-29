package np.ict.mad.mad25_p03_team03.utils

import android.util.Log
import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.AudioEvent
import be.tarsos.dsp.io.android.AudioDispatcherFactory
import be.tarsos.dsp.pitch.PitchDetectionHandler
import be.tarsos.dsp.pitch.PitchProcessor

class PitchDetector {
    private var dispatcher: AudioDispatcher? = null
    private var isRunning = false

    fun start(onPitchDetected: (Float, String) -> Unit) {
        if (isRunning) return

        // 采样率 22050Hz, 缓冲区 1024
        dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050, 1024, 0)

        val pitchHandler = PitchDetectionHandler { result, _ ->
            val pitchInHz = result.pitch
            // -1 表示没识别到有效声音（比如太安静）
            if (pitchInHz != -1f) {
                // 简单的频率转音名算法 (比如 261Hz -> C4)
                val noteName = getNoteName(pitchInHz)
                onPitchDetected(pitchInHz, noteName)
            }
        }

        val pitchProcessor = PitchProcessor(
            PitchProcessor.PitchEstimationAlgorithm.YIN,
            22050f,
            1024,
            pitchHandler
        )

        dispatcher?.addAudioProcessor(pitchProcessor)

        Thread(dispatcher, "Audio Dispatcher").start()
        isRunning = true
    }

    fun stop() {
        if (!isRunning) return
        dispatcher?.stop()
        isRunning = false
    }

    // 辅助函数：Hz 转 音名
    private fun getNoteName(hz: Float): String {
        if (hz < 20) return "" // 噪音过滤
        val notes = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
        val log2 = Math.log(hz / 440.0) / Math.log(2.0)
        val noteNumber = Math.round(12 * log2) + 69 // MIDI note number
        val octave = (noteNumber / 12) - 1
        val noteIndex = noteNumber % 12
        return if (noteIndex >= 0 && noteIndex < notes.size) {
            "${notes[noteIndex]}$octave" // 例如 C4, A3
        } else {
            "?"
        }
    }
}