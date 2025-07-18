package com.gempukku.lotro.bots.rl.v2.decisions.cardselection.rules;

import com.gempukku.lotro.bots.rl.v2.decisions.cardselection.AbstractCardSelectionAnswerer;

public class SanctuaryHealAnswerer extends AbstractCardSelectionAnswerer {
    @Override
    protected String getTextTrigger() {
        return "sanctuary healing";
    }
}
