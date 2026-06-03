package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.InteractiveLineChart
import com.example.ui.components.liquidGlassCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.LankaBudgetViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReportsScreen(
    viewModel: LankaBudgetViewModel,
    modifier: Modifier = Modifier
) {
    val reportMode by viewModel.reportMode.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val repayments by viewModel.repayments.collectAsState()
    val budgetSummary by viewModel.budgetSummaryState.collectAsState()
    val netWorth by viewModel.netWorth.collectAsState()
    val totalInvestmentBalance by viewModel.totalInvestmentBalance.collectAsState()
    val totalDebtBalance by viewModel.totalDebtBalance.collectAsState()
    val availableBalance by viewModel.availableBalance.collectAsState()

    val needsLimit by viewModel.needsLimit.collectAsState()
    val wantsLimit by viewModel.wantsLimit.collectAsState()
    val savingsLimit by viewModel.savingsLimit.collectAsState()

    val needsWarning = budgetSummary.needsExpenses >= needsLimit * 0.85 && needsLimit > 0
    val wantsWarning = budgetSummary.wantsExpenses >= wantsLimit * 0.85 && wantsLimit > 0
    val savingsWarning = budgetSummary.savingsExpenses < savingsLimit * 0.50 && savingsLimit > 0

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = 132.dp, // Clears floating top bar
                bottom = 110.dp, // Clears floating bottom navigation
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Section
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlassCard(cornerRadius = 24.dp, containerColor = Color.White.copy(alpha = 0.55f)),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(HeaderPillBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QueryStats,
                                    contentDescription = null,
                                    tint = ElectricNeeds,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "FINANCIAL ANALYTICS HUB",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.2.sp
                                    ),
                                    color = ElectricNeeds
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = ElectricNeeds,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "${reportMode.uppercase()} MODE",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Simple Mode: High Level metrics, easy understanding
            if (reportMode == "Simple") {
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
                                text = "EXECUTIVE PORTFOLIO SUMMARY",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = ElectricNeeds
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Net Worth", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                    Text(
                                        text = formatLKR(netWorth),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = TextPrimary
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Active Balance", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                    Text(
                                        text = formatLKR(availableBalance),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = ElectricNeeds
                                    )
                                }
                            }

                            HorizontalDivider(color = GridDividerLabel, thickness = 1.dp)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Invested Asset Value", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                    Text(
                                        text = formatLKR(totalInvestmentBalance),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = TextPrimary
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Outstanding Debts", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                    Text(
                                        text = formatLKR(totalDebtBalance),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = DangerRed
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .liquidGlassCard(cornerRadius = 20.dp, containerColor = Color.White.copy(alpha = 0.45f)),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Row(
                            modifier = Modifier.padding(18.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = ElectricNeeds,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    text = "Simplified Insights",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (netWorth >= 0) {
                                        "You are maintaining a positive Net Worth of ${formatLKR(netWorth)}. Try keeping your monthly 'Wants' spending below 30% to maximize savings."
                                    } else {
                                        "Your liabilities exceed your assets by ${formatLKR(-netWorth)}. Focus on settling outstanding loans and optimizing essential spending."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Normal & Expert Mode: Rich visualizations and detailed tables
            if (reportMode == "Normal" || reportMode == "Expert") {
                // Forecast & Prediction Card (Normal & Expert)
                item {
                    val salaryData = viewModel.getSalaryDatesAndPredictions()
                    val currentSalaryDate = salaryData.first
                    val nextSalaryDate = salaryData.second
                    val salaryPrediction = salaryData.third

                    SalaryPredictionCard(
                        currentSalaryDate = currentSalaryDate,
                        nextSalaryDate = nextSalaryDate,
                        salaryPrediction = salaryPrediction,
                        projections = viewModel.getFutureSalaryProjections(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Balance Trajectory Chart (Normal & Expert)
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .liquidGlassCard(cornerRadius = 24.dp, containerColor = Color.White.copy(alpha = 0.45f)),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoGraph,
                                    contentDescription = null,
                                    tint = ElectricNeeds,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "CASHFLOW BALANCE TRAJECTORY (FORECAST)",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = ElectricNeeds
                                )
                            }

                            InteractiveLineChart(
                                dataPoints = viewModel.getBalanceTrajectory(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(240.dp)
                            )
                        }
                    }
                }
            }

            // Expert Mode Additional Details
            if (reportMode == "Expert") {
                item {
                    AllocationStateMatrixCard(
                        summary = budgetSummary,
                        isNeedsWarning = needsWarning,
                        isWantsWarning = wantsWarning,
                        isSavingsWarning = savingsWarning,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Monthly Financial Reporting Hub (Normal & Expert)
            if (reportMode == "Normal" || reportMode == "Expert") {
                item {
                    MonthlyFinancialReportingHub(
                        transactions = transactions,
                        repayments = repayments
                    )
                }
            }
        }

        // Floating Top Bar Overlay
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
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
                            imageVector = Icons.Default.AutoGraph,
                            contentDescription = null,
                            tint = ElectricNeeds,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Reports & Analytics",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp,
                                fontSize = 18.sp
                            ),
                            color = TextPrimary
                        )
                        Text(
                            text = "Analyze your visual budget & net worth metrics",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}
