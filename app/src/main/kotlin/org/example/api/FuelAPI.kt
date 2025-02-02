package org.example.api

import com.github.kittinunf.fuel.httpGet
import org.example.model.FuelResponse
import org.example.model.FuelStation

fun fetchRecentStations(city: String): List<FuelStation> {
    val url = "https://public.opendatasoft.com/api/records/1.0/search/?dataset=prix-des-carburants-j-1&q=$city&rows=50&sort=update"
    val (_, _, result) = url.httpGet().responseObject(FuelStation.Deserializer())

    return result.fold(
        success = { it.records.map { record -> record.fields } },
        failure = { emptyList() }
    )
}
