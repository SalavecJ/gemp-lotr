package com.gempukku.lotro.bots.rl.fotrstarters.models.cardselection;

import com.gempukku.lotro.bots.rl.learning.LearningStep;
import com.gempukku.lotro.bots.rl.fotrstarters.FotrAbstractTrainer;

import java.util.List;

public class FallBackCardSelectionTrainer extends AbstractCardSelectionTrainer {

    // List of all other card selection trainers to check against
    private static final List<FotrAbstractTrainer> OTHER_TRAINERS = List.of(
            new ArcheryWoundTrainer(),
            new AttachItemTrainer(),
            new DiscardFromHandTrainer(),
            new DiscardFromPlayTrainer(),
            new ExertTrainer(),
            new HealTrainer(),
            new PlayFromHandTrainer(),
            new ReconcileTrainer(),
            new SanctuaryTrainer(),
            new SkirmishOrderTrainer()
    );

    @Override
    protected String getTextTrigger() {
        return ""; // No specific text trigger, allow fallback to handle any
    }

    @Override
    public boolean isStepRelevant(LearningStep step) {
        if (!super.isStepRelevant(step))
            return false;

        // Return false if any other specialized trainer considers this relevant
        for (FotrAbstractTrainer trainer : OTHER_TRAINERS) {
            if (trainer.isStepRelevant(step)) {
                return false;
            }
        }

        // Otherwise fallback is relevant
        return true;
    }
}
