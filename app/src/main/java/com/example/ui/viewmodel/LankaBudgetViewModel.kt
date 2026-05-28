package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.LankaBudgetDatabase
import com.example.data.model.Transaction
import com.example.data.model.Loan
import com.example.data.model.RepaymentLog
import com.example.data.model.RecurringTransaction
import com.example.data.model.Investment
import com.example.data.repository.LankaBudgetRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileWriter

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

class LankaBudgetViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPrefs: SharedPreferences = application.getSharedPreferences("lanka_budget_prefs", Context.MODE_PRIVATE)
    
    val activeProfile = MutableStateFlow(sharedPrefs.getString("active_profile", "Personal") ?: "Personal")
    
    val profilesList = MutableStateFlow<List<String>>(
        sharedPrefs.getStringSet("profiles_list", setOf("Personal"))?.toList()?.sorted() ?: listOf("Personal")
    )

    // Salary tracking configuration StateFlows
    val salaryDayOfMonth = MutableStateFlow(sharedPrefs.getInt("salary_day", 25))
    val predictedSalaryAmount = MutableStateFlow(sharedPrefs.getFloat("predicted_salary", 300000.0f).toDouble())

    fun updateSalaryDay(day: Int) {
        sharedPrefs.edit().putInt("salary_day", day).apply()
        salaryDayOfMonth.value = day
    }

    fun updateSalaryAmount(amount: Double) {
        sharedPrefs.edit().putFloat("predicted_salary", amount.toFloat()).apply()
        predictedSalaryAmount.value = amount
    }

    fun getSalaryDatesAndPredictions(): Triple<String, String, Double> {
        val txs = transactions.value
        val lastSalaryTx = txs.filter { it.isIncome && it.title.lowercase().contains("salary") }
            .maxByOrNull { it.timestamp }

        val sdf = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
        val lastPaidDateMillis: Long
        val predictedAmt = if (lastSalaryTx != null) lastSalaryTx.amount else predictedSalaryAmount.value

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

    val budgetSummaryState: StateFlow<BudgetSummary>
    val totalDebtBalance: StateFlow<Double>
    val availableBalance: StateFlow<Double>
    val totalInvestmentBalance: StateFlow<Double>
    val netWorth: StateFlow<Double>

    init {
        val initProfile = sharedPrefs.getString("active_profile", "Personal") ?: "Personal"
        val database = LankaBudgetDatabase.getDatabase(application, initProfile)
        repository = LankaBudgetRepository(
            transactionDao = database.transactionDao(),
            loanDao = database.loanDao(),
            repaymentLogDao = database.repaymentLogDao(),
            recurringTransactionDao = database.recurringTransactionDao(),
            investmentDao = database.investmentDao()
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
        budgetSummaryState = transactions
            .map { txList ->
                val totalIncome = txList.filter { it.isIncome }.sumOf { it.amount }

                val needsBudget = totalIncome * 0.50
                val wantsBudget = totalIncome * 0.30
                val savingsBudget = totalIncome * 0.20

                val needsExpenses = txList.filter { !it.isIncome && it.bucket == "NEEDS" }.sumOf { it.amount }
                val wantsExpenses = txList.filter { !it.isIncome && it.bucket == "WANTS" }.sumOf { it.amount }
                val savingsExpenses = txList.filter { !it.isIncome && it.bucket == "SAVINGS" }.sumOf { it.amount }

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
    fun addIncome(title: String, amount: Double, category: String) {
        viewModelScope.launch {
            repository.insertTransaction(
                Transaction(
                    title = title,
                    amount = amount,
                    isIncome = true,
                    bucket = "INCOME",
                    category = category
                )
            )
        }
    }

    fun addExpense(title: String, amount: Double, bucket: String, category: String) {
        viewModelScope.launch {
            repository.insertTransaction(
                Transaction(
                    title = title,
                    amount = amount,
                    isIncome = false,
                    bucket = bucket,
                    category = category
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

    fun addRecurringExpense(title: String, amount: Double, bucket: String, category: String, recurrencePeriod: String, startDate: Long) {
        viewModelScope.launch {
            repository.insertRecurringTransaction(
                RecurringTransaction(
                    title = title,
                    amount = amount,
                    isIncome = false,
                    bucket = bucket,
                    category = category,
                    recurrencePeriod = recurrencePeriod,
                    startDate = startDate
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

    // Dynamic Multi-Profile Management
    fun switchProfile(profileName: String) {
        viewModelScope.launch {
            val database = LankaBudgetDatabase.getDatabase(getApplication(), profileName)
            repository.updateDaos(
                newTransactionDao = database.transactionDao(),
                newLoanDao = database.loanDao(),
                newRepaymentLogDao = database.repaymentLogDao(),
                newRecurringTransactionDao = database.recurringTransactionDao(),
                newInvestmentDao = database.investmentDao()
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
                                timestamp = jo.getLong("timestamp")
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
        val backupJson = exportBackupAsJsonString() ?: return false
        return try {
            val backupDir = File(getApplication<Application>().filesDir, "backups")
            if (!backupDir.exists()) backupDir.mkdirs()
            
            val filename = "lankabudget_backup_${activeProfile.value.lowercase().trim().replace("\\s+".toRegex(), "_")}_${System.currentTimeMillis()}.json"
            val file = File(backupDir, filename)
            file.writeText(backupJson)
            true
        } catch (e: Exception) {
            Log.e("LankaBudgetViewModel", "Error saving local backup: $e")
            false
        }
    }

    fun getLocalBackups(): List<String> {
        return try {
            val backupDir = File(getApplication<Application>().filesDir, "backups")
            if (!backupDir.exists()) return emptyList()
            
            val list = backupDir.listFiles() ?: return emptyList()
            val safeNamePrefix = "lankabudget_backup_${activeProfile.value.lowercase().trim().replace("\\s+".toRegex(), "_")}"
            list.filter { it.name.startsWith(safeNamePrefix) }
                .map { it.name }
                .sortedDescending()
        } catch (e: Exception) {
            Log.e("LankaBudgetViewModel", "Error getting local backups: $e")
            emptyList()
        }
    }

    fun restoreFromLocalBackup(filename: String): Boolean {
        return try {
            val backupDir = File(getApplication<Application>().filesDir, "backups")
            val file = File(backupDir, filename)
            if (!file.exists()) return false
            
            val jsonStr = file.readText()
            importBackupFromJsonString(jsonStr)
        } catch (e: Exception) {
            Log.e("LankaBudgetViewModel", "Error restoring local backup: $e")
            false
        }
    }
}
