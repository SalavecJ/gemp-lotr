package com.gempukku.lotro.bots.rl.v2.decisions.cardselection.rules;

import com.gempukku.lotro.bots.rl.v2.decisions.cardselection.AbstractCardSelectionAnswerer;

public class ShadowArcherySelfWoundAnswerer extends AbstractCardSelectionAnswerer {

    @Override
    protected String getTextTrigger() {
        return "Choose minion to assign archery wound to - remaining wounds:";
    }
}
