import { HttpResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { NgbInputDatepicker } from '@ng-bootstrap/ng-bootstrap/datepicker';
import { TranslateModule } from '@ngx-translate/core';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import { IAuthor } from 'app/entities/author/author.model';
import { AuthorService } from 'app/entities/author/service/author.service';
import { ICategory } from 'app/entities/category/category.model';
import { CategoryService } from 'app/entities/category/service/category.service';
import { LibraryContextService } from 'app/core/library-context/library-context.service';
import { AlertError } from 'app/shared/alert/alert-error';
import { TranslateDirective } from 'app/shared/language';

import { IBook } from '../book.model';
import { BookService } from '../service/book.service';
import { BookFormGroup, BookFormService } from './book-form.service';

@Component({
  selector: 'jhi-book-update',
  templateUrl: './book-update.html',
  imports: [TranslateDirective, TranslateModule, FontAwesomeModule, AlertError, ReactiveFormsModule, NgbInputDatepicker],
})
export class BookUpdate implements OnInit {
  readonly isSaving = signal(false);
  book: IBook | null = null;

  categoriesSharedCollection = signal<ICategory[]>([]);
  authorsSharedCollection = signal<IAuthor[]>([]);

  protected bookService = inject(BookService);
  protected bookFormService = inject(BookFormService);
  protected categoryService = inject(CategoryService);
  protected authorService = inject(AuthorService);
  protected activatedRoute = inject(ActivatedRoute);
  private readonly libraryContext = inject(LibraryContextService);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: BookFormGroup = this.bookFormService.createBookFormGroup();

  compareCategory = (o1: ICategory | null, o2: ICategory | null): boolean => this.categoryService.compareCategory(o1, o2);
  compareAuthor = (o1: IAuthor | null, o2: IAuthor | null): boolean => this.authorService.compareAuthor(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ book }) => {
      this.book = book;
      if (book) {
        this.updateForm(book);
      } else {
        // Nouveau livre : pré-remplir la library depuis le contexte
        const library = this.libraryContext.currentLibrary();
        if (library) {
          this.editForm.patchValue({ library });
        }
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    globalThis.history.back();
  }

  save(): void {
    this.isSaving.set(true);
    const book = this.bookFormService.getBook(this.editForm);
    if (book.id === null) {
      this.subscribeToSaveResponse(this.bookService.create(book));
    } else {
      this.subscribeToSaveResponse(this.bookService.update(book));
    }
  }

  protected subscribeToSaveResponse(result: Observable<IBook | null>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe({
      next: () => this.onSaveSuccess(),
      error: () => this.onSaveError(),
    });
  }

  protected onSaveSuccess(): void {
    this.previousState();
  }

  protected onSaveError(): void {
    // Api for inheritance.
  }

  protected onSaveFinalize(): void {
    this.isSaving.set(false);
  }

  protected updateForm(book: IBook): void {
    this.book = book;
    this.bookFormService.resetForm(this.editForm, book);

    this.categoriesSharedCollection.update(categories =>
      this.categoryService.addCategoryToCollectionIfMissing<ICategory>(categories, book.category),
    );
    this.authorsSharedCollection.update(authors =>
      this.authorService.addAuthorToCollectionIfMissing<IAuthor>(authors, ...(book.authorses ?? [])),
    );
  }

  protected loadRelationshipsOptions(): void {
    const libraryId = this.libraryContext.currentLibraryId();
    this.categoryService
      .query(libraryId ? { 'libraryId.equals': libraryId } : {})
      .pipe(map((res: HttpResponse<ICategory[]>) => res.body ?? []))
      .pipe(
        map((categories: ICategory[]) => this.categoryService.addCategoryToCollectionIfMissing<ICategory>(categories, this.book?.category)),
      )
      .subscribe((categories: ICategory[]) => this.categoriesSharedCollection.set(categories));

    this.authorService
      .query()
      .pipe(map((res: HttpResponse<IAuthor[]>) => res.body ?? []))
      .pipe(
        map((authors: IAuthor[]) => this.authorService.addAuthorToCollectionIfMissing<IAuthor>(authors, ...(this.book?.authorses ?? []))),
      )
      .subscribe((authors: IAuthor[]) => this.authorsSharedCollection.set(authors));
  }
}
