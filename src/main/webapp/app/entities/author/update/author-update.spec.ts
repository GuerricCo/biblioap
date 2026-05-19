import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { HttpResponse } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';

import { TranslateModule } from '@ngx-translate/core';
import { Subject, from, of } from 'rxjs';

import { IBook } from 'app/entities/book/book.model';
import { BookService } from 'app/entities/book/service/book.service';
import { IAuthor } from '../author.model';
import { AuthorService } from '../service/author.service';

import { AuthorFormService } from './author-form.service';
import { AuthorUpdate } from './author-update';

describe('Author Management Update Component', () => {
  let comp: AuthorUpdate;
  let fixture: ComponentFixture<AuthorUpdate>;
  let activatedRoute: ActivatedRoute;
  let authorFormService: AuthorFormService;
  let authorService: AuthorService;
  let bookService: BookService;

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

    fixture = TestBed.createComponent(AuthorUpdate);
    activatedRoute = TestBed.inject(ActivatedRoute);
    authorFormService = TestBed.inject(AuthorFormService);
    authorService = TestBed.inject(AuthorService);
    bookService = TestBed.inject(BookService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should call Book query and add missing value', () => {
      const author: IAuthor = { id: 11676 };
      const bookses: IBook[] = [{ id: 32624 }];
      author.bookses = bookses;

      const bookCollection: IBook[] = [{ id: 32624 }];
      vitest.spyOn(bookService, 'query').mockReturnValue(of(new HttpResponse({ body: bookCollection })));
      const additionalBooks = [...bookses];
      const expectedCollection: IBook[] = [...additionalBooks, ...bookCollection];
      vitest.spyOn(bookService, 'addBookToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ author });
      comp.ngOnInit();

      expect(bookService.query).toHaveBeenCalled();
      expect(bookService.addBookToCollectionIfMissing).toHaveBeenCalledWith(
        bookCollection,
        ...additionalBooks.map(i => expect.objectContaining(i) as typeof i),
      );
      expect(comp.booksSharedCollection()).toEqual(expectedCollection);
    });

    it('should update editForm', () => {
      const author: IAuthor = { id: 11676 };
      const books: IBook = { id: 32624 };
      author.bookses = [books];

      activatedRoute.data = of({ author });
      comp.ngOnInit();

      expect(comp.booksSharedCollection()).toContainEqual(books);
      expect(comp.author).toEqual(author);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<IAuthor>();
      const author = { id: 32542 };
      vitest.spyOn(authorFormService, 'getAuthor').mockReturnValue(author);
      vitest.spyOn(authorService, 'update').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ author });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(author);
      saveSubject.complete();

      // THEN
      expect(authorFormService.getAuthor).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(authorService.update).toHaveBeenCalledWith(expect.objectContaining(author));
      expect(comp.isSaving()).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<IAuthor>();
      const author = { id: 32542 };
      vitest.spyOn(authorFormService, 'getAuthor').mockReturnValue({ id: null });
      vitest.spyOn(authorService, 'create').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ author: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(author);
      saveSubject.complete();

      // THEN
      expect(authorFormService.getAuthor).toHaveBeenCalled();
      expect(authorService.create).toHaveBeenCalled();
      expect(comp.isSaving()).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<IAuthor>();
      const author = { id: 32542 };
      vitest.spyOn(authorService, 'update').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ author });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(authorService.update).toHaveBeenCalled();
      expect(comp.isSaving()).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Compare relationships', () => {
    describe('compareBook', () => {
      it('should forward to bookService', () => {
        const entity = { id: 32624 };
        const entity2 = { id: 17120 };
        vitest.spyOn(bookService, 'compareBook');
        comp.compareBook(entity, entity2);
        expect(bookService.compareBook).toHaveBeenCalledWith(entity, entity2);
      });
    });
  });
});
