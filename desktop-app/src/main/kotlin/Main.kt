import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.unit.Density
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

@Composable
@Preview
fun App() {
    MaterialTheme {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text("Scan this code with your S Pen capable Smartphone")
            Image(WebServer.generateQRCodeForConnection().toComposeImageBitmap(),
                "QR Code")
        }

    }
}

fun main() = application {
    WebServer //init

    Window(
        title = "S Pen PC Remote",
        onCloseRequest = ::exitApplication,
        icon = loadSvgPainter(WebServer.javaClass.getResourceAsStream("/icon.svg")!!, density = Density(1.0F, 1.0F))
    ) {
        App()
    }
}
