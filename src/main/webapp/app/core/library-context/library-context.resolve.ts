import { HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, Router } from '@angular/router';

import { EMPTY, Observable, of } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';

import { ILibrary } from 'app/entities/library/library.model';
import { LibraryService } from 'app/entities/library/service/library.service';
import { LibraryContextService } from './library-context.service';

/**
 * Re-hydrates the current library context from the ":libraryId" route param whenever the
 * "app/:libraryId/..." route tree is entered directly (page refresh, deep link, new tab) rather
 * than via in-app navigation from the library list, which is the only place that otherwise sets it.
 * Without this, every list/form scoped "by library" silently falls back to showing/saving
 * unscoped data.
 */
const libraryContextResolve = (route: ActivatedRouteSnapshot): Observable<ILibrary | null> => {
  const libraryId = route.params.libraryId;
  if (!libraryId) {
    return of(null);
  }

  const libraryContext = inject(LibraryContextService);
  const current = libraryContext.currentLibrary();
  if (current && String(current.id) === String(libraryId)) {
    return of(current);
  }

  const router = inject(Router);
  const libraryService = inject(LibraryService);
  return libraryService.find(libraryId).pipe(
    tap(library => libraryContext.setLibrary(library)),
    catchError((error: HttpErrorResponse) => {
      if (error.status === 404) {
        router.navigate(['404']);
      } else {
        router.navigate(['error']);
      }
      return EMPTY;
    }),
  );
};

export default libraryContextResolve;
