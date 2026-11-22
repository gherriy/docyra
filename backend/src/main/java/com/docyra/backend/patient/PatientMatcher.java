package com.docyra.backend.patient;

import java.text.Normalizer;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class PatientMatcher {

    public List<Patient> findInText(List<Patient> patients, String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        String cleanText = " " + normalize(text) + " ";

        return patients.stream()
                .filter(patient -> {
                    String last = normalize(patient.getLastName());
                    String first = normalize(patient.getFirstName());
                    return cleanText.contains(" " + last + " " + first + " ")
                            || cleanText.contains(" " + first + " " + last + " ");
                })
                .toList();
    }

    static String normalize(String value) {
        String withoutAccents = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return withoutAccents.toUpperCase()
                .replaceAll("[^A-Z]+", " ")
                .trim();
    }
}