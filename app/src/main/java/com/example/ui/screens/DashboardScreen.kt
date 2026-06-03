package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import com.example.ui.components.InteractiveLineChart
import androidx.compose.animation.core.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.asImageBitmap
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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import android.net.Uri
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.DesignServices
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ArrowDownward

var appCurrency: String = "LKR"

fun formatLKR(amount: Double): String {
    return com.example.data.helper.CurrencyHelper.format(amount, appCurrency)
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
    val reportMode by viewModel.reportMode.collectAsState()

    val customWidgetsState by viewModel.customDashboardWidgets.collectAsState()
    var isDesigning by remember { mutableStateOf(false) }
    var selectedWidgetId by remember { mutableStateOf<String?>(null) }
    var draggingWidgetId by remember { mutableStateOf<String?>(null) }
    var resizingWidgetId by remember { mutableStateOf<String?>(null) }
    var localWidgetsDraft by remember(customWidgetsState, isDesigning) {
        mutableStateOf(customWidgetsState.sortedBy { it.order })
    }

    val activeProfile by viewModel.activeProfile.collectAsState()
    val profilesList by viewModel.profilesList.collectAsState()

    val needsLimit by viewModel.needsLimit.collectAsState()
    val wantsLimit by viewModel.wantsLimit.collectAsState()
    val savingsLimit by viewModel.savingsLimit.collectAsState()
    val repayments by viewModel.repayments.collectAsState()

    val needsWarning = budgetSummary.needsExpenses >= needsLimit * 0.85 && needsLimit > 0
    val wantsWarning = budgetSummary.wantsExpenses >= wantsLimit * 0.85 && wantsLimit > 0
    val savingsWarning = budgetSummary.savingsExpenses >= savingsLimit * 0.85 && savingsLimit > 0

    var showAddIncomeDialog by remember { mutableStateOf(false) }
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var showAIBillScanner by remember { mutableStateOf(false) }
    var showAIVoiceInput by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<Transaction?>(null) }
    var selectedBucketFilter by remember { mutableStateOf<String?>(null) }

    val filteredTransactions = remember(transactions, selectedBucketFilter) {
        if (selectedBucketFilter == null) {
            transactions
        } else {
            transactions.filter { !it.isIncome && it.bucket.uppercase() == selectedBucketFilter!!.uppercase() }
        }
    }

    val isSystemDark = SlateDark != Color(0xFFFBFDFD)
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        isDesigning = true
                        if (reportMode != "Custom") {
                            viewModel.updateReportMode("Custom")
                        }
                    }
                )
            }
            .drawBehind {
                if (isDesigning && reportMode == "Custom") {
                    val isMovingOrSizing = (draggingWidgetId != null || resizingWidgetId != null)
                    val gridColor = if (isMovingOrSizing) {
                        ElectricNeeds.copy(alpha = 0.16f)
                    } else if (isSystemDark) {
                        Color.White.copy(alpha = 0.06f)
                    } else {
                        Color.Black.copy(alpha = 0.05f)
                    }
                    val activeGridColor = if (isMovingOrSizing) {
                        EmeraldIncome.copy(alpha = 0.4f)
                    } else if (isSystemDark) {
                        Color.White.copy(alpha = 0.12f)
                    } else {
                        Color.Black.copy(alpha = 0.10f)
                    }
                    
                    // Draw 5 vertical columns lines matching the horizontal weights
                    val colWidth = size.width / 5f
                    for (i in 0..5) {
                        val posX = colWidth * i
                        drawLine(
                            color = if (i == 0 || i == 5) activeGridColor else gridColor,
                            start = Offset(posX, 0f),
                            end = Offset(posX, size.height),
                            strokeWidth = if (isMovingOrSizing && (i == 0 || i == 5)) 3.5f else if (isMovingOrSizing) 2.2f else if (i == 0 || i == 5) 2f else 1.2f
                        )
                    }
                    
                    // Draw horizontal segment row lines
                    val rowHeight = 75.dp.toPx()
                    var y = 0f
                    var idx = 0
                    while (y < size.height) {
                        drawLine(
                            color = if (idx % 2 == 0) activeGridColor else gridColor,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = if (isMovingOrSizing && idx % 2 == 0) 3.2f else if (isMovingOrSizing) 2f else if (idx % 2 == 0) 1.5f else 1f
                        )
                        y += rowHeight
                        idx++
                    }
                }
            }
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
            if (reportMode == "Custom") {
                // RENDER CUSTOM WORKSPACE
                item {
                    val isSystemDark = SlateDark != Color(0xFFFBFDFD)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .liquidGlassCard(
                                cornerRadius = 18.dp, 
                                containerColor = if (isDesigning) ElectricNeeds.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.25f),
                                hasShadow = true
                            ),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isDesigning) "🎨 COGNITIVE DESIGNER ACTIVE" else "📐 CUSTOM WORKSPACE MODE",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 1.2.sp),
                                    color = if (isDesigning) ElectricNeeds else TextPrimary
                                )
                                Text(
                                    text = if (isDesigning) "Customize layout elements, resize, tint, reorder live!" else "Unlock full customization over dashboard widgets",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (isDesigning) {
                                    FilledTonalButton(
                                        onClick = {
                                            localWidgetsDraft = customWidgetsState.sortedBy { it.order }
                                            isDesigning = false
                                            selectedWidgetId = null
                                            draggingWidgetId = null
                                            resizingWidgetId = null
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = DangerRed.copy(alpha = 0.15f),
                                            contentColor = DangerRed
                                        )
                                    ) {
                                        Text("Cancel", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    
                                    Button(
                                        onClick = {
                                            val finalWidgets = localWidgetsDraft.mapIndexed { index, item ->
                                                item.copy(order = index)
                                            }
                                            viewModel.saveCustomWidgets(finalWidgets)
                                            isDesigning = false
                                            selectedWidgetId = null
                                            draggingWidgetId = null
                                            resizingWidgetId = null
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = ElectricNeeds,
                                            contentColor = Color.Black
                                        )
                                    ) {
                                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Save", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Button(
                                        onClick = { isDesigning = true },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = ElectricNeeds,
                                            contentColor = Color.Black
                                        )
                                    ) {
                                        Icon(Icons.Default.DesignServices, contentDescription = null, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Design", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                val renderWidgetsList = if (isDesigning) localWidgetsDraft else localWidgetsDraft.filter { it.isEnabled }
                val gridRows = mutableListOf<List<com.example.data.model.DashboardWidget>>()
                var currentRow = mutableListOf<com.example.data.model.DashboardWidget>()
                var currentWidthSum = 0

                renderWidgetsList.forEach { widget ->
                    val w = widget.gridWidth.coerceIn(1, 4).let { if (widget.id == "CHART" || widget.gridWidth >= 5) 5 else it } // coerce bounds safely
                    val coercedWidth = widget.gridWidth.coerceIn(1, 5)
                    if (currentWidthSum + coercedWidth > 5) {
                        gridRows.add(currentRow)
                        currentRow = mutableListOf(widget)
                        currentWidthSum = coercedWidth
                    } else {
                        currentRow.add(widget)
                        currentWidthSum += coercedWidth
                    }
                }
                if (currentRow.isNotEmpty()) {
                    gridRows.add(currentRow)
                }

                gridRows.forEach { rowItems ->
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateContentSize(animationSpec = spring(dampingRatio = 0.82f, stiffness = 400f)),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowItems.forEach { widget ->
                                val animatedWeight by animateFloatAsState(
                                    targetValue = widget.gridWidth.toFloat(),
                                    animationSpec = spring(dampingRatio = 0.82f, stiffness = 400f),
                                    label = "weightAnim_${widget.id}"
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(animatedWeight.coerceAtLeast(0.01f))
                                        .pointerInput(widget.id) {
                                            detectTapGestures(
                                                onTap = {
                                                    if (isDesigning) {
                                                        selectedWidgetId = widget.id
                                                    }
                                                },
                                                onLongPress = {
                                                    isDesigning = true
                                                    selectedWidgetId = widget.id
                                                    if (reportMode != "Custom") {
                                                        viewModel.updateReportMode("Custom")
                                                    }
                                                }
                                            )
                                        }
                                ) {
                                    DashboardWidgetRenderer(
                                        widget = widget,
                                        viewModel = viewModel,
                                        budgetSummary = budgetSummary,
                                        availableBalance = availableBalance,
                                        totalInvestmentBalance = totalInvestmentBalance,
                                        totalDebtBalance = totalDebtBalance,
                                        netWorth = netWorth,
                                        needsLimit = needsLimit,
                                        wantsLimit = wantsLimit,
                                        savingsLimit = savingsLimit,
                                        needsWarning = needsWarning,
                                        wantsWarning = wantsWarning,
                                        savingsWarning = savingsWarning,
                                        filteredTransactions = filteredTransactions,
                                        onAddIncome = { showAddIncomeDialog = true },
                                        onLogExpense = { showAddExpenseDialog = true },
                                        onAIScan = { showAIBillScanner = true },
                                        onAIVoiceClick = { showAIVoiceInput = true },
                                        onEditTransaction = { editingTransaction = it },
                                        onDeleteTransaction = { viewModel.deleteTransaction(it) },
                                        selectedBucketFilter = selectedBucketFilter,
                                        onBucketFilterChange = { selectedBucketFilter = it },
                                        isDesigning = isDesigning,
                                        onMoveUp = {
                                            val list = localWidgetsDraft.toMutableList()
                                            val index = list.indexOf(widget)
                                            if (index > 0) {
                                                val temp = list[index]
                                                list[index] = list[index - 1]
                                                list[index - 1] = temp
                                                localWidgetsDraft = list
                                            }
                                        },
                                        onMoveDown = {
                                            val list = localWidgetsDraft.toMutableList()
                                            val index = list.indexOf(widget)
                                            if (index < list.size - 1) {
                                                val temp = list[index]
                                                list[index] = list[index + 1]
                                                list[index + 1] = temp
                                                localWidgetsDraft = list
                                            }
                                        },
                                        onToggleEnabled = {
                                            val list = localWidgetsDraft.map {
                                                if (it.id == widget.id) it.copy(isEnabled = !it.isEnabled) else it
                                            }
                                            localWidgetsDraft = list
                                        },
                                        onCycleTint = {
                                            val tints = listOf("White", "Indigo", "Emerald", "Coral", "Cyan", "SunsetGold")
                                            val nextTint = tints[(tints.indexOf(widget.glassColorTint ?: "White") + 1) % tints.size]
                                            val list = localWidgetsDraft.map {
                                                if (it.id == widget.id) it.copy(glassColorTint = nextTint) else it
                                            }
                                            localWidgetsDraft = list
                                        },
                                        onUpdateSize = { w, h ->
                                            val list = localWidgetsDraft.map {
                                                if (it.id == widget.id) it.copy(gridWidth = w, gridHeight = h) else it
                                            }
                                            localWidgetsDraft = list
                                        },
                                        onLongPressWidget = {
                                            isDesigning = true
                                            selectedWidgetId = widget.id
                                            if (reportMode != "Custom") {
                                                viewModel.updateReportMode("Custom")
                                            }
                                        },
                                        isSelected = (selectedWidgetId == widget.id),
                                        onSelectWidget = { selectedWidgetId = widget.id },
                                        onDragStateChanged = { isDragging -> draggingWidgetId = if (isDragging) widget.id else null },
                                        onResizeStateChanged = { isResizing -> resizingWidgetId = if (isResizing) widget.id else null }
                                    )
                                }
                            }
                            val rowSum = rowItems.sumOf { it.gridWidth }
                            if (rowSum < 5) {
                                Spacer(modifier = Modifier.weight((5 - rowSum).toFloat()))
                            }
                        }
                    }
                }
            } else {
                // Primary Balance Card
                item {
                    PrimaryBalanceCard(
                        balance = availableBalance,
                        onAddIncome = { showAddIncomeDialog = true },
                        onLogExpense = { showAddExpenseDialog = true },
                        onAIScan = { showAIBillScanner = true },
                        onAIVoiceClick = { showAIVoiceInput = true }
                    )
                }

            // Balance Sheet Overview Card (Net Worth, Investments and Debt) with side-scroll breakdown
            if (reportMode != "Simple") {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(modifier = Modifier.width(330.dp)) {
                            BalanceSheetOverviewCard(
                                netWorth = netWorth,
                                investments = totalInvestmentBalance,
                                debt = totalDebtBalance
                            )
                        }

                        // Side-scroll Sub-transaction Breakdown Card
                        val subTxTransactions = remember(transactions) {
                            transactions.filter { !it.subTransactionsStr.isNullOrBlank() && parseSubTransactions(it.subTransactionsStr).isNotEmpty() }.take(3)
                        }

                        Card(
                            modifier = Modifier
                                .width(330.dp)
                                .fillMaxHeight()
                                .liquidGlassCard(cornerRadius = 24.dp, containerColor = Color.White.copy(alpha = 0.52f)),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(20.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "RECENT ITEM BREAKDOWNS",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.2.sp
                                        ),
                                        color = TextSecondary
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(ElectricNeeds.copy(alpha = 0.15f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "SUB-LOGS",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = ElectricNeeds,
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                if (subTxTransactions.isEmpty()) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Swipe to break down items inside expenses to see details here!",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(horizontal = 8.dp)
                                        )
                                    }
                                } else {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        subTxTransactions.forEach { tx ->
                                            val subItems = parseSubTransactions(tx.subTransactionsStr ?: "")
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(SurfaceDarkSecondary.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                                    .padding(8.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = tx.title,
                                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                        color = TextPrimary,
                                                        maxLines = 1
                                                    )
                                                    Text(
                                                        text = formatLKR(tx.amount),
                                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                        color = if (tx.isIncome) ElectricNeeds else DangerRed
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                    subItems.take(2).forEach { sub ->
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth().padding(start = 6.dp),
                                                            horizontalArrangement = Arrangement.SpaceBetween
                                                        ) {
                                                            Text(
                                                                text = "- ${sub.title}",
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = TextSecondary
                                                            )
                                                            Text(
                                                                text = formatLKR(sub.amount),
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = TextSecondary
                                                            )
                                                        }
                                                    }
                                                    if (subItems.size > 2) {
                                                        Text(
                                                            text = "+ ${subItems.size - 2} more items",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = ElectricNeeds,
                                                            modifier = Modifier.padding(start = 6.dp)
                                                        )
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
            }

            // 50/30/20 Allocation Status Grid
            item {
                BudgetThreeColumnsAllocation(
                    summary = budgetSummary,
                    needsWarning = needsWarning,
                    wantsWarning = wantsWarning,
                    savingsWarning = savingsWarning,
                    selectedBucket = selectedBucketFilter,
                    onBucketClick = { bucket ->
                        selectedBucketFilter = if (selectedBucketFilter == bucket) null else bucket
                    }
                )
            }

            // Expert Mode: Real-time Double-Entry Auditor Equation Block
            if (reportMode == "Expert") {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .liquidGlassCard(cornerRadius = 20.dp, containerColor = Color.White.copy(alpha = 0.5f)),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📊 DOUBLE-ENTRY ACCOUNTING AUDIT",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                                    color = ElectricNeeds
                                )

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SettlementSuccess.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "SUM DEBITS = SUM CREDITS",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 9.sp),
                                        color = SettlementSuccess
                                    )
                                }
                            }

                            Text(
                                text = "Assets = Liabilities + Equity",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.3).sp),
                                color = TextPrimary
                            )

                            Text(
                                text = "Applying the fundamental Accounting Equation to secure real-time mathematical precision across your entire ledger:",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )

                            // Equation Visualizer
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SurfaceDarkSecondary.copy(alpha = 0.6f))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Assets (DEAD)", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                    Text(formatLKR(availableBalance + totalInvestmentBalance), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = ElectricNeeds)
                                    Text("Cash + Invest", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp), color = TextSecondary)
                                }

                                Text("=", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)

                                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Liabilities (GIRLS)", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                    Text(formatLKR(totalDebtBalance), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = DangerRed)
                                    Text("Peer Loans", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp), color = TextSecondary)
                                }

                                Text("+", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextSecondary)

                                val calculatedEquity = (availableBalance + totalInvestmentBalance - totalDebtBalance)
                                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Equity (GIRLS)", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                    Text(formatLKR(calculatedEquity), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = SavingsIndigo)
                                    Text("Owner's Equity", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp), color = TextSecondary)
                                }
                            }
                        }
                    }
                }
            }

            // List Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { selectedBucketFilter = null }
                            .padding(vertical = 4.dp, horizontal = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Transaction History",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.3).sp
                            ),
                            color = TextPrimary
                        )
                        if (selectedBucketFilter != null) {
                            Text(
                                text = "• $selectedBucketFilter (x)",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricNeeds
                                ),
                                modifier = Modifier
                                    .background(ElectricNeeds.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Export CSV Button
                        val context = androidx.compose.ui.platform.LocalContext.current
                        TextButton(
                            onClick = {
                                val csv = viewModel.exportCSV()
                                try {
                                    val sendIntent = android.content.Intent().apply {
                                        action = android.content.Intent.ACTION_SEND
                                        putExtra(android.content.Intent.EXTRA_TEXT, csv)
                                        type = "text/csv"
                                    }
                                    val shareIntent = android.content.Intent.createChooser(sendIntent, "Export LankaBudget Pro CSV")
                                    context.startActivity(shareIntent)
                                } catch(e: Exception) {
                                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    val clip = android.content.ClipData.newPlainText("LankaBudget CSV", csv)
                                    clipboard.setPrimaryClip(clip)
                                    android.widget.Toast.makeText(context, "Copied CSV database to Clipboard!", android.widget.Toast.LENGTH_LONG).show()
                                }
                            },
                            modifier = Modifier.height(32.dp).testTag("export_csv_button"),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            colors = ButtonDefaults.textButtonColors(contentColor = ElectricNeeds)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export CSV", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Text(
                            text = if (selectedBucketFilter == null) "${transactions.size} logs" else "${filteredTransactions.size} of ${transactions.size} logs",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = TextSecondary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { selectedBucketFilter = null }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Transaction Rows
            if (filteredTransactions.isEmpty()) {
                item {
                    EmptyHistoryPlaceholder()
                }
            } else {
                items(filteredTransactions, key = { it.id }) { transaction ->
                    TransactionRowItem(
                        transaction = transaction,
                        onEdit = { editingTransaction = transaction },
                        onDelete = { viewModel.deleteTransaction(transaction) }
                    )
                }
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
                viewModel = viewModel,
                onDismiss = { showAddIncomeDialog = false },
                onAdd = { title, amount, category, subTransactionsStr ->
                    viewModel.addIncome(title, amount, category, subTransactionsStr)
                    showAddIncomeDialog = false
                }
            )
        }

        if (showAddExpenseDialog) {
            AddExpenseDialog(
                viewModel = viewModel,
                onDismiss = { showAddExpenseDialog = false },
                onAdd = { title, amount, bucket, category, subTransactionsStr ->
                    viewModel.addExpense(title, amount, bucket, category, subTransactionsStr)
                    showAddExpenseDialog = false
                },
                needsMultiplier = budgetSummary.totalIncome > 0
            )
        }

        if (showAIBillScanner) {
            AIBillScannerDialog(
                viewModel = viewModel,
                onDismiss = { showAIBillScanner = false }
            )
        }

        if (showAIVoiceInput) {
            AIVoiceInputDialog(
                viewModel = viewModel,
                onDismiss = { showAIVoiceInput = false }
            )
        }

        editingTransaction?.let { tx ->
            if (tx.isIncome) {
                AddIncomeDialog(
                    viewModel = viewModel,
                    onDismiss = { editingTransaction = null },
                    editingTransaction = tx,
                    onAdd = { title, amount, category, subTransactionsStr ->
                        viewModel.updateTransaction(
                            tx.copy(title = title, amount = amount, category = category, subTransactionsStr = subTransactionsStr)
                        )
                        editingTransaction = null
                    }
                )
            } else {
                AddExpenseDialog(
                    viewModel = viewModel,
                    onDismiss = { editingTransaction = null },
                    needsMultiplier = false,
                    editingTransaction = tx,
                    onAdd = { title, amount, bucket, category, subTransactionsStr ->
                        viewModel.updateTransaction(
                            tx.copy(title = title, amount = amount, bucket = bucket, category = category, subTransactionsStr = subTransactionsStr)
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
    debt: Double,
    containerColor: Color? = null,
    isHalfWidth: Boolean = false,
    heightScale: Float = 1.0f
) {
    val resolvedBg = containerColor ?: Color.White.copy(alpha = 0.52f)
    val paddingVal = (if (isHalfWidth) 12 else (20 * heightScale).toInt()).coerceIn(10, 32).dp
    val spacingBetween = (if (isHalfWidth) 8 else (16 * heightScale).toInt()).coerceIn(6, 28).dp
    val netWorthTextSize = (if (isHalfWidth) 16 else (22 * heightScale).toInt()).coerceIn(14, 30).sp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("balance_sheet_card")
            .liquidGlassCard(cornerRadius = 24.dp, containerColor = resolvedBg),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingVal)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ESTIMATED NET WORTH",
                        style = if (isHalfWidth) {
                            MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        } else {
                            MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
                        },
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formatLKR(netWorth),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = netWorthTextSize
                        ),
                        color = if (netWorth >= 0) ElectricNeeds else DangerRed
                    )
                }

                if (!isHalfWidth) {
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
            }

            Spacer(modifier = Modifier.height(spacingBetween))
            HorizontalDivider(color = GridDividerLabel, thickness = 1.dp)
            Spacer(modifier = Modifier.height(spacingBetween))

            if (isHalfWidth) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Investments Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(SavingsIndigo))
                            Text("Investments", fontSize = 10.sp, color = TextSecondary)
                        }
                        Text(formatLKR(investments), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }

                    // Loans Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(DangerRed))
                            Text("Loans", fontSize = 10.sp, color = TextSecondary)
                        }
                        Text(formatLKR(debt), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DangerRed)
                    }
                }
            } else {
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
}

