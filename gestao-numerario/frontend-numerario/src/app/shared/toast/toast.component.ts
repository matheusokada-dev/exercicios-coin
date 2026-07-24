import { Component, inject } from '@angular/core';
import { CheckCircle2, CircleX, LucideAngularModule, X } from 'lucide-angular';
import { NotificationService } from '../../core/notification.service';

@Component({
  selector: 'app-toast',
  standalone: true,
  imports: [LucideAngularModule],
  templateUrl: './toast.component.html',
  styleUrl: './toast.component.css'
})
export class ToastComponent {
  readonly notifications = inject(NotificationService);
  readonly CheckCircle2 = CheckCircle2;
  readonly CircleX = CircleX;
  readonly X = X;
}
