package com.gempukku.lotro.bots.forge.cards.ability;

import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Race;

import java.util.HashMap;
import java.util.Map;

public class Effect {
    public static final int ALL = Integer.MAX_VALUE;

    public static AbilityProperty conditional(AbilityProperty condition, AbilityProperty ifOk, AbilityProperty ifNotOk) {
        return new AbilityProperty(AbilityProperty.Type.IF_ELSE,
                new HashMap<>(Map.of("condition", condition,
                        "if", ifOk,
                        "else", ifNotOk)));

    }

    public static AbilityProperty choice(AbilityProperty a, AbilityProperty b) {
        return new AbilityProperty(AbilityProperty.Type.CHOICE,
                new HashMap<>(Map.of("optionA", a,
                        "optionB", b)));
    }

    public static AbilityProperty wearRingUntilRegroup() {
        return new AbilityProperty(AbilityProperty.Type.WEAR_RING_UNTIL_REGROUP);
    }

    public static AbilityProperty preventWoundToBearerForBurden(int burdens) {
        return new AbilityProperty(AbilityProperty.Type.PREVENT_WOUND_TO_BEARER_FOR_BURDEN,
                new HashMap<>(Map.of("burdens", burdens)));
    }

    public static AbilityProperty putCardFromHandToBottomOfDeck() {
        return putCardFromHandToBottomOfDeck(1);
    }

    public static AbilityProperty putCardFromHandToBottomOfDeck(int numberOfCards) {
        return new AbilityProperty(AbilityProperty.Type.PUT_CARD_FROM_HAND_TO_BOTTOM_OF_DECK,
                new HashMap<>(Map.of("numberOfCards", numberOfCards)));
    }

    public static AbilityProperty putCardFromDiscardToBottomOfDeck() {
        return putCardFromDiscardToBottomOfDeck(1);
    }

    public static AbilityProperty putCardFromDiscardToBottomOfDeck(int numberOfCards) {
        return new AbilityProperty(AbilityProperty.Type.PUT_CARD_FROM_DISCARD_TO_BOTTOM_OF_DECK,
                new HashMap<>(Map.of("numberOfCards", numberOfCards)));
    }

    public static AbilityProperty putCardFromDiscardIntoHand() {
        return putCardFromDiscardIntoHand(1);
    }

    public static AbilityProperty putCardFromDiscardIntoHand(int numberOfCards) {
        return new AbilityProperty(AbilityProperty.Type.PUT_CARD_FROM_DISCARD_INTO_HAND,
                new HashMap<>(Map.of("numberOfCards", numberOfCards)));
    }

    public static AbilityProperty modifyStrength(int amount) {
        return modifyStrength(amount, 1);
    }

    public static AbilityProperty modifyStrength(int amount, int numberOfTargets) {
        return new AbilityProperty(AbilityProperty.Type.MODIFY_STRENGTH,
                new HashMap<>(Map.of("amount", amount,
                        "numberOfTargets", numberOfTargets)));
    }

    public static AbilityProperty modifyStrengthUntilRegroup(int amount) {
        return modifyStrengthUntilRegroup(amount, 1);
    }

    public static AbilityProperty modifyStrengthUntilRegroup(int amount, int numberOfTargets) {
        return new AbilityProperty(AbilityProperty.Type.MODIFY_STRENGTH,
                new HashMap<>(Map.of("amount", amount,
                        "untilRegroup", true,
                        "numberOfTargets", numberOfTargets)));
    }

    public static AbilityProperty modifyStrengthPerSpotted(int amount, Race race, boolean includeSelf) {
        return modifyStrengthPerSpotted(amount, race, includeSelf, 1);
    }

    public static AbilityProperty modifyStrengthPerSpotted(int amount, Race race, boolean includeSelf, int numberOfTargets) {
        return new AbilityProperty(AbilityProperty.Type.MODIFY_STRENGTH,
                new HashMap<>(Map.of("amount", amount,
                        "numberOfTargets", numberOfTargets,
                        "perSpottedRace", race,
                        "includeSelf", includeSelf)));
    }

    public static AbilityProperty modifyStrengthWithLimit(int amount, int limit) {
        return modifyStrengthWithLimit(amount, limit, 1);
    }

    public static AbilityProperty modifyStrengthWithLimit(int amount, int limit, int numberOfTargets) {
        return new AbilityProperty(AbilityProperty.Type.MODIFY_STRENGTH,
                new HashMap<>(Map.of("amount", amount,
                        "limit", limit,
                        "numberOfTargets", numberOfTargets)));
    }

    public static AbilityProperty discardFromPlay() {
        return discardFromPlay(1);
    }

    public static AbilityProperty discardFromPlay(int numberOfTargets) {
        return new AbilityProperty(AbilityProperty.Type.DISCARD_FROM_PLAY,
                new HashMap<>(Map.of("numberOfTargets", numberOfTargets)));
    }

    public static AbilityProperty preventNextWound() {
        return preventNextWound(1);
    }

    public static AbilityProperty preventNextWound(int amount) {
        return new AbilityProperty(AbilityProperty.Type.PREVENT_WOUND,
                new HashMap<>(Map.of("amount", amount)));
    }

    public static AbilityProperty revealOpponentsHand() {
        return new AbilityProperty(AbilityProperty.Type.REVEAL_OPPONENTS_HAND);
    }

