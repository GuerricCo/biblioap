package com.bibli.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class LibraryCriteriaTest {

    @Test
    void newLibraryCriteriaHasAllFiltersNullTest() {
        var libraryCriteria = new LibraryCriteria();
        assertThat(libraryCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void libraryCriteriaFluentMethodsCreatesFiltersTest() {
        var libraryCriteria = new LibraryCriteria();

        setAllFilters(libraryCriteria);

        assertThat(libraryCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void libraryCriteriaCopyCreatesNullFilterTest() {
        var libraryCriteria = new LibraryCriteria();
        var copy = libraryCriteria.copy();

        assertThat(libraryCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(libraryCriteria)
        );
    }

    @Test
    void libraryCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var libraryCriteria = new LibraryCriteria();
        setAllFilters(libraryCriteria);

        var copy = libraryCriteria.copy();

        assertThat(libraryCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(libraryCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var libraryCriteria = new LibraryCriteria();

        assertThat(libraryCriteria).hasToString("LibraryCriteria{}");
    }

    private static void setAllFilters(LibraryCriteria libraryCriteria) {
        libraryCriteria.id();
        libraryCriteria.name();
        libraryCriteria.address();
        libraryCriteria.city();
        libraryCriteria.phone();
        libraryCriteria.email();
        libraryCriteria.distinct();
    }

    private static Condition<LibraryCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getName()) &&
                condition.apply(criteria.getAddress()) &&
                condition.apply(criteria.getCity()) &&
                condition.apply(criteria.getPhone()) &&
                condition.apply(criteria.getEmail()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<LibraryCriteria> copyFiltersAre(LibraryCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getName(), copy.getName()) &&
                condition.apply(criteria.getAddress(), copy.getAddress()) &&
                condition.apply(criteria.getCity(), copy.getCity()) &&
                condition.apply(criteria.getPhone(), copy.getPhone()) &&
                condition.apply(criteria.getEmail(), copy.getEmail()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
