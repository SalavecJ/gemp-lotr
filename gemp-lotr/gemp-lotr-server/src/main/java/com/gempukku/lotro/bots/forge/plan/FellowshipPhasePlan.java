package com.gempukku.lotro.bots.forge.plan;

import com.gempukku.lotro.bots.forge.plan.action.*;
import com.gempukku.lotro.bots.forge.utils.BoardStateUtil;
import com.gempukku.lotro.cards.build.bot.BotCardFactory;
import com.gempukku.lotro.cards.build.bot.BotTargetingMode;
import com.gempukku.lotro.cards.build.bot.TriggerCondition;
import com.gempukku.lotro.cards.build.bot.ability.AbilityProperty;
import com.gempukku.lotro.cards.build.bot.ability.ActivatedAbility;
import com.gempukku.lotro.cards.build.bot.ability.BotAbility;
import com.gempukku.lotro.cards.build.bot.ability.TriggeredAbility;
import com.gempukku.lotro.cards.build.bot.abstractcard.BotCard;
import com.gempukku.lotro.cards.build.bot.abstractcard.BotObjectAttachableCard;
import com.gempukku.lotro.cards.build.bot.abstractcard.BotObjectSupportAreaCard;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.game.state.PlannedBoardState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import org.apache.commons.lang3.NotImplementedException;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
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
        List<BotCard> inPlayWithActivatedAbility =
                game.getGameState().getInPlay().stream()
                        .filter((Predicate<PhysicalCard>) card -> card.getOwner().equals(playerName))
                        .filter(card -> game.getGameState().isCardInPlayActive(card))
                        .filter(card -> !BotCardFactory.create(card).getActivatedAbilities(Phase.FELLOWSHIP).isEmpty())
                        .map((Function<PhysicalCard, BotCard>) BotCardFactory::create)
                        .toList();
        List<BotCard> inHandPlayableInFellowshipPhase =
                new ArrayList<>(
                        game.getGameState().getHand(playerName).stream()
                                .filter((Predicate<PhysicalCard>) card -> BotCardFactory.create(card).isPlayableInPhase(Phase.FELLOWSHIP))
                                .map((Function<PhysicalCard, BotCard>) BotCardFactory::create)
                                .toList());

        addHealCompanionsByDiscardActions(inHandPlayableInFellowshipPhase);

        addDiscardShadowCardsActions(inHandPlayableInFellowshipPhase, inPlayWithActivatedAbility);

        addPlayCompanionFromHandActions(inHandPlayableInFellowshipPhase);
        addPlayAlliesFromHandActions(inHandPlayableInFellowshipPhase);
        addPlayPossessionsFromHandActions(inHandPlayableInFellowshipPhase);

        addRemoveBurdensActions(inHandPlayableInFellowshipPhase, inPlayWithActivatedAbility);

        //TODO add another actions to action lists

        if (printDebugMessages) {
            System.out.println("Finally, will pass");
        }
        actions.add(new PassAction());
    }

    private void addRemoveBurdensActions(List<BotCard> inHandPlayableInFellowshipPhase, List<BotCard> inPlayWithActivatedAbility) {
        List<BotCard> fellowshipEvents = new ArrayList<>(inHandPlayableInFellowshipPhase.stream()
                .filter(botCard -> botCard.getSelf().getBlueprint().getCardType().equals(CardType.EVENT))
                .filter(botCard -> botCard.getAbilities().stream()
                        .filter(botAbility -> {
                            if (botAbility instanceof TriggeredAbility ta) {
                                return ta.getTriggerCondition().equals(TriggerCondition.WHEN_PLAYED);

                            } else {
                                return false;
                            }
                        })
                        .filter(botAbility -> {
                            AbilityProperty effect = botAbility.getEffect();
                            return effect.getType().equals(AbilityProperty.Type.REMOVE_BURDEN);
                        })
                        .anyMatch(botAbility -> {
                            TriggeredAbility ta = (TriggeredAbility) botAbility;
                            return plannedBoardState.canPayAllCosts(botCard, ta);
                        }))
                .toList());

        List<BotCard> fellowshipCardsInPlay = new ArrayList<>(inPlayWithActivatedAbility.stream()
                .filter(botCard -> botCard.getActivatedAbilities(Phase.FELLOWSHIP).stream()
                        .filter(botAbility -> {
                            AbilityProperty effect = botAbility.getEffect();
                            return effect.getType().equals(AbilityProperty.Type.REMOVE_BURDEN);
                        })
                        .anyMatch(botAbility -> {
                            ActivatedAbility aa = (ActivatedAbility) botAbility;
                            return plannedBoardState.canPayAllCosts(botCard, aa);
                        }))
                .toList());

        while (!fellowshipEvents.isEmpty()) {
            fellowshipEvents.sort((o1, o2) -> Double.compare(getValueOfFellowshipEvent(o1), getValueOfFellowshipEvent(o2)));
            BotCard topEvent = fellowshipEvents.getFirst();
            if (getValueOfFellowshipEvent(topEvent) <= 0.0) {
                break;
            } else {
                AtomicInteger numberOfBurdens = new AtomicInteger();
                topEvent.getAbilities().stream()
                        .filter(botAbility -> {
                            if (botAbility instanceof TriggeredAbility ta) {
                                return ta.getTriggerCondition().equals(TriggerCondition.WHEN_PLAYED);
                            } else {
                                return false;
                            }
                        })
                        .filter(botAbility -> {
                            AbilityProperty effect = botAbility.getEffect();
                            return effect.getType().equals(AbilityProperty.Type.REMOVE_BURDEN);
                        })
                        .forEach(ability -> {
                            AbilityProperty effect = ability.getEffect();
                            numberOfBurdens.set(effect.getParam("amount", Integer.class));
                        });


                if (printDebugMessages) {
                    System.out.println("Will play event " + topEvent.getSelf().getBlueprint().getFullName() + " from hand to remove burdens: " + numberOfBurdens.get());
                }
                actions.add(new PlayCardFromHandAction(topEvent.getSelf()));
                plannedBoardState.playEvent(topEvent);
                fellowshipEvents.remove(topEvent);
            }
        }

        if (!fellowshipCardsInPlay.isEmpty()) {
            throw new IllegalStateException("Removing burdens with abilities in fellowship phase not implemented");
        }
    }

    private void addDiscardShadowCardsActions(List<BotCard> inHandPlayableInFellowshipPhase, List<BotCard> inPlayWithActivatedAbility) {
        List<BotCard> fellowshipEvents = new ArrayList<>(inHandPlayableInFellowshipPhase.stream()
                .filter(botCard -> botCard.getSelf().getBlueprint().getCardType().equals(CardType.EVENT))
                .filter(botCard -> botCard.getAbilities().stream()
                        .filter(botAbility -> {
                            if (botAbility instanceof TriggeredAbility ta) {
                                return ta.getTriggerCondition().equals(TriggerCondition.WHEN_PLAYED);

                            } else {
                                return false;
                            }
                        })
                        .filter(botAbility -> {
                            AbilityProperty effect = botAbility.getEffect();
                            if (!effect.getType().equals(AbilityProperty.Type.DISCARD_FROM_PLAY)) return false;
                            List<BotCard> targets = plannedBoardState.getCardsEffectCanTarget(effect, playerName, Side.FREE_PEOPLE);
                            if (targets.stream().noneMatch(botCard1 -> Side.SHADOW.equals(botCard1.getSelf().getBlueprint().getSide()))) return false;
                            return true; // can discard shadow card from play
                        })
                        .anyMatch(botAbility -> {
                            TriggeredAbility ta = (TriggeredAbility) botAbility;
                            return plannedBoardState.canPayAllCosts(botCard, ta);
                        }))
                .toList());

        List<BotCard> fellowshipCardsInPlay = new ArrayList<>(inPlayWithActivatedAbility.stream()
                .filter(botCard -> botCard.getActivatedAbilities(Phase.FELLOWSHIP).stream()
                        .anyMatch(botAbility -> {
                            AbilityProperty effect = botAbility.getEffect();
                            if (!effect.getType().equals(AbilityProperty.Type.DISCARD_FROM_PLAY)) return false;
                            List<BotCard> targets = plannedBoardState.getCardsEffectCanTarget(effect, playerName, Side.FREE_PEOPLE);
                            if (targets.stream().noneMatch(botCard1 -> Side.SHADOW.equals(botCard1.getSelf().getBlueprint().getSide()))) return false;
                            return true; // can discard shadow card from play
                        }))
                .toList());

        while (!fellowshipEvents.isEmpty()) {
            fellowshipEvents.sort((o1, o2) -> Double.compare(getValueOfFellowshipEvent(o1), getValueOfFellowshipEvent(o2)));
            BotCard topEvent = fellowshipEvents.getFirst();
            if (getValueOfFellowshipEvent(topEvent) <= 0.0) {
                break;
            } else {
                List<BotCard> targets = new ArrayList<>();
                topEvent.getAbilities().stream()
                        .filter(botAbility -> {
                            if (botAbility instanceof TriggeredAbility ta) {
                                return ta.getTriggerCondition().equals(TriggerCondition.WHEN_PLAYED);
                            } else {
                                return false;
                            }
                        })
                        .filter(botAbility -> {
                            AbilityProperty effect = botAbility.getEffect();
                            if (!effect.getType().equals(AbilityProperty.Type.DISCARD_FROM_PLAY)) return false;
                            List<BotCard> targets1 = plannedBoardState.getCardsEffectCanTarget(effect, playerName, Side.FREE_PEOPLE);
                            if (targets1.stream().noneMatch(botCard1 -> Side.SHADOW.equals(botCard1.getSelf().getBlueprint().getSide()))) return false;
                            return true; // can discard shadow card from play
                        })
                        .forEach(ability -> {
                            AbilityProperty effect = ability.getEffect();
                            int numberOfTargets = effect.getParam("numberOfTargets", Integer.class);
                            List<BotCard> potentialTargets = plannedBoardState.getCardsEffectCanTarget(effect, playerName, Side.FREE_PEOPLE);
                            if (numberOfTargets >= potentialTargets.size()) {
                                targets.addAll(potentialTargets);
                            } else {
                                throw new IllegalStateException("Choosing targets for fellowship phase discarding not implemented for card " + topEvent.getSelf().getBlueprint().getFullName());
                            }
                        });

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

        if (!fellowshipCardsInPlay.isEmpty()) {
            throw new IllegalStateException("Discarding Shadow cards with abilities in fellowship phase not implemented");
        }
    }

    private double getValueOfFellowshipEvent(BotCard fellowshipEvent) {
        List<BotAbility> whenPlayedAbility = fellowshipEvent.getAbilities().stream()
                .filter(botAbility -> {
                    if (botAbility instanceof TriggeredAbility ta) {
                        return ta.getTriggerCondition().equals(TriggerCondition.WHEN_PLAYED);
                    } else {
                        return false;
                    }
                }).toList();
        double value = 0.0;
        for (BotAbility ability : whenPlayedAbility) {
            value += plannedBoardState.getValueIfUsed(ability, playerName, Side.FREE_PEOPLE);
        }
        return value;
    }

    private void addPlayPossessionsFromHandActions(List<BotCard> inHandPlayableInFellowshipPhase) {
        List<BotCard> possessionsInHand = new ArrayList<>(inHandPlayableInFellowshipPhase.stream()
                .filter(botCard -> CardType.POSSESSION.equals(botCard.getSelf().getBlueprint().getCardType()))
                .toList());

        Collections.shuffle(possessionsInHand);
        for (BotCard possession : possessionsInHand) {
            if (possession.canBePlayed(plannedBoardState)) {
                if (possession instanceof BotObjectSupportAreaCard) {
                    if (printDebugMessages) {
                        System.out.println("Will play possession " + possession.getSelf().getBlueprint().getFullName() + " from hand to support area");
                    }
                    actions.add(new PlayCardFromHandAction(possession.getSelf()));
                    plannedBoardState.playToFpSupportArea(possession);
                    inHandPlayableInFellowshipPhase.remove(possession);
                } else if (possession instanceof BotObjectAttachableCard attachableCard) {
                    List<BotCard> potentialTargets = plannedBoardState.getFpCardsInPlay(playerName).stream()
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
                    plannedBoardState.playOnBearer(possession, target);
                    inHandPlayableInFellowshipPhase.remove(possession);
                } else {
                    throw new IllegalStateException("Possession not instance of support area nor attachable object card: " + possession.getSelf().getBlueprint().getFullName());
                }
            }
        }
    }

    private void addPlayAlliesFromHandActions(List<BotCard> inHandPlayableInFellowshipPhase) {
        List<BotCard> playableAlliesInHand = inHandPlayableInFellowshipPhase.stream()
                .filter(card ->
                        CardType.ALLY.equals(card.getSelf().getBlueprint().getCardType())
                                && card.canBePlayed(plannedBoardState))
                .toList();

        List<BotCard> uniqueFilteredPlayableAllies = new ArrayList<>();
        for (BotCard ally : playableAlliesInHand) {
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
        for (BotCard allyInHand : uniqueFilteredPlayableAllies) {
            if (printDebugMessages) {
                System.out.println("Will play ally " + allyInHand.getSelf().getBlueprint().getFullName() + " from hand");
            }
            actions.add(new PlayCardFromHandAction(allyInHand.getSelf()));
            plannedBoardState.playToFpSupportArea(allyInHand);
            inHandPlayableInFellowshipPhase.remove(allyInHand);
        }
    }

    private void addPlayCompanionFromHandActions(List<BotCard> inHandPlayableInFellowshipPhase) {
        List<BotCard> playableCompanionsInHand = inHandPlayableInFellowshipPhase.stream()
                .filter(card ->
                        CardType.COMPANION.equals(card.getSelf().getBlueprint().getCardType())
                                && card.canBePlayed(plannedBoardState))
                .toList();

        int companionsInPlay = BoardStateUtil.getCompanionsInPlayCount(game, playerName);

        List<BotCard> uniqueFilteredPlayableCompanions = new ArrayList<>();
        for (BotCard companion : playableCompanionsInHand) {
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

        int ruleOfNineRemainder = BoardStateUtil.getRuleOfNineRemainder(game, playerName);

        int numberOfCompanionsThatCanBePlayed = Math.min(uniqueFilteredPlayableCompanions.size(), ruleOfNineRemainder);

        int numberOfCompanionsToBePlayed;
        if (companionsInPlay >= 6) {
            // already getting hit enquea, play whatever
            numberOfCompanionsToBePlayed = numberOfCompanionsThatCanBePlayed;
        } else {
            // if fellowship can get to large companion number, do it, else fill to 5 comps
            if (companionsInPlay + uniqueFilteredPlayableCompanions.size() >= 8) {
                numberOfCompanionsToBePlayed = numberOfCompanionsThatCanBePlayed;
            } else {
                numberOfCompanionsToBePlayed = Math.min(numberOfCompanionsThatCanBePlayed, 5 - companionsInPlay);
            }
        }

        if (numberOfCompanionsToBePlayed == uniqueFilteredPlayableCompanions.size()) {
            // play all
            for (BotCard companionInHand : uniqueFilteredPlayableCompanions) {
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

    private void addHealCompanionsByDiscardActions(List<BotCard> inHandPlayableInFellowshipPhase) {
        List<PhysicalCard> woundedUniqueCompanionsInPlay = BoardStateUtil.getWoundedCompanionsInPlay(game, playerName);

        for (PhysicalCard companion : woundedUniqueCompanionsInPlay) {
            int wounds = game.getGameState().getWounds(companion);

            List<BotCard> matchingCardsInHand = inHandPlayableInFellowshipPhase.stream()
                    .filter(cardInHand ->
                            CardType.COMPANION.equals(cardInHand.getSelf().getBlueprint().getCardType())
                            && cardInHand.getSelf().getBlueprint().getTitle().equals(companion.getBlueprint().getTitle()))
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
