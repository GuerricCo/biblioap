import { Component, OnInit, effect, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Data, ParamMap, Router, RouterLink } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap/modal';
import { TranslateModule } from '@ngx-translate/core';
import { Subscription, combineLatest, filter, tap } from 'rxjs';

import { DEFAULT_SORT_DATA, ITEM_DELETED_EVENT, SORT } from 'app/config/navigation.constants';
import { Alert } from 'app/shared/alert/alert';
import { AlertError } from 'app/shared/alert/alert-error';
import { TranslateDirective } from 'app/shared/language';
import { SortByDirective, SortDirective, SortService, type SortState, sortStateSignal } from 'app/shared/sort';
import { LibraryDeleteDialog } from '../delete/library-delete-dialog';
import { ILibrary } from '../library.model';
import { LibraryService } from '../service/library.service';
import { LibraryContextService } from 'app/core/library-context/library-context.service';

@Component({
  selector: 'jhi-library',
  templateUrl: './library.html',
  styleUrl: './library.scss',
  imports: [
    RouterLink,
    FormsModule,
    FontAwesomeModule,
    AlertError,
    Alert,
    SortDirective,
    SortByDirective,
    TranslateDirective,
    TranslateModule,
  ],
})
export class Library implements OnInit {
  subscription: Subscription | null = null;
  readonly libraries = signal<ILibrary[]>([]);

  sortState = sortStateSignal({});

  readonly router = inject(Router);
  protected readonly libraryService = inject(LibraryService);
  readonly isLoading = this.libraryService.librariesResource.isLoading;
  protected readonly activatedRoute = inject(ActivatedRoute);
  protected readonly sortService = inject(SortService);
  protected modalService = inject(NgbModal);
  private readonly libraryContext = inject(LibraryContextService);

  constructor() {
    effect(() => {
      this.libraries.set(this.fillComponentAttributesFromResponseBody([...this.libraryService.libraries()]));
    });
  }

  trackId = (item: ILibrary): number => this.libraryService.getLibraryIdentifier(item);

  ngOnInit(): void {
    this.subscription = combineLatest([this.activatedRoute.queryParamMap, this.activatedRoute.data])
      .pipe(
        tap(([params, data]) => this.fillComponentAttributeFromRoute(params, data)),
        tap(() => this.load()),
      )
      .subscribe();
  }

  delete(library: ILibrary): void {
    const modalRef = this.modalService.open(LibraryDeleteDialog, { size: 'lg', backdrop: 'static' });
    modalRef.componentInstance.library = library;
    modalRef.closed
      .pipe(
        filter(reason => reason === ITEM_DELETED_EVENT),
        tap(() => this.load()),
      )
      .subscribe();
  }

  openLibrary(library: ILibrary): void {
    this.libraryContext.setLibrary(library);
    this.router.navigate(['/app', library.id, 'books']);
  }

  load(): void {
    this.queryBackend();
  }

  navigateToWithComponentValues(event: SortState): void {
    this.handleNavigation(event);
  }

  protected fillComponentAttributeFromRoute(params: ParamMap, data: Data): void {
    this.sortState.set(this.sortService.parseSortParam(params.get(SORT) ?? data[DEFAULT_SORT_DATA]));
  }

  protected refineData(data: ILibrary[]): ILibrary[] {
    const { predicate, order } = this.sortState();
    return predicate && order ? data.sort(this.sortService.startSort({ predicate, order })) : data;
  }

  protected fillComponentAttributesFromResponseBody(data: ILibrary[]): ILibrary[] {
    return this.refineData(data);
  }

  protected queryBackend(): void {
    const queryObject: any = {
      sort: this.sortService.buildSortParam(this.sortState()),
    };
    this.libraryService.librariesParams.set(queryObject);
  }

  protected handleNavigation(sortState: SortState): void {
    const queryParamsObj = {
      sort: this.sortService.buildSortParam(sortState),
    };
    this.router.navigate(['./'], {
      relativeTo: this.activatedRoute,
      queryParams: queryParamsObj,
    });
  }
}
