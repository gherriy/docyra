package com.docyra.backend.document;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class UniqueFileNamesTest {

    @Test
    void numbersDuplicateNames() {
        UniqueFileNames names = new UniqueFileNames();

        assertThat(names.next("DUPONT_Marie.pdf")).isEqualTo("DUPONT_Marie.pdf");
        assertThat(names.next("MARTIN_Jean.pdf")).isEqualTo("MARTIN_Jean.pdf");
        assertThat(names.next("DUPONT_Marie.pdf")).isEqualTo("DUPONT_Marie_2.pdf");
        assertThat(names.next("DUPONT_Marie.pdf")).isEqualTo("DUPONT_Marie_3.pdf");
    }
}