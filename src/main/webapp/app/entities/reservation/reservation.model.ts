import dayjs from 'dayjs/esm';

import { IBook } from 'app/entities/book/book.model';
import { ReservationStatus } from 'app/entities/enumerations/reservation-status.model';
import { ILibrary } from 'app/entities/library/library.model';
import { IMember } from 'app/entities/member/member.model';

export interface IReservation {
  id: number;
  reservationDate?: dayjs.Dayjs | null;
  status?: keyof typeof ReservationStatus | null;
  library?: Pick<ILibrary, 'id' | 'name'> | null;
  book?: Pick<IBook, 'id' | 'title'> | null;
  member?: Pick<IMember, 'id' | 'email'> | null;
}

export type NewReservation = Omit<IReservation, 'id'> & { id: null };
