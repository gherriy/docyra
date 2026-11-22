package com.docyra.backend.patient;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class PatientMatcherTest {

    private final PatientMatcher matcher = new PatientMatcher();

    private final Patient dupont = new Patient("DUPONT", "Hélène");
    private final Patient martin = new Patient("MARTIN", "Jean");
    private final List<Patient> patients = List.of(dupont, martin);

    @Test
    void findsOnePatientWithAccentsAndPunctuation() {
        var result = matcher.findInText(patients, "Compte rendu pour Mme Helene DUPONT, née le...");

        assertThat(result).containsExactly(dupont);
    }

    @Test
    void findsSeveralPatients() {
        var result = matcher.findInText(patients, "DUPONT Hélène et Jean Martin");

        assertThat(result).containsExactlyInAnyOrder(dupont, martin);
    }

    @Test
    void findsNoPatient() {
        var result = matcher.findInText(patients, "Monsieur Paul MARTINEZ");

        assertThat(result).isEmpty();
    }
}