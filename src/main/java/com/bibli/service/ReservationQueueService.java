package com.bibli.service;

import com.bibli.domain.Reservation;
import com.bibli.domain.enumeration.ReservationStatus;
import com.bibli.repository.ReservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Maintains the per-book waiting list of reservations: whenever a copy is released back to a book's
 * available stock, the oldest waiting reservation for that book (if any) is promoted to
 * {@link ReservationStatus#READY} - the copy is re-consumed on its behalf - and its member is
 * notified by email that the book is available again.
 */
@Service
public class ReservationQueueService {

    private static final Logger LOG = LoggerFactory.getLogger(ReservationQueueService.class);

    private static final String ENTITY_NAME = "reservation";

    private final ReservationRepository reservationRepository;

    private final BookAvailabilityService bookAvailabilityService;

    private final MailService mailService;

    public ReservationQueueService(
        ReservationRepository reservationRepository,
        BookAvailabilityService bookAvailabilityService,
        MailService mailService
    ) {
        this.reservationRepository = reservationRepository;
        this.bookAvailabilityService = bookAvailabilityService;
        this.mailService = mailService;
    }

    /**
     * Releases a copy of the given book and, if a reservation was waiting for it, immediately
     * re-consumes it on that reservation's behalf and notifies its member.
     */
    @Transactional
    public void releaseCopyAndPromoteQueue(Long bookId, String releasingEntityName) {
        bookAvailabilityService.releaseCopy(bookId, releasingEntityName);
        promoteNextWaiting(bookId);
    }

    private void promoteNextWaiting(Long bookId) {
        reservationRepository
            .findFirstByBook_IdAndStatusOrderByReservationDateAscIdAsc(bookId, ReservationStatus.WAITING)
            .ifPresent(next -> {
                bookAvailabilityService.consumeCopy(bookId, ENTITY_NAME);
                next.setStatus(ReservationStatus.READY);
                reservationRepository.save(next);
                notifyMember(next);
            });
    }

    private void notifyMember(Reservation reservation) {
        if (reservation.getMember() == null || reservation.getMember().getEmail() == null || reservation.getBook() == null) {
            return;
        }
        String bookTitle = reservation.getBook().getTitle();
        String firstName = reservation.getMember().getFirstName();
        LOG.debug("Notifying {} that '{}' is available again", reservation.getMember().getEmail(), bookTitle);
        String subject = "Le livre \"" + bookTitle + "\" est de nouveau disponible";
        String content =
            "Bonjour " +
            (firstName != null ? firstName : "") +
            ",\n\nBonne nouvelle : le livre \"" +
            bookTitle +
            "\" que vous avez réservé est de nouveau disponible. " +
            "Vous pouvez venir le récupérer à votre bibliothèque.\n\nÀ bientôt !";
        mailService.sendEmail(reservation.getMember().getEmail(), subject, content, false, false);
    }
}
