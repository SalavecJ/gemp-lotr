package com.gempukku.lotro.bots.rl.v2.decisions;

import com.gempukku.lotro.bots.rl.v2.decisions.arbitrary.AbstractArbitraryAnswerer;
import com.gempukku.lotro.bots.rl.v2.decisions.arbitrary.DegenerateArbitraryDecisionAnswerer;
import com.gempukku.lotro.bots.rl.v2.decisions.arbitrary.general.GeneralArbitraryAnswerers;
import com.gempukku.lotro.bots.rl.v2.decisions.arbitrary.rules.PlaySiteAnswerer;
import com.gempukku.lotro.bots.rl.v2.decisions.arbitrary.rules.StartingFellowshipAnswerer;
import com.gempukku.lotro.bots.rl.v2.decisions.cardselection.general.AttachItemAnswerer;
import com.gempukku.lotro.bots.rl.v2.decisions.cardselection.rules.*;
import com.gempukku.lotro.bots.rl.v2.decisions.choice.rules.AnotherMoveAnswerer;
import com.gempukku.lotro.bots.rl.v2.decisions.choice.rules.GoFirstAnswerer;
import com.gempukku.lotro.bots.rl.v2.decisions.choice.rules.MulliganAnswerer;
import com.gempukku.lotro.bots.rl.v2.decisions.integer.rules.BurdenBidAnswerer;
import com.gempukku.lotro.bots.rl.v2.decisions.integer.general.SpotMaxAnswerer;
import com.gempukku.lotro.bots.rl.v2.learning.arbitrary.AbstractArbitraryTrainer;
import org.apache.commons.collections4.list.UnmodifiableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AnswerersV2 {
    private static final List<Class<? extends DecisionAnswererV2>> answererClasses = List.of(
            BurdenBidAnswerer.class,
            AnotherMoveAnswerer.class,
            GoFirstAnswerer.class,
            MulliganAnswerer.class,
            DegenerateArbitraryDecisionAnswerer.class,
            StartingFellowshipAnswerer.class,
            PlaySiteAnswerer.class,
            ReconcileDiscardDownAnswerer.class,
            ReconcileDiscardOneAnswerer.class,
            SanctuaryHealAnswerer.class,
            SkirmishOrderAnswerer.class,
            FpArcherySelfWoundAnswerer.class,
            FpThreatSelfWoundAnswerer.class,
            ShadowArcherySelfWoundAnswerer.class
    );
    private static final List<Class<? extends DecisionAnswererV2>> generalAnswererClasses = List.of(
            AttachItemAnswerer.class,
            SpotMaxAnswerer.class
    );

    private static final List<DecisionAnswererV2> answerers;
    private static final List<DecisionAnswererV2> generalAnswerers;

    static {
        answerers = new ArrayList<>();
        for (Class<? extends DecisionAnswererV2> answererClass : answererClasses) {
            try {
                answerers.add(answererClass.getDeclaredConstructor().newInstance());
            } catch (Exception e) {
                throw new RuntimeException("Failed to instantiate decision answerer: " + answererClass.getName(), e);
            }
        }

        generalAnswerers = new ArrayList<>();
        for (Map.Entry<AbstractArbitraryTrainer, AbstractArbitraryAnswerer> entry : GeneralArbitraryAnswerers.generateGeneralArbitraryCardChoicePairs().entrySet()) {
            generalAnswerers.add(entry.getValue());
        }
        for (Class<? extends DecisionAnswererV2> answererClass : generalAnswererClasses) {
            try {
                generalAnswerers.add(answererClass.getDeclaredConstructor().newInstance());
            } catch (Exception e) {
                throw new RuntimeException("Failed to instantiate decision answerer: " + answererClass.getName(), e);
            }
        }
    }

    private AnswerersV2() {

    }

    public static List<DecisionAnswererV2> getAllV2Answerers() {
        return new UnmodifiableList<>(answerers);
    }

    public static List<DecisionAnswererV2> getAllV2GeneralAnswerers() {
        return new UnmodifiableList<>(generalAnswerers);
    }

    public static void addAnswerer(DecisionAnswererV2 decisionAnswerer) {
        answerers.add(decisionAnswerer);
    }
}
