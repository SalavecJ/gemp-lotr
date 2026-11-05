package com.gempukku.lotro.bots.v2.fotr_starter;

import com.gempukku.lotro.bots.v2.decision.option.Option;
import com.gempukku.lotro.bots.v2.decision.request.DecisionRequest;

import java.util.List;

public interface Plan {
    List<Option> nextAction(DecisionRequest decisionRequest);
    List<List<Option>> getAllOptions();
    boolean isComplete();
}
