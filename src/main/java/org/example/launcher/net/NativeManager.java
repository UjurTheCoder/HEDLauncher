package org.example.launcher.net;

import org.example.launcher.minecraft.VersionDetails;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class NativeManager {

    public static Path downloadAndExtractNatives(VersionDetails details, Path baseDir, String versionId) {
        // Klasör: .hedlauncher/versions/SÜRÜM/natives
        Path nativesDir = baseDir.resolve("versions").resolve(versionId).resolve("natives");

        try {
            if (!Files.exists(nativesDir)) Files.createDirectories(nativesDir);

            if (details.libraries != null) {
                for (VersionDetails.Library lib : details.libraries) {
                    // LWJGL kütüphanelerini bul (Çünkü GLFW buradadır)
                    if (lib.name.contains("lwjgl")) {
                        // Kütüphane yolunu manuel oluştur (Hata payını sıfırlamak için)
                        String[] parts = lib.name.split(":");
                        Path jarFile = baseDir.resolve("libraries")
                                .resolve(parts[0].replace(".", "/"))
                                .resolve(parts[1])
                                .resolve(parts[2])
                                .resolve(parts[1] + "-" + parts[2] + ".jar");

                        if (Files.exists(jarFile)) {
                            extractNatives(jarFile, nativesDir);
                        }
                    }
                }
            }
            System.out.println("✅ Natives klasörü hazırlandı: " + nativesDir.toAbsolutePath());
        } catch (Exception e) { e.printStackTrace(); }
        return nativesDir;
    }

    private static void extractNatives(Path jarFile, Path destDir) {
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(jarFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().endsWith(".dll") || entry.getName().endsWith(".so") || entry.getName().endsWith(".dylib")) {
                    String fileName = new File(entry.getName()).getName();
                    Files.copy(zis, destDir.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
                }
                zis.closeEntry();
            }
        } catch (Exception e) {}
    }
}