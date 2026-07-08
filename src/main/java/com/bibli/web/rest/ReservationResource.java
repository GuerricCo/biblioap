package com.bibli.web.rest;

import com.bibli.repository.ReservationRepository;
import com.bibli.service.ReservationQueryService;
import com.bibli.service.ReservationService;
import com.bibli.service.criteria.ReservationCriteria;
import com.bibli.service.dto.LoanDTO;
import com.bibli.service.dto.ReservationDTO;
import com.bibli.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.bibli.domain.Reservation}.
 */
@RestController
@RequestMapping("/api/reservations")
public class ReservationResource {

    private static final Logger LOG = LoggerFactory.getLogger(ReservationResource.class);

    private static final String ENTITY_NAME = "reservation";

    @Value("${jhipster.clientApp.name:bibli}")
    private String applicationName;

    private final ReservationService reservationService;

    private final ReservationRepository reservationRepository;

    private final ReservationQueryService reservationQueryService;

    public ReservationResource(
        ReservationService reservationService,
        ReservationRepository reservationRepository,
        ReservationQueryService reservationQueryService
    ) {
        this.reservationService = reservationService;
        this.reservationRepository = reservationRepository;
        this.reservationQueryService = reservationQueryService;
    }

    /**
     * {@code POST  /reservations} : Create a new reservation.
     *
     * @param reservationDTO the reservationDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new reservationDTO, or with status {@code 400 (Bad Request)} if the reservation has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<ReservationDTO> createReservation(@Valid @RequestBody ReservationDTO reservationDTO) throws URISyntaxException {
        LOG.debug("REST request to save Reservation : {}", reservationDTO);
        if (reservationDTO.getId() != null) {
            throw new BadRequestAlertException("A new reservation cannot already have an ID", ENTITY_NAME, "idexists");
        }
        reservationDTO = reservationService.save(reservationDTO);
        return ResponseEntity.created(new URI("/api/reservations/" + reservationDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, reservationDTO.getId().toString()))
            .body(reservationDTO);
    }

    /**
     * {@code PUT  /reservations/:id} : Updates an existing reservation.
     *
     * @param id the id of the reservationDTO to save.
     * @param reservationDTO the reservationDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated reservationDTO,
     * or with status {@code 400 (Bad Request)} if the reservationDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the reservationDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ReservationDTO> updateReservation(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody ReservationDTO reservationDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Reservation : {}, {}", id, reservationDTO);
        if (reservationDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, reservationDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!reservationRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        reservationDTO = reservationService.update(reservationDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, reservationDTO.getId().toString()))
            .body(reservationDTO);
    }

    /**
     * {@code PATCH  /reservations/:id} : Partial updates given fields of an existing reservation, field will ignore if it is null
     *
     * @param id the id of the reservationDTO to save.
     * @param reservationDTO the reservationDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated reservationDTO,
     * or with status {@code 400 (Bad Request)} if the reservationDTO is not valid,
     * or with status {@code 404 (Not Found)} if the reservationDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the reservationDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<ReservationDTO> partialUpdateReservation(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody ReservationDTO reservationDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Reservation partially : {}, {}", id, reservationDTO);
        if (reservationDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, reservationDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!reservationRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<ReservationDTO> result = reservationService.partialUpdate(reservationDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, reservationDTO.getId().toString())
        );
    }

    /**
     * {@code POST  /reservations/:id/convert-to-loan} : converts an active reservation into a loan.
     *
     * @param id the id of the reservation to convert.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new loanDTO.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/{id}/convert-to-loan")
    public ResponseEntity<LoanDTO> convertReservationToLoan(@PathVariable("id") Long id) throws URISyntaxException {
        LOG.debug("REST request to convert Reservation {} to a Loan", id);
        LoanDTO loanDTO = reservationService.convertToLoan(id);
        return ResponseEntity.created(new URI("/api/loans/" + loanDTO.getId())).body(loanDTO);
    }

    /**
     * {@code GET  /reservations} : get all the Reservations.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Reservations in body.
     */
    @GetMapping("")
    public ResponseEntity<List<ReservationDTO>> getAllReservations(
        ReservationCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get Reservations by criteria: {}", criteria);

        Page<ReservationDTO> page = reservationQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /reservations/count} : count all the reservations.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countReservations(ReservationCriteria criteria) {
        LOG.debug("REST request to count Reservations by criteria: {}", criteria);
        return ResponseEntity.ok().body(reservationQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /reservations/:id} : get the "id" reservation.
     *
     * @param id the id of the reservationDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the reservationDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReservationDTO> getReservation(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Reservation : {}", id);
        Optional<ReservationDTO> reservationDTO = reservationService.findOne(id);
        return ResponseUtil.wrapOrNotFound(reservationDTO);
    }

    /**
     * {@code DELETE  /reservations/:id} : delete the "id" reservation.
     *
     * @param id the id of the reservationDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Reservation : {}", id);
        reservationService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
