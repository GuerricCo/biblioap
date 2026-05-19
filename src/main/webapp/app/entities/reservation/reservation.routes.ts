import { Routes } from '@angular/router';

import { ASC } from 'app/config/navigation.constants';
import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';

import ReservationResolve from './route/reservation-routing-resolve.service';

const reservationRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/reservation').then(m => m.Reservation),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/reservation-detail').then(m => m.ReservationDetail),
    resolve: {
      reservation: ReservationResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/reservation-update').then(m => m.ReservationUpdate),
    resolve: {
      reservation: ReservationResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/reservation-update').then(m => m.ReservationUpdate),
    resolve: {
      reservation: ReservationResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default reservationRoute;
