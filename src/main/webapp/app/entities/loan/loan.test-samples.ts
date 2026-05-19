import dayjs from 'dayjs/esm';

import { ILoan, NewLoan } from './loan.model';

export const sampleWithRequiredData: ILoan = {
  id: 12643,
  borrowDate: dayjs('2026-05-18'),
  dueDate: dayjs('2026-05-18'),
  status: 'LATE',
};

export const sampleWithPartialData: ILoan = {
  id: 19895,
  borrowDate: dayjs('2026-05-18'),
  dueDate: dayjs('2026-05-18'),
  returnDate: dayjs('2026-05-18'),
  status: 'LATE',
};

export const sampleWithFullData: ILoan = {
  id: 27230,
  borrowDate: dayjs('2026-05-18'),
  dueDate: dayjs('2026-05-18'),
  returnDate: dayjs('2026-05-18'),
  status: 'BORROWED',
};

export const sampleWithNewData: NewLoan = {
  borrowDate: dayjs('2026-05-18'),
  dueDate: dayjs('2026-05-18'),
  status: 'LATE',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
