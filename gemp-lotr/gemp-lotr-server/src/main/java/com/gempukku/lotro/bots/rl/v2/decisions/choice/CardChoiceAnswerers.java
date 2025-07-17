package com.gempukku.lotro.bots.rl.v2.decisions.choice;

import com.gempukku.lotro.bots.BotService;
import com.gempukku.lotro.bots.rl.learning.LearningStep;
import com.gempukku.lotro.bots.rl.learning.semanticaction.MultipleChoiceAction;
import com.gempukku.lotro.bots.rl.learning.semanticaction.SemanticAction;
import com.gempukku.lotro.bots.rl.v2.learning.SavedVector;
import com.gempukku.lotro.bots.rl.v2.learning.choice.AbstractChoiceTrainer;
import com.gempukku.lotro.bots.rl.v2.state.GeneralStateExtractor;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.*;
import java.util.stream.Collectors;

public class CardChoiceAnswerers {

    public static Map<AbstractChoiceTrainer, AbstractChoiceAnswerer> generateUniqueCardChoicePairs() {
        Map<String, LotroCardBlueprint> allBlueprints = BotService.staticLibrary.getBaseCards();
        Map<List<String>, AbstractChoiceTrainer> trainerMap = new HashMap<>();
        Map<AbstractChoiceTrainer, AbstractChoiceAnswerer> result = new LinkedHashMap<>();

        for (Map.Entry<String, LotroCardBlueprint> idBlueprintEntry : allBlueprints.entrySet()) {
            JSONObject json = idBlueprintEntry.getValue().getJsonDefinition();
            List<List<String>> choices = extractAllChoiceTexts(json);

            for (List<String> cardOptions : choices) {
                if (cardOptions.size() < 2 || cardOptions.size() > 4)
                    continue;

                // Normalize options for deduplication
                List<String> normalizedOptions = cardOptions.stream()
                        .map(String::trim)
                        .map(String::toLowerCase)
                        .sorted()
                        .toList();

                if (trainerMap.containsKey(normalizedOptions))
                    continue; // Already added

                AbstractChoiceTrainer trainer = new AbstractChoiceTrainer() {
                    @Override
                    public boolean isStepRelevant(LearningStep step) {
                        if (step.decision.getDecisionType() != AwaitingDecisionType.MULTIPLE_CHOICE)
                            return false;

                        if (!(step.action instanceof MultipleChoiceAction))
                            return false;

                        String[] options = step.decision.getDecisionParameters().get("results");
                        if (options.length != cardOptions.size())
                            return false;

                        List<String> inputOptions = Arrays.stream(options).map(String::trim).collect(Collectors.toList());
                        List<String> normalizedCardOptions = cardOptions.stream().map(String::trim).collect(Collectors.toList());

                        return new HashSet<>(inputOptions).equals(new HashSet<>(normalizedCardOptions));
                    }

                    @Override
                    protected String getTextTrigger() {
                        return null;
                    }

                    @Override
                    protected List<String> getOptions() {
                        return cardOptions;
                    }

                    @Override
                    public double[] extractFeatures(GameState gameState, AwaitingDecision decision, String playerName) {
                        return GeneralStateExtractor.extractFeatures(gameState, decision, playerName);
                    }

                    @Override
                    public String getName() {
                        return "Trainer" + cardOptions;
                    }

                    @Override
                    public List<SavedVector> toStringVectors(GameState gameState, SemanticAction action, String playerId, AwaitingDecision decision) {
                        String className = getName();
                        double[] state = extractFeatures(gameState, decision, playerId);

                        List<String> options = getOptions();
                        String chosenOption = ((MultipleChoiceAction) action).getChosenOption();

                        int chosenIndex = options.indexOf(chosenOption);
                        if (chosenIndex == -1)
                            throw new IllegalArgumentException("Chosen option not found in options");

                        double[] chosen = new double[options.size()];
                        chosen[chosenIndex] = 1.0;

                        List<double[]> notChosen = new ArrayList<>();
                        for (int i = 0; i < options.size(); i++) {
                            if (i != chosenIndex) {
                                double[] notChosenVec = new double[options.size()];
                                notChosenVec[i] = 1.0;
                                notChosen.add(notChosenVec);
                            }
                        }

                        return List.of(new SavedVector(className, state, chosen, notChosen));
                    }
                };

                AbstractChoiceAnswerer answerer = new AbstractChoiceAnswerer() {
                    @Override
                    public boolean appliesTo(GameState gameState, AwaitingDecision decision, String playerName) {
                        if (decision.getDecisionType() != AwaitingDecisionType.MULTIPLE_CHOICE)
                            return false;

                        String[] options = decision.getDecisionParameters().get("results");
                        if (options.length != cardOptions.size())
                            return false;

                        List<String> inputOptions = Arrays.stream(options).map(String::trim).collect(Collectors.toList());
                        List<String> normalizedCardOptions = cardOptions.stream().map(String::trim).collect(Collectors.toList());

                        return new HashSet<>(inputOptions).equals(new HashSet<>(normalizedCardOptions));
                    }

                    @Override
                    public double[] extractFeatures(GameState gameState, AwaitingDecision decision, String playerName) {
                        return GeneralStateExtractor.extractFeatures(gameState, decision, playerName);
                    }

                    @Override
                    protected String getTextTrigger() {
                        return null;
                    }

                    @Override
                    protected List<String> getOptions() {
                        return cardOptions;
                    }

                    @Override
                    public String getName() {
                        return "Answerer" + cardOptions;
                    }
                };

                trainerMap.put(normalizedOptions, trainer);
                result.put(trainer, answerer);
            }
        }

        return result;
    }

    private static List<List<String>> extractAllChoiceTexts(JSONObject jsonDefinition) {
        List<List<String>> allChoices = new ArrayList<>();
        findChoiceBlocks(jsonDefinition, allChoices);
        return allChoices;
    }

    private static void findChoiceBlocks(Object node, List<List<String>> allChoices) {
        if (node instanceof JSONObject obj) {
            if ("choice".equals(obj.get("type")) && obj.containsKey("texts")) {
                Object textArray = obj.get("texts");
                if (textArray instanceof JSONArray arr && arr.size() >= 2 && arr.size() <= 4) {
                    List<String> options = new ArrayList<>();
                    for (Object text : arr) {
                        options.add(text.toString());
                    }
                    allChoices.add(options);
                }
            }

            for (Object value : obj.values()) {
                findChoiceBlocks(value, allChoices);
            }
        } else if (node instanceof JSONArray arr) {
            for (Object item : arr) {
                findChoiceBlocks(item, allChoices);
            }
        }
    }
}
