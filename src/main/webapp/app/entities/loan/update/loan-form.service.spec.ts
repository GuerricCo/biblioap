import { beforeEach, describe, expect, it } from 'vitest';
import { TestBed } from '@angular/core/testing';

import { sampleWithNewData, sampleWithRequiredData } from '../loan.test-samples';

import { LoanFormService } from './loan-form.service';

describe('Loan Form Service', () => {
  let service: LoanFormService;

  beforeEach(() => {
    service = TestBed.inject(LoanFormService);
  });

  describe('Service methods', () => {
    describe('createLoanFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createLoanFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            borrowDate: expect.any(Object),
            dueDate: expect.any(Object),
            returnDate: expect.any(Object),
            status: expect.any(Object),
            library: expect.any(Object),
            book: expect.any(Object),
            member: expect.any(Object),
          }),
        );
      });

      it('passing ILoan should create a new form with FormGroup', () => {
        const formGroup = service.createLoanFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            borrowDate: expect.any(Object),
            dueDate: expect.any(Object),
            returnDate: expect.any(Object),
            status: expect.any(Object),
            library: expect.any(Object),
            book: expect.any(Object),
            member: expect.any(Object),
          }),
        );
      });
    });

    describe('getLoan', () => {
      it('should return NewLoan for default Loan initial value', () => {
        const formGroup = service.createLoanFormGroup(sampleWithNewData);

        const loan = service.getLoan(formGroup);

        expect(loan).toMatchObject(sampleWithNewData);
      });

      it('should return NewLoan for empty Loan initial value', () => {
        const formGroup = service.createLoanFormGroup();

        const loan = service.getLoan(formGroup);

        expect(loan).toMatchObject({});
      });

      it('should return ILoan', () => {
        const formGroup = service.createLoanFormGroup(sampleWithRequiredData);

        const loan = service.getLoan(formGroup);

        expect(loan).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing ILoan should not enable id FormControl', () => {
        const formGroup = service.createLoanFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewLoan should disable id FormControl', () => {
        const formGroup = service.createLoanFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
