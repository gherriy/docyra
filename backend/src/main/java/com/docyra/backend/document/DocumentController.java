package com.docyra.backend.document;

import java.io.IOException;
import java.util.List;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentResponse upload(@RequestParam("file") MultipartFile file) throws IOException {
        var result = documentService.upload(file);

        return DocumentResponse.from(result.document(), result.candidates());
    }

    @PutMapping("/{id}/patient")
    public DocumentResponse assignPatient(@PathVariable Long id, @RequestBody AssignPatientRequest request) {
        return DocumentResponse.from(documentService.assignPatient(id, request.patientId()), List.of());
    }
}