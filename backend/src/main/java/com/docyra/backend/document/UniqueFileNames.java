package com.docyra.backend.document;

import java.util.HashMap;
import java.util.Map;

public class UniqueFileNames {

    private final Map<String, Integer> counts = new HashMap<>();

    public String next(String fileName) {
        int count = counts.merge(fileName, 1, Integer::sum);
        if (count == 1) {
            return fileName;
        }
        String base = fileName.substring(0, fileName.length() - ".pdf".length());
        return base + "_" + count + ".pdf";
    }
}