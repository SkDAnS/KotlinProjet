package org.example.view

import javax.swing.*
import java.awt.*
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.border.EmptyBorder

class HomeView(private val mainView: MainView) {
    private val panel = JPanel(BorderLayout())

    init {


        val label = JLabel().apply {
            horizontalAlignment = SwingConstants.CENTER
        }


        val imageURL = HomeView::class.java.classLoader.getResource("image/logopage.png")
            ?: throw IllegalArgumentException("Image not found!")

        val originalImage = ImageIcon(imageURL).image


        val initialScaleFactor = 1.2


        panel.addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent) {

                val panelWidth = panel.width
                val panelHeight = panel.height
                if (panelWidth > 0 && panelHeight > 0) {

                    val adjustedWidth = (panelWidth * initialScaleFactor).toInt()
                    val scaledHeight = (adjustedWidth.toDouble() / originalImage.getWidth(null) * originalImage.getHeight(null)).toInt()
                    val resizedImage = originalImage.getScaledInstance(adjustedWidth, scaledHeight, Image.SCALE_SMOOTH)
                    label.icon = ImageIcon(resizedImage)
                }
            }
        })






        val btnEnter = RoundedButton("Entrer").apply {
            preferredSize = Dimension(100, 50)
            font = Font("Arial", Font.BOLD, 16)
            foreground = Color.WHITE
            background = Color.GRAY
            border = BorderFactory.createLineBorder(Color.WHITE, 2)
            isFocusPainted = false

            addMouseListener(object : java.awt.event.MouseAdapter() {
                override fun mouseEntered(e: java.awt.event.MouseEvent) {
                    background = Color.DARK_GRAY
                }

                override fun mouseExited(e: java.awt.event.MouseEvent) {
                    background = Color.GRAY
                }
            })

            addActionListener {
                mainView.showSearch()
            }
        }

        // 🎨 Conteneur de boutons avec fond assorti
        val buttonPanel = JPanel().apply {
            background = Color.LIGHT_GRAY
            add(btnEnter)
        }

        // 🎨 Ajouter tous les composants au panneau principal
        panel.apply {
            background = Color.LIGHT_GRAY
            border = BorderFactory.createLineBorder(Color.DARK_GRAY, 10)

            add(label, BorderLayout.CENTER)
            add(buttonPanel, BorderLayout.SOUTH)
        }
    }


    class RoundedButton(text: String) : JButton(text) {
        init {
            preferredSize = Dimension(100, 50)
            font = Font("Arial", Font.BOLD, 16)
            foreground = Color.WHITE
            background = Color.GRAY
            isContentAreaFilled = false
            isFocusPainted = false
            border = EmptyBorder(10, 20, 10, 20)
        }

        override fun paintComponent(g: Graphics) {
            val g2 = g as Graphics2D
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

            g2.color = background
            g2.fillRoundRect(0, 0, width, height, 20, 20) // (x, y, width, height, arcWidth, arcHeight)

            super.paintComponent(g)
        }

        override fun paintBorder(g: Graphics) {
            val g2 = g as Graphics2D
            g2.color = Color.WHITE
            g2.drawRoundRect(0, 0, width - 1, height - 1, 20, 20)
        }
    }

    fun getPanel(): JPanel {
        return panel
    }
}
