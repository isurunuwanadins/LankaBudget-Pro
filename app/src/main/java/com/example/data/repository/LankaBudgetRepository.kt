package com.example.data.repository

import com.example.data.db.TransactionDao
import com.example.data.db.LoanDao
import com.example.data.db.RepaymentLogDao
import com.example.data.db.RecurringTransactionDao
import com.example.data.db.InvestmentDao
import com.example.data.model.Transaction
import com.example.data.model.Loan
import com.example.data.model.RepaymentLog
import com.example.data.model.RecurringTransaction
import com.example.data.model.Investment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import java.util.Calendar
import java.util.TimeZone

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class LankaBudgetRepository(
    private var transactionDao: TransactionDao,
    private var loanDao: LoanDao,
    private var repaymentLogDao: RepaymentLogDao,
    private var recurringTransactionDao: RecurringTransactionDao,
    private var investmentDao: InvestmentDao
) {
    private val daoUpdateSignal = MutableStateFlow(0)

    fun updateDaos(
        newTransactionDao: TransactionDao,
        newLoanDao: LoanDao,
        newRepaymentLogDao: RepaymentLogDao,
        newRecurringTransactionDao: RecurringTransactionDao,
        newInvestmentDao: InvestmentDao
    ) {
        this.transactionDao = newTransactionDao
        this.loanDao = newLoanDao
        this.repaymentLogDao = newRepaymentLogDao
        this.recurringTransactionDao = newRecurringTransactionDao
        this.investmentDao = newInvestmentDao
        daoUpdateSignal.value++
    }

    val allTransactions: Flow<List<Transaction>> = daoUpdateSignal.flatMapLatest {
        transactionDao.getAllTransactions()
    }
    
    val allLoans: Flow<List<Loan>> = daoUpdateSignal.flatMapLatest {
        loanDao.getAllLoans()
    }
    
    val allRepayments: Flow<List<RepaymentLog>> = daoUpdateSignal.flatMapLatest {
        repaymentLogDao.getAllRepayments()
    }
    
    val allRecurringTransactions: Flow<List<RecurringTransaction>> = daoUpdateSignal.flatMapLatest {
        recurringTransactionDao.getAllRecurringTransactions()
    }
    
    val allInvestments: Flow<List<Investment>> = daoUpdateSignal.flatMapLatest {
        investmentDao.getAllInvestments()
    }

    private fun getCalendarMidnight(timestamp: Long): Calendar {
        val cal = Calendar.getInstance(TimeZone.getDefault())
        cal.timeInMillis = timestamp
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal
    }

    private fun getCalendarCurrentMidnight(): Calendar {
        val cal = Calendar.getInstance(TimeZone.getDefault())
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal
    }

    // Recurring Generation Engine
    suspend fun generateRecurringPayments() {
        val recurringList = recurringTransactionDao.getRecurringTransactionsList()
        val todayCal = getCalendarCurrentMidnight()

        for (rec in recurringList) {
            val startCal = getCalendarMidnight(rec.startDate)
            val lastGenCal = if (rec.lastGeneratedDate > 0L) getCalendarMidnight(rec.lastGeneratedDate) else null

            val currentOccurrence = if (lastGenCal != null) {
                val nextOcc = lastGenCal.clone() as Calendar
                when (rec.recurrencePeriod) {
                    "DAILY" -> nextOcc.add(Calendar.DAY_OF_YEAR, 1)
                    "WEEKLY" -> nextOcc.add(Calendar.WEEK_OF_YEAR, 1)
                    "MONTHLY" -> nextOcc.add(Calendar.MONTH, 1)
                    "YEARLY" -> nextOcc.add(Calendar.YEAR, 1)
                    else -> nextOcc.add(Calendar.DAY_OF_YEAR, 1)
                }
                nextOcc
            } else {
                startCal
            }

            val newTransactions = mutableListOf<Transaction>()
            var latestGenCal: Calendar? = lastGenCal

            while (!currentOccurrence.after(todayCal)) {
                newTransactions.add(
                    Transaction(
                        title = rec.title,
                        amount = rec.amount,
                        isIncome = rec.isIncome,
                        bucket = rec.bucket,
                        category = rec.category,
                        timestamp = currentOccurrence.timeInMillis
                    )
                )
                latestGenCal = currentOccurrence.clone() as Calendar
                when (rec.recurrencePeriod) {
                    "DAILY" -> currentOccurrence.add(Calendar.DAY_OF_YEAR, 1)
                    "WEEKLY" -> currentOccurrence.add(Calendar.WEEK_OF_YEAR, 1)
                    "MONTHLY" -> currentOccurrence.add(Calendar.MONTH, 1)
                    "YEARLY" -> currentOccurrence.add(Calendar.YEAR, 1)
                    else -> currentOccurrence.add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            if (newTransactions.isNotEmpty() && latestGenCal != null) {
                for (tx in newTransactions) {
                    transactionDao.insertTransaction(tx)
                }
                recurringTransactionDao.updateRecurringTransaction(
                    rec.copy(lastGeneratedDate = latestGenCal.timeInMillis)
                )
            }
        }
    }

    // Recurring API
    suspend fun insertRecurringTransaction(recurring: RecurringTransaction) {
        recurringTransactionDao.insertRecurringTransaction(recurring)
        generateRecurringPayments()
    }

    suspend fun updateRecurringTransaction(recurring: RecurringTransaction) {
        recurringTransactionDao.updateRecurringTransaction(recurring)
    }

    suspend fun deleteRecurringTransaction(recurring: RecurringTransaction) {
        recurringTransactionDao.deleteRecurringTransaction(recurring)
    }

    suspend fun clearAllRecurringTransactions() {
        recurringTransactionDao.clearAllRecurringTransactions()
    }

    // Transaction functions
    suspend fun insertTransaction(transaction: Transaction) {
        transactionDao.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun clearAllTransactions() {
        transactionDao.clearAllTransactions()
    }

    // Loan functions
    suspend fun insertLoan(loan: Loan) {
        loanDao.insertLoan(loan)
    }

    suspend fun updateLoan(loan: Loan) {
        loanDao.updateLoan(loan)
    }

    suspend fun deleteLoan(loan: Loan) {
        // First delete repayments associated with it
        repaymentLogDao.deleteRepaymentsForLoan(loan.id)
        loanDao.deleteLoan(loan)
    }

    suspend fun clearAllLoans() {
        repaymentLogDao.clearAllRepayments()
        loanDao.clearAllLoans()
    }

    // Repayment functions
    suspend fun payInstallment(loanId: Int, amount: Double) {
        val loan = loanDao.getLoanById(loanId) ?: return
        val newRemaining = (loan.remainingAmount - amount).coerceAtLeast(0.0)
        val isCleared = newRemaining <= 0.0
        val updatedLoan = loan.copy(remainingAmount = newRemaining, isCleared = isCleared)
        
        loanDao.updateLoan(updatedLoan)
        repaymentLogDao.insertRepayment(
            RepaymentLog(
                loanId = loanId,
                amountPaid = amount
            )
        )
    }

    // Investment functions
    suspend fun insertInvestment(investment: Investment) {
        investmentDao.insertInvestment(investment)
    }

    suspend fun updateInvestment(investment: Investment) {
        investmentDao.updateInvestment(investment)
    }

    suspend fun deleteInvestment(investment: Investment) {
        investmentDao.deleteInvestment(investment)
    }

    suspend fun clearAllInvestments() {
        investmentDao.clearAllInvestments()
    }
}
