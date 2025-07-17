package com.gempukku.lotro.bots.rl.v2.decisions.cardselection;

public class ReconcileDiscardDownAnswerer extends AbstractCardSelectionAnswerer {
    @Override
    protected String getTextTrigger() {
        return "Choose cards to discard down to";
    }
}
