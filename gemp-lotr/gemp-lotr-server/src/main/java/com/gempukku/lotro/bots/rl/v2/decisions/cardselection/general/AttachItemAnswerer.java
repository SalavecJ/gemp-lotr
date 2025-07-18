package com.gempukku.lotro.bots.rl.v2.decisions.cardselection.general;

import com.gempukku.lotro.bots.rl.v2.decisions.cardselection.AbstractCardSelectionAnswerer;

public class AttachItemAnswerer extends AbstractCardSelectionAnswerer {
    @Override
    protected String getTextTrigger() {
        return "Choose target to attach to";
    }
}
