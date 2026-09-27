package com.naze.expense.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Target menabung (misal: "Beli laptop"). */
@Entity(tableName = "saving_goals")
data class SavingGoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val targetAmount: Long,     // satuan terkecil (rupiah)
    val deadline: Long,          // epoch millis — untuk hitung mundur hari
    val photoPath: String? = null, // foto target (disimpan di internal filesDir)
    val createdAt: Long = System.currentTimeMillis(),
)
