package com.gempukku.lotro.bots.rl.v2.learning.cardselection.general;

import com.gempukku.lotro.bots.rl.v2.learning.cardselection.AbstractCardSelectionTrainer;

public class AttachItemTrainer extends AbstractCardSelectionTrainer {
    @Override
    protected String getTextTrigger() {
        return "Choose target to attach to";
    }
}
