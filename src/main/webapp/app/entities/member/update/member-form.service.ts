import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import { IMember, NewMember } from '../member.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IMember for edit and NewMemberFormGroupInput for create.
 */
type MemberFormGroupInput = IMember | PartialWithRequiredKeyOf<NewMember>;

type MemberFormDefaults = Pick<NewMember, 'id' | 'active'>;

type MemberFormGroupContent = {
  id: FormControl<IMember['id'] | NewMember['id']>;
  firstName: FormControl<IMember['firstName']>;
  lastName: FormControl<IMember['lastName']>;
  email: FormControl<IMember['email']>;
  phone: FormControl<IMember['phone']>;
  membershipDate: FormControl<IMember['membershipDate']>;
  active: FormControl<IMember['active']>;
  library: FormControl<IMember['library']>;
};

export type MemberFormGroup = FormGroup<MemberFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class MemberFormService {
  createMemberFormGroup(member?: MemberFormGroupInput): MemberFormGroup {
    const memberRawValue = {
      ...this.getFormDefaults(),
      ...(member ?? { id: null }),
    };
    return new FormGroup<MemberFormGroupContent>({
      id: new FormControl(
        { value: memberRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      firstName: new FormControl(memberRawValue.firstName, {
        validators: [Validators.required],
      }),
      lastName: new FormControl(memberRawValue.lastName, {
        validators: [Validators.required],
      }),
      email: new FormControl(memberRawValue.email, {
        validators: [Validators.required],
      }),
      phone: new FormControl(memberRawValue.phone),
      membershipDate: new FormControl(memberRawValue.membershipDate),
      active: new FormControl(memberRawValue.active, {
        validators: [Validators.required],
      }),
      library: new FormControl(memberRawValue.library),
    });
  }

  getMember(form: MemberFormGroup): IMember | NewMember {
    return form.getRawValue() as IMember | NewMember;
  }

  resetForm(form: MemberFormGroup, member: MemberFormGroupInput): void {
    const memberRawValue = { ...this.getFormDefaults(), ...member };
    form.reset({
      ...memberRawValue,
      id: { value: memberRawValue.id, disabled: true },
    });
  }

  private getFormDefaults(): MemberFormDefaults {
    return {
      id: null,
      active: false,
    };
  }
}
