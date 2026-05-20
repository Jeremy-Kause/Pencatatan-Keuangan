# Jeremy Zadrimman Kause

## Peran Utama

Backend integration, navigasi aplikasi, session, testing, dan build.

## Fokus Pekerjaan

Jeremy bertanggung jawab menyatukan pekerjaan anggota lain agar aplikasi berjalan end-to-end. Fokus utamanya adalah konfigurasi Maven, struktur package final, navigasi antar halaman, session user login, integrasi controller-service-DAO, testing manual, dan persiapan demo.

## File/Package yang Dikerjakan

```text
pom.xml
src/main/java/module-info.java
src/main/java/com/app/uangku/MainApplication.java
src/main/java/com/app/uangku/Launcher.java
src/main/java/com/app/uangku/service/AuthService.java
src/main/java/com/app/uangku/service/DashboardService.java
src/main/java/com/app/uangku/service/BudgetService.java
src/main/java/com/app/uangku/util/SceneManager.java
src/main/java/com/app/uangku/util/SessionManager.java
src/main/java/com/app/uangku/util/PasswordUtil.java
```

## Checklist Tugas

- [ ] Menyamakan versi JDK project dengan anggota lain.
- [ ] Merapikan template awal:
  - ganti `HelloApplication` menjadi `MainApplication`.
  - ganti `HelloController` dengan controller asli.
  - hapus atau abaikan `hello-view.fxml` setelah halaman baru siap.
- [ ] Mengatur `pom.xml`:
  - JavaFX dependency.
  - SQLite JDBC dependency.
  - BCrypt dependency.
  - konfigurasi main class JavaFX.
  - source/target Java sesuai JDK yang disepakati.
- [ ] Mengatur `module-info.java`:
  - `requires javafx.controls`.
  - `requires javafx.fxml`.
  - `requires java.sql`.
  - module/dependency BCrypt jika dipakai.
  - `opens` package controller/model yang dibutuhkan JavaFX.
- [ ] Membuat `MainApplication`:
  - inisialisasi database saat aplikasi start.
  - memuat halaman login pertama kali.
  - mengatur title aplikasi menjadi `UangKu`.
- [ ] Membuat `SceneManager` untuk:
  - pindah ke login.
  - pindah ke register.
  - pindah ke dashboard.
  - pindah ke transaksi.
  - pindah ke kategori.
  - pindah ke budget.
  - pindah ke laporan.
- [ ] Membuat `SessionManager` untuk:
  - menyimpan user yang sedang login.
  - mengambil user aktif.
  - logout dan menghapus session.
- [ ] Membuat `PasswordUtil` untuk:
  - hash password saat register.
  - verifikasi password saat login.
- [ ] Membuat `AuthService`:
  - validasi register tingkat service.
  - cek username/email.
  - hash password.
  - proses login.
- [ ] Membuat `DashboardService`:
  - ambil total saldo.
  - ambil pemasukan/pengeluaran bulan berjalan.
  - ambil transaksi terbaru.
- [ ] Membuat `BudgetService`:
  - hitung status budget aman/mendekati/melebihi limit.
  - menyiapkan data untuk tampilan budget.
- [ ] Menghubungkan controller dengan service dan DAO.
- [ ] Melakukan testing end-to-end:
  - register.
  - login.
  - tambah kategori.
  - tambah pemasukan.
  - tambah pengeluaran.
  - set budget.
  - lihat dashboard.
  - lihat laporan pie chart.
- [ ] Menyiapkan data dummy untuk demo jika diperlukan.
- [ ] Menulis catatan cara menjalankan aplikasi di README jika ada perubahan.

## Ketergantungan dengan Anggota Lain

- Mengintegrasikan FXML dari Delvin.
- Mengintegrasikan controller dari Waraney.
- Mengintegrasikan model/DAO/database dari Valentino.

## Output yang Harus Diserahkan

- Aplikasi bisa dibuka dari halaman login.
- Navigasi antar halaman berjalan.
- Session user aktif tersimpan setelah login.
- Controller bisa memakai service dan DAO.
- Project bisa dijalankan menggunakan Maven Wrapper.

## Kriteria Selesai

- Perintah `.\mvnw.cmd clean javafx:run` berhasil membuka aplikasi.
- Alur demo dari register sampai laporan berjalan tanpa error.
- Password user tidak disimpan dalam bentuk plain text.
- Data setiap user terpisah dan tidak tercampur.
- README berisi instruksi menjalankan aplikasi yang sesuai kondisi project terbaru.
