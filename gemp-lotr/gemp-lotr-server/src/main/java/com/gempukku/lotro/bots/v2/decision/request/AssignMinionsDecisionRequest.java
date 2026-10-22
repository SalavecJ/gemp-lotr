package com.gempukku.lotro.bots.v2.decision.request;

import com.gempukku.lotro.bots.v2.decision.option.AssignMinionOption;
import com.gempukku.lotro.bots.v2.decision.option.Option;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.decisions.PlayerAssignMinionsDecision;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AssignMinionsDecisionRequest extends DecisionRequest {
    private static final String ASSIGN_MINIONS_TEXT = "Assign minions to companions or allies at home";

    private final boolean fpPlayerAssigning;

    private final List<PhysicalCard> fpCharacters = new ArrayList<>();
    private final List<PhysicalCard> minions = new ArrayList<>();
    private int nextMinionIndex = 0;

    private final Map<PhysicalCard, List<PhysicalCard>> assignments = new HashMap<>();
    private final List<PhysicalCard> unassignedMinions = new ArrayList<>();

    private final List<Option> chosenOptions = new ArrayList<>();

    public AssignMinionsDecisionRequest(PlayerAssignMinionsDecision decision, DefaultLotroGame game) {
        super(getDecisionText(decision, game));

        Map<String, String[]> params = decision.getDecisionParameters();
        String[] player = params.get("player");
        fpPlayerAssigning = player[0].equals("fp");

        String[] freeCharacters = params.get("freeCharacters");
        String[] minionCharacters = params.get("minions");

        for (String freeCharacter : freeCharacters) {
            PhysicalCard card = game.getGameState().getPhysicalCard(Integer.parseInt(freeCharacter));
            fpCharacters.add(card);
        }

        for (String minionCharacter : minionCharacters) {
            PhysicalCard card = game.getGameState().getPhysicalCard(Integer.parseInt(minionCharacter));
            minions.add(card);
        }

        for (PhysicalCard fpCharacter : fpCharacters) {
            assignments.put(fpCharacter, new ArrayList<>());
        }
    }

    private static String getDecisionText(PlayerAssignMinionsDecision decision, DefaultLotroGame game) {
        Map<String, String[]> params = decision.getDecisionParameters();
        boolean fpPlayerAssigning = params.get("player")[0].equals("fp");
        String[] freeCharacters = params.get("freeCharacters");
        String[] minionCharacters = params.get("minions");

        List<PhysicalCard> fpCharacters = new ArrayList<>();
        List<PhysicalCard> minions = new ArrayList<>();
        for (String freeCharacter : freeCharacters) {
            PhysicalCard card = game.getGameState().getPhysicalCard(Integer.parseInt(freeCharacter));
            fpCharacters.add(card);
        }
        for (String minionCharacter : minionCharacters) {
            PhysicalCard card = game.getGameState().getPhysicalCard(Integer.parseInt(minionCharacter));
            minions.add(card);
        }

        StringBuilder sb = new StringBuilder();
        if (fpPlayerAssigning) {
            sb.append("Fp assignment: ");
        } else {
            sb.append("Shadow assignment: ");
        }

        sb.append(fpCharacters.stream().map(physicalCard -> physicalCard.getBlueprint().getFullName()).collect(Collectors.joining("; ")));
        sb.append(" vs ");
        sb.append(minions.stream().map(physicalCard -> physicalCard.getBlueprint().getFullName()).collect(Collectors.joining("; ")));

        return sb.toString();
    }

    public List<PhysicalCard> getFpCharacters() {
        return fpCharacters;
    }

    public List<PhysicalCard> getMinions() {
        return minions;
    }

    public PhysicalCard getNextMinion() {
        if (nextMinionIndex < minions.size()) {
            return minions.get(nextMinionIndex);
        }
        return null;
    }

    public List<PhysicalCard> getAvailableFpCharacters() {
        List<PhysicalCard> available = new ArrayList<>();
        for (PhysicalCard fpCharacter : fpCharacters) {
            if (assignments.get(fpCharacter).isEmpty() || !fpPlayerAssigning) {
                available.add(fpCharacter);
            }
        }
        return available;
    }

    public void assign(AssignMinionOption option) {
        if (option.getMinion().getCardId() != getNextMinion().getCardId()) {
            throw new IllegalArgumentException("Option does not match the next minion to assign");
        }

        PhysicalCard fpCharacter = option.getFpCharacter();

        if (fpCharacter == null && !fpPlayerAssigning) {
            throw new IllegalArgumentException("Shadow player cannot leave minion unassigned");
        }

        if (fpCharacter != null) {
            assignNextMinionToFpCharacter(fpCharacter);
        } else {
            leaveNextMinionUnassigned();
        }
        chosenOptions.add(option);
    }

    private void assignNextMinionToFpCharacter(PhysicalCard fpCharacter) {
        if (nextMinionIndex < minions.size()) {
            PhysicalCard minion = minions.get(nextMinionIndex);
            assignments.get(fpCharacter).add(minion);
            nextMinionIndex++;
        }
    }

    private void leaveNextMinionUnassigned() {
        if (nextMinionIndex < minions.size()) {
            PhysicalCard minion = minions.get(nextMinionIndex);
            unassignedMinions.add(minion);
            nextMinionIndex++;
        }
    }

    @Override
    public List<Option> getOptions() {
        PhysicalCard nextMinion = getNextMinion();
        if (nextMinion == null) {
            return null;
        }
        List<Option> tbr = new ArrayList<>();
        for (PhysicalCard fpCharacter : getAvailableFpCharacters()) {
            tbr.add(new AssignMinionOption(fpCharacter, nextMinion));
        }
        if (fpPlayerAssigning) {
            tbr.add(new AssignMinionOption(null, nextMinion));
        }
        return tbr;
    }

    public List<Option> getChosenOptions() {
        return chosenOptions;
    }

    public String combineSelected(List<Option> picked) {
        if (chosenOptions.size() != picked.size()) {
            throw new IllegalArgumentException("Picked options do not match the number of chosen options");
        }

        boolean allMatch = true;
        for (int i = 0; i < chosenOptions.size(); i++) {
            if (!chosenOptions.get(i).equals(picked.get(i))) {
                allMatch = false;
                break;
            }
        }

        if (!allMatch) {
            throw new IllegalArgumentException("Picked options do not match the chosen options");
        }

        return getAnswerForGameEngine();
    }

    private String getAnswerForGameEngine() {
        if (getNextMinion() != null) {
            throw new IllegalStateException("Not all minions have been assigned yet");
        }

        Map<String, List<String>> stringAssignments = new HashMap<>();
        for (Map.Entry<PhysicalCard, List<PhysicalCard>> entry : assignments.entrySet()) {
            String fpCharacterId = String.valueOf(entry.getKey().getCardId());
            List<String> minionIds = new ArrayList<>();
            for (PhysicalCard minion : entry.getValue()) {
                minionIds.add(String.valueOf(minion.getCardId()));
            }
            stringAssignments.put(fpCharacterId, minionIds);
        }


        return stringAssignments.entrySet().stream()
                .map(entry -> entry.getKey() + " " + String.join(" ", entry.getValue()))
                .collect(Collectors.joining(","));
    }
}
