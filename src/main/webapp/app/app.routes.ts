import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { Authority } from 'app/shared/jhipster/constants';
import { errorRoute } from './layouts/error/error.route';

import { Library } from './entities/library/list/library';
import { LibraryUpdate } from './entities/library/update/library-update';

const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./login/login'),
    title: 'login.title',
  },
  {
    path: '',
    loadComponent: () => import('./layouts/navbar/navbar'),
    outlet: 'navbar',
  },
  {
    path: 'admin',
    data: { authorities: [Authority.ADMIN] },
    canActivate: [UserRouteAccessService],
    loadChildren: () => import('./admin/admin.routes'),
  },
  {
    path: 'account',
    loadChildren: () => import('./account/account.route'),
  },
  {
    path: 'login',
    loadComponent: () => import('./login/login'),
    title: 'login.title',
  },
  {
    path: '',
    loadChildren: () => import('./entities/entity.routes'),
  },
  {
    path: 'library',
    canActivate: [UserRouteAccessService],
    children: [
      { path: '', component: Library },
      { path: 'new', component: LibraryUpdate },
      { path: ':id/edit', component: LibraryUpdate },
    ],
  },
  {
    path: 'app/:libraryId',
    canActivate: [UserRouteAccessService],
    children: [
      {
        path: 'books',
        loadComponent: () => import('./entities/book/list/book').then(m => m.Book),
      },
      {
        path: 'authors',
        loadComponent: () => import('./entities/author/list/author').then(m => m.Author),
      },
      {
        path: 'categories',
        loadComponent: () => import('./entities/category/list/category').then(m => m.Category),
      },
      {
        path: 'members',
        loadComponent: () => import('./entities/member/list/member').then(m => m.Member),
      },
      {
        path: 'loans',
        loadComponent: () => import('./entities/loan/list/loan').then(m => m.Loan),
      },
      {
        path: 'reservations',
        loadComponent: () => import('./entities/reservation/list/reservation').then(m => m.Reservation),
      },
      {
        path: 'reviews',
        loadComponent: () => import('./entities/review/list/review').then(m => m.Review),
      },
      {
        path: '',
        redirectTo: 'books',
        pathMatch: 'full',
      },
    ],
  },
  ...errorRoute,
];

export default routes;
