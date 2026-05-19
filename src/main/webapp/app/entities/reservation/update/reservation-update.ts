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
import { ILibrary } from 'app/entities/library/library.model';
import { LibraryService } from 'app/entities/library/service/library.service';
import { AlertError } from 'app/shared/alert/alert-error';
import { TranslateDirective } from 'app/shared/language';

import { IReservation } from '../reservation.model';
import { ReservationService } from '../service/reservation.service';

import { ReservationFormGroup, ReservationFormService } from './reservation-form.service';
import { IMember } from 'app/entities/member/member.model';
import { MemberService } from 'app/entities/member/service/member.service';

@Component({
  selector: 'jhi-reservation-update',
  templateUrl: './reservation-update.html',
  imports: [TranslateDirective, TranslateModule, FontAwesomeModule, AlertError, ReactiveFormsModule, NgbInputDatepicker],
})
export class ReservationUpdate implements OnInit {
  readonly isSaving = signal(false);
  reservation: IReservation | null = null;
  reservationStatusValues = Object.keys(ReservationStatus);

  librariesSharedCollection = signal<ILibrary[]>([]);
  booksSharedCollection = signal<IBook[]>([]);
  membersSharedCollection = signal<IMember[]>([]);

  protected reservationService = inject(ReservationService);
  protected reservationFormService = inject(ReservationFormService);
  protected libraryService = inject(LibraryService);
  protected bookService = inject(BookService);
  protected memberService = inject(MemberService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: ReservationFormGroup = this.reservationFormService.createReservationFormGroup();

  compareLibrary = (o1: ILibrary | null, o2: ILibrary | null): boolean => this.libraryService.compareLibrary(o1, o2);

  compareBook = (o1: IBook | null, o2: IBook | null): boolean => this.bookService.compareBook(o1, o2);

  compareMember = (o1: IMember | null, o2: IMember | null): boolean => this.memberService.compareMember(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ reservation }) => {
      this.reservation = reservation;
      if (reservation) {
        this.updateForm(reservation);
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

    this.librariesSharedCollection.update(libraries =>
      this.libraryService.addLibraryToCollectionIfMissing<ILibrary>(libraries, reservation.library),
    );
    this.booksSharedCollection.update(books => this.bookService.addBookToCollectionIfMissing<IBook>(books, reservation.book));
    this.membersSharedCollection.update(members => this.memberService.addMemberToCollectionIfMissing<IMember>(members, reservation.member));
  }

  protected loadRelationshipsOptions(): void {
    this.libraryService
      .query()
      .pipe(map((res: HttpResponse<ILibrary[]>) => res.body ?? []))
      .pipe(
        map((libraries: ILibrary[]) => this.libraryService.addLibraryToCollectionIfMissing<ILibrary>(libraries, this.reservation?.library)),
      )
      .subscribe((libraries: ILibrary[]) => this.librariesSharedCollection.set(libraries));

    this.bookService
      .query()
      .pipe(map((res: HttpResponse<IBook[]>) => res.body ?? []))
      .pipe(map((books: IBook[]) => this.bookService.addBookToCollectionIfMissing<IBook>(books, this.reservation?.book)))
      .subscribe((books: IBook[]) => this.booksSharedCollection.set(books));

    this.memberService
      .query()
      .pipe(map((res: HttpResponse<IMember[]>) => res.body ?? []))
      .pipe(map((members: IMember[]) => this.memberService.addMemberToCollectionIfMissing<IMember>(members, this.reservation?.member)))
      .subscribe((members: IMember[]) => this.membersSharedCollection.set(members));
  }
}
