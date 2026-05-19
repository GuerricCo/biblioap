import dayjs from 'dayjs/esm';

import { ILibrary } from 'app/entities/library/library.model';

export interface IMember {
  id: number;
  firstName?: string | null;
  lastName?: string | null;
  email?: string | null;
  phone?: string | null;
  membershipDate?: dayjs.Dayjs | null;
  active?: boolean | null;
  library?: Pick<ILibrary, 'id' | 'name'> | null;
}

export type NewMember = Omit<IMember, 'id'> & { id: null };
