import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { HttpResponse } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';

import { TranslateModule } from '@ngx-translate/core';
import { Subject, from, of } from 'rxjs';

import { ILibrary } from 'app/entities/library/library.model';
import { LibraryService } from 'app/entities/library/service/library.service';
import { IMember } from '../member.model';
import { MemberService } from '../service/member.service';

import { MemberFormService } from './member-form.service';
import { MemberUpdate } from './member-update';

describe('Member Management Update Component', () => {
  let comp: MemberUpdate;
  let fixture: ComponentFixture<MemberUpdate>;
  let activatedRoute: ActivatedRoute;
  let memberFormService: MemberFormService;
  let memberService: MemberService;
  let libraryService: LibraryService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [TranslateModule.forRoot()],
      providers: [
        provideHttpClientTesting(),
        {
          provide: ActivatedRoute,
          useValue: {
            params: from([{}]),
          },
        },
      ],
    });

    fixture = TestBed.createComponent(MemberUpdate);
    activatedRoute = TestBed.inject(ActivatedRoute);
    memberFormService = TestBed.inject(MemberFormService);
    memberService = TestBed.inject(MemberService);
    libraryService = TestBed.inject(LibraryService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should call Library query and add missing value', () => {
      const member: IMember = { id: 30790 };
      const library: ILibrary = { id: 28830 };
      member.library = library;

      const libraryCollection: ILibrary[] = [{ id: 28830 }];
      vitest.spyOn(libraryService, 'query').mockReturnValue(of(new HttpResponse({ body: libraryCollection })));
      const additionalLibraries = [library];
      const expectedCollection: ILibrary[] = [...additionalLibraries, ...libraryCollection];
      vitest.spyOn(libraryService, 'addLibraryToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ member });
      comp.ngOnInit();

      expect(libraryService.query).toHaveBeenCalled();
      expect(libraryService.addLibraryToCollectionIfMissing).toHaveBeenCalledWith(
        libraryCollection,
        ...additionalLibraries.map(i => expect.objectContaining(i) as typeof i),
      );
      expect(comp.librariesSharedCollection()).toEqual(expectedCollection);
    });

    it('should update editForm', () => {
      const member: IMember = { id: 30790 };
      const library: ILibrary = { id: 28830 };
      member.library = library;

      activatedRoute.data = of({ member });
      comp.ngOnInit();

      expect(comp.librariesSharedCollection()).toContainEqual(library);
      expect(comp.member).toEqual(member);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<IMember>();
      const member = { id: 17514 };
      vitest.spyOn(memberFormService, 'getMember').mockReturnValue(member);
      vitest.spyOn(memberService, 'update').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ member });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(member);
      saveSubject.complete();

      // THEN
      expect(memberFormService.getMember).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(memberService.update).toHaveBeenCalledWith(expect.objectContaining(member));
      expect(comp.isSaving()).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<IMember>();
      const member = { id: 17514 };
      vitest.spyOn(memberFormService, 'getMember').mockReturnValue({ id: null });
      vitest.spyOn(memberService, 'create').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ member: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(member);
      saveSubject.complete();

      // THEN
      expect(memberFormService.getMember).toHaveBeenCalled();
      expect(memberService.create).toHaveBeenCalled();
      expect(comp.isSaving()).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<IMember>();
      const member = { id: 17514 };
      vitest.spyOn(memberService, 'update').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ member });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(memberService.update).toHaveBeenCalled();
      expect(comp.isSaving()).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Compare relationships', () => {
    describe('compareLibrary', () => {
      it('should forward to libraryService', () => {
        const entity = { id: 28830 };
        const entity2 = { id: 16262 };
        vitest.spyOn(libraryService, 'compareLibrary');
        comp.compareLibrary(entity, entity2);
        expect(libraryService.compareLibrary).toHaveBeenCalledWith(entity, entity2);
      });
    });
  });
});
