package com.gempukku.lotro.bots.rl.v2.learning.choice.rules;

import com.gempukku.lotro.bots.rl.v2.learning.choice.AbstractChoiceTrainer;

public class AnotherMoveTrainer extends AbstractChoiceTrainer {
    @Override
    protected String getTextTrigger() {
        return "another move";
    }

    @Override
    protected int getNumberOfOptions() {
        return 2;
    }
}
