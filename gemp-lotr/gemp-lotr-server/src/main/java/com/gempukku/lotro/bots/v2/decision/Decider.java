package com.gempukku.lotro.bots.v2.decision;

import com.gempukku.lotro.bots.v2.decision.option.Option;
import com.gempukku.lotro.bots.v2.decision.request.DecisionRequest;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.List;

public interface Decider {
    List<Option> decide(DecisionRequest decisionRequest, DefaultLotroGame game);
}
