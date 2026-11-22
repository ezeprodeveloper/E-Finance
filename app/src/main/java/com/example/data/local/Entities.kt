package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [
        Index(value = ["email"], unique = true),
        Index(value = ["phone"], unique = true),
        Index(value = ["nationalIdNumber"], unique = true)
    ]
)
data class UserEntity(
    @PrimaryKey val id: String,
    val firstName: String,
    val secondName: String,
    val email: String,
    val phone: String,
    val gender: String,
    val dateOfBirth: String,
    val nationalIdNumber: String,
    val nationalIdFrontUri: String,
    val profilePhotoUri: String = "",
    // Step 2: Educational Background
    val educationCategory: String, // UNIVERSITY or A_LEVEL
    val institutionName: String,
    val academicLevelOrClass: String,
    val facultyOrCombination: String,
    val programOfStudy: String = "",
    // Step 3: Place of Birth
    val birthCountry: String,
    val birthProvince: String,
    val birthDistrict: String,
    val birthSector: String,
    val birthCell: String,
    val birthVillage: String,
    // Step 3: Current Residence
    val resCountry: String,
    val resProvince: String,
    val resDistrict: String = "",
    val resSector: String = "",
    val resCell: String = "",
    val resVillage: String = "",
    // Auth & Status
    val passwordHash: String,
    val passwordSalt: String,
    val role: String = "USER", // USER, SUPER_ADMIN
    val accountStatus: String = "PENDING", // PENDING, CORRECTION_REQUIRED, APPROVED_AWAITING_ACTIVATION, ACTIVE, REJECTED, SUSPENDED, BLOCKED
    val statusNote: String = "",
    val kycStatus: String = "NOT_SUBMITTED", // NOT_SUBMITTED, PENDING, UNDER_REVIEW, APPROVED, REJECTED, ADDITIONAL_INFO_REQUIRED
    val mustChangePassword: Boolean = false,
    val twoFactorEnabled: Boolean = false,
    val failedLoginAttempts: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "wallets")
data class WalletEntity(
    @PrimaryKey val userId: String,
    val availableBalanceRwf: Long = 0L,
    val savingsBalanceRwf: Long = 0L,
    val loanBalanceRwf: Long = 0L,
    val videoEarningsRwf: Long = 0L,
    val taskEarningsRwf: Long = 0L,
    val checkinRewardsRwf: Long = 0L,
    val welcomeBonusRwf: Long = 0L,
    val pendingWithdrawalsRwf: Long = 0L,
    val totalEarningsRwf: Long = 0L,
    val totalDepositsRwf: Long = 0L,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "financial_transactions",
    indices = [Index(value = ["userId"]), Index(value = ["category"]), Index(value = ["createdAt"])]
)
data class FinancialTransactionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val category: String,
    val amountRwf: Long,
    val feeRwf: Long = 0L,
    val netAmountRwf: Long,
    val description: String,
    val paymentReference: String = "",
    val status: String, // PENDING, UNDER_REVIEW, APPROVED, PROCESSING, COMPLETED, REJECTED, CANCELLED
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "payment_verifications",
    indices = [Index(value = ["userId"]), Index(value = ["status"])]
)
data class PaymentVerificationEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val paymentType: String, // ACTIVATION, DEPOSIT, VIDEO_CAMPAIGN, TASK_CAMPAIGN
    val relatedEntityId: String = "",
    val amountRwf: Long,
    val ussdReference: String,
    val submittedTransactionId: String,
    val status: String = "PENDING", // PENDING, VERIFIED, REJECTED
    val reviewedByAdminId: String = "",
    val reviewNote: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val reviewedAt: Long = 0L
)

@Entity(
    tableName = "kyc_applications",
    indices = [Index(value = ["userId"], unique = true), Index(value = ["status"])]
)
data class KycApplicationEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val fullLegalName: String,
    val nationalIdNumber: String,
    val dateOfBirth: String,
    val gender: String,
    val phone: String,
    val email: String,
    val province: String,
    val district: String,
    val sector: String,
    val cell: String,
    val village: String,
    val idFrontUri: String,
    val idBackUri: String = "",
    val selfieUri: String = "",
    val additionalNotes: String = "",
    val status: String = "PENDING", // PENDING, UNDER_REVIEW, APPROVED, REJECTED, ADDITIONAL_INFO_REQUIRED
    val rejectionReason: String = "",
    val reviewedBy: String = "",
    val submittedAt: Long = System.currentTimeMillis(),
    val reviewedAt: Long = 0L
)

