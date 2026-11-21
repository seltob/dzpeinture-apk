package com.dzpeinture.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

data class PainterReview(
    val id: String,
    val author: String,
    val rating: Int,
    val comment: String,
    val createdAt: String
)

data class PainterProfile(
    val id: String,
    val name: String,
    val commune: String,
    val phone: String,
    val specialty: String,
    val yearsExp: Int,
    val verified: Boolean,
    val avgRating: Double,
    val reviewsCount: Int,
    val reviews: List<PainterReview>
)

object PaintersRepository {
    private const val BASE_URL = "https://dzp-facebook-agen.onrender.com"

    val fallbackPainters = listOf(
        PainterProfile(
            id = "p-karim-elachour",
            name = "Karim Benali",
            commune = "El Achour / Oued Romane",
            phone = "0555095686",
            specialty = "Peinture Intérieure • Satiné & Décoration",
            yearsExp = 12,
            verified = true,
            avgRating = 5.0,
            reviewsCount = 2,
            reviews = listOf(
                PainterReview("rev-1", "Sofiane M.", 5, "Travail très propre sur un F4 complet avec SOIE SATINÉ Vanilla Latte. Finitions impeccables.", "2026-11-20"),
                PainterReview("rev-2", "Nabil K.", 5, "Sérieux, ponctuel et très bon conseil pour les couleurs DZpeinture.", "2026-11-21")
            )
        ),
        PainterProfile(
            id = "p-yacine-cheraga",
            name = "Yacine Mansouri",
            commune = "Chéraga",
            phone = "0555095686",
            specialty = "Enduit Pro • Velours Mat & Appartements Neufs",
            yearsExp = 9,
            verified = true,
            avgRating = 5.0,
            reviewsCount = 1,
            reviews = listOf(
                PainterReview("rev-3", "Amine B.", 5, "Super résultat sur plâtre neuf avec FIXATEUR + VELOURS MAT. Zéro trace !", "2026-11-19")
            )
        ),
        PainterProfile(
            id = "p-mourad-bachdjerrah",
            name = "Mourad Haddad",
            commune = "Bachdjerrah",
            phone = "0555095686",
            specialty = "Façades Villas • Imper Façade & Étanchéité",
            yearsExp = 15,
            verified = true,
            avgRating = 5.0,
            reviewsCount = 1,
            reviews = listOf(
                PainterReview("rev-4", "Farid D.", 5, "Façade villa R+2 rénovée avec GRIP FOND + IMPER FAÇADE 20 kg. Résultat magnifique !", "2026-11-18")
            )
        )
    )

    suspend fun fetchPainters(): Result<List<PainterProfile>> = withContext(Dispatchers.IO) {
        runCatching {
            val conn = (URL("$BASE_URL/api/painters").openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10000
                readTimeout = 10000
            }
            val code = conn.responseCode
            val body = (if (code in 200..299) conn.inputStream else conn.errorStream)
                ?.bufferedReader()?.use { it.readText() } ?: ""
            conn.disconnect()
            if (code !in 200..299) error("Erreur serveur ($code)")
            val json = JSONObject(body)
            parsePaintersArray(json.optJSONArray("painters") ?: JSONArray())
        }
    }

    suspend fun submitReview(
        painterId: String,
        author: String,
        rating: Int,
        comment: String
    ): Result<PainterProfile> = withContext(Dispatchers.IO) {
        runCatching {
            val payload = JSONObject().apply {
                put("painterId", painterId)
                put("author", author)
                put("rating", rating)
                put("comment", comment)
            }
            val responseJson = postJson("$BASE_URL/api/painters/review", payload)
            parsePainter(responseJson.getJSONObject("painter"))
        }
    }

    suspend fun registerPainter(
        name: String,
        commune: String,
        phone: String,
        specialty: String,
        yearsExp: Int
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val payload = JSONObject().apply {
                put("name", name)
                put("commune", commune)
                put("phone", phone)
                put("specialty", specialty)
                put("yearsExp", yearsExp)
            }
            val responseJson = postJson("$BASE_URL/api/painters/register", payload)
            responseJson.optString("message", "Demande envoyée ! DZpeinture validera votre profil sous 24h.")
        }
    }

    private fun postJson(urlStr: String, payload: JSONObject): JSONObject {
        val conn = (URL(urlStr).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 12000
            readTimeout = 12000
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
        }
        OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { writer ->
            writer.write(payload.toString())
        }
        val code = conn.responseCode
        val body = (if (code in 200..299) conn.inputStream else conn.errorStream)
            ?.bufferedReader()?.use { it.readText() } ?: ""
        conn.disconnect()
        val json = JSONObject(body.ifBlank { "{}" })
        if (code !in 200..299 || !json.optBoolean("ok", false)) {
            error(json.optString("error", "Erreur réseau ($code)"))
        }
        return json
    }

    private fun parsePaintersArray(arr: JSONArray): List<PainterProfile> {
        val list = mutableListOf<PainterProfile>()
        for (i in 0 until arr.length()) {
            list.add(parsePainter(arr.getJSONObject(i)))
        }
        return list
    }

    private fun parsePainter(obj: JSONObject): PainterProfile {
        val revArr = obj.optJSONArray("reviews") ?: JSONArray()
        val reviews = mutableListOf<PainterReview>()
        for (i in 0 until revArr.length()) {
            val r = revArr.getJSONObject(i)
            reviews.add(
                PainterReview(
                    id = r.optString("id"),
                    author = r.optString("author", "Client"),
                    rating = r.optInt("rating", 5),
                    comment = r.optString("comment", ""),
                    createdAt = r.optString("createdAt", "").take(10)
                )
            )
        }
        return PainterProfile(
            id = obj.optString("id"),
            name = obj.optString("name"),
            commune = obj.optString("commune"),
            phone = obj.optString("phone"),
            specialty = obj.optString("specialty"),
            yearsExp = obj.optInt("yearsExp", 1),
            verified = obj.optBoolean("verified", false),
            avgRating = obj.optDouble("avgRating", 0.0),
            reviewsCount = obj.optInt("reviewsCount", reviews.size),
            reviews = reviews
        )
    }
}
