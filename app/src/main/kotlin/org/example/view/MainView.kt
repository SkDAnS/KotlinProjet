package org.example.view

import javax.swing.*
import java.awt.*
import javax.swing.border.EmptyBorder

class MainView {
    private val frame = JFrame("CarbuMap")
    private val cardLayout = CardLayout()
    private val panelContainer = JPanel(cardLayout)
    private val menuPanel = JPanel(GridLayout(4, 1, 5, 5)) // Menu latéral
    private val mainPanel = JPanel(BorderLayout())

    private var menuVisible = false
    private val menuButton = JButton("MENU")

    // Couleur pour mode sombre
    private val darkGray = Color(64, 64, 64)
    private val white = Color.WHITE

    fun start() {
        SwingUtilities.invokeLater {
            frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            frame.layout = BorderLayout()
            frame.setSize(600, 800)
            frame.isResizable = false
            frame.background = darkGray // Fond sombrezz

            val homeView = HomeView(this)
            val searchView = SearchView(this)
            val mapView = MapView(this)

            panelContainer.add(homeView.getPanel(), "HOME")
            panelContainer.add(searchView.getPanel(), "SEARCH")
            panelContainer.add(mapView.getPanel(), "MAP")

            setupMenu()

            mainPanel.background = darkGray // Fond sombre
            mainPanel.add(menuButton, BorderLayout.NORTH)
            mainPanel.add(panelContainer, BorderLayout.CENTER)

            frame.add(menuPanel, BorderLayout.WEST)
            frame.add(mainPanel, BorderLayout.CENTER)

            showHome()
            frame.isVisible = true
        }
    }

    private fun setupMenu() {
        menuPanel.background = darkGray // Fond sombre du menu
        menuPanel.border = EmptyBorder(20, 10, 20, 10)
        menuPanel.layout = BoxLayout(menuPanel, BoxLayout.Y_AXIS)
        menuPanel.isVisible = false

        val buttonHeight = 40
        val buttonWidth = 180

        val homeButton = createMenuButton("Accueil") { showHome() }
        val searchButton = createMenuButton("Recherche") { showSearch() }
        val mapButton = createMenuButton("Carte") { showMap("Paris", "Lyon", "Tous", false, false) }

        listOf(homeButton, searchButton, mapButton).forEach { button ->
            button.preferredSize = Dimension(buttonWidth, buttonHeight)
            button.minimumSize = Dimension(buttonWidth, buttonHeight)
            button.maximumSize = Dimension(buttonWidth, buttonHeight)
            button.foreground = white
            button.background = darkGray // Fond gris foncé
            button.isFocusPainted = false
            button.border = BorderFactory.createLineBorder(white, 1) // Bordure blanche
            button.margin = Insets(10, 20, 10, 20)

            // Effet au survol
            button.addMouseListener(object : java.awt.event.MouseAdapter() {
                override fun mouseEntered(e: java.awt.event.MouseEvent) {
                    button.background = Color(96, 96, 96) // Gris légèrement plus clair
                }

                override fun mouseExited(e: java.awt.event.MouseEvent) {
                    button.background = darkGray // Retour au gris foncé
                }
            })

            menuPanel.add(Box.createRigidArea(Dimension(0, 10))) // Espacement entre boutons
            menuPanel.add(button)
        }

        // Menu principal (bouton d'ouverture)
        menuButton.font = Font("Arial", Font.BOLD, 18)
        menuButton.isFocusPainted = false
        menuButton.foreground = white
        menuButton.background = darkGray
        menuButton.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

        // Effet au survol du bouton menu
        menuButton.addMouseListener(object : java.awt.event.MouseAdapter() {
            override fun mouseEntered(e: java.awt.event.MouseEvent) {
                menuButton.background = Color(96, 96, 96)
            }

            override fun mouseExited(e: java.awt.event.MouseEvent) {
                menuButton.background = darkGray
            }
        })

        menuButton.addActionListener { toggleMenu() }
    }


    private fun createMenuButton(text: String, action: () -> Unit): JButton {
        val button = JButton(text)
        button.font = Font("Arial", Font.BOLD, 14)
        button.isFocusPainted = false

        // 🎨 Couleurs mises à jour
        button.foreground = Color(255, 255, 255) // Texte blanc
        button.background = Color(75, 0, 130) // Violet foncé
        button.border = BorderFactory.createLineBorder(Color.WHITE, 2) // Bordure blanche

        // 🎨 Effet au survol
        button.addMouseListener(object : java.awt.event.MouseAdapter() {
            override fun mouseEntered(e: java.awt.event.MouseEvent) {
                button.background = Color(0, 51, 102) // Bleu foncé au survol
            }

            override fun mouseExited(e: java.awt.event.MouseEvent) {
                button.background = Color(75, 0, 130) // Retour au violet foncé
            }
        })

        // Action à effectuer au clic
        button.addActionListener {
            action()
            toggleMenu() // Ferme le menu après un clic
        }

        return button
    }


    private fun toggleMenu() {
        menuVisible = !menuVisible
        menuPanel.isVisible = menuVisible
        frame.revalidate()
        frame.repaint()
    }

    fun showHome() {
        cardLayout.show(panelContainer, "HOME")
    }

    fun showSearch() {
        cardLayout.show(panelContainer, "SEARCH")
    }

    fun showMap(startCity: String, endCity: String, fuelType: String, hasStore: Boolean, hasToilets: Boolean) {
        val mapView = MapView(this)
        mapView.updateCities(startCity, endCity, fuelType, hasStore, hasToilets)
        panelContainer.add(mapView.getPanel(), "MAP")
        cardLayout.show(panelContainer, "MAP")
    }
}




