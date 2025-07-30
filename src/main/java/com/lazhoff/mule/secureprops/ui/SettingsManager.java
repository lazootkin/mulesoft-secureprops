
package com.lazhoff.mule.secureprops.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SettingsManager {

    private static final Logger LOG = LogManager.getLogger(SettingsManager.class);
  //  private static final Path CONFIG_PATH = Path.of(System.getProperty("user.home"), ".secureprops", "config.json");
    private static final Path CONFIG_PATH = getJarDirectory().resolve(".secureprops").resolve("config.json");

    private static Path getJarDirectory() {
        try {
            return new File(MainUI.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI())
                    .getParentFile()
                    .toPath();
        } catch (Exception e) {
            throw new RuntimeException("Failed to determine JAR directory", e);
        }
    }



    public static UISettings load() {
        try {
            if (Files.exists(CONFIG_PATH)) {
                LOG.info("Loading config from: {}", CONFIG_PATH);
                return new ObjectMapper().readValue(CONFIG_PATH.toFile(), UISettings.class);
            } else {
                LOG.info("No config found, returning default UISettings.");
                return new UISettings();
            }
        } catch (IOException e) {
            LOG.error("Failed to load settings: {}", CONFIG_PATH, e);
            return new UISettings();
        }
    }

    public static void save(UISettings settings) {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            new ObjectMapper()
                    .writerWithDefaultPrettyPrinter()
                    .writeValue(CONFIG_PATH.toFile(), settings);
            LOG.info("Settings saved to: {}", CONFIG_PATH);
        } catch (IOException e) {
            LOG.error("Failed to save settings: {}", CONFIG_PATH, e);
        }
    }

    public static void openSettingsDialog(JFrame parent) {
        UISettings settings = load();

        JTextArea textArea = new JTextArea(settings.toString(), 20, 60);
        JScrollPane scrollPane = new JScrollPane(textArea);

        int option = JOptionPane.showConfirmDialog(
                parent, scrollPane, "Edit Configuration (JSON)",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );

        if (option == JOptionPane.OK_OPTION) {
            try {
                UISettings updated = new ObjectMapper().readValue(textArea.getText(), UISettings.class);
                save(updated);
                JOptionPane.showMessageDialog(parent, "Configuration saved.", "Info", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                LOG.error("Invalid JSON in config dialog", e);
                JOptionPane.showMessageDialog(parent, "Invalid JSON: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
