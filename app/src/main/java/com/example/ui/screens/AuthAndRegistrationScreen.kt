package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.RwandaReferenceData
import com.example.data.local.PaymentVerificationEntity
import com.example.data.local.SystemSettingsEntity
import com.example.data.local.UserEntity
import com.example.data.security.SecurityUtils
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.RegistrationFormState
import java.io.File

@Composable
fun LoginScreen(
    settings: SystemSettingsEntity,
    onLogin: (String, String) -> Unit,
    onResetPassword: (String, String, String, String, () -> Unit) -> Unit,
    onNavigateRegister: () -> Unit,
    onBackToPublic: () -> Unit
) {
    BackHandler(onBack = onBackToPublic)

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showForgotDialog by remember { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(rememberAnimatedFintechGradient())
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackToPublic, modifier = Modifier.testTag("login_back_button")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PureWhite)
                }
                Text("Sign In to ${settings.platformName}", style = MaterialTheme.typography.titleLarge, color = PureWhite)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        EFinanceLogoMark()
                        Column {
                            Text("Secure Member & Admin Portal", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text("Protected by salted SHA-256 & session audit logs", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("login_email_input")
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, contentDescription = "Toggle password")
                            }
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("login_password_input")
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showForgotDialog = true }) {
                            Text("Forgot Password?")
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { onLogin(email, password) },
                        modifier = Modifier.fillMaxWidth().height(52.dp).testTag("login_submit_button")
                    ) {
                        Text("Sign In Securely", fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = onNavigateRegister,
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("New to E-FINANCE RWANDA? Create Account")
                    }
                }
            }
            item {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = DeepNavy900,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Super Administration Access", style = MaterialTheme.typography.titleMedium, color = Gold400)
                        Text(
                            "Authorized Super Administrators sign in using their assigned email (e.g. ezeprodeveloper@gmail.com). First login enforces an immediate password update and 2FA check.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Slate200
                        )
                    }
                }
            }
        }
    }

    if (showForgotDialog) {
        ForgotPasswordDialog(
            onDismiss = { showForgotDialog = false },
            onSubmit = { fEmail, fNid, fPhone, fNewPass ->
                onResetPassword(fEmail, fNid, fPhone, fNewPass) { showForgotDialog = false }
            }
        )
    }
}

