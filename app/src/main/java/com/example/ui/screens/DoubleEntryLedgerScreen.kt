package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.liquidGlassCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.LankaBudgetViewModel

@Composable
fun DoubleEntryLedgerScreen(
    viewModel: LankaBudgetViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Core state aggregates
    val txList by viewModel.transactions.collectAsState()
    val availableBalance by viewModel.availableBalance.collectAsState()
    val totalInvestmentBalance by viewModel.totalInvestmentBalance.collectAsState()
    val totalDebtBalance by viewModel.totalDebtBalance.collectAsState()
    
    val initialCapital by viewModel.initialCapital.collectAsState()
    
    // Allow custom capital injection to reconcile initial balances
    val sharedPrefs = remember { viewModel.getApplication<android.app.Application>().getSharedPreferences("lanka_budget_prefs", android.content.Context.MODE_PRIVATE) }
    var capitalEquityCorrection by remember { mutableStateOf(sharedPrefs.getFloat("capital_equity_correction", 0.0f).toDouble()) }

    // Computations
    val totalCashAsset = availableBalance
    val totalInvestmentsAsset = totalInvestmentBalance
    val totalAssets = totalCashAsset + totalInvestmentsAsset
    
    val totalExpenses = txList.filter { !it.isIncome }.sumOf { it.amount }
    val totalIncome = txList.filter { it.isIncome }.sumOf { it.amount }
    val totalLiabilities = totalDebtBalance
    
    // Capital is comprised of the standard initial beginning capital amount + any manual adjustments (retains balance even if salary gets a raise configuration!)
    val totalCapital = initialCapital + capitalEquityCorrection

    // LHS (Debit Elements) = Assets + Expenses
    val leftHandSide = totalAssets + totalExpenses
    
    // RHS (Credit Elements) = Capital + Income + Liabilities
    val rightHandSide = totalCapital + totalIncome + totalLiabilities
    
    val variance = kotlin.math.abs(leftHandSide - rightHandSide)
    val isBalanced = variance < 0.01

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Double-entry explanation pill
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlassCard(cornerRadius = 16.dp, containerColor = Color.White.copy(alpha = 0.5f)),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Balance,
                            contentDescription = null,
                            tint = ElectricNeeds
                        )
                        Text(
                            text = "Double-Entry Credit-Debit Balance Audit",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                    }
                    Text(
                        text = "Under professional double-entry accounting standards, your ledger must obey the system's golden balance equation:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceDarkSecondary)
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Asset + Expenses = Capital + Income + Liabilities",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = ElectricNeeds
                        )
                    }
                }
            }
        }

        // Ledger status auditor card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlassCard(cornerRadius = 20.dp, containerColor = Color.White.copy(alpha = 0.6f)),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (isBalanced) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(SettlementSuccess.copy(alpha = 0.15f))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SettlementSuccess,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "AUDIT STANDARDS MET • PERFECTLY BALANCED",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = SettlementSuccess
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(DangerRed.copy(alpha = 0.15f))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = DangerRed,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "UNRECONCILED AUDIT VARIANCE: ${formatLKR(variance)}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = DangerRed
                                )
                            }
                        }
                    }

                    // Comparison visual scales
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Left Debit elements panel (Assets + Expenses)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White.copy(alpha = 0.3f))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                "DEBIT ELEMENTS (LHS)",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = formatLKR(leftHandSide),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = ElectricNeeds
                            )
                            HorizontalDivider(color = GridDividerLabel, thickness = 1.dp)
                            
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("Total Assets:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                Text(formatLKR(totalAssets), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                            }
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("Expenses:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                Text(formatLKR(totalExpenses), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                            }
                        }

                        // Right Credit elements panel (Capital + Income + Liabilities)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White.copy(alpha = 0.3f))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                "CREDIT ELEMENTS (RHS)",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = formatLKR(rightHandSide),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = SavingsIndigo
                            )
                            HorizontalDivider(color = GridDividerLabel, thickness = 1.dp)

                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("Total Capital:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                Text(formatLKR(totalCapital), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                            }
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("Income:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                Text(formatLKR(totalIncome), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                            }
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("Liabilities:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                Text(formatLKR(totalLiabilities), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                            }
                        }
                    }

                    if (!isBalanced) {
                        Button(
                            onClick = {
                                // Auto calculate correct equity correction so that LHS = RHS
                                // LHS = Total Assets + Total Expenses
                                // RHS = (Base Salary + Correction) + Total Income + Total Liabilities
                                // Correction = LHS - initialCapital - Total Income - Total Liabilities
                                val correctCorrection = leftHandSide - initialCapital - totalIncome - totalLiabilities
                                capitalEquityCorrection = correctCorrection
                                sharedPrefs.edit().putFloat("capital_equity_correction", correctCorrection.toFloat()).apply()
                                Toast.makeText(context, "Ledger reconciled! Capital adjusted by ${formatLKR(correctCorrection)}", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricNeeds),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Reconcile Ledger via Capital Adjustment", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Professional accounting rules & Acronym panels (DEAD & GIRLS)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlassCard(cornerRadius = 16.dp, containerColor = Color.White.copy(alpha = 0.5f)),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "📒 THE DOUBLE-ENTRY GOLDEN RULES: 'DEAD' & 'GIRLS'",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = ElectricNeeds
                    )
                    Text(
                        "To easily track whether a transaction is a Debit or a Credit, use these corporate compliance acronyms:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // DEAD Panel
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ElectricNeeds.copy(alpha = 0.1f))
                                .border(1.dp, ElectricNeeds.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                "🔥 1. DEAD (Increases with DEBIT)",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = ElectricNeeds
                            )
                            Text("• Dividend (Draws)", style = MaterialTheme.typography.labelSmall, color = TextPrimary)
                            Text("• Expenses", style = MaterialTheme.typography.labelSmall, color = TextPrimary)
                            Text("• Assets (e.g. Cash)", style = MaterialTheme.typography.labelSmall, color = TextPrimary)
                            Text("• Losses", style = MaterialTheme.typography.labelSmall, color = TextPrimary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Rule: Debits INCREASE. Credits DECREASE.",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                                color = TextSecondary
                            )
                        }

                        // GIRLS Panel
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SavingsIndigo.copy(alpha = 0.1f))
                                .border(1.dp, SavingsIndigo.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                "💎 2. GIRLS (Increases with CREDIT)",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = SavingsIndigo
                            )
                            Text("• Gains", style = MaterialTheme.typography.labelSmall, color = TextPrimary)
                            Text("• Income & Revenue", style = MaterialTheme.typography.labelSmall, color = TextPrimary)
                            Text("• Revenues", style = MaterialTheme.typography.labelSmall, color = TextPrimary)
                            Text("• Liabilities (Loans)", style = MaterialTheme.typography.labelSmall, color = TextPrimary)
                            Text("• Stockholders' Equity", style = MaterialTheme.typography.labelSmall, color = TextPrimary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Rule: Credits INCREASE. Debits DECREASE.",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }

        // Expanded T-Account Ending Balances
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlassCard(cornerRadius = 20.dp, containerColor = Color.White.copy(alpha = 0.55f)),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        "📊 INDIVIDUAL ACCOUNT TYPE BALANCES (T-LEDGER)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = ElectricNeeds
                    )
                    Text(
                        "Calculated Ending Balances using the Expanded Formula:\n" +
                        "• DEAD Type Balance = Beginning + Debits - Credits\n" +
                        "• GIRLS Type Balance = Beginning + Credits - Debits",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    // Assets ledger
                    TAccountRow(
                        accountName = "Assets (DEAD)",
                        beginningBalance = totalCapital,
                        totalDebits = totalIncome,
                        totalCredits = totalExpenses + (initialCapital - totalCashAsset + totalInvestmentsAsset).coerceAtLeast(0.0),
                        endingBalance = totalAssets,
                        isDeadType = true
                    )

                    HorizontalDivider(color = GridDividerLabel.copy(alpha = 0.3f))

                    // Expenses ledger
                    TAccountRow(
                        accountName = "Expenses (DEAD)",
                        beginningBalance = 0.0,
                        totalDebits = totalExpenses,
                        totalCredits = 0.0,
                        endingBalance = totalExpenses,
                        isDeadType = true
                    )

                    HorizontalDivider(color = GridDividerLabel.copy(alpha = 0.3f))

                    // Liabilities Ledger
                    TAccountRow(
                        accountName = "Liabilities (GIRLS)",
                        beginningBalance = 0.0,
                        totalDebits = txList.filter { !it.isIncome && it.category == "Mandatory Debt" }.sumOf { it.amount },
                        totalCredits = totalLiabilities + txList.filter { !it.isIncome && it.category == "Mandatory Debt" }.sumOf { it.amount },
                        endingBalance = totalLiabilities,
                        isDeadType = false
                    )

                    HorizontalDivider(color = GridDividerLabel.copy(alpha = 0.3f))

                    // Owner's Equity / Capital Ledger
                    TAccountRow(
                        accountName = "Equity (GIRLS)",
                        beginningBalance = initialCapital,
                        totalDebits = 0.0,
                        totalCredits = capitalEquityCorrection,
                        endingBalance = totalCapital,
                        isDeadType = false
                    )

                    HorizontalDivider(color = GridDividerLabel.copy(alpha = 0.3f))

                    // Revenue / Income Ledger
                    TAccountRow(
                        accountName = "Revenue/Income (GIRLS)",
                        beginningBalance = 0.0,
                        totalDebits = 0.0,
                        totalCredits = totalIncome,
                        endingBalance = totalIncome,
                        isDeadType = false
                    )
                }
            }
        }

        // Professional audit plan break downs
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlassCard(cornerRadius = 16.dp, containerColor = Color.White.copy(alpha = 0.45f)),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "ASSET CREDIT TO DEBIT TRANSIT PLAN",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = ElectricNeeds
                    )
                    Text(
                        "How transactional actions impact each side:",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    
                    listOf(
                        "Adding Income (+)" to "Increases current Cash Assets (LHS) and increases total registered Income (RHS). Both sides rise equally.",
                        "Adding Expense (-)" to "Decreases Cash Assets (LHS) and increases registered Expenses (LHS). LHS remains balanced internally.",
                        "Borrowing Money (+)" to "Increases Cash Assets (LHS) and increases Outstanding Peer Liabilities (RHS). Both sides rise equally.",
                        "Shedding Debt (-)" to "Decreases Cash Assets (LHS) and reduces Liabilities (RHS). Both sides decrease equal amounts."
                    ).forEach { (caption, description) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White.copy(alpha = 0.25f))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(ElectricNeedsBg)
                                    .padding(vertical = 4.dp, horizontal = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(caption, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = ElectricNeeds)
                            }
                            Text(description, style = MaterialTheme.typography.bodySmall, color = TextSecondary, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TAccountRow(
    accountName: String,
    beginningBalance: Double,
    totalDebits: Double,
    totalCredits: Double,
    endingBalance: Double,
    isDeadType: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.15f))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = accountName,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
            val typeLabel = if (isDeadType) "DEAD Formula" else "GIRLS Formula"
            Text(
                text = typeLabel,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = TextSecondary
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Beginning", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                Text(formatLKR(beginningBalance), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Debits (+)", style = MaterialTheme.typography.labelSmall, color = ElectricNeeds)
                Text("+${formatLKR(totalDebits)}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = ElectricNeeds)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Credits (-)", style = MaterialTheme.typography.labelSmall, color = SavingsIndigo)
                Text("-${formatLKR(totalCredits)}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = SavingsIndigo)
            }
            Column(modifier = Modifier.weight(1.2f)) {
                Text("Ending Bal", style = MaterialTheme.typography.labelSmall, color = TextPrimary)
                Text(
                    text = formatLKR(endingBalance),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = if (isDeadType) ElectricNeeds else SavingsIndigo
                )
            }
        }
    }
}

