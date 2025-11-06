package com.gempukku.lotro.bots.forge.plan;

import com.gempukku.lotro.bots.forge.plan.endstate.RegroupPhaseEndState;
import com.gempukku.lotro.bots.forge.utils.BoardStateUtil;
import com.gempukku.lotro.cards.build.bot.ability2.util.WoundsValueUtil;
import com.gempukku.lotro.cards.build.bot.abstractcard.BotCard;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.game.state.PlannedBoardState;

import java.util.List;

/**
 * Wrapper around RegroupPhaseEndState that provides analysis methods
 * for evaluating the outcome of combat without needing to know the action history.
 */
public class CombatOutcome {
    private final PlannedBoardState initialBoardState;
    private final PlannedBoardState finalBoardState;

    public CombatOutcome(PlannedBoardState initialBoardState, RegroupPhaseEndState regroupPhaseEndState) {
        this.initialBoardState = new PlannedBoardState(initialBoardState);
        this.finalBoardState = new PlannedBoardState(regroupPhaseEndState.getBoardState());
    }

    public PlannedBoardState getInitialBoardState() {
        return initialBoardState;
    }

    public PlannedBoardState getFinalBoardState() {
        return finalBoardState;
    }

    // ========== Win Condition Analysis ==========

    public boolean winsTheGame() {
        BotCard ringBearer = finalBoardState.getRingBearer(finalBoardState.getCurrentFpPlayer());
        if (ringBearer == null || finalBoardState.getDeadPile(finalBoardState.getCurrentFpPlayer()).contains(ringBearer)) {
            return true; // Ring bearer is dead
        } else if (finalBoardState.getBurdens() >= finalBoardState.getResistance()) {
            return true; // Ring bearer corrupted
        }

        return false;
    }

    // ========== Casualty Analysis ==========

    public int getCompanionsCasualties() {
        List<BotCard> initialCompanions = BoardStateUtil.getCompanionsInPlay(initialBoardState);
        List<BotCard> finalCompanions = BoardStateUtil.getCompanionsInPlay(finalBoardState);
        return initialCompanions.size() - finalCompanions.size();
    }

    public int getMinionsCasualties() {
        List<BotCard> initialMinions = BoardStateUtil.getMinionsInPlay(initialBoardState);
        List<BotCard> finalMinions = BoardStateUtil.getMinionsInPlay(finalBoardState);
        return initialMinions.size() - finalMinions.size();
    }

    // ========== Damage Analysis ==========

    private int getWoundCount(PlannedBoardState state, BotCard card) {
        return state.getWounds(card);
    }

    // ========== Board Position Evaluation ==========

    /**
     * Evaluates the outcome from a damage perspective.
     * Focuses on killing companions and wounding them.
     * @return Higher score means better outcome for Shadow in terms of damage dealt
     */
    public double getDamageValue() {
        double score = 0.0;

        // Companion casualties are very valuable
        score += getCompanionsCasualties() * 5.0;

        // Wounds on companions - use WoundsValueUtil for sophisticated evaluation
        // This considers vitality levels, ring bearer status, and unique companion mechanics
        score += getTotalWoundsValueDealtToCompanions();

        // TODO: Add more sophisticated damage evaluation
        // - Consider which specific companions died (high-value targets)

        return score;
    }

    private double getTotalWoundsValueDealtToCompanions() {
        double totalValue = 0.0;
        String shadowPlayer = finalBoardState.getCurrentShadowPlayer();

        // Get all companions from initial state (to include those that died)
        List<BotCard> initialCompanions = BoardStateUtil.getCompanionsInPlay(initialBoardState);
        List<BotCard> finalCompanions = BoardStateUtil.getCompanionsInPlay(finalBoardState);

        for (BotCard initialCompanion : initialCompanions) {
            // Find the same companion in the final state (match by card ID)
            BotCard finalCompanion = finalCompanions.stream()
                    .filter(card -> card.getSelf().getCardId() == initialCompanion.getSelf().getCardId())
                    .findFirst()
                    .orElse(null);

            if (finalCompanion != null) {
                // Companion survived - evaluate wounds dealt
                int initialWounds = getWoundCount(initialBoardState, initialCompanion);
                int finalWounds = getWoundCount(finalBoardState, finalCompanion);
                int woundsDealt = finalWounds - initialWounds;

                if (woundsDealt > 0) {
                    // WoundsValueUtil returns positive value for Shadow wounding FP
                    double woundValue = WoundsValueUtil.evaluateWoundsChangeValue(
                            shadowPlayer,
                            initialBoardState,
                            initialCompanion,
                            woundsDealt
                    );
                    totalValue += woundValue;
                }
            } else {
                // Companion died - evaluate the vitality lost (all wounds that led to death)
                int initialVitality = initialBoardState.getVitality(initialCompanion);
                if (initialVitality > 0) {
                    // Evaluate the wounds that killed the companion
                    double woundValue = WoundsValueUtil.evaluateWoundsChangeValue(
                            shadowPlayer,
                            initialBoardState,
                            initialCompanion,
                            initialVitality
                    );
                    totalValue += woundValue;
                }
            }
        }

        return totalValue;
    }

