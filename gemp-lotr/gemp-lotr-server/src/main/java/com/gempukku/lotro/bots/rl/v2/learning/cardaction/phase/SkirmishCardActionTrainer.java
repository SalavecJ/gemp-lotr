package com.gempukku.lotro.bots.rl.v2.learning.cardaction.phase;

import com.gempukku.lotro.bots.rl.v2.learning.cardaction.AbstractCardActionTrainer;
import com.gempukku.lotro.bots.rl.v2.learning.cardaction.general.GeneralCardActionTrainerFactory;

import java.util.List;

public class SkirmishCardActionTrainer extends AbstractPhaseCardActionTrainer {
    private final List<AbstractCardActionTrainer> generalTrainers = GeneralCardActionTrainerFactory.getGeneralTrainers(getTextTrigger());

    @Override
    protected String getTextTrigger() {
        return "Choose action to play or Pass";
    }

    @Override
    public List<AbstractCardActionTrainer> getSubTrainers() {
        return generalTrainers;
    }
}
