import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';

import { TranslateModule } from '@ngx-translate/core';
import { Subject, from, of } from 'rxjs';

import { ILibrary } from '../library.model';
import { LibraryService } from '../service/library.service';

import { LibraryFormService } from './library-form.service';
import { LibraryUpdate } from './library-update';

describe('Library Management Update Component', () => {
  let comp: LibraryUpdate;
  let fixture: ComponentFixture<LibraryUpdate>;
  let activatedRoute: ActivatedRoute;
  let libraryFormService: LibraryFormService;
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

    fixture = TestBed.createComponent(LibraryUpdate);
    activatedRoute = TestBed.inject(ActivatedRoute);
    libraryFormService = TestBed.inject(LibraryFormService);
    libraryService = TestBed.inject(LibraryService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should update editForm', () => {
      const library: ILibrary = { id: 16262 };

      activatedRoute.data = of({ library });
      comp.ngOnInit();

      expect(comp.library).toEqual(library);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<ILibrary>();
      const library = { id: 28830 };
      vitest.spyOn(libraryFormService, 'getLibrary').mockReturnValue(library);
      vitest.spyOn(libraryService, 'update').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ library });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(library);
      saveSubject.complete();

      // THEN
      expect(libraryFormService.getLibrary).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(libraryService.update).toHaveBeenCalledWith(expect.objectContaining(library));
      expect(comp.isSaving()).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<ILibrary>();
      const library = { id: 28830 };
      vitest.spyOn(libraryFormService, 'getLibrary').mockReturnValue({ id: null });
      vitest.spyOn(libraryService, 'create').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ library: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.next(library);
      saveSubject.complete();

      // THEN
      expect(libraryFormService.getLibrary).toHaveBeenCalled();
      expect(libraryService.create).toHaveBeenCalled();
      expect(comp.isSaving()).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<ILibrary>();
      const library = { id: 28830 };
      vitest.spyOn(libraryService, 'update').mockReturnValue(saveSubject);
      vitest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ library });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving()).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(libraryService.update).toHaveBeenCalled();
      expect(comp.isSaving()).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });
});
