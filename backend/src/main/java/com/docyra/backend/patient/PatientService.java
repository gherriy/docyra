package com.docyra.backend.patient;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Transactional
    public PatientResponse create(CreatePatientRequest request) {
        if (request.lastName() == null || request.lastName().isBlank()
                || request.firstName() == null || request.firstName().isBlank()) {
            throw new InvalidPatientException("Le nom et le prénom sont obligatoires.");
        }

        String lastName = request.lastName().trim().toUpperCase();
        String firstName = request.firstName().trim();

        if (patientRepository.existsByLastNameAndFirstName(lastName, firstName)) {
            throw new DuplicatePatientException("Ce patient existe déjà : " + lastName + " " + firstName + ".");
        }

        return PatientResponse.from(patientRepository.save(new Patient(lastName, firstName)));
    }
}