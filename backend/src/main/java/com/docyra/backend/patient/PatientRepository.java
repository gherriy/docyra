package com.docyra.backend.patient;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    boolean existsByLastNameAndFirstName(String lastName, String firstName);
    
}
