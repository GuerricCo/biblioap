import { Routes } from '@angular/router';

import { ASC } from 'app/config/navigation.constants';
import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';

import LoanResolve from './route/loan-routing-resolve.service';

const loanRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/loan').then(m => m.Loan),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/loan-detail').then(m => m.LoanDetail),
    resolve: {
      loan: LoanResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/loan-update').then(m => m.LoanUpdate),
    resolve: {
      loan: LoanResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/loan-update').then(m => m.LoanUpdate),
    resolve: {
      loan: LoanResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default loanRoute;
