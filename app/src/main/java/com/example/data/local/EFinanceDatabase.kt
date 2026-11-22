package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        WalletEntity::class,
        FinancialTransactionEntity::class,
        PaymentVerificationEntity::class,
        KycApplicationEntity::class,
        SavingsAccountEntity::class,
        WithdrawalRequestEntity::class,
        LoanProductEntity::class,
        LoanApplicationEntity::class,
        LoanRepaymentEntity::class,
        VideoCampaignEntity::class,
        VideoViewEntity::class,
        PaidTaskEntity::class,
        TaskSubmissionEntity::class,
        DailyCheckinEntity::class,
        AdvertisementEntity::class,
        NotificationEntity::class,
        SupportTicketEntity::class,
        AuditLogEntity::class,
        SystemSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class EFinanceDatabase : RoomDatabase() {
    abstract fun dao(): EFinanceDao

    companion object {
        @Volatile
        private var INSTANCE: EFinanceDatabase? = null

        fun getInstance(context: Context): EFinanceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EFinanceDatabase::class.java,
                    "efinance_rwanda.db"
                )
                    .addCallback(SeedCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class SeedCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            val database = INSTANCE ?: return
            CoroutineScope(Dispatchers.IO).launch {
                val dao = database.dao()
                val now = System.currentTimeMillis()

                // 1. Default System Settings
                dao.upsertSystemSettings(SystemSettingsEntity())

                // 2. Seed initial Super Administrator using pre-computed salted SHA-256 hash
                // (No plain-text credential stored in source code; requires password change on first login)
                val adminId = "ADMIN-ROOT-001"
                dao.insertUser(
                    UserEntity(
                        id = adminId,
                        firstName = "Platform",
                        secondName = "Administrator",
                        email = "ezeprodeveloper@gmail.com",
                        phone = "+250788000100",
                        gender = "Prefer not to say",
                        dateOfBirth = "1995-01-01",
                        nationalIdNumber = "1199580000000001",
                        nationalIdFrontUri = "system://protected_admin_doc",
                        educationCategory = "UNIVERSITY",
                        institutionName = "University of Rwanda (UR)",
                        academicLevelOrClass = "Master's / Postgraduate",
                        facultyOrCombination = "Information Technology",
                        programOfStudy = "Financial Systems Engineering",
                        birthCountry = "Rwanda",
                        birthProvince = "Kigali City",
                        birthDistrict = "Gasabo",
                        birthSector = "Kacyiru",
                        birthCell = "Kamatamu",
                        birthVillage = "Nyacyonga",
                        resCountry = "Rwanda",
                        resProvince = "Kigali City",
                        resDistrict = "Gasabo",
                        resSector = "Kimihurura",
                        resCell = "Kamukina",
                        resVillage = "Urugwiro",
                        passwordHash = "55aba5fe9d2e23f28b7510791fdf37b53c7812b4ced2ef4896bea9cd226785df",
                        passwordSalt = "efrw_admin_seed_salt_2026",
                        role = "SUPER_ADMIN",
                        accountStatus = "ACTIVE",
                        kycStatus = "APPROVED",
                        mustChangePassword = true,
                        twoFactorEnabled = true,
                        createdAt = now,
                        updatedAt = now
                    )
                )
                dao.upsertWallet(WalletEntity(userId = adminId))

                // 3. Default Loan Products
                dao.upsertLoanProduct(
                    LoanProductEntity(
                        id = "LP-EDU",
                        name = "Student & Skills Support Loan",
                        description = "Structured micro-credit for university tuition, academic materials, and certification exams.",
                        minAmountRwf = 10000L,
                        maxAmountRwf = 250000L,
                        interestRatePercent = 10.0,
                        maxDurationMonths = 6
                    )
                )
                dao.upsertLoanProduct(
                    LoanProductEntity(
                        id = "LP-BIZ",
                        name = "Kigali Micro-Enterprise Loan",
                        description = "Working capital facility for verified Rwandan entrepreneurs and small merchants.",
                        minAmountRwf = 25000L,
                        maxAmountRwf = 1000000L,
                        interestRatePercent = 12.0,
                        maxDurationMonths = 12
                    )
                )
                dao.upsertLoanProduct(
                    LoanProductEntity(
                        id = "LP-FLEX",
                        name = "Emergency Bridge Facility",
                        description = "Short-term personal liquidity subject to KYC and compliance eligibility review.",
                        minAmountRwf = 5000L,
                        maxAmountRwf = 150000L,
                        interestRatePercent = 8.0,
                        maxDurationMonths = 3
                    )
                )

                // 4. Platform Public Advertisements & Announcements
                dao.upsertAdvertisement(
                    AdvertisementEntity(
                        id = "AD-001",
                        title = "Smart RWF Savings Vaults",
                        description = "Build goal-based and long-term savings in Rwandan Francs with full ledger transparency and instant Kigali tracking.",
                        bannerType = "SLIDE",
                        displayPosition = "HOME_HERO",
                        ctaLabel = "Open Savings Vault",
                        accentStyle = "NAVY_GOLD"
                    )
                )
                dao.upsertAdvertisement(
                    AdvertisementEntity(
                        id = "AD-002",
                        title = "Watch Verified Videos & Earn From 50 RWF",
                        description = "Explore approved financial literacy and creator video campaigns. Complete full-duration views to earn direct wallet rewards.",
                        bannerType = "PROMO_VIDEO",
                        displayPosition = "PUBLIC_FEED",
                        ctaLabel = "Explore Video Hub",
                        accentStyle = "ROYAL_CYAN"
                    )
                )
                dao.upsertAdvertisement(
                    AdvertisementEntity(
                        id = "AD-003",
                        title = "Daily Check-In Streak Rewards",
                        description = "Check in once every Rwanda calendar day (Africa/Kigali) to grow your streak and collect daily RWF bonuses.",
                        bannerType = "BANNER",
                        displayPosition = "DASHBOARD_STRIP",
                        ctaLabel = "Claim Daily Reward",
                        accentStyle = "EMERALD_TEAL"
                    )
                )

                // 5. Initial Approved Platform Educational Video Campaigns (Funded by Platform Treasury)
                dao.upsertVideoCampaign(
                    VideoCampaignEntity(
                        id = "VID-001",
                        creatorUserId = adminId,
                        title = "Financial Discipline & Mobile Money Budgeting in Rwanda",
                        description = "Learn practical methods for dividing monthly income across Flexible, Goal-Based, and Long-Term RWF savings vaults.",
                        category = "Financial Literacy",
                        durationSeconds = 20,
                        minWatchSeconds = 15,
                        rewardPerViewRwf = 50L,
                        targetViews = 500,
                        validViews = 0,
                        platformFeeRwf = 0L,
                        totalBudgetRwf = 25000L,
                        spentBudgetRwf = 0L,
                        paymentReference = "PLATFORM-SEED-001",
                        status = "ACTIVE"
                    )
                )
                dao.upsertVideoCampaign(
                    VideoCampaignEntity(
                        id = "VID-002",
                        creatorUserId = adminId,
                        title = "Understanding KYC, AML & Digital Ledger Security",
                        description = "How Rwanda's Know Your Customer standards protect members, prevent duplicate claims, and safeguard Mobile Money payouts.",
                        category = "Education & Skills",
                        durationSeconds = 18,
                        minWatchSeconds = 15,
                        rewardPerViewRwf = 75L,
                        targetViews = 300,
                        validViews = 0,
                        platformFeeRwf = 0L,
                        totalBudgetRwf = 22500L,
                        spentBudgetRwf = 0L,
                        paymentReference = "PLATFORM-SEED-002",
                        status = "ACTIVE"
                    )
                )

                // 6. Initial Approved Platform Paid Tasks
                dao.upsertPaidTask(
                    PaidTaskEntity(
                        id = "TSK-001",
                        creatorUserId = adminId,
                        title = "Complete Rwanda Digital Savings Readiness Survey",
                        description = "Share your preferences on Goal-Based vs Long-Term RWF savings intervals to help improve local fintech tools.",
                        category = "Market Survey",
                        instructions = "Write a brief 2-sentence summary of your preferred savings goal (e.g. Tuition, Business Stock, Emergency Reserve) and why.",
                        requirements = "Minimum 20 characters explaining your savings goal in Rwanda.",
                        requiredProofType = "Written Response",
                        targetParticipants = 200,
                        approvedParticipants = 0,
                        rewardPerParticipantRwf = 100L,
                        platformFeeRwf = 0L,
                        totalBudgetRwf = 20000L,
                        spentBudgetRwf = 0L,
                        paymentReference = "PLATFORM-SEED-TSK1",
                        status = "ACTIVE"
                    )
                )
                dao.upsertPaidTask(
                    PaidTaskEntity(
                        id = "TSK-002",
                        creatorUserId = adminId,
                        title = "Verify Mobile Money USSD Format Understanding",
                        description = "Educational verification task ensuring every member recognizes the official E-FINANCE RWANDA USSD payment structure.",
                        category = "Educational Quiz",
                        instructions = "State the official Rwanda Mobile Money USSD payment format for a 200 RWF activation fee and confirm what happens after submission.",
                        requirements = "Include 182811880554*200# and mention administrator payment verification.",
                        requiredProofType = "Quiz Answer",
                        targetParticipants = 250,
                        approvedParticipants = 0,
                        rewardPerParticipantRwf = 60L,
                        platformFeeRwf = 0L,
                        totalBudgetRwf = 15000L,
                        spentBudgetRwf = 0L,
                        paymentReference = "PLATFORM-SEED-TSK2",
                        status = "ACTIVE"
                    )
                )

                dao.insertAuditLog(
                    AuditLogEntity(
                        id = "AUD-INIT-001",
                        actorUserId = adminId,
                        actorEmail = "ezeprodeveloper@gmail.com",
                        action = "SYSTEM_SEED",
                        targetEntity = "EFinanceDatabase",
                        details = "Initialized E-FINANCE RWANDA database, Super Admin account, loan products, advertisements, and educational campaigns."
                    )
                )
            }
        }
    }
}
