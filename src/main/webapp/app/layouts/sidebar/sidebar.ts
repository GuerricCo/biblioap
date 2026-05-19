import { Component, inject, computed } from '@angular/core';
import { RouterLink, RouterLinkActive, Router, NavigationEnd } from '@angular/router';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { toSignal } from '@angular/core/rxjs-interop';
import { filter, map, startWith } from 'rxjs/operators';

import { AccountService } from 'app/core/auth/account.service';

@Component({
  selector: 'jhi-sidebar',
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.scss',
  imports: [RouterLink, RouterLinkActive, FontAwesomeModule],
})
export default class Sidebar {
  readonly account = inject(AccountService).account;
  private readonly router = inject(Router);

  private readonly currentUrl = toSignal(
    this.router.events.pipe(
      filter(e => e instanceof NavigationEnd),
      map((e: any) => e.urlAfterRedirects),
      startWith(this.router.url),
    ),
  );

  readonly isAuthPage = computed(() => {
    const url = this.currentUrl() ?? '';
    return url === '/' || url === '/login' || url.startsWith('/account/register') || url.startsWith('/account/reset/request');
  });
}
