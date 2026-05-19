import { HttpResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { NgbInputDatepicker } from '@ng-bootstrap/ng-bootstrap/datepicker';
import { TranslateModule } from '@ngx-translate/core';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import { ILibrary } from 'app/entities/library/library.model';
import { LibraryService } from 'app/entities/library/service/library.service';
import { AlertError } from 'app/shared/alert/alert-error';
import { TranslateDirective } from 'app/shared/language';
import { IMember } from '../member.model';
import { MemberService } from '../service/member.service';

import { MemberFormGroup, MemberFormService } from './member-form.service';

@Component({
  selector: 'jhi-member-update',
  templateUrl: './member-update.html',
  imports: [TranslateDirective, TranslateModule, FontAwesomeModule, AlertError, ReactiveFormsModule, NgbInputDatepicker],
})
export class MemberUpdate implements OnInit {
  readonly isSaving = signal(false);
  member: IMember | null = null;

  librariesSharedCollection = signal<ILibrary[]>([]);

  protected memberService = inject(MemberService);
  protected memberFormService = inject(MemberFormService);
  protected libraryService = inject(LibraryService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: MemberFormGroup = this.memberFormService.createMemberFormGroup();

  compareLibrary = (o1: ILibrary | null, o2: ILibrary | null): boolean => this.libraryService.compareLibrary(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ member }) => {
      this.member = member;
      if (member) {
        this.updateForm(member);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    globalThis.history.back();
  }

  save(): void {
    this.isSaving.set(true);
    const member = this.memberFormService.getMember(this.editForm);
    if (member.id === null) {
      this.subscribeToSaveResponse(this.memberService.create(member));
    } else {
      this.subscribeToSaveResponse(this.memberService.update(member));
    }
  }

  protected subscribeToSaveResponse(result: Observable<IMember | null>): void {
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

  protected updateForm(member: IMember): void {
    this.member = member;
    this.memberFormService.resetForm(this.editForm, member);

    this.librariesSharedCollection.update(libraries =>
      this.libraryService.addLibraryToCollectionIfMissing<ILibrary>(libraries, member.library),
    );
  }

  protected loadRelationshipsOptions(): void {
    this.libraryService
      .query()
      .pipe(map((res: HttpResponse<ILibrary[]>) => res.body ?? []))
      .pipe(map((libraries: ILibrary[]) => this.libraryService.addLibraryToCollectionIfMissing<ILibrary>(libraries, this.member?.library)))
      .subscribe((libraries: ILibrary[]) => this.librariesSharedCollection.set(libraries));
  }
}
