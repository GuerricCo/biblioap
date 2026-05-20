import { Component, OnInit, inject, signal } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { NgbInputDatepicker } from '@ng-bootstrap/ng-bootstrap/datepicker';
import { TranslateModule } from '@ngx-translate/core';
import { Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';

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

  protected authorService = inject(AuthorService);
  protected authorFormService = inject(AuthorFormService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: AuthorFormGroup = this.authorFormService.createAuthorFormGroup();

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ author }) => {
      this.author = author;
      if (author) {
        this.updateForm(author);
      }
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
  }
}
