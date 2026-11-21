package com.dzpeinture.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dzpeinture.app.data.DzPeintureData
import com.dzpeinture.app.data.DzProduct
import com.dzpeinture.app.data.OptimalPackResult
import com.dzpeinture.app.data.PaintShade
import com.dzpeinture.app.data.ProductCategory
import com.dzpeinture.app.ui.theme.DzEmerald
import com.dzpeinture.app.ui.theme.DzGold
import com.dzpeinture.app.ui.theme.DzNavy

private enum class WallSurfaceType(val label: String, val primerId: String?) {
    BRUT("Mur brut (ciment / plâtre)", "grip_fond"),
    ENDUIT("Enduit neuf fini", "fixateur"),
    DEJA_PEINT("Mur déjà peint (propre)", null)
}

@Composable
fun DevisScreen(
    selectedShade: PaintShade?,
    onAddQuoteToCart: (List<OptimalPackResult>) -> Unit,
    onSendWhatsApp: (String) -> Unit
) {
    var useDimensions by remember { mutableStateOf(false) }
    var surfaceInput by remember { mutableStateOf("60") }
    var lengthInput by remember { mutableStateOf("12") }
    var heightInput by remember { mutableStateOf("3") }
    var wallType by remember { mutableStateOf(WallSurfaceType.ENDUIT) }
    var selectedFinishId by remember { mutableStateOf("satine") }
    var coats by remember { mutableIntStateOf(2) }
    var showGuideDialog by remember { mutableStateOf(false) }

    val finishes = remember { DzPeintureData.products.filter { it.category == ProductCategory.FINITION } }
    val selectedFinish = remember(selectedFinishId) {
        finishes.firstOrNull { it.id == selectedFinishId } ?: finishes.first()
    }
    val recommendedPrimer = remember(wallType) {
        wallType.primerId?.let { id -> DzPeintureData.products.firstOrNull { it.id == id } }
    }

    val surfaceSqm = remember(useDimensions, surfaceInput, lengthInput, heightInput) {
        if (!useDimensions) {
            surfaceInput.replace(',', '.').toDoubleOrNull() ?: 0.0
        } else {
            val l = lengthInput.replace(',', '.').toDoubleOrNull() ?: 0.0
            val h = heightInput.replace(',', '.').toDoubleOrNull() ?: 0.0
            l * h
        }
    }

    val primerPack = remember(recommendedPrimer, surfaceSqm) {
        recommendedPrimer?.let { DzPeintureData.calculateOptimalPack(it, surfaceSqm, 1) }
    }
    val finishPack = remember(selectedFinish, surfaceSqm, coats) {
        DzPeintureData.calculateOptimalPack(selectedFinish, surfaceSqm, coats)
    }
    val totalQuoteDa = (primerPack?.totalPriceDa ?: 0) + finishPack.totalPriceDa

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DzNavy),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Calculateur de Devis Express",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Calcul exact en seaux 18/20 kg + 1 kg au détail avec les tarifs officiels DZpeinture.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFD6E4F0)
                        )
                    }
                }
            }
        }

        item {
            OutlinedButton(
                onClick = { showGuideDialog = true },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = DzGold, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Besoin d'aide ? Guide « Quel produit choisir ? »", fontWeight = FontWeight.SemiBold)
            }
        }

        // Step 1: Surface
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. Surface à peindre",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DzNavy
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = !useDimensions,
                            onClick = { useDimensions = false },
                            label = { Text("Surface (m²)") }
                        )
                        FilterChip(
                            selected = useDimensions,
                            onClick = { useDimensions = true },
                            label = { Text("Longueur × Hauteur") }
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    if (!useDimensions) {
                        OutlinedTextField(
                            value = surfaceInput,
                            onValueChange = { surfaceInput = it },
                            label = { Text("Surface totale en m²") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = lengthInput,
                                onValueChange = { lengthInput = it },
                                label = { Text("Longueur (m)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = heightInput,
                                onValueChange = { heightInput = it },
                                label = { Text("Hauteur (m)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Surface calculée : ${String.format("%.1f", surfaceSqm)} m²",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = DzEmerald
                        )
                    }
                }
            }
        }

        // Step 2: Wall State
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "2. État du mur (fixateur auto)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DzNavy
                    )
                    Spacer(Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        WallSurfaceType.entries.forEach { option ->
                            FilterChip(
                                selected = wallType == option,
                                onClick = { wallType = option },
                                label = { Text(option.label) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        // Step 3: Finish + Coats
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "3. Finition souhaitée & couches",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DzNavy
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        finishes.forEach { product ->
                            FilterChip(
                                selected = selectedFinishId == product.id,
                                onClick = { selectedFinishId = product.id },
                                label = { Text(product.name) }
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = selectedFinish.usageSummary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Couches :", fontWeight = FontWeight.SemiBold)
                        listOf(1, 2, 3).forEach { c ->
                            FilterChip(
                                selected = coats == c,
                                onClick = { coats = c },
                                label = { Text("$c couche${if (c > 1) "s" else ""}") }
                            )
                        }
                    }
                }
            }
        }

        // Result Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF7)),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Estimation de votre Devis",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DzNavy
                    )
                    if (selectedShade != null) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Teinte choisie : ${selectedShade.name} (${selectedShade.code})",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = DzEmerald
                        )
                    }
                    Spacer(Modifier.height(12.dp))

                    if (primerPack != null && primerPack.requiredKg > 0) {
                        QuoteLineRow(primerPack, "1 couche de fond")
                        Spacer(Modifier.height(10.dp))
                    }

                    if (finishPack.requiredKg > 0) {
                        QuoteLineRow(finishPack, "$coats couche${if (coats > 1) "s" else ""}")
                    }

                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TOTAL ESTIMÉ",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = DzNavy
                        )
                        Text(
                            text = DzPeintureData.formatDa(totalQuoteDa),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = DzEmerald
                        )
                    }

                    Spacer(Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = {
                                val packs = buildList {
                                    if (primerPack != null && primerPack.requiredKg > 0) add(primerPack)
                                    if (finishPack.requiredKg > 0) add(finishPack)
                                }
                                onAddQuoteToCart(packs)
                            },
                            enabled = totalQuoteDa > 0,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Au panier")
                        }
                        Button(
                            onClick = {
                                val msg = buildString {
                                    appendLine("Bonjour DZpeinture, voici mon devis calculé sur l'application :")
                                    appendLine("• Surface : ${String.format("%.1f", surfaceSqm)} m²")
                                    appendLine("• Support : ${wallType.label}")
                                    if (selectedShade != null) {
                                        appendLine("• Teinte : ${selectedShade.name} (${selectedShade.code})")
                                    }
                                    if (primerPack != null && primerPack.requiredKg > 0) {
                                        appendLine("• ${primerPack.product.name} : ${formatPackSummary(primerPack)} (${DzPeintureData.formatDa(primerPack.totalPriceDa)})")
                                    }
                                    appendLine("• ${finishPack.product.name} ($coats couches) : ${formatPackSummary(finishPack)} (${DzPeintureData.formatDa(finishPack.totalPriceDa)})")
                                    appendLine("➡️ Total estimé : ${DzPeintureData.formatDa(totalQuoteDa)}")
                                }
                                onSendWhatsApp(msg)
                            },
                            enabled = totalQuoteDa > 0,
                            colors = ButtonDefaults.buttonColors(containerColor = DzEmerald),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("WhatsApp", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }

    if (showGuideDialog) {
        ProductAdvisorDialog(
            onDismiss = { showGuideDialog = false },
            onApplyRecommendation = { recWall, recFinishId ->
                wallType = recWall
                selectedFinishId = recFinishId
                showGuideDialog = false
            }
        )
    }
}

