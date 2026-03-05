package com.gempukku.lotro.bots.forge;

import com.gempukku.lotro.bots.BotPlayer;
import com.gempukku.lotro.bots.BotService;
import com.gempukku.lotro.bots.forge.plan.FellowshipPhasePlan;
import com.gempukku.lotro.bots.forge.plan.Plan;
import com.gempukku.lotro.bots.forge.plan.ShadowAssigningPlan;
import com.gempukku.lotro.bots.forge.plan.specific.SpecificPlanMaker;
import com.gempukku.lotro.bots.forge.utils.StartingFellowshipUtil;
import com.gempukku.lotro.bots.random.RandomDecisionBot;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.gempukku.lotro.bots.forge.utils.BotLogging.log;

public class ForgeBot extends RandomDecisionBot implements BotPlayer {
    private Plan plan = null;

    public ForgeBot(String name) {
        super(name);
        if (!name.startsWith("~")) {
            throw new IllegalArgumentException("Bot names need to start with '~' character");
        }
    }

    @Override
    public void cleanUpAfterGame() {
        log(1, "Game ended, cleaning up", true);
        plan = null;
    }

    @Override
    public void decisionMadeByPlayer(DefaultLotroGame game, AwaitingDecision awaitingDecision, String answer, String player) {
        if (plan != null) {
            plan.decisionMadeByPlayer(awaitingDecision, answer, player);
        }
    }

    @Override
    public String chooseAction(DefaultLotroGame game, AwaitingDecision awaitingDecision) {
        try {
            return makeDecision(game, awaitingDecision);
        } catch (UnsupportedOperationException e) {
            log(1, e.getMessage());
            return super.chooseAction(game, awaitingDecision);
        }
    }

    private String makeDecision(DefaultLotroGame game, AwaitingDecision awaitingDecision) {
        log(3, "Making decision: " + awaitingDecision.toJson().toString(), true);
        if (isGamePreparationDecision(awaitingDecision)) {
            return makeGamePreparationDecision(game, awaitingDecision);
        } else {
            return makeGameDecision(game, awaitingDecision);
        }
    }

    private String makeGameDecision(DefaultLotroGame game, AwaitingDecision awaitingDecision) {
        if (plan == null || plan.isOutdated()) {
            makeNewPlan(game, awaitingDecision);
        }
        try {
            return plan.chooseActionToTakeOrPass(game, awaitingDecision);
        } catch (Exception e) {
            throw new UnsupportedOperationException("Error carrying out plan: " + e.getMessage(), e);
        }
    }

    private void makeNewPlan(DefaultLotroGame game, AwaitingDecision awaitingDecision) {
        if (SpecificPlanMaker.canMakePlan(game, awaitingDecision)) {
            plan = SpecificPlanMaker.makePlan(game, awaitingDecision);
        } else if (game.getGameState().getCurrentPhase() == Phase.FELLOWSHIP && game.getGameState().getCurrentPlayerId().equals(getName())){
            plan = new FellowshipPhasePlan(game);
        } else if (game.getGameState().getCurrentPhase() == Phase.ASSIGNMENT) {
            if (awaitingDecision.getText().equals("Assign minions to companions or allies at home")) {
                if (game.getGameState().getCurrentPlayerId().equals(getName())) {
                    // TODO
                    throw new UnsupportedOperationException("Making fp assigning plan not implemented yet. Decision: " + awaitingDecision.toJson().toString());
                } else {
                    plan = new ShadowAssigningPlan(game);
                }
            } else {
                // TODO
                throw new UnsupportedOperationException("Making assignment phase plans for not assigning not implemented yet. Decision: " + awaitingDecision.toJson().toString());
            }
        } else {
            throw new UnsupportedOperationException("Making non-fellowship phase plans not implemented yet. Decision: " + awaitingDecision.toJson().toString());
            //TODO make plans
        }
    }

    private boolean isGamePreparationDecision(AwaitingDecision awaitingDecision) {
        return isBurdenBidDecision(awaitingDecision)
                || isGoFirstDecision(awaitingDecision)
                || isStartingFellowshipDecision(awaitingDecision)
                || isMulliganDecision(awaitingDecision);
    }

