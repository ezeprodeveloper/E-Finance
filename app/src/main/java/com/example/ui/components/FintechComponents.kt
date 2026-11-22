package com.example.ui.components

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.security.SecurityUtils
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun rememberAnimatedFintechGradient(): Brush {
    val infiniteTransition = rememberInfiniteTransition(label = "fintechGradient")
    val c1 by infiniteTransition.animateColor(
        initialValue = DeepNavy900,
        targetValue = DeepNavy800,
        animationSpec = infiniteRepeatable(
            animation = tween(5500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "c1"
    )
    val c2 by infiniteTransition.animateColor(
        initialValue = RoyalBlue700,
        targetValue = Purple700,
        animationSpec = infiniteRepeatable(
            animation = tween(6500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "c2"
    )
    val c3 by infiniteTransition.animateColor(
        initialValue = Cyan500,
        targetValue = Emerald600,
        animationSpec = infiniteRepeatable(
            animation = tween(7500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "c3"
    )
    return Brush.linearGradient(
        colors = listOf(c1, c2, c3),
        start = Offset(0f, 0f),
        end = Offset(1100f, 900f)
    )
}

@Composable
fun EFinanceLogoMark(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.linearGradient(listOf(DeepNavy900, RoyalBlue700))
            )
            .border(1.5.dp, Gold400.copy(alpha = 0.85f), RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.AccountBalance,
            contentDescription = "E-FINANCE RWANDA Emblem",
            tint = Gold400,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun LiveRwandaClockCard(modifier: Modifier = Modifier) {
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            nowMillis = System.currentTimeMillis()
            delay(1000L)
        }
    }

    val timeFmt = remember {
        SimpleDateFormat("HH:mm:ss", Locale.US).apply { timeZone = SecurityUtils.KIGALI_TIMEZONE }
    }
    val dateFmt = remember {
        SimpleDateFormat("EEE, dd MMM yyyy", Locale.US).apply { timeZone = SecurityUtils.KIGALI_TIMEZONE }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "clockAccent")
    val startColor by infiniteTransition.animateColor(
        initialValue = RoyalBlue700,
        targetValue = Purple700,
        animationSpec = infiniteRepeatable(tween(4000), RepeatMode.Reverse),
        label = "clockStart"
    )
    val endColor by infiniteTransition.animateColor(
        initialValue = Cyan500,
        targetValue = Emerald600,
        animationSpec = infiniteRepeatable(tween(4000), RepeatMode.Reverse),
        label = "clockEnd"
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.horizontalGradient(listOf(startColor, endColor)))
            .border(1.dp, PureWhite.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Schedule,
            contentDescription = "Africa Kigali Live Clock",
            tint = Gold400,
            modifier = Modifier.size(18.dp)
        )
        Column {
            Text(
                text = "${timeFmt.format(Date(nowMillis))} CAT",
                style = MaterialTheme.typography.titleMedium.copy(fontFamily = MonoFontFamily),
                color = PureWhite,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${dateFmt.format(Date(nowMillis))} • Kigali",
                style = MaterialTheme.typography.labelMedium,
                color = PureWhite.copy(alpha = 0.88f)
            )
        }
    }
}

@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
    borderColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = containerColor,
        tonalElevation = 3.dp,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

@Composable
fun StatusBadge(status: String, modifier: Modifier = Modifier) {
    val (bg, fg) = when (status.uppercase()) {
        "ACTIVE", "APPROVED", "COMPLETED", "VERIFIED", "DISBURSED", "REPAID", "RESOLVED" ->
            Emerald600.copy(alpha = 0.14f) to Emerald600
        "PENDING", "UNDER_REVIEW", "PENDING_REVIEW", "PENDING_PAYMENT", "PAYMENT_VERIFICATION",
        "PENDING_APPROVAL", "APPROVED_AWAITING_ACTIVATION", "IN_PROGRESS", "PROCESSING", "OPEN" ->
            Gold600.copy(alpha = 0.16f) to Gold600
        "REJECTED", "SUSPENDED", "BLOCKED", "OVERDUE", "CANCELLED" ->
            Crimson600.copy(alpha = 0.14f) to Crimson600
        else -> RoyalBlue600.copy(alpha = 0.14f) to RoyalBlue600
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = bg
    ) {
        Text(
            text = status.replace("_", " "),
            style = MaterialTheme.typography.labelSmall,
            color = fg,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun UssdPaymentInstructionCard(
    paymentNumber: String,
    amountRwf: Long,
    title: String,
    recipientName: String = "E-FINANCE RWANDA Merchant",
    modifier: Modifier = Modifier
) {
    val ussd = SecurityUtils.formatUssdReference(paymentNumber, amountRwf)
    val clipboard = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = DeepNavy900,
        border = BorderStroke(1.dp, Gold400.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.PhoneAndroid, contentDescription = null, tint = Gold400)
                    Text(title, style = MaterialTheme.typography.titleMedium, color = PureWhite)
                }
                Text(
                    SecurityUtils.formatRwf(amountRwf),
                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = MonoFontFamily),
                    color = Emerald400,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                "Recipient: $recipientName ($paymentNumber)",
                style = MaterialTheme.typography.bodyMedium,
                color = Slate200
            )
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = DeepNavy800,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Rwanda Mobile Money USSD", style = MaterialTheme.typography.labelMedium, color = Cyan400)
                        Text(
                            ussd,
                            style = MaterialTheme.typography.titleLarge.copy(fontFamily = MonoFontFamily),
                            color = Gold400
                        )
                    }
                    FilledTonalButton(
                        onClick = {
                            clipboard.setText(AnnotatedString(ussd))
                            copied = true
                        },
                        modifier = Modifier.testTag("copy_ussd_button")
                    ) {
                        Icon(if (copied) Icons.Filled.Check else Icons.Filled.ContentCopy, contentDescription = "Copy USSD")
                        Spacer(Modifier.width(6.dp))
                        Text(if (copied) "Copied" else "Copy")
                    }
                }
            }
            Text(
                "Important: Payments are NEVER marked successful automatically. After dialing, submit your Mobile Money transaction ID below for administrator verification.",
                style = MaterialTheme.typography.bodyMedium,
                color = Gold400.copy(alpha = 0.92f)
            )
        }
    }
}

