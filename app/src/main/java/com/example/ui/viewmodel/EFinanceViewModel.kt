package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.*
import com.example.data.repository.EFinanceRepository
import com.example.data.security.SecurityUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

private val Context.sessionDataStore by preferencesDataStore(name = "efinance_session")

data class RegistrationFormState(
    val step: Int = 1,
    val firstName: String = "",
    val secondName: String = "",
    val email: String = "",
    val phone: String = "",
    val gender: String = "Male",
    val dateOfBirth: String = "",
    val nationalIdNumber: String = "",
    val nationalIdFrontUri: String = "",
    val educationCategory: String = "UNIVERSITY",
    val institutionName: String = "University of Rwanda (UR)",
    val academicLevelOrClass: String = "Year 1 (Level 1)",
    val facultyOrCombination: String = "",
    val programOfStudy: String = "",
    val birthCountry: String = "Rwanda",
    val birthProvince: String = "Kigali City",
    val birthDistrict: String = "Gasabo",
    val birthSector: String = "",
    val birthCell: String = "",
    val birthVillage: String = "",
    val resCountry: String = "Rwanda",
    val resProvince: String = "Kigali City",
    val resDistrict: String = "Gasabo",
    val resSector: String = "",
    val resCell: String = "",
    val resVillage: String = "",
    val password: String = "",
    val confirmPassword: String = ""
)

data class FeedbackBanner(val message: String, val isError: Boolean = false)

@OptIn(ExperimentalCoroutinesApi::class)
class EFinanceViewModel(application: Application) : AndroidViewModel(application) {
    private val db = EFinanceDatabase.getInstance(application)
    private val repo = EFinanceRepository(db)
    private val appContext = application.applicationContext
    private val activeUserKey = stringPreferencesKey("active_user_id")

    private val _activeUserId = MutableStateFlow<String?>(null)
    val activeUserId: StateFlow<String?> = _activeUserId.asStateFlow()

    private val _pendingTwoFactorChallenge = MutableStateFlow<String?>(null)
    val pendingTwoFactorChallenge: StateFlow<String?> = _pendingTwoFactorChallenge.asStateFlow()

    private val _isTwoFactorVerified = MutableStateFlow(true)
    val isTwoFactorVerified: StateFlow<Boolean> = _isTwoFactorVerified.asStateFlow()

    private val _introCompleted = MutableStateFlow(false)
    val introCompleted: StateFlow<Boolean> = _introCompleted.asStateFlow()

    fun completeIntro() {
        _introCompleted.value = true
    }

    private val _feedback = MutableStateFlow<FeedbackBanner?>(null)
    val feedback: StateFlow<FeedbackBanner?> = _feedback.asStateFlow()

    private val _registrationForm = MutableStateFlow(RegistrationFormState())
    val registrationForm: StateFlow<RegistrationFormState> = _registrationForm.asStateFlow()

