import { beforeEach, describe, expect, it } from 'vitest';
import { TestBed } from '@angular/core/testing';

import { sampleWithNewData, sampleWithRequiredData } from '../member.test-samples';

import { MemberFormService } from './member-form.service';

describe('Member Form Service', () => {
  let service: MemberFormService;

  beforeEach(() => {
    service = TestBed.inject(MemberFormService);
  });

  describe('Service methods', () => {
    describe('createMemberFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createMemberFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            firstName: expect.any(Object),
            lastName: expect.any(Object),
            email: expect.any(Object),
            phone: expect.any(Object),
            membershipDate: expect.any(Object),
            active: expect.any(Object),
            library: expect.any(Object),
          }),
        );
      });

      it('passing IMember should create a new form with FormGroup', () => {
        const formGroup = service.createMemberFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            firstName: expect.any(Object),
            lastName: expect.any(Object),
            email: expect.any(Object),
            phone: expect.any(Object),
            membershipDate: expect.any(Object),
            active: expect.any(Object),
            library: expect.any(Object),
          }),
        );
      });
    });

    describe('getMember', () => {
      it('should return NewMember for default Member initial value', () => {
        const formGroup = service.createMemberFormGroup(sampleWithNewData);

        const member = service.getMember(formGroup);

        expect(member).toMatchObject(sampleWithNewData);
      });

      it('should return NewMember for empty Member initial value', () => {
        const formGroup = service.createMemberFormGroup();

        const member = service.getMember(formGroup);

        expect(member).toMatchObject({});
      });

      it('should return IMember', () => {
        const formGroup = service.createMemberFormGroup(sampleWithRequiredData);

        const member = service.getMember(formGroup);

        expect(member).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IMember should not enable id FormControl', () => {
        const formGroup = service.createMemberFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewMember should disable id FormControl', () => {
        const formGroup = service.createMemberFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
