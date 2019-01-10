package tech.touraine.timer

import android.view.KeyEvent
import com.google.android.things.contrib.driver.button.Button
import com.google.android.things.contrib.driver.button.ButtonInputDriver
import java.io.Closeable

class Buttons(private val buttonDrivers: List<ButtonInputDriver> = listOf(
        registerButtonDriver(Buttons.BUTTON_A_GPIO_PIN, KeyEvent.KEYCODE_A),
        registerButtonDriver(Buttons.BUTTON_B_GPIO_PIN, KeyEvent.KEYCODE_B),
        registerButtonDriver(Buttons.BUTTON_C_GPIO_PIN, KeyEvent.KEYCODE_C))) : Closeable {

    companion object {

        const val BUTTON_A_GPIO_PIN = "GPIO6_IO14"
        const val BUTTON_B_GPIO_PIN = "GPIO6_IO15"
        const val BUTTON_C_GPIO_PIN = "GPIO2_IO07"

        private fun registerButtonDriver(pin: String, keycode: Int): ButtonInputDriver {
            val driver = ButtonInputDriver(pin, Button.LogicState.PRESSED_WHEN_LOW, keycode)
            driver.register()
            return driver
        }
    }

    override fun close() {
        buttonDrivers.forEach(ButtonInputDriver::close)
    }
}
