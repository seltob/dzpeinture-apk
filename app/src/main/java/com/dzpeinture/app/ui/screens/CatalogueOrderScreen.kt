package com.dzpeinture.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import com.dzpeinture.app.data.CartEntry
import com.dzpeinture.app.data.DzPeintureData
import com.dzpeinture.app.data.DzProduct
import com.dzpeinture.app.data.PaintShade
import com.dzpeinture.app.ui.theme.DzEmerald
import com.dzpeinture.app.ui.theme.DzNavy

@Composable
fun CatalogueOrderScreen(
    cart: Map<String, CartEntry>,
    selectedShade: PaintShade?,
    onUpdateCart: (DzProduct, Int, Int) -> Unit,
    onClearCart: () -> Unit,
    onSendOrderWhatsApp: (String) -> Unit,
    onCallDirect: () -> Unit
) {
    var deliveryMode by remember { mutableStateOf("Retrait Oued Romane") }
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }

    val deliveryOptions = listOf(
        "Retrait Oued Romane",
        "Retrait Chéraga",
        "Retrait Bachdjerrah",
        "Livraison Alger"
    )
    val totalDa = cart.values.sumOf { it.subtotalDa }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (selectedShade != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(selectedShade.hex))
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Teinte sélectionnée : ${selectedShade.name}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = DzNavy
                            )
                            Text(
                                text = "Code ${selectedShade.code} • Machine à teinter DZpeinture",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        items(DzPeintureData.products, key = { it.id }) { product ->
            val entry = cart[product.id] ?: CartEntry(product, 0, 0)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(product.imageRes),
                            contentDescription = product.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(76.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF3F4F6))
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = product.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(product.accentHex)
                            )
                            Text(
                                text = product.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = product.usageSummary,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "Seau ${product.bucketWeightKg} kg : ${DzPeintureData.formatDa(product.bucketPriceDa)}  •  1 kg : ${DzPeintureData.formatDa(product.pricePerKgDa)}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = DzEmerald
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        QuantityStepper(
                            label = "Seau ${product.bucketWeightKg} kg",
                            count = entry.buckets,
                            onChange = { newBuckets -> onUpdateCart(product, newBuckets, entry.extraKg) }
                        )
                        QuantityStepper(
                            label = "Détail (kg)",
                            count = entry.extraKg,
                            onChange = { newKg -> onUpdateCart(product, entry.buckets, newKg) }
                        )
                    }
                }
            }
        }

        // Order Checkout Summary
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF7)),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Votre Commande",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = DzNavy
                        )
                        if (totalDa > 0) {
                            IconButton(onClick = onClearCart) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Vider", tint = Color(0xFFDC2626))
                            }
                        }
                    }

                    if (totalDa == 0) {
                        Text(
                            text = "Ajoutez des seaux ou des kilos ci-dessus pour préparer votre commande.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        cart.values.filter { it.subtotalDa > 0 }.forEach { item ->
                            val qtyLabel = buildList {
                                if (item.buckets > 0) add("${item.buckets} seau(x) ${item.product.bucketWeightKg}kg")
                                if (item.extraKg > 0) add("${item.extraKg} kg")
                            }.joinToString(" + ")
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${item.product.name} ($qtyLabel)", style = MaterialTheme.typography.bodySmall)
                                Text(DzPeintureData.formatDa(item.subtotalDa), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("TOTAL", fontWeight = FontWeight.Bold, color = DzNavy)
                            Text(
                                DzPeintureData.formatDa(totalDa),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = DzEmerald
                            )
                        }

                        Spacer(Modifier.height(12.dp))
                        Text("Mode de récupération :", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(6.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            deliveryOptions.forEach { opt ->
                                FilterChip(
                                    selected = deliveryMode == opt,
                                    onClick = { deliveryMode = opt },
                                    label = { Text(opt) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = customerName,
                            onValueChange = { customerName = it },
                            label = { Text("Votre nom (optionnel)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = customerPhone,
                            onValueChange = { customerPhone = it },
                            label = { Text("Votre téléphone (optionnel)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(14.dp))
                        Button(
                            onClick = {
                                val orderMsg = buildString {
                                    appendLine("🛒 NOUVELLE COMMANDE DZPEINTURE (APP ANDROID)")
                                    if (customerName.isNotBlank()) appendLine("• Client : $customerName")
                                    if (customerPhone.isNotBlank()) appendLine("• Tél : $customerPhone")
                                    appendLine("• Mode : $deliveryMode")
                                    if (selectedShade != null) {
                                        appendLine("• Teinte : ${selectedShade.name} (${selectedShade.code})")
                                    }
                                    appendLine("")
                                    appendLine("Produits :")
                                    cart.values.filter { it.subtotalDa > 0 }.forEach { item ->
                                        val q = buildList {
                                            if (item.buckets > 0) add("${item.buckets} seau(x) ${item.product.bucketWeightKg}kg")
                                            if (item.extraKg > 0) add("${item.extraKg} kg")
                                        }.joinToString(" + ")
                                        appendLine("- ${item.product.name} : $q = ${DzPeintureData.formatDa(item.subtotalDa)}")
                                    }
                                    appendLine("")
                                    appendLine("➡️ TOTAL : ${DzPeintureData.formatDa(totalDa)}")
                                }
                                onSendOrderWhatsApp(orderMsg)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DzEmerald),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Envoyer la commande sur WhatsApp", fontWeight = FontWeight.Bold)
                        }

                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = onCallDirect,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Appeler directement : ${DzPeintureData.PHONE_ORDERS_DISPLAY}")
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun QuantityStepper(
    label: String,
    count: Int,
    onChange: (Int) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { if (count > 0) onChange(count - 1) },
                modifier = Modifier.size(34.dp)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Moins", modifier = Modifier.size(18.dp))
            }
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            IconButton(
                onClick = { onChange(count + 1) },
                modifier = Modifier.size(34.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Plus", modifier = Modifier.size(18.dp))
            }
        }
    }
}
