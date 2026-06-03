package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.LankaBudgetDatabase
import com.example.data.model.*
import com.example.data.repository.LankaBudgetRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileWriter
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

data class BackupItem(val name: String, val filePath: String, val isPublic: Boolean)

// Multimodal Gemini AI OCR Parsing structure
data class ScannedBillResult(
    val isIncome: Boolean,
    val title: String,
    val amount: Double,
    val category: String,
    val bucket: String,
    val suggestedSubBreakdown: List<Pair<String, Double>> = emptyList()
)

// High-fidelity Budget Summary data model for 50/30/20 division
data class BudgetSummary(
    val totalIncome: Double = 0.0,
    val needsBudget: Double = 0.0,
    val wantsBudget: Double = 0.0,
    val savingsBudget: Double = 0.0,
    val needsExpenses: Double = 0.0,
    val wantsExpenses: Double = 0.0,
    val savingsExpenses: Double = 0.0,
    val needsRemaining: Double = 0.0,
    val wantsRemaining: Double = 0.0,
    val savingsRemaining: Double = 0.0
)

data class CustomCategory(
    val name: String,
    val isIncome: Boolean,
    val bucket: String // "NEEDS", "WANTS", "SAVINGS", or "INCOME"
)

class LankaBudgetViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPrefs: SharedPreferences = application.getSharedPreferences("lanka_budget_prefs", Context.MODE_PRIVATE)
    
    val activeProfile = MutableStateFlow(sharedPrefs.getString("active_profile", "Personal") ?: "Personal")
    
    val profilesList = MutableStateFlow<List<String>>(
        sharedPrefs.getStringSet("profiles_list", setOf("Personal"))?.toList()?.sorted() ?: listOf("Personal")
    )

    // Salary tracking configuration StateFlows
    val salaryDayOfMonth = MutableStateFlow(sharedPrefs.getInt("salary_day", 25))
    val predictedSalaryAmount = MutableStateFlow(sharedPrefs.getFloat("predicted_salary", 300000.0f).toDouble())

    // Custom Transaction Categories
    val customCategories = MutableStateFlow<List<CustomCategory>>(emptyList())

    fun loadCustomCategories() {
        val serialized = sharedPrefs.getString("custom_categories_v2", "") ?: ""
        if (serialized.isEmpty()) {
            val defaults = listOf(
                CustomCategory("Salary", true, "INCOME"),
                CustomCategory("Freelance Income", true, "INCOME"),
                CustomCategory("Freelance", true, "INCOME"),
                CustomCategory("Bonus", true, "INCOME"),
                CustomCategory("Investment Income", true, "INCOME"),
                CustomCategory("Groceries", false, "NEEDS"),
                CustomCategory("Utilities", false, "NEEDS"),
                CustomCategory("Bills & Rent", false, "NEEDS"),
                CustomCategory("Transport", false, "NEEDS"),
                CustomCategory("Mandatory Debt", false, "NEEDS"),
                CustomCategory("Dine Out", false, "WANTS"),
                CustomCategory("Cinema & Movies", false, "WANTS"),
                CustomCategory("Hobbies", false, "WANTS"),
                CustomCategory("Gifts", false, "WANTS"),
                CustomCategory("Apparel", false, "WANTS"),
                CustomCategory("Money Market Fund (MMF)", false, "SAVINGS"),
                CustomCategory("Fixed Deposit", false, "SAVINGS"),
                CustomCategory("Emergency Fund", false, "SAVINGS"),
                CustomCategory("Crypto", false, "SAVINGS"),
                CustomCategory("Other Needs", false, "NEEDS"),
                CustomCategory("Other Wants", false, "WANTS"),
                CustomCategory("Other Savings", false, "SAVINGS"),
                CustomCategory("Other Income", true, "INCOME")
            )
            saveCustomCategories(defaults)
        } else {
            try {
                val array = JSONArray(serialized)
                val list = mutableListOf<CustomCategory>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        CustomCategory(
                            name = obj.getString("name"),
                            isIncome = obj.getBoolean("isIncome"),
                            bucket = obj.getString("bucket")
                        )
                    )
                }
                customCategories.value = list
            } catch (e: Exception) {
                Log.e("LankaBudgetViewModel", "Error parsing custom categories", e)
            }
        }
    }

    fun saveCustomCategories(list: List<CustomCategory>) {
        try {
            val array = JSONArray()
            list.forEach { item ->
                val obj = JSONObject().apply {
                    put("name", item.name)
                    put("isIncome", item.isIncome)
                    put("bucket", item.bucket)
                }
                array.put(obj)
            }
            sharedPrefs.edit().putString("custom_categories_v2", array.toString()).apply()
            customCategories.value = list
        } catch (e: Exception) {
            Log.e("LankaBudgetViewModel", "Error saving custom categories", e)
        }
    }

    fun addCustomCategory(name: String, isIncome: Boolean, bucket: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        val current = customCategories.value.toMutableList()
        if (current.any { it.name.equals(trimmed, ignoreCase = true) }) return
        current.add(CustomCategory(trimmed, isIncome, bucket))
        saveCustomCategories(current)
    }

    fun deleteCustomCategory(name: String) {
        val current = customCategories.value.filter { !it.name.equals(name, ignoreCase = true) }
        saveCustomCategories(current)
    }

    fun getBucketForCategory(category: String): String {
        val matched = customCategories.value.find { it.name.equals(category, ignoreCase = true) }
        if (matched != null) {
            return matched.bucket
        }
        val catLower = category.lowercase().trim()
        return when {
            catLower.contains("groceries") || 
            catLower.contains("utility") || 
            catLower.contains("utilities") || 
            catLower.contains("rent") || 
            catLower.contains("bill") || 
            catLower.contains("bills") || 
            catLower.contains("transport") || 
            catLower.contains("fuel") || 
            catLower.contains("debt") || 
            catLower.contains("medical") || 
            catLower.contains("insurance") || 
            catLower.contains("pharmacy") || 
            catLower.contains("grocer") -> "NEEDS"

            catLower.contains("saving") || 
            catLower.contains("savings") || 
            catLower.contains("money market") || 
            catLower.contains("mmf") || 
            catLower.contains("fixed deposit") || 
            catLower.contains("fd") || 
            catLower.contains("emergency") || 
            catLower.contains("crypto") || 
            catLower.contains("invest") || 
            catLower.contains("investment") -> "SAVINGS"

            else -> "WANTS"
        }
    }

    fun updateSalaryDay(day: Int) {
        sharedPrefs.edit().putInt("salary_day", day).apply()
        salaryDayOfMonth.value = day
    }

    fun updateSalaryAmount(amount: Double) {
        sharedPrefs.edit().putFloat("predicted_salary", amount.toFloat()).apply()
        predictedSalaryAmount.value = amount
        // If initial capital hasn't been set yet, set it now. Subsequent updates (salary raises) won't alter initial capital reference.
        if (!sharedPrefs.contains("initial_capital")) {
            sharedPrefs.edit().putFloat("initial_capital", amount.toFloat()).apply()
            initialCapital.value = amount
        }
    }

    fun getSalaryDatesAndPredictions(): Triple<String, String, Double> {
        val txs = transactions.value
        val lastSalaryTx = txs.filter { it.isIncome && it.title.lowercase().contains("salary") }
            .maxByOrNull { it.timestamp }

        val sdf = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
        val lastPaidDateMillis: Long
        // Always use the configured predictedSalaryAmount (which contains salary raises) for forecasting/budgets instead of pinning it to old historical transactions
        val predictedAmt = predictedSalaryAmount.value

        if (lastSalaryTx != null) {
            lastPaidDateMillis = lastSalaryTx.timestamp
        } else {
            val calendar = java.util.Calendar.getInstance()
            val currentDay = calendar.get(java.util.Calendar.DAY_OF_MONTH)
            val configDay = salaryDayOfMonth.value.coerceIn(1, 31)
            
            if (currentDay >= configDay) {
                calendar.set(java.util.Calendar.DAY_OF_MONTH, configDay)
            } else {
                calendar.add(java.util.Calendar.MONTH, -1)
                val maxDay = calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
                calendar.set(java.util.Calendar.DAY_OF_MONTH, configDay.coerceAtMost(maxDay))
            }
            lastPaidDateMillis = calendar.timeInMillis
        }

        val nextCal = java.util.Calendar.getInstance()
        nextCal.timeInMillis = lastPaidDateMillis
        nextCal.add(java.util.Calendar.MONTH, 1)
        val configDay = salaryDayOfMonth.value.coerceIn(1, 31)
        val maxDay = nextCal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
        nextCal.set(java.util.Calendar.DAY_OF_MONTH, configDay.coerceAtMost(maxDay))
        
        val currentSalaryDateStr = sdf.format(java.util.Date(lastPaidDateMillis))
        val nextSalaryDateStr = sdf.format(nextCal.time)

        return Triple(currentSalaryDateStr, nextSalaryDateStr, predictedAmt)
    }

    fun getFutureSalaryProjections(): List<Pair<String, Double>> {
        val salaryData = getSalaryDatesAndPredictions()
        val baseAmt = salaryData.third
        val sdf = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
        
        val lastPaidMillis: Long
        val txs = transactions.value
        val lastSalaryTx = txs.filter { it.isIncome && it.title.lowercase().contains("salary") }
            .maxByOrNull { it.timestamp }
        if (lastSalaryTx != null) {
            lastPaidMillis = lastSalaryTx.timestamp
        } else {
            val calendar = java.util.Calendar.getInstance()
            val currentDay = calendar.get(java.util.Calendar.DAY_OF_MONTH)
            val configDay = salaryDayOfMonth.value.coerceIn(1, 31)
            if (currentDay >= configDay) {
                calendar.set(java.util.Calendar.DAY_OF_MONTH, configDay)
            } else {
                calendar.add(java.util.Calendar.MONTH, -1)
                val maxDay = calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
                calendar.set(java.util.Calendar.DAY_OF_MONTH, configDay.coerceAtMost(maxDay))
            }
            lastPaidMillis = calendar.timeInMillis
        }

        val list = mutableListOf<Pair<String, Double>>()
        for (i in 1..4) {
            val cal = java.util.Calendar.getInstance()
            cal.timeInMillis = lastPaidMillis
            cal.add(java.util.Calendar.MONTH, i)
            val configDay = salaryDayOfMonth.value.coerceIn(1, 31)
            val maxDay = cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
            cal.set(java.util.Calendar.DAY_OF_MONTH, configDay.coerceAtMost(maxDay))
            list.add(Pair(sdf.format(cal.time), baseAmt))
        }
        return list
    }

    // Expense Cap Alerts and Targets
    val needsLimit = MutableStateFlow(sharedPrefs.getFloat("needs_limit", 150000.0f).toDouble())
    val wantsLimit = MutableStateFlow(sharedPrefs.getFloat("wants_limit", 100000.0f).toDouble())
    val savingsLimit = MutableStateFlow(sharedPrefs.getFloat("savings_limit", 50000.0f).toDouble())

    // Dual-Currency Mode Selector
    val selectedCurrency = MutableStateFlow(sharedPrefs.getString("selected_currency", "LKR") ?: "LKR")

    // Report Mode Toggle (Simple, Normal, Expert)
    val reportMode = MutableStateFlow(sharedPrefs.getString("report_mode", "Normal") ?: "Normal")

    // Security Lock Settings
    val useSecurityLock = MutableStateFlow(sharedPrefs.getBoolean("use_security_lock", false))
    val useBiometricLock = MutableStateFlow(sharedPrefs.getBoolean("use_biometric_lock", false))
    val securityPin = MutableStateFlow(sharedPrefs.getString("security_pin", "1234") ?: "1234")

    // Theme Mode settings (Auto, Light, Dark, Amoled)
    val themeMode = MutableStateFlow(sharedPrefs.getString("theme_mode", "Auto") ?: "Auto")

    // Capital equity starting point to insulate past Double-Entry balance balances during salary raises
    val initialCapital = MutableStateFlow(sharedPrefs.getFloat("initial_capital", sharedPrefs.getFloat("predicted_salary", 300000.0f)).toDouble())

    val customDashboardWidgets = MutableStateFlow<List<DashboardWidget>>(emptyList())

    private fun loadCustomWidgets(): List<DashboardWidget> {
        val jsonStr = sharedPrefs.getString("custom_dashboard_widgets_v1", null)
        if (!jsonStr.isNullOrBlank()) {
            try {
                val list = mutableListOf<DashboardWidget>()
                val arr = JSONArray(jsonStr)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val idStr = obj.getString("id")
                    val defaultH = when (idStr) {
                        "BALANCE" -> 2
                        "NET_WORTH" -> 3
                        "ALLOCATION" -> 2
                        "CHART" -> 3
                        "AUDIT" -> 2
                        "TRANSACTIONS" -> 3
                        else -> 2
                    }
                    list.add(
                        DashboardWidget(
                            id = idStr,
                            title = obj.getString("title"),
                            isEnabled = obj.optBoolean("isEnabled", true),
                            isHalfWidth = obj.optBoolean("isHalfWidth", false),
                            heightScale = obj.optDouble("heightScale", 1.0).toFloat(),
                            glassColorTint = obj.optString("glassColorTint", "White"),
                            order = obj.optInt("order", i),
                            gridWidth = obj.optInt("gridWidth", 5),
                            gridHeight = obj.optInt("gridHeight", defaultH)
                        )
                    )
                }
                return list.sortedBy { it.order }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return listOf(
            DashboardWidget(id = "BALANCE", title = "Primary Balance & Quick Entries", isEnabled = true, isHalfWidth = false, heightScale = 1.0f, glassColorTint = "White", order = 0, gridWidth = 5, gridHeight = 2),
            DashboardWidget(id = "NET_WORTH", title = "Balance Sheet & Net Worth", isEnabled = true, isHalfWidth = false, heightScale = 1.0f, glassColorTint = "White", order = 1, gridWidth = 5, gridHeight = 3),
            DashboardWidget(id = "ALLOCATION", title = "Budget Triple Allocation (50/30/20)", isEnabled = true, isHalfWidth = false, heightScale = 1.0f, glassColorTint = "White", order = 2, gridWidth = 5, gridHeight = 2),
            DashboardWidget(id = "CHART", title = "Analytics Ring Chart", isEnabled = true, isHalfWidth = false, heightScale = 1.0f, glassColorTint = "White", order = 3, gridWidth = 5, gridHeight = 3),
            DashboardWidget(id = "AUDIT", title = "Double-Entry Auditor Equation", isEnabled = false, isHalfWidth = false, heightScale = 1.0f, glassColorTint = "White", order = 4, gridWidth = 5, gridHeight = 2),
            DashboardWidget(id = "TRANSACTIONS", title = "Transaction Ledger logs", isEnabled = true, isHalfWidth = false, heightScale = 1.0f, glassColorTint = "White", order = 5, gridWidth = 5, gridHeight = 3)
        )
    }

    fun saveCustomWidgets(widgets: List<DashboardWidget>) {
        try {
            val arr = JSONArray()
            widgets.forEach { widget ->
                val obj = JSONObject()
                obj.put("id", widget.id)
                obj.put("title", widget.title)
                obj.put("isEnabled", widget.isEnabled)
                obj.put("isHalfWidth", widget.isHalfWidth)
                obj.put("heightScale", widget.heightScale.toDouble())
                obj.put("glassColorTint", widget.glassColorTint ?: "White")
                obj.put("order", widget.order)
                obj.put("gridWidth", widget.gridWidth)
                obj.put("gridHeight", widget.gridHeight)
                arr.put(obj)
            }
            val jsonStr = arr.toString()
            sharedPrefs.edit().putString("custom_dashboard_widgets_v1", jsonStr).apply()
            customDashboardWidgets.value = widgets
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateSecurityLock(enabled: Boolean) {
        sharedPrefs.edit().putBoolean("use_security_lock", enabled).apply()
        useSecurityLock.value = enabled
    }

    fun updateBiometricLock(enabled: Boolean) {
        sharedPrefs.edit().putBoolean("use_biometric_lock", enabled).apply()
        useBiometricLock.value = enabled
    }

    fun updateThemeMode(mode: String) {
        sharedPrefs.edit().putString("theme_mode", mode).apply()
        themeMode.value = mode
    }

    fun updateInitialCapital(amount: Double) {
        sharedPrefs.edit().putFloat("initial_capital", amount.toFloat()).apply()
        initialCapital.value = amount
    }

    fun updateSecurityPin(pin: String) {
        sharedPrefs.edit().putString("security_pin", pin).apply()
        securityPin.value = pin
    }

    fun updateReportMode(mode: String) {
        sharedPrefs.edit().putString("report_mode", mode).apply()
        reportMode.value = mode
    }

    fun updateNeedsLimit(limit: Double) {
        sharedPrefs.edit().putFloat("needs_limit", limit.toFloat()).apply()
        needsLimit.value = limit
    }

    fun updateWantsLimit(limit: Double) {
        sharedPrefs.edit().putFloat("wants_limit", limit.toFloat()).apply()
        wantsLimit.value = limit
    }

    fun updateSavingsLimit(limit: Double) {
        sharedPrefs.edit().putFloat("savings_limit", limit.toFloat()).apply()
        savingsLimit.value = limit
    }

    fun changeCurrency(currency: String) {
        sharedPrefs.edit().putString("selected_currency", currency).apply()
        selectedCurrency.value = currency
    }

    fun exportTransactionsToCSV(): String {
        val txs = transactions.value
        val csv = java.lang.StringBuilder()
        csv.append("ID,Date,Title,Type,Category,Bucket,Amount\n")
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        txs.forEach { tx ->
            val dateStr = sdf.format(java.util.Date(tx.timestamp))
            val cleanTitle = tx.title.replace("\"", "\"\"")
            val cleanCat = tx.category.replace("\"", "\"\"")
            val typeStr = if (tx.isIncome) "INCOME" else "EXPENSE"
            csv.append("${tx.id},\"$dateStr\",\"$cleanTitle\",$typeStr,\"$cleanCat\",${tx.bucket},${tx.amount}\n")
        }
        return csv.toString()
    }

    fun getBalanceTrajectory(): List<Pair<String, Double>> {
        val txs = transactions.value.sortedBy { it.timestamp }
        var current = 0.0
        val trajectory = mutableListOf<Pair<String, Double>>()
        val sdf = java.text.SimpleDateFormat("MM-dd", java.util.Locale.getDefault())
        
        trajectory.add(Pair("Start", 0.0))
        txs.forEach { tx ->
            if (tx.isIncome) {
                current += tx.amount
            } else {
                current -= tx.amount
            }
            val dateLabel = sdf.format(java.util.Date(tx.timestamp))
            trajectory.add(Pair(dateLabel, current))
        }
        if (txs.isEmpty()) {
            return listOf(Pair("Now", 0.0))
        }
        return if (trajectory.size > 14) {
            trajectory.takeLast(14)
        } else {
            trajectory
        }
    }

    private val repository: LankaBudgetRepository

    val transactions: StateFlow<List<Transaction>>
    val loans: StateFlow<List<Loan>>
    val repayments: StateFlow<List<RepaymentLog>>
    val recurringTransactions: StateFlow<List<RecurringTransaction>>
    val investments: StateFlow<List<Investment>>
    val budgetCaps: StateFlow<List<com.example.data.model.BudgetCapEntity>>
    val splitBills: StateFlow<List<SplitBillEntity>>
    val goals: StateFlow<List<Goal>>
    val goalContributions: StateFlow<List<GoalContribution>>

    val budgetSummaryState: StateFlow<BudgetSummary>
    val totalDebtBalance: StateFlow<Double>
    val availableBalance: StateFlow<Double>
    val totalInvestmentBalance: StateFlow<Double>
    val netWorth: StateFlow<Double>

    init {
        loadCustomCategories()
        customDashboardWidgets.value = loadCustomWidgets()
        val initProfile = sharedPrefs.getString("active_profile", "Personal") ?: "Personal"
        val database = LankaBudgetDatabase.getDatabase(application, initProfile)
        repository = LankaBudgetRepository(
            transactionDao = database.transactionDao(),
            loanDao = database.loanDao(),
            repaymentLogDao = database.repaymentLogDao(),
            recurringTransactionDao = database.recurringTransactionDao(),
            investmentDao = database.investmentDao(),
            budgetCapDao = database.budgetCapDao(),
            splitBillDao = database.splitBillDao(),
            goalDao = database.goalDao()
        )

        // Automatically trigger evaluation of recurring schedules on startup!
        viewModelScope.launch {
            repository.generateRecurringPayments()
        }

        recurringTransactions = repository.allRecurringTransactions
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        transactions = repository.allTransactions
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        loans = repository.allLoans
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        repayments = repository.allRepayments
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        investments = repository.allInvestments
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        budgetCaps = repository.allBudgetCaps
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        splitBills = repository.allSplitBills
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        goals = repository.allGoals
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        goalContributions = repository.allGoalContributions
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        totalInvestmentBalance = investments
            .map { invList ->
                invList.sumOf { it.currentValue }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = 0.0
            )

        // Reactive 50/30/20 Allocation Split Engine
        budgetSummaryState = combine(transactions, customCategories, budgetCaps) { txList, _, capsList ->
            val totalIncome = txList.filter { it.isIncome }.sumOf { it.amount }

            // Get current active calendar year & month (1-indexed for month)
            val cal = java.util.Calendar.getInstance()
            val m = cal.get(java.util.Calendar.MONTH) + 1
            val y = cal.get(java.util.Calendar.YEAR)

            // Current month category budget caps
            val activeCaps = capsList.filter { it.month == m && it.year == y }

            var customNeedsBudget = 0.0
            var customWantsBudget = 0.0
            var customSavingsBudget = 0.0

            activeCaps.forEach { cap ->
                val b = getBucketForCategory(cap.categoryId)
                when (b) {
                    "NEEDS" -> customNeedsBudget += cap.allocatedAmount
                    "WANTS" -> customWantsBudget += cap.allocatedAmount
                    "SAVINGS" -> customSavingsBudget += cap.allocatedAmount
                }
            }

            val needsBudget = if (customNeedsBudget > 0.0) customNeedsBudget else totalIncome * 0.50
            val wantsBudget = if (customWantsBudget > 0.0) customWantsBudget else totalIncome * 0.30
            val savingsBudget = if (customSavingsBudget > 0.0) customSavingsBudget else totalIncome * 0.20

            val needsExpenses = txList.filter { !it.isIncome && getBucketForCategory(it.category) == "NEEDS" }.sumOf { it.amount }
            val wantsExpenses = txList.filter { !it.isIncome && getBucketForCategory(it.category) == "WANTS" }.sumOf { it.amount }
            val savingsExpenses = txList.filter { !it.isIncome && getBucketForCategory(it.category) == "SAVINGS" }.sumOf { it.amount }

            BudgetSummary(
                totalIncome = totalIncome,
                needsBudget = needsBudget,
                wantsBudget = wantsBudget,
                savingsBudget = savingsBudget,
                needsExpenses = needsExpenses,
                wantsExpenses = wantsExpenses,
                savingsExpenses = savingsExpenses,
                needsRemaining = needsBudget - needsExpenses,
                wantsRemaining = wantsBudget - wantsExpenses,
                savingsRemaining = savingsBudget - savingsExpenses
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BudgetSummary()
        )

        // Reactive Total Debt Balance Calculation
        totalDebtBalance = loans
            .map { loanList ->
                loanList.filter { !it.isCleared }.sumOf { it.remainingAmount }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = 0.0
            )

        // Available Balance = Earned income - Spendings - Cumulative paid debt installments
        // This is extremely logical, maintaining exact financial fidelity
        availableBalance = combine(transactions, repayments) { txList, payList ->
            val totalIncome = txList.filter { it.isIncome }.sumOf { it.amount }
            val totalExpenses = txList.filter { !it.isIncome }.sumOf { it.amount }
            val totalDebtRepaid = payList.sumOf { it.amountPaid }
            
            (totalIncome - (totalExpenses + totalDebtRepaid)).coerceAtLeast(0.0)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

        netWorth = combine(availableBalance, totalInvestmentBalance, totalDebtBalance) { cash, invs, debt ->
            cash + invs - debt
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )
    }

    // Transactions API
    fun addIncome(title: String, amount: Double, category: String, subTransactionsStr: String = "") {
        viewModelScope.launch {
            repository.insertTransaction(
                Transaction(
                    title = title,
                    amount = amount,
                    isIncome = true,
                    bucket = "INCOME",
                    category = category,
                    subTransactionsStr = subTransactionsStr
                )
            )
        }
    }

    fun addExpense(title: String, amount: Double, bucket: String, category: String, subTransactionsStr: String = "") {
        viewModelScope.launch {
            repository.insertTransaction(
                Transaction(
                    title = title,
                    amount = amount,
                    isIncome = false,
                    bucket = bucket,
                    category = category,
                    subTransactionsStr = subTransactionsStr
                )
            )
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAllTransactions()
        }
    }

    // Loans and Debt API
    fun addLoan(lenderName: String, borrowedAmount: Double) {
        viewModelScope.launch {
            repository.insertLoan(
                Loan(
                    lenderName = lenderName,
                    borrowedAmount = borrowedAmount,
                    remainingAmount = borrowedAmount,
                    isCleared = false
                )
            )
        }
    }

    fun payLoanInstallment(loanId: Int, amount: Double) {
        viewModelScope.launch {
            repository.payInstallment(loanId, amount)
        }
    }

    fun deleteLoan(loan: Loan) {
        viewModelScope.launch {
            repository.deleteLoan(loan)
        }
    }

    fun clearAllLoans() {
        viewModelScope.launch {
            repository.clearAllLoans()
        }
    }

    // Recurring Transactions APIs
    fun addRecurringIncome(title: String, amount: Double, category: String, recurrencePeriod: String, startDate: Long) {
        viewModelScope.launch {
            repository.insertRecurringTransaction(
                RecurringTransaction(
                    title = title,
                    amount = amount,
                    isIncome = true,
                    bucket = "INCOME",
                    category = category,
                    recurrencePeriod = recurrencePeriod,
                    startDate = startDate
                )
            )
        }
    }

    fun addRecurringExpense(title: String, amount: Double, bucket: String, category: String, recurrencePeriod: String, startDate: Long, associatedLoanId: Int? = null) {
        viewModelScope.launch {
            repository.insertRecurringTransaction(
                RecurringTransaction(
                    title = title,
                    amount = amount,
                    isIncome = false,
                    bucket = bucket,
                    category = category,
                    recurrencePeriod = recurrencePeriod,
                    startDate = startDate,
                    associatedLoanId = associatedLoanId
                )
            )
        }
    }

    fun deleteRecurringTransaction(recurring: RecurringTransaction) {
        viewModelScope.launch {
            repository.deleteRecurringTransaction(recurring)
        }
    }

    fun clearAllRecurringTransactions() {
        viewModelScope.launch {
            repository.clearAllRecurringTransactions()
        }
    }

    fun triggerCatchUp() {
        viewModelScope.launch {
            repository.generateRecurringPayments()
        }
    }

    // Investment APIs
    fun addInvestment(name: String, type: String, amountInvested: Double, currentValue: Double, expectedYield: Double, remarks: String = "") {
        viewModelScope.launch {
            repository.insertInvestment(
                Investment(
                    name = name,
                    type = type,
                    amountInvested = amountInvested,
                    currentValue = currentValue,
                    expectedYield = expectedYield,
                    remarks = remarks
                )
            )
        }
    }

    fun updateInvestmentValue(investment: Investment, newValue: Double) {
        viewModelScope.launch {
            repository.updateInvestment(investment.copy(currentValue = newValue))
        }
    }

    fun deleteInvestment(investment: Investment) {
        viewModelScope.launch {
            repository.deleteInvestment(investment)
        }
    }

    fun clearAllInvestments() {
        viewModelScope.launch {
            repository.clearAllInvestments()
        }
    }

    // Split Bills API
    fun addSplitBill(description: String, totalAmount: Double, myShare: Double, payerName: String, membersJson: String) {
        viewModelScope.launch {
            repository.insertSplitBill(
                SplitBillEntity(
                    description = description,
                    totalAmount = totalAmount,
                    myShare = myShare,
                    payerName = payerName,
                    membersJson = membersJson
                )
            )
        }
    }

    fun updateSplitBill(splitBill: SplitBillEntity) {
        viewModelScope.launch {
            repository.updateSplitBill(splitBill)
        }
    }

    fun deleteSplitBill(splitBill: SplitBillEntity) {
        viewModelScope.launch {
            repository.deleteSplitBill(splitBill)
        }
    }

    fun clearAllSplitBills() {
        viewModelScope.launch {
            repository.clearAllSplitBills()
        }
    }

    fun factoryResetLedger() {
        viewModelScope.launch {
            repository.clearAllTransactions()
            repository.clearAllLoans()
            repository.clearAllRecurringTransactions()
            repository.clearAllInvestments()
            repository.clearAllSplitBills()
        }
    }

    // Dynamic Multi-Profile Management
    fun switchProfile(profileName: String) {
        viewModelScope.launch {
            val database = LankaBudgetDatabase.getDatabase(getApplication(), profileName)
            repository.updateDaos(
                newTransactionDao = database.transactionDao(),
                newLoanDao = database.loanDao(),
                newRepaymentLogDao = database.repaymentLogDao(),
                newRecurringTransactionDao = database.recurringTransactionDao(),
                newInvestmentDao = database.investmentDao(),
                newBudgetCapDao = database.budgetCapDao(),
                newSplitBillDao = database.splitBillDao(),
                newGoalDao = database.goalDao()
            )
            sharedPrefs.edit().putString("active_profile", profileName).apply()
            activeProfile.value = profileName
            repository.generateRecurringPayments()
        }
    }

    fun addProfile(profileName: String) {
        val trimmed = profileName.trim()
        if (trimmed.isEmpty()) return
        val currentSet = sharedPrefs.getStringSet("profiles_list", setOf("Personal")) ?: setOf("Personal")
        val newSet = currentSet.toMutableSet()
        newSet.add(trimmed)
        sharedPrefs.edit().putStringSet("profiles_list", newSet).apply()
        profilesList.value = newSet.toList().sorted()
    }

    fun deleteProfile(profileName: String) {
        if (profileName == "Personal") return
        val currentSet = sharedPrefs.getStringSet("profiles_list", setOf("Personal")) ?: setOf("Personal")
        val newSet = currentSet.toMutableSet()
        newSet.remove(profileName)
        sharedPrefs.edit().putStringSet("profiles_list", newSet).apply()
        profilesList.value = newSet.toList().sorted()
        
        if (activeProfile.value == profileName) {
            switchProfile("Personal")
        }
        
        try {
            val dbName = "lanka_budget_database_${profileName.lowercase().trim().replace("\\s+".toRegex(), "_")}"
            getApplication<Application>().deleteDatabase(dbName)
        } catch (e: Exception) {
            Log.e("LankaBudgetViewModel", "Error deleting profile database: $e")
        }
    }

    // Generic Update API Methods
    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
        }
    }

    fun updateLoan(loan: Loan) {
        viewModelScope.launch {
            repository.updateLoan(loan)
        }
    }

    fun updateRecurringTransaction(recurring: RecurringTransaction) {
        viewModelScope.launch {
            repository.updateRecurringTransaction(recurring)
        }
    }

    fun updateInvestment(investment: Investment) {
        viewModelScope.launch {
            repository.updateInvestment(investment)
        }
    }

    // Backup & Restore Engine
    fun exportBackupAsJsonString(): String? {
        return try {
            val jsonRoot = JSONObject()
            jsonRoot.put("backupVersion", 1)
            jsonRoot.put("profileName", activeProfile.value)
            jsonRoot.put("timestamp", System.currentTimeMillis())

            val jArrayTx = JSONArray()
            transactions.value.forEach { tx ->
                val jObj = JSONObject().apply {
                    put("title", tx.title)
                    put("amount", tx.amount)
                    put("isIncome", tx.isIncome)
                    put("bucket", tx.bucket)
                    put("category", tx.category)
                    put("timestamp", tx.timestamp)
                    put("subTransactionsStr", tx.subTransactionsStr ?: "")
                }
                jArrayTx.put(jObj)
            }
            jsonRoot.put("transactions", jArrayTx)

            val jArrayLoans = JSONArray()
            loans.value.forEach { loan ->
                val jObj = JSONObject().apply {
                    put("id", loan.id)
                    put("lenderName", loan.lenderName)
                    put("borrowedAmount", loan.borrowedAmount)
                    put("remainingAmount", loan.remainingAmount)
                    put("isCleared", loan.isCleared)
                    put("timestamp", loan.timestamp)
                }
                jArrayLoans.put(jObj)
            }
            jsonRoot.put("loans", jArrayLoans)

            val jArrayRepayments = JSONArray()
            repayments.value.forEach { pay ->
                val jObj = JSONObject().apply {
                    put("loanId", pay.loanId)
                    put("amountPaid", pay.amountPaid)
                    put("timestamp", pay.timestamp)
                }
                jArrayRepayments.put(jObj)
            }
            jsonRoot.put("repayments", jArrayRepayments)

            val jArrayRecurring = JSONArray()
            recurringTransactions.value.forEach { rec ->
                val jObj = JSONObject().apply {
                    put("title", rec.title)
                    put("amount", rec.amount)
                    put("isIncome", rec.isIncome)
                    put("bucket", rec.bucket)
                    put("category", rec.category)
                    put("recurrencePeriod", rec.recurrencePeriod)
                    put("startDate", rec.startDate)
                    put("lastGeneratedDate", rec.lastGeneratedDate)
                }
                jArrayRecurring.put(jObj)
            }
            jsonRoot.put("recurring", jArrayRecurring)

            val jArrayInvestments = JSONArray()
            investments.value.forEach { inv ->
                val jObj = JSONObject().apply {
                    put("name", inv.name)
                    put("type", inv.type)
                    put("amountInvested", inv.amountInvested)
                    put("currentValue", inv.currentValue)
                    put("expectedYield", inv.expectedYield)
                    put("remarks", inv.remarks)
                    put("timestamp", inv.timestamp)
                }
                jArrayInvestments.put(jObj)
            }
            jsonRoot.put("investments", jArrayInvestments)

            jsonRoot.toString(4)
        } catch (e: Exception) {
            Log.e("LankaBudgetViewModel", "Error exporting backup: $e")
            null
        }
    }

    fun exportCSV(): String {
        val builder = java.lang.StringBuilder()
        builder.append("Date,Type,Title,Amount LKR,Bucket,Category\n")
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        transactions.value.forEach { tx ->
            val dateStr = sdf.format(java.util.Date(tx.timestamp))
            val typeStr = if (tx.isIncome) "INCOME" else "EXPENSE"
            val safeTitle = tx.title.replace(",", " ")
            val safeCategory = tx.category.replace(",", " ")
            builder.append("$dateStr,$typeStr,$safeTitle,${tx.amount},${tx.bucket},$safeCategory\n")
        }
        return builder.toString()
    }

    fun importBackupFromJsonString(jsonStr: String): Boolean {
        return try {
            val jsonRoot = JSONObject(jsonStr)
            
            viewModelScope.launch {
                repository.clearAllTransactions()
                repository.clearAllLoans()
                repository.clearAllRecurringTransactions()
                repository.clearAllInvestments()

                val jArrayTx = jsonRoot.optJSONArray("transactions")
                if (jArrayTx != null) {
                    for (i in 0 until jArrayTx.length()) {
                        val jo = jArrayTx.getJSONObject(i)
                        repository.insertTransaction(
                            Transaction(
                                title = jo.getString("title"),
                                amount = jo.getDouble("amount"),
                                isIncome = jo.getBoolean("isIncome"),
                                bucket = jo.getString("bucket"),
                                category = jo.getString("category"),
                                timestamp = jo.getLong("timestamp"),
                                subTransactionsStr = jo.optString("subTransactionsStr", "")
                            )
                        )
                    }
                }

                val jArrayLoans = jsonRoot.optJSONArray("loans")
                if (jArrayLoans != null) {
                    for (i in 0 until jArrayLoans.length()) {
                        val jo = jArrayLoans.getJSONObject(i)
                        val loan = Loan(
                            lenderName = jo.getString("lenderName"),
                            borrowedAmount = jo.getDouble("borrowedAmount"),
                            remainingAmount = jo.getDouble("remainingAmount"),
                            isCleared = jo.optBoolean("isCleared", false),
                            timestamp = jo.getLong("timestamp")
                        )
                        repository.insertLoan(loan)
                    }
                }

                repository.allLoans.firstOrNull()?.let { insertedLoans ->
                    val jArrayRepayments = jsonRoot.optJSONArray("repayments")
                    if (jArrayRepayments != null) {
                        for (i in 0 until jArrayRepayments.length()) {
                            val jo = jArrayRepayments.getJSONObject(i)
                            val oldLoanId = jo.getInt("loanId")
                            var matchedLender = ""
                            if (jArrayLoans != null) {
                                for (k in 0 until jArrayLoans.length()) {
                                    val lo = jArrayLoans.getJSONObject(k)
                                    if (lo.getInt("id") == oldLoanId) {
                                        matchedLender = lo.getString("lenderName")
                                        break
                                    }
                                }
                            }
                            val targetLoan = insertedLoans.find { it.lenderName == matchedLender }
                            if (targetLoan != null) {
                                repository.payInstallment(targetLoan.id, jo.getDouble("amountPaid"))
                            }
                        }
                    }
                }

                val jArrayRecurring = jsonRoot.optJSONArray("recurring")
                if (jArrayRecurring != null) {
                    for (i in 0 until jArrayRecurring.length()) {
                        val jo = jArrayRecurring.getJSONObject(i)
                        repository.insertRecurringTransaction(
                            RecurringTransaction(
                                title = jo.getString("title"),
                                amount = jo.getDouble("amount"),
                                isIncome = jo.getBoolean("isIncome"),
                                bucket = jo.getString("bucket"),
                                category = jo.getString("category"),
                                recurrencePeriod = jo.getString("recurrencePeriod"),
                                startDate = jo.getLong("startDate"),
                                lastGeneratedDate = jo.optLong("lastGeneratedDate", 0L)
                            )
                        )
                    }
                }

                val jArrayInvestments = jsonRoot.optJSONArray("investments")
                if (jArrayInvestments != null) {
                    for (i in 0 until jArrayInvestments.length()) {
                        val jo = jArrayInvestments.getJSONObject(i)
                        repository.insertInvestment(
                            Investment(
                                name = jo.getString("name"),
                                type = jo.getString("type"),
                                amountInvested = jo.getDouble("amountInvested"),
                                currentValue = jo.getDouble("currentValue"),
                                expectedYield = jo.getDouble("expectedYield"),
                                remarks = jo.optString("remarks", ""),
                                timestamp = jo.getLong("timestamp")
                            )
                        )
                    }
                }

                repository.generateRecurringPayments()
            }
            true
        } catch (e: Exception) {
            Log.e("LankaBudgetViewModel", "Error importing backup: $e")
            false
        }
    }

    fun saveLocalBackup(): Boolean {
        return saveRobustJsonBackup() != null
    }

    fun getLocalBackups(): List<String> {
        return getRobustJsonBackups().map { it.name }
    }

    fun restoreFromLocalBackup(filename: String): Boolean {
        val backup = getRobustJsonBackups().find { it.name == filename } ?: return false
        return restoreFromRobustBackup(backup.filePath)
    }

    fun saveRobustJsonBackup(): String? {
        val backupJson = exportBackupAsJsonString() ?: return null
        return try {
            val timestamp = System.currentTimeMillis()
            val profileSanitized = activeProfile.value.lowercase().trim().replace("\\s+".toRegex(), "_")
            val filename = "LankaBudget_Backup_${profileSanitized}_$timestamp.json"

            // 1. Write to private filesDir double backup
            val privateDir = File(getApplication<Application>().filesDir, "backups")
            if (!privateDir.exists()) privateDir.mkdirs()
            val privateFile = File(privateDir, filename)
            privateFile.writeText(backupJson)

            // 2. Write to public downloads directory so it persists after uninstall!
            val publicDir = File(
                android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS),
                "LankaBudgetBackups"
            )
            if (!publicDir.exists()) {
                publicDir.mkdirs()
            }
            val publicFile = File(publicDir, filename)
            publicFile.writeText(backupJson)

            publicFile.absolutePath
        } catch (e: Exception) {
            Log.e("LankaBudgetViewModel", "Error saving robust JSON backup: $e")
            null
        }
    }

    fun getRobustJsonBackups(): List<BackupItem> {
        val backupsList = mutableListOf<BackupItem>()
        val profileSanitized = activeProfile.value.lowercase().trim().replace("\\s+".toRegex(), "_")
        val filterPrefix = "LankaBudget_Backup_${profileSanitized}_"
        val oldSafeNamePrefix = "lankabudget_backup_${profileSanitized}"

        // Scan private files dir
        try {
            val privateDir = File(getApplication<Application>().filesDir, "backups")
            if (privateDir.exists() && privateDir.isDirectory) {
                privateDir.listFiles()?.forEach { file ->
                    if (file.name.endsWith(".json") && (file.name.contains(filterPrefix) || file.name.contains(oldSafeNamePrefix))) {
                        backupsList.add(BackupItem(file.name, file.absolutePath, false))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("LankaBudgetViewModel", "Error reading private backups: $e")
        }

        // Scan public Downloads folder (preserves backups after uninstall!)
        try {
            val publicDir = File(
                android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS),
                "LankaBudgetBackups"
            )
            if (publicDir.exists() && publicDir.isDirectory) {
                publicDir.listFiles()?.forEach { file ->
                    if (file.name.endsWith(".json") && (file.name.contains(filterPrefix) || file.name.contains(oldSafeNamePrefix))) {
                        if (backupsList.none { it.name == file.name }) {
                            backupsList.add(BackupItem(file.name, file.absolutePath, true))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("LankaBudgetViewModel", "Error reading public backups: $e")
        }

        return backupsList.sortedWith { item1, item2 ->
            val f1 = File(item1.filePath)
            val f2 = File(item2.filePath)
            f2.lastModified().compareTo(f1.lastModified())
        }
    }

    fun restoreFromRobustBackup(filePath: String): Boolean {
        return try {
            val file = File(filePath)
            if (!file.exists()) return false
            val jsonStr = file.readText()
            importBackupFromJsonString(jsonStr)
        } catch (e: Exception) {
            Log.e("LankaBudgetViewModel", "Error restoring from robust backup: $e")
            false
        }
    }

    fun updateCategoryBudgetCap(categoryId: String, amount: Double, month: Int, year: Int) {
        viewModelScope.launch {
            repository.deleteBudgetCapByKeys(categoryId, month, year)
            if (amount > 0.0) {
                repository.insertBudgetCap(
                    com.example.data.model.BudgetCapEntity(
                        categoryId = categoryId,
                        allocatedAmount = amount,
                        month = month,
                        year = year
                    )
                )
            }
        }
    }

    // Convert bitmap to base64 with visual-scaling bounds optimized for API speed and tokens
    private fun bitmapToBase64(bitmap: android.graphics.Bitmap): String {
        val outputStream = java.io.ByteArrayOutputStream()
        val scaledBitmap = if (bitmap.width > 1024 || bitmap.height > 1024) {
            val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
            val width = if (aspectRatio > 1) 1024 else (1024 * aspectRatio).toInt()
            val height = if (aspectRatio > 1) (1024 / aspectRatio).toInt() else 1024
            android.graphics.Bitmap.createScaledBitmap(bitmap, width, height, true)
        } else {
            bitmap
        }
        scaledBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, outputStream)
        return android.util.Base64.encodeToString(outputStream.toByteArray(), android.util.Base64.NO_WRAP)
    }

    // Direct text-based Gemini query for parsing natural language spoken/written inputs safely
    fun parseVoiceCommandWithGemini(
        commandText: String,
        onSuccess: (ScannedBillResult) -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val apiKey = com.example.BuildConfig.GEMINI_API_KEY
                if (apiKey == "MY_GEMINI_API_KEY" || apiKey.isBlank()) {
                    launch(kotlinx.coroutines.Dispatchers.Main) {
                        onFailure("Gemini API Key is not configured. Go to AI Studio Secrets panel, insert GEMINI_API_KEY, and restart build.")
                    }
                    return@launch
                }

                val prompt = """
                    You are an intelligent Sri Lankan financial voice assistant. Parse the following spoken command into transaction details.
                    Spoken command: "$commandText"

                    Accurately extract the relevant details into a flat JSON object with these exact keys:
                    - "isIncome": boolean, true if it's income/deposit (e.g., salary, gift received), false if payment/expense (e.g., spent, paid, bought).
                    - "title": string, clear, concise name of merchant/payee or source (e.g. "Keells", "Dine Out", "Bonus", "Salary").
                    - "amount": number, exact transaction cost/amount in Rupees. If not specified, default to 0.0.
                    - "category": string, choose the closest from: "Groceries", "Utilities", "Bills & Rent", "Transport", "Mandatory Debt", "Dine Out", "Cinema & Movies", "Hobbies", "Gifts", "Apparel", "Salary", "Freelance", "Investment Income", "Other".
                    - "bucket": string, standard 50/30/20 category choice. "NEEDS", "WANTS", "SAVINGS", or "INCOME" (if isIncome is true).
                    - "suggestedSubBreakdown": empty array.
                    
                    Return ONLY valid plain JSON. Do NOT wrap it in ```json blocks or return other words. Output must be a parsable JSON string.
                """.trimIndent()

                val jsonRequest = JSONObject()
                val contentsArray = JSONArray()
                val contentObj = JSONObject()
                val partsArray = JSONArray()

                val partText = JSONObject()
                partText.put("text", prompt)
                partsArray.put(partText)

                contentObj.put("parts", partsArray)
                contentsArray.put(contentObj)
                jsonRequest.put("contents", contentsArray)

                val generationConfig = JSONObject()
                val responseFormat = JSONObject()
                responseFormat.put("mimeType", "application/json")
                generationConfig.put("responseFormat", responseFormat)
                jsonRequest.put("generationConfig", generationConfig)

                val requestBodyText = jsonRequest.toString()

                val client = okhttp3.OkHttpClient.Builder()
                    .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .build()

                val request = okhttp3.Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                    .post(requestBodyText.toRequestBody("application/json".toMediaTypeOrNull()))
                    .build()

                val response = client.newCall(request).execute()
                val responseBodyText = response.body?.string()

                if (!response.isSuccessful || responseBodyText.isNullOrBlank()) {
                    launch(kotlinx.coroutines.Dispatchers.Main) {
                        onFailure("Gemini API call failed with code ${response.code}: ${response.message}")
                    }
                    return@launch
                }

                val responseJson = JSONObject(responseBodyText)
                val candidates = responseJson.optJSONArray("candidates")
                val firstCandidate = candidates?.optJSONObject(0)
                val content = firstCandidate?.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                val firstPart = parts?.optJSONObject(0)
                var rawText = firstPart?.optString("text")

                if (rawText.isNullOrBlank()) {
                    launch(kotlinx.coroutines.Dispatchers.Main) {
                        onFailure("Gemini did not return any parseable content.")
                    }
                    return@launch
                }

                rawText = rawText.trim()
                if (rawText.startsWith("```json")) {
                    rawText = rawText.removePrefix("```json")
                } else if (rawText.startsWith("```")) {
                    rawText = rawText.removePrefix("```")
                }
                if (rawText.endsWith("```")) {
                    rawText = rawText.removeSuffix("```")
                }
                rawText = rawText.trim()

                val resultObj = JSONObject(rawText)
                val isIncome = resultObj.optBoolean("isIncome", false)
                val titleRes = resultObj.optString("title", "Voice Entry")
                val amountRes = resultObj.optDouble("amount", 0.0)
                val categoryRes = resultObj.optString("category", "Other")
                var bucketRes = resultObj.optString("bucket", "NEEDS").uppercase()

                if (isIncome) {
                    bucketRes = "INCOME"
                } else if (bucketRes != "NEEDS" && bucketRes != "WANTS" && bucketRes != "SAVINGS") {
                    bucketRes = "NEEDS"
                }

                val finalResult = ScannedBillResult(
                    isIncome = isIncome,
                    title = titleRes,
                    amount = amountRes,
                    category = categoryRes,
                    bucket = bucketRes,
                    suggestedSubBreakdown = emptyList()
                )

                launch(kotlinx.coroutines.Dispatchers.Main) {
                    onSuccess(finalResult)
                }

            } catch (e: Exception) {
                Log.e("LankaBudget", "Error parsing voice command", e)
                launch(kotlinx.coroutines.Dispatchers.Main) {
                    onFailure("Voice Command failed parsing response: ${e.localizedMessage ?: e.message}")
                }
            }
        }
    }

    // Direct multi-modal REST query for parsing bills / statement screenshots / receipts safely
    fun scanBillWithGemini(
        bitmap: android.graphics.Bitmap,
        onSuccess: (ScannedBillResult) -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val apiKey = com.example.BuildConfig.GEMINI_API_KEY
                if (apiKey == "MY_GEMINI_API_KEY" || apiKey.isBlank()) {
                    launch(kotlinx.coroutines.Dispatchers.Main) {
                        onFailure("Gemini API Key is not configured. Go to AI Studio Secrets panel, insert GEMINI_API_KEY, and restart build.")
                    }
                    return@launch
                }

                val base64Image = bitmapToBase64(bitmap)
                
                val prompt = """
                    You are a highly advanced Sri Lankan financial assistant. Parse the provided image which is an SMS transaction notification, printed bill, invoice, or bank statement.
                    Accurately extract the relevant transaction details into a flat JSON object with these exact keys:
                    - "isIncome": boolean, true if it's deposit/income, false if payment/expense.
                    - "title": string, concise name of merchant/payee (e.g. "Keells", "Dialog", "Commercial Bank", "Electricity Board").
                    - "amount": number, exact transaction cost.
                    - "category": string, choose the closest from: "Groceries", "Utilities", "Bills & Rent", "Transport", "Mandatory Debt", "Dine Out", "Cinema & Movies", "Hobbies", "Gifts", "Apparel", "Salary", "Freelance", "Investment Income", "Other".
                    - "bucket": string, standard 50/30/20 category choice. "NEEDS", "WANTS", "SAVINGS", or "INCOME" (if isIncome is true).
                    - "suggestedSubBreakdown": a JSON array of objects, where each object has "title" (string) and "amount" (number) representing itemized prices if visible in the bill, or empty array.
                    
                    Return ONLY valid plain JSON. Do NOT wrap it in ```json blocks or return other words. Output must be a parsable JSON string.
                """.trimIndent()

                // Construct request using standard org.json API
                val jsonRequest = JSONObject()
                val contentsArray = JSONArray()
                val contentObj = JSONObject()
                val partsArray = JSONArray()

                // Text Part
                val partText = JSONObject()
                partText.put("text", prompt)
                partsArray.put(partText)

                // Multimodal Part
                val partImage = JSONObject()
                val inlineDataObj = JSONObject()
                inlineDataObj.put("mimeType", "image/jpeg")
                inlineDataObj.put("data", base64Image)
                partImage.put("inlineData", inlineDataObj)
                partsArray.put(partImage)

                contentObj.put("parts", partsArray)
                contentsArray.put(contentObj)
                jsonRequest.put("contents", contentsArray)

                // Force model to return schema formatted JSON objects through direct instruction and format constraints
                val generationConfig = JSONObject()
                val responseFormat = JSONObject()
                responseFormat.put("mimeType", "application/json")
                generationConfig.put("responseFormat", responseFormat)
                jsonRequest.put("generationConfig", generationConfig)

                val requestBodyText = jsonRequest.toString()

                val client = okhttp3.OkHttpClient.Builder()
                    .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .build()

                val request = okhttp3.Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                    .post(requestBodyText.toRequestBody("application/json".toMediaTypeOrNull()))
                    .build()

                val response = client.newCall(request).execute()
                val responseBodyText = response.body?.string()

                if (!response.isSuccessful || responseBodyText.isNullOrBlank()) {
                    launch(kotlinx.coroutines.Dispatchers.Main) {
                        onFailure("Gemini API call failed with code ${response.code}: ${response.message}")
                    }
                    return@launch
                }

                val responseJson = JSONObject(responseBodyText)
                val candidates = responseJson.optJSONArray("candidates")
                val firstCandidate = candidates?.optJSONObject(0)
                val content = firstCandidate?.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                val firstPart = parts?.optJSONObject(0)
                var rawText = firstPart?.optString("text")

                if (rawText.isNullOrBlank()) {
                    launch(kotlinx.coroutines.Dispatchers.Main) {
                        onFailure("Gemini did not return any parseable content.")
                    }
                    return@launch
                }

                rawText = rawText.trim()
                if (rawText.startsWith("```json")) {
                    rawText = rawText.removePrefix("```json")
                } else if (rawText.startsWith("```")) {
                    rawText = rawText.removePrefix("```")
                }
                if (rawText.endsWith("```")) {
                    rawText = rawText.removeSuffix("```")
                }
                rawText = rawText.trim()

                val resultObj = JSONObject(rawText)
                val isIncome = resultObj.optBoolean("isIncome", false)
                val titleRes = resultObj.optString("title", "Scanned Transaction")
                val amountRes = resultObj.optDouble("amount", 0.0)
                val categoryRes = resultObj.optString("category", "Other")
                var bucketRes = resultObj.optString("bucket", "NEEDS").uppercase()

                // Align input constraints safely code-wise
                if (isIncome) {
                    bucketRes = "INCOME"
                } else if (bucketRes != "NEEDS" && bucketRes != "WANTS" && bucketRes != "SAVINGS") {
                    bucketRes = "NEEDS"
                }

                val breakdownList = mutableListOf<Pair<String, Double>>()
                val subArray = resultObj.optJSONArray("suggestedSubBreakdown")
                if (subArray != null) {
                    for (i in 0 until subArray.length()) {
                        val subObj = subArray.optJSONObject(i)
                        if (subObj != null) {
                            val subTitle = subObj.optString("title")
                            val subAmt = subObj.optDouble("amount", 0.0)
                            if (!subTitle.isNullOrBlank() && subAmt > 0) {
                                breakdownList.add(subTitle to subAmt)
                            }
                        }
                    }
                }

                val finalResult = ScannedBillResult(
                    isIncome = isIncome,
                    title = titleRes,
                    amount = amountRes,
                    category = categoryRes,
                    bucket = bucketRes,
                    suggestedSubBreakdown = breakdownList
                )

                launch(kotlinx.coroutines.Dispatchers.Main) {
                    onSuccess(finalResult)
                }

            } catch (e: Exception) {
                Log.e("LankaBudget", "Error parsing bill image", e)
                launch(kotlinx.coroutines.Dispatchers.Main) {
                    onFailure("Scan failed parsing text: ${e.localizedMessage ?: e.message}")
                }
            }
        }
    }

    // Goals CRUD
    fun addGoal(title: String, targetAmount: Double, targetDate: Long) {
        viewModelScope.launch {
            repository.insertGoal(Goal(title = title, targetAmount = targetAmount, targetDate = targetDate))
        }
    }

    fun deleteGoal(goal: Goal) {
        viewModelScope.launch {
            repository.deleteGoal(goal)
        }
    }

    fun clearAllGoals() {
        viewModelScope.launch {
            repository.clearAllGoals()
        }
    }

    fun addGoalContribution(goalId: Int, amount: Double, title: String) {
        viewModelScope.launch {
            repository.insertGoalContribution(goalId, amount, title)
        }
    }
}
