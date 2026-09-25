package com.naze.expense.domain.model

data class Category(
    val id: Long = 0L,
    val name: String,
    val icon: String,
    val colorHex: String,
    val isDefault: Boolean = false,
    val type: TransactionType = TransactionType.EXPENSE,
)

/** Kategori bawaan (di-seed saat pertama kali app dibuka). */
val DEFAULT_CATEGORIES = listOf(
    // Pengeluaran
    Category(name = "Makanan", icon = "restaurant", colorHex = "#F4511E", isDefault = true, type = TransactionType.EXPENSE),
    Category(name = "Transportasi", icon = "directions_bus", colorHex = "#1E88E5", isDefault = true, type = TransactionType.EXPENSE),
    Category(name = "Belanja", icon = "shopping_bag", colorHex = "#8E24AA", isDefault = true, type = TransactionType.EXPENSE),
    Category(name = "Hiburan", icon = "movie", colorHex = "#00897B", isDefault = true, type = TransactionType.EXPENSE),
    Category(name = "Tagihan", icon = "receipt_long", colorHex = "#6D4C41", isDefault = true, type = TransactionType.EXPENSE),
    Category(name = "Kesehatan", icon = "favorite", colorHex = "#E53935", isDefault = true, type = TransactionType.EXPENSE),
    Category(name = "Pendidikan", icon = "school", colorHex = "#3949AB", isDefault = true, type = TransactionType.EXPENSE),
    Category(name = "Lainnya", icon = "category", colorHex = "#757575", isDefault = true, type = TransactionType.EXPENSE),
    // Pemasukan
    Category(name = "Gaji", icon = "payments", colorHex = "#43A047", isDefault = true, type = TransactionType.INCOME),
    Category(name = "Bonus", icon = "card_giftcard", colorHex = "#FB8C00", isDefault = true, type = TransactionType.INCOME),
    Category(name = "Pemasukan Lain", icon = "savings", colorHex = "#7CB342", isDefault = true, type = TransactionType.INCOME),
)
