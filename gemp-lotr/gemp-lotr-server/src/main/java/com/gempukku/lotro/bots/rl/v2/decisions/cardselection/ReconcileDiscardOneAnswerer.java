package com.gempukku.lotro.bots.rl.v2.decisions.cardselection;

public class ReconcileDiscardOneAnswerer extends AbstractCardSelectionAnswerer {
    @Override
    protected String getTextTrigger() {
        return "Reconcile - choose card to discard or press DONE";
    }
}
