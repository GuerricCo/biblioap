package com.bibli.service.criteria;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.bibli.domain.Book} entity. This class is used
 * in {@link com.bibli.web.rest.BookResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /books?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class BookCriteria implements Serializable, Criteria {

    @Serial
    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter title;

    private StringFilter isbn;

    private LocalDateFilter publicationDate;

    private StringFilter description;

    private StringFilter language;

    private IntegerFilter pages;

    private BooleanFilter available;

    private IntegerFilter totalCopies;

    private IntegerFilter availableCopies;

    private LongFilter libraryId;

    private LongFilter categoryId;

    private LongFilter authorsId;

    private Boolean distinct;

    public BookCriteria() {}

    public BookCriteria(BookCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.title = other.optionalTitle().map(StringFilter::copy).orElse(null);
        this.isbn = other.optionalIsbn().map(StringFilter::copy).orElse(null);
        this.publicationDate = other.optionalPublicationDate().map(LocalDateFilter::copy).orElse(null);
        this.description = other.optionalDescription().map(StringFilter::copy).orElse(null);
        this.language = other.optionalLanguage().map(StringFilter::copy).orElse(null);
        this.pages = other.optionalPages().map(IntegerFilter::copy).orElse(null);
        this.available = other.optionalAvailable().map(BooleanFilter::copy).orElse(null);
        this.totalCopies = other.optionalTotalCopies().map(IntegerFilter::copy).orElse(null);
        this.availableCopies = other.optionalAvailableCopies().map(IntegerFilter::copy).orElse(null);
        this.libraryId = other.optionalLibraryId().map(LongFilter::copy).orElse(null);
        this.categoryId = other.optionalCategoryId().map(LongFilter::copy).orElse(null);
        this.authorsId = other.optionalAuthorsId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public BookCriteria copy() {
        return new BookCriteria(this);
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

    public StringFilter getTitle() {
        return title;
    }

    public Optional<StringFilter> optionalTitle() {
        return Optional.ofNullable(title);
    }

    public StringFilter title() {
        if (title == null) {
            setTitle(new StringFilter());
        }
        return title;
    }

    public void setTitle(StringFilter title) {
        this.title = title;
    }

    public StringFilter getIsbn() {
        return isbn;
    }

    public Optional<StringFilter> optionalIsbn() {
        return Optional.ofNullable(isbn);
    }

    public StringFilter isbn() {
        if (isbn == null) {
            setIsbn(new StringFilter());
        }
        return isbn;
    }

    public void setIsbn(StringFilter isbn) {
        this.isbn = isbn;
    }

    public LocalDateFilter getPublicationDate() {
        return publicationDate;
    }

    public Optional<LocalDateFilter> optionalPublicationDate() {
        return Optional.ofNullable(publicationDate);
    }

    public LocalDateFilter publicationDate() {
        if (publicationDate == null) {
            setPublicationDate(new LocalDateFilter());
        }
        return publicationDate;
    }

    public void setPublicationDate(LocalDateFilter publicationDate) {
        this.publicationDate = publicationDate;
    }

    public StringFilter getDescription() {
        return description;
    }

    public Optional<StringFilter> optionalDescription() {
        return Optional.ofNullable(description);
    }

    public StringFilter description() {
        if (description == null) {
            setDescription(new StringFilter());
        }
        return description;
    }

    public void setDescription(StringFilter description) {
        this.description = description;
    }

    public StringFilter getLanguage() {
        return language;
    }

    public Optional<StringFilter> optionalLanguage() {
        return Optional.ofNullable(language);
    }

    public StringFilter language() {
        if (language == null) {
            setLanguage(new StringFilter());
        }
        return language;
    }

    public void setLanguage(StringFilter language) {
        this.language = language;
    }

    public IntegerFilter getPages() {
        return pages;
    }

    public Optional<IntegerFilter> optionalPages() {
        return Optional.ofNullable(pages);
    }

    public IntegerFilter pages() {
        if (pages == null) {
            setPages(new IntegerFilter());
        }
        return pages;
    }

    public void setPages(IntegerFilter pages) {
        this.pages = pages;
    }

    public BooleanFilter getAvailable() {
        return available;
    }

    public Optional<BooleanFilter> optionalAvailable() {
        return Optional.ofNullable(available);
    }

    public BooleanFilter available() {
        if (available == null) {
            setAvailable(new BooleanFilter());
        }
        return available;
    }

    public void setAvailable(BooleanFilter available) {
        this.available = available;
    }

    public IntegerFilter getTotalCopies() {
        return totalCopies;
    }

    public Optional<IntegerFilter> optionalTotalCopies() {
        return Optional.ofNullable(totalCopies);
    }

    public IntegerFilter totalCopies() {
        if (totalCopies == null) {
            setTotalCopies(new IntegerFilter());
        }
        return totalCopies;
    }

    public void setTotalCopies(IntegerFilter totalCopies) {
        this.totalCopies = totalCopies;
    }

    public IntegerFilter getAvailableCopies() {
        return availableCopies;
    }

    public Optional<IntegerFilter> optionalAvailableCopies() {
        return Optional.ofNullable(availableCopies);
    }

    public IntegerFilter availableCopies() {
        if (availableCopies == null) {
            setAvailableCopies(new IntegerFilter());
        }
        return availableCopies;
    }

    public void setAvailableCopies(IntegerFilter availableCopies) {
        this.availableCopies = availableCopies;
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

    public LongFilter getCategoryId() {
        return categoryId;
    }

    public Optional<LongFilter> optionalCategoryId() {
        return Optional.ofNullable(categoryId);
    }

    public LongFilter categoryId() {
        if (categoryId == null) {
            setCategoryId(new LongFilter());
        }
        return categoryId;
    }

    public void setCategoryId(LongFilter categoryId) {
        this.categoryId = categoryId;
    }

    public LongFilter getAuthorsId() {
        return authorsId;
    }

    public Optional<LongFilter> optionalAuthorsId() {
        return Optional.ofNullable(authorsId);
    }

    public LongFilter authorsId() {
        if (authorsId == null) {
            setAuthorsId(new LongFilter());
        }
        return authorsId;
    }

    public void setAuthorsId(LongFilter authorsId) {
        this.authorsId = authorsId;
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
        final BookCriteria that = (BookCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(title, that.title) &&
            Objects.equals(isbn, that.isbn) &&
            Objects.equals(publicationDate, that.publicationDate) &&
            Objects.equals(description, that.description) &&
            Objects.equals(language, that.language) &&
            Objects.equals(pages, that.pages) &&
            Objects.equals(available, that.available) &&
            Objects.equals(totalCopies, that.totalCopies) &&
            Objects.equals(availableCopies, that.availableCopies) &&
            Objects.equals(libraryId, that.libraryId) &&
            Objects.equals(categoryId, that.categoryId) &&
            Objects.equals(authorsId, that.authorsId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            id,
            title,
            isbn,
            publicationDate,
            description,
            language,
            pages,
            available,
            totalCopies,
            availableCopies,
            libraryId,
            categoryId,
            authorsId,
            distinct
        );
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "BookCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalTitle().map(f -> "title=" + f + ", ").orElse("") +
            optionalIsbn().map(f -> "isbn=" + f + ", ").orElse("") +
            optionalPublicationDate().map(f -> "publicationDate=" + f + ", ").orElse("") +
            optionalDescription().map(f -> "description=" + f + ", ").orElse("") +
            optionalLanguage().map(f -> "language=" + f + ", ").orElse("") +
            optionalPages().map(f -> "pages=" + f + ", ").orElse("") +
            optionalAvailable().map(f -> "available=" + f + ", ").orElse("") +
            optionalTotalCopies().map(f -> "totalCopies=" + f + ", ").orElse("") +
            optionalAvailableCopies().map(f -> "availableCopies=" + f + ", ").orElse("") +
            optionalLibraryId().map(f -> "libraryId=" + f + ", ").orElse("") +
            optionalCategoryId().map(f -> "categoryId=" + f + ", ").orElse("") +
            optionalAuthorsId().map(f -> "authorsId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
