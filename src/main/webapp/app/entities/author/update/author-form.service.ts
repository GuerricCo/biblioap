import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import { IAuthor, NewAuthor } from '../author.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IAuthor for edit and NewAuthorFormGroupInput for create.
 */
type AuthorFormGroupInput = IAuthor | PartialWithRequiredKeyOf<NewAuthor>;

type AuthorFormDefaults = Pick<NewAuthor, 'id' | 'bookses'>;

type AuthorFormGroupContent = {
  id: FormControl<IAuthor['id'] | NewAuthor['id']>;
  firstName: FormControl<IAuthor['firstName']>;
  lastName: FormControl<IAuthor['lastName']>;
  birthDate: FormControl<IAuthor['birthDate']>;
  nationality: FormControl<IAuthor['nationality']>;
  biography: FormControl<IAuthor['biography']>;
  bookses: FormControl<IAuthor['bookses']>;
  library: FormControl<IAuthor['library']>;
};

export type AuthorFormGroup = FormGroup<AuthorFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class AuthorFormService {
  createAuthorFormGroup(author?: AuthorFormGroupInput): AuthorFormGroup {
    const authorRawValue = {
      ...this.getFormDefaults(),
      ...(author ?? { id: null }),
    };
    return new FormGroup<AuthorFormGroupContent>({
      id: new FormControl(
        { value: authorRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      firstName: new FormControl(authorRawValue.firstName, {
        validators: [Validators.required],
      }),
      lastName: new FormControl(authorRawValue.lastName, {
        validators: [Validators.required],
      }),
      birthDate: new FormControl(authorRawValue.birthDate),
      nationality: new FormControl(authorRawValue.nationality),
      biography: new FormControl(authorRawValue.biography),
      bookses: new FormControl(authorRawValue.bookses ?? []),
      library: new FormControl(authorRawValue.library),
    });
  }

  getAuthor(form: AuthorFormGroup): IAuthor | NewAuthor {
    return form.getRawValue() as IAuthor | NewAuthor;
  }

  resetForm(form: AuthorFormGroup, author: AuthorFormGroupInput): void {
    const authorRawValue = { ...this.getFormDefaults(), ...author };
    form.reset({
      ...authorRawValue,
      id: { value: authorRawValue.id, disabled: true },
    });
  }

  private getFormDefaults(): AuthorFormDefaults {
    return {
      id: null,
      bookses: [],
    };
  }
}
