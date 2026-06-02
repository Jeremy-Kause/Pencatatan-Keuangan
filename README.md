# UangKu - Aplikasi Pencatatan Keuangan

UangKu adalah aplikasi desktop berbasis Java dan JavaFX untuk membantu pengguna mencatat pemasukan, pengeluaran, kategori transaksi, anggaran bulanan, serta melihat ringkasan dan laporan visual keuangan pribadi.

Dokumen dasar perancangan: `Laporan_Perancangan_App_PrakRPL.pdf`.

## Tujuan Aplikasi

1. Membantu pengguna mencatat transaksi keuangan secara rapi dan terorganisir.
2. Menampilkan ringkasan saldo, pemasukan, dan pengeluaran bulan berjalan.
3. Membantu pengguna mengevaluasi kebiasaan belanja melalui laporan visual.
4. Membantu pengguna mengontrol pengeluaran dengan fitur budget bulanan.
5. Menerapkan konsep RPLBO/OOP melalui pemisahan View, Controller, Model, DAO, dan Utility.

## Fitur Utama

- Login dan register pengguna.
- Dashboard ringkasan total saldo, total pemasukan, total pengeluaran, dan transaksi terbaru.
- Pencatatan pemasukan dengan nominal, tanggal, kategori, sumber, dan deskripsi.
- Pencatatan pengeluaran dengan nominal, tanggal, kategori, dan deskripsi.
- Manajemen kategori kustom untuk pemasukan dan pengeluaran.
- Set budget bulanan per kategori pengeluaran.
- Target tabungan dengan progress bar berdasarkan saldo saat ini atau surplus bulanan.
- Riwayat transaksi dengan pencarian dan filter berdasarkan tanggal, tipe, dan kategori.
- Laporan visual menggunakan Pie Chart untuk persentase pengeluaran berdasarkan kategori.

## Teknologi

- Java
- JavaFX
- FXML
- CSS
- Maven
- SQLite
- JDBC
- JavaFX Chart API
- BCrypt atau library hashing sejenis untuk keamanan password

Catatan project saat ini:

- Project sudah menggunakan Maven Wrapper (`mvnw.cmd`).
- `pom.xml` saat ini memakai JavaFX `21.0.6`.
- `pom.xml` saat ini mengatur `source` dan `target` ke Java `25`. Jika komputer anggota belum memakai JDK 25, ubah ke JDK yang disepakati, misalnya `21`.

## Struktur Package yang Disarankan

Gunakan package utama project saat ini, yaitu:

```text
com.app.uangku
```

Struktur yang disarankan:

```text
src/main/java/com/app/pencatatan_keuangan/
+-- Launcher.java
+-- MainApplication.java
+-- controller/
|   +-- AuthController.java
|   +-- DashboardController.java
|   +-- TransactionController.java
|   +-- CategoryController.java
|   +-- BudgetController.java
|   +-- ReportController.java
+-- dao/
|   +-- UserDAO.java
|   +-- CategoryDAO.java
|   +-- TransactionDAO.java
|   +-- BudgetDAO.java
+-- model/
|   +-- User.java
|   +-- Category.java
|   +-- Transaction.java
|   +-- Budget.java
+-- service/
|   +-- AuthService.java
|   +-- DashboardService.java
|   +-- BudgetService.java
+-- util/
    +-- DatabaseHelper.java
    +-- PasswordUtil.java
    +-- SessionManager.java
    +-- SceneManager.java

src/main/resources/com/app/pencatatan_keuangan/
+-- fxml/
|   +-- login.fxml
|   +-- register.fxml
|   +-- dashboard.fxml
|   +-- transactions.fxml
|   +-- categories.fxml
|   +-- budgets.fxml
|   +-- reports.fxml
+-- css/
    +-- app.css
```

## Rancangan Database

Database menggunakan SQLite lokal, misalnya file:

```text
uangku.db
```

Tabel utama:

```sql
CREATE TABLE IF NOT EXISTS users (
    id_user INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS categories (
    id_category INTEGER PRIMARY KEY AUTOINCREMENT,
    id_user INTEGER NOT NULL,
    name TEXT NOT NULL,
    type TEXT NOT NULL CHECK (type IN ('PEMASUKAN', 'PENGELUARAN')),
    FOREIGN KEY (id_user) REFERENCES users(id_user)
);

CREATE TABLE IF NOT EXISTS transactions (
    id_transaction INTEGER PRIMARY KEY AUTOINCREMENT,
    id_user INTEGER NOT NULL,
    id_category INTEGER NOT NULL,
    amount REAL NOT NULL,
    date TEXT NOT NULL,
    description TEXT,
    type TEXT NOT NULL CHECK (type IN ('PEMASUKAN', 'PENGELUARAN')),
    FOREIGN KEY (id_user) REFERENCES users(id_user),
    FOREIGN KEY (id_category) REFERENCES categories(id_category)
);

CREATE TABLE IF NOT EXISTS budgets (
    id_budget INTEGER PRIMARY KEY AUTOINCREMENT,
    id_user INTEGER NOT NULL,
    id_category INTEGER NOT NULL,
    limit_amount REAL NOT NULL,
    month_year TEXT NOT NULL,
    FOREIGN KEY (id_user) REFERENCES users(id_user),
    FOREIGN KEY (id_category) REFERENCES categories(id_category)
);

CREATE TABLE IF NOT EXISTS savings_goals (
    id_goal INTEGER PRIMARY KEY AUTOINCREMENT,
    id_user INTEGER NOT NULL,
    name TEXT NOT NULL,
    target_amount REAL NOT NULL,
    target_date TEXT,
    progress_source TEXT NOT NULL CHECK (progress_source IN ('BALANCE', 'MONTHLY_SURPLUS')),
    description TEXT,
    created_at TEXT NOT NULL DEFAULT (date('now')),
    FOREIGN KEY (id_user) REFERENCES users(id_user)
);
```

