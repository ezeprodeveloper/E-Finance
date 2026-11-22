package com.example.data.security

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import com.example.data.local.FinancialTransactionEntity
import java.io.File
import java.io.OutputStream
import java.security.MessageDigest
import java.security.SecureRandom
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

object SecurityUtils {
    val KIGALI_TIMEZONE: TimeZone = TimeZone.getTimeZone("Africa/Kigali")

    private val rwandaPhoneRegex = Regex("^(\\+?250|0)?7[2389]\\d{7}$")
    private val nationalIdRegex = Regex("^1\\d{15}$")
    private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun generateSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun hashPassword(password: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest("$salt:$password".toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun verifyPassword(password: String, salt: String, expectedHash: String): Boolean {
        return hashPassword(password, salt) == expectedHash
    }

    fun isValidEmail(email: String): Boolean = emailRegex.matches(email.trim())

    fun normalizeRwandaPhone(phone: String): String {
        val clean = phone.trim().replace(" ", "").replace("-", "")
        return when {
            clean.startsWith("+250") -> clean
            clean.startsWith("250") -> "+$clean"
            clean.startsWith("07") -> "+250${clean.substring(1)}"
            clean.startsWith("7") && clean.length == 9 -> "+250$clean"
            else -> clean
        }
    }

    fun isValidRwandaPhone(phone: String): Boolean {
        val clean = phone.trim().replace(" ", "").replace("-", "")
        return rwandaPhoneRegex.matches(clean)
    }

    fun isValidNationalId(nid: String): Boolean {
        val clean = nid.trim().replace(" ", "")
        return nationalIdRegex.matches(clean)
    }

    data class PasswordStrength(val score: Int, val label: String, val isValid: Boolean)

    fun evaluatePasswordStrength(password: String): PasswordStrength {
        var score = 0
        if (password.length >= 8) score++
        if (password.any { it.isUpperCase() }) score++
        if (password.any { it.isLowerCase() }) score++
        if (password.any { it.isDigit() }) score++
        if (password.any { !it.isLetterOrDigit() }) score++
        val label = when (score) {
            0, 1 -> "Weak"
            2 -> "Fair"
            3 -> "Good"
            4 -> "Strong"
            else -> "Very Strong"
        }
        return PasswordStrength(score = score, label = label, isValid = password.length >= 8 && score >= 3)
    }

    fun generateId(prefix: String): String {
        val shortUuid = UUID.randomUUID().toString().replace("-", "").take(8).uppercase(Locale.US)
        return "$prefix-$shortUuid"
    }

    fun generateTwoFactorCode(): String {
        val num = SecureRandom().nextInt(900000) + 100000
        return num.toString()
    }

    fun formatUssdReference(paymentNumber: String, amountRwf: Long): String {
        val cleanNumber = paymentNumber.ifBlank { "182811880554" }
        return "$cleanNumber*$amountRwf#"
    }

    fun formatRwf(amount: Long): String {
        val nf = NumberFormat.getIntegerInstance(Locale.US)
        return "${nf.format(amount)} RWF"
    }

    fun kigaliTodayKey(nowMillis: Long = System.currentTimeMillis()): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        sdf.timeZone = KIGALI_TIMEZONE
        return sdf.format(Date(nowMillis))
    }

    fun kigaliMonthKey(nowMillis: Long = System.currentTimeMillis()): String {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.US)
        sdf.timeZone = KIGALI_TIMEZONE
        return sdf.format(Date(nowMillis))
    }

