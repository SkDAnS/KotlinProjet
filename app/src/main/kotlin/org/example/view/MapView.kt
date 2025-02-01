import org.example.controller.MapController
import org.example.model.MapModel
import org.example.view.MainView
import org.example.view.MapPolyline
import org.openstreetmap.gui.jmapviewer.*
import javax.swing.*
import java.awt.*
import org.openstreetmap.gui.jmapviewer.MapMarkerDot
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent

class MapView(private val mainView: MainView) {
    private val model = MapModel()
    private val controller = MapController(model)
    private val panel = JPanel(BorderLayout())
    private val mapViewer = model.mapViewer

    init {
        val topPanel = JPanel(BorderLayout())
        val btnBack = JButton("← Retour")
        btnBack.preferredSize = Dimension(100, 30)
        btnBack.addActionListener {
            mainView.showSearch()
        }
        topPanel.add(btnBack, BorderLayout.WEST)

        val navButtons = controller.createNavigationButtons()
        topPanel.add(navButtons, BorderLayout.EAST)

        panel.add(topPanel, BorderLayout.NORTH)
        panel.add(mapViewer, BorderLayout.CENTER)

        val mouseDragHandler = controller.MouseDragHandler()
        mapViewer.addMouseListener(mouseDragHandler)
        mapViewer.addMouseMotionListener(mouseDragHandler)
    }

    fun updateCities(startCity: String, endCity: String) {
        val startCoord = controller.getCityCoordinates(startCity)
        val endCoord = controller.getCityCoordinates(endCity)

        if (startCoord != null && endCoord != null) {
            // Centrer la carte sur le point de départ
            model.centerMap(startCoord.lat, startCoord.lon)

            // Nettoyer la carte
            mapViewer.mapMarkerList.clear()
            mapViewer.mapPolygonList.clear()

            // Ajouter les marqueurs
            val startMarker = MapMarkerDot(startCity, startCoord)
            startMarker.backColor = Color.RED
            mapViewer.mapMarkerList.add(startMarker)

            val endMarker = MapMarkerDot(endCity, endCoord)
            endMarker.backColor = Color.RED
            mapViewer.mapMarkerList.add(endMarker)

            // Récupérer et afficher l'itinéraire
            val route = controller.getRoute(startCoord, endCoord)
            if (route != null && route.isNotEmpty()) {
                // Créer le polyline uniquement avec les points de l'itinéraire
                val polyline = MapPolyline(route)
                mapViewer.addMapPolygon(polyline)
            } else {
                JOptionPane.showMessageDialog(
                    null,
                    "Impossible de récupérer l'itinéraire.",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE
                )
            }
        } else {
            JOptionPane.showMessageDialog(
                null,
                "Une ou plusieurs villes sont introuvables.",
                "Erreur",
                JOptionPane.ERROR_MESSAGE
            )
        }
    }

    fun getPanel(): JPanel {
        return panel
    }
}