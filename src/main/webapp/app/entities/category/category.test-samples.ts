import { ICategory, NewCategory } from './category.model';

export const sampleWithRequiredData: ICategory = {
  id: 8109,
  name: 'au-dedans de conférer mal',
};

export const sampleWithPartialData: ICategory = {
  id: 862,
  name: 'si',
  description: 'surveiller diablement de peur que',
};

export const sampleWithFullData: ICategory = {
  id: 28780,
  name: 'plouf',
  description: 'gens',
};

export const sampleWithNewData: NewCategory = {
  name: 'aussitôt que cocorico quand ?',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
