package com.bednarz.flashcardsapi.controller;

import com.bednarz.flashcardsapi.ai.OpenAIService;
import com.bednarz.flashcardsapi.model.CorrectnessRequest;
import com.bednarz.flashcardsapi.model.FlashCardsListRequest;
import com.bednarz.flashcardsapi.model.Flashcard;
import com.bednarz.flashcardsapi.service.FlashcardService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/flashcards")
@RequiredArgsConstructor
public class FlashcardController {

    private final FlashcardService flashcardService;
    private final OpenAIService aiService;

    @GetMapping
    public ResponseEntity<List<Flashcard>> getAllFlashcards() {
        return ResponseEntity.ok(flashcardService.getAllFlashcards());
    }

    @GetMapping("/to-repeat")
    public ResponseEntity<List<Flashcard>> getAllFlashcardsPlannerToRepeat() {
        return ResponseEntity.ok(flashcardService.getFlashcardsToRepeat());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Flashcard> getFlashcardById(@PathVariable String id) {
        Optional<Flashcard> flashcard = flashcardService.getFlashcardById(id);
        return flashcard.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/byRating/{rating}")
    public ResponseEntity<List<Flashcard>> getFlashcardById(@PathVariable int rating) {
        if (rating < 0){
            return getAllFlashcards();
        }
            return ResponseEntity.ok(flashcardService.getFlashcardByRating(rating));
    }

    /*
        @GetMapping("/category/{category}")
        public ResponseEntity<List<Flashcard>> getFlashcardsByCategory(@PathVariable String category) {
            return ResponseEntity.ok(flashcardService.getFlashcardsByCategory(category));
        }

        @PostMapping
        public ResponseEntity<Flashcard> createFlashcard(@RequestBody Flashcard flashcard) {
            Flashcard createdFlashcard = flashcardService.createFlashcard(flashcard);
            return new ResponseEntity<>(createdFlashcard, HttpStatus.CREATED);
        }

        @PutMapping("/{id}")
        @Operation(operationId = "updateFlashcard")
        public ResponseEntity<Flashcard> updateFlashcard(@PathVariable String id, @RequestBody Flashcard flashcard) {
            try {
                Flashcard updatedFlashcard = flashcardService.updateFlashcard(id, flashcard);
                return ResponseEntity.ok(updatedFlashcard);
            } catch (RuntimeException e) {
                return ResponseEntity.notFound().build();
            }
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<Void> deleteFlashcard(@PathVariable String id) {
            flashcardService.deleteFlashcard(id);
            return ResponseEntity.noContent().build();
        }
    */
    @PutMapping("/{id}/rate")
    @Operation(operationId = "rateFlashcard")
    public ResponseEntity<Flashcard> rateFlashcard(@PathVariable String id, @RequestParam int rating) {
        try {
            Flashcard ratedFlashcard = flashcardService.rateFlashcard(id, rating);
            return ResponseEntity.ok(ratedFlashcard);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/group")
    @Operation(operationId = "addGroupOfFlashcards")
    public ResponseEntity<List<Flashcard>> addGroupOfFlashcards(@RequestBody FlashCardsListRequest request) {
        List<Flashcard> createdFlashcards = flashcardService.addGroupOfFlashcards(request.getFlashcardList());
        return new ResponseEntity<>(createdFlashcards, HttpStatus.CREATED);
    }
}