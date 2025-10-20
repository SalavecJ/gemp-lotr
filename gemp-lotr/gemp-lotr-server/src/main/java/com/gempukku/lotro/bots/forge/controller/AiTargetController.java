package com.gempukku.lotro.bots.forge.controller;

import com.gempukku.lotro.bots.forge.utils.CombatUtil;
import com.gempukku.lotro.cards.build.bot.ability2.util.HandValueUtil;
import com.gempukku.lotro.cards.build.bot.BotCardFactory;
import com.gempukku.lotro.cards.build.bot.BotTargetingMode;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.game.state.Skirmish;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

import java.util.*;
import java.util.function.Function;
import java.util.function.ToIntFunction;

public class AiTargetController {
    private final Map<String, Function<List<PhysicalCard>, PhysicalCard>> specialTargetingMap = new HashMap<>();

    private final boolean printDebugMessages;
    private final LotroGame game;

    private final List<PhysicalCard> options;
    private final PhysicalCard source;
    private final AwaitingDecision awaitingDecision;
    private final int min;
    private final int max;

    public AiTargetController(boolean printDebugMessages, LotroGame game, List<PhysicalCard> options,
                              PhysicalCard source, AwaitingDecision awaitingDecision) {
        this.printDebugMessages = printDebugMessages;
        this.game = game;
        this.options = options;
        this.source = source;
        this.awaitingDecision = awaitingDecision;
        this.min = Integer.parseInt(awaitingDecision.getDecisionParameters().get("min")[0]);
        this.max = Integer.parseInt(awaitingDecision.getDecisionParameters().get("max")[0]);

        initSpecialTargetingRules();
    }

    public List<PhysicalCard> chooseTarget() {
        if (printDebugMessages) {
            System.out.println("Choosing target for " + source.getBlueprint().getFullName());
            StringBuilder builder = new StringBuilder("Options: ");
            for (PhysicalCard option : options) {
                builder.append(option.getBlueprint().getFullName()).append("; ");
            }
            System.out.println(builder);
        }

        BotTargetingMode mode = BotCardFactory.create(source).getTargetingModeForDecision(game, awaitingDecision);

        if (printDebugMessages) {
            System.out.println("Targeting mode: " + mode);
            if (min != max) {
                System.out.println("Min options to choose: " + min);
                System.out.println("Max options to choose: " + max);
            } else {
                System.out.println("Options to choose: " + min);
            }
        }

        List<PhysicalCard> tbr = new ArrayList<>();
        for (int i = 1; i <= min; i++) {
            PhysicalCard chosen = switch (mode) {
                case SPECIAL -> chooseTargetBasedOnSpecialRule();
                case RANDOM -> chooseTargetRandom();
                case SKIRMISHING_SIMPLE -> chooseSkirmishingTargetSimple();
                case COMPANION_HIGH_STRENGTH -> chooseHighestStrengthCompanion();
                case COMPANION_LOW_STRENGTH -> chooseLowestStrengthCompanion();
                case COMPANION_NOT_DYING -> chooseCompanionLeastLikelyToDie();
                case HIGH_STRENGTH -> chooseHighestStrength();
                case LOW_STRENGTH -> chooseLowestStrength();
                case HEAL -> chooseHealTarget();
                case EXERT_SELF -> chooseExertSelfTarget();
                case LOW_VALUE_CARD_IN_HAND_PREF_FP -> chooseLowestValueCardInHand(Side.FREE_PEOPLE);
                case LOW_VALUE_CARD_IN_HAND_PREF_SHADOW -> chooseLowestValueCardInHand(Side.SHADOW);
            };
            tbr.add(chosen);
            options.remove(chosen);
        }

        return tbr;
    }

    private PhysicalCard chooseTargetRandom() {
        PhysicalCard chosen = options.get(new Random().nextInt(0, options.size()));
        if (printDebugMessages) {
            System.out.println("Chosen: " + chosen.getBlueprint().getFullName());
        }
        return chosen;
    }

    private PhysicalCard chooseTargetBasedOnSpecialRule() {
        if (specialTargetingMap.containsKey(source.getBlueprint().getId())) {
            if (printDebugMessages) {
                System.out.println("Special targeting rules found for " + source.getBlueprint().getFullName());
            }
            return specialTargetingMap.get(source.getBlueprint().getId()).apply(options);
        } else {
            if (printDebugMessages) {
                System.out.println("No special targeting rules found for " + source.getBlueprint().getFullName());
            }
            throw new UnsupportedOperationException("Special targeting rules not implemented for " + awaitingDecision.toJson().toString());
        }
    }

    private PhysicalCard chooseLowestValueCardInHand(Side side) {
        PhysicalCard chosen = options.stream().min((o1, o2) -> Integer.compare(HandValueUtil.scoreCardInHand(game, o1, side), HandValueUtil.scoreCardInHand(game, o2, side))).orElseThrow();
        if (printDebugMessages) {
            System.out.println("Chosen: " + chosen.getBlueprint().getFullName());
        }
        return chosen;
    }

