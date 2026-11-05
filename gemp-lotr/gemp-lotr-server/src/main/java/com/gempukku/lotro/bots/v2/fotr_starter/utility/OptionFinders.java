package com.gempukku.lotro.bots.v2.fotr_starter.utility;

import com.gempukku.lotro.bots.v2.decision.option.*;
import com.gempukku.lotro.bots.v2.decision.request.DecisionRequest;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.PhysicalCard;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static com.gempukku.lotro.bots.v2.fotr_starter.utility.CardPredicates.hasFullName;

public final class OptionFinders {
    private OptionFinders() {
    }

    public static Option findActivate(List<Option> options, Predicate<PhysicalCard> cardFilter) {
        return options.stream()
                .filter(option -> option instanceof ActivateCardOption activateCardOption && cardFilter.test(activateCardOption.getCard()))
                .findFirst()
                .orElse(null);
    }

    public static Option findPlay(List<Option> options, Predicate<PhysicalCard> cardFilter) {
        return options.stream()
                .filter(option -> option instanceof PlayCardOption playCardOption && cardFilter.test(playCardOption.getCard()))
                .findFirst()
                .orElse(null);
    }

    public static Option findOptionOfType(List<Option> options, Class<? extends Option> optionType) {
        return options.stream()
                .filter(optionType::isInstance)
                .findFirst()
                .orElse(null);
    }

    public static List<PhysicalCard> getTargets(List<Option> options) {
        return options.stream()
                .filter(option -> option instanceof ChooseTargetForCardOption)
                .map(option -> ((ChooseTargetForCardOption) option).getTarget())
                .toList();
    }

    public static Option findOptionForTarget(List<Option> options, PhysicalCard target) {
        return options.stream()
                .filter(option -> option instanceof ChooseTargetForCardOption chooseTargetForCardOption && chooseTargetForCardOption.getTarget().getCardId() == target.getCardId())
                .findFirst()
                .orElse(null);
    }

    public static Option findTargetWithFullName(List<Option> options, String fullName) {
        return options.stream()
                .filter(option -> option instanceof ChooseTargetForCardOption chooseTargetForCardOption && hasFullName(fullName).test(chooseTargetForCardOption.getTarget()))
                .findFirst()
                .orElse(null);
    }

    public static Option requireTargetWithFullName(List<Option> options, String fullName, String errorMessage) {
        Option option = findTargetWithFullName(options, fullName);
        if (option == null) {
            throw new IllegalStateException(errorMessage);
        }
        return option;
    }

    public static Option findTargetByPriority(List<Option> options, List<String> fullNamePriority) {
        for (String fullName : fullNamePriority) {
            Option option = findTargetWithFullName(options, fullName);
            if (option != null) {
                return option;
            }
        }
        return null;
    }

    public static Optional<Option> findChoice(List<Option> options, String choice) {
        return options.stream()
                .filter(option -> option instanceof ChooseOptionForCardOption chooseOptionForCardOption && chooseOptionForCardOption.getChoice().equals(choice))
                .findFirst();
    }

    public static List<LotroCardBlueprint> getArbitraryTargets(List<Option> options) {
        return options.stream()
                .filter(option -> option instanceof ChooseArbitraryTargetForCardOption)
                .map(option -> ((ChooseArbitraryTargetForCardOption) option).getBlueprint())
                .toList();
    }

    public static Optional<Option> findArbitraryTarget(List<Option> options, String fullName) {
        return options.stream()
                .filter(option -> option instanceof ChooseArbitraryTargetForCardOption chooseArbitraryTargetForCardOption && chooseArbitraryTargetForCardOption.getBlueprint().getFullName().equals(fullName))
                .findFirst();
    }

    public static boolean sourceHasFullName(DecisionRequest decision, String fullName) {
        return decision.getSource().getBlueprint().getFullName().equals(fullName);
    }

    public static boolean textContains(DecisionRequest decision, String text) {
        return decision.getText().contains(text);
    }
}
