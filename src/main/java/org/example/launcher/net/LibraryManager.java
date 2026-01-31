package org.example.launcher.net;

import com.google.gson.Gson;
import org.example.launcher.minecraft.VersionDetails;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LibraryManager {

    public static VersionDetails downloadVersionDetails(String url, Path baseDir) {
        // ... (Bu kısım aynı, dokunmana gerek yok)
        try {
            if (url.startsWith("http")) {
                Path tempJson = baseDir.resolve("temp_" + System.currentTimeMillis() + ".json");
                FileDownloader.download(url, tempJson, p -> {});
                VersionDetails details = new Gson().fromJson(Files.readString(tempJson), VersionDetails.class);
                Files.deleteIfExists(tempJson);
                return details;
            }
            Path localPath = url.startsWith("file:") ? Path.of(java.net.URI.create(url)) : Path.of(url);
            if (Files.exists(localPath)) {
                return new Gson().fromJson(Files.readString(localPath), VersionDetails.class);
            }
            throw new Exception("JSON dosyası bulunamadı: " + url);
        } catch (Exception e) {
            throw new RuntimeException("JSON Okuma Hatası: " + e.getMessage());
        }
    }

    public static String downloadLibraries(VersionDetails details, Path baseDir, String realVersion) {
        List<Path> libPaths = new ArrayList<>();

        // 1. Ana Oyun JAR'ı
        Path vanillaJar = baseDir.resolve("versions").resolve(realVersion).resolve(realVersion + ".jar");
        if (Files.exists(vanillaJar)) libPaths.add(vanillaJar.toAbsolutePath());

        if (details.libraries != null) {
            for (VersionDetails.Library lib : details.libraries) {
                // --- KRİTİK DEĞİŞİKLİK BAŞLANGICI ---

                // A) Ana kütüphaneyi ekle
                processLibrary(lib, null, baseDir, libPaths);

                // B) EĞER VARSA NATIVE (CLASSIFIER) KÜTÜPHANELERİ DE EKLE
                // Modern sürümlerde GLFW hatasını bu kısım çözer!
                if (lib.downloads != null && lib.downloads.classifiers != null) {
                    String os = System.getProperty("os.name").toLowerCase();
                    String classifierKey = os.contains("win") ? "natives-windows" :
                            (os.contains("mac") ? "natives-macos" : "natives-linux");

                    // Windows x64 kontrolü (Örn: lwjgl-glfw-natives-windows.jar)
                    if (lib.downloads.classifiers.containsKey(classifierKey)) {
                        processLibrary(lib, classifierKey, baseDir, libPaths);
                    }
                    // Bazı paketlerde "natives-windows-x86_64" yazar
                    String x64Key = classifierKey + "-x86_64";
                    if (lib.downloads.classifiers.containsKey(x64Key)) {
                        processLibrary(lib, x64Key, baseDir, libPaths);
                    }
                }
                // --- KRİTİK DEĞİŞİKLİK BİTİŞİ ---
            }
        }

        return libPaths.stream()
                .map(Path::toString)
                .distinct()
                .collect(Collectors.joining(File.pathSeparator));
    }

    // Tekrarlanan indirme ve listeye ekleme mantığını bir metoda topladık
    private static void processLibrary(VersionDetails.Library lib, String classifier, Path baseDir, List<Path> libPaths) {
        String pathStr;
        String downloadUrl;

        if (classifier == null) {
            // Normal Artifact
            if (lib.downloads != null && lib.downloads.artifact != null) {
                pathStr = lib.downloads.artifact.path;
                downloadUrl = lib.downloads.artifact.url;
            } else {
                pathStr = convertNameToPath(lib.name, null);
                downloadUrl = (lib.url != null ? lib.url : "https://libraries.minecraft.net/") + pathStr;
            }
        } else {
            // Native Classifier
            VersionDetails.Artifact artifact = lib.downloads.classifiers.get(classifier);
            pathStr = artifact.path != null ? artifact.path : convertNameToPath(lib.name, classifier);
            downloadUrl = artifact.url;
        }

        if (pathStr == null) return;

        Path libPath = baseDir.resolve("libraries").resolve(pathStr.replace("/", File.separator));
        try {
            if (!Files.exists(libPath) && downloadUrl != null && !downloadUrl.isEmpty()) {
                Files.createDirectories(libPath.getParent());
                System.out.println("📥 Kütüphane İndiriliyor: " + (classifier != null ? classifier + " " : "") + lib.name);
                FileDownloader.download(downloadUrl, libPath, p -> {});
            }
            if (Files.exists(libPath)) {
                libPaths.add(libPath.toAbsolutePath());
            }
        } catch (Exception e) {
            System.err.println("⚠️ Kütüphane hatası: " + e.getMessage());
        }
    }

    private static String convertNameToPath(String name, String classifier) {
        try {
            String[] parts = name.split(":");
            String group = parts[0].replace(".", "/");
            String artifact = parts[1];
            String version = parts[2];
            String suffix = (classifier != null) ? "-" + classifier : "";
            return group + "/" + artifact + "/" + version + "/" + artifact + "-" + version + suffix + ".jar";
        } catch (Exception e) {
            return null;
        }
    }
}