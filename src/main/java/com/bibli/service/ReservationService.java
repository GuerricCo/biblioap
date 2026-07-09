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
 *
 * <p>A reservation never blocks: if a copy is available it is consumed immediately and the
 * reservation is {@link ReservationStatus#READY}; otherwise the reservation is queued as
 * {@link ReservationStatus#WAITING} until a copy is released (loan return, cancellation...), at
 * which point the oldest waiting reservation for that book is promoted and its member notified.
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

    private final ReservationQueueService reservationQueueService;

    private final LoanRepository loanRepository;

    private final LoanMapper loanMapper;

    public ReservationService(
        ReservationRepository reservationRepository,
        ReservationMapper reservationMapper,
        BookAvailabilityService bookAvailabilityService,
        ReservationQueueService reservationQueueService,
        LoanRepository loanRepository,
        LoanMapper loanMapper
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationMapper = reservationMapper;
        this.bookAvailabilityService = bookAvailabilityService;
        this.reservationQueueService = reservationQueueService;
        this.loanRepository = loanRepository;
        this.loanMapper = loanMapper;
    }

    /**
     * Save a reservation. Never refuses: consumes a copy and marks the reservation
     * {@link ReservationStatus#READY} if one is available, otherwise queues it as
     * {@link ReservationStatus#WAITING}.
     *
     * @param reservationDTO the entity to save.
     * @return the persisted entity.
     */
    public ReservationDTO save(ReservationDTO reservationDTO) {
        LOG.debug("Request to save Reservation : {}", reservationDTO);
        Reservation reservation = reservationMapper.toEntity(reservationDTO);
        Long bookId = reservation.getBook() == null ? null : reservation.getBook().getId();
        if (bookId != null && bookAvailabilityService.hasAvailableCopy(bookId, ENTITY_NAME)) {
            reservation.setBook(bookAvailabilityService.consumeCopy(bookId, ENTITY_NAME));
            reservation.setStatus(ReservationStatus.READY);
        } else {
            reservation.setStatus(ReservationStatus.WAITING);
        }
        reservation = reservationRepository.save(reservation);
        return reservationMapper.toDto(reservation);
    }

    /**
     * Update a reservation. Releases or consumes a copy of the book when the status transitions
     * to/from {@link ReservationStatus#READY}, so the available copies count stays accurate; a
     * release also promotes the next waiting reservation for that book, if any.
     *
     * @param reservationDTO the entity to save.
     * @return the persisted entity.
     */
    public ReservationDTO update(ReservationDTO reservationDTO) {
        LOG.debug("Request to update Reservation : {}", reservationDTO);
        Reservation existing = reservationRepository
            .findById(reservationDTO.getId())
            .orElseThrow(() -> new BusinessException("Entity not found", ENTITY_NAME, "idnotfound"));

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
     * Delete the reservation by id. If it held a copy (READY), releases it back to stock and
     * promotes the next waiting reservation for that book, if any.
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
                    reservationQueueService.releaseCopyAndPromoteQueue(bookId, ENTITY_NAME);
                }
                reservationRepository.deleteById(id);
            });
    }

    /**
     * Converts a {@link ReservationStatus#READY} reservation into a loan: the copy already held by
     * the reservation is transferred to the new loan without touching the available copies count,
     * and the reservation is marked {@link ReservationStatus#FULFILLED}.
     *
     * @param id the id of the reservation to convert.
     * @return the newly created loan.
     */
    public LoanDTO convertToLoan(Long id) {
        LOG.debug("Request to convert Reservation to Loan : {}", id);
        Reservation reservation = reservationRepository
            .findById(id)
            .orElseThrow(() -> new BusinessException("Entity not found", ENTITY_NAME, "idnotfound"));

        if (reservation.getStatus() != ReservationStatus.READY) {
            throw new BusinessException("Only a reservation with a copy held (READY) can be converted to a loan", ENTITY_NAME, "notactive");
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
     * Only a {@link ReservationStatus#READY} reservation actually holds a copy of its book; a
     * {@link ReservationStatus#WAITING} one is merely queued.
     */
    private static boolean holdsCopy(ReservationStatus status, Long bookId) {
        return bookId != null && status == ReservationStatus.READY;
    }

    /**
     * Adjusts the available copies count for a status/book transition: releases the hold on the
     * previous book if it was held and is no longer (promoting the next waiting reservation for it),
     * consumes a copy of the new book if it is now held and wasn't before.
     */
    private Book reconcileAvailability(ReservationStatus oldStatus, Long oldBookId, ReservationStatus newStatus, Long newBookId) {
        boolean wasHeld = holdsCopy(oldStatus, oldBookId);
        boolean isNowHeld = holdsCopy(newStatus, newBookId);

        if (Objects.equals(oldBookId, newBookId)) {
            if (wasHeld && !isNowHeld) {
                reservationQueueService.releaseCopyAndPromoteQueue(newBookId, ENTITY_NAME);
                return bookAvailabilityService.findBook(newBookId, ENTITY_NAME);
            }
            if (!wasHeld && isNowHeld) {
                return bookAvailabilityService.consumeCopy(newBookId, ENTITY_NAME);
            }
            return newBookId == null ? null : bookAvailabilityService.findBook(newBookId, ENTITY_NAME);
        }

        if (wasHeld) {
            reservationQueueService.releaseCopyAndPromoteQueue(oldBookId, ENTITY_NAME);
        }
        if (isNowHeld) {
            return bookAvailabilityService.consumeCopy(newBookId, ENTITY_NAME);
        }
        return newBookId == null ? null : bookAvailabilityService.findBook(newBookId, ENTITY_NAME);
    }
}
