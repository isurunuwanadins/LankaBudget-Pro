package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Warning
import com.example.ui.components.InteractiveLineChart
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Transaction
import com.example.ui.components.JointBudgetConcentricRings
import com.example.ui.components.liquidGlassCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.BudgetSummary
import com.example.ui.viewmodel.LankaBudgetViewModel

var appCurrency: String = "LKR"

fun formatLKR(amount: Double): String {
    val rate = when (appCurrency) {
        "USD" -> 1.0 / 300.0
        "EUR" -> 1.0 / 325.0
        else -> 1.0
    }
    val converted = amount * rate
    val symbol = when (appCurrency) {
        "USD" -> "$"
        "EUR" -> "€"
        else -> "LKR"
    }
    return if (symbol == "LKR") {
        if (converted % 1.0 == 0.0) {
            String.format("%,.0f $symbol", converted)
        } else {
            String.format("%,.2f $symbol", converted)
        }
    } else {
        if (converted % 1.0 == 0.0) {
            String.format("%s%,.0f", symbol, converted)
        } else {
            String.format("%s%,.2f", symbol, converted)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: LankaBudgetViewModel,
    modifier: Modifier = Modifier
) {
    val transactions by viewModel.transactions.collectAsState()
    val budgetSummary by viewModel.budgetSummaryState.collectAsState()
    val availableBalance by viewModel.availableBalance.collectAsState()
    val totalInvestmentBalance by viewModel.totalInvestmentBalance.collectAsState()
    val totalDebtBalance by viewModel.totalDebtBalance.collectAsState()
    val netWorth by viewModel.netWorth.collectAsState()

    val activeProfile by viewModel.activeProfile.collectAsState()
    val profilesList by viewModel.profilesList.collectAsState()

    val needsLimit by viewModel.needsLimit.collectAsState()
    val wantsLimit by viewModel.wantsLimit.collectAsState()
    val savingsLimit by viewModel.savingsLimit.collectAsState()

    val needsWarning = budgetSummary.needsExpenses >= needsLimit * 0.85 && needsLimit > 0
    val wantsWarning = budgetSummary.wantsExpenses >= wantsLimit * 0.85 && wantsLimit > 0
    val savingsWarning = budgetSummary.savingsExpenses >= savingsLimit * 0.85 && savingsLimit > 0

    var showAddIncomeDialog by remember { mutableStateOf(false) }
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<Transaction?>(null) }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Content Area scrolling behind floating bars
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = 132.dp, // Clears the floating top bar cleanly with ample notch breathing room
                bottom = 110.dp, // Clears the floating bottom navigation perfectly
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Primary Balance Card
            item {
                PrimaryBalanceCard(
                    balance = availableBalance,
                    onAddIncome = { showAddIncomeDialog = true },
                    onLogExpense = { showAddExpenseDialog = true }
                )
            }

            // Balance Sheet Overview Card (Net Worth, Investments and Debt)
            item {
                BalanceSheetOverviewCard(
                    netWorth = netWorth,
                    investments = totalInvestmentBalance,
                    debt = totalDebtBalance
                )
            }

            // 50/30/20 Allocation Status Grid
            item {
                BudgetThreeColumnsAllocation(budgetSummary)
            }

            // Horizontally Scrollable Cards Row
            item {
                Text(
                    text = "LANKABUDGET FORECAST & MATRIX HUBS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = TextSecondary,
                    modifier = Modifier.padding(horizontal = 4.dp).padding(top = 4.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    val salaryData = viewModel.getSalaryDatesAndPredictions()
                    val currentSalaryDate = salaryData.first
                    val nextSalaryDate = salaryData.second
                    val salaryPrediction = salaryData.third

                    item {
                        SalaryPredictionCard(
                            currentSalaryDate = currentSalaryDate,
                            nextSalaryDate = nextSalaryDate,
                            salaryPrediction = salaryPrediction,
                            projections = viewModel.getFutureSalaryProjections(),
                            modifier = Modifier.width(320.dp)
                        )
                    }

                    item {
                        AllocationStateMatrixCard(
                            summary = budgetSummary,
                            isNeedsWarning = needsWarning,
                            isWantsWarning = wantsWarning,
                            isSavingsWarning = savingsWarning,
                            modifier = Modifier.width(320.dp)
                        )
                    }
                }
            }

            // Visual Cashflow Trend Chart
            item {
                InteractiveLineChart(
                    dataPoints = viewModel.getBalanceTrajectory(),
                    modifier = Modifier.fillMaxWidth().height(265.dp)
                )
            }

            // List Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Transaction History",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.3).sp
                        ),
                        color = TextPrimary
                    )
                    Text(
                        text = "${transactions.size} logs",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = TextSecondary
                    )
                }
            }

            // Transaction Rows
            if (transactions.isEmpty()) {
                item {
                    EmptyHistoryPlaceholder()
                }
            } else {
                items(transactions, key = { it.id }) { transaction ->
                    TransactionRowItem(
                        transaction = transaction,
                        onEdit = { editingTransaction = transaction },
                        onDelete = { viewModel.deleteTransaction(transaction) }
                    )
                }
            }
        }

        // Floating Header top overlay
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
        ) {
            CleanHeader(
                activeProfile = activeProfile
            )
        }

        // Dialog Popups
        if (showAddIncomeDialog) {
            AddIncomeDialog(
                onDismiss = { showAddIncomeDialog = false },
                onAdd = { title, amount, category ->
                    viewModel.addIncome(title, amount, category)
                    showAddIncomeDialog = false
                }
            )
        }

        if (showAddExpenseDialog) {
            AddExpenseDialog(
                onDismiss = { showAddExpenseDialog = false },
                onAdd = { title, amount, bucket, category ->
                    viewModel.addExpense(title, amount, bucket, category)
                    showAddExpenseDialog = false
                },
                needsMultiplier = budgetSummary.totalIncome > 0
            )
        }

        editingTransaction?.let { tx ->
            if (tx.isIncome) {
                AddIncomeDialog(
                    onDismiss = { editingTransaction = null },
                    editingTransaction = tx,
                    onAdd = { title, amount, category ->
                        viewModel.updateTransaction(
                            tx.copy(title = title, amount = amount, category = category)
                        )
                        editingTransaction = null
                    }
                )
            } else {
                AddExpenseDialog(
                    onDismiss = { editingTransaction = null },
                    needsMultiplier = false,
                    editingTransaction = tx,
                    onAdd = { title, amount, bucket, category ->
                        viewModel.updateTransaction(
                            tx.copy(title = title, amount = amount, bucket = bucket, category = category)
                        )
                        editingTransaction = null
                    }
                )
            }
        }

    }
}

