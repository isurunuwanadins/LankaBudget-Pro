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
import androidx.compose.material.icons.filled.*
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
import com.example.data.model.RecurringTransaction
import com.example.ui.theme.*
import com.example.ui.components.liquidGlassCard
import com.example.ui.viewmodel.LankaBudgetViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatTimestamp(timestamp: Long): String {
    if (timestamp <= 0L) return "Never"
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringSchedulesScreen(
    viewModel: LankaBudgetViewModel,
    modifier: Modifier = Modifier
) {
    val recurringSchedules by viewModel.recurringTransactions.collectAsState()
    var showAddScheduleDialog by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }
    var isSyncing by remember { mutableStateOf(false) }
    var editingSchedule by remember { mutableStateOf<RecurringTransaction?>(null) }

    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Screen Header (floating liquid-glass panel)
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
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = ElectricNeeds,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Auto-Schedules",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.5).sp,
                                    fontSize = 18.sp
                                ),
                                color = TextPrimary
                            )
                            Text(
                                text = "Periodic ledger automation rules",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Button(
                            onClick = { showAddScheduleDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricNeeds),
                            shape = CircleShape,
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("add_schedule_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Rules", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Central Schedules Ledger can scroll safe above the floating bottom bar
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 110.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Main List Card container
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .liquidGlassCard(cornerRadius = 28.dp, containerColor = Color.White.copy(alpha = 0.52f)),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "AUTOMATION CRON STATES",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp
                                ),
                                color = TextSecondary
                            )
                            Text(
                                text = "${recurringSchedules.size} rules active",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = ElectricNeeds
                            )
                        }

                        Divider(color = GridDividerLabel, thickness = 1.dp)

                        if (recurringSchedules.isEmpty()) {
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
                                        imageVector = Icons.Default.Autorenew,
                                        contentDescription = null,
                                        tint = TextSecondary.copy(alpha = 0.6f),
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "No scheduled transactions",
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Add automated recurring expenses (like monthly rent, bills) or salaries to auto-generate ledger transactions.",
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
                                items(recurringSchedules, key = { it.id }) { schedule ->
                                    RecurringScheduleRow(
                                        schedule = schedule,
                                        onEdit = { editingSchedule = schedule },
                                        onDelete = { viewModel.deleteRecurringTransaction(schedule) }
                                    )
                                    Divider(color = SurfaceDarkSecondary, thickness = 1.dp)
                                }
                            }
                        }

                        // Sync catch-up bottom bar
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
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "CATCH-UP GENERATOR Engine",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        ),
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = "Auto-evaluates schedules up to today.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }

                                Button(
                                    onClick = {
                                        viewModel.triggerCatchUp()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ElectricNeeds),
                                    shape = CircleShape,
                                    modifier = Modifier
                                        .height(40.dp)
                                        .testTag("trigger_cron_sync_button")
                                ) {
                                    Icon(imageVector = Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Process Cron", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Recurring Dialog Popup
        if (showAddScheduleDialog) {
            AddRecurringDialog(
                onDismiss = { showAddScheduleDialog = false },
                onAdd = { title, amount, isIncome, bucket, category, period ->
                    if (isIncome) {
                        viewModel.addRecurringIncome(title, amount, category, period, System.currentTimeMillis())
                    } else {
                        viewModel.addRecurringExpense(title, amount, bucket, category, period, System.currentTimeMillis())
                    }
                    showAddScheduleDialog = false
                }
            )
        }

        editingSchedule?.let { schedule ->
            AddRecurringDialog(
                onDismiss = { editingSchedule = null },
                editingSchedule = schedule,
                onAdd = { title, amount, isIncome, bucket, category, period ->
                    viewModel.updateRecurringTransaction(
                        schedule.copy(
                            title = title,
                            amount = amount,
                            isIncome = isIncome,
                            bucket = bucket,
                            category = category,
                            recurrencePeriod = period
                        )
                    )
                    editingSchedule = null
                }
            )
        }

        // Confirmation Alert
        if (showClearConfirm) {
            AlertDialog(
                onDismissRequest = { showClearConfirm = false },
                title = { Text("Clear All Schedules?", color = TextPrimary) },
                text = { Text("This will permanently clear all recorded automated schedules. Transactions already entered into your ledger will remain.", color = TextSecondary) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.clearAllRecurringTransactions()
                            showClearConfirm = false
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = DangerRed)
                    ) {
                        Text("Reset Schedules", fontWeight = FontWeight.Bold)
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
fun RecurringScheduleRow(
    schedule: RecurringTransaction,
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
        val pillBg = if (schedule.isIncome) ElectricNeedsBg
        else when (schedule.bucket) {
            "NEEDS" -> ElectricNeedsBg
            "WANTS" -> WarmWantsBg
            else -> SavingsIndigoBg
        }
        
        val pillColor = if (schedule.isIncome) ElectricNeeds
        else when (schedule.bucket) {
            "NEEDS" -> ElectricNeeds
            "WANTS" -> WarmWants
            else -> SavingsIndigo
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // Rounded Icon identifier
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(pillBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when(schedule.recurrencePeriod) {
                        "DAILY" -> "D"
                        "WEEKLY" -> "W"
                        "MONTHLY" -> "M"
                        "YEARLY" -> "Y"
                        else -> "R"
                    },
                    fontWeight = FontWeight.Bold,
                    color = pillColor,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = schedule.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = schedule.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Box(
                        modifier = Modifier
                            .size(3.dp)
                            .clip(CircleShape)
                            .background(TextSecondary)
                    )
                    Text(
                        text = schedule.recurrencePeriod,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = pillColor
                    )
                }
                Text(
                    text = "Last Processed: ${formatTimestamp(schedule.lastGeneratedDate)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (schedule.isIncome) "+${formatLKR(schedule.amount)}" else "-${formatLKR(schedule.amount)}",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = if (schedule.isIncome) ElectricNeeds else DangerRed
                )
                Text(
                    text = if (schedule.isIncome) "Income" else "Expense",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove Schedule Rule",
                    tint = DangerRed.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun AddRecurringDialog(
    onDismiss: () -> Unit,
    onAdd: (title: String, amount: Double, isIncome: Boolean, bucket: String, category: String, period: String) -> Unit,
    editingSchedule: RecurringTransaction? = null
) {
    var title by remember { mutableStateOf(editingSchedule?.title ?: "") }
    var amount by remember { mutableStateOf(editingSchedule?.amount?.toString() ?: "") }
    var isIncome by remember { mutableStateOf(editingSchedule?.isIncome ?: false) }
    var bucket by remember { mutableStateOf(editingSchedule?.bucket ?: "NEEDS") }
    var recurrencePeriod by remember { mutableStateOf(editingSchedule?.recurrencePeriod ?: "MONTHLY") }

    val defaultCategory = if (isIncome) "Salary" else "Bills & Rent"
    var category by remember { mutableStateOf(editingSchedule?.category ?: defaultCategory) }

    val categories = if (isIncome) {
        listOf("Salary", "Freelance", "Bonus", "Investment", "Gift", "Other")
    } else {
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
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (editingSchedule != null) "Edit Automation Rule" else "Add Automation Rule",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        letterSpacing = (-0.5).sp
                    ),
                    color = ElectricNeeds
                )

                // Select Income vs Expense Tab-chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceDarkSecondary)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (!isIncome) DangerRed.copy(alpha = 0.15f) else Color.Transparent)
                            .border(1.dp, if (!isIncome) DangerRed else Color.Transparent, RoundedCornerShape(6.dp))
                            .clickable {
                                isIncome = false
                                bucket = "NEEDS"
                                category = "Bills & Rent"
                            }
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Expense Rule", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (!isIncome) DangerRed else TextPrimary)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isIncome) ElectricNeedsBg else Color.Transparent)
                            .border(1.dp, if (isIncome) ElectricNeeds else Color.Transparent, RoundedCornerShape(6.dp))
                            .clickable {
                                isIncome = true
                                bucket = "INCOME"
                                category = "Salary"
                            }
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Income Rule", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (isIncome) ElectricNeeds else TextPrimary)
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title / Item Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = ElectricNeeds,
                        unfocusedBorderColor = GridDividerLabel
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("schedule_title_input")
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount (LKR)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = ElectricNeeds,
                        unfocusedBorderColor = GridDividerLabel
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("schedule_amount_input")
                )

                // Select Recurrence Period Selection Row
                Column {
                    Text("Recurrence Cycle", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("DAILY", "WEEKLY", "MONTHLY", "YEARLY").forEach { cycle ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (recurrencePeriod == cycle) ElectricNeedsBg else SurfaceDarkSecondary)
                                    .border(
                                        1.dp,
                                        if (recurrencePeriod == cycle) ElectricNeeds else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { recurrencePeriod = cycle }
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cycle,
                                    fontSize = 11.sp,
                                    color = if (recurrencePeriod == cycle) ElectricNeeds else TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Select 50/30/20 Bucket (Only if Expense)
                if (!isIncome) {
                    Column {
                        Text("Select 50/30/20 Bucket", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
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
                                        text = bkt,
                                        fontSize = 11.sp,
                                        color = if (bucket == bkt) color else TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Category selection list
                Column {
                    Text("Automation Category", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
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
                                onAdd(title, amt, isIncome, bucket, category, recurrencePeriod)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricNeeds),
                        modifier = Modifier.testTag("submit_schedule_button")
                    ) {
                        Text(if (editingSchedule != null) "Save" else "Create Rule", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