    fun formatKigaliDateTime(millis: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.US)
        sdf.timeZone = KIGALI_TIMEZONE
        return sdf.format(Date(millis))
    }

    fun formatKigaliDate(millis: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.US)
        sdf.timeZone = KIGALI_TIMEZONE
        return sdf.format(Date(millis))
    }

    fun formatKigaliTime(millis: Long): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.US)
        sdf.timeZone = KIGALI_TIMEZONE
        return sdf.format(Date(millis))
    }

    /**
     * Copies a user-selected media URI into private internal app storage (never exposed publicly)
     * so National ID & KYC files remain accessible and protected even after picker permissions expire.
     */
    fun copyUriToPrivateStorage(context: Context, uri: Uri, folder: String, prefix: String): String? {
        return try {
            val dir = File(context.filesDir, folder)
            if (!dir.exists()) dir.mkdirs()
            val mime = context.contentResolver.getType(uri) ?: "image/jpeg"
            val ext = when {
                mime.contains("png", ignoreCase = true) -> "png"
                mime.contains("mp4", ignoreCase = true) -> "mp4"
                mime.contains("video", ignoreCase = true) -> "mp4"
                else -> "jpg"
            }
            val destFile = File(dir, "${prefix}_${System.currentTimeMillis()}.$ext")
            context.contentResolver.openInputStream(uri)?.use { input ->
                destFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            destFile.absolutePath
        } catch (_: Exception) {
            null
        }
    }

    fun writeTransactionsCsv(output: OutputStream, transactions: List<FinancialTransactionEntity>, userEmail: String) {
        val writer = output.bufferedWriter()
        writer.appendLine("E-FINANCE RWANDA - TRANSACTION STATEMENT ($userEmail)")
        writer.appendLine("Generated (Africa/Kigali): ${formatKigaliDateTime(System.currentTimeMillis())}")
        writer.appendLine("Transaction ID,Category,Amount (RWF),Fee (RWF),Net Amount (RWF),Date,Time,Status,Reference,Description")
        transactions.forEach { tx ->
            val safeDesc = tx.description.replace("\"", "\"\"")
            val safeRef = tx.paymentReference.replace("\"", "\"\"")
            writer.appendLine(
                "\"${tx.id}\",\"${tx.category}\",${tx.amountRwf},${tx.feeRwf},${tx.netAmountRwf},\"${formatKigaliDate(tx.createdAt)}\",\"${formatKigaliTime(tx.createdAt)}\",\"${tx.status}\",\"$safeRef\",\"$safeDesc\""
            )
        }
        writer.flush()
    }

    fun writeTransactionsPdf(
        output: OutputStream,
        transactions: List<FinancialTransactionEntity>,
        memberName: String,
        memberEmail: String
    ) {
        val document = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val headerPaint = Paint().apply {
            color = Color.rgb(10, 25, 49)
            style = Paint.Style.FILL
        }
        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val subtitlePaint = Paint().apply {
            color = Color.rgb(226, 232, 240)
            textSize = 11f
        }
        val bodyPaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 10f
        }
        val boldPaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val chunks = if (transactions.isEmpty()) listOf(emptyList()) else transactions.chunked(26)
        chunks.forEachIndexed { pageIndex, pageItems ->
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageIndex + 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            canvas.drawRect(0f, 0f, pageWidth.toFloat(), 86f, headerPaint)
            canvas.drawText("E-FINANCE RWANDA — FINANCIAL STATEMENT", 28f, 36f, titlePaint)
            canvas.drawText("Member: $memberName ($memberEmail)", 28f, 56f, subtitlePaint)
            canvas.drawText(
                "Kigali Time: ${formatKigaliDateTime(System.currentTimeMillis())} | Page ${pageIndex + 1}/${chunks.size}",
                28f,
                72f,
                subtitlePaint
            )

            var y = 112f
            canvas.drawText("TXN ID", 24f, y, boldPaint)
            canvas.drawText("CATEGORY", 105f, y, boldPaint)
            canvas.drawText("AMOUNT", 220f, y, boldPaint)
            canvas.drawText("NET", 295f, y, boldPaint)
            canvas.drawText("DATE", 365f, y, boldPaint)
            canvas.drawText("STATUS", 475f, y, boldPaint)
            y += 14f
            canvas.drawLine(24f, y, 570f, y, bodyPaint)
            y += 16f

            if (pageItems.isEmpty()) {
                canvas.drawText("No financial transactions recorded yet.", 24f, y, bodyPaint)
            } else {
                pageItems.forEach { tx ->
                    canvas.drawText(tx.id.take(12), 24f, y, bodyPaint)
                    canvas.drawText(tx.category.take(18), 105f, y, bodyPaint)
                    canvas.drawText("${tx.amountRwf}", 220f, y, bodyPaint)
                    canvas.drawText("${tx.netAmountRwf}", 295f, y, bodyPaint)
                    canvas.drawText(formatKigaliDate(tx.createdAt), 365f, y, bodyPaint)
                    canvas.drawText(tx.status, 475f, y, bodyPaint)
                    y += 13f
                    canvas.drawText(tx.description.take(78), 24f, y, subtitlePaint.apply { color = Color.DKGRAY; textSize = 8.5f })
                    y += 13f
                }
            }
            document.finishPage(page)
        }
        document.writeTo(output)
        document.close()
    }
}
