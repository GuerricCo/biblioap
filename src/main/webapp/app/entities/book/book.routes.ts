import { Routes } from '@angular/router';

import { ASC } from 'app/config/navigation.constants';
import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';

import BookResolve from './route/book-routing-resolve.service';

const bookRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/book').then(m => m.Book),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/book-detail').then(m => m.BookDetail),
    resolve: {
      book: BookResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/book-update').then(m => m.BookUpdate),
    resolve: {
      book: BookResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/book-update').then(m => m.BookUpdate),
    resolve: {
      book: BookResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default bookRoute;
