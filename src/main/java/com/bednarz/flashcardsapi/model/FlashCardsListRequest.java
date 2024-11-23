package com.bednarz.flashcardsapi.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FlashCardsListRequest {
    List<Flashcard> flashcardList;
}
