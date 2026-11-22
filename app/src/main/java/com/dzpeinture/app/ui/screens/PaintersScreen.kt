package com.dzpeinture.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dzpeinture.app.data.PainterProfile
import com.dzpeinture.app.data.PaintersRepository
import com.dzpeinture.app.ui.theme.DzEmerald
import com.dzpeinture.app.ui.theme.DzGold
import com.dzpeinture.app.ui.theme.DzNavy
import kotlinx.coroutines.launch

@Composable
fun PaintersScreen(
    onCallPhone: (String) -> Unit,
    onWhatsAppPhone: (String, String) -> Unit,
    onShowMessage: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    var painters by remember { mutableStateOf(PaintersRepository.fallbackPainters) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedCommune by remember { mutableStateOf("Toutes") }
    var activePainterForReviews by remember { mutableStateOf<PainterProfile?>(null) }
    var showRegisterDialog by remember { mutableStateOf(false) }

    fun refreshPainters() {
        scope.launch {
            isLoading = true
            PaintersRepository.fetchPainters()
                .onSuccess { list ->
                    if (list.isNotEmpty()) painters = list
                }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        refreshPainters()
    }

    val communes = remember(painters) {
        listOf("Toutes") + painters.map { it.commune }.distinct()
    }
    val filtered = remember(painters, selectedCommune) {
        if (selectedCommune == "Toutes") painters
        else painters.filter { it.commune == selectedCommune }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DzNavy),
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
                            text = "Peintres Pro DZpeinture",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        IconButton(onClick = { refreshPainters() }) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Icon(Icons.Default.Refresh, contentDescription = "Actualiser", tint = Color.White)
                            }
                        }
                    }
                    Text(
                        text = "Des peintres recommandés et notés par les propriétaires à Alger. Notez votre peintre après votre chantier !",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFD6E4F0)
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { showRegisterDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = DzEmerald),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Vous êtes Peintre Pro ? Inscription gratuite", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                communes.forEach { commune ->
                    FilterChip(
                        selected = selectedCommune == commune,
                        onClick = { selectedCommune = commune },
                        label = { Text(commune) }
                    )
                }
            }
        }

        items(filtered, key = { it.id }) { painter ->
            PainterCard(
                painter = painter,
                onCall = { onCallPhone(painter.phone) },
                onWhatsApp = {
                    onWhatsAppPhone(
                        painter.phone,
                        "Bonjour ${painter.name}, je vous contacte via l'application DZpeinture pour un chantier de peinture."
                    )
                },
                onOpenReviews = { activePainterForReviews = painter }
            )
        }

        item { Spacer(Modifier.height(12.dp)) }
    }

    // Dialog Avis & Notation ⭐
    activePainterForReviews?.let { painter ->
        PainterReviewsDialog(
            painter = painter,
            onDismiss = { activePainterForReviews = null },
            onSubmitReview = { author, rating, comment ->
                scope.launch {
                    PaintersRepository.submitReview(painter.id, author, rating, comment)
                        .onSuccess { updated ->
                            painters = painters.map { if (it.id == updated.id) updated else it }
                                .sortedWith(compareByDescending<PainterProfile> { it.verified }
                                    .thenByDescending { it.avgRating }
                                    .thenByDescending { it.reviewsCount })
                            activePainterForReviews = updated
                            onShowMessage("Merci ! Votre avis ($rating ★) a été publié.")
                        }
                        .onFailure { err ->
                            onShowMessage(err.message ?: "Impossible d'envoyer l'avis (vérifiez internet).")
                        }
                }
            }
        )
    }

    // Dialog Inscription Peintre Pro
    if (showRegisterDialog) {
        RegisterPainterDialog(
            onDismiss = { showRegisterDialog = false },
            onSubmit = { name, commune, phone, specialty, years ->
                scope.launch {
                    PaintersRepository.registerPainter(name, commune, phone, specialty, years)
                        .onSuccess { msg ->
                            showRegisterDialog = false
                            onShowMessage(msg)
                        }
                        .onFailure { err ->
                            onShowMessage(err.message ?: "Erreur lors de l'inscription.")
                        }
                }
            }
        )
    }
}