    val systemSettings: StateFlow<SystemSettingsEntity> = repo.observeSystemSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SystemSettingsEntity())

    val activeAds: StateFlow<List<AdvertisementEntity>> = repo.observeActiveAdvertisements()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAds: StateFlow<List<AdvertisementEntity>> = repo.observeAllAdvertisements()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val loanProducts: StateFlow<List<LoanProductEntity>> = repo.observeLoanProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeVideos: StateFlow<List<VideoCampaignEntity>> = repo.observeActiveVideos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeTasks: StateFlow<List<PaidTaskEntity>> = repo.observeActiveTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentUser: StateFlow<UserEntity?> = _activeUserId.flatMapLatest { uid ->
        if (uid.isNullOrBlank()) flowOf(null) else repo.observeUser(uid)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentWallet: StateFlow<WalletEntity?> = _activeUserId.flatMapLatest { uid ->
        if (uid.isNullOrBlank()) flowOf(null) else repo.observeWallet(uid)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val userTransactions: StateFlow<List<FinancialTransactionEntity>> = _activeUserId.flatMapLatest { uid ->
        if (uid.isNullOrBlank()) flowOf(emptyList()) else repo.observeTransactions(uid)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userSavings: StateFlow<List<SavingsAccountEntity>> = _activeUserId.flatMapLatest { uid ->
        if (uid.isNullOrBlank()) flowOf(emptyList()) else repo.observeSavings(uid)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userWithdrawals: StateFlow<List<WithdrawalRequestEntity>> = _activeUserId.flatMapLatest { uid ->
        if (uid.isNullOrBlank()) flowOf(emptyList()) else repo.observeWithdrawals(uid)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userLoans: StateFlow<List<LoanApplicationEntity>> = _activeUserId.flatMapLatest { uid ->
        if (uid.isNullOrBlank()) flowOf(emptyList()) else repo.observeLoans(uid)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userLoanRepayments: StateFlow<List<LoanRepaymentEntity>> = _activeUserId.flatMapLatest { uid ->
        if (uid.isNullOrBlank()) flowOf(emptyList()) else repo.observeLoanRepayments(uid)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userVideoCampaigns: StateFlow<List<VideoCampaignEntity>> = _activeUserId.flatMapLatest { uid ->
        if (uid.isNullOrBlank()) flowOf(emptyList()) else repo.observeUserVideoCampaigns(uid)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userVideoViews: StateFlow<List<VideoViewEntity>> = _activeUserId.flatMapLatest { uid ->
        if (uid.isNullOrBlank()) flowOf(emptyList()) else repo.observeUserVideoViews(uid)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userCreatedTasks: StateFlow<List<PaidTaskEntity>> = _activeUserId.flatMapLatest { uid ->
        if (uid.isNullOrBlank()) flowOf(emptyList()) else repo.observeUserCreatedTasks(uid)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userTaskSubmissions: StateFlow<List<TaskSubmissionEntity>> = _activeUserId.flatMapLatest { uid ->
        if (uid.isNullOrBlank()) flowOf(emptyList()) else repo.observeUserTaskSubmissions(uid)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userCheckins: StateFlow<List<DailyCheckinEntity>> = _activeUserId.flatMapLatest { uid ->
        if (uid.isNullOrBlank()) flowOf(emptyList()) else repo.observeCheckins(uid)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userKyc: StateFlow<KycApplicationEntity?> = _activeUserId.flatMapLatest { uid ->
        if (uid.isNullOrBlank()) flowOf(null) else repo.observeKyc(uid)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val userPayments: StateFlow<List<PaymentVerificationEntity>> = _activeUserId.flatMapLatest { uid ->
        if (uid.isNullOrBlank()) flowOf(emptyList()) else repo.observePayments(uid)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userNotifications: StateFlow<List<NotificationEntity>> = _activeUserId.flatMapLatest { uid ->
        if (uid.isNullOrBlank()) flowOf(emptyList()) else repo.observeNotifications(uid)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userTickets: StateFlow<List<SupportTicketEntity>> = _activeUserId.flatMapLatest { uid ->
        if (uid.isNullOrBlank()) flowOf(emptyList()) else repo.observeTickets(uid)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Admin Streams (active only when currentUser is SUPER_ADMIN)
    private val isAdminFlow = currentUser.map { it?.role == "SUPER_ADMIN" }.distinctUntilChanged()

    val allUsers: StateFlow<List<UserEntity>> = isAdminFlow.flatMapLatest { isAdmin ->
        if (isAdmin) repo.observeAllUsers() else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWallets: StateFlow<List<WalletEntity>> = isAdminFlow.flatMapLatest { isAdmin ->
        if (isAdmin) repo.observeAllWallets() else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<FinancialTransactionEntity>> = isAdminFlow.flatMapLatest { isAdmin ->
        if (isAdmin) repo.observeAllTransactions() else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPaymentVerifications: StateFlow<List<PaymentVerificationEntity>> = isAdminFlow.flatMapLatest { isAdmin ->
        if (isAdmin) repo.observeAllPaymentVerifications() else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allKycApplications: StateFlow<List<KycApplicationEntity>> = isAdminFlow.flatMapLatest { isAdmin ->
        if (isAdmin) repo.observeAllKyc() else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWithdrawals: StateFlow<List<WithdrawalRequestEntity>> = isAdminFlow.flatMapLatest { isAdmin ->
        if (isAdmin) repo.observeAllWithdrawals() else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLoans: StateFlow<List<LoanApplicationEntity>> = isAdminFlow.flatMapLatest { isAdmin ->
        if (isAdmin) repo.observeAllLoans() else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allVideosAdmin: StateFlow<List<VideoCampaignEntity>> = isAdminFlow.flatMapLatest { isAdmin ->
        if (isAdmin) repo.observeAllVideos() else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTasksAdmin: StateFlow<List<PaidTaskEntity>> = isAdminFlow.flatMapLatest { isAdmin ->
        if (isAdmin) repo.observeAllTasks() else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTaskSubmissions: StateFlow<List<TaskSubmissionEntity>> = isAdminFlow.flatMapLatest { isAdmin ->
        if (isAdmin) repo.observeAllTaskSubmissions() else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allVideoViewsAdmin: StateFlow<List<VideoViewEntity>> = isAdminFlow.flatMapLatest { isAdmin ->
        if (isAdmin) repo.observeAllVideoViews() else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTicketsAdmin: StateFlow<List<SupportTicketEntity>> = isAdminFlow.flatMapLatest { isAdmin ->
        if (isAdmin) repo.observeAllTickets() else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditLogs: StateFlow<List<AuditLogEntity>> = isAdminFlow.flatMapLatest { isAdmin ->
        if (isAdmin) repo.observeAuditLogs() else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            appContext.sessionDataStore.data.firstOrNull()?.get(activeUserKey)?.let { savedId ->
                if (savedId.isNotBlank()) _activeUserId.value = savedId
            }
        }
    }

    fun clearFeedback() { _feedback.value = null }

    private fun postSuccess(msg: String) { _feedback.value = FeedbackBanner(msg, isError = false) }
    private fun postError(err: Throwable) { _feedback.value = FeedbackBanner(err.message ?: "Operation failed.", isError = true) }
    fun postInfoMsg(msg: String) { _feedback.value = FeedbackBanner(msg, isError = false) }
    fun postErrorMsg(msg: String) { _feedback.value = FeedbackBanner(msg, isError = true) }

    fun updateRegistrationForm(transform: (RegistrationFormState) -> RegistrationFormState) {
        _registrationForm.update(transform)
    }

    fun storePrivateMedia(uri: Uri, folder: String, prefix: String): String? {
        return SecurityUtils.copyUriToPrivateStorage(appContext, uri, folder, prefix)
    }

    fun submitRegistration(onRegistered: () -> Unit) {
        val f = _registrationForm.value
        if (f.password != f.confirmPassword) {
            postErrorMsg("Passwords do not match.")
            return
        }
        viewModelScope.launch {
            repo.registerUser(
                firstName = f.firstName,
                secondName = f.secondName,
                email = f.email,
                phone = f.phone,
                gender = f.gender,
                dateOfBirth = f.dateOfBirth,
                nationalIdNumber = f.nationalIdNumber,
                nationalIdFrontUri = f.nationalIdFrontUri,
                educationCategory = f.educationCategory,
                institutionName = f.institutionName,
                academicLevelOrClass = f.academicLevelOrClass,
                facultyOrCombination = f.facultyOrCombination,
                programOfStudy = f.programOfStudy,
                birthCountry = f.birthCountry,
                birthProvince = f.birthProvince,
                birthDistrict = f.birthDistrict,
                birthSector = f.birthSector,
                birthCell = f.birthCell,
                birthVillage = f.birthVillage,
                resCountry = f.resCountry,
                resProvince = f.resProvince,
                resDistrict = f.resDistrict,
                resSector = f.resSector,
                resCell = f.resCell,
                resVillage = f.resVillage,
                password = f.password
            ).onSuccess { user ->
                persistSession(user.id)
                _registrationForm.value = RegistrationFormState()
                postSuccess("Registration submitted! Awaiting administrator approval.")
                onRegistered()
            }.onFailure(::postError)
        }
    }

    fun login(email: String, password: String, onSuccess: (UserEntity) -> Unit) {
        viewModelScope.launch {
            repo.login(email, password).onSuccess { user ->
                if (user.twoFactorEnabled && user.role == "SUPER_ADMIN") {
                    _pendingTwoFactorChallenge.value = SecurityUtils.generateTwoFactorCode()
                    _isTwoFactorVerified.value = false
                } else {
                    _pendingTwoFactorChallenge.value = null
                    _isTwoFactorVerified.value = true
                }
                persistSession(user.id)
                onSuccess(user)
            }.onFailure(::postError)
        }
    }

    fun verifyTwoFactorCode(code: String) {
        val expected = _pendingTwoFactorChallenge.value
        if (expected != null && code.trim() == expected) {
            _isTwoFactorVerified.value = true
            _pendingTwoFactorChallenge.value = null
            postSuccess("2FA verified successfully.")
        } else {
            postErrorMsg("Invalid 2FA verification code.")
        }
    }

    fun changePassword(newPassword: String) {
        val uid = _activeUserId.value ?: return
        viewModelScope.launch {
            repo.changePassword(uid, newPassword)
                .onSuccess { postSuccess("Password updated successfully.") }
                .onFailure(::postError)
        }
    }

    fun resetForgottenPassword(email: String, nid: String, phone: String, newPassword: String, onDone: () -> Unit) {
        viewModelScope.launch {
            repo.resetForgottenPassword(email, nid, phone, newPassword)
                .onSuccess {
                    postSuccess("Password reset successful. You may now sign in.")
                    onDone()
                }
                .onFailure(::postError)
        }
    }

    fun logout() {
        viewModelScope.launch {
            appContext.sessionDataStore.edit { it.remove(activeUserKey) }
            _activeUserId.value = null
            _pendingTwoFactorChallenge.value = null
            _isTwoFactorVerified.value = true
        }
    }

    private suspend fun persistSession(userId: String) {
        appContext.sessionDataStore.edit { it[activeUserKey] = userId }
        _activeUserId.value = userId
    }

    // --- User Financial & Platform Actions ---
    fun submitPaymentVerification(paymentType: String, amountRwf: Long, reference: String, relatedId: String = "") {
        val uid = _activeUserId.value ?: return
        viewModelScope.launch {
            repo.submitPaymentVerification(uid, paymentType, amountRwf, reference, relatedId)
                .onSuccess { postSuccess("Payment reference submitted for administrator verification.") }
                .onFailure(::postError)
        }
    }

    fun submitKyc(
        fullLegalName: String,
        nid: String,
        dob: String,
        gender: String,
        phone: String,
        email: String,
        province: String,
        district: String,
        sector: String,
        cell: String,
        village: String,
        idFrontUri: String,
        idBackUri: String,
        selfieUri: String,
        notes: String
    ) {
        val uid = _activeUserId.value ?: return
        viewModelScope.launch {
            repo.submitKyc(
                uid, fullLegalName, nid, dob, gender, phone, email,
                province, district, sector, cell, village,
                idFrontUri, idBackUri, selfieUri, notes
            ).onSuccess { postSuccess("KYC verification submitted for compliance review.") }
                .onFailure(::postError)
        }
    }

    fun createSavingsAccount(planType: String, purpose: String, description: String, targetAmount: Long, initialDeposit: Long) {
        val uid = _activeUserId.value ?: return
        viewModelScope.launch {
            repo.createOrDepositSavings(uid, planType, purpose, description, targetAmount, initialDeposit)
                .onSuccess { postSuccess("Saved ${SecurityUtils.formatRwf(initialDeposit)} into $planType vault.") }
                .onFailure(::postError)
        }
    }

    fun topUpSavings(savingsId: String, amount: Long) {
        val uid = _activeUserId.value ?: return
        viewModelScope.launch {
            repo.topUpSavingsAccount(uid, savingsId, amount)
                .onSuccess { postSuccess("Added ${SecurityUtils.formatRwf(amount)} to savings.") }
                .onFailure(::postError)
        }
    }

    fun withdrawSavings(savingsId: String, amount: Long) {
        val uid = _activeUserId.value ?: return
        viewModelScope.launch {
            repo.withdrawFromSavings(uid, savingsId, amount)
                .onSuccess { postSuccess("Transferred ${SecurityUtils.formatRwf(amount)} to Available Balance.") }
                .onFailure(::postError)
        }
    }

    fun requestWithdrawal(provider: String, phone: String, amount: Long) {
        val uid = _activeUserId.value ?: return
        viewModelScope.launch {
            repo.requestWithdrawal(uid, provider, phone, amount)
                .onSuccess { req ->
                    postSuccess("Withdrawal ${req.reference} submitted! Net payout: ${SecurityUtils.formatRwf(req.netAmountRwf)}.")
                }
                .onFailure(::postError)
        }
    }

    fun applyForLoan(productId: String, amount: Long, months: Int, purpose: String, supportingInfo: String) {
        val uid = _activeUserId.value ?: return
        viewModelScope.launch {
            repo.applyForLoan(uid, productId, amount, months, purpose, supportingInfo)
                .onSuccess { postSuccess("Loan application submitted for eligibility review.") }
                .onFailure(::postError)
        }
    }

    fun repayLoan(loanId: String, amount: Long) {
        val uid = _activeUserId.value ?: return
        viewModelScope.launch {
            repo.repayLoan(uid, loanId, amount)
                .onSuccess { postSuccess("Loan repayment recorded.") }
                .onFailure(::postError)
        }
    }

    fun completeVideoView(videoId: String, watchedSeconds: Int) {
        val uid = _activeUserId.value ?: return
        viewModelScope.launch {
            repo.completeVideoView(uid, videoId, watchedSeconds)
                .onSuccess { reward -> postSuccess("Video reward credited: +${SecurityUtils.formatRwf(reward)}!") }
                .onFailure(::postError)
        }
    }

    fun createVideoCampaign(
        title: String,
        desc: String,
        category: String,
        videoUri: String,
        thumbUri: String,
        durationSeconds: Int,
        rewardPerView: Long,
        targetViews: Int,
        durationDays: Int,
        onCreated: (VideoCampaignEntity) -> Unit
    ) {
        val uid = _activeUserId.value ?: return
        viewModelScope.launch {
            repo.createVideoCampaign(uid, title, desc, category, videoUri, thumbUri, durationSeconds, rewardPerView, targetViews, durationDays)
                .onSuccess { c ->
                    postSuccess("Campaign created! Pay ${SecurityUtils.formatRwf(c.totalBudgetRwf)} via 182811880554*${c.totalBudgetRwf}#.")
                    onCreated(c)
                }
                .onFailure(::postError)
        }
    }

    fun createPaidTask(
        title: String,
        desc: String,
        category: String,
        instructions: String,
        requirements: String,
        proofType: String,
        targetParticipants: Int,
        rewardPerParticipant: Long,
        durationDays: Int,
        onCreated: (PaidTaskEntity) -> Unit
    ) {
        val uid = _activeUserId.value ?: return
        viewModelScope.launch {
            repo.createPaidTask(uid, title, desc, category, instructions, requirements, proofType, targetParticipants, rewardPerParticipant, durationDays)
                .onSuccess { t ->
                    postSuccess("Task campaign created! Submit payment reference for ${SecurityUtils.formatRwf(t.totalBudgetRwf)}.")
                    onCreated(t)
                }
                .onFailure(::postError)
        }
    }

    fun submitTaskProof(taskId: String, proofText: String, proofImageUri: String) {
        val uid = _activeUserId.value ?: return
        viewModelScope.launch {
            repo.submitTaskCompletion(uid, taskId, proofText, proofImageUri)
                .onSuccess { postSuccess("Proof submitted! Status: PENDING REVIEW by Administration.") }
                .onFailure(::postError)
        }
    }

    fun claimDailyCheckin() {
        val uid = _activeUserId.value ?: return
        viewModelScope.launch {
            repo.claimDailyCheckin(uid)
                .onSuccess { c -> postSuccess("Daily Check-In claimed: +${SecurityUtils.formatRwf(c.rewardRwf)} (Day ${c.streakCount} streak)!") }
                .onFailure(::postError)
        }
    }

    fun markNotificationsRead() {
        val uid = _activeUserId.value ?: return
        viewModelScope.launch { repo.markNotificationsRead(uid) }
    }

    fun createSupportTicket(category: String, subject: String, description: String, screenshotUri: String) {
        val uid = _activeUserId.value ?: return
        viewModelScope.launch {
            repo.createSupportTicket(uid, category, subject, description, screenshotUri)
                .onSuccess { postSuccess("Support ticket created. Our team will respond shortly.") }
                .onFailure(::postError)
        }
    }

    // --- Admin Actions ---
    fun adminUpdateUserStatus(targetUserId: String, status: String, note: String) {
        val adminId = _activeUserId.value ?: return
        viewModelScope.launch {
            repo.adminUpdateUserStatus(adminId, targetUserId, status, note)
                .onSuccess { postSuccess("User status updated to $status.") }
                .onFailure(::postError)
        }
    }

    fun adminUnlockUser(targetUserId: String) {
        val adminId = _activeUserId.value ?: return
        viewModelScope.launch {
            repo.adminUnlockUserLogin(adminId, targetUserId)
                .onSuccess { postSuccess("Account unlocked and reactivated.") }
                .onFailure(::postError)
        }
    }

    fun adminCorrectUserProfile(targetUserId: String, first: String, second: String, nid: String, reason: String) {
        val adminId = _activeUserId.value ?: return
        viewModelScope.launch {
            repo.adminCorrectUserProfile(adminId, targetUserId, first, second, nid, reason)
                .onSuccess { postSuccess("User profile corrected with audit entry.") }
                .onFailure(::postError)
        }
    }

    fun adminReviewPayment(paymentId: String, approve: Boolean, note: String) {
        val adminId = _activeUserId.value ?: return
        viewModelScope.launch {
            repo.adminReviewPaymentVerification(adminId, paymentId, approve, note)
                .onSuccess { postSuccess(if (approve) "Payment verified and credited." else "Payment rejected.") }
                .onFailure(::postError)
        }
    }

    fun adminReviewKyc(kycId: String, status: String, reason: String) {
        val adminId = _activeUserId.value ?: return
        viewModelScope.launch {
            repo.adminReviewKyc(adminId, kycId, status, reason)
                .onSuccess { postSuccess("KYC application updated: $status.") }
                .onFailure(::postError)
        }
    }

    fun adminProcessWithdrawal(withdrawalId: String, status: String, note: String) {
        val adminId = _activeUserId.value ?: return
        viewModelScope.launch {
            repo.adminProcessWithdrawal(adminId, withdrawalId, status, note)
                .onSuccess { postSuccess("Withdrawal updated: $status.") }
                .onFailure(::postError)
        }
    }

    fun adminReviewLoan(loanId: String, action: String, note: String) {
        val adminId = _activeUserId.value ?: return
        viewModelScope.launch {
            repo.adminReviewLoan(adminId, loanId, action, note)
                .onSuccess { postSuccess("Loan application updated: $action.") }
                .onFailure(::postError)
        }
    }

    fun adminUpdateVideoStatus(videoId: String, status: String) {
        val adminId = _activeUserId.value ?: return
        viewModelScope.launch {
            repo.adminUpdateVideoStatus(adminId, videoId, status)
                .onSuccess { postSuccess("Video campaign set to $status.") }
                .onFailure(::postError)
        }
    }

    fun adminUpdateTaskStatus(taskId: String, status: String) {
        val adminId = _activeUserId.value ?: return
        viewModelScope.launch {
            repo.adminUpdateTaskStatus(adminId, taskId, status)
                .onSuccess { postSuccess("Task set to $status.") }
                .onFailure(::postError)
        }
    }

    fun adminReviewTaskSubmission(submissionId: String, approve: Boolean, reason: String) {
        val adminId = _activeUserId.value ?: return
        viewModelScope.launch {
            repo.adminReviewTaskSubmission(adminId, submissionId, approve, reason)
                .onSuccess { postSuccess(if (approve) "Submission approved & reward credited." else "Submission rejected.") }
                .onFailure(::postError)
        }
    }

    fun adminAdjustBalance(targetUserId: String, deltaRwf: Long, reason: String) {
        val adminId = _activeUserId.value ?: return
        viewModelScope.launch {
            repo.adminAdjustUserBalance(adminId, targetUserId, deltaRwf, reason)
                .onSuccess { postSuccess("Manual wallet adjustment logged.") }
                .onFailure(::postError)
        }
    }

    fun adminSaveAdvertisement(ad: AdvertisementEntity) {
        val adminId = _activeUserId.value ?: return
        viewModelScope.launch {
            repo.adminSaveAdvertisement(adminId, ad)
                .onSuccess { postSuccess("Advertisement saved.") }
                .onFailure(::postError)
        }
    }

    fun adminDeleteAdvertisement(adId: String) {
        val adminId = _activeUserId.value ?: return
        viewModelScope.launch {
            repo.adminDeleteAdvertisement(adminId, adId)
                .onSuccess { postSuccess("Advertisement deleted.") }
                .onFailure(::postError)
        }
    }

    fun adminSaveSystemSettings(settings: SystemSettingsEntity) {
        val adminId = _activeUserId.value ?: return
        viewModelScope.launch {
            repo.adminSaveSystemSettings(adminId, settings)
                .onSuccess { postSuccess("System settings updated.") }
                .onFailure(::postError)
        }
    }

    fun adminReplySupportTicket(ticketId: String, reply: String, status: String, agent: String) {
        val adminId = _activeUserId.value ?: return
        viewModelScope.launch {
            repo.adminReplySupportTicket(adminId, ticketId, reply, status, agent)
                .onSuccess { postSuccess("Support ticket updated.") }
                .onFailure(::postError)
        }
    }

    fun updateProfilePhoto(photoPath: String) {
        val uid = _activeUserId.value ?: return
        viewModelScope.launch {
            repo.updateProfilePhoto(uid, photoPath)
                .onSuccess { postSuccess("Profile photo updated.") }
                .onFailure(::postError)
        }
    }

    fun adminSaveLoanProduct(product: LoanProductEntity) {
        val adminId = _activeUserId.value ?: return
        viewModelScope.launch {
            repo.adminSaveLoanProduct(adminId, product)
                .onSuccess { postSuccess("Loan product saved.") }
                .onFailure(::postError)
        }
    }
}