@Composable
fun PrimaryBalanceCard(
    balance: Double,
    onAddIncome: () -> Unit,
    onLogExpense: () -> Unit,
    onAIScan: () -> Unit,
    onAIVoiceClick: () -> Unit,
    containerColor: Color? = null,
    isHalfWidth: Boolean = false,
    heightScale: Float = 1.0f
) {
    val resolvedBg = containerColor ?: EmeraldIncome.copy(alpha = 0.72f)
    val paddingVal = (if (isHalfWidth) 14 else (24 * heightScale).toInt()).coerceIn(10, 36).dp
    val verticalSpacerHeight = (if (isHalfWidth) 10 else (20 * heightScale).toInt()).coerceIn(6, 40).dp
    val balanceTextSize = (if (isHalfWidth) 24 else (38 * heightScale).toInt()).coerceIn(18, 48).sp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .liquidGlassCard(cornerRadius = 28.dp, containerColor = resolvedBg, borderColor = Color.White.copy(alpha = 0.6f)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingVal)
        ) {
            Text(
                text = "AVAILABLE BALANCE",
                style = if (isHalfWidth) {
                    MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                } else {
                    MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
                },
                color = Color.White.copy(alpha = 0.8f)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = if (balance % 1.0 == 0.0) {
                        String.format("%,.0f", balance)
                    } else {
                        String.format("%,.2f", balance)
                    },
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = balanceTextSize,
                        letterSpacing = (-1).sp
                    ),
                    color = Color.White
                )
                Text(
                    text = "LKR",
                    style = if (isHalfWidth) MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.padding(bottom = if (isHalfWidth) 2.dp else 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(verticalSpacerHeight))

            if (isHalfWidth) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onAddIncome,
                        modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f))
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Income", tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                    IconButton(
                        onClick = onLogExpense,
                        modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f))
                    ) {
                        Icon(imageVector = Icons.Default.TrendingDown, contentDescription = "Log Expense", tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                    IconButton(
                        onClick = onAIScan,
                        modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.White)
                    ) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "Scan Bill", tint = EmeraldIncome, modifier = Modifier.size(14.dp))
                    }
                    IconButton(
                        onClick = onAIVoiceClick,
                        modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.White)
                    ) {
                        Icon(imageVector = Icons.Default.Mic, contentDescription = "Voice Assistant", tint = Color(0xFF673AB7), modifier = Modifier.size(14.dp))
                    }
                }
            } else {
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

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onAIScan,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("ai_bill_scan_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = EmeraldIncome
                        ),
                        shape = CircleShape
                    ) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Photo Scan", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 12.sp))
                    }

                    Button(
                        onClick = onAIVoiceClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("ai_voice_assistant_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.9f),
                            contentColor = Color(0xFF673AB7)
                        ),
                        shape = CircleShape
                    ) {
                        Icon(imageVector = Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Voice AI", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 12.sp))
                    }
                }
            }
        }
    }
}

