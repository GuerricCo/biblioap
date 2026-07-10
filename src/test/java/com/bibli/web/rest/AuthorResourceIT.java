package com.bibli.web.rest;

import static com.bibli.domain.AuthorAsserts.*;
import static com.bibli.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.bibli.IntegrationTest;
import com.bibli.domain.Author;
import com.bibli.domain.Book;
import com.bibli.domain.Library;
import com.bibli.repository.AuthorRepository;
import com.bibli.repository.LibraryRepository;
import com.bibli.repository.UserRepository;
import com.bibli.service.dto.AuthorDTO;
import com.bibli.service.mapper.AuthorMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link AuthorResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class AuthorResourceIT {

    private static final String DEFAULT_FIRST_NAME = "AAAAAAAAAA";
    private static final String UPDATED_FIRST_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_LAST_NAME = "AAAAAAAAAA";
    private static final String UPDATED_LAST_NAME = "BBBBBBBBBB";

    private static final LocalDate DEFAULT_BIRTH_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_BIRTH_DATE = LocalDate.now(ZoneId.systemDefault());
    private static final LocalDate SMALLER_BIRTH_DATE = LocalDate.ofEpochDay(-1L);

    private static final String DEFAULT_NATIONALITY = "AAAAAAAAAA";
    private static final String UPDATED_NATIONALITY = "BBBBBBBBBB";

    private static final String DEFAULT_BIOGRAPHY = "AAAAAAAAAA";
    private static final String UPDATED_BIOGRAPHY = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/authors";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private AuthorMapper authorMapper;

    @Autowired
    private LibraryRepository libraryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restAuthorMockMvc;

    private Author author;

    private Author insertedAuthor;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Author createEntity() {
        return new Author()
            .firstName(DEFAULT_FIRST_NAME)
            .lastName(DEFAULT_LAST_NAME)
            .birthDate(DEFAULT_BIRTH_DATE)
            .nationality(DEFAULT_NATIONALITY)
            .biography(DEFAULT_BIOGRAPHY);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Author createUpdatedEntity() {
        return new Author()
            .firstName(UPDATED_FIRST_NAME)
            .lastName(UPDATED_LAST_NAME)
            .birthDate(UPDATED_BIRTH_DATE)
            .nationality(UPDATED_NATIONALITY)
            .biography(UPDATED_BIOGRAPHY);
    }

    @BeforeEach
    void initTest() {
        author = createEntity();
        Library library = LibraryResourceIT.createEntity();
        userRepository.findOneByLogin("user").ifPresent(library::setUser);
        library = libraryRepository.saveAndFlush(library);
        author.setLibrary(library);
    }

    @AfterEach
    void cleanup() {
        if (insertedAuthor != null) {
            authorRepository.delete(insertedAuthor);
            insertedAuthor = null;
        }
    }

    @Test
    @Transactional
    void createAuthor() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Author
        AuthorDTO authorDTO = authorMapper.toDto(author);
        var returnedAuthorDTO = om.readValue(
            restAuthorMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(authorDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            AuthorDTO.class
        );

        // Validate the Author in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedAuthor = authorMapper.toEntity(returnedAuthorDTO);
        assertAuthorUpdatableFieldsEquals(returnedAuthor, getPersistedAuthor(returnedAuthor));

        insertedAuthor = returnedAuthor;
    }

    @Test
    @Transactional
    void createAuthorWithExistingId() throws Exception {
        // Create the Author with an existing ID
        author.setId(1L);
        AuthorDTO authorDTO = authorMapper.toDto(author);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restAuthorMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(authorDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Author in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkFirstNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        author.setFirstName(null);

        // Create the Author, which fails.
        AuthorDTO authorDTO = authorMapper.toDto(author);

        restAuthorMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(authorDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkLastNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        author.setLastName(null);

        // Create the Author, which fails.
        AuthorDTO authorDTO = authorMapper.toDto(author);

        restAuthorMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(authorDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllAuthors() throws Exception {
        // Initialize the database
        insertedAuthor = authorRepository.saveAndFlush(author);

        // Get all the authorList
        restAuthorMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(author.getId().intValue())))
            .andExpect(jsonPath("$.[*].firstName").value(hasItem(DEFAULT_FIRST_NAME)))
            .andExpect(jsonPath("$.[*].lastName").value(hasItem(DEFAULT_LAST_NAME)))
            .andExpect(jsonPath("$.[*].birthDate").value(hasItem(DEFAULT_BIRTH_DATE.toString())))
            .andExpect(jsonPath("$.[*].nationality").value(hasItem(DEFAULT_NATIONALITY)))
            .andExpect(jsonPath("$.[*].biography").value(hasItem(DEFAULT_BIOGRAPHY)));
    }

    @Test
    @Transactional
    void getAuthor() throws Exception {
        // Initialize the database
        insertedAuthor = authorRepository.saveAndFlush(author);

        // Get the author
        restAuthorMockMvc
            .perform(get(ENTITY_API_URL_ID, author.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(author.getId().intValue()))
            .andExpect(jsonPath("$.firstName").value(DEFAULT_FIRST_NAME))
            .andExpect(jsonPath("$.lastName").value(DEFAULT_LAST_NAME))
            .andExpect(jsonPath("$.birthDate").value(DEFAULT_BIRTH_DATE.toString()))
            .andExpect(jsonPath("$.nationality").value(DEFAULT_NATIONALITY))
            .andExpect(jsonPath("$.biography").value(DEFAULT_BIOGRAPHY));
    }

    @Test
    @Transactional
    void getAuthorsByIdFiltering() throws Exception {
        // Initialize the database
        insertedAuthor = authorRepository.saveAndFlush(author);

        Long id = author.getId();

        defaultAuthorFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultAuthorFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultAuthorFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllAuthorsByFirstNameIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedAuthor = authorRepository.saveAndFlush(author);

        // Get all the authorList where firstName equals to
        defaultAuthorFiltering("firstName.equals=" + DEFAULT_FIRST_NAME, "firstName.equals=" + UPDATED_FIRST_NAME);
    }

    @Test
    @Transactional
    void getAllAuthorsByFirstNameIsInShouldWork() throws Exception {
        // Initialize the database
        insertedAuthor = authorRepository.saveAndFlush(author);

        // Get all the authorList where firstName in
        defaultAuthorFiltering("firstName.in=" + DEFAULT_FIRST_NAME + "," + UPDATED_FIRST_NAME, "firstName.in=" + UPDATED_FIRST_NAME);
    }

    @Test
    @Transactional
    void getAllAuthorsByFirstNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedAuthor = authorRepository.saveAndFlush(author);

        // Get all the authorList where firstName is not null
        defaultAuthorFiltering("firstName.specified=true", "firstName.specified=false");
    }

    @Test
    @Transactional
    void getAllAuthorsByFirstNameContainsSomething() throws Exception {
        // Initialize the database
        insertedAuthor = authorRepository.saveAndFlush(author);

        // Get all the authorList where firstName contains
        defaultAuthorFiltering("firstName.contains=" + DEFAULT_FIRST_NAME, "firstName.contains=" + UPDATED_FIRST_NAME);
    }

    @Test
    @Transactional
    void getAllAuthorsByFirstNameNotContainsSomething() throws Exception {
        // Initialize the database
        insertedAuthor = authorRepository.saveAndFlush(author);

        // Get all the authorList where firstName does not contain
        defaultAuthorFiltering("firstName.doesNotContain=" + UPDATED_FIRST_NAME, "firstName.doesNotContain=" + DEFAULT_FIRST_NAME);
    }

    @Test
    @Transactional
    void getAllAuthorsByLastNameIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedAuthor = authorRepository.saveAndFlush(author);

        // Get all the authorList where lastName equals to
        defaultAuthorFiltering("lastName.equals=" + DEFAULT_LAST_NAME, "lastName.equals=" + UPDATED_LAST_NAME);
    }

    @Test
    @Transactional
    void getAllAuthorsByLastNameIsInShouldWork() throws Exception {
        // Initialize the database
        insertedAuthor = authorRepository.saveAndFlush(author);

        // Get all the authorList where lastName in
        defaultAuthorFiltering("lastName.in=" + DEFAULT_LAST_NAME + "," + UPDATED_LAST_NAME, "lastName.in=" + UPDATED_LAST_NAME);
    }

    @Test
    @Transactional
    void getAllAuthorsByLastNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedAuthor = authorRepository.saveAndFlush(author);

        // Get all the authorList where lastName is not null
        defaultAuthorFiltering("lastName.specified=true", "lastName.specified=false");
    }

    @Test
    @Transactional
    void getAllAuthorsByLastNameContainsSomething() throws Exception {
        // Initialize the database
        insertedAuthor = authorRepository.saveAndFlush(author);

        // Get all the authorList where lastName contains
        defaultAuthorFiltering("lastName.contains=" + DEFAULT_LAST_NAME, "lastName.contains=" + UPDATED_LAST_NAME);
    }

    @Test
    @Transactional
    void getAllAuthorsByLastNameNotContainsSomething() throws Exception {
        // Initialize the database
        insertedAuthor = authorRepository.saveAndFlush(author);

        // Get all the authorList where lastName does not contain
        defaultAuthorFiltering("lastName.doesNotContain=" + UPDATED_LAST_NAME, "lastName.doesNotContain=" + DEFAULT_LAST_NAME);
    }

    @Test
    @Transactional
    void getAllAuthorsByBirthDateIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedAuthor = authorRepository.saveAndFlush(author);

        // Get all the authorList where birthDate equals to
        defaultAuthorFiltering("birthDate.equals=" + DEFAULT_BIRTH_DATE, "birthDate.equals=" + UPDATED_BIRTH_DATE);
    }

    @Test
    @Transactional
    void getAllAuthorsByBirthDateIsInShouldWork() throws Exception {
        // Initialize the database
        insertedAuthor = authorRepository.saveAndFlush(author);

        // Get all the authorList where birthDate in
        defaultAuthorFiltering("birthDate.in=" + DEFAULT_BIRTH_DATE + "," + UPDATED_BIRTH_DATE, "birthDate.in=" + UPDATED_BIRTH_DATE);
    }

    @Test
    @Transactional
    void getAllAuthorsByBirthDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedAuthor = authorRepository.saveAndFlush(author);

        // Get all the authorList where birthDate is not null
        defaultAuthorFiltering("birthDate.specified=true", "birthDate.specified=false");
    }

    @Test
    @Transactional
    void getAllAuthorsByBirthDateIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedAuthor = authorRepository.saveAndFlush(author);

        // Get all the authorList where birthDate is greater than or equal to
        defaultAuthorFiltering("birthDate.greaterThanOrEqual=" + DEFAULT_BIRTH_DATE, "birthDate.greaterThanOrEqual=" + UPDATED_BIRTH_DATE);
    }

    @Test
    @Transactional
    void getAllAuthorsByBirthDateIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedAuthor = authorRepository.saveAndFlush(author);

        // Get all the authorList where birthDate is less than or equal to
        defaultAuthorFiltering("birthDate.lessThanOrEqual=" + DEFAULT_BIRTH_DATE, "birthDate.lessThanOrEqual=" + SMALLER_BIRTH_DATE);
    }

    @Test
    @Transactional
    void getAllAuthorsByBirthDateIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedAuthor = authorRepository.saveAndFlush(author);

        // Get all the authorList where birthDate is less than
        defaultAuthorFiltering("birthDate.lessThan=" + UPDATED_BIRTH_DATE, "birthDate.lessThan=" + DEFAULT_BIRTH_DATE);
    }

    @Test
    @Transactional
    void getAllAuthorsByBirthDateIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedAuthor = authorRepository.saveAndFlush(author);

        // Get all the authorList where birthDate is greater than
        defaultAuthorFiltering("birthDate.greaterThan=" + SMALLER_BIRTH_DATE, "birthDate.greaterThan=" + DEFAULT_BIRTH_DATE);
    }

    @Test
    @Transactional
    void getAllAuthorsByNationalityIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedAuthor = authorRepository.saveAndFlush(author);

        // Get all the authorList where nationality equals to
        defaultAuthorFiltering("nationality.equals=" + DEFAULT_NATIONALITY, "nationality.equals=" + UPDATED_NATIONALITY);
    }

    @Test
    @Transactional
    void getAllAuthorsByNationalityIsInShouldWork() throws Exception {
        // Initialize the database
        insertedAuthor = authorRepository.saveAndFlush(author);

        // Get all the authorList where nationality in
        defaultAuthorFiltering(
            "nationality.in=" + DEFAULT_NATIONALITY + "," + UPDATED_NATIONALITY,
            "nationality.in=" + UPDATED_NATIONALITY
        );
    }

    @Test
    @Transactional
    void getAllAuthorsByNationalityIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedAuthor = authorRepository.saveAndFlush(author);

        // Get all the authorList where nationality is not null
        defaultAuthorFiltering("nationality.specified=true", "nationality.specified=false");
    }

    @Test
    @Transactional
    void getAllAuthorsByNationalityContainsSomething() throws Exception {
        // Initialize the database
        insertedAuthor = authorRepository.saveAndFlush(author);

        // Get all the authorList where nationality contains
        defaultAuthorFiltering("nationality.contains=" + DEFAULT_NATIONALITY, "nationality.contains=" + UPDATED_NATIONALITY);
    }

    @Test
    @Transactional
    void getAllAuthorsByNationalityNotContainsSomething() throws Exception {
        // Initialize the database
        insertedAuthor = authorRepository.saveAndFlush(author);

        // Get all the authorList where nationality does not contain
        defaultAuthorFiltering("nationality.doesNotContain=" + UPDATED_NATIONALITY, "nationality.doesNotContain=" + DEFAULT_NATIONALITY);
    }

    @Test
    @Transactional
    void getAllAuthorsByBiographyIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedAuthor = authorRepository.saveAndFlush(author);

        // Get all the authorList where biography equals to
        defaultAuthorFiltering("biography.equals=" + DEFAULT_BIOGRAPHY, "biography.equals=" + UPDATED_BIOGRAPHY);
    }

    @Test
    @Transactional
    void getAllAuthorsByBiographyIsInShouldWork() throws Exception {
        // Initialize the database
        insertedAuthor = authorRepository.saveAndFlush(author);

        // Get all the authorList where biography in
        defaultAuthorFiltering("biography.in=" + DEFAULT_BIOGRAPHY + "," + UPDATED_BIOGRAPHY, "biography.in=" + UPDATED_BIOGRAPHY);
    }

    @Test
    @Transactional
    void getAllAuthorsByBiographyIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedAuthor = authorRepository.saveAndFlush(author);

        // Get all the authorList where biography is not null
        defaultAuthorFiltering("biography.specified=true", "biography.specified=false");
    }

    @Test
    @Transactional
    void getAllAuthorsByBiographyContainsSomething() throws Exception {
        // Initialize the database
        insertedAuthor = authorRepository.saveAndFlush(author);

        // Get all the authorList where biography contains
        defaultAuthorFiltering("biography.contains=" + DEFAULT_BIOGRAPHY, "biography.contains=" + UPDATED_BIOGRAPHY);
    }

    @Test
    @Transactional
    void getAllAuthorsByBiographyNotContainsSomething() throws Exception {
        // Initialize the database
        insertedAuthor = authorRepository.saveAndFlush(author);

        // Get all the authorList where biography does not contain
        defaultAuthorFiltering("biography.doesNotContain=" + UPDATED_BIOGRAPHY, "biography.doesNotContain=" + DEFAULT_BIOGRAPHY);
    }

    @Test
    @Transactional
    void getAllAuthorsByBooksIsEqualToSomething() throws Exception {
        Book books;
        if (TestUtil.findAll(em, Book.class).isEmpty()) {
            authorRepository.saveAndFlush(author);
            books = BookResourceIT.createEntity();
        } else {
            books = TestUtil.findAll(em, Book.class).get(0);
        }
        em.persist(books);
        em.flush();
        author.addBooks(books);
        authorRepository.saveAndFlush(author);
        Long booksId = books.getId();
        // Get all the authorList where books equals to booksId
        defaultAuthorShouldBeFound("booksId.equals=" + booksId);

        // Get all the authorList where books equals to (booksId + 1)
        defaultAuthorShouldNotBeFound("booksId.equals=" + (booksId + 1));
    }

    private void defaultAuthorFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultAuthorShouldBeFound(shouldBeFound);
        defaultAuthorShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultAuthorShouldBeFound(String filter) throws Exception {
        restAuthorMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(author.getId().intValue())))
            .andExpect(jsonPath("$.[*].firstName").value(hasItem(DEFAULT_FIRST_NAME)))
            .andExpect(jsonPath("$.[*].lastName").value(hasItem(DEFAULT_LAST_NAME)))
            .andExpect(jsonPath("$.[*].birthDate").value(hasItem(DEFAULT_BIRTH_DATE.toString())))
            .andExpect(jsonPath("$.[*].nationality").value(hasItem(DEFAULT_NATIONALITY)))
            .andExpect(jsonPath("$.[*].biography").value(hasItem(DEFAULT_BIOGRAPHY)));

        // Check, that the count call also returns 1
        restAuthorMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultAuthorShouldNotBeFound(String filter) throws Exception {
        restAuthorMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restAuthorMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingAuthor() throws Exception {
        // Get the author
        restAuthorMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingAuthor() throws Exception {
        // Initialize the database
        insertedAuthor = authorRepository.saveAndFlush(author);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the author
        Author updatedAuthor = authorRepository.findById(author.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedAuthor are not directly saved in db
        em.detach(updatedAuthor);
        updatedAuthor
            .firstName(UPDATED_FIRST_NAME)
            .lastName(UPDATED_LAST_NAME)
            .birthDate(UPDATED_BIRTH_DATE)
            .nationality(UPDATED_NATIONALITY)
            .biography(UPDATED_BIOGRAPHY);
        AuthorDTO authorDTO = authorMapper.toDto(updatedAuthor);

        restAuthorMockMvc
            .perform(
                put(ENTITY_API_URL_ID, authorDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(authorDTO))
            )
            .andExpect(status().isOk());

        // Validate the Author in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedAuthorToMatchAllProperties(updatedAuthor);
    }

    @Test
    @Transactional
    void putNonExistingAuthor() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        author.setId(longCount.incrementAndGet());

        // Create the Author
        AuthorDTO authorDTO = authorMapper.toDto(author);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAuthorMockMvc
            .perform(
                put(ENTITY_API_URL_ID, authorDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(authorDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Author in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchAuthor() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        author.setId(longCount.incrementAndGet());

        // Create the Author
        AuthorDTO authorDTO = authorMapper.toDto(author);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAuthorMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(authorDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Author in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamAuthor() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        author.setId(longCount.incrementAndGet());

        // Create the Author
        AuthorDTO authorDTO = authorMapper.toDto(author);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAuthorMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(authorDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Author in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateAuthorWithPatch() throws Exception {
        // Initialize the database
        insertedAuthor = authorRepository.saveAndFlush(author);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the author using partial update
        Author partialUpdatedAuthor = new Author();
        partialUpdatedAuthor.setId(author.getId());

        partialUpdatedAuthor.nationality(UPDATED_NATIONALITY).biography(UPDATED_BIOGRAPHY);

        restAuthorMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAuthor.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedAuthor))
            )
            .andExpect(status().isOk());

        // Validate the Author in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertAuthorUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedAuthor, author), getPersistedAuthor(author));
    }

    @Test
    @Transactional
    void fullUpdateAuthorWithPatch() throws Exception {
        // Initialize the database
        insertedAuthor = authorRepository.saveAndFlush(author);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the author using partial update
        Author partialUpdatedAuthor = new Author();
        partialUpdatedAuthor.setId(author.getId());

        partialUpdatedAuthor
            .firstName(UPDATED_FIRST_NAME)
            .lastName(UPDATED_LAST_NAME)
            .birthDate(UPDATED_BIRTH_DATE)
            .nationality(UPDATED_NATIONALITY)
            .biography(UPDATED_BIOGRAPHY);

        restAuthorMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAuthor.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedAuthor))
            )
            .andExpect(status().isOk());

        // Validate the Author in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertAuthorUpdatableFieldsEquals(partialUpdatedAuthor, getPersistedAuthor(partialUpdatedAuthor));
    }

    @Test
    @Transactional
    void patchNonExistingAuthor() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        author.setId(longCount.incrementAndGet());

        // Create the Author
        AuthorDTO authorDTO = authorMapper.toDto(author);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAuthorMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, authorDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(authorDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Author in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchAuthor() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        author.setId(longCount.incrementAndGet());

        // Create the Author
        AuthorDTO authorDTO = authorMapper.toDto(author);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAuthorMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(authorDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Author in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamAuthor() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        author.setId(longCount.incrementAndGet());

        // Create the Author
        AuthorDTO authorDTO = authorMapper.toDto(author);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAuthorMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(authorDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Author in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteAuthor() throws Exception {
        // Initialize the database
        insertedAuthor = authorRepository.saveAndFlush(author);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the author
        restAuthorMockMvc
            .perform(delete(ENTITY_API_URL_ID, author.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return authorRepository.count();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected Author getPersistedAuthor(Author author) {
        return authorRepository.findById(author.getId()).orElseThrow();
    }

    protected void assertPersistedAuthorToMatchAllProperties(Author expectedAuthor) {
        assertAuthorAllPropertiesEquals(expectedAuthor, getPersistedAuthor(expectedAuthor));
    }

    protected void assertPersistedAuthorToMatchUpdatableProperties(Author expectedAuthor) {
        assertAuthorAllUpdatablePropertiesEquals(expectedAuthor, getPersistedAuthor(expectedAuthor));
    }
}
