import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { LoadingComponent } from './shared/loading/loading.component';
import { ToastComponent } from './shared/toast/toast.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, LoadingComponent, ToastComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  title = 'COIN Tesouraria';
}
