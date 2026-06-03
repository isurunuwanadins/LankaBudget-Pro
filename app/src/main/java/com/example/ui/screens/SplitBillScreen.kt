package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
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
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SplitBillEntity
import com.example.ui.components.liquidGlassCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.LankaBudgetViewModel

// Data structures for in-memory splits creation
data class CashSplitLine(
    val name: String,
    val amount: Double,
    val bucket: String, // NEEDS, WANTS, SAVINGS
    val category: String
)

data class BillSplitMember(
    val name: String,
    val share: Double,
    val isPaid: Boolean
)

@Composable
fun SplitBillScreen(
    viewModel: LankaBudgetViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Shared Bills, 1: Category Cash Division
    val splitBills by viewModel.splitBills.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Screen Section Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .liquidGlassCard(cornerRadius = 14.dp, containerColor = Color.White.copy(alpha = 0.35f))
                    .padding(4.dp)
            ) {
                listOf("Group Bill Splitter", "Multi-Category Cash Splitter").forEachIndexed { index, label ->
                    val isSelected = selectedTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) ElectricNeeds.copy(alpha = 0.15f) else Color.Transparent)
                            .clickable { selectedTab = index }
                            .padding(vertical = 12.dp),
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

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (selectedTab == 0) {
                    GroupBillSplitterSection(viewModel, splitBills)
                } else {
                    CashDivisionSection(viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GroupBillSplitterSection(
    viewModel: LankaBudgetViewModel,
    splitBills: List<SplitBillEntity>
) {
    val context = LocalContext.current
    
    // Group splitting states
    var billTitle by remember { mutableStateOf("") }
    var billTotalStr by remember { mutableStateOf("") }
    var currentMemberName by remember { mutableStateOf("") }
    val membersList = remember { mutableStateListOf<String>() }
    var isMeIncluded by remember { mutableStateOf(true) }
    var shouldLogMyShare by remember { mutableStateOf(true) }
    var selectedCategory by remember { mutableStateOf("Gifts & Celebrations") }
    var selectedBucket by remember { mutableStateOf("WANTS") }
    var createDebtsForOthers by remember { mutableStateOf(true) }

    val categories = listOf("Gifts & Celebrations", "Food & Snacks", "Entertainment", "Utility", "Transport", "Miscellaneous")
    val buckets = listOf("NEEDS", "WANTS", "SAVINGS")

    // Calculations
    val totalAmount = billTotalStr.toDoubleOrNull() ?: 0.0
    val totalPeopleCount = membersList.size + (if (isMeIncluded) 1 else 0)
    val shareAmount = if (totalPeopleCount > 0) totalAmount / totalPeopleCount else 0.0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Form Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlassCard(cornerRadius = 20.dp, containerColor = Color.White.copy(alpha = 0.5f))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "NEW GROUP BILL SPLIT",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = ElectricNeeds
                    )

                    OutlinedTextField(
                        value = billTitle,
                        onValueChange = { billTitle = it },
                        label = { Text("Bill Description / Event Name") },
                        placeholder = { Text("e.g. Office Friend's Birthday Cake") },
                        modifier = Modifier.fillMaxWidth().testTag("bill_title_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = billTotalStr,
                        onValueChange = { billTotalStr = it },
                        label = { Text("Total Bill Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        placeholder = { Text("e.g. 4000") },
                        modifier = Modifier.fillMaxWidth().testTag("bill_total_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Adding Members Section
                    Text(
                        text = "Split Participants:",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = currentMemberName,
                            onValueChange = { currentMemberName = it },
                            label = { Text("Add Friend Name") },
                            placeholder = { Text("e.g. Alice") },
                            modifier = Modifier.weight(1f).testTag("friend_name_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        Button(
                            onClick = {
                                if (currentMemberName.trim().isNotBlank()) {
                                    val name = currentMemberName.trim()
                                    if (!membersList.contains(name)) {
                                        membersList.add(name)
                                        currentMemberName = ""
                                    } else {
                                        Toast.makeText(context, "Friend already in split", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricNeeds),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Friend")
                        }
                    }

                    // Render List of Added Participants
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White.copy(alpha = 0.4f))
                                .clickable { isMeIncluded = !isMeIncluded }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Checkbox(
                                checked = isMeIncluded,
                                onCheckedChange = { isMeIncluded = it },
                                modifier = Modifier.size(24.dp)
                            )
                            Text("Me (My Share)", fontSize = 12.sp, color = TextPrimary)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "(${membersList.size} friends added)",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    if (membersList.isNotEmpty()) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            membersList.forEach { member ->
                                Row(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(HeaderPillBg)
                                        .padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(member, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove",
                                        tint = DangerRed,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable { membersList.remove(member) }
                                    )
                                }
                            }
                        }
                    }

                    if (totalPeopleCount > 0 && totalAmount > 0) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(ElectricNeeds.copy(alpha = 0.08f))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "Split share is: ${formatLKR(shareAmount)} each (${totalPeopleCount} total shares)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = ElectricNeeds
                            )
                        }
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.3f))

                    // Log options
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = shouldLogMyShare,
                            onCheckedChange = { shouldLogMyShare = it }
                        )
                        Column {
                            Text("Log My Share to Expenses Ledger", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("Records your individual cost immediately", fontSize = 11.sp, color = TextSecondary)
                        }
                    }

                    if (shouldLogMyShare) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Category Select
                            var catExpanded by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.weight(1.5f)) {
                                OutlinedButton(
                                    onClick = { catExpanded = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                                ) {
                                    Text(selectedCategory, maxLines = 1, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                                DropdownMenu(expanded = catExpanded, onDismissRequest = { catExpanded = false }) {
                                    categories.forEach { c ->
                                        DropdownMenuItem(
                                            text = { Text(c, fontSize = 12.sp) },
                                            onClick = {
                                                selectedCategory = c
                                                catExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Bucket Selector Click
                            var buckExpanded by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedButton(
                                    onClick = { buckExpanded = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                                ) {
                                    Text(selectedBucket, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                                DropdownMenu(expanded = buckExpanded, onDismissRequest = { buckExpanded = false }) {
                                    buckets.forEach { b ->
                                        DropdownMenuItem(
                                            text = { Text(b, fontSize = 12.sp) },
                                            onClick = {
                                                selectedBucket = b
                                                buckExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = createDebtsForOthers,
                            onCheckedChange = { createDebtsForOthers = it }
                        )
                        Column {
                            Text("Log Unpaid Parts to Peer Debts Ledger", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("Alice, Bob, etc. will show in Debts directory as receivables", fontSize = 11.sp, color = TextSecondary)
                        }
                    }

                    Button(
                        onClick = {
                            if (billTitle.trim().isBlank()) {
                                Toast.makeText(context, "Please enter split bill description", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (totalAmount <= 0) {
                                Toast.makeText(context, "Please enter a valid bill amount", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (totalPeopleCount <= 1) {
                                Toast.makeText(context, "Add friends or include yourself to split the bill", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            // 1. Format membersJson string
                            // Alice|1000.0|unpaid,Bob|1000.0|unpaid
                            val membersJson = membersList.joinToString(",") { "$it|$shareAmount|unpaid" }
                            val myShare = if (isMeIncluded) shareAmount else 0.0

                            // 2. Add Split Bill
                            viewModel.addSplitBill(
                                description = billTitle.trim(),
                                totalAmount = totalAmount,
                                myShare = myShare,
                                payerName = "Me",
                                membersJson = membersJson
                            )

                            // 3. Log My Share in Expenses if selected
                            if (shouldLogMyShare && myShare > 0) {
                                viewModel.addExpense(
                                    title = "${billTitle.trim()} (My Share)",
                                    amount = myShare,
                                    bucket = selectedBucket,
                                    category = selectedCategory
                                )
                            }

                            // 4. Log as Receivables (Loans) if selected
                            if (createDebtsForOthers && membersList.isNotEmpty()) {
                                membersList.forEach { member ->
                                    viewModel.addLoan(
                                        lenderName = "$member (for ${billTitle.trim()})",
                                        borrowedAmount = shareAmount
                                    )
                                }
                            }

                            Toast.makeText(context, "Group Bill Split Logged!", Toast.LENGTH_LONG).show()

                            // Reset Form
                            billTitle = ""
                            billTotalStr = ""
                            membersList.clear()
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("log_bill_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricNeeds),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Log Bill & Divide Costs", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Active Splits Header
        item {
            Text(
                text = "BILL COLLECTION DIRECTORY",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                modifier = Modifier.padding(top = 8.dp),
                color = ElectricNeeds
            )
        }

        if (splitBills.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.Group, contentDescription = null, modifier = Modifier.size(48.dp), tint = TextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No split bills logged yet.", fontSize = 13.sp, color = TextSecondary)
                    }
                }
            }
        } else {
            items(splitBills) { bill ->
                BillCard(bill, viewModel)
            }
        }
    }
}

@Composable
fun BillCard(
    bill: SplitBillEntity,
    viewModel: LankaBudgetViewModel
) {
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(false) }

    // Parse members formatted string: "Alice|1000.0|unpaid,Bob|1000.0|paid"
    val members = remember(bill.membersJson) {
        bill.membersJson.split(",")
            .filter { it.isNotBlank() }
            .mapNotNull {
                val parts = it.split("|")
                if (parts.size >= 3) {
                    BillSplitMember(
                        name = parts[0],
                        share = parts[1].toDoubleOrNull() ?: 0.0,
                        isPaid = parts[2] == "paid"
                    )
                } else null
            }
    }

    val totalToCollect = members.sumOf { it.share }
    val collectedAmount = members.filter { it.isPaid }.sumOf { it.share }
    val progressFraction = if (totalToCollect > 0) (collectedAmount / totalToCollect).toFloat() else 1f
    val isFullyCollected = members.all { it.isPaid }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .liquidGlassCard(cornerRadius = 16.dp, containerColor = Color.White.copy(alpha = 0.45f))
            .clickable { isExpanded = !isExpanded }
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(bill.description, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                    Text(
                        text = "Total Bill: ${formatLKR(bill.totalAmount)} • My share: ${formatLKR(bill.myShare)}",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isFullyCollected) SettlementSuccess.copy(alpha = 0.15f) else ElectricNeeds.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isFullyCollected) "RECONCILED" else "PENDING",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isFullyCollected) SettlementSuccess else ElectricNeeds
                    )
                }
            }

            // Collection Progress Indicator
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Collected ${formatLKR(collectedAmount)} of ${formatLKR(totalToCollect)}",
                        fontSize = 11.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "${(progressFraction * 100).toInt()}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricNeeds
                    )
                }
                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                    color = if (isFullyCollected) SettlementSuccess else ElectricNeeds,
                    trackColor = HeaderPillBg
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorizontalDivider(color = Color.White.copy(alpha = 0.3f))
                    
                    Text("Friend Shares / Collections:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)

                    members.forEach { m ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White.copy(alpha = 0.25f))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(m.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                Text(formatLKR(m.share), fontSize = 11.sp, color = TextSecondary)
                            }

                            if (m.isPaid) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Paid", tint = SettlementSuccess, modifier = Modifier.size(16.dp))
                                    Text("Collected", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SettlementSuccess)
                                }
                            } else {
                                Button(
                                    onClick = {
                                        // Update status of this friend to "paid"
                                        val updatedMembers = members.map {
                                            if (it.name == m.name) it.copy(isPaid = true) else it
                                        }
                                        val newMembersJson = updatedMembers.joinToString(",") {
                                            "${it.name}|${it.share}|${if (it.isPaid) "paid" else "unpaid"}"
                                        }

                                        viewModel.updateSplitBill(bill.copy(membersJson = newMembersJson))
                                        
                                        // Also add to income since we collected cash!
                                        viewModel.addIncome(
                                            title = "Collected Share from ${m.name} for ${bill.description}",
                                            amount = m.share,
                                            category = "Gifts & Celebrations"
                                        )

                                        Toast.makeText(context, "${m.name}'s share of ${formatLKR(m.share)} collected!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ElectricNeeds),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    shape = CircleShape,
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("Mark Collected", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                viewModel.deleteSplitBill(bill)
                                Toast.makeText(context, "Split bill deleted", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = DangerRed)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Delete Log", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CashDivisionSection(
    viewModel: LankaBudgetViewModel
) {
    val context = LocalContext.current

    // Local lists and states
    var withdrawalTitle by remember { mutableStateOf("") }
    var totalWithdrawalStr by remember { mutableStateOf("") }
    
    // Split lines creation states
    var itemName by remember { mutableStateOf("") }
    var itemAmtStr by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Food & Snacks") }
    var selectedBucket by remember { mutableStateOf("WANTS") }

    val categories = listOf("Food & Snacks", "Fuel & Transport", "Groceries", "Entertainment", "Utilities", "Savings")
    val buckets = listOf("NEEDS", "WANTS", "SAVINGS")

    val splitLines = remember { mutableStateListOf<CashSplitLine>() }

    // Computations
    val totalWithdrawal = totalWithdrawalStr.toDoubleOrNull() ?: 0.0
    val totalAllocated = splitLines.sumOf { it.amount }
    val remainingToAllocate = totalWithdrawal - totalAllocated
    val progressFraction = if (totalWithdrawal > 0) (totalAllocated / totalWithdrawal).toFloat().coerceIn(0f, 1f) else 0f

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Step 1: Overall Withdrawal Sum
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlassCard(cornerRadius = 20.dp, containerColor = Color.White.copy(alpha = 0.5f))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "1. DEFINE Cash WALLET DISBURSEMENT",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = ElectricNeeds
                    )

                    OutlinedTextField(
                        value = withdrawalTitle,
                        onValueChange = { withdrawalTitle = it },
                        label = { Text("Disbursement Source Describe") },
                        placeholder = { Text("e.g. ATM Withdraw / Salary Cash") },
                        modifier = Modifier.fillMaxWidth().testTag("withdrawal_title_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = totalWithdrawalStr,
                        onValueChange = { totalWithdrawalStr = it },
                        label = { Text("Gross Sum to Allocate") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        placeholder = { Text("e.g. 5000") },
                        modifier = Modifier.fillMaxWidth().testTag("withdrawal_gross_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            }
        }

        // Step 2: Define Split Allocations
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlassCard(cornerRadius = 20.dp, containerColor = Color.White.copy(alpha = 0.5f))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "2. ADD MULTIPLE CATEGORY SPEND LINES",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = ElectricNeeds
                    )

                    OutlinedTextField(
                        value = itemName,
                        onValueChange = { itemName = it },
                        label = { Text("Item Spent Title") },
                        placeholder = { Text("e.g. fuel for car, cookies") },
                        modifier = Modifier.fillMaxWidth().testTag("spend_item_name"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = itemAmtStr,
                            onValueChange = { itemAmtStr = it },
                            label = { Text("Spent Amount") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            placeholder = { Text("e.g. 1500") },
                            modifier = Modifier.weight(1.2f).testTag("spend_item_amount"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        // Category Selection Dropdown
                        var categoryExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1f).align(Alignment.CenterVertically)) {
                            OutlinedButton(
                                onClick = { categoryExpanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                            ) {
                                Text(selectedCategory, maxLines = 1, fontSize = 10.sp)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(14.dp))
                            }
                            DropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                                categories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat, fontSize = 12.sp) },
                                        onClick = {
                                            selectedCategory = cat
                                            categoryExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Bucket Selection
                        var bucketExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedButton(
                                onClick = { bucketExpanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                            ) {
                                Text("Bucket: $selectedBucket", fontSize = 11.sp)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(14.dp))
                            }
                            DropdownMenu(expanded = bucketExpanded, onDismissRequest = { bucketExpanded = false }) {
                                buckets.forEach { b ->
                                    DropdownMenuItem(
                                        text = { Text(b, fontSize = 12.sp) },
                                        onClick = {
                                            selectedBucket = b
                                            bucketExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                val amt = itemAmtStr.toDoubleOrNull() ?: 0.0
                                if (itemName.trim().isBlank()) {
                                    Toast.makeText(context, "Enter spent item description", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (amt <= 0.0) {
                                    Toast.makeText(context, "Enter a valid amount", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                splitLines.add(
                                    CashSplitLine(
                                        name = itemName.trim(),
                                        amount = amt,
                                        bucket = selectedBucket,
                                        category = selectedCategory
                                    )
                                )

                                // clear mini forms
                                itemName = ""
                                itemAmtStr = ""
                            },
                            modifier = Modifier.weight(1f).height(40.dp).testTag("add_item_split_line"),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricNeeds),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Line", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Live calculations status board
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlassCard(cornerRadius = 16.dp, containerColor = Color.White.copy(alpha = 0.4f))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Gross Cash: ${formatLKR(totalWithdrawal)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Remaining to Split: ${formatLKR(remainingToAllocate)}", fontSize = 11.sp, color = if (remainingToAllocate == 0.0) SettlementSuccess else ElectricNeeds)
                    }

                    LinearProgressIndicator(
                        progress = { progressFraction },
                        color = if (remainingToAllocate == 0.0 && totalWithdrawal > 0.0) SettlementSuccess else ElectricNeeds,
                        trackColor = HeaderPillBg,
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape)
                    )
                }
            }
        }

        // Split Lines lists
        if (splitLines.isNotEmpty()) {
            item {
                Text(
                    text = "DISBURSED CATEGORY LEDGERS:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = ElectricNeeds
                )
            }

            items(splitLines) { line ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlassCard(cornerRadius = 12.dp, containerColor = Color.White.copy(alpha = 0.35f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(line.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                        Text("Bucket: ${line.bucket} • Category: ${line.category}", fontSize = 11.sp, color = TextSecondary)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(formatLKR(line.amount), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                        IconButton(
                            onClick = { splitLines.remove(line) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Line", tint = DangerRed, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        if (totalWithdrawal <= 0) {
                            Toast.makeText(context, "Define withdrawal gross first", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (splitLines.isEmpty()) {
                            Toast.makeText(context, "Please add spend lines first", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        // Log all split expenses
                        splitLines.forEach { line ->
                            viewModel.addExpense(
                                title = line.name,
                                amount = line.amount,
                                bucket = line.bucket,
                                category = line.category
                            )
                        }

                        Toast.makeText(
                            context,
                            "Success! Logged ${splitLines.size} split expenses worth ${formatLKR(totalAllocated)} in budget ledger.",
                            Toast.LENGTH_LONG
                        ).show()

                        // Reset
                        splitLines.clear()
                        withdrawalTitle = ""
                        totalWithdrawalStr = ""
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("log_cash_splits_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = SettlementSuccess),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.DoneAll, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Consolidate & Log Cash Expenses", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
