package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.RwandaReferenceData
import com.example.data.local.*
import com.example.data.security.SecurityUtils
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import java.util.Calendar

@Composable
fun EarnAndCreatorScreen(
    initialTab: Int = 0,
    user: UserEntity,
    settings: SystemSettingsEntity,
    activeVideos: List<VideoCampaignEntity>,
    userVideoViews: List<VideoViewEntity>,
    userVideoCampaigns: List<VideoCampaignEntity>,
    activeTasks: List<PaidTaskEntity>,
    userTaskSubmissions: List<TaskSubmissionEntity>,
    userCreatedTasks: List<PaidTaskEntity>,
    onStoreMedia: (Uri, String) -> String?,
    onCompleteVideo: (String, Int) -> Unit,
    onCreateVideoCampaign: (String, String, String, String, String, Int, Long, Int, Int, (VideoCampaignEntity) -> Unit) -> Unit,
    onSubmitCampaignPayment: (String, Long, String, String) -> Unit,
    onSubmitTaskProof: (String, String, String) -> Unit,
    onCreatePaidTask: (String, String, String, String, String, String, Int, Long, Int, (PaidTaskEntity) -> Unit) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    var tabIndex by remember(initialTab) { mutableIntStateOf(initialTab) }
    var watchingVideo by remember { mutableStateOf<VideoCampaignEntity?>(null) }
    var submittingTask by remember { mutableStateOf<PaidTaskEntity?>(null) }
    var payingVideo by remember { mutableStateOf<VideoCampaignEntity?>(null) }
    var payingTask by remember { mutableStateOf<PaidTaskEntity?>(null) }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            ModuleTopBar("Videos & Paid Tasks Hub", "Earn & Promote in RWF", onBack)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            ScrollableTabRow(selectedTabIndex = tabIndex, edgePadding = 12.dp) {
                Tab(selected = tabIndex == 0, onClick = { tabIndex = 0 }, text = { Text("Watch Videos") })
                Tab(selected = tabIndex == 1, onClick = { tabIndex = 1 }, text = { Text("Upload Video") })
                Tab(selected = tabIndex == 2, onClick = { tabIndex = 2 }, text = { Text("Paid Tasks") })
                Tab(selected = tabIndex == 3, onClick = { tabIndex = 3 }, text = { Text("Create Task") })
            }

            when (tabIndex) {
                0 -> WatchVideosTab(
                    activeVideos = activeVideos,
                    userVideoViews = userVideoViews,
                    onWatch = { watchingVideo = it }
                )
                1 -> UploadVideoCampaignTab(
                    settings = settings,
                    myCampaigns = userVideoCampaigns,
                    onStoreMedia = onStoreMedia,
                    onCreate = { t, d, cat, vid, th, dur, rew, tgt, days ->
                        onCreateVideoCampaign(t, d, cat, vid, th, dur, rew, tgt, days) { created ->
                            payingVideo = created
                        }
                    },
                    onPay = { payingVideo = it }
                )
                2 -> PaidTasksTab(
                    userId = user.id,
                    activeTasks = activeTasks,
                    submissions = userTaskSubmissions,
                    onSubmitTask = { submittingTask = it }
                )
                3 -> CreateTaskTab(
                    settings = settings,
                    myTasks = userCreatedTasks,
                    onCreate = { t, d, cat, inst, req, proof, part, rew, days ->
                        onCreatePaidTask(t, d, cat, inst, req, proof, part, rew, days) { created ->
                            payingTask = created
                        }
                    },
                    onPay = { payingTask = it }
                )
            }
        }
    }

    watchingVideo?.let { video ->
        VideoWatchPlayerDialog(
            video = video,
            onDismiss = { watchingVideo = null },
            onClaimReward = { watchedSec ->
                onCompleteVideo(video.id, watchedSec)
                watchingVideo = null
            }
        )
    }

    submittingTask?.let { task ->
        TaskProofSubmissionDialog(
            task = task,
            onStoreScreenshot = { uri -> onStoreMedia(uri, "task_proof") },
            onDismiss = { submittingTask = null },
            onSubmit = { text, imgPath ->
                onSubmitTaskProof(task.id, text, imgPath)
                submittingTask = null
            }
        )
    }

    payingVideo?.let { vid ->
        CampaignPaymentDialog(
            title = "Fund Video Campaign: ${vid.title}",
            amountRwf = vid.totalBudgetRwf,
            paymentNumber = settings.paymentNumber,
            onDismiss = { payingVideo = null },
            onSubmit = { ref ->
                onSubmitCampaignPayment("VIDEO_CAMPAIGN", vid.totalBudgetRwf, ref, vid.id)
                payingVideo = null
            }
        )
    }

    payingTask?.let { tsk ->
        CampaignPaymentDialog(
            title = "Fund Paid Task: ${tsk.title}",
            amountRwf = tsk.totalBudgetRwf,
            paymentNumber = settings.paymentNumber,
            onDismiss = { payingTask = null },
            onSubmit = { ref ->
                onSubmitCampaignPayment("TASK_CAMPAIGN", tsk.totalBudgetRwf, ref, tsk.id)
                payingTask = null
            }
        )
    }
}