@Composable
private fun PainterCard(
    painter: PainterProfile,
    onCall: () -> Unit,
    onWhatsApp: () -> Unit,
    onOpenReviews: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(DzNavy.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = painter.name.take(1).uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = DzNavy
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = painter.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (painter.verified) {
                            Spacer(Modifier.width(6.dp))
                            Icon(
                                Icons.Default.Verified,
                                contentDescription = "Vérifié DZpeinture",
                                tint = DzEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(
                            text = "${painter.commune} • ${painter.yearsExp} ans d'exp.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = DzGold, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(2.dp))
                        Text(
                            text = if (painter.reviewsCount > 0) String.format("%.1f", painter.avgRating) else "Nouveau",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = DzNavy
                        )
                    }
                    Text(
                        text = "${painter.reviewsCount} avis",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = painter.specialty,
                style = MaterialTheme.typography.bodySmall,
                color = DzNavy,
                fontWeight = FontWeight.Medium
            )

            painter.reviews.firstOrNull()?.let { latest ->
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF8FAFC))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "« ${latest.comment} » — ${latest.author}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = onCall,
                    colors = ButtonDefaults.buttonColors(containerColor = DzNavy),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Appeler")
                }
                Button(
                    onClick = onWhatsApp,
                    colors = ButtonDefaults.buttonColors(containerColor = DzEmerald),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("WhatsApp")
                }
                OutlinedButton(
                    onClick = onOpenReviews,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = DzGold, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Noter")
                }
            }
        }
    }
}

@Composable
private fun PainterReviewsDialog(
    painter: PainterProfile,
    onDismiss: () -> Unit,
    onSubmitReview: (String, Int, String) -> Unit
) {
    var author by remember { mutableStateOf("") }
    var rating by remember { mutableIntStateOf(0) }
    var comment by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(painter.name, fontWeight = FontWeight.Bold)
                Text(
                    text = "${painter.commune} • ${String.format("%.1f", painter.avgRating)} ★ (${painter.reviewsCount} avis)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    Text("Donner votre avis sur ce peintre :", fontWeight = FontWeight.Bold, color = DzNavy)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = author,
                        onValueChange = { author = it },
                        label = { Text("Votre prénom / nom") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        label = { Text("Votre commentaire (qualité, délais, propreté...)") },
                        minLines = 2,
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = if (rating == 0) "Note obligatoire (touchez de 1 à 5 étoiles) *" else "Votre note : $rating / 5 ★",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (rating == 0) Color(0xFFDC2626) else DzEmerald
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                        (1..5).forEach { star ->
                            Icon(
                                imageVector = if (star <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "$star étoiles",
                                tint = if (star <= rating) DzGold else Color(0xFF9CA3AF),
                                modifier = Modifier
                                    .size(38.dp)
                                    .clickable { rating = star }
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = {
                            if (rating >= 1 && comment.isNotBlank()) {
                                onSubmitReview(author.ifBlank { "Client Alger" }, rating, comment.trim())
                                comment = ""
                                rating = 0
                            }
                        },
                        enabled = rating >= 1 && comment.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = DzEmerald),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (rating >= 1) "Publier mon avis ($rating ★)" else "Choisissez au moins 1 étoile ★",
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(8.dp))
                    Text("Avis clients (${painter.reviews.size}) :", fontWeight = FontWeight.Bold, color = DzNavy)
                }

                items(painter.reviews, key = { it.id }) { rev ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(rev.author, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                                Text("★".repeat(rev.rating), color = DzGold, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(rev.comment, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Fermer") }
        }
    )
}

@Composable
private fun RegisterPainterDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, String, String, String, Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var commune by remember { mutableStateOf("El Achour / Oued Romane") }
    var specialty by remember { mutableStateOf("Peinture Intérieure & Façade") }
    var years by remember { mutableStateOf("5") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Inscription Peintre Pro", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Rejoignez gratuitement l'annuaire officiel DZpeinture et recevez des chantiers à Alger !",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom complet *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Téléphone / WhatsApp *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = commune,
                    onValueChange = { commune = it },
                    label = { Text("Commune principale (ex: Chéraga, Hydra...)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = specialty,
                    onValueChange = { specialty = it },
                    label = { Text("Spécialité (Intérieur, Façade, Déco...)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = years,
                    onValueChange = { years = it },
                    label = { Text("Années d'expérience") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSubmit(
                        name.trim(),
                        commune.trim().ifBlank { "Alger" },
                        phone.trim(),
                        specialty.trim().ifBlank { "Peinture Intérieure & Façade" },
                        years.toIntOrNull() ?: 3
                    )
                },
                enabled = name.isNotBlank() && phone.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = DzEmerald)
            ) {
                Text("Envoyer ma demande")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}
