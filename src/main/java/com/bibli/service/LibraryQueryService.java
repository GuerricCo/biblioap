package com.bibli.service;

import com.bibli.domain.*; // for static metamodels
import com.bibli.domain.Library;
import com.bibli.repository.LibraryRepository;
import com.bibli.service.criteria.LibraryCriteria;
import com.bibli.service.dto.LibraryDTO;
import com.bibli.service.mapper.LibraryMapper;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link Library} entities in the database.
 * The main input is a {@link LibraryCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link LibraryDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class LibraryQueryService extends QueryService<Library> {

    private static final Logger LOG = LoggerFactory.getLogger(LibraryQueryService.class);

    private final LibraryRepository libraryRepository;

    private final LibraryMapper libraryMapper;

    public LibraryQueryService(LibraryRepository libraryRepository, LibraryMapper libraryMapper) {
        this.libraryRepository = libraryRepository;
        this.libraryMapper = libraryMapper;
    }

    /**
     * Return a {@link List} of {@link LibraryDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<LibraryDTO> findByCriteria(LibraryCriteria criteria) {
        LOG.debug("find by criteria : {}", criteria);
        final Specification<Library> specification = createSpecification(criteria);
        return libraryMapper.toDto(libraryRepository.findAll(specification));
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(LibraryCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<Library> specification = createSpecification(criteria);
        return libraryRepository.count(specification);
    }

    /**
     * Function to convert {@link LibraryCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Library> createSpecification(LibraryCriteria criteria) {
        Specification<Library> specification = Specification.unrestricted();
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = Specification.allOf(
                Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : Specification.unrestricted(),
                buildRangeSpecification(criteria.getId(), Library_.id),
                buildStringSpecification(criteria.getName(), Library_.name),
                buildStringSpecification(criteria.getAddress(), Library_.address),
                buildStringSpecification(criteria.getCity(), Library_.city),
                buildStringSpecification(criteria.getPhone(), Library_.phone),
                buildStringSpecification(criteria.getEmail(), Library_.email)
            );
        }
        return specification;
    }
}
