package org.example.launcher.net;

import com.google.gson.Gson;
import org.example.launcher.minecraft.VersionManifest;

public class VersionManifestDownloader {
    // En güncel Mojang Manifest URL'si
    private static final String MANIFEST_URL = "https://launchermeta.mojang.com/mc/game/version_manifest_v2.json";

    public static VersionManifest downloadManifest() throws Exception {
        // HttpUtil sınıfındaki get metodunu kullanarak JSON çekiyoruz
        String json = HttpUtil.get(MANIFEST_URL);

        if (json == null || json.isEmpty()) {
            throw new RuntimeException("Manifest verisi boş geldi!");
        }

        return new Gson().fromJson(json, VersionManifest.class);
    }
}