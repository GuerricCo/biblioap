import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import { ILoan, NewLoan } from '../loan.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts ILoan for edit and NewLoanFormGroupInput for create.
 */
type LoanFormGroupInput = ILoan | PartialWithRequiredKeyOf<NewLoan>;

type LoanFormDefaults = Pick<NewLoan, 'id'>;

type LoanFormGroupContent = {
  id: FormControl<ILoan['id'] | NewLoan['id']>;
  borrowDate: FormControl<ILoan['borrowDate']>;
  dueDate: FormControl<ILoan['dueDate']>;
  returnDate: FormControl<ILoan['returnDate']>;
  status: FormControl<ILoan['status']>;
  library: FormControl<ILoan['library']>;
  book: FormControl<ILoan['book']>;
  member: FormControl<ILoan['member']>;
};

export type LoanFormGroup = FormGroup<LoanFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class LoanFormService {
  createLoanFormGroup(loan?: LoanFormGroupInput): LoanFormGroup {
    const loanRawValue = {
      ...this.getFormDefaults(),
      ...(loan ?? { id: null }),
    };
    return new FormGroup<LoanFormGroupContent>({
      id: new FormControl(
        { value: loanRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      borrowDate: new FormControl(loanRawValue.borrowDate, {
        validators: [Validators.required],
      }),
      dueDate: new FormControl(loanRawValue.dueDate, {
        validators: [Validators.required],
      }),
      returnDate: new FormControl(loanRawValue.returnDate),
      status: new FormControl(loanRawValue.status, {
        validators: [Validators.required],
      }),
      library: new FormControl(loanRawValue.library),
      book: new FormControl(loanRawValue.book),
      member: new FormControl(loanRawValue.member),
    });
  }

  getLoan(form: LoanFormGroup): ILoan | NewLoan {
    return form.getRawValue() as ILoan | NewLoan;
  }

  resetForm(form: LoanFormGroup, loan: LoanFormGroupInput): void {
    const loanRawValue = { ...this.getFormDefaults(), ...loan };
    form.reset({
      ...loanRawValue,
      id: { value: loanRawValue.id, disabled: true },
    });
  }

  private getFormDefaults(): LoanFormDefaults {
    return {
      id: null,
    };
  }
}
