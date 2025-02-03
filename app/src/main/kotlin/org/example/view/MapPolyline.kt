package org.example.view

import org.openstreetmap.gui.jmapviewer.*
import java.awt.*

class MapPolyline(route: List<Coordinate>) : MapPolygonImpl(null, "", route) {
    init {
        color = Color.BLUE
        backColor = Color(255, 0, 0, 0)
        stroke = BasicStroke(3.0f)
    }

}
