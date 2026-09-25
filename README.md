# Naze Expense

Aplikasi pencatat keuangan pribadi untuk anak muda: cepat, sederhana, modern, dan **100% offline**.

## Teknologi
- Kotlin + Jetpack Compose (Material 3)
- Room + SQLite (database lokal)
- DataStore (pengaturan)
- Arsitektur MVVM (ViewModel + Repository)

## Prinsip
- Offline-first — tidak ada fitur online
- Tidak ada akun/login
- Semua data tersimpan lokal di perangkat

## Fitur V1
- Dashboard (saldo, pemasukan/pengeluaran bulan berjalan)
- Transaksi (tambah/edit/hapus pemasukan & pengeluaran)
- Budget per kategori (progress bar)
- Statistik (harian, mingguan, bulanan, distribusi kategori)
- Riwayat + filter (tanggal, kategori, jenis)
- Settings (mata uang, tema, kelola kategori, backup/restore JSON lokal)
