package eu.davidmayr.pcremote

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.gson.JsonObject
import okhttp3.*

class WebSocketViewModel : ViewModel() {

    var connected by mutableStateOf(false)

    private var webSocket: WebSocket? = null
    private val client = OkHttpClient()

    private val listener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            super.onOpen(webSocket, response)
            println("WebSocket Connected")

            if(webSocket == this@WebSocketViewModel.webSocket) {
                connected = true
            }
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            super.onMessage(webSocket, text)
            println("Message received: $text")
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            super.onFailure(webSocket, t, response)
            println("Error: ${t.message}")

            if(webSocket == this@WebSocketViewModel.webSocket) {
                this@WebSocketViewModel.webSocket = null
                connected = false
            }
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosing(webSocket, code, reason)
            println("Closing WebSocket: $reason")
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosed(webSocket, code, reason)
            println("WebSocket Closed: $reason")
            if(webSocket == this@WebSocketViewModel.webSocket) {
                this@WebSocketViewModel.webSocket = null
                connected = false
            }

        }
    }

    fun connect(ip: String, port: Int, pw: String) {
        closeConnection()

        val request = Request.Builder().url("ws://$ip:$port").build()
        webSocket = client.newWebSocket(request, listener)
        webSocket?.send(pw)
        println("connected to ws://$ip:$port")
    }

    fun sendButtonClick(double: Boolean) {
        webSocket?.send(JsonObject().also {
            it.addProperty("type", "b")
            it.addProperty("d", double)
        }.toString())
    }

    fun sendMotion(deltaX: Float, deltaY: Float) {
        webSocket?.send(JsonObject().also {
            it.addProperty("type", "m")
            it.addProperty("x", deltaX)
            it.addProperty("y", deltaY)
        }.toString())
    }

    fun closeConnection() {
        webSocket?.close(1000, "Closing connection")
        connected = false
    }

    override fun onCleared() {
        super.onCleared()
        closeConnection()
    }

    fun sendReleased() {
        webSocket?.send(JsonObject().also {
            it.addProperty("type", "mr")
        }.toString())
    }
}