package org.example.view

import javax.swing.*
import java.awt.*
import javax.swing.border.AbstractBorder
import javax.swing.border.EmptyBorder

class MainView {
    private val frame = JFrame("CarbuMap")
    private val cardLayout = CardLayout()
    private val panelContainer = JPanel(cardLayout)
    private val menuPanel = JPanel(GridLayout(4, 1, 5, 5)) // Menu latéral
    private val mainPanel = JPanel(BorderLayout())

    private var menuVisible = false
    private val menuButton = JButton("MENU")


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
            val loadingView = LoadingView()


            panelContainer.add(homeView.getPanel(), "HOME")
            panelContainer.add(searchView.getPanel(), "SEARCH")
            panelContainer.add(mapView.getPanel(), "MAP")
            panelContainer.add(loadingView.getPanel(), "LOADING")

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
        menuPanel.background = darkGray
        menuPanel.border = EmptyBorder(20, 10, 20, 10)
        menuPanel.layout = BoxLayout(menuPanel, BoxLayout.Y_AXIS)
        menuPanel.isVisible = false

        val buttonHeight = 40
        val buttonWidth = 180

        val homeButton = createMenuButton("Accueil") { showHome() }
        val searchButton = createMenuButton("Recherche") { showSearch() }
        val mapButton = createMenuButton("Carte") { showMap("", "", "Tous", false, false) }

        listOf(homeButton, searchButton, mapButton).forEach { button ->
            button.preferredSize = Dimension(buttonWidth, buttonHeight)
            button.minimumSize = Dimension(buttonWidth, buttonHeight)
            button.maximumSize = Dimension(buttonWidth, buttonHeight)
            button.foreground = white
            button.background = darkGray
            button.isFocusPainted = false
            button.border = BorderFactory.createLineBorder(white, 1)
            button.margin = Insets(10, 20, 10, 20)

            // Effet au survol
            button.addMouseListener(object : java.awt.event.MouseAdapter() {
                override fun mouseEntered(e: java.awt.event.MouseEvent) {
                    button.background = Color(96, 96, 96)
                }

                override fun mouseExited(e: java.awt.event.MouseEvent) {
                    button.background = darkGray
                }
            })

            menuPanel.add(Box.createRigidArea(Dimension(0, 10)))
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


    // Bordure Bouton arrondi
    inner class RoundedBorder(private val radius: Int, private val borderColor: Color) : AbstractBorder() {
        override fun paintBorder(c: Component, g: Graphics, x: Int, y: Int, width: Int, height: Int) {
            val g2 = g as Graphics2D
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

            g2.color = borderColor
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius)
        }
    }

    // Bouton Arrondi
    inner class RoundedButton(text: String, private val bgColor: Color, private val hoverColor: Color, private val action: () -> Unit) : JButton(text) {
        init {
            font = Font("Arial", Font.BOLD, 14)
            foreground = Color.WHITE
            background = bgColor
            isContentAreaFilled = false
            isFocusPainted = false
            border = RoundedBorder(20, Color.WHITE)

            addMouseListener(object : java.awt.event.MouseAdapter() {
                override fun mouseEntered(e: java.awt.event.MouseEvent) {
                    background = hoverColor
                    repaint()
                }

                override fun mouseExited(e: java.awt.event.MouseEvent) {
                    background = bgColor
                    repaint()
                }
            })


            addActionListener {
                action()
                toggleMenu()
            }
        }

        override fun paintComponent(g: Graphics) {
            val g2 = g as Graphics2D
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

            g2.color = background
            g2.fillRoundRect(0, 0, width, height, 20, 20)

            super.paintComponent(g)
        }

        override fun paintBorder(g: Graphics) {
            val g2 = g as Graphics2D
            g2.color = Color.WHITE
            g2.drawRoundRect(0, 0, width - 1, height - 1, 20, 20)
        }
    }

    private fun createMenuButton(text: String, action: () -> Unit): JButton {
        return RoundedButton(text, Color(75, 0, 130), Color(0, 51, 102), action)
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
    fun showLoading() {
        cardLayout.show(panelContainer, "LOADING")
    }

    fun showMap(startCity: String, endCity: String, fuelType: String, hasStore: Boolean, hasToilets: Boolean) {
        showLoading()

        SwingUtilities.invokeLater {
            val mapView = MapView(this)
            mapView.updateCities(startCity, endCity, fuelType, hasStore, hasToilets)
            panelContainer.add(mapView.getPanel(), "MAP")
            cardLayout.show(panelContainer, "MAP")
        }
    }


}




