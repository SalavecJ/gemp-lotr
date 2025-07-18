package com.gempukku.lotro.bots.rl.v2.decisions;

import com.gempukku.lotro.bots.rl.v2.decisions.arbitrary.DegenerateArbitraryDecisionAnswerer;
import com.gempukku.lotro.bots.rl.v2.decisions.arbitrary.PlaySiteAnswerer;
import com.gempukku.lotro.bots.rl.v2.decisions.arbitrary.StartingFellowshipAnswerer;
import com.gempukku.lotro.bots.rl.v2.decisions.cardselection.*;
import com.gempukku.lotro.bots.rl.v2.decisions.choice.AnotherMoveAnswerer;
import com.gempukku.lotro.bots.rl.v2.decisions.choice.GoFirstAnswerer;
import com.gempukku.lotro.bots.rl.v2.decisions.choice.MulliganAnswerer;
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
            FpSelfWoundAnswerer.class,
            ShadowSelfWoundAnswerer.class
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
