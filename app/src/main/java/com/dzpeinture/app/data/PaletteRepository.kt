package com.dzpeinture.app.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray

object PaletteRepository {
    @Volatile
    private var cachedSitePalette: List<PaintShade>? = null

    @Volatile
    private var cachedNcsPalette: List<PaintShade>? = null

    private fun parseHex(hexStr: String): Long {
        val clean = hexStr.trim().removePrefix("#")
        return try {
            ("FF$clean").toLong(16)
        } catch (_: Exception) {
            0xFFE5E7EB
        }
    }

    suspend fun loadNcsPalette(context: Context): List<PaintShade> {
        cachedNcsPalette?.let { return it }
        return withContext(Dispatchers.IO) {
            runCatching {
                val json = context.assets.open("palette_ncs.json").bufferedReader().use { it.readText() }
                val arr = JSONArray(json)
                val list = ArrayList<PaintShade>(arr.length())
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    val code = o.optString("code")
                    val name = o.optString("name", code)
                    val hex = parseHex(o.optString("hex", "#FFFFFF"))
                    val group = o.optString("collection", "NCS")
                    list.add(
                        PaintShade(
                            code = code,
                            name = name,
                            roomTag = group,
                            hex = hex,
                            recommendedFinish = "Machine à Teinter NCS"
                        )
                    )
                }
                cachedNcsPalette = list
                list
            }.getOrDefault(emptyList())
        }
    }

    suspend fun loadSitePalette(context: Context): List<PaintShade> {
        cachedSitePalette?.let { return it }
        return withContext(Dispatchers.IO) {
            runCatching {
                val json = context.assets.open("palette_site.json").bufferedReader().use { it.readText() }
                val arr = JSONArray(json)
                val list = ArrayList<PaintShade>(arr.length())
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    val code = o.optString("code")
                    val name = o.optString("name", code)
                    val hex = parseHex(o.optString("hex", "#FFFFFF"))
                    val collection = o.optString("collection", "LEOMIX")
                    val page = o.optString("page", "")
                    val finishInfo = if (page.isNotBlank()) "$collection • p.$page" else collection
                    list.add(
                        PaintShade(
                            code = code,
                            name = name,
                            roomTag = collection,
                            hex = hex,
                            recommendedFinish = finishInfo
                        )
                    )
                }
                cachedSitePalette = list
                list
            }.getOrDefault(emptyList())
        }
    }
}
