package org.example.view

import org.openstreetmap.gui.jmapviewer.*
import java.awt.*

class MapPolyline(route: List<Coordinate>) : MapPolygonImpl(null, "", doubleRoute(route)) {
    init {
        color = Color.BLUE
        backColor = Color(255, 0, 0, 0) // Transparent
        stroke = BasicStroke(3.0f)
    }

    companion object {
        fun doubleRoute(route: List<Coordinate>): List<Coordinate> {
            if (route.size < 2) return route // Ne rien faire si moins de 2 points
            return route + route.reversed() // Aller + retour pour éviter une fermeture visible
        }
    }
}