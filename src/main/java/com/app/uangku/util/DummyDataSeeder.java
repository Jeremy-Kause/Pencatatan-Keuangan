package com.app.uangku.util;

import com.app.uangku.dao.BudgetDAO;
import com.app.uangku.dao.CategoryDAO;
import com.app.uangku.dao.SavingsGoalDAO;
import com.app.uangku.dao.TransactionDAO;
import com.app.uangku.dao.UserDAO;
import com.app.uangku.model.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Menyiapkan data dummy untuk keperluan demo dan testing.
 *
 * <p>Kelas ini <b>tidak mengubah</b> DatabaseHelper maupun file lain yang sudah ada.
 * Ia hanya memanfaatkan DAO yang sudah tersedia untuk menyisipkan:
 * <ul>
 *   <li>Satu akun demo (username: {@code demo}, password: {@code demo123})</li>
 *   <li>Kategori default (dipanggil via {@link CategoryDAO#createDefaultCategoriesForUser})</li>
 *   <li>Transaksi dummy selama 3 bulan terakhir</li>
 *   <li>Budget dummy untuk bulan berjalan</li>
 * </ul>
 *
 * <p><b>Cara pakai:</b>
 * <pre>{@code
 *   // Panggil sekali saat aplikasi pertama kali dijalankan,
 *   // misalnya di HelloApplication.start() atau Launcher.main()
 *   DummyDataSeeder.seedIfEmpty();
 * }</pre>
 *
 * Kelompok 3 – Database, Model, dan DAO
 * Penanggung jawab: Valentino
 */
public final class DummyDataSeeder {

    private static final Logger LOGGER = Logger.getLogger(DummyDataSeeder.class.getName());

    /** Username dan password akun demo yang akan dibuat. */
    public static final String DEMO_USERNAME = "demo";
    public static final String DEMO_EMAIL    = "demo@uangku.app";
    public static final String DEMO_PASSWORD = "demo123";

    private DummyDataSeeder() { /* utility class */ }

    // ─── Entry Point ──────────────────────────────────────────────────────────────

    /**
     * Menyisipkan data dummy jika akun demo belum ada.
     * Aman dipanggil berkali-kali (idempoten).
     */
    public static void seedIfEmpty() {
        try {
            UserDAO userDAO = new UserDAO();
            Optional<User> existing = userDAO.findByUsernameOrEmail(DEMO_USERNAME);
            if (existing.isPresent()) {
                LOGGER.info("Data dummy sudah ada, lewati seeding.");
                return;
            }
            seed(userDAO);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Gagal menjalankan DummyDataSeeder", e);
        }
    }

    // ─── Seeding ──────────────────────────────────────────────────────────────────

    private static void seed(UserDAO userDAO) throws SQLException {
        // 1. Buat akun demo
        User demo = userDAO.register(DEMO_USERNAME, DEMO_EMAIL, DEMO_PASSWORD);
        int uid = demo.getIdUser();
        LOGGER.info("Akun demo dibuat: id=" + uid);

        // 2. Buat kategori default untuk user demo
        CategoryDAO categoryDAO = new CategoryDAO();
        categoryDAO.createDefaultCategoriesForUser(uid);

        // 3. Ambil kategori yang diperlukan
        List<Category> incomeCategories  = categoryDAO.findByUserIdAndType(uid, TransactionType.PEMASUKAN);
        List<Category> expenseCategories = categoryDAO.findByUserIdAndType(uid, TransactionType.PENGELUARAN);

        if (incomeCategories.isEmpty() || expenseCategories.isEmpty()) {
            LOGGER.warning("Kategori default tidak ditemukan, seeding transaksi dibatalkan.");
            return;
        }

        // Cari kategori berdasarkan nama (fallback ke indeks 0 jika tidak ditemukan)
        Category catGaji      = findCategory(incomeCategories,  "Gaji",          0);
        Category catBonus     = findCategory(incomeCategories,  "Bonus",         1);
        Category catMakanan   = findCategory(expenseCategories, "Makanan",       0);
        Category catTransport = findCategory(expenseCategories, "Transportasi",  1);
        Category catBelanja   = findCategory(expenseCategories, "Belanja",       2);
        Category catTagihan   = findCategory(expenseCategories, "Tagihan",       3);
        Category catHiburan   = findCategory(expenseCategories, "Hiburan",       4);

        // 4. Sisipkan transaksi dummy
        TransactionDAO transactionDAO = new TransactionDAO();
        seedTransactions(transactionDAO, uid, catGaji, catBonus,
                catMakanan, catTransport, catBelanja, catTagihan, catHiburan);

        // 5. Sisipkan budget dummy untuk bulan berjalan
        BudgetDAO budgetDAO = new BudgetDAO();
        seedBudgets(budgetDAO, uid, catMakanan, catTransport, catBelanja, catTagihan, catHiburan);

        SavingsGoalDAO savingsGoalDAO = new SavingsGoalDAO();
        seedSavingsGoals(savingsGoalDAO, uid);

        LOGGER.info("Data dummy berhasil disisipkan untuk user: " + DEMO_USERNAME);
    }

    // ─── Transaksi Dummy ──────────────────────────────────────────────────────────

    private static void seedTransactions(
            TransactionDAO dao, int uid,
            Category gaji, Category bonus,
            Category makanan, Category transport,
            Category belanja, Category tagihan, Category hiburan
    ) throws SQLException {

        YearMonth now   = YearMonth.now();
        YearMonth prev1 = now.minusMonths(1);
        YearMonth prev2 = now.minusMonths(2);

        // Helper: tanggal dalam bulan tertentu
        // ── Bulan ini ────────────────────────────────────────────────────────────
        create(dao, uid, gaji,      5_000_000, "Gaji bulan ini",          date(now,   1));
        create(dao, uid, makanan,     320_000, "Makan siang minggu 1",    date(now,   5));
        create(dao, uid, transport,   150_000, "Bensin motor",            date(now,   6));
        create(dao, uid, makanan,     280_000, "Makan siang minggu 2",    date(now,  12));
        create(dao, uid, tagihan,     250_000, "Listrik & internet",      date(now,  13));
        create(dao, uid, belanja,     430_000, "Belanja bulanan",         date(now,  15));
        create(dao, uid, makanan,     310_000, "Makan siang minggu 3",    date(now,  19));
        create(dao, uid, transport,   120_000, "Ojek & parkir",           date(now,  20));
        create(dao, uid, hiburan,     150_000, "Nonton bioskop",          date(now,  21));
        create(dao, uid, makanan,     290_000, "Makan siang minggu 4",    date(now,  26));

        // ── Bulan lalu ───────────────────────────────────────────────────────────
        create(dao, uid, gaji,      5_000_000, "Gaji bulan lalu",         date(prev1,  1));
        create(dao, uid, bonus,       800_000, "Bonus proyek",            date(prev1,  5));
        create(dao, uid, makanan,     340_000, "Makan siang minggu 1",    date(prev1,  6));
        create(dao, uid, transport,   170_000, "Bensin & tol",            date(prev1,  8));
        create(dao, uid, belanja,     510_000, "Belanja kebutuhan rumah", date(prev1, 10));
        create(dao, uid, tagihan,     260_000, "Listrik & air",           date(prev1, 12));
        create(dao, uid, makanan,     290_000, "Makan siang minggu 2",    date(prev1, 14));
        create(dao, uid, hiburan,     200_000, "Konser musik",            date(prev1, 18));
        create(dao, uid, makanan,     310_000, "Makan siang minggu 3",    date(prev1, 21));
        create(dao, uid, transport,   130_000, "Ojek online",             date(prev1, 24));
        create(dao, uid, makanan,     270_000, "Makan siang minggu 4",    date(prev1, 27));

        // ── Dua bulan lalu ───────────────────────────────────────────────────────
        create(dao, uid, gaji,      5_000_000, "Gaji dua bulan lalu",     date(prev2,  1));
        create(dao, uid, makanan,     350_000, "Makan siang minggu 1",    date(prev2,  5));
        create(dao, uid, transport,   160_000, "Bensin motor",            date(prev2,  7));
        create(dao, uid, tagihan,     245_000, "Listrik & internet",      date(prev2, 12));
        create(dao, uid, belanja,     480_000, "Belanja bulanan",         date(prev2, 14));
        create(dao, uid, makanan,     300_000, "Makan siang minggu 2",    date(prev2, 14));
        create(dao, uid, hiburan,     100_000, "Streaming bulanan",       date(prev2, 15));
        create(dao, uid, makanan,     320_000, "Makan siang minggu 3",    date(prev2, 20));
        create(dao, uid, transport,   140_000, "Ojek & parkir",           date(prev2, 22));
        create(dao, uid, makanan,     280_000, "Makan siang minggu 4",    date(prev2, 27));
    }

    private static void create(TransactionDAO dao, int uid, Category cat,
                                double amount, String desc, LocalDate date) throws SQLException {
        dao.create(new Transaction(uid, cat.getIdCategory(), amount, date, desc, cat.getType()));
    }

    // ─── Budget Dummy ─────────────────────────────────────────────────────────────

    private static void seedBudgets(
            BudgetDAO dao, int uid,
            Category makanan, Category transport,
            Category belanja, Category tagihan, Category hiburan
    ) throws SQLException {
        YearMonth month = YearMonth.now();
        setBudget(dao, uid, makanan,    1_500_000, month);
        setBudget(dao, uid, transport,    500_000, month);
        setBudget(dao, uid, belanja,      600_000, month);
        setBudget(dao, uid, tagihan,      300_000, month);
        setBudget(dao, uid, hiburan,      250_000, month);
    }

    private static void setBudget(BudgetDAO dao, int uid,
                                   Category cat, double limit, YearMonth month) throws SQLException {
        dao.setBudget(new Budget(uid, cat.getIdCategory(), limit, month));
    }


    private static void seedSavingsGoals(SavingsGoalDAO dao, int uid) throws SQLException {
        dao.create(new SavingsGoal(
                uid,
                "Laptop Baru",
                8_000_000,
                2_000_000,
                YearMonth.now().plusMonths(6).atEndOfMonth(),
                "Target demo untuk pembelian laptop"
        ));
    }

    // ─── Utilitas ─────────────────────────────────────────────────────────────────

    /**
     * Mencari kategori berdasarkan nama (case-insensitive).
     * Jika tidak ditemukan, kembalikan elemen pada {@code fallbackIndex}.
     */
    private static Category findCategory(List<Category> list, String name, int fallbackIndex) {
        return list.stream()
                .filter(c -> c.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(list.get(Math.min(fallbackIndex, list.size() - 1)));
    }

    /**
     * Membuat {@link LocalDate} dari bulan/tahun dan hari.
     * Jika hari melebihi panjang bulan, dipakai hari terakhir.
     */
    private static LocalDate date(YearMonth ym, int day) {
        return LocalDate.of(ym.getYear(), ym.getMonth(), Math.min(day, ym.lengthOfMonth()));
    }
}
