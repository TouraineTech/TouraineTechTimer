package tech.touraine.timer

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.view.KeyEvent
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
//import com.google.android.things.device.TimeManager
import java.io.Closeable
import java.util.*
import android.content.ActivityNotFoundException

import android.content.ComponentName
import android.view.View
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.widget.TimePicker
import com.google.android.things.device.TimeManager
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime


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

    private lateinit var timerTextView: TextView
    private var duration: Long = DURATION_50_MINUTES
    private var questionTime: Long = QUESTION_TIME_50_MINUTES
    private lateinit var countDownTimer: CountDownTimer
    private var running: Boolean = false
    private var once: Boolean = false
    private lateinit var timeManager: TimeManager
//    private lateinit var buttons: Buttons
//    private lateinit var buzzer: Buzzer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        timeManager = TimeManager.getInstance()
        timeManager.setTimeFormat(TimeManager.FORMAT_24)
        timeManager.setTimeZone("Europe/Paris")


//        buttons = Buttons()
//        buzzer = Buzzer()
        setContentView(R.layout.activity_main)
        timerTextView = findViewById(R.id.timerTextView)
        timerTextView.text = getTimeStringFromMillis(duration)

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

        findViewById<View>(R.id.imageView).setOnClickListener {
            openSetupTimeModal()
        }

        val buttonMinus1Minutes: Button = findViewById(R.id.buttonMinus1Minutes)
        buttonMinus1Minutes.setOnClickListener { setTimerValues(duration - 60000, questionTime) }
        val buttonPlus1Minutes: Button = findViewById(R.id.buttonPlus1Minutes)
        buttonPlus1Minutes.setOnClickListener { setTimerValues(duration + 60000, questionTime) }

        findViewById<Button>(R.id.autoTimer).setOnClickListener {
            //arrayOf(buttons, buzzer).forEach(Closeable::close)
            startActivity(Intent(this, RoomChoiceActivity::class.java))
        }
    }

    private fun openSetupTimeModal() {
        val timeSetListener = OnTimeSetListener { view, hourOfDay, minute ->
            val localDateTime = LocalDateTime.of(2022, 1, 21, hourOfDay, minute)
            val zdt: ZonedDateTime = localDateTime.atZone(ZoneId.of("Europe/Paris"))
            timeManager.setTime(zdt.toInstant().toEpochMilli())
        }

        val timePickerDialog = TimePickerDialog(
            this, timeSetListener, 9, 0, true
        )
        timePickerDialog.show()
    }

    private fun openWifiSettings() {
        try {
            val intent = Intent(Intent.ACTION_MAIN, null)
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            val cn = ComponentName("com.android.settings", "com.android.settings.wifi.WifiSettings")
            intent.component = cn
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        } catch (ignored: ActivityNotFoundException) {
            startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //arrayOf(buttons, buzzer).forEach(Closeable::close)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        //    buzzer.play(71.toDouble(), 300 * 0.8)
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
            countDownTimer.cancel()
            timerTextView.clearAnimation()
            timerTextView.setTextColor(Color.parseColor("#808080"))
            timerTextView.text = getTimeStringFromMillis(duration)
            running = false
        } else {
            button1.text = getString(R.string.stop_Btn)
            countDownTimer = getCountDown()
            countDownTimer.start()
        }
    }

    private fun setTimerValues(_duration: Long, _questionTime: Long) {
        duration = _duration
        questionTime = _questionTime
        timerTextView.clearAnimation()
        if (!running) {
            timerTextView.text = getTimeStringFromMillis(duration)
        }
    }

    private fun getCountDown(): CountDownTimer {
        return object : CountDownTimer(duration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timerTextView.text = getTimeStringFromMillis(millisUntilFinished)
                running = true
                if (millisUntilFinished < questionTime && !once) {
                    once = true
                    timerTextView.startAnimation(alphaAnimation(1500))
                    // buzzer.play(71.toDouble(), 5000.0)
                }
            }

            override fun onFinish() {
                timerTextView.setTextColor(Color.RED)
                timerTextView.startAnimation(alphaAnimation(500))
                //buzzer.play(71.toDouble(), 20000.0)
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

