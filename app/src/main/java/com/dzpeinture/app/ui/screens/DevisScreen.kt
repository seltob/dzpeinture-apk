package com.dzpeinture.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Palette
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.dzpeinture.app.data.MethodQuote
import com.dzpeinture.app.data.PaintShade
import com.dzpeinture.app.data.ProductCategory
import com.dzpeinture.app.data.QuoteLine
import com.dzpeinture.app.ui.theme.DzEmerald
import com.dzpeinture.app.ui.theme.DzNavy

@Composable
fun DevisScreen(
    selectedShade: PaintShade?,
    onOpenPalette: () -> Unit,
    onResetToWhite: () -> Unit,
    onAddMethodToCart: (MethodQuote) -> Unit,
    onSendWhatsApp: (String) -> Unit
) {
    var useDimensions by remember { mutableStateOf(false) }
    var surfaceInput by remember { mutableStateOf("100") }
    var lengthInput by remember { mutableStateOf("10") }
    var heightInput by remember { mutableStateOf("3") }
    var selectedFinishId by remember { mutableStateOf("mat") }

    val finishes = remember { DzPeintureData.products.filter { it.category == ProductCategory.FINITION } }
    val selectedFinish = remember(selectedFinishId) {
        finishes.firstOrNull { it.id == selectedFinishId } ?: finishes.first()
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

    val tintSupplement = selectedShade?.supplementPerKgDa ?: 0
    val methods = remember(surfaceSqm, selectedFinish, tintSupplement) {
        DzPeintureData.calculateAllMethods(surfaceSqm, selectedFinish, tintSupplement)
    }

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
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Calculateur de Devis Pro",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Les rendements incluent déjà les couches réglementaires. Choisissez votre surface, finition et couleur.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFD6E4F0)
                    )
                }
            }
        }

        // 1. Surface
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. Surface totale des murs (m²)",
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
                            label = { Text("Surface en m² (ex: 100)") },
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

        // 2. Finition
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "2. Peinture de finition",
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
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = selectedFinish.usageSummary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 3. Couleur depuis la Palette
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "3. Couleur (Palette NCS / Nuancier)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DzNavy
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(selectedShade?.hex ?: 0xFFF8F9FA))
                                .border(1.dp, Color(0xFFD1D5DB), CircleShape)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (selectedShade == null) "Blanc d'origine"
                                else "${selectedShade.code} • ${selectedShade.name}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = selectedShade?.supplementLabel ?: "Supplément : +0 DA/kg",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = DzEmerald
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = onOpenPalette,
                            colors = ButtonDefaults.buttonColors(containerColor = DzNavy),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1.3f)
                        ) {
                            Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(17.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Choisir une couleur")
                        }
                        if (selectedShade != null) {
                            OutlinedButton(
                                onClick = onResetToWhite,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(0.9f)
                            ) {
                                Text("Blanc (+0)")
                            }
                        }
                    }
                }
            }
        }

        // Les 3 méthodes calculées automatiquement
        items(methods, key = { it.id }) { method ->
            MethodResultCard(
                method = method,
                surfaceSqm = surfaceSqm,
                selectedShade = selectedShade,
                onAddToCart = { onAddMethodToCart(method) },
                onSendWhatsApp = {
                    val colorText = if (selectedShade == null) "Blanc d'origine (+0 DA/kg)"
                    else "${selectedShade.code} - ${selectedShade.name} (${selectedShade.supplementLabel})"
                    val msg = buildString {
                        appendLine("Bonjour DZpeinture, voici mon devis calculé sur l'application :")
                        appendLine("• Méthode : ${method.title}")
                        appendLine("• Surface : ${String.format("%.1f", surfaceSqm)} m²")
                        appendLine("• Couleur : $colorText")
                        appendLine("")
                        appendLine("Étapes & Produits :")
                        method.lines.forEach { line ->
                            appendLine("- ${line.product.name} : ${line.quantityLabel()} = ${DzPeintureData.formatDa(line.totalPriceDa)}")
                        }
                        appendLine("")
                        appendLine("➡️ TOTAL : ${DzPeintureData.formatDa(method.totalDa)}")
                    }
                    onSendWhatsApp(msg)
                }
            )
        }

        item {
            Text(
                text = "Note : Les rendements comprennent déjà le nombre de couches réglementaire. Les quantités sont arrondies au kilo supérieur.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun MethodResultCard(
    method: MethodQuote,
    surfaceSqm: Double,
    selectedShade: PaintShade?,
    onAddToCart: () -> Unit,
    onSendWhatsApp: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FCFA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = method.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = DzNavy
            )
            Text(
                text = method.description,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))

            method.lines.forEachIndexed { idx, line ->
                QuoteStepRow(stepNumber = idx + 1, line = line)
                if (idx < method.lines.lastIndex) {
                    Spacer(Modifier.height(8.dp))
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TOTAL",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DzNavy
                )
                Text(
                    text = DzPeintureData.formatDa(method.totalDa),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = DzEmerald
                )
            }

            if (selectedShade != null && selectedShade.supplementPerKgDa > 0) {
                Text(
                    text = "Inclut supplément teinte ${selectedShade.code} (+${DzPeintureData.formatDa(selectedShade.supplementPerKgDa)}/kg sur la finition)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onAddToCart,
                    enabled = surfaceSqm > 0.0 && method.totalDa > 0,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(5.dp))
                    Text("Au panier")
                }
                Button(
                    onClick = onSendWhatsApp,
                    enabled = surfaceSqm > 0.0 && method.totalDa > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = DzEmerald),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1.3f)
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(5.dp))
                    Text("Commander", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun QuoteStepRow(stepNumber: Int, line: QuoteLine) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Image(
            painter = painterResource(line.product.imageRes),
            contentDescription = line.product.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White)
        )
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "$stepNumber. ${line.product.name}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Quantité : ${line.quantityLabel()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = DzPeintureData.formatDa(line.totalPriceDa),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = DzNavy
        )
    }
}
