package org.example.view

import org.example.controller.MapController
import org.example.model.MapModel
import org.openstreetmap.gui.jmapviewer.Coordinate
import org.openstreetmap.gui.jmapviewer.MapMarkerDot
import java.awt.*
import javax.swing.*
import javax.swing.table.DefaultTableModel
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionListener
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.example.api.fetchRecentStations
import org.example.view.MainView
import org.example.view.MapPolyline
import kotlin.math.*

class MapView(private val mainView: MainView) {
    private val model = MapModel()
    private val controller = MapController(model)
    private val panel = JPanel(BorderLayout())
    private val mapViewer = model.mapViewer
    private val stationTableModel = DefaultTableModel()
    private val stationTable = JTable(stationTableModel)
    private val logger: Logger = LogManager.getLogger(MapView::class.java)

    private var lastMousePoint: Point? = null
    private var startCityCoord: Coordinate? = null
    private var endCityCoord: Coordinate? = null
    private var mainRoute: List<Coordinate>? = null // Stocke l'itinéraire principal
    private val infoLabel = JLabel("Distance: -- km | Temps: -- min", SwingConstants.CENTER)
    private val scrollPane = JScrollPane(stationTable)

    init {
        val topPanel = JPanel(BorderLayout())

        val btnBack = JButton("← Retour")
        btnBack.preferredSize = Dimension(100, 30)
        btnBack.addActionListener { mainView.showSearch() }
        topPanel.add(btnBack, BorderLayout.WEST) // Bouton retour à gauche

        val navButtons = controller.createNavigationButtons()
        topPanel.add(navButtons, BorderLayout.EAST) // Boutons de navigation à droite

        panel.add(topPanel, BorderLayout.NORTH) // `topPanel` en haut
        panel.add(mapViewer, BorderLayout.CENTER) // Carte au centre

        // 📌 Création d'un panel vertical pour contenir `infoLabel` et `scrollPane`
        val bottomContainer = JPanel()
        bottomContainer.layout = BoxLayout(bottomContainer, BoxLayout.Y_AXIS) // Affichage en colonne
        bottomContainer.add(infoLabel) // Ajout du `infoLabel`
        bottomContainer.add(scrollPane) // Ajout du tableau des stations

        panel.add(bottomContainer, BorderLayout.SOUTH) // Ajout en bas sous la carte







    // Déplacement de la carte avec la souris
        mapViewer.addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                lastMousePoint = e.point
            }

