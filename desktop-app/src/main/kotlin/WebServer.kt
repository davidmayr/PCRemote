import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.javalin.Javalin
import io.javalin.websocket.WsContext
import io.nayuki.qrcodegen.QrCode
import java.awt.Robot
import java.awt.event.KeyEvent
import java.awt.image.BufferedImage
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.*


fun generateAuthToken(length: Int = 16): String {
    val charPool = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    return (1..length)
        .map { charPool.random() }
        .joinToString("")
}

object WebServer {

    val robot = Robot()

    private var localIp: String = ""
    //Generate random secure token
    val password = generateAuthToken(24)

    private val authenticatedSessions = mutableListOf<WsContext>()

    val webServer = Javalin.create { config ->
        config.router.mount { router ->
            router.get("/") {
                println("Hi")
                it.result("Hello World")
            }
            router.ws("/") { ws ->
                ws.onError {
                    println("Err")
                }
                ws.onConnect { ctx ->
                    println("User joined")
                }
                ws.onClose { ctx ->
                    println("User left")
                    authenticatedSessions.remove(ctx)
                }
                ws.onMessage { ctx ->
                    if(!authenticatedSessions.contains(ctx)) {
                        if(ctx.message() == password) {
                            authenticatedSessions.add(ctx)
                            ctx.send("Authenticated")
                            println("New user authenticated")
                        } else {
                            ctx.send("Invalid password")
                            println("User failed to authenticate")
                        }
                    } else {

                        val jsonObject = JsonParser.parseString(ctx.message()).asJsonObject

                        if(jsonObject.get("type").asString == "m") {
                            LaserPointer.setVisibility(true)
                            LaserPointer.move(
                                jsonObject.get("x").asFloat,
                                jsonObject.get("y").asFloat
                            )
                        } else if(jsonObject.get("type").asString == "mr") {
                            LaserPointer.setVisibility(false)
                        } else {
                            val doubleClick = jsonObject.get("d").asBoolean
                            if(doubleClick) {

                                // Simulate pressing the "A" key
                                robot.keyPress(KeyEvent.VK_LEFT) // Press "A"
                                robot.keyRelease(KeyEvent.VK_LEFT) // Release "A"
                            } else {
                                robot.keyPress(KeyEvent.VK_RIGHT) // Press "A"
                                robot.keyRelease(KeyEvent.VK_RIGHT) // Release "A"
                            }
                        }

                        println("Message" + ctx.message())
                    }
                }
            }
        }
    }.start(54321)

    init {
        println("Web Server port: " +webServer.port())

        try {
            // Get the local host's InetAddress instance
            val localHost = InetAddress.getLocalHost()

            // Retrieve and print the IP address
            val ipAddress = localHost.hostAddress
            this.localIp = ipAddress
            println("Local IP Address: $ipAddress")
        } catch (e: UnknownHostException) {
            System.err.println("Unable to retrieve IP address: " + e.message)
        }
    }


    fun generateQRCodeForConnection(): BufferedImage {


        val json = JsonObject()
        json.addProperty("type", "pcmanager/remotconfig")
        json.addProperty("pw", password)
        json.addProperty("ip", localIp)
        json.addProperty("port", webServer.port())

        val qrCode = QrCode.encodeText(json.toString(), QrCode.Ecc.MEDIUM)
        val img: BufferedImage = toImage(qrCode, 10, 10)

        return img
    }

    private fun toImage(qr: QrCode, scale: Int, border: Int): BufferedImage {
        return toImage(qr, scale, border, 0xFFFFFF, 0x000000)
    }

    private fun toImage(qr: QrCode, scale: Int, border: Int, lightColor: Int, darkColor: Int): BufferedImage {
        require(!(scale <= 0 || border < 0)) { "Value out of range" }
        require(!(border > Int.MAX_VALUE / 2 || qr.size + border * 2L > Int.MAX_VALUE / scale)) { "Scale or border too large" }

        val result = BufferedImage(
            (qr.size + border * 2) * scale,
            (qr.size + border * 2) * scale,
            BufferedImage.TYPE_INT_RGB
        )
        for (y in 0..<result.height) {
            for (x in 0..<result.width) {
                val color = qr.getModule(x / scale - border, y / scale - border)
                result.setRGB(x, y, if (color) darkColor else lightColor)
            }
        }
        return result
    }
}