import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { HttpResponse } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';

import { TranslateModule } from '@ngx-translate/core';
import { Subject, from, of } from 'rxjs';

import { IBook } from 'app/entities/book/book.model';
import { BookService } from 'app/entities/book/service/book.service';
import { ILibrary } from 'app/entities/library/library.model';
import { LibraryService } from 'app/entities/library/service/library.service';
import { IMember } from 'app/entities/member/member.model';
import { MemberService } from 'app/entities/member/service/member.service';
import { ILoan } from '../loan.model';
import { LoanService } from '../service/loan.service';

import { LoanFormService } from './loan-form.service';
import { LoanUpdate } from './loan-update';

describe('Loan Management Update Component', () => {
  let comp: LoanUpdate;
  let fixture: ComponentFixture<LoanUpdate>;
  let activatedRoute: ActivatedRoute;
  let loanFormService: LoanFormService;
  let loanService: LoanService;
  let libraryService: LibraryService;
  let bookService: BookService;
  let memberService: MemberService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [TranslateModule.forRoot()],
      providers: [
        provideHttpClientTesting(),
        {
          provide: ActivatedRoute,
          useValue: {
            params: from([{}]),
          },
        },
      ],
    });

    fixture = TestBed.createComponent(LoanUpdate);
    activatedRoute = TestBed.inject(ActivatedRoute);
    loanFormService = TestBed.inject(LoanFormService);
    loanService = TestBed.inject(LoanService);
    libraryService = TestBed.inject(LibraryService);
    bookService = TestBed.inject(BookService);
    memberService = TestBed.inject(MemberService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should call Library query and add missing value', () => {
      const loan: ILoan = { id: 441 };
      const library: ILibrary = { id: 28830 };
      loan.library = library;

      const libraryCollection: ILibrary[] = [{ id: 28830 }];
      vitest.spyOn(libraryService, 'query').mockReturnValue(of(new HttpResponse({ body: libraryCollection })));
      const additionalLibraries = [library];
      const expectedCollection: ILibrary[] = [...additionalLibraries, ...libraryCollection];
      vitest.spyOn(libraryService, 'addLibraryToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ loan });
      comp.ngOnInit();

      expect(libraryService.query).toHaveBeenCalled();
      expect(libraryService.addLibraryToCollectionIfMissing).toHaveBeenCalledWith(
        libraryCollection,
        ...additionalLibraries.map(i => expect.objectContaining(i) as typeof i),
      );
      expect(comp.librariesSharedCollection()).toEqual(expectedCollection);
    });

    it('should call Book query and add missing value', () => {
      const loan: ILoan = { id: 441 };
      const book: IBook = { id: 32624 };
      loan.book = book;

      const bookCollection: IBook[] = [{ id: 32624 }];
      vitest.spyOn(bookService, 'query').mockReturnValue(of(new HttpResponse({ body: bookCollection })));
      const additionalBooks = [book];
      const expectedCollection: IBook[] = [...additionalBooks, ...bookCollection];
      vitest.spyOn(bookService, 'addBookToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ loan });
      comp.ngOnInit();

      expect(bookService.query).toHaveBeenCalled();
      expect(bookService.addBookToCollectionIfMissing).toHaveBeenCalledWith(
        bookCollection,
        ...additionalBooks.map(i => expect.objectContaining(i) as typeof i),
      );
      expect(comp.booksSharedCollection()).toEqual(expectedCollection);
    });

    it('should call Member query and add missing value', () => {
      const loan: ILoan = { id: 441 };
      const member: IMember = { id: 17514 };
      loan.member = member;

      const memberCollection: IMember[] = [{ id: 17514 }];
      vitest.spyOn(memberService, 'query').mockReturnValue(of(new HttpResponse({ body: memberCollection })));
      const additionalMembers = [member];
      const expectedCollection: IMember[] = [...additionalMembers, ...memberCollection];
      vitest.spyOn(memberService, 'addMemberToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ loan });
      comp.ngOnInit();

      expect(memberService.query).toHaveBeenCalled();
      expect(memberService.addMemberToCollectionIfMissing).toHaveBeenCalledWith(
        memberCollection,
        ...additionalMembers.map(i => expect.objectContaining(i) as typeof i),
      );
      expect(comp.membersSharedCollection()).toEqual(expectedCollection);
    });

    it('should update editForm', () => {
      const loan: ILoan = { id: 441 };
      const library: ILibrary = { id: 28830 };
      loan.library = library;
      const book: IBook = { id: 32624 };
      loan.book = book;
      const member: IMember = { id: 17514 };
      loan.member = member;

      activatedRoute.data = of({ loan });
      comp.ngOnInit();

      expect(comp.librariesSharedCollection()).toContainEqual(library);
      expect(comp.booksSharedCollection()).toContainEqual(book);
      expect(comp.membersSharedCollection()).toContainEqual(member);
      expect(comp.loan).toEqual(loan);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<ILoan>();
      const loan = { id: 1685 };
      vitest.spyOn(loanFormService, 'getLoan').mockReturnValue(loan);
      vitest.spyOn(loanService, 'update').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ loan });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(loan);
      saveSubject.complete();

      // THEN
      expect(loanFormService.getLoan).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(loanService.update).toHaveBeenCalledWith(expect.objectContaining(loan));
      expect(comp.isSaving()).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<ILoan>();
      const loan = { id: 1685 };
      vitest.spyOn(loanFormService, 'getLoan').mockReturnValue({ id: null });
      vitest.spyOn(loanService, 'create').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ loan: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(loan);
      saveSubject.complete();

      // THEN
      expect(loanFormService.getLoan).toHaveBeenCalled();
      expect(loanService.create).toHaveBeenCalled();
      expect(comp.isSaving()).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<ILoan>();
      const loan = { id: 1685 };
      vitest.spyOn(loanService, 'update').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ loan });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(loanService.update).toHaveBeenCalled();
      expect(comp.isSaving()).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Compare relationships', () => {
    describe('compareLibrary', () => {
      it('should forward to libraryService', () => {
        const entity = { id: 28830 };
        const entity2 = { id: 16262 };
        vitest.spyOn(libraryService, 'compareLibrary');
        comp.compareLibrary(entity, entity2);
        expect(libraryService.compareLibrary).toHaveBeenCalledWith(entity, entity2);
      });
    });

    describe('compareBook', () => {
      it('should forward to bookService', () => {
        const entity = { id: 32624 };
        const entity2 = { id: 17120 };
        vitest.spyOn(bookService, 'compareBook');
        comp.compareBook(entity, entity2);
        expect(bookService.compareBook).toHaveBeenCalledWith(entity, entity2);
      });
    });

    describe('compareMember', () => {
      it('should forward to memberService', () => {
        const entity = { id: 17514 };
        const entity2 = { id: 30790 };
        vitest.spyOn(memberService, 'compareMember');
        comp.compareMember(entity, entity2);
        expect(memberService.compareMember).toHaveBeenCalledWith(entity, entity2);
      });
    });
  });
});