## Langkah-Langkah Membangun Aplikasi

### 1. Persiapan Project

1. Samakan versi JDK pada semua anggota.
2. Pastikan project bisa dijalankan dengan `.\mvnw.cmd clean javafx:run`.
3. Tambahkan dependency yang dibutuhkan:
   - `org.xerial:sqlite-jdbc`
   - library BCrypt, misalnya `org.mindrot:jbcrypt`
   - ControlsFX/JFoenix jika ingin komponen UI tambahan.
4. Rapikan class awal dari template JavaFX, misalnya ganti `HelloApplication` menjadi `MainApplication`.

### 2. Fondasi Arsitektur

1. Buat package `model`, `dao`, `controller`, `service`, dan `util`.
2. Buat `SceneManager` untuk pindah halaman.
3. Buat `SessionManager` untuk menyimpan user yang sedang login.
4. Buat `DatabaseHelper` untuk koneksi SQLite dan inisialisasi tabel.

### 3. Model

1. Buat class `User`.
2. Buat class `Category`.
3. Buat class `Transaction`.
4. Buat class `Budget`.
5. Gunakan tipe data yang sesuai, misalnya `LocalDate` untuk tanggal dan `double`/`BigDecimal` untuk nominal.

### 4. Database dan DAO

1. Implementasikan inisialisasi database di `DatabaseHelper`.
2. Implementasikan `UserDAO`:
   - register user
   - login user
   - cek username/email sudah terpakai
3. Implementasikan `CategoryDAO`:
   - tambah kategori
   - hapus kategori
   - ambil kategori berdasarkan user dan tipe
4. Implementasikan `TransactionDAO`:
   - tambah transaksi
   - ubah transaksi
   - hapus transaksi
   - ambil riwayat transaksi
   - filter transaksi
   - hitung total pemasukan/pengeluaran
   - ambil data chart
5. Implementasikan `BudgetDAO`:
   - set budget
   - update budget
   - ambil budget bulan berjalan
   - hitung pemakaian budget per kategori

### 5. Autentikasi

1. Buat halaman `login.fxml`.
2. Buat halaman `register.fxml`.
3. Hubungkan dengan `AuthController`.
4. Hash password saat register.
5. Verifikasi password saat login.
6. Setelah login berhasil, simpan user aktif di `SessionManager` dan arahkan ke dashboard.

### 6. Dashboard

1. Buat tampilan kartu total saldo, pemasukan, dan pengeluaran.
2. Ambil data dari `TransactionDAO` atau `DashboardService`.
3. Tampilkan transaksi terbaru.
4. Pastikan dashboard diperbarui setelah transaksi baru disimpan.

### 7. Transaksi

1. Buat form tambah transaksi.
2. Buat tabel riwayat transaksi.
3. Tambahkan filter berdasarkan:
   - tanggal
   - kategori
   - tipe pemasukan/pengeluaran
   - kata kunci deskripsi
4. Tambahkan validasi:
   - nominal wajib lebih dari 0
   - kategori wajib dipilih
   - tanggal wajib diisi

### 8. Kategori

1. Buat halaman kategori pemasukan dan pengeluaran.
2. Tambahkan form tambah kategori.
3. Tampilkan kategori dalam list/grid.
4. Cegah penghapusan kategori jika masih dipakai transaksi, atau tampilkan konfirmasi yang jelas.

### 9. Budget Bulanan

1. Buat halaman budget per kategori pengeluaran.
2. Buat form set budget untuk bulan tertentu.
3. Hitung total pengeluaran kategori pada periode budget.
4. Tampilkan status budget:
   - aman
   - mendekati limit
   - melebihi limit

### 10. Laporan Visual

1. Buat halaman laporan.
2. Gunakan JavaFX `PieChart`.
3. Ambil data pengeluaran per kategori dari `TransactionDAO`.
4. Tampilkan statistik tambahan seperti kategori pengeluaran terbesar dan total pengeluaran bulan berjalan.

### 11. Styling UI

