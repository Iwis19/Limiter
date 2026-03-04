import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  AdminStats,
  BanActionResponse,
  IncidentItem,
  ListResponse,
  LogsFilter,
  RequestLogItem,
  RuleConfig,
  RulesUpdatePayload,
  UnbanActionResponse
} from './admin-api.types';

@Injectable({ providedIn: 'root' })
export class AdminApiService {
  constructor(private http: HttpClient) {}

  getStats(): Observable<AdminStats> {
    return this.http.get<AdminStats>('/admin/stats');
  }

  getLogs(filters: LogsFilter): Observable<ListResponse<RequestLogItem>> {
    let params = new HttpParams();
    if (filters.principalId) {
      params = params.set('principalId', filters.principalId);
    }
    if (filters.statusCode) {
      params = params.set('statusCode', filters.statusCode);
    }
    return this.http.get<ListResponse<RequestLogItem>>('/admin/logs', { params });
  }

  getIncidents(activeOnly: boolean): Observable<ListResponse<IncidentItem>> {
    return this.http.get<ListResponse<IncidentItem>>('/admin/incidents', { params: { activeOnly } });
  }

  updateRules(payload: RulesUpdatePayload): Observable<RuleConfig> {
    return this.http.put<RuleConfig>('/admin/rules', payload);
  }

  banPrincipal(principalId: string, minutes: number): Observable<BanActionResponse> {
    return this.http.post<BanActionResponse>('/admin/actions/ban', { principalId, minutes });
  }

  unbanPrincipal(principalId: string): Observable<UnbanActionResponse> {
    return this.http.post<UnbanActionResponse>('/admin/actions/unban', { principalId });
  }
}
