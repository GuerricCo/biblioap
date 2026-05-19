package com.bibli.repository;

import com.bibli.domain.Loan;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Loan entity.
 */
@Repository
public interface LoanRepository extends JpaRepository<Loan, Long>, JpaSpecificationExecutor<Loan> {
    default Optional<Loan> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Loan> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Loan> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select loan from Loan loan left join fetch loan.library left join fetch loan.book left join fetch loan.member",
        countQuery = "select count(loan) from Loan loan"
    )
    Page<Loan> findAllWithToOneRelationships(Pageable pageable);

    @Query("select loan from Loan loan left join fetch loan.library left join fetch loan.book left join fetch loan.member")
    List<Loan> findAllWithToOneRelationships();

    @Query(
        "select loan from Loan loan left join fetch loan.library left join fetch loan.book left join fetch loan.member where loan.id =:id"
    )
    Optional<Loan> findOneWithToOneRelationships(@Param("id") Long id);
}
