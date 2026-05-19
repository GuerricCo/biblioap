package com.bibli.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class AuthorCriteriaTest {

    @Test
    void newAuthorCriteriaHasAllFiltersNullTest() {
        var authorCriteria = new AuthorCriteria();
        assertThat(authorCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void authorCriteriaFluentMethodsCreatesFiltersTest() {
        var authorCriteria = new AuthorCriteria();

        setAllFilters(authorCriteria);

        assertThat(authorCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void authorCriteriaCopyCreatesNullFilterTest() {
        var authorCriteria = new AuthorCriteria();
        var copy = authorCriteria.copy();

        assertThat(authorCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(authorCriteria)
        );
    }

    @Test
    void authorCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var authorCriteria = new AuthorCriteria();
        setAllFilters(authorCriteria);

        var copy = authorCriteria.copy();

        assertThat(authorCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(authorCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var authorCriteria = new AuthorCriteria();

        assertThat(authorCriteria).hasToString("AuthorCriteria{}");
    }

    private static void setAllFilters(AuthorCriteria authorCriteria) {
        authorCriteria.id();
        authorCriteria.firstName();
        authorCriteria.lastName();
        authorCriteria.birthDate();
        authorCriteria.nationality();
        authorCriteria.biography();
        authorCriteria.booksId();
        authorCriteria.distinct();
    }

    private static Condition<AuthorCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getFirstName()) &&
                condition.apply(criteria.getLastName()) &&
                condition.apply(criteria.getBirthDate()) &&
                condition.apply(criteria.getNationality()) &&
                condition.apply(criteria.getBiography()) &&
                condition.apply(criteria.getBooksId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<AuthorCriteria> copyFiltersAre(AuthorCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getFirstName(), copy.getFirstName()) &&
                condition.apply(criteria.getLastName(), copy.getLastName()) &&
                condition.apply(criteria.getBirthDate(), copy.getBirthDate()) &&
                condition.apply(criteria.getNationality(), copy.getNationality()) &&
                condition.apply(criteria.getBiography(), copy.getBiography()) &&
                condition.apply(criteria.getBooksId(), copy.getBooksId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
