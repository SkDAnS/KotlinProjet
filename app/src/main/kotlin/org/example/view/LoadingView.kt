package org.example.view

import javax.swing.*
import java.awt.*

class LoadingView {
    private val panel = JPanel(BorderLayout())

    init {
        val label = JLabel("Chargement en cours...", SwingConstants.CENTER).apply {
            font = Font("Arial", Font.BOLD, 24)
            foreground = Color.BLACK
        }

        val progressBar = JProgressBar().apply {
            isIndeterminate = true
            preferredSize = Dimension(200, 30)
        }

        val loadingPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            background = Color.LIGHT_GRAY
            add(Box.createVerticalStrut(100))
            add(label)
            add(Box.createVerticalStrut(20))
            add(progressBar)
        }

        panel.apply {
            background = Color.LIGHT_GRAY
            add(loadingPanel, BorderLayout.CENTER)
        }
    }

    fun getPanel(): JPanel = panel
}
