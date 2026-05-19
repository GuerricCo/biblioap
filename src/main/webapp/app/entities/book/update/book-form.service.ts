import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import { IBook, NewBook } from '../book.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IBook for edit and NewBookFormGroupInput for create.
 */
type BookFormGroupInput = IBook | PartialWithRequiredKeyOf<NewBook>;

type BookFormDefaults = Pick<NewBook, 'id' | 'available' | 'authorses'>;

type BookFormGroupContent = {
  id: FormControl<IBook['id'] | NewBook['id']>;
  title: FormControl<IBook['title']>;
  isbn: FormControl<IBook['isbn']>;
  publicationDate: FormControl<IBook['publicationDate']>;
  description: FormControl<IBook['description']>;
  language: FormControl<IBook['language']>;
  pages: FormControl<IBook['pages']>;
  available: FormControl<IBook['available']>;
  totalCopies: FormControl<IBook['totalCopies']>;
  availableCopies: FormControl<IBook['availableCopies']>;
  library: FormControl<IBook['library']>;
  category: FormControl<IBook['category']>;
  authorses: FormControl<IBook['authorses']>;
};

export type BookFormGroup = FormGroup<BookFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class BookFormService {
  createBookFormGroup(book?: BookFormGroupInput): BookFormGroup {
    const bookRawValue = {
      ...this.getFormDefaults(),
      ...(book ?? { id: null }),
    };
    return new FormGroup<BookFormGroupContent>({
      id: new FormControl(
        { value: bookRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      title: new FormControl(bookRawValue.title, {
        validators: [Validators.required],
      }),
      isbn: new FormControl(bookRawValue.isbn, {
        validators: [Validators.required],
      }),
      publicationDate: new FormControl(bookRawValue.publicationDate),
      description: new FormControl(bookRawValue.description),
      language: new FormControl(bookRawValue.language),
      pages: new FormControl(bookRawValue.pages),
      available: new FormControl(bookRawValue.available, {
        validators: [Validators.required],
      }),
      totalCopies: new FormControl(bookRawValue.totalCopies, {
        validators: [Validators.required, Validators.min(0)],
      }),
      availableCopies: new FormControl(bookRawValue.availableCopies, {
        validators: [Validators.required, Validators.min(0)],
      }),
      library: new FormControl(bookRawValue.library),
      category: new FormControl(bookRawValue.category),
      authorses: new FormControl(bookRawValue.authorses ?? []),
    });
  }

  getBook(form: BookFormGroup): IBook | NewBook {
    return form.getRawValue() as IBook | NewBook;
  }

  resetForm(form: BookFormGroup, book: BookFormGroupInput): void {
    const bookRawValue = { ...this.getFormDefaults(), ...book };
    form.reset({
      ...bookRawValue,
      id: { value: bookRawValue.id, disabled: true },
    });
  }

  private getFormDefaults(): BookFormDefaults {
    return {
      id: null,
      available: false,
      authorses: [],
    };
  }
}
