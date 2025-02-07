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
            if (route.size < 2) return route
            return route + route.reversed()
        }
    }
}