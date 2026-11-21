package com.dzpeinture.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.dzpeinture.app.data.DzPeintureData
import com.dzpeinture.app.data.DzPeintureData.contrastText
import com.dzpeinture.app.data.PaintShade
import com.dzpeinture.app.ui.theme.DzEmerald
import com.dzpeinture.app.ui.theme.DzNavy

@Composable
fun PaletteScreen(
    selectedShade: PaintShade?,
    onSelectShadeForOrder: (PaintShade) -> Unit
) {
    val rooms = listOf("Toutes", "Salon", "Chambre", "Cuisine / SDB", "Façade")
    var activeRoom by remember { mutableStateOf("Toutes") }
    var previewShade by remember { mutableStateOf<PaintShade?>(null) }

    val filtered = remember(activeRoom) {
        if (activeRoom == "Toutes") DzPeintureData.shades
        else DzPeintureData.shades.filter { it.roomTag == activeRoom }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Palette Machine à Teinter DZpeinture",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DzNavy
            )
            Text(
                text = "Touchez une couleur pour la voir en plein écran (effet mur).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rooms.forEach { room ->
                    FilterChip(
                        selected = activeRoom == room,
                        onClick = { activeRoom = room },
                        label = { Text(room) }
                    )
                }
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filtered, key = { it.code }) { shade ->
                val isSelected = selectedShade?.code == shade.code
                val swatchColor = Color(shade.hex)
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (isSelected) Modifier.border(2.dp, DzEmerald, RoundedCornerShape(16.dp))
                            else Modifier
                        )
                        .clickable { previewShade = shade }
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(105.dp)
                                .background(swatchColor)
                                .padding(10.dp)
                        ) {
                            Text(
                                text = shade.code,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = swatchColor.contrastText(),
                                modifier = Modifier.align(Alignment.TopStart)
                            )
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(DzEmerald)
                                        .align(Alignment.TopEnd),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Sélectionnée",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = shade.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "${shade.roomTag} • ${shade.recommendedFinish}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }

    previewShade?.let { shade ->
        val wallColor = Color(shade.hex)
        val fgColor = wallColor.contrastText()
        Dialog(
            onDismissRequest = { previewShade = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(wallColor)
                    .padding(20.dp)
            ) {
                IconButton(
                    onClick = { previewShade = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.25f))
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Fermer", tint = Color.White)
                }

                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = shade.code,
                        style = MaterialTheme.typography.labelLarge,
                        color = fgColor.copy(alpha = 0.85f)
                    )
                    Text(
                        text = shade.name,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = fgColor
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Aperçu plein écran • Idéal ${shade.roomTag}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = fgColor.copy(alpha = 0.85f)
                    )
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Finition conseillée : ${shade.recommendedFinish}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = DzNavy
                        )
                        Text(
                            text = "Préparée sur place en 3 minutes à la machine à teinter DZpeinture.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = {
                                onSelectShadeForOrder(shade)
                                previewShade = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DzEmerald),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Choisir cette teinte pour ma commande", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
