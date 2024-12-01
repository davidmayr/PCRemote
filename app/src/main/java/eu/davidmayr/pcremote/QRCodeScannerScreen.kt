package eu.davidmayr.pcremote

import android.Manifest
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRCodeScannerScreen(navController: NavController, webSocketViewModel: WebSocketViewModel) {
    val context = LocalContext.current
    val lifeCycleOwner = LocalLifecycleOwner.current

    var permissionError by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }

    PermissionRequestDialog(
        permission = Manifest.permission.CAMERA,
        onResult = { isGranted ->
            permissionError = !isGranted
        },
    )

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(),
                title = {
                    Text(context.getString(R.string.qr_code_title))
                },
            )
        },
        modifier = Modifier.fillMaxSize(),
        content = { padding ->
            Column(
                modifier = Modifier.padding(padding),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {

                if (!loading) {
                    if (permissionError) {
                        Text(context.getString(R.string.perm_missing))
                    } else {
                        Text(context.getString(R.string.qr_code_desc))

                        AndroidView(
                            factory = { context ->
                                val previewView = PreviewView(context)
                                val preview = Preview.Builder().build()
                                val cameraSelector = CameraSelector.Builder().build()

                                preview.surfaceProvider = previewView.surfaceProvider

                                val imageAnalysis = ImageAnalysis.Builder().build()

                                imageAnalysis.setAnalyzer(
                                    ContextCompat.getMainExecutor(context),
                                    BarcodeAnalyzer(context, response = {
                                        if(loading) return@BarcodeAnalyzer

                                        loading = true
                                        webSocketViewModel.connect(it.get("ip").asString, it.get("port").asInt, it.get("pw").asString)
                                        navController.navigate("home")
                                        loading = false
                                    })
                                )

                                ProcessCameraProvider.getInstance(context).get().bindToLifecycle(
                                    lifeCycleOwner,
                                    cameraSelector,
                                    preview,
                                    imageAnalysis
                                )

                                previewView
                            }
                        )
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth().fillMaxHeight()
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.width(64.dp),
                            color = MaterialTheme.colorScheme.secondary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        )
                        Text(context.getString(R.string.loading), modifier = Modifier.padding(top = 30.dp))
                    }

                }
            }
        }
    )
}
