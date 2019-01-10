package tech.touraine.timer

import android.os.Handler
import com.google.android.things.contrib.driver.pwmspeaker.Speaker
import java.io.Closeable

class Buzzer(private val speaker: Speaker = Speaker(Buzzer.SPEAKER_PWM_PIN),
             private val stopHandler: Handler = Handler()) : Closeable {

    companion object {
        const val SPEAKER_PWM_PIN = "PWM2"
    }

    private var stopRunnable: Runnable? = null

    init {
        stopRunnable = Runnable { stop() }
    }

    fun play(frequency: Double) {
        speaker.play(frequency)
    }

    fun play(frequency: Double, duration: Double) {
        speaker.play(frequency)
        stopHandler.postDelayed(stopRunnable, duration.toLong())
    }

    fun stop() {
        speaker.stop()
    }

    override fun close() {
        stopHandler.removeCallbacks(stopRunnable)
        speaker.stop()
        speaker.close()
    }

}