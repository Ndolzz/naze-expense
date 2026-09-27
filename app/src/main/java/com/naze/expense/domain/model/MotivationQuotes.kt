package com.naze.expense.domain.model

import java.util.Calendar

/** Kutipan penyemangat menabung — berganti otomatis setiap hari. */
object MotivationQuotes {

    private val quotes = listOf(
        "Tabungan kecil hari ini, kebebasan besar esok hari.",
        "Tidak ada jalan pintas. Menabung adalah doa yang dikerjakan.",
        "Satu rupiah yang disimpan hari ini lebih berharga daripada seribu yang dijanjikan esok.",
        "Kaya bukan soal penghasilan, tapi sisa yang berhasil kamu simpan.",
        "Boros itu mudah, menabung itu keren.",
        "Setiap setor 1.000, kamu membeli sedikit masa depan.",
        "Sabar itu pahit, tapi buahnya manis — seperti tabungan.",
        "Jangan menunggu kaya untuk menabung. Menabunglah untuk jadi kaya.",
        "Impian tanpa tabungan hanya angan-angan.",
        "Disiplin hari ini, senyum hari esok.",
        "Uang yang kamu simpan adalah waktu masa depan yang kamu beli.",
        "Mulai kecil, mulai sekarang, mulai dari sini.",
        "Setiap hari adalah kesempatan untuk satu langkah lebih dekat.",
        "Orang sukses bukan yang penghasilannya besar, tapi yang tangannya tidak gatal.",
        "Hitung mundurnya bukan beban — itu pengingat bahwa targetmu nyata.",
        "Lebih baik capek atur uang sekarang daripada capek cari utang nanti.",
        "Foto targetmu itu bukan hiasan. Itu kontrak dengan dirimu sendiri.",
        "Konsisten mengalahkan besaran. 1.000 tiap hari tetap gunung.",
        "Hari ini kamu menabung, besok tabunganmu yang menolongmu.",
        "Semua mimpi butuh modal. Kumpulkan sekarang.",
    )

    /** Quote hari ini — berganti otomatis tiap hari (aman untuk minSdk 24). */
    fun today(): String {
        val cal = Calendar.getInstance()
        val day = cal.get(Calendar.DAY_OF_YEAR) + cal.get(Calendar.YEAR) * 366
        return quotes[((day % quotes.size) + quotes.size) % quotes.size]
    }
}
