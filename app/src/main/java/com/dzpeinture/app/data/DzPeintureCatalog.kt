package com.dzpeinture.app.data

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.dzpeinture.app.R
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

enum class ProductCategory { FIXATEUR, ENDUIT, FINITION }

data class DzProduct(
    val id: String,
    val name: String,
    val subtitle: String,
    val category: ProductCategory,
    val bucketWeightKg: Int,
    val bucketPriceDa: Int,
    val pricePerKgDa: Int,
    val coverageSqmPerKg: Double,
    val usageSummary: String,
    val accentHex: Long,
    @DrawableRes val imageRes: Int
) {
    val isTintable: Boolean
        get() = category == ProductCategory.FINITION
}

data class PaintShade(
    val code: String,
    val name: String,
    val roomTag: String,
    val hex: Long,
    val recommendedFinish: String
) {
    val supplementPerKgDa: Int
        get() = DzPeintureData.estimateTintSupplementDa(code, hex)

    val supplementLabel: String
        get() = if (supplementPerKgDa == 0) "Blanc / Origine (+0 DA/kg)"
        else "+${DzPeintureData.formatDa(supplementPerKgDa)} / kg"
}

data class DzStore(
    val id: String,
    val name: String,
    val district: String,
    val badgeColorHex: Long,
    val mapsUrl: String,
    val hours: String = "Samedi – Jeudi : 08h00 – 17h00"
)

data class QuoteLine(
    val product: DzProduct,
    val requiredKg: Int,
    val bucketsCount: Int,
    val extraKgCount: Int,
    val tintSupplementPerKgDa: Int,
    val totalPriceDa: Int
) {
    fun quantityLabel(): String {
        val parts = mutableListOf<String>()
        if (bucketsCount > 0) {
            parts.add("$bucketsCount pot${if (bucketsCount > 1) "s" else ""} de ${product.bucketWeightKg} kg")
        }
        if (extraKgCount > 0) {
            parts.add("$extraKgCount kg")
        }
        return if (parts.isEmpty()) "0 kg" else parts.joinToString(" + ")
    }
}

data class MethodQuote(
    val id: String,
    val title: String,
    val description: String,
    val lines: List<QuoteLine>,
    val totalDa: Int
)

data class CartEntry(
    val product: DzProduct,
    val buckets: Int = 0,
    val extraKg: Int = 0
) {
    fun subtotalDa(tintSupplementPerKgDa: Int): Int {
        val supp = if (product.isTintable) tintSupplementPerKgDa else 0
        val bucketUnit = product.bucketPriceDa + supp * product.bucketWeightKg
        val kgUnit = product.pricePerKgDa + supp
        return buckets * bucketUnit + extraKg * kgUnit
    }
}

object DzPeintureData {
    const val PHONE_ORDERS = "0555095686"
    const val PHONE_ORDERS_DISPLAY = "05 55 09 56 86"
    const val WHATSAPP_NUMBER = "213799421932"
    const val WHATSAPP_DISPLAY = "07 99 42 19 32"
    const val WEBSITE_URL = "https://dzp.paintingsoft.com"

