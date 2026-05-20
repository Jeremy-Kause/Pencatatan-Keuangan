# Valentino Kevin Yulianto

## Peran Utama

Database, model, DAO, dan query SQLite.

## Fokus Pekerjaan

Valentino bertanggung jawab membuat struktur data aplikasi. Fokus utamanya adalah model class, koneksi SQLite, inisialisasi tabel, CRUD database, query filter, query summary dashboard, dan query data laporan.

## File/Package yang Dikerjakan

```text
src/main/java/com/app/uangku/model/User.java
src/main/java/com/app/uangku/model/Category.java
src/main/java/com/app/uangku/model/Transaction.java
src/main/java/com/app/uangku/model/Budget.java
src/main/java/com/app/uangku/dao/UserDAO.java
src/main/java/com/app/uangku/dao/CategoryDAO.java
src/main/java/com/app/uangku/dao/TransactionDAO.java
src/main/java/com/app/uangku/dao/BudgetDAO.java
src/main/java/com/app/uangku/util/DatabaseHelper.java
```

## Checklist Tugas

- [ ] Membuat package `model`, `dao`, dan `util`.
- [ ] Membuat model `User` dengan field:
  - `idUser`
  - `username`
  - `password`
  - `email`
- [ ] Membuat model `Category` dengan field:
  - `idCategory`
  - `idUser`
  - `name`
  - `type`
- [ ] Membuat model `Transaction` dengan field:
  - `idTransaction`
  - `idUser`
  - `idCategory`
  - `amount`
  - `date`
  - `description`
  - `type`
- [ ] Membuat model `Budget` dengan field:
  - `idBudget`
  - `idUser`
  - `idCategory`
  - `limitAmount`
  - `monthYear`
- [ ] Membuat enum atau konstanta tipe transaksi:
  - `PEMASUKAN`
  - `PENGELUARAN`
- [ ] Menambahkan dependency SQLite JDBC di `pom.xml` bersama Jeremy.
- [ ] Membuat `DatabaseHelper` untuk:
  - membuka koneksi SQLite.
  - membuat file database `uangku.db`.
  - membuat tabel `users`, `categories`, `transactions`, dan `budgets`.
  - menjalankan `PRAGMA foreign_keys = ON`.
- [ ] Membuat `UserDAO`:
  - `insertUser`.
  - `findByUsernameOrEmail`.
  - `isUsernameExists`.
  - `isEmailExists`.
- [ ] Membuat `CategoryDAO`:
  - tambah kategori.
  - hapus kategori.
  - ambil kategori berdasarkan user.
  - ambil kategori berdasarkan user dan tipe.
  - cek kategori masih dipakai transaksi.
- [ ] Membuat `TransactionDAO`:
  - tambah transaksi.
  - update transaksi.
  - hapus transaksi.
  - ambil semua transaksi user.
  - ambil transaksi terbaru.
  - filter transaksi berdasarkan tanggal, tipe, kategori, dan kata kunci.
  - hitung total pemasukan.
  - hitung total pengeluaran.
  - hitung saldo.
  - ambil total pengeluaran per kategori untuk pie chart.
- [ ] Membuat `BudgetDAO`:
  - tambah budget.
  - update budget.
  - ambil budget bulan tertentu.
  - hitung pemakaian budget per kategori.
  - cek budget sudah ada untuk kategori dan bulan tertentu.
- [ ] Menyiapkan kategori default jika dibutuhkan:
  - Gaji.
  - Makanan.
  - Transportasi.
  - Belanja.
  - Hiburan.

## Ketergantungan dengan Anggota Lain

- DAO akan dipakai controller Waraney.
- Data model akan ditampilkan di FXML Delvin.
- Koneksi database dan dependency Maven perlu disinkronkan dengan Jeremy.

## Output yang Harus Diserahkan

- Model class lengkap dengan constructor, getter, dan setter.
- `DatabaseHelper` bisa membuat database dan tabel.
- DAO bisa menjalankan CRUD dasar.
- Query summary dan laporan bisa dipakai dashboard/report.

## Kriteria Selesai

- Database SQLite terbentuk otomatis saat aplikasi dijalankan.
- Register user bisa menyimpan data ke tabel `users`.
- Kategori, transaksi, dan budget bisa disimpan dan dibaca kembali.
- Query total saldo, pemasukan, pengeluaran, dan pie chart menghasilkan data benar.
- Aplikasi tetap bisa dijalankan dengan `.\mvnw.cmd clean javafx:run`.
