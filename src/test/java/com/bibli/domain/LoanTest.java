package com.bibli.domain;

import static com.bibli.domain.BookTestSamples.*;
import static com.bibli.domain.LibraryTestSamples.*;
import static com.bibli.domain.LoanTestSamples.*;
import static com.bibli.domain.MemberTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.bibli.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class LoanTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Loan.class);
        Loan loan1 = getLoanSample1();
        Loan loan2 = new Loan();
        assertThat(loan1).isNotEqualTo(loan2);

        loan2.setId(loan1.getId());
        assertThat(loan1).isEqualTo(loan2);

        loan2 = getLoanSample2();
        assertThat(loan1).isNotEqualTo(loan2);
    }

    @Test
    void libraryTest() {
        Loan loan = getLoanRandomSampleGenerator();
        Library libraryBack = getLibraryRandomSampleGenerator();

        loan.setLibrary(libraryBack);
        assertThat(loan.getLibrary()).isEqualTo(libraryBack);

        loan.library(null);
        assertThat(loan.getLibrary()).isNull();
    }

    @Test
    void bookTest() {
        Loan loan = getLoanRandomSampleGenerator();
        Book bookBack = getBookRandomSampleGenerator();

        loan.setBook(bookBack);
        assertThat(loan.getBook()).isEqualTo(bookBack);

        loan.book(null);
        assertThat(loan.getBook()).isNull();
    }

    @Test
    void memberTest() {
        Loan loan = getLoanRandomSampleGenerator();
        Member memberBack = getMemberRandomSampleGenerator();

        loan.setMember(memberBack);
        assertThat(loan.getMember()).isEqualTo(memberBack);

        loan.member(null);
        assertThat(loan.getMember()).isNull();
    }
}