    val products = listOf(
        DzProduct(
            id = "grip_fond",
            name = "GRIP FOND",
            subtitle = "Fixateur de Fond Rugueux • 18 kg",
            category = ProductCategory.FIXATEUR,
            bucketWeightKg = 18,
            bucketPriceDa = 5800,
            pricePerKgDa = 400,
            coverageSqmPerKg = 8.0,
            usageSummary = "Phase 1 obligatoire sur mur brut/ciment avant enduit. Rendement : 8 m²/kg.",
            accentHex = 0xFF4B5563,
            imageRes = R.drawable.pot_grip_fond
        ),
        DzProduct(
            id = "enduit_charge",
            name = "ENDUIT CHARGE",
            subtitle = "Enduit de Charge Préparé • 20 kg",
            category = ProductCategory.ENDUIT,
            bucketWeightKg = 20,
            bucketPriceDa = 2200,
            pricePerKgDa = 150,
            coverageSqmPerKg = 1.0,
            usageSummary = "Rebouchage & dégrossissage prêt à l'emploi, accrochage optimal. Rendement : 1 m²/kg.",
            accentHex = 0xFFA85532,
            imageRes = R.drawable.pot_enduit_charge
        ),
        DzProduct(
            id = "enduit_finition",
            name = "ENDUIT FINITION",
            subtitle = "Enduit Préparé de Finition • 20 kg",
            category = ProductCategory.ENDUIT,
            bucketWeightKg = 20,
            bucketPriceDa = 2200,
            pricePerKgDa = 150,
            coverageSqmPerKg = 1.5,
            usageSummary = "Lissage extra-fin prêt à peindre, ponçage facile. Rendement : 1,5 m²/kg.",
            accentHex = 0xFFB89768,
            imageRes = R.drawable.pot_enduit_finition
        ),
        DzProduct(
            id = "fixateur",
            name = "FIXATEUR",
            subtitle = "Fixateur Lisse avant Finition • 18 kg",
            category = ProductCategory.FIXATEUR,
            bucketWeightKg = 18,
            bucketPriceDa = 5800,
            pricePerKgDa = 400,
            coverageSqmPerKg = 8.0,
            usageSummary = "Fixateur de finition sur enduit sec avant peinture. Rendement : 8 m²/kg.",
            accentHex = 0xFF6B7280,
            imageRes = R.drawable.pot_fixateur
        ),
        DzProduct(
            id = "mat",
            name = "VELOURS MAT",
            subtitle = "Acrylique Mat Lessivable • 18 kg",
            category = ProductCategory.FINITION,
            bucketWeightKg = 18,
            bucketPriceDa = 9800,
            pricePerKgDa = 600,
            coverageSqmPerKg = 6.0,
            usageSummary = "Rendu mat velouté élégant, lessivable à la lavette. Rendement : 6 m²/kg (couches incluses).",
            accentHex = 0xFF4A6B7C,
            imageRes = R.drawable.pot_mat
        ),
        DzProduct(
            id = "satine",
            name = "SOIE SATINÉ",
            subtitle = "Semi-Brillante Satiné Lisse • 18 kg",
            category = ProductCategory.FINITION,
            bucketWeightKg = 18,
            bucketPriceDa = 9800,
            pricePerKgDa = 600,
            coverageSqmPerKg = 6.0,
            usageSummary = "Finition satinée soyeuse, ultra-lessivable et lumineuse. Rendement : 6 m²/kg (couches incluses).",
            accentHex = 0xFF107E57,
            imageRes = R.drawable.pot_satine
        ),
        DzProduct(
            id = "gloss",
            name = "CRISTAL LAQUE",
            subtitle = "Laque à l'Eau Brillante • 18 kg",
            category = ProductCategory.FINITION,
            bucketWeightKg = 18,
            bucketPriceDa = 10800,
            pricePerKgDa = 700,
            coverageSqmPerKg = 6.0,
            usageSummary = "Super lisse, résiste à l'eau et graisses (cuisines, SDB, portes). Rendement : 6 m²/kg.",
            accentHex = 0xFF0284C7,
            imageRes = R.drawable.pot_gloss
        ),
        DzProduct(
            id = "facade",
            name = "IMPER FAÇADE",
            subtitle = "Spéciale Façade Demi-Mat • 20 kg",
            category = ProductCategory.FINITION,
            bucketWeightKg = 20,
            bucketPriceDa = 13800,
            pricePerKgDa = 900,
            coverageSqmPerKg = 5.0,
            usageSummary = "Étanchéité extérieure, anti-humidité & auto-lavable. Rendement : 5 m²/kg (couches incluses).",
            accentHex = 0xFF1E3A5F,
            imageRes = R.drawable.pot_facade
        )
    )

    fun productById(id: String): DzProduct = products.first { it.id == id }

