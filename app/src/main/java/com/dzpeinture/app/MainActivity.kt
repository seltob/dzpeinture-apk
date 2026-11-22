package com.dzpeinture.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dzpeinture.app.data.CartEntry
import com.dzpeinture.app.data.DzPeintureData
import com.dzpeinture.app.data.PaintShade
import com.dzpeinture.app.ui.screens.CatalogueOrderScreen
import com.dzpeinture.app.ui.screens.DevisScreen
import com.dzpeinture.app.ui.screens.PaintersScreen
import com.dzpeinture.app.ui.screens.PaletteScreen
import com.dzpeinture.app.ui.screens.StoresScreen
import com.dzpeinture.app.ui.theme.DzNavy
import com.dzpeinture.app.ui.theme.DzPeintureTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DzPeintureTheme {
                DzPeintureApp()
            }
        }
    }
}

private data class NavTab(val label: String, val icon: ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DzPeintureApp() {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var currentTab by remember { mutableIntStateOf(0) }
    var selectedShade by remember { mutableStateOf<PaintShade?>(null) }
    var returnTabAfterPalette by remember { mutableIntStateOf(3) }
    val cart = remember { mutableStateMapOf<String, CartEntry>() }

    val totalCartItems = cart.values.sumOf { it.buckets + (if (it.extraKg > 0) 1 else 0) }

    val tabs = listOf(
        NavTab("Devis", Icons.Default.Calculate),
        NavTab("Palette", Icons.Default.Palette),
        NavTab("Peintres", Icons.Default.Engineering),
        NavTab("Panier", Icons.Default.ShoppingCart),
        NavTab("Magasins", Icons.Default.LocationOn)
    )

    fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }

    fun normalizeDzWhatsAppPhone(phone: String): String {
        val digits = phone.filter { it.isDigit() }
        return when {
            digits.startsWith("213") -> digits
            digits.startsWith("0") && digits.length >= 9 -> "213" + digits.drop(1)
            else -> DzPeintureData.WHATSAPP_NUMBER
        }
    }

    fun openWhatsApp(message: String) {
        val encoded = Uri.encode(message)
        openUrl("https://wa.me/${DzPeintureData.WHATSAPP_NUMBER}?text=$encoded")
    }

    fun openWhatsAppToPhone(phone: String, message: String) {
        val waNum = normalizeDzWhatsAppPhone(phone)
        val encoded = Uri.encode(message)
        openUrl("https://wa.me/$waNum?text=$encoded")
    }

    fun callPhone(phone: String) {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
        context.startActivity(intent)
    }

    fun callDirect() {
        callPhone(DzPeintureData.PHONE_ORDERS)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(R.drawable.dzp_logo),
                            contentDescription = "Logo DZpeinture",
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "DZpeinture",
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DzNavy)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = currentTab == index,
                        onClick = { currentTab = index },
                        icon = {
                            if (index == 3 && totalCartItems > 0) {
                                BadgedBox(badge = { Badge { Text(totalCartItems.toString()) } }) {
                                    Icon(tab.icon, contentDescription = tab.label)
                                }
                            } else {
                                Icon(tab.icon, contentDescription = tab.label)
                            }
                        },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                0 -> DevisScreen(
                    selectedShade = selectedShade,
                    onOpenPalette = {
                        returnTabAfterPalette = 0
                        currentTab = 1
                    },
                    onResetToWhite = { selectedShade = null },
                    onAddMethodToCart = { method ->
                        cart.clear()
                        method.lines.forEach { line ->
                            if (line.bucketsCount > 0 || line.extraKgCount > 0) {
                                cart[line.product.id] = CartEntry(
                                    product = line.product,
                                    buckets = line.bucketsCount,
                                    extraKg = line.extraKgCount
                                )
                            }
                        }
                        currentTab = 3
                        scope.launch {
                            snackbarHostState.showSnackbar("${method.title} ajoutée au panier !")
                        }
                    },
                    onSendWhatsApp = ::openWhatsApp
                )

                1 -> PaletteScreen(
                    selectedShade = selectedShade,
                    onSelectShadeForOrder = { shade ->
                        selectedShade = shade
                        currentTab = returnTabAfterPalette
                        scope.launch {
                            snackbarHostState.showSnackbar("Teinte ${shade.code} (${shade.supplementLabel}) sélectionnée !")
                        }
                    }
                )

                2 -> PaintersScreen(
                    onCallPhone = ::callPhone,
                    onWhatsAppPhone = ::openWhatsAppToPhone,
                    onShowMessage = { msg ->
                        scope.launch { snackbarHostState.showSnackbar(msg) }
                    }
                )

                3 -> CatalogueOrderScreen(
                    cart = cart,
                    selectedShade = selectedShade,
                    onOpenPalette = {
                        returnTabAfterPalette = 3
                        currentTab = 1
                    },
                    onResetToWhite = { selectedShade = null },
                    onUpdateCart = { product, buckets, extraKg ->
                        if (buckets <= 0 && extraKg <= 0) {
                            cart.remove(product.id)
                        } else {
                            cart[product.id] = CartEntry(product, buckets, extraKg)
                        }
                    },
                    onClearCart = { cart.clear() },
                    onSendOrderWhatsApp = ::openWhatsApp,
                    onCallDirect = ::callDirect
                )

                4 -> StoresScreen(
                    onOpenUrl = ::openUrl,
                    onCallDirect = ::callDirect,
                    onOpenWhatsApp = ::openWhatsApp
                )
            }
        }
    }
}
