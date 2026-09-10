package com.gempukku.lotro.bots.forge.plan;

import com.gempukku.lotro.bots.forge.cards.BotCardFactory;
import com.gempukku.lotro.bots.forge.cards.abstractcards.BotCard;
import com.gempukku.lotro.bots.forge.plan.action.ActionToTake;
import com.gempukku.lotro.common.AllyHome;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.game.DefaultUserFeedback;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static com.gempukku.lotro.bots.forge.utils.BotLogging.log;

public class CombatFpPlan implements Plan {
    private final int siteNumber;
    private final String playerName;
    private final DefaultLotroGame game;
    private final DefaultLotroGame copy;
    private final DefaultUserFeedback feedback = new DefaultUserFeedback();
    List<ActionToTake> actions = new ArrayList<>();
    private int nextStep = 0;

    public CombatFpPlan(DefaultLotroGame game) {
        this.siteNumber = game.getGameState().getCurrentSiteNumber();
        this.playerName = game.getGameState().getCurrentPlayerId();
        this.game = game;

        log(1, "Making new combat plan for site " + siteNumber + ", phase " + game.getGameState().getCurrentPhase(), true);

        copy = game.getCopyByReplayingDecisionsFromStart(feedback);

        makePlan();
    }

    private void printState(BotCard site, List<BotCard> minions, List<BotCard> companions, List<BotCard> alliesAtHome, List<BotCard> combatCardsInHand) {
        log(1, "Site: [" + siteNumber + "] " + site);

        if (minions.isEmpty()) {
            log(1, "No minions in play");
        } else {
            log(1, "Minions: " + minions.size());
            for (BotCard minion : minions) {
                int wounds = game.getGameState().getWounds(minion.getPhysicalCard().getCardId());
                if (wounds == 0) {
                    log(1, "  " + minion);
                } else {
                    log(1, "  " + minion + " (W " + wounds + ")");
                }
                for (BotCard attachedCard : minion.getAttachedCards()) {
                    log(1, "    " + attachedCard);
                }
            }

            log(1, "Companions: " + companions.size());
            for (BotCard companion : companions) {
                int wounds = game.getGameState().getWounds(companion.getPhysicalCard().getCardId());
                boolean isRingBearer = game.getGameState().getRingBearer(playerName).getCardId() == companion.getPhysicalCard().getCardId();
                if (isRingBearer) {
                    log(1, "  " + companion + " (W " + wounds + ", B " + game.getGameState().getPlayerBurdens(playerName) + ")");
                } else {
                    log(1, "  " + companion + " (W " + wounds + ")");
                }
                for (BotCard attachedCard : companion.getAttachedCards()) {
                    log(1, "    " + attachedCard);
                }
            }

            if (!alliesAtHome.isEmpty()) {
                log(1, "Allies at home: " + alliesAtHome.size());
                for (BotCard ally : alliesAtHome) {
                    int wounds = game.getGameState().getWounds(ally.getPhysicalCard().getCardId());
                    log(1, "  " + ally.getFullName() + " (W " + wounds + ")");
                    for (BotCard attachedCard : ally.getAttachedCards()) {
                        log(1, "    " + attachedCard);
                    }
                }
            }
        }

        log(1, "Combat cards in hand: " + combatCardsInHand.size());
        for (BotCard botCard : combatCardsInHand) {
            log(1, "  " + botCard);
        }
    }

    private void makePlan() {
        BotCard site = BotCardFactory.create(copy.getGameState().getCurrentSite());

        List<BotCard> minions = new ArrayList<>();
        List<BotCard> companions = new ArrayList<>();
        List<BotCard> alliesAtHome = new ArrayList<>();
        for (PhysicalCard physicalCard : copy.getGameState().getInPlay()) {
            if (physicalCard.getBlueprint().getCardType() == CardType.MINION) {
                BotCard minion = BotCardFactory.create(physicalCard);
                for (PhysicalCard attachedCard : game.getGameState().getAttachedCards(minion.getPhysicalCard().getCardId())) {
                    minion.attachCard(BotCardFactory.create(attachedCard));
                }
                minions.add(minion);
            } else if (physicalCard.getBlueprint().getCardType() == CardType.COMPANION) {
                if (physicalCard.getOwner().equals(playerName)) {
                    BotCard companion = BotCardFactory.create(physicalCard);
                    for (PhysicalCard attachedCard : game.getGameState().getAttachedCards(companion.getPhysicalCard().getCardId())) {
                        companion.attachCard(BotCardFactory.create(attachedCard));
                    }
                    companions.add(companion);
                }
            } else if (physicalCard.getBlueprint().getCardType() == CardType.ALLY &&
                    physicalCard.getBlueprint().hasAllyHome(new AllyHome(copy.getGameState().getCurrentSiteBlock(), copy.getGameState().getCurrentSiteNumber()))) {
                if (physicalCard.getOwner().equals(playerName)) {
                    BotCard ally = BotCardFactory.create(physicalCard);
                    for (PhysicalCard attachedCard : game.getGameState().getAttachedCards(ally.getPhysicalCard().getCardId())) {
                        ally.attachCard(BotCardFactory.create(attachedCard));
                    }
                    alliesAtHome.add(ally);
                }
            }
        }

        List<BotCard> combatCardsInHand = game.getGameState().getHand(playerName).stream()
                .filter((Predicate<PhysicalCard>) physicalCard -> physicalCard.getBlueprint().getSide() == Side.FREE_PEOPLE)
                .map(BotCardFactory::create)
                .filter(BotCard::canBePlayedFromHandDuringCombat)
                .toList();

        printState(site, minions, companions, alliesAtHome, combatCardsInHand);
    }

    @Override
    public String chooseActionToTakeOrPass(DefaultLotroGame game, AwaitingDecision awaitingDecision) {
        log(2, "Combat plan asked to take action on " + awaitingDecision.toJson().toString(), true);

        if (!isActive()) {
            log(2, "Combat plan is outdated");
            throw new IllegalStateException("Plan is outdated");
        }

        if (nextStep >= actions.size()) {
            log(2, "All actions from plan already taken");
            throw new IllegalStateException("All actions from plan already taken");
        }

        ActionToTake action = actions.get(nextStep);
        log(2, "Action " + (nextStep + 1) + " out of " + actions.size() + ": " + action.toString());
        nextStep++;
        return action.carryOut();
    }

    @Override
    public boolean isOutdated() {
        return !isActive() || nextStep >= actions.size();
    }

    private boolean isActive() {
        return game.getGameState().getCurrentPlayerId().equals(playerName)
                &&
                (game.getGameState().getCurrentPhase().equals(Phase.MANEUVER) ||
                game.getGameState().getCurrentPhase().equals(Phase.ARCHERY) ||
                game.getGameState().getCurrentPhase().equals(Phase.ASSIGNMENT) ||
                game.getGameState().getCurrentPhase().equals(Phase.SKIRMISH) ||
                game.getGameState().getCurrentPhase().equals(Phase.REGROUP))
                && game.getGameState().getCurrentSiteNumber() == siteNumber;
    }

    @Override
    public void decisionMadeByPlayer(AwaitingDecision awaitingDecision, String answer, String player) {
        // TODO check if opponent did anything that changes plans
    }
}
