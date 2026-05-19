import dayjs from 'dayjs/esm';

import { IBook } from 'app/entities/book/book.model';
import { ILibrary } from 'app/entities/library/library.model';
import { IMember } from 'app/entities/member/member.model';

export interface IReview {
  id: number;
  rating?: number | null;
  comment?: string | null;
  createdAt?: dayjs.Dayjs | null;
  library?: Pick<ILibrary, 'id' | 'name'> | null;
  book?: Pick<IBook, 'id' | 'title'> | null;
  member?: Pick<IMember, 'id' | 'email'> | null;
}

export type NewReview = Omit<IReview, 'id'> & { id: null };
