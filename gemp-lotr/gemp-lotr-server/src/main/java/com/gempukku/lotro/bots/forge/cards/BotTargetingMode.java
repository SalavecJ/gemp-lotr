package com.gempukku.lotro.bots.forge.cards;

import com.gempukku.lotro.bots.forge.cards.abstractcard.BotCard;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.bots.forge.plan.PlannedBoardState;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.function.ToIntFunction;

public enum BotTargetingMode {
    SPECIAL("Special targeting rules needed"),
    RANDOM("Random"),
    SKIRMISHING_SIMPLE("Skirmishing Simple"),
    COMPANION_HIGH_STRENGTH("Companion with the highest strength"),
    COMPANION_LOW_STRENGTH("Companion with the lowest strength"),
    COMPANION_NOT_DYING("Companion least likely to die"),
    HIGH_STRENGTH("Character with the highest strength"),
    LOW_STRENGTH("Character with the lowest strength"),
    HEAL("Heal"),
    EXERT_SELF("Self-exerting targeting"),
    LOW_VALUE_CARD_IN_HAND_PREF_FP("Lowest value card in hand, preferably FP"),
    LOW_VALUE_CARD_IN_HAND_PREF_SHADOW("Lowest value card in hand, preferably shadow");

    private final String humanReadable;

    BotTargetingMode(String humanReadable) {
        this.humanReadable = humanReadable;
    }

    public BotCard chooseTarget(PlannedBoardState plannedBoardState, List<BotCard> options, boolean printDebugMessages) {
        List<BotCard> list = chooseTargets(plannedBoardState, options, 1, 1, printDebugMessages);
        if (list.isEmpty()) return null;
        return list.getFirst();
    }

    public List<BotCard> chooseTargets(PlannedBoardState plannedBoardState, List<BotCard> options, int min, int max, boolean printDebugMessages) {
        if (printDebugMessages) {
            System.out.println("Targeting mode: " + this);
            if (min != max) {
                System.out.println("Min options to choose: " + min);
                System.out.println("Max options to choose: " + max);
            } else {
                System.out.println("Options to choose: " + min);
            }
        }

        List<BotCard> myOptions = new ArrayList<>(options);

        List<BotCard> tbr = new ArrayList<>();
        for (int i = 1; i <= min; i++) {
            BotCard chosen = switch (this) {
                case SPECIAL -> throw new IllegalStateException("Cannot choose target for special targeting mode like this");
                case RANDOM -> chooseTargetRandom(myOptions, printDebugMessages);
                case HEAL -> chooseHealTarget(plannedBoardState, myOptions, printDebugMessages);
                case EXERT_SELF -> chooseExertSelfTarget(plannedBoardState, myOptions, printDebugMessages);
                case COMPANION_HIGH_STRENGTH -> chooseHighestStrengthCompanion(plannedBoardState, myOptions, printDebugMessages);
                case COMPANION_LOW_STRENGTH -> chooseLowestStrengthCompanion(plannedBoardState, myOptions, printDebugMessages);
                case COMPANION_NOT_DYING -> chooseCompanionLeastLikelyToDie(plannedBoardState, myOptions, printDebugMessages);
                case HIGH_STRENGTH -> chooseHighestStrength(plannedBoardState, myOptions, printDebugMessages);
                default -> throw new IllegalStateException("Targeting from enum for " + this + " is not implemented yet");
            };
            tbr.add(chosen);
            myOptions.remove(chosen);
        }

        return tbr;
    }

    private BotCard chooseExertSelfTarget(PlannedBoardState plannedBoardState, List<BotCard> options, boolean printDebugMessages) {
        BotCard chosen = options.stream()
                .max((o1, o2) -> {
                    int vitality1 = plannedBoardState.getVitality(o1);
                    int vitality2 = plannedBoardState.getVitality(o2);

                    int strength1 = plannedBoardState.getStrength(o1);
                    int strength2 = plannedBoardState.getStrength(o2);

                    if (vitality1 == 1 && vitality2 != 1) {
                        return -1;
                    }
                    if (vitality1 != 1 && vitality2 == 1) {
                        return 1;
                    }

                    if (o1.getSelf().getBlueprint().getCardType().equals(CardType.ALLY) && o2.getSelf().getBlueprint().getCardType().equals(CardType.COMPANION)) {
                        return 1;
                    }

                    if (o2.getSelf().getBlueprint().getCardType().equals(CardType.ALLY) && o1.getSelf().getBlueprint().getCardType().equals(CardType.COMPANION)) {
                        return -1;
                    }

                    if (strength1 > 2 * strength2) {
                        return -1;
                    }
                    if (strength2 > 2 * strength1) {
                        return 1;
                    }

                    if (vitality1 != vitality2) {
                        return Integer.compare(vitality1, vitality2);
                    }

                    return Integer.compare(strength2, strength1);
                })
                .orElseThrow();


        if (printDebugMessages) {
            System.out.println("Chosen: " + chosen.getSelf().getBlueprint().getFullName());
            System.out.println("Vitality: " + plannedBoardState.getVitality(chosen));
            System.out.println("Strength: " + plannedBoardState.getStrength(chosen));
        }

        return chosen;
    }