1. Buat file `app.css`.
2. Terapkan warna, spacing, typography, dan style tombol yang konsisten.
3. Pastikan semua halaman mengikuti wireframe:
   - Login
   - Register
   - Dashboard
   - Transaksi
   - Kategori
   - Budget
   - Laporan

### 12. Testing

1. Test register dan login.
2. Test tambah, edit, hapus, dan filter transaksi.
3. Test tambah dan hapus kategori.
4. Test perhitungan saldo.
5. Test budget per bulan.
6. Test laporan pie chart.
7. Test data setiap user terpisah dan tidak tercampur.

### 13. Finalisasi

1. Bersihkan class/template yang tidak dipakai.
2. Pastikan tidak ada data sensitif masuk repository.
3. Tambahkan data dummy untuk demo jika diperlukan.
4. Siapkan skenario demo:
   - register user
   - login
   - tambah kategori
   - tambah pemasukan
   - tambah pengeluaran
   - set budget
   - lihat dashboard dan laporan

## Pembagian Tugas 4 Orang

| Anggota | NIM | Peran Utama | Tanggung Jawab |
|---|---:|---|---|
| Waraney Maikel Nathaniel Mambu | 71241164 | Backend Controller | Controller JavaFX, validasi input, event handling, alur dashboard, transaksi, kategori, budget, dan laporan |
| Delvin Laurens | 71241097 | Frontend / UI Designer | FXML, CSS, layout halaman, komponen UI, TableView, form, kartu dashboard, dan konsistensi desain |
| Valentino Kevin Yulianto | 71241126 | Database | SQLite schema, `DatabaseHelper`, DAO, query CRUD, query filter, query summary, dan data dummy |
| Jeremy Zadrimman Kause | 71241163 | Backend Integration | Integrasi View-Controller-DAO, navigasi scene, dependency Maven, session login, testing end-to-end, dan packaging |

## Kelompok Tugas Berdasarkan Perancangan

### Kelompok 1: UI / View

Penanggung jawab utama: Delvin

- Membuat `login.fxml` dan `register.fxml`.
- Membuat `dashboard.fxml`.
- Membuat `transactions.fxml`.
- Membuat `categories.fxml`.
- Membuat `budgets.fxml`.
- Membuat `reports.fxml`.
- Membuat `app.css`.
- Menyesuaikan tampilan dengan wireframe Figma.

### Kelompok 2: Controller dan Validasi

Penanggung jawab utama: Waraney

- Membuat `AuthController`.
- Membuat `DashboardController`.
- Membuat `TransactionController`.
- Membuat `CategoryController`.
- Membuat `BudgetController`.
- Membuat `ReportController`.
- Menangani validasi input dari user.
- Menampilkan pesan error dan sukses.

### Kelompok 3: Database, Model, dan DAO

Penanggung jawab utama: Valentino

- Membuat model `User`, `Category`, `Transaction`, dan `Budget`.
- Membuat `DatabaseHelper`.
- Membuat tabel SQLite.
- Membuat `UserDAO`, `CategoryDAO`, `TransactionDAO`, dan `BudgetDAO`.
- Membuat query summary, filter, dan chart.
- Menyiapkan data awal kategori default jika diperlukan.

### Kelompok 4: Integrasi, Testing, dan Build

Penanggung jawab utama: Jeremy

- Mengatur navigasi antar halaman.
- Menghubungkan FXML dengan controller.
- Menghubungkan controller dengan service/DAO.
- Mengelola session user login.
- Mengatur dependency di `pom.xml`.
- Melakukan testing alur aplikasi dari awal sampai akhir.
- Memastikan aplikasi bisa dijalankan dengan Maven.

## Milestone Pengerjaan

| Minggu | Target | Output |
|---|---|---|
| 1 | Setup project dan struktur package | Project bisa run, package rapi, dependency siap |
| 2 | Database, model, DAO dasar, login/register | Autentikasi berjalan dan data user tersimpan |
| 3 | Kategori dan transaksi | CRUD kategori dan transaksi berjalan |
| 4 | Dashboard dan budget | Ringkasan saldo dan budget berfungsi |
| 5 | Laporan visual dan filter | Pie chart, filter transaksi, statistik laporan |
| 6 | Testing, styling, bug fixing, demo | Aplikasi stabil dan siap presentasi |

## Aturan Kerja Tim

1. Setiap anggota membuat branch sesuai tugas, misalnya `feature/login-ui` atau `feature/transaction-dao`.
2. Hindari mengerjakan file yang sama secara bersamaan tanpa koordinasi.
3. Commit kecil dan jelas, misalnya `Add TransactionDAO insert query`.
4. Sebelum merge, jalankan aplikasi dan pastikan tidak ada error.
5. Jika ada perubahan schema database, informasikan ke semua anggota.

## Referensi Desain

- Figma: https://www.figma.com/design/4esEJqdFFhlxNZsQv1GSct/Untitled?node-id=0-1&t=VenqfzA1wSZwysM0-1
- Dokumen: https://docs.google.com/document/d/1Y-zJYlPx4kNugqctRs2FwwAsN2izI1do/edit
