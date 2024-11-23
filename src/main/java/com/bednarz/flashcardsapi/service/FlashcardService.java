package com.bednarz.flashcardsapi.service;

import com.bednarz.flashcardsapi.model.EASE_FACTOR;
import com.bednarz.flashcardsapi.model.Flashcard;
import com.bednarz.flashcardsapi.repository.FlashcardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FlashcardService {

    private final FlashcardRepository flashcardRepository;

    public List<Flashcard> getAllFlashcards() {
        return flashcardRepository.findAll();
    }

    public List<Flashcard> getFlashcardsToRepeat() {
        return flashcardRepository.findByNextReviewDateBefore(LocalDate.now()).stream()
                .sorted(Comparator.comparing(Flashcard::nextReviewDate)).toList();
    }

    public Optional<Flashcard> getFlashcardById(String id) {
        return flashcardRepository.findById(id);
    }

    public List<Flashcard> getFlashcardByRating(int rating) {
        return flashcardRepository.findByRating(rating);
    }

    public List<Flashcard> getFlashcardsByCategory(String category) {
        return flashcardRepository.findByCategory(category);
    }

    public Flashcard createFlashcard(Flashcard flashcard) {
        return flashcardRepository.save(flashcard);
    }

    public Flashcard updateFlashcard(String id, Flashcard flashcard) {
        return flashcardRepository.findById(id)
                .map(existingFlashcard -> {
                    Flashcard updatedFlashcard = new Flashcard(
                            id,
                            flashcard.front(),
                            flashcard.synonyms(),
                            flashcard.frontSentence(),
                            flashcard.back(),
                            flashcard.backSentence(),
                            flashcard.category(),
                            flashcard.status(),
                            flashcard.rating(),
                            0,
                            EASE_FACTOR.INITIAL.getValue(),
                            0,
                            LocalDate.now()
                    );
                    return flashcardRepository.save(updatedFlashcard);
                })
                .orElseThrow(() -> new RuntimeException("Flashcard not found"));
    }

    public void deleteFlashcard(String id) {
        flashcardRepository.deleteById(id);
    }

    public Flashcard rateFlashcard(String id, int rating) {
        return flashcardRepository.findById(id)
                .map(existingFlashcard -> {
                    double easeFactor = calculateNewEaseFactor(existingFlashcard.easeFactor(), rating);
                    int interval = calculateNewInterval(existingFlashcard.repetitions(),
                            existingFlashcard.nextReviewInDays(),
                            easeFactor,
                            existingFlashcard.rating());
                    int repetitions = calculateNewRepetitions(existingFlashcard.repetitions(), rating);
                    Flashcard ratedFlashcard = new Flashcard(
                            existingFlashcard.id(),
                            existingFlashcard.front(),
                            existingFlashcard.synonyms(),
                            existingFlashcard.frontSentence(),
                            existingFlashcard.back(),
                            existingFlashcard.backSentence(),
                            existingFlashcard.category(),
                            existingFlashcard.status(),
                            rating,
                            repetitions,
                            easeFactor,
                            interval,
                            LocalDate.now().plusDays((long) interval)
                    );
                    return flashcardRepository.save(ratedFlashcard);
                })
                .orElseThrow(() -> new RuntimeException("Flashcard not found"));
    }

    private static double calculateNewEaseFactor(double currentEaseFactor, int grade) {
        double newEaseFactor = currentEaseFactor + (0.1 - (5 - grade) * (0.08 + (5 - grade) * 0.02));
        return Math.max(currentEaseFactor, newEaseFactor);
    }

    private static int calculateNewInterval(int repetitions, int currentInterval, double easeFactor, int grade) {
        if (grade >= 3) {
            if (repetitions == 0) {
                return 1;
            } else if (repetitions == 1) {
                return 6;
            } else {
                return (int) Math.round(currentInterval * easeFactor);
            }
        } else {
            return 1;
        }
    }

    private static int calculateNewRepetitions(int currentRepetitions, int grade) {
        if (grade >= 3) {
            return currentRepetitions + 1;
        } else {
            return 0;
        }
    }


    public List<Flashcard> addGroupOfFlashcards(List<Flashcard> flashcards) {
        return flashcardRepository.saveAll(flashcards);
    }
}