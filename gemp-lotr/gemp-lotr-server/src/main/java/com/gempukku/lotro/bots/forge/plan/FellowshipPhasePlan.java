package com.gempukku.lotro.bots.forge.plan;

import com.gempukku.lotro.bots.forge.plan.action.*;
import com.gempukku.lotro.bots.forge.utils.BoardStateUtil;
import com.gempukku.lotro.cards.build.bot.BotTargetingMode;
import com.gempukku.lotro.cards.build.bot.ability2.ActivatedAbility;
import com.gempukku.lotro.cards.build.bot.ability2.cost.Cost;
import com.gempukku.lotro.cards.build.bot.ability2.cost.CostWithTarget;
import com.gempukku.lotro.cards.build.bot.ability2.effect.*;
import com.gempukku.lotro.cards.build.bot.abstractcard.*;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.game.state.PlannedBoardState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
import org.apache.commons.lang3.NotImplementedException;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        int numberOfActionsToTakeAtStart;
        do {
            numberOfActionsToTakeAtStart = actions.size();

            addRevealOpponentsHandActions();

            addHealCompanionsByDiscardActions();

            addDiscardShadowCardsActions();

            addPlayCompanionFromHandActions();
            addPlayAlliesFromHandActions();
            addPlayPossessionsFromHandActions();
            addPlayConditionsFromHandActions();

            addRemoveBurdensActions();
            addPlayFellowshipsNextSiteActions();
            addHealActions();

            addTakeIntoHandFromDiscardActions();
            addUnclogHandActions();

            //TODO add another actions to action lists

        } while (numberOfActionsToTakeAtStart != actions.size());


        if (printDebugMessages) {
            System.out.println("Finally, will pass");
        }
        actions.add(new PassAction());
    }

    private void addUnclogHandActions() {
        throwExceptionIfEventWithEffectIsFound(
                EffectPutFromHandToBottomOfDeck.class,
                eventCard -> true);

        activateAbilitiesWithEffect(EffectPutFromHandToBottomOfDeck.class);
    }

    private void addTakeIntoHandFromDiscardActions() {
        throwExceptionIfEventWithEffectIsFound(
                EffectTakeIntoHandFromDiscard.class,
                eventCard -> true);

        activateAbilitiesWithEffect(EffectTakeIntoHandFromDiscard.class);
    }

    private void addPlayFellowshipsNextSiteActions() {
        playBestEventsWithEffect(EffectPlayFellowshipsNextSite.class);

        throwExceptionIfActivatedAbilityWithEffectIsFound(
                EffectPlayFellowshipsNextSite.class,
                botCard -> true);
    }

    private void addRevealOpponentsHandActions() {
        playBestEventsWithEffect(EffectRevealOpponentsHand.class);
        activateAbilitiesWithEffect(EffectRevealOpponentsHand.class);
    }

    private void addHealActions() {
        throwExceptionIfEventWithEffectIsFound(
                EffectHeal.class,
                eventCard -> true);

        activateAbilitiesWithEffect(EffectHeal.class);
    }

    private void addRemoveBurdensActions() {
        playBestEventsWithEffect(EffectRemoveBurden.class);
        activateAbilitiesWithEffect(EffectRemoveBurden.class);
    }

    private void addDiscardShadowCardsActions() {
        playBestEventsWithEffect(EffectDiscardFromPlay.class);

        throwExceptionIfActivatedAbilityWithEffectIsFound(
                EffectDiscardFromPlay.class,
                botCard -> ((EffectDiscardFromPlay) botCard.getActivatedAbility(EffectDiscardFromPlay.class).getEffect()).getPotentialTargets(playerName, plannedBoardState).stream().anyMatch(botCard1 -> Side.SHADOW.equals(botCard1.getSelf().getBlueprint().getSide())));
    }

    private void throwExceptionIfEventWithEffectIsFound(Class<? extends Effect> effectClass, Predicate<BotEventCard> extraFilter) {
        List<BotEventCard> events = BoardStateUtil.getPlayableFellowshipEventsWithEffect(plannedBoardState, playerName, effectClass).stream().filter(extraFilter).toList();
        if (!events.isEmpty()) {
            String names = events.stream()
                    .map(e -> e.getSelf().getBlueprint().getFullName())
                    .collect(Collectors.joining(", "));
            throw new IllegalStateException("Unimplemented Fellowship events with effect " + effectClass.getSimpleName() + ": " + names);
        }
    }

    private void playBestEventsWithEffect(Class<? extends Effect> effectClass) {
        playBestEventsWithEffect(effectClass, eventCard -> true);
    }

    private void playBestEventsWithEffect(Class<? extends Effect> effectClass, Predicate<BotEventCard> extraFilter) {
        while (true) {
            List<BotEventCard> fellowshipEvents = new ArrayList<>(BoardStateUtil.getPlayableFellowshipEventsWithEffect(plannedBoardState, playerName, effectClass).stream().filter(extraFilter).toList());
            if (fellowshipEvents.isEmpty()) break;
            fellowshipEvents.sort((o1, o2) -> Double.compare(o1.getEventAbility().getValueIfUsed(playerName, plannedBoardState), o2.getEventAbility().getValueIfUsed(playerName, plannedBoardState)));
            BotEventCard topEvent = fellowshipEvents.getFirst();
            if (topEvent.getEventAbility().getValueIfUsed(playerName, plannedBoardState) < 0.0) {
                break;
            } else {
                List<BotCard> targets = new ArrayList<>();
                if (EffectWithTarget.class.isAssignableFrom(effectClass)) {
                    List<BotCard> potentialTargets = ((EffectWithTarget) topEvent.getEventAbility().getEffect()).getPotentialTargets(playerName, plannedBoardState);
                    if (((EffectWithTarget) topEvent.getEventAbility().getEffect()).affectsAll() || potentialTargets.size() <= 1) {
                        targets.addAll(potentialTargets);
                    } else {
                        throw new IllegalStateException("Cannot resolve effect if number of potential targets is greater than 1 and not all should be affected");
                    }
                    if (printDebugMessages) {
                        System.out.println("Will play event " + topEvent.getSelf().getBlueprint().getFullName() + " from hand to " + ((EffectWithTarget) topEvent.getEventAbility().getEffect()).toString(playerName, plannedBoardState, targets));
                    }
                } else {
                    if (printDebugMessages) {
                        System.out.println("Will play event " + topEvent.getSelf().getBlueprint().getFullName() + " from hand to " + topEvent.getEventAbility().getEffect().toString(playerName, plannedBoardState));
                    }
                }

                UseCardWithTargetAction.Targeting costTargeting = null;
                Cost cost = topEvent.getEventAbility().getCost();
                if (cost instanceof CostWithTarget costWithTarget) {
                    List<BotCard> potentialTargets = costWithTarget.getPotentialTargets(playerName, plannedBoardState);
                    BotCard target = costWithTarget.chooseTarget(playerName, plannedBoardState);
                    costTargeting = new UseCardWithTargetAction.Targeting(target, potentialTargets);
                }

                if (cost != null) {
                    if (costTargeting != null) {
                        System.out.println("Cost to pay: " + ((CostWithTarget) cost).toString(playerName, plannedBoardState, costTargeting.target()));
                    } else {
                        System.out.println("Cost to pay: " + cost.toString(playerName, plannedBoardState));
                    }
                }

                actions.add(new PlayCardFromHandAction(topEvent.getSelf()));
                plannedBoardState.playEvent(topEvent);
            }
        }
    }

    private void throwExceptionIfActivatedAbilityWithEffectIsFound(Class<? extends Effect> effectClass, Predicate<BotCard> extraFilter) {
        List<BotCard> inPlayWithActivatedAbility = new ArrayList<>(Stream.concat(plannedBoardState.getFpCardsInPlay(playerName).stream(), Stream.of(plannedBoardState.getCurrentSite()))
                .filter(botCard -> {
                    ActivatedAbility activatedAbility = botCard.getActivatedAbility(effectClass);
                    if (activatedAbility == null) return false;
                    if (activatedAbility.getPhase() != Phase.FELLOWSHIP) return false;
                    if (!activatedAbility.conditionOk(playerName, plannedBoardState)) return false;
                    if (!activatedAbility.canPayCost(playerName, plannedBoardState)) return false;
                    return true;
                })
                .filter(extraFilter)
                .toList());
        if (!inPlayWithActivatedAbility.isEmpty()) {
            String names = inPlayWithActivatedAbility.stream()
                    .map(e -> e.getSelf().getBlueprint().getFullName())
                    .collect(Collectors.joining(", "));
            throw new IllegalStateException("Unimplemented Fellowship activated abilities with effect " + effectClass.getSimpleName() + ": " + names);
        }
    }

    private void activateAbilitiesWithEffect(Class<? extends Effect> effectClass) {
        activateAbilitiesWithEffect(effectClass, botCard -> true);
    }

    private void activateAbilitiesWithEffect(Class<? extends Effect> effectClass, Predicate<BotCard> extraFilter) {
        while (true) {
            List<BotCard> inPlayWithActivatedAbility = new ArrayList<>(Stream.concat(plannedBoardState.getFpCardsInPlay(playerName).stream(), Stream.of(plannedBoardState.getCurrentSite()))
                    .filter(botCard -> {
                        ActivatedAbility activatedAbility = botCard.getActivatedAbility(effectClass);
                        if (activatedAbility == null) return false;
                        if (activatedAbility.getPhase() != Phase.FELLOWSHIP) return false;
                        if (!activatedAbility.conditionOk(playerName, plannedBoardState)) return false;
                        if (!activatedAbility.canPayCost(playerName, plannedBoardState)) return false;
                        return true;
                    })
                    .filter(extraFilter)
                    .toList());

            double maxValue = 0.0;
            BotCard chosenCard = null;

            for (BotCard botCard : inPlayWithActivatedAbility) {
                double value = botCard.getActivatedAbility(effectClass).getValueIfUsed(playerName, plannedBoardState);
                if (value > maxValue) {
                    maxValue = value;
                    chosenCard = botCard;
                }
            }

            if (chosenCard == null) {
                break;
            } else {
                UseCardWithTargetAction.Targeting effectTargeting = null;
                UseCardWithTargetAction.Targeting costTargeting = null;
                if (EffectWithTarget.class.isAssignableFrom(effectClass)) {
                    List<BotCard> targets = new ArrayList<>();
                    List<BotCard> potentialTargets = ((EffectWithTarget) chosenCard.getActivatedAbility(effectClass).getEffect()).getPotentialTargets(playerName, plannedBoardState);
                    if (((EffectWithTarget) chosenCard.getActivatedAbility(effectClass).getEffect()).affectsAll() || potentialTargets.size() <= 1) {
                        targets.addAll(potentialTargets);
                    } else {
                       targets.add(((EffectWithTarget) chosenCard.getActivatedAbility(effectClass).getEffect()).chooseTarget(playerName, plannedBoardState));
                    }

                    if (printDebugMessages) {
                        System.out.println("Will use ability of " + chosenCard.getSelf().getBlueprint().getFullName() + " from hand to " + ((EffectWithTarget) chosenCard.getActivatedAbility(effectClass).getEffect()).toString(playerName, plannedBoardState, targets));
                    }
                    if (targets.isEmpty()) {

                    } else if (targets.size() == 1) {
                        effectTargeting = new UseCardWithTargetAction.Targeting(targets.getFirst(), potentialTargets);
                    } else {
                        throw new IllegalStateException("Cannot resolve activated ability effect if number of targets greater than 1");
                    }
                } else {
                    if (printDebugMessages) {
                        System.out.println("Will use ability of " + chosenCard.getSelf().getBlueprint().getFullName() + " from hand to " + chosenCard.getActivatedAbility(effectClass).getEffect().toString(playerName, plannedBoardState));
                    }
                }

                Cost cost = chosenCard.getActivatedAbility(effectClass).getCost();
                if (cost instanceof CostWithTarget costWithTarget) {
                    List<BotCard> potentialTargets = costWithTarget.getPotentialTargets(playerName, plannedBoardState);
                    BotCard target = costWithTarget.chooseTarget(playerName, plannedBoardState);
                    costTargeting = new UseCardWithTargetAction.Targeting(target, potentialTargets);
                }

                List<UseCardWithTargetAction.Targeting> targetings = new ArrayList<>();
                if (effectTargeting != null) {
                    targetings.add(effectTargeting);
                }
                if (costTargeting != null) {
                    targetings.add(costTargeting);
                }

                if (cost != null) {
                    if (costTargeting != null) {
                        System.out.println("Cost to pay: " + ((CostWithTarget) cost).toString(playerName, plannedBoardState, costTargeting.target()));
                    } else {
                        System.out.println("Cost to pay: " + cost.toString(playerName, plannedBoardState));
                    }
                }

                if (effectTargeting == null && costTargeting == null) {
                    actions.add(new UseCardAction(chosenCard.getSelf()));
                } else {
                    actions.add(new UseCardWithTargetAction(
                            chosenCard.getSelf(),
                            targetings));
                }
                chosenCard.getActivatedAbility(effectClass).resolveAbility(playerName, plannedBoardState);
            }
        }
    }

    private void addPlayConditionsFromHandActions() {
        List<BotCard> conditionsInHand = new ArrayList<>(BoardStateUtil.getCardInHandPlayableInPhase(plannedBoardState, playerName, Phase.FELLOWSHIP).stream()
                .filter(botCard -> CardType.CONDITION.equals(botCard.getSelf().getBlueprint().getCardType()))
                .toList());

        Collections.shuffle(conditionsInHand);
        for (BotCard condition : conditionsInHand) {
            if (condition.canBePlayed(plannedBoardState)) {
                if (condition instanceof BotObjectSupportAreaCard) {
                    throw new IllegalStateException("Support Area conditions not implemented yet: " + condition.getSelf().getBlueprint().getFullName());
                } else if (condition instanceof BotObjectAttachableCard attachableCard) {
                    List<BotCard> potentialTargets = plannedBoardState.getActiveCards().stream()
                            .filter(botCard -> attachableCard.isValidBearer(botCard, plannedBoardState))
                            .toList();
                    BotTargetingMode attachTargetingMode = attachableCard.getAttachTargetingMode();
                    BotCard target = attachTargetingMode.chooseTarget(plannedBoardState, potentialTargets, false);
                    if (target == null) {
                        throw new IllegalStateException("Could not find target for " + condition.getSelf().getBlueprint().getFullName());
                    }
                    if (printDebugMessages) {
                        System.out.println("Will play condition " + condition.getSelf().getBlueprint().getFullName() + " from hand on " + target.getSelf().getBlueprint().getFullName());
                    }
                    actions.add(new PlayCardFromHandWithTargetAction(condition.getSelf(), target.getSelf()));
                    plannedBoardState.playOnBearer(attachableCard, target);
                } else {
                    throw new IllegalStateException("Condition not instance of support area nor attachable object card: " + condition.getSelf().getBlueprint().getFullName());
                }
            }
        }
    }

    private void addPlayPossessionsFromHandActions() {
        List<BotCard> possessionsInHand = new ArrayList<>(BoardStateUtil.getCardInHandPlayableInPhase(plannedBoardState, playerName, Phase.FELLOWSHIP).stream()
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
                } else {
                    throw new IllegalStateException("Possession not instance of support area nor attachable object card: " + possession.getSelf().getBlueprint().getFullName());
                }
            }
        }
    }

    private void addPlayAlliesFromHandActions() {
        List<BotAllyCard> playableAlliesInHand = BoardStateUtil.getCardInHandPlayableInPhase(plannedBoardState, playerName, Phase.FELLOWSHIP).stream()
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
        }
    }

    private void addPlayCompanionFromHandActions() {
        List<BotCompanionCard> playableCompanionsInHand = BoardStateUtil.getCardInHandPlayableInPhase(plannedBoardState, playerName, Phase.FELLOWSHIP).stream()
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

    private void addHealCompanionsByDiscardActions() {
        List<BotCard> woundedUniqueCompanionsInPlay = BoardStateUtil.getWoundedActiveCompanionsInPlay(plannedBoardState);

        for (BotCard companion : woundedUniqueCompanionsInPlay) {
            int wounds = plannedBoardState.getWounds(companion);

            List<BotCard> matchingCardsInHand = BoardStateUtil.getCardInHandPlayableInPhase(plannedBoardState, playerName, Phase.FELLOWSHIP).stream()
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
            List<Integer> physicalIds = new ArrayList<>();
            if (awaitingDecision.getDecisionType().equals(AwaitingDecisionType.ARBITRARY_CARDS)) {
                for (String physicalCard : awaitingDecision.getDecisionParameters().get("physicalId")) {
                    physicalIds.add(Integer.parseInt(physicalCard));
                }

            } else if (awaitingDecision.getDecisionType().equals(AwaitingDecisionType.CARD_SELECTION)) {
                for (String physicalCard : awaitingDecision.getDecisionParameters().get("cardId")) {
                    physicalIds.add(Integer.parseInt(physicalCard));
                }
            }
            if (printDebugMessages) {
                System.out.println("Target chosen by plan: " + actionWithTarget.getTarget(physicalIds).getBlueprint().getFullName());
            }
            return List.of(actionWithTarget.getTarget(physicalIds));
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
