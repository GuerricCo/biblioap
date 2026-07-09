import { ILibrary } from 'app/entities/library/library.model';

export interface ICategory {
  id: number;
  name?: string | null;
  description?: string | null;
  library?: Pick<ILibrary, 'id' | 'name'> | null;
}

export type NewCategory = Omit<ICategory, 'id'> & { id: null };
