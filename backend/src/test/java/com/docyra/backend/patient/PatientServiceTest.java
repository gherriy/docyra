package com.docyra.backend.patient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

class PatientServiceTest {

    private final PatientRepository repository = mock(PatientRepository.class);
    private final PatientService service = new PatientService(repository);

    @Test
    void createsPatientWithSameFormatAsImport() {
        when(repository.save(any(Patient.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PatientResponse response = service.create(new CreatePatientRequest("  bertin ", " Paul "));

        assertThat(response.lastName()).isEqualTo("BERTIN");
        assertThat(response.firstName()).isEqualTo("Paul");
    }

    @Test
    void refusesDuplicatePatient() {
        when(repository.existsByLastNameAndFirstName("BERTIN", "Paul")).thenReturn(true);

        assertThatThrownBy(() -> service.create(new CreatePatientRequest("Bertin", "Paul")))
                .isInstanceOf(DuplicatePatientException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void refusesMissingFirstName() {
        assertThatThrownBy(() -> service.create(new CreatePatientRequest("Bertin", " ")))
                .isInstanceOf(InvalidPatientException.class);
    }
}