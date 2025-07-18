package com.gempukku.lotro.bots.rl.v2.learning.cardselection.rules;

import com.gempukku.lotro.bots.rl.v2.learning.cardselection.AbstractCardSelectionTrainer;

public class SanctuaryHealTrainer extends AbstractCardSelectionTrainer {
    @Override
    protected String getTextTrigger() {
        return "sanctuary healing";
    }
}