@Composable
private fun ForgotPasswordDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, String, String, String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var nid by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Recover Account Password") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Verify your registered identity to set a new password.", style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Registered Email") }, singleLine = true)
                OutlinedTextField(value = nid, onValueChange = { nid = it }, label = { Text("16-Digit National ID") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Registered Phone") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                OutlinedTextField(value = newPass, onValueChange = { newPass = it }, label = { Text("New Strong Password") }, visualTransformation = PasswordVisualTransformation(), singleLine = true)
            }
        },
        confirmButton = {
            Button(onClick = { onSubmit(email, nid, phone, newPass) }) {
                Text("Reset Password")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun ThreeStepRegistrationScreen(
    form: RegistrationFormState,
    settings: SystemSettingsEntity,
    onUpdateForm: ((RegistrationFormState) -> RegistrationFormState) -> Unit,
    onStoreIdPhoto: (Uri) -> String?,
    onSubmit: () -> Unit,
    onError: (String) -> Unit,
    onBackToPublic: () -> Unit
) {
    BackHandler {
        if (form.step > 1) onUpdateForm { it.copy(step = it.step - 1) } else onBackToPublic()
    }

    val idPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val savedPath = onStoreIdPhoto(uri)
            if (savedPath != null) {
                onUpdateForm { it.copy(nationalIdFrontUri = savedPath) }
            } else {
                onError("Unable to store selected National ID image.")
            }
        }
    }

    val universities = remember(settings.universityListCsv) {
        settings.universityListCsv.split("|").map { it.trim() }.filter { it.isNotEmpty() }
            .ifEmpty { RwandaReferenceData.defaultUniversities }
    }
    val combinations = remember(settings.aLevelCombinationsCsv) {
        settings.aLevelCombinationsCsv.split("|").map { it.trim() }.filter { it.isNotEmpty() }
            .ifEmpty { RwandaReferenceData.defaultALevelCombinations }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(rememberAnimatedFintechGradient())
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        if (form.step > 1) onUpdateForm { it.copy(step = it.step - 1) } else onBackToPublic()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PureWhite)
                    }
                    Column {
                        Text("Create E-FINANCE Account", style = MaterialTheme.typography.titleLarge, color = PureWhite)
                        Text("Step ${form.step} of 3 • ${when (form.step) { 1 -> "Personal Identification"; 2 -> "Educational Background"; else -> "Birth, Residence & Security" }}", style = MaterialTheme.typography.labelMedium, color = Cyan400)
                    }
                }
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { form.step / 3f },
                    color = Gold400,
                    trackColor = PureWhite.copy(alpha = 0.25f),
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50))
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (form.step) {
                1 -> {
                    item {
                        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                            Text("Step 1: Personal Identification", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(10.dp))
                            OutlinedTextField(
                                value = form.firstName,
                                onValueChange = { v -> onUpdateForm { it.copy(firstName = v) } },
                                label = { Text("First Name *") },
                                modifier = Modifier.fillMaxWidth().testTag("reg_first_name")
                            )
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = form.secondName,
                                onValueChange = { v -> onUpdateForm { it.copy(secondName = v) } },
                                label = { Text("Second Name *") },
                                modifier = Modifier.fillMaxWidth().testTag("reg_second_name")
                            )
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = form.email,
                                onValueChange = { v -> onUpdateForm { it.copy(email = v) } },
                                label = { Text("Email Address (Unique) *") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                modifier = Modifier.fillMaxWidth().testTag("reg_email")
                            )
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = form.phone,
                                onValueChange = { v -> onUpdateForm { it.copy(phone = v) } },
                                label = { Text("Rwanda Phone (078/079/072/073...) *") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier.fillMaxWidth().testTag("reg_phone")
                            )
                            Spacer(Modifier.height(8.dp))
                            DropdownSelectorField(
                                label = "Gender *",
                                selectedValue = form.gender,
                                options = RwandaReferenceData.genders,
                                onSelected = { v -> onUpdateForm { it.copy(gender = v) } }
                            )
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = form.dateOfBirth,
                                onValueChange = { v -> onUpdateForm { it.copy(dateOfBirth = v) } },
                                label = { Text("Date of Birth (YYYY-MM-DD) *") },
                                placeholder = { Text("e.g. 2001-05-14") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = form.nationalIdNumber,
                                onValueChange = { v -> onUpdateForm { it.copy(nationalIdNumber = v) } },
                                label = { Text("National ID Number (16 digits) *") },
                                placeholder = { Text("1199980000000001") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth().testTag("reg_nid")
                            )
                            Spacer(Modifier.height(12.dp))
                            Text("National ID Photo Upload (JPG/JPEG/PNG — Locked after submission)", style = MaterialTheme.typography.labelLarge)
                            Spacer(Modifier.height(6.dp))
                            OutlinedButton(
                                onClick = {
                                    idPhotoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                },
                                modifier = Modifier.fillMaxWidth().testTag("reg_pick_id_photo")
                            ) {
                                Icon(Icons.Filled.Badge, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text(if (form.nationalIdFrontUri.isBlank()) "Select National ID Photo" else "National ID Photo Attached ✓")
                            }
                            if (form.nationalIdFrontUri.isNotBlank()) {
                                Spacer(Modifier.height(8.dp))
                                AsyncImage(
                                    model = File(form.nationalIdFrontUri),
                                    contentDescription = "Selected National ID Preview",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(12.dp))
                                )
                            }
                            Spacer(Modifier.height(14.dp))
                            Button(
                                onClick = {
                                    when {
                                        form.firstName.isBlank() || form.secondName.isBlank() -> onError("Enter your first and second name.")
                                        !SecurityUtils.isValidEmail(form.email) -> onError("Enter a valid email address.")
                                        !SecurityUtils.isValidRwandaPhone(form.phone) -> onError("Enter a valid Rwandan phone number (078/079/072/073...).")
                                        form.dateOfBirth.isBlank() -> onError("Enter your date of birth.")
                                        !SecurityUtils.isValidNationalId(form.nationalIdNumber) -> onError("National ID must be 16 digits starting with 1.")
                                        form.nationalIdFrontUri.isBlank() -> onError("Upload your National ID photo before continuing.")
                                        else -> onUpdateForm { it.copy(step = 2) }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("reg_step1_next")
                            ) {
                                Text("Continue to Education (Step 2)")
                            }
                        }
                    }
                }

                2 -> {
                    item {
                        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                            Text("Step 2: Educational Background", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(10.dp))
                            Text("Select Education Category", style = MaterialTheme.typography.labelLarge)
                            Spacer(Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                FilterChip(
                                    selected = form.educationCategory == "UNIVERSITY",
                                    onClick = {
                                        onUpdateForm {
                                            it.copy(
                                                educationCategory = "UNIVERSITY",
                                                institutionName = universities.first(),
                                                academicLevelOrClass = RwandaReferenceData.academicLevels.first(),
                                                facultyOrCombination = ""
                                            )
                                        }
                                    },
                                    label = { Text("UNIVERSITY") },
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = form.educationCategory == "A_LEVEL",
                                    onClick = {
                                        onUpdateForm {
                                            it.copy(
                                                educationCategory = "A_LEVEL",
                                                institutionName = "",
                                                academicLevelOrClass = RwandaReferenceData.aLevelClasses.first(),
                                                facultyOrCombination = combinations.first()
                                            )
                                        }
                                    },
                                    label = { Text("A-LEVEL") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            if (form.educationCategory == "UNIVERSITY") {
                                DropdownSelectorField(
                                    label = "University Name *",
                                    selectedValue = form.institutionName.ifBlank { universities.first() },
                                    options = universities,
                                    onSelected = { v -> onUpdateForm { it.copy(institutionName = v) } }
                                )
                                Spacer(Modifier.height(8.dp))
                                DropdownSelectorField(
                                    label = "Academic Level *",
                                    selectedValue = form.academicLevelOrClass.ifBlank { RwandaReferenceData.academicLevels.first() },
                                    options = RwandaReferenceData.academicLevels,
                                    onSelected = { v -> onUpdateForm { it.copy(academicLevelOrClass = v) } }
                                )
                                Spacer(Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = form.facultyOrCombination,
                                    onValueChange = { v -> onUpdateForm { it.copy(facultyOrCombination = v) } },
                                    label = { Text("Faculty *") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = form.programOfStudy,
                                    onValueChange = { v -> onUpdateForm { it.copy(programOfStudy = v) } },
                                    label = { Text("Program or Course of Study *") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                OutlinedTextField(
                                    value = form.institutionName,
                                    onValueChange = { v -> onUpdateForm { it.copy(institutionName = v) } },
                                    label = { Text("School Name *") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(Modifier.height(8.dp))
                                DropdownSelectorField(
                                    label = "Current Class *",
                                    selectedValue = form.academicLevelOrClass.ifBlank { RwandaReferenceData.aLevelClasses.first() },
                                    options = RwandaReferenceData.aLevelClasses,
                                    onSelected = { v -> onUpdateForm { it.copy(academicLevelOrClass = v) } }
                                )
                                Spacer(Modifier.height(8.dp))
                                DropdownSelectorField(
                                    label = "Combination *",
                                    selectedValue = form.facultyOrCombination.ifBlank { combinations.first() },
                                    options = combinations,
                                    onSelected = { v -> onUpdateForm { it.copy(facultyOrCombination = v) } }
                                )
                            }
                            Spacer(Modifier.height(14.dp))
                            Button(
                                onClick = {
                                    if (form.institutionName.isBlank() || form.facultyOrCombination.isBlank()) {
                                        onError("Please complete all required educational fields.")
                                    } else if (form.educationCategory == "UNIVERSITY" && form.programOfStudy.isBlank()) {
                                        onError("Enter your program or course of study.")
                                    } else {
                                        onUpdateForm { it.copy(step = 3) }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("reg_step2_next")
                            ) {
                                Text("Continue to Residence & Password (Step 3)")
                            }
                        }
                    }
                }

                else -> {
                    item {
                        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                            Text("Step 3: Place of Birth", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = form.birthCountry,
                                onValueChange = { v -> onUpdateForm { it.copy(birthCountry = v) } },
                                label = { Text("Birth Country *") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(8.dp))
                            DropdownSelectorField(
                                label = "Birth Province *",
                                selectedValue = form.birthProvince,
                                options = RwandaReferenceData.provinces,
                                onSelected = { prov ->
                                    val firstDist = RwandaReferenceData.districtsByProvince[prov]?.firstOrNull() ?: ""
                                    onUpdateForm { it.copy(birthProvince = prov, birthDistrict = firstDist) }
                                }
                            )
                            Spacer(Modifier.height(8.dp))
                            DropdownSelectorField(
                                label = "Birth District *",
                                selectedValue = form.birthDistrict,
                                options = RwandaReferenceData.districtsByProvince[form.birthProvince].orEmpty(),
                                onSelected = { d -> onUpdateForm { it.copy(birthDistrict = d) } }
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = form.birthSector,
                                    onValueChange = { v -> onUpdateForm { it.copy(birthSector = v) } },
                                    label = { Text("Sector *") },
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = form.birthCell,
                                    onValueChange = { v -> onUpdateForm { it.copy(birthCell = v) } },
                                    label = { Text("Cell *") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = form.birthVillage,
                                onValueChange = { v -> onUpdateForm { it.copy(birthVillage = v) } },
                                label = { Text("Village *") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    item {
                        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                            Text("Current Place of Residence", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = form.resCountry,
                                onValueChange = { v -> onUpdateForm { it.copy(resCountry = v) } },
                                label = { Text("Residence Country *") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(8.dp))
                            DropdownSelectorField(
                                label = "Residence Province *",
                                selectedValue = form.resProvince,
                                options = RwandaReferenceData.provinces,
                                onSelected = { prov ->
                                    val firstDist = RwandaReferenceData.districtsByProvince[prov]?.firstOrNull() ?: ""
                                    onUpdateForm { it.copy(resProvince = prov, resDistrict = firstDist) }
                                }
                            )
                            Spacer(Modifier.height(8.dp))
                            DropdownSelectorField(
                                label = "Residence District (Optional)",
                                selectedValue = form.resDistrict,
                                options = RwandaReferenceData.districtsByProvince[form.resProvince].orEmpty(),
                                onSelected = { d -> onUpdateForm { it.copy(resDistrict = d) } }
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = form.resSector,
                                    onValueChange = { v -> onUpdateForm { it.copy(resSector = v) } },
                                    label = { Text("Sector (Opt)") },
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = form.resCell,
                                    onValueChange = { v -> onUpdateForm { it.copy(resCell = v) } },
                                    label = { Text("Cell (Opt)") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = form.resVillage,
                                onValueChange = { v -> onUpdateForm { it.copy(resVillage = v) } },
                                label = { Text("Village (Optional)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    item {
                        val strength = remember(form.password) { SecurityUtils.evaluatePasswordStrength(form.password) }
                        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                            Text("Account Security", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = form.password,
                                onValueChange = { v -> onUpdateForm { it.copy(password = v) } },
                                label = { Text("Create Password *") },
                                visualTransformation = PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth().testTag("reg_password")
                            )
                            Spacer(Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { strength.score / 5f },
                                color = if (strength.isValid) Emerald600 else Gold600,
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50))
                            )
                            Text(
                                "Strength: ${strength.label} (Min 8 chars, mixed case + digits/symbols)",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (strength.isValid) Emerald600 else Gold600
                            )
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = form.confirmPassword,
                                onValueChange = { v -> onUpdateForm { it.copy(confirmPassword = v) } },
                                label = { Text("Confirm Password *") },
                                visualTransformation = PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth().testTag("reg_confirm_password")
                            )
                            Spacer(Modifier.height(14.dp))
                            Button(
                                onClick = {
                                    if (form.birthSector.isBlank() || form.birthCell.isBlank() || form.birthVillage.isBlank()) {
                                        onError("Complete Sector, Cell, and Village for Place of Birth.")
                                    } else {
                                        onSubmit()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(52.dp).testTag("reg_submit_button")
                            ) {
                                Text("Submit Registration for Approval", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AccountGateScreen(
    user: UserEntity,
    settings: SystemSettingsEntity,
    userPayments: List<PaymentVerificationEntity>,
    pendingTwoFactorCode: String?,
    isTwoFactorVerified: Boolean,
    onVerifyTwoFactor: (String) -> Unit,
    onChangePassword: (String) -> Unit,
    onSubmitActivationPayment: (String) -> Unit,
    onLogout: () -> Unit
) {
    var paymentRef by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var twoFactorInput by remember { mutableStateOf("") }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(rememberAnimatedFintechGradient())
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    EFinanceLogoMark()
                    Column {
                        Text("Account Verification Gate", style = MaterialTheme.typography.titleMedium, color = PureWhite)
                        Text("${user.firstName} ${user.secondName}", style = MaterialTheme.typography.labelMedium, color = Cyan400)
                    }
                }
                TextButton(onClick = onLogout) {
                    Text("Logout", color = Gold400)
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Two-Factor Challenge for Super Admin
            if (!isTwoFactorVerified && pendingTwoFactorCode != null) {
                item {
                    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                        Text("Two-Factor Authentication (2FA)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Enter the 6-digit security verification code for ${user.email}.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(8.dp))
                        Surface(color = DeepNavy900, shape = RoundedCornerShape(10.dp)) {
                            Text(
                                "Authenticator Code (Device Security Channel): $pendingTwoFactorCode",
                                style = MaterialTheme.typography.labelLarge.copy(fontFamily = MonoFontFamily),
                                color = Gold400,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(
                            value = twoFactorInput,
                            onValueChange = { twoFactorInput = it },
                            label = { Text("6-Digit 2FA Code") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().testTag("two_factor_input")
                        )
                        Spacer(Modifier.height(10.dp))
                        Button(
                            onClick = { onVerifyTwoFactor(twoFactorInput) },
                            modifier = Modifier.fillMaxWidth().testTag("verify_two_factor_button")
                        ) {
                            Text("Verify 2FA Code")
                        }
                    }
                }
            } else if (user.mustChangePassword) {
                item {
                    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                        Text("Mandatory First-Login Password Update", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Crimson600)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "For platform security, you must replace the initial seeded password before accessing the Super Administration Platform.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = { Text("New Strong Password (min 8 chars, mixed)") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth().testTag("forced_new_password_input")
                        )
                        Spacer(Modifier.height(10.dp))
                        Button(
                            onClick = { onChangePassword(newPassword) },
                            modifier = Modifier.fillMaxWidth().testTag("forced_change_password_button")
                        ) {
                            Text("Update Password & Continue")
                        }
                    }
                }
            } else {
                item {
                    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Application Status", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            StatusBadge(user.accountStatus)
                        }
                        Spacer(Modifier.height(10.dp))
                        LiveRwandaClockCard(modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(12.dp))
                        when (user.accountStatus) {
                            "PENDING" -> {
                                Text(
                                    "Your 3-step registration is under review by an E-FINANCE RWANDA administrator. Once approved, you will proceed to the ${SecurityUtils.formatRwf(settings.activationFeeRwf)} activation step.",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            "CORRECTION_REQUIRED" -> {
                                Text(
                                    "Administrator requested corrections: ${user.statusNote.ifBlank { "Please contact support for details." }}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Gold600
                                )
                            }
                            "REJECTED" -> {
                                Text(
                                    "Your application was rejected: ${user.statusNote.ifBlank { "Does not meet eligibility criteria." }}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Crimson600
                                )
                            }
                            "APPROVED_AWAITING_ACTIVATION" -> {
                                Text(
                                    "Congratulations! Your registration is approved. Complete the ${SecurityUtils.formatRwf(settings.activationFeeRwf)} account activation payment below to unlock your full dashboard and receive your ${SecurityUtils.formatRwf(settings.welcomeBonusRwf)} welcome bonus.",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }

                if (user.accountStatus == "APPROVED_AWAITING_ACTIVATION") {
                    item {
                        UssdPaymentInstructionCard(
                            paymentNumber = settings.paymentNumber,
                            amountRwf = settings.activationFeeRwf,
                            title = "Account Activation Payment"
                        )
                    }
                    item {
                        val activationPayments = userPayments.filter { it.paymentType == "ACTIVATION" }
                        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                            Text("Submit Payment Reference", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = paymentRef,
                                onValueChange = { paymentRef = it },
                                label = { Text("Mobile Money Transaction ID / Reference") },
                                placeholder = { Text("e.g. MP261120.1402.A9821") },
                                modifier = Modifier.fillMaxWidth().testTag("activation_ref_input")
                            )
                            Spacer(Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    onSubmitActivationPayment(paymentRef)
                                    paymentRef = ""
                                },
                                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("submit_activation_payment_button")
                            ) {
                                Text("Submit Activation Reference")
                            }
                            if (activationPayments.isNotEmpty()) {
                                Spacer(Modifier.height(12.dp))
                                Text("Submitted Verification Status:", style = MaterialTheme.typography.labelLarge)
                                activationPayments.forEach { pv ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(pv.submittedTransactionId, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                            Text(SecurityUtils.formatKigaliDateTime(pv.createdAt), style = MaterialTheme.typography.labelMedium)
                                        }
                                        StatusBadge(pv.status)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
