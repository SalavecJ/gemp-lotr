package com.gempukku.lotro.bots.forge.utils;

import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.effects.ResolveSkirmishEffect;
import com.gempukku.lotro.logic.modifiers.CantBeOverwhelmedModifier;

import java.util.Set;

public class CombatUtil {

    public static ResolveSkirmishEffect.Result getPotentialResultIfNothingUsed(LotroGame game, PhysicalCard fp, Set<PhysicalCard> minions) {
        int fpStrength = game.getModifiersQuerying().getStrength(game, fp);

        int shadowStrength = 0;
        for (PhysicalCard minion : minions) {
            shadowStrength += game.getModifiersQuerying().getStrength(game, minion);
        }

        int multiplier = game.getModifiersQuerying().getOverwhelmMultiplier(game, fp);
        int shadowMult = 2;
        for (PhysicalCard minion : minions) {
            int mult = game.getModifiersQuerying().getOverwhelmMultiplier(game, minion);
            shadowMult = Math.max(shadowMult, mult);
        }

        return getPotentialResult(fpStrength, shadowStrength, multiplier, shadowMult);
    }

    private static ResolveSkirmishEffect.Result getPotentialResult(int fpStrength, int shadowStrength, int multiplier, int shadowMult) {
        if (fpStrength == 0 && shadowStrength == 0) {
            return ResolveSkirmishEffect.Result.FELLOWSHIP_LOSES;
        } else if (multiplier < CantBeOverwhelmedModifier.ImmuneToOverwhelmThreshold
                && fpStrength * multiplier <= shadowStrength) {
            return ResolveSkirmishEffect.Result.FELLOWSHIP_OVERWHELMED;
        } else if (fpStrength <= shadowStrength) {
            return ResolveSkirmishEffect.Result.FELLOWSHIP_LOSES;
        } else if (fpStrength >= shadowMult * shadowStrength) {
            return ResolveSkirmishEffect.Result.SHADOW_OVERWHELMED;
        } else {
            return ResolveSkirmishEffect.Result.SHADOW_LOSES;
        }
    }

    public static int getPotentialDamageTakenIfFellowshipLoses(LotroGame game, PhysicalCard fp, Set<PhysicalCard> minions) {
        int dmg = 1;

        for (PhysicalCard physicalCard : minions)
            dmg += game.getModifiersQuerying().getKeywordCount(game, physicalCard, Keyword.DAMAGE);

        // Try how many wounds can fp character take
        for (int i = dmg; i > 0; i--) {
            if (game.getModifiersQuerying().canTakeWounds(game, minions, fp, i)) {
                // If vitality is lower than max damage, less damage vitality is lost
                return Math.min(i, game.getModifiersQuerying().getVitality(game, fp));
            }
        }
        return 0;
    }

    public static int getPotentialDamageTakenIfShadowLoses(LotroGame game, PhysicalCard fp) {
        return 1 + game.getModifiersQuerying().getKeywordCount(game, fp, Keyword.DAMAGE);
    }

}
