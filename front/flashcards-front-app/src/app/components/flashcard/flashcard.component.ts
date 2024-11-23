import {Component, EventEmitter, HostListener, Input, Output,} from '@angular/core';
import {FlashcardApiService} from '../../services/flashcard-api.service';
import {Flashcard} from '../../models/flashcard.model';
import {SharedService} from '../shared.service';
import {MODE} from '../mode';
import {CorrectnessModel} from '../../models/correctnessModel';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';

@Component({
  selector: 'app-flashcard',
  templateUrl: './flashcard.component.html',
  styleUrls: ['./flashcard.component.css'],
})
export class FlashcardComponent {
  INITIAL_EASE_FACTOR = 2.5;
  MIN_EASE_FACTOR = 1.3;

  @Input() flashcard: Flashcard = {
    id: '',
    front: '',
    synonyms: '',
    frontSentence: '',
    back: '',
    backSentence: '',
    category: '',
    status: 'UNKNOWN',
    rating: 1,
    repetitions: 0,
    easeFactor: this.INITIAL_EASE_FACTOR,
    interval: 0,
    nextReviewInDays: 0,
    nextReviewDate: new Date(),
  };
  @Input()
  flashcardsSize: number = 0;
  @Output() selectedIndex = new EventEmitter<number>();
  isUpsideDown = true;
  currentIndex: number = 0;
  mode: MODE = MODE.PREVIEW;
  loading: boolean = false;
  // correctnessResult: boolean = true;
  // explanationMessage: string = ""
  checkGrammarResponse: CorrectnessModel = {
    result: null,
    type: '',
    correction: '',
    explanation: '',
  };
  flashcardForm: FormGroup;
  clickedButton: string | null = null;

  constructor(
    private flashcardService: FlashcardApiService,
    sharedDataService: SharedService,
    private fb: FormBuilder,
  ) {
    this.flashcardForm = this.fb.group({
      sentence: ['', [Validators.required, Validators.minLength(4)]],
    });
    sharedDataService.mode$.subscribe((data: MODE | number) => {
      this.mode = data;
    });
  }

  setClickedButton(buttonType: string) {
    this.clickedButton = buttonType;
    this.onSubmit();
  }

  @HostListener('window:keydown', ['$event'])
  handleKeyboardEvent(event: KeyboardEvent) {
    const target = event.target as HTMLElement;
    if (
      target.tagName.toLowerCase() === 'input' ||
      target.tagName.toLowerCase() === 'textarea'
    ) {
      return;
    }

    if (event.key === '.') {
      this.getNextFlashcard();
    } else if (event.key === ',') {
      this.getPreviousFlashcard();
    } else if (event.key === '/') {
      this.turnCardOver();
    }
  }

  /*

    @HostListener('swiperight', ['$event'])
    onSwipeLeft(event: any) {
      console.log("right");
      this.getNextFlashcard();
    }

    @HostListener('swipeleft', ['$event'])
    onSwipeRight(event: any) {
      // console.log("left");
      // this.getPreviousFlashcard();
    }
  */

  async knownFlashcard(): Promise<void> {
    this.loading = true;
    if (this.flashcard.rating < 5) {
      this.flashcard.rating = this.flashcard.rating + 1;
      await this.updateRating(this.flashcard.id, this.flashcard.rating);
    }
    this.loading = false;
  }

  async notKnownFlashcard(): Promise<void> {
    this.loading = true;
    if (this.flashcard.rating > 1) {
      this.flashcard.rating = this.flashcard.rating - 1;
      await this.updateRating(this.flashcard.id, 2);
    }
    this.loading = false;
  }

  async rateFlashcard(rating: number): Promise<void> {
    this.loading = true;
    if (this.flashcard.rating > 1) {
      await this.updateRating(this.flashcard.id, rating);
    }
    this.loading = false;
  }

  getNextFlashcard(): void {
    console.log(this.currentIndex + ' :: ' + this.flashcardsSize);
    if (this.currentIndex + 1 < this.flashcardsSize) {
      this.currentIndex += 1;
      this.selectedIndex.emit(this.currentIndex);
      this.resetView();
    }
  }

  getPreviousFlashcard(): void {
    if (this.currentIndex - 1 >= 0) {
      this.currentIndex -= 1;
      this.selectedIndex.emit(this.currentIndex);
      this.resetView();
    }
  }

  updateRating(flashcardId: string, rating: number) {
    this.flashcardService.updateFlashcard(flashcardId, rating).subscribe(
      (updatedFlashcard: Flashcard) => {
        console.log('Flashcard updated:', updatedFlashcard);
      },
      (error) => {
        console.error('Error updating flashcard:', error);
      },
    );
  }

  private turnCardOver() {
    this.isUpsideDown = !this.isUpsideDown;
  }

  resetView() {
    this.checkGrammarResponse.result = null;
    this.checkGrammarResponse.type = '';
    this.checkGrammarResponse.explanation = '';
  }

  onSubmit() {
    if (!this.flashcardForm.valid) {
      return;
    }
    const sentence = this.flashcardForm.get('sentence')?.value;
    if (this.clickedButton === 'send') {
      this.flashcardService
        .checkCorrectness(this.flashcard.frontSentence, sentence)
        .subscribe(
          (response: CorrectnessModel) => {
            this.checkGrammarResponse = response;
            if (this.checkGrammarResponse.result) {
              console.log('correct!');
              // this.correctAnswer=true;
              // this.knowFlashcard()
            } else {
              // this.correctAnswer=false;
              // this.explanationMessage = response.explanation;
              console.log(
                'Incorrect sentence, correction:',
                response.correction,
                'Explanation:',
                this.checkGrammarResponse.type,
              );
            }
          },
          (error) => {
            console.error('Error:', error);
          },
        );
    }

    if (
      this.clickedButton === '1' ||
      this.clickedButton === '3' ||
      this.clickedButton === '5'
    ) {
      this.flashcardForm.get('sentence')?.reset();
      console.log('submit');
      this.flashcardForm.reset();
      let ratingNum = +this.clickedButton;
      this.rateFlashcard(ratingNum);
      console.log(ratingNum);
      this.getNextFlashcard();
    }
  }

  showNextCart() {
    this.getNextFlashcard();
  }

  showPreviousCart() {
    this.getPreviousFlashcard();
  }

  flipCart() {
    this.turnCardOver();
  }
}
