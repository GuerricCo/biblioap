package com.bibli.service;

import com.bibli.domain.Book;
import com.bibli.domain.Loan;
import com.bibli.domain.Reservation;
import com.bibli.domain.enumeration.LoanStatus;
import com.bibli.domain.enumeration.ReservationStatus;
import com.bibli.repository.LoanRepository;
import com.bibli.repository.ReservationRepository;
import com.bibli.service.dto.LoanDTO;
import com.bibli.service.dto.ReservationDTO;
import com.bibli.service.mapper.LoanMapper;
import com.bibli.service.mapper.ReservationMapper;
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
 * Service Implementation for managing {@link com.bibli.domain.Reservation}.
 */
@Service
@Transactional
public class ReservationService {

    private static final String ENTITY_NAME = "reservation";

    private static final int DEFAULT_LOAN_DURATION_DAYS = 14;

    private static final Logger LOG = LoggerFactory.getLogger(ReservationService.class);

    private final ReservationRepository reservationRepository;

    private final ReservationMapper reservationMapper;

    private final BookAvailabilityService bookAvailabilityService;

    private final LoanRepository loanRepository;

    private final LoanMapper loanMapper;

    public ReservationService(
        ReservationRepository reservationRepository,
        ReservationMapper reservationMapper,
        BookAvailabilityService bookAvailabilityService,
        LoanRepository loanRepository,
        LoanMapper loanMapper
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationMapper = reservationMapper;
        this.bookAvailabilityService = bookAvailabilityService;
        this.loanRepository = loanRepository;
        this.loanMapper = loanMapper;
    }

    /**
     * Save a reservation. Consumes one available copy of the reserved book, refusing the
     * reservation outright if none is left.
     *
     * @param reservationDTO the entity to save.
     * @return the persisted entity.
     */
    public ReservationDTO save(ReservationDTO reservationDTO) {
        LOG.debug("Request to save Reservation : {}", reservationDTO);
        Reservation reservation = reservationMapper.toEntity(reservationDTO);
        Long bookId = reservation.getBook() == null ? null : reservation.getBook().getId();
        if (holdsCopy(reservation.getStatus(), bookId)) {
            reservation.setBook(bookAvailabilityService.consumeCopy(bookId, ENTITY_NAME));
        }
        reservation = reservationRepository.save(reservation);
        return reservationMapper.toDto(reservation);
    }

    /**
     * Update a reservation. Releases or consumes a copy of the book when the status transitions
     * to/from an active state (WAITING/READY), so the available copies count stays accurate.
     *
     * @param reservationDTO the entity to save.
     * @return the persisted entity.
     */
    public ReservationDTO update(ReservationDTO reservationDTO) {
        LOG.debug("Request to update Reservation : {}", reservationDTO);
        Reservation existing = reservationRepository
            .findById(reservationDTO.getId())
            .orElseThrow(() -> new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));

        Reservation reservation = reservationMapper.toEntity(reservationDTO);
        Long oldBookId = existing.getBook() == null ? null : existing.getBook().getId();
        Long newBookId = reservation.getBook() == null ? null : reservation.getBook().getId();
        reservation.setBook(reconcileAvailability(existing.getStatus(), oldBookId, reservation.getStatus(), newBookId));
        reservation = reservationRepository.save(reservation);
        return reservationMapper.toDto(reservation);
    }

    /**
     * Partially update a reservation. Reconciles the available copies count when the patch changes
     * the status and/or the reserved book.
     *
     * @param reservationDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<ReservationDTO> partialUpdate(ReservationDTO reservationDTO) {
        LOG.debug("Request to partially update Reservation : {}", reservationDTO);

        return reservationRepository
            .findById(reservationDTO.getId())
            .map(existingReservation -> {
                ReservationStatus oldStatus = existingReservation.getStatus();
                Long oldBookId = existingReservation.getBook() == null ? null : existingReservation.getBook().getId();

                reservationMapper.partialUpdate(existingReservation, reservationDTO);

                Long newBookId = existingReservation.getBook() == null ? null : existingReservation.getBook().getId();
                if (oldStatus != existingReservation.getStatus() || !Objects.equals(oldBookId, newBookId)) {
                    existingReservation.setBook(reconcileAvailability(oldStatus, oldBookId, existingReservation.getStatus(), newBookId));
                }

                return existingReservation;
            })
            .map(reservationRepository::save)
            .map(reservationMapper::toDto);
    }

    /**
     * Get all the reservations with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<ReservationDTO> findAllWithEagerRelationships(Pageable pageable) {
        return reservationRepository.findAllWithEagerRelationships(pageable).map(reservationMapper::toDto);
    }

    /**
     * Get one reservation by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<ReservationDTO> findOne(Long id) {
        LOG.debug("Request to get Reservation : {}", id);
        return reservationRepository.findOneWithEagerRelationships(id).map(reservationMapper::toDto);
    }

    /**
     * Delete the reservation by id. Releases the held copy back to the book's available stock,
     * unless the reservation was no longer active (cancelled or already fulfilled).
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete Reservation : {}", id);
        reservationRepository
            .findById(id)
            .ifPresent(reservation -> {
                Long bookId = reservation.getBook() == null ? null : reservation.getBook().getId();
                if (holdsCopy(reservation.getStatus(), bookId)) {
                    bookAvailabilityService.releaseCopy(bookId, ENTITY_NAME);
                }
                reservationRepository.deleteById(id);
            });
    }

    /**
     * Converts an active reservation into a loan: the copy already held by the reservation is
     * transferred to the new loan without touching the available copies count, and the reservation
     * is marked {@link ReservationStatus#FULFILLED}.
     *
     * @param id the id of the reservation to convert.
     * @return the newly created loan.
     */
    public LoanDTO convertToLoan(Long id) {
        LOG.debug("Request to convert Reservation to Loan : {}", id);
        Reservation reservation = reservationRepository
            .findById(id)
            .orElseThrow(() -> new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));

        if (reservation.getStatus() != ReservationStatus.WAITING && reservation.getStatus() != ReservationStatus.READY) {
            throw new BadRequestAlertException("Only an active reservation can be converted to a loan", ENTITY_NAME, "notactive");
        }

        LocalDate borrowDate = LocalDate.now();
        Loan loan = new Loan()
            .library(reservation.getLibrary())
            .book(reservation.getBook())
            .member(reservation.getMember())
            .borrowDate(borrowDate)
            .dueDate(borrowDate.plusDays(DEFAULT_LOAN_DURATION_DAYS))
            .status(LoanStatus.BORROWED);
        loan = loanRepository.save(loan);

        reservation.setStatus(ReservationStatus.FULFILLED);
        reservationRepository.save(reservation);

        return loanMapper.toDto(loan);
    }

    /**
     * A reservation holds a copy of a book for as long as it references one and is waiting or ready.
     */
    private static boolean holdsCopy(ReservationStatus status, Long bookId) {
        return bookId != null && (status == ReservationStatus.WAITING || status == ReservationStatus.READY);
    }

    /**
     * Adjusts the available copies count for a status/book transition: releases the hold on the
     * previous book if it was active and is no longer, consumes a copy of the new book if it is now
     * active and wasn't before.
     */
    private Book reconcileAvailability(ReservationStatus oldStatus, Long oldBookId, ReservationStatus newStatus, Long newBookId) {
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
