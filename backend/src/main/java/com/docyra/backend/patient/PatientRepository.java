package com.docyra.backend.patient;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    boolean existsByLastNameAndFirstName(String lastName, String firstName);
        List<Patient> findAllByOrderByLastNameAscFirstNameAsc();

    
}
