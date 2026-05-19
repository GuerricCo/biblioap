import { HttpClient, HttpResponse, httpResource } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';

import dayjs from 'dayjs/esm';
import { Observable, map } from 'rxjs';

import { DATE_FORMAT } from 'app/config/input.constants';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { isPresent } from 'app/core/util/operators';
import { IBook, NewBook } from '../book.model';

export type PartialUpdateBook = Partial<IBook> & Pick<IBook, 'id'>;

type RestOf<T extends IBook | NewBook> = Omit<T, 'publicationDate'> & {
  publicationDate?: string | null;
};

export type RestBook = RestOf<IBook>;

export type NewRestBook = RestOf<NewBook>;

export type PartialUpdateRestBook = RestOf<PartialUpdateBook>;

@Injectable()
export class BooksService {
  readonly booksParams = signal<Record<string, string | number | boolean | readonly (string | number | boolean)[]> | undefined>(undefined);
  readonly booksResource = httpResource<RestBook[]>(() => {
    const params = this.booksParams();
    if (!params) {
      return undefined;
    }
    return { url: this.resourceUrl, params };
  });
  /**
   * This signal holds the list of book that have been fetched. It is updated when the booksResource emits a new value.
   * In case of error while fetching the books, the signal is set to an empty array.
   */
  readonly books = computed(() =>
    (this.booksResource.hasValue() ? this.booksResource.value() : []).map(item => this.convertValueFromServer(item)),
  );
  protected readonly applicationConfigService = inject(ApplicationConfigService);
  protected readonly resourceUrl = this.applicationConfigService.getEndpointFor('api/books');

  protected convertValueFromServer(restBook: RestBook): IBook {
    return {
      ...restBook,
      publicationDate: restBook.publicationDate ? dayjs(restBook.publicationDate) : undefined,
    };
  }
}

@Injectable({ providedIn: 'root' })
export class BookService extends BooksService {
  protected readonly http = inject(HttpClient);

  create(book: NewBook): Observable<IBook> {
    const copy = this.convertValueFromClient(book);
    return this.http.post<RestBook>(this.resourceUrl, copy).pipe(map(res => this.convertResponseFromServer(res)));
  }

  update(book: IBook): Observable<IBook> {
    const copy = this.convertValueFromClient(book);
    return this.http
      .put<RestBook>(`${this.resourceUrl}/${encodeURIComponent(this.getBookIdentifier(book))}`, copy)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  partialUpdate(book: PartialUpdateBook): Observable<IBook> {
    const copy = this.convertValueFromClient(book);
    return this.http
      .patch<RestBook>(`${this.resourceUrl}/${encodeURIComponent(this.getBookIdentifier(book))}`, copy)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  find(id: number): Observable<IBook> {
    return this.http.get<RestBook>(`${this.resourceUrl}/${encodeURIComponent(id)}`).pipe(map(res => this.convertResponseFromServer(res)));
  }

  query(req?: any): Observable<HttpResponse<IBook[]>> {
    const options = createRequestOption(req);
    return this.http
      .get<RestBook[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map(res => res.clone({ body: this.convertResponseArrayFromServer(res.body!) })));
  }

  delete(id: number): Observable<undefined> {
    return this.http.delete<undefined>(`${this.resourceUrl}/${encodeURIComponent(id)}`);
  }

  getBookIdentifier(book: Pick<IBook, 'id'>): number {
    return book.id;
  }

  compareBook(o1: Pick<IBook, 'id'> | null, o2: Pick<IBook, 'id'> | null): boolean {
    return o1 && o2 ? this.getBookIdentifier(o1) === this.getBookIdentifier(o2) : o1 === o2;
  }

  addBookToCollectionIfMissing<Type extends Pick<IBook, 'id'>>(
    bookCollection: Type[],
    ...booksToCheck: (Type | null | undefined)[]
  ): Type[] {
    const books: Type[] = booksToCheck.filter(isPresent);
    if (books.length > 0) {
      const bookCollectionIdentifiers = bookCollection.map(bookItem => this.getBookIdentifier(bookItem));
      const booksToAdd = books.filter(bookItem => {
        const bookIdentifier = this.getBookIdentifier(bookItem);
        if (bookCollectionIdentifiers.includes(bookIdentifier)) {
          return false;
        }
        bookCollectionIdentifiers.push(bookIdentifier);
        return true;
      });
      return [...booksToAdd, ...bookCollection];
    }
    return bookCollection;
  }

  protected convertValueFromClient<T extends IBook | NewBook | PartialUpdateBook>(book: T): RestOf<T> {
    return {
      ...book,
      publicationDate: book.publicationDate?.format(DATE_FORMAT) ?? null,
    };
  }

  protected convertResponseFromServer(res: RestBook): IBook {
    return this.convertValueFromServer(res);
  }

  protected convertResponseArrayFromServer(res: RestBook[]): IBook[] {
    return res.map(item => this.convertValueFromServer(item));
  }
}
