package com.docyra.backend.document;

import java.io.IOException;
import org.springframework.http.HttpHeaders;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentResponse upload(@RequestParam("files") List<MultipartFile> files) throws IOException {
        var result = documentService.upload(files);

        return DocumentResponse.from(result.document(), result.candidates());
    }

    @PutMapping("/{id}/patient")
    public DocumentResponse assignPatient(@PathVariable Long id, @RequestBody AssignPatientRequest request) {
        return DocumentResponse.from(documentService.assignPatient(id, request.patientId()), List.of());
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
        DocumentPdf pdf = documentService.getPdf(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(pdf.fileName()).build().toString())
                .body(pdf.content());
    }

    @GetMapping
    public List<DocumentSummary> list(@RequestParam(required = false) List<DocumentStatus> status) {
        return documentService.list(status).stream()
                .map(DocumentSummary::from)
                .toList();
    }
        @GetMapping("/{id}")
    public DocumentResponse detail(@PathVariable Long id) {
        UploadResult result = documentService.getDetail(id);
        return DocumentResponse.from(result.document(), result.candidates());
    }

    @GetMapping(value = "/{id}/pages/{pageNumber}/image", produces = MediaType.IMAGE_JPEG_VALUE)
    public byte[] pageImage(@PathVariable Long id, @PathVariable int pageNumber) {
        return documentService.getPageImage(id, pageNumber);
    }
        @PutMapping("/{id}/validate")
    public DocumentResponse validate(@PathVariable Long id) {
        return DocumentResponse.from(documentService.validate(id), List.of());
    }

    @GetMapping("/zip")
    public ResponseEntity<byte[]> downloadZip() throws IOException {
        String filename = "courriers-docyra-" + LocalDate.now() + ".zip";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/zip"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(filename).build().toString())
                .body(documentService.buildValidatedZip());
    }
}