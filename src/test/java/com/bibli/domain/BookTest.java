package com.bibli.domain;

import static com.bibli.domain.AuthorTestSamples.*;
import static com.bibli.domain.BookTestSamples.*;
import static com.bibli.domain.CategoryTestSamples.*;
import static com.bibli.domain.LibraryTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.bibli.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class BookTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Book.class);
        Book book1 = getBookSample1();
        Book book2 = new Book();
        assertThat(book1).isNotEqualTo(book2);

        book2.setId(book1.getId());
        assertThat(book1).isEqualTo(book2);

        book2 = getBookSample2();
        assertThat(book1).isNotEqualTo(book2);
    }

    @Test
    void libraryTest() {
        Book book = getBookRandomSampleGenerator();
        Library libraryBack = getLibraryRandomSampleGenerator();

        book.setLibrary(libraryBack);
        assertThat(book.getLibrary()).isEqualTo(libraryBack);

        book.library(null);
        assertThat(book.getLibrary()).isNull();
    }

    @Test
    void categoryTest() {
        Book book = getBookRandomSampleGenerator();
        Category categoryBack = getCategoryRandomSampleGenerator();

        book.setCategory(categoryBack);
        assertThat(book.getCategory()).isEqualTo(categoryBack);

        book.category(null);
        assertThat(book.getCategory()).isNull();
    }

    @Test
    void authorsTest() {
        Book book = getBookRandomSampleGenerator();
        Author authorBack = getAuthorRandomSampleGenerator();

        book.addAuthors(authorBack);
        assertThat(book.getAuthorses()).containsOnly(authorBack);

        book.removeAuthors(authorBack);
        assertThat(book.getAuthorses()).doesNotContain(authorBack);

        book.authorses(new HashSet<>(Set.of(authorBack)));
        assertThat(book.getAuthorses()).containsOnly(authorBack);

        book.setAuthorses(new HashSet<>());
        assertThat(book.getAuthorses()).doesNotContain(authorBack);
    }
}
