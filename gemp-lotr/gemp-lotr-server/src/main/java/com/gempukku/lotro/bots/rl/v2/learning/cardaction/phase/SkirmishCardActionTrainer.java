package com.gempukku.lotro.bots.rl.v2.learning.cardaction.phase;

import com.gempukku.lotro.bots.rl.v2.learning.cardaction.AbstractCardActionTrainer;
import com.gempukku.lotro.bots.rl.v2.learning.cardaction.general.GeneralCardActionTrainerFactory;

import java.util.ArrayList;
import java.util.List;

public class SkirmishCardActionTrainer extends AbstractPhaseCardActionTrainer {
    private final List<AbstractCardActionTrainer> generalTrainers = GeneralCardActionTrainerFactory.getGeneralTrainers(getTextTrigger());
    private final List<AbstractCardActionTrainer> specificTrainers = new ArrayList<>();

    @Override
    protected String getTextTrigger() {
        return "Choose action to play or Pass";
    }

    @Override
    public List<AbstractCardActionTrainer> getSubTrainers() {
        List<AbstractCardActionTrainer> tbr = new ArrayList<>();
        tbr.addAll(generalTrainers);
        tbr.addAll(specificTrainers);
        return tbr;
    }

    @Override
    public List<AbstractCardActionTrainer> getGeneralSubTrainers() {
        return generalTrainers;
    }

    @Override
    public List<AbstractCardActionTrainer> getSpecificSubTrainers() {
        return specificTrainers;
    }

    @Override
    protected void addSpecificSubTrainer(AbstractCardActionTrainer trainer) {
        specificTrainers.add(trainer);
    }
}
