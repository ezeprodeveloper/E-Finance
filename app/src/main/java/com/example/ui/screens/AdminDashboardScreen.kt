package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
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
import com.example.ui.components.*
import com.example.ui.theme.*
import java.io.File

enum class AdminSection(val label: String) {
    OVERVIEW("Overview"),
    USERS("Users"),
    PAYMENTS("Payments"),
    KYC("KYC"),
    WITHDRAWALS("Withdrawals"),
    LOANS("Loans"),
    VIDEOS_TASKS("Videos & Tasks"),
    ADS("Ads"),
    LEDGER("Ledger"),
    SETTINGS("Settings"),
    SUPPORT_AUDIT("Support & Audit")
}

@Composable
fun AdminDashboardScreen(
    admin: UserEntity,
    settings: SystemSettingsEntity,
    allUsers: List<UserEntity>,
    allWallets: List<WalletEntity>,
    allTransactions: List<FinancialTransactionEntity>,
    allPayments: List<PaymentVerificationEntity>,
    allKyc: List<KycApplicationEntity>,
    allWithdrawals: List<WithdrawalRequestEntity>,
    allLoans: List<LoanApplicationEntity>,
    loanProducts: List<LoanProductEntity>,
    allVideos: List<VideoCampaignEntity>,
    allVideoViews: List<VideoViewEntity>,
    allTasks: List<PaidTaskEntity>,
    allTaskSubmissions: List<TaskSubmissionEntity>,
    allAds: List<AdvertisementEntity>,
    allTickets: List<SupportTicketEntity>,
    auditLogs: List<AuditLogEntity>,
    onUpdateUserStatus: (String, String, String) -> Unit,
    onUnlockUser: (String) -> Unit,
    onCorrectUserProfile: (String, String, String, String, String) -> Unit,
    onAdjustBalance: (String, Long, String) -> Unit,
    onReviewPayment: (String, Boolean, String) -> Unit,
    onReviewKyc: (String, String, String) -> Unit,
    onProcessWithdrawal: (String, String, String) -> Unit,
    onReviewLoan: (String, String, String) -> Unit,
    onSaveLoanProduct: (LoanProductEntity) -> Unit,
    onUpdateVideoStatus: (String, String) -> Unit,
    onUpdateTaskStatus: (String, String) -> Unit,
    onReviewTaskSubmission: (String, Boolean, String) -> Unit,
    onSaveAd: (AdvertisementEntity) -> Unit,
    onDeleteAd: (String) -> Unit,
    onSaveSettings: (SystemSettingsEntity) -> Unit,
    onReplyTicket: (String, String, String, String) -> Unit,
    onLogout: () -> Unit
) {
    var activeSection by remember { mutableStateOf(AdminSection.OVERVIEW) }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(rememberAnimatedFintechGradient())
                    .statusBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        EFinanceLogoMark()
                        Column {
                            Text("SUPER ADMINISTRATION", style = MaterialTheme.typography.titleMedium, color = Gold400, fontWeight = FontWeight.ExtraBold)
                            Text(admin.email, style = MaterialTheme.typography.labelMedium, color = PureWhite)
                        }
                    }
                    IconButton(onClick = onLogout, modifier = Modifier.testTag("admin_logout_btn")) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout", tint = Gold400)
                    }
                }
                LiveRwandaClockCard(modifier = Modifier.fillMaxWidth())
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AdminSection.entries.forEach { sec ->
                        val selected = activeSection == sec
                        FilterChip(
                            selected = selected,
                            onClick = { activeSection = sec },
                            label = { Text(sec.label, color = if (selected) DeepNavy950 else PureWhite) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Gold400,
                                containerColor = DeepNavy800.copy(alpha = 0.7f)
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (activeSection) {
                AdminSection.OVERVIEW -> AdminOverviewTab(
                    allUsers = allUsers,
                    allWallets = allWallets,
                    allTransactions = allTransactions,
                    allPayments = allPayments,
                    allKyc = allKyc,
                    allWithdrawals = allWithdrawals,
                    allLoans = allLoans,
                    allVideos = allVideos,
                    allVideoViews = allVideoViews,
                    allTasks = allTasks,
                    allTaskSubmissions = allTaskSubmissions
                )
                AdminSection.USERS -> AdminUsersTab(
                    users = allUsers,
                    wallets = allWallets,
                    transactions = allTransactions,
                    onUpdateStatus = onUpdateUserStatus,
                    onUnlockUser = onUnlockUser,
                    onCorrectProfile = onCorrectUserProfile,
                    onAdjustBalance = onAdjustBalance
                )
                AdminSection.PAYMENTS -> AdminPaymentsTab(allPayments, allUsers, onReviewPayment)
                AdminSection.KYC -> AdminKycTab(allKyc, onReviewKyc)
                AdminSection.WITHDRAWALS -> AdminWithdrawalsTab(allWithdrawals, allUsers, onProcessWithdrawal)
                AdminSection.LOANS -> AdminLoansTab(allLoans, loanProducts, allUsers, onReviewLoan, onSaveLoanProduct)
                AdminSection.VIDEOS_TASKS -> AdminVideosAndTasksTab(
                    videos = allVideos,
                    tasks = allTasks,
                    submissions = allTaskSubmissions,
                    onUpdateVideoStatus = onUpdateVideoStatus,
                    onUpdateTaskStatus = onUpdateTaskStatus,
                    onReviewSubmission = onReviewTaskSubmission
                )
                AdminSection.ADS -> AdminAdsTab(allAds, onSaveAd, onDeleteAd)
                AdminSection.LEDGER -> AdminGlobalLedgerTab(allTransactions, allUsers)
                AdminSection.SETTINGS -> AdminSettingsTab(settings, onSaveSettings)
                AdminSection.SUPPORT_AUDIT -> AdminSupportAndAuditTab(allTickets, auditLogs, onReplyTicket)
            }
        }
    }
}

