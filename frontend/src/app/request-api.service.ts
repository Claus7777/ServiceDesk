import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';

export type RequestStatus = 'NEW' | 'IN_PROGRESS' | 'RESOLVED';
export type Priority = 'LOW' | 'MEDIUM' | 'HIGH';
export interface ServiceRequest {
  id: string;
  title: string;
  description: string;
  priority: Priority;
  status: RequestStatus;
  createdAt: string;
  updatedAt: string;
}
export interface RequestPage {
  content: ServiceRequest[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
export interface CreateRequest { title: string; description: string; priority: Priority }

@Injectable({ providedIn: 'root' })
export class RequestApi {
  private readonly http = inject(HttpClient);
  list(status: RequestStatus | '', search: string, page = 0) {
    let params = new HttpParams().set('page', page).set('size', 10);
    if (status) params = params.set('status', status);
    if (search.trim()) params = params.set('search', search.trim());
    return this.http.get<RequestPage>('/api/requests', { params });
  }
  create(input: CreateRequest) { return this.http.post<ServiceRequest>('/api/requests', input); }
  updateStatus(id: string, status: RequestStatus) {
    return this.http.patch<ServiceRequest>(`/api/requests/${id}/status`, { status });
  }
}
