package com.docyra.backend.document;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ImageStorageService {

    private final Path storageDir;

    public ImageStorageService(@Value("${docyra.storage.dir}") String storageDir) {
        this.storageDir = Path.of(storageDir);
    }

    public String store(InputStream image, String extension) throws IOException {
        Files.createDirectories(storageDir);
        String filename = UUID.randomUUID() + "." + extension;
        Files.copy(image, storageDir.resolve(filename));
        return filename;
    }
}