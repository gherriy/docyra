import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ValidationQueuePage } from './validation-queue-page';

describe('ValidationQueuePage', () => {
  let component: ValidationQueuePage;
  let fixture: ComponentFixture<ValidationQueuePage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ValidationQueuePage],
    }).compileComponents();

    fixture = TestBed.createComponent(ValidationQueuePage);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
