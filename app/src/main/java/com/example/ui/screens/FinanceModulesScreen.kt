package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.RwandaReferenceData
import com.example.data.local.*
import com.example.data.security.SecurityUtils
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun SavingsAndDepositsScreen(
    user: UserEntity,
    wallet: WalletEntity,
    settings: SystemSettingsEntity,
    savingsAccounts: List<SavingsAccountEntity>,
    payments: List<PaymentVerificationEntity>,
    onSubmitDeposit: (Long, String) -> Unit,
    onCreateSavings: (String, String, String, Long, Long) -> Unit,
    onTopUpSavings: (String, Long) -> Unit,
    onWithdrawSavings: (String, Long) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    var tabIndex by remember { mutableIntStateOf(0) }
    var depositAmount by remember { mutableStateOf("5000") }
    var depositRef by remember { mutableStateOf("") }

    var planType by remember { mutableStateOf("FLEXIBLE") }
    var purpose by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var targetAmount by remember { mutableStateOf("50000") }
    var initialDeposit by remember { mutableStateOf("5000") }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            ModuleTopBar("Savings & Deposits", "Available: ${SecurityUtils.formatRwf(wallet.availableBalanceRwf)}", onBack)
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                TabRow(selectedTabIndex = tabIndex) {
                    Tab(selected = tabIndex == 0, onClick = { tabIndex = 0 }, text = { Text("Save Money") })
                    Tab(selected = tabIndex == 1, onClick = { tabIndex = 1 }, text = { Text("Deposit RWF") })
                }
            }

            if (tabIndex == 0) {
                item {
                    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                        Text("Create Savings Vault", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        if (settings.kycRequiredForFinance && user.kycStatus != "APPROVED") {
                            Spacer(Modifier.height(6.dp))
                            Text("KYC Approval is required before opening savings vaults.", color = Crimson600, style = MaterialTheme.typography.bodyMedium)
                        }
                        Spacer(Modifier.height(10.dp))
                        DropdownSelectorField(
                            label = "Savings Option",
                            selectedValue = planType,
                            options = listOf("FLEXIBLE", "GOAL_BASED", "LONG_TERM"),
                            onSelected = { planType = it }
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(value = purpose, onValueChange = { purpose = it }, label = { Text("Savings Purpose (e.g. Tuition, Emergency)") }, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = targetAmount,
                                onValueChange = { targetAmount = it },
                                label = { Text("Target (RWF)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = initialDeposit,
                                onValueChange = { initialDeposit = it },
                                label = { Text("Amount (RWF)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = {
                                onCreateSavings(
                                    planType,
                                    purpose,
                                    desc,
                                    targetAmount.toLongOrNull() ?: 0L,
                                    initialDeposit.toLongOrNull() ?: 0L
                                )
                                purpose = ""
                                desc = ""
                            },
                            modifier = Modifier.fillMaxWidth().testTag("create_savings_btn")
                        ) {
                            Text("Deposit Into Savings Vault")
                        }
                    }
                }

                item {
                    Text("Your Savings Vaults (${savingsAccounts.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                items(savingsAccounts, key = { it.id }) { sav ->
                    var topUpInput by remember(sav.id) { mutableStateOf("1000") }
                    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(sav.purpose, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("${sav.planType.replace("_", " ")} • Created ${SecurityUtils.formatKigaliDate(sav.createdAt)}", style = MaterialTheme.typography.labelMedium)
                            }
                            Text(SecurityUtils.formatRwf(sav.balanceRwf), style = MaterialTheme.typography.titleMedium.copy(fontFamily = MonoFontFamily), color = Emerald600, fontWeight = FontWeight.Bold)
                        }
                        if (sav.description.isNotBlank()) {
                            Text(sav.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
                        }
                        Spacer(Modifier.height(8.dp))
                        val progress = (sav.balanceRwf.toFloat() / sav.targetAmountRwf.coerceAtLeast(1L).toFloat()).coerceIn(0f, 1f)
                        LinearProgressIndicator(progress = { progress }, color = Emerald600, modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(50)))
                        Text("Progress: ${SecurityUtils.formatRwf(sav.balanceRwf)} / ${SecurityUtils.formatRwf(sav.targetAmountRwf)} (Total Deposited: ${SecurityUtils.formatRwf(sav.totalDepositedRwf)})", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 4.dp))
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = topUpInput,
                                onValueChange = { topUpInput = it },
                                label = { Text("RWF") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            Button(onClick = { onTopUpSavings(sav.id, topUpInput.toLongOrNull() ?: 0L) }) { Text("Add") }
                            OutlinedButton(onClick = { onWithdrawSavings(sav.id, topUpInput.toLongOrNull() ?: 0L) }) { Text("Withdraw") }
                        }
                    }
                }
            } else {
                val parsedAmount = depositAmount.toLongOrNull() ?: 0L
                item {
                    OutlinedTextField(
                        value = depositAmount,
                        onValueChange = { depositAmount = it },
                        label = { Text("Deposit Amount (RWF)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (parsedAmount > 0) {
                    item {
                        UssdPaymentInstructionCard(
                            paymentNumber = settings.paymentNumber,
                            amountRwf = parsedAmount,
                            title = "Deposit via Rwanda Mobile Money"
                        )
                    }
                }
                item {
                    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = depositRef,
                            onValueChange = { depositRef = it },
                            label = { Text("Mobile Money Transaction ID / Reference") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(10.dp))
                        Button(
                            onClick = {
                                onSubmitDeposit(parsedAmount, depositRef)
                                depositRef = ""
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Submit Deposit for Verification")
                        }
                    }
                }
                items(payments.filter { it.paymentType == "DEPOSIT" }, key = { it.id }) { pv ->
                    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text(SecurityUtils.formatRwf(pv.amountRwf), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("Ref: ${pv.submittedTransactionId} • ${SecurityUtils.formatKigaliDateTime(pv.createdAt)}", style = MaterialTheme.typography.labelMedium)
                            }
                            StatusBadge(pv.status)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WithdrawalScreen(
    user: UserEntity,
    wallet: WalletEntity,
    settings: SystemSettingsEntity,
    withdrawals: List<WithdrawalRequestEntity>,
    onRequestWithdrawal: (String, String, Long) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    var provider by remember { mutableStateOf("MTN Mobile Money") }
    var phone by remember { mutableStateOf(user.phone) }
    var amountInput by remember { mutableStateOf("10000") }

    val requested = amountInput.toLongOrNull() ?: 0L
    val feeAmount = Math.round(requested * (settings.withdrawalFeePercent / 100.0))
    val netAmount = (requested - feeAmount).coerceAtLeast(0L)

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            ModuleTopBar("Withdraw Funds", "Available: ${SecurityUtils.formatRwf(wallet.availableBalanceRwf)}", onBack)
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Request Mobile Money Payout", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    if (user.kycStatus != "APPROVED") {
                        Spacer(Modifier.height(6.dp))
                        Text("KYC verification must be APPROVED before requesting withdrawals.", color = Crimson600, style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(Modifier.height(10.dp))
                    DropdownSelectorField(
                        label = "Payment Destination",
                        selectedValue = provider,
                        options = listOf("MTN Mobile Money", "Airtel Money"),
                        onSelected = { provider = it }
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Destination Rwanda Phone Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { amountInput = it },
                        label = { Text("Requested Amount (Min ${SecurityUtils.formatRwf(settings.minWithdrawalRwf)})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("withdraw_amount_input")
                    )
                    Spacer(Modifier.height(12.dp))
                    Surface(shape = MaterialTheme.shapes.medium, color = DeepNavy900, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Transparent Fee Calculation", style = MaterialTheme.typography.labelLarge, color = Gold400)
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Requested Amount", color = Slate200)
                                Text(SecurityUtils.formatRwf(requested), color = PureWhite, fontFamily = MonoFontFamily)
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Platform Fee (${settings.withdrawalFeePercent}%)", color = Slate200)
                                Text("-${SecurityUtils.formatRwf(feeAmount)}", color = Crimson500, fontFamily = MonoFontFamily)
                            }
                            HorizontalDivider(color = PureWhite.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Net Amount to Receive", color = Emerald400, fontWeight = FontWeight.Bold)
                                Text(SecurityUtils.formatRwf(netAmount), color = Emerald400, fontWeight = FontWeight.Bold, fontFamily = MonoFontFamily)
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { onRequestWithdrawal(provider, phone, requested) },
                        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("confirm_withdraw_btn")
                    ) {
                        Text("Confirm Withdrawal Request")
                    }
                }
            }

            item { Text("Withdrawal History (${withdrawals.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            items(withdrawals, key = { it.id }) { w ->
                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("${w.provider} • ${w.destinationPhone}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Requested: ${SecurityUtils.formatRwf(w.requestedAmountRwf)} | Fee: ${SecurityUtils.formatRwf(w.feeAmountRwf)} | Net: ${SecurityUtils.formatRwf(w.netAmountRwf)}", style = MaterialTheme.typography.bodyMedium)
                            Text("Ref: ${w.reference} • ${SecurityUtils.formatKigaliDateTime(w.createdAt)}", style = MaterialTheme.typography.labelSmall)
                            if (w.rejectionReason.isNotBlank()) {
                                Text("Note: ${w.rejectionReason}", style = MaterialTheme.typography.labelMedium, color = Crimson600)
                            }
                        }
                        StatusBadge(w.status)
                    }
                }
            }
        }
    }
}

@Composable
fun LoansScreen(
    user: UserEntity,
    wallet: WalletEntity,
    loanProducts: List<LoanProductEntity>,
    loans: List<LoanApplicationEntity>,
    repayments: List<LoanRepaymentEntity>,
    onApplyLoan: (String, Long, Int, String, String) -> Unit,
    onRepayLoan: (String, Long) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    var selectedProduct by remember(loanProducts) { mutableStateOf(loanProducts.firstOrNull()) }
    var amountInput by remember { mutableStateOf("50000") }
    var monthsInput by remember { mutableStateOf("3") }
    var purpose by remember { mutableStateOf("") }
    var supportingInfo by remember { mutableStateOf("") }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            ModuleTopBar("Loans & Credit", "Outstanding: ${SecurityUtils.formatRwf(wallet.loanBalanceRwf)}", onBack)
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Apply for a Loan", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(
                        "Responsible Lending Notice: Loan approvals require KYC eligibility checks and human administrator review.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gold600
                    )
                    Spacer(Modifier.height(10.dp))
                    if (loanProducts.isNotEmpty()) {
                        DropdownSelectorField(
                            label = "Loan Product",
                            selectedValue = selectedProduct?.name ?: loanProducts.first().name,
                            options = loanProducts.map { it.name },
                            onSelected = { name -> selectedProduct = loanProducts.firstOrNull { it.name == name } }
                        )
                        selectedProduct?.let { p ->
                            Text(
                                "${p.description} (${SecurityUtils.formatRwf(p.minAmountRwf)} - ${SecurityUtils.formatRwf(p.maxAmountRwf)}, ${p.interestRatePercent}% interest, max ${p.maxDurationMonths}m)",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = amountInput,
                            onValueChange = { amountInput = it },
                            label = { Text("Requested Amount (RWF)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = monthsInput,
                            onValueChange = { monthsInput = it },
                            label = { Text("Months") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(0.6f)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = purpose, onValueChange = { purpose = it }, label = { Text("Loan Purpose *") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = supportingInfo, onValueChange = { supportingInfo = it }, label = { Text("Supporting Information / Repayment Plan") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            selectedProduct?.let { prod ->
                                onApplyLoan(prod.id, amountInput.toLongOrNull() ?: 0L, monthsInput.toIntOrNull() ?: 1, purpose, supportingInfo)
                                purpose = ""
                                supportingInfo = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("submit_loan_btn")
                    ) {
                        Text("Submit Loan Application")
                    }
                }
            }

            item { Text("Your Loans & Repayment Schedules (${loans.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            items(loans, key = { it.id }) { loan ->
                var repayInput by remember(loan.id) { mutableStateOf("${(loan.totalRepayableRwf / loan.repaymentMonths.coerceAtLeast(1)).coerceAtLeast(1000L)}") }
                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(loan.productName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Principal: ${SecurityUtils.formatRwf(loan.requestedAmountRwf)} • Total Repayable: ${SecurityUtils.formatRwf(loan.totalRepayableRwf)} (${loan.interestRatePercent}%)", style = MaterialTheme.typography.bodyMedium)
                            Text("Outstanding: ${SecurityUtils.formatRwf(loan.outstandingBalanceRwf)} • Paid: ${SecurityUtils.formatRwf(loan.amountPaidRwf)}", style = MaterialTheme.typography.bodyMedium, color = RoyalBlue600)
                            Text("Term: ${loan.repaymentMonths} month(s) • Purpose: ${loan.purpose}", style = MaterialTheme.typography.labelMedium)
                        }
                        StatusBadge(loan.status)
                    }
                    if (loan.status == "DISBURSED" || loan.status == "OVERDUE") {
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = repayInput,
                                onValueChange = { repayInput = it },
                                label = { Text("Repay RWF") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            Button(onClick = { onRepayLoan(loan.id, repayInput.toLongOrNull() ?: 0L) }) {
                                Text("Pay Now")
                            }
                        }
                    }
                }
            }

            if (repayments.isNotEmpty()) {
                item { Text("Repayment History", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                items(repayments, key = { it.id }) { rep ->
                    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Loan ${rep.loanId} • ${rep.reference}", style = MaterialTheme.typography.bodyMedium)
                            Text(SecurityUtils.formatRwf(rep.amountRwf), fontFamily = MonoFontFamily, fontWeight = FontWeight.Bold, color = Emerald600)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KycVerificationScreen(
    user: UserEntity,
    existingKyc: KycApplicationEntity?,
    onStoreMedia: (Uri, String) -> String?,
    onSubmitKyc: (String, String, String, String, String, String, String, String, String, String, String, String, String, String, String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    var fullLegalName by remember { mutableStateOf(existingKyc?.fullLegalName ?: "${user.firstName} ${user.secondName}") }
    var nid by remember { mutableStateOf(existingKyc?.nationalIdNumber ?: user.nationalIdNumber) }
    var dob by remember { mutableStateOf(existingKyc?.dateOfBirth ?: user.dateOfBirth) }
    var gender by remember { mutableStateOf(existingKyc?.gender ?: user.gender) }
    var phone by remember { mutableStateOf(existingKyc?.phone ?: user.phone) }
    var email by remember { mutableStateOf(existingKyc?.email ?: user.email) }
    var province by remember { mutableStateOf(existingKyc?.province ?: user.resProvince.ifBlank { RwandaReferenceData.provinces.first() }) }
    var district by remember { mutableStateOf(existingKyc?.district ?: user.resDistrict.ifBlank { "Gasabo" }) }
    var sector by remember { mutableStateOf(existingKyc?.sector ?: user.resSector) }
    var cell by remember { mutableStateOf(existingKyc?.cell ?: user.resCell) }
    var village by remember { mutableStateOf(existingKyc?.village ?: user.resVillage) }
    var idFrontUri by remember { mutableStateOf(existingKyc?.idFrontUri ?: user.nationalIdFrontUri) }
    var idBackUri by remember { mutableStateOf(existingKyc?.idBackUri ?: "") }
    var selfieUri by remember { mutableStateOf(existingKyc?.selfieUri ?: "") }
    var notes by remember { mutableStateOf(existingKyc?.additionalNotes ?: "") }

    val backLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { onStoreMedia(it, "kyc_back")?.let { p -> idBackUri = p } }
    }
    val selfieLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { onStoreMedia(it, "kyc_selfie")?.let { p -> selfieUri = p } }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = { ModuleTopBar("Rwanda KYC Verification", "Status: ${user.kycStatus}", onBack) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Know Your Customer (KYC)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        StatusBadge(user.kycStatus)
                    }
                    if (!existingKyc?.rejectionReason.isNullOrBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Text("Compliance Note: ${existingKyc?.rejectionReason}", color = Crimson600, style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Identity documents are encrypted into private storage and never exposed via public URLs.", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(value = fullLegalName, onValueChange = { fullLegalName = it }, label = { Text("Full Legal Name *") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = nid, onValueChange = { nid = it }, label = { Text("National ID Number *") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = dob, onValueChange = { dob = it }, label = { Text("DOB") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, modifier = Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(8.dp))
                    DropdownSelectorField("Province", province, RwandaReferenceData.provinces, { prov ->
                        province = prov
                        district = RwandaReferenceData.districtsByProvince[prov]?.firstOrNull() ?: ""
                    })
                    Spacer(Modifier.height(8.dp))
                    DropdownSelectorField("District", district, RwandaReferenceData.districtsByProvince[province].orEmpty(), { district = it })
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = sector, onValueChange = { sector = it }, label = { Text("Sector") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = cell, onValueChange = { cell = it }, label = { Text("Cell") }, modifier = Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = village, onValueChange = { village = it }, label = { Text("Village") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Additional Verification Details") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(10.dp))
                    Text("National ID Front: ${if (idFrontUri.isNotBlank()) "Locked from Registration ✓" else "Missing"}", style = MaterialTheme.typography.labelLarge, color = Emerald600)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { backLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (idBackUri.isBlank()) "ID Back Photo" else "ID Back ✓")
                        }
                        OutlinedButton(
                            onClick = { selfieLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (selfieUri.isBlank()) "Selfie Photo" else "Selfie ✓")
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            onSubmitKyc(fullLegalName, nid, dob, gender, phone, email, province, district, sector, cell, village, idFrontUri, idBackUri, selfieUri, notes)
                        },
                        enabled = user.kycStatus != "APPROVED",
                        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("submit_kyc_btn")
                    ) {
                        Text(if (user.kycStatus == "APPROVED") "KYC Verified & Approved" else "Submit KYC for Review")
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionHistoryScreen(
    user: UserEntity,
    transactions: List<FinancialTransactionEntity>,
    onFeedback: (String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    var search by remember { mutableStateOf("") }
    var categoryFilter by remember { mutableStateOf("ALL") }
    var statusFilter by remember { mutableStateOf("ALL") }

    val filtered = remember(transactions, search, categoryFilter, statusFilter) {
        transactions.filter { tx ->
            (categoryFilter == "ALL" || tx.category == categoryFilter) &&
                (statusFilter == "ALL" || tx.status == statusFilter) &&
                (search.isBlank() || tx.id.contains(search, true) || tx.description.contains(search, true) || tx.paymentReference.contains(search, true))
        }
    }

    val csvLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null) {
            context.contentResolver.openOutputStream(uri)?.use { out ->
                SecurityUtils.writeTransactionsCsv(out, filtered, user.email)
            }
            onFeedback("CSV statement exported successfully.")
        }
    }

    val pdfLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        if (uri != null) {
            context.contentResolver.openOutputStream(uri)?.use { out ->
                SecurityUtils.writeTransactionsPdf(out, filtered, "${user.firstName} ${user.secondName}", user.email)
            }
            onFeedback("PDF financial statement saved successfully.")
        }
    }

    val categories = listOf(
        "ALL", "ACCOUNT_ACTIVATION", "WELCOME_BONUS", "DEPOSIT", "SAVING", "SAVINGS_WITHDRAWAL",
        "WITHDRAWAL", "WITHDRAWAL_FEE", "LOAN_DISBURSEMENT", "LOAN_REPAYMENT",
        "VIDEO_REWARD", "VIDEO_CAMPAIGN_PAYMENT", "TASK_REWARD", "TASK_CAMPAIGN_PAYMENT",
        "DAILY_CHECKIN", "REFUND", "ADJUSTMENT"
    )

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = { ModuleTopBar("Transaction History", "Immutable Financial Ledger", onBack) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = search,
                        onValueChange = { search = it },
                        label = { Text("Search TXN ID, Reference, Description") },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DropdownSelectorField("Category", categoryFilter, categories, { categoryFilter = it }, Modifier.weight(1f))
                        DropdownSelectorField("Status", statusFilter, listOf("ALL", "COMPLETED", "PENDING", "REJECTED"), { statusFilter = it }, Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { csvLauncher.launch("efinance_statement_${System.currentTimeMillis()}.csv") },
                            modifier = Modifier.weight(1f).testTag("export_csv_btn")
                        ) {
                            Icon(Icons.Filled.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Export CSV")
                        }
                        Button(
                            onClick = { pdfLauncher.launch("efinance_statement_${System.currentTimeMillis()}.pdf") },
                            modifier = Modifier.weight(1f).testTag("export_pdf_btn")
                        ) {
                            Icon(Icons.Filled.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Export PDF")
                        }
                    }
                }
            }

            if (filtered.isEmpty()) {
                item {
                    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                        Text("No matching ledger records found.", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else {
                items(filtered, key = { it.id }) { tx ->
                    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(tx.id, style = MaterialTheme.typography.labelSmall, color = RoyalBlue600)
                            StatusBadge(tx.status)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(tx.category.replace("_", " "), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(tx.description, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(6.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Amount: ${SecurityUtils.formatRwf(tx.amountRwf)} | Fee: ${SecurityUtils.formatRwf(tx.feeRwf)}", style = MaterialTheme.typography.labelMedium)
                            Text("Net: ${SecurityUtils.formatRwf(tx.netAmountRwf)}", style = MaterialTheme.typography.labelLarge.copy(fontFamily = MonoFontFamily), color = Emerald600, fontWeight = FontWeight.Bold)
                        }
                        Text("Ref: ${tx.paymentReference.ifBlank { "N/A" }} • ${SecurityUtils.formatKigaliDateTime(tx.createdAt)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun ModuleTopBar(title: String, subtitle: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(rememberAnimatedFintechGradient())
            .statusBarsPadding()
            .padding(horizontal = 10.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack, modifier = Modifier.testTag("module_back_btn")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PureWhite)
        }
        Column {
            Text(title, style = MaterialTheme.typography.titleLarge, color = PureWhite, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.labelMedium, color = Cyan400)
        }
    }
}
