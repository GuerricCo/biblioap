package com.bibli.service;

import com.bibli.domain.Book;
import com.bibli.repository.BookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Manages {@link Book#getAvailableCopies()} on behalf of reservations and loans, which both hold
 * a copy of a book for as long as they are active.
 */
@Service
public class BookAvailabilityService {

    private static final Logger LOG = LoggerFactory.getLogger(BookAvailabilityService.class);

    private final BookRepository bookRepository;

    public BookAvailabilityService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public Book findBook(Long bookId, String entityName) {
        return bookRepository.findById(bookId).orElseThrow(() -> new BusinessException("Book not found", entityName, "idnotfound"));
    }

    /**
     * Whether the given book currently has at least one available copy.
     */
    public boolean hasAvailableCopy(Long bookId, String entityName) {
        Book book = findBook(bookId, entityName);
        return book.getAvailableCopies() != null && book.getAvailableCopies() > 0;
    }

    /**
     * Decrements the available copies of the given book, refusing when none is left.
     */
    public Book consumeCopy(Long bookId, String entityName) {
        Book book = findBook(bookId, entityName);
        if (book.getAvailableCopies() == null || book.getAvailableCopies() <= 0) {
            throw new BusinessException("No available copies for this book", entityName, "noavailablecopies");
        }
        int before = book.getAvailableCopies();
        book.setAvailableCopies(before - 1);
        Book saved = bookRepository.save(book);
        LOG.info(
            "[stock] consumeCopy caller='{}' book={} {}->{}",
            entityName,
            bookId,
            before,
            saved.getAvailableCopies(),
            new Exception("stock trace")
        );
        return saved;
    }

    /**
     * Increments the available copies of the given book.
     */
    public Book releaseCopy(Long bookId, String entityName) {
        Book book = findBook(bookId, entityName);
        int before = book.getAvailableCopies() == null ? 0 : book.getAvailableCopies();
        book.setAvailableCopies(before + 1);
        Book saved = bookRepository.save(book);
        LOG.info(
            "[stock] releaseCopy caller='{}' book={} {}->{}",
            entityName,
            bookId,
            before,
            saved.getAvailableCopies(),
            new Exception("stock trace")
        );
        return saved;
    }
}
