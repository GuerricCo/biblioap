package com.bibli.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class MemberCriteriaTest {

    @Test
    void newMemberCriteriaHasAllFiltersNullTest() {
        var memberCriteria = new MemberCriteria();
        assertThat(memberCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void memberCriteriaFluentMethodsCreatesFiltersTest() {
        var memberCriteria = new MemberCriteria();

        setAllFilters(memberCriteria);

        assertThat(memberCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void memberCriteriaCopyCreatesNullFilterTest() {
        var memberCriteria = new MemberCriteria();
        var copy = memberCriteria.copy();

        assertThat(memberCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(memberCriteria)
        );
    }

    @Test
    void memberCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var memberCriteria = new MemberCriteria();
        setAllFilters(memberCriteria);

        var copy = memberCriteria.copy();

        assertThat(memberCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(memberCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var memberCriteria = new MemberCriteria();

        assertThat(memberCriteria).hasToString("MemberCriteria{}");
    }

    private static void setAllFilters(MemberCriteria memberCriteria) {
        memberCriteria.id();
        memberCriteria.firstName();
        memberCriteria.lastName();
        memberCriteria.email();
        memberCriteria.phone();
        memberCriteria.membershipDate();
        memberCriteria.active();
        memberCriteria.libraryId();
        memberCriteria.distinct();
    }

    private static Condition<MemberCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getFirstName()) &&
                condition.apply(criteria.getLastName()) &&
                condition.apply(criteria.getEmail()) &&
                condition.apply(criteria.getPhone()) &&
                condition.apply(criteria.getMembershipDate()) &&
                condition.apply(criteria.getActive()) &&
                condition.apply(criteria.getLibraryId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<MemberCriteria> copyFiltersAre(MemberCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getFirstName(), copy.getFirstName()) &&
                condition.apply(criteria.getLastName(), copy.getLastName()) &&
                condition.apply(criteria.getEmail(), copy.getEmail()) &&
                condition.apply(criteria.getPhone(), copy.getPhone()) &&
                condition.apply(criteria.getMembershipDate(), copy.getMembershipDate()) &&
                condition.apply(criteria.getActive(), copy.getActive()) &&
                condition.apply(criteria.getLibraryId(), copy.getLibraryId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
