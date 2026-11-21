package com.docyra.backend.patient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

class PatientImportServiceTest {

    private final PatientRepository repository = mock(PatientRepository.class);
    private final PatientImportService service = new PatientImportService(repository);

    private InputStream csv(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void importsNewPatients() {
        ImportResult result = service.importCsv(csv("nom;prenom\nDupont;Marie\nMARTIN;Jean\n"));

        assertThat(result.imported()).isEqualTo(2);
        verify(repository, times(2)).save(any(Patient.class));
    }

    @Test
    void skipsExistingPatients() {
        when(repository.existsByLastNameAndFirstName("DUPONT", "Marie")).thenReturn(true);

        ImportResult result = service.importCsv(csv("nom;prenom\nDupont;Marie\n"));

        assertThat(result.skipped()).isEqualTo(1);
        verify(repository, never()).save(any());
    }

    @Test
    void rejectsInvalidLine() {
        assertThatThrownBy(() -> service.importCsv(csv("nom;prenom\nDUPONT\n")))
                .isInstanceOf(InvalidCsvException.class)
                .hasMessageContaining("Ligne 2");
    }
}