@Composable
private fun QuoteLineRow(pack: OptimalPackResult, subtitle: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Image(
            painter = painterResource(pack.product.imageRes),
            contentDescription = pack.product.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White)
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = pack.product.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$subtitle • ${formatPackSummary(pack)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = DzPeintureData.formatDa(pack.totalPriceDa),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = DzNavy
        )
    }
}

private fun formatPackSummary(pack: OptimalPackResult): String {
    val parts = mutableListOf<String>()
    if (pack.bucketsCount > 0) {
        parts.add("${pack.bucketsCount} seau${if (pack.bucketsCount > 1) "x" else ""} ${pack.product.bucketWeightKg} kg")
    }
    if (pack.extraKgCount > 0) {
        parts.add("${pack.extraKgCount} kg détail")
    }
    return if (parts.isEmpty()) "0 kg" else parts.joinToString(" + ")
}

@Composable
private fun ProductAdvisorDialog(
    onDismiss: () -> Unit,
    onApplyRecommendation: (WallSurfaceType, String) -> Unit
) {
    var zone by remember { mutableStateOf("salon") }
    var isRawWall by remember { mutableStateOf(false) }

    val (recWall, recFinishId, recText) = remember(zone, isRawWall) {
        val w = if (isRawWall) WallSurfaceType.BRUT else WallSurfaceType.ENDUIT
        when (zone) {
            "facade" -> Triple(WallSurfaceType.BRUT, "facade", "GRIP FOND (accrochage extérieur) + IMPER FAÇADE 20 kg (étanchéité & anti-humidité)")
            "cuisine" -> Triple(w, "gloss", "${if (isRawWall) "GRIP FOND" else "FIXATEUR"} + CRISTAL LAQUE (brillant super lisse, résiste à l'eau & graisses)")
            "chambre" -> Triple(w, "mat", "${if (isRawWall) "GRIP FOND" else "FIXATEUR"} + VELOURS MAT (élégant, zéro reflet, lessivable à la lavette)")
            else -> Triple(w, "satine", "${if (isRawWall) "GRIP FOND" else "FIXATEUR"} + SOIE SATINÉ (lumineux, soyeux et ultra-lessivable)")
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Guide : Quel produit choisir ?", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("1. Quelle pièce / surface ?", fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(selected = zone == "salon", onClick = { zone = "salon" }, label = { Text("Salon") })
                    FilterChip(selected = zone == "chambre", onClick = { zone = "chambre" }, label = { Text("Chambre") })
                    FilterChip(selected = zone == "cuisine", onClick = { zone = "cuisine" }, label = { Text("Cuisine/SDB") })
                    FilterChip(selected = zone == "facade", onClick = { zone = "facade" }, label = { Text("Façade") })
                }
                if (zone != "facade") {
                    Text("2. État du mur ?", fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(selected = !isRawWall, onClick = { isRawWall = false }, label = { Text("Enduit fini") })
                        FilterChip(selected = isRawWall, onClick = { isRawWall = true }, label = { Text("Mur brut ciment") })
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFECFDF5))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "✅ Recommandation pro :\n$recText",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = DzNavy
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onApplyRecommendation(recWall, recFinishId) },
                colors = ButtonDefaults.buttonColors(containerColor = DzEmerald)
            ) {
                Text("Appliquer au devis")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Fermer") }
        }
    )
}
