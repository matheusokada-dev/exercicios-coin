import { Component, EventEmitter, Input, Output } from '@angular/core';
import { ChevronLeft, ChevronRight, LucideAngularModule } from 'lucide-angular';

@Component({
  selector: 'app-pagination',
  standalone: true,
  imports: [LucideAngularModule],
  templateUrl: './pagination.component.html',
  styleUrl: './pagination.component.css'
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
