package com.docyra.backend.document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.docyra.backend.error.NotFoundException;
import com.docyra.backend.patient.Patient;

import jakarta.persistence.*;

@Entity
@Table(name = "document")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

     @Column(name = "extracted_text", columnDefinition = "TEXT")
    private String extractedText;

        @ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentStatus status = DocumentStatus.TO_REVIEW;

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("pageNumber ASC")
    private List<DocumentPage> pages = new ArrayList<>();

    protected Document() {
    }

    public Document(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public void addPage(String storedFilename, String contentType) {
        pages.add(new DocumentPage(this, pages.size() + 1, storedFilename, contentType));
    }

    public DocumentPage getPage(int pageNumber) {
        if (pageNumber < 1 || pageNumber > pages.size()) {
            throw new NotFoundException("Page introuvable");
        }
        return pages.get(pageNumber - 1);
    }

    public List<DocumentPage> getPages() { return List.copyOf(pages); }
    public int getPageCount() { return pages.size(); }

    public Long getId() { return id; }
    public String getOriginalFilename() { return originalFilename; }
    public LocalDateTime getCreatedAt() { return createdAt; }

        public String getExtractedText() { return extractedText; }

    public void setExtractedText(String extractedText) {
        this.extractedText = extractedText;
    }
        public Patient getPatient() { return patient; }
    public DocumentStatus getStatus() { return status; }

    public void assignPatient(Patient patient) {
        this.patient = patient;
        this.status = DocumentStatus.MATCHED;
    }
        public void validate() {
        if (patient == null) {
            throw new MissingPatientException();
        }
        this.status = DocumentStatus.VALIDATED;
    }

    public void markDownloaded() {
        this.status = DocumentStatus.DOWNLOADED;
    }
}