import { Component, OnInit, inject, signal } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';
import { Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';

import { AlertError } from 'app/shared/alert/alert-error';
import { TranslateDirective } from 'app/shared/language';
import { ILibrary } from '../library.model';
import { LibraryService } from '../service/library.service';

import { LibraryFormGroup, LibraryFormService } from './library-form.service';

@Component({
  selector: 'jhi-library-update',
  templateUrl: './library-update.html',
  imports: [TranslateDirective, TranslateModule, FontAwesomeModule, AlertError, ReactiveFormsModule],
})
export class LibraryUpdate implements OnInit {
  readonly isSaving = signal(false);
  library: ILibrary | null = null;

  protected libraryService = inject(LibraryService);
  protected libraryFormService = inject(LibraryFormService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: LibraryFormGroup = this.libraryFormService.createLibraryFormGroup();

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ library }) => {
      this.library = library;
      if (library) {
        this.updateForm(library);
      }
    });
  }

  previousState(): void {
    globalThis.history.back();
  }

  save(): void {
    this.isSaving.set(true);
    const library = this.libraryFormService.getLibrary(this.editForm);
    if (library.id === null) {
      this.subscribeToSaveResponse(this.libraryService.create(library));
    } else {
      this.subscribeToSaveResponse(this.libraryService.update(library));
    }
  }

  protected subscribeToSaveResponse(result: Observable<ILibrary | null>): void {
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

  protected updateForm(library: ILibrary): void {
    this.library = library;
    this.libraryFormService.resetForm(this.editForm, library);
  }
}
