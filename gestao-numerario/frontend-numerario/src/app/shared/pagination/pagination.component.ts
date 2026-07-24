import { Component, EventEmitter, Input, Output } from '@angular/core';
import { ChevronLeft, ChevronRight, LucideAngularModule } from 'lucide-angular';

@Component({
  selector: 'app-pagination',
  standalone: true,
  imports: [LucideAngularModule],
  template: `
    @if (totalPages > 1) {
      <nav class="brad-pagination" aria-label="Paginação">
        <button
          type="button"
          class="brad-pagination__control"
          [disabled]="page === 0"
          aria-label="Página anterior"
          (click)="change(page - 1)">
          <lucide-icon [img]="ChevronLeft" [size]="17" aria-hidden="true" />
          <span>Anterior</span>
        </button>

        <div class="brad-pagination__pages">
          @for (pageNumber of visiblePages; track pageNumber) {
            <button
              type="button"
              class="brad-pagination__page"
              [class.brad-pagination__page--active]="pageNumber === page"
              [attr.aria-current]="pageNumber === page ? 'page' : null"
              [attr.aria-label]="'Página ' + (pageNumber + 1)"
              (click)="change(pageNumber)">
              {{ pageNumber + 1 }}
            </button>
          }
        </div>

        <span class="brad-pagination__compact">
          Página {{ page + 1 }} de {{ totalPages }}
        </span>

        <button
          type="button"
          class="brad-pagination__control"
          [disabled]="page + 1 >= totalPages"
          aria-label="Próxima página"
          (click)="change(page + 1)">
          <span>Próxima</span>
          <lucide-icon [img]="ChevronRight" [size]="17" aria-hidden="true" />
        </button>
      </nav>
    }
  `,
  styleUrl: './pagination.component.scss'
})
export class PaginationComponent {
  @Input() page = 0;
  @Input() totalPages = 1;
  @Input() countNumbersStart = 7;
  @Output() readonly pageChange = new EventEmitter<number>();

  readonly ChevronLeft = ChevronLeft;
  readonly ChevronRight = ChevronRight;

  get visiblePages(): number[] {
    const count = Math.max(1, Math.min(this.countNumbersStart, this.totalPages));
    const half = Math.floor(count / 2);
    const start = Math.max(0, Math.min(this.page - half, this.totalPages - count));
    return Array.from({ length: count }, (_, index) => start + index);
  }

  change(targetPage: number) {
    if (targetPage < 0 || targetPage >= this.totalPages || targetPage === this.page) {
      return;
    }
    this.pageChange.emit(targetPage);
  }
}
