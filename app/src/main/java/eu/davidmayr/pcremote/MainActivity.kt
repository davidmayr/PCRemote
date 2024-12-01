package eu.davidmayr.pcremote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import eu.davidmayr.pcremote.ui.theme.PCRemoteTheme


@AndroidEntryPoint
class MainActivity : ComponentActivity() {


    private val webSocketViewModel by viewModels<WebSocketViewModel>()

    //Needs to be stored here so we can disconnect with context
    private val sPenViewModel by viewModels<SPenViewModel>(factoryProducer = {
        SPenViewModel.SPenViewModelFactory(webSocketViewModel)
    })


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
fun Navigation(sPenViewModel: SPenViewModel, webSocketViewModel: WebSocketViewModel, modifier: Modifier = Modifier) {
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
fun PCRemote(navController: NavController, sPenViewModel: SPenViewModel, webSocketViewModel: WebSocketViewModel) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(),
                title = {
                    Text("PC-Remote")
                },
            )
        },
        modifier = Modifier.fillMaxSize(),
        content = { padding ->
            Column(
                modifier = Modifier.padding(padding),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp)
                ) {

                    Button(onClick = {
                        navController.navigate("scan")
                    }) {
                        Text("Scan Device Connection")
                    }


                    if(sPenViewModel.isSpenSupported) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {

                            Text("Use Samsung S-Pen")

                            Switch(sPenViewModel.connected, enabled = !sPenViewModel.isConnecting, onCheckedChange = {
                                sPenViewModel.toggleConnected(context)
                            })

                        }
                    }

                    if(webSocketViewModel.connected) {
                        Button(onClick = {
                            webSocketViewModel.closeConnection()
                        }) {
                            Text("Disconnect")
                        }
                    }
                    Text("Copyright 2024 - David Mayr")

                    // Error
                    Box(modifier = Modifier.fillMaxSize()) {
                        Text(
                            sPenViewModel.errorState,
                            color = Color.Red,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(8.dp)
                        )
                    }


                }
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