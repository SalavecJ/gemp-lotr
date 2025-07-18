package com.gempukku.lotro.bots.rl.v2.decisions.cardselection.rules;

import com.gempukku.lotro.bots.rl.v2.decisions.cardselection.AbstractCardSelectionAnswerer;

public class ReconcileDiscardDownAnswerer extends AbstractCardSelectionAnswerer {
    @Override
    protected String getTextTrigger() {
        return "Choose cards to discard down to";
    }
}
