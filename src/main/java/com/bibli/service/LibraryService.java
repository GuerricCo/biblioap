package com.bibli.service;

import com.bibli.domain.Library;
import com.bibli.repository.LibraryRepository;
import com.bibli.service.dto.LibraryDTO;
import com.bibli.service.mapper.LibraryMapper;
import java.util.Optional;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.bibli.domain.Library}.
 */
@Service
@Transactional
public class LibraryService {

    private static final Logger LOG = LoggerFactory.getLogger(LibraryService.class);

    private final LibraryRepository libraryRepository;

    private final LibraryMapper libraryMapper;

    public LibraryService(LibraryRepository libraryRepository, LibraryMapper libraryMapper) {
        this.libraryRepository = libraryRepository;
        this.libraryMapper = libraryMapper;
    }

    /**
     * Save a library.
     *
     * @param libraryDTO the entity to save.
     * @return the persisted entity.
     */
    public LibraryDTO save(LibraryDTO libraryDTO) {
        LOG.debug("Request to save Library : {}", libraryDTO);
        Library library = libraryMapper.toEntity(libraryDTO);
        library = libraryRepository.save(library);
        return libraryMapper.toDto(library);
    }

    /**
     * Update a library.
     *
     * @param libraryDTO the entity to save.
     * @return the persisted entity.
     */
    public LibraryDTO update(LibraryDTO libraryDTO) {
        LOG.debug("Request to update Library : {}", libraryDTO);
        Library library = libraryMapper.toEntity(libraryDTO);
        library = libraryRepository.save(library);
        return libraryMapper.toDto(library);
    }

    /**
     * Partially update a library.
     *
     * @param libraryDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<LibraryDTO> partialUpdate(LibraryDTO libraryDTO) {
        LOG.debug("Request to partially update Library : {}", libraryDTO);

        return libraryRepository
            .findById(libraryDTO.getId())
            .map(existingLibrary -> {
                libraryMapper.partialUpdate(existingLibrary, libraryDTO);

                return existingLibrary;
            })
            .map(libraryRepository::save)
            .map(libraryMapper::toDto);
    }

    /**
     * Get one library by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<LibraryDTO> findOne(Long id) {
        LOG.debug("Request to get Library : {}", id);
        return libraryRepository.findById(id).map(libraryMapper::toDto);
    }

    /**
     * Delete the library by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete Library : {}", id);
        libraryRepository
            .findById(id)
            .ifPresent(library -> {
                Hibernate.initialize(library.getReviews());
                library.getReviews().clear();

                Hibernate.initialize(library.getLoans());
                library.getLoans().clear();

                Hibernate.initialize(library.getReservations());
                library.getReservations().clear();

                Hibernate.initialize(library.getMembers());
                library.getMembers().clear();

                Hibernate.initialize(library.getBooks());
                library.getBooks().clear();

                libraryRepository.saveAndFlush(library);
                libraryRepository.deleteById(id);
            });
    }
}
