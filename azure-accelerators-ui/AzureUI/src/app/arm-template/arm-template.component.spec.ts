import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ArmTemplateComponent } from './arm-template.component';

describe('ArmTemplateComponent', () => {
  let component: ArmTemplateComponent;
  let fixture: ComponentFixture<ArmTemplateComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ArmTemplateComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ArmTemplateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