@Composable
fun BudgetThreeColumnsAllocation(
    summary: BudgetSummary,
    needsWarning: Boolean = false,
    wantsWarning: Boolean = false,
    savingsWarning: Boolean = false,
    selectedBucket: String? = null,
    onBucketClick: (String) -> Unit = {},
    isHalfWidth: Boolean = false,
    heightScale: Float = 1.0f
) {
    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "warningGlow")
    val alphaAnim = if (needsWarning || wantsWarning || savingsWarning) {
        val anim = infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 0.95f,
            animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                animation = androidx.compose.animation.core.tween(950, easing = androidx.compose.animation.core.LinearEasing),
                repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
            ),
            label = "alertAlpha"
        )
        anim.value
    } else {
        0.4f
    }

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

        val needsRatio = if (summary.needsBudget > 0.0) {
            (summary.needsExpenses / summary.needsBudget).toFloat().coerceIn(0f, 1f)
        } else 0.0f
        val needsBorderColor = if (needsWarning) DangerRed.copy(alpha = alphaAnim) else ElectricNeeds.copy(alpha = 0.4f)
        val needsProgressColor = if (needsWarning) DangerRed else ElectricNeeds
        val needsLabelColor = if (needsWarning) DangerRed else ElectricNeeds

        val wantsRatio = if (summary.wantsBudget > 0.0) {
            (summary.wantsExpenses / summary.wantsBudget).toFloat().coerceIn(0f, 1f)
        } else 0.0f
        val wantsBorderColor = if (wantsWarning) DangerRed.copy(alpha = alphaAnim) else WarmWants.copy(alpha = 0.4f)
        val wantsProgressColor = if (wantsWarning) DangerRed else WarmWants
        val wantsLabelColor = if (wantsWarning) DangerRed else WarmWants

        val savingsRatio = if (summary.savingsBudget > 0.0) {
            (summary.savingsExpenses / summary.savingsBudget).toFloat().coerceIn(0f, 1f)
        } else 0.0f
        val savingsBorderColor = if (savingsWarning) DangerRed.copy(alpha = alphaAnim) else SavingsIndigo.copy(alpha = 0.4f)
        val savingsProgressColor = if (savingsWarning) DangerRed else SavingsIndigo
        val savingsLabelColor = if (savingsWarning) DangerRed else SavingsIndigo

        if (isHalfWidth) {
            // Stacked vertically in compact mode
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy((6 * heightScale).toInt().coerceIn(4, 16).dp)
            ) {
                // Needs
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlassCard(
                            cornerRadius = 12.dp,
                            containerColor = if (selectedBucket?.uppercase() == "NEEDS") ElectricNeeds.copy(alpha = 0.18f) else if (needsWarning) DangerRed.copy(alpha = 0.08f) else ElectricNeedsBg.copy(alpha = 0.58f),
                            borderColor = if (selectedBucket?.uppercase() == "NEEDS") ElectricNeeds else needsBorderColor
                        )
                        .clickable { onBucketClick("NEEDS") }
                        .padding((10 * heightScale).toInt().coerceIn(6, 20).dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (needsWarning) "NEEDS ! LIMIT" else "NEEDS (50%)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = needsLabelColor
                            )
                            Text(
                                text = if (summary.needsBudget > 0.0) "${(needsRatio * 100).toInt()}%" else "0%",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                        }
                        LinearProgressIndicator(
                            progress = { needsRatio },
                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                            color = needsProgressColor,
                            trackColor = if (needsWarning) DangerRed.copy(alpha = 0.2f) else ElectricNeedsBorder
                        )
                    }
                }

                // Wants
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlassCard(
                            cornerRadius = 12.dp,
                            containerColor = if (selectedBucket?.uppercase() == "WANTS") WarmWants.copy(alpha = 0.18f) else if (wantsWarning) DangerRed.copy(alpha = 0.08f) else WarmWantsBg.copy(alpha = 0.58f),
                            borderColor = if (selectedBucket?.uppercase() == "WANTS") WarmWants else wantsBorderColor
                        )
                        .clickable { onBucketClick("WANTS") }
                        .padding((10 * heightScale).toInt().coerceIn(6, 20).dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (wantsWarning) "WANTS ! LIMIT" else "WANTS (30%)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = wantsLabelColor
                            )
                            Text(
                                text = if (summary.wantsBudget > 0.0) "${(wantsRatio * 100).toInt()}%" else "0%",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                        }
                        LinearProgressIndicator(
                            progress = { wantsRatio },
                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                            color = wantsProgressColor,
                            trackColor = if (wantsWarning) DangerRed.copy(alpha = 0.2f) else WarmWantsBorder
                        )
                    }
                }

                // Savings
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlassCard(
                            cornerRadius = 12.dp,
                            containerColor = if (selectedBucket?.uppercase() == "SAVINGS") SavingsIndigo.copy(alpha = 0.18f) else if (savingsWarning) DangerRed.copy(alpha = 0.08f) else SavingsIndigoBg.copy(alpha = 0.58f),
                            borderColor = if (selectedBucket?.uppercase() == "SAVINGS") SavingsIndigo else savingsBorderColor
                        )
                        .clickable { onBucketClick("SAVINGS") }
                        .padding((10 * heightScale).toInt().coerceIn(6, 20).dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (savingsWarning) "SAVINGS ! LIMIT" else "SAVINGS (20%)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = savingsLabelColor
                            )
                            Text(
                                text = if (summary.savingsBudget > 0.0) "${(savingsRatio * 100).toInt()}%" else "0%",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                        }
                        LinearProgressIndicator(
                            progress = { savingsRatio },
                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                            color = savingsProgressColor,
                            trackColor = if (savingsWarning) DangerRed.copy(alpha = 0.2f) else SavingsIndigoBorder
                        )
                    }
                }
            }
        } else {
            // Row based layout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Needs Column (50%)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .liquidGlassCard(
                            cornerRadius = 16.dp, 
                            containerColor = if (selectedBucket?.uppercase() == "NEEDS") ElectricNeeds.copy(alpha = 0.18f) else if (needsWarning) DangerRed.copy(alpha = 0.08f) else ElectricNeedsBg.copy(alpha = 0.58f), 
                            borderColor = if (selectedBucket?.uppercase() == "NEEDS") ElectricNeeds else needsBorderColor
                        )
                        .clickable { onBucketClick("NEEDS") }
                        .padding((12 * heightScale).toInt().coerceIn(8, 24).dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = if (needsWarning) "NEEDS ! LIMIT" else "NEEDS (50%)",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = needsLabelColor
                        )
                        LinearProgressIndicator(
                            progress = { needsRatio },
                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                            color = needsProgressColor,
                            trackColor = if (needsWarning) DangerRed.copy(alpha = 0.2f) else ElectricNeedsBorder
                        )
                        Text(
                            text = if (summary.needsBudget > 0.0) "${(needsRatio * 100).toInt()}% Used" else "0% Used",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                    }
                }

                // Wants Column (30%)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .liquidGlassCard(
                            cornerRadius = 16.dp, 
                            containerColor = if (selectedBucket?.uppercase() == "WANTS") WarmWants.copy(alpha = 0.18f) else if (wantsWarning) DangerRed.copy(alpha = 0.08f) else WarmWantsBg.copy(alpha = 0.58f), 
                            borderColor = if (selectedBucket?.uppercase() == "WANTS") WarmWants else wantsBorderColor
                        )
                        .clickable { onBucketClick("WANTS") }
                        .padding((12 * heightScale).toInt().coerceIn(8, 24).dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = if (wantsWarning) "WANTS ! LIMIT" else "WANTS (30%)",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = wantsLabelColor
                        )
                        LinearProgressIndicator(
                            progress = { wantsRatio },
                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                            color = wantsProgressColor,
                            trackColor = if (wantsWarning) DangerRed.copy(alpha = 0.2f) else WarmWantsBorder
                        )
                        Text(
                            text = if (summary.wantsBudget > 0.0) "${(wantsRatio * 100).toInt()}% Used" else "0% Used",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                    }
                }

                // Savings Column (20%)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .liquidGlassCard(
                            cornerRadius = 16.dp, 
                            containerColor = if (selectedBucket?.uppercase() == "SAVINGS") SavingsIndigo.copy(alpha = 0.18f) else if (savingsWarning) DangerRed.copy(alpha = 0.08f) else SavingsIndigoBg.copy(alpha = 0.58f), 
                            borderColor = if (selectedBucket?.uppercase() == "SAVINGS") SavingsIndigo else savingsBorderColor
                        )
                        .clickable { onBucketClick("SAVINGS") }
                        .padding((12 * heightScale).toInt().coerceIn(8, 24).dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = if (savingsWarning) "SAVINGS ! LIMIT" else "SAVINGS (20%)",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = savingsLabelColor
                        )
                        LinearProgressIndicator(
                            progress = { savingsRatio },
                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                            color = savingsProgressColor,
                            trackColor = if (savingsWarning) DangerRed.copy(alpha = 0.2f) else SavingsIndigoBorder
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

data class SubTransactionItem(val title: String, val amount: Double)

fun parseSubTransactions(str: String): List<SubTransactionItem> {
    if (str.isBlank()) return emptyList()
    return str.split(";").filter { it.isNotBlank() }.mapNotNull {
        val parts = it.split("|")
        if (parts.size >= 2) {
            val t = parts[0]
            val a = parts[1].toDoubleOrNull() ?: 0.0
            SubTransactionItem(t, a)
        } else null
    }
}

fun formatSubTransactions(list: List<SubTransactionItem>): String {
    return list.joinToString(";") { "${it.title.replace(";", "").replace("|", "")}|${it.amount}" }
}

@Composable
fun SubTransactionSection(
    parentType: String, // "Expense" or "Income"
    parentAmount: Double,
    subTxList: List<SubTransactionItem>,
    onListChange: (List<SubTransactionItem>) -> Unit,
    accentColor: Color
) {
    var isExpanded by remember { mutableStateOf(subTxList.isNotEmpty()) }
    var subTitle by remember { mutableStateOf("") }
    var subAmount by remember { mutableStateOf("") }

    val totalAllocated = remember(subTxList) { subTxList.sumOf { it.amount } }
    val remainingBalance = parentAmount - totalAllocated

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceDarkSecondary.copy(alpha = 0.5f))
            .border(1.dp, GridDividerLabel, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sub-Transactions Breakdown",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
            }
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }

        if (isExpanded) {
            Spacer(modifier = Modifier.height(12.dp))

            // Text info row
            if (subTxList.isEmpty()) {
                Text(
                    text = "No sub-transactions added to this $parentType yet. Add below to allocate.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    subTxList.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.4f))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                                Text(
                                    text = formatLKR(item.amount),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = accentColor
                                )
                            }
                            IconButton(
                                onClick = {
                                    val newList = subTxList.toMutableList()
                                    newList.remove(item)
                                    onListChange(newList)
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete sub-item",
                                    tint = DangerRed,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Quick add row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = subTitle,
                    onValueChange = { subTitle = it },
                    placeholder = { Text("Item Name", fontSize = 12.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = GridDividerLabel,
                        focusedContainerColor = Color.White.copy(alpha = 0.5f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.weight(1.2f),
                    textStyle = MaterialTheme.typography.bodyMedium
                )

                OutlinedTextField(
                    value = subAmount,
                    onValueChange = { subAmount = it },
                    placeholder = { Text("Amount", fontSize = 12.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = GridDividerLabel,
                        focusedContainerColor = Color.White.copy(alpha = 0.5f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.weight(0.8f),
                    textStyle = MaterialTheme.typography.bodyMedium
                )

                IconButton(
                    onClick = {
                        val amt = subAmount.toDoubleOrNull() ?: 0.0
                        if (subTitle.isNotBlank() && amt > 0) {
                            val newList = subTxList.toMutableList()
                            newList.add(SubTransactionItem(subTitle.trim(), amt))
                            onListChange(newList)
                            subTitle = ""
                            subAmount = ""
                        }
                    },
                    modifier = Modifier
                        .background(accentColor, CircleShape)
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add sub-transaction",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Balance indicator box - ONLY add if user adds an expense or income
            if (subTxList.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.08f))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Available balance (For $parentType: ${formatLKR(parentAmount)}): ${formatLKR(remainingBalance)}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = accentColor,
                        textAlign = TextAlign.Center
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
    val subTxItems = remember(transaction.subTransactionsStr) {
        parseSubTransactions(transaction.subTransactionsStr ?: "")
    }
    var cardExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .liquidGlassCard(cornerRadius = 16.dp, containerColor = Color.White.copy(alpha = 0.52f)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onEdit() }
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
                        if (subTxItems.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .size(3.dp)
                                    .clip(CircleShape)
                                    .background(TextSecondary)
                            )
                            Text(
                                text = "${subTxItems.size} items",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = TextSecondary
                            )
                        }
                    }
                }

                // Amount & Delete
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
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

                    if (subTxItems.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = { cardExpanded = !cardExpanded },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (cardExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Show Breakdown",
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            if (subTxItems.isNotEmpty() && cardExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceDarkSecondary.copy(alpha = 0.3f))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val accentColor = if (transaction.isIncome) ElectricNeeds else DangerRed
                    subTxItems.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "• ${item.title}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = formatLKR(item.amount),
                                style = MaterialTheme.typography.bodySmall,
                                color = accentColor
                            )
                        }
                    }
                    val totalAllocated = subTxItems.sumOf { it.amount }
                    val remainingBalance = transaction.amount - totalAllocated
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .height(1.dp)
                            .background(GridDividerLabel)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Remaining Allocation",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = TextSecondary
                        )
                        Text(
                            text = formatLKR(remainingBalance),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = accentColor
                        )
                    }
                }
            }
        }
    }
}

