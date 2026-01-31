package org.example.launcher.net;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.*;
import java.util.Map;

public class AssetManager {

    public static void downloadAssets(String assetIndexUrl, Path gameDir) throws Exception {
        Gson gson = new Gson();

        JsonObject index = gson.fromJson(
                new InputStreamReader(new URL(assetIndexUrl).openStream()),
                JsonObject.class
        );

        JsonObject objects = index.getAsJsonObject("objects");
        Path objectsDir = gameDir.resolve("assets/objects");

        for (Map.Entry<String, com.google.gson.JsonElement> e : objects.entrySet()) {
            JsonObject obj = e.getValue().getAsJsonObject();
            String hash = obj.get("hash").getAsString();
            String sub = hash.substring(0, 2);

            Path out = objectsDir.resolve(sub).resolve(hash);
            if (Files.exists(out)) continue;

            Files.createDirectories(out.getParent());
            URL url = new URL("https://resources.download.minecraft.net/" + sub + "/" + hash);
            Files.copy(url.openStream(), out, StandardCopyOption.REPLACE_EXISTING);
        }

        System.out.println("Assetler indirildi");
    }
}
