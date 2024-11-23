package com.bednarz.flashcardsapi.repository;

import com.bednarz.flashcardsapi.model.Flashcard;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FlashcardRepository extends MongoRepository<Flashcard, String> {
    List<Flashcard> findByCategory(String category);
    List<Flashcard> findByRating(int rating);
    List<Flashcard> findByNextReviewDateBefore(LocalDate date);
}