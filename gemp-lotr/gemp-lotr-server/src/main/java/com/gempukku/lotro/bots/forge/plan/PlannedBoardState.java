package com.gempukku.lotro.bots.forge.plan;

import com.gempukku.lotro.cards.build.bot.BotCardFactory;
import com.gempukku.lotro.cards.build.bot.abstractcard.BotCard;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class PlannedBoardState {
    private final Set<BotCard> companions = new HashSet<>();
    private final Set<BotCard> allies = new HashSet<>();
    private final Set<BotCard> conditions = new HashSet<>();
    private final Set<BotCard> possessions = new HashSet<>();

    private final Map<BotCard, Set<BotCard>> borneCards = new HashMap<>();

    public PlannedBoardState(LotroGame game, String player) {
        game.getGameState().getInPlay().stream()
                .filter((Predicate<PhysicalCard>) card -> card.getOwner().equals(player))
                .filter((Predicate<PhysicalCard>) card -> game.getGameState().isCardInPlayActive(card))
                .forEach((Consumer<PhysicalCard>) card -> {
                    BotCard botCard = BotCardFactory.create(card);
                    switch (card.getBlueprint().getCardType()) {
                        case COMPANION -> companions.add(botCard);
                        case ALLY -> allies.add(botCard);
                        case POSSESSION -> possessions.add(botCard);
                        case CONDITION -> conditions.add(botCard);
                    }
                });

        conditions.forEach(botCard -> {
            PhysicalCard attachedTo = botCard.getSelf().getAttachedTo();
            if (attachedTo != null) {
                BotCard bearer = findBearer(attachedTo);
                if (bearer != null) {
                    borneCards.computeIfAbsent(bearer, k -> new HashSet<>()).add(botCard);
                }
            }
        });

        possessions.forEach(botCard -> {
            PhysicalCard attachedTo = botCard.getSelf().getAttachedTo();
            if (attachedTo != null) {
                BotCard bearer = findBearer(attachedTo);
                if (bearer != null) {
                    borneCards.computeIfAbsent(bearer, k -> new HashSet<>()).add(botCard);
                }
            }
        });
    }

    public void addCompanion(BotCard botCard) {
        companions.add(botCard);
    }

    public void addAlly(BotCard botCard) {
        allies.add(botCard);
    }

    private BotCard findBearer(PhysicalCard attachedTo) {
        return Stream.of(companions, allies)
                .flatMap(Set::stream)
                .filter(b -> b.getSelf().equals(attachedTo))
                .findFirst()
                .orElse(null);
    }
}
