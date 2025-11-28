package com.gempukku.lotro.logic.timing.processes.turn.skirmish;

import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.SystemQueueAction;
import com.gempukku.lotro.logic.effects.TriggeringResultEffect;
import com.gempukku.lotro.logic.timing.processes.GameProcess;
import com.gempukku.lotro.logic.timing.processes.turn.AssignmentGameProcess;
import com.gempukku.lotro.logic.timing.processes.turn.RegroupGameProcess;
import com.gempukku.lotro.logic.timing.results.AfterAllSkirmishesResult;

public class EndSkirmishesGameProcess implements GameProcess {
    private final AfterAllSkirmishesResult _afterAllSkirmishesResult = new AfterAllSkirmishesResult();

    public static class CreateExtraAssignmentAndSkirmishPhases implements GameProcess {
        @Override
        public void process(LotroGame game) {
            game.getGameState().setExtraSkirmishes(true);
        }

        @Override
        public GameProcess getNextProcess() {
            return new AssignmentGameProcess();
        }

        @Override
        public GameProcess copyThisForNewGame(LotroGame game) {
            return new CreateExtraAssignmentAndSkirmishPhases();
        }
    }

    @Override
    public void process(LotroGame game) {
        SystemQueueAction action = new SystemQueueAction();
        action.setText("After all skirmishes");
        action.appendEffect(
                new TriggeringResultEffect(null, _afterAllSkirmishesResult, "After all skirmishes"));
        game.getActionsEnvironment().addActionToStack(action);
    }

    @Override
    public GameProcess getNextProcess() {
        if (_afterAllSkirmishesResult.isCreateAnExtraAssignmentAndSkirmishPhases()) {
            return new CreateExtraAssignmentAndSkirmishPhases();
        } else {
            return new RegroupGameProcess();
        }
    }

    @Override
    public GameProcess copyThisForNewGame(LotroGame game) {
        EndSkirmishesGameProcess copy = new EndSkirmishesGameProcess();
        copy._afterAllSkirmishesResult.setCreateAnExtraAssignmentAndSkirmishPhases(_afterAllSkirmishesResult.isCreateAnExtraAssignmentAndSkirmishPhases());
        return copy;
    }
}
