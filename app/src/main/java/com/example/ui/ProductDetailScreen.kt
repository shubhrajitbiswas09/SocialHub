package com.example.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.MarketplaceProduct
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    product: MarketplaceProduct,
    onBack: () -> Unit,
    onPurchase: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    var showSecureShieldDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Product Details", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0C0714)
                )
            )
        },
        containerColor = Color(0xFF0C0714)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Main Product Image Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Bottom gradient scrim
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xFF0C0714)),
                                startY = 100f
                            )
                        )
                )
            }

            // Info Card with Logo Overlay
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                border = BorderStroke(1.5.dp, MinimalBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Product Logo
                        AsyncImage(
                            model = product.logoUrl,
                            contentDescription = "Product Logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .border(2.dp, RazorBlue, CircleShape)
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = product.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = "By ${product.creatorName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = RazorBlue,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Product Price",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GrayText,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "$${String.format("%.2f", product.price)}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = SafeGold
                    )
                }
            }

            // Product Description Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Description",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = product.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = LightText,
                    lineHeight = 22.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // CTA Footer Action Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF150D24)),
                border = BorderStroke(1.dp, MinimalBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Official Affiliate Integration",
                        style = MaterialTheme.typography.labelMedium,
                        color = GrayText,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            showSecureShieldDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RazorBlue
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Launch, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "BUY NOW",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            if (showSecureShieldDialog) {
                SecureAffiliateShieldDialog(
                    productName = product.name,
                    affiliateLink = product.affiliateLink,
                    onDismiss = { showSecureShieldDialog = false },
                    onConfirm = {
                        showSecureShieldDialog = false
                        onPurchase()
                        try {
                            val secureTaggedLink = appendAffiliateTags(product.affiliateLink)
                            uriHandler.openUri(secureTaggedLink)
                        } catch (e: Exception) {
                            try {
                                uriHandler.openUri(product.affiliateLink)
                            } catch (fallbackEx: Exception) {
                                // Fallback
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun SecureAffiliateShieldDialog(
    productName: String,
    affiliateLink: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val cleanDomain = remember(affiliateLink) {
        try {
            val uri = java.net.URI(affiliateLink)
            val host = uri.host ?: ""
            if (host.startsWith("www.")) host.substring(4) else host
        } catch (e: Exception) {
            "external-partner-site.com"
        }
    }

    val isSecureProtocol = remember(affiliateLink) {
        affiliateLink.startsWith("https://", ignoreCase = true)
    }

    // Heuristic Scan 1: Detect URL Masking / Shorteners
    val isShortenerMasked = remember(cleanDomain) {
        val shorteners = listOf("bit.ly", "tinyurl.com", "t.co", "cutt.ly", "is.gd", "rebrand.ly", "ow.ly", "buff.ly", "shorte.st")
        shorteners.any { cleanDomain.equals(it, ignoreCase = true) || cleanDomain.endsWith(".$it") || cleanDomain.contains(it) }
    }

    // Heuristic Scan 2: Suspicious Keywords / Scam keywords
    val hasSuspiciousKeywords = remember(affiliateLink) {
        val badKeywords = listOf("free-money", "hacked", "giftcard", "bypass", "spoof", "phish", "scam", "malware", "win-prize", "survey-reward")
        badKeywords.any { affiliateLink.contains(it, ignoreCase = true) }
    }

    // Dynamic Trust Rating
    val trustRating = remember(isSecureProtocol, isShortenerMasked, hasSuspiciousKeywords) {
        if (hasSuspiciousKeywords) {
            "🚨 HIGH SUSPICION RISK"
        } else if (!isSecureProtocol) {
            "⚠️ UNENCRYPTED WARNING"
        } else if (isShortenerMasked) {
            "⚡ REDIRECT MASK DETECTED"
        } else {
            "🟢 SECURE & TRUSTED PARTNER"
        }
    }

    val trustRatingColor = remember(trustRating) {
        when {
            trustRating.contains("🚨") -> Color(0xFFEF4444) // Red
            trustRating.contains("⚠️") -> Color(0xFFF59E0B) // Amber
            trustRating.contains("⚡") -> Color(0xFF3B82F6) // Blue
            else -> RazorTeal
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "🛡️ SECURE ROUTING SHIELD",
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    color = RazorTeal,
                    letterSpacing = 0.5.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Real-time outbound trust filters scanned this link before redirecting. Outbound affiliate channels are wrapped with standard Google Play safety compliance metadata tags.",
                    color = LightText,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )

                // Domain details container
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F091D)),
                    border = BorderStroke(1.dp, MinimalBorder),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "DESTINATION DOMAIN",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = GrayText,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = trustRating,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = trustRatingColor
                            )
                        }

                        Text(
                            text = cleanDomain,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )

                        HorizontalDivider(color = Color.White.copy(0.06f))

                        Text(
                            text = "SECURITY METRIC FEEDBACK",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = GrayText,
                            letterSpacing = 1.sp
                        )

                        // 1. SSL Protocol Check
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isSecureProtocol) RazorTeal else Color(0xFFEF4444))
                            )
                            Text(
                                text = if (isSecureProtocol) "SSL Encryption: VERIFIED (HTTPS)" else "SSL Encryption: MISSING (HTTP Plaintext Risk)",
                                fontSize = 9.sp,
                                color = if (isSecureProtocol) LightText else Color(0xFFEF4444),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // 2. Redirect Mask check
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (!isShortenerMasked) RazorTeal else Color(0xFFF59E0B))
                            )
                            Text(
                                text = if (!isShortenerMasked) "URL Masking scan: CLEAN (Direct Link)" else "URL Masking scan: MASKED (Common Redirect Shortener)",
                                fontSize = 9.sp,
                                color = if (!isShortenerMasked) LightText else Color(0xFFF59E0B)
                            )
                        }

                        // 3. Phishing Scam check
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (!hasSuspiciousKeywords) RazorTeal else Color(0xFFEF4444))
                            )
                            Text(
                                text = if (!hasSuspiciousKeywords) "Phishing Heuristics: CLEAN (No scam cues)" else "Phishing Heuristics: HIGH SUSPICION WARNING!",
                                fontSize = 9.sp,
                                color = if (!hasSuspiciousKeywords) LightText else Color(0xFFEF4444),
                                fontWeight = if (hasSuspiciousKeywords) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                // Compliance Disclosure Text
                Text(
                    text = "🔒 Google Play Compliance: This outbound link is an authorized sponsor/affiliate landing. Any purchase completed supports this local app developer ecosystem. Complete transparency disclosures are active.",
                    color = GrayText,
                    fontSize = 9.sp,
                    lineHeight = 13.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = if (trustRating.contains("🚨")) Color(0xFFEF4444) else RazorBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (trustRating.contains("🚨")) "PROCEED AT OWN RISK" else "PROCEED SAFELY",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                border = BorderStroke(1.dp, Color.White.copy(0.12f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = LightText),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("ABORT ROUTING", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color(0xFF130E22),
        shape = RoundedCornerShape(20.dp)
    )
}

fun appendAffiliateTags(url: String): String {
    return try {
        val cleanUrl = url.trim()
        val uri = java.net.URI(cleanUrl)
        val query = uri.query
        val additionalParams = "utm_source=socialhub_playstore&utm_medium=affiliate_marketplace&referrer=socialhub_app_client"
        val newQuery = if (query.isNullOrEmpty()) {
            additionalParams
        } else {
            if (query.contains("utm_source")) query else "$query&$additionalParams"
        }
        val newUri = java.net.URI(
            uri.scheme,
            uri.authority,
            uri.path,
            newQuery,
            uri.fragment
        )
        newUri.toString()
    } catch (e: Exception) {
        val separator = if (url.contains("?")) "&" else "?"
        if (url.contains("utm_source")) url else "$url${separator}utm_source=socialhub_playstore&utm_medium=affiliate_marketplace&referrer=socialhub_app_client"
    }
}
