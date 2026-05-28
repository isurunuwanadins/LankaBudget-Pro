package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.LankaBudgetDatabase
import com.example.data.db.TransactionDao
import com.example.data.db.RecurringTransactionDao
import com.example.data.model.RecurringTransaction
import com.example.data.repository.LankaBudgetRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RecurringSchedulesUnitTests {

    private lateinit var db: LankaBudgetDatabase
    private lateinit var repository: LankaBudgetRepository
    private lateinit var transactionDao: TransactionDao
    private lateinit var recurringDao: RecurringTransactionDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, LankaBudgetDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        transactionDao = db.transactionDao()
        recurringDao = db.recurringTransactionDao()
        
        repository = LankaBudgetRepository(
            transactionDao = transactionDao,
            loanDao = db.loanDao(),
            repaymentLogDao = db.repaymentLogDao(),
            recurringTransactionDao = recurringDao,
            investmentDao = db.investmentDao()
        )
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testRecurringTransactionInsertion() = runBlocking {
        // Assert start state
        val startSchedules = repository.allRecurringTransactions.first()
        assertTrue(startSchedules.isEmpty())

        // Insert daily schedule starting 5 days ago
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -5)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val epochMilli = cal.timeInMillis

        val dailySchedule = RecurringTransaction(
            title = "Daily Tea Expense",
            amount = 150.0,
            isIncome = false,
            bucket = "WANTS",
            category = "Dine Out",
            recurrencePeriod = "DAILY",
            startDate = epochMilli,
            lastGeneratedDate = 0L
        )

        repository.insertRecurringTransaction(dailySchedule)

        // Verify rule is inserted
        val activeSchedules = repository.allRecurringTransactions.first()
        assertEquals(1, activeSchedules.size)
        assertEquals("DAILY", activeSchedules[0].recurrencePeriod)

        // Verify catch-up generation ran automatically!
        // Should generate occurrences for: today - 5, today - 4, today - 3, today - 2, today - 1, today (6 occurrences!)
        val transactionsLogged = repository.allTransactions.first()
        assertEquals(6, transactionsLogged.size)

        // Verify all generated transaction parameters
        transactionsLogged.forEach { tx ->
            assertEquals("Daily Tea Expense", tx.title)
            assertEquals(150.0, tx.amount, 0.0)
            assertEquals("WANTS", tx.bucket)
            assertFalse(tx.isIncome)
        }
    }
}
