package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.RwandaReferenceData
import com.example.data.local.AdvertisementEntity
import com.example.data.local.SystemSettingsEntity
import com.example.data.security.SecurityUtils
import com.example.ui.components.*
import com.example.ui.theme.*

enum class PublicSection(val label: String) {
    HOME("Home"),
    ABOUT("About"),
    SERVICES("Services"),
    ADS("Advertisements"),
    FAQ("FAQ"),
    CONTACT("Contact Us"),
    LEGAL("Terms & Privacy")
}

@Composable
fun PublicPortalScreen(
    settings: SystemSettingsEntity,
    advertisements: List<AdvertisementEntity>,
    onNavigateLogin: () -> Unit,
    onNavigateRegister: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeSection by remember { mutableStateOf(PublicSection.HOME) }
    var selectedServiceId by remember { mutableStateOf("saving") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(rememberAnimatedFintechGradient())
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        EFinanceLogoMark()
                        Column {
                            Text(
                                settings.platformName,
                                style = MaterialTheme.typography.titleMedium,
                                color = PureWhite,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                "Rwanda Digital Finance Platform",
                                style = MaterialTheme.typography.labelMedium,
                                color = Cyan400
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onNavigateLogin,
                            border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.6f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PureWhite),
                            modifier = Modifier.testTag("public_login_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Login")
                        }
                        Button(
                            onClick = onNavigateRegister,
                            colors = ButtonDefaults.buttonColors(containerColor = Gold500, contentColor = DeepNavy950),
                            modifier = Modifier.testTag("public_register_button")
                        ) {
                            Text("Join", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PublicSection.entries.forEach { section ->
                        val selected = activeSection == section
                        FilterChip(
                            selected = selected,
                            onClick = { activeSection = section },
                            label = {
                                Text(
                                    section.label,
                                    color = if (selected) DeepNavy950 else PureWhite
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Gold400,
                                containerColor = DeepNavy800.copy(alpha = 0.65f)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selected,
                                borderColor = PureWhite.copy(alpha = 0.25f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (settings.maintenanceMode) {
                item {
                    Surface(
                        color = GoldSoftCard(),
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Filled.Build, contentDescription = null, tint = Gold600)
                            Text(
                                "Platform Maintenance Notice: Scheduled upgrades are active. Public information remains accessible.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = DeepNavy900
                            )
                        }
                    }
                }
            }

            when (activeSection) {
                PublicSection.HOME -> {
                    item {
                        PublicHeroCard(
                            settings = settings,
                            onCreateAccount = onNavigateRegister,
                            onExploreServices = { activeSection = PublicSection.SERVICES }
                        )
                    }
                    item {
                        ComplianceTrustStrip(settings = settings)
                    }
                    item {
                        PublicRwfTransparencyCalculator(
                            withdrawalFeePercent = settings.withdrawalFeePercent,
                            activationFeeRwf = settings.activationFeeRwf,
                            welcomeBonusRwf = settings.welcomeBonusRwf,
                            onGetStarted = onNavigateRegister
                        )
                    }
                    item {
                        Text(
                            "Featured Announcements & Campaigns",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    item {
                        if (advertisements.isEmpty()) {
                            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                                Text("No active advertisements at this moment.", style = MaterialTheme.typography.bodyMedium)
                            }
                        } else {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(advertisements, key = { it.id }) { ad ->
                                    PublicAdvertisementCard(
                                        ad = ad,
                                        onAction = onNavigateRegister,
                                        modifier = Modifier.width(290.dp)
                                    )
                                }
                            }
                        }
                    }
                    item {
                        Text(
                            "Explore E-FINANCE RWANDA Services",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    items(RwandaReferenceData.publicServices, key = { it.id }) { svc ->
                        ServiceOverviewCard(
                            title = svc.title,
                            subtitle = svc.subtitle,
                            summary = svc.summary,
                            onLearnMore = {
                                selectedServiceId = svc.id
                                activeSection = PublicSection.SERVICES
                            }
                        )
                    }
                    item { PublicFooterBanner(settings = settings, onOpenLegal = { activeSection = PublicSection.LEGAL }) }
                }

                PublicSection.ABOUT -> {
                    item {
                        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                            Text("About E-FINANCE RWANDA", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "E-FINANCE RWANDA is a digital financial technology platform built for Rwandan citizens, university students, A-Level scholars, and entrepreneurs. Our mission is to combine disciplined RWF savings, transparent credit workflows, KYC-secured Mobile Money withdrawals, and verified digital earning opportunities in one reliable environment.",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Regulatory & Sandbox Disclosure: Financial services operate under sandbox/demo controls until all applicable National Bank of Rwanda (BNR) authorizations and official payment gateway integrations are finalized.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Gold600
                            )
                        }
                    }
                    item { PublicFooterBanner(settings = settings, onOpenLegal = { activeSection = PublicSection.LEGAL }) }
                }

                PublicSection.SERVICES -> {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            RwandaReferenceData.publicServices.forEach { svc ->
                                FilterChip(
                                    selected = selectedServiceId == svc.id,
                                    onClick = { selectedServiceId = svc.id },
                                    label = { Text(svc.title.substringBefore(" ")) }
                                )
                            }
                        }
                    }
                    val activeSvc = RwandaReferenceData.publicServices.firstOrNull { it.id == selectedServiceId }
                        ?: RwandaReferenceData.publicServices.first()
                    item {
                        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                            Text(activeSvc.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            Text(activeSvc.subtitle, style = MaterialTheme.typography.titleMedium, color = Emerald600)
                            Spacer(Modifier.height(10.dp))
                            Text(activeSvc.summary, style = MaterialTheme.typography.bodyLarge)
                            Spacer(Modifier.height(12.dp))
                            activeSvc.highlights.forEach { h ->
                                Row(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Emerald600, modifier = Modifier.size(18.dp))
                                    Text(h, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            Button(onClick = onNavigateRegister, modifier = Modifier.fillMaxWidth()) {
                                Text("Create Account to Access ${activeSvc.title}")
                            }
                        }
                    }
                }

                PublicSection.ADS -> {
                    item {
                        Text("Active Platform Advertisements", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    }
                    items(advertisements, key = { it.id }) { ad ->
                        PublicAdvertisementCard(ad = ad, onAction = onNavigateRegister, modifier = Modifier.fillMaxWidth())
                    }
                }

                PublicSection.FAQ -> {
                    item {
                        Text("Frequently Asked Questions", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    }
                    items(RwandaReferenceData.faqItems) { faq ->
                        var expanded by remember { mutableStateOf(false) }
                        GlassmorphicCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expanded = !expanded }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(faq.question, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                                Icon(if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, contentDescription = null)
                            }
                            AnimatedVisibility(visible = expanded) {
                                Text(
                                    faq.answer,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }
                }

                PublicSection.CONTACT -> {
                    item {
                        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                            Text("Contact E-FINANCE RWANDA", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(10.dp))
                            ContactRow(Icons.Filled.Email, "Official Support Email", settings.contactEmail)
                            ContactRow(Icons.Filled.Phone, "Kigali Support Desk", settings.contactPhone)
                            ContactRow(Icons.Filled.LocationOn, "Headquarters", "KN 4 Ave, Nyarugenge, Kigali, Rwanda")
                            ContactRow(Icons.Filled.Schedule, "Operating Hours", "Mon - Sat • 08:00 - 18:00 CAT (Africa/Kigali)")
                            Spacer(Modifier.height(12.dp))
                            Button(onClick = onNavigateLogin, modifier = Modifier.fillMaxWidth()) {
                                Text("Sign In to Open Support Ticket")
                            }
                        }
                    }
                }

                PublicSection.LEGAL -> {
                    item {
                        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                            Text("Terms and Conditions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            Text(RwandaReferenceData.TERMS_AND_CONDITIONS.trim(), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    item {
                        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                            Text("Privacy & Data Protection Policy", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            Text(RwandaReferenceData.PRIVACY_POLICY.trim(), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GoldSoftCard(): Color = ColorTokens.GoldSoft

@Composable
private fun PublicHeroCard(
    settings: SystemSettingsEntity,
    onCreateAccount: () -> Unit,
    onExploreServices: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(DeepNavy900, RoyalBlue700, Purple700)
                    )
                )
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(shape = RoundedCornerShape(50), color = Gold500.copy(alpha = 0.22f)) {
                Text(
                    "TRUSTED RWANDA FINTECH • RWF",
                    style = MaterialTheme.typography.labelSmall,
                    color = Gold400,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
            Text(
                "Save, Earn, and Grow With Institutional Transparency.",
                style = MaterialTheme.typography.headlineLarge,
                color = PureWhite
            )
            Text(
                "Built for Rwandan citizens, students, and creators. Manage RWF savings vaults, verified Mobile Money withdrawals, responsible loans, paid video campaigns, and daily check-ins.",
                style = MaterialTheme.typography.bodyMedium,
                color = Slate200
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onCreateAccount,
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald500, contentColor = DeepNavy950),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Create Account", fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onExploreServices,
                    border = BorderStroke(1.dp, Cyan400),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PureWhite),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Explore Services")
                }
            }
            Text(
                "Activation Fee: ${SecurityUtils.formatRwf(settings.activationFeeRwf)} • Welcome Bonus: ${SecurityUtils.formatRwf(settings.welcomeBonusRwf)} • Daily Check-In: ${SecurityUtils.formatRwf(settings.dailyCheckinRewardRwf)}",
                style = MaterialTheme.typography.labelSmall,
                color = Cyan400
            )
        }
    }
}

@Composable
private fun PublicAdvertisementCard(
    ad: AdvertisementEntity,
    onAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gradient = when (ad.accentStyle) {
        "ROYAL_CYAN" -> listOf(RoyalBlue700, Cyan500)
        "EMERALD_TEAL" -> listOf(DeepNavy800, Emerald600)
        "PURPLE_INDIGO" -> listOf(Purple700, RoyalBlue600)
        else -> listOf(DeepNavy900, Gold600)
    }
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .background(Brush.linearGradient(gradient))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Surface(shape = RoundedCornerShape(50), color = PureWhite.copy(alpha = 0.2f)) {
                    Text(
                        ad.bannerType.replace("_", " "),
                        style = MaterialTheme.typography.labelSmall,
                        color = PureWhite,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Icon(
                    if (ad.bannerType == "PROMO_VIDEO") Icons.Filled.PlayCircle else Icons.Filled.Campaign,
                    contentDescription = null,
                    tint = Gold400
                )
            }
            Text(ad.title, style = MaterialTheme.typography.titleLarge, color = PureWhite, fontWeight = FontWeight.Bold)
            Text(ad.description, style = MaterialTheme.typography.bodyMedium, color = PureWhite.copy(alpha = 0.9f))
            TextButton(
                onClick = onAction,
                colors = ButtonDefaults.textButtonColors(contentColor = Gold400),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("${ad.ctaLabel} →", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ServiceOverviewCard(
    title: String,
    subtitle: String,
    summary: String,
    onLearnMore: () -> Unit
) {
    GlassmorphicCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onLearnMore)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.labelMedium, color = RoyalBlue600)
            }
            Box(
                modifier = Modifier.size(36.dp).clip(CircleShape).background(RoyalBlue600.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.ArrowForward, contentDescription = null, tint = RoyalBlue600, modifier = Modifier.size(18.dp))
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(summary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ContactRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, contentDescription = null, tint = RoyalBlue600)
        Column {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun ComplianceTrustStrip(settings: SystemSettingsEntity) {
    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TrustPill(icon = Icons.Filled.VerifiedUser, title = "KYC Protected", subtitle = "Rwanda National ID")
            TrustPill(icon = Icons.Filled.Schedule, title = "Kigali CAT", subtitle = settings.timezone)
            TrustPill(icon = Icons.Filled.Security, title = "Audited Ledger", subtitle = "RWF Precision")
        }
    }
}

@Composable
private fun TrustPill(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Icon(icon, contentDescription = null, tint = Emerald600, modifier = Modifier.size(20.dp))
        Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun PublicRwfTransparencyCalculator(
    withdrawalFeePercent: Double,
    activationFeeRwf: Long,
    welcomeBonusRwf: Long,
    onGetStarted: () -> Unit
) {
    var amountInput by remember { mutableStateOf("10000") }
    val amount = amountInput.toLongOrNull()?.coerceAtLeast(0L) ?: 0L
    val fee = kotlin.math.round(amount * (withdrawalFeePercent / 100.0)).toLong()
    val net = (amount - fee).coerceAtLeast(0L)

    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
        Text("RWF Transparency & Payout Estimator", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(
            "Preview exact Mobile Money withdrawal fees (${withdrawalFeePercent}%) and net payouts before creating an account.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = amountInput,
            onValueChange = { if (it.all { ch -> ch.isDigit() } && it.length <= 9) amountInput = it },
            label = { Text("Sample Amount (RWF)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("public_estimator_input")
        )
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Platform Fee (${withdrawalFeePercent}%)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(SecurityUtils.formatRwf(fee), style = MaterialTheme.typography.titleMedium.copy(fontFamily = MonoFontFamily), color = Crimson600, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Net MoMo Payout", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(SecurityUtils.formatRwf(net), style = MaterialTheme.typography.titleMedium.copy(fontFamily = MonoFontFamily), color = Emerald600, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "Activation Fee: ${SecurityUtils.formatRwf(activationFeeRwf)} • Welcome Bonus: ${SecurityUtils.formatRwf(welcomeBonusRwf)}",
            style = MaterialTheme.typography.labelSmall,
            color = RoyalBlue600
        )
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onGetStarted, modifier = Modifier.fillMaxWidth()) {
            Text("Start 3-Step Registration")
        }
    }
}

@Composable
private fun PublicFooterBanner(settings: SystemSettingsEntity, onOpenLegal: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = DeepNavy900
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(settings.platformName, style = MaterialTheme.typography.titleMedium, color = Gold400, fontWeight = FontWeight.Bold)
            Text(
                "Support: ${settings.contactEmail} | ${settings.contactPhone} | Timezone: ${settings.timezone}",
                style = MaterialTheme.typography.bodyMedium,
                color = Slate200
            )
            TextButton(onClick = onOpenLegal, contentPadding = PaddingValues(0.dp)) {
                Text("View Terms & Conditions and Privacy Policy", color = Cyan400)
            }
        }
    }
}
