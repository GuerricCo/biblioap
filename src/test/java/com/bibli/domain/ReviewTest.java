package com.bibli.domain;

import static com.bibli.domain.BookTestSamples.*;
import static com.bibli.domain.LibraryTestSamples.*;
import static com.bibli.domain.MemberTestSamples.*;
import static com.bibli.domain.ReviewTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.bibli.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ReviewTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Review.class);
        Review review1 = getReviewSample1();
        Review review2 = new Review();
        assertThat(review1).isNotEqualTo(review2);

        review2.setId(review1.getId());
        assertThat(review1).isEqualTo(review2);

        review2 = getReviewSample2();
        assertThat(review1).isNotEqualTo(review2);
    }

    @Test
    void libraryTest() {
        Review review = getReviewRandomSampleGenerator();
        Library libraryBack = getLibraryRandomSampleGenerator();

        review.setLibrary(libraryBack);
        assertThat(review.getLibrary()).isEqualTo(libraryBack);

        review.library(null);
        assertThat(review.getLibrary()).isNull();
    }

    @Test
    void bookTest() {
        Review review = getReviewRandomSampleGenerator();
        Book bookBack = getBookRandomSampleGenerator();

        review.setBook(bookBack);
        assertThat(review.getBook()).isEqualTo(bookBack);

        review.book(null);
        assertThat(review.getBook()).isNull();
    }

    @Test
    void memberTest() {
        Review review = getReviewRandomSampleGenerator();
        Member memberBack = getMemberRandomSampleGenerator();

        review.setMember(memberBack);
        assertThat(review.getMember()).isEqualTo(memberBack);

        review.member(null);
        assertThat(review.getMember()).isNull();
    }
}
