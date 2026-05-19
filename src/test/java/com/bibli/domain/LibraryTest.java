package com.bibli.domain;

import static com.bibli.domain.LibraryTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.bibli.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class LibraryTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Library.class);
        Library library1 = getLibrarySample1();
        Library library2 = new Library();
        assertThat(library1).isNotEqualTo(library2);

        library2.setId(library1.getId());
        assertThat(library1).isEqualTo(library2);

        library2 = getLibrarySample2();
        assertThat(library1).isNotEqualTo(library2);
    }
}
