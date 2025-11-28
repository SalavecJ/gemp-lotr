package com.gempukku.lotro.bots.forge.plan;

import com.gempukku.lotro.bots.forge.utils.BoardStateUtil;
import com.gempukku.lotro.bots.forge.cards.ability2.util.WoundsValueUtil;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.modifiers.ModifierFlag;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;
import com.gempukku.lotro.logic.timing.RuleUtils;

import java.util.List;

/**
 * Wrapper around RegroupPhaseEndState that provides analysis methods
 * for evaluating the outcome of combat without needing to know the action history.
 */
public class CombatOutcome {
    private final DefaultLotroGame initialBoardState;
    private final DefaultLotroGame finalBoardState;

    public CombatOutcome(DefaultLotroGame initialBoardState, DefaultLotroGame finalBoardState) {
        this.initialBoardState = initialBoardState;
        this.finalBoardState = finalBoardState;
    }

    // ========== Win Condition Analysis ==========

    public boolean winsTheGame() {
        return finalBoardState.getWinnerPlayerId() != null && finalBoardState.getWinnerPlayerId().equals(initialBoardState.getGameState().getCurrentShadowPlayer());
    }

    // ========== Casualty Analysis ==========

    public int getCompanionsCasualties() {
        List<PhysicalCard> initialCompanions = BoardStateUtil.getCompanionsInPlay(initialBoardState);
        List<PhysicalCard> finalCompanions = BoardStateUtil.getCompanionsInPlay(finalBoardState);
        return initialCompanions.size() - finalCompanions.size();
    }

    public int getMinionsCasualties() {
        List<PhysicalCard> initialMinions = BoardStateUtil.getMinionsInPlay(initialBoardState);
        List<PhysicalCard> finalMinions = BoardStateUtil.getMinionsInPlay(finalBoardState);
        return initialMinions.size() - finalMinions.size();
    }

    // ========== Damage Analysis ==========

