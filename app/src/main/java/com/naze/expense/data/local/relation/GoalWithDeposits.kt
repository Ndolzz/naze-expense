package com.naze.expense.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.naze.expense.data.local.entity.SavingDepositEntity
import com.naze.expense.data.local.entity.SavingGoalEntity

data class GoalWithDeposits(
    @Embedded val goal: SavingGoalEntity,
    @Relation(parentColumn = "id", entityColumn = "goalId")
    val deposits: List<SavingDepositEntity>,
) {
    val saved: Long get() = deposits.sumOf { it.amount }
}
