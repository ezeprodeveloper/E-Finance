# E-FINANCE RWANDA

An Android digital financial technology platform built with **Kotlin**, **Jetpack Compose**, **Material Design 3**, and **Room**.

## Getting the Android App from GitHub

- **Android Source Code:** Browse [`app/src/main/java/com/example/`](app/src/main/java/com/example/) for all Kotlin screens, ViewModels, Room database entities, security helpers, and tests.
- **Downloadable APK:** Open the **Actions** tab in this GitHub repository, select the latest **Android CI** workflow run, and download **`E-Finance-Rwanda-debug.apk`** under **Artifacts**.
- **If You Only See `README.md` on GitHub:** In Google AI Studio, open the settings/export menu and choose **Push to GitHub** again so all Android project files (`app/`, `gradle/`, `build.gradle.kts`, `.github/`) are pushed to the repository branch.

## Core Modules

- **Startup Sequence:** Multi-color animated app logo sequence (10s) followed by rotating **E-Finance** word reveal (5s) before opening the platform.
- **Public Portal & Registration:** Services catalog, live advertisements, FAQ, Contact Us, Terms & Privacy, 3-step registration with locked National ID image, and admin approval + Rwanda Mobile Money (`182811880554*AMOUNT#`) activation.
- **Member Finance & Earn Hub:** Smart Savings Vaults (Flexible, Goal-Based, Long-Term), MTN MoMo & Airtel Money Withdrawals, Loan Applications & Repayments, Watch & Upload Video Campaigns, Paid Tasks, Daily Kigali Check-In, KYC Verification, Transaction History with CSV & PDF export, and Support Center.
- **Super Administration Platform:** 18 KPIs, user/KYC/payment/withdrawal/loan approvals, loan product configuration, global financial ledger, video & task moderation, advertisement manager, system settings, support desk, and immutable audit logs.

## Build & Verify Locally

```bash
# Run unit & Robolectric tests
gradle :app:testDebugUnitTest

# Run Android Lint
gradle :app:lintDebug

# Build debug APK
gradle :app:assembleDebug
```
