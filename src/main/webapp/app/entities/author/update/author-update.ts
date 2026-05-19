import { HttpResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { NgbInputDatepicker } from '@ng-bootstrap/ng-bootstrap/datepicker';
import { TranslateModule } from '@ngx-translate/core';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import { IBook } from 'app/entities/book/book.model';
import { BookService } from 'app/entities/book/service/book.service';
import { AlertError } from 'app/shared/alert/alert-error';
import { TranslateDirective } from 'app/shared/language';
import { IAuthor } from '../author.model';
import { AuthorService } from '../service/author.service';

import { AuthorFormGroup, AuthorFormService } from './author-form.service';

@Component({
  selector: 'jhi-author-update',
  templateUrl: './author-update.html',
  imports: [TranslateDirective, TranslateModule, FontAwesomeModule, AlertError, ReactiveFormsModule, NgbInputDatepicker],
})
export class AuthorUpdate implements OnInit {
  readonly isSaving = signal(false);
  author: IAuthor | null = null;

  booksSharedCollection = signal<IBook[]>([]);

  protected authorService = inject(AuthorService);
  protected authorFormService = inject(AuthorFormService);
  protected bookService = inject(BookService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: AuthorFormGroup = this.authorFormService.createAuthorFormGroup();

  compareBook = (o1: IBook | null, o2: IBook | null): boolean => this.bookService.compareBook(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ author }) => {
      this.author = author;
      if (author) {
        this.updateForm(author);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    globalThis.history.back();
  }

  save(): void {
    this.isSaving.set(true);
    const author = this.authorFormService.getAuthor(this.editForm);
    if (author.id === null) {
      this.subscribeToSaveResponse(this.authorService.create(author));
    } else {
      this.subscribeToSaveResponse(this.authorService.update(author));
    }
  }

  protected subscribeToSaveResponse(result: Observable<IAuthor | null>): void {
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

  protected updateForm(author: IAuthor): void {
    this.author = author;
    this.authorFormService.resetForm(this.editForm, author);

    this.booksSharedCollection.update(books => this.bookService.addBookToCollectionIfMissing<IBook>(books, ...(author.bookses ?? [])));
  }

  protected loadRelationshipsOptions(): void {
    this.bookService
      .query()
      .pipe(map((res: HttpResponse<IBook[]>) => res.body ?? []))
      .pipe(map((books: IBook[]) => this.bookService.addBookToCollectionIfMissing<IBook>(books, ...(this.author?.bookses ?? []))))
      .subscribe((books: IBook[]) => this.booksSharedCollection.set(books));
  }
}
