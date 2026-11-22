package com.docyra.backend.document;

import java.time.LocalDateTime;

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

    @Column(name = "stored_filename", nullable = false)
    private String storedFilename;

    @Column(name = "content_type", nullable = false)
    private String contentType;

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

    protected Document() {
    }

    public Document(String originalFilename, String storedFilename, String contentType) {
        this.originalFilename = originalFilename;
        this.storedFilename = storedFilename;
        this.contentType = contentType;
    }

    public Long getId() { return id; }
    public String getOriginalFilename() { return originalFilename; }
    public String getStoredFilename() { return storedFilename; }
    public String getContentType() { return contentType; }
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
}