import { IUser } from './user.model';

export const sampleWithRequiredData: IUser = {
  id: 24814,
  login: 'Lucienne36',
};

export const sampleWithPartialData: IUser = {
  id: 966,
  login: 'Fulcran_Prevost',
};

export const sampleWithFullData: IUser = {
  id: 5440,
  login: 'Hedelin.Cousin20',
};
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
