import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-delete-confirmation-modal',
  imports: [],
  templateUrl: './delete-confirmation-modal.component.html',
  styleUrl: './delete-confirmation-modal.component.css',
})
export class DeleteConfirmationModalComponent {
  @Input() title?: string;
  @Input() message?: string;
  @Output() confirmed = new EventEmitter<void>();
  @Output() cancelled = new EventEmitter<void>();

  confirm() {
    this.confirmed.emit();
  }
  cancel() {
    this.cancelled.emit();
  }
}
