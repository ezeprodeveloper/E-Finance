package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.local.*
import com.example.data.security.SecurityUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class EFinanceRepository(private val db: EFinanceDatabase) {
    private val dao = db.dao()

    // Reactive Streams
    fun observeSystemSettings(): Flow<SystemSettingsEntity> =
        dao.observeSystemSettings().map { it ?: SystemSettingsEntity() }

    fun observeActiveAdvertisements(): Flow<List<AdvertisementEntity>> = dao.observeActiveAdvertisements()
    fun observeAllAdvertisements(): Flow<List<AdvertisementEntity>> = dao.observeAllAdvertisements()
    fun observeLoanProducts(): Flow<List<LoanProductEntity>> = dao.observeLoanProducts()
    fun observeActiveVideos(): Flow<List<VideoCampaignEntity>> = dao.observeActiveVideos()
    fun observeAllVideos(): Flow<List<VideoCampaignEntity>> = dao.observeAllVideos()
    fun observeActiveTasks(): Flow<List<PaidTaskEntity>> = dao.observeActiveTasks()
    fun observeAllTasks(): Flow<List<PaidTaskEntity>> = dao.observeAllTasks()

    fun observeUser(userId: String): Flow<UserEntity?> = dao.observeUserById(userId)
    fun observeWallet(userId: String): Flow<WalletEntity?> = dao.observeWallet(userId)
    fun observeTransactions(userId: String): Flow<List<FinancialTransactionEntity>> = dao.observeTransactionsForUser(userId)
    fun observeSavings(userId: String): Flow<List<SavingsAccountEntity>> = dao.observeSavingsForUser(userId)
    fun observeWithdrawals(userId: String): Flow<List<WithdrawalRequestEntity>> = dao.observeWithdrawalsForUser(userId)
    fun observeLoans(userId: String): Flow<List<LoanApplicationEntity>> = dao.observeLoansForUser(userId)
    fun observeLoanRepayments(userId: String): Flow<List<LoanRepaymentEntity>> = dao.observeLoanRepaymentsForUser(userId)
    fun observeUserVideoCampaigns(userId: String): Flow<List<VideoCampaignEntity>> = dao.observeUserVideoCampaigns(userId)
    fun observeUserVideoViews(userId: String): Flow<List<VideoViewEntity>> = dao.observeUserVideoViews(userId)
    fun observeUserCreatedTasks(userId: String): Flow<List<PaidTaskEntity>> = dao.observeUserCreatedTasks(userId)
    fun observeUserTaskSubmissions(userId: String): Flow<List<TaskSubmissionEntity>> = dao.observeUserTaskSubmissions(userId)
    fun observeCheckins(userId: String): Flow<List<DailyCheckinEntity>> = dao.observeCheckinsForUser(userId)
    fun observeKyc(userId: String): Flow<KycApplicationEntity?> = dao.observeKycForUser(userId)
    fun observePayments(userId: String): Flow<List<PaymentVerificationEntity>> = dao.observePaymentsForUser(userId)
    fun observeNotifications(userId: String): Flow<List<NotificationEntity>> = dao.observeNotificationsForUser(userId)
    fun observeTickets(userId: String): Flow<List<SupportTicketEntity>> = dao.observeTicketsForUser(userId)

    // Admin Streams
    fun observeAllUsers(): Flow<List<UserEntity>> = dao.observeAllUsers()
    fun observeAllWallets(): Flow<List<WalletEntity>> = dao.observeAllWallets()
    fun observeAllTransactions(): Flow<List<FinancialTransactionEntity>> = dao.observeAllTransactions()
    fun observeAllPaymentVerifications(): Flow<List<PaymentVerificationEntity>> = dao.observeAllPaymentVerifications()
    fun observeAllKyc(): Flow<List<KycApplicationEntity>> = dao.observeAllKyc()
    fun observeAllWithdrawals(): Flow<List<WithdrawalRequestEntity>> = dao.observeAllWithdrawals()
    fun observeAllLoans(): Flow<List<LoanApplicationEntity>> = dao.observeAllLoans()
    fun observeAllTaskSubmissions(): Flow<List<TaskSubmissionEntity>> = dao.observeAllTaskSubmissions()
    fun observeAllVideoViews(): Flow<List<VideoViewEntity>> = dao.observeAllVideoViews()
    fun observeAllTickets(): Flow<List<SupportTicketEntity>> = dao.observeAllTickets()
    fun observeAuditLogs(): Flow<List<AuditLogEntity>> = dao.observeAuditLogs()

    // --- Registration ---
    suspend fun registerUser(
        firstName: String,
        secondName: String,
        email: String,
        phone: String,
        gender: String,
        dateOfBirth: String,
        nationalIdNumber: String,
        nationalIdFrontUri: String,
        educationCategory: String,
        institutionName: String,
        academicLevelOrClass: String,
        facultyOrCombination: String,
        programOfStudy: String,
        birthCountry: String,
        birthProvince: String,
        birthDistrict: String,
        birthSector: String,
        birthCell: String,
        birthVillage: String,
        resCountry: String,
        resProvince: String,
        resDistrict: String,
        resSector: String,
        resCell: String,
        resVillage: String,
        password: String
    ): Result<UserEntity> = runCatching {
        val settings = dao.getSystemSettings() ?: SystemSettingsEntity()
        require(settings.registrationOpen && !settings.maintenanceMode) {
            "New registrations are currently closed by platform administration."
        }
        val cleanEmail = email.trim().lowercase()
        val normPhone = SecurityUtils.normalizeRwandaPhone(phone)
        val cleanNid = nationalIdNumber.trim().replace(" ", "")

        require(firstName.isNotBlank() && secondName.isNotBlank()) { "First and second name are required." }
        require(SecurityUtils.isValidEmail(cleanEmail)) { "Enter a valid email address." }
        require(SecurityUtils.isValidRwandaPhone(phone)) { "Enter a valid Rwandan phone number (e.g. 078xxxxxxx or +25078xxxxxxx)." }
        require(SecurityUtils.isValidNationalId(cleanNid)) { "Rwanda National ID must be 16 digits starting with 1." }
        require(nationalIdFrontUri.isNotBlank()) { "National ID photo upload is required." }
        require(SecurityUtils.evaluatePasswordStrength(password).isValid) {
            "Password must be at least 8 characters and include mixed case, numbers, or symbols."
        }

        val salt = SecurityUtils.generateSalt()
        val hash = SecurityUtils.hashPassword(password, salt)
        val userId = SecurityUtils.generateId("USR")

        db.withTransaction {
            require(dao.getUserByEmail(cleanEmail) == null) { "An account with this email already exists." }
            require(dao.getUserByPhone(normPhone) == null) { "This phone number is already registered." }
            require(dao.getUserByNationalId(cleanNid) == null) { "This National ID number is already registered." }

            val newUser = UserEntity(
                id = userId,
                firstName = firstName.trim(),
                secondName = secondName.trim(),
                email = cleanEmail,
                phone = normPhone,
                gender = gender,
                dateOfBirth = dateOfBirth.trim(),
                nationalIdNumber = cleanNid,
                nationalIdFrontUri = nationalIdFrontUri,
                educationCategory = educationCategory,
                institutionName = institutionName.trim(),
                academicLevelOrClass = academicLevelOrClass.trim(),
                facultyOrCombination = facultyOrCombination.trim(),
                programOfStudy = programOfStudy.trim(),
                birthCountry = birthCountry.trim(),
                birthProvince = birthProvince.trim(),
                birthDistrict = birthDistrict.trim(),
                birthSector = birthSector.trim(),
                birthCell = birthCell.trim(),
                birthVillage = birthVillage.trim(),
                resCountry = resCountry.trim(),
                resProvince = resProvince.trim(),
                resDistrict = resDistrict.trim(),
                resSector = resSector.trim(),
                resCell = resCell.trim(),
                resVillage = resVillage.trim(),
                passwordHash = hash,
                passwordSalt = salt,
                role = "USER",
                accountStatus = "PENDING",
                kycStatus = "NOT_SUBMITTED"
            )
            dao.insertUser(newUser)
            dao.upsertWallet(WalletEntity(userId = userId))

            dao.insertNotification(
                NotificationEntity(
                    id = SecurityUtils.generateId("NTF"),
                    userId = userId,
                    title = "Registration Submitted",
                    message = "Welcome ${newUser.firstName}! Your application is pending administrator review.",
                    category = "REGISTRATION"
                )
            )

            dao.getAdminUsers().forEach { admin ->
                dao.insertNotification(
                    NotificationEntity(
                        id = SecurityUtils.generateId("NTF"),
                        userId = admin.id,
                        title = "New User Registration",
                        message = "${newUser.firstName} ${newUser.secondName} (${newUser.email}) submitted registration for approval.",
                        category = "ADMIN_ALERT"
                    )
                )
            }

            dao.insertAuditLog(
                AuditLogEntity(
                    id = SecurityUtils.generateId("AUD"),
                    actorUserId = userId,
                    actorEmail = cleanEmail,
                    action = "USER_REGISTER",
                    targetEntity = userId,
                    details = "Completed 3-step registration; status set to PENDING."
                )
            )
            newUser
        }
    }

    // --- Authentication ---
    suspend fun login(email: String, password: String): Result<UserEntity> = runCatching {
        val cleanEmail = email.trim().lowercase()
        val user = dao.getUserByEmail(cleanEmail)
            ?: error("Invalid email or password.")

        if (user.accountStatus == "BLOCKED") {
            error("This account has been blocked for security review. Contact support.")
        }
        if (user.accountStatus == "SUSPENDED") {
            error("This account is currently suspended. Contact platform administration.")
        }
        if (user.failedLoginAttempts >= 5) {
            error("Too many failed login attempts. Use Forgot Password or contact an administrator.")
        }

        val valid = SecurityUtils.verifyPassword(password, user.passwordSalt, user.passwordHash)
        if (!valid) {
            dao.updateUser(user.copy(failedLoginAttempts = user.failedLoginAttempts + 1, updatedAt = System.currentTimeMillis()))
            error("Invalid email or password. (${4 - user.failedLoginAttempts} attempts remaining)")
        }

        val updated = user.copy(failedLoginAttempts = 0, updatedAt = System.currentTimeMillis())
        dao.updateUser(updated)
        dao.insertAuditLog(
            AuditLogEntity(
                id = SecurityUtils.generateId("AUD"),
                actorUserId = updated.id,
                actorEmail = updated.email,
                action = "USER_LOGIN",
                targetEntity = updated.id,
                details = "Authenticated session (${updated.role})."
            )
        )
        updated
    }

    suspend fun changePassword(userId: String, newPassword: String): Result<UserEntity> = runCatching {
        require(SecurityUtils.evaluatePasswordStrength(newPassword).isValid) {
            "New password must be at least 8 characters with mixed character types."
        }
        val user = dao.getUserById(userId) ?: error("User not found.")
        val salt = SecurityUtils.generateSalt()
        val hash = SecurityUtils.hashPassword(newPassword, salt)
        val updated = user.copy(
            passwordSalt = salt,
            passwordHash = hash,
            mustChangePassword = false,
            failedLoginAttempts = 0,
            updatedAt = System.currentTimeMillis()
        )
        dao.updateUser(updated)
        dao.insertAuditLog(
            AuditLogEntity(
                id = SecurityUtils.generateId("AUD"),
                actorUserId = userId,
                actorEmail = user.email,
                action = "PASSWORD_CHANGED",
                targetEntity = userId,
                details = "Password updated securely."
            )
        )
        updated
    }

    suspend fun resetForgottenPassword(
        email: String,
        nationalIdNumber: String,
        phone: String,
        newPassword: String
    ): Result<Unit> = runCatching {
        val user = dao.getUserByEmail(email.trim().lowercase()) ?: error("No account found for this email.")
        val normPhone = SecurityUtils.normalizeRwandaPhone(phone)
        require(user.nationalIdNumber == nationalIdNumber.trim() && user.phone == normPhone) {
            "Identity details do not match our records."
        }
        require(SecurityUtils.evaluatePasswordStrength(newPassword).isValid) {
            "New password is not strong enough."
        }
        val salt = SecurityUtils.generateSalt()
        val hash = SecurityUtils.hashPassword(newPassword, salt)
        dao.updateUser(
            user.copy(
                passwordSalt = salt,
                passwordHash = hash,
                failedLoginAttempts = 0,
                updatedAt = System.currentTimeMillis()
            )
        )
        dao.insertAuditLog(
            AuditLogEntity(
                id = SecurityUtils.generateId("AUD"),
                actorUserId = user.id,
                actorEmail = user.email,
                action = "PASSWORD_RECOVERY",
                targetEntity = user.id,
                details = "Password reset via verified identity check."
            )
        )
    }

    suspend fun updateProfilePhoto(userId: String, photoPath: String): Result<Unit> = runCatching {
        require(photoPath.isNotBlank()) { "Select a valid image." }
        val user = dao.getUserById(userId) ?: error("User not found.")
        dao.updateUser(user.copy(profilePhotoUri = photoPath, updatedAt = System.currentTimeMillis()))
        dao.insertAuditLog(
            AuditLogEntity(
                id = SecurityUtils.generateId("AUD"),
                actorUserId = userId,
                actorEmail = user.email,
                action = "PROFILE_PHOTO_UPDATED",
                targetEntity = userId,
                details = "Updated member profile photo (National ID document remains locked)."
            )
        )
    }

    // --- Activation & Deposits (Mobile Money Reference Verification) ---
    suspend fun submitPaymentVerification(
        userId: String,
        paymentType: String,
        amountRwf: Long,
        submittedRef: String,
        relatedEntityId: String = ""
    ): Result<Unit> = runCatching {
        require(amountRwf > 0) { "Amount must be greater than zero." }
        require(submittedRef.trim().length >= 5) { "Enter a valid Mobile Money transaction reference." }
        val settings = dao.getSystemSettings() ?: SystemSettingsEntity()
        val ussdRef = SecurityUtils.formatUssdReference(settings.paymentNumber, amountRwf)

        db.withTransaction {
            val pv = PaymentVerificationEntity(
                id = SecurityUtils.generateId("PAY"),
                userId = userId,
                paymentType = paymentType,
                relatedEntityId = relatedEntityId,
                amountRwf = amountRwf,
                ussdReference = ussdRef,
                submittedTransactionId = submittedRef.trim().uppercase(),
                status = "PENDING"
            )
            dao.insertPaymentVerification(pv)

            if (paymentType == "VIDEO_CAMPAIGN" && relatedEntityId.isNotBlank()) {
                dao.getVideoById(relatedEntityId)?.let { v ->
                    dao.upsertVideoCampaign(v.copy(status = "PAYMENT_VERIFICATION", paymentReference = submittedRef.trim().uppercase()))
                }
            }
            if (paymentType == "TASK_CAMPAIGN" && relatedEntityId.isNotBlank()) {
                dao.getTaskById(relatedEntityId)?.let { t ->
                    dao.upsertPaidTask(t.copy(status = "PAYMENT_VERIFICATION", paymentReference = submittedRef.trim().uppercase()))
                }
            }

            dao.insertTransaction(
                FinancialTransactionEntity(
                    id = SecurityUtils.generateId("TXN"),
                    userId = userId,
                    category = when (paymentType) {
                        "ACTIVATION" -> "ACCOUNT_ACTIVATION"
                        "VIDEO_CAMPAIGN" -> "VIDEO_CAMPAIGN_PAYMENT"
                        "TASK_CAMPAIGN" -> "TASK_CAMPAIGN_PAYMENT"
                        else -> "DEPOSIT"
                    },
                    amountRwf = amountRwf,
                    feeRwf = 0L,
                    netAmountRwf = amountRwf,
                    description = "Submitted payment reference ${submittedRef.trim().uppercase()} ($paymentType) awaiting verification.",
                    paymentReference = submittedRef.trim().uppercase(),
                    status = "PENDING"
                )
            )

            dao.getAdminUsers().forEach { admin ->
                dao.insertNotification(
                    NotificationEntity(
                        id = SecurityUtils.generateId("NTF"),
                        userId = admin.id,
                        title = "Payment Verification Request",
                        message = "$paymentType of ${SecurityUtils.formatRwf(amountRwf)} (Ref: ${submittedRef.trim().uppercase()}) awaiting review.",
                        category = "ADMIN_PAYMENT"
                    )
                )
            }
        }
    }

    // --- KYC Submission ---
    suspend fun submitKyc(
        userId: String,
        fullLegalName: String,
        nationalIdNumber: String,
        dateOfBirth: String,
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
        additionalNotes: String
    ): Result<Unit> = runCatching {
        require(fullLegalName.isNotBlank()) { "Full legal name is required." }
        require(SecurityUtils.isValidNationalId(nationalIdNumber)) { "Enter a valid 16-digit Rwandan National ID." }
        require(idFrontUri.isNotBlank()) { "National ID front photo is required." }

        db.withTransaction {
            val user = dao.getUserById(userId) ?: error("User not found.")
            val kyc = KycApplicationEntity(
                id = SecurityUtils.generateId("KYC"),
                userId = userId,
                fullLegalName = fullLegalName.trim(),
                nationalIdNumber = nationalIdNumber.trim(),
                dateOfBirth = dateOfBirth.trim(),
                gender = gender,
                phone = SecurityUtils.normalizeRwandaPhone(phone),
                email = email.trim().lowercase(),
                province = province.trim(),
                district = district.trim(),
                sector = sector.trim(),
                cell = cell.trim(),
                village = village.trim(),
                idFrontUri = idFrontUri,
                idBackUri = idBackUri,
                selfieUri = selfieUri,
                additionalNotes = additionalNotes.trim(),
                status = "PENDING"
            )
            dao.upsertKyc(kyc)
            dao.updateUser(user.copy(kycStatus = "PENDING", updatedAt = System.currentTimeMillis()))
            dao.insertNotification(
                NotificationEntity(
                    id = SecurityUtils.generateId("NTF"),
                    userId = userId,
                    title = "KYC Submitted",
                    message = "Your KYC documents have been securely stored for compliance review.",
                    category = "KYC"
                )
            )
            dao.getAdminUsers().forEach { admin ->
                dao.insertNotification(
                    NotificationEntity(
                        id = SecurityUtils.generateId("NTF"),
                        userId = admin.id,
                        title = "New KYC Application",
                        message = "$fullLegalName submitted KYC verification documents.",
                        category = "ADMIN_KYC"
                    )
                )
            }
        }
    }

    // --- Savings Module ---
    suspend fun createOrDepositSavings(
        userId: String,
        planType: String,
        purpose: String,
        description: String,
        targetAmountRwf: Long,
        depositAmountRwf: Long
    ): Result<Unit> = runCatching {
        require(depositAmountRwf > 0) { "Savings deposit must be greater than zero." }
        require(purpose.isNotBlank()) { "Please enter a savings purpose." }

        db.withTransaction {
            val user = dao.getUserById(userId) ?: error("User not found.")
            val settings = dao.getSystemSettings() ?: SystemSettingsEntity()
            require(user.accountStatus == "ACTIVE") { "Account must be active before saving." }
            if (settings.kycRequiredForFinance) {
                require(user.kycStatus == "APPROVED") { "Approved KYC verification is required before using savings vaults." }
            }
            val wallet = dao.getWallet(userId) ?: error("Wallet not found.")
            require(wallet.availableBalanceRwf >= depositAmountRwf) {
                "Insufficient available balance (${SecurityUtils.formatRwf(wallet.availableBalanceRwf)})."
            }

            val savingsId = SecurityUtils.generateId("SAV")
            val account = SavingsAccountEntity(
                id = savingsId,
                userId = userId,
                planType = planType,
                purpose = purpose.trim(),
                description = description.trim(),
                targetAmountRwf = targetAmountRwf.coerceAtLeast(depositAmountRwf),
                balanceRwf = depositAmountRwf,
                totalDepositedRwf = depositAmountRwf
            )
            dao.upsertSavingsAccount(account)
            dao.upsertWallet(
                wallet.copy(
                    availableBalanceRwf = wallet.availableBalanceRwf - depositAmountRwf,
                    savingsBalanceRwf = wallet.savingsBalanceRwf + depositAmountRwf,
                    updatedAt = System.currentTimeMillis()
                )
            )
            dao.insertTransaction(
                FinancialTransactionEntity(
                    id = SecurityUtils.generateId("TXN"),
                    userId = userId,
                    category = "SAVING",
                    amountRwf = depositAmountRwf,
                    feeRwf = 0L,
                    netAmountRwf = depositAmountRwf,
                    description = "Saved into $planType vault: $purpose",
                    paymentReference = savingsId,
                    status = "COMPLETED"
                )
            )
        }
    }

    suspend fun topUpSavingsAccount(userId: String, savingsId: String, amountRwf: Long): Result<Unit> = runCatching {
        require(amountRwf > 0) { "Amount must be greater than zero." }
        db.withTransaction {
            val user = dao.getUserById(userId) ?: error("User not found.")
            val settings = dao.getSystemSettings() ?: SystemSettingsEntity()
            require(user.accountStatus == "ACTIVE") { "Account must be active." }
            if (settings.kycRequiredForFinance) {
                require(user.kycStatus == "APPROVED") { "KYC must be approved." }
            }
            val wallet = dao.getWallet(userId) ?: error("Wallet not found.")
            require(wallet.availableBalanceRwf >= amountRwf) { "Insufficient available balance." }
            val account = dao.getSavingsAccountById(savingsId) ?: error("Savings vault not found.")
            require(account.userId == userId) { "Unauthorized savings access." }

            dao.upsertSavingsAccount(
                account.copy(
                    balanceRwf = account.balanceRwf + amountRwf,
                    totalDepositedRwf = account.totalDepositedRwf + amountRwf,
                    updatedAt = System.currentTimeMillis()
                )
            )
            dao.upsertWallet(
                wallet.copy(
                    availableBalanceRwf = wallet.availableBalanceRwf - amountRwf,
                    savingsBalanceRwf = wallet.savingsBalanceRwf + amountRwf,
                    updatedAt = System.currentTimeMillis()
                )
            )
            dao.insertTransaction(
                FinancialTransactionEntity(
                    id = SecurityUtils.generateId("TXN"),
                    userId = userId,
                    category = "SAVING",
                    amountRwf = amountRwf,
                    feeRwf = 0L,
                    netAmountRwf = amountRwf,
                    description = "Top-up to ${account.purpose} (${account.planType})",
                    paymentReference = savingsId,
                    status = "COMPLETED"
                )
            )
        }
    }

    suspend fun withdrawFromSavings(userId: String, savingsId: String, amountRwf: Long): Result<Unit> = runCatching {
        require(amountRwf > 0) { "Amount must be greater than zero." }
        db.withTransaction {
            val account = dao.getSavingsAccountById(savingsId) ?: error("Savings vault not found.")
            require(account.userId == userId) { "Unauthorized savings access." }
            require(account.balanceRwf >= amountRwf) { "Insufficient balance in this savings vault." }
            if (account.planType == "LONG_TERM" && account.balanceRwf < account.targetAmountRwf) {
                error("Long-Term Savings unlocks after reaching target (${SecurityUtils.formatRwf(account.targetAmountRwf)}).")
            }
            val wallet = dao.getWallet(userId) ?: error("Wallet not found.")

            dao.upsertSavingsAccount(
                account.copy(
                    balanceRwf = account.balanceRwf - amountRwf,
                    updatedAt = System.currentTimeMillis()
                )
            )
            dao.upsertWallet(
                wallet.copy(
                    availableBalanceRwf = wallet.availableBalanceRwf + amountRwf,
                    savingsBalanceRwf = (wallet.savingsBalanceRwf - amountRwf).coerceAtLeast(0L),
                    updatedAt = System.currentTimeMillis()
                )
            )
            dao.insertTransaction(
                FinancialTransactionEntity(
                    id = SecurityUtils.generateId("TXN"),
                    userId = userId,
                    category = "SAVINGS_WITHDRAWAL",
                    amountRwf = amountRwf,
                    feeRwf = 0L,
                    netAmountRwf = amountRwf,
                    description = "Transferred from ${account.purpose} savings vault to Available Balance",
                    paymentReference = savingsId,
                    status = "COMPLETED"
                )
            )
        }
    }

    // --- Withdrawals (25% Configurable Fee) ---
    suspend fun requestWithdrawal(
        userId: String,
        provider: String,
        destinationPhone: String,
        requestedAmountRwf: Long
    ): Result<WithdrawalRequestEntity> = runCatching {
        require(SecurityUtils.isValidRwandaPhone(destinationPhone)) {
            "Enter a valid Rwandan Mobile Money number."
        }
        db.withTransaction {
            val user = dao.getUserById(userId) ?: error("User not found.")
            val settings = dao.getSystemSettings() ?: SystemSettingsEntity()
            require(user.accountStatus == "ACTIVE") { "Account must be active." }
            require(user.kycStatus == "APPROVED") { "KYC verification must be approved before withdrawing funds." }
            require(requestedAmountRwf >= settings.minWithdrawalRwf) {
                "Minimum withdrawal is ${SecurityUtils.formatRwf(settings.minWithdrawalRwf)}."
            }
            require(requestedAmountRwf <= settings.maxWithdrawalRwf) {
                "Maximum single withdrawal is ${SecurityUtils.formatRwf(settings.maxWithdrawalRwf)}."
            }

            val wallet = dao.getWallet(userId) ?: error("Wallet not found.")
            val withdrawableBalance = if (settings.welcomeBonusWithdrawable) {
                wallet.availableBalanceRwf
            } else {
                (wallet.availableBalanceRwf - wallet.welcomeBonusRwf).coerceAtLeast(0L)
            }
            require(withdrawableBalance >= requestedAmountRwf) {
                "Insufficient withdrawable balance (${SecurityUtils.formatRwf(withdrawableBalance)})."
            }

            val feeAmount = Math.round(requestedAmountRwf * (settings.withdrawalFeePercent / 100.0))
            val netAmount = (requestedAmountRwf - feeAmount).coerceAtLeast(0L)
            val ref = SecurityUtils.generateId("WDR")

            val req = WithdrawalRequestEntity(
                id = ref,
                userId = userId,
                provider = provider,
                destinationPhone = SecurityUtils.normalizeRwandaPhone(destinationPhone),
                requestedAmountRwf = requestedAmountRwf,
                feePercentage = settings.withdrawalFeePercent,
                feeAmountRwf = feeAmount,
                netAmountRwf = netAmount,
                reference = ref,
                status = "PENDING"
            )
            dao.insertWithdrawal(req)

            // Deduct once and hold in pendingWithdrawals
            dao.upsertWallet(
                wallet.copy(
                    availableBalanceRwf = wallet.availableBalanceRwf - requestedAmountRwf,
                    pendingWithdrawalsRwf = wallet.pendingWithdrawalsRwf + requestedAmountRwf,
                    updatedAt = System.currentTimeMillis()
                )
            )
            dao.insertTransaction(
                FinancialTransactionEntity(
                    id = SecurityUtils.generateId("TXN"),
                    userId = userId,
                    category = "WITHDRAWAL",
                    amountRwf = requestedAmountRwf,
                    feeRwf = feeAmount,
                    netAmountRwf = netAmount,
                    description = "$provider withdrawal to ${req.destinationPhone} (Fee ${settings.withdrawalFeePercent}%)",
                    paymentReference = ref,
                    status = "PENDING"
                )
            )
            dao.getAdminUsers().forEach { admin ->
                dao.insertNotification(
                    NotificationEntity(
                        id = SecurityUtils.generateId("NTF"),
                        userId = admin.id,
                        title = "Withdrawal Request",
                        message = "${user.firstName} requested ${SecurityUtils.formatRwf(requestedAmountRwf)} via $provider.",
                        category = "ADMIN_WITHDRAWAL"
                    )
                )
            }
            req
        }
    }

    // --- Loans ---
    suspend fun applyForLoan(
        userId: String,
        productId: String,
        requestedAmountRwf: Long,
        repaymentMonths: Int,
        purpose: String,
        supportingInfo: String
    ): Result<Unit> = runCatching {
        require(purpose.isNotBlank()) { "Please specify your loan purpose." }
        db.withTransaction {
            val user = dao.getUserById(userId) ?: error("User not found.")
            val settings = dao.getSystemSettings() ?: SystemSettingsEntity()
            require(user.accountStatus == "ACTIVE") { "Account must be active." }
            if (settings.kycRequiredForFinance) {
                require(user.kycStatus == "APPROVED") { "Approved KYC is required to apply for a loan." }
            }
            val product = dao.getLoanProductById(productId) ?: error("Loan product not found.")
            require(product.isActive) { "Selected loan product is unavailable." }
            require(requestedAmountRwf in product.minAmountRwf..minOf(product.maxAmountRwf, settings.maxLoanLimitRwf)) {
                "Amount must be between ${SecurityUtils.formatRwf(product.minAmountRwf)} and ${SecurityUtils.formatRwf(minOf(product.maxAmountRwf, settings.maxLoanLimitRwf))}."
            }
            require(repaymentMonths in 1..product.maxDurationMonths) {
                "Repayment period must be 1 to ${product.maxDurationMonths} months."
            }

            val totalInterest = Math.round(requestedAmountRwf * (product.interestRatePercent / 100.0))
            val totalRepayable = requestedAmountRwf + totalInterest

            val loan = LoanApplicationEntity(
                id = SecurityUtils.generateId("LON"),
                userId = userId,
                productId = product.id,
                productName = product.name,
                requestedAmountRwf = requestedAmountRwf,
                repaymentMonths = repaymentMonths,
                interestRatePercent = product.interestRatePercent,
                totalRepayableRwf = totalRepayable,
                outstandingBalanceRwf = totalRepayable,
                purpose = purpose.trim(),
                supportingInfo = supportingInfo.trim(),
                status = "PENDING"
            )
            dao.insertLoanApplication(loan)
            dao.insertNotification(
                NotificationEntity(
                    id = SecurityUtils.generateId("NTF"),
                    userId = userId,
                    title = "Loan Application Received",
                    message = "Your ${SecurityUtils.formatRwf(requestedAmountRwf)} loan request is under eligibility review.",
                    category = "LOAN"
                )
            )
            dao.getAdminUsers().forEach { admin ->
                dao.insertNotification(
                    NotificationEntity(
                        id = SecurityUtils.generateId("NTF"),
                        userId = admin.id,
                        title = "New Loan Application",
                        message = "${user.firstName} ${user.secondName} applied for ${SecurityUtils.formatRwf(requestedAmountRwf)}.",
                        category = "ADMIN_LOAN"
                    )
                )
            }
        }
    }

    suspend fun repayLoan(userId: String, loanId: String, amountRwf: Long): Result<Unit> = runCatching {
        require(amountRwf > 0) { "Repayment amount must be greater than zero." }
        db.withTransaction {
            val loan = dao.getLoanById(loanId) ?: error("Loan not found.")
            require(loan.userId == userId) { "Unauthorized loan access." }
            require(loan.status == "DISBURSED" || loan.status == "OVERDUE") {
                "Only disbursed or overdue loans can be repaid."
            }
            val wallet = dao.getWallet(userId) ?: error("Wallet not found.")
            require(wallet.availableBalanceRwf >= amountRwf) { "Insufficient available balance." }
            val applied = minOf(amountRwf, loan.outstandingBalanceRwf)
            val newOutstanding = loan.outstandingBalanceRwf - applied
            val newStatus = if (newOutstanding == 0L) "REPAID" else loan.status
            val ref = SecurityUtils.generateId("RPY")

            dao.insertLoanRepayment(
                LoanRepaymentEntity(
                    id = ref,
                    loanId = loanId,
                    userId = userId,
                    amountRwf = applied,
                    reference = ref
                )
            )
            dao.updateLoanApplication(
                loan.copy(
                    amountPaidRwf = loan.amountPaidRwf + applied,
                    outstandingBalanceRwf = newOutstanding,
                    status = newStatus
                )
            )
            dao.upsertWallet(
                wallet.copy(
                    availableBalanceRwf = wallet.availableBalanceRwf - applied,
                    loanBalanceRwf = (wallet.loanBalanceRwf - applied).coerceAtLeast(0L),
                    updatedAt = System.currentTimeMillis()
                )
            )
            dao.insertTransaction(
                FinancialTransactionEntity(
                    id = SecurityUtils.generateId("TXN"),
                    userId = userId,
                    category = "LOAN_REPAYMENT",
                    amountRwf = applied,
                    feeRwf = 0L,
                    netAmountRwf = applied,
                    description = "Loan repayment for ${loan.productName} ($loanId)",
                    paymentReference = ref,
                    status = "COMPLETED"
                )
            )
        }
    }

    // --- Watch Videos & Earn ---
    suspend fun completeVideoView(userId: String, videoId: String, watchedSeconds: Int): Result<Long> = runCatching {
        db.withTransaction {
            val user = dao.getUserById(userId) ?: error("User not found.")
            require(user.accountStatus == "ACTIVE") { "Active account required to earn video rewards." }
            val video = dao.getVideoById(videoId) ?: error("Video campaign not found.")
            require(video.status == "ACTIVE") { "This video campaign is no longer active." }
            require(video.validViews < video.targetViews && video.spentBudgetRwf + video.rewardPerViewRwf <= video.totalBudgetRwf) {
                "This campaign has reached its maximum eligible views."
            }
            require(dao.getVideoView(userId, videoId) == null) {
                "You have already earned a reward for this video."
            }
            require(watchedSeconds >= video.minWatchSeconds) {
                "Watch at least ${video.minWatchSeconds} seconds to qualify for the reward."
            }

            val reward = video.rewardPerViewRwf
            dao.insertVideoView(
                VideoViewEntity(
                    id = SecurityUtils.generateId("VW"),
                    userId = userId,
                    videoId = videoId,
                    watchedSeconds = watchedSeconds,
                    completed = true,
                    rewarded = true
                )
            )

            val newValidViews = video.validViews + 1
            val newSpent = video.spentBudgetRwf + reward
            val newStatus = if (newValidViews >= video.targetViews) "COMPLETED" else "ACTIVE"
            dao.upsertVideoCampaign(
                video.copy(
                    validViews = newValidViews,
                    spentBudgetRwf = newSpent,
                    status = newStatus
                )
            )

            val wallet = dao.getWallet(userId) ?: WalletEntity(userId = userId)
            dao.upsertWallet(
                wallet.copy(
                    availableBalanceRwf = wallet.availableBalanceRwf + reward,
                    videoEarningsRwf = wallet.videoEarningsRwf + reward,
                    totalEarningsRwf = wallet.totalEarningsRwf + reward,
                    updatedAt = System.currentTimeMillis()
                )
            )
            dao.insertTransaction(
                FinancialTransactionEntity(
                    id = SecurityUtils.generateId("TXN"),
                    userId = userId,
                    category = "VIDEO_REWARD",
                    amountRwf = reward,
                    feeRwf = 0L,
                    netAmountRwf = reward,
                    description = "Completed watch reward for \"${video.title}\"",
                    paymentReference = videoId,
                    status = "COMPLETED"
                )
            )
            dao.insertNotification(
                NotificationEntity(
                    id = SecurityUtils.generateId("NTF"),
                    userId = userId,
                    title = "Video Reward Credited",
                    message = "You earned ${SecurityUtils.formatRwf(reward)} for watching \"${video.title}\".",
                    category = "REWARD"
                )
            )
            reward
        }
    }

    suspend fun createVideoCampaign(
        userId: String,
        title: String,
        description: String,
        category: String,
        videoUri: String,
        thumbnailUri: String,
        durationSeconds: Int,
        rewardPerViewRwf: Long,
        targetViews: Int,
        durationDays: Int
    ): Result<VideoCampaignEntity> = runCatching {
        val settings = dao.getSystemSettings() ?: SystemSettingsEntity()
        require(title.isNotBlank() && description.isNotBlank()) { "Title and description are required." }
        require(rewardPerViewRwf >= settings.minVideoRewardRwf) {
            "Minimum reward per valid view is ${SecurityUtils.formatRwf(settings.minVideoRewardRwf)}."
        }
        require(targetViews >= 1) { "Target viewers must be at least 1." }

        val rewardBudget = rewardPerViewRwf * targetViews
        val platformFee = Math.round(rewardBudget * (settings.campaignFeePercent / 100.0))
        val totalBudget = rewardBudget + platformFee
        val now = System.currentTimeMillis()

        val campaign = VideoCampaignEntity(
            id = SecurityUtils.generateId("VID"),
            creatorUserId = userId,
            title = title.trim(),
            description = description.trim(),
            category = category,
            videoUri = videoUri,
            thumbnailUri = thumbnailUri,
            durationSeconds = durationSeconds.coerceAtLeast(15),
            minWatchSeconds = settings.minVideoWatchSeconds.coerceAtMost(durationSeconds.coerceAtLeast(15)),
            rewardPerViewRwf = rewardPerViewRwf,
            targetViews = targetViews,
            platformFeeRwf = platformFee,
            totalBudgetRwf = totalBudget,
            status = "PENDING_PAYMENT",
            startDateMillis = now,
            endDateMillis = now + durationDays.coerceAtLeast(1) * 24L * 60L * 60L * 1000L
        )
        dao.upsertVideoCampaign(campaign)
        campaign
    }

    // --- Paid Tasks ---
    suspend fun createPaidTask(
        userId: String,
        title: String,
        description: String,
        category: String,
        instructions: String,
        requirements: String,
        requiredProofType: String,
        targetParticipants: Int,
        rewardPerParticipantRwf: Long,
        durationDays: Int
    ): Result<PaidTaskEntity> = runCatching {
        val settings = dao.getSystemSettings() ?: SystemSettingsEntity()
        require(title.isNotBlank() && instructions.isNotBlank() && requirements.isNotBlank()) {
            "Title, instructions, and requirements are mandatory."
        }
        require(targetParticipants >= 1 && rewardPerParticipantRwf >= 20L) {
            "Specify valid participants and reward (>= 20 RWF)."
        }

        val rewardBudget = rewardPerParticipantRwf * targetParticipants
        val platformFee = Math.round(rewardBudget * (settings.campaignFeePercent / 100.0))
        val totalBudget = rewardBudget + platformFee
        val now = System.currentTimeMillis()

        val task = PaidTaskEntity(
            id = SecurityUtils.generateId("TSK"),
            creatorUserId = userId,
            title = title.trim(),
            description = description.trim(),
            category = category,
            instructions = instructions.trim(),
            requirements = requirements.trim(),
            requiredProofType = requiredProofType,
            targetParticipants = targetParticipants,
            rewardPerParticipantRwf = rewardPerParticipantRwf,
            platformFeeRwf = platformFee,
            totalBudgetRwf = totalBudget,
            status = "PENDING_PAYMENT",
            startDateMillis = now,
            endDateMillis = now + durationDays.coerceAtLeast(1) * 24L * 60L * 60L * 1000L
        )
        dao.upsertPaidTask(task)
        task
    }

    suspend fun submitTaskCompletion(
        userId: String,
        taskId: String,
        proofText: String,
        proofImageUri: String
    ): Result<Unit> = runCatching {
        require(proofText.isNotBlank() || proofImageUri.isNotBlank()) {
            "Provide required text proof or screenshot evidence."
        }
        db.withTransaction {
            val user = dao.getUserById(userId) ?: error("User not found.")
            require(user.accountStatus == "ACTIVE") { "Active account required." }
            val task = dao.getTaskById(taskId) ?: error("Task not found.")
            require(task.status == "ACTIVE") { "This task is not active." }
            require(task.creatorUserId != userId) { "Task creators cannot submit their own task." }
            require(task.approvedParticipants < task.targetParticipants) { "Task participant quota is full." }
            require(dao.getTaskSubmission(userId, taskId) == null) {
                "Anti-duplication rule: You have already submitted this task."
            }

            dao.insertTaskSubmission(
                TaskSubmissionEntity(
                    id = SecurityUtils.generateId("SUB"),
                    taskId = taskId,
                    userId = userId,
                    proofText = proofText.trim(),
                    proofImageUri = proofImageUri,
                    status = "PENDING_REVIEW"
                )
            )
            dao.getAdminUsers().forEach { admin ->
                dao.insertNotification(
                    NotificationEntity(
                        id = SecurityUtils.generateId("NTF"),
                        userId = admin.id,
                        title = "Task Submission Pending Review",
                        message = "${user.firstName} submitted proof for \"${task.title}\".",
                        category = "ADMIN_TASK"
                    )
                )
            }
        }
    }

    // --- Daily Check-In (Africa/Kigali) ---
    suspend fun claimDailyCheckin(userId: String): Result<DailyCheckinEntity> = runCatching {
        val todayKey = SecurityUtils.kigaliTodayKey()
        val yesterdayKey = SecurityUtils.kigaliTodayKey(System.currentTimeMillis() - 24L * 60L * 60L * 1000L)

        db.withTransaction {
            val user = dao.getUserById(userId) ?: error("User not found.")
            require(user.accountStatus == "ACTIVE") { "Account must be active to claim daily check-in." }
            require(dao.getCheckinForDate(userId, todayKey) == null) {
                "Today's Rwanda calendar check-in ($todayKey) has already been claimed."
            }
            val settings = dao.getSystemSettings() ?: SystemSettingsEntity()
            val reward = settings.dailyCheckinRewardRwf
            val yesterdayCheckin = dao.getCheckinForDate(userId, yesterdayKey)
            val streak = (yesterdayCheckin?.streakCount ?: 0) + 1

            val checkin = DailyCheckinEntity(
                id = SecurityUtils.generateId("CHK"),
                userId = userId,
                kigaliDate = todayKey,
                rewardRwf = reward,
                streakCount = streak
            )
            dao.insertDailyCheckin(checkin)

            val wallet = dao.getWallet(userId) ?: WalletEntity(userId = userId)
            dao.upsertWallet(
                wallet.copy(
                    availableBalanceRwf = wallet.availableBalanceRwf + reward,
                    checkinRewardsRwf = wallet.checkinRewardsRwf + reward,
                    totalEarningsRwf = wallet.totalEarningsRwf + reward,
                    updatedAt = System.currentTimeMillis()
                )
            )
            dao.insertTransaction(
                FinancialTransactionEntity(
                    id = SecurityUtils.generateId("TXN"),
                    userId = userId,
                    category = "DAILY_CHECKIN",
                    amountRwf = reward,
                    feeRwf = 0L,
                    netAmountRwf = reward,
                    description = "Daily Check-In Reward ($todayKey • Day $streak streak)",
                    paymentReference = checkin.id,
                    status = "COMPLETED"
                )
            )
            dao.insertNotification(
                NotificationEntity(
                    id = SecurityUtils.generateId("NTF"),
                    userId = userId,
                    title = "Daily Check-In Claimed",
                    message = "+${SecurityUtils.formatRwf(reward)} credited! Current streak: $streak day(s).",
                    category = "REWARD"
                )
            )
            checkin
        }
    }

    // --- Support & Notifications ---
    suspend fun markNotificationsRead(userId: String) = dao.markAllNotificationsRead(userId)

    suspend fun createSupportTicket(
        userId: String,
        category: String,
        subject: String,
        description: String,
        screenshotUri: String
    ): Result<Unit> = runCatching {
        require(subject.isNotBlank() && description.isNotBlank()) { "Subject and problem explanation are required." }
        val ticket = SupportTicketEntity(
            id = SecurityUtils.generateId("TKT"),
            userId = userId,
            category = category,
            subject = subject.trim(),
            description = description.trim(),
            screenshotUri = screenshotUri
        )
        dao.upsertSupportTicket(ticket)
        dao.getAdminUsers().forEach { admin ->
            dao.insertNotification(
                NotificationEntity(
                    id = SecurityUtils.generateId("NTF"),
                    userId = admin.id,
                    title = "New Support Ticket",
                    message = "[$category] ${subject.trim()}",
                    category = "SUPPORT"
                )
            )
        }
    }

    // =====================================================================
    // SUPER ADMINISTRATOR OPERATIONS (Full Audit Trail)
    // =====================================================================
    private suspend fun requireAdmin(adminId: String): UserEntity {
        val admin = dao.getUserById(adminId) ?: error("Administrator not found.")
        require(admin.role == "SUPER_ADMIN") { "Unauthorized: Super Admin privileges required." }
        return admin
    }

    suspend fun adminUpdateUserStatus(
        adminId: String,
        targetUserId: String,
        newStatus: String,
        note: String
    ): Result<Unit> = runCatching {
        db.withTransaction {
            val admin = requireAdmin(adminId)
            val target = dao.getUserById(targetUserId) ?: error("User not found.")
            val mappedStatus = when (newStatus) {
                "APPROVE" -> "APPROVED_AWAITING_ACTIVATION"
                else -> newStatus
            }
            dao.updateUser(target.copy(accountStatus = mappedStatus, statusNote = note.trim(), updatedAt = System.currentTimeMillis()))
            dao.insertNotification(
                NotificationEntity(
                    id = SecurityUtils.generateId("NTF"),
                    userId = targetUserId,
                    title = "Account Status Updated: $mappedStatus",
                    message = if (mappedStatus == "APPROVED_AWAITING_ACTIVATION") {
                        "Your registration was approved! Proceed to pay the activation fee to unlock your dashboard."
                    } else {
                        note.ifBlank { "Your account status was updated to $mappedStatus by administration." }
                    },
                    category = "ACCOUNT"
                )
            )
            dao.insertAuditLog(
                AuditLogEntity(
                    id = SecurityUtils.generateId("AUD"),
                    actorUserId = admin.id,
                    actorEmail = admin.email,
                    action = "USER_STATUS_$mappedStatus",
                    targetEntity = targetUserId,
                    details = "Changed ${target.email} status to $mappedStatus. Note: $note"
                )
            )
        }
    }

    suspend fun adminUnlockUserLogin(adminId: String, targetUserId: String): Result<Unit> = runCatching {
        db.withTransaction {
            val admin = requireAdmin(adminId)
            val target = dao.getUserById(targetUserId) ?: error("User not found.")
            dao.updateUser(target.copy(failedLoginAttempts = 0, accountStatus = if (target.accountStatus == "SUSPENDED" || target.accountStatus == "BLOCKED") "ACTIVE" else target.accountStatus))
            dao.insertAuditLog(
                AuditLogEntity(
                    id = SecurityUtils.generateId("AUD"),
                    actorUserId = admin.id,
                    actorEmail = admin.email,
                    action = "USER_UNLOCK",
                    targetEntity = targetUserId,
                    details = "Reset failed login attempts / reactivated ${target.email}."
                )
            )
        }
    }

    suspend fun adminCorrectUserProfile(
        adminId: String,
        targetUserId: String,
        newFirstName: String,
        newSecondName: String,
        newNationalId: String,
        reason: String
    ): Result<Unit> = runCatching {
        require(reason.isNotBlank()) { "An audit reason is mandatory for user corrections." }
        db.withTransaction {
            val admin = requireAdmin(adminId)
            val target = dao.getUserById(targetUserId) ?: error("User not found.")
            dao.updateUser(
                target.copy(
                    firstName = newFirstName.trim(),
                    secondName = newSecondName.trim(),
                    nationalIdNumber = newNationalId.trim(),
                    updatedAt = System.currentTimeMillis()
                )
            )
            dao.insertAuditLog(
                AuditLogEntity(
                    id = SecurityUtils.generateId("AUD"),
                    actorUserId = admin.id,
                    actorEmail = admin.email,
                    action = "USER_INFO_CORRECTION",
                    targetEntity = targetUserId,
                    details = "Updated profile fields for ${target.email}. Reason: $reason"
                )
            )
        }
    }

    suspend fun adminReviewPaymentVerification(
        adminId: String,
        paymentId: String,
        approve: Boolean,
        note: String
    ): Result<Unit> = runCatching {
        db.withTransaction {
            val admin = requireAdmin(adminId)
            val pv = dao.getPaymentVerificationById(paymentId) ?: error("Payment record not found.")
            require(pv.status == "PENDING") { "This payment has already been reviewed." }
            val user = dao.getUserById(pv.userId) ?: error("User not found.")
            val settings = dao.getSystemSettings() ?: SystemSettingsEntity()

            val newStatus = if (approve) "VERIFIED" else "REJECTED"
            dao.updatePaymentVerification(
                pv.copy(
                    status = newStatus,
                    reviewedByAdminId = admin.id,
                    reviewNote = note.trim(),
                    reviewedAt = System.currentTimeMillis()
                )
            )

            if (approve) {
                val wallet = dao.getWallet(pv.userId) ?: WalletEntity(userId = pv.userId)
                when (pv.paymentType) {
                    "ACTIVATION" -> {
                        dao.updateUser(user.copy(accountStatus = "ACTIVE", updatedAt = System.currentTimeMillis()))
                        val bonus = settings.welcomeBonusRwf
                        dao.upsertWallet(
                            wallet.copy(
                                availableBalanceRwf = wallet.availableBalanceRwf + bonus,
                                welcomeBonusRwf = wallet.welcomeBonusRwf + bonus,
                                totalEarningsRwf = wallet.totalEarningsRwf + bonus,
                                updatedAt = System.currentTimeMillis()
                            )
                        )
                        dao.insertTransaction(
                            FinancialTransactionEntity(
                                id = SecurityUtils.generateId("TXN"),
                                userId = pv.userId,
                                category = "ACCOUNT_ACTIVATION",
                                amountRwf = pv.amountRwf,
                                feeRwf = 0L,
                                netAmountRwf = pv.amountRwf,
                                description = "Verified account activation payment (${pv.submittedTransactionId})",
                                paymentReference = pv.submittedTransactionId,
                                status = "COMPLETED"
                            )
                        )
                        if (bonus > 0) {
                            dao.insertTransaction(
                                FinancialTransactionEntity(
                                    id = SecurityUtils.generateId("TXN"),
                                    userId = pv.userId,
                                    category = "WELCOME_BONUS",
                                    amountRwf = bonus,
                                    feeRwf = 0L,
                                    netAmountRwf = bonus,
                                    description = "Account Activation Welcome Bonus credited separately",
                                    paymentReference = pv.id,
                                    status = "COMPLETED"
                                )
                            )
                        }
                    }
                    "DEPOSIT" -> {
                        dao.upsertWallet(
                            wallet.copy(
                                availableBalanceRwf = wallet.availableBalanceRwf + pv.amountRwf,
                                totalDepositsRwf = wallet.totalDepositsRwf + pv.amountRwf,
                                updatedAt = System.currentTimeMillis()
                            )
                        )
                        dao.insertTransaction(
                            FinancialTransactionEntity(
                                id = SecurityUtils.generateId("TXN"),
                                userId = pv.userId,
                                category = "DEPOSIT",
                                amountRwf = pv.amountRwf,
                                feeRwf = 0L,
                                netAmountRwf = pv.amountRwf,
                                description = "Verified Mobile Money deposit (${pv.submittedTransactionId})",
                                paymentReference = pv.submittedTransactionId,
                                status = "COMPLETED"
                            )
                        )
                    }
                    "VIDEO_CAMPAIGN" -> {
                        dao.getVideoById(pv.relatedEntityId)?.let { video ->
                            dao.upsertVideoCampaign(video.copy(status = "PENDING_APPROVAL"))
                        }
                        dao.insertTransaction(
                            FinancialTransactionEntity(
                                id = SecurityUtils.generateId("TXN"),
                                userId = pv.userId,
                                category = "VIDEO_CAMPAIGN_PAYMENT",
                                amountRwf = pv.amountRwf,
                                feeRwf = 0L,
                                netAmountRwf = pv.amountRwf,
                                description = "Verified video campaign funding (${pv.submittedTransactionId})",
                                paymentReference = pv.submittedTransactionId,
                                status = "COMPLETED"
                            )
                        )
                    }
                    "TASK_CAMPAIGN" -> {
                        dao.getTaskById(pv.relatedEntityId)?.let { task ->
                            dao.upsertPaidTask(task.copy(status = "PENDING_APPROVAL"))
                        }
                        dao.insertTransaction(
                            FinancialTransactionEntity(
                                id = SecurityUtils.generateId("TXN"),
                                userId = pv.userId,
                                category = "TASK_CAMPAIGN_PAYMENT",
                                amountRwf = pv.amountRwf,
                                feeRwf = 0L,
                                netAmountRwf = pv.amountRwf,
                                description = "Verified paid task campaign funding (${pv.submittedTransactionId})",
                                paymentReference = pv.submittedTransactionId,
                                status = "COMPLETED"
                            )
                        )
                    }
                }
                dao.insertNotification(
                    NotificationEntity(
                        id = SecurityUtils.generateId("NTF"),
                        userId = pv.userId,
                        title = "Payment Verified (${pv.paymentType})",
                        message = "Your payment of ${SecurityUtils.formatRwf(pv.amountRwf)} (${pv.submittedTransactionId}) has been verified.",
                        category = "PAYMENT"
                    )
                )
            } else {
                if (pv.paymentType == "VIDEO_CAMPAIGN") {
                    dao.getVideoById(pv.relatedEntityId)?.let { v ->
                        dao.upsertVideoCampaign(v.copy(status = "PENDING_PAYMENT"))
                    }
                }
                if (pv.paymentType == "TASK_CAMPAIGN") {
                    dao.getTaskById(pv.relatedEntityId)?.let { t ->
                        dao.upsertPaidTask(t.copy(status = "PENDING_PAYMENT"))
                    }
                }
                dao.insertNotification(
                    NotificationEntity(
                        id = SecurityUtils.generateId("NTF"),
                        userId = pv.userId,
                        title = "Payment Verification Rejected",
                        message = "Payment ${pv.submittedTransactionId} was rejected: ${note.ifBlank { "Invalid reference" }}",
                        category = "PAYMENT"
                    )
                )
            }

            dao.insertAuditLog(
                AuditLogEntity(
                    id = SecurityUtils.generateId("AUD"),
                    actorUserId = admin.id,
                    actorEmail = admin.email,
                    action = "PAYMENT_$newStatus",
                    targetEntity = pv.id,
                    details = "${pv.paymentType} ${pv.amountRwf} RWF (${pv.submittedTransactionId}) -> $newStatus"
                )
            )
        }
    }

    suspend fun adminReviewKyc(
        adminId: String,
        kycId: String,
        newStatus: String, // APPROVED, REJECTED, ADDITIONAL_INFO_REQUIRED, UNDER_REVIEW
        reason: String
    ): Result<Unit> = runCatching {
        db.withTransaction {
            val admin = requireAdmin(adminId)
            val kyc = dao.getKycById(kycId) ?: error("KYC record not found.")
            val user = dao.getUserById(kyc.userId) ?: error("User not found.")
            dao.upsertKyc(
                kyc.copy(
                    status = newStatus,
                    rejectionReason = reason.trim(),
                    reviewedBy = admin.email,
                    reviewedAt = System.currentTimeMillis()
                )
            )
            dao.updateUser(user.copy(kycStatus = newStatus, updatedAt = System.currentTimeMillis()))
            dao.insertNotification(
                NotificationEntity(
                    id = SecurityUtils.generateId("NTF"),
                    userId = user.id,
                    title = "KYC Status: $newStatus",
                    message = if (newStatus == "APPROVED") {
                        "Your KYC identity verification is approved. Savings, withdrawals, and lending are now unlocked."
                    } else {
                        "KYC update ($newStatus): ${reason.ifBlank { "Check KYC details." }}"
                    },
                    category = "KYC"
                )
            )
            dao.insertAuditLog(
                AuditLogEntity(
                    id = SecurityUtils.generateId("AUD"),
                    actorUserId = admin.id,
                    actorEmail = admin.email,
                    action = "KYC_$newStatus",
                    targetEntity = kyc.id,
                    details = "KYC for ${user.email} -> $newStatus. Note: $reason"
                )
            )
        }
    }

    suspend fun adminProcessWithdrawal(
        adminId: String,
        withdrawalId: String,
        newStatus: String, // UNDER_REVIEW, APPROVED, PROCESSING, COMPLETED, REJECTED
        note: String
    ): Result<Unit> = runCatching {
        db.withTransaction {
            val admin = requireAdmin(adminId)
            val w = dao.getWithdrawalById(withdrawalId) ?: error("Withdrawal not found.")
            require(w.status != "COMPLETED" && w.status != "REJECTED" && w.status != "CANCELLED") {
                "Finalized withdrawals cannot be modified."
            }
            val wallet = dao.getWallet(w.userId) ?: error("Wallet not found.")

            dao.updateWithdrawal(
                w.copy(
                    status = newStatus,
                    rejectionReason = note.trim(),
                    processedAt = System.currentTimeMillis()
                )
            )

            when (newStatus) {
                "COMPLETED" -> {
                    dao.upsertWallet(
                        wallet.copy(
                            pendingWithdrawalsRwf = (wallet.pendingWithdrawalsRwf - w.requestedAmountRwf).coerceAtLeast(0L),
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                    dao.insertTransaction(
                        FinancialTransactionEntity(
                            id = SecurityUtils.generateId("TXN"),
                            userId = w.userId,
                            category = "WITHDRAWAL",
                            amountRwf = w.requestedAmountRwf,
                            feeRwf = w.feeAmountRwf,
                            netAmountRwf = w.netAmountRwf,
                            description = "Paid out ${SecurityUtils.formatRwf(w.netAmountRwf)} to ${w.provider} ${w.destinationPhone}",
                            paymentReference = w.reference,
                            status = "COMPLETED"
                        )
                    )
                    if (w.feeAmountRwf > 0) {
                        dao.insertTransaction(
                            FinancialTransactionEntity(
                                id = SecurityUtils.generateId("TXN"),
                                userId = w.userId,
                                category = "WITHDRAWAL_FEE",
                                amountRwf = w.feeAmountRwf,
                                feeRwf = 0L,
                                netAmountRwf = w.feeAmountRwf,
                                description = "Platform withdrawal fee (${w.feePercentage}%) for ${w.reference}",
                                paymentReference = w.reference,
                                status = "COMPLETED"
                            )
                        )
                    }
                }
                "REJECTED", "CANCELLED" -> {
                    dao.upsertWallet(
                        wallet.copy(
                            availableBalanceRwf = wallet.availableBalanceRwf + w.requestedAmountRwf,
                            pendingWithdrawalsRwf = (wallet.pendingWithdrawalsRwf - w.requestedAmountRwf).coerceAtLeast(0L),
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                    dao.insertTransaction(
                        FinancialTransactionEntity(
                            id = SecurityUtils.generateId("TXN"),
                            userId = w.userId,
                            category = "REFUND",
                            amountRwf = w.requestedAmountRwf,
                            feeRwf = 0L,
                            netAmountRwf = w.requestedAmountRwf,
                            description = "Refund for $newStatus withdrawal ${w.reference}: $note",
                            paymentReference = w.reference,
                            status = "COMPLETED"
                        )
                    )
                }
            }

            dao.insertNotification(
                NotificationEntity(
                    id = SecurityUtils.generateId("NTF"),
                    userId = w.userId,
                    title = "Withdrawal $newStatus",
                    message = "Withdrawal ${w.reference} (${SecurityUtils.formatRwf(w.requestedAmountRwf)}) is now $newStatus. ${note}",
                    category = "WITHDRAWAL"
                )
            )
            dao.insertAuditLog(
                AuditLogEntity(
                    id = SecurityUtils.generateId("AUD"),
                    actorUserId = admin.id,
                    actorEmail = admin.email,
                    action = "WITHDRAWAL_$newStatus",
                    targetEntity = w.id,
                    details = "Withdrawal ${w.reference} -> $newStatus. Note: $note"
                )
            )
        }
    }

    suspend fun adminReviewLoan(
        adminId: String,
        loanId: String,
        action: String, // DISBURSED, REJECTED, UNDER_REVIEW, OVERDUE
        note: String
    ): Result<Unit> = runCatching {
        db.withTransaction {
            val admin = requireAdmin(adminId)
            val loan = dao.getLoanById(loanId) ?: error("Loan not found.")
            val wallet = dao.getWallet(loan.userId) ?: error("Wallet not found.")
            val now = System.currentTimeMillis()

            if (action == "DISBURSED") {
                require(loan.status == "PENDING" || loan.status == "UNDER_REVIEW" || loan.status == "APPROVED") {
                    "Loan already disbursed or closed."
                }
                dao.updateLoanApplication(
                    loan.copy(
                        status = "DISBURSED",
                        adminNote = note.trim(),
                        approvedAt = now,
                        nextDueDateMillis = now + 30L * 24L * 60L * 60L * 1000L
                    )
                )
                dao.upsertWallet(
                    wallet.copy(
                        availableBalanceRwf = wallet.availableBalanceRwf + loan.requestedAmountRwf,
                        loanBalanceRwf = wallet.loanBalanceRwf + loan.totalRepayableRwf,
                        updatedAt = now
                    )
                )
                dao.insertTransaction(
                    FinancialTransactionEntity(
                        id = SecurityUtils.generateId("TXN"),
                        userId = loan.userId,
                        category = "LOAN_DISBURSEMENT",
                        amountRwf = loan.requestedAmountRwf,
                        feeRwf = 0L,
                        netAmountRwf = loan.requestedAmountRwf,
                        description = "Loan disbursed: ${loan.productName} (${loan.repaymentMonths} months)",
                        paymentReference = loan.id,
                        status = "COMPLETED"
                    )
                )
            } else {
                dao.updateLoanApplication(loan.copy(status = action, adminNote = note.trim()))
            }

            dao.insertNotification(
                NotificationEntity(
                    id = SecurityUtils.generateId("NTF"),
                    userId = loan.userId,
                    title = "Loan Application: $action",
                    message = "Your loan ${loan.id} (${SecurityUtils.formatRwf(loan.requestedAmountRwf)}) is $action. $note",
                    category = "LOAN"
                )
            )
            dao.insertAuditLog(
                AuditLogEntity(
                    id = SecurityUtils.generateId("AUD"),
                    actorUserId = admin.id,
                    actorEmail = admin.email,
                    action = "LOAN_$action",
                    targetEntity = loan.id,
                    details = "Loan ${loan.id} -> $action. Note: $note"
                )
            )
        }
    }

    suspend fun adminUpdateVideoStatus(adminId: String, videoId: String, newStatus: String): Result<Unit> = runCatching {
        db.withTransaction {
            val admin = requireAdmin(adminId)
            val video = dao.getVideoById(videoId) ?: error("Video not found.")
            dao.upsertVideoCampaign(video.copy(status = newStatus))
            dao.insertNotification(
                NotificationEntity(
                    id = SecurityUtils.generateId("NTF"),
                    userId = video.creatorUserId,
                    title = "Video Campaign $newStatus",
                    message = "Your video campaign \"${video.title}\" status is now $newStatus.",
                    category = "VIDEO"
                )
            )
            dao.insertAuditLog(
                AuditLogEntity(
                    id = SecurityUtils.generateId("AUD"),
                    actorUserId = admin.id,
                    actorEmail = admin.email,
                    action = "VIDEO_$newStatus",
                    targetEntity = videoId,
                    details = "Video campaign status updated to $newStatus."
                )
            )
        }
    }

    suspend fun adminUpdateTaskStatus(adminId: String, taskId: String, newStatus: String): Result<Unit> = runCatching {
        db.withTransaction {
            val admin = requireAdmin(adminId)
            val task = dao.getTaskById(taskId) ?: error("Task not found.")
            dao.upsertPaidTask(task.copy(status = newStatus))
            dao.insertNotification(
                NotificationEntity(
                    id = SecurityUtils.generateId("NTF"),
                    userId = task.creatorUserId,
                    title = "Paid Task $newStatus",
                    message = "Your task \"${task.title}\" status is now $newStatus.",
                    category = "TASK"
                )
            )
            dao.insertAuditLog(
                AuditLogEntity(
                    id = SecurityUtils.generateId("AUD"),
                    actorUserId = admin.id,
                    actorEmail = admin.email,
                    action = "TASK_$newStatus",
                    targetEntity = taskId,
                    details = "Task status updated to $newStatus."
                )
            )
        }
    }

    suspend fun adminReviewTaskSubmission(
        adminId: String,
        submissionId: String,
        approve: Boolean,
        reason: String
    ): Result<Unit> = runCatching {
        db.withTransaction {
            val admin = requireAdmin(adminId)
            val sub = dao.getTaskSubmissionById(submissionId) ?: error("Submission not found.")
            require(sub.status == "PENDING_REVIEW") { "Submission already reviewed." }
            val task = dao.getTaskById(sub.taskId) ?: error("Task not found.")

            val status = if (approve) "APPROVED" else "REJECTED"
            dao.updateTaskSubmission(
                sub.copy(
                    status = status,
                    rejectionReason = reason.trim(),
                    reviewedAt = System.currentTimeMillis(),
                    reviewedBy = admin.email
                )
            )

            if (approve) {
                require(task.approvedParticipants < task.targetParticipants && task.spentBudgetRwf + task.rewardPerParticipantRwf <= task.totalBudgetRwf) {
                    "Task campaign budget exhausted."
                }
                val reward = task.rewardPerParticipantRwf
                val newApproved = task.approvedParticipants + 1
                val newSpent = task.spentBudgetRwf + reward
                dao.upsertPaidTask(
                    task.copy(
                        approvedParticipants = newApproved,
                        spentBudgetRwf = newSpent,
                        status = if (newApproved >= task.targetParticipants) "COMPLETED" else task.status
                    )
                )
                val wallet = dao.getWallet(sub.userId) ?: WalletEntity(userId = sub.userId)
                dao.upsertWallet(
                    wallet.copy(
                        availableBalanceRwf = wallet.availableBalanceRwf + reward,
                        taskEarningsRwf = wallet.taskEarningsRwf + reward,
                        totalEarningsRwf = wallet.totalEarningsRwf + reward,
                        updatedAt = System.currentTimeMillis()
                    )
                )
                dao.insertTransaction(
                    FinancialTransactionEntity(
                        id = SecurityUtils.generateId("TXN"),
                        userId = sub.userId,
                        category = "TASK_REWARD",
                        amountRwf = reward,
                        feeRwf = 0L,
                        netAmountRwf = reward,
                        description = "Approved task reward: \"${task.title}\"",
                        paymentReference = sub.id,
                        status = "COMPLETED"
                    )
                )
            }

            dao.insertNotification(
                NotificationEntity(
                    id = SecurityUtils.generateId("NTF"),
                    userId = sub.userId,
                    title = "Task Submission $status",
                    message = if (approve) {
                        "Your submission for \"${task.title}\" was approved (+${SecurityUtils.formatRwf(task.rewardPerParticipantRwf)})."
                    } else {
                        "Your submission for \"${task.title}\" was rejected: ${reason.ifBlank { "Proof incomplete" }}"
                    },
                    category = "TASK"
                )
            )
            dao.insertAuditLog(
                AuditLogEntity(
                    id = SecurityUtils.generateId("AUD"),
                    actorUserId = admin.id,
                    actorEmail = admin.email,
                    action = "TASK_SUBMISSION_$status",
                    targetEntity = sub.id,
                    details = "Submission ${sub.id} for task ${task.id} -> $status. $reason"
                )
            )
        }
    }

    suspend fun adminAdjustUserBalance(
        adminId: String,
        targetUserId: String,
        deltaRwf: Long,
        reason: String
    ): Result<Unit> = runCatching {
        require(deltaRwf != 0L) { "Adjustment cannot be zero." }
        require(reason.isNotBlank()) { "An audit reason is required for manual balance adjustments." }
        db.withTransaction {
            val admin = requireAdmin(adminId)
            val wallet = dao.getWallet(targetUserId) ?: error("Wallet not found.")
            val newBalance = wallet.availableBalanceRwf + deltaRwf
            require(newBalance >= 0L) { "Adjustment would result in a negative balance." }

            dao.upsertWallet(
                wallet.copy(
                    availableBalanceRwf = newBalance,
                    updatedAt = System.currentTimeMillis()
                )
            )
            dao.insertTransaction(
                FinancialTransactionEntity(
                    id = SecurityUtils.generateId("TXN"),
                    userId = targetUserId,
                    category = "ADJUSTMENT",
                    amountRwf = kotlin.math.abs(deltaRwf),
                    feeRwf = 0L,
                    netAmountRwf = deltaRwf,
                    description = "Admin Adjustment by ${admin.email}: $reason",
                    paymentReference = SecurityUtils.generateId("ADJ"),
                    status = "COMPLETED"
                )
            )
            dao.insertNotification(
                NotificationEntity(
                    id = SecurityUtils.generateId("NTF"),
                    userId = targetUserId,
                    title = "Wallet Adjustment",
                    message = "${if (deltaRwf > 0) "+" else ""}${SecurityUtils.formatRwf(deltaRwf)} adjusted by administration: $reason",
                    category = "FINANCE"
                )
            )
            dao.insertAuditLog(
                AuditLogEntity(
                    id = SecurityUtils.generateId("AUD"),
                    actorUserId = admin.id,
                    actorEmail = admin.email,
                    action = "MANUAL_BALANCE_ADJUSTMENT",
                    targetEntity = targetUserId,
                    details = "Adjusted wallet by $deltaRwf RWF. Reason: $reason"
                )
            )
        }
    }

    suspend fun adminSaveAdvertisement(adminId: String, ad: AdvertisementEntity): Result<Unit> = runCatching {
        db.withTransaction {
            val admin = requireAdmin(adminId)
            dao.upsertAdvertisement(ad)
            dao.insertAuditLog(
                AuditLogEntity(
                    id = SecurityUtils.generateId("AUD"),
                    actorUserId = admin.id,
                    actorEmail = admin.email,
                    action = "AD_UPSERT",
                    targetEntity = ad.id,
                    details = "Saved advertisement '${ad.title}' (Active=${ad.isActive})."
                )
            )
        }
    }

    suspend fun adminDeleteAdvertisement(adminId: String, adId: String): Result<Unit> = runCatching {
        db.withTransaction {
            val admin = requireAdmin(adminId)
            dao.deleteAdvertisementById(adId)
            dao.insertAuditLog(
                AuditLogEntity(
                    id = SecurityUtils.generateId("AUD"),
                    actorUserId = admin.id,
                    actorEmail = admin.email,
                    action = "AD_DELETE",
                    targetEntity = adId,
                    details = "Deleted advertisement $adId."
                )
            )
        }
    }

    suspend fun adminSaveSystemSettings(adminId: String, settings: SystemSettingsEntity): Result<Unit> = runCatching {
        db.withTransaction {
            val admin = requireAdmin(adminId)
            dao.upsertSystemSettings(settings.copy(id = 1))
            dao.insertAuditLog(
                AuditLogEntity(
                    id = SecurityUtils.generateId("AUD"),
                    actorUserId = admin.id,
                    actorEmail = admin.email,
                    action = "SYSTEM_SETTINGS_UPDATED",
                    targetEntity = "system_settings",
                    details = "Updated platform configuration (PayNo=${settings.paymentNumber}, Fee=${settings.withdrawalFeePercent}%, CheckIn=${settings.dailyCheckinRewardRwf})."
                )
            )
        }
    }

    suspend fun adminReplySupportTicket(
        adminId: String,
        ticketId: String,
        reply: String,
        status: String,
        assignedAgent: String
    ): Result<Unit> = runCatching {
        db.withTransaction {
            val admin = requireAdmin(adminId)
            val ticket = dao.getTicketById(ticketId) ?: error("Ticket not found.")
            dao.upsertSupportTicket(
                ticket.copy(
                    adminReply = reply.trim(),
                    status = status,
                    assignedAgent = assignedAgent.ifBlank { admin.email },
                    updatedAt = System.currentTimeMillis()
                )
            )
            dao.insertNotification(
                NotificationEntity(
                    id = SecurityUtils.generateId("NTF"),
                    userId = ticket.userId,
                    title = "Support Ticket Updated ($status)",
                    message = "Admin reply on '${ticket.subject}': ${reply.trim()}",
                    category = "SUPPORT"
                )
            )
            dao.insertAuditLog(
                AuditLogEntity(
                    id = SecurityUtils.generateId("AUD"),
                    actorUserId = admin.id,
                    actorEmail = admin.email,
                    action = "TICKET_REPLY",
                    targetEntity = ticketId,
                    details = "Updated ticket $ticketId to $status."
                )
            )
        }
    }

    suspend fun adminSaveLoanProduct(adminId: String, product: LoanProductEntity): Result<Unit> = runCatching {
        require(product.name.isNotBlank()) { "Product name is required." }
        require(product.minAmountRwf in 1000L..product.maxAmountRwf) { "Check min/max lending limits." }
        db.withTransaction {
            val admin = requireAdmin(adminId)
            dao.upsertLoanProduct(product)
            dao.insertAuditLog(
                AuditLogEntity(
                    id = SecurityUtils.generateId("AUD"),
                    actorUserId = admin.id,
                    actorEmail = admin.email,
                    action = "LOAN_PRODUCT_SAVED",
                    targetEntity = product.id,
                    details = "Saved loan product '${product.name}' (${product.interestRatePercent}%, max ${product.maxAmountRwf} RWF)."
                )
            )
        }
    }
}
