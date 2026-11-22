package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.WalletEntity
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.EFinanceViewModel
import com.example.ui.viewmodel.FeedbackBanner
import kotlinx.coroutines.delay

private enum class UnauthRoute {
    PUBLIC,
    LOGIN,
    REGISTER
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EFinanceRwandaTheme {
                EFinanceAppRoot()
            }
        }
    }
}

@Composable
private fun EFinanceAppRoot(vm: EFinanceViewModel = viewModel()) {
    val settings by vm.systemSettings.collectAsStateWithLifecycle()
    val activeAds by vm.activeAds.collectAsStateWithLifecycle()
    val currentUser by vm.currentUser.collectAsStateWithLifecycle()
    val currentWallet by vm.currentWallet.collectAsStateWithLifecycle()
    val feedback by vm.feedback.collectAsStateWithLifecycle()
    val regForm by vm.registrationForm.collectAsStateWithLifecycle()
    val pending2fa by vm.pendingTwoFactorChallenge.collectAsStateWithLifecycle()
    val is2faVerified by vm.isTwoFactorVerified.collectAsStateWithLifecycle()
    val introCompleted by vm.introCompleted.collectAsStateWithLifecycle()

    var unauthRoute by remember { mutableStateOf(UnauthRoute.PUBLIC) }
    var userDestination by remember { mutableStateOf(UserDestination.DASHBOARD) }
    var earnInitialTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(feedback) {
        if (feedback != null) {
            delay(4200L)
            vm.clearFeedback()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val user = currentUser
        when {
            !introCompleted -> {
                IntroSplashScreen(
                    onFinished = { vm.completeIntro() }
                )
            }
            user == null -> {
                when (unauthRoute) {
                    UnauthRoute.PUBLIC -> PublicPortalScreen(
                        settings = settings,
                        advertisements = activeAds,
                        onNavigateLogin = { unauthRoute = UnauthRoute.LOGIN },
                        onNavigateRegister = { unauthRoute = UnauthRoute.REGISTER }
                    )
                    UnauthRoute.LOGIN -> LoginScreen(
                        settings = settings,
                        onLogin = { email, pass ->
                            vm.login(email, pass) { loggedIn ->
                                userDestination = UserDestination.DASHBOARD
                                unauthRoute = UnauthRoute.PUBLIC
                            }
                        },
                        onResetPassword = { email, nid, phone, newPass, onDone ->
                            vm.resetForgottenPassword(email, nid, phone, newPass, onDone)
                        },
                        onNavigateRegister = { unauthRoute = UnauthRoute.REGISTER },
                        onBackToPublic = { unauthRoute = UnauthRoute.PUBLIC }
                    )
                    UnauthRoute.REGISTER -> ThreeStepRegistrationScreen(
                        form = regForm,
                        settings = settings,
                        onUpdateForm = { vm.updateRegistrationForm(it) },
                        onStoreIdPhoto = { uri -> vm.storePrivateMedia(uri, "private_kyc", "nid_front") },
                        onSubmit = {
                            vm.submitRegistration {
                                unauthRoute = UnauthRoute.PUBLIC
                            }
                        },
                        onError = { vm.postErrorMsg(it) },
                        onBackToPublic = { unauthRoute = UnauthRoute.PUBLIC }
                    )
                }
            }

            !is2faVerified || user.mustChangePassword || user.accountStatus != "ACTIVE" -> {
                val userPayments by vm.userPayments.collectAsStateWithLifecycle()
                AccountGateScreen(
                    user = user,
                    settings = settings,
                    userPayments = userPayments,
                    pendingTwoFactorCode = pending2fa,
                    isTwoFactorVerified = is2faVerified,
                    onVerifyTwoFactor = { vm.verifyTwoFactorCode(it) },
                    onChangePassword = { vm.changePassword(it) },
                    onSubmitActivationPayment = { ref ->
                        vm.submitPaymentVerification("ACTIVATION", settings.activationFeeRwf, ref)
                    },
                    onLogout = {
                        vm.logout()
                        unauthRoute = UnauthRoute.PUBLIC
                    }
                )
            }

            user.role == "SUPER_ADMIN" -> {
                val allUsers by vm.allUsers.collectAsStateWithLifecycle()
                val allWallets by vm.allWallets.collectAsStateWithLifecycle()
                val allTransactions by vm.allTransactions.collectAsStateWithLifecycle()
                val allPayments by vm.allPaymentVerifications.collectAsStateWithLifecycle()
                val allKyc by vm.allKycApplications.collectAsStateWithLifecycle()
                val allWithdrawals by vm.allWithdrawals.collectAsStateWithLifecycle()
                val allLoans by vm.allLoans.collectAsStateWithLifecycle()
                val loanProducts by vm.loanProducts.collectAsStateWithLifecycle()
                val allVideos by vm.allVideosAdmin.collectAsStateWithLifecycle()
                val allVideoViews by vm.allVideoViewsAdmin.collectAsStateWithLifecycle()
                val allTasks by vm.allTasksAdmin.collectAsStateWithLifecycle()
                val allTaskSubmissions by vm.allTaskSubmissions.collectAsStateWithLifecycle()
                val allAds by vm.allAds.collectAsStateWithLifecycle()
                val allTickets by vm.allTicketsAdmin.collectAsStateWithLifecycle()
                val auditLogs by vm.auditLogs.collectAsStateWithLifecycle()

                AdminDashboardScreen(
                    admin = user,
                    settings = settings,
                    allUsers = allUsers,
                    allWallets = allWallets,
                    allTransactions = allTransactions,
                    allPayments = allPayments,
                    allKyc = allKyc,
                    allWithdrawals = allWithdrawals,
                    allLoans = allLoans,
                    loanProducts = loanProducts,
                    allVideos = allVideos,
                    allVideoViews = allVideoViews,
                    allTasks = allTasks,
                    allTaskSubmissions = allTaskSubmissions,
                    allAds = allAds,
                    allTickets = allTickets,
                    auditLogs = auditLogs,
                    onUpdateUserStatus = { id, st, note -> vm.adminUpdateUserStatus(id, st, note) },
                    onUnlockUser = { id -> vm.adminUnlockUser(id) },
                    onCorrectUserProfile = { id, f, s, nid, reason -> vm.adminCorrectUserProfile(id, f, s, nid, reason) },
                    onAdjustBalance = { id, delta, reason -> vm.adminAdjustBalance(id, delta, reason) },
                    onReviewPayment = { id, ok, note -> vm.adminReviewPayment(id, ok, note) },
                    onReviewKyc = { id, st, reason -> vm.adminReviewKyc(id, st, reason) },
                    onProcessWithdrawal = { id, st, note -> vm.adminProcessWithdrawal(id, st, note) },
                    onReviewLoan = { id, act, note -> vm.adminReviewLoan(id, act, note) },
                    onSaveLoanProduct = { product -> vm.adminSaveLoanProduct(product) },
                    onUpdateVideoStatus = { id, st -> vm.adminUpdateVideoStatus(id, st) },
                    onUpdateTaskStatus = { id, st -> vm.adminUpdateTaskStatus(id, st) },
                    onReviewTaskSubmission = { id, ok, reason -> vm.adminReviewTaskSubmission(id, ok, reason) },
                    onSaveAd = { ad -> vm.adminSaveAdvertisement(ad) },
                    onDeleteAd = { id -> vm.adminDeleteAdvertisement(id) },
                    onSaveSettings = { s -> vm.adminSaveSystemSettings(s) },
                    onReplyTicket = { id, rep, st, ag -> vm.adminReplySupportTicket(id, rep, st, ag) },
                    onLogout = {
                        vm.logout()
                        unauthRoute = UnauthRoute.PUBLIC
                    }
                )
            }

            else -> {
                val wallet = currentWallet ?: WalletEntity(userId = user.id)
                val transactions by vm.userTransactions.collectAsStateWithLifecycle()
                val savings by vm.userSavings.collectAsStateWithLifecycle()
                val withdrawals by vm.userWithdrawals.collectAsStateWithLifecycle()
                val loanProducts by vm.loanProducts.collectAsStateWithLifecycle()
                val loans by vm.userLoans.collectAsStateWithLifecycle()
                val repayments by vm.userLoanRepayments.collectAsStateWithLifecycle()
                val activeVideos by vm.activeVideos.collectAsStateWithLifecycle()
                val userVideoViews by vm.userVideoViews.collectAsStateWithLifecycle()
                val userVideoCampaigns by vm.userVideoCampaigns.collectAsStateWithLifecycle()
                val activeTasks by vm.activeTasks.collectAsStateWithLifecycle()
                val userTaskSubmissions by vm.userTaskSubmissions.collectAsStateWithLifecycle()
                val userCreatedTasks by vm.userCreatedTasks.collectAsStateWithLifecycle()
                val checkins by vm.userCheckins.collectAsStateWithLifecycle()
                val kyc by vm.userKyc.collectAsStateWithLifecycle()
                val payments by vm.userPayments.collectAsStateWithLifecycle()
                val notifications by vm.userNotifications.collectAsStateWithLifecycle()
                val tickets by vm.userTickets.collectAsStateWithLifecycle()

                val primaryNavItems = listOf(
                    UserDestination.DASHBOARD,
                    UserDestination.SAVINGS,
                    UserDestination.EARN,
                    UserDestination.CHECKIN,
                    UserDestination.SUPPORT
                )

                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val isWideScreen = maxWidth >= 600.dp

                    Row(modifier = Modifier.fillMaxSize()) {
                        if (isWideScreen) {
                            NavigationRail(
                                modifier = Modifier.fillMaxHeight().statusBarsPadding()
                            ) {
                                primaryNavItems.forEach { dest ->
                                    NavigationRailItem(
                                        selected = userDestination == dest,
                                        onClick = {
                                            earnInitialTab = 0
                                            userDestination = dest
                                        },
                                        icon = { Icon(dest.icon, contentDescription = dest.label) },
                                        label = { Text(dest.label, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                                    )
                                }
                            }
                        }

                        Scaffold(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            contentWindowInsets = WindowInsets(0, 0, 0, 0),
                            bottomBar = {
                                if (!isWideScreen) {
                                    NavigationBar(
                                        windowInsets = WindowInsets.navigationBars
                                    ) {
                                        primaryNavItems.forEach { dest ->
                                            NavigationBarItem(
                                                selected = userDestination == dest,
                                                onClick = {
                                                    earnInitialTab = 0
                                                    userDestination = dest
                                                },
                                                icon = { Icon(dest.icon, contentDescription = dest.label) },
                                                label = { Text(dest.label, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                                            )
                                        }
                                    }
                                }
                            }
                        ) { innerPadding ->
                            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                                when (userDestination) {
                                    UserDestination.DASHBOARD -> UserDashboardScreen(
                                        user = user,
                                        wallet = wallet,
                                        transactions = transactions,
                                        savings = savings,
                                        loans = loans,
                                        checkins = checkins,
                                        notifications = notifications,
                                        advertisements = activeAds,
                                        onNavigate = {
                                            earnInitialTab = 0
                                            userDestination = it
                                        },
                                        onOpenUploadVideo = {
                                            earnInitialTab = 1
                                            userDestination = UserDestination.EARN
                                        },
                                        onOpenCreateTask = {
                                            earnInitialTab = 3
                                            userDestination = UserDestination.EARN
                                        },
                                        onUpdateProfilePhoto = { uri ->
                                            vm.storePrivateMedia(uri, "profile_photos", "avatar")?.let { path ->
                                                vm.updateProfilePhoto(path)
                                            }
                                        },
                                        onChangePassword = { newPass -> vm.changePassword(newPass) },
                                        onLogout = {
                                            vm.logout()
                                            unauthRoute = UnauthRoute.PUBLIC
                                        }
                                    )

                                    UserDestination.SAVINGS -> SavingsAndDepositsScreen(
                                        user = user,
                                        wallet = wallet,
                                        settings = settings,
                                        savingsAccounts = savings,
                                        payments = payments,
                                        onSubmitDeposit = { amt, ref -> vm.submitPaymentVerification("DEPOSIT", amt, ref) },
                                        onCreateSavings = { type, purpose, desc, target, dep ->
                                            vm.createSavingsAccount(type, purpose, desc, target, dep)
                                        },
                                        onTopUpSavings = { id, amt -> vm.topUpSavings(id, amt) },
                                        onWithdrawSavings = { id, amt -> vm.withdrawSavings(id, amt) },
                                        onBack = { userDestination = UserDestination.DASHBOARD }
                                    )

                                    UserDestination.WITHDRAW -> WithdrawalScreen(
                                        user = user,
                                        wallet = wallet,
                                        settings = settings,
                                        withdrawals = withdrawals,
                                        onRequestWithdrawal = { prov, phone, amt -> vm.requestWithdrawal(prov, phone, amt) },
                                        onBack = { userDestination = UserDestination.DASHBOARD }
                                    )

                                    UserDestination.LOANS -> LoansScreen(
                                        user = user,
                                        wallet = wallet,
                                        loanProducts = loanProducts,
                                        loans = loans,
                                        repayments = repayments,
                                        onApplyLoan = { pid, amt, m, purp, info -> vm.applyForLoan(pid, amt, m, purp, info) },
                                        onRepayLoan = { lid, amt -> vm.repayLoan(lid, amt) },
                                        onBack = { userDestination = UserDestination.DASHBOARD }
                                    )

                                    UserDestination.EARN -> EarnAndCreatorScreen(
                                        initialTab = earnInitialTab,
                                        user = user,
                                        settings = settings,
                                        activeVideos = activeVideos,
                                        userVideoViews = userVideoViews,
                                        userVideoCampaigns = userVideoCampaigns,
                                        activeTasks = activeTasks,
                                        userTaskSubmissions = userTaskSubmissions,
                                        userCreatedTasks = userCreatedTasks,
                                        onStoreMedia = { uri, folder -> vm.storePrivateMedia(uri, folder, "media") },
                                        onCompleteVideo = { vid, sec -> vm.completeVideoView(vid, sec) },
                                        onCreateVideoCampaign = { t, d, c, v, th, dur, r, tg, days, cb ->
                                            vm.createVideoCampaign(t, d, c, v, th, dur, r, tg, days, cb)
                                        },
                                        onSubmitCampaignPayment = { type, amt, ref, id ->
                                            vm.submitPaymentVerification(type, amt, ref, id)
                                        },
                                        onSubmitTaskProof = { tid, txt, img -> vm.submitTaskProof(tid, txt, img) },
                                        onCreatePaidTask = { t, d, c, inst, req, pt, part, r, days, cb ->
                                            vm.createPaidTask(t, d, c, inst, req, pt, part, r, days, cb)
                                        },
                                        onBack = { userDestination = UserDestination.DASHBOARD }
                                    )

                                    UserDestination.CHECKIN -> DailyCheckinScreen(
                                        wallet = wallet,
                                        settings = settings,
                                        checkins = checkins,
                                        onClaimCheckin = { vm.claimDailyCheckin() },
                                        onBack = { userDestination = UserDestination.DASHBOARD }
                                    )

                                    UserDestination.KYC -> KycVerificationScreen(
                                        user = user,
                                        existingKyc = kyc,
                                        onStoreMedia = { uri, pref -> vm.storePrivateMedia(uri, "private_kyc", pref) },
                                        onSubmitKyc = { fn, nid, dob, gen, ph, em, pr, dist, sec, cel, vil, fr, bk, sf, nt ->
                                            vm.submitKyc(fn, nid, dob, gen, ph, em, pr, dist, sec, cel, vil, fr, bk, sf, nt)
                                        },
                                        onBack = { userDestination = UserDestination.DASHBOARD }
                                    )

                                    UserDestination.HISTORY -> TransactionHistoryScreen(
                                        user = user,
                                        transactions = transactions,
                                        onFeedback = { vm.postInfoMsg(it) },
                                        onBack = { userDestination = UserDestination.DASHBOARD }
                                    )

                                    UserDestination.NOTIFICATIONS -> NotificationsScreen(
                                        notifications = notifications,
                                        onMarkAllRead = { vm.markNotificationsRead() },
                                        onBack = { userDestination = UserDestination.DASHBOARD }
                                    )

                                    UserDestination.SUPPORT -> SupportCenterScreen(
                                        tickets = tickets,
                                        onStoreScreenshot = { uri -> vm.storePrivateMedia(uri, "support_uploads", "tkt") },
                                        onCreateTicket = { cat, sub, desc, img -> vm.createSupportTicket(cat, sub, desc, img) },
                                        onBack = { userDestination = UserDestination.DASHBOARD }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Floating Feedback Toast Banner
        AnimatedVisibility(
            visible = feedback != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 72.dp)
        ) {
            feedback?.let { fb ->
                FeedbackBannerCard(feedback = fb, onDismiss = { vm.clearFeedback() })
            }
        }
    }
}

@Composable
private fun FeedbackBannerCard(feedback: FeedbackBanner, onDismiss: () -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = if (feedback.isError) Crimson600 else Emerald600,
        shadowElevation = 6.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                feedback.message,
                style = MaterialTheme.typography.bodyMedium,
                color = PureWhite,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Filled.Close, contentDescription = "Dismiss", tint = PureWhite)
            }
        }
    }
}
