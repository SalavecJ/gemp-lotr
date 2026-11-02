package com.gempukku.lotro.bots.v2.fotr_starter;

public enum FotrStarterDeck {
    ARAGORN_STARTER("Aragorn Deck"), GANDALF_STARTER("Gandalf Deck");

    private final String name;
    FotrStarterDeck(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
