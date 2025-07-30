package com.lazhoff.mule.secureprops.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class UISettingsPersistence {

    private static final Logger logger = LogManager.getLogger(UISettingsPersistence.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    public static UISettings loadSettings(Path configPath) {
        try {
            if (Files.exists(configPath)) {
                logger.info("Loading settings from {}", configPath);
                return mapper.readValue(Files.readString(configPath), UISettings.class);
            } else {
                logger.warn("Settings file not found at {}, using default.", configPath);
            }
        } catch (IOException e) {
            logger.error("Failed to load settings from {}: {}", configPath, e.getMessage());
        }
        return new UISettings(); // return default if not found or error
    }

    public static void saveSettings(Path configPath, UISettings settings) {
        try {
            Files.createDirectories(configPath.getParent());
            String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(settings);
            Files.writeString(configPath, json);
            logger.info("Settings saved to {}", configPath);
        } catch (IOException e) {
            logger.error("Failed to save settings to {}: {}", configPath, e.getMessage());
        }
    }
}