// Dialog Implementation
@Composable
fun AddIncomeDialog(
    viewModel: LankaBudgetViewModel,
    onDismiss: () -> Unit,
    onAdd: (title: String, amount: Double, category: String, subTransactionsStr: String) -> Unit,
    editingTransaction: Transaction? = null
) {
    var title by remember { mutableStateOf(editingTransaction?.title ?: "") }
    var amount by remember { mutableStateOf(editingTransaction?.amount?.toString() ?: "") }
    var category by remember { mutableStateOf(editingTransaction?.category ?: "Salary") }
    var subTxList by remember { mutableStateOf(parseSubTransactions(editingTransaction?.subTransactionsStr ?: "")) }
    var activePopupTab by remember { mutableStateOf(0) } // 0: Main Details, 1: Breakdown

    val customCats by viewModel.customCategories.collectAsState()
    val categories = customCats.filter { it.isIncome }.map { it.name }.ifEmpty {
        listOf("Salary", "Freelance", "Bonus", "Investment", "Gift", "Other")
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
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
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

                // Tab Switcher Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceDarkSecondary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (activePopupTab == 0) ElectricNeeds else Color.Transparent)
                            .clickable { activePopupTab = 0 }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Main Details",
                            color = if (activePopupTab == 0) Color.White else TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (activePopupTab == 1) ElectricNeeds else Color.Transparent)
                            .clickable { activePopupTab = 1 }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Breakdown",
                                color = if (activePopupTab == 1) Color.White else TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (subTxList.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(if (activePopupTab == 1) Color.White.copy(alpha = 0.25f) else ElectricNeeds.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = subTxList.size.toString(),
                                        color = if (activePopupTab == 1) Color.White else ElectricNeeds,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                if (activePopupTab == 0) {
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
                } else {
                    SubTransactionSection(
                        parentType = "Income",
                        parentAmount = amount.toDoubleOrNull() ?: 0.0,
                        subTxList = subTxList,
                        onListChange = { subTxList = it },
                        accentColor = ElectricNeeds
                    )
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
                                onAdd(title, amt, category, formatSubTransactions(subTxList))
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
    viewModel: LankaBudgetViewModel,
    onDismiss: () -> Unit,
    onAdd: (title: String, amount: Double, bucket: String, category: String, subTransactionsStr: String) -> Unit,
    needsMultiplier: Boolean,
    editingTransaction: Transaction? = null
) {
    var title by remember { mutableStateOf(editingTransaction?.title ?: "") }
    var amount by remember { mutableStateOf(editingTransaction?.amount?.toString() ?: "") }
    var bucket by remember { mutableStateOf(editingTransaction?.bucket ?: "NEEDS") }
    var category by remember { mutableStateOf(editingTransaction?.category ?: "Bills & Rent") }
    var subTxList by remember { mutableStateOf(parseSubTransactions(editingTransaction?.subTransactionsStr ?: "")) }
    var activePopupTab by remember { mutableStateOf(0) } // 0: Main Details, 1: Breakdown

    val customCats by viewModel.customCategories.collectAsState()
    val categories = customCats.filter { !it.isIncome && it.bucket == bucket }.map { it.name }.ifEmpty {
        when (bucket) {
            "NEEDS" -> listOf("Bills & Rent", "Utilities", "Transport", "Mandatory Debt", "Groceries", "Other")
            "WANTS" -> listOf("Dine Out", "Cinema & Movies", "Hobbies", "Gifts", "Apparel", "Other")
            else -> listOf("Money Market Fund (MMF)", "Fixed Deposit", "Emergency Fund", "Crypto", "Other")
        }
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
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
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

                // Tab Switcher Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceDarkSecondary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (activePopupTab == 0) DangerRed else Color.Transparent)
                            .clickable { activePopupTab = 0 }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Main Details",
                            color = if (activePopupTab == 0) Color.White else TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (activePopupTab == 1) DangerRed else Color.Transparent)
                            .clickable { activePopupTab = 1 }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Breakdown",
                                color = if (activePopupTab == 1) Color.White else TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (subTxList.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(if (activePopupTab == 1) Color.White.copy(alpha = 0.25f) else DangerRed.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = subTxList.size.toString(),
                                        color = if (activePopupTab == 1) Color.White else DangerRed,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                if (activePopupTab == 0) {
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
                                            val subCats = customCats.filter { !it.isIncome && it.bucket == bkt }.map { it.name }
                                            category = subCats.firstOrNull() ?: when (bkt) {
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
                } else {
                    SubTransactionSection(
                        parentType = "Expense",
                        parentAmount = amount.toDoubleOrNull() ?: 0.0,
                        subTxList = subTxList,
                        onListChange = { subTxList = it },
                        accentColor = DangerRed
                    )
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
                                onAdd(title, amt, bucket, category, formatSubTransactions(subTxList))
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

@Composable
fun MonthlyFinancialReportingHub(
    transactions: List<Transaction>,
    repayments: List<com.example.data.model.RepaymentLog>
) {
    val sdfYearMonth = remember { java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.getDefault()) }
    val sdfLabel = remember { java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.getDefault()) }

    val availableMonths = remember(transactions, repayments) {
        val monthsSet = mutableSetOf<String>()
        val currentMonth = sdfYearMonth.format(java.util.Date())
        monthsSet.add(currentMonth)
        
        transactions.forEach {
            monthsSet.add(sdfYearMonth.format(java.util.Date(it.timestamp)))
        }
        repayments.forEach {
            monthsSet.add(sdfYearMonth.format(java.util.Date(it.timestamp)))
        }
        
        monthsSet.sortedDescending()
    }

    var selectedReportMonth by remember(availableMonths) { 
        mutableStateOf(availableMonths.firstOrNull() ?: sdfYearMonth.format(java.util.Date())) 
    }

    val monthIncomeList = remember(transactions, selectedReportMonth) {
        transactions.filter { it.isIncome && sdfYearMonth.format(java.util.Date(it.timestamp)) == selectedReportMonth }
    }
    val monthExpenseList = remember(transactions, selectedReportMonth) {
        transactions.filter { !it.isIncome && sdfYearMonth.format(java.util.Date(it.timestamp)) == selectedReportMonth }
    }
    val selectedRepayments = remember(repayments, selectedReportMonth) {
        repayments.filter { sdfYearMonth.format(java.util.Date(it.timestamp)) == selectedReportMonth }
    }

    val totalIncome = remember(monthIncomeList) { monthIncomeList.sumOf { it.amount } }
    val expensePartition = remember(monthExpenseList) { monthExpenseList.filter { it.bucket != "SAVINGS" } }
    val totalExpenses = remember(expensePartition) { expensePartition.sumOf { it.amount } }
    val totalSavingsAllocated = remember(monthExpenseList) { monthExpenseList.filter { it.bucket == "SAVINGS" }.sumOf { it.amount } }
    val totalDebtRepaid = remember(selectedRepayments) { selectedRepayments.sumOf { it.amountPaid } }

    val expenseByCategory = remember(expensePartition) {
        expensePartition.groupBy { it.category }.mapValues { entry -> entry.value.sumOf { it.amount } }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .liquidGlassCard(cornerRadius = 24.dp, containerColor = Color.White.copy(alpha = 0.52f)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "MONTHLY PERFORMANCE REPORT",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = ElectricNeeds
                    )
                    Text(
                        text = "Aggregated breakdown of month performance",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            // Month Selector Row
            val currentMonthIndex = availableMonths.indexOf(selectedReportMonth)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceDarkSecondary)
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (currentMonthIndex < availableMonths.lastIndex) {
                            selectedReportMonth = availableMonths[currentMonthIndex + 1]
                        }
                    },
                    enabled = currentMonthIndex < availableMonths.lastIndex,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowLeft,
                        contentDescription = "Prev Month",
                        tint = if (currentMonthIndex < availableMonths.lastIndex) TextPrimary else TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                var expandedMonthDropdown by remember { mutableStateOf(false) }
                Box(modifier = Modifier.wrapContentSize(), contentAlignment = Alignment.Center) {
                    val parsedDate = try { sdfYearMonth.parse(selectedReportMonth) } catch(e: Exception) { null }
                    val label = parsedDate?.let { sdfLabel.format(it) } ?: selectedReportMonth
                    Text(
                        text = label.uppercase(),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                        color = ElectricNeeds,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { expandedMonthDropdown = true }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )

                    DropdownMenu(
                        expanded = expandedMonthDropdown,
                        onDismissRequest = { expandedMonthDropdown = false },
                        modifier = Modifier.background(SurfaceDarkSecondary)
                    ) {
                        availableMonths.forEach { m ->
                            val dateObj = try { sdfYearMonth.parse(m) } catch(e: Exception) { null }
                            val dateLabel = dateObj?.let { sdfLabel.format(it) } ?: m
                            DropdownMenuItem(
                                text = { Text(dateLabel, color = TextPrimary, fontWeight = FontWeight.Bold) },
                                onClick = {
                                    selectedReportMonth = m
                                    expandedMonthDropdown = false
                                }
                            )
                        }
                    }
                }

                IconButton(
                    onClick = {
                        if (currentMonthIndex > 0) {
                            selectedReportMonth = availableMonths[currentMonthIndex - 1]
                        }
                    },
                    enabled = currentMonthIndex > 0,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Next Month",
                        tint = if (currentMonthIndex > 0) TextPrimary else TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // 2x2 Grid of Report Cards
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ReportGridCard(
                        title = "TOTAL INCOME",
                        amount = totalIncome,
                        color = SettlementSuccess,
                        icon = Icons.Default.TrendingUp,
                        modifier = Modifier.weight(1f)
                    )
                    ReportGridCard(
                        title = "TOTAL EXPENSES",
                        amount = totalExpenses,
                        color = DangerRed,
                        icon = Icons.Default.TrendingDown,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ReportGridCard(
                        title = "SAVINGS ALLOC",
                        amount = totalSavingsAllocated,
                        color = SavingsIndigo,
                        icon = Icons.Default.AccountBalanceWallet,
                        modifier = Modifier.weight(1f)
                    )
                    ReportGridCard(
                        title = "DEBT REPAID",
                        amount = totalDebtRepaid,
                        color = ElectricNeeds,
                        icon = Icons.Default.CreditCard,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Segmented category breakdown bar
            if (expenseByCategory.isNotEmpty()) {
                val totalExp = expenseByCategory.values.sumOf { it }
                val colors = listOf(ElectricNeeds, WarmWants, SavingsIndigo, WarmWantsBorder, DangerRed, ElectricNeedsBorder, SettlementSuccess)
                
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "EXPENSE SOURCE SPREAD",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                        color = TextSecondary
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(SurfaceDarkSecondary)
                    ) {
                        var colorIdx = 0
                        expenseByCategory.forEach { (catName, amount) ->
                            val weight = (amount / totalExp).toFloat()
                            if (weight > 0.001f) {
                                val color = colors[colorIdx % colors.size]
                                colorIdx++
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .weight(weight)
                                        .background(color)
                                )
                            }
                        }
                    }
                    
                    // Legends
                    FlowLegend(
                        expenseByCategory = expenseByCategory,
                        colors = colors
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No structured expenses found for this period.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }

            // Breakdown table of categories
            if (expenseByCategory.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "DETAILED SPREADSHEEET TABLE",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp),
                        color = TextSecondary
                    )
                    expenseByCategory.entries.sortedByDescending { it.value }.forEach { entry ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceDarkSecondary.copy(alpha = 0.45f))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = entry.key,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = formatLKR(entry.value),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReportGridCard(
    title: String,
    amount: Double,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.08f))
            .border(1.dp, color.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                    color = TextSecondary
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(14.dp)
                )
            }
            Text(
                formatLKR(amount),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp),
                color = TextPrimary
            )
        }
    }
}

@Composable
fun FlowLegend(expenseByCategory: Map<String, Double>, colors: List<Color>, modifier: Modifier = Modifier) {
    val items = expenseByCategory.entries.toList()
    val total = expenseByCategory.values.sumOf { it }
    
    items.chunked(2).forEach { rowItems ->
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            rowItems.forEach { entry ->
                val idx = items.indexOf(entry)
                val color = colors[idx % colors.size]
                val pct = if (total > 0) (entry.value / total * 100).toInt() else 0
                
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                    Text(
                        text = "${entry.key} ($pct%)",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (rowItems.size == 1) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIBillScannerDialog(
    viewModel: LankaBudgetViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isScanning by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Forms auto-filled states after scan
    var transactionTitle by remember { mutableStateOf("") }
    var transactionAmount by remember { mutableStateOf("") }
    var isIncomeType by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("Groceries") }
    var selectedBucket by remember { mutableStateOf("NEEDS") }
    
    // Sub transactions parsed
    var subTxList by remember { mutableStateOf<List<SubTransactionItem>>(emptyList()) }

    var scanCompleted by remember { mutableStateOf(false) }

    // Activity launchers for Image picking & Taking photo
    val selectPictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val bitmap = context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it)
                }
                if (bitmap != null) {
                    selectedBitmap = bitmap
                    errorMessage = null
                    scanCompleted = false
                }
            } catch (e: Exception) {
                errorMessage = "Failed to load image: ${e.localizedMessage}"
            }
        }
    }

    val takePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            selectedBitmap = bitmap
            errorMessage = null
            scanCompleted = false
        }
    }

    var hasLaunchedCamera by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        val isEmulator = android.os.Build.FINGERPRINT.startsWith("generic") ||
                android.os.Build.FINGERPRINT.startsWith("unknown") ||
                android.os.Build.MODEL.contains("google_sdk") ||
                android.os.Build.MODEL.contains("Emulator") ||
                android.os.Build.MODEL.contains("Android SDK built for x86") ||
                android.os.Build.MANUFACTURER.contains("Genymotion") ||
                android.os.Build.HARDWARE.contains("goldfish") ||
                android.os.Build.HARDWARE.contains("ranchu") ||
                android.os.Build.HARDWARE.contains("vbox86") ||
                android.os.Build.PRODUCT.contains("sdk") ||
                android.os.Build.PRODUCT.contains("google_sdk") ||
                android.os.Build.PRODUCT.contains("sdk_x86") ||
                android.os.Build.PRODUCT.contains("vbox86p")

        if (!hasLaunchedCamera && selectedBitmap == null) {
            hasLaunchedCamera = true
            if (!isEmulator) {
                try {
                    takePhotoLauncher.launch(null)
                } catch (e: Exception) {
                    errorMessage = "Camera capture is not supported on this device/emulator: ${e.localizedMessage}"
                }
            } else {
                errorMessage = "Running on Emulator: Camera auto-open skipped. Please click 'From Gallery' to load a demo bill image!"
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .liquidGlassCard(cornerRadius = 24.dp)
                .testTag("ai_bill_scanner_dialog_card"),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "🔮 Smart AI Bill Reader",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = TextPrimary
                        )
                        Text(
                            text = "Powered by Gemini Multimodal AI",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.clip(CircleShape).background(Color.White.copy(alpha = 0.05f))
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Close", tint = DangerRed)
                    }
                }

                HorizontalDivider(color = GridDividerLabel.copy(alpha = 0.5f))

                // Options to select images
                if (selectedBitmap == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.04f))
                            .border(1.dp, GridDividerLabel.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .clickable { selectPictureLauncher.launch("image/*") }
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = "Upload icon",
                                modifier = Modifier.size(44.dp),
                                tint = ElectricNeeds
                            )
                            Text(
                                text = "Choose a bill screenshot from Gallery or tap above to upload",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "(Camera will also auto-open for instant capture on enter.)",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    // Preview existing bitmap
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Black.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.foundation.Image(
                            bitmap = selectedBitmap!!.asImageBitmap(),
                            contentDescription = "Selected Bill",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = androidx.compose.ui.layout.ContentScale.Fit
                        )

                        // Clear button on hover corner
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            contentAlignment = Alignment.TopEnd
                        ) {
                            Button(
                                onClick = {
                                    selectedBitmap = null
                                    scanCompleted = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("Clear", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Choose image buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            try {
                                takePhotoLauncher.launch(null)
                            } catch (e: Exception) {
                                errorMessage = "Camera app is not available on this device/emulator: ${e.localizedMessage}"
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceDarkSecondary, contentColor = TextPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = "Camera", modifier = Modifier.size(16.dp))
                            Text("Take Photo", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = {
                            try {
                                selectPictureLauncher.launch("image/*")
                            } catch (e: Exception) {
                                errorMessage = "Image gallery is not accessible: ${e.localizedMessage}"
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceDarkSecondary, contentColor = TextPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Image, contentDescription = "Gallery", modifier = Modifier.size(16.dp))
                            Text("From Gallery", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Error section
                errorMessage?.let { error ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DangerRed.copy(alpha = 0.12f))
                            .border(1.dp, DangerRed.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = error,
                            color = DangerRed,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Trigger scan action
                if (selectedBitmap != null && !scanCompleted) {
                    Button(
                        onClick = {
                            isScanning = true
                            errorMessage = null
                            viewModel.scanBillWithGemini(
                                bitmap = selectedBitmap!!,
                                onSuccess = { result ->
                                    isScanning = false
                                    transactionTitle = result.title
                                    transactionAmount = result.amount.toString()
                                    isIncomeType = result.isIncome
                                    selectedCategory = result.category
                                    selectedBucket = result.bucket
                                    
                                    // Map breakdown
                                    subTxList = result.suggestedSubBreakdown.map {
                                        SubTransactionItem(it.first, it.second)
                                    }
                                    scanCompleted = true
                                    android.widget.Toast.makeText(context, "AI Scan Complete & Details Auto-filled!", android.widget.Toast.LENGTH_LONG).show()
                                },
                                onFailure = { m ->
                                    isScanning = false
                                    errorMessage = m
                                }
                            )
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricNeeds),
                        shape = RoundedCornerShape(14.dp),
                        enabled = !isScanning
                    ) {
                        if (isScanning) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Analyzing image with Gemini...", fontWeight = FontWeight.Bold)
                        } else {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = "Magic")
                                Text("Analyze Bill with Gemini AI", fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                }

                // Dynamic UI showing Auto-filled details for adjustment
                if (scanCompleted) {
                    HorizontalDivider(color = GridDividerLabel.copy(alpha = 0.5f))

                    Text(
                        text = "🔎 AUTO-FILLED TRANSACTION",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp),
                        color = ElectricNeeds
                    )

                    // Type Toggle (Income vs Expense)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceDarkSecondary)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Button(
                            onClick = { isIncomeType = false },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isIncomeType) DangerRed.copy(alpha = 0.2f) else Color.Transparent,
                                contentColor = if (!isIncomeType) DangerRed else TextSecondary
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Expense", fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { isIncomeType = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isIncomeType) EmeraldIncome.copy(alpha = 0.2f) else Color.Transparent,
                                contentColor = if (isIncomeType) EmeraldIncome else TextSecondary
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Income", fontWeight = FontWeight.Bold)
                        }
                    }

                    // Merchant title text field
                    OutlinedTextField(
                        value = transactionTitle,
                        onValueChange = { transactionTitle = it },
                        label = { Text("Merchant / Description") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = ElectricNeeds,
                            unfocusedBorderColor = GridDividerLabel
                        )
                    )

                    // Total amount text field
                    OutlinedTextField(
                        value = transactionAmount,
                        onValueChange = { transactionAmount = it },
                        label = { Text("Total Amount (LKR)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = ElectricNeeds,
                            unfocusedBorderColor = GridDividerLabel
                        )
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Category Allocation:", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        
                        val categories = if (isIncomeType) {
                            listOf("Salary", "Freelance", "Investment Income", "Other")
                        } else {
                            listOf("Groceries", "Utilities", "Bills & Rent", "Transport", "Mandatory Debt", "Dine Out", "Cinema & Movies", "Hobbies", "Gifts", "Apparel", "Other")
                        }

                        Box(modifier = Modifier.fillMaxWidth()) {
                            androidx.compose.foundation.lazy.LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(categories) { cat ->
                                    val isSelected = selectedCategory == cat
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(if (isSelected) ElectricNeeds else Color.White.copy(alpha = 0.05f))
                                            .border(1.dp, if (isSelected) ElectricNeeds else GridDividerLabel, RoundedCornerShape(20.dp))
                                            .clickable { selectedCategory = cat }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(text = cat, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    if (!isIncomeType) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Designated Bucket Allocation:", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("NEEDS", "WANTS", "SAVINGS").forEach { bucket ->
                                    val isSelected = selectedBucket == bucket
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isSelected) {
                                                    when (bucket) {
                                                        "NEEDS" -> ElectricNeeds.copy(alpha = 0.2f)
                                                        "WANTS" -> WarmWants.copy(alpha = 0.2f)
                                                        else -> SavingsIndigo.copy(alpha = 0.2f)
                                                    }
                                                } else Color.White.copy(alpha = 0.05f)
                                            )
                                            .border(
                                                1.dp,
                                                if (isSelected) {
                                                    when (bucket) {
                                                        "NEEDS" -> ElectricNeeds
                                                        "WANTS" -> WarmWants
                                                        else -> SavingsIndigo
                                                    }
                                                } else GridDividerLabel,
                                                RoundedCornerShape(12.dp)
                                            )
                                            .clickable { selectedBucket = bucket }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = bucket,
                                            color = if (isSelected) {
                                                when (bucket) {
                                                    "NEEDS" -> ElectricNeeds
                                                    "WANTS" -> WarmWants
                                                    else -> SavingsIndigo
                                                }
                                            } else TextSecondary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Itemised breakdown
                    if (subTxList.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "📊 ITEMISED BREAKDOWN:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextSecondary
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.03f))
                                .border(1.dp, GridDividerLabel, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            subTxList.forEach { item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = formatLKR(item.amount),
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = TextPrimary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            val finalAmt = transactionAmount.toDoubleOrNull() ?: 0.0
                            if (transactionTitle.isNotBlank() && finalAmt > 0) {
                                if (isIncomeType) {
                                    viewModel.addIncome(
                                        title = transactionTitle,
                                        amount = finalAmt,
                                        category = selectedCategory,
                                        subTransactionsStr = formatSubTransactions(subTxList)
                                    )
                                } else {
                                    viewModel.addExpense(
                                        title = transactionTitle,
                                        amount = finalAmt,
                                        bucket = selectedBucket,
                                        category = selectedCategory,
                                        subTransactionsStr = formatSubTransactions(subTxList)
                                    )
                                }
                                onDismiss()
                            } else {
                                android.widget.Toast.makeText(context, "Please enter a valid title and amount", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (isIncomeType) EmeraldIncome else DangerRed),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(text = "Save Transaction to Ledger", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

fun resolveTint(tint: String?, isSystemDark: Boolean): Color {
    val alpha = if (isSystemDark) 0.15f else 0.45f
    val base = when (tint) {
        "Indigo" -> Color(0xFF3F51B5)
        "Emerald" -> Color(0xFF00C853)
        "Coral" -> Color(0xFFFF5722)
        "Cyan" -> Color(0xFF00E5FF)
        "SunsetGold" -> Color(0xFFFFB300)
        else -> Color.White
    }
    return base.copy(alpha = if (tint == "White" || tint == null) (if (isSystemDark) 0.12f else 0.45f) else alpha)
}

@Composable
fun DashboardWidgetRenderer(
    widget: com.example.data.model.DashboardWidget,
    viewModel: LankaBudgetViewModel,
    budgetSummary: BudgetSummary,
    availableBalance: Double,
    totalInvestmentBalance: Double,
    totalDebtBalance: Double,
    netWorth: Double,
    needsLimit: Double,
    wantsLimit: Double,
    savingsLimit: Double,
    needsWarning: Boolean,
    wantsWarning: Boolean,
    savingsWarning: Boolean,
    filteredTransactions: List<Transaction>,
    onAddIncome: () -> Unit,
    onLogExpense: () -> Unit,
    onAIScan: () -> Unit,
    onAIVoiceClick: () -> Unit = {},
    onEditTransaction: (Transaction) -> Unit,
    onDeleteTransaction: (Transaction) -> Unit,
    selectedBucketFilter: String?,
    onBucketFilterChange: (String?) -> Unit,
    isDesigning: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onToggleEnabled: () -> Unit,
    onCycleTint: () -> Unit,
    onUpdateSize: (gridWidth: Int, gridHeight: Int) -> Unit,
    onLongPressWidget: () -> Unit,
    isSelected: Boolean,
    onSelectWidget: () -> Unit,
    onDragStateChanged: (Boolean) -> Unit,
    onResizeStateChanged: (Boolean) -> Unit
) {
    val isSystemDark = SlateDark != Color(0xFFFBFDFD)
    val tintColor = resolveTint(widget.glassColorTint, isSystemDark)

    val widgetHeightScale by animateFloatAsState(
        targetValue = when (widget.gridHeight) {
            1 -> 0.7f
            2 -> 1.0f
            3 -> 1.3f
            4 -> 1.6f
            5 -> 2.0f
            else -> 1.0f
        },
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 500f),
        label = "heightScale"
    )
    val resolveIsHalfWidth = widget.gridWidth <= 2

    var isDraggingThis by remember { mutableStateOf(false) }
    var isResizingThis by remember { mutableStateOf(false) }

    LaunchedEffect(isDraggingThis) {
        onDragStateChanged(isDraggingThis)
    }
    LaunchedEffect(isResizingThis) {
        onResizeStateChanged(isResizingThis)
    }

    val scaleFactor by animateFloatAsState(
        targetValue = when {
            isDraggingThis -> 1.05f
            isResizingThis -> 0.96f
            isSelected -> 1.03f
            isDesigning -> 0.98f
            else -> 1.0f
        },
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 600f),
        label = "scale"
    )

    val offsetAnimation by animateDpAsState(
        targetValue = when {
            isDraggingThis -> (-6).dp
            isSelected -> (-4).dp
            else -> 0.dp
        },
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 600f),
        label = "offset"
    )

    val borderModifier = Modifier.then(
        if (isDesigning) {
            val borderWidth = when {
                isDraggingThis -> 2.5.dp
                isResizingThis -> 2.5.dp
                isSelected -> 3.dp
                else -> 1.5.dp
            }
            val borderBrush = when {
                isDraggingThis -> Brush.linearGradient(listOf(EmeraldIncome, EmeraldIncome.copy(alpha = 0.4f), EmeraldIncome))
                isResizingThis -> Brush.linearGradient(listOf(ElectricNeeds, ElectricNeeds.copy(alpha = 0.3f), ElectricNeeds))
                isSelected -> Brush.linearGradient(listOf(ElectricNeeds, EmeraldIncome, ElectricNeeds))
                else -> Brush.sweepGradient(listOf(ElectricNeeds, ElectricNeeds.copy(alpha = 0.3f), ElectricNeeds))
            }
            Modifier
                .border(
                    width = borderWidth,
                    brush = borderBrush,
                    shape = RoundedCornerShape(26.dp)
                )
                .padding(4.dp)
        } else {
            Modifier
        }
    )

    val heightModifier = Modifier.fillMaxWidth()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(borderModifier)
            .offset(y = offsetAnimation)
            .graphicsLayer(
                scaleX = scaleFactor,
                scaleY = scaleFactor,
                shadowElevation = if (isDraggingThis) 32f else if (isSelected) 24f else if (isResizingThis) 12f else 0f
            )
            .alpha(if (isDesigning && !widget.isEnabled) 0.4f else 1.0f)
            .pointerInput(widget.id) {
                detectTapGestures(
                    onTap = {
                        if (isDesigning) {
                            onSelectWidget()
                        }
                    },
                    onLongPress = {
                        onLongPressWidget()
                    }
                )
            }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(modifier = heightModifier) {
                when (widget.id) {
                    "BALANCE" -> {
                        PrimaryBalanceCard(
                            balance = availableBalance,
                            onAddIncome = onAddIncome,
                            onLogExpense = onLogExpense,
                            onAIScan = onAIScan,
                            onAIVoiceClick = onAIVoiceClick,
                            containerColor = tintColor,
                            isHalfWidth = resolveIsHalfWidth,
                            heightScale = widgetHeightScale
                        )
                    }
                    "NET_WORTH" -> {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            BalanceSheetOverviewCard(
                                netWorth = netWorth,
                                investments = totalInvestmentBalance,
                                debt = totalDebtBalance,
                                containerColor = tintColor,
                                isHalfWidth = resolveIsHalfWidth,
                                heightScale = widgetHeightScale
                            )
                            
                            if (!resolveIsHalfWidth) {
                                val transactionsLocal by viewModel.transactions.collectAsState()
                                val subTxTransactions = remember(transactionsLocal) {
                                    transactionsLocal.filter { !it.subTransactionsStr.isNullOrBlank() && parseSubTransactions(it.subTransactionsStr).isNotEmpty() }.take(2)
                                }
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .liquidGlassCard(cornerRadius = 24.dp, containerColor = tintColor),
                                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                ) {
                                    Column(modifier = Modifier.fillMaxWidth().padding((16 * widgetHeightScale).toInt().coerceIn(8, 28).dp)) {
                                        Text(
                                            text = "RECENT ITEM BREAKDOWNS",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = TextSecondary
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        if (subTxTransactions.isEmpty()) {
                                            Text(
                                                text = "No sublogs logged inside expenses",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextSecondary
                                            )
                                        } else {
                                            subTxTransactions.forEach { tx ->
                                                Row(
                                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(tx.title, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                                                    Text(formatLKR(tx.amount), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = if (tx.isIncome) ElectricNeeds else DangerRed)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    "ALLOCATION" -> {
                        BudgetThreeColumnsAllocation(
                            summary = budgetSummary,
                            needsWarning = needsWarning,
                            wantsWarning = wantsWarning,
                            savingsWarning = savingsWarning,
                            selectedBucket = selectedBucketFilter,
                            onBucketClick = { bucket ->
                                onBucketFilterChange(if (selectedBucketFilter == bucket) null else bucket)
                            },
                            isHalfWidth = resolveIsHalfWidth,
                            heightScale = widgetHeightScale
                        )
                    }
                    "CHART" -> {
                        val ringSize = (if (resolveIsHalfWidth) 80 else (150 * widgetHeightScale).toInt()).coerceIn(60, 200).dp
                        val chartPadding = (if (resolveIsHalfWidth) 12 else (18 * widgetHeightScale).toInt()).coerceIn(8, 28).dp
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .liquidGlassCard(cornerRadius = 24.dp, containerColor = tintColor),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(chartPadding),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (resolveIsHalfWidth) "SECTOR LOAD" else "STATISTICAL SECTOR LOAD",
                                    style = if (resolveIsHalfWidth) MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                                    color = TextSecondary,
                                    modifier = Modifier.align(Alignment.Start)
                                )
                                Spacer(modifier = Modifier.height((if (resolveIsHalfWidth) 6 else (12 * widgetHeightScale).toInt()).coerceIn(4, 20).dp))
                                val needsProgress = if (needsLimit > 0) (budgetSummary.needsExpenses / needsLimit).toFloat() else 0f
                                val wantsProgress = if (wantsLimit > 0) (budgetSummary.wantsExpenses / wantsLimit).toFloat() else 0f
                                val savingsProgress = if (savingsLimit > 0) (budgetSummary.savingsExpenses / savingsLimit).toFloat() else 0f
                                JointBudgetConcentricRings(
                                    needsProgress = needsProgress,
                                    wantsProgress = wantsProgress,
                                    savingsProgress = savingsProgress,
                                    isNeedsWarning = needsWarning,
                                    isWantsWarning = wantsWarning,
                                    isSavingsWarning = savingsWarning,
                                    modifier = Modifier.size(ringSize)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                if (resolveIsHalfWidth) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(ElectricNeeds))
                                                Text("Needs", fontSize = 9.sp, color = TextSecondary)
                                            }
                                            Text("${(needsProgress * 100).toInt()}%", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                        }
                                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(WarmWants))
                                                Text("Wants", fontSize = 9.sp, color = TextSecondary)
                                            }
                                            Text("${(wantsProgress * 100).toInt()}%", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                        }
                                    }
                                } else {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(ElectricNeeds))
                                            Text("Needs", fontSize = 10.sp, color = TextSecondary)
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(WarmWants))
                                            Text("Wants", fontSize = 10.sp, color = TextSecondary)
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(SavingsIndigo))
                                            Text("Savings", fontSize = 10.sp, color = TextSecondary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    "AUDIT" -> {
                        val auditPadding = (if (resolveIsHalfWidth) 12 else (18 * widgetHeightScale).toInt()).coerceIn(8, 28).dp
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .liquidGlassCard(cornerRadius = 20.dp, containerColor = tintColor),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                        ) {
                            Column(
                                modifier = Modifier.padding(auditPadding),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (resolveIsHalfWidth) "📊 DOUBLE LEDGER" else "📊 COGNITIVE DOUBLE-ENTRY LEDGER",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                                        color = ElectricNeeds
                                    )
                                    if (!resolveIsHalfWidth) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(SettlementSuccess.copy(alpha = 0.15f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("DEBITS = CREDITS", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 8.sp), color = SettlementSuccess)
                                        }
                                    }
                                }
                                Text("A = L + E Overview", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold), color = TextPrimary)
                                
                                val calculatedEquity = (availableBalance + totalInvestmentBalance - totalDebtBalance)
                                if (resolveIsHalfWidth) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(SurfaceDarkSecondary.copy(alpha = 0.4f))
                                            .padding(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                            Text("Assets", fontSize = 10.sp, color = TextSecondary)
                                            Text(formatLKR(availableBalance + totalInvestmentBalance), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = ElectricNeeds)
                                        }
                                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                            Text("Debt", fontSize = 10.sp, color = TextSecondary)
                                            Text(formatLKR(totalDebtBalance), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = DangerRed)
                                        }
                                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                            Text("Equity", fontSize = 10.sp, color = TextSecondary)
                                            Text(formatLKR(calculatedEquity), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = SavingsIndigo)
                                        }
                                    }
                                } else {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(SurfaceDarkSecondary.copy(alpha = 0.4f))
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("Assets", fontSize = 9.sp, color = TextSecondary)
                                            Text(formatLKR(availableBalance + totalInvestmentBalance), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = ElectricNeeds, maxLines = 1)
                                        }
                                        Text("=", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("Debt", fontSize = 9.sp, color = TextSecondary)
                                            Text(formatLKR(totalDebtBalance), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = DangerRed, maxLines = 1)
                                        }
                                        Text("+", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = TextSecondary)
                                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("Equity", fontSize = 9.sp, color = TextSecondary)
                                            Text(formatLKR(calculatedEquity), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = SavingsIndigo, maxLines = 1)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    "TRANSACTIONS" -> {
                        val limitCount = if (resolveIsHalfWidth) 2 else 3
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (resolveIsHalfWidth) "History" else "Transaction History",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                                Text("${filteredTransactions.size} logs", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                            
                            if (filteredTransactions.isEmpty()) {
                                EmptyHistoryPlaceholder()
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    filteredTransactions.take(limitCount).forEach { transaction ->
                                        TransactionRowItem(
                                            transaction = transaction,
                                            onEdit = { onEditTransaction(transaction) },
                                            onDelete = { onDeleteTransaction(transaction) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            if (isDesigning) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                        .background(SurfaceDarkSecondary.copy(alpha = 0.90f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Top Row: Moves, Drag handle, Palette icon, Visibility toggler
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            // DRAG GRIP HANDLE FOR INTERACTIVE DRAG-TO-REORDER
                            val densityForDrag = LocalDensity.current
                            var accumulatedDragY by remember { mutableStateOf(0f) }
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(Color.White.copy(alpha = 0.08f), CircleShape)
                                    .pointerInput(widget.id) {
                                        detectDragGestures(
                                            onDragStart = {
                                                isDraggingThis = true
                                                accumulatedDragY = 0f
                                            },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                accumulatedDragY += dragAmount.y
                                                val dragYInDp = with(densityForDrag) { accumulatedDragY.toDp() }
                                                
                                                if (dragYInDp > 75.dp) {
                                                    onMoveDown()
                                                    accumulatedDragY = 0f
                                                } else if (dragYInDp < -75.dp) {
                                                    onMoveUp()
                                                    accumulatedDragY = 0f
                                                }
                                            },
                                            onDragEnd = {
                                                isDraggingThis = false
                                                accumulatedDragY = 0f
                                            },
                                            onDragCancel = {
                                                isDraggingThis = false
                                                accumulatedDragY = 0f
                                            }
                                        )
                                    }
                                    .wrapContentSize(Alignment.Center)
                            ) {
                                CustomDragHandle(tint = ElectricNeeds)
                            }

                            IconButton(
                                onClick = onMoveUp,
                                modifier = Modifier.size(28.dp).background(Color.White.copy(alpha = 0.08f), CircleShape)
                            ) {
                                Icon(Icons.Default.ArrowUpward, contentDescription = "Move Up", tint = TextPrimary, modifier = Modifier.size(15.dp))
                            }
                            IconButton(
                                onClick = onMoveDown,
                                modifier = Modifier.size(28.dp).background(Color.White.copy(alpha = 0.08f), CircleShape)
                            ) {
                                Icon(Icons.Default.ArrowDownward, contentDescription = "Move Down", tint = TextPrimary, modifier = Modifier.size(15.dp))
                            }
                            IconButton(
                                onClick = onCycleTint,
                                modifier = Modifier.size(28.dp).background(Color.White.copy(alpha = 0.08f), CircleShape)
                            ) {
                                Icon(
                                    Icons.Default.Palette,
                                    contentDescription = "Glass Hue",
                                    tint = when(widget.glassColorTint) {
                                        "Indigo" -> Color(0xFF3F51B5)
                                        "Emerald" -> Color(0xFF00C853)
                                        "Coral" -> Color(0xFFFF5722)
                                        "Cyan" -> Color(0xFF00E5FF)
                                        "SunsetGold" -> Color(0xFFFFB300)
                                        else -> TextPrimary
                                    },
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            Text(
                                text = widget.glassColorTint ?: "White",
                                fontSize = 9.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(
                            onClick = onToggleEnabled,
                            modifier = Modifier.size(28.dp).background(Color.White.copy(alpha = 0.08f), CircleShape)
                        ) {
                            Icon(
                                if (widget.isEnabled) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle Visibility",
                                tint = if (widget.isEnabled) EmeraldIncome else DangerRed,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
        }

        // MS Word Style Resize Overlay displaying the dynamic sizing real-time 
        if (isResizingThis) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color.Black.copy(alpha = 0.75f))
                    .wrapContentSize(Alignment.Center)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "RESIZING WIDGET",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.2.sp
                        ),
                        color = ElectricNeeds
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${widget.gridWidth}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "×",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                        Text(
                            text = "${widget.gridHeight}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                    Text(
                        text = "Columns × Rows",
                        fontSize = 10.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Modern Active Edit Mode indicators and Multiple Anchor Resize Handles
        if (isDesigning && isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-8).dp, y = 8.dp)
                    .background(ElectricNeeds, RoundedCornerShape(8.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(10.dp)
                    )
                    Text(
                        text = "EDIT RESIZE ACTIVE",
                        color = Color.Black,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            val density = LocalDensity.current

            // 1. Bottom-Right Interactive Resize anchor (Width & Height)
            var dragInitialWidthBR by remember { mutableStateOf(widget.gridWidth) }
            var dragInitialHeightBR by remember { mutableStateOf(widget.gridHeight) }
            var dragAccumulatedXBR by remember { mutableStateOf(0f) }
            var dragAccumulatedYBR by remember { mutableStateOf(0f) }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 6.dp, y = 6.dp)
                    .size(36.dp)
                    .pointerInput(widget.id + "_br_anchor") {
                        detectDragGestures(
                            onDragStart = {
                                dragInitialWidthBR = widget.gridWidth
                                dragInitialHeightBR = widget.gridHeight
                                dragAccumulatedXBR = 0f
                                dragAccumulatedYBR = 0f
                                isResizingThis = true
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                dragAccumulatedXBR += dragAmount.x
                                dragAccumulatedYBR += dragAmount.y

                                val dragXInDp = with(density) { dragAccumulatedXBR.toDp() }
                                val dragYInDp = with(density) { dragAccumulatedYBR.toDp() }

                                // Interactive Snapping Mechanism: Snaps to steps of 45dp
                                val widthStepChange = (dragXInDp.value / 45f).toInt()
                                val heightStepChange = (dragYInDp.value / 45f).toInt()

                                val newWidth = (dragInitialWidthBR + widthStepChange).coerceIn(1, 5)
                                val newHeight = (dragInitialHeightBR + heightStepChange).coerceIn(1, 4)

                                if (newWidth != widget.gridWidth || newHeight != widget.gridHeight) {
                                    onUpdateSize(newWidth, newHeight)
                                }
                            },
                            onDragEnd = { isResizingThis = false },
                            onDragCancel = { isResizingThis = false }
                        )
                    }
                    .wrapContentSize(Alignment.Center)
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(Color.White, CircleShape)
                        .border(2.5.dp, ElectricNeeds, CircleShape)
                )
            }

            // 2. Right Center (Center-End) Interactive Resize anchor (Width ONLY)
            var dragInitialWidthR by remember { mutableStateOf(widget.gridWidth) }
            var dragAccumulatedXR by remember { mutableStateOf(0f) }

            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = 6.dp, y = 0.dp)
                    .size(36.dp)
                    .pointerInput(widget.id + "_r_anchor") {
                        detectDragGestures(
                            onDragStart = {
                                dragInitialWidthR = widget.gridWidth
                                dragAccumulatedXR = 0f
                                isResizingThis = true
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                dragAccumulatedXR += dragAmount.x

                                val dragXInDp = with(density) { dragAccumulatedXR.toDp() }
                                val widthStepChange = (dragXInDp.value / 45f).toInt()

                                val newWidth = (dragInitialWidthR + widthStepChange).coerceIn(1, 5)

                                if (newWidth != widget.gridWidth) {
                                    onUpdateSize(newWidth, widget.gridHeight)
                                }
                            },
                            onDragEnd = { isResizingThis = false },
                            onDragCancel = { isResizingThis = false }
                        )
                    }
                    .wrapContentSize(Alignment.Center)
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(Color.White, CircleShape)
                        .border(2.5.dp, ElectricNeeds, CircleShape)
                )
            }

            // 3. Bottom Center Interactive Resize anchor (Height ONLY)
            var dragInitialHeightB by remember { mutableStateOf(widget.gridHeight) }
            var dragAccumulatedYB by remember { mutableStateOf(0f) }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(x = 0.dp, y = 6.dp)
                    .size(36.dp)
                    .pointerInput(widget.id + "_b_anchor") {
                        detectDragGestures(
                            onDragStart = {
                                dragInitialHeightB = widget.gridHeight
                                dragAccumulatedYB = 0f
                                isResizingThis = true
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                dragAccumulatedYB += dragAmount.y

                                val dragYInDp = with(density) { dragAccumulatedYB.toDp() }
                                val heightStepChange = (dragYInDp.value / 45f).toInt()

                                val newHeight = (dragInitialHeightB + heightStepChange).coerceIn(1, 4)

                                if (newHeight != widget.gridHeight) {
                                    onUpdateSize(widget.gridWidth, newHeight)
                                }
                            },
                            onDragEnd = { isResizingThis = false },
                            onDragCancel = { isResizingThis = false }
                        )
                    }
                    .wrapContentSize(Alignment.Center)
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(Color.White, CircleShape)
                        .border(2.5.dp, ElectricNeeds, CircleShape)
                )
            }

            // 4. Decorative anchors showing full selected guide boundaries
            val decorativeAlignments = listOf(
                Alignment.TopStart to Offset(-6f, -6f),
                Alignment.TopEnd to Offset(6f, -6f),
                Alignment.BottomStart to Offset(-6f, 6f),
                Alignment.TopCenter to Offset(0f, -6f),
                Alignment.CenterStart to Offset(-6f, 0f)
            )
            decorativeAlignments.forEach { (alignment, offset) ->
                Box(
                    modifier = Modifier
                        .align(alignment)
                        .offset(x = offset.x.dp, y = offset.y.dp)
                        .size(12.dp)
                        .background(Color.White, CircleShape)
                        .border(1.5.dp, ElectricNeeds.copy(alpha = 0.7f), CircleShape)
                )
            }
        }
    }
}

@Composable
fun CustomDragHandle(modifier: Modifier = Modifier, tint: Color = TextPrimary) {
    Column(
        modifier = modifier.size(24.dp).padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        repeat(3) {
            Box(
                modifier = Modifier
                    .width(14.dp)
                    .height(2.dp)
                    .background(tint.copy(alpha = 0.8f), RoundedCornerShape(1.dp))
            )
        }
    }
}

@Composable
fun AIVoiceInputDialog(
    viewModel: LankaBudgetViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var voiceInputText by remember { mutableStateOf("") }
    var isAnalyzing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Returned transaction state
    var transactionTitle by remember { mutableStateOf("") }
    var transactionAmount by remember { mutableStateOf("") }
    var isIncomeType by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("Groceries") }
    var selectedBucket by remember { mutableStateOf("NEEDS") }
    var parseCompleted by remember { mutableStateOf(false) }

    // Speech-to-text safety checks (Disable system prompt on emulators to prevent asynchronous ActivityNotFound / engine crashes)
    val hasSpeechRecognition = remember {
        try {
            val isEmulator = android.os.Build.FINGERPRINT.startsWith("generic") ||
                    android.os.Build.FINGERPRINT.startsWith("unknown") ||
                    android.os.Build.MODEL.contains("google_sdk") ||
                    android.os.Build.MODEL.contains("Emulator") ||
                    android.os.Build.MODEL.contains("Android SDK built for x86") ||
                    android.os.Build.MANUFACTURER.contains("Genymotion") ||
                    android.os.Build.HARDWARE.contains("goldfish") ||
                    android.os.Build.HARDWARE.contains("ranchu") ||
                    android.os.Build.HARDWARE.contains("vbox86") ||
                    android.os.Build.PRODUCT.contains("sdk") ||
                    android.os.Build.PRODUCT.contains("google_sdk") ||
                    android.os.Build.PRODUCT.contains("sdk_x86") ||
                    android.os.Build.PRODUCT.contains("vbox86p")

            if (isEmulator) {
                false
            } else {
                val pm = context.packageManager
                val intent = android.content.Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                val isServiceAvailable = android.speech.SpeechRecognizer.isRecognitionAvailable(context)
                isServiceAvailable && intent.resolveActivity(pm) != null
            }
        } catch (e: java.lang.Exception) {
            false
        }
    }

    // Speech-to-text launcher using system ACTION_RECOGNIZE_SPEECH
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val data = result.data
            val spokenText = data?.getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS)?.firstOrNull() ?: ""
            if (spokenText.isNotBlank()) {
                voiceInputText = spokenText
            }
        }
    }

    fun startSpeechToText() {
        if (!hasSpeechRecognition) {
            android.widget.Toast.makeText(context, "Voice speech recognition is not supported on this device/emulator. Please enter your command by typing below!", android.widget.Toast.LENGTH_LONG).show()
            return
        }
        try {
            val intent = android.content.Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL, android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE, java.util.Locale.getDefault().toString())
                putExtra(android.speech.RecognizerIntent.EXTRA_PROMPT, "Describe transaction (e.g., Spent 5000 on Keells groceries)...")
            }
            speechRecognizerLauncher.launch(intent)
        } catch (e: Exception) {
            android.widget.Toast.makeText(context, "Speech recognition is not supported on this device/emulator: ${e.localizedMessage}", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .liquidGlassCard(cornerRadius = 24.dp)
                .testTag("ai_voice_assistant_dialog_card"),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "🎙️ Real-time Voice AI",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = TextPrimary
                        )
                        Text(
                            text = "Powered by Google Gemini Assistant",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.clip(CircleShape).background(Color.White.copy(alpha = 0.05f))
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Close", tint = DangerRed)
                    }
                }

                HorizontalDivider(color = GridDividerLabel.copy(alpha = 0.5f))

                // Error Message if present
                errorMessage?.let { msg ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DangerRed.copy(alpha = 0.15f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = "Error", tint = DangerRed)
                            Text(text = msg, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                        }
                    }
                }

                // Speech Input Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.04f))
                        .border(1.dp, GridDividerLabel.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Tap mic to speak or enter command description:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextSecondary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (hasSpeechRecognition) {
                                // Circular pulsing voice trigger button
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF673AB7).copy(alpha = 0.15f))
                                        .clickable { startSpeechToText() }
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF673AB7)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Mic,
                                            contentDescription = "Tap to speak",
                                            tint = Color.White,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                            } else {
                                // Informative fallback layout when system speech recognizer is not available
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, Color(0xFF673AB7).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "🎤 Speech recognition service not available",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "Type or choose a sample template command below. Our secure Gemini AI can parse natural phrasing and Sri Lankan currency terms (LKR/Rupees) flawlessly!",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextSecondary,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }

                        // Manual Entry / Transcription Box
                        OutlinedTextField(
                            value = voiceInputText,
                            onValueChange = { voiceInputText = it },
                            placeholder = { Text("E.g., I bought dinner at Cafe for 1200 rupees") },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
                            singleLine = false,
                            maxLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF673AB7),
                                unfocusedBorderColor = GridDividerLabel.copy(alpha = 0.6f),
                                focusedLabelColor = Color(0xFF673AB7),
                                cursorColor = Color(0xFF673AB7)
                            )
                        )

                        // Quick try templates
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("Quick Try Samples:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = TextSecondary)
                            
                            val samples = listOf(
                                "Spent 4500 LKR for groceries at Keells Super",
                                "Received salary deposit of 145000 rupees",
                                "Paid 12000 electricity utility rent bill"
                            )

                            samples.forEach { sampleText ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White.copy(alpha = 0.05f))
                                        .clickable { voiceInputText = sampleText }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = sampleText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }

                        // Analyzer Trigger button
                        Button(
                            onClick = {
                                if (voiceInputText.isBlank()) {
                                    errorMessage = "Please enter some spoken command or type a description first."
                                    return@Button
                                }
                                isAnalyzing = true
                                errorMessage = null
                                viewModel.parseVoiceCommandWithGemini(
                                    commandText = voiceInputText,
                                    onSuccess = { result ->
                                        transactionTitle = result.title
                                        transactionAmount = result.amount.toString()
                                        isIncomeType = result.isIncome
                                        selectedCategory = result.category
                                        selectedBucket = result.bucket
                                        isAnalyzing = false
                                        parseCompleted = true
                                    },
                                    onFailure = { err ->
                                        errorMessage = err
                                        isAnalyzing = false
                                    }
                                )
                            },
                            enabled = !isAnalyzing,
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF673AB7)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isAnalyzing) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Analyzing voice command...", fontWeight = FontWeight.Bold)
                            } else {
                                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Analyze Input with Gemini", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Extracted Parse result card (Editable form to adjust and approve transactions)
                if (parseCompleted) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .liquidGlassCard(cornerRadius = 16.dp, containerColor = Color.White.copy(alpha = 0.05f)),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "✨ AI Extracted Details",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = TextPrimary
                            )

                            HorizontalDivider(color = GridDividerLabel.copy(alpha = 0.3f))

                            // Income vs Expense Segment
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.05f))
                                    .padding(4.dp)
                            ) {
                                Button(
                                    onClick = {
                                        isIncomeType = false
                                        if (selectedBucket == "INCOME") selectedBucket = "NEEDS"
                                    },
                                    modifier = Modifier.weight(1f).height(38.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (!isIncomeType) DangerRed else Color.Transparent,
                                        contentColor = if (!isIncomeType) Color.White else TextSecondary
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Expense", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                }

                                Button(
                                    onClick = {
                                        isIncomeType = true
                                        selectedBucket = "INCOME"
                                    },
                                    modifier = Modifier.weight(1f).height(38.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isIncomeType) EmeraldIncome else Color.Transparent,
                                        contentColor = if (isIncomeType) Color.White else TextSecondary
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Income", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                }
                            }

                            // Editable transaction title
                            OutlinedTextField(
                                value = transactionTitle,
                                onValueChange = { transactionTitle = it },
                                label = { Text("Title / Payee") },
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF673AB7),
                                    unfocusedBorderColor = GridDividerLabel.copy(alpha = 0.4f),
                                    focusedLabelColor = Color(0xFF673AB7),
                                    cursorColor = Color(0xFF673AB7)
                                )
                            )

                            // Editable transaction amount
                            OutlinedTextField(
                                value = transactionAmount,
                                onValueChange = { transactionAmount = it },
                                label = { Text("Amount (LKR)") },
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF673AB7),
                                    unfocusedBorderColor = GridDividerLabel.copy(alpha = 0.4f),
                                    focusedLabelColor = Color(0xFF673AB7),
                                    cursorColor = Color(0xFF673AB7)
                                )
                            )

                            // Editable category
                            Text(text = "Category", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = TextSecondary)
                            val budgetCategories = if (isIncomeType) {
                                listOf("Salary", "Freelance", "Investment Income", "Gifts", "Other")
                            } else {
                                listOf("Groceries", "Utilities", "Bills & Rent", "Transport", "Mandatory Debt", "Dine Out", "Cinema & Movies", "Hobbies", "Gifts", "Apparel", "Other")
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                budgetCategories.forEach { cat ->
                                    val isSelected = (selectedCategory == cat)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) Color(0xFF673AB7).copy(alpha = 0.3f) else Color.White.copy(alpha = 0.05f))
                                            .border(1.dp, if (isSelected) Color(0xFF673AB7) else Color.Transparent, RoundedCornerShape(8.dp))
                                            .clickable { selectedCategory = cat }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(text = cat, style = MaterialTheme.typography.bodySmall, color = if (isSelected) Color.White else TextPrimary)
                                    }
                                }
                            }

                            // Editable 50/30/20 standard budget bucket allocation
                            if (!isIncomeType) {
                                Text(text = "Budget Allocation Bucket", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = TextSecondary)
                                val bucketsList = listOf("NEEDS", "WANTS", "SAVINGS")
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    bucketsList.forEach { bcd ->
                                        val isSelected = (selectedBucket == bcd)
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSelected) Color(0xFF673AB7).copy(alpha = 0.3f) else Color.White.copy(alpha = 0.05f))
                                                .border(1.dp, if (isSelected) Color(0xFF673AB7) else Color.Transparent, RoundedCornerShape(8.dp))
                                                .clickable { selectedBucket = bcd }
                                                .padding(vertical = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = bcd, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = if (isSelected) Color.White else TextPrimary)
                                        }
                                    }
                                }
                            }

                            // Final confirmation & logging submit button
                            Button(
                                onClick = {
                                    val amountDouble = transactionAmount.toDoubleOrNull() ?: 0.0
                                    if (transactionTitle.isNotBlank() && amountDouble > 0) {
                                        if (isIncomeType) {
                                            viewModel.addIncome(transactionTitle, amountDouble, selectedCategory)
                                        } else {
                                            viewModel.addExpense(transactionTitle, amountDouble, selectedBucket, selectedCategory)
                                        }
                                        onDismiss()
                                    } else {
                                        android.widget.Toast.makeText(context, "Please enter a valid title and amount", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = if (isIncomeType) EmeraldIncome else DangerRed),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Log Transaction to Ledger", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}


