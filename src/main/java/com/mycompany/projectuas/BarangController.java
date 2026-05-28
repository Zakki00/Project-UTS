package com.mycompany.projectuas;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

public class BarangController implements Initializable {

    // ── Sidebar ───────────────────────────────────────────
    @FXML private VBox sidebar;
    @FXML private HBox logoRow;
    @FXML private VBox logoBrand;
    @FXML private VBox userInfo;
    @FXML private HBox userRow;
    @FXML private Button toggleBtn;
    @FXML private VBox navMenu;

    @FXML private HBox navDashboard;
    @FXML private HBox navProduk;
    @FXML private HBox navKasir;
    @FXML private HBox navPelanggan;
    @FXML private HBox navLaporan;
    @FXML private HBox navPiutang;
    @FXML private HBox navPengaturan;

    @FXML private Label navLblDashboard;
    @FXML private Label navLblProduk;
    @FXML private Label navLblKasir;
    @FXML private Label navLblPelanggan;
    @FXML private Label navLblLaporan;
    @FXML private Label navLblPengaturan;

    // ── Form Input Barang ─────────────────────────────────
    // ID Barang tidak memakai TextField visual (dikelola otomatis lewat state tersembunyi)
    private final SimpleIntegerProperty selectedId = new SimpleIntegerProperty(-1); 
    
    @FXML private TextField txtNama;
    @FXML private ComboBox<String> cmbKategori;
    @FXML private TextField txtHarga;
    @FXML private TextField txtStok;
    @FXML private TextArea txtDeskripsi;
    @FXML private Label lblFilePath; // Menampilkan path file gambar terpilih

    // ── Search & Table Barang ─────────────────────────────
    @FXML private TextField txtCari;
    @FXML private TableView<BarangModel> tabelBarang;
    @FXML private TableColumn<BarangModel, Integer> colId;
    @FXML private TableColumn<BarangModel, String> colGambar; // Path string / URL lokal foto
    @FXML private TableColumn<BarangModel, String> colNama;
    @FXML private TableColumn<BarangModel, String> colKategori;
    @FXML private TableColumn<BarangModel, Integer> colHarga;
    @FXML private TableColumn<BarangModel, Integer> colStok;
    @FXML private TableColumn<BarangModel, String> colDeskripsi;
    @FXML private TableColumn<BarangModel, String> colStatus;

    // ── State & Data ──────────────────────────────────────
    private boolean sidebarCollapsed = false;
    private static final double SIDEBAR_FULL = 220;
    private static final double SIDEBAR_MINI = 60;
    
    private ObservableList<BarangModel> masterDataBarang = FXCollections.observableArrayList();
    private int autoIncrementId = 4; // Dimulai dari 4 karena ID 1-3 dipakai dummy data
    private String stringPathGambarTerpilih = "";

    // ═════════════════════════════════════════════════════
    // INITIALIZE
    // ═════════════════════════════════════════════════════
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupSidebarNav();
        setupFormControls();
        initDummyData();
        setupTableColumns();
        
