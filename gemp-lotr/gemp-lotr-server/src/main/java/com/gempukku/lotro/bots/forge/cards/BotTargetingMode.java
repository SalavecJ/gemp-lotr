package com.gempukku.lotro.bots.forge.cards;

import com.gempukku.lotro.bots.forge.cards.ability2.util.WoundsValueUtil;
import com.gempukku.lotro.bots.forge.cards.abstractcard.BotCard;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;
import com.gempukku.lotro.logic.timing.RuleUtils;

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
    LOW_VALUE_CARD_IN_HAND_PREF_SHADOW("Lowest value card in hand, preferably shadow"),
    BASIC_SHADOW_WEAPON_TARGETING("Basic shadow weapon targeting");

    private final String humanReadable;

    BotTargetingMode(String humanReadable) {
        this.humanReadable = humanReadable;
    }

    public BotCard chooseTarget(DefaultLotroGame game, List<BotCard> options, boolean printDebugMessages) {
        List<BotCard> list = chooseTargets(game, options, 1, 1, printDebugMessages);
        if (list.isEmpty()) return null;
        return list.getFirst();
    }

    public List<BotCard> chooseTargets(DefaultLotroGame game, List<BotCard> options, int min, int max, boolean printDebugMessages) {
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
                case HEAL -> chooseHealTarget(game, myOptions, printDebugMessages);
                case EXERT_SELF -> chooseExertSelfTarget(game, myOptions, printDebugMessages);
                case COMPANION_HIGH_STRENGTH -> chooseHighestStrengthCompanion(game, myOptions, printDebugMessages);
                case COMPANION_LOW_STRENGTH -> chooseLowestStrengthCompanion(game, myOptions, printDebugMessages);
                case COMPANION_NOT_DYING -> chooseCompanionLeastLikelyToDie(game, myOptions, printDebugMessages);
                case HIGH_STRENGTH -> chooseHighestStrength(game, myOptions, printDebugMessages);
                case BASIC_SHADOW_WEAPON_TARGETING -> chooseBasicShadowWeaponTarget(game, myOptions, printDebugMessages);
                default -> throw new IllegalStateException("Targeting from enum for " + this + " is not implemented yet");
            };
            tbr.add(chosen);
            myOptions.remove(chosen);
        }

        return tbr;
    }


    private BotCard chooseBasicShadowWeaponTarget(DefaultLotroGame game, List<BotCard> myOptions, boolean printDebugMessages) {
        int minionInPlay = Math.toIntExact(game.getGameState().getActiveCards().stream().filter(physicalCard -> CardType.MINION == physicalCard.getBlueprint().getCardType()).count());

        int companionsInPlay = Math.toIntExact(game.getGameState().getActiveCards().stream().filter(physicalCard -> CardType.COMPANION == physicalCard.getBlueprint().getCardType()).count());
        int alliesInPlay = Math.toIntExact(game.getGameState().getActiveCards().stream()
                .filter(physicalCard ->
                        CardType.ALLY == physicalCard.getBlueprint().getCardType()
                                && RuleUtils.isAllyAtHome(physicalCard, game.getGameState().getCurrentSiteNumber(), game.getGameState().getCurrentSiteBlock()))
                .count());
        int fpThatCanSkirmish = companionsInPlay + alliesInPlay;

        boolean swarm = minionInPlay > fpThatCanSkirmish;

        if (swarm) {
            return chooseLowestStrength(game, myOptions, printDebugMessages);
        } else {
            return chooseHighestStrength(game, myOptions, printDebugMessages);
        }
    }

    private BotCard chooseExertSelfTarget(DefaultLotroGame game, List<BotCard> options, boolean printDebugMessages) {
        BotCard chosen = options.stream()
                .max(Comparator.comparingDouble(o -> WoundsValueUtil.evaluateWoundsChangeValue(
                        options.getFirst().getSelf().getOwner(),
                        game,
                        o.getSelf(),
                        1)))
                .orElseThrow();


        if (printDebugMessages) {
            System.out.println("Chosen: " + chosen.getSelf().getBlueprint().getFullName());
            System.out.println("Vitality: " + game.getModifiersQuerying().getVitality(game, chosen.getSelf()));
            System.out.println("Strength: " + game.getModifiersQuerying().getStrength(game, chosen.getSelf()));
        }

        return chosen;
    }

    private BotCard chooseHealTarget(DefaultLotroGame game, List<BotCard> options, boolean printDebugMessages) {
        BotCard chosen = options.stream()
                .max(Comparator.comparingDouble(o -> WoundsValueUtil.evaluateWoundsChangeValue(
                        options.getFirst().getSelf().getOwner(),
                        game,
                        o.getSelf(),
                        -1)))
                .orElseThrow();


        if (printDebugMessages) {
            System.out.println("Chosen: " + chosen.getSelf().getBlueprint().getFullName());
            System.out.println("Vitality: " + game.getModifiersQuerying().getVitality(game, chosen.getSelf()));
            System.out.println("Strength: " + game.getModifiersQuerying().getStrength(game, chosen.getSelf()));
        }

        return chosen;
    }

    private BotCard chooseHighestStrengthCompanion(DefaultLotroGame game, List<BotCard> options, boolean printDebugMessages) {
        BotCard chosen =  options.stream()
                .max(Comparator
                        .comparingInt((ToIntFunction<BotCard>) card -> card.getSelf().getBlueprint().getCardType() == CardType.COMPANION ? 1 : 0)
                        .thenComparingInt(card -> game.getModifiersQuerying().getVitality(game, card.getSelf()) > 1 ? 1 : 0)
                        .thenComparingInt(card -> game.getModifiersQuerying().getStrength(game, card.getSelf()))
                        .thenComparingInt(card -> game.getModifiersQuerying().getVitality(game, card.getSelf())))
                .orElseThrow();

        if (printDebugMessages) {
            System.out.println("Chosen: " + chosen.getSelf().getBlueprint().getFullName());
            System.out.println("Has more than 1 vitality: " + (game.getModifiersQuerying().getVitality(game, chosen.getSelf()) > 1));
            System.out.println("Strength: " + game.getModifiersQuerying().getStrength(game, chosen.getSelf()));
            System.out.println("Vitality: " + game.getModifiersQuerying().getVitality(game, chosen.getSelf()));
        }

        return chosen;
    }

    private BotCard chooseHighestStrength(DefaultLotroGame game, List<BotCard> options, boolean printDebugMessages) {
        BotCard chosen =  options.stream()
                .max(Comparator
                        .comparingInt((ToIntFunction<BotCard>) card -> game.getModifiersQuerying().getVitality(game, card.getSelf()) > 1 ? 1 : 0)
                        .thenComparingInt(card -> game.getModifiersQuerying().getStrength(game, card.getSelf()))
                        .thenComparingInt(card -> game.getModifiersQuerying().getVitality(game, card.getSelf())))
                .orElseThrow();

        if (printDebugMessages) {
            System.out.println("Chosen: " + chosen.getSelf().getBlueprint().getFullName());
            System.out.println("Has more than 1 vitality: " + (game.getModifiersQuerying().getVitality(game, chosen.getSelf()) > 1));
            System.out.println("Strength: " + game.getModifiersQuerying().getStrength(game, chosen.getSelf()));
            System.out.println("Vitality: " + game.getModifiersQuerying().getVitality(game, chosen.getSelf()));
        }

        return chosen;
    }

    private BotCard chooseLowestStrengthCompanion(DefaultLotroGame game, List<BotCard> options, boolean printDebugMessages) {
        BotCard chosen =  options.stream()
                .max(Comparator
                        .comparingInt((ToIntFunction<BotCard>) card -> card.getSelf().getBlueprint().getCardType() == CardType.COMPANION ? 1 : 0)
                        .thenComparingInt(card -> game.getModifiersQuerying().getVitality(game, card.getSelf()) > 1 ? 1 : 0)
                        .thenComparingInt(card -> - game.getModifiersQuerying().getStrength(game, card.getSelf()))
                        .thenComparingInt(card -> game.getModifiersQuerying().getVitality(game, card.getSelf())))
                .orElseThrow();

        if (printDebugMessages) {
            System.out.println("Chosen: " + chosen.getSelf().getBlueprint().getFullName());
            System.out.println("Has more than 1 vitality: " + (game.getModifiersQuerying().getVitality(game, chosen.getSelf()) > 1));
            System.out.println("Strength: " + game.getModifiersQuerying().getStrength(game, chosen.getSelf()));
            System.out.println("Vitality: " + game.getModifiersQuerying().getVitality(game, chosen.getSelf()));
        }

        return chosen;
    }

    private BotCard chooseLowestStrength(DefaultLotroGame game, List<BotCard> options, boolean printDebugMessages) {
        BotCard chosen =  options.stream()
                .max(Comparator
                        .comparingInt((ToIntFunction<BotCard>) card -> - game.getModifiersQuerying().getStrength(game, card.getSelf()))
                        .thenComparingInt(card -> game.getModifiersQuerying().getVitality(game, card.getSelf())))
                .orElseThrow();

        if (printDebugMessages) {
            System.out.println("Chosen: " + chosen.getSelf().getBlueprint().getFullName());
            System.out.println("Strength: " + game.getModifiersQuerying().getStrength(game, chosen.getSelf()));
            System.out.println("Vitality: " + game.getModifiersQuerying().getVitality(game, chosen.getSelf()));
        }

        return chosen;
    }

    private BotCard chooseCompanionLeastLikelyToDie(DefaultLotroGame game, List<BotCard> options, boolean printDebugMessages) {
        BotCard chosen = options.stream()
                .max(Comparator
                        .comparingInt((ToIntFunction<BotCard>) card -> card.getSelf().getBlueprint().getCardType() == CardType.COMPANION ? 1 : 0)
                        .thenComparingInt(card -> game.getGameState().getRingBearers().contains(card.getSelf()) ? 1 : 0)
                        .thenComparingInt(card -> game.getModifiersQuerying().getVitality(game, card.getSelf()) > 1 ? 1 : 0)
                        .thenComparingInt(card -> game.getModifiersQuerying().getStrength(game, card.getSelf()))
                        .thenComparingInt(card -> game.getModifiersQuerying().getVitality(game, card.getSelf())))
                .orElseThrow();

        if (printDebugMessages) {
            System.out.println("Chosen: " + chosen.getSelf().getBlueprint().getFullName());
            System.out.println("Ring-bearer: " + game.getGameState().getRingBearers().contains(chosen.getSelf()));
            System.out.println("Has more than 1 vitality: " + (game.getModifiersQuerying().getVitality(game, chosen.getSelf()) > 1));
            System.out.println("Strength: " + game.getModifiersQuerying().getStrength(game, chosen.getSelf()));
            System.out.println("Vitality: " + game.getModifiersQuerying().getVitality(game, chosen.getSelf()));
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