    /**
     * Tendances réelles basées sur les vrais codes NCS & références du Nuancier LEOMIX
     * (les teintes les plus demandées par les architectes & peintres en Algérie).
     */
    val shades = listOf(
        // Salon — Tendances NCS & LEOMIX
        PaintShade("S 0500-N", "Blanc Universel", "Salon", 0xFFF2F2F0, "NCS • SOIE SATINÉ / MAT"),
        PaintShade("S 0502-Y", "Blanc Cassé Chaud", "Salon", 0xFFF2EFE7, "NCS • SOIE SATINÉ"),
        PaintShade("S 1002-Y", "Pierre Claire", "Salon", 0xFFE4E0D5, "NCS • VELOURS MAT"),
        PaintShade("S 1002-Y50R", "Greige Très Clair", "Salon", 0xFFE3DDD7, "NCS • SOIE SATINÉ"),
        PaintShade("S 1005-Y20R", "Vanilla Latte", "Salon", 0xFFE6D9C4, "NCS • SOIE SATINÉ"),
        PaintShade("S 1005-Y30R", "Crème Amande", "Salon", 0xFFE7D7C4, "NCS • SOIE SATINÉ"),
        PaintShade("S 1502-Y50R", "Greige Moderne", "Salon", 0xFFD5CFC9, "NCS • VELOURS MAT"),
        PaintShade("S 1505-Y20R", "Sable Doux", "Salon", 0xFFD8CBB6, "NCS • SOIE SATINÉ"),
        PaintShade("LPC 0972", "Lin Doré (LEOMIX)", "Salon", 0xFFEDDAC1, "LEOMIX PRIME • p.97"),
        // Chambre — Tendances NCS & LEOMIX
        PaintShade("S 1505-B20G", "Breeze", "Chambre", 0xFFCED9DA, "NCS • VELOURS MAT"),
        PaintShade("S 1005-B20G", "Breeze Clair", "Chambre", 0xFFDCE6E6, "NCS • VELOURS MAT"),
        PaintShade("S 1500-N", "Gris Perle Clair", "Chambre", 0xFFD6D6D4, "NCS • VELOURS MAT"),
        PaintShade("S 2002-Y50R", "Greige Chic", "Chambre", 0xFFC7C0BA, "NCS • VELOURS MAT"),
        PaintShade("S 2010-G20Y", "Vert Sauge", "Chambre", 0xFFB4C2AF, "NCS • VELOURS MAT"),
        PaintShade("S 1010-Y80R", "Rose Poudré", "Chambre", 0xFFE6CCC6, "NCS • SOIE SATINÉ"),
        PaintShade("S 7020-R90B", "Bleu Nuit", "Chambre", 0xFF2B3D51, "NCS • VELOURS MAT"),
        PaintShade("OW9-C", "Blanc Rosé (LEOMIX)", "Chambre", 0xFFF7E6E5, "LEOMIX 1000 • p.171"),
        // Cuisine / SDB — Tendances NCS
        PaintShade("S 0300-N", "Blanc Pur Laqué", "Cuisine / SDB", 0xFFF7F7F5, "NCS • CRISTAL LAQUE"),
        PaintShade("S 1015-B", "Ciel d'Alger", "Cuisine / SDB", 0xFFC1DCE8, "NCS • CRISTAL LAQUE"),
        PaintShade("S 1010-G10Y", "Menthe Douce", "Cuisine / SDB", 0xFFD0DFD1, "NCS • CRISTAL LAQUE"),
        PaintShade("S 2000-N", "Gris Perle", "Cuisine / SDB", 0xFFC8C8C6, "NCS • CRISTAL LAQUE"),
        PaintShade("S 7500-N", "Anthracite Pro", "Cuisine / SDB", 0xFF3E3E3D, "NCS • CRISTAL LAQUE"),
        // Façade — Tendances NCS
        PaintShade("S 0505-Y20R", "Ivoire Façade", "Façade", 0xFFF5ECDC, "NCS • IMPER FAÇADE"),
        PaintShade("S 1510-Y20R", "Sable Doré", "Façade", 0xFFD8C2A2, "NCS • IMPER FAÇADE"),
        PaintShade("S 2010-Y20R", "Sable Sahara", "Façade", 0xFFCAB393, "NCS • IMPER FAÇADE"),
        PaintShade("S 2020-Y60R", "Terracotta Douce", "Façade", 0xFFD29B84, "NCS • IMPER FAÇADE"),
        PaintShade("S 3005-Y20R", "Taupe Façade", "Façade", 0xFFB0A391, "NCS • IMPER FAÇADE"),
        PaintShade("S 3500-N", "Gris Béton Façade", "Façade", 0xFF9E9E9C, "NCS • IMPER FAÇADE")
    )

    val stores = listOf(
        DzStore(
            id = "oued_romane",
            name = "DZpeinture Oued Romane",
            district = "Oued Romane (El Achour), Alger",
            badgeColorHex = 0xFF107E57,
            mapsUrl = "https://maps.app.goo.gl/v7aLUKratJuBiLLBA"
        ),
        DzStore(
            id = "cheraga",
            name = "DZpeinture Chéraga",
            district = "Chéraga Centre, Alger",
            badgeColorHex = 0xFF0284C7,
            mapsUrl = "https://maps.app.goo.gl/6XYtWaT3pxakFoPV6"
        ),
        DzStore(
            id = "bachdjerrah",
            name = "DZpeinture Bachdjerrah",
            district = "Bachdjerrah, Alger",
            badgeColorHex = 0xFFDC2626,
            mapsUrl = "https://maps.app.goo.gl/cdG2WcDu9S919ftd9"
        )
    )

