import dayjs from 'dayjs/esm';

import { IReview, NewReview } from './review.model';

export const sampleWithRequiredData: IReview = {
  id: 6640,
  rating: 2,
  createdAt: dayjs('2026-05-18T03:00'),
};

export const sampleWithPartialData: IReview = {
  id: 14979,
  rating: 2,
  createdAt: dayjs('2026-05-18T07:28'),
};

export const sampleWithFullData: IReview = {
  id: 30916,
  rating: 4,
  comment: 'bof',
  createdAt: dayjs('2026-05-18T04:51'),
};

export const sampleWithNewData: NewReview = {
  rating: 2,
  createdAt: dayjs('2026-05-18T19:59'),
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
