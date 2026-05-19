import { HttpClient, HttpResponse, httpResource } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';

import dayjs from 'dayjs/esm';
import { Observable, map } from 'rxjs';

import { DATE_FORMAT } from 'app/config/input.constants';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { isPresent } from 'app/core/util/operators';
import { IMember, NewMember } from '../member.model';

export type PartialUpdateMember = Partial<IMember> & Pick<IMember, 'id'>;

type RestOf<T extends IMember | NewMember> = Omit<T, 'membershipDate'> & {
  membershipDate?: string | null;
};

export type RestMember = RestOf<IMember>;

export type NewRestMember = RestOf<NewMember>;

export type PartialUpdateRestMember = RestOf<PartialUpdateMember>;

@Injectable()
export class MembersService {
  readonly membersParams = signal<Record<string, string | number | boolean | readonly (string | number | boolean)[]> | undefined>(
    undefined,
  );
  readonly membersResource = httpResource<RestMember[]>(() => {
    const params = this.membersParams();
    if (!params) {
      return undefined;
    }
    return { url: this.resourceUrl, params };
  });
  /**
   * This signal holds the list of member that have been fetched. It is updated when the membersResource emits a new value.
   * In case of error while fetching the members, the signal is set to an empty array.
   */
  readonly members = computed(() =>
    (this.membersResource.hasValue() ? this.membersResource.value() : []).map(item => this.convertValueFromServer(item)),
  );
  protected readonly applicationConfigService = inject(ApplicationConfigService);
  protected readonly resourceUrl = this.applicationConfigService.getEndpointFor('api/members');

  protected convertValueFromServer(restMember: RestMember): IMember {
    return {
      ...restMember,
      membershipDate: restMember.membershipDate ? dayjs(restMember.membershipDate) : undefined,
    };
  }
}

@Injectable({ providedIn: 'root' })
export class MemberService extends MembersService {
  protected readonly http = inject(HttpClient);

  create(member: NewMember): Observable<IMember> {
    const copy = this.convertValueFromClient(member);
    return this.http.post<RestMember>(this.resourceUrl, copy).pipe(map(res => this.convertResponseFromServer(res)));
  }

  update(member: IMember): Observable<IMember> {
    const copy = this.convertValueFromClient(member);
    return this.http
      .put<RestMember>(`${this.resourceUrl}/${encodeURIComponent(this.getMemberIdentifier(member))}`, copy)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  partialUpdate(member: PartialUpdateMember): Observable<IMember> {
    const copy = this.convertValueFromClient(member);
    return this.http
      .patch<RestMember>(`${this.resourceUrl}/${encodeURIComponent(this.getMemberIdentifier(member))}`, copy)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  find(id: number): Observable<IMember> {
    return this.http.get<RestMember>(`${this.resourceUrl}/${encodeURIComponent(id)}`).pipe(map(res => this.convertResponseFromServer(res)));
  }

  query(req?: any): Observable<HttpResponse<IMember[]>> {
    const options = createRequestOption(req);
    return this.http
      .get<RestMember[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map(res => res.clone({ body: this.convertResponseArrayFromServer(res.body!) })));
  }

  delete(id: number): Observable<undefined> {
    return this.http.delete<undefined>(`${this.resourceUrl}/${encodeURIComponent(id)}`);
  }

  getMemberIdentifier(member: Pick<IMember, 'id'>): number {
    return member.id;
  }

  compareMember(o1: Pick<IMember, 'id'> | null, o2: Pick<IMember, 'id'> | null): boolean {
    return o1 && o2 ? this.getMemberIdentifier(o1) === this.getMemberIdentifier(o2) : o1 === o2;
  }

  addMemberToCollectionIfMissing<Type extends Pick<IMember, 'id'>>(
    memberCollection: Type[],
    ...membersToCheck: (Type | null | undefined)[]
  ): Type[] {
    const members: Type[] = membersToCheck.filter(isPresent);
    if (members.length > 0) {
      const memberCollectionIdentifiers = memberCollection.map(memberItem => this.getMemberIdentifier(memberItem));
      const membersToAdd = members.filter(memberItem => {
        const memberIdentifier = this.getMemberIdentifier(memberItem);
        if (memberCollectionIdentifiers.includes(memberIdentifier)) {
          return false;
        }
        memberCollectionIdentifiers.push(memberIdentifier);
        return true;
      });
      return [...membersToAdd, ...memberCollection];
    }
    return memberCollection;
  }

  protected convertValueFromClient<T extends IMember | NewMember | PartialUpdateMember>(member: T): RestOf<T> {
    return {
      ...member,
      membershipDate: member.membershipDate?.format(DATE_FORMAT) ?? null,
    };
  }

  protected convertResponseFromServer(res: RestMember): IMember {
    return this.convertValueFromServer(res);
  }

  protected convertResponseArrayFromServer(res: RestMember[]): IMember[] {
    return res.map(item => this.convertValueFromServer(item));
  }
}