    private String makeGamePreparationDecision(DefaultLotroGame game, AwaitingDecision awaitingDecision) {
        if (isBurdenBidDecision(awaitingDecision)) {
            return makeBurdenBidDecision(game, awaitingDecision);
        } else if (isGoFirstDecision(awaitingDecision)) {
            return makeGoFirstDecision(game, awaitingDecision);
        } else if (isStartingFellowshipDecision(awaitingDecision)) {
            return makeStartingFellowshipDecision(game, awaitingDecision);
        } else if (isMulliganDecision(awaitingDecision)) {
            return makeMulliganDecision(game, awaitingDecision);
        }
        throw new UnsupportedOperationException("Game preparation decision not supported: " + awaitingDecision.toJson().toString());
    }

    public boolean isBurdenBidDecision(AwaitingDecision awaitingDecision) {
        return awaitingDecision.getDecisionType().equals(AwaitingDecisionType.INTEGER)
                && awaitingDecision.getText().equals("Choose a number of burdens to bid");
    }

    private String makeBurdenBidDecision(DefaultLotroGame game, AwaitingDecision awaitingDecision) {
        log(1, "Chosing number to bid", true);

        // Bid randomly less than half of resistance
        int resistance = 10;
        if (BotService.staticLibrary != null) {
            try {
                resistance = BotService.staticLibrary.getLotroCardBlueprint(game.getGameState().getLotroDeck(getName()).getRingBearer()).getResistance();
                log(2, "Ring-bearer found: " + BotService.staticLibrary.getLotroCardBlueprint(game.getGameState().getLotroDeck(getName()).getRingBearer()).getFullName());
            } catch (CardNotFoundException ignored) {

            }
        }

        log(2, "Ring-bearer resistance: " + resistance);

        // Bid more if starting with Sam
        boolean startingWithSamSoh = false;
        try {
            startingWithSamSoh = StartingFellowshipUtil.getStartingFellowship(game.getGameState().getLotroDeck(getName())).contains("1_311");
        } catch (UnsupportedOperationException ignored) {

        }

        Random random = new Random();
        int origin = startingWithSamSoh ? 3 : 0;
        int bound = (resistance / 2) + (startingWithSamSoh ? 2 : 0);
        int chosenNumber = random.nextInt(origin, bound);

        log(2, "Choosing randomly between " + origin + " (inclusive) and " + bound + " (exclusive)");
        if (startingWithSamSoh) {
            log(2, "Bidding more because Sam is in starting fellowship");
        }
        log(1, "Chosen: " + chosenNumber);

        return String.valueOf(chosenNumber);
    }

    public boolean isGoFirstDecision(AwaitingDecision awaitingDecision) {
        if (!awaitingDecision.getDecisionType().equals(AwaitingDecisionType.MULTIPLE_CHOICE))
            return false;

        String[] options = awaitingDecision.getDecisionParameters().get("results");
        if (options.length != 2)
            return false;

        List<String> inputOptions = Arrays.stream(options).map(String::trim).toList();
        List<String> normalizedCardOptions = Stream.of("Go first", "Go second").map(String::trim).toList();

        return new HashSet<>(inputOptions).equals(new HashSet<>(normalizedCardOptions));
    }

    private String makeGoFirstDecision(DefaultLotroGame game, AwaitingDecision awaitingDecision) {
        log(1, "Deciding whether to go first - always go first if possible", true);
        return getWantedOptionIndex(awaitingDecision, "Go first");
    }

    public boolean isMulliganDecision(AwaitingDecision awaitingDecision) {
        return awaitingDecision.getDecisionType().equals(AwaitingDecisionType.MULTIPLE_CHOICE)
                && awaitingDecision.getText().equals("Do you wish to mulligan? (Shuffle cards back and draw 6)");
    }

