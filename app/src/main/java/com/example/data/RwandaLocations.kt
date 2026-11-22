package com.example.data

data class FaqItem(val question: String, val answer: String)

data class PublicServiceDetail(
    val id: String,
    val title: String,
    val subtitle: String,
    val summary: String,
    val highlights: List<String>
)

object RwandaReferenceData {
    val provinces: List<String> = listOf(
        "Kigali City",
        "Eastern Province",
        "Northern Province",
        "Southern Province",
        "Western Province"
    )

    val districtsByProvince: Map<String, List<String>> = mapOf(
        "Kigali City" to listOf("Gasabo", "Kicukiro", "Nyarugenge"),
        "Eastern Province" to listOf("Bugesera", "Gatsibo", "Kayonza", "Kirehe", "Ngoma", "Nyagatare", "Rwamagana"),
        "Northern Province" to listOf("Burera", "Gakenke", "Gicumbi", "Musanze", "Rulindo"),
        "Southern Province" to listOf("Gisagara", "Huye", "Kamonyi", "Muhanga", "Nyamagabe", "Nyanza", "Nyaruguru", "Ruhango"),
        "Western Province" to listOf("Karongi", "Ngororero", "Nyabihu", "Nyamasheke", "Rubavu", "Rusizi", "Rutsiro")
    )

    val defaultUniversities: List<String> = listOf(
        "University of Rwanda (UR)",
        "Carnegie Mellon University Africa",
        "African Leadership University (ALU)",
        "Kepler College",
        "Adventist University of Central Africa (AUCA)",
        "University of Kigali (UoK)",
        "INES-Ruhengeri",
        "ULK - Kigali Independent University",
        "Mount Kigali University",
        "Rwanda Polytechnic (IPRC)"
    )

    val academicLevels: List<String> = listOf(
        "Year 1 (Level 1)",
        "Year 2 (Level 2)",
        "Year 3 (Level 3)",
        "Year 4 (Level 4)",
        "Year 5 / Honors",
        "Master's / Postgraduate",
        "PhD / Doctoral"
    )

    val aLevelClasses: List<String> = listOf("Senior 4 (S4)", "Senior 5 (S5)", "Senior 6 (S6)", "TVET Level 3-5")

    val defaultALevelCombinations: List<String> = listOf(
        "MPC (Mathematics, Physics, Computer Science)",
        "PCM (Physics, Chemistry, Mathematics)",
        "PCB (Physics, Chemistry, Biology)",
        "MCB (Mathematics, Chemistry, Biology)",
        "MEG (Mathematics, Economics, Geography)",
        "HEG (History, Economics, Geography)",
        "MCE (Mathematics, Computer Science, Economics)",
        "LKK (Literature, Kinyarwanda, Kiswahili)",
        "Accounting & Finance (TVET)",
        "Software Development (TVET)"
    )

    val genders: List<String> = listOf("Male", "Female", "Prefer not to say")

    val supportCategories: List<String> = listOf(
        "Account & Registration",
        "Activation Payment",
        "KYC Verification",
        "Deposits & Savings",
        "Withdrawals & Mobile Money",
        "Loans & Repayments",
        "Video Campaigns & Rewards",
        "Paid Tasks & Submissions",
        "Security & Account Access"
    )

    val taskCategories: List<String> = listOf(
        "Video Engagement",
        "Website Visit",
        "Social Media Follow",
        "Market Survey",
        "Screenshot Verification",
        "Educational Quiz",
        "App & Product Feedback"
    )

    val videoCategories: List<String> = listOf(
        "Financial Literacy",
        "Business & Entrepreneurship",
        "Product Showcase",
        "Education & Skills",
        "Community Announcements",
        "Technology & Innovation"
    )

    val publicServices: List<PublicServiceDetail> = listOf(
        PublicServiceDetail(
            id = "saving",
            title = "Smart Savings Vaults",
            subtitle = "Flexible, Goal-Based & Long-Term RWF Savings",
            summary = "Build disciplined wealth at your own pace. Choose Flexible Savings for instant liquidity, Goal-Based Savings for tuition or business targets, or Long-Term Savings for structured growth.",
            highlights = listOf(
                "Save any amount anytime in Rwandan Francs (RWF)",
                "Track deposit history, progress bars, and target milestones",
                "Immutable ledger records for every deposit and withdrawal",
                "Transparent rules configured by platform compliance"
            )
        ),
        PublicServiceDetail(
            id = "withdrawals",
            title = "Mobile Money Withdrawals",
            subtitle = "Direct Payouts to MTN MoMo & Airtel Money",
            summary = "Request payouts from your available wallet directly to your verified Rwandan mobile money number with clear fee disclosure before confirmation.",
            highlights = listOf(
                "Supports MTN Mobile Money and Airtel Money",
                "Clear fee breakdown displayed prior to confirmation",
                "Protected by KYC verification and admin settlement review",
                "Real-time status tracking from Pending to Completed"
            )
        ),
        PublicServiceDetail(
            id = "loans",
            title = "Structured Credit & Loans",
            subtitle = "Transparent Lending with Responsible Review",
            summary = "Apply for personal, education, or micro-business loans with clear repayment schedules, interest disclosures, and dedicated review.",
            highlights = listOf(
                "Multiple loan products tailored for students and entrepreneurs",
                "Full repayment schedule and outstanding balance tracking",
                "Human compliance and eligibility review on every application",
                "No deceptive automatic loan promises"
            )
        ),
        PublicServiceDetail(
            id = "videos",
            title = "Earn & Promote With Videos",
            subtitle = "Watch Verified Campaigns or Launch Your Own",
            summary = "Verified members earn rewards for genuine, full-duration views of approved video campaigns. Creators can launch campaigns starting at 50 RWF per valid view.",
            highlights = listOf(
                "Anti-duplication protection: one verified reward per user per video",
                "Minimum watch duration enforced before reward credit",
                "Creator campaigns require 182811880554*AMOUNT# payment verification",
                "Live campaign spending and remaining viewer analytics"
            )
        ),
        PublicServiceDetail(
            id = "tasks",
            title = "Paid Micro-Tasks",
            subtitle = "Complete Tasks or Hire Members With Proof",
            summary = "Earn RWF by completing surveys, educational activities, or verified social tasks. Every submission is independently verified by administrators.",
            highlights = listOf(
                "Database-level anti-duplication (1 submission per user per task)",
                "Independent admin proof review protects both workers and creators",
                "Upload screenshot or text evidence cleanly",
                "Instant wallet credit upon administrator approval"
            )
        )
    )

