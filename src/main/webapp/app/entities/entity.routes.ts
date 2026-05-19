import { Routes } from '@angular/router';

const routes: Routes = [
  {
    path: 'authority',
    data: { pageTitle: 'bibliApp.adminAuthority.home.title' },
    loadChildren: () => import('./admin/authority/authority.routes'),
  },
  {
    path: 'user-management',
    data: { pageTitle: 'userManagement.home.title' },
    loadChildren: () => import('./admin/user-management/user-management.routes'),
  },
  {
    path: 'library',
    data: { pageTitle: 'bibliApp.library.home.title' },
    loadChildren: () => import('./library/library.routes'),
  },
  {
    path: 'book',
    data: { pageTitle: 'bibliApp.book.home.title' },
    loadChildren: () => import('./book/book.routes'),
  },
  {
    path: 'author',
    data: { pageTitle: 'bibliApp.author.home.title' },
    loadChildren: () => import('./author/author.routes'),
  },
  {
    path: 'category',
    data: { pageTitle: 'bibliApp.category.home.title' },
    loadChildren: () => import('./category/category.routes'),
  },
  {
    path: 'member',
    data: { pageTitle: 'bibliApp.member.home.title' },
    loadChildren: () => import('./member/member.routes'),
  },
  {
    path: 'loan',
    data: { pageTitle: 'bibliApp.loan.home.title' },
    loadChildren: () => import('./loan/loan.routes'),
  },
  {
    path: 'review',
    data: { pageTitle: 'bibliApp.review.home.title' },
    loadChildren: () => import('./review/review.routes'),
  },
  {
    path: 'reservation',
    data: { pageTitle: 'bibliApp.reservation.home.title' },
    loadChildren: () => import('./reservation/reservation.routes'),
  },
  /* jhipster-needle-add-entity-route - JHipster will add entity modules routes here */
];

export default routes;
