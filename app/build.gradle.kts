plugins {
    kotlin("jvm") version "1.9.0"
    application
}

group = "com.example"
version = "1.0"

repositories {
    mavenCentral()
    maven { url = uri("https://josm.openstreetmap.de/nexus/content/repositories/releases/") }
}

dependencies {
    implementation("org.openstreetmap.jmapviewer:jmapviewer:2.22")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.0")
    implementation("org.json:json:20210307")


}

kotlin {
    jvmToolchain(17) // Utiliser Java 17 qui est 100% supporté par Kotlin
}


application {
    mainClass.set("MainKt")
}
