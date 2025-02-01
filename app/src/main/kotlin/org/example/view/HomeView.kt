package org.example.view

import javax.swing.*
import java.awt.*

class HomeView(private val mainView: MainView) {
    private val panel = JPanel(BorderLayout())

    init {
        val label = JLabel("Bienvenue sur l'application", SwingConstants.CENTER)
        label.font = Font("Arial", Font.BOLD, 20)

        val btnEnter = JButton("Entrer")
        btnEnter.preferredSize = Dimension(100, 50)
        btnEnter.addActionListener {
            mainView.showSearch() // Aller à la page de recherche
        }

        val buttonPanel = JPanel()
        buttonPanel.add(btnEnter)

        panel.add(label, BorderLayout.CENTER)
        panel.add(buttonPanel, BorderLayout.SOUTH)
    }

    fun getPanel(): JPanel {
        return panel
    }
}
