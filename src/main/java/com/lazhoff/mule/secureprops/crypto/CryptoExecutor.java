package com.lazhoff.mule.secureprops.crypto;

import com.lazhoff.mule.secureprops.util.KeyMatcher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

import static com.lazhoff.mule.secureprops.crypto.CryptoExecutionResult.Status.*;

public class CryptoExecutor {

    private static final Logger logger = LogManager.getLogger(CryptoExecutor.class);
    private static final int REPORT_LINE_LEN = 500;

    private final String action;
    private final CryptoConfig baseConfig;
    private final KeyMatcher keyMatcher;

    public CryptoExecutor(String action, CryptoConfig baseConfig, String envKeyMappingArg) {
        this.action = action.toLowerCase();
        this.baseConfig = baseConfig;
        this.keyMatcher = new KeyMatcher(KeyMatcher.parseMapping(envKeyMappingArg));
    }

    public List<CryptoExecutionResult> execute() {
        List<CryptoExecutionResult> results = new ArrayList<>();
        Path root = Paths.get(baseConfig.getFilePath());

        if (!Files.exists(root)) {
            logger.error("Path does not exist: {}", root);
            return List.of(new CryptoExecutionResult(root, FAILED, "Path does not exist", null));
        }

        try {
            List<Path> filesToProcess = Files.walk(root)
                    .filter(Files::isRegularFile)
                    .filter(this::isSupportedFileType)
                    .collect(Collectors.toList());

            logger.info("Discovered {} file(s) to evaluate.", filesToProcess.size());

            for (Path file : filesToProcess) {
                Optional<String> matchedKey = keyMatcher.match(file);
                if (matchedKey.isEmpty()) {
                    logger.warn("Skipping file (no matching key): {}", file);
                    results.add(new CryptoExecutionResult(file, SKIPPED, "No matching key", null));
                    continue;
                }

                CryptoConfig config = new CryptoConfig(
                        baseConfig.getFileOrLine(),
                        file.toString(),
                        baseConfig.getAlgorithm(),
                        baseConfig.getMode(),
                        matchedKey.get(),
                        baseConfig.isUseRandomIV(),
                        baseConfig.getTempFolder(),
                        baseConfig.isDryRun(),
                        baseConfig.isDebug(),
                        baseConfig.isBackup(),
                        baseConfig.getSecureAttributeNameRegex()
                );

                ICryptoService service = resolveService(file, config);
                if (service == null) {
                    logger.warn("Unsupported file type: {}", file);
                    results.add(new CryptoExecutionResult(file, SKIPPED, "Unsupported file type", null));
                    continue;
                }

                try {
                    if (baseConfig.isDryRun()) {
                        String original = Files.readString(file);
                        String processed = getProcessedContent(service, original);

                        if (!original.equals(processed)) {
                            results.add(new CryptoExecutionResult(
                                    file, DRYRUN_CHANGED, "Would change", summarizeDiff(original, processed))
                            );
                        } else {
                            results.add(new CryptoExecutionResult(file, UNCHANGED, "No change", null));
                        }

                    } else {
                        CryptoExecutionResult.Status status = executeService(service);
                        results.add(new CryptoExecutionResult(file, status, "Processed", null));
                    }

                } catch (Exception e) {
                    logger.error("Failed to process file {}: {}", file, e.getMessage(), e);
                    results.add(new CryptoExecutionResult(file, FAILED, e.getMessage(), null));
                }
            }

        } catch (IOException e) {
            logger.error("Error traversing files: {}", e.getMessage(), e);
            results.add(new CryptoExecutionResult(root, FAILED, e.getMessage(), null));
        }

        return results;
    }

    private CryptoExecutionResult.Status executeService(ICryptoService service) {
        if ("encrypt".equals(action)) {
            return service.encrypt();
        } else if ("decrypt".equals(action)) {
            return service.decrypt();
        } else {
            throw new IllegalArgumentException("Unknown action: " + action);
        }
    }

    private String getProcessedContent(ICryptoService service, String original) throws Exception {
        if (service instanceof SupportsPreview previewer) {
            return "encrypt".equals(action)
                    ? previewer.previewEncrypt(original)
                    : previewer.previewDecrypt(original);
        } else {
            throw new UnsupportedOperationException("Service does not support preview: " + service.getClass().getSimpleName());
        }
    }

    private boolean isSupportedFileType(Path file) {
        String name = file.getFileName().toString().toLowerCase();
        return name.endsWith(".json") || name.endsWith(".yaml") || name.endsWith(".yml");
    }

    private ICryptoService resolveService(Path file, CryptoConfig config) {
        if (baseConfig.getFileOrLine() == CryptoConfig.FileOrLine.WHOLE_FILE) {
            return new DefaultCryptoServiceFileLevel(config);
        }

        String name = file.getFileName().toString().toLowerCase();
        if (name.endsWith(".json")) {
            return new DefaultCryptoServiceJson(config);
        } else if (name.endsWith(".yaml") || name.endsWith(".yml")) {
            return new DefaultCryptoServiceYaml(config);
        }

        return null;
    }

    private String summarizeDiff(String original, String modified) {
        String[] origLines = original.split("\n");
        String[] modLines = modified.split("\n");
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < Math.min(origLines.length, modLines.length); i++) {
            if (!origLines[i].equals(modLines[i])) {
                sb.append("- ").append(origLines[i]).append("\n");
                sb.append("+ ").append(modLines[i]).append("\n");
                if (sb.length() > REPORT_LINE_LEN) break;
            }
        }

        return sb.toString();
    }
}
