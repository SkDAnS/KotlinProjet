package org.example.model

import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson

data class FuelResponse(val nhits: Int, val records: List<Record>)
data class Record(val fields: FuelStation)

data class FuelStation(
    val name: String?,
    val address: String?,
    val com_arm_name: String?,
    val price_gazole: Double?,
    val price_sp95: Double?,
    val price_sp98: Double?,
    val price_e10: Double?,
    val price_e85: Double?,
    val price_gplc: Double?,
    val services: String?,
    var geo_point: List<Double>?
) {
    class Deserializer : ResponseDeserializable<FuelResponse> {
        override fun deserialize(content: String): FuelResponse = Gson().fromJson(content, FuelResponse::class.java)
    }
}
