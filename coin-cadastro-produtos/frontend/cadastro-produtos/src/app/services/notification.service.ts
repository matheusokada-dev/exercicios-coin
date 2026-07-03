import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export type NotificationType = 'success' | 'error';

export type NotificationMessage = {
  text: string;
  type: NotificationType;
};

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private readonly notificationSubject = new BehaviorSubject<NotificationMessage | null>(null);
  readonly notification$ = this.notificationSubject.asObservable();
  private timeoutId: ReturnType<typeof setTimeout> | null = null;

  success(text: string): void {
    this.show({ text, type: 'success' });
  }

  error(text: string): void {
    this.show({ text, type: 'error' });
  }

  clear(): void {
    this.notificationSubject.next(null);
  }

  private show(notification: NotificationMessage): void {
    if (this.timeoutId) {
      clearTimeout(this.timeoutId);
    }

    this.notificationSubject.next(notification);
    this.timeoutId = setTimeout(() => this.clear(), 1500);
  }
}
