import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { HttpResponse } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';

import { TranslateModule } from '@ngx-translate/core';
import { Subject, from, of } from 'rxjs';

import { IAuthor } from 'app/entities/author/author.model';
import { AuthorService } from 'app/entities/author/service/author.service';
import { ICategory } from 'app/entities/category/category.model';
import { CategoryService } from 'app/entities/category/service/category.service';
import { ILibrary } from 'app/entities/library/library.model';
import { LibraryService } from 'app/entities/library/service/library.service';
import { IBook } from '../book.model';
import { BookService } from '../service/book.service';

import { BookFormService } from './book-form.service';
import { BookUpdate } from './book-update';

describe('Book Management Update Component', () => {
  let comp: BookUpdate;
  let fixture: ComponentFixture<BookUpdate>;
  let activatedRoute: ActivatedRoute;
  let bookFormService: BookFormService;
  let bookService: BookService;
  let libraryService: LibraryService;
  let categoryService: CategoryService;
  let authorService: AuthorService;

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

    fixture = TestBed.createComponent(BookUpdate);
    activatedRoute = TestBed.inject(ActivatedRoute);
    bookFormService = TestBed.inject(BookFormService);
    bookService = TestBed.inject(BookService);
    libraryService = TestBed.inject(LibraryService);
    categoryService = TestBed.inject(CategoryService);
    authorService = TestBed.inject(AuthorService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should call Library query and add missing value', () => {
      const book: IBook = { id: 17120 };
      const library: ILibrary = { id: 28830 };
      book.library = library;

      const libraryCollection: ILibrary[] = [{ id: 28830 }];
      vitest.spyOn(libraryService, 'query').mockReturnValue(of(new HttpResponse({ body: libraryCollection })));
      const additionalLibraries = [library];
      const expectedCollection: ILibrary[] = [...additionalLibraries, ...libraryCollection];
      vitest.spyOn(libraryService, 'addLibraryToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ book });
      comp.ngOnInit();

      expect(libraryService.query).toHaveBeenCalled();
      expect(libraryService.addLibraryToCollectionIfMissing).toHaveBeenCalledWith(
        libraryCollection,
        ...additionalLibraries.map(i => expect.objectContaining(i) as typeof i),
      );
      expect(comp.librariesSharedCollection()).toEqual(expectedCollection);
    });

    it('should call Category query and add missing value', () => {
      const book: IBook = { id: 17120 };
      const category: ICategory = { id: 6752 };
      book.category = category;

      const categoryCollection: ICategory[] = [{ id: 6752 }];
      vitest.spyOn(categoryService, 'query').mockReturnValue(of(new HttpResponse({ body: categoryCollection })));
      const additionalCategories = [category];
      const expectedCollection: ICategory[] = [...additionalCategories, ...categoryCollection];
      vitest.spyOn(categoryService, 'addCategoryToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ book });
      comp.ngOnInit();

      expect(categoryService.query).toHaveBeenCalled();
      expect(categoryService.addCategoryToCollectionIfMissing).toHaveBeenCalledWith(
        categoryCollection,
        ...additionalCategories.map(i => expect.objectContaining(i) as typeof i),
      );
      expect(comp.categoriesSharedCollection()).toEqual(expectedCollection);
    });

    it('should call Author query and add missing value', () => {
      const book: IBook = { id: 17120 };
      const authorses: IAuthor[] = [{ id: 32542 }];
      book.authorses = authorses;

      const authorCollection: IAuthor[] = [{ id: 32542 }];
      vitest.spyOn(authorService, 'query').mockReturnValue(of(new HttpResponse({ body: authorCollection })));
      const additionalAuthors = [...authorses];
      const expectedCollection: IAuthor[] = [...additionalAuthors, ...authorCollection];
      vitest.spyOn(authorService, 'addAuthorToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ book });
      comp.ngOnInit();

      expect(authorService.query).toHaveBeenCalled();
      expect(authorService.addAuthorToCollectionIfMissing).toHaveBeenCalledWith(
        authorCollection,
        ...additionalAuthors.map(i => expect.objectContaining(i) as typeof i),
      );
      expect(comp.authorsSharedCollection()).toEqual(expectedCollection);
    });

    it('should update editForm', () => {
      const book: IBook = { id: 17120 };
      const library: ILibrary = { id: 28830 };
      book.library = library;
      const category: ICategory = { id: 6752 };
      book.category = category;
      const authors: IAuthor = { id: 32542 };
      book.authorses = [authors];

      activatedRoute.data = of({ book });
      comp.ngOnInit();

      expect(comp.librariesSharedCollection()).toContainEqual(library);
      expect(comp.categoriesSharedCollection()).toContainEqual(category);
      expect(comp.authorsSharedCollection()).toContainEqual(authors);
      expect(comp.book).toEqual(book);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<IBook>();
      const book = { id: 32624 };
      vitest.spyOn(bookFormService, 'getBook').mockReturnValue(book);
      vitest.spyOn(bookService, 'update').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ book });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(book);
      saveSubject.complete();

      // THEN
      expect(bookFormService.getBook).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(bookService.update).toHaveBeenCalledWith(expect.objectContaining(book));
      expect(comp.isSaving()).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<IBook>();
      const book = { id: 32624 };
      vitest.spyOn(bookFormService, 'getBook').mockReturnValue({ id: null });
      vitest.spyOn(bookService, 'create').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ book: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(book);
      saveSubject.complete();

      // THEN
      expect(bookFormService.getBook).toHaveBeenCalled();
      expect(bookService.create).toHaveBeenCalled();
      expect(comp.isSaving()).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<IBook>();
      const book = { id: 32624 };
      vitest.spyOn(bookService, 'update').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ book });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(bookService.update).toHaveBeenCalled();
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

    describe('compareCategory', () => {
      it('should forward to categoryService', () => {
        const entity = { id: 6752 };
        const entity2 = { id: 4374 };
        vitest.spyOn(categoryService, 'compareCategory');
        comp.compareCategory(entity, entity2);
        expect(categoryService.compareCategory).toHaveBeenCalledWith(entity, entity2);
      });
    });

    describe('compareAuthor', () => {
      it('should forward to authorService', () => {
        const entity = { id: 32542 };
        const entity2 = { id: 11676 };
        vitest.spyOn(authorService, 'compareAuthor');
        comp.compareAuthor(entity, entity2);
        expect(authorService.compareAuthor).toHaveBeenCalledWith(entity, entity2);
      });
    });
  });
});
