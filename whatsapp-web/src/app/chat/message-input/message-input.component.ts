import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
	selector: 'app-message-input',
	standalone: true,
	imports: [CommonModule, FormsModule],
	templateUrl: './message-input.component.html',
	styleUrls: ['./message-input.component.css'],
})
export class MessageInputComponent {
	@Output() messageSent = new EventEmitter<string>();
	messageContent: string = '';

	sendMessage(): void {
		const content = this.messageContent.trim();
		if (content) {
			this.messageSent.emit(content);
			this.messageContent = ''; // Clear input after sending
		}
	}
}
