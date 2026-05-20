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
import { LoanStatus } from 'app/entities/enumerations/loan-status.model';
import { IMember } from 'app/entities/member/member.model';
import { MemberService } from 'app/entities/member/service/member.service';
import { LibraryContextService } from 'app/core/library-context/library-context.service';
import { AlertError } from 'app/shared/alert/alert-error';
import { TranslateDirective } from 'app/shared/language';

import { ILoan } from '../loan.model';
import { LoanService } from '../service/loan.service';
import { LoanFormGroup, LoanFormService } from './loan-form.service';

@Component({
  selector: 'jhi-loan-update',
  templateUrl: './loan-update.html',
  imports: [TranslateDirective, TranslateModule, FontAwesomeModule, AlertError, ReactiveFormsModule, NgbInputDatepicker],
})
export class LoanUpdate implements OnInit {
  readonly isSaving = signal(false);
  loan: ILoan | null = null;
  loanStatusValues = Object.keys(LoanStatus);

  booksSharedCollection = signal<IBook[]>([]);
  membersSharedCollection = signal<IMember[]>([]);

  protected loanService = inject(LoanService);
  protected loanFormService = inject(LoanFormService);
  protected bookService = inject(BookService);
  protected memberService = inject(MemberService);
  protected activatedRoute = inject(ActivatedRoute);
  private readonly libraryContext = inject(LibraryContextService);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: LoanFormGroup = this.loanFormService.createLoanFormGroup();

  compareBook = (o1: IBook | null, o2: IBook | null): boolean => this.bookService.compareBook(o1, o2);
  compareMember = (o1: IMember | null, o2: IMember | null): boolean => this.memberService.compareMember(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ loan }) => {
      this.loan = loan;
      if (loan) {
        this.updateForm(loan);
      } else {
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
    const loan = this.loanFormService.getLoan(this.editForm);
    if (loan.id === null) {
      this.subscribeToSaveResponse(this.loanService.create(loan));
    } else {
      this.subscribeToSaveResponse(this.loanService.update(loan));
    }
  }

  protected subscribeToSaveResponse(result: Observable<ILoan | null>): void {
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

  protected updateForm(loan: ILoan): void {
    this.loan = loan;
    this.loanFormService.resetForm(this.editForm, loan);
    this.booksSharedCollection.update(books => this.bookService.addBookToCollectionIfMissing<IBook>(books, loan.book));
    this.membersSharedCollection.update(members => this.memberService.addMemberToCollectionIfMissing<IMember>(members, loan.member));
  }

  protected loadRelationshipsOptions(): void {
    const libraryId = this.libraryContext.currentLibraryId();

    this.bookService
      .query(libraryId ? { 'libraryId.equals': libraryId } : {})
      .pipe(map((res: HttpResponse<IBook[]>) => res.body ?? []))
      .pipe(map((books: IBook[]) => this.bookService.addBookToCollectionIfMissing<IBook>(books, this.loan?.book)))
      .subscribe((books: IBook[]) => this.booksSharedCollection.set(books));

    this.memberService
      .query(libraryId ? { 'libraryId.equals': libraryId } : {})
      .pipe(map((res: HttpResponse<IMember[]>) => res.body ?? []))
      .pipe(map((members: IMember[]) => this.memberService.addMemberToCollectionIfMissing<IMember>(members, this.loan?.member)))
      .subscribe((members: IMember[]) => this.membersSharedCollection.set(members));
  }
}
