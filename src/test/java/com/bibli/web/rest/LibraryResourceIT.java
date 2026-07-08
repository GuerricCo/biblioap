package com.bibli.web.rest;

import static com.bibli.domain.LibraryAsserts.*;
import static com.bibli.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.bibli.IntegrationTest;
import com.bibli.domain.Library;
import com.bibli.repository.LibraryRepository;
import com.bibli.repository.UserRepository;
import com.bibli.service.dto.LibraryDTO;
import com.bibli.service.mapper.LibraryMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
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
 * Integration tests for the {@link LibraryResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class LibraryResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_ADDRESS = "AAAAAAAAAA";
    private static final String UPDATED_ADDRESS = "BBBBBBBBBB";

    private static final String DEFAULT_CITY = "AAAAAAAAAA";
    private static final String UPDATED_CITY = "BBBBBBBBBB";

    private static final String DEFAULT_PHONE = "AAAAAAAAAA";
    private static final String UPDATED_PHONE = "BBBBBBBBBB";

    private static final String DEFAULT_EMAIL = "AAAAAAAAAA";
    private static final String UPDATED_EMAIL = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/libraries";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private LibraryRepository libraryRepository;

    @Autowired
    private LibraryMapper libraryMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restLibraryMockMvc;

    private Library library;

    private Library insertedLibrary;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Library createEntity() {
        return new Library().name(DEFAULT_NAME).address(DEFAULT_ADDRESS).city(DEFAULT_CITY).phone(DEFAULT_PHONE).email(DEFAULT_EMAIL);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Library createUpdatedEntity() {
        return new Library().name(UPDATED_NAME).address(UPDATED_ADDRESS).city(UPDATED_CITY).phone(UPDATED_PHONE).email(UPDATED_EMAIL);
    }

    @BeforeEach
    void initTest() {
        library = createEntity();
        // Owned by the "user" login, matching the default @WithMockUser principal for this test class.
        userRepository.findOneByLogin("user").ifPresent(library::setUser);
    }

    @AfterEach
    void cleanup() {
        if (insertedLibrary != null) {
            libraryRepository.delete(insertedLibrary);
            insertedLibrary = null;
        }
    }

    @Test
    @Transactional
    void createLibrary() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Library
        LibraryDTO libraryDTO = libraryMapper.toDto(library);
        var returnedLibraryDTO = om.readValue(
            restLibraryMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(libraryDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            LibraryDTO.class
        );

        // Validate the Library in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedLibrary = libraryMapper.toEntity(returnedLibraryDTO);
        assertLibraryUpdatableFieldsEquals(returnedLibrary, getPersistedLibrary(returnedLibrary));

        insertedLibrary = returnedLibrary;
    }

    @Test
    @Transactional
    void createLibraryWithExistingId() throws Exception {
        // Create the Library with an existing ID
        library.setId(1L);
        LibraryDTO libraryDTO = libraryMapper.toDto(library);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restLibraryMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(libraryDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Library in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        library.setName(null);

        // Create the Library, which fails.
        LibraryDTO libraryDTO = libraryMapper.toDto(library);

        restLibraryMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(libraryDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllLibraries() throws Exception {
        // Initialize the database
        insertedLibrary = libraryRepository.saveAndFlush(library);

        // Get all the libraryList
        restLibraryMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(library.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].address").value(hasItem(DEFAULT_ADDRESS)))
            .andExpect(jsonPath("$.[*].city").value(hasItem(DEFAULT_CITY)))
            .andExpect(jsonPath("$.[*].phone").value(hasItem(DEFAULT_PHONE)))
            .andExpect(jsonPath("$.[*].email").value(hasItem(DEFAULT_EMAIL)));
    }

    @Test
    @Transactional
    void getLibrary() throws Exception {
        // Initialize the database
        insertedLibrary = libraryRepository.saveAndFlush(library);

        // Get the library
        restLibraryMockMvc
            .perform(get(ENTITY_API_URL_ID, library.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(library.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.address").value(DEFAULT_ADDRESS))
            .andExpect(jsonPath("$.city").value(DEFAULT_CITY))
            .andExpect(jsonPath("$.phone").value(DEFAULT_PHONE))
            .andExpect(jsonPath("$.email").value(DEFAULT_EMAIL));
    }

    @Test
    @Transactional
    void getLibrariesByIdFiltering() throws Exception {
        // Initialize the database
        insertedLibrary = libraryRepository.saveAndFlush(library);

        Long id = library.getId();

        defaultLibraryFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultLibraryFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultLibraryFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllLibrariesByNameIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedLibrary = libraryRepository.saveAndFlush(library);

        // Get all the libraryList where name equals to
        defaultLibraryFiltering("name.equals=" + DEFAULT_NAME, "name.equals=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllLibrariesByNameIsInShouldWork() throws Exception {
        // Initialize the database
        insertedLibrary = libraryRepository.saveAndFlush(library);

        // Get all the libraryList where name in
        defaultLibraryFiltering("name.in=" + DEFAULT_NAME + "," + UPDATED_NAME, "name.in=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllLibrariesByNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedLibrary = libraryRepository.saveAndFlush(library);

        // Get all the libraryList where name is not null
        defaultLibraryFiltering("name.specified=true", "name.specified=false");
    }

    @Test
    @Transactional
    void getAllLibrariesByNameContainsSomething() throws Exception {
        // Initialize the database
        insertedLibrary = libraryRepository.saveAndFlush(library);

        // Get all the libraryList where name contains
        defaultLibraryFiltering("name.contains=" + DEFAULT_NAME, "name.contains=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllLibrariesByNameNotContainsSomething() throws Exception {
        // Initialize the database
        insertedLibrary = libraryRepository.saveAndFlush(library);

        // Get all the libraryList where name does not contain
        defaultLibraryFiltering("name.doesNotContain=" + UPDATED_NAME, "name.doesNotContain=" + DEFAULT_NAME);
    }

    @Test
    @Transactional
    void getAllLibrariesByAddressIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedLibrary = libraryRepository.saveAndFlush(library);

        // Get all the libraryList where address equals to
        defaultLibraryFiltering("address.equals=" + DEFAULT_ADDRESS, "address.equals=" + UPDATED_ADDRESS);
    }

    @Test
    @Transactional
    void getAllLibrariesByAddressIsInShouldWork() throws Exception {
        // Initialize the database
        insertedLibrary = libraryRepository.saveAndFlush(library);

        // Get all the libraryList where address in
        defaultLibraryFiltering("address.in=" + DEFAULT_ADDRESS + "," + UPDATED_ADDRESS, "address.in=" + UPDATED_ADDRESS);
    }

    @Test
    @Transactional
    void getAllLibrariesByAddressIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedLibrary = libraryRepository.saveAndFlush(library);

        // Get all the libraryList where address is not null
        defaultLibraryFiltering("address.specified=true", "address.specified=false");
    }

    @Test
    @Transactional
    void getAllLibrariesByAddressContainsSomething() throws Exception {
        // Initialize the database
        insertedLibrary = libraryRepository.saveAndFlush(library);

        // Get all the libraryList where address contains
        defaultLibraryFiltering("address.contains=" + DEFAULT_ADDRESS, "address.contains=" + UPDATED_ADDRESS);
    }

    @Test
    @Transactional
    void getAllLibrariesByAddressNotContainsSomething() throws Exception {
        // Initialize the database
        insertedLibrary = libraryRepository.saveAndFlush(library);

        // Get all the libraryList where address does not contain
        defaultLibraryFiltering("address.doesNotContain=" + UPDATED_ADDRESS, "address.doesNotContain=" + DEFAULT_ADDRESS);
    }

    @Test
    @Transactional
    void getAllLibrariesByCityIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedLibrary = libraryRepository.saveAndFlush(library);

        // Get all the libraryList where city equals to
        defaultLibraryFiltering("city.equals=" + DEFAULT_CITY, "city.equals=" + UPDATED_CITY);
    }

    @Test
    @Transactional
    void getAllLibrariesByCityIsInShouldWork() throws Exception {
        // Initialize the database
        insertedLibrary = libraryRepository.saveAndFlush(library);

        // Get all the libraryList where city in
        defaultLibraryFiltering("city.in=" + DEFAULT_CITY + "," + UPDATED_CITY, "city.in=" + UPDATED_CITY);
    }

    @Test
    @Transactional
    void getAllLibrariesByCityIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedLibrary = libraryRepository.saveAndFlush(library);

        // Get all the libraryList where city is not null
        defaultLibraryFiltering("city.specified=true", "city.specified=false");
    }

    @Test
    @Transactional
    void getAllLibrariesByCityContainsSomething() throws Exception {
        // Initialize the database
        insertedLibrary = libraryRepository.saveAndFlush(library);

        // Get all the libraryList where city contains
        defaultLibraryFiltering("city.contains=" + DEFAULT_CITY, "city.contains=" + UPDATED_CITY);
    }

    @Test
    @Transactional
    void getAllLibrariesByCityNotContainsSomething() throws Exception {
        // Initialize the database
        insertedLibrary = libraryRepository.saveAndFlush(library);

        // Get all the libraryList where city does not contain
        defaultLibraryFiltering("city.doesNotContain=" + UPDATED_CITY, "city.doesNotContain=" + DEFAULT_CITY);
    }

    @Test
    @Transactional
    void getAllLibrariesByPhoneIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedLibrary = libraryRepository.saveAndFlush(library);

        // Get all the libraryList where phone equals to
        defaultLibraryFiltering("phone.equals=" + DEFAULT_PHONE, "phone.equals=" + UPDATED_PHONE);
    }

    @Test
    @Transactional
    void getAllLibrariesByPhoneIsInShouldWork() throws Exception {
        // Initialize the database
        insertedLibrary = libraryRepository.saveAndFlush(library);

        // Get all the libraryList where phone in
        defaultLibraryFiltering("phone.in=" + DEFAULT_PHONE + "," + UPDATED_PHONE, "phone.in=" + UPDATED_PHONE);
    }

    @Test
    @Transactional
    void getAllLibrariesByPhoneIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedLibrary = libraryRepository.saveAndFlush(library);

        // Get all the libraryList where phone is not null
        defaultLibraryFiltering("phone.specified=true", "phone.specified=false");
    }

    @Test
    @Transactional
    void getAllLibrariesByPhoneContainsSomething() throws Exception {
        // Initialize the database
        insertedLibrary = libraryRepository.saveAndFlush(library);

        // Get all the libraryList where phone contains
        defaultLibraryFiltering("phone.contains=" + DEFAULT_PHONE, "phone.contains=" + UPDATED_PHONE);
    }

    @Test
    @Transactional
    void getAllLibrariesByPhoneNotContainsSomething() throws Exception {
        // Initialize the database
        insertedLibrary = libraryRepository.saveAndFlush(library);

        // Get all the libraryList where phone does not contain
        defaultLibraryFiltering("phone.doesNotContain=" + UPDATED_PHONE, "phone.doesNotContain=" + DEFAULT_PHONE);
    }

    @Test
    @Transactional
    void getAllLibrariesByEmailIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedLibrary = libraryRepository.saveAndFlush(library);

        // Get all the libraryList where email equals to
        defaultLibraryFiltering("email.equals=" + DEFAULT_EMAIL, "email.equals=" + UPDATED_EMAIL);
    }

    @Test
    @Transactional
    void getAllLibrariesByEmailIsInShouldWork() throws Exception {
        // Initialize the database
        insertedLibrary = libraryRepository.saveAndFlush(library);

        // Get all the libraryList where email in
        defaultLibraryFiltering("email.in=" + DEFAULT_EMAIL + "," + UPDATED_EMAIL, "email.in=" + UPDATED_EMAIL);
    }

    @Test
    @Transactional
    void getAllLibrariesByEmailIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedLibrary = libraryRepository.saveAndFlush(library);

        // Get all the libraryList where email is not null
        defaultLibraryFiltering("email.specified=true", "email.specified=false");
    }

    @Test
    @Transactional
    void getAllLibrariesByEmailContainsSomething() throws Exception {
        // Initialize the database
        insertedLibrary = libraryRepository.saveAndFlush(library);

        // Get all the libraryList where email contains
        defaultLibraryFiltering("email.contains=" + DEFAULT_EMAIL, "email.contains=" + UPDATED_EMAIL);
    }

    @Test
    @Transactional
    void getAllLibrariesByEmailNotContainsSomething() throws Exception {
        // Initialize the database
        insertedLibrary = libraryRepository.saveAndFlush(library);

        // Get all the libraryList where email does not contain
        defaultLibraryFiltering("email.doesNotContain=" + UPDATED_EMAIL, "email.doesNotContain=" + DEFAULT_EMAIL);
    }

    private void defaultLibraryFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultLibraryShouldBeFound(shouldBeFound);
        defaultLibraryShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultLibraryShouldBeFound(String filter) throws Exception {
        restLibraryMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(library.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].address").value(hasItem(DEFAULT_ADDRESS)))
            .andExpect(jsonPath("$.[*].city").value(hasItem(DEFAULT_CITY)))
            .andExpect(jsonPath("$.[*].phone").value(hasItem(DEFAULT_PHONE)))
            .andExpect(jsonPath("$.[*].email").value(hasItem(DEFAULT_EMAIL)));

        // Check, that the count call also returns 1
        restLibraryMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultLibraryShouldNotBeFound(String filter) throws Exception {
        restLibraryMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restLibraryMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingLibrary() throws Exception {
        // Get the library
        restLibraryMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingLibrary() throws Exception {
        // Initialize the database
        insertedLibrary = libraryRepository.saveAndFlush(library);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the library
        Library updatedLibrary = libraryRepository.findById(library.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedLibrary are not directly saved in db
        em.detach(updatedLibrary);
        updatedLibrary.name(UPDATED_NAME).address(UPDATED_ADDRESS).city(UPDATED_CITY).phone(UPDATED_PHONE).email(UPDATED_EMAIL);
        LibraryDTO libraryDTO = libraryMapper.toDto(updatedLibrary);

        restLibraryMockMvc
            .perform(
                put(ENTITY_API_URL_ID, libraryDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(libraryDTO))
            )
            .andExpect(status().isOk());

        // Validate the Library in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedLibraryToMatchAllProperties(updatedLibrary);
    }

    @Test
    @Transactional
    void putNonExistingLibrary() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        library.setId(longCount.incrementAndGet());

        // Create the Library
        LibraryDTO libraryDTO = libraryMapper.toDto(library);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restLibraryMockMvc
            .perform(
                put(ENTITY_API_URL_ID, libraryDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(libraryDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Library in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchLibrary() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        library.setId(longCount.incrementAndGet());

        // Create the Library
        LibraryDTO libraryDTO = libraryMapper.toDto(library);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restLibraryMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(libraryDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Library in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamLibrary() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        library.setId(longCount.incrementAndGet());

        // Create the Library
        LibraryDTO libraryDTO = libraryMapper.toDto(library);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restLibraryMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(libraryDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Library in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateLibraryWithPatch() throws Exception {
        // Initialize the database
        insertedLibrary = libraryRepository.saveAndFlush(library);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the library using partial update
        Library partialUpdatedLibrary = new Library();
        partialUpdatedLibrary.setId(library.getId());

        partialUpdatedLibrary.name(UPDATED_NAME).address(UPDATED_ADDRESS).city(UPDATED_CITY);

        restLibraryMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedLibrary.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedLibrary))
            )
            .andExpect(status().isOk());

        // Validate the Library in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertLibraryUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedLibrary, library), getPersistedLibrary(library));
    }

    @Test
    @Transactional
    void fullUpdateLibraryWithPatch() throws Exception {
        // Initialize the database
        insertedLibrary = libraryRepository.saveAndFlush(library);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the library using partial update
        Library partialUpdatedLibrary = new Library();
        partialUpdatedLibrary.setId(library.getId());

        partialUpdatedLibrary.name(UPDATED_NAME).address(UPDATED_ADDRESS).city(UPDATED_CITY).phone(UPDATED_PHONE).email(UPDATED_EMAIL);

        restLibraryMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedLibrary.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedLibrary))
            )
            .andExpect(status().isOk());

        // Validate the Library in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertLibraryUpdatableFieldsEquals(partialUpdatedLibrary, getPersistedLibrary(partialUpdatedLibrary));
    }

    @Test
    @Transactional
    void patchNonExistingLibrary() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        library.setId(longCount.incrementAndGet());

        // Create the Library
        LibraryDTO libraryDTO = libraryMapper.toDto(library);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restLibraryMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, libraryDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(libraryDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Library in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchLibrary() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        library.setId(longCount.incrementAndGet());

        // Create the Library
        LibraryDTO libraryDTO = libraryMapper.toDto(library);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restLibraryMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(libraryDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Library in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamLibrary() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        library.setId(longCount.incrementAndGet());

        // Create the Library
        LibraryDTO libraryDTO = libraryMapper.toDto(library);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restLibraryMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(libraryDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Library in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteLibrary() throws Exception {
        // Initialize the database
        insertedLibrary = libraryRepository.saveAndFlush(library);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the library
        restLibraryMockMvc
            .perform(delete(ENTITY_API_URL_ID, library.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return libraryRepository.count();
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

    protected Library getPersistedLibrary(Library library) {
        return libraryRepository.findById(library.getId()).orElseThrow();
    }

    protected void assertPersistedLibraryToMatchAllProperties(Library expectedLibrary) {
        assertLibraryAllPropertiesEquals(expectedLibrary, getPersistedLibrary(expectedLibrary));
    }

    protected void assertPersistedLibraryToMatchUpdatableProperties(Library expectedLibrary) {
        assertLibraryAllUpdatablePropertiesEquals(expectedLibrary, getPersistedLibrary(expectedLibrary));
    }
}
