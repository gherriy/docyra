package com.docyra.backend.document;

public record DocumentResponse(Long id, String originalFilename, String extractedText) {

    public static DocumentResponse from(Document document) {
        return new DocumentResponse(document.getId(), document.getOriginalFilename(), document.getExtractedText());
    }
}