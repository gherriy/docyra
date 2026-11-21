package com.docyra.backend.document;

import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentService {

    private static final Map<String, String> ALLOWED_TYPES = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png");

    private final DocumentRepository documentRepository;
    private final ImageStorageService storageService;

    public DocumentService(DocumentRepository documentRepository, ImageStorageService storageService) {
        this.documentRepository = documentRepository;
        this.storageService = storageService;
    }

    public Document upload(MultipartFile file) throws IOException {
        String extension = ALLOWED_TYPES.get(file.getContentType());
        if (extension == null) {
            throw new InvalidImageException("Seules les images JPG et PNG sont acceptées");
        }

        String storedFilename = storageService.store(file.getInputStream(), extension);
        Document document = new Document(file.getOriginalFilename(), storedFilename, file.getContentType());
        return documentRepository.save(document);
    }
}