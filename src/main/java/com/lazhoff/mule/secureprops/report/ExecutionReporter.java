package com.lazhoff.mule.secureprops.report;

import com.lazhoff.mule.secureprops.crypto.CryptoExecutionResult;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class ExecutionReporter {

    private static final int REPORT_LINE_LEN = 256;

    private final String action;
    private final boolean dryRun;
    private final List<CryptoExecutionResult> results;

    public ExecutionReporter(String action, boolean dryRun, List<CryptoExecutionResult> results) {
        this.action = action.toLowerCase();
        this.dryRun = dryRun;
        this.results = results;
    }

    public void printReport() {
        Map<Path, List<CryptoExecutionResult>> grouped = results.stream()
                .collect(Collectors.groupingBy(r -> r.getFile().getParent(), TreeMap::new, Collectors.toList()));

        System.out.println("\n--- Secure Properties Detailed Report ---");
        for (Map.Entry<Path, List<CryptoExecutionResult>> entry : grouped.entrySet()) {
            System.out.println(entry.getKey() + ":");
            for (CryptoExecutionResult result : entry.getValue()) {
                System.out.printf("  %s: %s%n", result.getFile().getFileName(), result.getStatus());
            }
        }

        long success = results.stream().filter(r -> r.getStatus() == CryptoExecutionResult.Status.SUCCESS).count();
        long skipped = results.stream().filter(r -> r.getStatus() == CryptoExecutionResult.Status.SKIPPED).count();
        long failed = results.stream().filter(r -> r.getStatus() == CryptoExecutionResult.Status.FAILED).count();
        long unchanged = results.stream().filter(r -> r.getStatus() == CryptoExecutionResult.Status.UNCHANGED).count();
        long wouldChange = results.stream().filter(r -> r.getStatus() == CryptoExecutionResult.Status.DRYRUN_CHANGED).count();

        System.out.println("\n--- Summary ---");
        System.out.println("Action:            " + action.toUpperCase());
        System.out.println("Processed files:   " + results.size());
        System.out.println("Success:           " + success);
        System.out.println("Skipped:           " + skipped);
        System.out.println("Failed:            " + failed);
        if (dryRun) {
            System.out.println("Dry-run changed:   " + wouldChange);
            System.out.println("Dry-run unchanged: " + unchanged);
        }

        if (failed > 0) {
            System.out.println("\n--- Failed Files ---");
            results.stream()
                    .filter(r -> r.getStatus() == CryptoExecutionResult.Status.FAILED)
                    .forEach(r -> System.out.printf(" - %s: %s%n", r.getFile(), r.getMessage()));
        }

        if (dryRun && wouldChange > 0) {
            System.out.println("\n--- Files That Would Change ---");
            results.stream()
                    .filter(r -> r.getStatus() == CryptoExecutionResult.Status.DRYRUN_CHANGED)
                    .forEach(r -> {
                        System.out.println(" - " + r.getFile());
                        String preview = r.getDiffPreview();
                        if (preview != null) {
                            if (preview.length() > REPORT_LINE_LEN) {
                                System.out.println("   Preview:\n" + preview.substring(0, REPORT_LINE_LEN) + "\n   [ ...truncated... ]");
                            } else {
                                System.out.println("   Preview:\n" + preview);
                            }
                        }
                    });
        }


        System.out.println("------------------------------------------");
    }

    public int getExitCode() {
        boolean anyFailed = results.stream().anyMatch(r -> r.getStatus() == CryptoExecutionResult.Status.FAILED);
        boolean anyWouldChange = results.stream().anyMatch(r -> r.getStatus() == CryptoExecutionResult.Status.DRYRUN_CHANGED);

        if (anyFailed) return 10;
        if (dryRun && anyWouldChange) return 20;
        return 0;
    }
}
