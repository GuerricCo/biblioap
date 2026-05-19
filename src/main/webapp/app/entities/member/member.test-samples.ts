import dayjs from 'dayjs/esm';

import { IMember, NewMember } from './member.model';

export const sampleWithRequiredData: IMember = {
  id: 26797,
  firstName: 'Amaranthe',
  lastName: 'Caron',
  email: 'Amaryllis94@gmail.com',
  active: false,
};

export const sampleWithPartialData: IMember = {
  id: 8524,
  firstName: 'Cassien',
  lastName: 'Renault',
  email: 'Amiel.Guillot@yahoo.fr',
  active: true,
};

export const sampleWithFullData: IMember = {
  id: 30531,
  firstName: 'Pélagie',
  lastName: 'Guyot',
  email: 'Georges_Guerin@hotmail.fr',
  phone: '0740638908',
  membershipDate: dayjs('2026-05-18'),
  active: true,
};

export const sampleWithNewData: NewMember = {
  firstName: 'Janine',
  lastName: 'Girard',
  email: 'Aaron.Carpentier@hotmail.fr',
  active: false,
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