    private BotCard chooseHealTarget(PlannedBoardState plannedBoardState, List<BotCard> options, boolean printDebugMessages) {
        BotCard chosen = options.stream()
                .min((o1, o2) -> {
                    int vitality1 = plannedBoardState.getVitality(o1);
                    int vitality2 = plannedBoardState.getVitality(o2);

                    int strength1 = plannedBoardState.getStrength(o1);
                    int strength2 = plannedBoardState.getStrength(o2);

                    if (vitality1 == 1 && vitality2 != 1) {
                        return -1;
                    }
                    if (vitality1 != 1 && vitality2 == 1) {
                        return 1;
                    }

                    if (o1.getSelf().getBlueprint().getCardType().equals(CardType.ALLY) && o2.getSelf().getBlueprint().getCardType().equals(CardType.COMPANION)) {
                        return 1;
                    }

                    if (o2.getSelf().getBlueprint().getCardType().equals(CardType.ALLY) && o1.getSelf().getBlueprint().getCardType().equals(CardType.COMPANION)) {
                        return -1;
                    }

                    if (strength1 > 2 * strength2) {
                        return -1;
                    }
                    if (strength2 > 2 * strength1) {
                        return 1;
                    }

                    if (vitality1 != vitality2) {
                        return Integer.compare(vitality1, vitality2);
                    }

                    return Integer.compare(strength2, strength1);
                })
                .orElseThrow();


        if (printDebugMessages) {
            System.out.println("Chosen: " + chosen.getSelf().getBlueprint().getFullName());
            System.out.println("Vitality: " + plannedBoardState.getVitality(chosen));
            System.out.println("Strength: " + plannedBoardState.getStrength(chosen));
        }

        return chosen;
    }

    private BotCard chooseHighestStrengthCompanion(PlannedBoardState plannedBoardState, List<BotCard> options, boolean printDebugMessages) {
        BotCard chosen =  options.stream()
                .max(Comparator
                        .comparingInt((ToIntFunction<BotCard>) card -> card.getSelf().getBlueprint().getCardType() == CardType.COMPANION ? 1 : 0)
                        .thenComparingInt(card -> plannedBoardState.getVitality(card) > 1 ? 1 : 0)
                        .thenComparingInt(plannedBoardState::getStrength)
                        .thenComparingInt(plannedBoardState::getVitality))
                .orElseThrow();

        if (printDebugMessages) {
            System.out.println("Chosen: " + chosen.getSelf().getBlueprint().getFullName());
            System.out.println("Has more than 1 vitality: " + (plannedBoardState.getVitality(chosen) > 1));
            System.out.println("Strength: " + plannedBoardState.getStrength(chosen));
            System.out.println("Vitality: " + plannedBoardState.getVitality(chosen));
        }

        return chosen;
    }

    private BotCard chooseHighestStrength(PlannedBoardState plannedBoardState, List<BotCard> options, boolean printDebugMessages) {
        BotCard chosen =  options.stream()
                .max(Comparator
                        .comparingInt((ToIntFunction<BotCard>) card -> plannedBoardState.getVitality(card) > 1 ? 1 : 0)
                        .thenComparingInt(plannedBoardState::getStrength)
                        .thenComparingInt(plannedBoardState::getVitality))
                .orElseThrow();

        if (printDebugMessages) {
            System.out.println("Chosen: " + chosen.getSelf().getBlueprint().getFullName());
            System.out.println("Has more than 1 vitality: " + (plannedBoardState.getVitality(chosen) > 1));
            System.out.println("Strength: " + plannedBoardState.getStrength(chosen));
            System.out.println("Vitality: " + plannedBoardState.getVitality(chosen));
        }

        return chosen;
    }

    private BotCard chooseLowestStrengthCompanion(PlannedBoardState plannedBoardState, List<BotCard> options, boolean printDebugMessages) {
        BotCard chosen =  options.stream()
                .max(Comparator
                        .comparingInt((ToIntFunction<BotCard>) card -> plannedBoardState.getVitality(card) > 1 ? 1 : 0)
                        .thenComparingInt(card -> - plannedBoardState.getStrength(card))
                        .thenComparingInt(plannedBoardState::getVitality))
                .orElseThrow();

        if (printDebugMessages) {
            System.out.println("Chosen: " + chosen.getSelf().getBlueprint().getFullName());
            System.out.println("Has more than 1 vitality: " + (plannedBoardState.getVitality(chosen) > 1));
            System.out.println("Strength: " + plannedBoardState.getStrength(chosen));
            System.out.println("Vitality: " + plannedBoardState.getVitality(chosen));
        }

        return chosen;
    }

    private BotCard chooseCompanionLeastLikelyToDie(PlannedBoardState plannedBoardState, List<BotCard> options, boolean printDebugMessages) {
        BotCard chosen = options.stream()
                .max(Comparator
                        .comparingInt((ToIntFunction<BotCard>) card -> plannedBoardState.getRingBearers().contains(card) ? 1 : 0)
                        .thenComparingInt(card -> plannedBoardState.getVitality(card) > 1 ? 1 : 0)
                        .thenComparingInt(plannedBoardState::getStrength)
                        .thenComparingInt(plannedBoardState::getVitality))
                .orElseThrow();

        if (printDebugMessages) {
            System.out.println("Chosen: " + chosen.getSelf().getBlueprint().getFullName());
            System.out.println("Ring-bearer: " + plannedBoardState.getRingBearers().contains(chosen));
            System.out.println("Has more than 1 vitality: " + (plannedBoardState.getVitality(chosen) > 1));
            System.out.println("Strength: " + plannedBoardState.getStrength(chosen));
            System.out.println("Vitality: " + plannedBoardState.getVitality(chosen));
        }

        return chosen;
    }

    private BotCard chooseTargetRandom(List<BotCard> options, boolean printDebugMessages) {
        BotCard chosen = options.get(new Random().nextInt(0, options.size()));
        if (printDebugMessages) {
            System.out.println("Chosen: " + chosen.getSelf().getBlueprint().getFullName());
        }
        return chosen;
    }

    @Override
    public String toString() {
        return humanReadable;
    }
}
