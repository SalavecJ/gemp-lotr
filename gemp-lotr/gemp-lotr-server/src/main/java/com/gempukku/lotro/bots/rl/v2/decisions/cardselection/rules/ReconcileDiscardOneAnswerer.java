package com.gempukku.lotro.bots.rl.v2.decisions.cardselection.rules;

import com.gempukku.lotro.bots.rl.v2.decisions.cardselection.AbstractCardSelectionAnswerer;

public class ReconcileDiscardOneAnswerer extends AbstractCardSelectionAnswerer {
    @Override
    protected String getTextTrigger() {
        return "Reconcile - choose card to discard or press DONE";
    }
}
