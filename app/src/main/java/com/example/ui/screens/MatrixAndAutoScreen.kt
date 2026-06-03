package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.liquidGlassCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.LankaBudgetViewModel

@Composable
fun MatrixAndAutoScreen(
    viewModel: LankaBudgetViewModel,
    modifier: Modifier = Modifier
) {
    val reportMode by viewModel.reportMode.collectAsState()
    val availableTabs = remember(reportMode) {
        when (reportMode) {
            "Simple" -> listOf("Debts", "Splits", "Goals")
            "Normal" -> listOf("Debts", "Splits", "Goals", "Assets", "Auto")
            else -> listOf("Debts", "Splits", "Goals", "Assets", "Auto", "Audit")
        }
    }

    var activeSubTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(availableTabs) {
        if (activeSubTab >= availableTabs.size) {
            activeSubTab = 0
        }
    }

    val selectedTabLabel = if (activeSubTab < availableTabs.size) availableTabs[activeSubTab] else "Debts"

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // A unified header card
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
                    Column {
                        Text(
                            text = "PORTFOLIO & AUTOMATIONS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = ElectricNeeds
                        )
                        Text(
                            text = when (selectedTabLabel) {
                                "Debts" -> "Track peer debts & loans ledger"
                                "Splits" -> "Divide & split bills with friends"
                                "Goals" -> "Track visual goals & savings allocations"
                                "Assets" -> "Manage critical assets & investments"
                                "Auto" -> "Manage automated cron ledger actions"
                                "Audit" -> "Equation: Asset + Expense = Capital + Income + Liab"
                                else -> "Equation: Asset + Expense = Capital + Income + Liab"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            // Sub-Tabs segmented swapper for unified navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp)
                    .liquidGlassCard(cornerRadius = 14.dp, containerColor = Color.White.copy(alpha = 0.35f))
                    .padding(4.dp)
            ) {
                availableTabs.forEachIndexed { index, label ->
                    val isSelected = activeSubTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Color.White.copy(alpha = 0.62f) else Color.Transparent)
                            .clickable { activeSubTab = index }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isSelected) ElectricNeeds else TextSecondary
                        )
                    }
                }
            }

            // Centralized rendering
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (selectedTabLabel) {
                    "Debts" -> {
                        DebtLedgerScreen(
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize(),
                            forceSubTab = 0,
                            hideHeaderAndSwitcher = true
                        )
                    }
                    "Splits" -> {
                        SplitBillScreen(
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    "Goals" -> {
                        FinancialGoalsScreen(
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    "Assets" -> {
                        DebtLedgerScreen(
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize(),
                            forceSubTab = 1,
                            hideHeaderAndSwitcher = true
                        )
                    }
                    "Auto" -> {
                        RecurringSchedulesScreen(
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize(),
                            hideHeader = true
                        )
                    }
                    "Audit" -> {
                        DoubleEntryLedgerScreen(
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}
