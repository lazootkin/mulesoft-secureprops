package com.lazhoff.mule.secureprops;

import com.lazhoff.mule.secureprops.crypto.CryptoConfig;
import com.lazhoff.mule.secureprops.crypto.CryptoExecutor;
import com.lazhoff.mule.secureprops.crypto.CryptoExecutionResult;
import com.lazhoff.mule.secureprops.report.ExecutionReporter;
import com.lazhoff.mule.secureprops.ui.MainUI;
import com.lazhoff.mule.secureprops.util.TempFileManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.List;

public class SecurePropertiesCLI {

    private static final Logger logger = LogManager.getLogger(SecurePropertiesCLI.class);


    public static void main(String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("--ui")) {
            MainUI.main(new String[0]);
            return;
        }

        if (args.length < 7) {
            System.err.println("""
                Usage:
                  java -jar secure-tool.jar <encrypt|decrypt> <file|file-level> <filePath> <algorithm> <mode> <useRandomIV:true|false> --envKeyMapping=(regex):(key),...

                Optional:
                   [--dryRun] [--debug] [--noBackup] [--rex=REGEX] [--tmp=TempFolder]
                """);
            System.exit(1);
        }

        String action = args[0].toLowerCase();               // encrypt or decrypt
        String fileOrLineArg = args[1].toLowerCase();        // file or file-level
        String filePath = args[2];
        String algorithm = args[3];
        String mode = args[4];
        boolean useRandomIV = Boolean.parseBoolean(args[5]);

        String envKeyMapping = getValue(args, "--envKeyMapping=", null);
        if (envKeyMapping == null) {
            System.err.println("Missing required argument: --envKeyMapping");
            System.exit(2);
        }

        boolean dryRun = getFlag(args, "--dryRun");
        boolean debug = getFlag(args, "--debug");
        boolean backup = !getFlag(args, "--noBackup");
        String regex = getValue(args, "--rex=", ".*");
        String tempDir = getValue(args, "--tmp=", TempFileManager.getSystemPathDir().toAbsolutePath().toString());

        logger.info("tempDir:{}",tempDir);

        CryptoConfig.FileOrLine fileOrLine;
        try {
            fileOrLine = CryptoConfig.FileOrLine.fromString(fileOrLineArg);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid file type: " + fileOrLineArg + " — expected: file or file-level");
            System.exit(3);
            return;
        }

        CryptoConfig config = new CryptoConfig(
                fileOrLine,
                filePath,
                algorithm,
                mode,
                "PLACEHOLDER", // Key will be resolved from envKeyMapping
                useRandomIV,
                Path.of(tempDir),
                dryRun,
                debug,
                backup,
                regex
        );

        CryptoExecutor executor = new CryptoExecutor(action, config, envKeyMapping);
        List<CryptoExecutionResult> results = executor.execute();

        ExecutionReporter reporter = new ExecutionReporter(action, dryRun, results);
        reporter.printReport();

        System.exit(reporter.getExitCode());
    }

    private static boolean getFlag(String[] args, String name) {
        for (String arg : args) {
            if (arg.equalsIgnoreCase(name)) return true;
        }
        return false;
    }

    private static String getValue(String[] args, String prefix, String defaultValue) {
        for (String arg : args) {
            if (arg.startsWith(prefix)) {
                return arg.substring(prefix.length());
            }
        }
        return defaultValue;
    }
}
