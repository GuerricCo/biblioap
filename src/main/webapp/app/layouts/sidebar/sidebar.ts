import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';

import { AccountService } from 'app/core/auth/account.service';
import { LibraryContextService } from 'app/core/library-context/library-context.service';

@Component({
  selector: 'jhi-sidebar',
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.scss',
  imports: [RouterLink, RouterLinkActive, FontAwesomeModule],
})
export default class Sidebar {
  readonly account = inject(AccountService).account;

  private readonly router = inject(Router);
  private readonly libraryContext = inject(LibraryContextService);

  readonly currentLibrary = this.libraryContext.currentLibrary;
  readonly libraryId = this.libraryContext.currentLibraryId;

  isAuthPage(): boolean {
    const url = this.router.url;
    return url === '/' || url.startsWith('/login') || url.startsWith('/account/register') || url.startsWith('/library');
  }
}