    /**
     * Evaluates the outcome from a stopping perspective.
     * Focuses on preventing double moves and slowing down FP progress.
     * @return Higher score means better outcome for Shadow in terms of stopping FP
     */
    public double getStoppingValue() {
        // If FP has already made 2 moves this turn, stopping is irrelevant
        if (finalBoardState.getMovesMade() >= 2) {
            return 0.0;
        }

        double score = 0.0;

        // Minions remaining on board after combat are the most valuable for stopping
        // They persist if FP moves again and will force another combat
        int minionsRemaining = BoardStateUtil.getMinionsInPlay(finalBoardState).size();
        score += minionsRemaining * 8.0;

        // Minions killed reduce stopping power because:
        // Shadow draws back to 8 cards during regroup, but only ~50% will be shadow cards
        // More minions played/killed = more cards drawn, but FP knows many won't be shadow
        // FP is more confident to double move when Shadow has played many cards (less expected shadow threat)
        int minionsKilled = getMinionsCasualties();
        score -= minionsKilled * 3.0;

        // Setup value already includes persistent shadow cards (conditions, possessions not on minions)
        // We reuse that logic here since good setup discourages double moves
        score += getSetupValue() * 0.6;

        // Damage value (companions killed/wounded) also discourages double moves
        // FP is less willing to move again if they've taken heavy casualties or wounds
        score += getDamageValue() * 0.4;

        // Shadow cards attached to minions also persist if FP moves, adding to stopping power
        int shadowCardsAttachedToMinions = getShadowCardsAttachedToMinions();
        score += shadowCardsAttachedToMinions * 3.0;

        return score;
    }

    private int getShadowCardsAttachedToMinions() {
        int total = 0;
        List<BotCard> minions = BoardStateUtil.getMinionsInPlay(finalBoardState);
        for (BotCard minion : minions) {
            total += (int) finalBoardState.getAttachedCards(minion).stream()
                    .filter(card -> card.getSelf().getBlueprint().getSide() == Side.SHADOW)
                    .count();
        }
        return total;
    }

    /**
     * Evaluates the outcome from a setup perspective.
     * Focuses on board position for future turns.
     * @return Higher score means better setup for Shadow for future turns
     */
    public double getSetupValue() {
        double score = 0.0;

        // Persistent shadow cards (conditions, possessions not on minions) are valuable for future turns
        int initialPersistentShadowCards = BoardStateUtil.getPersistentShadowCards(initialBoardState).size();
        int finalPersistentShadowCards = BoardStateUtil.getPersistentShadowCards(finalBoardState).size();
        int persistentShadowCardsChange = finalPersistentShadowCards - initialPersistentShadowCards;
        score += persistentShadowCardsChange * 5.0;

        // FP non-companion cards lost (possessions, allies, conditions) are valuable for Shadow
        // Companions excluded as they're counted in damage value
        int initialFpNonCompanionCards = BoardStateUtil.getFpNonCompanionCards(initialBoardState).size();
        int finalFpNonCompanionCards = BoardStateUtil.getFpNonCompanionCards(finalBoardState).size();
        int fpCardsLost = initialFpNonCompanionCards - finalFpNonCompanionCards;
        score += fpCardsLost * 4.0;

        // Ally vitality loss is valuable (reduces their ability to use exert abilities)
        int totalAllyVitalityLost = getTotalAllyVitalityLost();
        score += totalAllyVitalityLost * 2;

        return score;
    }

    private int getTotalAllyVitalityLost() {
        int totalVitalityLost = 0;

        // Get all allies from the initial state
        List<BotCard> initialAllies = BoardStateUtil.getAlliesInPlay(initialBoardState);
        List<BotCard> finalAllies = BoardStateUtil.getAlliesInPlay(finalBoardState);

        for (BotCard initialAlly : initialAllies) {
            // Find the same ally in the final state (match by card ID)
            BotCard finalAlly = finalAllies.stream()
                    .filter(card -> card.getSelf().getCardId() == initialAlly.getSelf().getCardId())
                    .findFirst()
                    .orElse(null);

            int initialVitality = initialBoardState.getVitality(initialAlly);
            if (finalAlly != null) {
                // Ally survived - count vitality lost from wounds
                int finalVitality = finalBoardState.getVitality(finalAlly);
                int vitalityLost = initialVitality - finalVitality;

                if (vitalityLost > 0) {
                    totalVitalityLost += vitalityLost;
                }
            } else {
                // Ally died - count its full initial vitality as lost
                totalVitalityLost += initialVitality;
            }
        }

        return totalVitalityLost;
    }

