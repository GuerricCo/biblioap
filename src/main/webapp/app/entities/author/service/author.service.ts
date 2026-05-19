import { HttpClient, HttpResponse, httpResource } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';

import dayjs from 'dayjs/esm';
import { Observable, map } from 'rxjs';

import { DATE_FORMAT } from 'app/config/input.constants';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { isPresent } from 'app/core/util/operators';
import { IAuthor, NewAuthor } from '../author.model';

export type PartialUpdateAuthor = Partial<IAuthor> & Pick<IAuthor, 'id'>;

type RestOf<T extends IAuthor | NewAuthor> = Omit<T, 'birthDate'> & {
  birthDate?: string | null;
};

export type RestAuthor = RestOf<IAuthor>;

export type NewRestAuthor = RestOf<NewAuthor>;

export type PartialUpdateRestAuthor = RestOf<PartialUpdateAuthor>;

@Injectable()
export class AuthorsService {
  readonly authorsParams = signal<Record<string, string | number | boolean | readonly (string | number | boolean)[]> | undefined>(
    undefined,
  );
  readonly authorsResource = httpResource<RestAuthor[]>(() => {
    const params = this.authorsParams();
    if (!params) {
      return undefined;
    }
    return { url: this.resourceUrl, params };
  });
  /**
   * This signal holds the list of author that have been fetched. It is updated when the authorsResource emits a new value.
   * In case of error while fetching the authors, the signal is set to an empty array.
   */
  readonly authors = computed(() =>
    (this.authorsResource.hasValue() ? this.authorsResource.value() : []).map(item => this.convertValueFromServer(item)),
  );
  protected readonly applicationConfigService = inject(ApplicationConfigService);
  protected readonly resourceUrl = this.applicationConfigService.getEndpointFor('api/authors');

  protected convertValueFromServer(restAuthor: RestAuthor): IAuthor {
    return {
      ...restAuthor,
      birthDate: restAuthor.birthDate ? dayjs(restAuthor.birthDate) : undefined,
    };
  }
}

@Injectable({ providedIn: 'root' })
export class AuthorService extends AuthorsService {
  protected readonly http = inject(HttpClient);

  create(author: NewAuthor): Observable<IAuthor> {
    const copy = this.convertValueFromClient(author);
    return this.http.post<RestAuthor>(this.resourceUrl, copy).pipe(map(res => this.convertResponseFromServer(res)));
  }

  update(author: IAuthor): Observable<IAuthor> {
    const copy = this.convertValueFromClient(author);
    return this.http
      .put<RestAuthor>(`${this.resourceUrl}/${encodeURIComponent(this.getAuthorIdentifier(author))}`, copy)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  partialUpdate(author: PartialUpdateAuthor): Observable<IAuthor> {
    const copy = this.convertValueFromClient(author);
    return this.http
      .patch<RestAuthor>(`${this.resourceUrl}/${encodeURIComponent(this.getAuthorIdentifier(author))}`, copy)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  find(id: number): Observable<IAuthor> {
    return this.http.get<RestAuthor>(`${this.resourceUrl}/${encodeURIComponent(id)}`).pipe(map(res => this.convertResponseFromServer(res)));
  }

  query(req?: any): Observable<HttpResponse<IAuthor[]>> {
    const options = createRequestOption(req);
    return this.http
      .get<RestAuthor[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map(res => res.clone({ body: this.convertResponseArrayFromServer(res.body!) })));
  }

  delete(id: number): Observable<undefined> {
    return this.http.delete<undefined>(`${this.resourceUrl}/${encodeURIComponent(id)}`);
  }

  getAuthorIdentifier(author: Pick<IAuthor, 'id'>): number {
    return author.id;
  }

  compareAuthor(o1: Pick<IAuthor, 'id'> | null, o2: Pick<IAuthor, 'id'> | null): boolean {
    return o1 && o2 ? this.getAuthorIdentifier(o1) === this.getAuthorIdentifier(o2) : o1 === o2;
  }

  addAuthorToCollectionIfMissing<Type extends Pick<IAuthor, 'id'>>(
    authorCollection: Type[],
    ...authorsToCheck: (Type | null | undefined)[]
  ): Type[] {
    const authors: Type[] = authorsToCheck.filter(isPresent);
    if (authors.length > 0) {
      const authorCollectionIdentifiers = authorCollection.map(authorItem => this.getAuthorIdentifier(authorItem));
      const authorsToAdd = authors.filter(authorItem => {
        const authorIdentifier = this.getAuthorIdentifier(authorItem);
        if (authorCollectionIdentifiers.includes(authorIdentifier)) {
          return false;
        }
        authorCollectionIdentifiers.push(authorIdentifier);
        return true;
      });
      return [...authorsToAdd, ...authorCollection];
    }
    return authorCollection;
  }

  protected convertValueFromClient<T extends IAuthor | NewAuthor | PartialUpdateAuthor>(author: T): RestOf<T> {
    return {
      ...author,
      birthDate: author.birthDate?.format(DATE_FORMAT) ?? null,
    };
  }

  protected convertResponseFromServer(res: RestAuthor): IAuthor {
    return this.convertValueFromServer(res);
  }

  protected convertResponseArrayFromServer(res: RestAuthor[]): IAuthor[] {
    return res.map(item => this.convertValueFromServer(item));
  }
}
