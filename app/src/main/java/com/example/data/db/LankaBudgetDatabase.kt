package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.Transaction
import com.example.data.model.Loan
import com.example.data.model.RepaymentLog
import com.example.data.model.RecurringTransaction
import com.example.data.model.Investment
import com.example.data.model.BudgetCapEntity
import com.example.data.model.SplitBillEntity
import com.example.data.model.Goal
import com.example.data.model.GoalContribution

@Database(
    entities = [
        Transaction::class, 
        Loan::class, 
        RepaymentLog::class, 
        RecurringTransaction::class, 
        Investment::class, 
        BudgetCapEntity::class, 
        SplitBillEntity::class,
        Goal::class,
        GoalContribution::class
    ],
    version = 8,
    exportSchema = false
)
abstract class LankaBudgetDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun loanDao(): LoanDao
    abstract fun repaymentLogDao(): RepaymentLogDao
    abstract fun recurringTransactionDao(): RecurringTransactionDao
    abstract fun investmentDao(): InvestmentDao
    abstract fun budgetCapDao(): BudgetCapDao
    abstract fun splitBillDao(): SplitBillDao
    abstract fun goalDao(): GoalDao

    companion object {
        @Volatile
        private var INSTANCE: LankaBudgetDatabase? = null
        private val INSTANCES = java.util.concurrent.ConcurrentHashMap<String, LankaBudgetDatabase>()

        fun getDatabase(context: Context, profileName: String = "Personal"): LankaBudgetDatabase {
            val dbName = if (profileName == "Personal") {
                "lanka_budget_database"
            } else {
                "lanka_budget_database_${profileName.lowercase().trim().replace("\\s+".toRegex(), "_")}"
            }
            return INSTANCES[dbName] ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LankaBudgetDatabase::class.java,
                    dbName
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCES[dbName] = instance
                if (profileName == "Personal") {
                    INSTANCE = instance
                }
                instance
            }
        }
    }
}
