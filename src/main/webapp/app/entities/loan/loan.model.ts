import dayjs from 'dayjs/esm';

import { IBook } from 'app/entities/book/book.model';
import { LoanStatus } from 'app/entities/enumerations/loan-status.model';
import { ILibrary } from 'app/entities/library/library.model';
import { IMember } from 'app/entities/member/member.model';

export interface ILoan {
  id: number;
  borrowDate?: dayjs.Dayjs | null;
  dueDate?: dayjs.Dayjs | null;
  returnDate?: dayjs.Dayjs | null;
  status?: keyof typeof LoanStatus | null;
  library?: Pick<ILibrary, 'id' | 'name'> | null;
  book?: Pick<IBook, 'id' | 'title'> | null;
  member?: Pick<IMember, 'id' | 'email'> | null;
}

export type NewLoan = Omit<ILoan, 'id'> & { id: null };
