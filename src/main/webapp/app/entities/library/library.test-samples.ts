import { ILibrary, NewLibrary } from './library.model';

export const sampleWithRequiredData: ILibrary = {
  id: 5333,
  name: 'jusque',
};

export const sampleWithPartialData: ILibrary = {
  id: 4903,
  name: 'alentour avant que',
  phone: '+33 714259196',
};

export const sampleWithFullData: ILibrary = {
  id: 19721,
  name: 'afin de',
  address: 'de peur que de crainte que minuscule',
  city: 'Marseille',
  phone: '+33 466593076',
  email: 'Athenais_Marty7@gmail.com',
};

export const sampleWithNewData: NewLibrary = {
  name: 'ah',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
