package com.docyra.backend.document;

import java.time.LocalDateTime;

import com.docyra.backend.patient.PatientResponse;

public record DocumentSummary(Long id, DocumentStatus status, PatientResponse patient,
                              String fileName, LocalDateTime createdAt) {

    public static DocumentSummary from(Document document) {
        String fileName = document.getPatient() == null ? null : DocumentFileName.of(document.getPatient());
        return new DocumentSummary(document.getId(), document.getStatus(),
                PatientResponse.from(document.getPatient()), fileName, document.getCreatedAt());
    }
}