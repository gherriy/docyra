package com.docyra.backend.document;

import java.util.List;

import com.docyra.backend.patient.Patient;
import com.docyra.backend.patient.PatientResponse;

public record DocumentResponse(Long id, String originalFilename, String extractedText, DocumentStatus status, PatientResponse patient,
                               List<PatientResponse> candidates) {

    public static DocumentResponse from(Document document, List<Patient> candidates) {
        return new DocumentResponse(document.getId(), document.getOriginalFilename(), document.getExtractedText(), document.getStatus(),
                PatientResponse.from(document.getPatient()), 
                candidates.stream().map(PatientResponse::from).toList());
    }
}