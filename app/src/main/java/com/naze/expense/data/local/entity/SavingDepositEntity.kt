package com.naze.expense.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Setoran/tabungan untuk sebuah goal. */
@Entity(
    tableName = "saving_deposits",
    foreignKeys = [
        ForeignKey(
            entity = SavingGoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["goalId"],
            onDelete = ForeignKey.CASCADE, // goal dihapus -> setoran ikut terhapus
        )
    ],
    indices = [Index("goalId"), Index("date")],
)
data class SavingDepositEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val goalId: Long,
    val amount: Long,            // satuan terkecil (rupiah)
    val date: Long = System.currentTimeMillis(),
)
