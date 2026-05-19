import { Routes } from '@angular/router';

import { ASC } from 'app/config/navigation.constants';
import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';

import MemberResolve from './route/member-routing-resolve.service';

const memberRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/member').then(m => m.Member),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/member-detail').then(m => m.MemberDetail),
    resolve: {
      member: MemberResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/member-update').then(m => m.MemberUpdate),
    resolve: {
      member: MemberResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/member-update').then(m => m.MemberUpdate),
    resolve: {
      member: MemberResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default memberRoute;
