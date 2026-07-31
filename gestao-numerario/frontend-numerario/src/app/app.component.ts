import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { LoadingComponent } from './components/shared/loading/loading.component';
import { ToastComponent } from './components/shared/toast/toast.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, LoadingComponent, ToastComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'COIN Tesouraria';
}
