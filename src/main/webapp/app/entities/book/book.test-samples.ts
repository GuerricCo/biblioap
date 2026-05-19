import dayjs from 'dayjs/esm';

import { IBook, NewBook } from './book.model';

export const sampleWithRequiredData: IBook = {
  id: 3991,
  title: 'antique placide',
  isbn: 'marron vide',
  available: false,
  totalCopies: 31886,
  availableCopies: 31164,
};

export const sampleWithPartialData: IBook = {
  id: 24837,
  title: 'ha ha',
  isbn: 'biathlète',
  available: false,
  totalCopies: 26158,
  availableCopies: 7670,
};

export const sampleWithFullData: IBook = {
  id: 8637,
  title: 'satisfaire',
  isbn: 'étant donné que maintenant',
  publicationDate: dayjs('2026-05-18'),
  description: 'ouah',
  language: 'bof',
  pages: 10493,
  available: false,
  totalCopies: 8777,
  availableCopies: 25283,
};

export const sampleWithNewData: NewBook = {
  title: 'personnel professionnel si',
  isbn: 'affable bang',
  available: false,
  totalCopies: 14362,
  availableCopies: 32497,
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
