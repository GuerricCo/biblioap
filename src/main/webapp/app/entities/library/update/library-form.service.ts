import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import { ILibrary, NewLibrary } from '../library.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts ILibrary for edit and NewLibraryFormGroupInput for create.
 */
type LibraryFormGroupInput = ILibrary | PartialWithRequiredKeyOf<NewLibrary>;

type LibraryFormDefaults = Pick<NewLibrary, 'id'>;

type LibraryFormGroupContent = {
  id: FormControl<ILibrary['id'] | NewLibrary['id']>;
  name: FormControl<ILibrary['name']>;
  address: FormControl<ILibrary['address']>;
  city: FormControl<ILibrary['city']>;
  phone: FormControl<ILibrary['phone']>;
  email: FormControl<ILibrary['email']>;
};

export type LibraryFormGroup = FormGroup<LibraryFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class LibraryFormService {
  createLibraryFormGroup(library?: LibraryFormGroupInput): LibraryFormGroup {
    const libraryRawValue = {
      ...this.getFormDefaults(),
      ...(library ?? { id: null }),
    };
    return new FormGroup<LibraryFormGroupContent>({
      id: new FormControl(
        { value: libraryRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      name: new FormControl(libraryRawValue.name, {
        validators: [Validators.required],
      }),
      address: new FormControl(libraryRawValue.address),
      city: new FormControl(libraryRawValue.city),
      phone: new FormControl(libraryRawValue.phone),
      email: new FormControl(libraryRawValue.email),
    });
  }

  getLibrary(form: LibraryFormGroup): ILibrary | NewLibrary {
    return form.getRawValue() as ILibrary | NewLibrary;
  }

  resetForm(form: LibraryFormGroup, library: LibraryFormGroupInput): void {
    const libraryRawValue = { ...this.getFormDefaults(), ...library };
    form.reset({
      ...libraryRawValue,
      id: { value: libraryRawValue.id, disabled: true },
    });
  }

  private getFormDefaults(): LibraryFormDefaults {
    return {
      id: null,
    };
  }
}
