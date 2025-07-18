package com.gempukku.lotro.bots.rl.v2.decisions.cardselection;

public class ShadowSelfWoundAnswerer extends AbstractCardSelectionAnswerer {

    @Override
    protected String getTextTrigger() {
        return "Choose minion to assign archery wound to - remaining wounds:"; // Not used
    }
}
