import { Component, inject } from '@angular/core';
import { LoadingService } from '../../core/loading.service';

@Component({
  selector: 'app-loading',
  standalone: true,
  templateUrl: './loading.component.html',
  styleUrl: './loading.component.css'
})
export class LoadingComponent {
  readonly loading = inject(LoadingService);
}
