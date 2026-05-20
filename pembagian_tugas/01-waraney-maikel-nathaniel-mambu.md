# Waraney Maikel Nathaniel Mambu

## Peran Utama

Backend Controller dan validasi input JavaFX.

## Fokus Pekerjaan

Waraney bertanggung jawab membuat controller yang menghubungkan tampilan FXML dengan logic aplikasi. Fokus utamanya adalah event handling, validasi input, pesan error/sukses, dan memastikan setiap halaman bisa memanggil service atau DAO yang sesuai.

## File/Package yang Dikerjakan

```text
src/main/java/com/app/uangku/controller/AuthController.java
src/main/java/com/app/uangku/controller/DashboardController.java
src/main/java/com/app/uangku/controller/TransactionController.java
src/main/java/com/app/uangku/controller/CategoryController.java
src/main/java/com/app/uangku/controller/BudgetController.java
src/main/java/com/app/uangku/controller/ReportController.java
```

## Checklist Tugas

- [ ] Membuat package `controller`.
- [ ] Membuat `AuthController` untuk login dan register.
- [ ] Membuat validasi login:
  - username/email wajib diisi.
  - password wajib diisi.
  - tampilkan pesan jika akun tidak ditemukan atau password salah.
- [ ] Membuat validasi register:
  - username wajib diisi.
  - email wajib diisi dan formatnya valid.
  - password wajib diisi.
  - username/email tidak boleh duplikat.
- [ ] Membuat `DashboardController` untuk menampilkan:
  - total saldo.
  - total pemasukan bulan berjalan.
  - total pengeluaran bulan berjalan.
  - transaksi terbaru.
- [ ] Membuat `TransactionController` untuk:
  - tambah transaksi pemasukan.
  - tambah transaksi pengeluaran.
  - edit transaksi.
  - hapus transaksi.
  - filter transaksi berdasarkan tanggal, tipe, kategori, dan kata kunci.
- [ ] Membuat validasi transaksi:
  - nominal harus lebih dari 0.
  - tanggal wajib dipilih.
  - kategori wajib dipilih.
  - tipe transaksi wajib dipilih.
- [ ] Membuat `CategoryController` untuk:
  - tambah kategori pemasukan.
  - tambah kategori pengeluaran.
  - hapus kategori.
  - memuat daftar kategori berdasarkan user login.
- [ ] Membuat `BudgetController` untuk:
  - set budget per kategori pengeluaran.
  - update budget.
  - menampilkan status budget.
- [ ] Membuat `ReportController` untuk:
  - menampilkan data pie chart pengeluaran.
  - menampilkan kategori pengeluaran terbesar.
  - menampilkan total pengeluaran bulan berjalan.
- [ ] Menambahkan pesan sukses/error menggunakan `Alert` atau label status di halaman.
- [ ] Memastikan nama method controller sesuai dengan `onAction` di file FXML.

## Ketergantungan dengan Anggota Lain

- Membutuhkan FXML dari Delvin.
- Membutuhkan DAO dan model dari Valentino.
- Membutuhkan `SceneManager`, `SessionManager`, dan integrasi service dari Jeremy.

## Output yang Harus Diserahkan

- Semua controller berhasil dikompilasi.
- Tombol dan form di FXML terhubung ke method controller.
- Validasi input berjalan dan tidak membuat aplikasi crash.
- Data dari form bisa diteruskan ke service/DAO.

## Kriteria Selesai

- Login/register bisa memberikan respons berhasil/gagal.
- Form transaksi, kategori, budget, dan laporan memiliki controller aktif.
- Semua error input ditangani dengan pesan yang jelas.
- Aplikasi tetap bisa dijalankan dengan `.\mvnw.cmd clean javafx:run`.
