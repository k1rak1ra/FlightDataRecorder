import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import moe.tlaster.precompose.PreComposeApp
import net.k1ra.flight_data_recorder_dashboard.App
import net.k1ra.flight_data_recorder_dashboard.di.modules.ViewModelModule
import org.koin.core.context.startKoin

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    //Init Koin
    startKoin {
        modules(arrayListOf(
            ViewModelModule,
        ))
    }

    CanvasBasedWindow(canvasElementId = "ComposeTarget") { PreComposeApp { App() } }
}