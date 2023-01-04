package tech.touraine.timer

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.Button
import android.widget.TextView
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import tech.touraine.timer.data.Speaker
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

    private lateinit var timerTextView: TextView
    private lateinit var rooms: Rooms
    private lateinit var room: Array<Time>
    private var duration: Long = 10000
    private var questionTime: Long = 300000
    private val speakersMap = HashMap<String, Speaker>()
    private lateinit var countDownTimer: CountDownTimer

    private var running: Boolean = false
    private var once:Boolean = false
    private var currentTimeIndex: Int = 0
    private lateinit var currentRoomName: String
    private lateinit var currentDay: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_auto_timer)
        timerTextView = findViewById(R.id.timerTextView)
        findViewById<Button>(R.id.manualTimer).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        initResources()

        findViewById<TextView>(R.id.currentRoomTextView).text = getString(R.string.roomName, currentRoomName)

        currentTimeIndex = initCurrentTimeIndex() - 1
        startNextTimer()
    }

    private fun initCurrentTimeIndex(): Int {
        var index = 0
        val now = LocalTime.now()
        var parsed = LocalTime.parse(room[index].time, DateTimeFormatter.ofPattern("HH:mm"))
        val last = LocalTime.parse(room[room.size-1].time, DateTimeFormatter.ofPattern("HH:mm"))
        if (now.isAfter(last)) {
            return 1
        }
        while (parsed.isBefore(now)) {
            index++
            parsed = LocalTime.parse(room[index].time, DateTimeFormatter.ofPattern("HH:mm"))
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
        val currentTime = room[currentTimeIndex]
        timerTextView.setTextColor(if (currentTime.talk.id == "break") Color.GREEN else Color.parseColor("#808080"))

        val nextTalkTime = room[currentTimeIndex + 1]
        val nextTalk = nextTalkTime.talk

        findViewById<TextView>(R.id.nextTalkTextView).text = if (nextTalk.id != "break")
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
        currentTimeIndex++
    }

    private fun initResources() {
        currentRoomName = intent.getStringExtra("roomName") ?: "N/A"
        currentDay = intent.getStringExtra("day") ?: "N/A"
        val gson = GsonBuilder().setPrettyPrinting().create()
        val roomTimeTalks = resources.openRawResource(R.raw.room_time_talks).bufferedReader().use { it.readText() }
        val days = gson.fromJson(roomTimeTalks, Days::class.java)
        rooms = if ("day1" == currentDay) { days.day1 } else { days.day2 }

        when(currentRoomName) {
            "Turing" -> room = rooms.Turing
            "Pascal" -> room = rooms.Pascal
            "Lovelace" -> room = rooms.Lovelace
            "TD1" -> room = rooms.TD1
        }

        val speakerText = resources.openRawResource(R.raw.speakers).bufferedReader().use { it.readText() }
        val speakers: List<Speaker> = gson.fromJson(speakerText, object : TypeToken<List<Speaker>>() {}.type)
        speakers
            .filter { it.confirmed }
            .forEach { speakersMap[it.id] = it }
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

data class Days(val day1: Rooms, val day2: Rooms){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Days

        if (day1 != other.day1) return false
        if (day2 != other.day2) return false

        return true
    }

    override fun hashCode(): Int {
        var result = day1.hashCode()
        result = 31 * result + day2.hashCode()
        return result
    }
}


data class Rooms(val Turing: Array<Time>, val Pascal: Array<Time>, val Lovelace: Array<Time>, val TD1: Array<Time>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Rooms

        if (!Turing.contentEquals(other.Turing)) return false
        if (!Pascal.contentEquals(other.Pascal)) return false
        if (!Lovelace.contentEquals(other.Lovelace)) return false
        if (!TD1.contentEquals(other.TD1)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = Turing.contentHashCode()
        result = 31 * result + Pascal.contentHashCode()
        result = 31 * result + Lovelace.contentHashCode()
        return result
    }
}

data class Time(val time: String, val talk:Talk)

data class Talk(val id: String, val name: String, val speakers: Array<String>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Talk

        if (id != other.id) return false
        if (name != other.name) return false
        if (!speakers.contentEquals(other.speakers)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + speakers.contentHashCode()
        return result
    }
}