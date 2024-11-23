import {Component, OnInit} from '@angular/core';
import {FlashcardApiService} from '../../services/flashcard-api.service';
import {Flashcard} from "../../models/flashcard.model";
import {SharedService} from "../shared.service";
import {MODE} from '../mode';
import {MatSelectChange} from '@angular/material/select';
import {takeLast} from 'rxjs';

@Component({
  selector: 'app-cards',
  templateUrl: './cards.component.html',
  styleUrl: './cards.component.css',
})
export class CardsComponent implements OnInit {
  sharedService: SharedService;
  isQuizMode: number = 0;

  INITIAL_EASE_FACTOR = 2.5;
  MIN_EASE_FACTOR = 1.3;

  constructor(
    private flashcardService: FlashcardApiService,
    sharedService: SharedService,
  ) {
    this.sharedService = sharedService;
    sharedService.mode$.subscribe((data: MODE | number) => {
        this.isQuizMode = data;
      }
    )
    ;
  }

  mode = MODE.PREVIEW;
  flashcards: Flashcard[] = [];
  selectedFlashcard: Flashcard = {
    id: '',
    front: '',
    synonyms: '',
    frontSentence: '',
    back: '',
    backSentence: '',
    category: '',
    status: 'UNKNOWN',
    rating: 0,
    repetitions: 0,
    easeFactor: this.INITIAL_EASE_FACTOR,
    interval: 0,
    nextReviewInDays: 0,
    nextReviewDate: new Date(),
  };

  flashcardsSize = 0;

  ngOnInit(): void {
    this.getFlashcardsToRepeat();
  }

  fetchFlashcards(rating: number) {
    console.log(rating);
    this.flashcardService
      .getFlashcards(rating)
      .pipe(takeLast(1))
      .subscribe({
        next: (data: Flashcard[]) => this.handleFetchResponse(data, rating),
        error: (error) => this.handleError(error),
      });
  }

  getFlashcardsToRepeat() {
    this.flashcardService
      .getFlashcardsToRepeat()
      .pipe(takeLast(1))
      .subscribe({
        next: (data: Flashcard[]) => this.handleFetchToRepeatResponse(data),
        error: (error) => this.handleError(error),
      });
  }

  handleFetchToRepeatResponse(data: Flashcard[]) {
    this.flashcards = data;
    this.flashcardsSize = this.flashcards.length;
    this.getFlashcardByIndex(0);
  }

  handleFetchResponse(data: Flashcard[], rating: number) {
    console.log('check rating: ' + rating);
    if (data.length === 0 && rating < 5) {
      console.log('no data');
      this.fetchFlashcards(rating + 1);
    } else {
      this.flashcards = data;
      this.flashcardsSize = this.flashcards.length;
      this.getFlashcardByIndex(0);
    }
    console.log(data);
  }

  handleError(error: any) {
    console.error('Error fetching flashcards', error);
  }

  getFlashcardByIndex(index: number): void {
    if (this.flashcards[index]) {
      console.log(
        'get flash cards index: ' + index + ': ' + this.flashcards[index].front,
      );
      this.selectedFlashcard = this.flashcards[index];
    }
  }

  filterFlashCards($event: MatSelectChange) {
    this.fetchFlashcards($event.value as number);
  }
}
