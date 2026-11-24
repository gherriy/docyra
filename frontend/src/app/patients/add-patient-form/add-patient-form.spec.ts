import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AddPatientForm } from './add-patient-form';

describe('AddPatientForm', () => {
  let component: AddPatientForm;
  let fixture: ComponentFixture<AddPatientForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddPatientForm],
    }).compileComponents();

    fixture = TestBed.createComponent(AddPatientForm);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
