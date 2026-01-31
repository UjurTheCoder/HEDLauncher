package org.example.launcher;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.example.launcher.minecraft.MinecraftLauncher;
import org.example.launcher.minecraft.VersionManifest;
import org.example.launcher.net.VersionManifestDownloader;
import org.example.launcher.net.FileDownloader; // Önemli: Senin profesyonel downloader'ın
import org.example.launcher.utils.SettingsManager;
import org.example.launcher.modrinth.ModrinthService;
import org.example.launcher.modrinth.ModrinthMod;

import java.awt.Desktop;
import java.io.File;
import java.nio.file.Path;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import java.util.List;

public class Main extends Application {
    private ComboBox<VersionManifest.Version> versionComboBox;
    private TextField nameField;
    private ProgressBar progressBar;
    private CheckBox fabricToggle;

    @Override
    public void start(Stage primaryStage) {
        SettingsManager.load();
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);

        try {
            Image bgImage = new Image(getClass().getResourceAsStream("/background.png"));
            BackgroundImage bImg = new BackgroundImage(bgImage, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, new BackgroundSize(100, 100, true, true, false, true));
            root.setBackground(new Background(bImg));
        } catch (Exception e) { root.setStyle("-fx-background-color: #1a1a2e;"); }

        Label titleLabel = new Label("HED LAUNCHER");
        titleLabel.setStyle("-fx-font-size: 38px; -fx-text-fill: white; -fx-font-weight: bold;");

