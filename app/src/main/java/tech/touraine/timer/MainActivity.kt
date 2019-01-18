package tech.touraine.timer

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.KeyEvent
import android.widget.TextView
import android.widget.Button
import android.view.animation.Animation
import android.view.animation.AlphaAnimation
import java.io.Closeable


/**
 * Skeleton of an Android Things activity.
 *
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 *
 * <pre>{@code
 * val service = PeripheralManagerService()
 * val mLedGpio = service.openGpio("BCM6")
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)
 * mLedGpio.value = true
 * }</pre>
 * <p>
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 *
 * @see <a href="https://github.com/androidthings/contrib-drivers#readme">https://github.com/androidthings/contrib-drivers#readme</a>
 *
 */
const val DURATION_50_MINUTES: Long = 3000000
const val QUESTION_TIME_50_MINUTES: Long = 300000
const val DURATION_15_MINUTES: Long = 900000
const val QUESTION_TIME_15_MINUTES: Long = 0

class MainActivity : Activity() {

    var timerTextView: TextView? = null
    var duration: Long = DURATION_50_MINUTES
    var questionTime: Long = QUESTION_TIME_50_MINUTES
    var countDownTimer: CountDownTimer? = null
    var running: Boolean = false
    var once:Boolean = false
    private lateinit var buttons: Buttons
    private lateinit var buzzer: Buzzer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        buttons = Buttons()
        buzzer = Buzzer()
        setContentView(R.layout.activity_main)
        timerTextView = findViewById(R.id.timerTextView)
        timerTextView!!.text = getTimeStringFromMillis(duration)

        val startStopButton = findViewById<Button>(R.id.buttonStart)
        startStopButton.text = getString(R.string.start_Btn)
        startStopButton.setOnClickListener {
            startStopFunction(it as Button)
        }

        findViewById<Button>(R.id.button50Minutes).setOnClickListener {
            setTimerValues(DURATION_50_MINUTES, QUESTION_TIME_50_MINUTES)
        }

        findViewById<Button>(R.id.button15Minutes).setOnClickListener {
            setTimerValues(DURATION_15_MINUTES, QUESTION_TIME_15_MINUTES)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        arrayOf(buttons, buzzer).forEach(Closeable::close)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        buzzer.play(71.toDouble(), 300 * 0.8)
        return when (keyCode) {
            KeyEvent.KEYCODE_A -> {
                startStopFunction(findViewById(R.id.buttonStart))
                true
            }
            KeyEvent.KEYCODE_B -> {
                setTimerValues(DURATION_50_MINUTES, QUESTION_TIME_50_MINUTES)
                true
            }
            KeyEvent.KEYCODE_C -> {
                setTimerValues(DURATION_15_MINUTES, QUESTION_TIME_15_MINUTES)
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }

    private fun startStopFunction(button1: Button) {
        if (getString(R.string.stop_Btn) == button1.text) {
            button1.text = getString(R.string.start_Btn)
            countDownTimer!!.cancel()
            timerTextView!!.clearAnimation()
            timerTextView!!.setTextColor(Color.parseColor("#808080"))
            timerTextView!!.text = getTimeStringFromMillis(duration)
            running = false
        } else {
            button1.text = getString(R.string.stop_Btn)
            countDownTimer = getCountDown()
            countDownTimer!!.start()
        }
    }

    private fun setTimerValues(_duration: Long, _questionTime: Long) {
        duration = _duration
        questionTime = _questionTime
        timerTextView!!.clearAnimation()
        if (!running) {
            timerTextView!!.text = getTimeStringFromMillis(duration)
        }
    }

    private fun getCountDown(): CountDownTimer {
        return object: CountDownTimer(duration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timerTextView!!.text = getTimeStringFromMillis(millisUntilFinished)
                running = true
                if (millisUntilFinished < questionTime && !once) {
                    once = true
                    timerTextView!!.startAnimation(alphaAnimation(1500))
                    buzzer.play(71.toDouble(), 5000.0)
                }
            }

            override fun onFinish() {
                timerTextView!!.setTextColor(Color.RED)
                timerTextView!!.startAnimation(alphaAnimation(500))
                buzzer.play(71.toDouble(), 20000.0)
                running = false
                once = false
            }
        }
    }

    private fun alphaAnimation(duration: Long): AlphaAnimation {
        val anim = AlphaAnimation(0.0f, 1.0f)
        anim.duration = duration //You can manage the blinking time with this parameter
        anim.startOffset = 20
        anim.repeatMode = Animation.REVERSE
        anim.repeatCount = Animation.INFINITE
        return anim
    }

    private fun getTimeStringFromMillis(millisUntilFinished: Long): String {
        val secondsUntilFinished = millisUntilFinished / 1000
        val minutes = secondsUntilFinished / 60
        val seconds = secondsUntilFinished % 60
        return String.format("%d:%02d", minutes, seconds)
    }
}

