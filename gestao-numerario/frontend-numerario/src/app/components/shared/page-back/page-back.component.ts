import { Component, Input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ArrowLeft, LucideAngularModule } from 'lucide-angular';

@Component({
  selector: 'app-page-back',
  standalone: true,
  imports: [RouterLink, LucideAngularModule],
  templateUrl: './page-back.component.html'
})
export class PageBackComponent {
  @Input({ required: true }) to = '/menu';
  readonly ArrowLeft = ArrowLeft;
}
