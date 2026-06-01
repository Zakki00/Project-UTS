package com.mycompany.projectuas;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class BarangController implements Initializable {

    @FXML private TextField txtNama;
    @FXML private ComboBox<String> cmbKategori;
    @FXML private TextField txtHarga;
    @FXML private TextField txtStok;
    @FXML private TextArea txtDeskripsi;
    @FXML private Label lblFilePath;
    @FXML private TextField txtCari;

    @FXML private TableView<BarangModel> tabelBarang;
    @FXML private TableColumn<BarangModel, Integer> colId;
    @FXML private TableColumn<BarangModel, String> colGambar;
    @FXML private TableColumn<BarangModel, String> colNama;
    @FXML private TableColumn<BarangModel, String> colKategori;
    @FXML private TableColumn<BarangModel, Integer> colHarga;
    @FXML private TableColumn<BarangModel, Integer> colStok;
    @FXML private TableColumn<BarangModel, String> colDeskripsi;
    @FXML private TableColumn<BarangModel, String> colStatus;

    @FXML private VBox sidebar;
    @FXML private VBox logoBrand;
    @FXML private Button toggleBtn;
    @FXML private Label navLblDashboard;
    @FXML private Label navLblProduk;
    @FXML private Label navLblKasir;
    @FXML private Label navLblPelanggan;
    @FXML private Label navLblLaporan;
    @FXML private Label navLblPengaturan;
    @FXML private Label navLblPengaturan1;
    @FXML private VBox userInfo;

    @FXML private HBox navDashboard;
    @FXML private HBox navProduk;
    @FXML private HBox navKasir;
    @FXML private HBox navPelanggan;
    @FXML private HBox navLaporan;
    @FXML private HBox navPiutang;
    @FXML private HBox navPengaturan;

    private ObservableList<BarangModel> masterData = FXCollections.observableArrayList();
    private FilteredList<BarangModel> filteredData;
    private boolean isSidebarExpanded = true;
    private boolean isUpdatingHarga = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cmbKategori.setItems(FXCollections.observableArrayList("Makanan", "Minuman"));

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNama.setCellValueFactory(new PropertyValueFactory<>("nama"));
        colKategori.setCellValueFactory(new PropertyValueFactory<>("kategori"));
        colStok.setCellValueFactory(new PropertyValueFactory<>("stok"));
        colDeskripsi.setCellValueFactory(new PropertyValueFactory<>("deskripsi"));
        tabelBarang.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // ── Kolom harga tampil dengan prefix "Rp" ──
        colHarga.setCellValueFactory(new PropertyValueFactory<>("harga"));
        colHarga.setCellFactory(col -> new TableCell<BarangModel, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("Rp " + String.format("%,d", item).replace(',', '.'));
                }
            }
        });

        // ── Kolom gambar ──
        colGambar.setCellValueFactory(new PropertyValueFactory<>("gambar"));
        colGambar.setCellFactory(column -> new TableCell<BarangModel, String>() {
            private final ImageView imageView = new ImageView();
            {
                imageView.setFitWidth(50);
                imageView.setFitHeight(50);
                imageView.setPreserveRatio(true);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isBlank()) {
                    setGraphic(null);
                    setText(null);
                } else {
                    try {
                        var stream = getClass().getResourceAsStream("/image-barang/" + item);
                        if (stream != null) {
                            imageView.setImage(new Image(stream));
                            setGraphic(imageView);
                            setText(null);
                        } else {
                            setGraphic(null);
                            setText(item);
                        }
                    } catch (Exception e) {
                        setGraphic(null);
                        setText(null);
                    }
                }
            }
        });

        // ── Kolom status ──
        colStatus.setCellValueFactory(cellData -> {
            int stokVal = cellData.getValue().getStok();
            return new SimpleStringProperty(stokVal > 0 ? "Tersedia" : "Habis");
        });

        // ── txtStok: hanya angka ──
        txtStok.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                txtStok.setText(newVal.replaceAll("[^\\d]", ""));
            }
        });

        // ── txtHarga: hanya angka, format Rp otomatis ──
        txtHarga.textProperty().addListener((obs, oldVal, newVal) -> {
            if (isUpdatingHarga) return;
            isUpdatingHarga = true;

            String angkaSaja = newVal.replaceAll("[^\\d]", "");

            if (angkaSaja.isEmpty()) {
                txtHarga.setText("");
            } else {
                String formatted = "Rp " + String.format("%,d", Long.parseLong(angkaSaja)).replace(',', '.');
                txtHarga.setText(formatted);
                Platform.runLater(() -> txtHarga.positionCaret(txtHarga.getText().length()));
            }

            isUpdatingHarga = false;
        });

        loadDataFromDB();

        filteredData = new FilteredList<>(masterData, p -> true);
        tabelBarang.setItems(filteredData);

        // ── Listener pilih baris tabel ──
        tabelBarang.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                txtNama.setText(newSelection.getNama());
                cmbKategori.setValue(newSelection.getKategori());
                txtStok.setText(String.valueOf(newSelection.getStok()));
                txtDeskripsi.setText(newSelection.getDeskripsi());
                lblFilePath.setText(newSelection.getGambar() != null ? newSelection.getGambar() : "Tidak ada file dipilih");

                // Set harga dengan format Rp
                isUpdatingHarga = true;
                String hargaFormatted = "Rp " + String.format("%,d", newSelection.getHarga()).replace(',', '.');
                txtHarga.setText(hargaFormatted);
                Platform.runLater(() -> txtHarga.positionCaret(txtHarga.getText().length()));
                isUpdatingHarga = false;
            }
        });
    }

    // ── Ambil angka murni dari txtHarga ──
    private int getHargaValue() {
        String angkaSaja = txtHarga.getText().replaceAll("[^\\d]", "");
        if (angkaSaja.isEmpty()) return 0;
        try {
            return Integer.parseInt(angkaSaja);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // ═══════════════════════════════════════════
    // VALIDASI INPUT
    // ═══════════════════════════════════════════
    private boolean isInputValid(boolean isUpdate, BarangModel dipilih) {
        StringBuilder pesan = new StringBuilder();

        if (txtNama.getText().trim().isEmpty()) {
            pesan.append("- Nama barang wajib diisi.\n");
        }
        if (cmbKategori.getValue() == null) {
            pesan.append("- Kategori wajib dipilih.\n");
        }
        if (txtHarga.getText().trim().isEmpty() || getHargaValue() <= 0) {
            pesan.append("- Harga wajib diisi dan harus lebih dari 0.\n");
        }
        if (txtStok.getText().trim().isEmpty()) {
            pesan.append("- Stok wajib diisi.\n");
        }

        // Validasi gambar:
        // - Tambah: wajib pilih gambar baru
        // - Ubah  : boleh tidak pilih ulang jika data lama sudah punya gambar
        boolean gambarBaru = !lblFilePath.getText().equals("Tidak ada file dipilih");
        boolean gambarLama = isUpdate && dipilih != null
                && dipilih.getGambar() != null
                && !dipilih.getGambar().isBlank();

        if (!gambarBaru && !gambarLama) {
            pesan.append("- Gambar barang wajib dipilih.\n");
        }

        if (pesan.length() > 0) {
            showAlert("Peringatan", "Harap lengkapi data berikut:\n\n" + pesan.toString().trim());
            return false;
        }
        return true;
    }

    // ═══════════════════════════════════════════
    // LOAD DATA DARI DATABASE
    // ═══════════════════════════════════════════
    private void loadDataFromDB() {
        masterData.clear();
        String query = "SELECT id_barang, nama_barang, kategori, harga, stok, deskripsi, image_url FROM tb_barang";
        try (Connection conn = koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                masterData.add(new BarangModel(
                    rs.getInt("id_barang"),
                    rs.getString("nama_barang"),
                    rs.getString("kategori"),
                    rs.getInt("harga"),
                    rs.getInt("stok"),
                    rs.getString("deskripsi"),
                    rs.getString("image_url")
                ));
            }
        } catch (SQLException e) {
            showAlert("Error DB", "Gagal memuat data: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════
    // CRUD
    // ═══════════════════════════════════════════
    @FXML
    void tambahBarang(ActionEvent event) {
        // Validasi: gambar wajib diisi saat tambah (isUpdate=false, dipilih=null)
        if (!isInputValid(false, null)) return;

        try {
            String nama      = txtNama.getText().trim();
            String kategori  = cmbKategori.getValue();
            int harga        = getHargaValue();
            int stok         = Integer.parseInt(txtStok.getText().trim());
            String deskripsi = txtDeskripsi.getText();
            String gambar    = lblFilePath.getText();

            String query = "INSERT INTO tb_barang (nama_barang, kategori, harga, stok, deskripsi, image_url) VALUES (?, ?, ?, ?, ?, ?)";
            try (Connection conn = koneksi.getConnection();
                 PreparedStatement ps = conn.prepareStatement(query)) {

                ps.setString(1, nama);
                ps.setString(2, kategori);
                ps.setInt(3, harga);
                ps.setInt(4, stok);
                ps.setString(5, deskripsi);
                ps.setString(6, gambar);
                ps.executeUpdate();
            }

            loadDataFromDB();
            clearForm(null);

        } catch (NumberFormatException e) {
            showAlert("Kesalahan Input", "Stok harus berupa angka bulat!");
        } catch (SQLException e) {
            showAlert("Error DB", "Gagal menambah data: " + e.getMessage());
        }
    }

    @FXML
    void ubahBarang(ActionEvent event) {
        BarangModel dipilih = tabelBarang.getSelectionModel().getSelectedItem();
        if (dipilih == null) {
            showAlert("Peringatan", "Silakan pilih salah satu data barang di tabel yang ingin diubah!");
            return;
        }

        // Validasi: gambar boleh tidak dipilih ulang jika data lama sudah ada (isUpdate=true)
        if (!isInputValid(true, dipilih)) return;

        try {
            String nama      = txtNama.getText().trim();
            String kategori  = cmbKategori.getValue();
            int harga        = getHargaValue();
            int stok         = Integer.parseInt(txtStok.getText().trim());
            String deskripsi = txtDeskripsi.getText();
            String gambar    = lblFilePath.getText().equals("Tidak ada file dipilih")
                               ? dipilih.getGambar()
                               : lblFilePath.getText();

            String query = "UPDATE tb_barang SET nama_barang=?, kategori=?, harga=?, stok=?, deskripsi=?, image_url=? WHERE id_barang=?";
            try (Connection conn = koneksi.getConnection();
                 PreparedStatement ps = conn.prepareStatement(query)) {

                ps.setString(1, nama);
                ps.setString(2, kategori);
                ps.setInt(3, harga);
                ps.setInt(4, stok);
                ps.setString(5, deskripsi);
                ps.setString(6, gambar);
                ps.setInt(7, dipilih.getId());
                ps.executeUpdate();
            }

            loadDataFromDB();
            clearForm(null);

        } catch (NumberFormatException e) {
            showAlert("Kesalahan Input", "Stok harus berupa angka bulat!");
        } catch (SQLException e) {
            showAlert("Error DB", "Gagal mengubah data: " + e.getMessage());
        }
    }

    @FXML
    void hapusBarang(ActionEvent event) {
        BarangModel dipilih = tabelBarang.getSelectionModel().getSelectedItem();
        if (dipilih == null) {
            showAlert("Peringatan", "Silakan pilih data di tabel terlebih dahulu yang ingin dihapus!");
            return;
        }
        try {
            String query = "DELETE FROM tb_barang WHERE id_barang=?";
            try (Connection conn = koneksi.getConnection();
                 PreparedStatement ps = conn.prepareStatement(query)) {

                ps.setInt(1, dipilih.getId());
                ps.executeUpdate();
            }

            loadDataFromDB();
            clearForm(null);

        } catch (SQLException e) {
            showAlert("Error DB", "Gagal menghapus data: " + e.getMessage());
        }
    }

    @FXML
    void clearForm(ActionEvent event) {
        txtNama.clear();
        cmbKategori.setValue(null);
        isUpdatingHarga = true;
        txtHarga.clear();
        isUpdatingHarga = false;
        txtStok.clear();
        txtDeskripsi.clear();
        lblFilePath.setText("Tidak ada file dipilih");
        tabelBarang.getSelectionModel().clearSelection();
    }

    @FXML
    void cariBarang(KeyEvent event) {
        String keyword = txtCari.getText().toLowerCase();
        filteredData.setPredicate(barang -> {
            if (keyword == null || keyword.isEmpty()) return true;
            return barang.getNama().toLowerCase().contains(keyword)
                    || barang.getKategori().toLowerCase().contains(keyword);
        });
    }

    @FXML
    void onPilihFoto(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(
            new FileChooser.ExtensionFilter(
                "Gambar (*.png, *.jpg, *.jpeg, *.webp, *.jfif)",
                "*.png", "*.jpg", "*.jpeg", "*.webp", "*.jfif"
            )
        );

        File file = fc.showOpenDialog(txtNama.getScene().getWindow());
        if (file != null) {
            try {
                Path tujuan = Path.of("src/main/resources/image-barang/" + file.getName());
                Files.copy(file.toPath(), tujuan, StandardCopyOption.REPLACE_EXISTING);
                lblFilePath.setText(file.getName());
            } catch (IOException e) {
                showAlert("Error", "Gagal menyalin file gambar: " + e.getMessage());
            }
        }
    }

    // ═══════════════════════════════════════════
    // SIDEBAR TOGGLE
    // ═══════════════════════════════════════════
    @FXML
    void onToggleSidebar(ActionEvent event) {
        if (isSidebarExpanded) {
            sidebar.setPrefWidth(60);
            logoBrand.setVisible(false);
            userInfo.setVisible(false);
            toggleBtn.setText("▶");
            setNavLabelsVisible(false);
            isSidebarExpanded = false;
        } else {
            sidebar.setPrefWidth(220);
            logoBrand.setVisible(true);
            userInfo.setVisible(true);
            toggleBtn.setText("◀");
            setNavLabelsVisible(true);
            isSidebarExpanded = true;
        }
    }

    private void setNavLabelsVisible(boolean visible) {
        navLblDashboard.setVisible(visible);
        navLblProduk.setVisible(visible);
        navLblKasir.setVisible(visible);
        navLblPelanggan.setVisible(visible);
        navLblLaporan.setVisible(visible);
        navLblPengaturan.setVisible(visible);
        navLblPengaturan1.setVisible(visible);
    }

    // ═══════════════════════════════════════════
    // NAVIGASI SIDEBAR
    // ═══════════════════════════════════════════
    @FXML
    void onNavDashboard() {
        setActiveNav(navDashboard);
        navigation nav = new navigation();
        nav.navigateToDashboard();
        Stage stage = (Stage) navDashboard.getScene().getWindow();
        stage.close();
    }

    @FXML
    void onNavProduk() {
        setActiveNav(navProduk);
        System.out.println("ℹ️ Sudah di halaman Produk");
    }

    @FXML
    void onNavKasir() {
        setActiveNav(navKasir);
        navigation nav = new navigation();
        nav.navigateToTransaksi();
        Stage stage = (Stage) navKasir.getScene().getWindow();
        stage.close();
    }

    @FXML void onNavPelanggan() { setActiveNav(navPelanggan); }

    @FXML
    void onNavLaporan() {
        setActiveNav(navLaporan);
        navigation nav = new navigation();
        nav.navigateToLaporan();
        Stage stage = (Stage) navLaporan.getScene().getWindow();
        stage.close();
    }

    @FXML
    void onNavPiutang() {
        setActiveNav(navPiutang);
        navigation nav = new navigation();
        nav.navigateToPiutang();
        Stage stage = (Stage) navPiutang.getScene().getWindow();
        stage.close();
    }

    @FXML void onNavPengaturan() { setActiveNav(navPengaturan); }

    private void setActiveNav(HBox selected) {
        java.util.List<HBox> all = java.util.List.of(
            navDashboard, navProduk, navKasir,
            navPelanggan, navLaporan, navPiutang, navPengaturan
        );
        for (HBox item : all) {
            item.getStyleClass().removeAll("nav-active");
            if (!item.getStyleClass().contains("nav-item")) {
                item.getStyleClass().add("nav-item");
            }
        }
        selected.getStyleClass().add("nav-active");
    }

    // ═══════════════════════════════════════════
    // HELPER
    // ═══════════════════════════════════════════
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}