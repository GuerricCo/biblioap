import { HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, Router } from '@angular/router';

import { EMPTY, Observable, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { IReservation } from '../reservation.model';
import { ReservationService } from '../service/reservation.service';

const reservationResolve = (route: ActivatedRouteSnapshot): Observable<null | IReservation> => {
  const id = route.params.id;
  if (id) {
    const router = inject(Router);
    const service = inject(ReservationService);
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

export default reservationResolve;
