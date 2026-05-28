package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.ManageAccounts
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.liquidGlassCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.LankaBudgetViewModel

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

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // Local Backups list state
    var localBackups by remember { mutableStateOf(viewModel.getLocalBackups()) }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = 24.dp, // clean top spacing
                bottom = 120.dp, // plenty of clearance for bottom nav bar
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Screen Header & Avatar Display Option
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
                        // Profile Avatar
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(HeaderPillBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Active Profile Avatar",
                                tint = ElectricNeeds,
                                modifier = Modifier.size(52.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Settings Ledger Hub",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.5).sp
                                ),
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Current Active Profile: $activeProfile",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = ElectricNeeds,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Multi-Profile Ledgers Panel
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlassCard(cornerRadius = 20.dp, containerColor = Color.White.copy(alpha = 0.45f)),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "MULTI-PROFILE LEDGERS",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = ElectricNeeds
                        )
                        
                        Text(
                            text = "Keep your self-employment income, household balance sheets, and savings separate.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )

                        // Add profile action row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newProfileName,
                                onValueChange = { newProfileName = it },
                                label = { Text("New Profile Name") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedBorderColor = ElectricNeeds,
                                    unfocusedBorderColor = GridDividerLabel
                                ),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    if (newProfileName.isNotBlank()) {
                                        viewModel.addProfile(newProfileName)
                                        newProfileName = ""
                                        Toast.makeText(context, "New profile added!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricNeeds),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Create", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 📈 Category Spending Cap Thresholds
            item {
                val needsLimit by viewModel.needsLimit.collectAsState()
                val wantsLimit by viewModel.wantsLimit.collectAsState()
                val savingsLimit by viewModel.savingsLimit.collectAsState()

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlassCard(cornerRadius = 20.dp, containerColor = Color.White.copy(alpha = 0.45f)),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "EXPENSE CAP ALERT THRESHOLDS",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = ElectricNeeds
                        )
                        Text(
                            text = "Define monthly spending limits for each division. Dashboard concentric rings will transition to crimson red and pulse when approaching limits.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = if (needsLimit == 0.0) "" else needsLimit.toInt().toString(),
                                onValueChange = { newValue ->
                                    val limit = newValue.toDoubleOrNull() ?: 0.0
                                    viewModel.updateNeedsLimit(limit)
                                },
                                label = { Text("Needs Limit") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ElectricNeeds,
                                    unfocusedBorderColor = GridDividerLabel
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = if (wantsLimit == 0.0) "" else wantsLimit.toInt().toString(),
                                onValueChange = { newValue ->
                                    val limit = newValue.toDoubleOrNull() ?: 0.0
                                    viewModel.updateWantsLimit(limit)
                                },
                                label = { Text("Wants Limit") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = WarmWants,
                                    unfocusedBorderColor = GridDividerLabel
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = if (savingsLimit == 0.0) "" else savingsLimit.toInt().toString(),
                                onValueChange = { newValue ->
                                    val limit = newValue.toDoubleOrNull() ?: 0.0
                                    viewModel.updateSavingsLimit(limit)
                                },
                                label = { Text("Savings Limit") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SavingsIndigo,
                                    unfocusedBorderColor = GridDividerLabel
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 💼 Salary Configuration Section
            item {
                val salaryDay by viewModel.salaryDayOfMonth.collectAsState()
                val salaryAmt by viewModel.predictedSalaryAmount.collectAsState()

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlassCard(cornerRadius = 20.dp, containerColor = Color.White.copy(alpha = 0.45f)),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "SALARY CYCLE CONFIGURATOR",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = ElectricNeeds
                        )
                        Text(
                            text = "Set your standard recurring salary payday (1-31) and monthly expected salary base. These values are used to compute predictions when matching transactions aren't found in your active ledger.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = salaryDay.toString(),
                                onValueChange = { newValue ->
                                    val day = newValue.toIntOrNull() ?: 25
                                    viewModel.updateSalaryDay(day.coerceIn(1, 31))
                                },
                                label = { Text("Payday (1-31)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ElectricNeeds,
                                    unfocusedBorderColor = GridDividerLabel
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = if (salaryAmt == 0.0) "" else salaryAmt.toInt().toString(),
                                onValueChange = { newValue ->
                                    val amt = newValue.toDoubleOrNull() ?: 0.0
                                    viewModel.updateSalaryAmount(amt)
                                },
                                label = { Text("Base Salary") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ElectricNeeds,
                                    unfocusedBorderColor = GridDividerLabel
                                ),
                                modifier = Modifier.weight(1.3f)
                            )
                        }
                    }
                }
            }

            // 🪙 Global Currency Switcher
            item {
                val selectedCurrency by viewModel.selectedCurrency.collectAsState()

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlassCard(cornerRadius = 20.dp, containerColor = Color.White.copy(alpha = 0.45f)),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "DUAL-CURRENCY CONVERTER BASE",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = ElectricNeeds
                        )
                        Text(
                            text = "Toggle your preferred visual display currency. Base values are dynamically converted at stable mid-market rates for expat budgeting.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val currencies = listOf("LKR", "USD", "EUR")
                            currencies.forEach { curr ->
                                val isSelected = curr == selectedCurrency
                                Button(
                                    onClick = { viewModel.changeCurrency(curr) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) ElectricNeeds else Color.White.copy(alpha = 0.6f)
                                    ),
                                    border = BorderStroke(1.dp, if (isSelected) ElectricNeeds else GridDividerLabel),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = curr,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 📥 CSV Spreadsheet Export
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlassCard(cornerRadius = 20.dp, containerColor = Color.White.copy(alpha = 0.45f)),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "SPREADSHEET CSV EXPORTS",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = ElectricNeeds
                        )
                        Text(
                            text = "Export your entire active profile's ledger history as a standard .csv spreadsheet you can open straight in Microsoft Excel, Apple Numbers, or Google Sheets.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )

                        Button(
                            onClick = {
                                val csvContent = viewModel.exportTransactionsToCSV()
                                if (csvContent.isNotBlank()) {
                                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(csvContent))
                                    Toast.makeText(context, "Spreadsheet CSV successfully copied to clipboard!", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, "No transactions recorded yet to compile CSV", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricNeedsBg),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Export Active History to CSV", color = ElectricNeeds, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Render profiles list as separate visual items
            item {
                Text(
                    text = "AVAILABLE PROFILES (TAP TO SWITCH)",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                    color = TextSecondary,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            items(profilesList) { profile ->
                val isActive = profile == activeProfile
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (!isActive) {
                                viewModel.switchProfile(profile)
                                Toast.makeText(context, "Switched to profile: $profile", Toast.LENGTH_SHORT).show()
                            }
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isActive) ElectricNeedsBg else Color.White.copy(alpha = 0.6f)
                    ),
                    border = BorderStroke(1.dp, if (isActive) ElectricNeeds else GridDividerLabel),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ManageAccounts,
                                contentDescription = null,
                                tint = if (isActive) ElectricNeeds else TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = profile,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isActive) ElectricNeeds else TextPrimary
                            )
                            if (isActive) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(ElectricNeeds)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "ACTIVE",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 8.sp
                                        )
                                    )
                                }
                            }
                        }
                        
                        // Delete option if not "Personal" profile
                        if (profile != "Personal" && !isActive) {
                            IconButton(
                                onClick = { showDeleteConfirmProfile = profile },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Profile",
                                    tint = DangerRed,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Data Backups section
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlassCard(cornerRadius = 20.dp, containerColor = Color.White.copy(alpha = 0.45f)),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "DATA BACKUP & RESTORE",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = ElectricNeeds
                        )
                        
                        Text(
                            text = "You can export/import all your transactions safely as structural JSON raw code.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val backupStr = viewModel.exportBackupAsJsonString()
                                    if (backupStr != null) {
                                        clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(backupStr))
                                        Toast.makeText(context, "Backup JSON copied to clipboard!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Failed to export backup JSON.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.7f)),
                                border = BorderStroke(1.dp, GridDividerLabel),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Copy JSON Backup", color = TextPrimary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }

                            Button(
                                onClick = {
                                    val clipboardText = clipboardManager.getText()?.text
                                    if (!clipboardText.isNullOrBlank()) {
                                        val ok = viewModel.importBackupFromJsonString(clipboardText)
                                        if (ok) {
                                            Toast.makeText(context, "Backup imported successfully!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Failed to parse JSON backup from clipboard.", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        Toast.makeText(context, "Clipboard is empty.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.7f)),
                                border = BorderStroke(1.dp, GridDividerLabel),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Paste & Import JSON", color = TextPrimary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Offline harddisk snapshot backup
                        Button(
                            onClick = {
                                val ok = viewModel.saveLocalBackup()
                                if (ok) {
                                    localBackups = viewModel.getLocalBackups()
                                    Toast.makeText(context, "Local snapshot saved successfully!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Failed to save local snapshot.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricNeedsBg),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Create Harddisk Snapshot", color = ElectricNeeds, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Local snapshot lists rendering
            if (localBackups.isNotEmpty()) {
                item {
                    Text(
                        text = "AVAILABLE OFFLINE SNAPSHOTS",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                items(localBackups) { filename ->
                    val prettyDate = formatBackupName(filename)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.45f)),
                        border = BorderStroke(1.dp, GridDividerLabel),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = prettyDate,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                                Text(
                                    text = filename,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            TextButton(
                                onClick = {
                                    val ok = viewModel.restoreFromLocalBackup(filename)
                                    if (ok) {
                                        Toast.makeText(context, "Snapshot backup restored successfully!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Failed to restore snapshot.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                colors = ButtonDefaults.textButtonColors(contentColor = ElectricNeeds)
                            ) {
                                Text("Restore", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Factory Reset & Destruction panel
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlassCard(cornerRadius = 20.dp, containerColor = Color.White.copy(alpha = 0.45f)),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.15f))
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "DESTRUCTIVE ZONE",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = DangerRed
                        )
                        Text(
                            text = "Factory reset all data logs, transactions, loans, and portfolios under this current profile.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(
                            onClick = { showAppResetConfirm = true },
                            colors = ButtonDefaults.buttonColors(containerColor = DangerRed.copy(alpha = 0.15f)),
                            border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Reset All Current Profile Data", color = DangerRed, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Confirmation dialog overlay
    if (showDeleteConfirmProfile != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmProfile = null },
            title = { Text("Delete Profile ledger?") },
            text = { Text("This will permanently discard profile '${showDeleteConfirmProfile}' and delete its SQLite database. There is no recovery.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmProfile?.let {
                            viewModel.deleteProfile(it)
                        }
                        showDeleteConfirmProfile = null
                        Toast.makeText(context, "Profile deleted permanently", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = DangerRed)
                ) {
                    Text("Permanently Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmProfile = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = SurfaceDark
        )
    }

    if (showAppResetConfirm) {
        AlertDialog(
            onDismissRequest = { showAppResetConfirm = false },
            title = { Text("Factory Reset Profile Ledger?") },
            text = { Text("All transaction records, peer debt loans, investment portfolios, and recurrence algorithms under '${activeProfile}' will be permanently deleted.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllHistory()
                        viewModel.clearAllLoans()
                        viewModel.clearAllRecurringTransactions()
                        viewModel.clearAllInvestments()
                        showAppResetConfirm = false
                        Toast.makeText(context, "Current profile ledger reset completely", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = DangerRed)
                ) {
                    Text("Reset Everything", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAppResetConfirm = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = SurfaceDark
        )
    }
}
