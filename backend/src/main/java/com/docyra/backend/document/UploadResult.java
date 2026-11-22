package com.docyra.backend.document;

import java.util.List;

import com.docyra.backend.patient.Patient;

public record UploadResult(Document document, List<Patient> candidates) {
    
}