@Composable
private fun WatchVideosTab(
    activeVideos: List<VideoCampaignEntity>,
    userVideoViews: List<VideoViewEntity>,
    onWatch: (VideoCampaignEntity) -> Unit
) {
    val watchedIds = remember(userVideoViews) { userVideoViews.map { it.videoId }.toSet() }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (activeVideos.isEmpty()) {
            item {
                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Text("No active video campaigns available right now.", style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            items(activeVideos, key = { it.id }) { video ->
                val alreadyWatched = video.id in watchedIds
                val remaining = (video.targetViews - video.validViews).coerceAtLeast(0)
                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = DeepNavy900,
                        modifier = Modifier.fillMaxWidth().height(110.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.linearGradient(listOf(DeepNavy900, RoyalBlue700, Cyan500))),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(video.category, style = MaterialTheme.typography.labelSmall, color = Gold400)
                                    Text(video.title, style = MaterialTheme.typography.titleMedium, color = PureWhite, fontWeight = FontWeight.Bold)
                                    Text("${video.durationSeconds}s • Min watch ${video.minWatchSeconds}s", style = MaterialTheme.typography.labelMedium, color = Slate200)
                                }
                                Icon(Icons.Filled.PlayCircleFilled, contentDescription = null, tint = Gold400, modifier = Modifier.size(44.dp))
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(video.description, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Reward: ${SecurityUtils.formatRwf(video.rewardPerViewRwf)}", style = MaterialTheme.typography.titleMedium, color = Emerald600, fontWeight = FontWeight.Bold)
                            Text("$remaining eligible views remaining", style = MaterialTheme.typography.labelMedium)
                        }
                        Button(
                            onClick = { onWatch(video) },
                            enabled = !alreadyWatched && remaining > 0,
                            modifier = Modifier.testTag("watch_video_btn_${video.id}")
                        ) {
                            Text(if (alreadyWatched) "Earned ✓" else "Watch Video")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VideoWatchPlayerDialog(
    video: VideoCampaignEntity,
    onDismiss: () -> Unit,
    onClaimReward: (Int) -> Unit
) {
    var watchedSeconds by remember { mutableIntStateOf(0) }
    var isPlaying by remember { mutableStateOf(true) }

    LaunchedEffect(isPlaying) {
        while (isPlaying && watchedSeconds < video.durationSeconds) {
            delay(1000L)
            watchedSeconds++
        }
    }

    val qualified = watchedSeconds >= video.minWatchSeconds
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(video.title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = DeepNavy900,
                    modifier = Modifier.fillMaxWidth().height(130.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(14.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            if (qualified) Icons.Filled.CheckCircle else Icons.Filled.OndemandVideo,
                            contentDescription = null,
                            tint = if (qualified) Emerald400 else Gold400,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            if (qualified) "Minimum Watch Time Verified!" else "Playing Campaign Video...",
                            color = PureWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Watched ${watchedSeconds}s / ${video.minWatchSeconds}s required",
                            style = MaterialTheme.typography.labelMedium.copy(fontFamily = MonoFontFamily),
                            color = Cyan400
                        )
                    }
                }
                LinearProgressIndicator(
                    progress = { (watchedSeconds.toFloat() / video.minWatchSeconds.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f) },
                    color = if (qualified) Emerald600 else RoyalBlue600,
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(50))
                )
                Text(
                    "Reward upon verified completion: ${SecurityUtils.formatRwf(video.rewardPerViewRwf)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onClaimReward(watchedSeconds) },
                enabled = qualified,
                modifier = Modifier.testTag("claim_video_reward_btn")
            ) {
                Text(if (qualified) "Claim ${SecurityUtils.formatRwf(video.rewardPerViewRwf)}" else "Wait (${(video.minWatchSeconds - watchedSeconds).coerceAtLeast(0)}s)")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
private fun UploadVideoCampaignTab(
    settings: SystemSettingsEntity,
    myCampaigns: List<VideoCampaignEntity>,
    onStoreMedia: (Uri, String) -> String?,
    onCreate: (String, String, String, String, String, Int, Long, Int, Int) -> Unit,
    onPay: (VideoCampaignEntity) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(RwandaReferenceData.videoCategories.first()) }
    var videoUri by remember { mutableStateOf("") }
    var thumbUri by remember { mutableStateOf("") }
    var durationSec by remember { mutableStateOf("30") }
    var rewardPerView by remember { mutableStateOf("${settings.minVideoRewardRwf}") }
    var targetViews by remember { mutableStateOf("100") }
    var durationDays by remember { mutableStateOf("14") }

    val videoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { onStoreMedia(it, "video_campaigns")?.let { p -> videoUri = p } }
    }
    val thumbPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { onStoreMedia(it, "video_thumbs")?.let { p -> thumbUri = p } }
    }

    val rewardNum = rewardPerView.toLongOrNull() ?: 0L
    val targetNum = targetViews.toIntOrNull() ?: 0
    val rewardBudget = rewardNum * targetNum
    val feeBudget = Math.round(rewardBudget * (settings.campaignFeePercent / 100.0))
    val totalBudget = rewardBudget + feeBudget

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Text("Upload Video & Launch Paid Campaign", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Minimum reward enforced: ${SecurityUtils.formatRwf(settings.minVideoRewardRwf)} per valid view.", style = MaterialTheme.typography.labelLarge, color = Gold600)
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Video Title *") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description *") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                DropdownSelectorField("Video Category", category, RwandaReferenceData.videoCategories, { category = it })
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { videoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)) },
                        modifier = Modifier.weight(1f)
                    ) { Text(if (videoUri.isBlank()) "Pick Video File" else "Video Attached ✓") }
                    OutlinedButton(
                        onClick = { thumbPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        modifier = Modifier.weight(1f)
                    ) { Text(if (thumbUri.isBlank()) "Pick Thumbnail" else "Thumbnail ✓") }
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = rewardPerView,
                        onValueChange = { rewardPerView = it },
                        label = { Text("Reward/View (>=${settings.minVideoRewardRwf})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = targetViews,
                        onValueChange = { targetViews = it },
                        label = { Text("Target Viewers") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = durationSec,
                        onValueChange = { durationSec = it },
                        label = { Text("Video Sec") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = durationDays,
                        onValueChange = { durationDays = it },
                        label = { Text("Campaign Days") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(10.dp))
                Surface(shape = MaterialTheme.shapes.medium, color = DeepNavy900, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Automatic Campaign Budget", style = MaterialTheme.typography.labelLarge, color = Gold400)
                        Text("Viewer Rewards ($rewardNum × $targetNum): ${SecurityUtils.formatRwf(rewardBudget)}", color = PureWhite)
                        Text("Platform Fee (${settings.campaignFeePercent}%): ${SecurityUtils.formatRwf(feeBudget)}", color = Slate200)
                        Text("Total Campaign Budget: ${SecurityUtils.formatRwf(totalBudget)}", color = Emerald400, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = {
                        onCreate(
                            title, description, category, videoUri, thumbUri,
                            durationSec.toIntOrNull() ?: 30, rewardNum, targetNum, durationDays.toIntOrNull() ?: 14
                        )
                    },
                    modifier = Modifier.fillMaxWidth().testTag("create_video_campaign_btn")
                ) {
                    Text("Create Campaign & Pay Budget")
                }
            }
        }

        if (myCampaigns.isNotEmpty()) {
            item { Text("Your Video Campaigns (${myCampaigns.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            items(myCampaigns, key = { it.id }) { c ->
                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(c.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Budget: ${SecurityUtils.formatRwf(c.totalBudgetRwf)} • Spent: ${SecurityUtils.formatRwf(c.spentBudgetRwf)}", style = MaterialTheme.typography.bodyMedium)
                            Text("Valid Views: ${c.validViews}/${c.targetViews} • Remaining: ${(c.targetViews - c.validViews).coerceAtLeast(0)}", style = MaterialTheme.typography.labelMedium)
                        }
                        StatusBadge(c.status)
                    }
                    if (c.status == "PENDING_PAYMENT") {
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { onPay(c) }, modifier = Modifier.fillMaxWidth()) {
                            Text("Submit Payment (${SecurityUtils.formatUssdReference(settings.paymentNumber, c.totalBudgetRwf)})")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PaidTasksTab(
    userId: String,
    activeTasks: List<PaidTaskEntity>,
    submissions: List<TaskSubmissionEntity>,
    onSubmitTask: (PaidTaskEntity) -> Unit
) {
    val subByTaskId = remember(submissions) { submissions.associateBy { it.taskId } }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(activeTasks, key = { it.id }) { task ->
            val mySub = subByTaskId[task.id]
            val isOwnTask = task.creatorUserId == userId
            val remaining = (task.targetParticipants - task.approvedParticipants).coerceAtLeast(0)
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(task.category, style = MaterialTheme.typography.labelMedium, color = RoyalBlue600, fontWeight = FontWeight.Bold)
                    Text(SecurityUtils.formatRwf(task.rewardPerParticipantRwf), style = MaterialTheme.typography.titleMedium.copy(fontFamily = MonoFontFamily), color = Emerald600, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(4.dp))
                Text(task.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(task.description, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(6.dp))
                Text("Instructions: ${task.instructions}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Required Proof: ${task.requiredProofType} • $remaining spots left", style = MaterialTheme.typography.labelMedium, color = Gold600)
                Spacer(Modifier.height(8.dp))
                if (mySub != null) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Submission Recorded", style = MaterialTheme.typography.labelLarge)
                        StatusBadge(mySub.status)
                    }
                    if (mySub.rejectionReason.isNotBlank()) {
                        Text("Reason: ${mySub.rejectionReason}", style = MaterialTheme.typography.labelMedium, color = Crimson600)
                    }
                } else {
                    Button(
                        onClick = { onSubmitTask(task) },
                        enabled = !isOwnTask && remaining > 0,
                        modifier = Modifier.fillMaxWidth().testTag("complete_task_btn_${task.id}")
                    ) {
                        Text(if (isOwnTask) "Creator Cannot Self-Submit" else "Complete Task & Submit Proof")
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskProofSubmissionDialog(
    task: PaidTaskEntity,
    onStoreScreenshot: (Uri) -> String?,
    onDismiss: () -> Unit,
    onSubmit: (String, String) -> Unit
) {
    var proofText by remember { mutableStateOf("") }
    var screenshotPath by remember { mutableStateOf("") }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { onStoreScreenshot(it)?.let { p -> screenshotPath = p } }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Submit Proof: ${task.title}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Requirements: ${task.requirements}", style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(
                    value = proofText,
                    onValueChange = { proofText = it },
                    label = { Text("Your Answer / Proof Details *") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedButton(
                    onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (screenshotPath.isBlank()) "Attach Screenshot (Optional)" else "Screenshot Attached ✓")
                }
                Text(
                    "Note: Task creators cannot approve submissions. Only authorized administrators verify proof before crediting rewards.",
                    style = MaterialTheme.typography.labelMedium,
                    color = Gold600
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSubmit(proofText, screenshotPath) }) {
                Text("Submit for Admin Review")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun CreateTaskTab(
    settings: SystemSettingsEntity,
    myTasks: List<PaidTaskEntity>,
    onCreate: (String, String, String, String, String, String, Int, Long, Int) -> Unit,
    onPay: (PaidTaskEntity) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(RwandaReferenceData.taskCategories.first()) }
    var instructions by remember { mutableStateOf("") }
    var requirements by remember { mutableStateOf("") }
    var proofType by remember { mutableStateOf("Screenshot & Text") }
    var participants by remember { mutableStateOf("50") }
    var reward by remember { mutableStateOf("50") }
    var days by remember { mutableStateOf("14") }

    val partNum = participants.toIntOrNull() ?: 0
    val rewNum = reward.toLongOrNull() ?: 0L
    val rewardTotal = partNum * rewNum
    val feeTotal = Math.round(rewardTotal * (settings.campaignFeePercent / 100.0))
    val grandTotal = rewardTotal + feeTotal

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Text("Create Paid Task for Members", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Task Title *") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Task Description *") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                DropdownSelectorField("Task Category", category, RwandaReferenceData.taskCategories, { category = it })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = instructions, onValueChange = { instructions = it }, label = { Text("Task Instructions *") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = requirements, onValueChange = { requirements = it }, label = { Text("Task Requirements *") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = proofType, onValueChange = { proofType = it }, label = { Text("Required Proof Type *") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = participants,
                        onValueChange = { participants = it },
                        label = { Text("Participants") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = reward,
                        onValueChange = { reward = it },
                        label = { Text("Reward (RWF)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = days,
                        onValueChange = { days = it },
                        label = { Text("Days") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(0.7f)
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text("Campaign Budget: ${SecurityUtils.formatRwf(grandTotal)} (incl. ${settings.campaignFeePercent}% fee)", style = MaterialTheme.typography.labelLarge, color = RoyalBlue600)
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = { onCreate(title, desc, category, instructions, requirements, proofType, partNum, rewNum, days.toIntOrNull() ?: 14) },
                    modifier = Modifier.fillMaxWidth().testTag("create_task_btn")
                ) {
                    Text("Create Task & Proceed to Payment")
                }
            }
        }

        items(myTasks, key = { it.id }) { t ->
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(t.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Approved: ${t.approvedParticipants}/${t.targetParticipants} • Budget: ${SecurityUtils.formatRwf(t.totalBudgetRwf)}", style = MaterialTheme.typography.bodyMedium)
                    }
                    StatusBadge(t.status)
                }
                if (t.status == "PENDING_PAYMENT") {
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { onPay(t) }, modifier = Modifier.fillMaxWidth()) {
                        Text("Pay Campaign Budget (${SecurityUtils.formatRwf(t.totalBudgetRwf)})")
                    }
                }
            }
        }
    }
}

@Composable
private fun CampaignPaymentDialog(
    title: String,
    amountRwf: Long,
    paymentNumber: String,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var ref by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                UssdPaymentInstructionCard(paymentNumber = paymentNumber, amountRwf = amountRwf, title = "Campaign Funding")
                OutlinedTextField(
                    value = ref,
                    onValueChange = { ref = it },
                    label = { Text("Mobile Money Transaction ID *") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSubmit(ref) }) { Text("Submit Payment Reference") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Later") } }
    )
}

@Composable
fun DailyCheckinScreen(
    wallet: WalletEntity,
    settings: SystemSettingsEntity,
    checkins: List<DailyCheckinEntity>,
    onClaimCheckin: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val todayKey = SecurityUtils.kigaliTodayKey()
    val monthPrefix = SecurityUtils.kigaliMonthKey()
    val checkedToday = checkins.any { it.kigaliDate == todayKey }
    val streak = checkins.firstOrNull()?.streakCount ?: 0
    val claimedDaysThisMonth = remember(checkins, monthPrefix) {
        checkins.filter { it.kigaliDate.startsWith(monthPrefix) }
            .mapNotNull { it.kigaliDate.takeLast(2).toIntOrNull() }
            .toSet()
    }
    val kigaliCal = remember {
        Calendar.getInstance(SecurityUtils.KIGALI_TIMEZONE)
    }
    val daysInMonth = kigaliCal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val currentDay = kigaliCal.get(Calendar.DAY_OF_MONTH)

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = { ModuleTopBar("Daily Check-In Rewards", "Timezone: Africa/Kigali", onBack) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Surface(shape = MaterialTheme.shapes.extraLarge, color = DeepNavy900, modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .background(Brush.linearGradient(listOf(DeepNavy900, RoyalBlue700, Emerald600)))
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Rwanda Calendar Day: $todayKey", style = MaterialTheme.typography.labelLarge, color = Cyan400)
                        Text(
                            if (checkedToday) "Today's Reward Claimed!" else "Claim ${SecurityUtils.formatRwf(settings.dailyCheckinRewardRwf)} Today",
                            style = MaterialTheme.typography.headlineMedium,
                            color = PureWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Consecutive Streak", style = MaterialTheme.typography.labelMedium, color = Slate200)
                                Text("$streak Day(s)", style = MaterialTheme.typography.titleLarge, color = Gold400, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Total Check-In Earnings", style = MaterialTheme.typography.labelMedium, color = Slate200)
                                Text(SecurityUtils.formatRwf(wallet.checkinRewardsRwf), style = MaterialTheme.typography.titleLarge, color = Emerald400, fontWeight = FontWeight.Bold)
                            }
                        }
                        Button(
                            onClick = onClaimCheckin,
                            enabled = !checkedToday,
                            colors = ButtonDefaults.buttonColors(containerColor = Gold500, contentColor = DeepNavy950),
                            modifier = Modifier.fillMaxWidth().height(50.dp).testTag("claim_daily_checkin_btn")
                        ) {
                            Icon(if (checkedToday) Icons.Filled.CheckCircle else Icons.Filled.Redeem, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(if (checkedToday) "Claimed for $todayKey" else "Claim +${SecurityUtils.formatRwf(settings.dailyCheckinRewardRwf)}", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Monthly Check-In Calendar ($monthPrefix)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    val daysList = (1..daysInMonth).toList()
                    daysList.chunked(7).forEach { week ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            week.forEach { day ->
                                val isClaimed = day in claimedDaysThisMonth
                                val isToday = day == currentDay
                                Surface(
                                    modifier = Modifier.weight(1f).height(38.dp),
                                    shape = CircleShape,
                                    color = when {
                                        isClaimed -> Emerald600
                                        isToday -> Gold500.copy(alpha = 0.25f)
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    },
                                    border = if (isToday) BorderStroke(1.5.dp, Gold500) else null
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            "$day",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (isClaimed) PureWhite else MaterialTheme.colorScheme.onSurface,
                                            fontWeight = if (isClaimed || isToday) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                            repeat(7 - week.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                }
            }

            item { Text("Check-In History", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            items(checkins, key = { it.id }) { item ->
                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(item.kigaliDate, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Day ${item.streakCount} streak • ${SecurityUtils.formatKigaliTime(item.createdAt)} CAT", style = MaterialTheme.typography.labelMedium)
                        }
                        Text("+${SecurityUtils.formatRwf(item.rewardRwf)}", fontFamily = MonoFontFamily, fontWeight = FontWeight.Bold, color = Emerald600)
                    }
                }
            }
        }
    }
}
