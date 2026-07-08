package com.bibli.service;

import com.bibli.domain.Book;
import com.bibli.domain.Loan;
import com.bibli.domain.enumeration.LoanStatus;
import com.bibli.repository.LoanRepository;
import com.bibli.service.dto.LoanDTO;
import com.bibli.service.mapper.LoanMapper;
import com.bibli.web.rest.errors.BadRequestAlertException;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.bibli.domain.Loan}.
 */
@Service
@Transactional
public class LoanService {

    private static final String ENTITY_NAME = "loan";

    private static final Logger LOG = LoggerFactory.getLogger(LoanService.class);

    private final LoanRepository loanRepository;

    private final LoanMapper loanMapper;

    private final BookAvailabilityService bookAvailabilityService;

    public LoanService(LoanRepository loanRepository, LoanMapper loanMapper, BookAvailabilityService bookAvailabilityService) {
        this.loanRepository = loanRepository;
        this.loanMapper = loanMapper;
        this.bookAvailabilityService = bookAvailabilityService;
    }

    /**
     * Save a loan. Consumes one available copy of the borrowed book, refusing the loan outright if
     * none is left.
     *
     * @param loanDTO the entity to save.
     * @return the persisted entity.
     */
    public LoanDTO save(LoanDTO loanDTO) {
        LOG.debug("Request to save Loan : {}", loanDTO);
        Loan loan = loanMapper.toEntity(loanDTO);
        Long bookId = loan.getBook() == null ? null : loan.getBook().getId();
        if (holdsCopy(loan.getStatus(), bookId)) {
            loan.setBook(bookAvailabilityService.consumeCopy(bookId, ENTITY_NAME));
        }
        loan = loanRepository.save(loan);
        return loanMapper.toDto(loan);
    }

    /**
     * Update a loan. Releases or consumes a copy of the book when the status transitions to/from
     * {@link LoanStatus#RETURNED}, so the available copies count stays accurate.
     *
     * @param loanDTO the entity to save.
     * @return the persisted entity.
     */
    public LoanDTO update(LoanDTO loanDTO) {
        LOG.debug("Request to update Loan : {}", loanDTO);
        Loan existing = loanRepository
            .findById(loanDTO.getId())
            .orElseThrow(() -> new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));

        Loan loan = loanMapper.toEntity(loanDTO);
        Long oldBookId = existing.getBook() == null ? null : existing.getBook().getId();
        Long newBookId = loan.getBook() == null ? null : loan.getBook().getId();
        loan.setBook(reconcileAvailability(existing.getStatus(), oldBookId, loan.getStatus(), newBookId));
        loan = loanRepository.save(loan);
        return loanMapper.toDto(loan);
    }

    /**
     * Partially update a loan. Reconciles the available copies count when the patch changes the
     * status and/or the borrowed book.
     *
     * @param loanDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<LoanDTO> partialUpdate(LoanDTO loanDTO) {
        LOG.debug("Request to partially update Loan : {}", loanDTO);

        return loanRepository
            .findById(loanDTO.getId())
            .map(existingLoan -> {
                LoanStatus oldStatus = existingLoan.getStatus();
                Long oldBookId = existingLoan.getBook() == null ? null : existingLoan.getBook().getId();

                loanMapper.partialUpdate(existingLoan, loanDTO);

                Long newBookId = existingLoan.getBook() == null ? null : existingLoan.getBook().getId();
                if (oldStatus != existingLoan.getStatus() || !Objects.equals(oldBookId, newBookId)) {
                    existingLoan.setBook(reconcileAvailability(oldStatus, oldBookId, existingLoan.getStatus(), newBookId));
                }

                return existingLoan;
            })
            .map(loanRepository::save)
            .map(loanMapper::toDto);
    }

    /**
     * Get all the loans with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<LoanDTO> findAllWithEagerRelationships(Pageable pageable) {
        return loanRepository.findAllWithEagerRelationships(pageable).map(loanMapper::toDto);
    }

    /**
     * Get one loan by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<LoanDTO> findOne(Long id) {
        LOG.debug("Request to get Loan : {}", id);
        return loanRepository.findOneWithEagerRelationships(id).map(loanMapper::toDto);
    }

    /**
     * Delete the loan by id. Releases the held copy back to the book's available stock, unless the
     * loan was already returned (which already released it).
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete Loan : {}", id);
        loanRepository
            .findById(id)
            .ifPresent(loan -> {
                Long bookId = loan.getBook() == null ? null : loan.getBook().getId();
                if (holdsCopy(loan.getStatus(), bookId)) {
                    bookAvailabilityService.releaseCopy(bookId, ENTITY_NAME);
                }
                loanRepository.deleteById(id);
            });
    }

    /**
     * Marks a loan as returned today and releases its held copy back to the book's available stock.
     *
     * @param id the id of the loan to return.
     * @return the updated entity.
     */
    public LoanDTO returnLoan(Long id) {
        LOG.debug("Request to return Loan : {}", id);
        Loan loan = loanRepository
            .findById(id)
            .orElseThrow(() -> new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));

        if (loan.getStatus() == LoanStatus.RETURNED) {
            throw new BadRequestAlertException("This loan has already been returned", ENTITY_NAME, "alreadyreturned");
        }

        Long bookId = loan.getBook() == null ? null : loan.getBook().getId();
        if (bookId != null) {
            bookAvailabilityService.releaseCopy(bookId, ENTITY_NAME);
        }
        loan.setStatus(LoanStatus.RETURNED);
        loan.setReturnDate(LocalDate.now());
        loan = loanRepository.save(loan);
        return loanMapper.toDto(loan);
    }

    /**
     * A loan holds a copy of a book for as long as it references one and has not been returned.
     */
    private static boolean holdsCopy(LoanStatus status, Long bookId) {
        return bookId != null && status != LoanStatus.RETURNED;
    }

    /**
     * Adjusts the available copies count for a status/book transition: releases the hold on the
     * previous book if it was active and is no longer, consumes a copy of the new book if it is now
     * active and wasn't before.
     */
    private Book reconcileAvailability(LoanStatus oldStatus, Long oldBookId, LoanStatus newStatus, Long newBookId) {
        boolean wasActive = holdsCopy(oldStatus, oldBookId);
        boolean isNowActive = holdsCopy(newStatus, newBookId);

        if (Objects.equals(oldBookId, newBookId)) {
            if (wasActive && !isNowActive) {
                return bookAvailabilityService.releaseCopy(newBookId, ENTITY_NAME);
            }
            if (!wasActive && isNowActive) {
                return bookAvailabilityService.consumeCopy(newBookId, ENTITY_NAME);
            }
            return newBookId == null ? null : bookAvailabilityService.findBook(newBookId, ENTITY_NAME);
        }

        if (wasActive) {
            bookAvailabilityService.releaseCopy(oldBookId, ENTITY_NAME);
        }
        if (isNowActive) {
            return bookAvailabilityService.consumeCopy(newBookId, ENTITY_NAME);
        }
        return newBookId == null ? null : bookAvailabilityService.findBook(newBookId, ENTITY_NAME);
    }
}
