import { beforeEach, describe, expect, it } from 'vitest';
import { TestBed } from '@angular/core/testing';

import { sampleWithNewData, sampleWithRequiredData } from '../library.test-samples';

import { LibraryFormService } from './library-form.service';

describe('Library Form Service', () => {
  let service: LibraryFormService;

  beforeEach(() => {
    service = TestBed.inject(LibraryFormService);
  });

  describe('Service methods', () => {
    describe('createLibraryFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createLibraryFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            name: expect.any(Object),
            address: expect.any(Object),
            city: expect.any(Object),
            phone: expect.any(Object),
            email: expect.any(Object),
          }),
        );
      });

      it('passing ILibrary should create a new form with FormGroup', () => {
        const formGroup = service.createLibraryFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            name: expect.any(Object),
            address: expect.any(Object),
            city: expect.any(Object),
            phone: expect.any(Object),
            email: expect.any(Object),
          }),
        );
      });
    });

    describe('getLibrary', () => {
      it('should return NewLibrary for default Library initial value', () => {
        const formGroup = service.createLibraryFormGroup(sampleWithNewData);

        const library = service.getLibrary(formGroup);

        expect(library).toMatchObject(sampleWithNewData);
      });

      it('should return NewLibrary for empty Library initial value', () => {
        const formGroup = service.createLibraryFormGroup();

        const library = service.getLibrary(formGroup);

        expect(library).toMatchObject({});
      });

      it('should return ILibrary', () => {
        const formGroup = service.createLibraryFormGroup(sampleWithRequiredData);

        const library = service.getLibrary(formGroup);

        expect(library).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing ILibrary should not enable id FormControl', () => {
        const formGroup = service.createLibraryFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewLibrary should disable id FormControl', () => {
        const formGroup = service.createLibraryFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
