import dayjs from 'dayjs/esm';

import { IReservation, NewReservation } from './reservation.model';

export const sampleWithRequiredData: IReservation = {
  id: 30105,
  reservationDate: dayjs('2026-05-18'),
  status: 'WAITING',
};

export const sampleWithPartialData: IReservation = {
  id: 8852,
  reservationDate: dayjs('2026-05-18'),
  status: 'READY',
};

export const sampleWithFullData: IReservation = {
  id: 27073,
  reservationDate: dayjs('2026-05-18'),
  status: 'READY',
};

export const sampleWithNewData: NewReservation = {
  reservationDate: dayjs('2026-05-18'),
  status: 'CANCELLED',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
