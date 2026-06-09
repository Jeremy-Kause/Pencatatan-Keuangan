package com.app.uangku.controller;

import com.app.uangku.dao.TransactionDAO;
import com.app.uangku.model.Transaction;
import com.app.uangku.model.User;
import com.app.uangku.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class ReportController extends BaseWireframeController {
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    private ComboBox<YearMonth> reportMonthComboBox;

    @FXML
    private PieChart expensePieChart;

    @FXML
    private Label reportTotalIncomeLabel;

    @FXML
    private Label reportTotalExpenseLabel;

    @FXML
    private Label reportBalanceLabel;

    @FXML
    private Label topCategoryLabel;

    @FXML
    private Label topCategoryPercentLabel;

    @FXML
    private Label transactionCountLabel;

    @FXML
    private Label reportNoteLabel;

    @FXML
    private Label exportStatusLabel;

    @FXML
    private void initialize() {
        YearMonth current = YearMonth.now();
        reportMonthComboBox.setItems(FXCollections.observableArrayList(
                current,
                current.minusMonths(1),
                current.minusMonths(2),
                current.minusMonths(3),
                current.minusMonths(4),
                current.minusMonths(5)
        ));
        reportMonthComboBox.setValue(current);
        reportMonthComboBox.valueProperty().addListener((observable, oldValue, newValue) -> loadReport());
        loadReport();
    }

    private void loadReport() {
        if (!hasActiveSession()) {
            expensePieChart.setData(FXCollections.emptyObservableList());
            setMessage(reportTotalIncomeLabel, formatCurrency(0));
            setMessage(reportTotalExpenseLabel, formatCurrency(0));
            setMessage(reportBalanceLabel, formatCurrency(0));
            setMessage(topCategoryLabel, "Belum ada data");
            setMessage(topCategoryPercentLabel, "Login diperlukan");
            setMessage(transactionCountLabel, "0 transaksi");
            setErrorMessage(reportNoteLabel, "Silakan login untuk melihat laporan.");
            return;
        }

        try {
            User user = SessionManager.getCurrentUser().orElseThrow();
            YearMonth month = reportMonthComboBox.getValue();
            Map<String, Double> expenseByCategory = transactionDAO.getExpenseByCategory(user.getIdUser(), month);
            double totalExpense = expenseByCategory.values().stream().mapToDouble(Double::doubleValue).sum();

            expensePieChart.setData(FXCollections.observableArrayList(
                    expenseByCategory.entrySet().stream()
                            .map(entry -> new PieChart.Data(entry.getKey(), entry.getValue()))
                            .toList()
            ));
            setMessage(reportTotalExpenseLabel, formatCurrency(totalExpense));
            setTopCategory(expenseByCategory, totalExpense);

            List<Transaction> transactions = transactionDAO.filter(
                    user.getIdUser(),
                    month.atDay(1),
                    month.atEndOfMonth(),
                    null, null, null
            );

            double totalPemasukan = transactions.stream()
                    .filter(t -> t.getType() == com.app.uangku.model.TransactionType.PEMASUKAN)
                    .mapToDouble(Transaction::getAmount).sum();
            double totalPengeluaran = transactions.stream()
                    .filter(t -> t.getType() == com.app.uangku.model.TransactionType.PENGELUARAN)
                    .mapToDouble(Transaction::getAmount).sum();
            double selisih = totalPemasukan - totalPengeluaran;

            setMessage(reportTotalIncomeLabel, formatCurrency(totalPemasukan));
            setMessage(reportTotalExpenseLabel, formatCurrency(totalExpense));
            setMessage(reportBalanceLabel, formatCurrency(selisih));

            if (reportBalanceLabel != null) {
                reportBalanceLabel.getStyleClass().removeAll("metric-value-income", "metric-value-danger");
                reportBalanceLabel.getStyleClass().add(selisih >= 0 ? "metric-value-income" : "metric-value-danger");
            }

            setMessage(transactionCountLabel, transactions.size() + " transaksi");
            clearMessage(reportNoteLabel);
            setMessage(reportNoteLabel, "Laporan bulan " + formatMonth(month));
        } catch (SQLException exception) {
            setErrorMessage(reportNoteLabel, "Gagal memuat laporan: " + exception.getMessage());
        }
    }

    private void setTopCategory(Map<String, Double> expenseByCategory, double totalExpense) {
        if (expenseByCategory.isEmpty() || totalExpense <= 0) {
            setMessage(topCategoryLabel, "Belum ada pengeluaran");
            setMessage(topCategoryPercentLabel, "0% dari total pengeluaran");
            return;
        }

        Map.Entry<String, Double> top = expenseByCategory.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElseThrow();
        double percentage = (top.getValue() / totalExpense) * 100;
        setMessage(topCategoryLabel, top.getKey());
        setMessage(topCategoryPercentLabel, String.format("%.1f%% dari total pengeluaran", percentage));
    }



    // ─── Export Excel ─────────────────────────────────────────────────────────

    @FXML
    private void exportToExcel() {
        if (!hasActiveSession()) {
            setErrorMessage(exportStatusLabel, "Silakan login terlebih dahulu.");
            return;
        }

        YearMonth month = reportMonthComboBox.getValue();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Simpan Laporan Excel");
        fileChooser.setInitialFileName("laporan_" + month.toString() + ".xlsx");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files (*.xlsx)", "*.xlsx")
        );

        File file = fileChooser.showSaveDialog(reportMonthComboBox.getScene().getWindow());
        if (file == null) return;

        try {
            User user = SessionManager.getCurrentUser().orElseThrow();
            List<Transaction> transactions = transactionDAO.filter(
                    user.getIdUser(),
                    month.atDay(1),
                    month.atEndOfMonth(),
                    null,
                    null,
                    null
            );
            Map<String, Double> expenseByCategory = transactionDAO.getExpenseByCategory(user.getIdUser(), month);

            writeExcel(file, transactions, expenseByCategory, month);
            setSuccessMessage(exportStatusLabel, "Berhasil disimpan: " + file.getName());
        } catch (SQLException e) {
            setErrorMessage(exportStatusLabel, "Gagal mengambil data: " + e.getMessage());
        } catch (IOException e) {
            setErrorMessage(exportStatusLabel, "Gagal menulis file: " + e.getMessage());
        }
    }

    private void writeExcel(File file, List<Transaction> transactions, Map<String, Double> expenseByCategory, YearMonth month) throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {

            // ── Style helpers ──
            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerFont.setFontName("Arial");
            headerFont.setFontHeightInPoints((short) 11);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_TEAL.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFontWhite = wb.createFont();
            headerFontWhite.setBold(true);
            headerFontWhite.setFontName("Arial");
            headerFontWhite.setFontHeightInPoints((short) 11);
            headerFontWhite.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFontWhite);

            CellStyle titleStyle = wb.createCellStyle();
            Font titleFont = wb.createFont();
            titleFont.setBold(true);
            titleFont.setFontName("Arial");
            titleFont.setFontHeightInPoints((short) 13);
            titleStyle.setFont(titleFont);

            CellStyle labelStyle = wb.createCellStyle();
            Font labelFont = wb.createFont();
            labelFont.setBold(true);
            labelFont.setFontName("Arial");
            labelStyle.setFont(labelFont);

            CellStyle rupiahStyle = wb.createCellStyle();
            DataFormat fmt = wb.createDataFormat();
            rupiahStyle.setDataFormat(fmt.getFormat("#,##0"));

            CellStyle altRowStyle = wb.createCellStyle();
            altRowStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
            altRowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle altRowRupiahStyle = wb.createCellStyle();
            altRowRupiahStyle.cloneStyleFrom(altRowStyle);
            altRowRupiahStyle.setDataFormat(fmt.getFormat("#,##0"));

            // ════════════════════════════════════════════
            // Sheet 1: Riwayat Transaksi
            // ════════════════════════════════════════════
            Sheet sheetTx = wb.createSheet("Riwayat Transaksi");
            sheetTx.setColumnWidth(0, 4000);   // Tanggal
            sheetTx.setColumnWidth(1, 4500);   // Tipe
            sheetTx.setColumnWidth(2, 6000);   // Kategori
            sheetTx.setColumnWidth(3, 5000);   // Nominal
            sheetTx.setColumnWidth(4, 10000);  // Deskripsi

            // Title
            Row titleRow = sheetTx.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Laporan Keuangan UangKu — " + formatMonth(month));
            titleCell.setCellStyle(titleStyle);
            sheetTx.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));

            // Ringkasan
            double totalPemasukan = transactions.stream()
                    .filter(t -> t.getType() == com.app.uangku.model.TransactionType.PEMASUKAN)
                    .mapToDouble(Transaction::getAmount).sum();
            double totalPengeluaran = transactions.stream()
                    .filter(t -> t.getType() == com.app.uangku.model.TransactionType.PENGELUARAN)
                    .mapToDouble(Transaction::getAmount).sum();

            String[][] summary = {
                    {"Total Transaksi", String.valueOf(transactions.size())},
                    {"Total Pemasukan", null},
                    {"Total Pengeluaran", null},
                    {"Selisih", null}
            };
            double[] summaryValues = {0, totalPemasukan, totalPengeluaran, totalPemasukan - totalPengeluaran};

            for (int i = 0; i < summary.length; i++) {
                Row r = sheetTx.createRow(2 + i);
                Cell lbl = r.createCell(0);
                lbl.setCellValue(summary[i][0]);
                lbl.setCellStyle(labelStyle);
                Cell val = r.createCell(1);
                if (i == 0) {
                    val.setCellValue(transactions.size());
                } else {
                    val.setCellValue(summaryValues[i]);
                    val.setCellStyle(rupiahStyle);
                }
            }

            // Header kolom
            int headerRow = 7;
            Row colHeader = sheetTx.createRow(headerRow);
            String[] cols = {"Tanggal", "Tipe", "Kategori", "Nominal", "Deskripsi"};
            for (int i = 0; i < cols.length; i++) {
                Cell c = colHeader.createCell(i);
                c.setCellValue(cols[i]);
                c.setCellStyle(headerStyle);
            }

            // Data transaksi
            int rowNum = headerRow + 1;
            for (Transaction t : transactions) {
                Row row = sheetTx.createRow(rowNum);
                boolean alt = (rowNum % 2 == 0);

                Cell cDate = row.createCell(0);
                cDate.setCellValue(t.getDate().format(DATE_FORMAT));
                if (alt) cDate.setCellStyle(altRowStyle);

                Cell cType = row.createCell(1);
                cType.setCellValue(t.getType().getDisplayName());
                if (alt) cType.setCellStyle(altRowStyle);

                Cell cCat = row.createCell(2);
                cCat.setCellValue(t.getCategoryName() != null ? t.getCategoryName() : "");
                if (alt) cCat.setCellStyle(altRowStyle);

                Cell cAmt = row.createCell(3);
                cAmt.setCellValue(t.getAmount());
                cAmt.setCellStyle(alt ? altRowRupiahStyle : rupiahStyle);

                Cell cDesc = row.createCell(4);
                cDesc.setCellValue(t.getDescription() != null ? t.getDescription() : "");
                if (alt) cDesc.setCellStyle(altRowStyle);

                rowNum++;
            }

            // Total row
            if (!transactions.isEmpty()) {
                Row totalRow = sheetTx.createRow(rowNum + 1);
                Cell totalLabel = totalRow.createCell(2);
                totalLabel.setCellValue("TOTAL");
                totalLabel.setCellStyle(labelStyle);
                Cell totalVal = totalRow.createCell(3);
                totalVal.setCellFormula("SUM(D" + (headerRow + 2) + ":D" + rowNum + ")");
                totalVal.setCellStyle(rupiahStyle);
            }

            // ════════════════════════════════════════════
            // Sheet 2: Pengeluaran per Kategori
            // ════════════════════════════════════════════
            Sheet sheetCat = wb.createSheet("Per Kategori");
            sheetCat.setColumnWidth(0, 7000);
            sheetCat.setColumnWidth(1, 5000);
            sheetCat.setColumnWidth(2, 4000);

            Row catTitle = sheetCat.createRow(0);
            Cell catTitleCell = catTitle.createCell(0);
            catTitleCell.setCellValue("Pengeluaran per Kategori — " + formatMonth(month));
            catTitleCell.setCellStyle(titleStyle);
            sheetCat.addMergedRegion(new CellRangeAddress(0, 0, 0, 2));

            Row catHeader = sheetCat.createRow(2);
            String[] catCols = {"Kategori", "Nominal", "Persentase"};
            for (int i = 0; i < catCols.length; i++) {
                Cell c = catHeader.createCell(i);
                c.setCellValue(catCols[i]);
                c.setCellStyle(headerStyle);
            }

            CellStyle pctStyle = wb.createCellStyle();
            pctStyle.setDataFormat(fmt.getFormat("0.0%"));

            CellStyle altPctStyle = wb.createCellStyle();
            altPctStyle.cloneStyleFrom(altRowStyle);
            altPctStyle.setDataFormat(fmt.getFormat("0.0%"));

            int catRow = 3;
            int catDataStart = catRow + 1;
            for (Map.Entry<String, Double> entry : expenseByCategory.entrySet()) {
                Row row = sheetCat.createRow(catRow);
                boolean alt = (catRow % 2 == 0);

                Cell cName = row.createCell(0);
                cName.setCellValue(entry.getKey());
                if (alt) cName.setCellStyle(altRowStyle);

                Cell cAmt = row.createCell(1);
                cAmt.setCellValue(entry.getValue());
                cAmt.setCellStyle(alt ? altRowRupiahStyle : rupiahStyle);

                Cell cPct = row.createCell(2);
                int excelRow = catRow + 1;
                int totalStart = catDataStart;
                int totalEnd = catDataStart + expenseByCategory.size() - 1;
                cPct.setCellFormula("IF(SUM(B" + totalStart + ":B" + totalEnd
                        + ")=0,0,B" + excelRow + "/SUM(B" + totalStart + ":B" + totalEnd + "))");
                cPct.setCellStyle(alt ? altPctStyle : pctStyle);

                catRow++;
            }

            // Total kategori
            if (!expenseByCategory.isEmpty()) {
                Row totRow = sheetCat.createRow(catRow + 1);
                Cell totLbl = totRow.createCell(0);
                totLbl.setCellValue("TOTAL");
                totLbl.setCellStyle(labelStyle);
                Cell totAmt = totRow.createCell(1);
                totAmt.setCellFormula("SUM(B" + catDataStart + ":B" + catRow + ")");
                totAmt.setCellStyle(rupiahStyle);
            }

            try (FileOutputStream fos = new FileOutputStream(file)) {
                wb.write(fos);
            }
        }
    }
}