    /**
     * Calcule automatiquement le supplément couleur par kg (+0 à +1 000 DA/kg)
     * selon la luminosité et la saturation réelle de la teinte (Blanc = 0, Claire = 200,
     * Moyenne = 400, Soutenue = 600, Foncée = 800, Très foncée/intense = 1000).
     */
    fun estimateTintSupplementDa(code: String, hex: Long): Int {
        val normCode = code.trim().uppercase()
        if (normCode == "BLANC" || normCode == "S 0300-N" || normCode == "S 0500-N") return 0

        val r = ((hex shr 16) and 0xFF) / 255.0
        val g = ((hex shr 8) and 0xFF) / 255.0
        val b = (hex and 0xFF) / 255.0
        val maxC = max(r, max(g, b))
        val minC = min(r, min(g, b))
        val saturation = if (maxC == 0.0) 0.0 else (maxC - minC) / maxC
        val luminance = 0.299 * r + 0.587 * g + 0.114 * b

        // Pure near-white with almost zero tint
        if (luminance >= 0.96 && saturation <= 0.03) return 0

        val pigmentScore = (1.0 - luminance) * 0.72 + saturation * 0.45
        return when {
            pigmentScore < 0.22 -> 200
            pigmentScore < 0.40 -> 400
            pigmentScore < 0.56 -> 600
            pigmentScore < 0.72 -> 800
            else -> 1000
        }
    }

    /**
     * Formule exacte du site dzp.paintingsoft.com (rendement incluant déjà les couches).
     */
    fun calculateQuoteLine(product: DzProduct, surfaceSqm: Double, tintSupplementPerKgDa: Int): QuoteLine {
        if (surfaceSqm <= 0.0) {
            return QuoteLine(product, 0, 0, 0, 0, 0)
        }
        val requiredKg = ceil((surfaceSqm / product.coverageSqmPerKg) - 1e-9).toInt().coerceAtLeast(1)
        val supp = if (product.isTintable) tintSupplementPerKgDa else 0

        val formats = requiredKg / product.bucketWeightKg
        val remainder = requiredKg % product.bucketWeightKg
        val priceKgFinal = product.pricePerKgDa + supp
        val priceBucketFinal = product.bucketPriceDa + supp * product.bucketWeightKg

        val totalPrice = if (formats == 0) {
            requiredKg * priceKgFinal
        } else {
            formats * priceBucketFinal + remainder * priceKgFinal
        }

        return QuoteLine(
            product = product,
            requiredKg = requiredKg,
            bucketsCount = formats,
            extraKgCount = remainder,
            tintSupplementPerKgDa = supp,
            totalPriceDa = totalPrice
        )
    }

    /**
     * Calcule les 3 méthodes officielles DZpeinture :
     * 1. Méthode Générale : GRIP FOND -> ENDUIT CHARGE -> ENDUIT FINITION -> FIXATEUR -> Finition
     * 2. Méthode Retouche : GRIP FOND -> ENDUIT FINITION -> FIXATEUR -> Finition
     * 3. Repeinturé       : FIXATEUR -> Finition
     */
    fun calculateAllMethods(
        surfaceSqm: Double,
        finishProduct: DzProduct,
        tintSupplementPerKgDa: Int
    ): List<MethodQuote> {
        val gripFond = productById("grip_fond")
        val charge = productById("enduit_charge")
        val enduitFin = productById("enduit_finition")
        val fixateur = productById("fixateur")

        fun buildMethod(id: String, title: String, desc: String, steps: List<DzProduct>): MethodQuote {
            val lines = steps.map { p -> calculateQuoteLine(p, surfaceSqm, tintSupplementPerKgDa) }
            return MethodQuote(
                id = id,
                title = title,
                description = desc,
                lines = lines,
                totalDa = lines.sumOf { it.totalPriceDa }
            )
        }

        return listOf(
            buildMethod(
                id = "generale",
                title = "1. Méthode Générale (Complet)",
                desc = "GRIP FOND → Enduit Charge → Enduit Finition → FIXATEUR → ${finishProduct.name}",
                steps = listOf(gripFond, charge, enduitFin, fixateur, finishProduct)
            ),
            buildMethod(
                id = "retouche",
                title = "2. Méthode Retouche",
                desc = "GRIP FOND → Enduit Finition → FIXATEUR → ${finishProduct.name}",
                steps = listOf(gripFond, enduitFin, fixateur, finishProduct)
            ),
            buildMethod(
                id = "repeinture",
                title = "3. Repeinturé (Mur déjà prêt)",
                desc = "FIXATEUR → ${finishProduct.name}",
                steps = listOf(fixateur, finishProduct)
            )
        )
    }

    fun formatDa(amount: Int): String {
        val formatter = NumberFormat.getIntegerInstance(Locale.FRANCE)
        return "${formatter.format(amount)} DA"
    }

    fun Color.contrastText(): Color {
        val luminance = 0.299f * red + 0.587f * green + 0.114f * blue
        return if (luminance > 0.6f) Color(0xFF111827) else Color.White
    }
}
