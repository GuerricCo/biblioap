package com.bibli.service;

import com.bibli.domain.Book;
import com.bibli.domain.Reservation;
import com.bibli.domain.enumeration.ReservationStatus;
import com.bibli.repository.BookRepository;
import com.bibli.repository.ReservationRepository;
import com.bibli.service.dto.ReservationDTO;
import com.bibli.service.mapper.ReservationMapper;
import com.bibli.web.rest.errors.BadRequestAlertException;
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

    private static final Logger LOG = LoggerFactory.getLogger(ReservationService.class);

    private final ReservationRepository reservationRepository;

    private final ReservationMapper reservationMapper;

    private final BookRepository bookRepository;

    public ReservationService(
        ReservationRepository reservationRepository,
        ReservationMapper reservationMapper,
        BookRepository bookRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationMapper = reservationMapper;
        this.bookRepository = bookRepository;
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
        if (holdsCopy(reservation.getStatus(), reservation.getBook() == null ? null : reservation.getBook().getId())) {
            reservation.setBook(consumeCopy(reservation.getBook().getId()));
        }
        reservation = reservationRepository.save(reservation);
        return reservationMapper.toDto(reservation);
    }

    /**
     * Update a reservation. Releases or consumes a copy of the book when the status transitions
     * to/from {@link ReservationStatus#CANCELLED}, so the available copies count stays accurate.
     *
     * @param reservationDTO the entity to save.
     * @return the persisted entity.
     */
    public ReservationDTO update(ReservationDTO reservationDTO) {
        LOG.debug("Request to update Reservation : {}", reservationDTO);
        Reservation existing = reservationRepository
            .findById(reservationDTO.getId())
            .orElseThrow(() -> new BadRequestAlertException("Entity not found", "reservation", "idnotfound"));

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
     * unless the reservation was already cancelled (which already released it).
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
                    releaseCopy(bookId);
                }
                reservationRepository.deleteById(id);
            });
    }

    /**
     * A reservation holds a copy of a book for as long as it references one and is not cancelled.
     */
    private static boolean holdsCopy(ReservationStatus status, Long bookId) {
        return bookId != null && status != ReservationStatus.CANCELLED;
    }

    /**
     * Decrements the available copies of the given book, refusing when none is left.
     */
    private Book consumeCopy(Long bookId) {
        Book book = findBook(bookId);
        if (book.getAvailableCopies() == null || book.getAvailableCopies() <= 0) {
            throw new BadRequestAlertException("No available copies for this book", "reservation", "noavailablecopies");
        }
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        return bookRepository.save(book);
    }

    /**
     * Increments the available copies of the given book.
     */
    private Book releaseCopy(Long bookId) {
        Book book = findBook(bookId);
        book.setAvailableCopies((book.getAvailableCopies() == null ? 0 : book.getAvailableCopies()) + 1);
        return bookRepository.save(book);
    }

    private Book findBook(Long bookId) {
        return bookRepository
            .findById(bookId)
            .orElseThrow(() -> new BadRequestAlertException("Book not found", "reservation", "idnotfound"));
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
                return releaseCopy(newBookId);
            }
            if (!wasActive && isNowActive) {
                return consumeCopy(newBookId);
            }
            return newBookId == null ? null : findBook(newBookId);
        }

        if (wasActive) {
            releaseCopy(oldBookId);
        }
        if (isNowActive) {
            return consumeCopy(newBookId);
        }
        return newBookId == null ? null : findBook(newBookId);
    }
}
