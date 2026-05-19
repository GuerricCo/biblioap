package com.bibli.service.criteria;

import com.bibli.domain.enumeration.LoanStatus;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.bibli.domain.Loan} entity. This class is used
 * in {@link com.bibli.web.rest.LoanResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /loans?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class LoanCriteria implements Serializable, Criteria {

    /**
     * Class for filtering LoanStatus
     */
    public static class LoanStatusFilter extends Filter<LoanStatus> {

        public LoanStatusFilter() {}

        public LoanStatusFilter(LoanStatusFilter filter) {
            super(filter);
        }

        @Override
        public LoanStatusFilter copy() {
            return new LoanStatusFilter(this);
        }
    }

    @Serial
    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private LocalDateFilter borrowDate;

    private LocalDateFilter dueDate;

    private LocalDateFilter returnDate;

    private LoanStatusFilter status;

    private LongFilter libraryId;

    private LongFilter bookId;

    private LongFilter memberId;

    private Boolean distinct;

    public LoanCriteria() {}

    public LoanCriteria(LoanCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.borrowDate = other.optionalBorrowDate().map(LocalDateFilter::copy).orElse(null);
        this.dueDate = other.optionalDueDate().map(LocalDateFilter::copy).orElse(null);
        this.returnDate = other.optionalReturnDate().map(LocalDateFilter::copy).orElse(null);
        this.status = other.optionalStatus().map(LoanStatusFilter::copy).orElse(null);
        this.libraryId = other.optionalLibraryId().map(LongFilter::copy).orElse(null);
        this.bookId = other.optionalBookId().map(LongFilter::copy).orElse(null);
        this.memberId = other.optionalMemberId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public LoanCriteria copy() {
        return new LoanCriteria(this);
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

    public LocalDateFilter getBorrowDate() {
        return borrowDate;
    }

    public Optional<LocalDateFilter> optionalBorrowDate() {
        return Optional.ofNullable(borrowDate);
    }

    public LocalDateFilter borrowDate() {
        if (borrowDate == null) {
            setBorrowDate(new LocalDateFilter());
        }
        return borrowDate;
    }

    public void setBorrowDate(LocalDateFilter borrowDate) {
        this.borrowDate = borrowDate;
    }

    public LocalDateFilter getDueDate() {
        return dueDate;
    }

    public Optional<LocalDateFilter> optionalDueDate() {
        return Optional.ofNullable(dueDate);
    }

    public LocalDateFilter dueDate() {
        if (dueDate == null) {
            setDueDate(new LocalDateFilter());
        }
        return dueDate;
    }

    public void setDueDate(LocalDateFilter dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDateFilter getReturnDate() {
        return returnDate;
    }

    public Optional<LocalDateFilter> optionalReturnDate() {
        return Optional.ofNullable(returnDate);
    }

    public LocalDateFilter returnDate() {
        if (returnDate == null) {
            setReturnDate(new LocalDateFilter());
        }
        return returnDate;
    }

    public void setReturnDate(LocalDateFilter returnDate) {
        this.returnDate = returnDate;
    }

    public LoanStatusFilter getStatus() {
        return status;
    }

    public Optional<LoanStatusFilter> optionalStatus() {
        return Optional.ofNullable(status);
    }

    public LoanStatusFilter status() {
        if (status == null) {
            setStatus(new LoanStatusFilter());
        }
        return status;
    }

    public void setStatus(LoanStatusFilter status) {
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
        final LoanCriteria that = (LoanCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(borrowDate, that.borrowDate) &&
            Objects.equals(dueDate, that.dueDate) &&
            Objects.equals(returnDate, that.returnDate) &&
            Objects.equals(status, that.status) &&
            Objects.equals(libraryId, that.libraryId) &&
            Objects.equals(bookId, that.bookId) &&
            Objects.equals(memberId, that.memberId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, borrowDate, dueDate, returnDate, status, libraryId, bookId, memberId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "LoanCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalBorrowDate().map(f -> "borrowDate=" + f + ", ").orElse("") +
            optionalDueDate().map(f -> "dueDate=" + f + ", ").orElse("") +
            optionalReturnDate().map(f -> "returnDate=" + f + ", ").orElse("") +
            optionalStatus().map(f -> "status=" + f + ", ").orElse("") +
            optionalLibraryId().map(f -> "libraryId=" + f + ", ").orElse("") +
            optionalBookId().map(f -> "bookId=" + f + ", ").orElse("") +
            optionalMemberId().map(f -> "memberId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
