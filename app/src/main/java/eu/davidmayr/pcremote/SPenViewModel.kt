package eu.davidmayr.pcremote

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.samsung.android.sdk.penremote.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

const val doubleClickTime: Long = 250


class SPenViewModel(
    val webSocketViewModel: WebSocketViewModel
): ViewModel() {

    private var manager: SpenUnitManager? = null

    var isConnecting by mutableStateOf(false)
    var connected by mutableStateOf(false)
    var errorState by mutableStateOf("")

    private var buttonPressedSince: Long by mutableLongStateOf(-1)
    private var buttonUpLastTime: Long by mutableLongStateOf(-1)

    var buttonPressed: Boolean by mutableStateOf(false)

    val isSpenSupported = SpenRemote.getInstance().isFeatureEnabled(SpenRemote.FEATURE_TYPE_BUTTON)
            || SpenRemote.getInstance().isFeatureEnabled(SpenRemote.FEATURE_TYPE_AIR_MOTION)


    private val isLaserPointerActive: Boolean
        get() {
            return buttonPressed && System.currentTimeMillis() - buttonPressedSince > doubleClickTime*2
        }

    fun toggleConnected(context: Context) {
        if(SpenRemote.getInstance().isConnected) {
            disconnect(context)
        } else {
            connect(context)
        }
    }


    private fun connect(context: Context) {
        isConnecting = true
        try {

            SpenRemote.getInstance().connect(context, object : SpenRemote.ConnectionResultCallback {

                override fun onSuccess(spenUnitManager: SpenUnitManager?) {
                    isConnecting = false
                    manager = spenUnitManager
                    connected = true
                    errorState = ""
                    listenEvents()
                }

                override fun onFailure(error: Int) {
                    connected = false
                    isConnecting = false
                    errorState = when (error) {
                        SpenRemote.Error.UNSUPPORTED_DEVICE -> "Unsupported"
                        SpenRemote.Error.CONNECTION_FAILED -> "Failed"
                        else -> "Error"
                    }
                }

            })

        } catch (e: NoClassDefFoundError) {
            isConnecting = false
            errorState = "Unsupported"
        }
    }

    fun disconnect(context: Context) {

        manager?.getUnit(SpenUnit.TYPE_BUTTON)?.let { button ->
            manager?.unregisterSpenEventListener(button)
        }
        manager?.getUnit(SpenUnit.TYPE_AIR_MOTION)?.let { airMotion ->
            manager?.unregisterSpenEventListener(airMotion)
        }

        SpenRemote.getInstance().disconnect(context)

        connected = false
        errorState = ""
    }

    private fun listenEvents() {

        val sPen = SpenRemote.getInstance()

        // Button
        if (sPen.isFeatureEnabled(SpenRemote.FEATURE_TYPE_BUTTON)) {
            manager?.registerSpenEventListener({ event ->

                when (ButtonEvent(event).action) {
                    ButtonEvent.ACTION_DOWN ->{
                        buttonPressed = true
                        buttonPressedSince = System.currentTimeMillis()
                    }
                    ButtonEvent.ACTION_UP -> {
                        val time = System.currentTimeMillis()

                        if(!isLaserPointerActive) {
                            println(buttonUpLastTime - System.currentTimeMillis())
                            if(System.currentTimeMillis()-buttonUpLastTime > doubleClickTime) {

                                viewModelScope.launch {
                                    delay(doubleClickTime)
                                    if (buttonUpLastTime != time) {
                                        webSocketViewModel.sendButtonClick(true)
                                    } else {
                                        webSocketViewModel.sendButtonClick(false)
                                    }
                                }
                            }
                        } else {
                            webSocketViewModel.sendReleased()
                        }

                        buttonUpLastTime = time
                        buttonPressedSince = -1
                        buttonPressed = false
                    }
                }

            }, manager?.getUnit(SpenUnit.TYPE_BUTTON))
        }

        // Air motion
        if (sPen.isFeatureEnabled(SpenRemote.FEATURE_TYPE_AIR_MOTION)) {
            manager?.registerSpenEventListener({ event ->

                if (isLaserPointerActive) {

                    val airMotionEvent = AirMotionEvent(event)
                    val deltaX = airMotionEvent.deltaX
                    val deltaY = airMotionEvent.deltaY

                    if((deltaX < 0.0000000000001 && deltaX > -0.0000000000001) || (deltaY < 0.0000000000001 && deltaY
                                > -0.0000000000001))
                        return@registerSpenEventListener

                    webSocketViewModel.sendMotion(deltaX, deltaY)
                }

            }, manager?.getUnit(SpenUnit.TYPE_AIR_MOTION))
        }
    }

    class SPenViewModelFactory(
        private val webSocketViewModel: WebSocketViewModel
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SPenViewModel::class.java)) {
                return SPenViewModel(webSocketViewModel) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }


}