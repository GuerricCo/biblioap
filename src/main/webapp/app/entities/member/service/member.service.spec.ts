import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { DATE_FORMAT } from 'app/config/input.constants';
import { IMember } from '../member.model';
import { sampleWithFullData, sampleWithNewData, sampleWithPartialData, sampleWithRequiredData } from '../member.test-samples';

import { MemberService, RestMember } from './member.service';

const requireRestSample: RestMember = {
  ...sampleWithRequiredData,
  membershipDate: sampleWithRequiredData.membershipDate?.format(DATE_FORMAT),
};

describe('Member Service', () => {
  let service: MemberService;
  let httpMock: HttpTestingController;
  let expectedResult: IMember | IMember[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClientTesting()],
    });
    expectedResult = null;
    service = TestBed.inject(MemberService);
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

    it('should create a Member', () => {
      const member = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(member).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a Member', () => {
      const member = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(member).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a Member', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of Member', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a Member', () => {
      service.delete(123).subscribe();

      const requests = httpMock.match({ method: 'DELETE' });
      expect(requests.length).toBe(1);
    });

    describe('addMemberToCollectionIfMissing', () => {
      it('should add a Member to an empty array', () => {
        const member: IMember = sampleWithRequiredData;
        expectedResult = service.addMemberToCollectionIfMissing([], member);
        expect(expectedResult).toEqual([member]);
      });

      it('should not add a Member to an array that contains it', () => {
        const member: IMember = sampleWithRequiredData;
        const memberCollection: IMember[] = [
          {
            ...member,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addMemberToCollectionIfMissing(memberCollection, member);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a Member to an array that doesn't contain it", () => {
        const member: IMember = sampleWithRequiredData;
        const memberCollection: IMember[] = [sampleWithPartialData];
        expectedResult = service.addMemberToCollectionIfMissing(memberCollection, member);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(member);
      });

      it('should add only unique Member to an array', () => {
        const memberArray: IMember[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const memberCollection: IMember[] = [sampleWithRequiredData];
        expectedResult = service.addMemberToCollectionIfMissing(memberCollection, ...memberArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const member: IMember = sampleWithRequiredData;
        const member2: IMember = sampleWithPartialData;
        expectedResult = service.addMemberToCollectionIfMissing([], member, member2);
        expect(expectedResult).toEqual([member, member2]);
      });

      it('should accept null and undefined values', () => {
        const member: IMember = sampleWithRequiredData;
        expectedResult = service.addMemberToCollectionIfMissing([], null, member, undefined);
        expect(expectedResult).toEqual([member]);
      });

      it('should return initial array if no Member is added', () => {
        const memberCollection: IMember[] = [sampleWithRequiredData];
        expectedResult = service.addMemberToCollectionIfMissing(memberCollection, undefined, null);
        expect(expectedResult).toEqual(memberCollection);
      });
    });

    describe('compareMember', () => {
      it('should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareMember(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('should return false if one entity is null', () => {
        const entity1 = { id: 17514 };
        const entity2 = null;

        const compareResult1 = service.compareMember(entity1, entity2);
        const compareResult2 = service.compareMember(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey differs', () => {
        const entity1 = { id: 17514 };
        const entity2 = { id: 30790 };

        const compareResult1 = service.compareMember(entity1, entity2);
        const compareResult2 = service.compareMember(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey matches', () => {
        const entity1 = { id: 17514 };
        const entity2 = { id: 17514 };

        const compareResult1 = service.compareMember(entity1, entity2);
        const compareResult2 = service.compareMember(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
