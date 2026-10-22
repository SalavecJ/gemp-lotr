package com.gempukku.lotro.bots.v2.decision.option;

import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.Assignment;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.List;

public class NextSkirmishOption extends Option {
    private final PhysicalCard fpCard;
    private final List<PhysicalCard> shadowCards;


    public NextSkirmishOption(PhysicalCard physicalCard, DefaultLotroGame game) {
        super(getHumanReadableOption(physicalCard, game), String.valueOf(physicalCard.getCardId()));
        fpCard = physicalCard;
        shadowCards = game.getGameState().getAssignments().stream()
                .filter(assignment -> assignment.getFellowshipCharacter().getCardId() == physicalCard.getCardId())
                .flatMap(assignment -> assignment.getShadowCharacters().stream())
                .toList();
    }

    private static String getHumanReadableOption(PhysicalCard physicalCard, DefaultLotroGame game) {
        for (Assignment assignment : game.getGameState().getAssignments()) {
            if (assignment.getFellowshipCharacter().getCardId() == physicalCard.getCardId()) {
                String minions = String.join(", ", assignment.getShadowCharacters().stream().map(PhysicalCard::getBlueprint).map(LotroCardBlueprint::getFullName).toList());
                return "Choose " + physicalCard.getBlueprint().getFullName() + " vs " + minions;
            }
        }
        throw new IllegalArgumentException("Card " + physicalCard.getBlueprint().getFullName() + " is not assigned to any minions");
    }


    public PhysicalCard getFpCard() {
        return fpCard;
    }

    public List<PhysicalCard> getShadowCards() {
        return shadowCards;
    }
}
