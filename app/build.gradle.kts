plugins {
    kotlin("jvm") version "1.9.0"
    application
}

group = "com.example"
version = "1.0"

repositories {
    mavenCentral()
    maven { url = uri("https://josm.openstreetmap.de/nexus/content/repositories/releases/") } // Dépôt JOSM pour JMapViewer
}

dependencies {



    // 🚀 Kotlin Standard Library
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.0")

    // 🌐 Fuel - HTTP Client (API requêtes HTTP)
    implementation("com.github.kittinunf.fuel:fuel:2.3.1")
    implementation("com.github.kittinunf.fuel:fuel-gson:2.3.1")

    // 📝 JSON Parsing (Deux options : org.json et Gson)
    implementation("org.json:json:20210307")
    implementation("com.google.code.gson:gson:2.9.0")

    // 📍 OpenStreetMap - JMapViewer (Version 2.22 confirmée disponible)
    implementation("org.openstreetmap.jmapviewer:jmapviewer:2.22")

    // 🛠️ Logging avec Log4j (Meilleur que println)
    implementation("org.apache.logging.log4j:log4j-core:2.17.2")
    implementation("org.apache.logging.log4j:log4j-api:2.17.2")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")


}

kotlin {
    jvmToolchain(17) // ✅ Utilisation de Java 17 (nécessaire pour Kotlin 1.9)
}

application {
    mainClass.set("com.example.MainKt") // ✅ Mets ici le bon package de ton fichier Main.kt
}
