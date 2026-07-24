import { Component } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import { LogOut, LucideAngularModule } from 'lucide-angular';
import { AuthService } from '../core/auth.service';

@Component({
  selector: 'app-layout',
  imports: [RouterOutlet, LucideAngularModule],
  template: `
    <header class="app-header">
      <button class="logout-button" type="button" (click)="logout()" title="Sair">
        <lucide-icon [img]="LogOut" [size]="18" aria-hidden="true" />
        <span>Sair</span>
      </button>
    </header>
    <main class="page"><router-outlet /></main>
  `
})
export class LayoutComponent {
  readonly LogOut = LogOut;

  constructor(private auth: AuthService, private router: Router) {}

  logout() {
    this.auth.sair();
    this.router.navigateByUrl('/login');
  }
}
