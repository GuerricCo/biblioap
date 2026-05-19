import { HttpResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import { IBook } from 'app/entities/book/book.model';
import { BookService } from 'app/entities/book/service/book.service';
import { ILibrary } from 'app/entities/library/library.model';
import { LibraryService } from 'app/entities/library/service/library.service';
import { IMember } from 'app/entities/member/member.model';
import { MemberService } from 'app/entities/member/service/member.service';
import { AlertError } from 'app/shared/alert/alert-error';
import { TranslateDirective } from 'app/shared/language';

import { IReview } from '../review.model';
import { ReviewService } from '../service/review.service';

import { ReviewFormGroup, ReviewFormService } from './review-form.service';

@Component({
  selector: 'jhi-review-update',
  templateUrl: './review-update.html',
  imports: [TranslateDirective, TranslateModule, FontAwesomeModule, AlertError, ReactiveFormsModule],
})
export class ReviewUpdate implements OnInit {
  readonly isSaving = signal(false);
  review: IReview | null = null;

  librariesSharedCollection = signal<ILibrary[]>([]);
  booksSharedCollection = signal<IBook[]>([]);
  membersSharedCollection = signal<IMember[]>([]);

  protected reviewService = inject(ReviewService);
  protected reviewFormService = inject(ReviewFormService);
  protected libraryService = inject(LibraryService);
  protected bookService = inject(BookService);
  protected memberService = inject(MemberService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: ReviewFormGroup = this.reviewFormService.createReviewFormGroup();

  compareLibrary = (o1: ILibrary | null, o2: ILibrary | null): boolean => this.libraryService.compareLibrary(o1, o2);

  compareBook = (o1: IBook | null, o2: IBook | null): boolean => this.bookService.compareBook(o1, o2);

  compareMember = (o1: IMember | null, o2: IMember | null): boolean => this.memberService.compareMember(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ review }) => {
      this.review = review;
      if (review) {
        this.updateForm(review);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    globalThis.history.back();
  }

  save(): void {
    this.isSaving.set(true);
    const review = this.reviewFormService.getReview(this.editForm);
    if (review.id === null) {
      this.subscribeToSaveResponse(this.reviewService.create(review));
    } else {
      this.subscribeToSaveResponse(this.reviewService.update(review));
    }
  }

  protected subscribeToSaveResponse(result: Observable<IReview | null>): void {
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

  protected updateForm(review: IReview): void {
    this.review = review;
    this.reviewFormService.resetForm(this.editForm, review);

    this.librariesSharedCollection.update(libraries =>
      this.libraryService.addLibraryToCollectionIfMissing<ILibrary>(libraries, review.library),
    );
    this.booksSharedCollection.update(books => this.bookService.addBookToCollectionIfMissing<IBook>(books, review.book));
    this.membersSharedCollection.update(members => this.memberService.addMemberToCollectionIfMissing<IMember>(members, review.member));
  }

  protected loadRelationshipsOptions(): void {
    this.libraryService
      .query()
      .pipe(map((res: HttpResponse<ILibrary[]>) => res.body ?? []))
      .pipe(map((libraries: ILibrary[]) => this.libraryService.addLibraryToCollectionIfMissing<ILibrary>(libraries, this.review?.library)))
      .subscribe((libraries: ILibrary[]) => this.librariesSharedCollection.set(libraries));

    this.bookService
      .query()
      .pipe(map((res: HttpResponse<IBook[]>) => res.body ?? []))
      .pipe(map((books: IBook[]) => this.bookService.addBookToCollectionIfMissing<IBook>(books, this.review?.book)))
      .subscribe((books: IBook[]) => this.booksSharedCollection.set(books));

    this.memberService
      .query()
      .pipe(map((res: HttpResponse<IMember[]>) => res.body ?? []))
      .pipe(map((members: IMember[]) => this.memberService.addMemberToCollectionIfMissing<IMember>(members, this.review?.member)))
      .subscribe((members: IMember[]) => this.membersSharedCollection.set(members));
  }
}
