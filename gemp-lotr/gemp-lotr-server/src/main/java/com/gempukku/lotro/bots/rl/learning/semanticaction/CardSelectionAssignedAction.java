package com.gempukku.lotro.bots.rl.learning.semanticaction;

import com.alibaba.fastjson2.JSONObject;
import com.gempukku.lotro.game.state.Assignment;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.CardsSelectionDecision;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CardSelectionAssignedAction extends CardSelectionAction {
    private final List<Integer> minionsOnChosen = new ArrayList<>();
    private final List<Integer> strengthOfMinionsOnChosen = new ArrayList<>();
    private final List<Integer> minionsOnNotChosen = new ArrayList<>();
    private final List<Integer> strengthOfMinionsOnNotChosen = new ArrayList<>();

    public CardSelectionAssignedAction(String answer, AwaitingDecision decision, GameState gameState) {
        super(answer, decision, gameState);
        String[] individualCards = answer.split(",");

        for (String individualCard : individualCards) {
            for (Assignment assignment : gameState.getAssignments()) {
                if (assignment.getFellowshipCharacter().getCardId() == Integer.parseInt(individualCard)) {
                    minionsOnChosen.add(assignment.getShadowCharacters().size());
                    strengthOfMinionsOnChosen.add(assignment.getShadowCharacters().stream().mapToInt(value -> value.getBlueprint().getStrength()).sum());
                }
            }
        }

        if (decision instanceof CardsSelectionDecision csd) {
            String[] allChoices = csd.getDecisionParameters().get("cardId");
            for (String choice : allChoices) {
                if (Arrays.asList(individualCards).contains(choice)) {
                    continue;
                }
                for (Assignment assignment : gameState.getAssignments()) {
                    if (assignment.getFellowshipCharacter().getCardId() == Integer.parseInt(choice)) {
                        minionsOnNotChosen.add(assignment.getShadowCharacters().size());
                        strengthOfMinionsOnNotChosen.add(assignment.getShadowCharacters().stream().mapToInt(value -> value.getBlueprint().getStrength()).sum());
                    }
                }
            }
        }
    }

    public CardSelectionAssignedAction(String[] chosenBlueprintIds, String[] notChosenBlueprintIds, Integer[] woundsOnChosen,
                                       Integer[] woundsOnNotChosen, String zone, Integer[] minionsOnChosen,
                                       Integer[] strengthOfMinionsOnChosen, Integer[] minionsOnNotChosen,
                                       Integer[] strengthOfMinionsOnNotChosen) {
        super(chosenBlueprintIds, notChosenBlueprintIds, woundsOnChosen, woundsOnNotChosen, zone);
        this.minionsOnChosen.addAll(Arrays.asList(minionsOnChosen));
        this.strengthOfMinionsOnChosen.addAll(Arrays.asList(strengthOfMinionsOnChosen));
        this.minionsOnNotChosen.addAll(Arrays.asList(minionsOnNotChosen));
        this.strengthOfMinionsOnNotChosen.addAll(Arrays.asList(strengthOfMinionsOnNotChosen));
    }

    public List<Integer> getMinionsOnChosen() {
        return minionsOnChosen;
    }

    public List<Integer> getStrengthOfMinionsOnChosen() {
        return strengthOfMinionsOnChosen;
    }

    public List<Integer> getMinionsOnNotChosen() {
        return minionsOnNotChosen;
    }

    public List<Integer> getStrengthOfMinionsOnNotChosen() {
        return strengthOfMinionsOnNotChosen;
    }

    @Override
    public JSONObject toJson() {
        JSONObject obj = new JSONObject();
        obj.put("type", "CardSelectionAssignedAction");
        obj.put("chosenBlueprints", chosenBlueprintIds.toArray());
        obj.put("woundsOnChosen", woundsOnChosen.toArray());
        obj.put("notChosenBlueprints", notChosenBlueprintIds.toArray());
        obj.put("woundsOnNotChosen", woundsOnNotChosen.toArray());
        obj.put("zone", zoneString);
        obj.put("minionsOnChosen", minionsOnChosen.toArray());
        obj.put("strengthOfMinionsOnChosen", strengthOfMinionsOnChosen.toArray());
        obj.put("minionsOnNotChosen", minionsOnNotChosen.toArray());
        obj.put("strengthOfMinionsOnNotChosen", strengthOfMinionsOnNotChosen.toArray());
        return obj;
    }

    public static CardSelectionAssignedAction fromJson(JSONObject obj) {
        return new CardSelectionAssignedAction(obj.getObject("chosenBlueprints", String[].class), obj.getObject("notChosenBlueprints", String[].class),
                obj.getObject("woundsOnChosen", Integer[].class), obj.getObject("woundsOnNotChosen", Integer[].class), obj.getString("zone"),
                obj.getObject("minionsOnChosen", Integer[].class), obj.getObject("strengthOfMinionsOnChosen", Integer[].class),
                obj.getObject("minionsOnNotChosen", Integer[].class), obj.getObject("strengthOfMinionsOnNotChosen", Integer[].class));
    }
}
