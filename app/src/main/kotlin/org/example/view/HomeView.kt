package org.example.view

import javax.swing.*
import java.awt.*
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent

class HomeView(private val mainView: MainView) {
    private val panel = JPanel(BorderLayout())

    init {
        // 🎨 Créer un label pour afficher l'image
        val label = JLabel().apply {
            horizontalAlignment = SwingConstants.CENTER
        }

        // 🎨 Charger l'image depuis les ressources
        val imageURL = HomeView::class.java.classLoader.getResource("image/logopage.png")
            ?: throw IllegalArgumentException("Image not found!")

        val originalImage = ImageIcon(imageURL).image

        // 🎨 Définir une échelle initiale de 90%
        val initialScaleFactor = 0.8

        // 🎨 Ajouter un écouteur pour ajuster la taille de l'image lorsque le panel change de taille
        panel.addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent) {
                // Calculer la nouvelle largeur en tenant compte de l'échelle initiale
                val panelWidth = panel.width
                val panelHeight = panel.height
                if (panelWidth > 0 && panelHeight > 0) {
                    // Appliquer la réduction de 10% (90% de la largeur disponible)
                    val adjustedWidth = (panelWidth * initialScaleFactor).toInt()
                    val scaledHeight = (adjustedWidth.toDouble() / originalImage.getWidth(null) * originalImage.getHeight(null)).toInt()
                    val resizedImage = originalImage.getScaledInstance(adjustedWidth, scaledHeight, Image.SCALE_SMOOTH)
                    label.icon = ImageIcon(resizedImage)
                }
            }
        })

        // 🎨 Ajouter un titre en haut
        val title = JLabel("CarbuMap").apply {
            font = Font("Impact", Font.BOLD, 50)
            foreground = Color.BLACK
            horizontalAlignment = SwingConstants.CENTER
            border = BorderFactory.createEmptyBorder(10, 0, 10, 0)
        }

        // 🎨 Bouton en gris
        val btnEnter = JButton("Entrer").apply {
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
            add(title, BorderLayout.NORTH)
            add(label, BorderLayout.CENTER)
            add(buttonPanel, BorderLayout.SOUTH)
        }
    }

    fun getPanel(): JPanel {
        return panel
    }
}
