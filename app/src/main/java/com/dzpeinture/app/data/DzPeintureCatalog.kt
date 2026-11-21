package com.dzpeinture.app.data

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.dzpeinture.app.R
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.ceil

enum class ProductCategory { FIXATEUR, FINITION }

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
)

data class PaintShade(
    val code: String,
    val name: String,
    val roomTag: String,
    val hex: Long,
    val recommendedFinish: String
)

data class DzStore(
    val id: String,
    val name: String,
    val district: String,
    val badgeColorHex: Long,
    val mapsUrl: String,
    val hours: String = "Samedi – Jeudi : 08h00 – 17h00"
)

data class OptimalPackResult(
    val product: DzProduct,
    val requiredKg: Int,
    val bucketsCount: Int,
    val extraKgCount: Int,
    val totalPriceDa: Int
)

data class CartEntry(
    val product: DzProduct,
    val buckets: Int = 0,
    val extraKg: Int = 0
) {
    val subtotalDa: Int
        get() = buckets * product.bucketPriceDa + extraKg * product.pricePerKgDa
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
            coverageSqmPerKg = 7.0,
            usageSummary = "Mur brut ciment, brique ou plâtre avant enduit & extérieur.",
            accentHex = 0xFF4B5563,
            imageRes = R.drawable.pot_grip_fond
        ),
        DzProduct(
            id = "fixateur",
            name = "FIXATEUR",
            subtitle = "Fixateur Lisse • 18 kg",
            category = ProductCategory.FIXATEUR,
            bucketWeightKg = 18,
            bucketPriceDa = 5800,
            pricePerKgDa = 400,
            coverageSqmPerKg = 8.0,
            usageSummary = "Sur enduit fini avant la peinture de finition. Bloque l'absorption.",
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
            coverageSqmPerKg = 7.5,
            usageSummary = "Rendu mat velouté élégant, lessivable à la lavette. Idéal salons & chambres.",
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
            coverageSqmPerKg = 7.5,
            usageSummary = "Finition satinée soyeuse, ultra-lessivable, très lumineuse pour toute la maison.",
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
            coverageSqmPerKg = 8.0,
            usageSummary = "Super lisse, haute résistance à l'eau et graisses : cuisines, salles de bain & boiseries.",
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
            coverageSqmPerKg = 5.5,
            usageSummary = "Étanchéité extérieure, anti-humidité, résistante au soleil & auto-lavable.",
            accentHex = 0xFF1E3A5F,
            imageRes = R.drawable.pot_facade
        )
    )

    val shades = listOf(
        // Salon (9 teintes)
        PaintShade("DZP-101", "Vanilla Latte", "Salon", 0xFFE6D6BE, "SOIE SATINÉ"),
        PaintShade("DZP-102", "Blanc Pur", "Salon", 0xFFF8F9FA, "VELOURS MAT"),
        PaintShade("DZP-103", "Greige Chic", "Salon", 0xFFC9C0B5, "SOIE SATINÉ"),
        PaintShade("DZP-104", "Lin Naturel", "Salon", 0xFFEAE2D6, "VELOURS MAT"),
        PaintShade("DZP-105", "Crème Ivoire", "Salon", 0xFFF4ECDD, "SOIE SATINÉ"),
        PaintShade("DZP-115", "Sable d'Or", "Salon", 0xFFDFC7A0, "SOIE SATINÉ"),
        PaintShade("DZP-116", "Taupe Doux", "Salon", 0xFFB8ACA0, "VELOURS MAT"),
        PaintShade("DZP-117", "Blanc Cassé", "Salon", 0xFFF2EFE9, "SOIE SATINÉ"),
        PaintShade("DZP-118", "Cappuccino Clair", "Salon", 0xFFD2BFA8, "SOIE SATINÉ"),
        // Chambre (8 teintes)
        PaintShade("DZP-106", "Breeze", "Chambre", 0xFFD2E2E5, "VELOURS MAT"),
        PaintShade("DZP-107", "Gris Perle", "Chambre", 0xFFD9DDE0, "VELOURS MAT"),
        PaintShade("DZP-108", "Vert Sauge", "Chambre", 0xFFB8C7B8, "VELOURS MAT"),
        PaintShade("DZP-109", "Rose Poudré", "Chambre", 0xFFE8D1D1, "SOIE SATINÉ"),
        PaintShade("DZP-110", "Bleu Nuit", "Chambre", 0xFF1F3652, "VELOURS MAT"),
        PaintShade("DZP-119", "Lavande Douce", "Chambre", 0xFFD7D2E2, "VELOURS MAT"),
        PaintShade("DZP-120", "Bleu Glacier", "Chambre", 0xFFC5DAE8, "VELOURS MAT"),
        PaintShade("DZP-121", "Vert Olive Clair", "Chambre", 0xFFC5CCB6, "VELOURS MAT"),
        // Cuisine / SDB (7 teintes)
        PaintShade("DZP-111", "Ciel d'Alger", "Cuisine / SDB", 0xFF8BBCD4, "CRISTAL LAQUE"),
        PaintShade("DZP-112", "Blanc Neige Laqué", "Cuisine / SDB", 0xFFFDFEFF, "CRISTAL LAQUE"),
        PaintShade("DZP-113", "Menthe Douce", "Cuisine / SDB", 0xFFCDE3D8, "CRISTAL LAQUE"),
        PaintShade("DZP-114", "Anthracite Pro", "Cuisine / SDB", 0xFF3A414A, "CRISTAL LAQUE"),
        PaintShade("DZP-122", "Gris Aluminium", "Cuisine / SDB", 0xFFC4C9CE, "CRISTAL LAQUE"),
        PaintShade("DZP-123", "Turquoise Clair", "Cuisine / SDB", 0xFFAAD9DC, "CRISTAL LAQUE"),
        PaintShade("DZP-124", "Beige Satiné", "Cuisine / SDB", 0xFFE5DAC8, "CRISTAL LAQUE"),
        // Façade (6 teintes)
        PaintShade("DZP-201", "Sable Sahara", "Façade", 0xFFDCC39E, "IMPER FAÇADE"),
        PaintShade("DZP-202", "Pierre Calcaire", "Façade", 0xFFE5DFD3, "IMPER FAÇADE"),
        PaintShade("DZP-203", "Terracotta Douce", "Façade", 0xFFC97B5B, "IMPER FAÇADE"),
        PaintShade("DZP-204", "Blanc Méditerranée", "Façade", 0xFFF5F4EF, "IMPER FAÇADE"),
        PaintShade("DZP-205", "Ocre Doré", "Façade", 0xFFD6A76C, "IMPER FAÇADE"),
        PaintShade("DZP-206", "Gris Béton Chic", "Façade", 0xFFB8BDC2, "IMPER FAÇADE")
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
     * Calcule la combinaison la plus économique entre seaux complets (18/20 kg) et kg au détail.
     */
    fun calculateOptimalPack(product: DzProduct, surfaceSqm: Double, coats: Int): OptimalPackResult {
        if (surfaceSqm <= 0.0 || coats <= 0) {
            return OptimalPackResult(product, 0, 0, 0, 0)
        }
        val requiredKg = ceil((surfaceSqm * coats) / product.coverageSqmPerKg).toInt().coerceAtLeast(1)
        val fullBuckets = requiredKg / product.bucketWeightKg
        val remainderKg = requiredKg % product.bucketWeightKg

        val costWithExtraKg = fullBuckets * product.bucketPriceDa + remainderKg * product.pricePerKgDa
        val costWithOneMoreBucket = (fullBuckets + 1) * product.bucketPriceDa

        return if (remainderKg > 0 && costWithOneMoreBucket <= costWithExtraKg) {
            OptimalPackResult(
                product = product,
                requiredKg = requiredKg,
                bucketsCount = fullBuckets + 1,
                extraKgCount = 0,
                totalPriceDa = costWithOneMoreBucket
            )
        } else {
            OptimalPackResult(
                product = product,
                requiredKg = requiredKg,
                bucketsCount = fullBuckets,
                extraKgCount = remainderKg,
                totalPriceDa = costWithExtraKg
            )
        }
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
