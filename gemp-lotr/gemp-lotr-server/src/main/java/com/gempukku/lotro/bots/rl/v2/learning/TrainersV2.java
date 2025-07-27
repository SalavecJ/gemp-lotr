package com.gempukku.lotro.bots.rl.v2.learning;

import com.gempukku.lotro.bots.rl.v2.ModelRegistryV2;
import com.gempukku.lotro.bots.rl.v2.learning.arbitrary.DegenerateArbitraryTrainer;
import com.gempukku.lotro.bots.rl.v2.learning.arbitrary.general.GeneralArbitraryCardSelectionTrainerFactory;
import com.gempukku.lotro.bots.rl.v2.learning.arbitrary.rules.PlaySiteTrainer;
import com.gempukku.lotro.bots.rl.v2.learning.arbitrary.rules.StartingFellowshipTrainer;
import com.gempukku.lotro.bots.rl.v2.learning.assignment.FpAssignmentTrainer;
import com.gempukku.lotro.bots.rl.v2.learning.assignment.ShadowAssignmentTrainer;
import com.gempukku.lotro.bots.rl.v2.learning.cardaction.phase.*;
import com.gempukku.lotro.bots.rl.v2.learning.cardselection.DegenerateCardSelectionTrainer;
import com.gempukku.lotro.bots.rl.v2.learning.cardselection.general.AttachItemTrainer;
import com.gempukku.lotro.bots.rl.v2.learning.cardselection.rules.*;
import com.gempukku.lotro.bots.rl.v2.learning.choice.rules.AnotherMoveTrainer;
import com.gempukku.lotro.bots.rl.v2.learning.choice.rules.GoFirstTrainer;
import com.gempukku.lotro.bots.rl.v2.learning.choice.rules.MulliganTrainer;
import com.gempukku.lotro.bots.rl.v2.learning.integer.general.SpotMaxTrainer;
import com.gempukku.lotro.bots.rl.v2.learning.integer.rules.BurdenBidTrainer;
import org.apache.commons.collections4.list.UnmodifiableList;
import smile.classification.SoftClassifier;

import java.util.ArrayList;
import java.util.List;

public class TrainersV2 {
    private static final List<TrainerV2> degenerateTrainers = new ArrayList<>();
    private static final List<TrainerV2> generalTrainers = new ArrayList<>();
    private static final List<TrainerV2> trainers = new ArrayList<>();
    private static final List<TrainerV2> subTrainers = new ArrayList<>();

    static {
        trainers.add(new GoFirstTrainer());
        trainers.add(new AnotherMoveTrainer());
        trainers.add(new MulliganTrainer());
        trainers.add(new StartingFellowshipTrainer());
        trainers.add(new PlaySiteTrainer());
        trainers.add(new FpArcherySelfWoundTrainer());
        trainers.add(new FpThreatSelfWoundTrainer());
        trainers.add(new ReconcileDiscardDownTrainer());
        trainers.add(new ReconcileDiscardOneTrainer());
        trainers.add(new SanctuaryHealTrainer());
        trainers.add(new ShadowArcherySelfWoundTrainer());
        trainers.add(new SkirmishOrderTrainer());
        trainers.add(new SpotMaxTrainer());
        trainers.add(new BurdenBidTrainer());
        trainers.add(new FpAssignmentTrainer());
        trainers.add(new ShadowAssignmentTrainer());
        AbstractPhaseCardActionTrainer fellowship = new FellowshipCardActionTrainer();
        trainers.add(fellowship);
        subTrainers.addAll(fellowship.getSubTrainers());
        AbstractPhaseCardActionTrainer shadow = new ShadowCardActionTrainer();
        trainers.add(shadow);
        subTrainers.addAll(shadow.getSubTrainers());
        AbstractPhaseCardActionTrainer maneuver = new ManeuverCardActionTrainer();
        trainers.add(maneuver);
        subTrainers.addAll(maneuver.getSubTrainers());
        AbstractPhaseCardActionTrainer archery = new ArcheryCardActionTrainer();
        trainers.add(archery);
        subTrainers.addAll(archery.getSubTrainers());
        AbstractPhaseCardActionTrainer assignment = new AssignmentCardActionTrainer();
        trainers.add(assignment);
        subTrainers.addAll(assignment.getSubTrainers());
        AbstractPhaseCardActionTrainer skirmish = new SkirmishCardActionTrainer();
        trainers.add(skirmish);
        subTrainers.addAll(skirmish.getSubTrainers());
        AbstractPhaseCardActionTrainer regroup = new RegroupCardActionTrainer();
        trainers.add(regroup);
        subTrainers.addAll(regroup.getSubTrainers());
        AbstractPhaseCardActionTrainer response = new ResponseCardActionTrainer();
        trainers.add(response);
        subTrainers.addAll(response.getSubTrainers());

        generalTrainers.add(new AttachItemTrainer());
        generalTrainers.addAll(GeneralArbitraryCardSelectionTrainerFactory.generateGeneralArbitraryCardSelectionTrainers());

        degenerateTrainers.add(new DegenerateArbitraryTrainer());
        degenerateTrainers.add(new DegenerateCardSelectionTrainer());
    }

    private TrainersV2() {

    }

    public static void add(TrainerV2 trainer) {
        trainers.add(trainer);
    }

    public static List<TrainerV2> getAllV2Trainers() {
        return new UnmodifiableList<>(trainers);
    }

    public static List<TrainerV2> getAllV2GeneralTrainers() {
        return new UnmodifiableList<>(generalTrainers);
    }

    public static List<TrainerV2> getAllV2SubTrainers() {
        return new UnmodifiableList<>(subTrainers);
    }

    public static List<TrainerV2> getAllV2DegenerateTrainers() {
        return new UnmodifiableList<>(degenerateTrainers);
    }

    public static void trainModels(ModelRegistryV2 modelRegistry) {
        List<TrainerV2> trainable = new ArrayList<>();
        trainable.addAll(trainers);
        trainable.addAll(generalTrainers);
        trainable.addAll(subTrainers);

        for (TrainerV2 trainer : trainable) {
            try {
                List<SavedVector> vectors = SavedVectorPersistence.load(trainer);
                // Trainers that do not train and only provide answers have 0 saved vectors
                if (!vectors.isEmpty()) {
                    try {
                        SoftClassifier<double[]> model = trainer.train(vectors);
                        modelRegistry.registerModel(trainer.getName(), model);
                    } catch (IllegalArgumentException e) {
                        // If only not enough data for model, let it be
                        if (!e.getMessage().toLowerCase().contains("Only one class".toLowerCase())) {
                            throw e;
                        }
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to make model for trainer: " + trainer.getName(), e);
            }
        }
    }
}
