// src/main/webapp/app/core/library-context/library-context.service.ts
import { Injectable, signal } from '@angular/core';
import { ILibrary } from 'app/entities/library/library.model';

@Injectable({ providedIn: 'root' })
export class LibraryContextService {
  readonly currentLibrary = signal<ILibrary | null>(null);
  readonly currentLibraryId = signal<number | null>(null);

  setLibrary(library: ILibrary): void {
    this.currentLibrary.set(library);
    this.currentLibraryId.set(library.id!);
  }

  clear(): void {
    this.currentLibrary.set(null);
    this.currentLibraryId.set(null);
  }
}
