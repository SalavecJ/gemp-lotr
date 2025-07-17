package com.gempukku.lotro.bots.rl.v2.decisions.cardselection;

public class SanctuaryHealAnswerer extends AbstractCardSelectionAnswerer {
    @Override
    protected String getTextTrigger() {
        return "sanctuary healing";
    }
}
