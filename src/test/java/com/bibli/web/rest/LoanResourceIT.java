package com.bibli.web.rest;

import static com.bibli.domain.LoanAsserts.*;
import static com.bibli.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.bibli.IntegrationTest;
import com.bibli.domain.Book;
import com.bibli.domain.Library;
import com.bibli.domain.Loan;
import com.bibli.domain.Member;
import com.bibli.domain.enumeration.LoanStatus;
import com.bibli.repository.LoanRepository;
import com.bibli.service.LoanService;
import com.bibli.service.dto.LoanDTO;
import com.bibli.service.mapper.LoanMapper;
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
 * Integration tests for the {@link LoanResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class LoanResourceIT {

    private static final LocalDate DEFAULT_BORROW_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_BORROW_DATE = LocalDate.now(ZoneId.systemDefault());
    private static final LocalDate SMALLER_BORROW_DATE = LocalDate.ofEpochDay(-1L);

    private static final LocalDate DEFAULT_DUE_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_DUE_DATE = LocalDate.now(ZoneId.systemDefault());
    private static final LocalDate SMALLER_DUE_DATE = LocalDate.ofEpochDay(-1L);

    private static final LocalDate DEFAULT_RETURN_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_RETURN_DATE = LocalDate.now(ZoneId.systemDefault());
    private static final LocalDate SMALLER_RETURN_DATE = LocalDate.ofEpochDay(-1L);

    private static final LoanStatus DEFAULT_STATUS = LoanStatus.BORROWED;
    private static final LoanStatus UPDATED_STATUS = LoanStatus.RETURNED;

    private static final String ENTITY_API_URL = "/api/loans";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private LoanRepository loanRepository;

    @Mock
    private LoanRepository loanRepositoryMock;

    @Autowired
    private LoanMapper loanMapper;

    @Mock
    private LoanService loanServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restLoanMockMvc;

    private Loan loan;

    private Loan insertedLoan;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Loan createEntity() {
        return new Loan().borrowDate(DEFAULT_BORROW_DATE).dueDate(DEFAULT_DUE_DATE).returnDate(DEFAULT_RETURN_DATE).status(DEFAULT_STATUS);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Loan createUpdatedEntity() {
        return new Loan().borrowDate(UPDATED_BORROW_DATE).dueDate(UPDATED_DUE_DATE).returnDate(UPDATED_RETURN_DATE).status(UPDATED_STATUS);
    }

    @BeforeEach
    void initTest() {
        loan = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedLoan != null) {
            loanRepository.delete(insertedLoan);
            insertedLoan = null;
        }
    }

    @Test
    @Transactional
    void createLoan() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Loan
        LoanDTO loanDTO = loanMapper.toDto(loan);
        var returnedLoanDTO = om.readValue(
            restLoanMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(loanDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            LoanDTO.class
        );

        // Validate the Loan in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedLoan = loanMapper.toEntity(returnedLoanDTO);
        assertLoanUpdatableFieldsEquals(returnedLoan, getPersistedLoan(returnedLoan));

        insertedLoan = returnedLoan;
    }

    @Test
    @Transactional
    void createLoanWithExistingId() throws Exception {
        // Create the Loan with an existing ID
        loan.setId(1L);
        LoanDTO loanDTO = loanMapper.toDto(loan);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restLoanMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(loanDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Loan in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkBorrowDateIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        loan.setBorrowDate(null);

        // Create the Loan, which fails.
        LoanDTO loanDTO = loanMapper.toDto(loan);

        restLoanMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(loanDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkDueDateIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        loan.setDueDate(null);

        // Create the Loan, which fails.
        LoanDTO loanDTO = loanMapper.toDto(loan);

        restLoanMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(loanDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStatusIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        loan.setStatus(null);

        // Create the Loan, which fails.
        LoanDTO loanDTO = loanMapper.toDto(loan);

        restLoanMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(loanDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllLoans() throws Exception {
        // Initialize the database
        insertedLoan = loanRepository.saveAndFlush(loan);

        // Get all the loanList
        restLoanMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(loan.getId().intValue())))
            .andExpect(jsonPath("$.[*].borrowDate").value(hasItem(DEFAULT_BORROW_DATE.toString())))
            .andExpect(jsonPath("$.[*].dueDate").value(hasItem(DEFAULT_DUE_DATE.toString())))
            .andExpect(jsonPath("$.[*].returnDate").value(hasItem(DEFAULT_RETURN_DATE.toString())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllLoansWithEagerRelationshipsIsEnabled() throws Exception {
        when(loanServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restLoanMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(loanServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllLoansWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(loanServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restLoanMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(loanRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getLoan() throws Exception {
        // Initialize the database
        insertedLoan = loanRepository.saveAndFlush(loan);

        // Get the loan
        restLoanMockMvc
            .perform(get(ENTITY_API_URL_ID, loan.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(loan.getId().intValue()))
            .andExpect(jsonPath("$.borrowDate").value(DEFAULT_BORROW_DATE.toString()))
            .andExpect(jsonPath("$.dueDate").value(DEFAULT_DUE_DATE.toString()))
            .andExpect(jsonPath("$.returnDate").value(DEFAULT_RETURN_DATE.toString()))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()));
    }

    @Test
    @Transactional
    void getLoansByIdFiltering() throws Exception {
        // Initialize the database
        insertedLoan = loanRepository.saveAndFlush(loan);

        Long id = loan.getId();

        defaultLoanFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultLoanFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultLoanFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllLoansByBorrowDateIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedLoan = loanRepository.saveAndFlush(loan);

        // Get all the loanList where borrowDate equals to
        defaultLoanFiltering("borrowDate.equals=" + DEFAULT_BORROW_DATE, "borrowDate.equals=" + UPDATED_BORROW_DATE);
    }

    @Test
    @Transactional
    void getAllLoansByBorrowDateIsInShouldWork() throws Exception {
        // Initialize the database
        insertedLoan = loanRepository.saveAndFlush(loan);

        // Get all the loanList where borrowDate in
        defaultLoanFiltering("borrowDate.in=" + DEFAULT_BORROW_DATE + "," + UPDATED_BORROW_DATE, "borrowDate.in=" + UPDATED_BORROW_DATE);
    }

    @Test
    @Transactional
    void getAllLoansByBorrowDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedLoan = loanRepository.saveAndFlush(loan);

        // Get all the loanList where borrowDate is not null
        defaultLoanFiltering("borrowDate.specified=true", "borrowDate.specified=false");
    }

    @Test
    @Transactional
    void getAllLoansByBorrowDateIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedLoan = loanRepository.saveAndFlush(loan);

        // Get all the loanList where borrowDate is greater than or equal to
        defaultLoanFiltering(
            "borrowDate.greaterThanOrEqual=" + DEFAULT_BORROW_DATE,
            "borrowDate.greaterThanOrEqual=" + UPDATED_BORROW_DATE
        );
    }

    @Test
    @Transactional
    void getAllLoansByBorrowDateIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedLoan = loanRepository.saveAndFlush(loan);

        // Get all the loanList where borrowDate is less than or equal to
        defaultLoanFiltering("borrowDate.lessThanOrEqual=" + DEFAULT_BORROW_DATE, "borrowDate.lessThanOrEqual=" + SMALLER_BORROW_DATE);
    }

    @Test
    @Transactional
    void getAllLoansByBorrowDateIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedLoan = loanRepository.saveAndFlush(loan);

        // Get all the loanList where borrowDate is less than
        defaultLoanFiltering("borrowDate.lessThan=" + UPDATED_BORROW_DATE, "borrowDate.lessThan=" + DEFAULT_BORROW_DATE);
    }

    @Test
    @Transactional
    void getAllLoansByBorrowDateIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedLoan = loanRepository.saveAndFlush(loan);

        // Get all the loanList where borrowDate is greater than
        defaultLoanFiltering("borrowDate.greaterThan=" + SMALLER_BORROW_DATE, "borrowDate.greaterThan=" + DEFAULT_BORROW_DATE);
    }

    @Test
    @Transactional
    void getAllLoansByDueDateIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedLoan = loanRepository.saveAndFlush(loan);

        // Get all the loanList where dueDate equals to
        defaultLoanFiltering("dueDate.equals=" + DEFAULT_DUE_DATE, "dueDate.equals=" + UPDATED_DUE_DATE);
    }

    @Test
    @Transactional
    void getAllLoansByDueDateIsInShouldWork() throws Exception {
        // Initialize the database
        insertedLoan = loanRepository.saveAndFlush(loan);

        // Get all the loanList where dueDate in
        defaultLoanFiltering("dueDate.in=" + DEFAULT_DUE_DATE + "," + UPDATED_DUE_DATE, "dueDate.in=" + UPDATED_DUE_DATE);
    }

    @Test
    @Transactional
    void getAllLoansByDueDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedLoan = loanRepository.saveAndFlush(loan);

        // Get all the loanList where dueDate is not null
        defaultLoanFiltering("dueDate.specified=true", "dueDate.specified=false");
    }

    @Test
    @Transactional
    void getAllLoansByDueDateIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedLoan = loanRepository.saveAndFlush(loan);

        // Get all the loanList where dueDate is greater than or equal to
        defaultLoanFiltering("dueDate.greaterThanOrEqual=" + DEFAULT_DUE_DATE, "dueDate.greaterThanOrEqual=" + UPDATED_DUE_DATE);
    }

    @Test
    @Transactional
    void getAllLoansByDueDateIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedLoan = loanRepository.saveAndFlush(loan);

        // Get all the loanList where dueDate is less than or equal to
        defaultLoanFiltering("dueDate.lessThanOrEqual=" + DEFAULT_DUE_DATE, "dueDate.lessThanOrEqual=" + SMALLER_DUE_DATE);
    }

    @Test
    @Transactional
    void getAllLoansByDueDateIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedLoan = loanRepository.saveAndFlush(loan);

        // Get all the loanList where dueDate is less than
        defaultLoanFiltering("dueDate.lessThan=" + UPDATED_DUE_DATE, "dueDate.lessThan=" + DEFAULT_DUE_DATE);
    }

    @Test
    @Transactional
    void getAllLoansByDueDateIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedLoan = loanRepository.saveAndFlush(loan);

        // Get all the loanList where dueDate is greater than
        defaultLoanFiltering("dueDate.greaterThan=" + SMALLER_DUE_DATE, "dueDate.greaterThan=" + DEFAULT_DUE_DATE);
    }

    @Test
    @Transactional
    void getAllLoansByReturnDateIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedLoan = loanRepository.saveAndFlush(loan);

        // Get all the loanList where returnDate equals to
        defaultLoanFiltering("returnDate.equals=" + DEFAULT_RETURN_DATE, "returnDate.equals=" + UPDATED_RETURN_DATE);
    }

    @Test
    @Transactional
    void getAllLoansByReturnDateIsInShouldWork() throws Exception {
        // Initialize the database
        insertedLoan = loanRepository.saveAndFlush(loan);

        // Get all the loanList where returnDate in
        defaultLoanFiltering("returnDate.in=" + DEFAULT_RETURN_DATE + "," + UPDATED_RETURN_DATE, "returnDate.in=" + UPDATED_RETURN_DATE);
    }

    @Test
    @Transactional
    void getAllLoansByReturnDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedLoan = loanRepository.saveAndFlush(loan);

        // Get all the loanList where returnDate is not null
        defaultLoanFiltering("returnDate.specified=true", "returnDate.specified=false");
    }

    @Test
    @Transactional
    void getAllLoansByReturnDateIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedLoan = loanRepository.saveAndFlush(loan);

        // Get all the loanList where returnDate is greater than or equal to
        defaultLoanFiltering(
            "returnDate.greaterThanOrEqual=" + DEFAULT_RETURN_DATE,
            "returnDate.greaterThanOrEqual=" + UPDATED_RETURN_DATE
        );
    }

    @Test
    @Transactional
    void getAllLoansByReturnDateIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedLoan = loanRepository.saveAndFlush(loan);

        // Get all the loanList where returnDate is less than or equal to
        defaultLoanFiltering("returnDate.lessThanOrEqual=" + DEFAULT_RETURN_DATE, "returnDate.lessThanOrEqual=" + SMALLER_RETURN_DATE);
    }

    @Test
    @Transactional
    void getAllLoansByReturnDateIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedLoan = loanRepository.saveAndFlush(loan);

        // Get all the loanList where returnDate is less than
        defaultLoanFiltering("returnDate.lessThan=" + UPDATED_RETURN_DATE, "returnDate.lessThan=" + DEFAULT_RETURN_DATE);
    }

    @Test
    @Transactional
    void getAllLoansByReturnDateIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedLoan = loanRepository.saveAndFlush(loan);

        // Get all the loanList where returnDate is greater than
        defaultLoanFiltering("returnDate.greaterThan=" + SMALLER_RETURN_DATE, "returnDate.greaterThan=" + DEFAULT_RETURN_DATE);
    }

    @Test
    @Transactional
    void getAllLoansByStatusIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedLoan = loanRepository.saveAndFlush(loan);

        // Get all the loanList where status equals to
        defaultLoanFiltering("status.equals=" + DEFAULT_STATUS, "status.equals=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    void getAllLoansByStatusIsInShouldWork() throws Exception {
        // Initialize the database
        insertedLoan = loanRepository.saveAndFlush(loan);

        // Get all the loanList where status in
        defaultLoanFiltering("status.in=" + DEFAULT_STATUS + "," + UPDATED_STATUS, "status.in=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    void getAllLoansByStatusIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedLoan = loanRepository.saveAndFlush(loan);

        // Get all the loanList where status is not null
        defaultLoanFiltering("status.specified=true", "status.specified=false");
    }

    @Test
    @Transactional
    void getAllLoansByLibraryIsEqualToSomething() throws Exception {
        Library library;
        if (TestUtil.findAll(em, Library.class).isEmpty()) {
            loanRepository.saveAndFlush(loan);
            library = LibraryResourceIT.createEntity();
        } else {
            library = TestUtil.findAll(em, Library.class).get(0);
        }
        em.persist(library);
        em.flush();
        loan.setLibrary(library);
        loanRepository.saveAndFlush(loan);
        Long libraryId = library.getId();
        // Get all the loanList where library equals to libraryId
        defaultLoanShouldBeFound("libraryId.equals=" + libraryId);

        // Get all the loanList where library equals to (libraryId + 1)
        defaultLoanShouldNotBeFound("libraryId.equals=" + (libraryId + 1));
    }

    @Test
    @Transactional
    void getAllLoansByBookIsEqualToSomething() throws Exception {
        Book book;
        if (TestUtil.findAll(em, Book.class).isEmpty()) {
            loanRepository.saveAndFlush(loan);
            book = BookResourceIT.createEntity();
        } else {
            book = TestUtil.findAll(em, Book.class).get(0);
        }
        em.persist(book);
        em.flush();
        loan.setBook(book);
        loanRepository.saveAndFlush(loan);
        Long bookId = book.getId();
        // Get all the loanList where book equals to bookId
        defaultLoanShouldBeFound("bookId.equals=" + bookId);

        // Get all the loanList where book equals to (bookId + 1)
        defaultLoanShouldNotBeFound("bookId.equals=" + (bookId + 1));
    }

    @Test
    @Transactional
    void getAllLoansByMemberIsEqualToSomething() throws Exception {
        Member member;
        if (TestUtil.findAll(em, Member.class).isEmpty()) {
            loanRepository.saveAndFlush(loan);
            member = MemberResourceIT.createEntity();
        } else {
            member = TestUtil.findAll(em, Member.class).get(0);
        }
        em.persist(member);
        em.flush();
        loan.setMember(member);
        loanRepository.saveAndFlush(loan);
        Long memberId = member.getId();
        // Get all the loanList where member equals to memberId
        defaultLoanShouldBeFound("memberId.equals=" + memberId);

        // Get all the loanList where member equals to (memberId + 1)
        defaultLoanShouldNotBeFound("memberId.equals=" + (memberId + 1));
    }

    private void defaultLoanFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultLoanShouldBeFound(shouldBeFound);
        defaultLoanShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultLoanShouldBeFound(String filter) throws Exception {
        restLoanMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(loan.getId().intValue())))
            .andExpect(jsonPath("$.[*].borrowDate").value(hasItem(DEFAULT_BORROW_DATE.toString())))
            .andExpect(jsonPath("$.[*].dueDate").value(hasItem(DEFAULT_DUE_DATE.toString())))
            .andExpect(jsonPath("$.[*].returnDate").value(hasItem(DEFAULT_RETURN_DATE.toString())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())));

        // Check, that the count call also returns 1
        restLoanMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultLoanShouldNotBeFound(String filter) throws Exception {
        restLoanMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restLoanMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingLoan() throws Exception {
        // Get the loan
        restLoanMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingLoan() throws Exception {
        // Initialize the database
        insertedLoan = loanRepository.saveAndFlush(loan);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the loan
        Loan updatedLoan = loanRepository.findById(loan.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedLoan are not directly saved in db
        em.detach(updatedLoan);
        updatedLoan.borrowDate(UPDATED_BORROW_DATE).dueDate(UPDATED_DUE_DATE).returnDate(UPDATED_RETURN_DATE).status(UPDATED_STATUS);
        LoanDTO loanDTO = loanMapper.toDto(updatedLoan);

        restLoanMockMvc
            .perform(put(ENTITY_API_URL_ID, loanDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(loanDTO)))
            .andExpect(status().isOk());

        // Validate the Loan in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedLoanToMatchAllProperties(updatedLoan);
    }

    @Test
    @Transactional
    void putNonExistingLoan() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        loan.setId(longCount.incrementAndGet());

        // Create the Loan
        LoanDTO loanDTO = loanMapper.toDto(loan);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restLoanMockMvc
            .perform(put(ENTITY_API_URL_ID, loanDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(loanDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Loan in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchLoan() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        loan.setId(longCount.incrementAndGet());

        // Create the Loan
        LoanDTO loanDTO = loanMapper.toDto(loan);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restLoanMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(loanDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Loan in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamLoan() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        loan.setId(longCount.incrementAndGet());

        // Create the Loan
        LoanDTO loanDTO = loanMapper.toDto(loan);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restLoanMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(loanDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Loan in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateLoanWithPatch() throws Exception {
        // Initialize the database
        insertedLoan = loanRepository.saveAndFlush(loan);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the loan using partial update
        Loan partialUpdatedLoan = new Loan();
        partialUpdatedLoan.setId(loan.getId());

        partialUpdatedLoan.dueDate(UPDATED_DUE_DATE).returnDate(UPDATED_RETURN_DATE).status(UPDATED_STATUS);

        restLoanMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedLoan.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedLoan))
            )
            .andExpect(status().isOk());

        // Validate the Loan in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertLoanUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedLoan, loan), getPersistedLoan(loan));
    }

    @Test
    @Transactional
    void fullUpdateLoanWithPatch() throws Exception {
        // Initialize the database
        insertedLoan = loanRepository.saveAndFlush(loan);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the loan using partial update
        Loan partialUpdatedLoan = new Loan();
        partialUpdatedLoan.setId(loan.getId());

        partialUpdatedLoan.borrowDate(UPDATED_BORROW_DATE).dueDate(UPDATED_DUE_DATE).returnDate(UPDATED_RETURN_DATE).status(UPDATED_STATUS);

        restLoanMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedLoan.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedLoan))
            )
            .andExpect(status().isOk());

        // Validate the Loan in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertLoanUpdatableFieldsEquals(partialUpdatedLoan, getPersistedLoan(partialUpdatedLoan));
    }

    @Test
    @Transactional
    void patchNonExistingLoan() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        loan.setId(longCount.incrementAndGet());

        // Create the Loan
        LoanDTO loanDTO = loanMapper.toDto(loan);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restLoanMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, loanDTO.getId()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(loanDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Loan in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchLoan() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        loan.setId(longCount.incrementAndGet());

        // Create the Loan
        LoanDTO loanDTO = loanMapper.toDto(loan);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restLoanMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(loanDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Loan in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamLoan() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        loan.setId(longCount.incrementAndGet());

        // Create the Loan
        LoanDTO loanDTO = loanMapper.toDto(loan);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restLoanMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(loanDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Loan in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteLoan() throws Exception {
        // Initialize the database
        insertedLoan = loanRepository.saveAndFlush(loan);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the loan
        restLoanMockMvc
            .perform(delete(ENTITY_API_URL_ID, loan.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return loanRepository.count();
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

    protected Loan getPersistedLoan(Loan loan) {
        return loanRepository.findById(loan.getId()).orElseThrow();
    }

    protected void assertPersistedLoanToMatchAllProperties(Loan expectedLoan) {
        assertLoanAllPropertiesEquals(expectedLoan, getPersistedLoan(expectedLoan));
    }

    protected void assertPersistedLoanToMatchUpdatableProperties(Loan expectedLoan) {
        assertLoanAllUpdatablePropertiesEquals(expectedLoan, getPersistedLoan(expectedLoan));
    }
}
