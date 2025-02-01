package org.example.view

import MapView
import javax.swing.*
import java.awt.*

class MainView {
    private val frame = JFrame("Application Cartographique")
    private val cardLayout = CardLayout()
    private val panelContainer = JPanel(cardLayout)

    fun start() {
        SwingUtilities.invokeLater {
            frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            frame.layout = BorderLayout()
            frame.setSize(400, 800)
            frame.isResizable = false

            val homeView = HomeView(this)
            val searchView = SearchView(this)
            val mapView = MapView(this)

            panelContainer.add(homeView.getPanel(), "HOME")
            panelContainer.add(searchView.getPanel(), "SEARCH")
            panelContainer.add(mapView.getPanel(), "MAP")

            frame.add(panelContainer, BorderLayout.CENTER)

            showHome()
            frame.isVisible = true
        }
    }

    fun showHome() {
        cardLayout.show(panelContainer, "HOME")
    }

    fun showSearch() {
        cardLayout.show(panelContainer, "SEARCH")
    }

    fun showMap(startCity: String, endCity: String) {


        val mapView = MapView(this)
        mapView.updateCities(startCity, endCity)
        panelContainer.add(mapView.getPanel(), "MAP")
        cardLayout.show(panelContainer, "MAP")
    }
}
