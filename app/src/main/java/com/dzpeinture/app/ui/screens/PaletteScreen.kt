package com.dzpeinture.app.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current
    val rooms = listOf("Toutes", "Salon", "Chambre", "Cuisine / SDB", "Façade")
    var activeRoom by remember { mutableStateOf("Toutes") }
    var searchQuery by remember { mutableStateOf("") }
    var previewShade by remember { mutableStateOf<PaintShade?>(null) }

    // Simulateur couleur sur photo
    var showSimulator by remember { mutableStateOf(false) }
    var wallBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var simShade by remember { mutableStateOf(selectedShade ?: DzPeintureData.shades.first()) }
    var simIntensity by remember { mutableFloatStateOf(0.55f) }
    var showOriginal by remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            wallBitmap = bitmap
            showSimulator = true
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            runCatching {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
            }.getOrNull()?.let { bmp ->
                wallBitmap = bmp
                showSimulator = true
            }
        }
    }

    val filtered = remember(activeRoom, searchQuery) {
        val byRoom = if (activeRoom == "Toutes") DzPeintureData.shades
        else DzPeintureData.shades.filter { it.roomTag == activeRoom }
        val q = searchQuery.trim().lowercase()
        if (q.isEmpty()) byRoom
        else byRoom.filter {
            it.name.lowercase().contains(q) ||
                it.code.lowercase().contains(q) ||
                it.recommendedFinish.lowercase().contains(q)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Palette Machine à Teinter (${DzPeintureData.shades.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DzNavy
                    )
                    Text(
                        text = "Testez une couleur sur votre mur ou en plein écran.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Boutons Simulateur Photo (Caméra / Galerie)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { cameraLauncher.launch(null) },
                    colors = ButtonDefaults.buttonColors(containerColor = DzNavy),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Photo du mur", fontWeight = FontWeight.SemiBold, maxLines = 1)
                }
                OutlinedButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Galerie", fontWeight = FontWeight.SemiBold, maxLines = 1)
                }
            }

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Rechercher (ex: Vanilla, Breeze, DZP-101...)") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

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
                                .height(100.dp)
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

    // Dialog 1 : Aperçu Plein Écran d'une teinte
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
                            text = "Préparée en 3 min à la machine à teinter DZpeinture.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    simShade = shade
                                    previewShade = null
                                    cameraLauncher.launch(null)
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Sur mon mur")
                            }
                            Button(
                                onClick = {
                                    onSelectShadeForOrder(shade)
                                    previewShade = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DzEmerald),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1.2f)
                            ) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Choisir", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog 2 : Simulateur Couleur sur Photo du Mur
    if (showSimulator && wallBitmap != null) {
        val bmp = wallBitmap!!
        val tintColor = Color(simShade.hex)
        Dialog(
            onDismissRequest = { showSimulator = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF111827))
            ) {
                // Zone Photo + Teinte réaliste
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "Photo du mur",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    if (!showOriginal) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawRect(
                                color = tintColor.copy(alpha = simIntensity),
                                blendMode = BlendMode.Multiply
                            )
                            drawRect(
                                color = tintColor.copy(alpha = simIntensity * 0.28f)
                            )
                        }
                    }

                    // Boutons haut
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                            .align(Alignment.TopCenter),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.65f)),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = if (showOriginal) "AVANT (Original)" else "APRÈS : ${simShade.name}",
                                color = Color.White,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                        IconButton(
                            onClick = { showSimulator = false },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.55f))
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Fermer", tint = Color.White)
                        }
                    }
                }

                // Panneau de contrôle bas
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Intensité : ${(simIntensity * 100).toInt()}%",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = DzNavy
                            )
                            FilterChip(
                                selected = showOriginal,
                                onClick = { showOriginal = !showOriginal },
                                leadingIcon = { Icon(Icons.Default.Compare, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                label = { Text(if (showOriginal) "Voir teinte" else "Voir avant") }
                            )
                        }

                        Slider(
                            value = simIntensity,
                            onValueChange = {
                                showOriginal = false
                                simIntensity = it
                            },
                            valueRange = 0.15f..0.85f
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            items(DzPeintureData.shades, key = { it.code }) { sh ->
                                val active = sh.code == simShade.code
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .width(64.dp)
                                        .clickable {
                                            simShade = sh
                                            showOriginal = false
                                        }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(Color(sh.hex))
                                            .border(
                                                width = if (active) 3.dp else 1.dp,
                                                color = if (active) DzEmerald else Color(0xFFD1D5DB),
                                                shape = CircleShape
                                            )
                                    )
                                    Spacer(Modifier.height(3.dp))
                                    Text(
                                        text = sh.name,
                                        style = MaterialTheme.typography.labelSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(10.dp))
                        Button(
                            onClick = {
                                onSelectShadeForOrder(simShade)
                                showSimulator = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DzEmerald),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Commander ${simShade.name} (${simShade.code})", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
