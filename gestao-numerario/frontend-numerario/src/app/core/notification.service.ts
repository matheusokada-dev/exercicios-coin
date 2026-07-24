import { Injectable, signal } from '@angular/core';

export type NotificationType = 'success' | 'error';

export interface Notification {
  type: NotificationType;
  text: string;
}

@Injectable({ providedIn: 'root' })
export class NotificationService {
  readonly notification = signal<Notification | null>(null);
  private timer?: ReturnType<typeof setTimeout>;

  success(text: string) {
    this.show({ type: 'success', text });
  }

  error(text: string) {
    this.show({ type: 'error', text });
  }

  clear() {
    if (this.timer) {
      clearTimeout(this.timer);
    }
    this.notification.set(null);
  }

  private show(notification: Notification) {
    this.clear();
    this.notification.set(notification);
    this.timer = setTimeout(() => this.notification.set(null), 4200);
  }
}
