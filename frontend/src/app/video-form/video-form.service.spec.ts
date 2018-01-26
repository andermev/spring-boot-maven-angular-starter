import { TestBed, inject } from '@angular/core/testing';

import { VideoFormService } from './video-form.service';

describe('VideoFormService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [VideoFormService]
    });
  });

  it('should be created', inject([VideoFormService], (service: VideoFormService) => {
    expect(service).toBeTruthy();
  }));
});
