package com.bestaford.mintplay.util;

public enum Emoticons {

    FOOD('\ue100'), ARMOR('\ue101'), COIN('\ue102'), TOKEN('\ue105');

    private final char character;

    Emoticons(char character) {
        this.character = character;
    }

    public char getCharacter() {
        return character;
    }

    @Override
    public String toString() {
        return String.valueOf(character);
    }
}