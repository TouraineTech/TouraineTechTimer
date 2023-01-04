package tech.touraine.timer

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button

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

    private lateinit var currentDay: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        currentDay = intent.getStringExtra("day") ?: "N/A"

        setContentView(R.layout.activity_room_choice)

        findViewById<Button>(R.id.room1Btn).setOnClickListener {
            launchAutoTimerActivityWithRoomNumber("Turing")
        }
        findViewById<Button>(R.id.room2Btn).setOnClickListener {
            launchAutoTimerActivityWithRoomNumber("Pascal")
        }
        findViewById<Button>(R.id.room3Btn).setOnClickListener {
            launchAutoTimerActivityWithRoomNumber("Lovelace")
        }
        findViewById<Button>(R.id.room4Btn).setOnClickListener {
            launchAutoTimerActivityWithRoomNumber("TD1")
        }
    }

    private fun launchAutoTimerActivityWithRoomNumber(roomNumber: String) {
        val intent = Intent(this, AutoTimerActivity::class.java)
        intent.putExtra("roomName", roomNumber)
        intent.putExtra("day", currentDay)
        startActivity(intent)
    }

}
