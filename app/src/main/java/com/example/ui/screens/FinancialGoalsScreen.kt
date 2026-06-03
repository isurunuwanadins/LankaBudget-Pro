package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Goal
import com.example.ui.components.liquidGlassCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.LankaBudgetViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun FinancialGoalsScreen(
    viewModel: LankaBudgetViewModel,
    modifier: Modifier = Modifier
) {
    val goals by viewModel.goals.collectAsState()
    val goalContributions by viewModel.goalContributions.collectAsState()

    var showAddGoalDialog by remember { mutableStateOf(false) }
    var showAddContributionDialog by remember { mutableStateOf<Goal?>(null) }

    val sdfDate = remember { SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 70.dp) // Leave clean padding for the floating navigation pill bar
    ) {
        if (goals.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .liquidGlassCard(cornerRadius = 50.dp, containerColor = Color.White.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrackChanges,
                            contentDescription = "Target icon",
                            tint = SavingsIndigo,
                            modifier = Modifier.size(52.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Build Your Financial Dreams",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Establish long-term savings goals and automatically path your 'SAVINGS' investments to fuel them of your own budget.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.widthIn(max = 280.dp)
                    )
                    Spacer(modifier = Modifier.height(28.dp))
                    Button(
                        onClick = { showAddGoalDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SavingsIndigo),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.testTag("create_first_goal_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Goal Icon")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Create Your First Goal", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                contentPadding = PaddingValues(top = 10.dp, bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(goals, key = { it.id }) { goal ->
                    // Calculate totals
                    val contributionsForThisGoal = remember(goalContributions, goal.id) {
                        goalContributions.filter { it.goalId == goal.id }.sumOf { it.amount }
                    }
                    val progressFraction = if (goal.targetAmount > 0) {
                        (contributionsForThisGoal / goal.targetAmount).coerceIn(0.0, 1.0).toFloat()
                    } else {
                        0f
                    }

                    val percentText = "${(progressFraction * 100).toInt()}%"
                    val daysLeft = ((goal.targetDate - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).coerceAtLeast(0)

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .liquidGlassCard(cornerRadius = 24.dp, containerColor = Color.White.copy(alpha = 0.52f))
                            .padding(18.dp)
                            .testTag("goal_item_${goal.id}")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = goal.title.uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    ),
                                    color = SavingsIndigo
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Target Date: ${sdfDate.format(Date(goal.targetDate))}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                            IconButton(
                                onClick = { viewModel.deleteGoal(goal) },
                                modifier = Modifier.testTag("delete_goal_${goal.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete Goal",
                                    tint = DangerRed.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Visual progress tracking with visual numbers
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    text = "Balance: LKR ${String.format("%,.2f", contributionsForThisGoal)}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Target: LKR ${String.format("%,.2f", goal.targetAmount)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SavingsIndigo.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = percentText,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = SavingsIndigo
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Beautiful Custom Progress Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.28f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(progressFraction)
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(SavingsIndigo, ElectricNeeds)
                                        )
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (daysLeft > 0) "$daysLeft Days Left" else "DUE TODAY / REACHED",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = if (daysLeft > 10) TextSecondary else DangerRed
                            )

                            Button(
                                onClick = { showAddContributionDialog = goal },
                                colors = ButtonDefaults.buttonColors(containerColor = SavingsIndigo),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .height(34.dp)
                                    .testTag("fund_goal_${goal.id}")
                            ) {
                                Icon(
                                    Icons.Default.Savings,
                                    contentDescription = "Fund",
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add Funds", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }
            }

            // Floated Action Button to add Goals easily on top right or floating
            FloatingActionButton(
                onClick = { showAddGoalDialog = true },
                containerColor = SavingsIndigo,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 14.dp)
                    .testTag("add_goal_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Goal Fab")
            }
        }

        // Dialog for entering goals
        if (showAddGoalDialog) {
            AddGoalDialog(
                onDismiss = { showAddGoalDialog = false },
                onAdd = { title, amount, monthsLeft ->
                    // Convert months left to exact date
                    val cal = Calendar.getInstance()
                    cal.add(Calendar.MONTH, monthsLeft)
                    viewModel.addGoal(
                        title = title,
                        targetAmount = amount,
                        targetDate = cal.timeInMillis
                    )
                    showAddGoalDialog = false
                }
            )
        }

        // Dialog for logging saving contributions
        showAddContributionDialog?.let { goal ->
            LogContributionDialog(
                goalTitle = goal.title,
                onDismiss = { showAddContributionDialog = null },
                onAdd = { amount ->
                    viewModel.addGoalContribution(
                        goalId = goal.id,
                        amount = amount,
                        title = goal.title
                    )
                    showAddContributionDialog = null
                }
            )
        }
    }
}

@Composable
fun AddGoalDialog(
    onDismiss: () -> Unit,
    onAdd: (title: String, amount: Double, months: Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var monthsToGoal by remember { mutableFloatStateOf(6f) } // slider defaults to 6 months

    val calculatedDate = remember(monthsToGoal) {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, monthsToGoal.toInt())
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        sdf.format(cal.time)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Establish Financial Goal", fontWeight = FontWeight.Bold, color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Goal Title (e.g., Laptop, Holiday)") },
                    textStyle = LocalTextStyle.current.copy(color = TextPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_goal_title_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Target Amount (LKR)") },
                    textStyle = LocalTextStyle.current.copy(color = TextPrimary),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_goal_amount_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Timeline: ${monthsToGoal.toInt()} months (${calculatedDate})",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = SavingsIndigo
                )

                Slider(
                    value = monthsToGoal,
                    onValueChange = { monthsToGoal = it },
                    valueRange = 1f..60f,
                    steps = 59,
                    colors = SliderDefaults.colors(
                        thumbColor = SavingsIndigo,
                        activeTrackColor = SavingsIndigo,
                        inactiveTrackColor = SavingsIndigo.copy(alpha = 0.24f)
                    ),
                    modifier = Modifier.testTag("add_goal_duration_slider")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                    if (title.isNotBlank() && amt > 0.0) {
                        onAdd(title.trim(), amt, monthsToGoal.toInt())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SavingsIndigo),
                enabled = title.isNotBlank() && amountStr.toDoubleOrNull() != null,
                modifier = Modifier.testTag("confirm_create_goal_btn")
            ) {
                Text("Launch Goal")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White
    )
}

@Composable
fun LogContributionDialog(
    goalTitle: String,
    onDismiss: () -> Unit,
    onAdd: (amount: Double) -> Unit
) {
    var amountStr by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Savings Contribution", fontWeight = FontWeight.Bold, color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Add funds towards '$goalTitle' from your budget 'SAVINGS' investments bucket.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Contribution Amount (LKR)") },
                    textStyle = LocalTextStyle.current.copy(color = TextPrimary),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("funding_amount_input"),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                    if (amt > 0.0) {
                        onAdd(amt)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SavingsIndigo),
                enabled = amountStr.toDoubleOrNull() != null,
                modifier = Modifier.testTag("confirm_add_funds_btn")
            ) {
                Text("Commit Savings")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White
    )
}