    private String makeMulliganDecision(DefaultLotroGame game, AwaitingDecision awaitingDecision) {
        boolean goingFirst = game.getGameState().getFirstPlayerId().equals(getName());

        log(1, "Choosing whether to mulligan while " + (goingFirst ? "going first" : "going second"), true);

        List<? extends PhysicalCard> hand = game.getGameState().getHand(getName());

        StringBuilder builder = new StringBuilder("Cards in hand: ");
        hand.forEach(card -> builder.append(card.getBlueprint().getFullName()).append("; "));
        log(2, builder.toString());

        if (hand.stream().anyMatch((Predicate<PhysicalCard>) physicalCard -> physicalCard.getBlueprintId().equals("8_20"))) {
            // Always keep hand with Saved from the Fire
            log(2, "Keeping hand with Saved from the Fire");
            return getWantedOptionIndex(awaitingDecision, "No");
        }

        // TODO check if cards are playable
        int fpCards = Math.toIntExact(hand.stream().filter((Predicate<PhysicalCard>) physicalCard -> physicalCard.getBlueprint().getSide().equals(Side.FREE_PEOPLE)).count());
        int shadowCards = Math.toIntExact(hand.stream().filter((Predicate<PhysicalCard>) physicalCard -> physicalCard.getBlueprint().getSide().equals(Side.SHADOW)).count());

        // Can expect 3 cards after mulligan
        boolean atLeastThreeFpCards = fpCards >= 3;
        boolean atLeastThreeShadowCards = shadowCards >= 3;

        boolean keep = (goingFirst && atLeastThreeFpCards) || (!goingFirst && atLeastThreeShadowCards);

        log(2, fpCards + " fp cards and " + shadowCards + " shadow cards in hand while going " + (goingFirst ? "first" : "second"));
        log(1, (keep ? "Keep" : "Mulligan"));

        return getWantedOptionIndex(awaitingDecision, keep? "No" : "Yes");
    }

    public boolean isStartingFellowshipDecision(AwaitingDecision awaitingDecision) {
        return awaitingDecision.getDecisionType().equals(AwaitingDecisionType.ARBITRARY_CARDS)
                && awaitingDecision.getText().equals("Starting fellowship - Choose next character or press DONE");
    }

    private String makeStartingFellowshipDecision(DefaultLotroGame game, AwaitingDecision awaitingDecision) {
        log(1, "Choosing starting fellowship", true);

        String wantedOption = ""; // Pass

        // Find complete starting fellowship
        List<String> startingFellowship = StartingFellowshipUtil.getStartingFellowship(game.getGameState().getLotroDeck(getName()));

        String wantedCompanionsMessage = "Wanted companions: ";
        if (BotService.staticLibrary != null) {
            List<String> names = startingFellowship.stream().map(s -> {
                try {
                    return BotService.staticLibrary.getLotroCardBlueprint(s).getFullName();
                } catch (CardNotFoundException ignored) {
                    return s;
                }
            }).toList();
            wantedCompanionsMessage += names;
        } else {
            wantedCompanionsMessage += startingFellowship;
        }
        log(2, wantedCompanionsMessage);

        List<? extends PhysicalCard> cardsInPlay = game.getGameState().getInPlay().stream().filter((Predicate<PhysicalCard>) physicalCard -> physicalCard.getOwner().equals(getName())).toList();
        for (String companion : startingFellowship) {
            // Find the first one that has not been played already
            if (cardsInPlay.stream().anyMatch((Predicate<PhysicalCard>) physicalCard -> physicalCard.getBlueprintId().equals(companion))) {
                continue;
            }
            String toPlayCompanionMessage = "Chosen: ";

            if (BotService.staticLibrary != null) {
                try {
                    toPlayCompanionMessage += BotService.staticLibrary.getLotroCardBlueprint(companion).getFullName();
                } catch (CardNotFoundException ignored) {
                    toPlayCompanionMessage += companion;
                }
            } else {
                toPlayCompanionMessage += companion;
            }

            log(1, toPlayCompanionMessage);

            wantedOption = companion;
        }

        if (wantedOption.isEmpty()) {
            log(1, "All companions played already, passing");
            return ""; // Pass
        }

        List<String> cardIds = Arrays.stream(awaitingDecision.getDecisionParameters().get("cardId")).toList();
        List<String> blueprints = Arrays.stream(awaitingDecision.getDecisionParameters().get("blueprintId")).toList();

        for (int i = 0; i < blueprints.size(); i++) {
            if (blueprints.get(i).equals(wantedOption)) {
                return cardIds.get(i);
            }
        }

        throw new UnsupportedOperationException("Decision not supported: " + awaitingDecision.toJson().toString());
    }

    private String getWantedOptionIndex(AwaitingDecision awaitingDecision, String wantedOption) {
        String[] options = awaitingDecision.getDecisionParameters().get("results");
        for (int i = 0; i < options.length; i++) {
            if (options[i].equals(wantedOption)) {
                return String.valueOf(i);
            }
        }
        throw new UnsupportedOperationException("Decision not supported: " + awaitingDecision.toJson().toString());
    }
}
