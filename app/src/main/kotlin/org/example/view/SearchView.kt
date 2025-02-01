package org.example.view

import javax.swing.*
import java.awt.*

class SearchView(private val mainView: MainView) {
    private val panel = JPanel(BorderLayout())
    private val textFieldStart = JTextField(15)
    private val textFieldEnd = JTextField(15)

    init {
        val label = JLabel("Entrez les villes :", SwingConstants.CENTER)
        label.font = Font("Arial", Font.BOLD, 16)

        val btnValidate = JButton("Valider")
        btnValidate.preferredSize = Dimension(100, 50)
        btnValidate.addActionListener {
            val startCity = textFieldStart.text
            val endCity = textFieldEnd.text
            if (startCity.isNotBlank() && endCity.isNotBlank()) {
                mainView.showMap(startCity, endCity) // Aller à la carte avec les 2 villes
            }
        }

        val inputPanel = JPanel()
        inputPanel.layout = GridLayout(3, 2, 5, 5)
        inputPanel.add(JLabel("Ville de départ :"))
        inputPanel.add(textFieldStart)
        inputPanel.add(JLabel("Ville d'arrivée :"))
        inputPanel.add(textFieldEnd)
        inputPanel.add(JLabel("")) // Espace vide
        inputPanel.add(btnValidate)

        panel.add(label, BorderLayout.NORTH)
        panel.add(inputPanel, BorderLayout.CENTER)
    }

    fun getPanel(): JPanel {
        return panel
    }
}