        // Listener saat baris tabel di-klik (Read Single Data ke Form)
        tabelBarang.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                pilihBarang(newSelection);
            }
        });
    }

    private void setupFormControls() {
        cmbKategori.setItems(FXCollections.observableArrayList("Makanan", "Minuman", "Kebutuhan Rumah"));
    }

    private void initDummyData() {
        masterDataBarang.add(new BarangModel(1, "Indomie Goreng", "Makanan", 3500, 50, "Indomie seleraku rasa mi goreng", ""));
        masterDataBarang.add(new BarangModel(2, "Coca Cola 250ml", "Minuman", 5000, 0, "Minuman bersoda segar", ""));
        masterDataBarang.add(new BarangModel(3, "Sabun Cuci Piring", "Kebutuhan Rumah", 12000, 12, "Bersih kesat anti lemak", ""));
        tabelBarang.setItems(masterDataBarang);
    }

    // ═════════════════════════════════════════════════════
    // TABLE CONFIGURATION & RENDER
    // ═════════════════════════════════════════════════════
    private void setupTableColumns() {
        colId.setCellValueFactory(cell -> cell.getValue().idProperty().asObject());
        colNama.setCellValueFactory(cell -> cell.getValue().namaProperty());
        colKategori.setCellValueFactory(cell -> cell.getValue().kategoriProperty());
        colHarga.setCellValueFactory(cell -> cell.getValue().hargaProperty().asObject());
        colStok.setCellValueFactory(cell -> cell.getValue().stokProperty().asObject());
        colDeskripsi.setCellValueFactory(cell -> cell.getValue().deskripsiProperty());
        
        // Custom Render untuk Status Stok (Badge)
        colStatus.setCellValueFactory(cell -> {
            int stok = cell.getValue().getStok();
            return new SimpleStringProperty(stok > 0 ? "Tersedia" : "Tidak Tersedia");
        });
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.equals("Tersedia")) {
                        setStyle("-fx-text-fill: #00E5A0; -fx-font-weight: bold;"); // Hijau
                    } else {
                        setStyle("-fx-text-fill: #FF5C7C; -fx-font-weight: bold;"); // Merah
                    }
                }
            }
        });

        // Custom Render Gambar dari File PC lokal ke Table Cell
        colGambar.setCellValueFactory(cell -> cell.getValue().gambarProperty());
        colGambar.setCellFactory(col -> new TableCell<>() {
            private final ImageView imgView = new ImageView();
            @Override
            protected void updateItem(String path, boolean empty) {
                super.updateItem(path, empty);
                if (empty || path == null || path.isEmpty()) {
                    setGraphic(null);
                } else {
                    try {
                        File file = new File(path);
                        if (file.exists()) {
                            Image image = new Image(file.toURI().toString(), 40, 40, true, true);
                            imgView.setImage(image);
                            setGraphic(imgView);
                        } else {
                            setGraphic(null); // Fallback box kosong/default jika file dipindahkan
                        }
                    } catch (Exception e) {
                        setGraphic(null);
                    }
                }
            }
        });

        tabelBarang.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    // ═════════════════════════════════════════════════════
    // FILE CHOOSER (INPUT GAMBAR DARI PC/LAPTOP)
    // ═════════════════════════════════════════════════════
    @FXML
    private void onPilihFoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Pilih Foto Produk");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        
        Stage stage = (Stage) tabelBarang.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        
        if (selectedFile != null) {
            stringPathGambarTerpilih = selectedFile.getAbsolutePath();
            lblFilePath.setText(selectedFile.getName());
        }
    }

    // ═════════════════════════════════════════════════════
    // CRUD OPERATIONS
    // ═════════════════════════════════════════════════════
    @FXML
    private void tambahBarang() {
        String nama = txtNama.getText();
        String kategori = cmbKategori.getValue();
        String hargaStr = txtHarga.getText();
        String stokStr = txtStok.getText();
        String deskripsi = txtDeskripsi.getText();

        if (nama.isEmpty() || kategori == null || hargaStr.isEmpty() || stokStr.isEmpty()) {
            System.out.println("Validasi gagal: Lengkapi form!");
            return;
        }

        int harga = Integer.parseInt(hargaStr);
        int stok = Integer.parseInt(stokStr);

        BarangModel baru = new BarangModel(autoIncrementId++, nama, kategori, harga, stok, deskripsi, stringPathGambarTerpilih);
        masterDataBarang.add(baru);
        
        clearForm();
    }

    private void pilihBarang(BarangModel barang) {
        selectedId.set(barang.getId());
        txtNama.setText(barang.getNama());
        cmbKategori.setValue(barang.getKategori());
        txtHarga.setText(String.valueOf(barang.getHarga()));
        txtStok.setText(String.valueOf(barang.getStok()));
        txtDeskripsi.setText(barang.getDeskripsi());
        stringPathGambarTerpilih = barang.getGambar();
        
        if (!stringPathGambarTerpilih.isEmpty()) {
            File f = new File(stringPathGambarTerpilih);
            lblFilePath.setText(f.getName());
        } else {
            lblFilePath.setText("Tidak ada file dipilih");
        }
    }

    @FXML
    private void ubahBarang() {
        int id = selectedId.get();
        if (id == -1) {
            System.out.println("Pilih data di tabel terlebih dahulu!");
            return;
        }

        for (BarangModel b : masterDataBarang) {
            if (b.getId() == id) {
                b.setNama(txtNama.getText());
                b.setKategori(cmbKategori.getValue());
                b.setHarga(Integer.parseInt(txtHarga.getText()));
                b.setStok(Integer.parseInt(txtStok.getText()));
                b.setDeskripsi(txtDeskripsi.getText());
                b.setGambar(stringPathGambarTerpilih);
                break;
            }
        }
        tabelBarang.refresh();
        clearForm();
    }

    @FXML
    private void hapusBarang() {
        int id = selectedId.get();
        if (id == -1) {
            System.out.println("Pilih data di tabel terlebih dahulu!");
            return;
        }

        masterDataBarang.removeIf(b -> b.getId() == id);
        clearForm();
    }

    @FXML
    private void cariBarang() {
        String keyword = txtCari.getText().toLowerCase();
        if (keyword.isEmpty()) {
            tabelBarang.setItems(masterDataBarang);
            return;
        }

        ObservableList<BarangModel> filteredList = FXCollections.observableArrayList();
        for (BarangModel b : masterDataBarang) {
            if (b.getNama().toLowerCase().contains(keyword) || b.getKategori().toLowerCase().contains(keyword)) {
                filteredList.add(b);
            }
        }
        tabelBarang.setItems(filteredList);
    }

    @FXML
    private void clearForm() {
        selectedId.set(-1);
        txtNama.clear();
        cmbKategori.setValue(null);
        txtHarga.clear();
        txtStok.clear();
        txtDeskripsi.clear();
        stringPathGambarTerpilih = "";
        lblFilePath.setText("Tidak ada file dipilih");
        tabelBarang.getSelectionModel().clearSelection();
    }

    // ═════════════════════════════════════════════════════
    // SIDEBAR NAVIGATION UTILITY (Relasi package lama)
    // ═════════════════════════════════════════════════════
    private void setupSidebarNav() {
        List<HBox> all = List.of(navDashboard, navProduk, navKasir, navPelanggan, navLaporan, navPengaturan);
        for (HBox item : all) {
            item.setOnMouseEntered(e -> item.setStyle("-fx-background-color: #252840; -fx-background-radius: 10;"));
            item.setOnMouseExited(e -> item.setStyle(""));
        }
    }

    @FXML private void onNavDashboard() { setActiveNav(navDashboard); }
    @FXML private void onNavProduk() { setActiveNav(navProduk); }

    @FXML
    private void onNavKasir() {
        setActiveNav(navKasir);
        navigation nav = new navigation();
        nav.navigateToTransaksi();
        closeCurrentStage();
    }

    @FXML private void onNavPelanggan() { setActiveNav(navPelanggan); }

    @FXML
    private void onNavLaporan() {
        setActiveNav(navLaporan);
        navigation nav = new navigation();
        nav.navigateToLaporan();
    }

    @FXML
    private void onNavPiutang() {
        setActiveNav(navPiutang);
        navigation nav = new navigation();
        nav.navigateToPiutang();
        closeCurrentStage();
    }

    @FXML private void onNavPengaturan() { setActiveNav(navPengaturan); }

    private void setActiveNav(HBox selected) {
        List<HBox> all = List.of(navDashboard, navProduk, navKasir, navPelanggan, navLaporan, navPengaturan);
        for (HBox item : all) {
            item.getStyleClass().removeAll("nav-active");
            if (!item.getStyleClass().contains("nav-item")) item.getStyleClass().add("nav-item");
        }
        selected.getStyleClass().add("nav-active");
    }

    private void closeCurrentStage() {
        Stage stage = (Stage) navProduk.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void onToggleSidebar() {
        sidebarCollapsed = !sidebarCollapsed;
        double targetWidth = sidebarCollapsed ? SIDEBAR_MINI : SIDEBAR_FULL;
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(sidebar.prefWidthProperty(), sidebar.getPrefWidth()), new KeyValue(sidebar.minWidthProperty(), sidebar.getMinWidth())),
                new KeyFrame(Duration.millis(350), new KeyValue(sidebar.prefWidthProperty(), targetWidth), new KeyValue(sidebar.minWidthProperty(), targetWidth)));
        if (sidebarCollapsed) {
            logoBrand.setVisible(false); logoBrand.setManaged(false);
            userInfo.setVisible(false); userInfo.setManaged(false);
            setNavLabelsVisible(false);
            toggleBtn.setText("▶");
            logoRow.setAlignment(Pos.CENTER); logoRow.setPadding(new Insets(18, 0, 18, 0));
            userRow.setAlignment(Pos.CENTER); userRow.setPadding(new Insets(12, 0, 12, 0));
        } else {
            timeline.setOnFinished(e -> {
                logoBrand.setVisible(true); logoBrand.setManaged(true);
                userInfo.setVisible(true); userInfo.setManaged(true);
                setNavLabelsVisible(true);
                logoRow.setAlignment(Pos.CENTER_LEFT); logoRow.setPadding(new Insets(18, 16, 18, 16));
                userRow.setAlignment(Pos.CENTER_LEFT); userRow.setPadding(new Insets(12, 16, 12, 16));
            });
            toggleBtn.setText("◀");
        }
        updateNavPadding(sidebarCollapsed);
        timeline.play();
    }

    private void setNavLabelsVisible(boolean visible) {
        List<Label> labels = List.of(navLblDashboard, navLblProduk, navLblKasir, navLblPelanggan, navLblLaporan, navLblPengaturan);
        for (Label lbl : labels) {
            lbl.setVisible(visible);
            lbl.setManaged(visible);
        }
    }

    private void updateNavPadding(boolean collapsed) {
        Insets pad = collapsed ? new Insets(10, 0, 10, 0) : new Insets(10, 14, 10, 0);
        List<HBox> items = List.of(navDashboard, navProduk, navKasir, navPelanggan, navLaporan, navPengaturan);
        for (HBox item : items) {
            item.setAlignment(collapsed ? Pos.CENTER : Pos.CENTER_LEFT);
            item.setPadding(pad);
        }
    }
}