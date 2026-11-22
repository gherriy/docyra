package com.docyra.backend.document;

import java.text.Normalizer;

import com.docyra.backend.patient.Patient;

public final class DocumentFileName {

    private DocumentFileName() {
    }

    public static String of(Patient patient) {
        return clean(patient.getLastName()).toUpperCase() + "_" + clean(patient.getFirstName()) + ".pdf";
    }

    private static String clean(String value) {
        String withoutAccents = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return withoutAccents.trim()
                .replaceAll("\\s+", "-")
                .replaceAll("[^A-Za-z0-9-]", "");
    }
}