@Composable
fun CleanHeader(
    activeProfile: String
) {
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
                // Circular branding badge
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(HeaderPillBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ManageAccounts,
                        contentDescription = null,
                        tint = ElectricNeeds,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "LankaBudget Pro",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp,
                                fontSize = 18.sp
                            ),
                            color = TextPrimary
                        )
                        // Beautiful active profile pill badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(ElectricNeedsBg)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = activeProfile,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = ElectricNeeds,
                                fontSize = 9.sp
                            )
                        }
                    }
                    Text(
                        text = "50/30/20 Minimalist Utility",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun BalanceSheetOverviewCard(
    netWorth: Double,
    investments: Double,
    debt: Double
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("balance_sheet_card")
            .liquidGlassCard(cornerRadius = 24.dp, containerColor = Color.White.copy(alpha = 0.52f)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ESTIMATED NET WORTH",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        ),
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formatLKR(netWorth),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp
                        ),
                        color = if (netWorth >= 0) ElectricNeeds else DangerRed
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (netWorth >= 0) ElectricNeedsBg else SavingsIndigoBg)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (netWorth >= 0) "HEALTHY" else "DEBT HEAVY",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (netWorth >= 0) ElectricNeeds else DangerRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = GridDividerLabel, thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Investments Asset
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(SavingsIndigo)
                        )
                        Text(
                            text = "Investments",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatLKR(investments),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                }

                // Outstanding Debt
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(DangerRed)
                        )
                        Text(
                            text = "Outstanding Loans",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatLKR(debt),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = DangerRed
                    )
                }
            }
        }
    }
}

