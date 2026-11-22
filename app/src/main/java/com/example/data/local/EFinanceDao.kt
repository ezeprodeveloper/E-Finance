package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EFinanceDao {
    // --- Users ---
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun observeUserById(userId: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: String): UserEntity?

    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    suspend fun getUserByPhone(phone: String): UserEntity?

    @Query("SELECT * FROM users WHERE nationalIdNumber = :nid LIMIT 1")
    suspend fun getUserByNationalId(nid: String): UserEntity?

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun observeAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE role = 'SUPER_ADMIN'")
    suspend fun getAdminUsers(): List<UserEntity>

    // --- Wallets ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWallet(wallet: WalletEntity)

    @Query("SELECT * FROM wallets WHERE userId = :userId LIMIT 1")
    fun observeWallet(userId: String): Flow<WalletEntity?>

    @Query("SELECT * FROM wallets WHERE userId = :userId LIMIT 1")
    suspend fun getWallet(userId: String): WalletEntity?

    @Query("SELECT * FROM wallets")
    fun observeAllWallets(): Flow<List<WalletEntity>>

    // --- Transactions (Immutable Ledger) ---
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTransaction(tx: FinancialTransactionEntity)

    @Query("SELECT * FROM financial_transactions WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeTransactionsForUser(userId: String): Flow<List<FinancialTransactionEntity>>

    @Query("SELECT * FROM financial_transactions ORDER BY createdAt DESC")
    fun observeAllTransactions(): Flow<List<FinancialTransactionEntity>>

    // --- Payment Verifications ---
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPaymentVerification(pv: PaymentVerificationEntity)

    @Update
    suspend fun updatePaymentVerification(pv: PaymentVerificationEntity)

    @Query("SELECT * FROM payment_verifications WHERE userId = :userId ORDER BY createdAt DESC")
    fun observePaymentsForUser(userId: String): Flow<List<PaymentVerificationEntity>>

    @Query("SELECT * FROM payment_verifications ORDER BY createdAt DESC")
    fun observeAllPaymentVerifications(): Flow<List<PaymentVerificationEntity>>

    @Query("SELECT * FROM payment_verifications WHERE id = :id LIMIT 1")
    suspend fun getPaymentVerificationById(id: String): PaymentVerificationEntity?

    // --- KYC ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertKyc(kyc: KycApplicationEntity)

    @Query("SELECT * FROM kyc_applications WHERE userId = :userId LIMIT 1")
    fun observeKycForUser(userId: String): Flow<KycApplicationEntity?>

    @Query("SELECT * FROM kyc_applications WHERE userId = :userId LIMIT 1")
    suspend fun getKycForUser(userId: String): KycApplicationEntity?

    @Query("SELECT * FROM kyc_applications ORDER BY submittedAt DESC")
    fun observeAllKyc(): Flow<List<KycApplicationEntity>>

    @Query("SELECT * FROM kyc_applications WHERE id = :id LIMIT 1")
    suspend fun getKycById(id: String): KycApplicationEntity?

    // --- Savings ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSavingsAccount(account: SavingsAccountEntity)

    @Query("SELECT * FROM savings_accounts WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeSavingsForUser(userId: String): Flow<List<SavingsAccountEntity>>

    @Query("SELECT * FROM savings_accounts WHERE id = :id LIMIT 1")
    suspend fun getSavingsAccountById(id: String): SavingsAccountEntity?

    @Query("SELECT * FROM savings_accounts ORDER BY createdAt DESC")
    fun observeAllSavings(): Flow<List<SavingsAccountEntity>>

    // --- Withdrawals ---
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertWithdrawal(w: WithdrawalRequestEntity)

    @Update
    suspend fun updateWithdrawal(w: WithdrawalRequestEntity)

    @Query("SELECT * FROM withdrawal_requests WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeWithdrawalsForUser(userId: String): Flow<List<WithdrawalRequestEntity>>

    @Query("SELECT * FROM withdrawal_requests ORDER BY createdAt DESC")
    fun observeAllWithdrawals(): Flow<List<WithdrawalRequestEntity>>

    @Query("SELECT * FROM withdrawal_requests WHERE id = :id LIMIT 1")
    suspend fun getWithdrawalById(id: String): WithdrawalRequestEntity?

    // --- Loans ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLoanProduct(product: LoanProductEntity)

    @Query("SELECT * FROM loan_products ORDER BY minAmountRwf ASC")
    fun observeLoanProducts(): Flow<List<LoanProductEntity>>

    @Query("SELECT * FROM loan_products WHERE id = :id LIMIT 1")
    suspend fun getLoanProductById(id: String): LoanProductEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertLoanApplication(loan: LoanApplicationEntity)

    @Update
    suspend fun updateLoanApplication(loan: LoanApplicationEntity)

    @Query("SELECT * FROM loan_applications WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeLoansForUser(userId: String): Flow<List<LoanApplicationEntity>>

    @Query("SELECT * FROM loan_applications ORDER BY createdAt DESC")
    fun observeAllLoans(): Flow<List<LoanApplicationEntity>>

    @Query("SELECT * FROM loan_applications WHERE id = :id LIMIT 1")
    suspend fun getLoanById(id: String): LoanApplicationEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertLoanRepayment(repayment: LoanRepaymentEntity)

    @Query("SELECT * FROM loan_repayments WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeLoanRepaymentsForUser(userId: String): Flow<List<LoanRepaymentEntity>>

    // --- Videos & Video Views ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertVideoCampaign(video: VideoCampaignEntity)

    @Query("SELECT * FROM video_campaigns ORDER BY createdAt DESC")
    fun observeAllVideos(): Flow<List<VideoCampaignEntity>>

    @Query("SELECT * FROM video_campaigns WHERE status = 'ACTIVE' AND validViews < targetViews ORDER BY createdAt DESC")
    fun observeActiveVideos(): Flow<List<VideoCampaignEntity>>

    @Query("SELECT * FROM video_campaigns WHERE creatorUserId = :userId ORDER BY createdAt DESC")
    fun observeUserVideoCampaigns(userId: String): Flow<List<VideoCampaignEntity>>

    @Query("SELECT * FROM video_campaigns WHERE id = :id LIMIT 1")
    suspend fun getVideoById(id: String): VideoCampaignEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertVideoView(view: VideoViewEntity)

    @Query("SELECT * FROM video_views WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeUserVideoViews(userId: String): Flow<List<VideoViewEntity>>

    @Query("SELECT * FROM video_views WHERE userId = :userId AND videoId = :videoId LIMIT 1")
    suspend fun getVideoView(userId: String, videoId: String): VideoViewEntity?

    @Query("SELECT * FROM video_views ORDER BY createdAt DESC")
    fun observeAllVideoViews(): Flow<List<VideoViewEntity>>

    // --- Paid Tasks & Submissions ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPaidTask(task: PaidTaskEntity)

    @Query("SELECT * FROM paid_tasks ORDER BY createdAt DESC")
    fun observeAllTasks(): Flow<List<PaidTaskEntity>>

    @Query("SELECT * FROM paid_tasks WHERE status = 'ACTIVE' AND approvedParticipants < targetParticipants ORDER BY createdAt DESC")
    fun observeActiveTasks(): Flow<List<PaidTaskEntity>>

    @Query("SELECT * FROM paid_tasks WHERE creatorUserId = :userId ORDER BY createdAt DESC")
    fun observeUserCreatedTasks(userId: String): Flow<List<PaidTaskEntity>>

    @Query("SELECT * FROM paid_tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: String): PaidTaskEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTaskSubmission(submission: TaskSubmissionEntity)

    @Update
    suspend fun updateTaskSubmission(submission: TaskSubmissionEntity)

    @Query("SELECT * FROM task_submissions WHERE userId = :userId ORDER BY submittedAt DESC")
    fun observeUserTaskSubmissions(userId: String): Flow<List<TaskSubmissionEntity>>

    @Query("SELECT * FROM task_submissions ORDER BY submittedAt DESC")
    fun observeAllTaskSubmissions(): Flow<List<TaskSubmissionEntity>>

    @Query("SELECT * FROM task_submissions WHERE userId = :userId AND taskId = :taskId LIMIT 1")
    suspend fun getTaskSubmission(userId: String, taskId: String): TaskSubmissionEntity?

    @Query("SELECT * FROM task_submissions WHERE id = :id LIMIT 1")
    suspend fun getTaskSubmissionById(id: String): TaskSubmissionEntity?

    // --- Daily Check-ins ---
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertDailyCheckin(checkin: DailyCheckinEntity)

    @Query("SELECT * FROM daily_checkins WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeCheckinsForUser(userId: String): Flow<List<DailyCheckinEntity>>

    @Query("SELECT * FROM daily_checkins WHERE userId = :userId ORDER BY createdAt DESC")
    suspend fun getCheckinsForUser(userId: String): List<DailyCheckinEntity>

    @Query("SELECT * FROM daily_checkins WHERE userId = :userId AND kigaliDate = :dateKey LIMIT 1")
    suspend fun getCheckinForDate(userId: String, dateKey: String): DailyCheckinEntity?

    // --- Advertisements ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAdvertisement(ad: AdvertisementEntity)

    @Query("SELECT * FROM advertisements WHERE isActive = 1 ORDER BY createdAt DESC")
    fun observeActiveAdvertisements(): Flow<List<AdvertisementEntity>>

    @Query("SELECT * FROM advertisements ORDER BY createdAt DESC")
    fun observeAllAdvertisements(): Flow<List<AdvertisementEntity>>

    @Query("DELETE FROM advertisements WHERE id = :id")
    suspend fun deleteAdvertisementById(id: String)

    // --- Notifications ---
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeNotificationsForUser(userId: String): Flow<List<NotificationEntity>>

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllNotificationsRead(userId: String)

    // --- Support Tickets ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSupportTicket(ticket: SupportTicketEntity)

    @Query("SELECT * FROM support_tickets WHERE userId = :userId ORDER BY updatedAt DESC")
    fun observeTicketsForUser(userId: String): Flow<List<SupportTicketEntity>>

    @Query("SELECT * FROM support_tickets ORDER BY updatedAt DESC")
    fun observeAllTickets(): Flow<List<SupportTicketEntity>>

    @Query("SELECT * FROM support_tickets WHERE id = :id LIMIT 1")
    suspend fun getTicketById(id: String): SupportTicketEntity?

    // --- Audit Logs ---
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAuditLog(log: AuditLogEntity)

    @Query("SELECT * FROM audit_logs ORDER BY createdAt DESC")
    fun observeAuditLogs(): Flow<List<AuditLogEntity>>

    // --- System Settings ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSystemSettings(settings: SystemSettingsEntity)

    @Query("SELECT * FROM system_settings WHERE id = 1 LIMIT 1")
    fun observeSystemSettings(): Flow<SystemSettingsEntity?>

    @Query("SELECT * FROM system_settings WHERE id = 1 LIMIT 1")
    suspend fun getSystemSettings(): SystemSettingsEntity?
}
