package net.k1ra.flight_data_recorder_dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import org.jetbrains.compose.resources.ExperimentalResourceApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.compose.resources.painterResource
import flightdatarecorderproject.flightdatarecorderwebdashboard.generated.resources.Res
import flightdatarecorderproject.flightdatarecorderwebdashboard.generated.resources.error
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalResourceApi::class)
@Composable
fun EizoImageWeb(
    url: String,
    modifier: Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    contentDescription: String? = null,
    alignment: Alignment = Alignment.Center,
    alpha: Float = 1.0f,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = FilterQuality.Low,
    showProgressIndicator: Boolean = true,
    fallbackPainter: Painter = painterResource(Res.drawable.error),
    fallbackModifier: Modifier? = null
) {
    var bitmap by remember { mutableStateOf(null as ImageBitmap?) }
    var isLoading by remember { mutableStateOf(true) }
    var startedLoadingImage by remember { mutableStateOf(false) }

    if (!startedLoadingImage) {
        startedLoadingImage = true
        isLoading = true

        //TODO If image loading time is annoying, maybe use ktor client built in caching here?
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val bodyBytes: ByteArray = HttpClient().get(url).body()

                bitmap = org.jetbrains.skia.Image.makeFromEncoded(bodyBytes).toComposeImageBitmap()
                isLoading = false
            } catch (e: Exception) {
                isLoading = false
                println("ERROR: Failed to load image from $url because of $e")
            }
        }
    }

    Box(
        modifier = modifier.testTag("eizoBox"),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            if (showProgressIndicator) {
                CircularProgressIndicator(
                    modifier = Modifier.fillMaxSize(0.8f).testTag("eizoProgressIndicator")
                )
            }
        } else {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap!!,
                    contentDescription = contentDescription,
                    modifier = Modifier.fillMaxSize().testTag("eizoImageBitmap"),
                    contentScale = contentScale,
                    alignment = alignment,
                    alpha = alpha,
                    colorFilter = colorFilter,
                    filterQuality = filterQuality
                )
            } else {
                Image(
                    painter = fallbackPainter,
                    contentDescription = contentDescription,
                    modifier = (fallbackModifier ?: Modifier.fillMaxSize()).testTag("eizoFallbackImage"),
                    contentScale = contentScale,
                    alignment = alignment,
                    alpha = alpha,
                    colorFilter = colorFilter
                )
            }
        }
    }
}