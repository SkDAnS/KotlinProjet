package org.example.controller

import org.example.model.MapModel
import org.example.view.HomeView
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionListener
import java.util.logging.Logger
import org.openstreetmap.gui.jmapviewer.Coordinate
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject
import org.json.JSONArray
import java.awt.*
import javax.swing.*

class MapController(private val model: MapModel) {
    private val logger: Logger = Logger.getLogger(MapController::class.java.name)
    private val apiKey = "43a5fb5f-306c-4957-b1bd-b2c08e18dbc0"



    fun panMap(latChange: Double, lonChange: Double) {
        val currentPosition = model.mapViewer.position
        val newLat = currentPosition.lat + latChange
        val newLon = currentPosition.lon + lonChange
        model.mapViewer.setDisplayPosition(Coordinate(newLat, newLon), model.mapViewer.zoom)

        logger.info("Carte déplacée vers Lat: $newLat, Lon: $newLon")
    }

    fun createNavigationButtons(): JPanel {
        val controlPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            background = Color.LIGHT_GRAY
            border = BorderFactory.createEmptyBorder(0, 10, 0, 10)

            // Définir une taille carrée pour chaque bouton
            val buttonSize = Dimension(40, 40)

            val directions = listOf("↑", "←", "↓", "→")
            directions.forEach { dir ->
                val button = HomeView.RoundedButton(dir).apply {
                    preferredSize = buttonSize
                    minimumSize = buttonSize
                    maximumSize = buttonSize
                    font = Font("Arial", Font.BOLD, 20)
                    foreground = Color.WHITE
                    background = Color.GRAY
                    border = BorderFactory.createLineBorder(Color.WHITE, 2)
                    isFocusPainted = false

                    // Changement de couleur au survol
                    addMouseListener(object : java.awt.event.MouseAdapter() {
                        override fun mouseEntered(e: java.awt.event.MouseEvent) {
                            background = Color.DARK_GRAY
                        }

                        override fun mouseExited(e: java.awt.event.MouseEvent) {
                            background = Color.GRAY
                        }
                    })
                }

                // Ajouter l’action associée
                when (dir) {
                    "↑" -> button.addActionListener { panMap(0.5, 0.0) }
                    "←" -> button.addActionListener { panMap(0.0, -0.5) }
                    "↓" -> button.addActionListener { panMap(-0.5, 0.0) }
                    "→" -> button.addActionListener { panMap(0.0, 0.5) }
                }

                add(button)
                add(Box.createHorizontalStrut(5))
            }
        }

        return controlPanel
    }





    fun getCityCoordinates(city: String): Coordinate? {
        return try {
            val url = URL("https://nominatim.openstreetmap.org/search?format=json&q=$city,France")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "Mozilla/5.0")

            val response = connection.inputStream.bufferedReader().readText()
            val jsonArray = org.json.JSONArray(response)

            if (jsonArray.length() > 0) {
                val jsonObject = jsonArray.getJSONObject(0)
                val lat = jsonObject.getDouble("lat")
                val lon = jsonObject.getDouble("lon")
                logger.info("Ville trouvée : $city, Coordonnées : ($lat, $lon)")
                Coordinate(lat, lon)
            } else {
                logger.warning("Ville non trouvée : $city")
                null
            }
        } catch (e: Exception) {
            logger.severe("Erreur lors de la récupération des coordonnées : ${e.message}")
            null
        }
    }


    fun getRoute(start: Coordinate, end: Coordinate): List<Coordinate>? {
        return try {
            val url = URL("https://graphhopper.com/api/1/route?point=${start.lat},${start.lon}&point=${end.lat},${end.lon}&vehicle=car&key=$apiKey&instructions=false&points_encoded=false")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "Mozilla/5.0")

            val responseCode = connection.responseCode
            val response = connection.inputStream.bufferedReader().readText()

            if (responseCode != 200) {
                logger.severe("Erreur API GraphHopper : Code $responseCode, Réponse : $response")
                return null
            }

            val jsonObject = JSONObject(response)


            logger.info("Réponse complète de GraphHopper: $jsonObject")


            if (!jsonObject.has("paths")) {
                logger.severe("GraphHopper ne contient pas de 'paths'. Réponse : $jsonObject")
                return null
            }

            val pathsArray = jsonObject.getJSONArray("paths")
            if (pathsArray.length() == 0) {
                logger.warning("Aucun itinéraire trouvé.")
                return null
            }

            val coordinatesList = mutableListOf<Coordinate>()


            val pointsObject = pathsArray.getJSONObject(0).getJSONObject("points")
            val pointsArray = pointsObject.getJSONArray("coordinates")

            for (i in 0 until pointsArray.length()) {
                val coord = pointsArray.getJSONArray(i)
                val lon = coord.getDouble(0)
                val lat = coord.getDouble(1)
                coordinatesList.add(Coordinate(lat, lon))
            }

            logger.info("Itinéraire récupéré avec ${coordinatesList.size} points.")
            coordinatesList
        } catch (e: Exception) {
            logger.severe("Erreur lors de la récupération de l'itinéraire : ${e.message}")
            null
        }
    }

    fun getRouteInfo(start: Coordinate, end: Coordinate): Pair<Double, Double>? {
        return try {
            val apiKey = "43a5fb5f-306c-4957-b1bd-b2c08e18dbc0"
            val url = URL("https://graphhopper.com/api/1/route?point=${start.lat},${start.lon}&point=${end.lat},${end.lon}&vehicle=car&key=$apiKey&instructions=false&points_encoded=false")

            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "Mozilla/5.0")

            val responseCode = connection.responseCode
            val response = connection.inputStream.bufferedReader().use { it.readText() }

            if (responseCode != 200) {
                logger.severe("Erreur API GraphHopper : Code $responseCode, Réponse : $response")
                return null
            }

            val jsonObject = JSONObject(response)
            val pathsArray = jsonObject.getJSONArray("paths")
            if (pathsArray.length() == 0) {
                logger.warning("Aucun itinéraire trouvé.")
                return null
            }

            val pathObject = pathsArray.getJSONObject(0)
            val distance = pathObject.getDouble("distance") / 1000.0
            val time = pathObject.getDouble("time") / 1000.0 / 60.0

            logger.info("Distance: ${"%.2f".format(distance)} km, Durée: ${"%.2f".format(time)} min")
            return Pair(distance, time)
        } catch (e: Exception) {
            logger.severe("Erreur lors de la récupération de la distance et du temps : ${e.message}")
            null
        }
    }





}