@Composable
private fun AdminOverviewTab(
    allUsers: List<UserEntity>,
    allWallets: List<WalletEntity>,
    allTransactions: List<FinancialTransactionEntity>,
    allPayments: List<PaymentVerificationEntity>,
    allKyc: List<KycApplicationEntity>,
    allWithdrawals: List<WithdrawalRequestEntity>,
    allLoans: List<LoanApplicationEntity>,
    allVideos: List<VideoCampaignEntity>,
    allVideoViews: List<VideoViewEntity>,
    allTasks: List<PaidTaskEntity>,
    allTaskSubmissions: List<TaskSubmissionEntity>
) {
    val members = allUsers.filter { it.role != "SUPER_ADMIN" }
    val approvedUsers = members.count { it.accountStatus == "ACTIVE" || it.accountStatus == "APPROVED_AWAITING_ACTIVATION" }
    val pendingUsers = members.count { it.accountStatus == "PENDING" }
    val rejectedUsers = members.count { it.accountStatus == "REJECTED" }
    val totalSavings = allWallets.sumOf { it.savingsBalanceRwf }
    val totalDeposits = allWallets.sumOf { it.totalDepositsRwf }
    val totalWithdrawals = allWithdrawals.filter { it.status == "COMPLETED" }.sumOf { it.requestedAmountRwf }
    val pendingWithdrawals = allWithdrawals.count { it.status == "PENDING" || it.status == "UNDER_REVIEW" || it.status == "APPROVED" }
    val totalLoans = allLoans.filter { it.status == "DISBURSED" || it.status == "REPAID" }.sumOf { it.requestedAmountRwf }
    val pendingLoans = allLoans.count { it.status == "PENDING" || it.status == "UNDER_REVIEW" }
    val activeVideos = allVideos.count { it.status == "ACTIVE" }
    val totalVideoViews = allVideoViews.size
    val activeTasks = allTasks.count { it.status == "ACTIVE" }
    val pendingTaskReviews = allTaskSubmissions.count { it.status == "PENDING_REVIEW" }
    val totalRewards = allWallets.sumOf { it.totalEarningsRwf }
    val totalRevenue = allTransactions.filter { it.category == "WITHDRAWAL_FEE" && it.status == "COMPLETED" }.sumOf { it.amountRwf } +
        allVideos.sumOf { it.platformFeeRwf } + allTasks.sumOf { it.platformFeeRwf }
    val pendingKyc = allKyc.count { it.status == "PENDING" || it.status == "UNDER_REVIEW" }
    val pendingPayments = allPayments.count { it.status == "PENDING" }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text("Platform Intelligence (18 KPIs)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        val kpis = listOf(
            "Total Registered Users" to "${members.size}",
            "Approved Users" to "$approvedUsers",
            "Pending Users" to "$pendingUsers",
            "Rejected Users" to "$rejectedUsers",
            "Total Savings" to SecurityUtils.formatRwf(totalSavings),
            "Total Deposits" to SecurityUtils.formatRwf(totalDeposits),
            "Total Withdrawals" to SecurityUtils.formatRwf(totalWithdrawals),
            "Pending Withdrawals" to "$pendingWithdrawals",
            "Total Loans Disbursed" to SecurityUtils.formatRwf(totalLoans),
            "Pending Loan Apps" to "$pendingLoans",
            "Active Video Campaigns" to "$activeVideos",
            "Total Video Views" to "$totalVideoViews",
            "Active Tasks" to "$activeTasks",
            "Pending Task Reviews" to "$pendingTaskReviews",
            "Rewards Distributed" to SecurityUtils.formatRwf(totalRewards),
            "Platform Revenue" to SecurityUtils.formatRwf(totalRevenue),
            "Pending KYC Apps" to "$pendingKyc",
            "Payment Verifications" to "$pendingPayments"
        )
        items(kpis.chunked(2)) { pair ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                pair.forEach { (title, value) ->
                    GlassmorphicCard(modifier = Modifier.weight(1f)) {
                        Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(4.dp))
                        Text(value, style = MaterialTheme.typography.titleMedium.copy(fontFamily = MonoFontFamily), fontWeight = FontWeight.Bold, color = RoyalBlue600)
                    }
                }
            }
        }
        item {
            FinancialBarChart(
                title = "Platform Financial Flow (RWF)",
                bars = listOf(
                    ChartBarData("Deposits", totalDeposits.toFloat(), RoyalBlue600),
                    ChartBarData("Savings", totalSavings.toFloat(), Emerald600),
                    ChartBarData("Loans", totalLoans.toFloat(), Purple700),
                    ChartBarData("Rewards", totalRewards.toFloat(), Gold500),
                    ChartBarData("Revenue", totalRevenue.toFloat(), Cyan500)
                )
            )
        }
    }
}

