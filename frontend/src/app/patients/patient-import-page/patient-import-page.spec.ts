import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PatientImportPage } from './patient-import-page';

describe('PatientImportPage', () => {
  let component: PatientImportPage;
  let fixture: ComponentFixture<PatientImportPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PatientImportPage],
    }).compileComponents();

    fixture = TestBed.createComponent(PatientImportPage);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
