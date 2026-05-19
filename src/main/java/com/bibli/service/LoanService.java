package com.bibli.service;

import com.bibli.domain.Loan;
import com.bibli.repository.LoanRepository;
import com.bibli.service.dto.LoanDTO;
import com.bibli.service.mapper.LoanMapper;
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

    private static final Logger LOG = LoggerFactory.getLogger(LoanService.class);

    private final LoanRepository loanRepository;

    private final LoanMapper loanMapper;

    public LoanService(LoanRepository loanRepository, LoanMapper loanMapper) {
        this.loanRepository = loanRepository;
        this.loanMapper = loanMapper;
    }

    /**
     * Save a loan.
     *
     * @param loanDTO the entity to save.
     * @return the persisted entity.
     */
    public LoanDTO save(LoanDTO loanDTO) {
        LOG.debug("Request to save Loan : {}", loanDTO);
        Loan loan = loanMapper.toEntity(loanDTO);
        loan = loanRepository.save(loan);
        return loanMapper.toDto(loan);
    }

    /**
     * Update a loan.
     *
     * @param loanDTO the entity to save.
     * @return the persisted entity.
     */
    public LoanDTO update(LoanDTO loanDTO) {
        LOG.debug("Request to update Loan : {}", loanDTO);
        Loan loan = loanMapper.toEntity(loanDTO);
        loan = loanRepository.save(loan);
        return loanMapper.toDto(loan);
    }

    /**
     * Partially update a loan.
     *
     * @param loanDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<LoanDTO> partialUpdate(LoanDTO loanDTO) {
        LOG.debug("Request to partially update Loan : {}", loanDTO);

        return loanRepository
            .findById(loanDTO.getId())
            .map(existingLoan -> {
                loanMapper.partialUpdate(existingLoan, loanDTO);

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
     * Delete the loan by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete Loan : {}", id);
        loanRepository.deleteById(id);
    }
}
