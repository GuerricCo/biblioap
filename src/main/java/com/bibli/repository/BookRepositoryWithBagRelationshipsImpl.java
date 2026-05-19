package com.bibli.repository;

import com.bibli.domain.Book;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

/**
 * Utility repository to load bag relationships based on https://vladmihalcea.com/hibernate-multiplebagfetchexception/
 */
public class BookRepositoryWithBagRelationshipsImpl implements BookRepositoryWithBagRelationships {

    private static final String ID_PARAMETER = "id";
    private static final String BOOKS_PARAMETER = "books";

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<Book> fetchBagRelationships(Optional<Book> book) {
        return book.map(this::fetchAuthorses);
    }

    @Override
    public Page<Book> fetchBagRelationships(Page<Book> books) {
        return new PageImpl<>(fetchBagRelationships(books.getContent()), books.getPageable(), books.getTotalElements());
    }

    @Override
    public List<Book> fetchBagRelationships(List<Book> books) {
        return Optional.of(books).map(this::fetchAuthorses).orElse(Collections.emptyList());
    }

    Book fetchAuthorses(Book result) {
        return entityManager
            .createQuery("select book from Book book left join fetch book.authorses where book.id = :id", Book.class)
            .setParameter(ID_PARAMETER, result.getId())
            .getSingleResult();
    }

    List<Book> fetchAuthorses(List<Book> books) {
        HashMap<Object, Integer> order = new HashMap<>();
        IntStream.range(0, books.size()).forEach(index -> order.put(books.get(index).getId(), index));
        List<Book> result = entityManager
            .createQuery("select book from Book book left join fetch book.authorses where book in :books", Book.class)
            .setParameter(BOOKS_PARAMETER, books)
            .getResultList();
        Collections.sort(result, (o1, o2) -> Integer.compare(order.get(o1.getId()), order.get(o2.getId())));
        return result;
    }
}
