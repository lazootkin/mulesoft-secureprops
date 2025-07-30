package com.lazhoff.mule.secureprops.util;


import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FileWalker {
    public static List<Path> walkFiles(Path root, Predicate<Path> filter) throws IOException {
        return Files.walk(root)
                .filter(Files::isRegularFile)
                .filter(filter)
                .collect(Collectors.toList());
    }
}
