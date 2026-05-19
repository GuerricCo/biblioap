export interface ILibrary {
  id: number;
  name?: string | null;
  address?: string | null;
  city?: string | null;
  phone?: string | null;
  email?: string | null;
}

export type NewLibrary = Omit<ILibrary, 'id'> & { id: null };
