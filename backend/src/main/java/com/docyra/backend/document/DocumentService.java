package com.docyra.backend.document;

import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.docyra.backend.ocr.OcrService;

@Service
public class DocumentService {

    private static final Map<String, String> ALLOWED_TYPES = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png");

    private final DocumentRepository documentRepository;
    private final ImageStorageService storageService;
    private final OcrService ocrService;

    public DocumentService(DocumentRepository documentRepository, ImageStorageService storageService,
            OcrService ocrService) {
        this.documentRepository = documentRepository;
        this.storageService = storageService;
        this.ocrService = ocrService;
    }

    public Document upload(MultipartFile file) throws IOException {
        String extension = ALLOWED_TYPES.get(file.getContentType());
        if (extension == null) {
            throw new InvalidImageException("Seules les images JPG et PNG sont acceptées");
        }

        String storedFilename = storageService.store(file.getInputStream(), extension);
        Document document = new Document(file.getOriginalFilename(), storedFilename, file.getContentType());
        document.setExtractedText(ocrService.extractText(storageService.resolve(storedFilename)));

        return documentRepository.save(document);
    }
}