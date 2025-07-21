package com.gempukku.lotro.bots.rl.v2.decisions;

import com.gempukku.lotro.bots.rl.v2.decisions.arbitrary.DegenerateArbitraryDecisionAnswerer;
import com.gempukku.lotro.bots.rl.v2.decisions.arbitrary.rules.PlaySiteAnswerer;
import com.gempukku.lotro.bots.rl.v2.decisions.arbitrary.rules.StartingFellowshipAnswerer;
import com.gempukku.lotro.bots.rl.v2.decisions.cardselection.general.AttachItemAnswerer;
import com.gempukku.lotro.bots.rl.v2.decisions.cardselection.rules.*;
import com.gempukku.lotro.bots.rl.v2.decisions.choice.rules.AnotherMoveAnswerer;
import com.gempukku.lotro.bots.rl.v2.decisions.choice.rules.GoFirstAnswerer;
import com.gempukku.lotro.bots.rl.v2.decisions.choice.rules.MulliganAnswerer;
import com.gempukku.lotro.bots.rl.v2.decisions.integer.BurdenBidAnswerer;
import com.gempukku.lotro.bots.rl.v2.decisions.integer.SpotMaxAnswerer;
import org.apache.commons.collections4.list.UnmodifiableList;

import java.util.ArrayList;
import java.util.List;

public class AnswerersV2 {
    private static final List<Class<? extends DecisionAnswererV2>> answererClasses = List.of(
            BurdenBidAnswerer.class,
            SpotMaxAnswerer.class,
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
            AttachItemAnswerer.class,
            FpArcherySelfWoundAnswerer.class,
            FpThreatSelfWoundAnswerer.class,
            ShadowArcherySelfWoundAnswerer.class
    );

    private static final List<DecisionAnswererV2> answerers;

    static {
        answerers = new ArrayList<>();
        for (Class<? extends DecisionAnswererV2> answererClass : answererClasses) {
            try {
                answerers.add(answererClass.getDeclaredConstructor().newInstance());
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

    public static void addAnswerer(DecisionAnswererV2 decisionAnswerer) {
        answerers.add(decisionAnswerer);
    }
}
