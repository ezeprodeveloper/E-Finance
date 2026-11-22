package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.local.*
import com.example.data.security.SecurityUtils
import com.example.ui.components.*
import com.example.ui.theme.*
import java.io.File

enum class UserDestination(val label: String, val icon: ImageVector) {
    DASHBOARD("Dashboard", Icons.Filled.Dashboard),
    SAVINGS("Save & Pay", Icons.Filled.Savings),
    WITHDRAW("Withdraw", Icons.Filled.Payments),
    LOANS("Loans", Icons.Filled.AccountBalance),
    EARN("Earn Hub", Icons.Filled.PlayCircle),
    CHECKIN("Check-In", Icons.Filled.CalendarToday),
    KYC("KYC", Icons.Filled.VerifiedUser),
    HISTORY("Ledger", Icons.Filled.ReceiptLong),
    NOTIFICATIONS("Alerts", Icons.Filled.Notifications),
    SUPPORT("Support", Icons.Filled.SupportAgent)
}

@Composable
fun UserDashboardScreen(
    user: UserEntity,
    wallet: WalletEntity,
    transactions: List<FinancialTransactionEntity>,
    savings: List<SavingsAccountEntity>,
    loans: List<LoanApplicationEntity>,
    checkins: List<DailyCheckinEntity>,
    notifications: List<NotificationEntity>,
    advertisements: List<AdvertisementEntity>,
    onNavigate: (UserDestination) -> Unit,
    onOpenUploadVideo: () -> Unit,
    onOpenCreateTask: () -> Unit,
    onUpdateProfilePhoto: (Uri) -> Unit,
    onChangePassword: (String) -> Unit,
    onLogout: () -> Unit
) {
    val unreadCount = notifications.count { !it.isRead }
    val todayKey = SecurityUtils.kigaliTodayKey()
    val checkedInToday = checkins.any { it.kigaliDate == todayKey }
    val currentStreak = checkins.firstOrNull()?.streakCount ?: 0
    var showProfileDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Gradient Welcome Header + Live Rwanda Clock
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(rememberAnimatedFintechGradient())
                    .statusBarsPadding()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showProfileDialog = true }
                            .testTag("dashboard_user_menu")
                    ) {
                        if (user.profilePhotoUri.isNotBlank()) {
                            AsyncImage(
                                model = File(user.profilePhotoUri),
                                contentDescription = "User profile photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(48.dp).clip(CircleShape)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Gold400.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "${user.firstName.firstOrNull() ?: 'E'}${user.secondName.firstOrNull() ?: 'F'}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Gold400,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Column {
                            Text(
                                "Welcome, ${user.firstName}",
                                style = MaterialTheme.typography.titleLarge,
                                color = PureWhite,
                                fontWeight = FontWeight.Bold
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                StatusBadge(user.accountStatus)
                                StatusBadge("KYC: ${user.kycStatus}")
                            }
                        }
                    }
                    Row {
                        IconButton(onClick = { showProfileDialog = true }, modifier = Modifier.testTag("dashboard_profile_btn")) {
                            Icon(Icons.Filled.ManageAccounts, contentDescription = "User Menu & Profile", tint = Cyan400)
                        }
                        BadgedBox(
                            badge = {
                                if (unreadCount > 0) {
                                    Badge { Text("$unreadCount") }
                                }
                            }
                        ) {
                            IconButton(
                                onClick = { onNavigate(UserDestination.NOTIFICATIONS) },
                                modifier = Modifier.testTag("dashboard_notifications_btn")
                            ) {
                                Icon(Icons.Filled.Notifications, contentDescription = "Notifications", tint = PureWhite)
                            }
                        }
                        IconButton(onClick = onLogout, modifier = Modifier.testTag("dashboard_logout_btn")) {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout", tint = Gold400)
                        }
                    }
                }
                LiveRwandaClockCard(modifier = Modifier.fillMaxWidth())
            }
        }

        // 9 Financial Summary Cards
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Financial Summary (RWF)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                val cards = listOf(
                    Triple("Available Balance", wallet.availableBalanceRwf, Icons.Filled.AccountBalanceWallet to RoyalBlue600),
                    Triple("Savings Balance", wallet.savingsBalanceRwf, Icons.Filled.Savings to Emerald600),
                    Triple("Loan Balance", wallet.loanBalanceRwf, Icons.Filled.CreditScore to Purple700),
                    Triple("Video Earnings", wallet.videoEarningsRwf, Icons.Filled.OndemandVideo to Cyan500),
                    Triple("Task Earnings", wallet.taskEarningsRwf, Icons.Filled.TaskAlt to Emerald500),
                    Triple("Daily Check-In Rewards", wallet.checkinRewardsRwf, Icons.Filled.EventAvailable to Gold600),
                    Triple("Welcome Bonus", wallet.welcomeBonusRwf, Icons.Filled.CardGiftcard to Gold500),
                    Triple("Total Earnings", wallet.totalEarningsRwf, Icons.Filled.TrendingUp to RoyalBlue700),
                    Triple("Pending Withdrawals", wallet.pendingWithdrawalsRwf, Icons.Filled.HourglassTop to Crimson600)
                )
                cards.chunked(2).forEach { rowItems ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        rowItems.forEach { (label, amount, iconPair) ->
                            MetricSummaryTile(
                                title = label,
                                amountRwf = amount,
                                icon = iconPair.first,
                                accent = iconPair.second,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rowItems.size == 1) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // 10 Quick Actions
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Quick Actions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                val actions = listOf(
                    QuickActionItem("Deposit Money", Icons.Filled.AddCard, RoyalBlue600) { onNavigate(UserDestination.SAVINGS) },
                    QuickActionItem("Save Money", Icons.Filled.Savings, Emerald600) { onNavigate(UserDestination.SAVINGS) },
                    QuickActionItem("Withdraw Money", Icons.Filled.Payments, Gold600) { onNavigate(UserDestination.WITHDRAW) },
                    QuickActionItem("Apply for Loan", Icons.Filled.RequestQuote, Purple700) { onNavigate(UserDestination.LOANS) },
                    QuickActionItem("Watch Videos", Icons.Filled.PlayCircle, Cyan500) { onNavigate(UserDestination.EARN) },
                    QuickActionItem("Upload Video", Icons.Filled.VideoCall, RoyalBlue700) { onOpenUploadVideo() },
                    QuickActionItem("Create Task", Icons.Filled.AddTask, Emerald500) { onOpenCreateTask() },
                    QuickActionItem("Daily Check-in", Icons.Filled.CalendarToday, Gold500) { onNavigate(UserDestination.CHECKIN) },
                    QuickActionItem("Transaction History", Icons.Filled.ReceiptLong, DeepNavy800) { onNavigate(UserDestination.HISTORY) },
                    QuickActionItem("Complete KYC", Icons.Filled.VerifiedUser, Crimson600) { onNavigate(UserDestination.KYC) }
                )
                actions.chunked(2).forEach { pair ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        pair.forEach { item ->
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable(onClick = item.onClick)
                                    .testTag("quick_action_${item.label.lowercase().replace(" ", "_")}"),
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 2.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.size(36.dp).clip(CircleShape).background(item.color.copy(alpha = 0.14f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(item.icon, contentDescription = item.label, tint = item.color, modifier = Modifier.size(18.dp))
                                    }
                                    Text(item.label, style = MaterialTheme.typography.labelLarge, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Financial Statistics & Chart
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                FinancialBarChart(
                    title = "Earnings & Savings Breakdown",
                    bars = listOf(
                        ChartBarData("Savings", wallet.savingsBalanceRwf.toFloat(), Emerald600),
                        ChartBarData("Videos", wallet.videoEarningsRwf.toFloat(), Cyan500),
                        ChartBarData("Tasks", wallet.taskEarningsRwf.toFloat(), Purple700),
                        ChartBarData("Check-In", wallet.checkinRewardsRwf.toFloat(), Gold500),
                        ChartBarData("Bonus", wallet.welcomeBonusRwf.toFloat(), RoyalBlue600)
                    )
                )
            }
        }

        // Progress Overview: Savings, Loans, Daily Check-In
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val totalTarget = savings.sumOf { it.targetAmountRwf }.coerceAtLeast(1L)
                val savingsRatio = (wallet.savingsBalanceRwf.toFloat() / totalTarget.toFloat()).coerceIn(0f, 1f)
                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Account Progress & Status", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    ProgressArcRing(
                        progress = savingsRatio,
                        color = Emerald600,
                        label = "Savings Goal Progress (${savings.size} vaults)",
                        valueText = "${SecurityUtils.formatRwf(wallet.savingsBalanceRwf)} / ${SecurityUtils.formatRwf(if (savings.isEmpty()) 0L else totalTarget)}"
                    )
                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Active Loan Status", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            val latestLoan = loans.firstOrNull()
                            Text(
                                latestLoan?.let { "${it.productName} (${it.status})" } ?: "No Active Loans",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Daily Check-In", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                if (checkedInToday) "Claimed • ${currentStreak}d Streak" else "Ready to Claim",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (checkedInToday) Emerald600 else Gold600,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Platform Announcements
        if (advertisements.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Announcements & Campaigns", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(advertisements, key = { it.id }) { ad ->
                            Surface(
                                shape = MaterialTheme.shapes.medium,
                                color = Color.Transparent,
                                modifier = Modifier.width(260.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .background(Brush.linearGradient(listOf(DeepNavy900, RoyalBlue700)))
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(ad.title, style = MaterialTheme.typography.titleMedium, color = Gold400, fontWeight = FontWeight.Bold)
                                    Text(ad.description, style = MaterialTheme.typography.bodyMedium, color = PureWhite, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Recent Transactions
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Recent Ledger Activity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    TextButton(onClick = { onNavigate(UserDestination.HISTORY) }) {
                        Text("See All")
                    }
                }
                if (transactions.isEmpty()) {
                    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                        Text("No financial transactions recorded yet.", style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    transactions.take(5).forEach { tx ->
                        TransactionRowCard(tx = tx)
                    }
                }
            }
        }
    }

    if (showProfileDialog) {
        UserProfileAndSecurityDialog(
            user = user,
            onDismiss = { showProfileDialog = false },
            onUpdateProfilePhoto = onUpdateProfilePhoto,
            onChangePassword = onChangePassword
        )
    }
}

@Composable
private fun UserProfileAndSecurityDialog(
    user: UserEntity,
    onDismiss: () -> Unit,
    onUpdateProfilePhoto: (Uri) -> Unit,
    onChangePassword: (String) -> Unit
) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordMessage by remember { mutableStateOf<String?>(null) }
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            onUpdateProfilePhoto(uri)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Filled.ManageAccounts, contentDescription = null, tint = RoyalBlue600)
                Text("Member Profile & Security", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 460.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (user.profilePhotoUri.isNotBlank()) {
                            AsyncImage(
                                model = File(user.profilePhotoUri),
                                contentDescription = "Profile photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(56.dp).clip(CircleShape)
                            )
                        } else {
                            Box(
                                modifier = Modifier.size(56.dp).clip(CircleShape).background(RoyalBlue600.copy(alpha = 0.14f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Person, contentDescription = null, tint = RoyalBlue600)
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("${user.firstName} ${user.secondName}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(user.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(user.phone, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        modifier = Modifier.fillMaxWidth().testTag("update_profile_photo_btn")
                    ) {
                        Icon(Icons.Filled.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Update Profile Photo")
                    }
                }
                item {
                    HorizontalDivider()
                    Text("Locked Identity & Verification", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text("National ID: ${user.nationalIdNumber} (Locked)", style = MaterialTheme.typography.bodyMedium)
                    Text("Education: ${user.institutionName} • ${user.academicLevelOrClass}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Residence: ${user.resProvince}, ${user.resDistrict}, ${user.resSector}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                item {
                    HorizontalDivider()
                    Text("Change Account Password", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it; passwordMessage = null },
                        label = { Text("New Password (min 8 chars)") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it; passwordMessage = null },
                        label = { Text("Confirm New Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    passwordMessage?.let {
                        Spacer(Modifier.height(4.dp))
                        Text(it, style = MaterialTheme.typography.labelMedium, color = Crimson600)
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            when {
                                newPassword.length < 8 -> passwordMessage = "Password must be at least 8 characters."
                                newPassword != confirmPassword -> passwordMessage = "Passwords do not match."
                                else -> {
                                    onChangePassword(newPassword)
                                    newPassword = ""
                                    confirmPassword = ""
                                    onDismiss()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("profile_change_password_btn")
                    ) {
                        Text("Update Password")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

private data class QuickActionItem(
    val label: String,
    val icon: ImageVector,
    val color: Color,
    val onClick: () -> Unit
)

@Composable
fun TransactionRowCard(tx: FinancialTransactionEntity) {
    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(tx.category.replace("_", " "), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(tx.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                Text(
                    "${tx.id} • ${SecurityUtils.formatKigaliDateTime(tx.createdAt)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    SecurityUtils.formatRwf(tx.netAmountRwf),
                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = MonoFontFamily),
                    fontWeight = FontWeight.Bold,
                    color = RoyalBlue600
                )
                StatusBadge(tx.status)
            }
        }
    }
}
