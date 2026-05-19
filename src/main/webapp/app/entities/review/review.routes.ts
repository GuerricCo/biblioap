import { Routes } from '@angular/router';

import { ASC } from 'app/config/navigation.constants';
import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';

import ReviewResolve from './route/review-routing-resolve.service';

const reviewRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/review').then(m => m.Review),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/review-detail').then(m => m.ReviewDetail),
    resolve: {
      review: ReviewResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/review-update').then(m => m.ReviewUpdate),
    resolve: {
      review: ReviewResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/review-update').then(m => m.ReviewUpdate),
    resolve: {
      review: ReviewResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default reviewRoute;
