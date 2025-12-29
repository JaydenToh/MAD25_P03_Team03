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


        dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050, 1024, 0)

        val pitchHandler = PitchDetectionHandler { result, _ ->
            val pitchInHz = result.pitch

            if (pitchInHz != -1f) {

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


    private fun getNoteName(hz: Float): String {
        if (hz < 20) return ""
        val notes = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
        val log2 = Math.log(hz / 440.0) / Math.log(2.0)
        val noteNumber = (Math.round(12 * log2) + 69).toInt() // MIDI note number
        val octave = (noteNumber / 12) - 1
        val noteIndex = noteNumber % 12
        return if (noteIndex >= 0 && noteIndex < notes.size) {
            "${notes[noteIndex]}$octave"
        } else {
            "?"
        }
    }
}