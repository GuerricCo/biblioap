package com.bibli.repository;

import com.bibli.domain.Review;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Review entity.
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long>, JpaSpecificationExecutor<Review> {
    default Optional<Review> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Review> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Review> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select review from Review review left join fetch review.library left join fetch review.book left join fetch review.member",
        countQuery = "select count(review) from Review review"
    )
    Page<Review> findAllWithToOneRelationships(Pageable pageable);

    @Query("select review from Review review left join fetch review.library left join fetch review.book left join fetch review.member")
    List<Review> findAllWithToOneRelationships();

    @Query(
        "select review from Review review left join fetch review.library left join fetch review.book left join fetch review.member where review.id =:id"
    )
    Optional<Review> findOneWithToOneRelationships(@Param("id") Long id);
}
