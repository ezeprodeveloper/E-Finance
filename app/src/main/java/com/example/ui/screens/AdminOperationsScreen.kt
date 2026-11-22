package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.local.*
import com.example.data.security.SecurityUtils
import com.example.ui.components.DropdownSelectorField
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.Crimson600
import com.example.ui.theme.Emerald600
import com.example.ui.theme.RoyalBlue600
import java.io.File

@Composable
fun AdminVideosAndTasksTab(
    videos: List<VideoCampaignEntity>,
    tasks: List<PaidTaskEntity>,
    submissions: List<TaskSubmissionEntity>,
    onUpdateVideoStatus: (String, String) -> Unit,
    onUpdateTaskStatus: (String, String) -> Unit,
    onReviewSubmission: (String, Boolean, String) -> Unit
) {
    var subTab by remember { mutableIntStateOf(0) }
    val taskMap = remember(tasks) { tasks.associateBy { it.id } }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = subTab) {
            Tab(selected = subTab == 0, onClick = { subTab = 0 }, text = { Text("Task Submissions (${submissions.count { it.status == "PENDING_REVIEW" }})") })
            Tab(selected = subTab == 1, onClick = { subTab = 1 }, text = { Text("Videos (${videos.size})") })
            Tab(selected = subTab == 2, onClick = { subTab = 2 }, text = { Text("Tasks (${tasks.size})") })
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (subTab) {
                0 -> {
                    items(submissions, key = { it.id }) { sub ->
                        val t = taskMap[sub.taskId]
                        var reason by remember(sub.id) { mutableStateOf("") }
                        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(t?.title ?: sub.taskId, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                StatusBadge(sub.status)
                            }
                            Text("Worker ID: ${sub.userId} • Submitted: ${SecurityUtils.formatKigaliDateTime(sub.submittedAt)}", style = MaterialTheme.typography.labelMedium)
                            Spacer(Modifier.height(4.dp))
                            Text("Original Requirements: ${t?.requirements ?: ""}", style = MaterialTheme.typography.labelMedium, color = RoyalBlue600)
                            Text("Submitted Proof: ${sub.proofText}", style = MaterialTheme.typography.bodyMedium)
                            if (sub.proofImageUri.isNotBlank()) {
                                Spacer(Modifier.height(6.dp))
                                AsyncImage(
                                    model = File(sub.proofImageUri),
                                    contentDescription = "Task Proof Image",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(10.dp))
                                )
                            }
                            if (sub.status == "PENDING_REVIEW") {
                                Spacer(Modifier.height(8.dp))
                                OutlinedTextField(value = reason, onValueChange = { reason = it }, label = { Text("Rejection Reason / Note") }, modifier = Modifier.fillMaxWidth())
                                Spacer(Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(onClick = { onReviewSubmission(sub.id, true, reason) }, modifier = Modifier.weight(1f)) { Text("Approve & Credit") }
                                    OutlinedButton(onClick = { onReviewSubmission(sub.id, false, reason) }, modifier = Modifier.weight(1f)) { Text("Reject") }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    items(videos, key = { it.id }) { v ->
                        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(v.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("Reward: ${SecurityUtils.formatRwf(v.rewardPerViewRwf)}/view • Views: ${v.validViews}/${v.targetViews}", style = MaterialTheme.typography.bodyMedium)
                                }
                                StatusBadge(v.status)
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(onClick = { onUpdateVideoStatus(v.id, "ACTIVE") }, modifier = Modifier.weight(1f)) { Text("Activate") }
                                OutlinedButton(onClick = { onUpdateVideoStatus(v.id, "PAUSED") }, modifier = Modifier.weight(1f)) { Text("Pause") }
                                OutlinedButton(onClick = { onUpdateVideoStatus(v.id, "REJECTED") }, modifier = Modifier.weight(1f)) { Text("Reject") }
                            }
                        }
                    }
                }
                else -> {
                    items(tasks, key = { it.id }) { t ->
                        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(t.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("Reward: ${SecurityUtils.formatRwf(t.rewardPerParticipantRwf)} • Done: ${t.approvedParticipants}/${t.targetParticipants}", style = MaterialTheme.typography.bodyMedium)
                                }
                                StatusBadge(t.status)
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(onClick = { onUpdateTaskStatus(t.id, "ACTIVE") }, modifier = Modifier.weight(1f)) { Text("Activate") }
                                OutlinedButton(onClick = { onUpdateTaskStatus(t.id, "PAUSED") }, modifier = Modifier.weight(1f)) { Text("Pause") }
                                OutlinedButton(onClick = { onUpdateTaskStatus(t.id, "REJECTED") }, modifier = Modifier.weight(1f)) { Text("Reject") }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminAdsTab(
    ads: List<AdvertisementEntity>,
    onSaveAd: (AdvertisementEntity) -> Unit,
    onDeleteAd: (String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var bannerType by remember { mutableStateOf("BANNER") }
    var displayPos by remember { mutableStateOf("HOME_HERO") }
    var ctaLabel by remember { mutableStateOf("Learn More") }
    var accentStyle by remember { mutableStateOf("NAVY_GOLD") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Text("Create Advertisement / Promo Slide", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Advertisement Title *") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description *") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DropdownSelectorField("Type", bannerType, listOf("BANNER", "PROMO_VIDEO", "SLIDE"), { bannerType = it }, Modifier.weight(1f))
                    DropdownSelectorField("Position", displayPos, listOf("HOME_HERO", "PUBLIC_FEED", "DASHBOARD_STRIP"), { displayPos = it }, Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = ctaLabel, onValueChange = { ctaLabel = it }, label = { Text("Button Label") }, modifier = Modifier.weight(1f))
                    DropdownSelectorField("Style", accentStyle, listOf("NAVY_GOLD", "ROYAL_CYAN", "EMERALD_TEAL", "PURPLE_INDIGO"), { accentStyle = it }, Modifier.weight(1f))
                }
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = {
                        if (title.isNotBlank() && desc.isNotBlank()) {
                            onSaveAd(
                                AdvertisementEntity(
                                    id = SecurityUtils.generateId("AD"),
                                    title = title.trim(),
                                    description = desc.trim(),
                                    bannerType = bannerType,
                                    displayPosition = displayPos,
                                    ctaLabel = ctaLabel.ifBlank { "Explore" },
                                    accentStyle = accentStyle,
                                    isActive = true
                                )
                            )
                            title = ""
                            desc = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("admin_publish_ad_btn")
                ) {
                    Text("Publish Advertisement")
                }
            }
        }

        items(ads, key = { it.id }) { ad ->
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(ad.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("${ad.bannerType} • ${ad.displayPosition}", style = MaterialTheme.typography.labelMedium)
                    }
                    StatusBadge(if (ad.isActive) "ACTIVE" else "PAUSED")
                }
                Text(ad.description, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(onClick = { onSaveAd(ad.copy(isActive = !ad.isActive)) }, modifier = Modifier.weight(1f)) {
                        Text(if (ad.isActive) "Deactivate" else "Activate")
                    }
                    IconButton(onClick = { onDeleteAd(ad.id) }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete Ad", tint = Crimson600)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminSettingsTab(
    settings: SystemSettingsEntity,
    onSaveSettings: (SystemSettingsEntity) -> Unit
) {
    var platformName by remember(settings) { mutableStateOf(settings.platformName) }
    var contactEmail by remember(settings) { mutableStateOf(settings.contactEmail) }
    var contactPhone by remember(settings) { mutableStateOf(settings.contactPhone) }
    var paymentNumber by remember(settings) { mutableStateOf(settings.paymentNumber) }
    var activationFee by remember(settings) { mutableStateOf("${settings.activationFeeRwf}") }
    var welcomeBonus by remember(settings) { mutableStateOf("${settings.welcomeBonusRwf}") }
    var withdrawalFee by remember(settings) { mutableStateOf("${settings.withdrawalFeePercent}") }
    var minWithdrawal by remember(settings) { mutableStateOf("${settings.minWithdrawalRwf}") }
    var maxWithdrawal by remember(settings) { mutableStateOf("${settings.maxWithdrawalRwf}") }
    var dailyCheckin by remember(settings) { mutableStateOf("${settings.dailyCheckinRewardRwf}") }
    var minVideoReward by remember(settings) { mutableStateOf("${settings.minVideoRewardRwf}") }
    var minWatchSeconds by remember(settings) { mutableStateOf("${settings.minVideoWatchSeconds}") }
    var campaignFee by remember(settings) { mutableStateOf("${settings.campaignFeePercent}") }
    var universities by remember(settings) { mutableStateOf(settings.universityListCsv) }
    var combinations by remember(settings) { mutableStateOf(settings.aLevelCombinationsCsv) }
    var maintenanceMode by remember(settings) { mutableStateOf(settings.maintenanceMode) }
    var registrationOpen by remember(settings) { mutableStateOf(settings.registrationOpen) }
    var kycRequired by remember(settings) { mutableStateOf(settings.kycRequiredForFinance) }
    var bonusWithdrawable by remember(settings) { mutableStateOf(settings.welcomeBonusWithdrawable) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Text("System Configuration", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = platformName, onValueChange = { platformName = it }, label = { Text("Platform Name") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = contactEmail, onValueChange = { contactEmail = it }, label = { Text("Contact Email") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = contactPhone, onValueChange = { contactPhone = it }, label = { Text("Contact Phone") }, modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = paymentNumber, onValueChange = { paymentNumber = it }, label = { Text("Mobile Money Payment Number (182811880554)") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = activationFee, onValueChange = { activationFee = it }, label = { Text("Activation Fee") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                    OutlinedTextField(value = welcomeBonus, onValueChange = { welcomeBonus = it }, label = { Text("Welcome Bonus") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = withdrawalFee, onValueChange = { withdrawalFee = it }, label = { Text("Withdrawal Fee %") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.weight(1f))
                    OutlinedTextField(value = dailyCheckin, onValueChange = { dailyCheckin = it }, label = { Text("Daily Check-In RWF") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = minWithdrawal, onValueChange = { minWithdrawal = it }, label = { Text("Min Withdrawal") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                    OutlinedTextField(value = maxWithdrawal, onValueChange = { maxWithdrawal = it }, label = { Text("Max Withdrawal") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = minVideoReward, onValueChange = { minVideoReward = it }, label = { Text("Min Video Reward") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                    OutlinedTextField(value = minWatchSeconds, onValueChange = { minWatchSeconds = it }, label = { Text("Min Watch Sec") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                    OutlinedTextField(value = campaignFee, onValueChange = { campaignFee = it }, label = { Text("Campaign Fee %") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = universities, onValueChange = { universities = it }, label = { Text("Universities (pipe '|' separated)") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = combinations, onValueChange = { combinations = it }, label = { Text("A-Level Combinations (pipe '|' separated)") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                SettingSwitch("Maintenance Mode", maintenanceMode) { maintenanceMode = it }
                SettingSwitch("Registration Open", registrationOpen) { registrationOpen = it }
                SettingSwitch("Require KYC for Savings & Loans", kycRequired) { kycRequired = it }
                SettingSwitch("Welcome Bonus Withdrawable", bonusWithdrawable) { bonusWithdrawable = it }
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = {
                        onSaveSettings(
                            settings.copy(
                                platformName = platformName,
                                contactEmail = contactEmail,
                                contactPhone = contactPhone,
                                paymentNumber = paymentNumber,
                                activationFeeRwf = activationFee.toLongOrNull() ?: settings.activationFeeRwf,
                                welcomeBonusRwf = welcomeBonus.toLongOrNull() ?: settings.welcomeBonusRwf,
                                withdrawalFeePercent = withdrawalFee.toDoubleOrNull() ?: settings.withdrawalFeePercent,
                                minWithdrawalRwf = minWithdrawal.toLongOrNull() ?: settings.minWithdrawalRwf,
                                maxWithdrawalRwf = maxWithdrawal.toLongOrNull() ?: settings.maxWithdrawalRwf,
                                dailyCheckinRewardRwf = dailyCheckin.toLongOrNull() ?: settings.dailyCheckinRewardRwf,
                                minVideoRewardRwf = minVideoReward.toLongOrNull() ?: settings.minVideoRewardRwf,
                                minVideoWatchSeconds = minWatchSeconds.toIntOrNull() ?: settings.minVideoWatchSeconds,
                                campaignFeePercent = campaignFee.toDoubleOrNull() ?: settings.campaignFeePercent,
                                universityListCsv = universities,
                                aLevelCombinationsCsv = combinations,
                                maintenanceMode = maintenanceMode,
                                registrationOpen = registrationOpen,
                                kycRequiredForFinance = kycRequired,
                                welcomeBonusWithdrawable = bonusWithdrawable
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth().testTag("save_system_settings_btn")
                ) {
                    Text("Save System Settings")
                }
            }
        }
    }
}

@Composable
private fun SettingSwitch(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun AdminSupportAndAuditTab(
    tickets: List<SupportTicketEntity>,
    auditLogs: List<AuditLogEntity>,
    onReplyTicket: (String, String, String, String) -> Unit
) {
    var subTab by remember { mutableIntStateOf(0) }
    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = subTab) {
            Tab(selected = subTab == 0, onClick = { subTab = 0 }, text = { Text("Support Tickets (${tickets.size})") })
            Tab(selected = subTab == 1, onClick = { subTab = 1 }, text = { Text("Audit Logs (${auditLogs.size})") })
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (subTab == 0) {
                items(tickets, key = { it.id }) { t ->
                    var reply by remember(t.id) { mutableStateOf(t.adminReply) }
                    var agent by remember(t.id) { mutableStateOf(t.assignedAgent) }
                    var status by remember(t.id) { mutableStateOf(t.status) }
                    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("[${t.category}] ${t.subject}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            StatusBadge(t.status)
                        }
                        Text(t.description, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(value = agent, onValueChange = { agent = it }, label = { Text("Assigned Agent") }, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(6.dp))
                        DropdownSelectorField("Ticket Status", status, listOf("OPEN", "IN_PROGRESS", "RESOLVED", "CLOSED"), { status = it })
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(value = reply, onValueChange = { reply = it }, label = { Text("Official Reply") }, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(6.dp))
                        Button(onClick = { onReplyTicket(t.id, reply, status, agent) }, modifier = Modifier.fillMaxWidth()) {
                            Text("Send Reply & Update Ticket")
                        }
                    }
                }
            } else {
                items(auditLogs, key = { it.id }) { log ->
                    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(log.action, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Emerald600)
                            Text(SecurityUtils.formatKigaliDateTime(log.createdAt), style = MaterialTheme.typography.labelSmall)
                        }
                        Text("Actor: ${log.actorEmail} | Target: ${log.targetEntity}", style = MaterialTheme.typography.labelMedium)
                        Text(log.details, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
