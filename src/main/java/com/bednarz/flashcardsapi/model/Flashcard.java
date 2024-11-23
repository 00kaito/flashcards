package com.bednarz.flashcardsapi.model;

import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document(collection = "flashcards")
@Builder
public record Flashcard(
        @Id
        String id,
        String front,
        String synonyms,
        String frontSentence,
        String back,
        String backSentence,
        String category,
        LearningStatus status,
        int rating,
        Integer repetitions,
        Double easeFactor,
        Integer nextReviewInDays,
        LocalDate nextReviewDate
) {
}

enum LearningStatus {
    KNOWN,
    NEEDS_REVIEW,
    UNKNOWN
}