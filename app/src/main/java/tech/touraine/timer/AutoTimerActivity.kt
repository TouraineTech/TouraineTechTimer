package tech.touraine.timer

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.KeyEvent
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.Button
import android.widget.TextView
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.apache.commons.collections.map.MultiKeyMap
import tech.touraine.timer.data.Speaker
import tech.touraine.timer.data.Talk
import tech.touraine.timer.data.Times
import java.io.Closeable
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

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
class AutoTimerActivity : Activity() {

    private lateinit var buttons: Buttons
    private lateinit var buzzer: Buzzer

    private lateinit var timerTextView: TextView
    private lateinit var times: Times
    private var duration: Long = 10000
    private var questionTime: Long = 5000
    private val speakersMap = HashMap<String, Speaker>()
    private val talksMap: MultiKeyMap = MultiKeyMap()
    private lateinit var countDownTimer: CountDownTimer

    private var running: Boolean = false
    private var once:Boolean = false
    private var currentTimeIndex: Int = 0
    private var currentRoomIndex: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buttons = Buttons()
        buzzer = Buzzer()

        setContentView(R.layout.activity_auto_timer)
        timerTextView = findViewById(R.id.timerTextView)
        findViewById<Button>(R.id.manualTimer).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
        loadResources()
        currentRoomIndex = intent.getIntExtra("roomNumber", 1)

        findViewById<TextView>(R.id.currentRoomTextView).text = "Salle \n" + times.rooms[currentRoomIndex - 1]

        currentTimeIndex = initCurrentTimeIndex() - 1 // minus one because startNextTimer increment currentTimeIndex
        startNextTimer()
    }

    private fun initCurrentTimeIndex(): Int {
        var index = 0
        var parsed = LocalTime.parse(times.times[index].time, DateTimeFormatter.ofPattern("HH:mm"))
        val now = LocalTime.now()
        while (parsed.isBefore(now)) {
            index++
            parsed = LocalTime.parse(times.times[index].time, DateTimeFormatter.ofPattern("HH:mm"))
        }
        return index
    }

    private fun getCountDown(): CountDownTimer {
        return object: CountDownTimer(duration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timerTextView.text = getTimeStringFromMillis(millisUntilFinished)
                if (millisUntilFinished < questionTime && !once) {
                    once = true
                    timerTextView.startAnimation(alphaAnimation(1500))
                    buzzer.play(71.toDouble(), 5000.0)
                }
            }

            override fun onFinish() {
                timerTextView.setTextColor(Color.RED)
                timerTextView.clearAnimation()
                running = false
                once = false
                startNextTimer()
            }
        }
    }

    private fun startNextTimer() {
        val currentTalk = times.times[currentTimeIndex]
        timerTextView.setTextColor(if (currentTalk.talk) Color.parseColor("#808080") else Color.GREEN)

        currentTimeIndex++
        val nextTalkTime = times.times[currentTimeIndex]
        val nextTalk = talksMap[currentRoomIndex, currentTimeIndex +1]
        findViewById<TextView>(R.id.nextTalkTextView).text = if (nextTalk is Talk)
            (
                (getString(R.string.nextTalk) +
                    if (nextTalk.name.length > 55)
                    nextTalk.name.subSequence(0,55).toString() + "..."
                    else
                    nextTalk.name
                ) + " " + getString(R.string.bySpeaker) + " " +
                        nextTalk.speakers.map { speakersMap[it]!!.name }
            )
            else ""
        duration = LocalTime.now()
                .until(LocalTime.parse(nextTalkTime.time, DateTimeFormatter.ofPattern("HH:mm")),
                        ChronoUnit.MILLIS
                )
        countDownTimer = getCountDown()
        countDownTimer.start()
    }

    private fun loadResources() {
        val timesText = resources.openRawResource(R.raw.times).bufferedReader().use { it.readText() }
        val gson = GsonBuilder().setPrettyPrinting().create()
        times = gson.fromJson(timesText, Times::class.java)

        val talkText = resources.openRawResource(R.raw.talks).bufferedReader().use { it.readText() }
        val talks: List<Talk> = gson.fromJson(talkText, object : TypeToken<List<Talk>>() {}.type)
        talks
            .filter { !it.backup }
            .forEach { iterateOverRooms(it) }

        val speakerText = resources.openRawResource(R.raw.speakers).bufferedReader().use { it.readText() }
        val speakers: List<Speaker> = gson.fromJson(speakerText, object : TypeToken<List<Speaker>>() {}.type)
        speakers
            .filter { it.confirmed }
            .forEach { speakersMap[it.id] = it }
    }

    private fun iterateOverRooms(talk: Talk) {
        talk.rooms.forEach { iterateOverTimes(talk, it) }
    }

    private fun iterateOverTimes(talk: Talk, roomNumber: Int) {
        talk.times.forEach { talksMap.put(roomNumber, it, talk)}
    }

    override fun onDestroy() {
        super.onDestroy()
        arrayOf(buttons, buzzer).forEach(Closeable::close)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        buzzer.play(71.toDouble(), 300 * 0.8)
        return when (keyCode) {
            KeyEvent.KEYCODE_A -> {
                arrayOf(buttons, buzzer).forEach(Closeable::close)
                startActivity(Intent(this, MainActivity::class.java))
                true
            }
            else -> super.onKeyDown(keyCode, event)
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
