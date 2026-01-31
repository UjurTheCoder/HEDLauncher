package org.example.launcher.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SettingsManager {
    private static final Path SETTINGS_PATH = Path.of(System.getProperty("user.home"), ".hedlauncher", "settings.json");
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static Config currentConfig;

    public static class Config {
        public int ramAmount = 4096; // Varsayılan 4GB
    }

    // Bu metodu Main.java başlatılırken çağırmalısın
    public static void load() {
        try {
            if (Files.exists(SETTINGS_PATH)) {
                currentConfig = gson.fromJson(Files.readString(SETTINGS_PATH), Config.class);
            } else {
                currentConfig = new Config();
                save();
            }
        } catch (IOException e) {
            currentConfig = new Config();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(SETTINGS_PATH.getParent());
            Files.writeString(SETTINGS_PATH, gson.toJson(currentConfig));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Config getConfig() {
        if (currentConfig == null) load();
        return currentConfig;
    }
}