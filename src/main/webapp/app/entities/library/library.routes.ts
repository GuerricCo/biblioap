import { Routes } from '@angular/router';

import { ASC } from 'app/config/navigation.constants';
import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';

import LibraryResolve from './route/library-routing-resolve.service';

const libraryRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/library').then(m => m.Library),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/library-detail').then(m => m.LibraryDetail),
    resolve: {
      library: LibraryResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/library-update').then(m => m.LibraryUpdate),
    resolve: {
      library: LibraryResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/library-update').then(m => m.LibraryUpdate),
    resolve: {
      library: LibraryResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default libraryRoute;
