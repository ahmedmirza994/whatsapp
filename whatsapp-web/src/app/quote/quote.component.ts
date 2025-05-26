import { Component, OnDestroy, OnInit } from '@angular/core';
import { interval, Subscription } from 'rxjs';

@Component({
  selector: 'app-quote',
  templateUrl: './quote.component.html',
  styleUrls: ['./quote.component.css'],
})
export class QuoteComponent implements OnInit, OnDestroy {
  quote: { text: string; author: string } | undefined;
  fadeState: 'in' | 'out' = 'in';
  private quoteInterval?: Subscription;

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
    // Communication-related quotes
    {
      text: 'The single biggest problem in communication is the illusion that it has taken place.',
      author: 'George Bernard Shaw',
    },
    {
      text: 'To effectively communicate, we must realize that we are all different in the way we perceive the world.',
      author: 'Tony Robbins',
    },
    {
      text: 'Communication works for those who work at it.',
      author: 'John Powell',
    },
    {
      text: "The most important thing in communication is hearing what isn't said.",
      author: 'Peter Drucker',
    },
  ];

  ngOnInit() {
    this.setRandomQuote();
    this.startQuoteRotation();
  }

  ngOnDestroy() {
    if (this.quoteInterval) {
      this.quoteInterval.unsubscribe();
    }
  }

  setRandomQuote() {
    // Get current quote to avoid showing the same one twice
    const currentQuote = this.quote;
    let newQuote;

    do {
      const randomIndex = Math.floor(Math.random() * this.quotes.length);
      newQuote = this.quotes[randomIndex];
    } while (newQuote === currentQuote && this.quotes.length > 1);

    this.fadeState = 'out';

    // Wait for fade out animation to complete
    setTimeout(() => {
      this.quote = newQuote;
      this.fadeState = 'in';
    }, 500);
  }

  private startQuoteRotation() {
    // Change quote every 15 seconds
    this.quoteInterval = interval(15000).subscribe(() => {
      this.setRandomQuote();
    });
  }
}
