import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AdminApiService } from '../../services/admin-api.service';

@Component({
  selector: 'app-rules-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './rules-page.component.html',
  styleUrls: ['./rules-page.component.css']
})
export class RulesPageComponent implements OnInit {
  message = '';
  error = '';

  ruleForm = this.formBuilder.group({
    baseLimitPerMinute: [60, [Validators.required, Validators.min(1)]],
    throttledLimitPerMinute: [20, [Validators.required, Validators.min(1)]],
    warnThreshold: [2, [Validators.required, Validators.min(0)]],
    throttleThreshold: [4, [Validators.required, Validators.min(1)]],
    banThreshold: [7, [Validators.required, Validators.min(1)]],
    banMinutes: [15, [Validators.required, Validators.min(1)]]
  });

  banForm = this.formBuilder.group({
    principalId: ['', Validators.required],
    minutes: [15, [Validators.required, Validators.min(1)]]
  });

  unbanForm = this.formBuilder.group({
    principalId: ['', Validators.required]
  });

  constructor(
    private formBuilder: FormBuilder,
    private adminApiService: AdminApiService
  ) {}

  ngOnInit(): void {
    this.loadRules();
  }

  loadRules(): void {
    this.adminApiService.getStats().subscribe({
      next: (response) => {
        const rules = response.rules;
        this.ruleForm.patchValue({
          baseLimitPerMinute: rules.baseLimitPerMinute,
          throttledLimitPerMinute: rules.throttledLimitPerMinute,
          warnThreshold: rules.warnThreshold,
          throttleThreshold: rules.throttleThreshold,
          banThreshold: rules.banThreshold,
          banMinutes: rules.banMinutes
        });
      }
    });
  }

  saveRules(): void {
    this.message = '';
    this.error = '';
    if (this.ruleForm.invalid) {
      this.ruleForm.markAllAsTouched();
      return;
    }

    const payload = this.ruleForm.getRawValue();
    this.adminApiService.updateRules(payload).subscribe({
      next: () => {
        this.message = 'Rules updated successfully.';
      },
      error: (error: HttpErrorResponse) => {
        this.error = this.extractErrorMessage(error, 'Unable to update rules.');
      }
    });
  }

  banPrincipal(): void {
    this.message = '';
    this.error = '';
    if (this.banForm.invalid) {
      this.banForm.markAllAsTouched();
      return;
    }

    const values = this.banForm.getRawValue();
    this.adminApiService.banPrincipal(values.principalId || '', Number(values.minutes || 15)).subscribe({
      next: () => {
        this.message = 'Principal banned.';
      },
      error: (error: HttpErrorResponse) => {
        this.error = this.extractErrorMessage(error, 'Unable to ban principal.');
      }
    });
  }

  unbanPrincipal(): void {
    this.message = '';
    this.error = '';
    if (this.unbanForm.invalid) {
      this.unbanForm.markAllAsTouched();
      return;
    }

    const values = this.unbanForm.getRawValue();
    this.adminApiService.unbanPrincipal(values.principalId || '').subscribe({
      next: () => {
        this.message = 'Principal unbanned.';
      },
      error: (error: HttpErrorResponse) => {
        this.error = this.extractErrorMessage(error, 'Unable to unban principal.');
      }
    });
  }

  private extractErrorMessage(error: HttpErrorResponse, fallback: string): string {
    const message = error.error?.error;
    return typeof message === 'string' && message.trim() ? message : fallback;
  }
}
