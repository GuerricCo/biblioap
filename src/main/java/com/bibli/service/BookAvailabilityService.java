package com.bibli.service;

import com.bibli.domain.Book;
import com.bibli.repository.BookRepository;
import com.bibli.web.rest.errors.BadRequestAlertException;
import org.springframework.stereotype.Service;

/**
 * Manages {@link Book#getAvailableCopies()} on behalf of reservations and loans, which both hold
 * a copy of a book for as long as they are active.
 */
@Service
public class BookAvailabilityService {

    private final BookRepository bookRepository;

    public BookAvailabilityService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public Book findBook(Long bookId, String entityName) {
        return bookRepository.findById(bookId).orElseThrow(() -> new BadRequestAlertException("Book not found", entityName, "idnotfound"));
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
            throw new BadRequestAlertException("No available copies for this book", entityName, "noavailablecopies");
        }
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        return bookRepository.save(book);
    }

    /**
     * Increments the available copies of the given book.
     */
    public Book releaseCopy(Long bookId, String entityName) {
        Book book = findBook(bookId, entityName);
        book.setAvailableCopies((book.getAvailableCopies() == null ? 0 : book.getAvailableCopies()) + 1);
        return bookRepository.save(book);
    }
}
