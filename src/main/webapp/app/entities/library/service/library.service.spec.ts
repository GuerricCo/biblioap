import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { ILibrary } from '../library.model';
import { sampleWithFullData, sampleWithNewData, sampleWithPartialData, sampleWithRequiredData } from '../library.test-samples';

import { LibraryService } from './library.service';

const requireRestSample: ILibrary = {
  ...sampleWithRequiredData,
};

describe('Library Service', () => {
  let service: LibraryService;
  let httpMock: HttpTestingController;
  let expectedResult: ILibrary | ILibrary[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClientTesting()],
    });
    expectedResult = null;
    service = TestBed.inject(LibraryService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  describe('Service methods', () => {
    it('should find an element', () => {
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.find(123).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should create a Library', () => {
      const library = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(library).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a Library', () => {
      const library = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(library).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a Library', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of Library', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a Library', () => {
      service.delete(123).subscribe();

      const requests = httpMock.match({ method: 'DELETE' });
      expect(requests.length).toBe(1);
    });

    describe('addLibraryToCollectionIfMissing', () => {
      it('should add a Library to an empty array', () => {
        const library: ILibrary = sampleWithRequiredData;
        expectedResult = service.addLibraryToCollectionIfMissing([], library);
        expect(expectedResult).toEqual([library]);
      });

      it('should not add a Library to an array that contains it', () => {
        const library: ILibrary = sampleWithRequiredData;
        const libraryCollection: ILibrary[] = [
          {
            ...library,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addLibraryToCollectionIfMissing(libraryCollection, library);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a Library to an array that doesn't contain it", () => {
        const library: ILibrary = sampleWithRequiredData;
        const libraryCollection: ILibrary[] = [sampleWithPartialData];
        expectedResult = service.addLibraryToCollectionIfMissing(libraryCollection, library);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(library);
      });

      it('should add only unique Library to an array', () => {
        const libraryArray: ILibrary[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const libraryCollection: ILibrary[] = [sampleWithRequiredData];
        expectedResult = service.addLibraryToCollectionIfMissing(libraryCollection, ...libraryArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const library: ILibrary = sampleWithRequiredData;
        const library2: ILibrary = sampleWithPartialData;
        expectedResult = service.addLibraryToCollectionIfMissing([], library, library2);
        expect(expectedResult).toEqual([library, library2]);
      });

      it('should accept null and undefined values', () => {
        const library: ILibrary = sampleWithRequiredData;
        expectedResult = service.addLibraryToCollectionIfMissing([], null, library, undefined);
        expect(expectedResult).toEqual([library]);
      });

      it('should return initial array if no Library is added', () => {
        const libraryCollection: ILibrary[] = [sampleWithRequiredData];
        expectedResult = service.addLibraryToCollectionIfMissing(libraryCollection, undefined, null);
        expect(expectedResult).toEqual(libraryCollection);
      });
    });

    describe('compareLibrary', () => {
      it('should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareLibrary(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('should return false if one entity is null', () => {
        const entity1 = { id: 28830 };
        const entity2 = null;

        const compareResult1 = service.compareLibrary(entity1, entity2);
        const compareResult2 = service.compareLibrary(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey differs', () => {
        const entity1 = { id: 28830 };
        const entity2 = { id: 16262 };

        const compareResult1 = service.compareLibrary(entity1, entity2);
        const compareResult2 = service.compareLibrary(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey matches', () => {
        const entity1 = { id: 28830 };
        const entity2 = { id: 28830 };

        const compareResult1 = service.compareLibrary(entity1, entity2);
        const compareResult2 = service.compareLibrary(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
