import { Component, Input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ArrowLeft, LucideAngularModule } from 'lucide-angular';

@Component({
  selector: 'app-page-back',
  standalone: true,
  imports: [RouterLink, LucideAngularModule],
  template: `
    <a class="page-back" [routerLink]="to">
      <lucide-icon [img]="ArrowLeft" [size]="17" aria-hidden="true" />
      Voltar
    </a>
  `
})
export class PageBackComponent {
  @Input({ required: true }) to = '/menu';
  readonly ArrowLeft = ArrowLeft;
}
