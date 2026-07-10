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
import { ReservationStatus } from 'app/entities/enumerations/reservation-status.model';
import { IMember } from 'app/entities/member/member.model';
import { MemberService } from 'app/entities/member/service/member.service';
import { LibraryContextService } from 'app/core/library-context/library-context.service';
import { AlertError } from 'app/shared/alert/alert-error';
import { TranslateDirective } from 'app/shared/language';

import { IReservation } from '../reservation.model';
import { ReservationService } from '../service/reservation.service';
import { ReservationFormGroup, ReservationFormService } from './reservation-form.service';

@Component({
  selector: 'jhi-reservation-update',
  templateUrl: './reservation-update.html',
  imports: [TranslateDirective, TranslateModule, FontAwesomeModule, AlertError, ReactiveFormsModule, NgbInputDatepicker],
})
export class ReservationUpdate implements OnInit {
  readonly isSaving = signal(false);
  reservation: IReservation | null = null;
  reservationStatusValues = Object.keys(ReservationStatus);

  booksSharedCollection = signal<IBook[]>([]);
  membersSharedCollection = signal<IMember[]>([]);

  protected reservationService = inject(ReservationService);
  protected reservationFormService = inject(ReservationFormService);
  protected bookService = inject(BookService);
  protected memberService = inject(MemberService);
  protected activatedRoute = inject(ActivatedRoute);
  private readonly libraryContext = inject(LibraryContextService);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: ReservationFormGroup = this.reservationFormService.createReservationFormGroup();

  compareBook = (o1: IBook | null, o2: IBook | null): boolean => this.bookService.compareBook(o1, o2);
  compareMember = (o1: IMember | null, o2: IMember | null): boolean => this.memberService.compareMember(o1, o2);

  isSelectedBookUnavailable(): boolean {
    const selectedBook = this.editForm.controls.book.value;
    if (!selectedBook) {
      return false;
    }
    const book = this.booksSharedCollection().find(b => b.id === selectedBook.id);
    return !book?.availableCopies;
  }

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ reservation }) => {
      this.reservation = reservation;
      if (reservation) {
        this.updateForm(reservation);
      } else {
        // Nouveau : on rattache automatiquement la library courante
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
    const reservation = this.reservationFormService.getReservation(this.editForm);
    // The library isn't shown in the form: fall back to the selected book/member's own library if
    // the navigation context didn't provide one, so the reservation doesn't silently end up without
    // a library and disappear from the library-scoped list.
    if (!reservation.library) {
      reservation.library =
        (reservation.book as IBook | null)?.library ??
        (reservation.member as IMember | null)?.library ??
        this.libraryContext.currentLibrary();
    }
    if (reservation.id === null) {
      this.subscribeToSaveResponse(this.reservationService.create(reservation));
    } else {
      this.subscribeToSaveResponse(this.reservationService.update(reservation));
    }
  }

  protected subscribeToSaveResponse(result: Observable<IReservation | null>): void {
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

  protected updateForm(reservation: IReservation): void {
    this.reservation = reservation;
    this.reservationFormService.resetForm(this.editForm, reservation);

    this.booksSharedCollection.update(books => this.bookService.addBookToCollectionIfMissing<IBook>(books, reservation.book));
    this.membersSharedCollection.update(members => this.memberService.addMemberToCollectionIfMissing<IMember>(members, reservation.member));
  }

  protected loadRelationshipsOptions(): void {
    const libraryId = this.libraryContext.currentLibraryId();

    // Uniquement les livres de la library courante
    const bookQuery: any = { size: 1000 };
    if (libraryId) {
      bookQuery['libraryId.equals'] = libraryId;
    }
    this.bookService
      .query(bookQuery)
      .pipe(map((res: HttpResponse<IBook[]>) => res.body ?? []))
      .pipe(map((books: IBook[]) => this.bookService.addBookToCollectionIfMissing<IBook>(books, this.reservation?.book)))
      .subscribe((books: IBook[]) => this.booksSharedCollection.set(books));

    // Uniquement les membres actifs de la library courante
    const memberQuery: any = { size: 1000, 'active.equals': true };
    if (libraryId) {
      memberQuery['libraryId.equals'] = libraryId;
    }
    this.memberService
      .query(memberQuery)
      .pipe(map((res: HttpResponse<IMember[]>) => res.body ?? []))
      .pipe(map((members: IMember[]) => this.memberService.addMemberToCollectionIfMissing<IMember>(members, this.reservation?.member)))
      .subscribe((members: IMember[]) => this.membersSharedCollection.set(members));
  }
}
