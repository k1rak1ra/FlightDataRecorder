import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    println(javaClass.packageName)

    Window(onCloseRequest = ::exitApplication, title = "FlightDataRecorder") {
        App()
    }
}