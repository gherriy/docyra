package com.docyra.backend.document;

public record DocumentResponse(Long id, String originalFilename) {

    public static DocumentResponse from(Document document) {
        return new DocumentResponse(document.getId(), document.getOriginalFilename());
    }
}