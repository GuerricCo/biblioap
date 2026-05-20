import { Component, OnInit, inject, signal } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { NgbInputDatepicker } from '@ng-bootstrap/ng-bootstrap/datepicker';
import { TranslateModule } from '@ngx-translate/core';
import { Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';

import { LibraryContextService } from 'app/core/library-context/library-context.service';
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

  protected memberService = inject(MemberService);
  protected memberFormService = inject(MemberFormService);
  protected activatedRoute = inject(ActivatedRoute);
  private readonly libraryContext = inject(LibraryContextService);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: MemberFormGroup = this.memberFormService.createMemberFormGroup();

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ member }) => {
      this.member = member;
      if (member) {
        this.updateForm(member);
      } else {
        // Nouveau membre : pré-remplir la library depuis le contexte
        const library = this.libraryContext.currentLibrary();
        if (library) {
          this.editForm.patchValue({ library });
        }
      }
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
  }
}
