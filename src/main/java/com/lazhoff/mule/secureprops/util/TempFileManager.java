package com.lazhoff.mule.secureprops.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.*;

public class TempFileManager {

    private static final Logger logger = LogManager.getLogger(TempFileManager.class);

    private final Path tempDir;

    public TempFileManager(Path tempDir) {
        logger.debug("tempDir: {}",tempDir.toAbsolutePath().toString());
        this.tempDir = tempDir;
    }

    public static Path getSystemPathDir() { return Path.of(System.getProperty("java.io.tmpdir")); }

    public Path createTempSecureYamlFile() throws IOException {
        return Files.createTempFile(tempDir, "secure-yaml-", ".tmp.yaml");
    }

    public Path createTempSecureFile() throws IOException {
        return Files.createTempFile(tempDir, "secure-file-", ".tmp");
    }

    public Path createTempPreviewInYamlFile() throws IOException {
        return Files.createTempFile(tempDir, "preview-yaml-", ".in.yaml");
    }

    public Path createTempPreviewOutYamlFile() throws IOException {
        return Files.createTempFile(tempDir, "preview-yaml-", ".out.yaml");
    }

    public void cleanAllTempFiles() {
        cleanByPattern("secure-yaml-", ".tmp.yaml");
        cleanByPattern("preview-yaml-", ".in.yaml");
        cleanByPattern("preview-yaml-", ".out.yaml");
        cleanByPattern("secure-file-", ".tmp");
    }

    private void cleanByPattern(String prefix, String suffix) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(tempDir)) {
            for (Path file : stream) {
                String name = file.getFileName().toString();
                if (name.startsWith(prefix) && name.endsWith(suffix)) {
                    Files.deleteIfExists(file);
                    System.out.println("Deleted: " + file);
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to clean files: " + e.getMessage());
        }
    }
}