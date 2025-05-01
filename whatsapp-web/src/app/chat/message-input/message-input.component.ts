import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { PickerComponent } from '@ctrl/ngx-emoji-mart';

@Component({
	selector: 'app-message-input',
	standalone: true,
	imports: [CommonModule, FormsModule, PickerComponent],
	templateUrl: './message-input.component.html',
	styleUrls: ['./message-input.component.css'],
})
export class MessageInputComponent {
	@Output() messageSent = new EventEmitter<string>();
	messageContent: string = '';
	showEmojiPicker = signal(false);

	sendMessage(): void {
		const content = this.messageContent.trim();
		if (content) {
			this.messageSent.emit(content);
			this.messageContent = ''; // Clear input after sending
			this.showEmojiPicker.set(false);
		}
	}

	toggleEmojiPicker(): void {
		this.showEmojiPicker.update(visible => !visible);
	}

	addEmoji(event: any): void {
		// event.emoji.native contains the native emoji character
		if (event.emoji?.native) {
			this.messageContent += event.emoji.native;
		}
	}

	// A more robust solution might use Angular CDK's Overlay or a directive
	handleClickOutside(event: MouseEvent, emojiButton: HTMLElement): void {
		// Check if the click target is outside the picker and the toggle button
		const pickerElement = document.querySelector('ngx-emoji-mart-picker'); // Simple selector, might need refinement
		if (
			this.showEmojiPicker() &&
			pickerElement &&
			!pickerElement.contains(event.target as Node) &&
			!emojiButton.contains(event.target as Node)
		) {
			this.showEmojiPicker.set(false);
		}
	}
}
