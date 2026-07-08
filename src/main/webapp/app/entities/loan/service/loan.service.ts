import { HttpClient, HttpResponse, httpResource } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';

import dayjs from 'dayjs/esm';
import { Observable, map } from 'rxjs';

import { DATE_FORMAT } from 'app/config/input.constants';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { isPresent } from 'app/core/util/operators';
import { ILoan, NewLoan } from '../loan.model';

export type PartialUpdateLoan = Partial<ILoan> & Pick<ILoan, 'id'>;

type RestOf<T extends ILoan | NewLoan> = Omit<T, 'borrowDate' | 'dueDate' | 'returnDate'> & {
  borrowDate?: string | null;
  dueDate?: string | null;
  returnDate?: string | null;
};

export type RestLoan = RestOf<ILoan>;

export type NewRestLoan = RestOf<NewLoan>;

export type PartialUpdateRestLoan = RestOf<PartialUpdateLoan>;

@Injectable()
export class LoansService {
  readonly loansParams = signal<Record<string, string | number | boolean | readonly (string | number | boolean)[]> | undefined>(undefined);
  readonly loansResource = httpResource<RestLoan[]>(() => {
    const params = this.loansParams();
    if (!params) {
      return undefined;
    }
    return { url: this.resourceUrl, params };
  });
  /**
   * This signal holds the list of loan that have been fetched. It is updated when the loansResource emits a new value.
   * In case of error while fetching the loans, the signal is set to an empty array.
   */
  readonly loans = computed(() =>
    (this.loansResource.hasValue() ? this.loansResource.value() : []).map(item => this.convertValueFromServer(item)),
  );
  protected readonly applicationConfigService = inject(ApplicationConfigService);
  protected readonly resourceUrl = this.applicationConfigService.getEndpointFor('api/loans');

  protected convertValueFromServer(restLoan: RestLoan): ILoan {
    return {
      ...restLoan,
      borrowDate: restLoan.borrowDate ? dayjs(restLoan.borrowDate) : undefined,
      dueDate: restLoan.dueDate ? dayjs(restLoan.dueDate) : undefined,
      returnDate: restLoan.returnDate ? dayjs(restLoan.returnDate) : undefined,
    };
  }
}

@Injectable({ providedIn: 'root' })
export class LoanService extends LoansService {
  protected readonly http = inject(HttpClient);

  create(loan: NewLoan): Observable<ILoan> {
    const copy = this.convertValueFromClient(loan);
    return this.http.post<RestLoan>(this.resourceUrl, copy).pipe(map(res => this.convertResponseFromServer(res)));
  }

  update(loan: ILoan): Observable<ILoan> {
    const copy = this.convertValueFromClient(loan);
    return this.http
      .put<RestLoan>(`${this.resourceUrl}/${encodeURIComponent(this.getLoanIdentifier(loan))}`, copy)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  partialUpdate(loan: PartialUpdateLoan): Observable<ILoan> {
    const copy = this.convertValueFromClient(loan);
    return this.http
      .patch<RestLoan>(`${this.resourceUrl}/${encodeURIComponent(this.getLoanIdentifier(loan))}`, copy)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  find(id: number): Observable<ILoan> {
    return this.http.get<RestLoan>(`${this.resourceUrl}/${encodeURIComponent(id)}`).pipe(map(res => this.convertResponseFromServer(res)));
  }

  query(req?: any): Observable<HttpResponse<ILoan[]>> {
    const options = createRequestOption(req);
    return this.http
      .get<RestLoan[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map(res => res.clone({ body: this.convertResponseArrayFromServer(res.body!) })));
  }

  delete(id: number): Observable<undefined> {
    return this.http.delete<undefined>(`${this.resourceUrl}/${encodeURIComponent(id)}`);
  }

  returnLoan(id: number): Observable<ILoan> {
    return this.http
      .post<RestLoan>(`${this.resourceUrl}/${encodeURIComponent(id)}/return`, {})
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  getLoanIdentifier(loan: Pick<ILoan, 'id'>): number {
    return loan.id;
  }

  compareLoan(o1: Pick<ILoan, 'id'> | null, o2: Pick<ILoan, 'id'> | null): boolean {
    return o1 && o2 ? this.getLoanIdentifier(o1) === this.getLoanIdentifier(o2) : o1 === o2;
  }

  addLoanToCollectionIfMissing<Type extends Pick<ILoan, 'id'>>(
    loanCollection: Type[],
    ...loansToCheck: (Type | null | undefined)[]
  ): Type[] {
    const loans: Type[] = loansToCheck.filter(isPresent);
    if (loans.length > 0) {
      const loanCollectionIdentifiers = loanCollection.map(loanItem => this.getLoanIdentifier(loanItem));
      const loansToAdd = loans.filter(loanItem => {
        const loanIdentifier = this.getLoanIdentifier(loanItem);
        if (loanCollectionIdentifiers.includes(loanIdentifier)) {
          return false;
        }
        loanCollectionIdentifiers.push(loanIdentifier);
        return true;
      });
      return [...loansToAdd, ...loanCollection];
    }
    return loanCollection;
  }

  protected convertValueFromClient<T extends ILoan | NewLoan | PartialUpdateLoan>(loan: T): RestOf<T> {
    return {
      ...loan,
      borrowDate: loan.borrowDate?.format(DATE_FORMAT) ?? null,
      dueDate: loan.dueDate?.format(DATE_FORMAT) ?? null,
      returnDate: loan.returnDate?.format(DATE_FORMAT) ?? null,
    };
  }

  protected convertResponseFromServer(res: RestLoan): ILoan {
    return this.convertValueFromServer(res);
  }

  protected convertResponseArrayFromServer(res: RestLoan[]): ILoan[] {
    return res.map(item => this.convertValueFromServer(item));
  }
}
