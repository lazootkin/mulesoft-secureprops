
package com.lazhoff.mule.secureprops.ui;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.File;
import java.util.Map;

public class UIFileLoader {

    private static final Logger LOG = LogManager.getLogger(UIFileLoader.class);
    private static final String[] SUPPORTED_EXTENSIONS = {".json", ".yaml", ".yml"};

    public static void loadFiles(File folder, DefaultListModel<String> listModel, Map<String, File> fileMap) {
        LOG.debug("Loading files from folder: {}", folder.getAbsolutePath());
        listModel.clear();
        fileMap.clear();

        File[] files = folder.listFiles((dir, name) -> {
            String lower = name.toLowerCase();
            for (String ext : SUPPORTED_EXTENSIONS) {
                if (lower.endsWith(ext)) {
                    return true;
                }
            }
            return false;
        });

        if (files == null || files.length == 0) {
            LOG.debug("No supported files found.");
        } else {
            for (File file : files) {
                listModel.addElement(file.getName());
                fileMap.put(file.getName(), file);
            }
            LOG.debug("Loaded {} files into listModel", files.length);
        }
    }
}
