import { Component, Input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { PageBackComponent } from '../page-back/page-back.component';

export interface BreadcrumbItem {
  label: string;
  link?: string;
}

@Component({
  selector: 'app-page-header',
  standalone: true,
  imports: [RouterLink, PageBackComponent],
  template: `
    <header class="page-header">
      <div class="page-navigation">
        <nav class="crumb" aria-label="Navegação estrutural">
          <ol>
            @for (item of breadcrumbs; track item.label; let last = $last) {
              <li>
                @if (item.link && !last) {
                  <a [routerLink]="item.link">{{ item.label }}</a>
                } @else {
                  <span [attr.aria-current]="last ? 'page' : null">{{ item.label }}</span>
                }
              </li>
            }
          </ol>
        </nav>
        <app-page-back [to]="backTo" />
      </div>

      <div class="page-header__content">
        <div>
          <h1>{{ title }}</h1>
          @if (description) {
            <p>{{ description }}</p>
          }
        </div>
        <ng-content />
      </div>
    </header>
  `,
  styleUrl: './page-header.component.scss'
})
export class PageHeaderComponent {
  @Input({ required: true }) title = '';
  @Input() description = '';
  @Input() backTo = '/tesouraria';
  @Input() breadcrumbs: BreadcrumbItem[] = [];
}
