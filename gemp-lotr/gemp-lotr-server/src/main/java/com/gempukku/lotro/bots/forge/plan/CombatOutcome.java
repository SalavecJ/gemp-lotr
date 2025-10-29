package com.gempukku.lotro.bots.forge.plan;

import com.gempukku.lotro.bots.forge.utils.BoardStateUtil;
import com.gempukku.lotro.cards.build.bot.abstractcard.BotCard;
import com.gempukku.lotro.game.state.PlannedBoardState;

import java.util.List;

public class CombatOutcome {
    private final PlannedBoardState boardState;

    public CombatOutcome(PlannedBoardState afterShadowPhaseState) {
        this.boardState = new PlannedBoardState(afterShadowPhaseState);
    }

    // TODO - rough implementation

    public boolean hasPotentialToWinTheGameForShadowPlayer() {
        List<BotCard> fpCharactersThatCanFight = BoardStateUtil.getCompanionsAndAlliesAtHome(boardState);
        List<BotCard> minions = BoardStateUtil.getMinionsInPlay(boardState);

        if (minions.size() >= fpCharactersThatCanFight.size()) {
            // overwhelm check
            int minionsOnRingbearer = minions.size() - fpCharactersThatCanFight.size() + 1;
            minions.sort((o1, o2) -> Integer.compare(boardState.getStrength(o1), boardState.getStrength(o2)));
            int minionsStrength = 0;
            for (int i = 0; i < minionsOnRingbearer; i++) {
                minionsStrength += boardState.getStrength(minions.get(i));
            }

            int rbStrength = boardState.getStrength(boardState.getRingBearer(boardState.getCurrentFpPlayer()));

            if (minionsStrength >= 2 * rbStrength) {
                return true;
            }
        }

        return false;
    }
}
