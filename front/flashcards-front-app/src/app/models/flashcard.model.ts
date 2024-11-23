export interface Flashcard {
  id: string;
  front: string;
  synonyms: string;
  frontSentence: string;
  back: string;
  backSentence: string;
  category: string;
  status: 'KNOWN' | 'NEEDS_REVIEW' | 'UNKNOWN';
  rating: number;
  repetitions: number;
  easeFactor: number;
  interval: number;
  nextReviewInDays: number;
  nextReviewDate: Date;
}
