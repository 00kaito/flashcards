package com.bednarz.flashcardsapi.model;

public record CorrectnessRequest(
        String originalSentence,
        String userSentenceToCheck) {
}
