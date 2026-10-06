import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { RequestApi } from './request-api.service';

describe('RequestApi', () => {
  let api: RequestApi;
  let http: HttpTestingController;
  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    api = TestBed.inject(RequestApi);
    http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => http.verify());

  it('sends trimmed search, status and bounded pagination', () => {
    api.list('NEW', '  printer  ', 2).subscribe();
    const request = http.expectOne(req => req.url === '/api/requests');
    expect(request.request.params.get('search')).toBe('printer');
    expect(request.request.params.get('status')).toBe('NEW');
    expect(request.request.params.get('page')).toBe('2');
    expect(request.request.params.get('size')).toBe('10');
    request.flush({ content: [], page: 2, size: 10, totalElements: 0, totalPages: 0 });
  });

  it('does not send empty filters', () => {
    api.list('', '  ').subscribe();
    const request = http.expectOne(req => req.url === '/api/requests');
    expect(request.request.params.has('status')).toBe(false);
    expect(request.request.params.has('search')).toBe(false);
    request.flush({ content: [], page: 0, size: 10, totalElements: 0, totalPages: 0 });
  });

  it('updates status using PATCH', () => {
    api.updateStatus('request-id', 'RESOLVED').subscribe();
    const request = http.expectOne('/api/requests/request-id/status');
    expect(request.request.method).toBe('PATCH');
    expect(request.request.body).toEqual({ status: 'RESOLVED' });
    request.flush({});
  });
});
