package tech.touraine.timer

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Button
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
class RoomChoiceActivity : Activity() {

    private lateinit var buttons: Buttons
    private lateinit var buzzer: Buzzer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room_choice)

        buttons = Buttons()
        buzzer = Buzzer()

        findViewById<Button>(R.id.room1Btn).setOnClickListener {
            launchAutoTimerActivityWithRoomNumber("Turing")
        }
        findViewById<Button>(R.id.room2Btn).setOnClickListener {
            launchAutoTimerActivityWithRoomNumber("Pascal")
        }
        findViewById<Button>(R.id.room3Btn).setOnClickListener {
            launchAutoTimerActivityWithRoomNumber("LoveLace")
        }
        findViewById<Button>(R.id.room4Btn).setOnClickListener {
            launchAutoTimerActivityWithRoomNumber("TD1")
        }
        findViewById<Button>(R.id.room5Btn).setOnClickListener {
            launchAutoTimerActivityWithRoomNumber("TD2")
        }

    }

    private fun launchAutoTimerActivityWithRoomNumber(roomNumber: String) {
        arrayOf(buttons, buzzer).forEach(Closeable::close)
        val intent = Intent(this, AutoTimerActivity::class.java)
        intent.putExtra("roomName", roomNumber)
        startActivity(intent)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        buzzer.play(71.toDouble(), 300 * 0.8)
        return when (keyCode) {
            KeyEvent.KEYCODE_A -> {
                launchAutoTimerActivityWithRoomNumber("Turing")
                true
            }
            KeyEvent.KEYCODE_B -> {
                launchAutoTimerActivityWithRoomNumber("Pascal")
                true
            }
            KeyEvent.KEYCODE_C -> {
                launchAutoTimerActivityWithRoomNumber("LoveLace")
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }

}
