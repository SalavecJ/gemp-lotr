package com.gempukku.lotro.bots.forge.plan;

import com.gempukku.lotro.bots.forge.plan.action.*;
import com.gempukku.lotro.bots.forge.utils.BoardStateUtil;
import com.gempukku.lotro.cards.build.bot.BotCardFactory;
import com.gempukku.lotro.cards.build.bot.BotTargetingMode;
import com.gempukku.lotro.cards.build.bot.ability2.EventAbility;
import com.gempukku.lotro.cards.build.bot.ability2.effect.EffectDiscardFromPlay;
import com.gempukku.lotro.cards.build.bot.ability2.effect.EffectHeal;
import com.gempukku.lotro.cards.build.bot.ability2.effect.EffectRemoveBurden;
import com.gempukku.lotro.cards.build.bot.abstractcard.*;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.game.state.PlannedBoardState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import org.apache.commons.lang3.NotImplementedException;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class FellowshipPhasePlan {
    private final int siteNumber;
    private final String playerName;
    private final LotroGame game;
    private final boolean printDebugMessages;

    private int nextStep = 0;
    List<ActionToTake> actions = new ArrayList<>();
    private final PlannedBoardState plannedBoardState;

    public FellowshipPhasePlan(boolean printDebugMessages, LotroGame game) {
        this.siteNumber = game.getGameState().getCurrentSiteNumber();
        this.playerName = game.getGameState().getCurrentPlayerId();
        this.game = game;
        this.printDebugMessages = printDebugMessages;

        if (printDebugMessages) {
            System.out.println("Making new fellowship phase plan for site " + siteNumber);
        }

        plannedBoardState = new PlannedBoardState(game);
        makePlan();
    }

    private void makePlan() {
        List<BotCard> inHandPlayableInFellowshipPhase =
                new ArrayList<>(
                        game.getGameState().getHand(playerName).stream()
                                .filter((Predicate<PhysicalCard>) card -> BotCardFactory.create(card).isPlayableInPhase(Phase.FELLOWSHIP))
                                .map((Function<PhysicalCard, BotCard>) BotCardFactory::create)
                                .toList());

        addHealCompanionsByDiscardActions(inHandPlayableInFellowshipPhase);

        addDiscardShadowCardsActions(inHandPlayableInFellowshipPhase);

        addPlayCompanionFromHandActions(inHandPlayableInFellowshipPhase);
        addPlayAlliesFromHandActions(inHandPlayableInFellowshipPhase);
        addPlayPossessionsFromHandActions(inHandPlayableInFellowshipPhase);

        addHealActions(inHandPlayableInFellowshipPhase);
        addRemoveBurdensActions(inHandPlayableInFellowshipPhase);

        //TODO add another actions to action lists

        if (printDebugMessages) {
            System.out.println("Finally, will pass");
        }
        actions.add(new PassAction());
    }

    private void addHealActions(List<BotCard> inHandPlayableInFellowshipPhase) {
        List<BotEventCard> fellowshipEvents = new ArrayList<>(inHandPlayableInFellowshipPhase.stream()
                .filter(botCard -> botCard.getSelf().getBlueprint().getCardType().equals(CardType.EVENT)
                        && botCard.canBePlayed(plannedBoardState))
                .filter(botCard -> {
                    if (botCard instanceof BotEventCard eventCard) {
                        EventAbility eventAbility = eventCard.getEventAbility();
                        return eventAbility.getEffect() instanceof EffectHeal;
                    } else {
                        throw new IllegalStateException("Event " + botCard.getSelf().getBlueprint().getFullName() + " is not implemented as BotEventCard");
                    }
                })
                .map(botCard -> (BotEventCard) botCard)
                .toList());

        if (!fellowshipEvents.isEmpty()) {
            throw new IllegalStateException("Healing with events in fellowship phase not implemented");
        }

        while (true) {
            List<BotCard> inPlayWithActivatedAbility = new ArrayList<>(plannedBoardState.getFpCardsInPlay(playerName).stream()
                    .filter(botCard -> {
                        com.gempukku.lotro.cards.build.bot.ability2.ActivatedAbility activatedAbility = botCard.getActivatedAbility(EffectHeal.class);
                        if (activatedAbility == null) return false;
                        if (activatedAbility.getPhase() != Phase.FELLOWSHIP) return false;
                        if (!activatedAbility.canPayCost(botCard, plannedBoardState)) return false;
                        return true;
                    })
                    .toList());

            double maxValue = 0.0;
            BotCard chosenCard = null;

            for (BotCard botCard : inPlayWithActivatedAbility) {
                double value = botCard.getActivatedAbility(EffectHeal.class).getValueIfUsed(botCard, plannedBoardState);
                if (value > maxValue) {
                    maxValue = value;
                    chosenCard = botCard;
                }
            }

            if (chosenCard == null) {
                break;
            } else {
                EffectHeal effectHeal = ((EffectHeal) chosenCard.getActivatedAbility(EffectHeal.class).getEffect());
                BotCard target = effectHeal.chooseTarget(plannedBoardState);
                if (printDebugMessages) {
                    if (target != null) {
                        System.out.println("Will use ability of " + chosenCard.getSelf().getBlueprint().getFullName()
                                + " to remove " + effectHeal.getAmount()
                                + " wound(s) from "
                                + target.getSelf().getBlueprint().getFullName());
                    } else {
                        System.out.println("Will use ability of " + chosenCard.getSelf().getBlueprint().getFullName()
                                + " but there is nothing to heal");
                    }
                }
                actions.add(new UseCardWithTargetAction(
                        chosenCard.getSelf(),
                        target.getSelf()));
                chosenCard.getActivatedAbility(EffectHeal.class).resolveAbility(chosenCard, plannedBoardState);
            }
        }
    }

    private void addRemoveBurdensActions(List<BotCard> inHandPlayableInFellowshipPhase) {
        List<BotEventCard> fellowshipEvents = new ArrayList<>(inHandPlayableInFellowshipPhase.stream()
                .filter(botCard -> botCard.getSelf().getBlueprint().getCardType().equals(CardType.EVENT)
                        && botCard.canBePlayed(plannedBoardState))
                .filter(botCard -> {
                    if (botCard instanceof BotEventCard eventCard) {
                        EventAbility eventAbility = eventCard.getEventAbility();
                        return eventAbility.getEffect() instanceof EffectRemoveBurden;
                    } else {
                        throw new IllegalStateException("Event " + botCard.getSelf().getBlueprint().getFullName() + " is not implemented as BotEventCard");
                    }
                })
                .map(botCard -> (BotEventCard) botCard)
                .toList());

        while (!fellowshipEvents.isEmpty()) {
            fellowshipEvents.sort((o1, o2) -> Double.compare(getValueOfFellowshipEvent(o1), getValueOfFellowshipEvent(o2)));
            BotEventCard topEvent = fellowshipEvents.getFirst();
            if (getValueOfFellowshipEvent(topEvent) <= 0.0) {
                break;
            } else {
                if (topEvent.getEventAbility().getEffect() instanceof EffectRemoveBurden removeBurdenEffect) {
                    if (printDebugMessages) {
                        System.out.println("Will play event " + topEvent.getSelf().getBlueprint().getFullName() + " from hand to remove burdens: " + removeBurdenEffect.getAmount());
                    }
                    actions.add(new PlayCardFromHandAction(topEvent.getSelf()));
                    plannedBoardState.playEvent(topEvent);
                    fellowshipEvents.remove(topEvent);
                } else {
                    throw new IllegalStateException("Event " + topEvent.getSelf().getBlueprint().getFullName() + " does not have RemoveBurden effect");
                }
            }
        }

        while (true) {
            List<BotCard> inPlayWithActivatedAbility = new ArrayList<>(plannedBoardState.getFpCardsInPlay(playerName).stream()
                    .filter(botCard -> {
                        com.gempukku.lotro.cards.build.bot.ability2.ActivatedAbility activatedAbility = botCard.getActivatedAbility(EffectRemoveBurden.class);
                        if (activatedAbility == null) return false;
                        if (activatedAbility.getPhase() != Phase.FELLOWSHIP) return false;
                        if (!activatedAbility.canPayCost(botCard, plannedBoardState)) return false;
                        return true;
                    })
                    .toList());

            double maxValue = 0.0;
            BotCard chosenCard = null;

            for (BotCard botCard : inPlayWithActivatedAbility) {
                double value = botCard.getActivatedAbility(EffectRemoveBurden.class).getValueIfUsed(botCard, plannedBoardState);
                if (value > maxValue) {
                    maxValue = value;
                    chosenCard = botCard;
                }
            }

            if (chosenCard == null) {
                break;
            } else {
                int numberOfBurdens = ((EffectRemoveBurden) chosenCard.getActivatedAbility(EffectRemoveBurden.class).getEffect()).getAmount();
                if (printDebugMessages) {
                    System.out.println("Will use ability of " + chosenCard.getSelf().getBlueprint().getFullName() + " to remove burdens: " + numberOfBurdens);
                }
                actions.add(new UseCardAction(chosenCard.getSelf()));
                chosenCard.getActivatedAbility(EffectRemoveBurden.class).resolveAbility(chosenCard, plannedBoardState);
            }
        }
    }

    private void addDiscardShadowCardsActions(List<BotCard> inHandPlayableInFellowshipPhase) {
        List<BotEventCard> fellowshipEvents = new ArrayList<>(inHandPlayableInFellowshipPhase.stream()
                .filter(botCard -> botCard.getSelf().getBlueprint().getCardType().equals(CardType.EVENT)
                        && botCard.canBePlayed(plannedBoardState))
                .filter(botCard -> {
                    if (botCard instanceof BotEventCard eventCard) {
                        EventAbility eventAbility = eventCard.getEventAbility();
                        if (eventAbility.getEffect() instanceof EffectDiscardFromPlay discardEffect) {
                            return discardEffect.getPotentialTargets(plannedBoardState).stream().anyMatch(botCard1 -> Side.SHADOW.equals(botCard1.getSelf().getBlueprint().getSide()));
                        } else {
                            return false;
                        }
                    } else {
                        throw new IllegalStateException("Event " + botCard.getSelf().getBlueprint().getFullName() + " is not implemented as BotEventCard");
                    }
                })
                .map(botCard -> (BotEventCard) botCard)
                .toList());

        while (!fellowshipEvents.isEmpty()) {
            fellowshipEvents.sort((o1, o2) -> Double.compare(getValueOfFellowshipEvent(o1), getValueOfFellowshipEvent(o2)));
            BotEventCard topEvent = fellowshipEvents.getFirst();
            if (getValueOfFellowshipEvent(topEvent) <= 0.0) {
                break;
            } else {
                List<BotCard> targets = new ArrayList<>();
                if (topEvent.getEventAbility().getEffect() instanceof EffectDiscardFromPlay discardEffect) {
                    List<BotCard> potentialTargets = discardEffect.getPotentialTargets(plannedBoardState);
                    if (discardEffect.discardsAll() || potentialTargets.size() <= 1) {
                        targets.addAll(potentialTargets);
                    } else {
                        throw new IllegalStateException("Cannot resolve discard effect if number of potential targets is greater than 1 and not all should be discarded");
                    }
                } else {
                    throw new IllegalStateException("Event " + topEvent.getSelf().getBlueprint().getFullName() + " does not have DiscardFromPlay effect");
                }

                if (printDebugMessages) {
                    if (targets.size() == 1) {
                        System.out.println("Will play event " + topEvent.getSelf().getBlueprint().getFullName() + " from hand to discard " + targets.getFirst().getSelf().getBlueprint().getFullName());
                    } else {
                        StringBuilder stringBuilder = new StringBuilder();
                        for (BotCard target : targets) {
                            stringBuilder.append(target.getSelf().getBlueprint().getFullName());
                            stringBuilder.append("; ");
                        }
                        System.out.println("Will play event " + topEvent.getSelf().getBlueprint().getFullName() + " from hand to discard cards: " + stringBuilder);
                    }
                }
                actions.add(new PlayCardFromHandAction(topEvent.getSelf()));
                plannedBoardState.playEvent(topEvent);
                fellowshipEvents.remove(topEvent);
            }
        }

        while (true) {
            List<BotCard> inPlayWithActivatedAbility = new ArrayList<>(plannedBoardState.getFpCardsInPlay(playerName).stream()
                    .filter(botCard -> {
                        com.gempukku.lotro.cards.build.bot.ability2.ActivatedAbility activatedAbility = botCard.getActivatedAbility(EffectDiscardFromPlay.class);
                        if (activatedAbility == null) return false;
                        if (activatedAbility.getPhase() != Phase.FELLOWSHIP) return false;
                        if (!activatedAbility.canPayCost(botCard, plannedBoardState)) return false;
                        List<BotCard> targets = ((EffectDiscardFromPlay) activatedAbility.getEffect()).getPotentialTargets(plannedBoardState);
                        if (targets.stream().noneMatch(botCard1 -> Side.SHADOW.equals(botCard1.getSelf().getBlueprint().getSide())))
                            return false;
                        return true;
                    })
                    .toList());

            double maxValue = 0.0;
            BotCard chosenCard = null;

            for (BotCard botCard : inPlayWithActivatedAbility) {
                double value = botCard.getActivatedAbility(EffectDiscardFromPlay.class).getValueIfUsed(botCard, plannedBoardState);
                if (value > maxValue) {
                    maxValue = value;
                    chosenCard = botCard;
                }
            }

            if (chosenCard == null) {
                break;
            } else {
                throw new IllegalStateException("Discarding Shadow cards with abilities in fellowship phase not implemented");
            }
        }
    }

    private double getValueOfFellowshipEvent(BotCard fellowshipEvent) {
        if (fellowshipEvent instanceof BotEventCard eventCard) {
            return eventCard.getEventAbility().getValueIfUsed(fellowshipEvent, plannedBoardState);
        } else {
            throw new IllegalStateException("Not an event: " + fellowshipEvent.getSelf().getBlueprint().getFullName());
        }
    }

    private void addPlayPossessionsFromHandActions(List<BotCard> inHandPlayableInFellowshipPhase) {
        List<BotCard> possessionsInHand = new ArrayList<>(inHandPlayableInFellowshipPhase.stream()
                .filter(botCard -> CardType.POSSESSION.equals(botCard.getSelf().getBlueprint().getCardType()))
                .toList());

        Collections.shuffle(possessionsInHand);
        for (BotCard possession : possessionsInHand) {
            if (possession.canBePlayed(plannedBoardState)) {
                if (possession instanceof BotObjectSupportAreaCard) {
                    throw new IllegalStateException("Support Area possessions not implemented yet: " + possession.getSelf().getBlueprint().getFullName());
                } else if (possession instanceof BotObjectAttachableCard attachableCard) {
                    List<BotCard> potentialTargets = plannedBoardState.getActiveCards().stream()
                            .filter(botCard -> attachableCard.isValidBearer(botCard, plannedBoardState))
                            .toList();
                    BotTargetingMode attachTargetingMode = attachableCard.getAttachTargetingMode();
                    BotCard target = attachTargetingMode.chooseTarget(plannedBoardState, potentialTargets, false);
                    if (target == null) {
                        throw new IllegalStateException("Could not find target for " + possession.getSelf().getBlueprint().getFullName());
                    }
                    if (printDebugMessages) {
                        System.out.println("Will play possession " + possession.getSelf().getBlueprint().getFullName() + " from hand on " + target.getSelf().getBlueprint().getFullName());
                    }
                    actions.add(new PlayCardFromHandWithTargetAction(possession.getSelf(), target.getSelf()));
                    plannedBoardState.playOnBearer(attachableCard, target);
                    inHandPlayableInFellowshipPhase.remove(possession);
                } else {
                    throw new IllegalStateException("Possession not instance of support area nor attachable object card: " + possession.getSelf().getBlueprint().getFullName());
                }
            }
        }
    }

    private void addPlayAlliesFromHandActions(List<BotCard> inHandPlayableInFellowshipPhase) {
        List<BotAllyCard> playableAlliesInHand = inHandPlayableInFellowshipPhase.stream()
                .filter(card -> card instanceof BotAllyCard)
                .filter(card ->
                        CardType.ALLY.equals(card.getSelf().getBlueprint().getCardType())
                                && card.canBePlayed(plannedBoardState))
                .map(botCard -> (BotAllyCard) botCard)
                .toList();

        List<BotAllyCard> uniqueFilteredPlayableAllies = new ArrayList<>();
        for (BotAllyCard ally : playableAlliesInHand) {
            if (!ally.getSelf().getBlueprint().isUnique()) {
                uniqueFilteredPlayableAllies.add(ally);
            } else {
                boolean additionalCopy = uniqueFilteredPlayableAllies.stream()
                        .anyMatch(alreadyThere -> alreadyThere.getSelf().getBlueprint().getTitle().equals(ally.getSelf().getBlueprint().getTitle()));
                if (!additionalCopy) {
                    uniqueFilteredPlayableAllies.add(ally);
                }
            }
        }
        for (BotAllyCard allyInHand : uniqueFilteredPlayableAllies) {
            if (printDebugMessages) {
                System.out.println("Will play ally " + allyInHand.getSelf().getBlueprint().getFullName() + " from hand");
            }
            actions.add(new PlayCardFromHandAction(allyInHand.getSelf()));
            plannedBoardState.playToFpSupportArea(allyInHand);
            inHandPlayableInFellowshipPhase.remove(allyInHand);
        }
    }

    private void addPlayCompanionFromHandActions(List<BotCard> inHandPlayableInFellowshipPhase) {
        List<BotCompanionCard> playableCompanionsInHand = inHandPlayableInFellowshipPhase.stream()
                .filter(card -> card instanceof BotCompanionCard)
                .filter(card ->
                        CardType.COMPANION.equals(card.getSelf().getBlueprint().getCardType())
                                && card.canBePlayed(plannedBoardState))
                .map(botCard -> (BotCompanionCard) botCard)
                .toList();

        int companionsInPlay = BoardStateUtil.getActiveCompanionsInPlayCount(plannedBoardState);

        List<BotCompanionCard> uniqueFilteredPlayableCompanions = new ArrayList<>();
        for (BotCompanionCard companion : playableCompanionsInHand) {
            if (!companion.getSelf().getBlueprint().isUnique()) {
                uniqueFilteredPlayableCompanions.add(companion);
            } else {
                boolean additionalCopy = uniqueFilteredPlayableCompanions.stream()
                        .anyMatch(alreadyThere -> alreadyThere.getSelf().getBlueprint().getTitle().equals(companion.getSelf().getBlueprint().getTitle()));
                if (!additionalCopy) {
                    uniqueFilteredPlayableCompanions.add(companion);
                }
            }
        }

        int ruleOfNineRemainder = BoardStateUtil.getRuleOfNineRemainder(plannedBoardState);

        int numberOfCompanionsToBePlayed = getNumberOfCompanionsToBePlayed(uniqueFilteredPlayableCompanions.size(), ruleOfNineRemainder, companionsInPlay);

        if (numberOfCompanionsToBePlayed == uniqueFilteredPlayableCompanions.size()) {
            // play all
            for (BotCompanionCard companionInHand : uniqueFilteredPlayableCompanions) {
                if (printDebugMessages) {
                    System.out.println("Will play companion " + companionInHand.getSelf().getBlueprint().getFullName() + " from hand");
                }
                actions.add(new PlayCardFromHandAction(companionInHand.getSelf()));
                plannedBoardState.playCompanion(companionInHand);
                inHandPlayableInFellowshipPhase.remove(companionInHand);
            }
        } else if (numberOfCompanionsToBePlayed == 0) {
            if (printDebugMessages) {
                System.out.println("Won't play any companions. Companions in play: " + companionsInPlay + ". Playable companions in hand: " + uniqueFilteredPlayableCompanions.size());
            }
            return;
        } else {
            // find the best cards to play and play those
            throw new NotImplementedException("Choosing which companion to play is not yet implemented");
        }
    }

    private int getNumberOfCompanionsToBePlayed(int playableCompanions, int ruleOfNineRemainder, int companionsInPlay) {
        int numberOfCompanionsThatCanBePlayed = Math.min(playableCompanions, ruleOfNineRemainder);

        int numberOfCompanionsToBePlayed;
        if (companionsInPlay >= 6) {
            // already getting hit enquea, play whatever
            numberOfCompanionsToBePlayed = numberOfCompanionsThatCanBePlayed;
        } else {
            // if fellowship can get to large companion number, do it, else fill to 5 comps
            if (companionsInPlay + playableCompanions >= 8) {
                numberOfCompanionsToBePlayed = numberOfCompanionsThatCanBePlayed;
            } else {
                numberOfCompanionsToBePlayed = Math.min(numberOfCompanionsThatCanBePlayed, 5 - companionsInPlay);
            }
        }
        return numberOfCompanionsToBePlayed;
    }

    private void addHealCompanionsByDiscardActions(List<BotCard> inHandPlayableInFellowshipPhase) {
        List<BotCard> woundedUniqueCompanionsInPlay = BoardStateUtil.getWoundedActiveCompanionsInPlay(plannedBoardState);

        for (BotCard companion : woundedUniqueCompanionsInPlay) {
            int wounds = plannedBoardState.getWounds(companion);

            List<BotCard> matchingCardsInHand = inHandPlayableInFellowshipPhase.stream()
                    .filter(cardInHand ->
                            CardType.COMPANION.equals(cardInHand.getSelf().getBlueprint().getCardType())
                            && cardInHand.getSelf().getBlueprint().getTitle().equals(companion.getSelf().getBlueprint().getTitle()))
                    .toList();

            int cardsToDiscardToHeal = Math.min(wounds, matchingCardsInHand.size());

            for (int i = 0; i < cardsToDiscardToHeal; i++) {
                BotCard toDiscard = matchingCardsInHand.get(i);
                if (printDebugMessages) {
                    System.out.println("Will discard " + toDiscard.getSelf().getBlueprint().getFullName() + " from hand to heal companion in play");
                }
                actions.add(new DiscardCompanionToHealAction(toDiscard.getSelf()));
                plannedBoardState.healByDiscard(toDiscard);
                inHandPlayableInFellowshipPhase.remove(toDiscard);
            }
        }
    }

    public int chooseActionToTakeOrPass(AwaitingDecision awaitingDecision) {
        if (printDebugMessages) {
            System.out.println("Fellowship phase plan asked to take action on " + awaitingDecision.toJson().toString());
        }

        if (!isActive()) {
            if (printDebugMessages) {
                System.out.println("Plan is outdated");
            }
            throw new IllegalStateException("Plan is outdated");
        }

        if (nextStep >= actions.size()) {
            System.out.println("All actions from plan already taken");
            throw new IllegalStateException("All actions from plan already taken");
        }

        ActionToTake action = actions.get(nextStep);
        if (printDebugMessages) {
            System.out.println("Action " + (nextStep + 1) + " out of " + actions.size());
            System.out.println(action.toString());
        }
        nextStep++;
        return action.carryOut(awaitingDecision);
    }

    public List<PhysicalCard> chooseTarget(AwaitingDecision awaitingDecision) {
        if (printDebugMessages) {
            System.out.println("Fellowship phase plan asked to take action on " + awaitingDecision.toJson().toString());
        }

        if (!isActive()) {
            if (printDebugMessages) {
                System.out.println("Plan is outdated");
            }
            throw new IllegalStateException("Plan is outdated");
        }

        if (nextStep > actions.size()) {
            System.out.println("All actions from plan already fully taken");
            throw new IllegalStateException("All actions from plan already fully taken");
        }

        ActionToTake action = actions.get(nextStep - 1);
        if (printDebugMessages) {
            System.out.println("Last action");
            System.out.println(action.toString());
        }

        if (action instanceof PlayCardFromHandWithTargetAction actionWithTarget) {
            if (printDebugMessages) {
                System.out.println("Target chosen by plan: " + actionWithTarget.getTarget().getBlueprint().getFullName());
            }
            return List.of(actionWithTarget.getTarget());
        } else if (action instanceof UseCardWithTargetAction actionWithTarget) {
            if (printDebugMessages) {
                System.out.println("Target chosen by plan: " + actionWithTarget.getTarget().getBlueprint().getFullName());
            }
            return List.of(actionWithTarget.getTarget());
        } else {
            throw new IllegalStateException("Last action should not trigger targeting");
        }
    }

    public boolean replanningNeeded() {
        return !isActive() || nextStep >= actions.size() || actions.get(nextStep) instanceof ReplanAction;
    }

    private boolean isActive() {
        boolean tbr =  game.getGameState().getCurrentPlayerId().equals(playerName)
                && game.getGameState().getCurrentPhase().equals(Phase.FELLOWSHIP)
                && game.getGameState().getCurrentSiteNumber() == siteNumber;
        if (printDebugMessages) {
            System.out.println("Plan is active: " + tbr);
        }
        return tbr;
    }
}
