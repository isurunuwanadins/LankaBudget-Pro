package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Loan
import com.example.data.model.Investment
import com.example.ui.theme.*
import com.example.ui.components.liquidGlassCard
import com.example.ui.viewmodel.LankaBudgetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtLedgerScreen(
    viewModel: LankaBudgetViewModel,
    modifier: Modifier = Modifier
) {
    val loans by viewModel.loans.collectAsState()
    val totalDebtBalance by viewModel.totalDebtBalance.collectAsState()
    
    val investments by viewModel.investments.collectAsState()
    val totalInvestmentBalance by viewModel.totalInvestmentBalance.collectAsState()

    var activeSubTab by remember { mutableIntStateOf(0) } // 0: Debts, 1: Investments

    var showAddLoanDialog by remember { mutableStateOf(false) }
    var showAddInvestmentDialog by remember { mutableStateOf(false) }
    var showPayDialog by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }
    var editingLoan by remember { mutableStateOf<Loan?>(null) }
    var editingInvestment by remember { mutableStateOf<Investment?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Debt Header (floating liquid-glass panel)
            Box(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 4.dp)
                    .fillMaxWidth()
                    .liquidGlassCard(cornerRadius = 24.dp, containerColor = Color.White.copy(alpha = 0.55f))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(HeaderPillBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Handshake,
                                contentDescription = null,
                                tint = ElectricNeeds,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Financial Portfolio",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.5).sp,
                                    fontSize = 18.sp
                                ),
                                color = TextPrimary
                            )
                            Text(
                                text = "Track your custom peer debts & investments",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Button(
                            onClick = {
                                if (activeSubTab == 0) {
                                    showAddLoanDialog = true
                                } else {
                                    showAddInvestmentDialog = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricNeeds),
                            shape = CircleShape,
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("add_item_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (activeSubTab == 0) "Add Debt" else "Add Asset",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Central Ledger area with spacing for floating navigation
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 110.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Sub-Tabs Segmented Swapper
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlassCard(cornerRadius = 14.dp, containerColor = Color.White.copy(alpha = 0.35f))
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (activeSubTab == 0) Color.White.copy(alpha = 0.62f) else Color.Transparent)
                            .clickable { activeSubTab = 0 }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Debts & Loans",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (activeSubTab == 0) ElectricNeeds else TextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (activeSubTab == 1) Color.White.copy(alpha = 0.62f) else Color.Transparent)
                            .clickable { activeSubTab = 1 }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Asset Portfolio",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (activeSubTab == 1) ElectricNeeds else TextSecondary
                        )
                    }
                }

                // Main Capsule List Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .liquidGlassCard(cornerRadius = 28.dp, containerColor = Color.White.copy(alpha = 0.52f)),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        if (activeSubTab == 0) {
                            // Header info
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "DEBT LEDGER",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.2.sp
                                    ),
                                    color = TextSecondary
                                )
                                Text(
                                    text = "${loans.size} active",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = ElectricNeeds
                                )
                            }

                            HorizontalDivider(color = GridDividerLabel, thickness = 1.dp)

                            // Scrollable list
                            if (loans.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Handshake,
                                            contentDescription = null,
                                            tint = TextSecondary.copy(alpha = 0.6f),
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = "Your matrix is empty",
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                            color = TextPrimary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Add loans or borrowing amounts below to track P2P balances cleanly.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextSecondary,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(horizontal = 16.dp)
                                        )
                                    }
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    contentPadding = PaddingValues(bottom = 12.dp)
                                ) {
                                    items(loans, key = { it.id }) { loan ->
                                        DebtLedgerRow(
                                            loan = loan,
                                            onEdit = { editingLoan = loan },
                                            onDelete = { viewModel.deleteLoan(loan) }
                                        )
                                        HorizontalDivider(color = SurfaceDarkSecondary, thickness = 1.dp)
                                    }
                                }
                            }

                            // Aggregate visual footer (Pinned)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SurfaceDarkSecondary)
                                    .border(
                                        BorderStroke(1.dp, GridDividerLabel),
                                        RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
                                    )
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "TOTAL DEBT BALANCE",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.2.sp
                                            ),
                                            color = TextSecondary
                                        )
                                        Text(
                                            text = formatLKR(totalDebtBalance),
                                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                            color = TextPrimary
                                        )
                                    }

                                    Button(
                                        onClick = { showPayDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = ElectricNeeds),
                                        shape = CircleShape,
                                        enabled = loans.any { !it.isCleared },
                                        modifier = Modifier
                                            .height(40.dp)
                                            .testTag("pay_installment_button")
                                    ) {
                                        Text("Pay Installment", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                            }
                        } else {
                            // Investments Active Portfolio Screen layout
                            // Header info
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "INVESTMENT ASSETS",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.2.sp
                                    ),
                                    color = TextSecondary
                                )
                                Text(
                                    text = "${investments.size} active assets",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = ElectricNeeds
                                )
                            }

                            HorizontalDivider(color = GridDividerLabel, thickness = 1.dp)

                            // Scrollable list
                            if (investments.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            tint = TextSecondary.copy(alpha = 0.6f),
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = "No recorded investments",
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                            color = TextPrimary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Keep tabs on your Money Market Accounts, Fixed Deposits, Stocks, and other assets securely.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextSecondary,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(horizontal = 16.dp)
                                        )
                                    }
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    contentPadding = PaddingValues(bottom = 12.dp)
                                ) {
                                    items(investments, key = { it.id }) { inv ->
                                        InvestmentRow(
                                            investment = inv,
                                            onEdit = { editingInvestment = inv },
                                            onUpdateValue = { newVal ->
                                                viewModel.updateInvestmentValue(inv, newVal)
                                            },
                                            onDelete = { viewModel.deleteInvestment(inv) }
                                        )
                                        HorizontalDivider(color = SurfaceDarkSecondary, thickness = 1.dp)
                                    }
                                }
                            }

                            // Aggregate visual footer (Pinned)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SurfaceDarkSecondary)
                                    .border(
                                        BorderStroke(1.dp, GridDividerLabel),
                                        RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
                                    )
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "TOTAL INVESTED ASSETS",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.2.sp
                                            ),
                                            color = TextSecondary
                                        )
                                        Text(
                                            text = formatLKR(totalInvestmentBalance),
                                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                            color = TextPrimary
                                        )
                                    }

                                    Button(
                                        onClick = { showAddInvestmentDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = ElectricNeeds),
                                        shape = CircleShape,
                                        modifier = Modifier
                                            .height(40.dp)
                                            .testTag("add_investment_footer_button")
                                    ) {
                                        Text("Add Asset", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Primary and secondary interactive dialog triggers
        if (showAddLoanDialog) {
            AddLoanDialog(
                onDismiss = { showAddLoanDialog = false },
                onAdd = { lender, amount ->
                    viewModel.addLoan(lender, amount)
                    showAddLoanDialog = false
                }
            )
        }

        editingLoan?.let { loan ->
            AddLoanDialog(
                onDismiss = { editingLoan = null },
                editingLoan = loan,
                onAdd = { lenderName, amount ->
                    viewModel.updateLoan(
                        loan.copy(lenderName = lenderName, borrowedAmount = amount)
                    )
                    editingLoan = null
                }
            )
        }

        if (showPayDialog) {
            PayInstallmentDialog(
                loans = loans.filter { !it.isCleared },
                onDismiss = { showPayDialog = false },
                onSettle = { loanId, amt ->
                    viewModel.payLoanInstallment(loanId, amt)
                    showPayDialog = false
                }
            )
        }

        if (showAddInvestmentDialog) {
            AddInvestmentDialog(
                onDismiss = { showAddInvestmentDialog = false },
                onAdd = { name, type, invested, current, yield, remarks ->
                    viewModel.addInvestment(name, type, invested, current, yield, remarks)
                    showAddInvestmentDialog = false
                }
            )
        }

        editingInvestment?.let { inv ->
            AddInvestmentDialog(
                onDismiss = { editingInvestment = null },
                editingInvestment = inv,
                onAdd = { name, type, invested, current, yield, remarks ->
                    viewModel.updateInvestment(
                        inv.copy(
                            name = name,
                            type = type,
                            amountInvested = invested,
                            currentValue = current,
                            expectedYield = yield,
                            remarks = remarks
                        )
                    )
                    editingInvestment = null
                }
            )
        }

        // Database reset warnings
        if (showClearConfirm) {
            val titleText = if (activeSubTab == 0) "Clear All Debts?" else "Clear All Investments?"
            val bodyText = if (activeSubTab == 0) {
                "This will permanently clear all recorded lender names, transactions, and repaid installment logs. This cannot be undone."
            } else {
                "This will permanently clear all your saved investment assets, yield records, and accumulated balances. This cannot be undone."
            }
            AlertDialog(
                onDismissRequest = { showClearConfirm = false },
                title = { Text(titleText, color = TextPrimary) },
                text = { Text(bodyText, color = TextSecondary) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (activeSubTab == 0) {
                                viewModel.clearAllLoans()
                            } else {
                                viewModel.clearAllInvestments()
                            }
                            showClearConfirm = false
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = DangerRed)
                    ) {
                        Text("Reset All", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearConfirm = false }) {
                        Text("Cancel", color = TextSecondary)
                    }
                },
                containerColor = SurfaceDark
            )
        }
    }
}

