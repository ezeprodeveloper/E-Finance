package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.*
import com.example.data.repository.EFinanceRepository
import com.example.data.security.SecurityUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class EFinanceRepositoryTest {
    private lateinit var db: EFinanceDatabase
    private lateinit var repo: EFinanceRepository
    private val adminId = "ADMIN-SEED-TEST"

    @Before
    fun setup() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, EFinanceDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val dao = db.dao()
        dao.upsertSystemSettings(SystemSettingsEntity())
        val salt = SecurityUtils.generateSalt()
        dao.insertUser(
            UserEntity(
                id = adminId,
                firstName = "Admin",
                secondName = "Tester",
                email = "ezeprodeveloper@gmail.com",
                phone = "+250788000100",
                gender = "Male",
                dateOfBirth = "1995-01-01",
                nationalIdNumber = "1199580000000001",
                nationalIdFrontUri = "system://admin",
                educationCategory = "UNIVERSITY",
                institutionName = "UR",
                academicLevelOrClass = "Master's",
                facultyOrCombination = "IT",
                birthCountry = "Rwanda",
                birthProvince = "Kigali City",
                birthDistrict = "Gasabo",
                birthSector = "Kacyiru",
                birthCell = "Kamatamu",
                birthVillage = "Nyacyonga",
                resCountry = "Rwanda",
                resProvince = "Kigali City",
                passwordHash = SecurityUtils.hashPassword("StrongPass@2026", salt),
                passwordSalt = salt,
                role = "SUPER_ADMIN",
                accountStatus = "ACTIVE",
                kycStatus = "APPROVED"
            )
        )
        dao.upsertLoanProduct(
            LoanProductEntity(
                id = "LP-EDU",
                name = "Student Loan",
                description = "Education credit",
                minAmountRwf = 5000L,
                maxAmountRwf = 200000L,
                interestRatePercent = 10.0,
                maxDurationMonths = 6
            )
        )
        repo = EFinanceRepository(db)
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun `full financial lifecycle enforces approvals, fees, anti-duplication, and ledger accuracy`() = runTest {
        // 1. Register user
        val regResult = repo.registerUser(
            firstName = "Jean",
            secondName = "Mugisha",
            email = "jean@rwanda.rw",
            phone = "0788123456",
            gender = "Male",
            dateOfBirth = "2000-03-15",
            nationalIdNumber = "1200080012345678",
            nationalIdFrontUri = "/private/id_front.jpg",
            educationCategory = "UNIVERSITY",
            institutionName = "University of Rwanda (UR)",
            academicLevelOrClass = "Year 3 (Level 3)",
            facultyOrCombination = "Business",
            programOfStudy = "Finance",
            birthCountry = "Rwanda",
            birthProvince = "Kigali City",
            birthDistrict = "Gasabo",
            birthSector = "Kimironko",
            birthCell = "Bibare",
            birthVillage = "Amahoro",
            resCountry = "Rwanda",
            resProvince = "Kigali City",
            resDistrict = "Kicukiro",
            resSector = "Niboye",
            resCell = "Niboye",
            resVillage = "Kigali",
            password = "UserPass@2026"
        )
        assertTrue(regResult.isSuccess)
        val user = regResult.getOrThrow()
        assertEquals("PENDING", user.accountStatus)

        // 2. Duplicate National ID rejected
        val dupResult = repo.registerUser(
            firstName = "Other", secondName = "Person", email = "other@rwanda.rw", phone = "0788999888",
            gender = "Female", dateOfBirth = "2001-01-01", nationalIdNumber = "1200080012345678",
            nationalIdFrontUri = "/private/x.jpg", educationCategory = "A_LEVEL", institutionName = "GS",
            academicLevelOrClass = "S6", facultyOrCombination = "MPC", programOfStudy = "",
            birthCountry = "Rwanda", birthProvince = "Kigali City", birthDistrict = "Gasabo",
            birthSector = "S", birthCell = "C", birthVillage = "V", resCountry = "Rwanda",
            resProvince = "Kigali City", resDistrict = "", resSector = "", resCell = "", resVillage = "",
            password = "UserPass@2026"
        )
        assertTrue(dupResult.isFailure)

        // 3. Admin approves registration & verifies activation payment
        assertTrue(repo.adminUpdateUserStatus(adminId, user.id, "APPROVE", "Approved").isSuccess)
        assertTrue(repo.submitPaymentVerification(user.id, "ACTIVATION", 200L, "MOMO12345").isSuccess)
        // Not active until admin verifies!
        assertEquals("APPROVED_AWAITING_ACTIVATION", db.dao().getUserById(user.id)?.accountStatus)

        val payId = db.dao().observePaymentsForUser(user.id).first().first().id
        assertTrue(repo.adminReviewPaymentVerification(adminId, payId, true, "Verified").isSuccess)
        assertEquals("ACTIVE", db.dao().getUserById(user.id)?.accountStatus)
        assertEquals(200L, db.dao().getWallet(user.id)?.welcomeBonusRwf)

        // 4. Submit & Approve KYC + Deposit 20,000 RWF
        assertTrue(
            repo.submitKyc(
                user.id, "Jean Mugisha", user.nationalIdNumber, user.dateOfBirth, user.gender,
                user.phone, user.email, "Kigali City", "Kicukiro", "Niboye", "Niboye", "Kigali",
                "/private/id_front.jpg", "/private/id_back.jpg", "/private/selfie.jpg", "Ready"
            ).isSuccess
        )
        val kycId = db.dao().getKycForUser(user.id)!!.id
        assertTrue(repo.adminReviewKyc(adminId, kycId, "APPROVED", "ID Verified").isSuccess)

        assertTrue(repo.submitPaymentVerification(user.id, "DEPOSIT", 20000L, "DEP99887").isSuccess)
        val depPayId = db.dao().observePaymentsForUser(user.id).first().first { it.paymentType == "DEPOSIT" }.id
        assertTrue(repo.adminReviewPaymentVerification(adminId, depPayId, true, "OK").isSuccess)

        // 5. Save 5,000 RWF & Withdraw 10,000 RWF (25% fee = 2,500 RWF, Net = 7,500 RWF)
        assertTrue(repo.createOrDepositSavings(user.id, "GOAL_BASED", "Tuition", "UR Tuition", 50000L, 5000L).isSuccess)
        val wResult = repo.requestWithdrawal(user.id, "MTN Mobile Money", "0788123456", 10000L)
        assertTrue(wResult.isSuccess)
        val w = wResult.getOrThrow()
        assertEquals(2500L, w.feeAmountRwf)
        assertEquals(7500L, w.netAmountRwf)
        assertTrue(repo.adminProcessWithdrawal(adminId, w.id, "COMPLETED", "Paid via MoMo").isSuccess)

        // 6. Daily check-in once per Rwanda calendar day
        assertTrue(repo.claimDailyCheckin(user.id).isSuccess)
        assertTrue(repo.claimDailyCheckin(user.id).isFailure) // blocked second claim same day

        // 7. Video campaign min 50 RWF rule + anti-duplication view
        assertTrue(repo.createVideoCampaign(user.id, "Too Low", "Desc", "Education", "", "", 20, 40L, 10, 7).isFailure)
        val vidRes = repo.createVideoCampaign(adminId, "Valid Video", "Desc", "Education", "", "", 20, 50L, 10, 7)
        assertTrue(vidRes.isSuccess)
        val video = vidRes.getOrThrow()
        assertTrue(repo.adminUpdateVideoStatus(adminId, video.id, "ACTIVE").isSuccess)
        assertTrue(repo.completeVideoView(user.id, video.id, 16).isSuccess)
        assertTrue(repo.completeVideoView(user.id, video.id, 18).isFailure) // duplicate blocked

        // 8. Paid Task anti-duplication & admin-only approval
        val taskRes = repo.createPaidTask(adminId, "Survey", "Desc", "Market Survey", "Write feedback", "Min 10 chars", "Text", 5, 100L, 7)
        assertTrue(taskRes.isSuccess)
        val task = taskRes.getOrThrow()
        assertTrue(repo.adminUpdateTaskStatus(adminId, task.id, "ACTIVE").isSuccess)
        assertTrue(repo.submitTaskCompletion(user.id, task.id, "Great platform for RWF savings", "").isSuccess)
        assertTrue(repo.submitTaskCompletion(user.id, task.id, "Duplicate", "").isFailure)
        val subId = db.dao().getTaskSubmission(user.id, task.id)!!.id
        assertTrue(repo.adminReviewTaskSubmission(adminId, subId, true, "Verified").isSuccess)

        // 9. Verify final wallet balances:
        // Available = 200 (bonus) + 20000 (deposit) - 5000 (savings) - 10000 (withdrawal) + 20 (checkin) + 50 (video) + 100 (task) = 5370
        val finalWallet = db.dao().getWallet(user.id)!!
        assertEquals(5370L, finalWallet.availableBalanceRwf)
        assertEquals(5000L, finalWallet.savingsBalanceRwf)
        assertEquals(0L, finalWallet.pendingWithdrawalsRwf)

        // 10. Profile photo update preserves locked National ID & Admin loan product catalog save works
        assertTrue(repo.updateProfilePhoto(user.id, "/private/profile_avatar.jpg").isSuccess)
        val updatedUser = db.dao().getUserById(user.id)!!
        assertEquals("/private/profile_avatar.jpg", updatedUser.profilePhotoUri)
        assertEquals("1200080012345678", updatedUser.nationalIdNumber)
        assertEquals("/private/id_front.jpg", updatedUser.nationalIdFrontUri)

        val newProduct = LoanProductEntity(
            id = "LP-SME-2026",
            name = "SME Kigali Micro-Credit",
            description = "Working capital credit",
            minAmountRwf = 10000L,
            maxAmountRwf = 500000L,
            interestRatePercent = 9.5,
            maxDurationMonths = 12,
            isActive = true
        )
        assertTrue(repo.adminSaveLoanProduct(adminId, newProduct).isSuccess)
        assertTrue(db.dao().observeLoanProducts().first().any { it.id == "LP-SME-2026" && it.name == "SME Kigali Micro-Credit" })
    }
}