@Composable
private fun AdminUsersTab(
    users: List<UserEntity>,
    wallets: List<WalletEntity>,
    transactions: List<FinancialTransactionEntity>,
    onUpdateStatus: (String, String, String) -> Unit,
    onUnlockUser: (String) -> Unit,
    onCorrectProfile: (String, String, String, String, String) -> Unit,
    onAdjustBalance: (String, Long, String) -> Unit
) {
    var search by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("ALL") }
    var selectedUser by remember { mutableStateOf<UserEntity?>(null) }

    val walletMap = remember(wallets) { wallets.associateBy { it.userId } }
    val filtered = remember(users, search, statusFilter) {
        users.filter { u ->
            u.role != "SUPER_ADMIN" &&
                (statusFilter == "ALL" || u.accountStatus == statusFilter) &&
                (search.isBlank() || u.firstName.contains(search, true) || u.secondName.contains(search, true) || u.email.contains(search, true) || u.nationalIdNumber.contains(search, true))
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(value = search, onValueChange = { search = it }, label = { Text("Search name, email, National ID") }, leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                DropdownSelectorField(
                    label = "Filter Status",
                    selectedValue = statusFilter,
                    options = listOf("ALL", "PENDING", "APPROVED_AWAITING_ACTIVATION", "ACTIVE", "CORRECTION_REQUIRED", "REJECTED", "SUSPENDED", "BLOCKED"),
                    onSelected = { statusFilter = it }
                )
            }
        }
        items(filtered, key = { it.id }) { u ->
            val w = walletMap[u.id]
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("${u.firstName} ${u.secondName}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("${u.email} • ${u.phone}", style = MaterialTheme.typography.bodyMedium)
                        Text("NID: ${u.nationalIdNumber} • Balance: ${SecurityUtils.formatRwf(w?.availableBalanceRwf ?: 0L)}", style = MaterialTheme.typography.labelMedium)
                    }
                    StatusBadge(u.accountStatus)
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (u.accountStatus == "PENDING" || u.accountStatus == "CORRECTION_REQUIRED") {
                        Button(onClick = { onUpdateStatus(u.id, "APPROVE", "Registration approved") }, modifier = Modifier.weight(1f).testTag("admin_approve_user_${u.id}")) {
                            Text("Approve")
                        }
                    }
                    OutlinedButton(onClick = { selectedUser = u }, modifier = Modifier.weight(1f).testTag("admin_inspect_user_${u.id}")) {
                        Text("Manage Profile")
                    }
                }
            }
        }
    }

    selectedUser?.let { u ->
        val userTx = transactions.filter { it.userId == u.id }
        AdminUserDetailDialog(
            user = u,
            wallet = walletMap[u.id],
            transactions = userTx,
            onDismiss = { selectedUser = null },
            onUpdateStatus = { st, note -> onUpdateStatus(u.id, st, note); selectedUser = null },
            onUnlock = { onUnlockUser(u.id); selectedUser = null },
            onCorrectProfile = { f, s, nid, reason -> onCorrectProfile(u.id, f, s, nid, reason); selectedUser = null },
            onAdjustBalance = { delta, reason -> onAdjustBalance(u.id, delta, reason); selectedUser = null }
        )
    }
}

