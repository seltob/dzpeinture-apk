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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.dzpeinture.app.data.PaletteRepository
import com.dzpeinture.app.ui.theme.DzEmerald
import com.dzpeinture.app.ui.theme.DzNavy

private enum class PaletteSource(val label: String) {
    TENDANCES("Tendances"),
    NCS("Code NCS"),
    SITE("Nuancier Site")
}

@Composable
fun PaletteScreen(
    selectedShade: PaintShade?,
    onSelectShadeForOrder: (PaintShade) -> Unit
) {
    val context = LocalContext.current
    var activeSource by remember { mutableStateOf(PaletteSource.TENDANCES) }
    var activeFilter by remember { mutableStateOf("Toutes") }
    var searchQuery by remember { mutableStateOf("") }
    var previewShade by remember { mutableStateOf<PaintShade?>(null) }

    var ncsList by remember { mutableStateOf<List<PaintShade>>(emptyList()) }
    var siteList by remember { mutableStateOf<List<PaintShade>>(emptyList()) }
    var isLoadingAssets by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        ncsList = PaletteRepository.loadNcsPalette(context)
        siteList = PaletteRepository.loadSitePalette(context)
        isLoadingAssets = false
    }

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

    val currentSourceList = remember(activeSource, ncsList, siteList) {
        when (activeSource) {
            PaletteSource.TENDANCES -> DzPeintureData.shades
            PaletteSource.NCS -> ncsList
            PaletteSource.SITE -> siteList
        }
    }

    val filterOptions = remember(activeSource, currentSourceList) {
        when (activeSource) {
            PaletteSource.TENDANCES -> listOf("Toutes", "Salon", "Chambre", "Cuisine / SDB", "Façade")
            PaletteSource.NCS -> listOf("Toutes") + currentSourceList.map { it.roomTag }.distinct()
            PaletteSource.SITE -> listOf("Toutes", "LEOMIX PRIME", "LEOMIX 1000", "LEOMIX DECO", "LEOMIX 260")
        }
    }

    val filtered = remember(currentSourceList, activeFilter, searchQuery) {
        val q = searchQuery.trim().lowercase()
        val byFilter = if (activeFilter == "Toutes") currentSourceList
        else currentSourceList.filter { it.roomTag == activeFilter }

        val res = if (q.isEmpty()) byFilter
        else byFilter.filter {
            it.code.lowercase().contains(q) ||
                it.name.lowercase().contains(q) ||
                it.roomTag.lowercase().contains(q)
        }
        res
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
                Text(
                    text = "Nuancier Officiel (${filtered.size} teintes)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DzNavy
                )
            }

            Spacer(Modifier.height(8.dp))

            // Sélecteur de palette : Code NCS | Nuancier Site (3820) | Tendances
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                PaletteSource.entries.forEach { src ->
                    val countLabel = when (src) {
                        PaletteSource.TENDANCES -> "${src.label} (${DzPeintureData.shades.size})"
                        PaletteSource.NCS -> "${src.label} (${ncsList.size})"
                        PaletteSource.SITE -> "${src.label} (${siteList.size})"
                    }
                    FilterChip(
                        selected = activeSource == src,
                        onClick = {
                            activeSource = src
                            activeFilter = "Toutes"
                        },
                        label = { Text(countLabel, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            // Boutons Simulateur Photo (Caméra / Galerie)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { cameraLauncher.launch(null) },
                    colors = ButtonDefaults.buttonColors(containerColor = DzNavy),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Photo du mur", fontWeight = FontWeight.SemiBold, maxLines = 1)
                }
                OutlinedButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Galerie", fontWeight = FontWeight.SemiBold, maxLines = 1)
                }
            }

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Code NCS (S 1005-Y20R), LPC 0972, Vanilla...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Effacer")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filterOptions.forEach { opt ->
                    FilterChip(
                        selected = activeFilter == opt,
                        onClick = { activeFilter = opt },
                        label = { Text(opt) }
                    )
                }
            }
        }

        if (isLoadingAssets && filtered.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = DzNavy)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filtered, key = { "${it.roomTag}_${it.code}_${it.hex}" }) { shade ->
                    val isSelected = selectedShade?.code == shade.code
                    val swatchColor = Color(shade.hex)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (isSelected) Modifier.border(2.dp, DzEmerald, RoundedCornerShape(14.dp))
                                else Modifier
                            )
                            .clickable { previewShade = shade }
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(92.dp)
                                    .background(swatchColor)
                                    .padding(9.dp)
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
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(DzEmerald)
                                            .align(Alignment.TopEnd),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = "Sélectionnée",
                                            tint = Color.White,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                            }
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = shade.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = shade.recommendedFinish,
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
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = fgColor.copy(alpha = 0.9f)
                    )
                    Text(
                        text = shade.name,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = fgColor
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "${shade.roomTag} • ${shade.recommendedFinish}",
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
                            text = "Code Machine à Teinter : ${shade.code}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = DzNavy
                        )
                        Text(
                            text = "Supplément teinte (finitions) : ${shade.supplementLabel}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = DzEmerald
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
        val simPaletteStrip = remember(activeSource, ncsList, filtered) {
            if (filtered.isNotEmpty()) filtered.take(60)
            else if (ncsList.isNotEmpty()) ncsList.take(60)
            else DzPeintureData.shades
        }
        Dialog(
            onDismissRequest = { showSimulator = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF111827))
            ) {
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
                                text = if (showOriginal) "AVANT (Original)" else "${simShade.code} • ${simShade.name}",
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
                            items(simPaletteStrip, key = { "${it.roomTag}_${it.code}" }) { sh ->
                                val active = sh.code == simShade.code
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .width(68.dp)
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
                                        text = sh.code,
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
                            Text("Commander ${simShade.code}", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
