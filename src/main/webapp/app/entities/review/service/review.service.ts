import { HttpClient, HttpResponse, httpResource } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';

import dayjs from 'dayjs/esm';
import { Observable, map } from 'rxjs';

import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { isPresent } from 'app/core/util/operators';
import { IReview, NewReview } from '../review.model';

export type PartialUpdateReview = Partial<IReview> & Pick<IReview, 'id'>;

type RestOf<T extends IReview | NewReview> = Omit<T, 'createdAt'> & {
  createdAt?: string | null;
};

export type RestReview = RestOf<IReview>;

export type NewRestReview = RestOf<NewReview>;

export type PartialUpdateRestReview = RestOf<PartialUpdateReview>;

@Injectable()
export class ReviewsService {
  readonly reviewsParams = signal<Record<string, string | number | boolean | readonly (string | number | boolean)[]> | undefined>(
    undefined,
  );
  readonly reviewsResource = httpResource<RestReview[]>(() => {
    const params = this.reviewsParams();
    if (!params) {
      return undefined;
    }
    return { url: this.resourceUrl, params };
  });
  /**
   * This signal holds the list of review that have been fetched. It is updated when the reviewsResource emits a new value.
   * In case of error while fetching the reviews, the signal is set to an empty array.
   */
  readonly reviews = computed(() =>
    (this.reviewsResource.hasValue() ? this.reviewsResource.value() : []).map(item => this.convertValueFromServer(item)),
  );
  protected readonly applicationConfigService = inject(ApplicationConfigService);
  protected readonly resourceUrl = this.applicationConfigService.getEndpointFor('api/reviews');

  protected convertValueFromServer(restReview: RestReview): IReview {
    return {
      ...restReview,
      createdAt: restReview.createdAt ? dayjs(restReview.createdAt) : undefined,
    };
  }
}

@Injectable({ providedIn: 'root' })
export class ReviewService extends ReviewsService {
  protected readonly http = inject(HttpClient);

  create(review: NewReview): Observable<IReview> {
    const copy = this.convertValueFromClient(review);
    return this.http.post<RestReview>(this.resourceUrl, copy).pipe(map(res => this.convertResponseFromServer(res)));
  }

  update(review: IReview): Observable<IReview> {
    const copy = this.convertValueFromClient(review);
    return this.http
      .put<RestReview>(`${this.resourceUrl}/${encodeURIComponent(this.getReviewIdentifier(review))}`, copy)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  partialUpdate(review: PartialUpdateReview): Observable<IReview> {
    const copy = this.convertValueFromClient(review);
    return this.http
      .patch<RestReview>(`${this.resourceUrl}/${encodeURIComponent(this.getReviewIdentifier(review))}`, copy)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  find(id: number): Observable<IReview> {
    return this.http.get<RestReview>(`${this.resourceUrl}/${encodeURIComponent(id)}`).pipe(map(res => this.convertResponseFromServer(res)));
  }

  query(req?: any): Observable<HttpResponse<IReview[]>> {
    const options = createRequestOption(req);
    return this.http
      .get<RestReview[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map(res => res.clone({ body: this.convertResponseArrayFromServer(res.body!) })));
  }

  delete(id: number): Observable<undefined> {
    return this.http.delete<undefined>(`${this.resourceUrl}/${encodeURIComponent(id)}`);
  }

  getReviewIdentifier(review: Pick<IReview, 'id'>): number {
    return review.id;
  }

  compareReview(o1: Pick<IReview, 'id'> | null, o2: Pick<IReview, 'id'> | null): boolean {
    return o1 && o2 ? this.getReviewIdentifier(o1) === this.getReviewIdentifier(o2) : o1 === o2;
  }

  addReviewToCollectionIfMissing<Type extends Pick<IReview, 'id'>>(
    reviewCollection: Type[],
    ...reviewsToCheck: (Type | null | undefined)[]
  ): Type[] {
    const reviews: Type[] = reviewsToCheck.filter(isPresent);
    if (reviews.length > 0) {
      const reviewCollectionIdentifiers = reviewCollection.map(reviewItem => this.getReviewIdentifier(reviewItem));
      const reviewsToAdd = reviews.filter(reviewItem => {
        const reviewIdentifier = this.getReviewIdentifier(reviewItem);
        if (reviewCollectionIdentifiers.includes(reviewIdentifier)) {
          return false;
        }
        reviewCollectionIdentifiers.push(reviewIdentifier);
        return true;
      });
      return [...reviewsToAdd, ...reviewCollection];
    }
    return reviewCollection;
  }

  protected convertValueFromClient<T extends IReview | NewReview | PartialUpdateReview>(review: T): RestOf<T> {
    return {
      ...review,
      createdAt: review.createdAt?.toJSON() ?? null,
    };
  }

  protected convertResponseFromServer(res: RestReview): IReview {
    return this.convertValueFromServer(res);
  }

  protected convertResponseArrayFromServer(res: RestReview[]): IReview[] {
    return res.map(item => this.convertValueFromServer(item));
  }
}
