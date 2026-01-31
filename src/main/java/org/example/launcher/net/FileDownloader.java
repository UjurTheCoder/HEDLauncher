package org.example.launcher.net;

import javafx.application.Platform;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.DoubleConsumer;

public class FileDownloader {

    // progressCallback: Yüzde değiştikçe çalışacak olan fonksiyon
    public static void download(String urlStr, Path target, DoubleConsumer progressCallback) {
        try {
            Files.createDirectories(target.getParent());
            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Dosya boyutunu alıyoruz
            long fileSize = connection.getContentLengthLong();

            try (InputStream in = connection.getInputStream();
                 OutputStream out = Files.newOutputStream(target)) {

                byte[] buffer = new byte[8192]; // 8KB'lık parçalarla oku
                long totalRead = 0;
                int read;

                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                    totalRead += read;

                    if (fileSize > 0) {
                        double progress = (double) totalRead / fileSize;
                        // Arayüzü (JavaFX) güvenli bir şekilde güncelle
                        Platform.runLater(() -> progressCallback.accept(progress));
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("İndirme hatası: " + e.getMessage(), e);
        }
    }
}