        VBox contentBox = new VBox(15);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7); -fx-background-radius: 25; -fx-padding: 25;");
        contentBox.setMaxWidth(400);

        nameField = new TextField();
        nameField.setPromptText("Kullanıcı Adı...");
        nameField.setStyle("-fx-background-radius: 15; -fx-padding: 10;");

        HBox controlGroup = new HBox(10);
        controlGroup.setAlignment(Pos.CENTER);
        versionComboBox = new ComboBox<>();
        versionComboBox.setPrefWidth(220);
        Button settingsBtn = new Button("⚙");
        settingsBtn.setOnAction(e -> openSettingsWindow());
        controlGroup.getChildren().addAll(versionComboBox, settingsBtn);

        fabricToggle = new CheckBox("Fabric Desteği");
        fabricToggle.setStyle("-fx-text-fill: #00d2ff; -fx-font-weight: bold;");

        Button playBtn = new Button("OYUNA BAŞLA");
        playBtn.setPrefWidth(280);
        playBtn.setPrefHeight(45);
        playBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 15;");
        playBtn.setOnAction(e -> handleLaunch());

        Button modrinthBtn = new Button("🌐 MODLARI KEŞFET");
        modrinthBtn.setPrefWidth(280);
        modrinthBtn.setStyle("-fx-background-color: #1bd96a; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 15;");
        modrinthBtn.setOnAction(e -> openModrinthWindow());

        Button openModsBtn = new Button("📂 Mod Klasörünü Aç");
        openModsBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #bdc3c7; -fx-underline: true;");
        openModsBtn.setOnAction(e -> openModsFolder());

        progressBar = new ProgressBar(0);
        progressBar.setVisible(false);
        progressBar.setPrefWidth(280);

        contentBox.getChildren().addAll(nameField, controlGroup, fabricToggle, playBtn, modrinthBtn, openModsBtn, progressBar);
        root.getChildren().addAll(titleLabel, contentBox);

        primaryStage.setScene(new Scene(root, 600, 600));
        primaryStage.setTitle("HEDLauncher v1.2.0");
        primaryStage.show();
        loadVersions();
    }

    private void openModrinthWindow() {
        Stage stage = new Stage();
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(15));
        layout.setStyle("-fx-background-color: #121212;");

        TextField searchField = new TextField();
        searchField.setPromptText("Mod ara (örn: Sodium) ve Enter'a bas...");
        searchField.setStyle("-fx-background-color: #252525; -fx-text-fill: white; -fx-background-radius: 10;");

        VBox modContainer = new VBox(10);
        ScrollPane scrollPane = new ScrollPane(modContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);
        scrollPane.setStyle("-fx-background: #121212; -fx-background-color: transparent;");

        Runnable refresh = () -> {
            String query = searchField.getText();
            VersionManifest.Version sel = versionComboBox.getValue();
            String ver = (sel != null) ? sel.id : "1.21.1";
            String ldr = fabricToggle.isSelected() ? "fabric" : "minecraft";

            new Thread(() -> {
                List<ModrinthMod> mods = ModrinthService.searchMods(ver, ldr, query);
                Platform.runLater(() -> {
                    modContainer.getChildren().clear();
                    for (ModrinthMod mod : mods) {
                        HBox card = new HBox(10);
                        card.setStyle("-fx-background-color: #1e1e1e; -fx-padding: 10; -fx-background-radius: 10;");
                        card.setAlignment(Pos.CENTER_LEFT);

                        ImageView icon = new ImageView();
                        try { icon.setImage(new Image(mod.icon_url, 40, 40, true, true, true)); } catch (Exception e) {}

                        VBox info = new VBox(2);
                        Label t = new Label(mod.title); t.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
                        Label d = new Label(mod.description); d.setStyle("-fx-text-fill: #888; -fx-font-size: 10px;");
                        d.setWrapText(true); d.setMaxWidth(200);
                        info.getChildren().addAll(t, d);

                        Region s = new Region(); HBox.setHgrow(s, Priority.ALWAYS);
                        Button ins = new Button("YÜKLE");
                        ins.setStyle("-fx-background-color: #1bd96a; -fx-font-size: 10px; -fx-cursor: hand;");

                        ins.setOnAction(e -> {
                            ins.setText("...");
                            ins.setDisable(true);
                            new Thread(() -> {
                                String dUrl = ModrinthService.getDownloadUrl(mod.project_id, ver, ldr);
                                if (dUrl != null) {
                                    try {
                                        Path modsDirPath = Path.of(System.getProperty("user.home"), ".hedlauncher", "instances", ver + (fabricToggle.isSelected() ? "-fabric" : ""), "mods");
                                        File fDir = modsDirPath.toFile();
                                        if (!fDir.exists()) fDir.mkdirs();

                                        String fName = dUrl.substring(dUrl.lastIndexOf('/') + 1);
                                        Path targetFilePath = modsDirPath.resolve(fName);

                                        // HATA GİDERİLDİ: 3 parametreli download kullanımı
                                        FileDownloader.download(dUrl, targetFilePath, progress -> {
                                            // İndirme ilerlemesi buraya gelebilir (0.0 - 1.0 arası)
                                        });

                                        Platform.runLater(() -> {
                                            ins.setText("TAMAM ✅");
                                            ins.setStyle("-fx-background-color: #2ecc71;");
                                        });
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                        Platform.runLater(() -> { ins.setText("HATA"); ins.setDisable(false); });
                                    }
                                } else {
                                    Platform.runLater(() -> { ins.setText("YOK"); ins.setDisable(false); });
                                }
                            }).start();
                        });

                        card.getChildren().addAll(icon, info, s, ins);
                        modContainer.getChildren().add(card);
                    }
                });
            }).start();
        };

        searchField.setOnAction(e -> refresh.run());
        refresh.run();
        layout.getChildren().addAll(new Label("MODRINTH KEŞİF (HED)"), searchField, scrollPane);
        stage.setScene(new Scene(layout, 450, 550));
        stage.show();
    }

    private void loadVersions() {
        new Thread(() -> {
            try {
                var manifest = VersionManifestDownloader.downloadManifest();
                Platform.runLater(() -> {
                    if (manifest != null) versionComboBox.getItems().addAll(manifest.versions);
                    versionComboBox.getSelectionModel().selectFirst();
                });
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private void handleLaunch() {
        String user = nameField.getText().trim();
        VersionManifest.Version sel = versionComboBox.getValue();
        if (user.isEmpty() || sel == null) return;
        progressBar.setVisible(true);
        new Thread(() -> {
            try {
                String fId = fabricToggle.isSelected() ? sel.id + "-fabric" : sel.id;
                String fUrl = fabricToggle.isSelected() ? "https://meta.fabricmc.net/v2/versions/loader/" + sel.id + "/" + getLatestFabricLoader(sel.id) + "/profile/json" : sel.url;
                new MinecraftLauncher().launch(fId, fUrl, user);
            } catch (Exception e) { e.printStackTrace(); }
            finally { Platform.runLater(() -> progressBar.setVisible(false)); }
        }).start();
    }

    private String getLatestFabricLoader(String gv) {
        try {
            HttpResponse<String> r = HttpClient.newHttpClient().send(HttpRequest.newBuilder().uri(URI.create("https://meta.fabricmc.net/v2/versions/loader/" + gv)).build(), HttpResponse.BodyHandlers.ofString());
            return JsonParser.parseString(r.body()).getAsJsonArray().get(0).getAsJsonObject().get("loader").getAsJsonObject().get("version").getAsString();
        } catch (Exception e) { return "0.15.11"; }
    }

    private void openModsFolder() {
        VersionManifest.Version sel = versionComboBox.getValue();
        if (sel == null) return;
        Path p = Path.of(System.getProperty("user.home"), ".hedlauncher", "instances", sel.id + (fabricToggle.isSelected()?"-fabric":""), "mods");
        try { File f = p.toFile(); if (!f.exists()) f.mkdirs(); Desktop.getDesktop().open(f); } catch (Exception e) {}
    }

    private void openSettingsWindow() {
        Stage s = new Stage();
        VBox l = new VBox(10); l.setPadding(new Insets(20)); l.setAlignment(Pos.CENTER);
        Slider r = new Slider(1024, 16384, SettingsManager.getConfig().ramAmount);
        Label v = new Label((SettingsManager.getConfig().ramAmount/1024) + " GB");
        r.valueProperty().addListener((o, ov, nv) -> { int val = (nv.intValue()/512)*512; v.setText((val/1024.0)+" GB"); SettingsManager.getConfig().ramAmount=val; });
        Button b = new Button("Kaydet"); b.setOnAction(e -> { SettingsManager.save(); s.close(); });
        l.getChildren().addAll(new Label("RAM Limit (MB):"), r, v, b);
        s.setScene(new Scene(l, 300, 200)); s.show();
    }

    public static void main(String[] args) { launch(args); }
}