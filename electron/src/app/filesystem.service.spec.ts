import { TestBed } from '@angular/core/testing';

import { FilesystemService } from './services/filesystem.service';

describe('FilesystemService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: FilesystemService = TestBed.get(FilesystemService);
    expect(service).toBeTruthy();
  });
});
