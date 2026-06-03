package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.liquidGlassCard
import com.example.ui.components.LiquidGlassToggle
import com.example.ui.theme.*
import com.example.ui.viewmodel.LankaBudgetViewModel
import com.example.ui.viewmodel.BackupItem
import com.example.ui.viewmodel.CustomCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: LankaBudgetViewModel,
    modifier: Modifier = Modifier
) {
    val activeProfile by viewModel.activeProfile.collectAsState()
    val profilesList by viewModel.profilesList.collectAsState()

    var newProfileName by remember { mutableStateOf("") }
    var showDeleteConfirmProfile by remember { mutableStateOf<String?>(null) }
    var showAppResetConfirm by remember { mutableStateOf(false) }
    var pastedJsonText by remember { mutableStateOf("") }

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // Live list of JSON backups on the SD Card public folder + internal cache
    var availableBackups by remember { mutableStateOf(viewModel.getRobustJsonBackups()) }

    var expandedTheme by remember { mutableStateOf(false) }
    var expandedCurrency by remember { mutableStateOf(false) }
    var expandedReportMode by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = 24.dp,
                bottom = 120.dp,
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 👤 Header Banner
            item {
                Box(
                    modifier = Modifier
                        .statusBarsPadding()
                        .fillMaxWidth()
                        .liquidGlassCard(cornerRadius = 24.dp, containerColor = Color.White.copy(alpha = 0.55f))
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(HeaderPillBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Active Profile Avatar",
                                tint = ElectricNeeds,
                                modifier = Modifier.size(48.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "LankaBudget Pro",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Profile Ledger: $activeProfile",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // 🎨 SECTION 1: INTERFACE THEME & GENERAL DISPLAY
            item {
                val activeTheme by viewModel.themeMode.collectAsState()
                val selectedCurrency by viewModel.selectedCurrency.collectAsState()
                val reportMode by viewModel.reportMode.collectAsState()

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlassCard(cornerRadius = 20.dp, containerColor = Color.White.copy(alpha = 0.45f)),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "INTERFACE THEME & CURRENCY",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = ElectricNeeds
                        )

                        // Theme Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("App Theme", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                                Text("Amoled, pitch-black, dark or light mode", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                            Box {
                                OutlinedButton(
                                    onClick = { expandedTheme = true },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                                ) {
                                    Text(activeTheme, fontWeight = FontWeight.Bold)
                                    Icon(Icons.Default.ArrowDropDown, null)
                                }
                                DropdownMenu(
                                    expanded = expandedTheme,
                                    onDismissRequest = { expandedTheme = false }
                                ) {
                                    listOf("Auto", "Light", "Dark", "Amoled").forEach { mode ->
                                        DropdownMenuItem(
                                            text = { Text(mode) },
                                            onClick = {
                                                viewModel.updateThemeMode(mode)
                                                expandedTheme = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = GridDividerLabel.copy(alpha = 0.5f))

                        // Currency Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Active Currency", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                                Text("Standard profile symbol display", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                            Box {
                                OutlinedButton(
                                    onClick = { expandedCurrency = true },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                                ) {
                                    Text(selectedCurrency, fontWeight = FontWeight.Bold)
                                    Icon(Icons.Default.ArrowDropDown, null)
                                }
                                DropdownMenu(
                                    expanded = expandedCurrency,
                                    onDismissRequest = { expandedCurrency = false }
                                ) {
                                    listOf("LKR", "USD", "EUR", "GBP").forEach { code ->
                                        DropdownMenuItem(
                                            text = { Text(code) },
                                            onClick = {
                                                viewModel.changeCurrency(code)
                                                expandedCurrency = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = GridDividerLabel.copy(alpha = 0.5f))

                        // Report Mode Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Dashboard Mode", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                                Text("Toggle Simple, Normal or Expert widgets", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                            Box {
                                OutlinedButton(
                                    onClick = { expandedReportMode = true },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                                ) {
                                    Text(reportMode, fontWeight = FontWeight.Bold)
                                    Icon(Icons.Default.ArrowDropDown, null)
                                }
                                DropdownMenu(
                                    expanded = expandedReportMode,
                                    onDismissRequest = { expandedReportMode = false }
                                ) {
                                    listOf("Simple", "Normal", "Expert", "Custom").forEach { mode ->
                                        DropdownMenuItem(
                                            text = { Text(mode) },
                                            onClick = {
                                                viewModel.updateReportMode(mode)
                                                expandedReportMode = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 💼 SECTION 2: SALARY CONFIGURATOR & 50/30/20 BASE TARGETS
            item {
                val salaryDay by viewModel.salaryDayOfMonth.collectAsState()
                val salaryAmt by viewModel.predictedSalaryAmount.collectAsState()
                val needsVal by viewModel.needsLimit.collectAsState()
                val wantsVal by viewModel.wantsLimit.collectAsState()
                val savingsVal by viewModel.savingsLimit.collectAsState()

                var inputSalaryText by remember { mutableStateOf(salaryAmt.toString()) }
                var inputDayText by remember { mutableStateOf(salaryDay.toString()) }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlassCard(cornerRadius = 20.dp, containerColor = Color.White.copy(alpha = 0.45f)),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "SALARY CIRCLE CONFIGURATOR & TARGETS",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = ElectricNeeds
                        )

                        Text(
                            text = "Configure base payday cycle, active salary raise parameters, and your default Material 50/30/20 caps. Historic accounting capital is insulated automatically during salary raises.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = inputSalaryText,
                                onValueChange = { inputSalaryText = it },
                                label = { Text("Base Salary Amount") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ElectricNeeds,
                                    unfocusedBorderColor = GridDividerLabel
                                ),
                                modifier = Modifier.weight(1.3f)
                            )

                            OutlinedTextField(
                                value = inputDayText,
                                onValueChange = { inputDayText = it },
                                label = { Text("Payday (1-31)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ElectricNeeds,
                                    unfocusedBorderColor = GridDividerLabel
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Button(
                            onClick = {
                                val sAmt = inputSalaryText.toDoubleOrNull()
                                val sDay = inputDayText.toIntOrNull()
                                if (sAmt != null && sDay != null && sDay in 1..31) {
                                    viewModel.updateSalaryAmount(sAmt)
                                    viewModel.updateSalaryDay(sDay)
                                    // Proportionally set target constraints
                                    viewModel.updateNeedsLimit(sAmt * 0.5)
                                    viewModel.updateWantsLimit(sAmt * 0.3)
                                    viewModel.updateSavingsLimit(sAmt * 0.2)
                                    Toast.makeText(context, "Salary raise parameters and 50/30/20 limits updated successfully!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Please enter a valid salary amount and a payday between 1 and 31.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricNeeds),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Apply Salary Raise Configurations", fontWeight = FontWeight.Bold)
                        }

                        HorizontalDivider(color = GridDividerLabel.copy(alpha = 0.5f))

                        Text(
                            text = "ACTIVE 50 / 30 / 20 TARGET LIMITS",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                            color = TextSecondary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("NEEDS (50%)", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                Text(formatLKR(needsVal), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ElectricNeeds)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("WANTS (30%)", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                Text(formatLKR(wantsVal), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = WarmWants)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("SAVINGS (20%)", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                Text(formatLKR(savingsVal), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SavingsIndigo)
                            }
                        }
                    }
                }
            }

            // 🔒 SECTION 3: PASSCODE & NATIVE BIOMETRIC LOCKS
            item {
                val useLock by viewModel.useSecurityLock.collectAsState()
                val useBiometric by viewModel.useBiometricLock.collectAsState()
                val activePin by viewModel.securityPin.collectAsState()
                var inputPin by remember { mutableStateOf(activePin) }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlassCard(cornerRadius = 20.dp, containerColor = Color.White.copy(alpha = 0.45f)),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "PASSCODE & SECURITY LOCKS",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                                    color = ElectricNeeds
                                )
                                Text(
                                    text = "Secure your profiles from unauthorized glances.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                            LiquidGlassToggle(
                                checked = useLock,
                                onCheckedChange = { viewModel.updateSecurityLock(it) }
                            )
                        }

                        if (useLock) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = inputPin,
                                    onValueChange = { if (it.length <= 4) inputPin = it },
                                    label = { Text("4-Digit PIN") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = ElectricNeeds,
                                        unfocusedBorderColor = GridDividerLabel
                                    ),
                                    modifier = Modifier.weight(1f)
                                )

                                Button(
                                    onClick = {
                                        if (inputPin.length == 4 && inputPin.all { it.isDigit() }) {
                                            viewModel.updateSecurityPin(inputPin)
                                            Toast.makeText(context, "Passcode updated successfully!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "PIN must be exactly 4 digits", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ElectricNeeds),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Set PIN", fontWeight = FontWeight.Bold)
                                }
                            }

                            HorizontalDivider(color = GridDividerLabel.copy(alpha = 0.5f))

                            // Actual Biometric / Fingerprint Option
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1.3f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Default.Fingerprint, "Biometric icon", tint = ElectricNeeds, modifier = Modifier.size(20.dp))
                                        Text("Biometric Authentication", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                                    }
                                    Text("Trigger native diagnostic biometric fingerprint scanner upon startup", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                }
                                LiquidGlassToggle(
                                    checked = useBiometric,
                                    onCheckedChange = { viewModel.updateBiometricLock(it) }
                                )
                            }
                        }
                    }
                }
            }

            // 📂 SECTION 4: MANAGE PROFILE LEDGERS
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlassCard(cornerRadius = 20.dp, containerColor = Color.White.copy(alpha = 0.45f)),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "PROFILE MANAGEMENT LIBRARIES",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = ElectricNeeds
                        )

                        // Create profile inputs
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newProfileName,
                                onValueChange = { newProfileName = it },
                                label = { Text("New Profile Name") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ElectricNeeds,
                                    unfocusedBorderColor = GridDividerLabel
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            Button(
                                onClick = {
                                    if (newProfileName.trim().isNotEmpty()) {
                                        val trimmedInput = newProfileName.trim()
                                        if (profilesList.contains(trimmedInput)) {
                                            Toast.makeText(context, "A profile named '$trimmedInput' already exists.", Toast.LENGTH_SHORT).show()
                                        } else {
                                            viewModel.addProfile(trimmedInput)
                                            Toast.makeText(context, "New ledger '$trimmedInput' configured!", Toast.LENGTH_SHORT).show()
                                            newProfileName = ""
                                        }
                                    } else {
                                        Toast.makeText(context, "Enter a profile name first.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricNeeds),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Create", fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Switch profile lists
                        Text(
                            text = "AVAILABLE PROFILES (TAP TO SWITCH):",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                            color = TextSecondary
                        )

                        profilesList.forEach { profile ->
                            val isActive = profile == activeProfile
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isActive) ElectricNeedsBg else Color.White.copy(alpha = 0.2f))
                                    .border(
                                        width = 1.dp,
                                        color = if (isActive) ElectricNeeds else GridDividerLabel.copy(alpha = 0.6f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        if (!isActive) {
                                            viewModel.switchProfile(profile)
                                            Toast.makeText(context, "Switched to profile: $profile", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    .padding(horizontal = 14.dp, vertical = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ManageAccounts,
                                            contentDescription = null,
                                            tint = if (isActive) ElectricNeeds else TextSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = profile,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (isActive) ElectricNeeds else TextPrimary
                                        )
                                    }

                                    if (profile != "Personal" && !isActive) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Profile",
                                            tint = DangerRed,
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clickable { showDeleteConfirmProfile = profile }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 💾 SECTION 5: UNINSTALL-SAFE JSON FILE BACKUPS
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlassCard(cornerRadius = 20.dp, containerColor = Color.White.copy(alpha = 0.45f)),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "UNINSTALL-SAFE JSON FILE BACKUPS",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = ElectricNeeds
                        )

                        Text(
                            text = "Backups are automatically written directly as raw JSON files inside your device's global standard Downloads folder ('LankaBudgetBackups'). These files are completely protected and won't get deleted, even if you accidentally uninstall the application!",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )

                        Button(
                            onClick = {
                                val savedPath = viewModel.saveRobustJsonBackup()
                                if (savedPath != null) {
                                    availableBackups = viewModel.getRobustJsonBackups()
                                    Toast.makeText(context, "Backup JSON file created successfully inside Downloads folder!", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, "Error compiling robust backup JSON.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricNeeds),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.UploadFile, "Save file")
                                Text("Export Safe JSON Backup File", fontWeight = FontWeight.Bold)
                            }
                        }

                        HorizontalDivider(color = GridDividerLabel.copy(alpha = 0.5f))

                        Text(
                            text = "📋 DIRECT COPY & PASTE JSON BACKUP",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                            color = TextSecondary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val backupString = viewModel.exportBackupAsJsonString()
                                    if (backupString != null) {
                                        clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(backupString))
                                        Toast.makeText(context, "Full Backup JSON copied to clipboard!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Error reading backup data", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1.2f),
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricNeeds.copy(alpha = 0.15f), contentColor = ElectricNeeds),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                                    Text("Copy JSON", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }

                            Button(
                                onClick = {
                                    val clipboardText = clipboardManager.getText()?.text
                                    if (!clipboardText.isNullOrBlank()) {
                                        pastedJsonText = clipboardText
                                        Toast.makeText(context, "Pasted backup JSON from clipboard!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(0.8f),
                                colors = ButtonDefaults.buttonColors(containerColor = SurfaceDarkSecondary, contentColor = TextPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.ContentPaste, contentDescription = "Paste")
                                    Text("Paste", fontSize = 12.sp)
                                }
                            }
                        }

                        OutlinedTextField(
                            value = pastedJsonText,
                            onValueChange = { pastedJsonText = it },
                            label = { Text("Backup JSON String") },
                            placeholder = { Text("Paste raw backup JSON string here...") },
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            maxLines = 4,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = ElectricNeeds,
                                unfocusedBorderColor = GridDividerLabel
                            )
                        )

                        Button(
                            onClick = {
                                if (pastedJsonText.isNotBlank()) {
                                    val result = viewModel.importBackupFromJsonString(pastedJsonText.trim())
                                    if (result) {
                                        pastedJsonText = ""
                                        Toast.makeText(context, "Full ledger backup restored successfully from pasted JSON!", Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(context, "Failed to parse/restore backup! Please ensure the JSON is valid.", Toast.LENGTH_LONG).show()
                                    }
                                } else {
                                    Toast.makeText(context, "Please paste or copy code first.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricNeeds),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Restore / Import Pasted JSON", fontWeight = FontWeight.Bold)
                        }

                        if (availableBackups.isNotEmpty()) {
                            HorizontalDivider(color = GridDividerLabel.copy(alpha = 0.5f))

                            Text(
                                text = "AVAILABLE LOCAL RESTORE FILES:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                                color = TextSecondary
                            )

                            availableBackups.take(5).forEach { backup ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White.copy(alpha = 0.15f))
                                        .border(1.dp, GridDividerLabel.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = if (backup.isPublic) Icons.Default.FolderOpen else Icons.Default.CloudQueue,
                                                    contentDescription = null,
                                                    tint = ElectricNeeds,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = if (backup.isPublic) "SDCard Downloads Space" else "Private Sandbox Cache",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextSecondary
                                                )
                                            }

                                            Button(
                                                onClick = {
                                                    val success = viewModel.restoreFromRobustBackup(backup.filePath)
                                                    if (success) {
                                                        Toast.makeText(context, "Full ledger backup restored successfully!", Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        Toast.makeText(context, "Failed to restore backup snapshot.", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = ElectricNeeds.copy(alpha = 0.15f), contentColor = ElectricNeeds),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.height(26.dp)
                                            ) {
                                                Text("Restore", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        Text(
                                            text = backup.name,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 📊 SECTION 6: CATEGORIES & MONTHLY BUDGET OVERRIDES
            item {
                var isExpanded by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlassCard(cornerRadius = 20.dp, containerColor = Color.White.copy(alpha = 0.45f)),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isExpanded = !isExpanded },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "SUBCATEGORIES & BUDGET OVERRIDES",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                                    color = ElectricNeeds
                                )
                                Text(
                                    text = "Manage custom categories and override monthly caps",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Expand info",
                                tint = ElectricNeeds
                            )
                        }

                        if (isExpanded) {
                            var customCatName by remember { mutableStateOf("") }
                            var customCatGroup by remember { mutableStateOf("Needs") }
                            val customCategories by viewModel.customCategories.collectAsState()

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = customCatName,
                                    onValueChange = { customCatName = it },
                                    label = { Text("Subcategory (e.g. Pet)") },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = ElectricNeeds,
                                        unfocusedBorderColor = GridDividerLabel
                                    ),
                                    modifier = Modifier.weight(1.2f)
                                )

                                Box(modifier = Modifier.weight(0.8f)) {
                                    var dropgrpExpanded by remember { mutableStateOf(false) }
                                    OutlinedButton(
                                        onClick = { dropgrpExpanded = true },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                                    ) {
                                        Text(customCatGroup, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(14.dp))
                                    }
                                    DropdownMenu(
                                        expanded = dropgrpExpanded,
                                        onDismissRequest = { dropgrpExpanded = false }
                                    ) {
                                        listOf("Needs", "Wants", "Savings").forEach { g ->
                                            DropdownMenuItem(text = { Text(g) }, onClick = {
                                                customCatGroup = g
                                                dropgrpExpanded = false
                                            })
                                        }
                                    }
                                }

                                Button(
                                    onClick = {
                                        if (customCatName.trim().isNotEmpty()) {
                                            viewModel.addCustomCategory(customCatName.trim(), false, customCatGroup.uppercase())
                                            Toast.makeText(context, "Added subcategory!", Toast.LENGTH_SHORT).show()
                                            customCatName = ""
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ElectricNeeds),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    Text("Add", fontWeight = FontWeight.Bold)
                                }
                            }

                            if (customCategories.isNotEmpty()) {
                                Text("ACTIVE CUSTOM CATEGORIES:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = TextSecondary)
                                customCategories.forEach { cat ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color.White.copy(alpha = 0.15f))
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(cat.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                                            Text("Group: ${cat.bucket}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                        }
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete custom category",
                                            tint = DangerRed,
                                            modifier = Modifier
                                                .size(18.dp)
                                                .clickable {
                                                    viewModel.deleteCustomCategory(cat.name)
                                                    Toast.makeText(context, "Deleted category", Toast.LENGTH_SHORT).show()
                                                }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ⚠️ Danger Zone: FACTORY RESET PROFILE LEDGER
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlassCard(cornerRadius = 20.dp, containerColor = Color(0x11BA1A1A)),
                    border = BorderStroke(1.2.dp, DangerRed.copy(alpha = 0.35f)),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "DANGER ZONE",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = DangerRed
                        )
                        Text(
                            text = "Factory reset permanently clears your entire profile database: split-bill history, recurrence formulas, portfolio assets and ledger transactions.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )

                        Button(
                            onClick = { showAppResetConfirm = true },
                            colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Warning, "Warning")
                                Text("Factory Reset Profile Ledger", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // CONFIRMATION DIALOGS

        if (showDeleteConfirmProfile != null) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmProfile = null },
                title = { Text("Delete Profile ledger?") },
                text = { Text("This will permanently discard profile '${showDeleteConfirmProfile}' and delete its SQLite database. There is no recovery.") },
                confirmButton = {
                    Button(
                        onClick = {
                            val target = showDeleteConfirmProfile
                            if (target != null) {
                                viewModel.deleteProfile(target)
                                Toast.makeText(context, "Profile deleted permanently", Toast.LENGTH_SHORT).show()
                                showDeleteConfirmProfile = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                    ) {
                        Text("Permanently Delete", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmProfile = null }) {
                        Text("Cancel", color = TextSecondary)
                    }
                }
            )
        }

        if (showAppResetConfirm) {
            AlertDialog(
                onDismissRequest = { showAppResetConfirm = false },
                title = { Text("Factory Reset Profile Ledger?") },
                text = { Text("All transaction records, peer debt loans, investment portfolios, and recurrence algorithms under '${activeProfile}' will be permanently deleted.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.factoryResetLedger()
                            Toast.makeText(context, "Current profile ledger reset completely", Toast.LENGTH_SHORT).show()
                            showAppResetConfirm = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                    ) {
                        Text("Reset Everything", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAppResetConfirm = false }) {
                        Text("Cancel", color = TextSecondary)
                    }
                }
            )
        }
    }
}