@Entity(
    tableName = "savings_accounts",
    indices = [Index(value = ["userId"])]
)
data class SavingsAccountEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val planType: String, // FLEXIBLE, GOAL_BASED, LONG_TERM
    val purpose: String,
    val description: String,
    val targetAmountRwf: Long,
    val balanceRwf: Long = 0L,
    val totalDepositedRwf: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "withdrawal_requests",
    indices = [Index(value = ["userId"]), Index(value = ["status"])]
)
data class WithdrawalRequestEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val provider: String, // MTN_MOMO, AIRTEL_MONEY
    val destinationPhone: String,
    val requestedAmountRwf: Long,
    val feePercentage: Double,
    val feeAmountRwf: Long,
    val netAmountRwf: Long,
    val reference: String,
    val status: String = "PENDING", // PENDING, UNDER_REVIEW, APPROVED, PROCESSING, COMPLETED, REJECTED, CANCELLED
    val rejectionReason: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val processedAt: Long = 0L
)

@Entity(tableName = "loan_products")
data class LoanProductEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val minAmountRwf: Long,
    val maxAmountRwf: Long,
    val interestRatePercent: Double,
    val maxDurationMonths: Int,
    val isActive: Boolean = true
)

@Entity(
    tableName = "loan_applications",
    indices = [Index(value = ["userId"]), Index(value = ["status"])]
)
data class LoanApplicationEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val productId: String,
    val productName: String,
    val requestedAmountRwf: Long,
    val repaymentMonths: Int,
    val interestRatePercent: Double,
    val totalRepayableRwf: Long,
    val outstandingBalanceRwf: Long,
    val amountPaidRwf: Long = 0L,
    val purpose: String,
    val supportingInfo: String,
    val status: String = "PENDING", // PENDING, UNDER_REVIEW, APPROVED, DISBURSED, REPAID, REJECTED, OVERDUE
    val adminNote: String = "",
    val nextDueDateMillis: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val approvedAt: Long = 0L
)

@Entity(
    tableName = "loan_repayments",
    indices = [Index(value = ["loanId"]), Index(value = ["userId"])]
)
data class LoanRepaymentEntity(
    @PrimaryKey val id: String,
    val loanId: String,
    val userId: String,
    val amountRwf: Long,
    val reference: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "video_campaigns",
    indices = [Index(value = ["creatorUserId"]), Index(value = ["status"])]
)
data class VideoCampaignEntity(
    @PrimaryKey val id: String,
    val creatorUserId: String,
    val title: String,
    val description: String,
    val category: String,
    val videoUri: String = "",
    val thumbnailUri: String = "",
    val durationSeconds: Int,
    val minWatchSeconds: Int,
    val rewardPerViewRwf: Long,
    val targetViews: Int,
    val validViews: Int = 0,
    val platformFeeRwf: Long,
    val totalBudgetRwf: Long,
    val spentBudgetRwf: Long = 0L,
    val paymentReference: String = "",
    val status: String = "PENDING_PAYMENT", // DRAFT, PENDING_PAYMENT, PAYMENT_VERIFICATION, PENDING_APPROVAL, APPROVED, ACTIVE, PAUSED, COMPLETED, REJECTED
    val startDateMillis: Long = System.currentTimeMillis(),
    val endDateMillis: Long = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000L,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "video_views",
    indices = [Index(value = ["userId", "videoId"], unique = true)]
)
data class VideoViewEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val videoId: String,
    val watchedSeconds: Int,
    val completed: Boolean,
    val rewarded: Boolean,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "paid_tasks",
    indices = [Index(value = ["creatorUserId"]), Index(value = ["status"])]
)
data class PaidTaskEntity(
    @PrimaryKey val id: String,
    val creatorUserId: String,
    val title: String,
    val description: String,
    val category: String,
    val instructions: String,
    val requirements: String,
    val requiredProofType: String,
    val targetParticipants: Int,
    val approvedParticipants: Int = 0,
    val rewardPerParticipantRwf: Long,
    val platformFeeRwf: Long,
    val totalBudgetRwf: Long,
    val spentBudgetRwf: Long = 0L,
    val paymentReference: String = "",
    val status: String = "PENDING_PAYMENT", // PENDING_PAYMENT, PAYMENT_VERIFICATION, PENDING_APPROVAL, ACTIVE, PAUSED, COMPLETED, REJECTED
    val startDateMillis: Long = System.currentTimeMillis(),
    val endDateMillis: Long = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000L,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "task_submissions",
    indices = [Index(value = ["userId", "taskId"], unique = true), Index(value = ["status"])]
)
data class TaskSubmissionEntity(
    @PrimaryKey val id: String,
    val taskId: String,
    val userId: String,
    val proofText: String,
    val proofImageUri: String = "",
    val status: String = "PENDING_REVIEW", // PENDING_REVIEW, APPROVED, REJECTED
    val rejectionReason: String = "",
    val submittedAt: Long = System.currentTimeMillis(),
    val reviewedAt: Long = 0L,
    val reviewedBy: String = ""
)

