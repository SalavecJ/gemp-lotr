package com.gempukku.lotro.bots.rl.v2.learning.cardselection.rules;

import com.gempukku.lotro.bots.rl.v2.learning.cardselection.AbstractCardSelectionTrainer;

public class FpArcherySelfWoundTrainer extends AbstractCardSelectionTrainer {
    @Override
    protected String getTextTrigger() {
        return "Choose character to assign archery wound to - remaining wounds:";
    }
}
