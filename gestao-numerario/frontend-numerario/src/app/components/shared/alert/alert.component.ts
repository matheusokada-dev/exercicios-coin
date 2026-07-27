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
  templateUrl: './alert.component.html',
  styleUrl: './alert.component.css'
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