    private int getTotalNonRingbearerVitality(PlannedBoardState plannedBoardState) {
        BotCard ringBearer = plannedBoardState.getRingBearer(plannedBoardState.getCurrentFpPlayer());
        List<BotCard> companions = BoardStateUtil.getCompanionsInPlay(plannedBoardState);

        int totalVitality = 0;
        for (BotCard companion : companions) {
            // Skip the ring bearer
            if (ringBearer != null && companion.getSelf().getCardId() == ringBearer.getSelf().getCardId()) {
                continue;
            }

            totalVitality += plannedBoardState.getVitality(companion);
        }

        return totalVitality;
    }

    private int getTotalWoundsOnCompanions(PlannedBoardState plannedBoardState) {
        List<BotCard> companions = BoardStateUtil.getCompanionsInPlay(plannedBoardState);
        int totalWounds = 0;

        for (BotCard companion : companions) {
            totalWounds += getWoundCount(plannedBoardState, companion);
        }

        return totalWounds;
    }

    // ========== Combined Evaluation ==========

    /**
     * Evaluates the overall value of this combat outcome for the Shadow player.
     * Combines damage, stopping, setup, and winning metrics into a single score.
     * Higher scores are better for Shadow.
     *
     * @return A single evaluation score for use in minimax tree search
     */
    public double evaluateOutcome() {
        // Winning the game is infinitely valuable - short circuit
        if (winsTheGame()) {
            return Double.POSITIVE_INFINITY;
        }

        double score = 0.0;

        // Base weights for each component
        double damageWeight = 1.0;
        double stoppingWeight = 1.0;
        double setupWeight = 1.0;

        // Adjust weights based on game state context

        // If FP has already moved twice, stopping is irrelevant
        if (finalBoardState.getMovesMade() >= 2) {
            stoppingWeight = 0.0;
        }

        // Setup becomes more valuable if we're not winning through damage
        // If we haven't killed companions or dealt significant wounds, setup for next turn matters more
        if (getCompanionsCasualties() == 0 && getDamageValue() < 5.0) {
            setupWeight = 1.5;
        }

        // Damage becomes critical if non-ringbearer companions have low total vitality
        // This indicates FP is weak, and we should press the advantage
        int totalNonRingbearerVitality = getTotalNonRingbearerVitality(finalBoardState);
        if (totalNonRingbearerVitality <= 5) {
            damageWeight = 2.0;
        }

        // At sanctuaries (3 and 6), damage is worthless if minimal
        // Only applies if no companions were killed and total wounds are 5 or less
        if (getCompanionsCasualties() == 0
                && getTotalWoundsOnCompanions(finalBoardState) <= 5
                && finalBoardState.getCurrentSite().getSelf().getBlueprint().getSiteNumber() == 3 || finalBoardState.getCurrentSite().getSelf().getBlueprint().getSiteNumber() == 6) {
            damageWeight = 0.0;
        }

        // Calculate weighted score
        score += getDamageValue() * damageWeight;
        score += getStoppingValue() * stoppingWeight;
        score += getSetupValue() * setupWeight;

        return score;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        // Companions analysis
        List<BotCard> initialCompanions = BoardStateUtil.getCompanionsInPlay(initialBoardState);
        List<BotCard> finalCompanions = BoardStateUtil.getCompanionsInPlay(finalBoardState);

        // Find killed companions
        List<String> killedCompanions = initialCompanions.stream()
                .filter(initialComp -> finalCompanions.stream()
                        .noneMatch(finalComp -> finalComp.getSelf().getCardId() == initialComp.getSelf().getCardId()))
                .map(card -> card.getSelf().getBlueprint().getFullName())
                .toList();

        // Find wounded companions (survived but took damage)
        List<String> woundedCompanions = initialCompanions.stream()
                .filter(initialComp -> {
                    BotCard finalComp = finalCompanions.stream()
                            .filter(fc -> fc.getSelf().getCardId() == initialComp.getSelf().getCardId())
                            .findFirst()
                            .orElse(null);
                    if (finalComp == null) return false; // Dead, not wounded

                    int initialWounds = getWoundCount(initialBoardState, initialComp);
                    int finalWounds = getWoundCount(finalBoardState, finalComp);
                    return finalWounds > initialWounds;
                })
                .map(card -> {
                    BotCard finalComp = finalCompanions.stream()
                            .filter(fc -> fc.getSelf().getCardId() == card.getSelf().getCardId())
                            .findFirst()
                            .orElseThrow();
                    int initialWounds = getWoundCount(initialBoardState, card);
                    int finalWounds = getWoundCount(finalBoardState, finalComp);
                    int woundsDealt = finalWounds - initialWounds;
                    return card.getSelf().getBlueprint().getFullName() + " (+" + woundsDealt + " wound" + (woundsDealt > 1 ? "s" : "") + ")";
                })
                .toList();

        sb.append("Companions killed: ");
        if (killedCompanions.isEmpty()) {
            sb.append("none");
        } else {
            sb.append(String.join(", ", killedCompanions));
        }
        sb.append("\n");

        sb.append("Companions wounded: ");
        if (woundedCompanions.isEmpty()) {
            sb.append("none");
        } else {
            sb.append(String.join(", ", woundedCompanions));
        }
        sb.append("\n");

        // Allies analysis
        List<BotCard> initialAllies = BoardStateUtil.getAlliesInPlay(initialBoardState);
        List<BotCard> finalAllies = BoardStateUtil.getAlliesInPlay(finalBoardState);

        // Find killed allies
        List<String> killedAllies = initialAllies.stream()
                .filter(initialAlly -> finalAllies.stream()
                        .noneMatch(finalAlly -> finalAlly.getSelf().getCardId() == initialAlly.getSelf().getCardId()))
                .map(card -> card.getSelf().getBlueprint().getFullName())
                .toList();

        // Find wounded allies (survived but took damage)
        List<String> woundedAllies = initialAllies.stream()
                .filter(initialAlly -> {
                    BotCard finalAlly = finalAllies.stream()
                            .filter(fa -> fa.getSelf().getCardId() == initialAlly.getSelf().getCardId())
                            .findFirst()
                            .orElse(null);
                    if (finalAlly == null) return false; // Dead, not wounded

                    int initialWounds = getWoundCount(initialBoardState, initialAlly);
                    int finalWounds = getWoundCount(finalBoardState, finalAlly);
                    return finalWounds > initialWounds;
                })
                .map(card -> {
                    BotCard finalAlly = finalAllies.stream()
                            .filter(fa -> fa.getSelf().getCardId() == card.getSelf().getCardId())
                            .findFirst()
                            .orElseThrow();
                    int initialWounds = getWoundCount(initialBoardState, card);
                    int finalWounds = getWoundCount(finalBoardState, finalAlly);
                    int woundsDealt = finalWounds - initialWounds;
                    return card.getSelf().getBlueprint().getFullName() + " (+" + woundsDealt + " wound" + (woundsDealt > 1 ? "s" : "") + ")";
                })
                .toList();

        sb.append("Allies killed: ");
        if (killedAllies.isEmpty()) {
            sb.append("none");
        } else {
            sb.append(String.join(", ", killedAllies));
        }
        sb.append("\n");

        sb.append("Allies wounded: ");
        if (woundedAllies.isEmpty()) {
            sb.append("none");
        } else {
            sb.append(String.join(", ", woundedAllies));
        }
        sb.append("\n");

        // Minions analysis
        List<BotCard> initialMinions = BoardStateUtil.getMinionsInPlay(initialBoardState);
        List<BotCard> finalMinions = BoardStateUtil.getMinionsInPlay(finalBoardState);

        // Find killed minions
        List<String> killedMinions = initialMinions.stream()
                .filter(initialMin -> finalMinions.stream()
                        .noneMatch(finalMin -> finalMin.getSelf().getCardId() == initialMin.getSelf().getCardId()))
                .map(card -> card.getSelf().getBlueprint().getFullName())
                .toList();

        // Find surviving minions
        List<String> survivingMinions = finalMinions.stream()
                .map(card -> card.getSelf().getBlueprint().getFullName())
                .toList();

        sb.append("Minions killed: ");
        if (killedMinions.isEmpty()) {
            sb.append("none");
        } else {
            sb.append(String.join(", ", killedMinions));
        }
        sb.append("\n");

        sb.append("Minions surviving: ");
        if (survivingMinions.isEmpty()) {
            sb.append("none");
        } else {
            sb.append(String.join(", ", survivingMinions));
        }

        return sb.toString();
    }
}
