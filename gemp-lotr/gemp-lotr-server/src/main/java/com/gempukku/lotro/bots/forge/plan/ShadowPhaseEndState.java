package com.gempukku.lotro.bots.forge.plan;

import com.gempukku.lotro.bots.forge.plan.action.ActionToTake;
import com.gempukku.lotro.bots.forge.utils.BoardStateUtil;
import com.gempukku.lotro.game.state.PlannedBoardState;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ShadowPhaseEndState {
    private final PlannedBoardState finalBoardState;
    private final List<ActionToTake> actions;
    private final CombatOutcome predictedOutcome;

    public ShadowPhaseEndState(PlannedBoardState finalBoardState, List<ActionToTake> actions) {
        this.finalBoardState = new PlannedBoardState(finalBoardState);
        this.actions = new ArrayList<>(actions);
        this.predictedOutcome = new CombatOutcome(finalBoardState);
    }

    public boolean hasPotentialToWinTheGame() {
        return predictedOutcome.hasPotentialToWinTheGameForShadowPlayer();
    }

    public List<ActionToTake> getActions() {
        return actions;
    }

    @Override
    public String toString() {
        return actions.stream().map(Object::toString).collect(Collectors.joining("\n"));
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ShadowPhaseEndState that = (ShadowPhaseEndState) o;
        return Objects.equals(getTwilight(), that.getTwilight())
                && Objects.equals(getMinionsInPlayAsString(), that.getMinionsInPlayAsString())
                && Objects.equals(getCardsInHandAsString(), that.getCardsInHandAsString());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTwilight(), getMinionsInPlayAsString(), getCardsInHandAsString());
    }

    private int getTwilight() {
        return finalBoardState.getTwilight();
    }

    private String getMinionsInPlayAsString() {
        return BoardStateUtil.getMinionsInPlay(finalBoardState).stream()
                .map(t -> t.getSelf().getBlueprint().getFullName())
                .sorted()
                .collect(Collectors.joining("; "));
    }

    private String getCardsInHandAsString() {
        return finalBoardState.getHand(finalBoardState.getCurrentShadowPlayer()).stream()
                .map(t -> t.getSelf().getBlueprint().getFullName())
                .sorted()
                .collect(Collectors.joining("; "));
    }
}
