package com.bibli.web.rest;

import com.bibli.repository.LoanRepository;
import com.bibli.service.LoanQueryService;
import com.bibli.service.LoanService;
import com.bibli.service.criteria.LoanCriteria;
import com.bibli.service.dto.LoanDTO;
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
 * REST controller for managing {@link com.bibli.domain.Loan}.
 */
@RestController
@RequestMapping("/api/loans")
public class LoanResource {

    private static final Logger LOG = LoggerFactory.getLogger(LoanResource.class);

    private static final String ENTITY_NAME = "loan";

    @Value("${jhipster.clientApp.name:bibli}")
    private String applicationName;

    private final LoanService loanService;

    private final LoanRepository loanRepository;

    private final LoanQueryService loanQueryService;

    public LoanResource(LoanService loanService, LoanRepository loanRepository, LoanQueryService loanQueryService) {
        this.loanService = loanService;
        this.loanRepository = loanRepository;
        this.loanQueryService = loanQueryService;
    }

    /**
     * {@code POST  /loans} : Create a new loan.
     *
     * @param loanDTO the loanDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new loanDTO, or with status {@code 400 (Bad Request)} if the loan has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<LoanDTO> createLoan(@Valid @RequestBody LoanDTO loanDTO) throws URISyntaxException {
        LOG.debug("REST request to save Loan : {}", loanDTO);
        if (loanDTO.getId() != null) {
            throw new BadRequestAlertException("A new loan cannot already have an ID", ENTITY_NAME, "idexists");
        }
        loanDTO = loanService.save(loanDTO);
        return ResponseEntity.created(new URI("/api/loans/" + loanDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, loanDTO.getId().toString()))
            .body(loanDTO);
    }

    /**
     * {@code PUT  /loans/:id} : Updates an existing loan.
     *
     * @param id the id of the loanDTO to save.
     * @param loanDTO the loanDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated loanDTO,
     * or with status {@code 400 (Bad Request)} if the loanDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the loanDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<LoanDTO> updateLoan(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody LoanDTO loanDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Loan : {}, {}", id, loanDTO);
        if (loanDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, loanDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!loanRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        loanDTO = loanService.update(loanDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, loanDTO.getId().toString()))
            .body(loanDTO);
    }

    /**
     * {@code PATCH  /loans/:id} : Partial updates given fields of an existing loan, field will ignore if it is null
     *
     * @param id the id of the loanDTO to save.
     * @param loanDTO the loanDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated loanDTO,
     * or with status {@code 400 (Bad Request)} if the loanDTO is not valid,
     * or with status {@code 404 (Not Found)} if the loanDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the loanDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<LoanDTO> partialUpdateLoan(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody LoanDTO loanDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Loan partially : {}, {}", id, loanDTO);
        if (loanDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, loanDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!loanRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<LoanDTO> result = loanService.partialUpdate(loanDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, loanDTO.getId().toString())
        );
    }

    /**
     * {@code POST  /loans/:id/return} : marks a loan as returned and releases its copy back to stock.
     *
     * @param id the id of the loan to return.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated loanDTO.
     */
    @PostMapping("/{id}/return")
    public ResponseEntity<LoanDTO> returnLoan(@PathVariable("id") Long id) {
        LOG.debug("REST request to return Loan : {}", id);
        LoanDTO loanDTO = loanService.returnLoan(id);
        return ResponseEntity.ok().body(loanDTO);
    }

    /**
     * {@code GET  /loans} : get all the Loans.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Loans in body.
     */
    @GetMapping("")
    public ResponseEntity<List<LoanDTO>> getAllLoans(
        LoanCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get Loans by criteria: {}", criteria);

        Page<LoanDTO> page = loanQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /loans/count} : count all the loans.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countLoans(LoanCriteria criteria) {
        LOG.debug("REST request to count Loans by criteria: {}", criteria);
        return ResponseEntity.ok().body(loanQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /loans/:id} : get the "id" loan.
     *
     * @param id the id of the loanDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the loanDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<LoanDTO> getLoan(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Loan : {}", id);
        Optional<LoanDTO> loanDTO = loanService.findOne(id);
        return ResponseUtil.wrapOrNotFound(loanDTO);
    }

    /**
     * {@code DELETE  /loans/:id} : delete the "id" loan.
     *
     * @param id the id of the loanDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLoan(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Loan : {}", id);
        loanService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
