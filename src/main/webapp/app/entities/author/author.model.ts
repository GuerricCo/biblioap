import dayjs from 'dayjs/esm';

import { IBook } from 'app/entities/book/book.model';
import { ILibrary } from 'app/entities/library/library.model';

export interface IAuthor {
  id: number;
  firstName?: string | null;
  lastName?: string | null;
  birthDate?: dayjs.Dayjs | null;
  nationality?: string | null;
  biography?: string | null;
  bookses?: Pick<IBook, 'id' | 'title'>[] | null;
  library?: Pick<ILibrary, 'id' | 'name'> | null;
}

export type NewAuthor = Omit<IAuthor, 'id'> & { id: null };
