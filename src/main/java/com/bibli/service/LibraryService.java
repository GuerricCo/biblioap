package com.bibli.service;

import com.bibli.domain.Library;
import com.bibli.repository.LibraryRepository;
import com.bibli.repository.UserRepository;
import com.bibli.security.AuthoritiesConstants;
import com.bibli.security.SecurityUtils;
import com.bibli.service.dto.LibraryDTO;
import com.bibli.service.mapper.LibraryMapper;
import com.bibli.web.rest.errors.BadRequestAlertException;
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

    private final UserRepository userRepository;

    public LibraryService(LibraryRepository libraryRepository, LibraryMapper libraryMapper, UserRepository userRepository) {
        this.libraryRepository = libraryRepository;
        this.libraryMapper = libraryMapper;
        this.userRepository = userRepository;
    }

    /**
     * Save a library, owned by the currently authenticated user.
     *
     * @param libraryDTO the entity to save.
     * @return the persisted entity.
     */
    public LibraryDTO save(LibraryDTO libraryDTO) {
        LOG.debug("Request to save Library : {}", libraryDTO);
        Library library = libraryMapper.toEntity(libraryDTO);
        SecurityUtils.getCurrentUserLogin().flatMap(userRepository::findOneByLogin).ifPresent(library::setUser);
        library = libraryRepository.save(library);
        return libraryMapper.toDto(library);
    }

    /**
     * Update a library. The owner is preserved from the existing entity: the frontend form never
     * exposes it, so it must not be overwritten by a null value from the incoming DTO.
     *
     * @param libraryDTO the entity to save.
     * @return the persisted entity.
     */
    public LibraryDTO update(LibraryDTO libraryDTO) {
        LOG.debug("Request to update Library : {}", libraryDTO);
        Library existing = libraryRepository
            .findById(libraryDTO.getId())
            .filter(this::canAccess)
            .orElseThrow(() -> new BadRequestAlertException("Entity not found", "library", "idnotfound"));

        Library library = libraryMapper.toEntity(libraryDTO);
        library.setUser(existing.getUser());
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
            .filter(this::canAccess)
            .map(existingLibrary -> {
                libraryMapper.partialUpdate(existingLibrary, libraryDTO);

                return existingLibrary;
            })
            .map(libraryRepository::save)
            .map(libraryMapper::toDto);
    }

    /**
     * Get one library by id. Returns empty if the current user is neither the owner nor an admin,
     * so that a library is invisible to anyone but its creator.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<LibraryDTO> findOne(Long id) {
        LOG.debug("Request to get Library : {}", id);
        return libraryRepository.findById(id).filter(this::canAccess).map(libraryMapper::toDto);
    }

    /**
     * Delete the library by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete Library : {}", id);
        Library library = libraryRepository
            .findById(id)
            .filter(this::canAccess)
            .orElseThrow(() -> new BadRequestAlertException("Entity not found", "library", "idnotfound"));

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
    }

    /**
     * A library is accessible to admins and to the user who created it.
     */
    private boolean canAccess(Library library) {
        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN)) {
            return true;
        }
        return (
            library.getUser() != null &&
            SecurityUtils.getCurrentUserLogin()
                .map(login -> login.equalsIgnoreCase(library.getUser().getLogin()))
                .orElse(false)
        );
    }
}
