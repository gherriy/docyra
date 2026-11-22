package com.docyra.backend.patient;

public record PatientResponse(Long id, String lastName, String firstName) {

    public static PatientResponse from(Patient patient) {
        return patient == null ? null
                : new PatientResponse(patient.getId(), patient.getLastName(), patient.getFirstName());
    }
}