import java.awt.Color
import java.awt.Graphics
import java.awt.Window
import java.awt.event.ActionEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JComponent
import javax.swing.JFrame
import javax.swing.Timer

object LaserPointer {

    val laserFrame = JFrame()

    init {

        laserFrame.isUndecorated = true
        laserFrame.opacity = 0.5f
        laserFrame.background = Color(0, 0, 0, 0)
        laserFrame.type = Window.Type.UTILITY
        laserFrame.setSize(10, 10)
        laserFrame.setLocationRelativeTo(null)
        laserFrame.isAlwaysOnTop = true

        laserFrame.add(object : JComponent() {
            override fun paintComponent(g: Graphics) {
                super.paintComponent(g)
                g.color = Color.RED // Laser color
                g.fillOval(0, 0, 10, 10) // Draw a small circle as laser
            }
        })

        setVisibility(false)
    }

    fun setVisibility(visible: Boolean) {
        laserFrame.isVisible = visible
        if(!visible) {
            laserFrame.setLocationRelativeTo(null)
        }
    }

    fun move(x: Float, y: Float) {
        val point = laserFrame.location
        laserFrame.setLocation(
            point.x + (x * 1000).toInt(),
            point.y + (-y * 1000).toInt()
        )
    }

}
