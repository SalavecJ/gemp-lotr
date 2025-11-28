package com.gempukku.lotro.bots.forge.utils;

import com.gempukku.lotro.bots.forge.plan.action2.ActionToTake2;
import com.gempukku.lotro.bots.forge.plan.action2.ChooseSkirmishAction2;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.List;

public class SkirmishOrderUtil {
    private static List<String> fpCharactersToResolveFirst = List.of(

    );
    private static List<String> fpCharactersToResolveLast = List.of(
            "Boromir, Son of Denethor"
    );


    public static ActionToTake2 chooseNextSkirmish(DefaultLotroGame game, List<ActionToTake2> possibleActions) {
        if (!possibleActions.stream().allMatch(actionToTake2 -> actionToTake2 instanceof ChooseSkirmishAction2)) {
            throw new IllegalArgumentException("Not all actions are to choose skirmish order");
        }

        // Preferred first
        for (ActionToTake2 action : possibleActions) {
            ChooseSkirmishAction2 chooseSkirmishAction = (ChooseSkirmishAction2) action;
            PhysicalCard fpCharacter = chooseSkirmishAction.getFpCharacter();
            if (fpCharactersToResolveFirst.contains(fpCharacter.getBlueprint().getFullName())) {
                return action;
            }
        }

        // Ring-bearer then
        PhysicalCard ringBearer = game.getGameState().getRingBearer(game.getGameState().getCurrentPlayerId());
        for (ActionToTake2 action : possibleActions) {
            ChooseSkirmishAction2 chooseSkirmishAction = (ChooseSkirmishAction2) action;
            PhysicalCard fpCharacter = chooseSkirmishAction.getFpCharacter();
            if (fpCharacter.equals(ringBearer)) {
                return action;
            }
        }

        // Not to resolve last
        for (ActionToTake2 action : possibleActions) {
            ChooseSkirmishAction2 chooseSkirmishAction = (ChooseSkirmishAction2) action;
            PhysicalCard fpCharacter = chooseSkirmishAction.getFpCharacter();
            if (!fpCharactersToResolveLast.contains(fpCharacter.getBlueprint().getFullName())) {
                return action;
            }
        }

        // Any
        return possibleActions.getFirst();
    }
}