    val faqItems: List<FaqItem> = listOf(
        FaqItem(
            "How do I activate my E-FINANCE RWANDA account?",
            "After completing the 3-step registration and receiving administrator approval, send the 200 RWF activation fee via Rwanda Mobile Money using reference 182811880554*200# and submit your transaction ID for verification. Once verified, you receive full dashboard access plus a 200 RWF welcome bonus."
        ),
        FaqItem(
            "Why is KYC verification required before financial transactions?",
            "Know Your Customer (KYC) verification protects member accounts against identity fraud and ensures compliance with Rwandan financial integrity standards before deposits, savings, withdrawals, or loan applications."
        ),
        FaqItem(
            "How are withdrawal fees calculated?",
            "A transparent platform withdrawal fee (default 25%, configurable by system administrators) is calculated and shown clearly before you confirm any withdrawal. For example, a 10,000 RWF withdrawal has a 2,500 RWF fee and 7,500 RWF net payout."
        ),
        FaqItem(
            "When do I earn my Daily Check-In reward?",
            "Every active member can claim the daily check-in reward (default 20 RWF) once per calendar day in the Africa/Kigali timezone. Duplicate claims on the same day are automatically blocked."
        ),
        FaqItem(
            "How do Paid Video Campaigns and Tasks work?",
            "Creators fund campaigns upfront via Mobile Money (182811880554*AMOUNT#) with a minimum reward of 50 RWF per valid video view. Administrators verify payment and content before publication, and also verify task submissions so rewards are distributed fairly."
        ),
        FaqItem(
            "Are loans approved automatically upon registration?",
            "No. Loan applications require an active, KYC-verified account, eligibility checks, and human administrator review. Repayment terms and rates are disclosed upfront."
        )
    )

    const val TERMS_AND_CONDITIONS = """
E-FINANCE RWANDA — TERMS AND CONDITIONS (SANDBOX & OPERATIONAL FRAMEWORK)

1. Platform Nature & Regulatory Notice
E-FINANCE RWANDA is a digital financial technology platform operating under sandbox/demo controls until full regulatory licensing from the National Bank of Rwanda (BNR) and applicable authorities is finalized for live real-money operations. It does not represent itself as a licensed commercial bank unless expressly authorized.

2. Account Registration & Identity Accuracy
Applicants must provide truthful personal identification, educational background, and Rwandan residential information. National ID numbers, phone numbers, and email addresses must be unique. Submitted National ID images are immutable once lodged; corrections require formal administrator review and audit logging.

3. Activation & Welcome Bonus
Approved registrations require verification of the configured activation payment (default 200 RWF) via Mobile Money reference 182811880554*AMOUNT#. Accounts are activated only after verified payment confirmation. Welcome bonuses are displayed separately and governed by platform withdrawal eligibility rules.

4. Fees & Financial Ledger Integrity
All savings, deposits, withdrawals, loan repayments, video rewards, task rewards, and daily check-ins are recorded on an immutable financial ledger. Withdrawal requests are subject to the disclosed platform fee (default 25%) and administrator settlement verification.

5. Anti-Fraud & Acceptable Use
Automated viewing scripts, duplicate task submissions, falsified Mobile Money transaction IDs, or multi-account abuse will result in immediate account suspension, reward forfeiture, and audit escalation.

6. Dispute & Complaint Management
Members may lodge complaints or transaction inquiries anytime via the in-app Customer Support Center.
    """

    const val PRIVACY_POLICY = """
E-FINANCE RWANDA — PRIVACY & DATA PROTECTION POLICY

1. Data Collected
We collect personal identification details (legal name, email, Rwandan phone number, gender, date of birth, National ID number and document images), educational profile, place of birth and residence, and financial ledger records.

2. Purpose of Processing
Data is processed strictly for member onboarding, Know Your Customer (KYC) & Anti-Money Laundering (AML) verification, ledger integrity, fraud prevention, loan assessment, and reward distribution.

3. Protection of Sensitive Identity Documents
National ID photographs and KYC verification selfies are stored privately within controlled application storage and are never exposed via public URLs. Only authorized administrators may review KYC documents with full audit logging.

4. Password & Account Security
Passwords are protected using salted cryptographic hashing (SHA-256) and are never stored in plain text. Sessions and administrative actions are tracked in immutable audit logs.

5. User Rights
Members may inspect their profile, request audited corrections through Support, and export their personal transaction statements in CSV or PDF format at any time.
    """
}
