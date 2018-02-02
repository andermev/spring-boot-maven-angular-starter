import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { S3DownloadComponent } from './s3-download.component';

describe('S3DownloadComponent', () => {
  let component: S3DownloadComponent;
  let fixture: ComponentFixture<S3DownloadComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ S3DownloadComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(S3DownloadComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
