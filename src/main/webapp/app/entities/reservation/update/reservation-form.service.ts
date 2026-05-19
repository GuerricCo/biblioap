import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import { IReservation, NewReservation } from '../reservation.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IReservation for edit and NewReservationFormGroupInput for create.
 */
type ReservationFormGroupInput = IReservation | PartialWithRequiredKeyOf<NewReservation>;

type ReservationFormDefaults = Pick<NewReservation, 'id'>;

type ReservationFormGroupContent = {
  id: FormControl<IReservation['id'] | NewReservation['id']>;
  reservationDate: FormControl<IReservation['reservationDate']>;
  status: FormControl<IReservation['status']>;
  library: FormControl<IReservation['library']>;
  book: FormControl<IReservation['book']>;
  member: FormControl<IReservation['member']>;
};

export type ReservationFormGroup = FormGroup<ReservationFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class ReservationFormService {
  createReservationFormGroup(reservation?: ReservationFormGroupInput): ReservationFormGroup {
    const reservationRawValue = {
      ...this.getFormDefaults(),
      ...(reservation ?? { id: null }),
    };
    return new FormGroup<ReservationFormGroupContent>({
      id: new FormControl(
        { value: reservationRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      reservationDate: new FormControl(reservationRawValue.reservationDate, {
        validators: [Validators.required],
      }),
      status: new FormControl(reservationRawValue.status, {
        validators: [Validators.required],
      }),
      library: new FormControl(reservationRawValue.library),
      book: new FormControl(reservationRawValue.book),
      member: new FormControl(reservationRawValue.member),
    });
  }

  getReservation(form: ReservationFormGroup): IReservation | NewReservation {
    return form.getRawValue() as IReservation | NewReservation;
  }

  resetForm(form: ReservationFormGroup, reservation: ReservationFormGroupInput): void {
    const reservationRawValue = { ...this.getFormDefaults(), ...reservation };
    form.reset({
      ...reservationRawValue,
      id: { value: reservationRawValue.id, disabled: true },
    });
  }

  private getFormDefaults(): ReservationFormDefaults {
    return {
      id: null,
    };
  }
}