@Entity(
    tableName = "daily_checkins",
    indices = [Index(value = ["userId", "kigaliDate"], unique = true)]
)
data class DailyCheckinEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val kigaliDate: String, // YYYY-MM-DD in Africa/Kigali
    val rewardRwf: Long,
    val streakCount: Int,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "advertisements")
data class AdvertisementEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val bannerType: String, // BANNER, PROMO_VIDEO, SLIDE
    val displayPosition: String, // HOME_HERO, DASHBOARD_STRIP, PUBLIC_FEED
    val ctaLabel: String = "Explore",
    val accentStyle: String = "NAVY_GOLD", // NAVY_GOLD, ROYAL_CYAN, EMERALD_TEAL, PURPLE_INDIGO
    val mediaUri: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "notifications",
    indices = [Index(value = ["userId"]), Index(value = ["createdAt"])]
)
data class NotificationEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val message: String,
    val category: String,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "support_tickets",
    indices = [Index(value = ["userId"]), Index(value = ["status"])]
)
data class SupportTicketEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val category: String,
    val subject: String,
    val description: String,
    val screenshotUri: String = "",
    val status: String = "OPEN", // OPEN, IN_PROGRESS, RESOLVED, CLOSED
    val adminReply: String = "",
    val assignedAgent: String = "Unassigned",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "audit_logs",
    indices = [Index(value = ["createdAt"])]
)
data class AuditLogEntity(
    @PrimaryKey val id: String,
    val actorUserId: String,
    val actorEmail: String,
    val action: String,
    val targetEntity: String,
    val details: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "system_settings")
data class SystemSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val platformName: String = "E-FINANCE RWANDA",
    val contactEmail: String = "support@efinancerwanda.rw",
    val contactPhone: String = "+250 788 000 100",
    val paymentNumber: String = "182811880554",
    val activationFeeRwf: Long = 200L,
    val welcomeBonusRwf: Long = 200L,
    val withdrawalFeePercent: Double = 25.0,
    val minWithdrawalRwf: Long = 1000L,
    val maxWithdrawalRwf: Long = 500000L,
    val dailyCheckinRewardRwf: Long = 20L,
    val minVideoRewardRwf: Long = 50L,
    val minVideoWatchSeconds: Int = 15,
    val campaignFeePercent: Double = 10.0,
    val defaultLoanInterestPercent: Double = 12.0,
    val maxLoanLimitRwf: Long = 1000000L,
    val currency: String = "RWF",
    val timezone: String = "Africa/Kigali",
    val maintenanceMode: Boolean = false,
    val registrationOpen: Boolean = true,
    val kycRequiredForFinance: Boolean = true,
    val welcomeBonusWithdrawable: Boolean = true,
    val universityListCsv: String = "University of Rwanda (UR)|Carnegie Mellon University Africa|African Leadership University (ALU)|Kepler College|Adventist University of Central Africa (AUCA)|University of Kigali (UoK)|INES-Ruhengeri|ULK|Mount Kigali University|Rwanda Polytechnic",
    val aLevelCombinationsCsv: String = "MPC|PCM|PCB|MCB|MEG|HEG|MCE|LKK|Accounting & Finance|Software Development"
)
