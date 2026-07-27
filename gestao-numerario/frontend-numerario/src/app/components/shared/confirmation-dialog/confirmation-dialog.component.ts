import {
  Component,
  ElementRef,
  EventEmitter,
  Input,
  Output,
  ViewChild
} from '@angular/core';

@Component({
  selector: 'app-confirmation-dialog',
  standalone: true,
  templateUrl: './confirmation-dialog.component.html',
  styleUrl: './confirmation-dialog.component.css'
})
export class ConfirmationDialogComponent {
  @ViewChild('dialog', { static: true })
  private readonly dialog!: ElementRef<HTMLDialogElement>;

  @Input() title = 'Confirmar ação';
  @Input() message = '';
  @Input() confirmLabel = 'Confirmar';
  @Input() danger = false;
  @Output() readonly confirmed = new EventEmitter<void>();
  @Output() readonly cancelled = new EventEmitter<void>();

  private confirmedByAction = false;

  open() {
    this.confirmedByAction = false;
    this.dialog.nativeElement.showModal();
  }

  close() {
    this.dialog.nativeElement.close();
  }

  confirm() {
    this.confirmedByAction = true;
    this.confirmed.emit();
    this.dialog.nativeElement.close();
  }

  handleClose() {
    if (!this.confirmedByAction) {
      this.cancelled.emit();
    }
  }
}
