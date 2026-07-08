package com.bibli.web.rest;

import static com.bibli.domain.MemberAsserts.*;
import static com.bibli.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.bibli.IntegrationTest;
import com.bibli.domain.Library;
import com.bibli.domain.Member;
import com.bibli.repository.MemberRepository;
import com.bibli.repository.UserRepository;
import com.bibli.service.MemberService;
import com.bibli.service.dto.MemberDTO;
import com.bibli.service.mapper.MemberMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link MemberResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class MemberResourceIT {

    private static final String DEFAULT_FIRST_NAME = "AAAAAAAAAA";
    private static final String UPDATED_FIRST_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_LAST_NAME = "AAAAAAAAAA";
    private static final String UPDATED_LAST_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_EMAIL = "AAAAAAAAAA";
    private static final String UPDATED_EMAIL = "BBBBBBBBBB";

    private static final String DEFAULT_PHONE = "AAAAAAAAAA";
    private static final String UPDATED_PHONE = "BBBBBBBBBB";

    private static final LocalDate DEFAULT_MEMBERSHIP_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_MEMBERSHIP_DATE = LocalDate.now(ZoneId.systemDefault());
    private static final LocalDate SMALLER_MEMBERSHIP_DATE = LocalDate.ofEpochDay(-1L);

    private static final Boolean DEFAULT_ACTIVE = false;
    private static final Boolean UPDATED_ACTIVE = true;

    private static final String ENTITY_API_URL = "/api/members";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private MemberRepository memberRepository;

    @Mock
    private MemberRepository memberRepositoryMock;

    @Autowired
    private MemberMapper memberMapper;

    @Mock
    private MemberService memberServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MockMvc restMemberMockMvc;

    private Member member;

    private Member insertedMember;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Member createEntity() {
        return new Member()
            .firstName(DEFAULT_FIRST_NAME)
            .lastName(DEFAULT_LAST_NAME)
            .email(DEFAULT_EMAIL)
            .phone(DEFAULT_PHONE)
            .membershipDate(DEFAULT_MEMBERSHIP_DATE)
            .active(DEFAULT_ACTIVE);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Member createUpdatedEntity() {
        return new Member()
            .firstName(UPDATED_FIRST_NAME)
            .lastName(UPDATED_LAST_NAME)
            .email(UPDATED_EMAIL)
            .phone(UPDATED_PHONE)
            .membershipDate(UPDATED_MEMBERSHIP_DATE)
            .active(UPDATED_ACTIVE);
    }

    @BeforeEach
    void initTest() {
        member = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedMember != null) {
            memberRepository.delete(insertedMember);
            insertedMember = null;
        }
    }

    @Test
    @Transactional
    void createMember() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Member
        MemberDTO memberDTO = memberMapper.toDto(member);
        var returnedMemberDTO = om.readValue(
            restMemberMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(memberDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            MemberDTO.class
        );

        // Validate the Member in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedMember = memberMapper.toEntity(returnedMemberDTO);
        assertMemberUpdatableFieldsEquals(returnedMember, getPersistedMember(returnedMember));

        insertedMember = returnedMember;
    }

    @Test
    @Transactional
    void createMemberWithExistingId() throws Exception {
        // Create the Member with an existing ID
        member.setId(1L);
        MemberDTO memberDTO = memberMapper.toDto(member);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restMemberMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(memberDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Member in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkFirstNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        member.setFirstName(null);

        // Create the Member, which fails.
        MemberDTO memberDTO = memberMapper.toDto(member);

        restMemberMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(memberDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkLastNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        member.setLastName(null);

        // Create the Member, which fails.
        MemberDTO memberDTO = memberMapper.toDto(member);

        restMemberMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(memberDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkEmailIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        member.setEmail(null);

        // Create the Member, which fails.
        MemberDTO memberDTO = memberMapper.toDto(member);

        restMemberMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(memberDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkActiveIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        member.setActive(null);

        // Create the Member, which fails.
        MemberDTO memberDTO = memberMapper.toDto(member);

        restMemberMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(memberDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllMembers() throws Exception {
        // Initialize the database
        insertedMember = memberRepository.saveAndFlush(member);

        // Get all the memberList
        restMemberMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(member.getId().intValue())))
            .andExpect(jsonPath("$.[*].firstName").value(hasItem(DEFAULT_FIRST_NAME)))
            .andExpect(jsonPath("$.[*].lastName").value(hasItem(DEFAULT_LAST_NAME)))
            .andExpect(jsonPath("$.[*].email").value(hasItem(DEFAULT_EMAIL)))
            .andExpect(jsonPath("$.[*].phone").value(hasItem(DEFAULT_PHONE)))
            .andExpect(jsonPath("$.[*].membershipDate").value(hasItem(DEFAULT_MEMBERSHIP_DATE.toString())))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllMembersWithEagerRelationshipsIsEnabled() throws Exception {
        when(memberServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restMemberMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(memberServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllMembersWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(memberServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restMemberMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(memberRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getMember() throws Exception {
        // Initialize the database
        insertedMember = memberRepository.saveAndFlush(member);

        // Get the member
        restMemberMockMvc
            .perform(get(ENTITY_API_URL_ID, member.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(member.getId().intValue()))
            .andExpect(jsonPath("$.firstName").value(DEFAULT_FIRST_NAME))
            .andExpect(jsonPath("$.lastName").value(DEFAULT_LAST_NAME))
            .andExpect(jsonPath("$.email").value(DEFAULT_EMAIL))
            .andExpect(jsonPath("$.phone").value(DEFAULT_PHONE))
            .andExpect(jsonPath("$.membershipDate").value(DEFAULT_MEMBERSHIP_DATE.toString()))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE));
    }

    @Test
    @Transactional
    void getMembersByIdFiltering() throws Exception {
        // Initialize the database
        insertedMember = memberRepository.saveAndFlush(member);

        Long id = member.getId();

        defaultMemberFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultMemberFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultMemberFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllMembersByFirstNameIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedMember = memberRepository.saveAndFlush(member);

        // Get all the memberList where firstName equals to
        defaultMemberFiltering("firstName.equals=" + DEFAULT_FIRST_NAME, "firstName.equals=" + UPDATED_FIRST_NAME);
    }

    @Test
    @Transactional
    void getAllMembersByFirstNameIsInShouldWork() throws Exception {
        // Initialize the database
        insertedMember = memberRepository.saveAndFlush(member);

        // Get all the memberList where firstName in
        defaultMemberFiltering("firstName.in=" + DEFAULT_FIRST_NAME + "," + UPDATED_FIRST_NAME, "firstName.in=" + UPDATED_FIRST_NAME);
    }

    @Test
    @Transactional
    void getAllMembersByFirstNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedMember = memberRepository.saveAndFlush(member);

        // Get all the memberList where firstName is not null
        defaultMemberFiltering("firstName.specified=true", "firstName.specified=false");
    }

    @Test
    @Transactional
    void getAllMembersByFirstNameContainsSomething() throws Exception {
        // Initialize the database
        insertedMember = memberRepository.saveAndFlush(member);

        // Get all the memberList where firstName contains
        defaultMemberFiltering("firstName.contains=" + DEFAULT_FIRST_NAME, "firstName.contains=" + UPDATED_FIRST_NAME);
    }

    @Test
    @Transactional
    void getAllMembersByFirstNameNotContainsSomething() throws Exception {
        // Initialize the database
        insertedMember = memberRepository.saveAndFlush(member);

        // Get all the memberList where firstName does not contain
        defaultMemberFiltering("firstName.doesNotContain=" + UPDATED_FIRST_NAME, "firstName.doesNotContain=" + DEFAULT_FIRST_NAME);
    }

    @Test
    @Transactional
    void getAllMembersByLastNameIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedMember = memberRepository.saveAndFlush(member);

        // Get all the memberList where lastName equals to
        defaultMemberFiltering("lastName.equals=" + DEFAULT_LAST_NAME, "lastName.equals=" + UPDATED_LAST_NAME);
    }

    @Test
    @Transactional
    void getAllMembersByLastNameIsInShouldWork() throws Exception {
        // Initialize the database
        insertedMember = memberRepository.saveAndFlush(member);

        // Get all the memberList where lastName in
        defaultMemberFiltering("lastName.in=" + DEFAULT_LAST_NAME + "," + UPDATED_LAST_NAME, "lastName.in=" + UPDATED_LAST_NAME);
    }

    @Test
    @Transactional
    void getAllMembersByLastNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedMember = memberRepository.saveAndFlush(member);

        // Get all the memberList where lastName is not null
        defaultMemberFiltering("lastName.specified=true", "lastName.specified=false");
    }

    @Test
    @Transactional
    void getAllMembersByLastNameContainsSomething() throws Exception {
        // Initialize the database
        insertedMember = memberRepository.saveAndFlush(member);

        // Get all the memberList where lastName contains
        defaultMemberFiltering("lastName.contains=" + DEFAULT_LAST_NAME, "lastName.contains=" + UPDATED_LAST_NAME);
    }

    @Test
    @Transactional
    void getAllMembersByLastNameNotContainsSomething() throws Exception {
        // Initialize the database
        insertedMember = memberRepository.saveAndFlush(member);

        // Get all the memberList where lastName does not contain
        defaultMemberFiltering("lastName.doesNotContain=" + UPDATED_LAST_NAME, "lastName.doesNotContain=" + DEFAULT_LAST_NAME);
    }

    @Test
    @Transactional
    void getAllMembersByEmailIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedMember = memberRepository.saveAndFlush(member);

        // Get all the memberList where email equals to
        defaultMemberFiltering("email.equals=" + DEFAULT_EMAIL, "email.equals=" + UPDATED_EMAIL);
    }

    @Test
    @Transactional
    void getAllMembersByEmailIsInShouldWork() throws Exception {
        // Initialize the database
        insertedMember = memberRepository.saveAndFlush(member);

        // Get all the memberList where email in
        defaultMemberFiltering("email.in=" + DEFAULT_EMAIL + "," + UPDATED_EMAIL, "email.in=" + UPDATED_EMAIL);
    }

    @Test
    @Transactional
    void getAllMembersByEmailIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedMember = memberRepository.saveAndFlush(member);

        // Get all the memberList where email is not null
        defaultMemberFiltering("email.specified=true", "email.specified=false");
    }

    @Test
    @Transactional
    void getAllMembersByEmailContainsSomething() throws Exception {
        // Initialize the database
        insertedMember = memberRepository.saveAndFlush(member);

        // Get all the memberList where email contains
        defaultMemberFiltering("email.contains=" + DEFAULT_EMAIL, "email.contains=" + UPDATED_EMAIL);
    }

    @Test
    @Transactional
    void getAllMembersByEmailNotContainsSomething() throws Exception {
        // Initialize the database
        insertedMember = memberRepository.saveAndFlush(member);

        // Get all the memberList where email does not contain
        defaultMemberFiltering("email.doesNotContain=" + UPDATED_EMAIL, "email.doesNotContain=" + DEFAULT_EMAIL);
    }

    @Test
    @Transactional
    void getAllMembersByPhoneIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedMember = memberRepository.saveAndFlush(member);

        // Get all the memberList where phone equals to
        defaultMemberFiltering("phone.equals=" + DEFAULT_PHONE, "phone.equals=" + UPDATED_PHONE);
    }

    @Test
    @Transactional
    void getAllMembersByPhoneIsInShouldWork() throws Exception {
        // Initialize the database
        insertedMember = memberRepository.saveAndFlush(member);

        // Get all the memberList where phone in
        defaultMemberFiltering("phone.in=" + DEFAULT_PHONE + "," + UPDATED_PHONE, "phone.in=" + UPDATED_PHONE);
    }

    @Test
    @Transactional
    void getAllMembersByPhoneIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedMember = memberRepository.saveAndFlush(member);

        // Get all the memberList where phone is not null
        defaultMemberFiltering("phone.specified=true", "phone.specified=false");
    }

    @Test
    @Transactional
    void getAllMembersByPhoneContainsSomething() throws Exception {
        // Initialize the database
        insertedMember = memberRepository.saveAndFlush(member);

        // Get all the memberList where phone contains
        defaultMemberFiltering("phone.contains=" + DEFAULT_PHONE, "phone.contains=" + UPDATED_PHONE);
    }

    @Test
    @Transactional
    void getAllMembersByPhoneNotContainsSomething() throws Exception {
        // Initialize the database
        insertedMember = memberRepository.saveAndFlush(member);

        // Get all the memberList where phone does not contain
        defaultMemberFiltering("phone.doesNotContain=" + UPDATED_PHONE, "phone.doesNotContain=" + DEFAULT_PHONE);
    }

    @Test
    @Transactional
    void getAllMembersByMembershipDateIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedMember = memberRepository.saveAndFlush(member);

        // Get all the memberList where membershipDate equals to
        defaultMemberFiltering("membershipDate.equals=" + DEFAULT_MEMBERSHIP_DATE, "membershipDate.equals=" + UPDATED_MEMBERSHIP_DATE);
    }

    @Test
    @Transactional
    void getAllMembersByMembershipDateIsInShouldWork() throws Exception {
        // Initialize the database
        insertedMember = memberRepository.saveAndFlush(member);

        // Get all the memberList where membershipDate in
        defaultMemberFiltering(
            "membershipDate.in=" + DEFAULT_MEMBERSHIP_DATE + "," + UPDATED_MEMBERSHIP_DATE,
            "membershipDate.in=" + UPDATED_MEMBERSHIP_DATE
        );
    }

    @Test
    @Transactional
    void getAllMembersByMembershipDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedMember = memberRepository.saveAndFlush(member);

        // Get all the memberList where membershipDate is not null
        defaultMemberFiltering("membershipDate.specified=true", "membershipDate.specified=false");
    }

    @Test
    @Transactional
    void getAllMembersByMembershipDateIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedMember = memberRepository.saveAndFlush(member);

        // Get all the memberList where membershipDate is greater than or equal to
        defaultMemberFiltering(
            "membershipDate.greaterThanOrEqual=" + DEFAULT_MEMBERSHIP_DATE,
            "membershipDate.greaterThanOrEqual=" + UPDATED_MEMBERSHIP_DATE
        );
    }

    @Test
    @Transactional
    void getAllMembersByMembershipDateIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedMember = memberRepository.saveAndFlush(member);

        // Get all the memberList where membershipDate is less than or equal to
        defaultMemberFiltering(
            "membershipDate.lessThanOrEqual=" + DEFAULT_MEMBERSHIP_DATE,
            "membershipDate.lessThanOrEqual=" + SMALLER_MEMBERSHIP_DATE
        );
    }

    @Test
    @Transactional
    void getAllMembersByMembershipDateIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedMember = memberRepository.saveAndFlush(member);

        // Get all the memberList where membershipDate is less than
        defaultMemberFiltering("membershipDate.lessThan=" + UPDATED_MEMBERSHIP_DATE, "membershipDate.lessThan=" + DEFAULT_MEMBERSHIP_DATE);
    }

    @Test
    @Transactional
    void getAllMembersByMembershipDateIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedMember = memberRepository.saveAndFlush(member);

        // Get all the memberList where membershipDate is greater than
        defaultMemberFiltering(
            "membershipDate.greaterThan=" + SMALLER_MEMBERSHIP_DATE,
            "membershipDate.greaterThan=" + DEFAULT_MEMBERSHIP_DATE
        );
    }

    @Test
    @Transactional
    void getAllMembersByActiveIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedMember = memberRepository.saveAndFlush(member);

        // Get all the memberList where active equals to
        defaultMemberFiltering("active.equals=" + DEFAULT_ACTIVE, "active.equals=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    void getAllMembersByActiveIsInShouldWork() throws Exception {
        // Initialize the database
        insertedMember = memberRepository.saveAndFlush(member);

        // Get all the memberList where active in
        defaultMemberFiltering("active.in=" + DEFAULT_ACTIVE + "," + UPDATED_ACTIVE, "active.in=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    void getAllMembersByActiveIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedMember = memberRepository.saveAndFlush(member);

        // Get all the memberList where active is not null
        defaultMemberFiltering("active.specified=true", "active.specified=false");
    }

    @Test
    @Transactional
    void getAllMembersByLibraryIsEqualToSomething() throws Exception {
        Library library;
        if (TestUtil.findAll(em, Library.class).isEmpty()) {
            memberRepository.saveAndFlush(member);
            library = LibraryResourceIT.createEntity();
        } else {
            library = TestUtil.findAll(em, Library.class).get(0);
        }
        library.setUser(userRepository.findAll().get(0));
        em.persist(library);
        em.flush();
        member.setLibrary(library);
        memberRepository.saveAndFlush(member);
        Long libraryId = library.getId();
        // Get all the memberList where library equals to libraryId
        defaultMemberShouldBeFound("libraryId.equals=" + libraryId);

        // Get all the memberList where library equals to (libraryId + 1)
        defaultMemberShouldNotBeFound("libraryId.equals=" + (libraryId + 1));
    }

    private void defaultMemberFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultMemberShouldBeFound(shouldBeFound);
        defaultMemberShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultMemberShouldBeFound(String filter) throws Exception {
        restMemberMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(member.getId().intValue())))
            .andExpect(jsonPath("$.[*].firstName").value(hasItem(DEFAULT_FIRST_NAME)))
            .andExpect(jsonPath("$.[*].lastName").value(hasItem(DEFAULT_LAST_NAME)))
            .andExpect(jsonPath("$.[*].email").value(hasItem(DEFAULT_EMAIL)))
            .andExpect(jsonPath("$.[*].phone").value(hasItem(DEFAULT_PHONE)))
            .andExpect(jsonPath("$.[*].membershipDate").value(hasItem(DEFAULT_MEMBERSHIP_DATE.toString())))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE)));

        // Check, that the count call also returns 1
        restMemberMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultMemberShouldNotBeFound(String filter) throws Exception {
        restMemberMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restMemberMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingMember() throws Exception {
        // Get the member
        restMemberMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingMember() throws Exception {
        // Initialize the database
        insertedMember = memberRepository.saveAndFlush(member);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the member
        Member updatedMember = memberRepository.findById(member.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedMember are not directly saved in db
        em.detach(updatedMember);
        updatedMember
            .firstName(UPDATED_FIRST_NAME)
            .lastName(UPDATED_LAST_NAME)
            .email(UPDATED_EMAIL)
            .phone(UPDATED_PHONE)
            .membershipDate(UPDATED_MEMBERSHIP_DATE)
            .active(UPDATED_ACTIVE);
        MemberDTO memberDTO = memberMapper.toDto(updatedMember);

        restMemberMockMvc
            .perform(
                put(ENTITY_API_URL_ID, memberDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(memberDTO))
            )
            .andExpect(status().isOk());

        // Validate the Member in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedMemberToMatchAllProperties(updatedMember);
    }

    @Test
    @Transactional
    void putNonExistingMember() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        member.setId(longCount.incrementAndGet());

        // Create the Member
        MemberDTO memberDTO = memberMapper.toDto(member);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restMemberMockMvc
            .perform(
                put(ENTITY_API_URL_ID, memberDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(memberDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Member in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchMember() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        member.setId(longCount.incrementAndGet());

        // Create the Member
        MemberDTO memberDTO = memberMapper.toDto(member);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMemberMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(memberDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Member in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamMember() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        member.setId(longCount.incrementAndGet());

        // Create the Member
        MemberDTO memberDTO = memberMapper.toDto(member);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMemberMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(memberDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Member in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateMemberWithPatch() throws Exception {
        // Initialize the database
        insertedMember = memberRepository.saveAndFlush(member);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the member using partial update
        Member partialUpdatedMember = new Member();
        partialUpdatedMember.setId(member.getId());

        partialUpdatedMember.firstName(UPDATED_FIRST_NAME).phone(UPDATED_PHONE);

        restMemberMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedMember.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedMember))
            )
            .andExpect(status().isOk());

        // Validate the Member in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertMemberUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedMember, member), getPersistedMember(member));
    }

    @Test
    @Transactional
    void fullUpdateMemberWithPatch() throws Exception {
        // Initialize the database
        insertedMember = memberRepository.saveAndFlush(member);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the member using partial update
        Member partialUpdatedMember = new Member();
        partialUpdatedMember.setId(member.getId());

        partialUpdatedMember
            .firstName(UPDATED_FIRST_NAME)
            .lastName(UPDATED_LAST_NAME)
            .email(UPDATED_EMAIL)
            .phone(UPDATED_PHONE)
            .membershipDate(UPDATED_MEMBERSHIP_DATE)
            .active(UPDATED_ACTIVE);

        restMemberMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedMember.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedMember))
            )
            .andExpect(status().isOk());

        // Validate the Member in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertMemberUpdatableFieldsEquals(partialUpdatedMember, getPersistedMember(partialUpdatedMember));
    }

    @Test
    @Transactional
    void patchNonExistingMember() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        member.setId(longCount.incrementAndGet());

        // Create the Member
        MemberDTO memberDTO = memberMapper.toDto(member);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restMemberMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, memberDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(memberDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Member in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchMember() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        member.setId(longCount.incrementAndGet());

        // Create the Member
        MemberDTO memberDTO = memberMapper.toDto(member);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMemberMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(memberDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Member in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamMember() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        member.setId(longCount.incrementAndGet());

        // Create the Member
        MemberDTO memberDTO = memberMapper.toDto(member);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMemberMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(memberDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Member in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteMember() throws Exception {
        // Initialize the database
        insertedMember = memberRepository.saveAndFlush(member);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the member
        restMemberMockMvc
            .perform(delete(ENTITY_API_URL_ID, member.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return memberRepository.count();
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

    protected Member getPersistedMember(Member member) {
        return memberRepository.findById(member.getId()).orElseThrow();
    }

    protected void assertPersistedMemberToMatchAllProperties(Member expectedMember) {
        assertMemberAllPropertiesEquals(expectedMember, getPersistedMember(expectedMember));
    }

    protected void assertPersistedMemberToMatchUpdatableProperties(Member expectedMember) {
        assertMemberAllUpdatablePropertiesEquals(expectedMember, getPersistedMember(expectedMember));
    }
}
