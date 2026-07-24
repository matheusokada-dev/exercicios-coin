import { Component, Input } from '@angular/core';
import {
  CheckCircle2,
  CircleAlert,
  CircleX,
  Info,
  LucideAngularModule
} from 'lucide-angular';

export type AlertType = 'info' | 'success' | 'warning' | 'error';

@Component({
  selector: 'app-alert',
  standalone: true,
  imports: [LucideAngularModule],
  template: `
    <section
      class="brad-alert"
      [class]="'brad-alert brad-alert--' + type"
      [attr.role]="type === 'error' ? 'alert' : 'status'"
      [attr.aria-live]="type === 'error' ? 'assertive' : 'polite'">
      <lucide-icon
        class="brad-alert__icon"
        [img]="icon"
        [size]="20"
        aria-hidden="true" />
      <div class="brad-alert__content">
        @if (title) {
          <strong>{{ title }}</strong>
        }
        <p>{{ message }}</p>
      </div>
      <ng-content />
    </section>
  `,
  styleUrl: './alert.component.scss'
})
export class AlertComponent {
  @Input() type: AlertType = 'info';
  @Input() title = '';
  @Input({ required: true }) message = '';

  get icon() {
    switch (this.type) {
      case 'success':
        return CheckCircle2;
      case 'warning':
        return CircleAlert;
      case 'error':
        return CircleX;
      default:
        return Info;
    }
  }
}
