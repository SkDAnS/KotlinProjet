package org.example.api

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson
import org.example.model.FuelStation
import org.json.JSONArray
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.File
import java.io.FileInputStream
import java.net.URLEncoder
import java.util.zip.ZipInputStream
import javax.xml.parsers.DocumentBuilderFactory

// Initialisation du logger
private val logger = org.apache.logging.log4j.LogManager.getLogger("FuelAPI")

// Modèle principal pour Opendatasoft
data class FuelResponse(val nhits: Int, val records: List<Record>)
data class Record(val fields: FuelStation)



fun fetchRecentStations(city: String): List<FuelStation> {
    val url = "https://public.opendatasoft.com/api/records/1.0/search/?dataset=prix-des-carburants-j-1&q=$city&rows=50&sort=update"
    val (_, _, result) = url.httpGet().responseObject(FuelStation.Deserializer())

    return result.fold(
        success = { it.records.map { record ->
            val station = record.fields
            station
        } },
        failure = {
            logger.error("Échec de récupération depuis Opendatasoft, tentative avec Roulez-Éco...")
            fetchBackupStations(city)
        }
    )
}

fun fetchBackupStations(city: String): List<FuelStation> {
    val backupUrl = "https://donnees.roulez-eco.fr/opendata/jour"
    val (_, _, result) = backupUrl.httpGet().response()

    return result.fold(
        success = { responseData ->
            val tempZipFile = File.createTempFile("roulez-eco", ".zip")
            tempZipFile.writeBytes(responseData)

            val extractedXml = extractXmlFromZip(tempZipFile)
            if (extractedXml != null) {
                return parseXmlStations(extractedXml, city)
            }

            logger.error("Impossible d'extraire le fichier XML du ZIP.")
            emptyList()
        },
        failure = {
            logger.error("Échec de récupération depuis Roulez-Éco. Aucune donnée disponible.")
            emptyList()
        }
    )
}

// Fonction pour extraire le fichier XML du ZIP
fun extractXmlFromZip(zipFile: File): File? {
    ZipInputStream(FileInputStream(zipFile)).use { zipInputStream ->
        var entry = zipInputStream.nextEntry
        while (entry != null) {
            if (entry.name.endsWith(".xml")) {
                logger.info("Extraction du fichier XML : ${entry.name}")

                val extractedFile = File.createTempFile("roulez-eco", ".xml")
                extractedFile.outputStream().use { it.write(zipInputStream.readBytes()) }
                zipInputStream.closeEntry()
                return extractedFile
            }
            entry = zipInputStream.nextEntry
        }
    }
    return null
}

// Fonction pour parser le fichier XML
fun parseXmlStations(xmlFile: File, city: String): List<FuelStation> {
    val stations = mutableListOf<FuelStation>()

    // Parser le fichier XML
    val factory = DocumentBuilderFactory.newInstance()
    val builder = factory.newDocumentBuilder()
    val document: Document = builder.parse(xmlFile)
    document.documentElement.normalize()

    // Récupérer les noeuds <pdv> (chaque station)
    val pdvList = document.getElementsByTagName("pdv")

    for (i in 0 until pdvList.length) {
        val pdv = pdvList.item(i) as Element
        val ville = getTagValue("ville", pdv)

        // Vérifier que la ville correspond exactement
        if (ville?.uppercase() == city.uppercase()) {
            val adresse = getTagValue("adresse", pdv)
            val latitude = pdv.getAttribute("latitude").toDoubleOrNull()?.div(100000)
            val longitude = pdv.getAttribute("longitude").toDoubleOrNull()?.div(100000)

            // Récupération des prix
            val priceMap = mutableMapOf<String, Double?>()
            val prixList = pdv.getElementsByTagName("prix")
            for (j in 0 until prixList.length) {
                val prixNode = prixList.item(j) as Element
                val carburant = prixNode.getAttribute("nom")
                val valeur = prixNode.getAttribute("valeur").toDoubleOrNull()
                if (carburant.isNotEmpty()) {
                    priceMap[carburant] = valeur
                }
            }

            // Récupération des services
            val servicesList = mutableListOf<String>()
            val servicesNodes = pdv.getElementsByTagName("service")
            for (j in 0 until servicesNodes.length) {
                servicesList.add(servicesNodes.item(j).textContent)
            }

            stations.add(
                FuelStation(
                    name = "Station",
                    address = adresse,
                    com_arm_name = ville,
                    price_gazole = priceMap["Gazole"],
                    price_sp95 = priceMap["SP95"],
                    price_sp98 = priceMap["SP98"],
                    price_e10 = priceMap["E10"],
                    price_e85 = priceMap["E85"],
                    price_gplc = priceMap["GPLc"],
                    services = servicesList.joinToString(", "),
                    geo_point = if (latitude != null && longitude != null) listOf(latitude, longitude) else null
                )
            )
        }
    }

    return stations
}



fun getCoordinatesFromAddress(address: String?, city: String?): List<Double>? {
    if (address.isNullOrBlank() || city.isNullOrBlank()) return null

    val formattedAddress = URLEncoder.encode("$address, $city, France", "UTF-8")
    val url = "https://nominatim.openstreetmap.org/search?format=jsonv2&limit=1&addressdetails=0&q=$formattedAddress"


    val (_, _, result) = url.httpGet().responseString()

    return result.fold(
        success = { responseBody ->
            val jsonArray = JSONArray(responseBody)
            if (jsonArray.length() > 0) {
                val jsonObject = jsonArray.getJSONObject(0)
                val lat = jsonObject.getDouble("lat")
                val lon = jsonObject.getDouble("lon")
                listOf(lat, lon)
            } else {
                null
            }
        },
        failure = {
            logger.error("Impossible d'obtenir les coordonnées pour l'adresse : $address, $city")
            null
        }
    )
}


// Fonction pour extraire la valeur d'un tag XML
fun getTagValue(tag: String, element: Element): String? {
    val nodeList = element.getElementsByTagName(tag)
    return if (nodeList.length > 0) nodeList.item(0).textContent else null
}
