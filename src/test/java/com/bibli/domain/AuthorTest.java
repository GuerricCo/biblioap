package com.bibli.domain;

import static com.bibli.domain.AuthorTestSamples.*;
import static com.bibli.domain.BookTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.bibli.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class AuthorTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Author.class);
        Author author1 = getAuthorSample1();
        Author author2 = new Author();
        assertThat(author1).isNotEqualTo(author2);

        author2.setId(author1.getId());
        assertThat(author1).isEqualTo(author2);

        author2 = getAuthorSample2();
        assertThat(author1).isNotEqualTo(author2);
    }

    @Test
    void booksTest() {
        Author author = getAuthorRandomSampleGenerator();
        Book bookBack = getBookRandomSampleGenerator();

        author.addBooks(bookBack);
        assertThat(author.getBookses()).containsOnly(bookBack);
        assertThat(bookBack.getAuthorses()).containsOnly(author);

        author.removeBooks(bookBack);
        assertThat(author.getBookses()).doesNotContain(bookBack);
        assertThat(bookBack.getAuthorses()).doesNotContain(author);

        author.bookses(new HashSet<>(Set.of(bookBack)));
        assertThat(author.getBookses()).containsOnly(bookBack);
        assertThat(bookBack.getAuthorses()).containsOnly(author);

        author.setBookses(new HashSet<>());
        assertThat(author.getBookses()).doesNotContain(bookBack);
        assertThat(bookBack.getAuthorses()).doesNotContain(author);
    }
}