    public static AbilityProperty limitWoundsTakenPerPhase(Phase phase, int amount) {
        return new AbilityProperty(AbilityProperty.Type.LIMIT_WOUNDS_TAKEN_PER_PHASE,
                new HashMap<>(Map.of("amount", amount, "phase", phase)));
    }

    public static AbilityProperty heal() {
        return heal(1);
    }

    public static AbilityProperty heal(int amount) {
        return new AbilityProperty(AbilityProperty.Type.HEAL,
                new HashMap<>(Map.of("amount", amount)));
    }

    public static AbilityProperty removeBurden() {
        return removeBurden(1);
    }

    public static AbilityProperty removeBurden(int amount) {
        return new AbilityProperty(AbilityProperty.Type.REMOVE_BURDEN,
                new HashMap<>(Map.of("amount", amount)));
    }

    public static AbilityProperty setOverwhelmMultiplier(int amount) {
        return new AbilityProperty(AbilityProperty.Type.OVERWHELM_MULTIPLIER,
                new HashMap<>(Map.of("amount", amount)));
    }

    public static AbilityProperty modifyFpArchery(int amount) {
        return new AbilityProperty(AbilityProperty.Type.FP_ARCHERY_MODIFIER,
                new HashMap<>(Map.of("amount", amount)));
    }

    public static AbilityProperty modifyShadowArchery(int amount) {
        return new AbilityProperty(AbilityProperty.Type.SHADOW_ARCHERY_MODIFIER,
                new HashMap<>(Map.of("amount", amount)));
    }

    public static AbilityProperty modifySiteShadowNumber(int amount) {
        return new AbilityProperty(AbilityProperty.Type.SHADOW_NUMBER_MODIFIER,
                new HashMap<>(Map.of("amount", amount)));
    }

    public static AbilityProperty playNextSite() {
        return new AbilityProperty(AbilityProperty.Type.PLAY_NEXT_SITE);
    }

    public static AbilityProperty playFromDiscard() {
        return new AbilityProperty(AbilityProperty.Type.PLAY_FROM_DISCARD);
    }

    public static AbilityProperty playItemFromDiscardOn(Culture culture, Race race) {
        return new AbilityProperty(AbilityProperty.Type.PLAY_FROM_DISCARD,
                new HashMap<>(Map.of("playItemOnCulture", culture,
                        "playItemOnRace", race)));
    }

    public static AbilityProperty modifyTwilightCost(int amount) {
        return new AbilityProperty(AbilityProperty.Type.MODIFY_TWILIGHT_COST,
                new HashMap<>(Map.of("amount", amount)));
    }

    public static AbilityProperty freePeoplesPlayerMillsCards(int amount) {
        return new AbilityProperty(AbilityProperty.Type.MILL_CARDS,
                new HashMap<>(Map.of("amount", amount,
                        "player", "fp")));
    }

    public static AbilityProperty drawCards(int amount) {
        return new AbilityProperty(AbilityProperty.Type.DRAW_CARDS,
                new HashMap<>(Map.of("amount", amount)));
    }

    public static AbilityProperty makeSamRingBearer() {
        return new AbilityProperty(AbilityProperty.Type.MAKE_SAM_RING_BEARER);
    }

    public static AbilityProperty modifyMoveLimit(int amount) {
        return new AbilityProperty(AbilityProperty.Type.MOVE_LIMIT_MODIFIER,
                new HashMap<>(Map.of("amount", amount)));
    }

    public static AbilityProperty cancelSkirmishingInvolving(Race race) {
        return new AbilityProperty(AbilityProperty.Type.CANCEL_SKIRMISH,
                new HashMap<>(Map.of("race", race)));
    }

    public static AbilityProperty playWithTwilightCostModification(int twilightCostModification) {
        return new AbilityProperty(AbilityProperty.Type.PLAY_FROM_HAND,
                new HashMap<>(Map.of("twilightCostModification", twilightCostModification)));
    }

    public static AbilityProperty playFromDeckWithTwilightCostModification(int twilightCostModification) {
        return new AbilityProperty(AbilityProperty.Type.PLAY_FROM_DECK,
                new HashMap<>(Map.of("twilightCostModification", twilightCostModification)));
    }

    public static AbilityProperty addKeyword(Keyword keyword) {
        return new AbilityProperty(AbilityProperty.Type.ADD_KEYWORD,
                new HashMap<>(Map.of("keyword", keyword)));
    }

    public static AbilityProperty addKeywordUntilRegroup(Keyword keyword) {
        return new AbilityProperty(AbilityProperty.Type.ADD_KEYWORD,
                new HashMap<>(Map.of("keyword", keyword,
                        "untilRegroup", true)));
    }

    public static AbilityProperty playWithBonusEffect(AbilityProperty bonusEffect) {
        return new AbilityProperty(AbilityProperty.Type.PLAY_FROM_HAND,
                new HashMap<>(Map.of("bonusEffect", bonusEffect)));
    }

    public static AbilityProperty exert(int amount) {
        return exert(amount, 1);
    }

    public static AbilityProperty exert(int amount, int numberOfTargets) {
        return new AbilityProperty(AbilityProperty.Type.EXERT,
                new HashMap<>(Map.of("amount", amount,
                        "numberOfTargets", numberOfTargets)));
    }
}
