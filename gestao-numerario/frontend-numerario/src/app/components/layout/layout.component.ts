import { Component } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import { LogOut, LucideAngularModule } from 'lucide-angular';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-layout',
  imports: [RouterOutlet, LucideAngularModule],
  templateUrl: './layout.component.html'
})
export class LayoutComponent {
  readonly LogOut = LogOut;

  constructor(private auth: AuthService, private router: Router) {}

  logout() {
    this.auth.sair();
    this.router.navigateByUrl('/login');
  }
}