    private int getWoundCount(DefaultLotroGame game, PhysicalCard card) {
        return game.getGameState().getWounds(card);
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
        String shadowPlayer = initialBoardState.getGameState().getCurrentShadowPlayer();

        // Get all companions from initial state (to include those that died)
        List<PhysicalCard> initialCompanions = BoardStateUtil.getCompanionsInPlay(initialBoardState);
        List<PhysicalCard> finalCompanions = BoardStateUtil.getCompanionsInPlay(finalBoardState);

        for (PhysicalCard initialCompanion : initialCompanions) {
            // Find the same companion in the final state (match by card ID)
            PhysicalCard finalCompanion = finalCompanions.stream()
                    .filter(card -> card.getCardId() == initialCompanion.getCardId())
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
                int initialVitality = initialBoardState.getModifiersQuerying().getVitality(initialBoardState, initialCompanion);
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
        // Stopping is irrelevant
        if (finalBoardState.getModifiersQuerying().hasFlagActive(finalBoardState, ModifierFlag.CANT_MOVE)) {
            return 0;
        }
        if (finalBoardState.getGameState().getMoveCount() >= RuleUtils.calculateMoveLimit(finalBoardState)) {
            return 0;
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
        List<PhysicalCard> minions = BoardStateUtil.getMinionsInPlay(finalBoardState);
        for (PhysicalCard minion : minions) {
            total += (int) finalBoardState.getGameState().getAttachedCards(minion).stream()
                    .filter(card -> card.getBlueprint().getSide() == Side.SHADOW)
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
        if (finalBoardState.getGameState().getCurrentSite().getBlueprint().getSiteNumber() == 9) {
            // At site 9, setup is irrelevant as the game ends
            return 0.0;
        }

        double score = 0.0;

        // Persistent shadow cards (conditions, possessions not on minions) are valuable for future turns
        int finalPersistentShadowCards = BoardStateUtil.getPersistentShadowCards(finalBoardState).size();
        score += finalPersistentShadowCards * 5.0;

        // FP non-companion cards lost (possessions, allies, conditions) are valuable for Shadow
        // Companions excluded as they're counted in damage value
        int finalFpNonCompanionCards = BoardStateUtil.getFpNonCompanionCards(finalBoardState).size();
        score -= finalFpNonCompanionCards * 4.0;

        // Ally vitality loss is valuable (reduces their ability to use exert abilities)
        int totalAllyVitalityLost = getTotalAllyVitalityLost();
        score += totalAllyVitalityLost * 2;

        return score;
    }

    private int getTotalAllyVitalityLost() {
        int totalVitalityLost = 0;

        // Get all allies from the initial state
        List<PhysicalCard> initialAllies = BoardStateUtil.getAlliesInPlay(initialBoardState);
        List<PhysicalCard> finalAllies = BoardStateUtil.getAlliesInPlay(finalBoardState);

        for (PhysicalCard initialAlly : initialAllies) {
            // Find the same ally in the final state (match by card ID)
            PhysicalCard finalAlly = finalAllies.stream()
                    .filter(card -> card.getCardId() == initialAlly.getCardId())
                    .findFirst()
                    .orElse(null);

            int initialVitality = initialBoardState.getModifiersQuerying().getVitality(initialBoardState, initialAlly);
            if (finalAlly != null) {
                // Ally survived - count vitality lost from wounds
                int finalVitality = finalBoardState.getModifiersQuerying().getVitality(finalBoardState, finalAlly);
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

    private int getTotalNonRingbearerVitality(DefaultLotroGame game) {
        PhysicalCard ringBearer =game.getGameState().getRingBearer(game.getGameState().getCurrentPlayerId());
        List<PhysicalCard> companions = BoardStateUtil.getCompanionsInPlay(game);

        int totalVitality = 0;
        for (PhysicalCard companion : companions) {
            // Skip the ring bearer
            if (ringBearer != null && companion.getCardId() == ringBearer.getCardId()) {
                continue;
            }

            totalVitality += game.getModifiersQuerying().getVitality(game, companion);
        }

        return totalVitality;
    }

    private int getTotalWoundsOnCompanions(DefaultLotroGame game) {
        List<PhysicalCard> companions = BoardStateUtil.getCompanionsInPlay(game);
        int totalWounds = 0;

        for (PhysicalCard companion : companions) {
            totalWounds += getWoundCount(game, companion);
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
        double score = 0.0;

        // Deter fp decisions from losing the game earlier than needed
        if (finalBoardState.getGameState().getCurrentPhase() == Phase.ARCHERY) {
            score += 600.0; // Make fp player not kill the ring bearer in archery phase if possible
        }

        if (finalBoardState.getGameState().getCurrentPhase() == Phase.SKIRMISH && !finalBoardState.getGameState().getAssignments().isEmpty()) {
            score += 300.0; // Make fp player kill the ring bearer in skirmish phase as late as possible if needed
        }

        if (winsTheGame()) {
            score += 1000.0;
        }

        // Base weights for each component
        double damageWeight = 1.0;
        double stoppingWeight = 1.0;
        double setupWeight = 1.0;

        // Adjust weights based on game state context

        // Stopping is irrelevant

        if (finalBoardState.getModifiersQuerying().hasFlagActive(finalBoardState, ModifierFlag.CANT_MOVE)) {
            stoppingWeight = 0.0;
        }
        if (finalBoardState.getGameState().getMoveCount() >= RuleUtils.calculateMoveLimit(finalBoardState)) {
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
                && finalBoardState.getGameState().getCurrentSite().getBlueprint().getSiteNumber() == 3 || finalBoardState.getGameState().getCurrentSite().getBlueprint().getSiteNumber() == 6) {
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
        List<PhysicalCard> initialCompanions = BoardStateUtil.getCompanionsInPlay(initialBoardState);
        List<PhysicalCard> finalCompanions = BoardStateUtil.getCompanionsInPlay(finalBoardState);

        // Find killed companions
        List<String> killedCompanions = initialCompanions.stream()
                .filter(initialComp -> finalCompanions.stream()
                        .noneMatch(finalComp -> finalComp.getCardId() == initialComp.getCardId()))
                .map(card -> card.getBlueprint().getFullName())
                .toList();

        // Find wounded companions (survived but took damage)
        List<String> woundedCompanions = initialCompanions.stream()
                .filter(initialComp -> {
                    PhysicalCard finalComp = finalCompanions.stream()
                            .filter(fc -> fc.getCardId() == initialComp.getCardId())
                            .findFirst()
                            .orElse(null);
                    if (finalComp == null) return false; // Dead, not wounded

                    int initialWounds = getWoundCount(initialBoardState, initialComp);
                    int finalWounds = getWoundCount(finalBoardState, finalComp);
                    return finalWounds > initialWounds;
                })
                .map(card -> {
                    PhysicalCard finalComp = finalCompanions.stream()
                            .filter(fc -> fc.getCardId() == card.getCardId())
                            .findFirst()
                            .orElseThrow();
                    int initialWounds = getWoundCount(initialBoardState, card);
                    int finalWounds = getWoundCount(finalBoardState, finalComp);
                    int woundsDealt = finalWounds - initialWounds;
                    return card.getBlueprint().getFullName() + " (+" + woundsDealt + " wound" + (woundsDealt > 1 ? "s" : "") + ")";
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
        List<PhysicalCard> initialAllies = BoardStateUtil.getAlliesInPlay(initialBoardState);
        List<PhysicalCard> finalAllies = BoardStateUtil.getAlliesInPlay(finalBoardState);

        // Find killed allies
        List<String> killedAllies = initialAllies.stream()
                .filter(initialAlly -> finalAllies.stream()
                        .noneMatch(finalAlly -> finalAlly.getCardId() == initialAlly.getCardId()))
                .map(card -> card.getBlueprint().getFullName())
                .toList();

        // Find wounded allies (survived but took damage)
        List<String> woundedAllies = initialAllies.stream()
                .filter(initialAlly -> {
                    PhysicalCard finalAlly = finalAllies.stream()
                            .filter(fa -> fa.getCardId() == initialAlly.getCardId())
                            .findFirst()
                            .orElse(null);
                    if (finalAlly == null) return false; // Dead, not wounded

                    int initialWounds = getWoundCount(initialBoardState, initialAlly);
                    int finalWounds = getWoundCount(finalBoardState, finalAlly);
                    return finalWounds > initialWounds;
                })
                .map(card -> {
                    PhysicalCard finalAlly = finalAllies.stream()
                            .filter(fa -> fa.getCardId() == card.getCardId())
                            .findFirst()
                            .orElseThrow();
                    int initialWounds = getWoundCount(initialBoardState, card);
                    int finalWounds = getWoundCount(finalBoardState, finalAlly);
                    int woundsDealt = finalWounds - initialWounds;
                    return card.getBlueprint().getFullName() + " (+" + woundsDealt + " wound" + (woundsDealt > 1 ? "s" : "") + ")";
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
        List<PhysicalCard> initialMinions = BoardStateUtil.getMinionsInPlay(initialBoardState);
        List<PhysicalCard> finalMinions = BoardStateUtil.getMinionsInPlay(finalBoardState);

        // Find killed minions
        List<String> killedMinions = initialMinions.stream()
                .filter(initialMin -> finalMinions.stream()
                        .noneMatch(finalMin -> finalMin.getCardId() == initialMin.getCardId()))
                .map(card -> card.getBlueprint().getFullName())
                .toList();

        // Find surviving minions
        List<String> survivingMinions = finalMinions.stream()
                .map(card -> card.getBlueprint().getFullName())
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
        sb.append("\n");

        // Persistent shadow cards analysis
        List<String> persistentShadowCards = BoardStateUtil.getPersistentShadowCards(finalBoardState).stream()
                .map(card -> card.getBlueprint().getFullName())
                .toList();


        sb.append("Persistent shadow cards: ");
        if (persistentShadowCards.isEmpty()) {
            sb.append("none");
        } else {
            sb.append(String.join(", ", persistentShadowCards));
        }

        return sb.toString();
    }
}
