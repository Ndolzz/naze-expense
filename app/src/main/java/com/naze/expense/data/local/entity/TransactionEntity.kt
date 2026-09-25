package com.naze.expense.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT, // tidak bisa hapus kategori yang masih dipakai
        )
    ],
    indices = [Index("categoryId"), Index("date")],
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val amount: Long,          // satuan terkecil (rupiah)
    val type: String,          // INCOME / EXPENSE
    val categoryId: Long,
    val note: String? = null,
    val date: Long,             // epoch millis
)
