import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { AdminApiService } from '../../services/admin-api.service';
import { RequestLogItem } from '../../services/admin-api.types';

@Component({
  selector: 'app-logs-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './logs-page.component.html',
  styleUrls: ['./logs-page.component.css']
})
export class LogsPageComponent implements OnInit {
  loading = false;
  rows: RequestLogItem[] = [];

  filterForm = this.formBuilder.group({
    principalId: [''],
    statusCode: ['']
  });

  constructor(
    private formBuilder: FormBuilder,
    private adminApiService: AdminApiService
  ) {}

  ngOnInit(): void {
    this.search();
  }

  get errorResponsesCount(): number {
    return this.rows.filter((row) => row.statusCode >= 400).length;
  }

  get averageLatencyMs(): number {
    if (this.rows.length === 0) {
      return 0;
    }

    const total = this.rows.reduce((sum, row) => sum + row.latencyMs, 0);
    return Math.round(total / this.rows.length);
  }

  search(): void {
    this.loading = true;
    const values = this.filterForm.getRawValue();
    this.adminApiService.getLogs({
      principalId: values.principalId || undefined,
      statusCode: values.statusCode || undefined
    }).subscribe({
      next: (response) => {
        this.rows = response.items ?? [];
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  clearFilters(): void {
    this.filterForm.reset({
      principalId: '',
      statusCode: ''
    });
    this.search();
  }

  exportCsv(): void {
    if (this.rows.length === 0) {
      return;
    }

    const header = [
      'timestamp',
      'principalId',
      'ipAddress',
      'httpMethod',
      'path',
      'statusCode',
      'latencyMs'
    ];

    const csvRows = [
      header.join(','),
      ...this.rows.map((row) =>
        [
          this.escapeCsvValue(row.timestamp),
          this.escapeCsvValue(row.principalId),
          this.escapeCsvValue(row.ipAddress),
          this.escapeCsvValue(row.httpMethod),
          this.escapeCsvValue(row.path),
          row.statusCode.toString(),
          row.latencyMs.toString()
        ].join(',')
      )
    ];

    const csv = csvRows.join('\n');
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    const stamp = new Date().toISOString().replace(/[:.]/g, '-');
    link.href = url;
    link.download = `request-logs-${stamp}.csv`;
    link.click();
    URL.revokeObjectURL(url);
  }

  private escapeCsvValue(value: string): string {
    const escaped = value.replace(/"/g, '""');
    return `"${escaped}"`;
  }
}
