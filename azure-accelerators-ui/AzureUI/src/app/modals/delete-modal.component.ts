import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-delete-modal',
  templateUrl: './delete-modal.component.html',
  styleUrls: ['./delete-modal.component.css'],
})
export class DeleteModalComponent {
  @Input() delServiceName = new EventEmitter<string>();
  @Output() closeModalPopUp = new EventEmitter<boolean>();
  @Output() confirmDelete = new EventEmitter<boolean>();

  confirm() {
    this.confirmDelete.emit(true);
    this.closeModalPopUp.emit(true);
  }

  closeModal() {
    this.closeModalPopUp.emit(true);
  }
}
