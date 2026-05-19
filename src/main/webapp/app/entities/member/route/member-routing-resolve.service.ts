import { HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, Router } from '@angular/router';

import { EMPTY, Observable, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { IMember } from '../member.model';
import { MemberService } from '../service/member.service';

const memberResolve = (route: ActivatedRouteSnapshot): Observable<null | IMember> => {
  const id = route.params.id;
  if (id) {
    const router = inject(Router);
    const service = inject(MemberService);
    return service.find(id).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 404) {
          router.navigate(['404']);
        } else {
          router.navigate(['error']);
        }
        return EMPTY;
      }),
    );
  }

  return of(null);
};

export default memberResolve;