data class ChartBarData(val label: String, val value: Float, val color: Color)

@Composable
fun FinancialBarChart(
    title: String,
    bars: List<ChartBarData>,
    modifier: Modifier = Modifier
) {
    val maxVal = (bars.maxOfOrNull { it.value } ?: 1f).coerceAtLeast(1f)
    val animProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "barAnim"
    )

    GlassmorphicCard(modifier = modifier.fillMaxWidth()) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(12.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            val count = bars.size.coerceAtLeast(1)
            val slotWidth = size.width / count
            val barWidth = (slotWidth * 0.48f).coerceAtMost(46.dp.toPx())
            val chartBottom = size.height - 10.dp.toPx()
            val maxBarHeight = chartBottom - 12.dp.toPx()

            bars.forEachIndexed { index, item ->
                val ratio = (item.value / maxVal).coerceIn(0f, 1f) * animProgress
                val barHeight = (maxBarHeight * ratio).coerceAtLeast(6.dp.toPx())
                val left = index * slotWidth + (slotWidth - barWidth) / 2f
                drawRoundRect(
                    color = item.color.copy(alpha = 0.14f),
                    topLeft = Offset(left, 12.dp.toPx()),
                    size = Size(barWidth, maxBarHeight),
                    cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
                )
                drawRoundRect(
                    color = item.color,
                    topLeft = Offset(left, chartBottom - barHeight),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            bars.forEach { item ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Text(
                        item.label,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        SecurityUtils.formatRwf(item.value.toLong()),
                        style = MaterialTheme.typography.labelSmall,
                        color = item.color,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun ProgressArcRing(
    progress: Float,
    color: Color,
    label: String,
    valueText: String,
    modifier: Modifier = Modifier
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(700),
        label = "ring"
    )
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(modifier = Modifier.size(56.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke = 6.dp.toPx()
                drawArc(
                    color = color.copy(alpha = 0.18f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
                drawArc(
                    color = color,
                    startAngle = -90f,
                    sweepAngle = 360f * animated,
                    useCenter = false,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            }
            Text(
                "${(animated * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
        Column {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(valueText, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownSelectorField(
    label: String,
    selectedValue: String,
    options: List<String>,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedValue,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
                .let { if (testTag.isNotEmpty()) it.testTag(testTag) else it }
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun MetricSummaryTile(
    title: String,
    amountRwf: Long,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier
            .let { if (onClick != null) it.clickable(onClick = onClick) else it },
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        border = BorderStroke(1.dp, accent.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = title, tint = accent, modifier = Modifier.size(18.dp))
                }
            }
            Text(
                title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                SecurityUtils.formatRwf(amountRwf),
                style = MaterialTheme.typography.titleMedium.copy(fontFamily = MonoFontFamily),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
