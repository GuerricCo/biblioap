import { HttpClient, HttpResponse, httpResource } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';

import dayjs from 'dayjs/esm';
import { Observable, map } from 'rxjs';

import { DATE_FORMAT } from 'app/config/input.constants';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { isPresent } from 'app/core/util/operators';
import { ILoan } from 'app/entities/loan/loan.model';
import { IReservation, NewReservation } from '../reservation.model';

export type PartialUpdateReservation = Partial<IReservation> & Pick<IReservation, 'id'>;

type RestOf<T extends IReservation | NewReservation> = Omit<T, 'reservationDate'> & {
  reservationDate?: string | null;
};

export type RestReservation = RestOf<IReservation>;

export type NewRestReservation = RestOf<NewReservation>;

export type PartialUpdateRestReservation = RestOf<PartialUpdateReservation>;

@Injectable()
export class ReservationsService {
  readonly reservationsParams = signal<Record<string, string | number | boolean | readonly (string | number | boolean)[]> | undefined>(
    undefined,
  );
  readonly reservationsResource = httpResource<RestReservation[]>(() => {
    const params = this.reservationsParams();
    if (!params) {
      return undefined;
    }
    return { url: this.resourceUrl, params };
  });
  /**
   * This signal holds the list of reservation that have been fetched. It is updated when the reservationsResource emits a new value.
   * In case of error while fetching the reservations, the signal is set to an empty array.
   */
  readonly reservations = computed(() =>
    (this.reservationsResource.hasValue() ? this.reservationsResource.value() : []).map(item => this.convertValueFromServer(item)),
  );
  protected readonly applicationConfigService = inject(ApplicationConfigService);
  protected readonly resourceUrl = this.applicationConfigService.getEndpointFor('api/reservations');

  protected convertValueFromServer(restReservation: RestReservation): IReservation {
    return {
      ...restReservation,
      reservationDate: restReservation.reservationDate ? dayjs(restReservation.reservationDate) : undefined,
    };
  }
}

@Injectable({ providedIn: 'root' })
export class ReservationService extends ReservationsService {
  protected readonly http = inject(HttpClient);

  create(reservation: NewReservation): Observable<IReservation> {
    const copy = this.convertValueFromClient(reservation);
    return this.http.post<RestReservation>(this.resourceUrl, copy).pipe(map(res => this.convertResponseFromServer(res)));
  }

  update(reservation: IReservation): Observable<IReservation> {
    const copy = this.convertValueFromClient(reservation);
    return this.http
      .put<RestReservation>(`${this.resourceUrl}/${encodeURIComponent(this.getReservationIdentifier(reservation))}`, copy)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  partialUpdate(reservation: PartialUpdateReservation): Observable<IReservation> {
    const copy = this.convertValueFromClient(reservation);
    return this.http
      .patch<RestReservation>(`${this.resourceUrl}/${encodeURIComponent(this.getReservationIdentifier(reservation))}`, copy)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  find(id: number): Observable<IReservation> {
    return this.http
      .get<RestReservation>(`${this.resourceUrl}/${encodeURIComponent(id)}`)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  query(req?: any): Observable<HttpResponse<IReservation[]>> {
    const options = createRequestOption(req);
    return this.http
      .get<RestReservation[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map(res => res.clone({ body: this.convertResponseArrayFromServer(res.body!) })));
  }

  delete(id: number): Observable<undefined> {
    return this.http.delete<undefined>(`${this.resourceUrl}/${encodeURIComponent(id)}`);
  }

  convertToLoan(id: number): Observable<ILoan> {
    return this.http
      .post<ILoan>(`${this.resourceUrl}/${encodeURIComponent(id)}/convert-to-loan`, {})
      .pipe(map(res => this.convertLoanFromServer(res)));
  }

  private convertLoanFromServer(restLoan: ILoan): ILoan {
    return {
      ...restLoan,
      borrowDate: restLoan.borrowDate ? dayjs(restLoan.borrowDate) : undefined,
      dueDate: restLoan.dueDate ? dayjs(restLoan.dueDate) : undefined,
      returnDate: restLoan.returnDate ? dayjs(restLoan.returnDate) : undefined,
    };
  }

  getReservationIdentifier(reservation: Pick<IReservation, 'id'>): number {
    return reservation.id;
  }

  compareReservation(o1: Pick<IReservation, 'id'> | null, o2: Pick<IReservation, 'id'> | null): boolean {
    return o1 && o2 ? this.getReservationIdentifier(o1) === this.getReservationIdentifier(o2) : o1 === o2;
  }

  addReservationToCollectionIfMissing<Type extends Pick<IReservation, 'id'>>(
    reservationCollection: Type[],
    ...reservationsToCheck: (Type | null | undefined)[]
  ): Type[] {
    const reservations: Type[] = reservationsToCheck.filter(isPresent);
    if (reservations.length > 0) {
      const reservationCollectionIdentifiers = reservationCollection.map(reservationItem => this.getReservationIdentifier(reservationItem));
      const reservationsToAdd = reservations.filter(reservationItem => {
        const reservationIdentifier = this.getReservationIdentifier(reservationItem);
        if (reservationCollectionIdentifiers.includes(reservationIdentifier)) {
          return false;
        }
        reservationCollectionIdentifiers.push(reservationIdentifier);
        return true;
      });
      return [...reservationsToAdd, ...reservationCollection];
    }
    return reservationCollection;
  }

  protected convertValueFromClient<T extends IReservation | NewReservation | PartialUpdateReservation>(reservation: T): RestOf<T> {
    return {
      ...reservation,
      reservationDate: reservation.reservationDate?.format(DATE_FORMAT) ?? null,
    };
  }

  protected convertResponseFromServer(res: RestReservation): IReservation {
    return this.convertValueFromServer(res);
  }

  protected convertResponseArrayFromServer(res: RestReservation[]): IReservation[] {
    return res.map(item => this.convertValueFromServer(item));
  }
}
