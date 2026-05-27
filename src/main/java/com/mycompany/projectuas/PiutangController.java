package com.mycompany.projectuas;

import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class PiutangController implements Initializable {

    // ── Sidebar ───────────────────────────────────────────
    @FXML
    private VBox sidebar;
    @FXML
    private HBox logoRow;
    @FXML
    private VBox logoBrand;
    @FXML
    private VBox userInfo;
    @FXML
    private HBox userRow;
    @FXML
    private Button toggleBtn;
    @FXML
    private VBox navMenu;

    // Nav items
    @FXML
    private HBox navDashboard;
    @FXML
    private HBox navProduk;
    @FXML
    private HBox navKasir;
    @FXML
    private HBox navPelanggan;
    @FXML
    private HBox navLaporan;
    @FXML
    private HBox navPiutang;
    @FXML
    private HBox navPengaturan;

    // Nav labels (semua label teks nav)
    @FXML
    private Label navLblDashboard;
    @FXML
    private Label navLblProduk;
    @FXML
    private Label navLblKasir;
    @FXML
    private Label navLblPelanggan;
    @FXML
    private Label navLblLaporan;
    @FXML
    private Label navLblPiutang;
    @FXML
    private Label navLblPengaturan;

    // ── Topbar ───────────────────────────────────────────
    @FXML
    private Label kpiTotalPiutang;

    @FXML
    private Label kpiJumlahTransaksi;

    @FXML
    private Label kpiHariIni;

    @FXML
    private Label lblDeltaPiutang;

    @FXML
    private Label lblDeltaTransaksi;

    @FXML
    private Label lblDeltaHariIni;

    // ── FXML refs ──────────────────────────────────
    @FXML
    private TextField tfPelanggan;
    @FXML
    private TableView<DataHutang> tableHutang;
    @FXML
    private TableColumn<DataHutang, String> colNo;
    @FXML
    private TableColumn<DataHutang, String> colNama;
    @FXML
    private TableColumn<DataHutang, String> colTotalPembayaran;
    @FXML
    private TableColumn<DataHutang, String> colUangPembayaran;
    @FXML
    private TableColumn<DataHutang, String> colKekurangan;
    @FXML
    private TableColumn<DataHutang, String> colTanggal_Transaksi;

    @FXML
    private TextField tfTunai;
    @FXML
    private Label lblKembalian;
    @FXML
    private Label lblNamaPelanggan;
    @FXML
    private Label lblIdTransaksi;
    @FXML
    private Label lblJumlahHutang;
    @FXML
    private Button btnLunas;
    @FXML
    private ScrollPane detailScroll;
    @FXML
    private Button btnBatal;

    // ── Data ───────────────────────────────────────
    private static final NumberFormat FMT = NumberFormat.getInstance(new Locale("id", "ID"));

    // ---------prepare data hutang dari database----------------
    private final ObservableList<DataHutang> dataHutang = FXCollections.observableArrayList();
    private final static List<DataBarang> dataBarang = new ArrayList<>();

    static class DataHutang {

        int no;
        String idTransaksi;
        String namaPelanggan;
        Long total_pembayaran;
        Long uang_pembayaran;
        long kekurangan;
        String status_pembayaran;
        String tanggal_transaksi;

        DataHutang(int no, String idTransaksi, String namaPelanggan, Long total_pembayaran, Long uang_pembayaran,
                long kekurangan, String status_pembayaran, String tanggal_transaksi) {
            this.no = no;
            this.idTransaksi = idTransaksi;
            this.namaPelanggan = namaPelanggan;
            this.total_pembayaran = total_pembayaran;
            this.uang_pembayaran = uang_pembayaran;
            this.kekurangan = kekurangan;
            this.status_pembayaran = status_pembayaran;
            this.tanggal_transaksi = tanggal_transaksi;

        }

    }

    static class DataBarang {

        String nama_barang;
        long harga_barang;
        int qty;

        DataBarang(String nama_barang, long harga_barang, int qty) {
            this.nama_barang = nama_barang;
            this.harga_barang = harga_barang;
            this.qty = qty;
        }
    }

    // -------------------ambil data hutang dari database----------------
    private void load_data_hutang(String namapelanggan) {

        String sql = "SELECT " + "t.id_transaksi, " + "t.pelanggan AS nama_pelanggan, " + "t.total_pembayaran, "
                + "t.uang_pembayaran, " + "t.kekurangan, " + "t.status_pembayaran, " + "t.tanggal_transaksi "
                + "FROM tb_transaksi t " + "WHERE t.status_pembayaran = 'Belum Lunas' " + "AND t.pelanggan LIKE '%"
                + namapelanggan + "%'";

        int rowNo = 1;

        List<Object[]> results = koneksi.ambilData(sql);
        System.out.println("Hasil query Data Hutang: " + results.size() + " baris");
        for (Object[] row : results) {

            String idTransaksi = String.valueOf(row[0]);
            String namaPelanggan = String.valueOf(row[1]);
            Long totalPembayaran = ((Number) row[2]).longValue();
            Long uangPembayaran = ((Number) row[3]).longValue();
            Long kekurangan = ((Number) row[4]).longValue();
            String status = String.valueOf(row[5]);
            String tanggal = String.valueOf(row[6]);

            dataHutang.add(new DataHutang(rowNo++, idTransaksi, namaPelanggan, totalPembayaran, uangPembayaran,
                    kekurangan, status, tanggal));
        }
    }

    private void load_data_barang(DataHutang dataHutang) {
        String sql = "SELECT " + "b.nama_barang, " + "b.harga, " + "td.jumlah " + "FROM tb_detail_transaksi td "
                + "JOIN tb_barang b " + "ON td.id_barang = b.id_barang " + "WHERE td.id_transaksi = '"
                + dataHutang.idTransaksi + "'";
        List<Object[]> results = koneksi.ambilData(sql);
        System.out.println("Hasil query barang: " + results.size() + " baris");
        for (Object[] row : results) {
            String nama = String.valueOf(row[0]);
            long harga = ((Number) row[1]).longValue();
            int qty = ((Number) row[2]).intValue();
            dataBarang.add(new DataBarang(nama, harga, qty));
        }
    }

    private Stage myStage;

    public void setStage(Stage stage) {
        this.myStage = stage;
    }

    // ── State ─────────────────────────────────────────────
    private boolean sidebarCollapsed = false;
    private static final double SIDEBAR_FULL = 220;
    private static final double SIDEBAR_MINI = 60;

    // ═════════════════════════════════════════════════════
    // INITIALIZE
    // ═════════════════════════════════════════════════════
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupNavHover();
        setupTable();
        tableHutang.setItems(dataHutang);
        load_data_hutang("");
        setupLayout();
        SetupRowClick();
        renderList();
        loadKPI();
    }

    // ═════════════════════════════════════════════════════
    // SIDEBAR TOGGLE (animasi smooth)
    // ═════════════════════════════════════════════════════
    @FXML
    private void onToggleSidebar() {
        sidebarCollapsed = !sidebarCollapsed;
        double targetWidth = sidebarCollapsed ? SIDEBAR_MINI : SIDEBAR_FULL;
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(sidebar.prefWidthProperty(), sidebar.getPrefWidth()),
                        new KeyValue(sidebar.minWidthProperty(), sidebar.getMinWidth())),
                new KeyFrame(Duration.millis(350), new KeyValue(sidebar.prefWidthProperty(), targetWidth),
                        new KeyValue(sidebar.minWidthProperty(), targetWidth)));
        if (sidebarCollapsed) {
            hideSidebarText();
            toggleBtn.setText("▶");
            logoRow.setAlignment(Pos.CENTER);
            logoRow.setPadding(new Insets(18, 0, 18, 0));
            userRow.setAlignment(Pos.CENTER);
            userRow.setPadding(new Insets(12, 0, 12, 0));
        } else {
            timeline.setOnFinished(e -> {
                showSidebarText();
                logoRow.setAlignment(Pos.CENTER_LEFT);
                logoRow.setPadding(new Insets(18, 16, 18, 16));
                userRow.setAlignment(Pos.CENTER_LEFT);
                userRow.setPadding(new Insets(12, 16, 12, 16));
            });
            toggleBtn.setText("◀");
        }
        updateNavPadding(sidebarCollapsed);
        timeline.play();
    }

    private void hideSidebarText() {
        logoBrand.setVisible(false);
        logoBrand.setManaged(false);
        userInfo.setVisible(false);
        userInfo.setManaged(false);
        setNavLabelsVisible(false);
    }

    private void showSidebarText() {
        logoBrand.setVisible(true);
        logoBrand.setManaged(true);
        userInfo.setVisible(true);
        userInfo.setManaged(true);
        setNavLabelsVisible(true);
    }

    private void setNavLabelsVisible(boolean visible) {
        List<Label> labels = List.of(navLblDashboard, navLblProduk, navLblKasir, navLblPelanggan, navLblLaporan,
                navLblPiutang, navLblPengaturan);
        for (Label lbl : labels) {
            lbl.setVisible(visible);
            lbl.setManaged(visible);
        }
    }

    private void updateNavPadding(boolean collapsed) {
        Insets collapsedPad = new Insets(10, 0, 10, 0);
        Insets normalPad = new Insets(10, 14, 10, 0);
        Insets pad = collapsed ? collapsedPad : normalPad;

        List<HBox> items = List.of(navDashboard, navProduk, navKasir, navPelanggan, navLaporan, navPiutang,
                navPengaturan);
        for (HBox item : items) {
            item.setAlignment(collapsed ? Pos.CENTER : Pos.CENTER_LEFT);
            item.setPadding(pad);

        }
    }

    // ═════════════════════════════════════════════════════
    // NAV CLICK HANDLERS
    // ═════════════════════════════════════════════════════
    @FXML
    private void onNavDashboard() {
        setActiveNav(navDashboard);
        navigation nav = new navigation();
        nav.navigateToDashboard();
        Stage stage = (Stage) navDashboard.getScene().getWindow();
        stage.close();

    }

    @FXML
    private void onNavProduk() {
        setActiveNav(navProduk);
    }

    @FXML
    private void onNavKasir() {
        setActiveNav(navKasir);
        navigation nav = new navigation();
        nav.navigateToTransaksi();
        Stage stage = (Stage) navKasir.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void onNavPelanggan() {
        setActiveNav(navPelanggan);
    }

    @FXML
    private void onNavLaporan() {
        setActiveNav(navLaporan);
        navigation nav = new navigation();
        nav.navigateToLaporan();
    }

    @FXML
    private void onNavPiutang() {
        setActiveNav(navPiutang);
    }

    @FXML
    private void onNavPengaturan() {
        setActiveNav(navPengaturan);

    }

    private void setActiveNav(HBox selected) {
        List<HBox> all = List.of(navDashboard, navProduk, navKasir, navPelanggan, navLaporan, navPiutang,
                navPengaturan);
        for (HBox item : all) {
            item.getStyleClass().removeAll("nav-active");
            if (!item.getStyleClass().contains("nav-item")) {
                item.getStyleClass().add("nav-item");
            }
        }
        selected.getStyleClass().add("nav-active");
    }

    private void setupNavHover() {
        List<HBox> all = List.of(navDashboard, navProduk, navKasir, navPelanggan, navLaporan, navPiutang,
                navPengaturan);
        for (HBox item : all) {
            item.setOnMouseEntered(e -> item.setStyle("-fx-background-color: #252840; -fx-background-radius: 10;"));
            item.setOnMouseExited(e -> item.setStyle(""));
        }
    }

    // ── Setup tabel ────────────────────────────────
    private void setupTable() {
        colNo.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().no)));
        colNama.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().namaPelanggan));
        colTotalPembayaran
                .setCellValueFactory(d -> new SimpleStringProperty("Rp " + FMT.format(d.getValue().total_pembayaran)));
        colUangPembayaran
                .setCellValueFactory(d -> new SimpleStringProperty("Rp " + FMT.format(d.getValue().uang_pembayaran)));
        colKekurangan.setCellValueFactory(d -> new SimpleStringProperty("Rp " + FMT.format(d.getValue().kekurangan)));
        colTanggal_Transaksi.setCellValueFactory(d -> new SimpleStringProperty((d.getValue().tanggal_transaksi)));

        tableHutang.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }
    // ═══════════════════════════════════════════════
    // LOAD DATA KPI
    private void loadKPI() {

        String sql = "SELECT "
                + "SUM(CASE "
                + "WHEN status_pembayaran = 'Belum Lunas' "
                + "THEN kekurangan ELSE 0 END) AS total_piutang, "

                + "COUNT(CASE "
                + "WHEN status_pembayaran = 'Belum Lunas' "
                + "THEN 1 END) AS jumlah_transaksi, "

                + "SUM(CASE "
                + "WHEN status_pembayaran = 'Belum Lunas' "
                + "AND DATE(tanggal_transaksi) = CURDATE() "
                + "THEN 1 ELSE 0 END) AS transaksi_hari_ini, "

                // total piutang kemarin
                + "SUM(CASE "
                + "WHEN status_pembayaran = 'Belum Lunas' "
                + "AND DATE(tanggal_transaksi) = CURDATE() - INTERVAL 1 DAY "
                + "THEN kekurangan ELSE 0 END) AS piutang_kemarin, "

                // transaksi kemarin
                + "COUNT(CASE "
                + "WHEN status_pembayaran = 'Belum Lunas' "
                + "AND DATE(tanggal_transaksi) = CURDATE() - INTERVAL 1 DAY "
                + "THEN 1 END) AS transaksi_kemarin "

                + "FROM tb_transaksi";

        List<Object[]> results = koneksi.ambilData(sql);

        if (!results.isEmpty()) {

            Object[] row = results.get(0);

            long totalPiutang = row[0] != null
                    ? ((Number) row[0]).longValue()
                    : 0;

            int jumlahTransaksi = row[1] != null
                    ? ((Number) row[1]).intValue()
                    : 0;

            int transaksiHariIni = row[2] != null
                    ? ((Number) row[2]).intValue()
                    : 0;

            long piutangKemarin = row[3] != null
                    ? ((Number) row[3]).longValue()
                    : 0;

            int transaksiKemarin = row[4] != null
                    ? ((Number) row[4]).intValue()
                    : 0;

            // ================= KPI VALUE =================

            kpiTotalPiutang.setText(
                    "Rp " + FMT.format(totalPiutang));

            kpiJumlahTransaksi.setText(
                    String.valueOf(jumlahTransaksi));

            kpiHariIni.setText(
                    String.valueOf(transaksiHariIni));

            // ================= DELTA =================

            setDeltaLabel(
                    lblDeltaPiutang,
                    totalPiutang,
                    piutangKemarin);

            setDeltaLabel(
                    lblDeltaTransaksi,
                    jumlahTransaksi,
                    transaksiKemarin);

            setDeltaLabel(
                    lblDeltaHariIni,
                    transaksiHariIni,
                    transaksiKemarin);
        }
    }

    private void setDeltaLabel(Label label,
            double today,
            double yesterday) {

        if (yesterday == 0) {

            label.setText("▲ 100% vs kemarin");
            label.getStyleClass().removeAll(
                    "kpi-delta-up",
                    "kpi-delta-down");

            label.getStyleClass().add("kpi-delta-up");
            return;
        }

        double percent = ((today - yesterday) / yesterday) * 100;

        String arrow = percent >= 0 ? "▲" : "▼";

        String text = arrow + " "
                + String.format("%.0f", Math.abs(percent))
                + "% vs kemarin";

        label.setText(text);

        label.getStyleClass().removeAll(
                "kpi-delta-up",
                "kpi-delta-down");

        label.getStyleClass().add(
                percent >= 0
                        ? "kpi-delta-up"
                        : "kpi-delta-down");
    }

    // ═══════════════════════════════════════════════
    // LOAD DATA & RENDER

    @FXML
    private void onLunas() {
        String sql = "UPDATE tb_transaksi SET status_pembayaran = 'Lunas', kembalian = "
                + lblKembalian.getText().replaceAll("[^0-9]", "") + " WHERE id_transaksi = '" + lblIdTransaksi.getText()
                + "'";
        koneksi.eksekusiQuery(sql);
        System.out.println(
                "Transaksi " + lblIdTransaksi.getText() + " ditandai LUNAS dengan kembalian " + lblKembalian.getText());
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Konfirmasi Lunas");
        confirm.setHeaderText(null);
        confirm.setContentText("Tandai transaksi ini sebagai LUNAS?");
        dataBarang.clear();
        confirm.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                load_data_hutang(tfPelanggan.getText());
                renderList();
                updateSummary();
                setupTable();
                loadKPI();
            }
        });

    }

    @FXML
    private void onBatal() {
        dataBarang.clear();
        renderList();
    }

    long kembalian;
    long tunai;

    public void updateSummary() {
        DataHutang selected = tableHutang.getSelectionModel().getSelectedItem();
        // Kembalian
        tunai = parseLong(tfTunai.getText().replaceAll("[^0-9]", ""));
        kembalian = tunai - (selected != null ? selected.kekurangan : 0);
        lblKembalian.setText(kembalian >= 0 ? "Kembalian Rp " + FMT.format(kembalian)
                : "Kurang Rp " + FMT.format(Math.abs(kembalian)));
        lblKembalian.setStyle(kembalian >= 0 ? "-fx-text-fill: #00E5A0;" : "-fx-text-fill: #FF5C7C;");
    }

    private boolean isUpdating = false;

    @FXML
    private void onTunaiChanged() {
        if (isUpdating) {
            return;
        }
        isUpdating = true;
        String raw = tfTunai.getText().replaceAll("[^0-9]", "");
        if (raw.isEmpty()) {
            tfTunai.setText("");
            isUpdating = false;
            updateSummary();
        }
        long value = Long.parseLong(raw);
        tfTunai.setText("Rp " + FMT.format(value));
        tfTunai.positionCaret(tfTunai.getText().length());
        isUpdating = false;
        updateSummary();
    }

    private long parseLong(String s) {
        try {
            return s == null || s.isBlank() ? 0 : Long.parseLong(s.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    void SetupRowClick() {
        tableHutang.setOnMouseClicked(e -> {
            DataHutang selected = tableHutang.getSelectionModel().getSelectedItem();
            if (selected != null) {
                lblIdTransaksi.setText(selected.idTransaksi);
                lblNamaPelanggan.setText(selected.namaPelanggan);
                lblJumlahHutang.setText("Rp " + FMT.format(selected.kekurangan));
                load_data_barang(selected);
                renderList();
                dataBarang.clear();
            }
        });
    }

    // ── Setup layout scroll content ───────────────────
    private void setupLayout() {
        detailList = new VBox(10);
        detailList.getStyleClass().add("detail-list");
        detailList.setPadding(new Insets(12, 0, 12, 0));

        detailScroll.setContent(detailList);
        detailScroll.getStyleClass().add("detail-scroll");
        detailScroll.setFitToWidth(true);
    }

    private VBox detailList;

    private void renderList() {
        detailList.getChildren().clear();

        if (dataBarang.isEmpty()) {
            // Empty state
            VBox empty = new VBox(8);
            empty.setAlignment(Pos.CENTER);
            empty.setPadding(new Insets(60, 0, 60, 0));

            Label icon = new Label("🛒");
            icon.getStyleClass().add("empty-icon");

            Label text = new Label("Belum ada item");
            text.getStyleClass().add("empty-text");

            Label sub = new Label("Tambahkan produk dari halaman kasir");
            sub.getStyleClass().add("empty-sub");

            empty.getChildren().addAll(icon, text, sub);
            detailList.getChildren().add(empty);
            btnLunas.setDisable(true);

            System.out.println("Keranjang kosong, tampilkan empty state");
            return;
        } else {
            // Table header
            detailList.getChildren().add(buildTableHeader());
            // Item rows
            int no = 1;
            for (DataBarang barang : dataBarang) {
                detailList.getChildren().add(setdatabarang(barang, no));
                no++;
            }
            // Summary
            updateSummary();
            btnLunas.setDisable(false);
            System.out.println("Menmpulkan " + dataBarang.size() + " item di detail list");
        }

    }

    // ── Table header ──────────────────────────────────
    private HBox buildTableHeader() {
        Label thNo = new Label("No");
        Label thNama = new Label("Nama Produk");
        Label thHarga = new Label("Harga Satuan");
        Label thQty = new Label("Qty");

        thNo.getStyleClass().add("th-label");
        thNama.getStyleClass().add("th-label");
        thHarga.getStyleClass().add("th-label");
        thQty.getStyleClass().add("th-label");

        // ← samakan ukuran dengan item row
        thNo.setPrefWidth(40);
        thNo.setMinWidth(40);
        thNo.setAlignment(Pos.CENTER);

        HBox.setHgrow(thNama, Priority.ALWAYS);
        thNama.setMaxWidth(Double.MAX_VALUE);

        thHarga.setPrefWidth(130);
        thHarga.setMinWidth(130);
        thHarga.setAlignment(Pos.CENTER_RIGHT);

        thQty.setPrefWidth(60);
        thQty.setMinWidth(60);
        thQty.setAlignment(Pos.CENTER);

        HBox header = new HBox(12, thNo, thNama, thHarga, thQty);
        header.getStyleClass().add("table-header");
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    private HBox setdatabarang(DataBarang barang, int no) {
        // No urut
        Label lblNo = new Label(String.valueOf(no));
        lblNo.getStyleClass().add("item-no");
        lblNo.setPrefWidth(40);
        lblNo.setMinWidth(40);
        lblNo.setAlignment(Pos.CENTER);

        // Nama + kategori
        Label lblNama = new Label(barang.nama_barang);
        lblNama.getStyleClass().add("item-nama");

        // Harga satuan
        Label lblHarga = new Label("Rp " + FMT.format(barang.harga_barang));
        lblHarga.getStyleClass().add("item-harga");
        lblHarga.setPrefWidth(130);
        lblHarga.setMinWidth(130);
        lblHarga.setAlignment(Pos.CENTER_RIGHT);

        // Qty
        Label lblQty = new Label("x" + barang.qty);
        lblQty.getStyleClass().add("item-qty");
        lblQty.setPrefWidth(60);
        lblQty.setMinWidth(60);
        lblQty.setAlignment(Pos.CENTER);

        HBox row = new HBox(12, lblNo, lblHarga, lblQty);
        row.getStyleClass().add("item-row");
        row.setAlignment(Pos.CENTER_LEFT);

        return new HBox(12, lblNo, lblNama, lblHarga, lblQty);
    }

    
    // ═════════════════════════════════════════════════════
    // OTHER HANDLERS
    // ═════════════════════════════════════════════════════


    @FXML
    private void onQuick5() {
        tfTunai.setText("5000");
        updateSummary();
    }

    @FXML
    private void onQuick10() {
        tfTunai.setText("10000");
        updateSummary();
    }

    @FXML
    private void onQuick20() {
        tfTunai.setText("20000");
        updateSummary();
    }

    @FXML
    private void onQuick50() {
        tfTunai.setText("50000");
        updateSummary();
    }

    @FXML
    private void onNotif() {
        System.out.println("Notifikasi dibuka");
    }

    @FXML
    private void onLihatSemua() {
        System.out.println("Lihat semua transaksi");

    }
}