            override fun mouseReleased(e: MouseEvent) {
                val clickedMarker = mapViewer.mapMarkerList.find { marker ->
                    val markerPos = mapViewer.getMapPosition(marker.coordinate)
                    markerPos != null && e.point.distance(markerPos) < 10
                }

                clickedMarker?.let {
                    when (it.backColor) {
                        Color.GREEN -> {
                            logger.info("Station cliquée : ${it.name}")
                            startCityCoord?.let { startCoord ->
                                showRouteToStation(startCoord, it.coordinate)
                            }
                        }
                        Color.RED -> {
                            logger.info("Ville cliquée : ${it.name}")
                            restoreMainRoute()
                        }

                        else -> {}
                    }
                }
            }
        })

        mapViewer.addMouseMotionListener(object : MouseMotionListener {
            override fun mouseDragged(e: MouseEvent) {
                lastMousePoint?.let {
                    val dx = it.x - e.x
                    val dy = it.y - e.y
                    model.moveMapByPixels(dx, dy)
                }
                lastMousePoint = e.point
            }

            override fun mouseMoved(e: MouseEvent) {}
        })
    }

    fun updateCities(startCity: String, endCity: String, fuelType: String, hasStore: Boolean, hasToilets: Boolean) {
        val startCoord = controller.getCityCoordinates(startCity)
        val endCoord = controller.getCityCoordinates(endCity)



        if (startCoord == null || endCoord == null) {
            JOptionPane.showMessageDialog(null, "Une ou plusieurs villes sont introuvables.", "Erreur", JOptionPane.ERROR_MESSAGE)
            return
        }



        startCityCoord = startCoord
        endCityCoord = endCoord

        val routeInfo = controller.getRouteInfo(startCoord, endCoord)
        val distanceKm = routeInfo?.first ?: 0.0
        val estimatedTimeMin = routeInfo?.second ?: 0.0

// Mettre à jour le texte du label
        infoLabel.text = "Distance: ${"%.2f".format(distanceKm)} km | Temps: ${"%.2f".format(estimatedTimeMin)} min"



        val route = controller.getRoute(startCoord, endCoord)
        if (route.isNullOrEmpty()) {
            JOptionPane.showMessageDialog(null, "Impossible de récupérer l'itinéraire.", "Erreur", JOptionPane.ERROR_MESSAGE)
            return
        }

        mainRoute = route // Stocke l'itinéraire principal

        val allStations = (fetchRecentStations(startCity) + fetchRecentStations(endCity))
            .distinctBy { "${it.address}-${it.com_arm_name}" }

        val filteredStations = allStations.filter { station ->
            val fuelMatch = when (fuelType) {
                "Tous" -> true
                "Gazole" -> station.price_gazole != null
                "SP95" -> station.price_sp95 != null
                "SP98" -> station.price_sp98 != null
                "E10" -> station.price_e10 != null
                "E85" -> station.price_e85 != null
                "GPLc" -> station.price_gplc != null
                else -> false
            }

            val storeMatch = !hasStore || (station.services?.contains("Boutique", ignoreCase = true) == true)
            val toiletMatch = !hasToilets || (station.services?.contains("Toilettes", ignoreCase = true) == true)

            val nearRouteOrCities = station.geo_point?.let {
                isNearRouteOrCities(it, route, startCoord, endCoord, 50.0)
            } ?: false

            // L'instruction finale assure que toutes les conditions sélectionnées sont respectées simultanément
            fuelMatch && storeMatch && toiletMatch && nearRouteOrCities
        }


        val columns = arrayOf("Nom", "Adresse", "Ville", "Prix Gazole", "Prix SP95", "Prix SP98", "Prix E10", "Prix E85", "Prix GPLc")
        val data = filteredStations.map {
            arrayOf(
                it.name,
                it.address,
                it.com_arm_name,
                multiplyPrice(it.price_gazole),
                multiplyPrice(it.price_sp95),
                multiplyPrice(it.price_sp98),
                multiplyPrice(it.price_e10),
                multiplyPrice(it.price_e85),
                multiplyPrice(it.price_gplc)
            )
        }.toTypedArray()

        stationTableModel.setDataVector(data, columns)


        mapViewer.mapMarkerList.clear()

        for (station in filteredStations) {
            station.geo_point?.let { coords ->
                if (coords.size >= 2) {
                    val stationMarker = MapMarkerDot(station.name ?: "Station", Coordinate(coords[0], coords[1]))
                    stationMarker.backColor = Color.GREEN
                    mapViewer.mapMarkerList.add(stationMarker)
                }
            }
        }

        model.centerMap(startCoord.lat, startCoord.lon)
        mapViewer.mapMarkerList.add(MapMarkerDot(startCity, startCoord).apply { backColor = Color.RED })
        mapViewer.mapMarkerList.add(MapMarkerDot(endCity, endCoord).apply { backColor = Color.RED })

        val polyline = MapPolyline(route)
        mapViewer.addMapPolygon(polyline)
    }

    private fun showRouteToStation(startCoord: Coordinate, stationCoord: Coordinate) {
        logger.info("Calcul de l'itinéraire de ${startCoord} vers la station ${stationCoord}")

        val routeToStation = controller.getRoute(startCoord, stationCoord)
        if (routeToStation.isNullOrEmpty()) {
            JOptionPane.showMessageDialog(null, "Impossible de récupérer l'itinéraire vers cette station.", "Erreur", JOptionPane.ERROR_MESSAGE)
            return
        }

        mapViewer.mapPolygonList.clear()
        val polyline = MapPolyline(routeToStation)
        polyline.color = Color.BLUE
        mapViewer.addMapPolygon(polyline)
    }

    private fun restoreMainRoute() {
        if (mainRoute == null) return

        logger.info("Restauration de l'itinéraire principal")

        mapViewer.mapPolygonList.clear()
        val polyline = MapPolyline(mainRoute!!)
        polyline.color = Color.BLUE
        mapViewer.addMapPolygon(polyline)
    }

    private fun isNearRouteOrCities(stationCoords: List<Double>, route: List<Coordinate>, startCoord: Coordinate, endCoord: Coordinate, radiusKm: Double): Boolean {
        return route.any { haversineDistance(stationCoords[0], stationCoords[1], it.lat, it.lon) <= radiusKm } ||
                haversineDistance(stationCoords[0], stationCoords[1], startCoord.lat, startCoord.lon) <= radiusKm ||
                haversineDistance(stationCoords[0], stationCoords[1], endCoord.lat, endCoord.lon) <= radiusKm
    }

    private fun haversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371.0 // Rayon de la Terre en km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c // Retourne la distance en kilomètres
    }

    private fun multiplyPrice(price: Any?): String {
        return when (price) {
            is String -> price.toDoubleOrNull()?.times(1000)?.let { "%.3f €".format(it) } ?: "N/A"
            is Double -> "%.3f €".format(price * 1000)
            is Number -> "%.3f €".format(price.toDouble() * 1000)
            else -> "N/A"
        }
    }



    fun getPanel(): JPanel = panel
}
