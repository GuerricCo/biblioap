import dayjs from 'dayjs/esm';

import { IAuthor, NewAuthor } from './author.model';

export const sampleWithRequiredData: IAuthor = {
  id: 24433,
  firstName: 'Adéodat',
  lastName: 'Renault',
};

export const sampleWithPartialData: IAuthor = {
  id: 14133,
  firstName: 'Annette',
  lastName: 'Lemaire',
  birthDate: dayjs('2026-05-18'),
  nationality: 'désormais patientèle au cas où',
};

export const sampleWithFullData: IAuthor = {
  id: 16232,
  firstName: 'Agnane',
  lastName: 'Francois',
  birthDate: dayjs('2026-05-18'),
  nationality: 'hé rédaction',
  biography: 'tousser saigner touriste',
};

export const sampleWithNewData: NewAuthor = {
  firstName: 'Séraphin',
  lastName: 'Noel',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
