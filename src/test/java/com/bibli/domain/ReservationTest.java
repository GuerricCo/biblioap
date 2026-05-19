package com.bibli.domain;

import static com.bibli.domain.BookTestSamples.*;
import static com.bibli.domain.LibraryTestSamples.*;
import static com.bibli.domain.MemberTestSamples.*;
import static com.bibli.domain.ReservationTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.bibli.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ReservationTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Reservation.class);
        Reservation reservation1 = getReservationSample1();
        Reservation reservation2 = new Reservation();
        assertThat(reservation1).isNotEqualTo(reservation2);

        reservation2.setId(reservation1.getId());
        assertThat(reservation1).isEqualTo(reservation2);

        reservation2 = getReservationSample2();
        assertThat(reservation1).isNotEqualTo(reservation2);
    }

    @Test
    void libraryTest() {
        Reservation reservation = getReservationRandomSampleGenerator();
        Library libraryBack = getLibraryRandomSampleGenerator();

        reservation.setLibrary(libraryBack);
        assertThat(reservation.getLibrary()).isEqualTo(libraryBack);

        reservation.library(null);
        assertThat(reservation.getLibrary()).isNull();
    }

    @Test
    void bookTest() {
        Reservation reservation = getReservationRandomSampleGenerator();
        Book bookBack = getBookRandomSampleGenerator();

        reservation.setBook(bookBack);
        assertThat(reservation.getBook()).isEqualTo(bookBack);

        reservation.book(null);
        assertThat(reservation.getBook()).isNull();
    }

    @Test
    void memberTest() {
        Reservation reservation = getReservationRandomSampleGenerator();
        Member memberBack = getMemberRandomSampleGenerator();

        reservation.setMember(memberBack);
        assertThat(reservation.getMember()).isEqualTo(memberBack);

        reservation.member(null);
        assertThat(reservation.getMember()).isNull();
    }
}
