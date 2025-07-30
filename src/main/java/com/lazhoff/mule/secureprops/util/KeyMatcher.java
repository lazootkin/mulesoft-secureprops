package com.lazhoff.mule.secureprops.util;

import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

public class KeyMatcher {
    private final Map<Pattern, String> patternToKey;

    public KeyMatcher(Map<Pattern, String> patternToKey) {
        this.patternToKey = patternToKey;
    }

    public Optional<String> match(Path file) {
        String filename = file.getFileName().toString();
        return patternToKey.entrySet().stream()
                .filter(entry -> entry.getKey().matcher(filename).matches())
                .map(Map.Entry::getValue)
                .findFirst();
    }

    public static Map<Pattern, String> parseMapping(String arg) {
        Map<Pattern, String> map = new LinkedHashMap<>();
        if (arg == null || arg.isBlank()) return map;

        String[] entries = arg.split("(?<=\\)),(?=\\()");
        for (String entry : entries) {
            String[] parts = entry.split("\\):\\(");
            if (parts.length == 2) {
                String patternStr = parts[0].replaceFirst("^\\(", "");
                String key = parts[1].replaceFirst("\\)$", "");
                map.put(Pattern.compile(patternStr), key);
            }
        }
        return map;
    }
}
