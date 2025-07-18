package com.gempukku.lotro.bots.rl.v2.learning.cardselection.rules;

import com.gempukku.lotro.bots.rl.v2.learning.cardselection.AbstractCardSelectionTrainer;

public class ReconcileDiscardOneTrainer extends AbstractCardSelectionTrainer {
    @Override
    protected String getTextTrigger() {
        return "Reconcile - choose card to discard or press DONE";
    }
}
