package com.example.data.db

import androidx.room.*
import com.example.data.model.RepaymentLog
import kotlinx.coroutines.flow.Flow

@Dao
interface RepaymentLogDao {
    @Query("SELECT * FROM repayment_logs WHERE loanId = :loanId ORDER BY timestamp DESC")
    fun getRepaymentsForLoan(loanId: Int): Flow<List<RepaymentLog>>

    @Query("SELECT * FROM repayment_logs ORDER BY timestamp DESC")
    fun getAllRepayments(): Flow<List<RepaymentLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepayment(repaymentLog: RepaymentLog)

    @Query("DELETE FROM repayment_logs WHERE loanId = :loanId")
    suspend fun deleteRepaymentsForLoan(loanId: Int)

    @Query("DELETE FROM repayment_logs")
    suspend fun clearAllRepayments()
}
