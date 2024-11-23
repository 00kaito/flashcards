package com.bednarz.flashcardsapi.model;

import lombok.Getter;

@Getter
public enum EASE_FACTOR {
    INITIAL(2.5),
    MIN(1.3);

    double value;

    EASE_FACTOR(double value) {
        this.value = value;
    }
}
