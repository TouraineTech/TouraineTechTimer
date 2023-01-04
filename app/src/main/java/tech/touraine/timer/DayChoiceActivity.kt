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
class DayChoiceActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_day_choice)

        findViewById<Button>(R.id.day1Btn).setOnClickListener {
            launchRoomChoiceActivityWithDayNumber("day1")
        }
        findViewById<Button>(R.id.day2Btn).setOnClickListener {
            launchRoomChoiceActivityWithDayNumber("day2")
        }
    }

    private fun launchRoomChoiceActivityWithDayNumber(dayNumber: String) {
        val intent = Intent(this, RoomChoiceActivity::class.java)
        intent.putExtra("day", dayNumber)
        startActivity(intent)
    }
}
