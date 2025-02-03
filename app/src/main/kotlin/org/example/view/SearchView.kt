package org.example.view

import javax.swing.*
import java.awt.*

class SearchView(private val mainView: MainView) {
    private val panel = JPanel(BorderLayout())

    private val textFieldStart = JTextField(12).apply {
        font = Font("Arial", Font.PLAIN, 16)
        border = BorderFactory.createLineBorder(Color.GRAY, 1)
        preferredSize = Dimension(200, 30)
    }

    private val textFieldEnd = JTextField(12).apply {
        font = Font("Arial", Font.PLAIN, 16)
        border = BorderFactory.createLineBorder(Color.GRAY, 1)
        preferredSize = Dimension(200, 30)
    }

    private val fuelTypeOptions = arrayOf("Tous", "Gazole", "SP95", "SP98", "E10", "E85", "GPLc")
    private val fuelTypeComboBox = JComboBox(fuelTypeOptions).apply {
        font = Font("Arial", Font.PLAIN, 16)
        border = BorderFactory.createLineBorder(Color.GRAY, 1)
        preferredSize = Dimension(200, 30)
    }

    private val checkBoxStore = JCheckBox("Boutique alimentaire").apply {
        font = Font("Arial", Font.PLAIN, 16)
        background = Color.LIGHT_GRAY
    }

    private val checkBoxToilets = JCheckBox("Toilettes").apply {
        font = Font("Arial", Font.PLAIN, 16)
        background = Color.LIGHT_GRAY
    }

    init {
        // 🎨 Titre imposant
        val label = JLabel("Rechercher un itinéraire").apply {
            font = Font("Arial", Font.BOLD, 36)
            horizontalAlignment = SwingConstants.CENTER
            border = BorderFactory.createEmptyBorder(20, 0, 20, 0)
            foreground = Color.BLACK
        }

        // 🎨 Bouton en gris avec style cohérent
        val btnValidate = JButton("Rechercher").apply {
            preferredSize = Dimension(140, 50)
            font = Font("Arial", Font.BOLD, 18)
            foreground = Color.WHITE
            background = Color.GRAY
            border = BorderFactory.createLineBorder(Color.WHITE, 2)
            isFocusPainted = false

            addMouseListener(object : java.awt.event.MouseAdapter() {
                override fun mouseEntered(e: java.awt.event.MouseEvent) {
                    background = Color.DARK_GRAY
                }

                override fun mouseExited(e: java.awt.event.MouseEvent) {
                    background = Color.GRAY
                }
            })

            addActionListener {
                val startCity = textFieldStart.text
                val endCity = textFieldEnd.text
                val fuelType = fuelTypeComboBox.selectedItem as String
                val hasStore = checkBoxStore.isSelected
                val hasToilets = checkBoxToilets.isSelected

                if (startCity.isNotBlank() && endCity.isNotBlank()) {
                    mainView.showMap(startCity, endCity, fuelType, hasStore, hasToilets)
                }
            }
        }

        // 🎨 Panneau pour les champs de saisie
        val inputPanel = JPanel().apply {
            layout = GridBagLayout()
            background = Color.LIGHT_GRAY
            border = BorderFactory.createEmptyBorder(20, 20, 20, 20)
        }

        val constraints = GridBagConstraints().apply {
            fill = GridBagConstraints.HORIZONTAL
            insets = Insets(20, 20, 20, 20) // Augmente l'espacement vertical
        }

        constraints.gridx = 0
        constraints.gridy = 0
        inputPanel.add(JLabel("Ville de départ :").apply {
            font = Font("Arial", Font.PLAIN, 16)
        }, constraints)

        constraints.gridx = 1
        inputPanel.add(textFieldStart, constraints)

        constraints.gridx = 0
        constraints.gridy = 1
        inputPanel.add(JLabel("Ville d'arrivée :").apply {
            font = Font("Arial", Font.PLAIN, 16)
        }, constraints)

        constraints.gridx = 1
        inputPanel.add(textFieldEnd, constraints)

        constraints.gridx = 0
        constraints.gridy = 2
        inputPanel.add(JLabel("Type de carburant :").apply {
            font = Font("Arial", Font.PLAIN, 16)
        }, constraints)

        constraints.gridx = 1
        inputPanel.add(fuelTypeComboBox, constraints)

        constraints.gridx = 0
        constraints.gridy = 3
        constraints.gridwidth = 2
        inputPanel.add(checkBoxStore, constraints)

        constraints.gridy = 4
        inputPanel.add(checkBoxToilets, constraints)


        // 🎨 Panneau pour le bouton de validation
        val buttonPanel = JPanel().apply {
            background = Color.LIGHT_GRAY
            add(btnValidate)
        }

        // 🎨 Ajouter tous les composants au panneau principal
        panel.apply {
            background = Color.LIGHT_GRAY
            border = BorderFactory.createLineBorder(Color.DARK_GRAY, 10)
            add(label, BorderLayout.NORTH)
            add(inputPanel, BorderLayout.CENTER)
            add(buttonPanel, BorderLayout.SOUTH)
        }
    }

    fun getPanel(): JPanel {
        return panel
    }
}



