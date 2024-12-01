import java.awt.Color
import java.awt.Graphics
import java.awt.GraphicsEnvironment
import java.awt.Rectangle
import java.awt.Window
import javax.swing.JComponent
import javax.swing.JFrame


object LaserPointer {

    val laserFrame = JFrame()

    init {

        laserFrame.isUndecorated = true
        laserFrame.opacity = 0.5f
        laserFrame.background = Color(0, 0, 0, 0)
        laserFrame.type = Window.Type.UTILITY
        laserFrame.focusableWindowState = false
        laserFrame.isAlwaysOnTop = true

        laserFrame.setSize(20, 20)
        laserFrame.add(object : JComponent() {
            override fun paintComponent(g: Graphics) {
                super.paintComponent(g)
                g.color = Color.RED // Laser color
                g.fillOval(0, 0, 20, 20) // Draw a small circle as laser
            }
        })

        setVisibility(false)
    }

    private fun resetPos() {
        val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
        val screens = ge.screenDevices


        // Check if a secondary screen is available
        if (screens.size > 1) {
            val secondaryScreen = screens[1] // Get the secondary screen
            val bounds: Rectangle = secondaryScreen.defaultConfiguration.bounds

            // Calculate center position
            val x: Int = bounds.x + (bounds.width - laserFrame.width) / 2
            val y: Int = bounds.y + (bounds.height - laserFrame.height) / 2

            // Set location to center on secondary screen
            laserFrame.setLocation(x, y)
        } else {
            // Default to centering on primary screen
            laserFrame.setLocationRelativeTo(null)
        }

    }

    fun setVisibility(visible: Boolean) {
        if(laserFrame.isVisible != visible) {
            resetPos()
        }
        laserFrame.isVisible = visible
    }

    fun move(x: Float, y: Float) {
        val point = laserFrame.location
        laserFrame.setLocation(
            point.x + (x * 1000).toInt(),
            point.y + (-y * 1000).toInt()
        )
    }

}
