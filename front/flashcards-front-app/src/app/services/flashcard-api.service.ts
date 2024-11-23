import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Flashcard} from '../models/flashcard.model';
import {CorrectnessModel} from '../models/correctnessModel';
import {QuizModel} from "../models/quizModel";
import {environment} from '../env/environment';


@Injectable({
  providedIn: 'root',
})
export class FlashcardApiService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {
  }

  updateFlashcard(id: string, rating: number): Observable<Flashcard> {
    const params = new HttpParams().set('rating', rating.toString());
    return this.http.put<Flashcard>(
      `${this.apiUrl}/flashcards/${id}/rate`,
      null,
      {params},
    );
  }

  getFlashcards(rating: number): Observable<Flashcard[]> {
    return this.http.get<Flashcard[]>(
      `${this.apiUrl}/flashcards/byRating/${rating}`,
    );
  }

  getFlashcardsToRepeat(): Observable<Flashcard[]> {
    return this.http.get<Flashcard[]>(`${this.apiUrl}/flashcards/to-repeat`)
  }


  getQuizDialogue(phrases: string[]): Observable<QuizModel> {
    return this.http.post<QuizModel>(`${this.apiUrl}/ai/dialogue-quiz`, phrases);
  }

  checkCorrectness(
    originalSentence: string,
    sentenceToCheck: string,
  ): Observable<CorrectnessModel> {
    const body = new HttpParams()
      .set('originalSentence', originalSentence)
      .set('sentenceToCheck', sentenceToCheck);

    const headers = new HttpHeaders({
      'Content-Type': 'application/x-www-form-urlencoded',
    });

    return this.http.post<CorrectnessModel>(
      `${this.apiUrl}/ai/check-sentence`,
      body.toString(),
      {headers},
    );
  }

  getAllFlashcards(): Observable<Flashcard[]> {
    return this.http.get<Flashcard[]>(`${this.apiUrl}/`);
  }
}
