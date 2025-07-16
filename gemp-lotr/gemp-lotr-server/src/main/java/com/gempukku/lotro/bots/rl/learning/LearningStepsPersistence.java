package com.gempukku.lotro.bots.rl.learning;

import java.util.List;

public interface LearningStepsPersistence {
    void save(List<LearningStep> steps);
    List<LearningStep> load(Trainer trainer);
}
