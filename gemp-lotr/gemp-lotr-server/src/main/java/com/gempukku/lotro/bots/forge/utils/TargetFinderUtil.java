package com.gempukku.lotro.bots.forge.utils;

import com.gempukku.lotro.bots.forge.cards.ability.AbilityStep;
import com.gempukku.lotro.bots.forge.cards.ability.targeting.BotTargetingPolicy;
import com.gempukku.lotro.bots.forge.cards.abstractcards.BotCard;
import com.gempukku.lotro.bots.forge.cards.abstractcards.BotItemCard;
import com.gempukku.lotro.bots.forge.plan.action.ChooseTargetsAction;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.gempukku.lotro.bots.forge.utils.BotLogging.log;

public class TargetFinderUtil {

    public static ChooseTargetsAction getBestTarget(List<ChooseTargetsAction> possibleActions, DefaultLotroGame game, String playerName) {
        if (possibleActions.stream().allMatch(ChooseTargetsAction::isAttachAction)) {
            ChooseTargetsAction firstAction = possibleActions.getFirst();

            BotItemCard attachment = (BotItemCard) firstAction.getSource();
            verifyAllActionsShareSameCard(possibleActions);

            List<BotCard> potentialTargets = possibleActions.stream()
                    .map(ChooseTargetsAction::getTargets)
                    .map(List::getFirst)
                    .toList();

            BotCard chosenTarget = attachment.chooseTargetToAttachTo(game, potentialTargets);

            return findActionWithTarget(possibleActions, List.of(chosenTarget.getPhysicalCard()));
        } else {
            ChooseTargetsAction firstAction = possibleActions.getFirst();
            verifyAllActionsShareSameCard(possibleActions);
            AbilityStep step = firstAction.getSource().getAbilityStepForDecision(firstAction.getDecisionText());

            if (step == null) {
                throw new IllegalStateException("Could not find ability step for decision text " + firstAction.getDecisionText() + " and source " + firstAction.getSource());
            }

            log(2, "Choosing targets for step: '" + step + "' of card: " + firstAction.getSource());
            BotTargetingPolicy targetingPolicy = step.getBotTargetingPolicy();

            if (targetingPolicy == null) {
                throw new IllegalStateException("No targeting policy for step: '" + step + "' of card: " + firstAction.getSource());
            }
            log(2, "Using targeting policy: " + targetingPolicy);

            List<List<BotCard>> potentialTargets = possibleActions.stream()
                    .map(ChooseTargetsAction::getTargets)
                    .toList();

            List<BotCard> chosenTargets = targetingPolicy.getTargets(potentialTargets, game, playerName);
            log(2, "Chosen targets: " + chosenTargets);

            return findActionWithTarget(possibleActions, chosenTargets.stream().map(BotCard::getPhysicalCard).toList());
        }
    }

    private static void verifyAllActionsShareSameCard(List<ChooseTargetsAction> possibleActions) {
        BotCard firstCard = possibleActions.getFirst().getSource();
        for (ChooseTargetsAction action : possibleActions) {
            BotCard card = action.getSource();
            if (!card.equals(firstCard)) {
                throw new IllegalStateException("Not all actions share the same source");
            }
        }
    }

    private static ChooseTargetsAction findActionWithTarget(List<ChooseTargetsAction> possibleActions, List<PhysicalCard> chosenTargets) {
        if (chosenTargets == null) {
            throw new IllegalStateException("Could not find target for " + possibleActions.getFirst().getSource());
        }

        return possibleActions.stream()
                .filter(action -> listsHaveSameElements(action.getTargets().stream().map(BotCard::getPhysicalCard).toList(), chosenTargets))
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
