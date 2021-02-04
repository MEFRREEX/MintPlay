package com.bestaford.mintplay;

public enum Characters {

    FOOD('\ue100'), ARMOR('\ue101'), COIN('\ue102'), TOKEN('\ue105');

    public char character;

    Characters(char character) {
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