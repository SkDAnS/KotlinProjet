package org.example.model

import org.openstreetmap.gui.jmapviewer.Coordinate
import org.openstreetmap.gui.jmapviewer.JMapViewer
import java.awt.Point
import java.util.logging.Logger

class MapModel {
    private val logger: Logger = Logger.getLogger(MapModel::class.java.name)

    val mapViewer: JMapViewer = JMapViewer().apply {
        zoom = 3
        setTileSource(org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource.Mapnik())
        setDisplayPosition(Coordinate(48.8566, 2.3522), zoom) // Centre sur Paris
    }

    fun panMap(latChange: Double, lonChange: Double) {
        val currentPosition = mapViewer.position
        val newLat = currentPosition.lat + latChange
        val newLon = currentPosition.lon + lonChange
        mapViewer.setDisplayPosition(Coordinate(newLat, newLon), mapViewer.zoom)

        logger.info("Carte déplacée vers Lat: $newLat, Lon: $newLon")
    }

    fun moveMapByPixels(dx: Int, dy: Int) {
        val viewport = mapViewer.size
        val center = mapViewer.getPosition(Point(viewport.width / 2 + dx, viewport.height / 2 + dy))
        mapViewer.setDisplayPosition(center, mapViewer.zoom)

        logger.info("Carte déplacée de $dx pixels horizontalement et $dy pixels verticalement")
    }

    fun centerMap(lat: Double, lon: Double) {
        mapViewer.setDisplayPosition(Coordinate(lat, lon), 10) // Zoom 10 par défaut
        logger.info("Carte centrée sur : ($lat, $lon)")
    }






}