    private PhysicalCard chooseExertSelfTarget() {
        PhysicalCard chosen = options.stream()
                .max((o1, o2) -> {
                    int vitality1 = game.getModifiersQuerying().getVitality(game, o1);
                    int vitality2 = game.getModifiersQuerying().getVitality(game, o2);

                    int strength1 = game.getModifiersQuerying().getStrength(game, o1);
                    int strength2 = game.getModifiersQuerying().getStrength(game, o2);

                    if (vitality1 == 1 && vitality2 != 1) {
                        return -1;
                    }
                    if (vitality1 != 1 && vitality2 == 1) {
                        return 1;
                    }

                    if (o1.getBlueprint().getCardType().equals(CardType.ALLY) && o2.getBlueprint().getCardType().equals(CardType.COMPANION)) {
                        return 1;
                    }

                    if (o2.getBlueprint().getCardType().equals(CardType.ALLY) && o1.getBlueprint().getCardType().equals(CardType.COMPANION)) {
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
            System.out.println("Chosen: " + chosen.getBlueprint().getFullName());
            System.out.println("Vitality: " + game.getModifiersQuerying().getVitality(game, chosen));
            System.out.println("Strength: " + game.getModifiersQuerying().getStrength(game, chosen));
        }

        return chosen;
    }

    private PhysicalCard chooseHealTarget() {
        PhysicalCard chosen = options.stream()
                .min((o1, o2) -> {
                    int vitality1 = game.getModifiersQuerying().getVitality(game, o1);
                    int vitality2 = game.getModifiersQuerying().getVitality(game, o2);

                    int strength1 = game.getModifiersQuerying().getStrength(game, o1);
                    int strength2 = game.getModifiersQuerying().getStrength(game, o2);

                    if (vitality1 == 1 && vitality2 != 1) {
                        return -1;
                    }
                    if (vitality1 != 1 && vitality2 == 1) {
                        return 1;
                    }

                    if (o1.getBlueprint().getCardType().equals(CardType.ALLY) && o2.getBlueprint().getCardType().equals(CardType.COMPANION)) {
                        return 1;
                    }

                    if (o2.getBlueprint().getCardType().equals(CardType.ALLY) && o1.getBlueprint().getCardType().equals(CardType.COMPANION)) {
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
            System.out.println("Chosen: " + chosen.getBlueprint().getFullName());
            System.out.println("Vitality: " + game.getModifiersQuerying().getVitality(game, chosen));
            System.out.println("Strength: " + game.getModifiersQuerying().getStrength(game, chosen));
        }

        return chosen;
    }

    private PhysicalCard chooseLowestStrength() {
        PhysicalCard chosen =  options.stream()
                .max(Comparator
                        .comparingInt((ToIntFunction<PhysicalCard>) card -> - game.getModifiersQuerying().getStrength(game, card))
                        .thenComparingInt(card -> game.getModifiersQuerying().getVitality(game, card)))
                .orElseThrow();

        if (printDebugMessages) {
            System.out.println("Chosen: " + chosen.getBlueprint().getFullName());
            System.out.println("Strength: " + game.getModifiersQuerying().getStrength(game, chosen));
            System.out.println("Vitality: " + game.getModifiersQuerying().getVitality(game, chosen));
        }

        return chosen;
    }

    private PhysicalCard chooseHighestStrength() {
        PhysicalCard chosen =  options.stream()
                .max(Comparator
                        .comparingInt((ToIntFunction<PhysicalCard>) card -> game.getModifiersQuerying().getStrength(game, card))
                        .thenComparingInt(card -> game.getModifiersQuerying().getVitality(game, card)))
                .orElseThrow();

        if (printDebugMessages) {
            System.out.println("Chosen: " + chosen.getBlueprint().getFullName());
            System.out.println("Strength: " + game.getModifiersQuerying().getStrength(game, chosen));
            System.out.println("Vitality: " + game.getModifiersQuerying().getVitality(game, chosen));
        }

        return chosen;
    }

    private PhysicalCard chooseCompanionLeastLikelyToDie() {
        PhysicalCard chosen = options.stream()
                .max(Comparator
                        .comparingInt((ToIntFunction<PhysicalCard>) card -> CardType.COMPANION.equals(card.getBlueprint().getCardType()) ? 1 : 0)
                        .thenComparingInt(card -> game.getGameState().getRingBearers().contains(card) ? 1 : 0)
                        .thenComparingInt(card -> game.getModifiersQuerying().getVitality(game, card) > 1 ? 1 : 0)
                        .thenComparingInt(card -> game.getModifiersQuerying().getStrength(game, card))
                        .thenComparingInt(card -> game.getModifiersQuerying().getVitality(game, card)))
                .orElseThrow();

        if (printDebugMessages) {
            System.out.println("Chosen: " + chosen.getBlueprint().getFullName());
            System.out.println("Companion: " + CardType.COMPANION.equals(chosen.getBlueprint().getCardType()));
            System.out.println("Ring-bearer: " + game.getGameState().getRingBearers().contains(chosen));
            System.out.println("Has more than 1 vitality: " + (game.getModifiersQuerying().getVitality(game, chosen) > 1));
            System.out.println("Strength: " + game.getModifiersQuerying().getStrength(game, chosen));
            System.out.println("Vitality: " + game.getModifiersQuerying().getVitality(game, chosen));
        }

        return chosen;
    }

    private PhysicalCard chooseLowestStrengthCompanion() {
        PhysicalCard chosen =  options.stream()
                .max(Comparator
                        .comparingInt((ToIntFunction<PhysicalCard>) card -> CardType.COMPANION.equals(card.getBlueprint().getCardType()) ? 1 : 0)
                        .thenComparingInt(card -> game.getModifiersQuerying().getVitality(game, card) > 1 ? 1 : 0)
                        .thenComparingInt(card -> - game.getModifiersQuerying().getStrength(game, card))
                        .thenComparingInt(card -> game.getModifiersQuerying().getVitality(game, card)))
                .orElseThrow();

        if (printDebugMessages) {
            System.out.println("Chosen: " + chosen.getBlueprint().getFullName());
            System.out.println("Companion: " + CardType.COMPANION.equals(chosen.getBlueprint().getCardType()));
            System.out.println("Has last vitality: " + (game.getModifiersQuerying().getVitality(game, chosen) > 1));
            System.out.println("Strength: " + game.getModifiersQuerying().getStrength(game, chosen));
            System.out.println("Vitality: " + game.getModifiersQuerying().getVitality(game, chosen));
        }

        return chosen;
    }

    private PhysicalCard chooseHighestStrengthCompanion() {
        PhysicalCard chosen =  options.stream()
                .max(Comparator
                        .comparingInt((ToIntFunction<PhysicalCard>) card -> CardType.COMPANION.equals(card.getBlueprint().getCardType()) ? 1 : 0)
                        .thenComparingInt(card -> game.getModifiersQuerying().getVitality(game, card) > 1 ? 1 : 0)
                        .thenComparingInt(card -> game.getModifiersQuerying().getStrength(game, card))
                        .thenComparingInt(card -> game.getModifiersQuerying().getVitality(game, card)))
                .orElseThrow();

        if (printDebugMessages) {
            System.out.println("Chosen: " + chosen.getBlueprint().getFullName());
            System.out.println("Companion: " + CardType.COMPANION.equals(chosen.getBlueprint().getCardType()));
            System.out.println("Has last vitality: " + (game.getModifiersQuerying().getVitality(game, chosen) > 1));
            System.out.println("Strength: " + game.getModifiersQuerying().getStrength(game, chosen));
            System.out.println("Vitality: " + game.getModifiersQuerying().getVitality(game, chosen));
        }

        return chosen;
    }

    private PhysicalCard chooseSkirmishingTargetSimple() {
        if (game.getGameState().getSkirmish() == null) {
            throw new IllegalStateException("No skirmish found for skirmish targeting");
        }

        List<PhysicalCard> skirmishingOptions = CombatUtil.returnSkirmishingCharacters(game, options);

        if (skirmishingOptions.isEmpty()) {
            PhysicalCard chosen = options.getFirst();
            if (printDebugMessages) {
                Skirmish skirmish = game.getGameState().getSkirmish();
                System.out.println("No skirmishing target found, choosing whatever");
                System.out.println("Skirmishing fp: " + skirmish.getFellowshipCharacter().getBlueprint().getFullName());
                System.out.println("Skirmishing minions: " + skirmish.getShadowCharacters().stream().map(card -> card.getBlueprint().getFullName()).toList());
                System.out.println("Chosen: " + chosen.getBlueprint().getFullName());
            }
            return chosen;
        } else if (skirmishingOptions.size() == 1) {
            PhysicalCard chosen = skirmishingOptions.getFirst();
            if (printDebugMessages) {
                System.out.println("One skirmishing target found");
                System.out.println("Chosen: " + chosen.getBlueprint().getFullName());
            }
            return chosen;
        } else {
            PhysicalCard chosen = skirmishingOptions.stream().max(Comparator.comparingInt(card -> game.getModifiersQuerying().getVitality(game, card))).orElseThrow();
            if (printDebugMessages) {
                System.out.println("More skirmishing targets found, choosing one with most vitality");
                System.out.println("Chosen: " + chosen.getBlueprint().getFullName());
            }
            return chosen;
        }
    }


    private void initSpecialTargetingRules() {
        specialTargetingMap.put("1_360",
                options -> options.stream().max(
                        Comparator.comparingInt((ToIntFunction<PhysicalCard>) card ->
                                        game.getModifiersQuerying().hasKeyword(game, card, Keyword.FIERCE) ? -1 : 1)
                                .thenComparingInt(card ->
                                        game.getModifiersQuerying().getVitality(game, card)))
                        .orElseThrow());
    }
}