@Composable
fun PrimaryBalanceCard(
    balance: Double,
    onAddIncome: () -> Unit,
    onLogExpense: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .liquidGlassCard(cornerRadius = 28.dp, containerColor = EmeraldIncome.copy(alpha = 0.72f), borderColor = Color.White.copy(alpha = 0.6f)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "AVAILABLE BALANCE",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                ),
                color = Color.White.copy(alpha = 0.8f)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = if (balance % 1.0 == 0.0) {
                        String.format("%,.0f", balance)
                    } else {
                        String.format("%,.2f", balance)
                    },
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 38.sp,
                        letterSpacing = (-1).sp
                    ),
                    color = Color.White
                )
                Text(
                    text = "LKR",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onAddIncome,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("log_income_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.2f),
                        contentColor = Color.White
                    ),
                    shape = CircleShape
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Income", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                }

                Button(
                    onClick = onLogExpense,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("log_expense_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.2f),
                        contentColor = Color.White
                    ),
                    shape = CircleShape
                ) {
                    Icon(imageVector = Icons.Default.TrendingDown, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Log Expense", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}

@Composable
fun BudgetThreeColumnsAllocation(summary: BudgetSummary) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "BUDGET ALLOCATION (50/30/20)",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = TextSecondary,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Needs Column (50%)
            val needsRatio = if (summary.needsBudget > 0.0) {
                (summary.needsExpenses / summary.needsBudget).toFloat().coerceIn(0f, 1f)
            } else 0.0f
            Box(
                modifier = Modifier
                    .weight(1f)
                    .liquidGlassCard(cornerRadius = 16.dp, containerColor = ElectricNeedsBg.copy(alpha = 0.58f), borderColor = ElectricNeeds.copy(alpha = 0.4f))
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "NEEDS (50%)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = ElectricNeeds
                    )
                    LinearProgressIndicator(
                        progress = { needsRatio },
                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                        color = ElectricNeeds,
                        trackColor = ElectricNeedsBorder
                    )
                    Text(
                        text = if (summary.needsBudget > 0.0) "${(needsRatio * 100).toInt()}% Used" else "0% Used",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                }
            }

            // Wants Column (30%)
            val wantsRatio = if (summary.wantsBudget > 0.0) {
                (summary.wantsExpenses / summary.wantsBudget).toFloat().coerceIn(0f, 1f)
            } else 0.0f
            Box(
                modifier = Modifier
                    .weight(1f)
                    .liquidGlassCard(cornerRadius = 16.dp, containerColor = WarmWantsBg.copy(alpha = 0.58f), borderColor = WarmWants.copy(alpha = 0.4f))
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "WANTS (30%)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = WarmWants
                    )
                    LinearProgressIndicator(
                        progress = { wantsRatio },
                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                        color = WarmWants,
                        trackColor = WarmWantsBorder
                    )
                    Text(
                        text = if (summary.wantsBudget > 0.0) "${(wantsRatio * 100).toInt()}% Used" else "0% Used",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                }
            }

            // Savings Column (20%)
            val savingsRatio = if (summary.savingsBudget > 0.0) {
                (summary.savingsExpenses / summary.savingsBudget).toFloat().coerceIn(0f, 1f)
            } else 0.0f
            Box(
                modifier = Modifier
                    .weight(1f)
                    .liquidGlassCard(cornerRadius = 16.dp, containerColor = SavingsIndigoBg.copy(alpha = 0.58f), borderColor = SavingsIndigo.copy(alpha = 0.4f))
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "SAVINGS (20%)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = SavingsIndigo
                    )
                    LinearProgressIndicator(
                        progress = { savingsRatio },
                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                        color = SavingsIndigo,
                        trackColor = SavingsIndigoBorder
                    )
                    Text(
                        text = if (summary.savingsBudget > 0.0) "${(savingsRatio * 100).toInt()}% Logged" else "0% Aim",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun VisualMatrixGaugeCard(
    summary: BudgetSummary,
    isNeedsWarning: Boolean = false,
    isWantsWarning: Boolean = false,
    isSavingsWarning: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .liquidGlassCard(cornerRadius = 24.dp, containerColor = Color.White.copy(alpha = 0.52f)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Allocation State Matrix",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            val needsR = if (summary.needsBudget > 0) {
                (summary.needsRemaining / summary.needsBudget).toFloat().coerceIn(0f, 1f)
            } else 1f

            val wantsR = if (summary.wantsBudget > 0) {
                (summary.wantsRemaining / summary.wantsBudget).toFloat().coerceIn(0f, 1f)
            } else 1f

            val savingsR = if (summary.savingsBudget > 0) {
                (1f - (summary.savingsExpenses / summary.savingsBudget).toFloat()).coerceIn(0f, 1f)
            } else 1f

            Box(
                modifier = Modifier.size(160.dp),
                contentAlignment = Alignment.Center
            ) {
                JointBudgetConcentricRings(
                    needsProgress = needsR,
                    wantsProgress = wantsR,
                    savingsProgress = savingsR,
                    isNeedsWarning = isNeedsWarning,
                    isWantsWarning = isWantsWarning,
                    isSavingsWarning = isSavingsWarning,
                    modifier = Modifier.fillMaxSize()
                )
            }

            if (isNeedsWarning || isWantsWarning || isSavingsWarning) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DangerRed.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                        .border(1.dp, DangerRed.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning Cap Triggered",
                        tint = DangerRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CAP ALERT: Category spending exceeds 85% limit!",
                        color = DangerRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.2.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Row budgets
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDarkSecondary, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total Net Income", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text(
                        text = formatLKR(summary.totalIncome),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = ElectricNeeds
                    )
                }
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(28.dp)
                        .background(GridDividerLabel)
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val totalExt = summary.needsExpenses + summary.wantsExpenses + summary.savingsExpenses
                    Text("Total Expenses", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text(
                        text = formatLKR(totalExt),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = DangerRed
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyHistoryPlaceholder() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .liquidGlassCard(cornerRadius = 20.dp, containerColor = Color.White.copy(alpha = 0.52f)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No recorded transactions yet",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Log your income above to see automatic budget splits, then subtract expenses cleanly under respective buckets.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

@Composable
fun TransactionRowItem(
    transaction: Transaction,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() }
            .liquidGlassCard(cornerRadius = 16.dp, containerColor = Color.White.copy(alpha = 0.52f)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon identifier with Clean backgrounds
            val circleBg = if (transaction.isIncome) ElectricNeedsBg
            else when (transaction.bucket) {
                "NEEDS" -> ElectricNeedsBg
                "WANTS" -> WarmWantsBg
                else -> SavingsIndigoBg
            }
            
            val iconColor = if (transaction.isIncome) ElectricNeeds
            else when (transaction.bucket) {
                "NEEDS" -> ElectricNeeds
                "WANTS" -> WarmWants
                else -> SavingsIndigo
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(circleBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (transaction.isIncome) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Body text
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = transaction.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = transaction.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    if (!transaction.isIncome) {
                        Box(
                            modifier = Modifier
                                .size(3.dp)
                                .clip(CircleShape)
                                .background(TextSecondary)
                        )
                        Text(
                            text = transaction.bucket,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = iconColor
                        )
                    }
                }
            }

            // Amount & Delete
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    text = if (transaction.isIncome) "+${formatLKR(transaction.amount)}" else "-${formatLKR(transaction.amount)}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (transaction.isIncome) ElectricNeeds else DangerRed
                )
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Item",
                        tint = DangerRed.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// Dialog Implementation
@Composable
fun AddIncomeDialog(
    onDismiss: () -> Unit,
    onAdd: (title: String, amount: Double, category: String) -> Unit,
    editingTransaction: Transaction? = null
) {
    var title by remember { mutableStateOf(editingTransaction?.title ?: "") }
    var amount by remember { mutableStateOf(editingTransaction?.amount?.toString() ?: "") }
    var category by remember { mutableStateOf(editingTransaction?.category ?: "Salary") }
    val categories = listOf("Salary", "Freelance", "Bonus", "Investment", "Gift", "Other")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .liquidGlassCard(cornerRadius = 24.dp, containerColor = Color.White.copy(alpha = 0.94f)),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (editingTransaction != null) "Edit Income Log" else "Log Net Income",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = ElectricNeeds
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Income Source (e.g., Paycheck)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = ElectricNeeds,
                        unfocusedBorderColor = GridDividerLabel
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("income_title_input")
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Net Amount (LKR)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = ElectricNeeds,
                        unfocusedBorderColor = GridDividerLabel
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("income_amount_input")
                )

                // Category Selection list
                Column {
                    Text("Select Category", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyColumn(
                        modifier = Modifier.height(100.dp).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(categories) { cat ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (category == cat) ElectricNeedsBg else Color.Transparent)
                                    .border(
                                        1.dp,
                                        if (category == cat) ElectricNeeds else GridDividerLabel,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { category = cat }
                                    .padding(8.dp)
                            ) {
                                Text(cat, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

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
                            if (title.isNotBlank() && amt > 0) {
                                onAdd(title, amt, category)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricNeeds),
                        modifier = Modifier.testTag("submit_income_button")
                    ) {
                        Text(if (editingTransaction != null) "Save" else "Add Income", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onAdd: (title: String, amount: Double, bucket: String, category: String) -> Unit,
    needsMultiplier: Boolean,
    editingTransaction: Transaction? = null
) {
    var title by remember { mutableStateOf(editingTransaction?.title ?: "") }
    var amount by remember { mutableStateOf(editingTransaction?.amount?.toString() ?: "") }
    var bucket by remember { mutableStateOf(editingTransaction?.bucket ?: "NEEDS") }
    var category by remember { mutableStateOf(editingTransaction?.category ?: "Bills & Rent") }

    val categories = when (bucket) {
        "NEEDS" -> listOf("Bills & Rent", "Utilities", "Transport", "Mandatory Debt", "Groceries", "Other")
        "WANTS" -> listOf("Dine Out", "Cinema & Movies", "Hobbies", "Gifts", "Apparel", "Other")
        else -> listOf("Money Market Fund (MMF)", "Fixed Deposit", "Emergency Fund", "Crypto", "Other")
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .liquidGlassCard(cornerRadius = 24.dp, containerColor = Color.White.copy(alpha = 0.94f)),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (editingTransaction != null) "Edit Expense Log" else "Log Budget Expense",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = DangerRed
                )

                if (!needsMultiplier) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DangerRed.copy(alpha = 0.1f))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = DangerRed, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Caution: Set net income first to allocate standard buckets",
                            style = MaterialTheme.typography.bodySmall,
                            color = DangerRed
                        )
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title / Item") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = DangerRed,
                        unfocusedBorderColor = GridDividerLabel
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("expense_title_input")
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Expense Cost (LKR)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = DangerRed,
                        unfocusedBorderColor = GridDividerLabel
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("expense_amount_input")
                )

                // Select Budget Bucket
                Column {
                    Text("Select 50/30/20 Bucket", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("NEEDS", "WANTS", "SAVINGS").forEach { bkt ->
                            val color = when (bkt) {
                                "NEEDS" -> ElectricNeeds
                                "WANTS" -> WarmWants
                                else -> SavingsIndigo
                            }
                            val bColor = when (bkt) {
                                "NEEDS" -> ElectricNeedsBorder
                                "WANTS" -> WarmWantsBorder
                                else -> SavingsIndigoBorder
                            }
                            val bgC = when (bkt) {
                                "NEEDS" -> ElectricNeedsBg
                                "WANTS" -> WarmWantsBg
                                else -> SavingsIndigoBg
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (bucket == bkt) bgC else SurfaceDarkSecondary)
                                    .border(
                                        1.dp,
                                        if (bucket == bkt) color else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        bucket = bkt
                                        category = when (bkt) {
                                            "NEEDS" -> "Bills & Rent"
                                            "WANTS" -> "Dine Out"
                                            else -> "Money Market Fund (MMF)"
                                        }
                                    }
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when (bkt) {
                                        "NEEDS" -> "Needs\n50%"
                                        "WANTS" -> "Wants\n30%"
                                        else -> "Savings\n20%"
                                    },
                                    textAlign = TextAlign.Center,
                                    fontSize = 11.sp,
                                    color = if (bucket == bkt) color else TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Dynamic Categories depending on selected bucket
                Column {
                    Text("Expense Category", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyColumn(
                        modifier = Modifier.height(100.dp).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(categories) { cat ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (category == cat) SurfaceDarkSecondary else Color.Transparent)
                                    .border(
                                        1.dp,
                                        if (category == cat) DangerRed else GridDividerLabel,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { category = cat }
                                    .padding(8.dp)
                            ) {
                                Text(cat, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

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
                            if (title.isNotBlank() && amt > 0) {
                                onAdd(title, amt, bucket, category)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                        modifier = Modifier.testTag("submit_expense_button")
                    ) {
                        Text(if (editingTransaction != null) "Save" else "Add Expense", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsControlHubDialog(
    viewModel: LankaBudgetViewModel,
    activeProfile: String,
    profilesList: List<String>,
    onDismiss: () -> Unit
) {
    var newProfileName by remember { mutableStateOf("") }
    var showDeleteConfirmProfile by remember { mutableStateOf<String?>(null) }
    var showAppResetConfirm by remember { mutableStateOf(false) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

    // Local Backups list state
    var localBackups by remember { mutableStateOf(viewModel.getLocalBackups()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .liquidGlassCard(cornerRadius = 24.dp, containerColor = Color.White.copy(alpha = 0.96f)),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            LazyColumn(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Title
                item {
                    Text(
                        text = "Profiles & Backup Control Hub",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            letterSpacing = (-0.5).sp
                        ),
                        color = ElectricNeeds
                    )
                    Text(
                        text = "Manage named profile ledgers, local snapshots, and JSON backups safely.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                // Profile Configuration Section
                item {
                    Text(
                        text = "MULTI-PROFILE LEDGERS",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = ElectricNeeds
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Create Profile row
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
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricNeeds),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Create")
                        }
                    }
                }

                // Profiles Listing
                items(profilesList) { profile ->
                    val isActive = profile == activeProfile
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (!isActive) {
                                    viewModel.switchProfile(profile)
                                    onDismiss()
                                }
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isActive) ElectricNeedsBg else Color.White.copy(alpha = 0.4f)
                        ),
                        border = BorderStroke(1.dp, if (isActive) ElectricNeeds else GridDividerLabel),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
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
                            // Delete button if not Default "Personal" profile
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

                // Backup Section
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "DATA BACKUP & RESTORE",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = ElectricNeeds
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Clipboard Backup Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val backupStr = viewModel.exportBackupAsJsonString()
                                    if (backupStr != null) {
                                        clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(backupStr))
                                        android.widget.Toast.makeText(context, "Backup JSON copied to clipboard!", android.widget.Toast.LENGTH_SHORT).show()
                                    } else {
                                        android.widget.Toast.makeText(context, "Failed to export backup JSON.", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.62f)),
                                border = BorderStroke(1.dp, GridDividerLabel),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Copy JSON to Clipboard", color = TextPrimary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }

                            Button(
                                onClick = {
                                    val clipboardText = clipboardManager.getText()?.text
                                    if (!clipboardText.isNullOrBlank()) {
                                        val ok = viewModel.importBackupFromJsonString(clipboardText)
                                        if (ok) {
                                            android.widget.Toast.makeText(context, "Backup imported successfully from clipboard!", android.widget.Toast.LENGTH_SHORT).show()
                                            onDismiss()
                                        } else {
                                            android.widget.Toast.makeText(context, "Failed to parse JSON backup from clipboard.", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        android.widget.Toast.makeText(context, "Clipboard is empty.", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.62f)),
                                border = BorderStroke(1.dp, GridDividerLabel),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Paste & Import JSON", color = TextPrimary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }

                        // Local Snapshots Button
                        Button(
                            onClick = {
                                val ok = viewModel.saveLocalBackup()
                                if (ok) {
                                    localBackups = viewModel.getLocalBackups()
                                    android.widget.Toast.makeText(context, "Local file backup saved!", android.widget.Toast.LENGTH_SHORT).show()
                                } else {
                                    android.widget.Toast.makeText(context, "Failed to save local file backup.", android.widget.Toast.LENGTH_SHORT).show()
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

                // Render local files if any
                if (localBackups.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "AVAILABLE LOCAL SNAPSHOTS",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = TextSecondary
                        )
                    }

                    items(localBackups) { filename ->
                        val prettyDate = formatBackupName(filename)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.3f)),
                            border = BorderStroke(1.dp, GridDividerLabel),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
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
                                            android.widget.Toast.makeText(context, "Snapshot restored!", android.widget.Toast.LENGTH_SHORT).show()
                                            onDismiss()
                                        } else {
                                            android.widget.Toast.makeText(context, "Failed to restore snapshot.", android.widget.Toast.LENGTH_SHORT).show()
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

                // Destructive Danger Zone (HIDDEN SOMEWHERE ACCESSIBLE/CLEAR APP DATA BUTTON)
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "DESTRUCTIVE ZONE",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = DangerRed
                    )
                    Spacer(modifier = Modifier.height(8.dp))
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

                // Close Button
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Dismiss", color = ElectricNeeds, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Confirmation dialogs
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
                        onDismiss()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = DangerRed)
                ) {
                    Text("Erase Profile Data", fontWeight = FontWeight.Bold)
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

fun formatBackupName(filename: String): String {
    return try {
        val parts = filename.removeSuffix(".json").split("_")
        val timestampStr = parts.last()
        val timestamp = timestampStr.toLongOrNull()
        if (timestamp != null) {
            val date = java.util.Date(timestamp)
            java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(date)
        } else {
            filename
        }
    } catch (e: Exception) {
        filename
    }
}

@Composable
fun SalaryPredictionCard(
    currentSalaryDate: String,
    nextSalaryDate: String,
    salaryPrediction: Double,
    projections: List<Pair<String, Double>>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .testTag("salary_prediction_card")
            .liquidGlassCard(cornerRadius = 24.dp, containerColor = Color.White.copy(alpha = 0.52f)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SALARY FORECASTS",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = ElectricNeeds
                )
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(ElectricNeedsBg)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "PREDICTIVE ENGINE",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                        color = ElectricNeeds
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Current Paid Cycle", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = currentSalaryDate, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Estimated Next Pay", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = nextSalaryDate, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = ElectricNeeds)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column {
                Text(text = "Predicted Base", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                Text(
                    text = formatLKR(salaryPrediction),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = GridDividerLabel, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "FUTURE CYCLE DISBURSEMENTS",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Payday Month", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = TextSecondary, modifier = Modifier.weight(1.2f))
                Text(text = "Est. Amount", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = TextSecondary, modifier = Modifier.weight(1.3f), textAlign = TextAlign.End)
                Text(text = "Needs/Wants/Save", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = TextSecondary, modifier = Modifier.weight(2f), textAlign = TextAlign.End)
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                projections.forEach { (date, amt) ->
                    val needsP = amt * 0.5
                    val wantsP = amt * 0.3
                    val savingsP = amt * 0.2

                    val symbol = when (appCurrency) {
                        "USD" -> "$"
                        "EUR" -> "€"
                        else -> ""
                    }
                    val rate = when (appCurrency) {
                        "USD" -> 1.0 / 300.0
                        "EUR" -> 1.0 / 325.0
                        else -> 1.0
                    }
                    val converted = amt * rate
                    val cleanAmt = if (symbol == "") {
                        String.format("%,.0f LKR", converted)
                    } else {
                        String.format("%s%,.0f", symbol, converted)
                    }

                    val compactN = if (symbol == "") String.format("%,.0fK", (needsP * rate) / 1000.0) else String.format("%s%,.0f", symbol, needsP * rate)
                    val compactW = if (symbol == "") String.format("%,.0fK", (wantsP * rate) / 1000.0) else String.format("%s%,.0f", symbol, wantsP * rate)
                    val compactS = if (symbol == "") String.format("%,.0fK", (savingsP * rate) / 1000.0) else String.format("%s%,.0f", symbol, savingsP * rate)

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = date.substringBefore(","),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary,
                            modifier = Modifier.weight(1.2f)
                        )
                        Text(
                            text = cleanAmt,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = ElectricNeeds,
                            modifier = Modifier.weight(1.3f),
                            textAlign = TextAlign.End
                        )
                        Text(
                            text = "$compactN/$compactW/$compactS",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            modifier = Modifier.weight(2f),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AllocationStateMatrixCard(
    summary: BudgetSummary,
    isNeedsWarning: Boolean,
    isWantsWarning: Boolean,
    isSavingsWarning: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .testTag("allocation_state_matrix_card")
            .liquidGlassCard(cornerRadius = 24.dp, containerColor = Color.White.copy(alpha = 0.52f)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "STATE GAUGE MATRIX",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = SavingsIndigo
                )
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isNeedsWarning || isWantsWarning || isSavingsWarning) DangerRed.copy(alpha = 0.1f) else SavingsIndigoBg)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (isNeedsWarning || isWantsWarning || isSavingsWarning) "ALERT ACTIVE" else "STABLE STATE",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                        color = if (isNeedsWarning || isWantsWarning || isSavingsWarning) DangerRed else SavingsIndigo
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val needsR = if (summary.needsBudget > 0) {
                    (summary.needsRemaining / summary.needsBudget).toFloat().coerceIn(0f, 1f)
                } else 1f

                val wantsR = if (summary.wantsBudget > 0) {
                    (summary.wantsRemaining / summary.wantsBudget).toFloat().coerceIn(0f, 1f)
                } else 1f

                val savingsR = if (summary.savingsBudget > 0) {
                    (1f - (summary.savingsExpenses / summary.savingsBudget).toFloat()).coerceIn(0f, 1f)
                } else 1f

                Box(
                    modifier = Modifier.size(90.dp),
                    contentAlignment = Alignment.Center
                ) {
                    JointBudgetConcentricRings(
                        needsProgress = needsR,
                        wantsProgress = wantsR,
                        savingsProgress = savingsR,
                        isNeedsWarning = isNeedsWarning,
                        isWantsWarning = isWantsWarning,
                        isSavingsWarning = isSavingsWarning,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(if (isNeedsWarning) DangerRed else ElectricNeeds))
                            Text("Needs", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Act: ${formatCompactLKR(summary.needsExpenses)}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Text("Rem: ${formatCompactLKR(summary.needsRemaining)}", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = if (summary.needsRemaining >= 0.0) SettlementSuccess else DangerRed)
                        }
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(if (isWantsWarning) DangerRed else WarmWants))
                            Text("Wants", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Act: ${formatCompactLKR(summary.wantsExpenses)}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Text("Rem: ${formatCompactLKR(summary.wantsRemaining)}", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = if (summary.wantsRemaining >= 0.0) SettlementSuccess else DangerRed)
                        }
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(if (isSavingsWarning) DangerRed else SavingsIndigo))
                            Text("Savings", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Act: ${formatCompactLKR(summary.savingsExpenses)}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Text("Rem: ${formatCompactLKR(summary.savingsRemaining)}", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = if (summary.savingsRemaining >= 0.0) SettlementSuccess else DangerRed)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = GridDividerLabel, thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))

            if (isNeedsWarning || isWantsWarning || isSavingsWarning) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Alert",
                        tint = DangerRed,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "CAP ALERT: Category spending exceeds 85% limit!",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                        color = DangerRed
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(SettlementSuccess))
                    Text(
                        text = "Ledger allocation in fully balanced state.",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

fun formatCompactLKR(amount: Double): String {
    val symbol = when (appCurrency) {
        "USD" -> "$"
        "EUR" -> "€"
        else -> ""
    }
    val rate = when (appCurrency) {
        "USD" -> 1.0 / 300.0
        "EUR" -> 1.0 / 325.0
        else -> 1.0
    }
    val converted = amount * rate
    return if (symbol == "") {
        if (converted >= 1000.0) {
            String.format("%,.0fK", converted / 1000.0)
        } else {
            String.format("%,.0f", converted)
        }
    } else {
        if (converted >= 1000.0) {
            String.format("%s%,.1fK", symbol, converted / 1000.0)
        } else {
            String.format("%s%,.0f", symbol, converted)
        }
    }
}

