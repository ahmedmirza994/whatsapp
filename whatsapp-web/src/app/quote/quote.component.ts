import { Component, OnInit } from '@angular/core';

@Component({
	selector: 'app-quote',
	templateUrl: './quote.component.html',
	styleUrls: ['./quote.component.css'],
})
export class QuoteComponent implements OnInit {
	quote: { text: string; author: string } | undefined;

	quotes = [
		{
			text: 'Success is not the key to happiness. Happiness is the key to success. If you love what you are doing, you will be successful.',
			author: 'Albert Schweitzer',
		},
		{
			text: 'The only way to do great work is to love what you do.',
			author: 'Steve Jobs',
		},
		{
			text: "Believe you can and you're halfway there.",
			author: 'Theodore Roosevelt',
		},
		{
			text: "Your time is limited, don't waste it living someone else's life.",
			author: 'Steve Jobs',
		},
		{
			text: 'The best way to predict the future is to invent it.',
			author: 'Alan Kay',
		},
		{
			text: 'The only limit to our realization of tomorrow is our doubts of today.',
			author: 'Franklin D. Roosevelt',
		},
	];

	ngOnInit() {
		this.setRandomQuote();
	}

	setRandomQuote() {
		const randomIndex = Math.floor(Math.random() * this.quotes.length);
		this.quote = this.quotes[randomIndex];
	}
}
