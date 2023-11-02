import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AzureDevopsComponent } from './azure-devops.component';

describe('AzureDevopsComponent', () => {
  let component: AzureDevopsComponent;
  let fixture: ComponentFixture<AzureDevopsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AzureDevopsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AzureDevopsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
