import dayjs from 'dayjs/esm';

import { IAuthor } from 'app/entities/author/author.model';
import { ICategory } from 'app/entities/category/category.model';
import { ILibrary } from 'app/entities/library/library.model';

export interface IBook {
  id: number;
  title?: string | null;
  isbn?: string | null;
  publicationDate?: dayjs.Dayjs | null;
  description?: string | null;
  language?: string | null;
  pages?: number | null;
  available?: boolean | null;
  totalCopies?: number | null;
  availableCopies?: number | null;
  library?: Pick<ILibrary, 'id' | 'name'> | null;
  category?: Pick<ICategory, 'id' | 'name'> | null;
  authorses?: Pick<IAuthor, 'id' | 'firstName'>[] | null;
}

export type NewBook = Omit<IBook, 'id'> & { id: null };
