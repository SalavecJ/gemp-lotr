package com.gempukku.lotro.bots.rl.v2.learning.cardselection.rules;

import com.gempukku.lotro.bots.rl.v2.learning.cardselection.AbstractCardSelectionTrainer;

public class ReconcileDiscardDownTrainer extends AbstractCardSelectionTrainer {
    @Override
    protected String getTextTrigger() {
        return "Choose cards to discard down to";
    }
}
