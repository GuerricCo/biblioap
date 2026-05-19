import dayjs from 'dayjs/esm';

import { IBook } from 'app/entities/book/book.model';

export interface IAuthor {
  id: number;
  firstName?: string | null;
  lastName?: string | null;
  birthDate?: dayjs.Dayjs | null;
  nationality?: string | null;
  biography?: string | null;
  bookses?: Pick<IBook, 'id' | 'title'>[] | null;
}

export type NewAuthor = Omit<IAuthor, 'id'> & { id: null };
