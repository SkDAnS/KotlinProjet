package org.example.view

import kotlinx.coroutines.*
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
import org.example.api.getCoordinatesFromAddress
import org.example.view.MainView
import org.example.view.MapPolyline
import java.util.concurrent.Semaphore
import kotlin.math.*

class MapView(private val mainView: MainView) {
    private val model = MapModel()
    private val controller = MapController(model)
    private val panel = JPanel(BorderLayout()).apply {
        background = Color.LIGHT_GRAY
        border = BorderFactory.createLineBorder(Color.DARK_GRAY, 10)
    }
    private val mapViewer = model.mapViewer
    private val stationTableModel = DefaultTableModel()
    private val stationTable = JTable(stationTableModel).apply {
        rowHeight = 20
        background = Color.LIGHT_GRAY
        foreground = Color.BLACK
        gridColor = Color.DARK_GRAY
    }




    private val logger: Logger = LogManager.getLogger(MapView::class.java)

    private var lastMousePoint: Point? = null
    private var startCityCoord: Coordinate? = null
    private var endCityCoord: Coordinate? = null
    private var mainRoute: List<Coordinate>? = null
    private val infoLabel = JLabel("\t Distance: -- km | Temps: -- min \t", SwingConstants.CENTER).apply {
        font = Font("Impact", Font.ROMAN_BASELINE, 20)
        foreground = Color.BLACK
        border = BorderFactory.createEmptyBorder(10, 100, 10, 100)
        background = Color.LIGHT_GRAY
        isOpaque = true

    }
    private val scrollPane = JScrollPane(stationTable).apply {
        background = Color.LIGHT_GRAY
        border = BorderFactory.createLineBorder(Color.DARK_GRAY)
        preferredSize = Dimension(900, 200)
    }


    init {
        val topPanel = JPanel(BorderLayout()).apply {
            background = Color.LIGHT_GRAY
            border = BorderFactory.createEmptyBorder(10, 10, 10, 10)


            val btnBack = HomeView.RoundedButton("← Retour").apply {
                preferredSize = Dimension(150, 40)
                font = Font("Arial", Font.BOLD, 14)
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
            }
            btnBack.addActionListener { mainView.showSearch() }


            val backContainer = JPanel(BorderLayout()).apply {
                background = Color.LIGHT_GRAY
                add(btnBack, BorderLayout.WEST)
            }
            add(backContainer, BorderLayout.WEST)


            val controller = MapController(model)
            val navButtons = controller.createNavigationButtons()



            add(navButtons, BorderLayout.EAST)


            val infoLabelContainer = JPanel().apply {
                background = Color.LIGHT_GRAY
                layout = BorderLayout()
                add(infoLabel, BorderLayout.CENTER)
            }
            add(infoLabelContainer, BorderLayout.CENTER)
        }

        panel.add(topPanel, BorderLayout.NORTH)

        val mapPanel = JPanel(BorderLayout()).apply {
            background = Color.LIGHT_GRAY
            border = BorderFactory.createLineBorder(Color.DARK_GRAY)
            preferredSize = Dimension(900, 1100)
            add(mapViewer, BorderLayout.CENTER)
        }

        panel.add(mapPanel, BorderLayout.CENTER)

        val bottomContainer = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            background = Color.LIGHT_GRAY
            border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
            add(infoLabel)
            add(Box.createRigidArea(Dimension(0, 10)))
            add(scrollPane)
        }
        panel.add(bottomContainer, BorderLayout.SOUTH)







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

        val hours = (estimatedTimeMin / 60).toInt()
        val minutes = (estimatedTimeMin % 60).toInt()
        infoLabel.text = "Distance : ${"%.2f".format(distanceKm)} km | Temps : ${hours}h ${minutes}min"



        val route = controller.getRoute(startCoord, endCoord)
        if (route.isNullOrEmpty()) {
            JOptionPane.showMessageDialog(null, "Impossible de récupérer l'itinéraire.", "Erreur", JOptionPane.ERROR_MESSAGE)
            return
        }

        mainRoute = route // stocker l'itinéraire principal

        var allStations = (fetchRecentStations(startCity) + fetchRecentStations(endCity))
            .distinctBy { "${it.address}-${it.com_arm_name}" }
        val stationsSansCoord = allStations.filter { it.geo_point == null }
        logger.info(" ${stationsSansCoord.size} stations sans coordonnées trouvées")

        if (stationsSansCoord.isNotEmpty()) {
            val semaphore = Semaphore(3)

            runBlocking {
                supervisorScope {
                    stationsSansCoord.chunked(3).forEach { chunk ->
                        val jobs = chunk.map { station ->
                            async(Dispatchers.IO) {
                                semaphore.acquire()
                                try {

                                    val coords = getCoordinatesFromAddress(station.address, station.com_arm_name)
                                    if (coords != null) {
                                        station.geo_point = coords

                                    } else {
                                        logger.warn(" Aucune donnée pour : ${station.address}, ${station.com_arm_name}")
                                    }
                                } finally {
                                    semaphore.release()
                                }
                            }
                        }
                        jobs.awaitAll()
                    }
                }
            }

        }

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

            val storeMatch = if (!hasStore || (station.services?.contains("Boutique", ignoreCase = true) == true)) "✔" else "✖"
            val toiletMatch = if (!hasToilets || (station.services?.contains("Toilettes", ignoreCase = true) == true)) "✔" else "✖"

            val nearRouteOrCities = station.geo_point?.let {
                isNearRouteOrCities(it, route, startCoord, endCoord, 50.0)
            } ?: false

            // verifie les conditions
            fuelMatch && (storeMatch == "✔") && (toiletMatch == "✔") && nearRouteOrCities
        }



        val columns = arrayOf(
            "Nom", "Adresse", "Ville", "Prix Gazole", "Prix SP95", "Prix SP98",
            "Prix E10", "Prix E85", "Prix GPLc", "Toilettes", "Magasin"
        )

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
                multiplyPrice(it.price_gplc),
                if (it.services?.contains("Toilettes", ignoreCase = true) == true) "✔" else "✖",
                if (it.services?.contains("Boutique", ignoreCase = true) == true) "✔" else "✖"
            )
        }.toTypedArray()

        stationTableModel.setDataVector(data, columns)





        mapViewer.mapMarkerList.clear()

        for (station in filteredStations) {
            station.geo_point?.let { coords ->
                if (coords.size >= 2) {
                    val stationMarker = MapMarkerDot(station.name ?: station.address, Coordinate(coords[0], coords[1]))
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
