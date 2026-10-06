import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AppComponent } from './app.component';

describe('AppComponent', () => {
  let http: HttpTestingController;
  beforeEach(() => {
    TestBed.configureTestingModule({ imports: [AppComponent], providers: [provideHttpClient(), provideHttpClientTesting()] });
    http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => http.verify());

  function setup() {
    const fixture = TestBed.createComponent(AppComponent);
    http.expectOne(req => req.url === '/api/requests').flush({ content: [], page: 0, size: 10, totalElements: 0, totalPages: 0 });
    fixture.detectChanges();
    return fixture;
  }

  it('renders an empty state and rejects whitespace-only requests', () => {
    const fixture = setup();
    fixture.componentInstance.form.setValue({ title: '  ', description: 'Test', priority: 'LOW' });
    fixture.componentInstance.create();
    http.expectNone('/api/requests');
    expect(fixture.nativeElement.textContent).toContain('No requests here yet.');
    expect(fixture.componentInstance.form.controls.title.touched).toBe(true);
  });

  it('prevents duplicate submissions while a save is in flight', () => {
    const fixture = setup();
    const component = fixture.componentInstance;
    component.form.setValue({ title: 'Printer', description: 'Offline', priority: 'HIGH' });
    component.create();
    component.create();
    const requests = http.match(req => req.url === '/api/requests' && req.method === 'POST');
    expect(requests.length).toBe(1);
    requests[0].flush({ id: 'test' });
    http.expectOne(req => req.url === '/api/requests' && req.method === 'GET')
      .flush({ content: [], page: 0, size: 10, totalElements: 0, totalPages: 0 });
    expect(component.saving()).toBe(false);
    expect(component.form.controls.title.value).toBe('');
  });

  it('keeps the draft and shows feedback after a failed save', () => {
    const fixture = setup();
    const component = fixture.componentInstance;
    component.form.setValue({ title: 'Printer', description: 'Offline', priority: 'HIGH' });
    component.create();
    http.expectOne('/api/requests').flush({}, { status: 500, statusText: 'Server Error' });
    expect(component.form.controls.title.value).toBe('Printer');
    expect(component.error()).toContain('Your draft has been kept');
    expect(component.saving()).toBe(false);
  });
});
