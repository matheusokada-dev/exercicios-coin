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
  templateUrl: './page-header.component.html',
  styleUrl: './page-header.component.css'
})
export class PageHeaderComponent {
  @Input({ required: true }) title = '';
  @Input() description = '';
  @Input() backTo = '/tesouraria';
  @Input() breadcrumbs: BreadcrumbItem[] = [];
}
