package org.example.launcher.modrinth;

import com.google.gson.*;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ModrinthService {
    private static final String API_URL = "https://api.modrinth.com/v2/search";

    /**
     * Modları aramak için kullanılır.
     */
    public static List<ModrinthMod> searchMods(String version, String loader, String queryText) {
        List<ModrinthMod> mods = new ArrayList<>();
        try {
            String q = (queryText != null && !queryText.isEmpty()) ? "&query=" + URLEncoder.encode(queryText, StandardCharsets.UTF_8) : "";

            // Filtreleri güvenli URL formatına çeviriyoruz
            String facetsRaw = "[[\"versions:" + version + "\"],[\"categories:" + loader + "\"]]";
            String facets = URLEncoder.encode(facetsRaw, StandardCharsets.UTF_8);

            URL url = new URL(API_URL + "?facets=" + facets + q);
            InputStreamReader reader = new InputStreamReader(url.openStream());

            JsonObject response = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray hits = response.getAsJsonArray("hits");

            Gson gson = new Gson();
            for (JsonElement hit : hits) {
                mods.add(gson.fromJson(hit, ModrinthMod.class));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mods;
    }

    /**
     * Belirli bir modun indirme linkini getirir.
     * HTTP 400 hatasını önlemek için parametreler encode edilmiştir.
     */
    public static String getDownloadUrl(String projectId, String mcVersion, String loader) {
        try {
            // Modrinth API'sinin beklediği format: ["fabric"] ve ["1.21.1"]
            String ldrParam = URLEncoder.encode("[\"" + loader + "\"]", StandardCharsets.UTF_8);
            String verParam = URLEncoder.encode("[\"" + mcVersion + "\"]", StandardCharsets.UTF_8);

            String urlString = String.format("https://api.modrinth.com/v2/project/%s/version?loaders=%s&game_versions=%s",
                    projectId, ldrParam, verParam);

            URL url = new URL(urlString);
            InputStreamReader reader = new InputStreamReader(url.openStream());
            JsonArray versions = JsonParser.parseReader(reader).getAsJsonArray();

            if (versions != null && versions.size() > 0) {
                // En güncel sürümün ilk dosyasını çekiyoruz
                return versions.get(0).getAsJsonObject()
                        .getAsJsonArray("files").get(0).getAsJsonObject()
                        .get("url").getAsString();
            }
        } catch (Exception e) {
            System.err.println("Modrinth API Dosya Hatası: " + e.getMessage());
        }
        return null;
    }
}