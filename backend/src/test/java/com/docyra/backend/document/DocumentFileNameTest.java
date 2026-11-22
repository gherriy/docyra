package com.docyra.backend.document;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.docyra.backend.patient.Patient;

class DocumentFileNameTest {

    @Test
    void buildsNameWithoutAccents() {
        assertThat(DocumentFileName.of(new Patient("DUPONT", "Hélène")))
                .isEqualTo("DUPONT_Helene.pdf");
    }

    @Test
    void replacesSpacesAndRemovesForbiddenCharacters() {
        assertThat(DocumentFileName.of(new Patient("N'Diaye", "Marie Anne")))
                .isEqualTo("NDIAYE_Marie-Anne.pdf");
    }
}