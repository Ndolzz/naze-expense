package com.naze.expense.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,          // unik
    val icon: String,
    val colorHex: String,
    val isDefault: Boolean = false,
    val type: String,          // INCOME / EXPENSE
)
