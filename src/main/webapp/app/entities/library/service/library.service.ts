import { HttpClient, HttpResponse, httpResource } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';

import { Observable } from 'rxjs';

import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { isPresent } from 'app/core/util/operators';
import { ILibrary, NewLibrary } from '../library.model';

export type PartialUpdateLibrary = Partial<ILibrary> & Pick<ILibrary, 'id'>;

@Injectable()
export class LibrariesService {
  readonly librariesParams = signal<Record<string, string | number | boolean | readonly (string | number | boolean)[]> | undefined>(
    undefined,
  );
  readonly librariesResource = httpResource<ILibrary[]>(() => {
    const params = this.librariesParams();
    if (!params) {
      return undefined;
    }
    return { url: this.resourceUrl, params };
  });
  /**
   * This signal holds the list of library that have been fetched. It is updated when the librariesResource emits a new value.
   * In case of error while fetching the libraries, the signal is set to an empty array.
   */
  readonly libraries = computed(() => (this.librariesResource.hasValue() ? this.librariesResource.value() : []));
  protected readonly applicationConfigService = inject(ApplicationConfigService);
  protected readonly resourceUrl = this.applicationConfigService.getEndpointFor('api/libraries');
}

@Injectable({ providedIn: 'root' })
export class LibraryService extends LibrariesService {
  protected readonly http = inject(HttpClient);

  create(library: NewLibrary): Observable<ILibrary> {
    return this.http.post<ILibrary>(this.resourceUrl, library);
  }

  update(library: ILibrary): Observable<ILibrary> {
    return this.http.put<ILibrary>(`${this.resourceUrl}/${encodeURIComponent(this.getLibraryIdentifier(library))}`, library);
  }

  partialUpdate(library: PartialUpdateLibrary): Observable<ILibrary> {
    return this.http.patch<ILibrary>(`${this.resourceUrl}/${encodeURIComponent(this.getLibraryIdentifier(library))}`, library);
  }

  find(id: number): Observable<ILibrary> {
    return this.http.get<ILibrary>(`${this.resourceUrl}/${encodeURIComponent(id)}`);
  }

  query(req?: any): Observable<HttpResponse<ILibrary[]>> {
    const options = createRequestOption(req);
    return this.http.get<ILibrary[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<undefined> {
    return this.http.delete<undefined>(`${this.resourceUrl}/${encodeURIComponent(id)}`);
  }

  getLibraryIdentifier(library: Pick<ILibrary, 'id'>): number {
    return library.id;
  }

  compareLibrary(o1: Pick<ILibrary, 'id'> | null, o2: Pick<ILibrary, 'id'> | null): boolean {
    return o1 && o2 ? this.getLibraryIdentifier(o1) === this.getLibraryIdentifier(o2) : o1 === o2;
  }

  addLibraryToCollectionIfMissing<Type extends Pick<ILibrary, 'id'>>(
    libraryCollection: Type[],
    ...librariesToCheck: (Type | null | undefined)[]
  ): Type[] {
    const libraries: Type[] = librariesToCheck.filter(isPresent);
    if (libraries.length > 0) {
      const libraryCollectionIdentifiers = libraryCollection.map(libraryItem => this.getLibraryIdentifier(libraryItem));
      const librariesToAdd = libraries.filter(libraryItem => {
        const libraryIdentifier = this.getLibraryIdentifier(libraryItem);
        if (libraryCollectionIdentifiers.includes(libraryIdentifier)) {
          return false;
        }
        libraryCollectionIdentifiers.push(libraryIdentifier);
        return true;
      });
      return [...librariesToAdd, ...libraryCollection];
    }
    return libraryCollection;
  }
}
