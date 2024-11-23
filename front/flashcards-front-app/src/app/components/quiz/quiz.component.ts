import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {Flashcard} from "../../models/flashcard.model";
import {FlashcardApiService} from "../../services/flashcard-api.service";
import {QuizModel} from "../../models/quizModel";
import {tap} from "rxjs";

@Component({
  selector: 'app-quiz',
  templateUrl: './quiz.component.html',
  styleUrl: './quiz.component.css'
})
export class QuizComponent implements OnInit, OnChanges {
  dialogues = [
    {
      A1: "",
      B: "",
      A2: "",
      gap: ""
    }
  ];

  gapOptions: string[] = [];

  @Input()
  flashcards: Flashcard[] = [];

  apiService: FlashcardApiService;

  constructor(apiService: FlashcardApiService) {
    this.apiService = apiService;
  }

  ngOnInit(): void {
    const dialoguesToGet = 3;
    let randomPhrases = this.getRandomPhrases(dialoguesToGet);
    console.log("---" + randomPhrases)
    if (randomPhrases.length == dialoguesToGet) {
      this.apiService.getQuizDialogue(randomPhrases).pipe(tap(data => {
        console.log('Fetched data:', data);
      }))
        .subscribe({
          next: (data: QuizModel) => {
            this.gapOptions = [];
            this.dialogues = data.dialogueList;
            for (let i = 0; i < data.dialogueList.length; i++) {
              this.gapOptions.push(data.dialogueList[i].gap)
            }
          },
          error: (error: any) => {
            console.error("An error occurred:", error);
          },
          complete: () => {
            console.log("getRandomPhrases Observable completed");
          }
        });
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['flashcards']) {
      console.log("CHANGESSS " + this.flashcards)
    }
  }

  handleError(error: any) {
    console.error('Error fetching flashcards', error);
  }

  getRandomPhrases(count: number = 5): string[] {
    const shuffledFlashcards = this.flashcards.sort(() => Math.random() - 0.5);
    console.log("random: " + shuffledFlashcards)
    return shuffledFlashcards.slice(0, count).map(flashcard => flashcard.front);
  }


  userResponses: string[] = new Array(this.dialogues.length).fill('');
  results: string[] = [];

  checkAnswers() {
    this.results = this.dialogues.map((dialogue, index) => {
      return this.userResponses[index].toLowerCase() === dialogue.gap.toLowerCase() ? 'Correct' : 'Incorrect';
    });
  }
}