@Composable
fun DebtLedgerRow(
    loan: Loan,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() }
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = loan.lenderName,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
            Text(
                text = "Borrowed: ${formatLKR(loan.borrowedAmount)}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(horizontalAlignment = Alignment.End) {
                if (loan.isCleared) {
                    Text(
                        text = "CLEARED",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = ElectricNeeds
                    )
                    Text(
                        text = "Fully Paid",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                } else {
                    Text(
                        text = "-${formatLKR(loan.remainingAmount)}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = DangerRed
                    )
                    Text(
                        text = "Remaining",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Remove Loan",
                    tint = DangerRed.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// Add Loan Dialog Form Setup
@Composable
fun AddLoanDialog(
    onDismiss: () -> Unit,
    onAdd: (lenderName: String, amount: Double) -> Unit,
    editingLoan: Loan? = null
) {
    var lenderName by remember { mutableStateOf(editingLoan?.lenderName ?: "") }
    var amount by remember { mutableStateOf(editingLoan?.borrowedAmount?.toString() ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .liquidGlassCard(cornerRadius = 24.dp, containerColor = Color.White.copy(alpha = 0.94f)),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (editingLoan != null) "Edit Peer Debt / Loan" else "Add Peer Debt / Loan",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        letterSpacing = (-0.5).sp
                    ),
                    color = ElectricNeeds
                )

                OutlinedTextField(
                    value = lenderName,
                    onValueChange = { lenderName = it },
                    label = { Text("Lender or Debtor Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = ElectricNeeds,
                        unfocusedBorderColor = GridDividerLabel
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("loan_title_input")
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Borrowed Amount (LKR)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = ElectricNeeds,
                        unfocusedBorderColor = GridDividerLabel
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("loan_amount_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amt = amount.toDoubleOrNull() ?: 0.0
                            if (lenderName.isNotBlank() && amt > 0) {
                                onAdd(lenderName, amt)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricNeeds),
                        modifier = Modifier.testTag("submit_loan_button")
                    ) {
                        Text(if (editingLoan != null) "Save" else "Add Debt", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// Settle Installment repayments Form Setup
@Composable
fun PayInstallmentDialog(
    loans: List<Loan>,
    onDismiss: () -> Unit,
    onSettle: (loanId: Int, amount: Double) -> Unit
) {
    var selectedIndex by remember { mutableIntStateOf(0) }
    var payAmount by remember { mutableStateOf("") }
    var activeDropdown by remember { mutableStateOf(false) }

    val activeLoan = loans.getOrNull(selectedIndex)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .liquidGlassCard(cornerRadius = 24.dp, containerColor = Color.White.copy(alpha = 0.94f)),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Repay Active Installment",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        letterSpacing = (-0.5).sp
                    ),
                    color = ElectricNeeds
                )

                if (loans.isEmpty()) {
                    Text(
                        "No pending debts to pay.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                } else {
                    // Custom Selection Dropdown row
                    Column {
                        Text("Select Outstanding Debt", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, GridDividerLabel, RoundedCornerShape(8.dp))
                                .clickable { activeDropdown = !activeDropdown }
                                .padding(12.dp)
                        ) {
                            Text(
                                text = activeLoan?.let { "${it.lenderName} (Remaining: ${formatLKR(it.remainingAmount)})" } ?: "Select Debt",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        if (activeDropdown) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceDarkSecondary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                LazyColumn(modifier = Modifier.height(100.dp)) {
                                    items(loans.size) { index ->
                                        val l = loans[index]
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    selectedIndex = index
                                                    activeDropdown = false
                                                }
                                                .padding(10.dp)
                                        ) {
                                            Text(
                                                text = "${l.lenderName} (${formatLKR(l.remainingAmount)})",
                                                color = TextPrimary,
                                                fontSize = 13.sp,
                                                fontWeight = if (selectedIndex == index) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                        Divider(color = GridDividerLabel)
                                    }
                                }
                            }
                        }
                    }

                    // Payment Amount Input
                    OutlinedTextField(
                        value = payAmount,
                        onValueChange = { payAmount = it },
                        label = { Text("Installment Payment Amount (LKR)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = ElectricNeeds,
                            unfocusedBorderColor = GridDividerLabel
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("payment_amount_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel", color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val amt = payAmount.toDoubleOrNull() ?: 0.0
                                if (activeLoan != null && amt > 0) {
                                    onSettle(activeLoan.id, amt)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricNeeds),
                            modifier = Modifier.testTag("submit_payment_button")
                        ) {
                            Text("Pay Installment", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InvestmentRow(
    investment: Investment,
    onEdit: () -> Unit,
    onUpdateValue: (Double) -> Unit,
    onDelete: () -> Unit
) {
    var showUpdateValueDialog by remember { mutableStateOf(false) }
    var newValueInput by remember { mutableStateOf(investment.currentValue.toString()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() }
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val itemIcon = when (investment.type) {
                    "Money Market" -> Icons.Default.Savings
                    "Fixed Deposit" -> Icons.Default.Lock
                    "Stocks" -> Icons.Default.ShowChart
                    "Crypto" -> Icons.Default.MonetizationOn
                    else -> Icons.Default.AccountBalance
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(HeaderPillBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = itemIcon,
                        contentDescription = null,
                        tint = ElectricNeeds,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = investment.name,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(HeaderPillBg)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = investment.type,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = ElectricNeeds,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                Text(
                    text = "Invested: ${formatLKR(investment.amountInvested)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                if (investment.expectedYield > 0) {
                    Text(
                        text = "Yield: ${investment.expectedYield}% p.a.",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = WarmWants
                    )
                }
            }
            if (investment.remarks.isNotBlank()) {
                Text(
                    text = investment.remarks,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary.copy(alpha = 0.8f)
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.clickable { showUpdateValueDialog = true }
            ) {
                Text(
                    text = formatLKR(investment.currentValue),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = EmeraldIncome
                )
                Text(
                    text = "Update Balance ✎",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextSecondary
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Remove Investment",
                    tint = DangerRed.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }

    if (showUpdateValueDialog) {
        AlertDialog(
            onDismissRequest = { showUpdateValueDialog = false },
            title = { Text("Update Current Value", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter the latest balance of ${investment.name}:", color = TextSecondary)
                    OutlinedTextField(
                        value = newValueInput,
                        onValueChange = { newValueInput = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = ElectricNeeds,
                            unfocusedBorderColor = GridDividerLabel
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("investment_value_update_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val valDouble = newValueInput.toDoubleOrNull() ?: investment.currentValue
                        onUpdateValue(valDouble)
                        showUpdateValueDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricNeeds)
                ) {
                    Text("Update", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUpdateValueDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = SurfaceDark
        )
    }
}

@Composable
fun AddInvestmentDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, type: String, invested: Double, current: Double, yield: Double, remarks: String) -> Unit,
    editingInvestment: Investment? = null
) {
    var name by remember { mutableStateOf(editingInvestment?.name ?: "") }
    var type by remember { mutableStateOf(editingInvestment?.type ?: "Money Market") }
    val types = listOf("Money Market", "Fixed Deposit", "Stocks", "Crypto", "Other")
    var typeDropdownExpanded by remember { mutableStateOf(false) }

    var amountInvested by remember { mutableStateOf(editingInvestment?.amountInvested?.toString() ?: "") }
    var currentValue by remember { mutableStateOf(editingInvestment?.currentValue?.toString() ?: "") }
    var expectedYield by remember { mutableStateOf(editingInvestment?.expectedYield?.toString() ?: "") }
    var remarks by remember { mutableStateOf(editingInvestment?.remarks ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .liquidGlassCard(cornerRadius = 24.dp, containerColor = Color.White.copy(alpha = 0.94f)),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            LazyColumn(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = if (editingInvestment != null) "Edit Asset Investment" else "Add Asset Investment",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            letterSpacing = (-0.5).sp
                        ),
                        color = ElectricNeeds
                    )
                }

                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Investment Name") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            focusedBorderColor = ElectricNeeds,
                            unfocusedBorderColor = GridDividerLabel
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("inv_name_input")
                    )
                }

                item {
                    Column {
                        Text("Investment Instrument", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, GridDividerLabel, RoundedCornerShape(8.dp))
                                .clickable { typeDropdownExpanded = !typeDropdownExpanded }
                                .padding(12.dp)
                        ) {
                            Text(text = type, color = TextPrimary, fontWeight = FontWeight.Bold)
                        }

                        if (typeDropdownExpanded) {
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceDarkSecondary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column {
                                    types.forEach { t ->
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    type = t
                                                    typeDropdownExpanded = false
                                                }
                                                .padding(12.dp)
                                        ) {
                                            Text(text = t, color = TextPrimary)
                                        }
                                        HorizontalDivider(color = GridDividerLabel)
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = amountInvested,
                        onValueChange = { 
                            amountInvested = it
                            if (currentValue.isEmpty()) {
                                currentValue = it
                            }
                        },
                        label = { Text("Amount Invested (LKR)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            focusedBorderColor = ElectricNeeds,
                            unfocusedBorderColor = GridDividerLabel
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("inv_invested_input")
                    )
                }

                item {
                    OutlinedTextField(
                        value = currentValue,
                        onValueChange = { currentValue = it },
                        label = { Text("Current Estimate Value (LKR)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            focusedBorderColor = ElectricNeeds,
                            unfocusedBorderColor = GridDividerLabel
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("inv_current_input")
                    )
                }

                item {
                    OutlinedTextField(
                        value = expectedYield,
                        onValueChange = { expectedYield = it },
                        label = { Text("Annual Yield Rate (%) - Optional") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            focusedBorderColor = ElectricNeeds,
                            unfocusedBorderColor = GridDividerLabel
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("inv_yield_input")
                    )
                }

                item {
                    OutlinedTextField(
                        value = remarks,
                        onValueChange = { remarks = it },
                        label = { Text("Remarks (e.g. Maturity, MMF Fund Name)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            focusedBorderColor = ElectricNeeds,
                            unfocusedBorderColor = GridDividerLabel
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("inv_remarks_input")
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel", color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val investedD = amountInvested.toDoubleOrNull() ?: 0.0
                                val currentD = currentValue.toDoubleOrNull() ?: investedD
                                val yieldD = expectedYield.toDoubleOrNull() ?: 0.0
                                if (name.isNotBlank() && investedD > 0) {
                                    onAdd(name, type, investedD, currentD, yieldD, remarks)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricNeeds),
                            modifier = Modifier.testTag("submit_investment_button")
                        ) {
                            Text(if (editingInvestment != null) "Save" else "Add Asset", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}


