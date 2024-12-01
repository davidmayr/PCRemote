package eu.davidmayr.pcremote

import android.graphics.Paint
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import eu.davidmayr.pcremote.ui.theme.PCRemoteTheme

class MainActivity : ComponentActivity() {


    private val webSocketViewModel by viewModels<WebSocketViewModel>()

    //Needs to be stored here so we can disconnect with context
    private val sPenViewModel by viewModels<SPenViewModel>(factoryProducer = {
        SPenViewModel.SPenViewModelFactory(webSocketViewModel)
    })


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        sPenViewModel.connect(this)
        enableEdgeToEdge()
        setContent {
            PCRemoteTheme {
                Navigation(sPenViewModel, webSocketViewModel)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sPenViewModel.disconnect(this)
    }

}

@Composable
fun Navigation(
    sPenViewModel: SPenViewModel,
    webSocketViewModel: WebSocketViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    NavHost(navController, "home") {
        composable("home") {
            PCRemote(navController, sPenViewModel, webSocketViewModel)
        }
        composable("scan") {
            QRCodeScannerScreen(navController, webSocketViewModel)
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PCRemote(
    navController: NavController,
    sPenViewModel: SPenViewModel,
    webSocketViewModel: WebSocketViewModel
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(),
                title = {
                    Text(context.getString(R.string.app_name))
                },
            )
        },
        modifier = Modifier.fillMaxSize(),
        content = { padding ->
            Column(
                modifier = Modifier.padding(padding).fillMaxWidth().fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp)
                ) {

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 5.dp, bottom = 20.dp)
                    ) {
                        if (!sPenViewModel.isSpenSupported || sPenViewModel.errorState.isNotEmpty()) {
                            Icon(Icons.Default.Clear, "Failed", modifier = Modifier.padding(end = 10.dp))
                            Text(context.getString(R.string.spen_unsupported))
                        } else {
                            Icon(Icons.Default.CheckCircle, "Success", modifier = Modifier.padding(end = 10.dp))
                            Text(context.getString(R.string.spen_supported))
                        }
                    }



                    if(!webSocketViewModel.connected) {
                        Text(context.getString(R.string.same_network))
                        Button(onClick = {
                            navController.navigate("scan")
                        }, modifier = Modifier.padding(top = 10.dp)) {
                            Text(context.getString(R.string.scan_device))
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 5.dp, bottom = 20.dp)
                        ) {
                            Icon(Icons.Default.Check, "Connected", modifier = Modifier.padding(end = 10.dp))
                            Text(context.getString(R.string.connected))
                        }

                        Button(onClick = {
                            webSocketViewModel.closeConnection()
                        }) {
                            Text(context.getString(R.string.disconnect))
                        }
                    }
                }

                Text("Copyright 2024 - David Mayr")

            }
        }
    )

}

@Preview(showSystemUi = true)
@Composable
fun DefaultPreview() {
    val nav = rememberNavController()
    PCRemoteTheme {
        PCRemote(nav, SPenViewModel(WebSocketViewModel()), WebSocketViewModel())
    }
}