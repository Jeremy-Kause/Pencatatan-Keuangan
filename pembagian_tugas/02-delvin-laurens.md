# Delvin Laurens

## Peran Utama

Frontend, FXML, dan desain antarmuka JavaFX.

## Fokus Pekerjaan

Delvin bertanggung jawab membuat tampilan aplikasi sesuai rancangan. Fokus utamanya adalah layout halaman, komponen JavaFX, CSS, konsistensi warna, spacing, dan memastikan setiap elemen penting memiliki `fx:id` atau `onAction` agar bisa dipakai controller.

## File/Package yang Dikerjakan

```text
src/main/resources/com/app/uangku/fxml/login.fxml
src/main/resources/com/app/uangku/fxml/register.fxml
src/main/resources/com/app/uangku/fxml/dashboard.fxml
src/main/resources/com/app/uangku/fxml/transactions.fxml
src/main/resources/com/app/uangku/fxml/categories.fxml
src/main/resources/com/app/uangku/fxml/budgets.fxml
src/main/resources/com/app/uangku/fxml/reports.fxml
src/main/resources/com/app/uangku/css/app.css
```

## Checklist Tugas

- [ ] Membuat folder `fxml` dan `css` di resources.
- [ ] Membuat `login.fxml` berisi:
  - input username/email.
  - input password.
  - tombol login.
  - link/tombol ke halaman register.
  - label pesan error.
- [ ] Membuat `register.fxml` berisi:
  - input username.
  - input email.
  - input password.
  - tombol register.
  - tombol kembali ke login.
  - label pesan error.
- [ ] Membuat `dashboard.fxml` berisi:
  - kartu total saldo.
  - kartu total pemasukan.
  - kartu total pengeluaran.
  - tabel/list transaksi terbaru.
  - navigasi ke transaksi, kategori, budget, dan laporan.
- [ ] Membuat `transactions.fxml` berisi:
  - form tambah/edit transaksi.
  - pilihan tipe pemasukan/pengeluaran.
  - pilihan kategori.
  - input nominal.
  - date picker tanggal.
  - input deskripsi.
  - tabel riwayat transaksi.
  - filter tanggal, tipe, kategori, dan kata kunci.
- [ ] Membuat `categories.fxml` berisi:
  - form tambah kategori.
  - pilihan tipe kategori.
  - daftar kategori pemasukan.
  - daftar kategori pengeluaran.
- [ ] Membuat `budgets.fxml` berisi:
  - form set budget.
  - pilihan kategori pengeluaran.
  - input nominal limit.
  - pilihan bulan/tahun.
  - tabel/list status budget.
- [ ] Membuat `reports.fxml` berisi:
  - `PieChart` pengeluaran per kategori.
  - ringkasan total pengeluaran.
  - kategori pengeluaran terbesar.
  - filter bulan/tahun jika sempat.
- [ ] Membuat `app.css` untuk:
  - warna utama aplikasi.
  - style tombol.
  - style form.
  - style kartu dashboard.
  - style tabel.
  - spacing dan typography yang konsisten.
- [ ] Memastikan setiap input penting punya `fx:id`.
- [ ] Memastikan setiap tombol penting punya `onAction`.
- [ ] Memastikan `fx:controller` mengarah ke controller yang dibuat Waraney.

## Ketergantungan dengan Anggota Lain

- Perlu koordinasi nama controller dan method dengan Waraney.
- Perlu data yang akan ditampilkan dari DAO/model Valentino.
- Perlu format navigasi dan scene dari Jeremy.

## Output yang Harus Diserahkan

- Semua halaman FXML tersedia.
- CSS diterapkan ke semua halaman.
- Tampilan sudah rapi dan siap dihubungkan ke controller.
- Tidak ada error saat file FXML dimuat.

## Kriteria Selesai

- Login, register, dashboard, transaksi, kategori, budget, dan laporan punya tampilan masing-masing.
- Semua komponen input, tombol, tabel, dan chart sudah tersedia.
- Nama `fx:id` konsisten dan mudah dipakai controller.
- Aplikasi tetap bisa dijalankan dengan `.\mvnw.cmd clean javafx:run`.
