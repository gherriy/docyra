package com.docyra.backend.patient;

import static org.assertj.core.api.Assertions.assertThat;
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
    void skipsInvalidLinesAndReportsThem() {
        ImportResult result = service.importCsv(csv("nom;prenom\nDUPONT;Marie\nMOREAU;;55\nLEROY\n"));

        assertThat(result.imported()).isEqualTo(1);
        assertThat(result.invalidLines()).containsExactly(3, 4);
    }

    @Test
    void ignoresExtraColumns() {
        ImportResult result = service.importCsv(csv("nom;prenom;age\nDupont;Marie;34\n"));

        assertThat(result.imported()).isEqualTo(1);
    }
}