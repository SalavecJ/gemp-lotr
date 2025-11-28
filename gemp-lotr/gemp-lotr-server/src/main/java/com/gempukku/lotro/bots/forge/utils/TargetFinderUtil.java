package com.gempukku.lotro.bots.forge.utils;

import com.gempukku.lotro.bots.forge.cards.ability2.Ability;
import com.gempukku.lotro.bots.forge.cards.ability2.ActivatedAbility;
import com.gempukku.lotro.bots.forge.cards.ability2.EventAbility;
import com.gempukku.lotro.bots.forge.cards.ability2.TriggeredAbility;
import com.gempukku.lotro.bots.forge.cards.ability2.cost.Cost;
import com.gempukku.lotro.bots.forge.cards.ability2.cost.CostWithTarget;
import com.gempukku.lotro.bots.forge.cards.ability2.effect.Effect;
import com.gempukku.lotro.bots.forge.cards.ability2.effect.EffectWithTarget;
import com.gempukku.lotro.bots.forge.cards.abstractcard.BotCard;
import com.gempukku.lotro.bots.forge.cards.abstractcard.BotObjectAttachableCard;
import com.gempukku.lotro.bots.forge.plan.action2.ChooseTargetsAction2;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TargetFinderUtil {
    public static ChooseTargetsAction2 getBestTarget(List<ChooseTargetsAction2> possibleActions, DefaultLotroGame game, String playerName) {
        if (possibleActions.stream().allMatch(ChooseTargetsAction2::isAttachAction)) {
            ChooseTargetsAction2 firstAction = possibleActions.getFirst();

            BotObjectAttachableCard attachment = (BotObjectAttachableCard) firstAction.getSource();
            verifyAllActionsShareSameCard(possibleActions);

            List<BotCard> potentialTargets = possibleActions.stream()
                    .map(ChooseTargetsAction2::getTargets)
                    .map(List::getFirst)
                    .toList();

            BotCard chosenTarget = attachment.chooseTargetToAttachTo(game, potentialTargets);

            return findActionWithTarget(possibleActions, List.of(chosenTarget.getSelf()));
        } else {
            ChooseTargetsAction2 firstAction = possibleActions.getFirst();

            BotCard source = firstAction.getSource();

            verifyAllActionsShareSameCard(possibleActions);

            List<List<PhysicalCard>> potentialTargets = possibleActions.stream()
                    .map(ChooseTargetsAction2::getTargets)
                    .map(botCards -> {
                        List<PhysicalCard> tbr = new ArrayList<>();
                        for (BotCard botCard : botCards) {
                            tbr.add(botCard.getSelf());
                        }
                        return tbr;
                    })
                    .toList();

            List<ActivatedAbility> abilities = source.getActivatedAbilities();
            TriggeredAbility triggeredAbility = source.getTriggeredAbility();
            EventAbility eventAbility = source.getEventAbility();

            int possibleAbilitySources = abilities.size() + (triggeredAbility != null ? 1 : 0) + (eventAbility != null ? 1 : 0);
            if (possibleAbilitySources != 1) {
                throw new IllegalStateException("Source " + source.getSelf().getBlueprint().getFullName() + " has multiple possible abilities to choose from");
            }

            Ability sourceAbility =  (abilities.isEmpty() ? (triggeredAbility == null ? eventAbility : triggeredAbility) : abilities.getFirst());
            Cost cost = sourceAbility.getCost();
            Effect effect = sourceAbility.getEffect();

            boolean costTargeting;
            if (cost instanceof CostWithTarget costWithTarget) {
                costTargeting = costWithTarget.decisionTextMatches(firstAction.getDecisionText());
            } else {
                costTargeting = false;
            }

            boolean effectTargeting;
            if (effect instanceof EffectWithTarget effectWithTarget) {
                effectTargeting = effectWithTarget.decisionTextMatches(firstAction.getDecisionText());
            } else {
                effectTargeting = false;
            }

            if (costTargeting && effectTargeting) {
                throw new IllegalStateException("Unclear what targets are for - cost or effect for source " + source.getSelf().getBlueprint().getFullName() + " and decision text " + firstAction.getDecisionText());
            } else if (!costTargeting && !effectTargeting) {
                throw new IllegalStateException("Neither cost nor effect seem to be targeting for source " + source.getSelf().getBlueprint().getFullName() + " and decision text " + firstAction.getDecisionText());
            } else if (costTargeting) {
                CostWithTarget costWithTarget = (CostWithTarget) sourceAbility.getCost();
                List<PhysicalCard> chosenTargets = costWithTarget.chooseTargets(playerName, game, potentialTargets);
                return findActionWithTarget(possibleActions, chosenTargets);
            } else {
                EffectWithTarget effectWithTarget = (EffectWithTarget) sourceAbility.getEffect();
                List<PhysicalCard> chosenTargets = effectWithTarget.chooseTargets(playerName, game, potentialTargets);
                return findActionWithTarget(possibleActions, chosenTargets);
            }
        }
    }

    private static void verifyAllActionsShareSameCard(List<ChooseTargetsAction2> possibleActions) {
        BotCard firstCard = possibleActions.getFirst().getSource();
        for (ChooseTargetsAction2 action : possibleActions) {
            BotCard card = action.getSource();
            if (!card.equals(firstCard)) {
                throw new IllegalStateException("Not all actions share the same source");
            }
        }
    }

    private static ChooseTargetsAction2 findActionWithTarget(List<ChooseTargetsAction2> possibleActions, List<PhysicalCard> chosenTargets) {
        if (chosenTargets == null) {
            throw new IllegalStateException("Could not find target for " + possibleActions.getFirst().getSource());
        }

        return possibleActions.stream()
                .filter(action -> listsHaveSameElements(action.getTargets().stream().map(BotCard::getSelf).toList(), chosenTargets))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Could not find action for chosen target"));
    }


    private static <T> boolean listsHaveSameElements(List<T> a, List<T> b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        if (a.size() != b.size()) return false;

        Map<T, Integer> counts = new HashMap<>();
        for (T e : a) counts.merge(e, 1, Integer::sum);
        for (T e : b) {
            Integer c = counts.get(e);
            if (c == null) return false;
            if (c == 1) counts.remove(e);
            else counts.put(e, c - 1);
        }
        return counts.isEmpty();
    }
}
