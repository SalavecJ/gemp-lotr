package com.gempukku.lotro.bots.forge.cards.abstractcards;

import com.gempukku.lotro.bots.forge.cards.ability.AbilityStep;
import com.gempukku.lotro.bots.forge.cards.ability.EventAbility;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.ArrayList;
import java.util.List;

public class BotEventCard extends BotCard {
    private final List<Timeword> timewords = new ArrayList<>();

    public BotEventCard(PhysicalCard physicalCard, Timeword timeword) {
        super(physicalCard);
        this.timewords.add(timeword);
    }

    public BotEventCard(PhysicalCard physicalCard, List<Timeword> timewords) {
        super(physicalCard);
        this.timewords.addAll(timewords);
    }

    public List<Timeword> getTimewords() {
        return new ArrayList<>(timewords);
    }

    public EventAbility getEventAbility() {
        List<EventAbility> eventAbilities = getAbilities().stream().filter(ability1 -> ability1 instanceof EventAbility).map(ability -> (EventAbility) ability).toList();
        if (eventAbilities.size() > 1) {
            throw new IllegalStateException("Event card has more than one event ability");
        }
        if (eventAbilities.isEmpty()) {
            throw new IllegalStateException("Event card does not have an event ability");
        }
        return eventAbilities.getFirst();
    }

    public double valueIfPlayed(DefaultLotroGame game, String playerName) {
        EventAbility eventAbility = getEventAbility();
        double value = 0;
        for (AbilityStep step : eventAbility.getSteps()) {
            value += step.getValue(game, playerName);
        }
        return value;
    }
}