@Composable
private fun AdminUserDetailDialog(
    user: UserEntity,
    wallet: WalletEntity?,
    transactions: List<FinancialTransactionEntity>,
    onDismiss: () -> Unit,
    onUpdateStatus: (String, String) -> Unit,
    onUnlock: () -> Unit,
    onCorrectProfile: (String, String, String, String) -> Unit,
    onAdjustBalance: (Long, String) -> Unit
) {
    var note by remember { mutableStateOf("") }
    var adjustDelta by remember { mutableStateOf("") }
    var adjustReason by remember { mutableStateOf("") }
    var editFirst by remember { mutableStateOf(user.firstName) }
    var editSecond by remember { mutableStateOf(user.secondName) }
    var editNid by remember { mutableStateOf(user.nationalIdNumber) }
    var editReason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${user.firstName} ${user.secondName}") },
        text = {
            LazyColumn(modifier = Modifier.heightIn(max = 460.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    Text("ID: ${user.id} | Status: ${user.accountStatus} | KYC: ${user.kycStatus}", style = MaterialTheme.typography.labelLarge)
                    Text("Education: ${user.educationCategory} - ${user.institutionName} (${user.academicLevelOrClass}, ${user.facultyOrCombination})", style = MaterialTheme.typography.bodyMedium)
                    Text("Birth: ${user.birthCountry}, ${user.birthProvince}, ${user.birthDistrict}, ${user.birthSector}", style = MaterialTheme.typography.bodyMedium)
                    Text("Residence: ${user.resCountry}, ${user.resProvince}, ${user.resDistrict}", style = MaterialTheme.typography.bodyMedium)
                    Text("Wallet: ${SecurityUtils.formatRwf(wallet?.availableBalanceRwf ?: 0L)} | Savings: ${SecurityUtils.formatRwf(wallet?.savingsBalanceRwf ?: 0L)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    if (user.nationalIdFrontUri.isNotBlank() && !user.nationalIdFrontUri.startsWith("system://")) {
                        Spacer(Modifier.height(6.dp))
                        AsyncImage(
                            model = File(user.nationalIdFrontUri),
                            contentDescription = "Protected National ID",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(10.dp))
                        )
                    }
                }
                item {
                    HorizontalDivider()
                    OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Status / Rejection / Correction Note") }, modifier = Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 6.dp)) {
                        Button(onClick = { onUpdateStatus("APPROVE", note) }, modifier = Modifier.weight(1f)) { Text("Approve") }
                        OutlinedButton(onClick = { onUpdateStatus("CORRECTION_REQUIRED", note) }, modifier = Modifier.weight(1f)) { Text("Correct") }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 6.dp)) {
                        OutlinedButton(onClick = { onUpdateStatus("SUSPENDED", note) }, modifier = Modifier.weight(1f)) { Text("Suspend") }
                        OutlinedButton(onClick = { onUpdateStatus("REJECTED", note) }, modifier = Modifier.weight(1f)) { Text("Reject") }
                        OutlinedButton(onClick = onUnlock, modifier = Modifier.weight(1f)) { Text("Unlock") }
                    }
                }
                item {
                    HorizontalDivider()
                    Text("Audited Profile Correction", style = MaterialTheme.typography.labelLarge)
                    OutlinedTextField(value = editFirst, onValueChange = { editFirst = it }, label = { Text("First Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = editSecond, onValueChange = { editSecond = it }, label = { Text("Second Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = editNid, onValueChange = { editNid = it }, label = { Text("National ID") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = editReason, onValueChange = { editReason = it }, label = { Text("Mandatory Audit Reason *") }, modifier = Modifier.fillMaxWidth())
                    Button(onClick = { onCorrectProfile(editFirst, editSecond, editNid, editReason) }, modifier = Modifier.fillMaxWidth()) {
                        Text("Save Audited Correction")
                    }
                }
                item {
                    HorizontalDivider()
                    Text("Manual Balance Adjustment (Audited)", style = MaterialTheme.typography.labelLarge)
                    OutlinedTextField(value = adjustDelta, onValueChange = { adjustDelta = it }, label = { Text("Delta RWF (e.g. 500 or -200)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = adjustReason, onValueChange = { adjustReason = it }, label = { Text("Adjustment Reason *") }, modifier = Modifier.fillMaxWidth())
                    Button(onClick = { onAdjustBalance(adjustDelta.toLongOrNull() ?: 0L, adjustReason) }, modifier = Modifier.fillMaxWidth()) {
                        Text("Apply Adjustment")
                    }
                }
                item {
                    Text("Recent User Transactions (${transactions.size})", style = MaterialTheme.typography.labelLarge)
                    transactions.take(5).forEach { tx ->
                        Text("• ${tx.category}: ${SecurityUtils.formatRwf(tx.netAmountRwf)} (${tx.status})", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
private fun AdminPaymentsTab(
    payments: List<PaymentVerificationEntity>,
    users: List<UserEntity>,
    onReviewPayment: (String, Boolean, String) -> Unit
) {
    val userMap = remember(users) { users.associateBy { it.id } }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(payments, key = { it.id }) { pv ->
            var note by remember(pv.id) { mutableStateOf("") }
            val u = userMap[pv.userId]
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("${pv.paymentType} • ${SecurityUtils.formatRwf(pv.amountRwf)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("User: ${u?.firstName ?: pv.userId} (${u?.email ?: ""})", style = MaterialTheme.typography.bodyMedium)
                        Text("Submitted TXN ID: ${pv.submittedTransactionId} | USSD: ${pv.ussdReference}", style = MaterialTheme.typography.labelMedium)
                    }
                    StatusBadge(pv.status)
                }
                if (pv.status == "PENDING") {
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Verification Note") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { onReviewPayment(pv.id, true, note) }, modifier = Modifier.weight(1f).testTag("verify_pay_btn_${pv.id}")) { Text("Verify & Credit") }
                        OutlinedButton(onClick = { onReviewPayment(pv.id, false, note) }, modifier = Modifier.weight(1f)) { Text("Reject") }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminKycTab(
    kycList: List<KycApplicationEntity>,
    onReviewKyc: (String, String, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(kycList, key = { it.id }) { kyc ->
            var reason by remember(kyc.id) { mutableStateOf(kyc.rejectionReason) }
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(kyc.fullLegalName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("User ID: ${kyc.userId} • Phone: ${kyc.phone}", style = MaterialTheme.typography.bodyMedium)
                        Text("NID: ${kyc.nationalIdNumber} • ${kyc.province}, ${kyc.district}, ${kyc.sector}", style = MaterialTheme.typography.labelMedium)
                    }
                    StatusBadge(kyc.status)
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(kyc.idFrontUri, kyc.idBackUri, kyc.selfieUri).filter { it.isNotBlank() && !it.startsWith("system://") }.forEach { path ->
                        AsyncImage(
                            model = File(path),
                            contentDescription = "KYC Document",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(72.dp).clip(RoundedCornerShape(8.dp))
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = reason, onValueChange = { reason = it }, label = { Text("Verification / Rejection Note") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(onClick = { onReviewKyc(kyc.id, "APPROVED", reason) }, modifier = Modifier.weight(1f).testTag("approve_kyc_btn_${kyc.id}")) { Text("Approve") }
                    OutlinedButton(onClick = { onReviewKyc(kyc.id, "ADDITIONAL_INFO_REQUIRED", reason) }, modifier = Modifier.weight(1f)) { Text("Request Docs") }
                    OutlinedButton(onClick = { onReviewKyc(kyc.id, "REJECTED", reason) }, modifier = Modifier.weight(1f)) { Text("Reject") }
                }
            }
        }
    }
}

@Composable
private fun AdminWithdrawalsTab(
    withdrawals: List<WithdrawalRequestEntity>,
    users: List<UserEntity>,
    onProcessWithdrawal: (String, String, String) -> Unit
) {
    val userMap = remember(users) { users.associateBy { it.id } }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(withdrawals, key = { it.id }) { w ->
            var note by remember(w.id) { mutableStateOf("") }
            val u = userMap[w.userId]
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("${w.provider} • ${w.destinationPhone}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Member: ${u?.firstName ?: w.userId} | Net Payout: ${SecurityUtils.formatRwf(w.netAmountRwf)} (Fee: ${SecurityUtils.formatRwf(w.feeAmountRwf)})", style = MaterialTheme.typography.bodyMedium)
                    }
                    StatusBadge(w.status)
                }
                if (w.status !in listOf("COMPLETED", "REJECTED", "CANCELLED")) {
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Payout / Rejection Note") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(onClick = { onProcessWithdrawal(w.id, "COMPLETED", note) }, modifier = Modifier.weight(1f)) { Text("Confirm Paid") }
                        OutlinedButton(onClick = { onProcessWithdrawal(w.id, "PROCESSING", note) }, modifier = Modifier.weight(1f)) { Text("Processing") }
                        OutlinedButton(onClick = { onProcessWithdrawal(w.id, "REJECTED", note) }, modifier = Modifier.weight(1f)) { Text("Reject & Refund") }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminLoansTab(
    loans: List<LoanApplicationEntity>,
    loanProducts: List<LoanProductEntity>,
    users: List<UserEntity>,
    onReviewLoan: (String, String, String) -> Unit,
    onSaveLoanProduct: (LoanProductEntity) -> Unit
) {
    val userMap = remember(users) { users.associateBy { it.id } }
    var productName by remember { mutableStateOf("") }
    var productDesc by remember { mutableStateOf("") }
    var minAmt by remember { mutableStateOf("5000") }
    var maxAmt by remember { mutableStateOf("250000") }
    var interestRate by remember { mutableStateOf("10.0") }
    var maxMonths by remember { mutableStateOf("6") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Text("Configure Loan Product", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(value = productName, onValueChange = { productName = it }, label = { Text("Product Name") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(value = productDesc, onValueChange = { productDesc = it }, label = { Text("Product Description") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = minAmt, onValueChange = { minAmt = it }, label = { Text("Min (RWF)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                    OutlinedTextField(value = maxAmt, onValueChange = { maxAmt = it }, label = { Text("Max (RWF)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = interestRate, onValueChange = { interestRate = it }, label = { Text("Interest %") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.weight(1f))
                    OutlinedTextField(value = maxMonths, onValueChange = { maxMonths = it }, label = { Text("Max Months") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        onSaveLoanProduct(
                            LoanProductEntity(
                                id = SecurityUtils.generateId("LP"),
                                name = productName.trim(),
                                description = productDesc.trim().ifBlank { "Configured by Super Administration" },
                                minAmountRwf = minAmt.toLongOrNull() ?: 5000L,
                                maxAmountRwf = maxAmt.toLongOrNull() ?: 250000L,
                                interestRatePercent = interestRate.toDoubleOrNull() ?: 10.0,
                                maxDurationMonths = maxMonths.toIntOrNull() ?: 6,
                                isActive = true
                            )
                        )
                        productName = ""
                        productDesc = ""
                    },
                    modifier = Modifier.fillMaxWidth().testTag("admin_save_loan_product_btn")
                ) {
                    Text("Publish Loan Product")
                }
                if (loanProducts.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text("Active Catalog (${loanProducts.size})", style = MaterialTheme.typography.labelLarge, color = RoyalBlue600)
                    loanProducts.forEach { lp ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(lp.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text("${SecurityUtils.formatRwf(lp.minAmountRwf)} - ${SecurityUtils.formatRwf(lp.maxAmountRwf)} • ${lp.interestRatePercent}% • ${lp.maxDurationMonths}m", style = MaterialTheme.typography.labelSmall)
                            }
                            TextButton(onClick = { onSaveLoanProduct(lp.copy(isActive = !lp.isActive)) }) {
                                Text(if (lp.isActive) "Disable" else "Enable")
                            }
                        }
                    }
                }
            }
        }
        items(loans, key = { it.id }) { loan ->
            var note by remember(loan.id) { mutableStateOf(loan.adminNote) }
            val u = userMap[loan.userId]
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("${loan.productName} • ${SecurityUtils.formatRwf(loan.requestedAmountRwf)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Applicant: ${u?.firstName ?: loan.userId} • ${loan.repaymentMonths}m @ ${loan.interestRatePercent}%", style = MaterialTheme.typography.bodyMedium)
                        Text("Purpose: ${loan.purpose} | Info: ${loan.supportingInfo}", style = MaterialTheme.typography.labelMedium)
                    }
                    StatusBadge(loan.status)
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Review Note") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (loan.status == "PENDING" || loan.status == "UNDER_REVIEW") {
                        Button(onClick = { onReviewLoan(loan.id, "DISBURSED", note) }, modifier = Modifier.weight(1f)) { Text("Approve & Disburse") }
                        OutlinedButton(onClick = { onReviewLoan(loan.id, "REJECTED", note) }, modifier = Modifier.weight(1f)) { Text("Reject") }
                    } else if (loan.status == "DISBURSED") {
                        OutlinedButton(onClick = { onReviewLoan(loan.id, "OVERDUE", note) }, modifier = Modifier.weight(1f)) { Text("Mark Overdue") }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminGlobalLedgerTab(
    transactions: List<FinancialTransactionEntity>,
    users: List<UserEntity>
) {
    var search by remember { mutableStateOf("") }
    var categoryFilter by remember { mutableStateOf("ALL") }
    val userMap = remember(users) { users.associateBy { it.id } }
    val categories = remember(transactions) {
        listOf("ALL") + transactions.map { it.category }.distinct().sorted()
    }
    val filtered = remember(transactions, search, categoryFilter) {
        transactions.filter { tx ->
            (categoryFilter == "ALL" || tx.category == categoryFilter) &&
                (search.isBlank() || tx.id.contains(search, true) || tx.paymentReference.contains(search, true) ||
                    tx.description.contains(search, true) || (userMap[tx.userId]?.email?.contains(search, true) == true))
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Text("Platform Financial Ledger (${filtered.size} entries)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    label = { Text("Search TXN ID, reference, email") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                DropdownSelectorField(
                    label = "Filter Category",
                    selectedValue = categoryFilter,
                    options = categories,
                    onSelected = { categoryFilter = it }
                )
            }
        }
        items(filtered, key = { it.id }) { tx ->
            val u = userMap[tx.userId]
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(tx.category.replace("_", " "), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("${u?.firstName ?: tx.userId} (${u?.email ?: "-"})", style = MaterialTheme.typography.labelMedium, color = RoyalBlue600)
                        Text(tx.description, style = MaterialTheme.typography.bodyMedium)
                        Text("${tx.id} • ${SecurityUtils.formatKigaliDateTime(tx.createdAt)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(SecurityUtils.formatRwf(tx.netAmountRwf), style = MaterialTheme.typography.titleMedium.copy(fontFamily = MonoFontFamily), fontWeight = FontWeight.Bold, color = RoyalBlue600)
                        if (tx.feeRwf > 0L) {
                            Text("Fee: ${SecurityUtils.formatRwf(tx.feeRwf)}", style = MaterialTheme.typography.labelSmall, color = Crimson600)
                        }
                        StatusBadge(tx.status)
                    }
                }
            }
        }
    }
}
