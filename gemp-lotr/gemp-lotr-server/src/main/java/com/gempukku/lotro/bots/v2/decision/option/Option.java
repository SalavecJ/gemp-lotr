package com.gempukku.lotro.bots.v2.decision.option;

public abstract class Option {
    private final String humanReadable;
    private final String answerForGame;

    public Option(String humanReadable, String answerForGame) {
        this.humanReadable = humanReadable;
        this.answerForGame = answerForGame;
    }

    public String getHumanReadable() {
        return humanReadable;
    }

    public String getAnswerForGame() {
        return answerForGame;
    }

    @Override
    public String toString() {
        return humanReadable;
    }
}
