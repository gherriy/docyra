import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DocumentReviewPage } from './document-review-page';

describe('DocumentReviewPage', () => {
  let component: DocumentReviewPage;
  let fixture: ComponentFixture<DocumentReviewPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DocumentReviewPage],
    }).compileComponents();

    fixture = TestBed.createComponent(DocumentReviewPage);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
