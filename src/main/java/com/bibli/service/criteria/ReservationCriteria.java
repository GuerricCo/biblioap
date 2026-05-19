package com.bibli.service.criteria;

import com.bibli.domain.enumeration.ReservationStatus;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.bibli.domain.Reservation} entity. This class is used
 * in {@link com.bibli.web.rest.ReservationResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /reservations?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ReservationCriteria implements Serializable, Criteria {

    /**
     * Class for filtering ReservationStatus
     */
    public static class ReservationStatusFilter extends Filter<ReservationStatus> {

        public ReservationStatusFilter() {}

        public ReservationStatusFilter(ReservationStatusFilter filter) {
            super(filter);
        }

        @Override
        public ReservationStatusFilter copy() {
            return new ReservationStatusFilter(this);
        }
    }

    @Serial
    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private LocalDateFilter reservationDate;

    private ReservationStatusFilter status;

    private LongFilter libraryId;

    private LongFilter bookId;

    private LongFilter memberId;

    private Boolean distinct;

    public ReservationCriteria() {}

    public ReservationCriteria(ReservationCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.reservationDate = other.optionalReservationDate().map(LocalDateFilter::copy).orElse(null);
        this.status = other.optionalStatus().map(ReservationStatusFilter::copy).orElse(null);
        this.libraryId = other.optionalLibraryId().map(LongFilter::copy).orElse(null);
        this.bookId = other.optionalBookId().map(LongFilter::copy).orElse(null);
        this.memberId = other.optionalMemberId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public ReservationCriteria copy() {
        return new ReservationCriteria(this);
    }

    public LongFilter getId() {
        return id;
    }

    public Optional<LongFilter> optionalId() {
        return Optional.ofNullable(id);
    }

    public LongFilter id() {
        if (id == null) {
            setId(new LongFilter());
        }
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public LocalDateFilter getReservationDate() {
        return reservationDate;
    }

    public Optional<LocalDateFilter> optionalReservationDate() {
        return Optional.ofNullable(reservationDate);
    }

    public LocalDateFilter reservationDate() {
        if (reservationDate == null) {
            setReservationDate(new LocalDateFilter());
        }
        return reservationDate;
    }

    public void setReservationDate(LocalDateFilter reservationDate) {
        this.reservationDate = reservationDate;
    }

    public ReservationStatusFilter getStatus() {
        return status;
    }

    public Optional<ReservationStatusFilter> optionalStatus() {
        return Optional.ofNullable(status);
    }

    public ReservationStatusFilter status() {
        if (status == null) {
            setStatus(new ReservationStatusFilter());
        }
        return status;
    }

    public void setStatus(ReservationStatusFilter status) {
        this.status = status;
    }

    public LongFilter getLibraryId() {
        return libraryId;
    }

    public Optional<LongFilter> optionalLibraryId() {
        return Optional.ofNullable(libraryId);
    }

    public LongFilter libraryId() {
        if (libraryId == null) {
            setLibraryId(new LongFilter());
        }
        return libraryId;
    }

    public void setLibraryId(LongFilter libraryId) {
        this.libraryId = libraryId;
    }

    public LongFilter getBookId() {
        return bookId;
    }

    public Optional<LongFilter> optionalBookId() {
        return Optional.ofNullable(bookId);
    }

    public LongFilter bookId() {
        if (bookId == null) {
            setBookId(new LongFilter());
        }
        return bookId;
    }

    public void setBookId(LongFilter bookId) {
        this.bookId = bookId;
    }

    public LongFilter getMemberId() {
        return memberId;
    }

    public Optional<LongFilter> optionalMemberId() {
        return Optional.ofNullable(memberId);
    }

    public LongFilter memberId() {
        if (memberId == null) {
            setMemberId(new LongFilter());
        }
        return memberId;
    }

    public void setMemberId(LongFilter memberId) {
        this.memberId = memberId;
    }

    public Boolean getDistinct() {
        return distinct;
    }

    public Optional<Boolean> optionalDistinct() {
        return Optional.ofNullable(distinct);
    }

    public Boolean distinct() {
        if (distinct == null) {
            setDistinct(true);
        }
        return distinct;
    }

    public void setDistinct(Boolean distinct) {
        this.distinct = distinct;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ReservationCriteria that = (ReservationCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(reservationDate, that.reservationDate) &&
            Objects.equals(status, that.status) &&
            Objects.equals(libraryId, that.libraryId) &&
            Objects.equals(bookId, that.bookId) &&
            Objects.equals(memberId, that.memberId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, reservationDate, status, libraryId, bookId, memberId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ReservationCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalReservationDate().map(f -> "reservationDate=" + f + ", ").orElse("") +
            optionalStatus().map(f -> "status=" + f + ", ").orElse("") +
            optionalLibraryId().map(f -> "libraryId=" + f + ", ").orElse("") +
            optionalBookId().map(f -> "bookId=" + f + ", ").orElse("") +
            optionalMemberId().map(f -> "memberId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
