package org.example.view

import javax.swing.*
import java.awt.*

class SearchView(private val mainView: MainView) {
    private val panel = JPanel(BorderLayout())
    private val textFieldStart = JTextField(15)
    private val textFieldEnd = JTextField(15)

    // 🔥 Ajout des critères de recherche
    private val fuelTypeOptions = arrayOf("Tous", "Gazole", "SP95", "SP98", "E10", "E85", "GPLc")
    private val fuelTypeComboBox = JComboBox(fuelTypeOptions)

    private val checkBoxStore = JCheckBox("Boutique alimentaire")
    private val checkBoxToilets = JCheckBox("Toilettes")

    init {
        val label = JLabel("Entrez les villes :", SwingConstants.CENTER)
        label.font = Font("Arial", Font.BOLD, 16)

        val btnValidate = JButton("Valider")
        btnValidate.preferredSize = Dimension(100, 50)
        btnValidate.addActionListener {
            val startCity = textFieldStart.text
            val endCity = textFieldEnd.text
            val fuelType = fuelTypeComboBox.selectedItem as String
            val hasStore = checkBoxStore.isSelected
            val hasToilets = checkBoxToilets.isSelected

            if (startCity.isNotBlank() && endCity.isNotBlank()) {
                mainView.showMap(startCity, endCity, fuelType, hasStore, hasToilets)
            }
        }

        val inputPanel = JPanel()
        inputPanel.layout = GridLayout(5, 2, 5, 5)

        inputPanel.add(JLabel("Ville de départ :"))
        inputPanel.add(textFieldStart)

        inputPanel.add(JLabel("Ville d'arrivée :"))
        inputPanel.add(textFieldEnd)

        inputPanel.add(JLabel("Type de carburant :"))
        inputPanel.add(fuelTypeComboBox)

        inputPanel.add(JLabel("Critères supplémentaires :"))
        val checkBoxPanel = JPanel()
        checkBoxPanel.add(checkBoxStore)
        checkBoxPanel.add(checkBoxToilets)
        inputPanel.add(checkBoxPanel)

        inputPanel.add(JLabel("")) // Espace vide
        inputPanel.add(btnValidate)

        panel.add(label, BorderLayout.NORTH)
        panel.add(inputPanel, BorderLayout.CENTER)
    }

    fun getPanel(): JPanel {
        return panel
    }
}


