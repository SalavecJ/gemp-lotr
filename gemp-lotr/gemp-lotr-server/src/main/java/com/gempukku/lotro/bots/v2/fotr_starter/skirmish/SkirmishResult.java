package com.gempukku.lotro.bots.v2.fotr_starter.skirmish;

import com.gempukku.lotro.bots.v2.simulation.SimulatedGame;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.Skirmish;

import java.util.ArrayList;
import java.util.List;

import static com.gempukku.lotro.bots.v2.fotr_starter.utility.GameQueries.getVitality;
import static com.gempukku.lotro.bots.v2.fotr_starter.utility.GameQueries.isInPlay;

public class SkirmishResult {
    private static final String FRODO = "Frodo, Son of Drogo";

    private final PhysicalCard fpCharacter;
    private final int fpCharacterId;
    private final int fpCharacterVitality;

    private final List<PhysicalCard> minions;
    private final List<Integer> minionIds;
    private final List<Integer> minionVitalities;

    private final int fpCharacterVitalityIfAllPass;
    private final List<Integer> minionVitalitiesIfAllPass;

    public SkirmishResult(SimulatedGame beforeSkirmish, SimulatedGame afterSkirmish) {
        Skirmish skirmish = beforeSkirmish.getGame().getGameState().getSkirmish();
        this.fpCharacter = skirmish.getFellowshipCharacter();
        this.fpCharacterId = fpCharacter.getCardId();
        this.fpCharacterVitality = getVitality(beforeSkirmish, fpCharacter);

        this.minions = new ArrayList<>(skirmish.getShadowCharacters());
        this.minionIds = new ArrayList<>(minions.stream().map(PhysicalCard::getCardId).toList());
        this.minionVitalities = new ArrayList<>(minions.stream().map(card -> getVitality(beforeSkirmish, card)).toList());


        fpCharacterVitalityIfAllPass = isInPlay(afterSkirmish, fpCharacter) ? getVitality(afterSkirmish, fpCharacter) : 0;
        minionVitalitiesIfAllPass = new ArrayList<>(minions.stream().map(card -> isInPlay(afterSkirmish, card) ? getVitality(afterSkirmish, card) : 0).toList());
    }

    public boolean fpCharacterSurvived() {
        return fpCharacterVitalityIfAllPass > 0;
    }

    public int fpCharacterVitalityLost() {
        return fpCharacterVitality - fpCharacterVitalityIfAllPass;
    }

    public boolean allMinionsDied() {
        return minionVitalitiesIfAllPass.stream().allMatch(vitality -> vitality <= 0);
    }

    public int score() {
        if (allMinionsDied()) {
            return 1;
        } else if (fpCharacterVitalityLost() == 0) {
            return 0;
        } else if (fpCharacterSurvived()) {
            return -5 + fpCharacterVitalityIfAllPass;
        } else if (fpCharacter.getBlueprint().getFullName().equals(FRODO)) {
            return -100;
        } else {
            return -10 - fpCharacterVitalityLost();
        }
    }
}
