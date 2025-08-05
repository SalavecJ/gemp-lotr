package com.gempukku.lotro.cards.evaluation;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.game.state.Skirmish;
import com.gempukku.lotro.logic.effects.ResolveSkirmishEffect;
import com.gempukku.lotro.logic.modifiers.CantBeOverwhelmedModifier;
import com.gempukku.lotro.logic.modifiers.ModifierFlag;
import com.gempukku.lotro.logic.timing.RuleUtils;

import java.util.HashMap;
import java.util.function.Predicate;

public class CardEvaluators {
    private static final HashMap<String, CardEvaluator> EVALUATORS = new HashMap<>();

    static {
        addSetOneCards();
    }

    private CardEvaluators() {

    }

    public static boolean doesAnythingIfPlayed(LotroGame game, int physicalId, String playerName, LotroCardBlueprint blueprint) {
        if (blueprint.getCardType() != CardType.EVENT) {
            return true;
        } else {
            if (EVALUATORS.containsKey(blueprint.getId())) {
                return EVALUATORS.get(blueprint.getId()).doesAnythingIfPlayed(game, physicalId, playerName);
            } else {
                throw new IllegalArgumentException("Unknown blueprint id: " + blueprint.getId());
            }
        }
    }

    private static void addSetOneCards() {
        addSetOneEvents();
    }

    private static void addSetOneEvents() {
        // Eregion's Trails - Maneuver: Exert a ranger to make each roaming minion strength -3 until the regroup phase.
        EVALUATORS.put("1_104", (game, physicalId, playerName) -> {
            for (PhysicalCard physicalCard : game.getGameState().getInPlay()) {
                if (game.getModifiersQuerying().hasKeyword(game, physicalCard, Keyword.ROAMING)) {
                    return true;
                }
            }
            return false;
        });

        // Gondor's Vengeance - Regroup: Exert a ranger companion to discard a minion.
        EVALUATORS.put("1_106", (game, physicalId, playerName) -> {
            if (!game.getModifiersQuerying().hasFlagActive(game, ModifierFlag.CANT_MOVE)
                    && game.getGameState().getMoveCount() < RuleUtils.calculateMoveLimit(game)
                    && game.getGameState().getInPlay().stream().anyMatch((Predicate<PhysicalCard>) physicalCard -> physicalCard.getBlueprint().getCardType() == CardType.MINION)) {
                return true;
            }
            if (game.getGameState().getInPlay().stream().anyMatch((Predicate<PhysicalCard>) physicalCard -> physicalCard.getBlueprint().getCardType() == CardType.MINION && physicalCard.getBlueprint().hasTimeword(Timeword.REGROUP))) {
                return true;
            }

            return false;
        });

        // Pathfinder - Fellowship or Regroup: Spot a ranger to play the fellowship's next site (replacing opponent's site if necessary).
        EVALUATORS.put("1_110", (game, physicalId, playerName) -> {
            String opponent = game.getGameState().getPlayerNames().stream()
                    .filter(s -> !s.equals(playerName)).findFirst()
                    .orElseThrow(() -> new IllegalStateException("Unknown second player"));
            boolean opponentBehind = game.getGameState().getPlayerPosition(playerName) > game.getGameState().getPlayerPosition(opponent);

            boolean canMoveAgain = !game.getModifiersQuerying().hasFlagActive(game, ModifierFlag.CANT_MOVE)
                    && game.getGameState().getMoveCount() < RuleUtils.calculateMoveLimit(game);

            if (game.getGameState().getCurrentPhase() == Phase.FELLOWSHIP || canMoveAgain || opponentBehind) {
                int currentSite = game.getGameState().getCurrentSiteNumber();
                PhysicalCard nextSite = game.getGameState().getSite(currentSite + 1);
                if (nextSite == null || !nextSite.getOwner().equals(playerName)) {
                    return true;
                }
            }

            return false;
        });

        // Swordarm of the White Tower - Skirmish: Make a [gondor] companion strength +2 (or +4 if he or she is defender +1).
        EVALUATORS.put("1_116", (game, physicalId, playerName) -> {
            Skirmish skirmish = game.getGameState().getSkirmish();
            if (skirmish == null)
                return false;

            final PhysicalCard fellowshipCharacter = skirmish.getFellowshipCharacter();
            if (fellowshipCharacter == null)
                return false;

            if (!fellowshipCharacter.getBlueprint().getCulture().equals(Culture.GONDOR)) {
                return false;
            }
            int bonus = game.getModifiersQuerying().hasKeyword(game, fellowshipCharacter, Keyword.DEFENDER) ? 4 : 2;

            return simpleSkirmishPumpDoesAnything(game, playerName, bonus, 0);
        });

        // Swordsman of the Northern Kingdom - Skirmish: Make a ranger strength +2 (or +4 when skirmishing a roaming minion).
        EVALUATORS.put("1_117", (game, physicalId, playerName) -> {
            Skirmish skirmish = game.getGameState().getSkirmish();
            if (skirmish == null)
                return false;

            final PhysicalCard fellowshipCharacter = skirmish.getFellowshipCharacter();
            if (fellowshipCharacter == null)
                return false;

            if (!fellowshipCharacter.getBlueprint().hasKeyword(Keyword.RANGER)) {
                return false;
            }

            boolean anyRoaming = false;
            for (PhysicalCard physicalCard : skirmish.getShadowCharacters()) {
                if (game.getModifiersQuerying().hasKeyword(game, physicalCard, Keyword.ROAMING)) {
                    anyRoaming = true;
                }
            }
            int bonus = anyRoaming ? 4 : 2;

            return simpleSkirmishPumpDoesAnything(game, playerName, bonus, 0);
        });

        // Hobbit Intuition - Stealth. Skirmish: At sites 1 to 4, cancel a skirmish involving a Hobbit. At any other site, make a Hobbit strength +3.
        EVALUATORS.put("1_296", (game, physicalId, playerName) -> {
            Skirmish skirmish = game.getGameState().getSkirmish();
            if (skirmish == null)
                return false;

            final PhysicalCard fellowshipCharacter = skirmish.getFellowshipCharacter();
            if (fellowshipCharacter == null)
                return false;

            if (!fellowshipCharacter.getBlueprint().getRace().equals(Race.HOBBIT)) {
                return false;
            }

            int bonus = 3;
            boolean canCancel = game.getGameState().getCurrentSite().getBlueprint().getSiteBlock().equals(SitesBlock.FELLOWSHIP)
                    && game.getGameState().getCurrentSiteNumber() <= 4
                    && game.getModifiersQuerying().canCancelSkirmish(game, game.getGameState().getPhysicalCard(physicalId))
                    && (game.getFormat().canCancelRingBearerSkirmish() || game.getGameState().getRingBearer(playerName).getCardId() != fellowshipCharacter.getCardId());

            int fpStrength = RuleUtils.getFellowshipSkirmishStrength(game);
            int shadowStrength = RuleUtils.getShadowSkirmishStrength(game);
            int multiplier = game.getModifiersQuerying().getOverwhelmMultiplier(game, fellowshipCharacter);
            int shadowMult = 2;
            for (PhysicalCard minion : skirmish.getShadowCharacters()) {
                int mult = game.getModifiersQuerying().getOverwhelmMultiplier(game, minion);
                shadowMult = Math.max(shadowMult, mult);
            }

            ResolveSkirmishEffect.Result without = getPotentialResult(fpStrength, shadowStrength, multiplier, shadowMult);
            ResolveSkirmishEffect.Result with = getPotentialResult(fpStrength + bonus, shadowStrength, multiplier, shadowMult);

            return skirmishEventMadeDifference(game, playerName, without, with, skirmish) || (canCancel && (without.equals(ResolveSkirmishEffect.Result.FELLOWSHIP_LOSES) || without.equals(ResolveSkirmishEffect.Result.FELLOWSHIP_OVERWHELMED)));
        });

        // Bred for Battle - Skirmish: Exert an Uruk-hai to make it strength +3.
        EVALUATORS.put("1_121", (game, physicalId, playerName) -> {
            Skirmish skirmish = game.getGameState().getSkirmish();
            if (skirmish == null)
                return false;

            boolean anyMatch = false;
            for (PhysicalCard physicalCard : skirmish.getShadowCharacters()) {
                if (physicalCard.getBlueprint().getRace().equals(Race.URUK_HAI) && game.getModifiersQuerying().canBeExerted(game, game.getGameState().getPhysicalCard(physicalId), physicalCard)) {
                    anyMatch = true;
                }
            }
            if (!anyMatch)
                return false;

            int bonus = 3;

            return simpleSkirmishPumpDoesAnything(game, playerName, 0, bonus);
        });

        // Their Halls of Stone - Skirmish: Make a Dwarf strength +2 (or +4 if at an underground site).
        EVALUATORS.put("1_26", (game, physicalId, playerName) -> {
            Skirmish skirmish = game.getGameState().getSkirmish();
            if (skirmish == null)
                return false;

            final PhysicalCard fellowshipCharacter = skirmish.getFellowshipCharacter();
            if (fellowshipCharacter == null)
                return false;

            if (!fellowshipCharacter.getBlueprint().getRace().equals(Race.DWARF)) {
                return false;
            }
            int bonus = game.getModifiersQuerying().hasKeyword(game, game.getGameState().getCurrentSite(), Keyword.UNDERGROUND) ? 4 : 2;

            return simpleSkirmishPumpDoesAnything(game, playerName, bonus, 0);
        });

        // Defiance - Skirmish: Make an Elf strength +2 (or +4 if skirmishing a Nazgûl).
        EVALUATORS.put("1_37", (game, physicalId, playerName) -> {
            Skirmish skirmish = game.getGameState().getSkirmish();
            if (skirmish == null)
                return false;

            final PhysicalCard fellowshipCharacter = skirmish.getFellowshipCharacter();
            if (fellowshipCharacter == null)
                return false;

            if (!fellowshipCharacter.getBlueprint().getRace().equals(Race.ELF)) {
                return false;
            }

            boolean anyMatch = false;
            for (PhysicalCard physicalCard : skirmish.getShadowCharacters()) {
                if (physicalCard.getBlueprint().getRace().equals(Race.NAZGUL)) {
                    anyMatch = true;
                }
            }

            int bonus = anyMatch ? 4 : 2;

            return simpleSkirmishPumpDoesAnything(game, playerName, bonus, 0);
        });

        // Intimidate - Spell. Response: If a companion is about to take a wound, spot Gandalf to prevent that wound.
        EVALUATORS.put("1_76", (game, physicalId, playerName) -> {
            // Simplified, not counting for more damage at the same time
            return true;
        });

        // Mysterious Wizard - Spell. Skirmish: Make Gandalf strength +2 (or +4 if there are 4 or fewer burdens on the Ring-bearer).
        EVALUATORS.put("1_78", (game, physicalId, playerName) -> {
            Skirmish skirmish = game.getGameState().getSkirmish();
            if (skirmish == null)
                return false;

            final PhysicalCard fellowshipCharacter = skirmish.getFellowshipCharacter();
            if (fellowshipCharacter == null)
                return false;

            if (!fellowshipCharacter.getBlueprint().getTitle().equals("Gandalf")) {
                return false;
            }

            int bonus = game.getGameState().getBurdens() <= 4 ? 4 : 2;

            return simpleSkirmishPumpDoesAnything(game, playerName, bonus, 0);
        });

        //Sleep, Caradhras - Spell. Fellowship: Exert Gandalf to discard every condition.
        EVALUATORS.put("1_84", (game, physicalId, playerName) -> game.getGameState().getInPlay().stream().anyMatch((Predicate<PhysicalCard>) physicalCard -> {
            LotroCardBlueprint blueprint = physicalCard.getBlueprint();
            return !physicalCard.getOwner().equals(playerName) && Side.SHADOW.equals(blueprint.getSide()) && blueprint.getCardType().equals(CardType.CONDITION);
        }));

        // Treachery Deeper Than You Know - Spell Fellowship: Spot Gandalf to reveal an opponent's hand.
        EVALUATORS.put("1_86", (game, physicalId, playerName) -> {
            // Bots do not use it now (4th of August 2025), but lets assume having a look is good
            // Simplified, not taking into account double reveal
            return true;
        });

        // Noble Intentions - Skirmish: Exert a companion (except a Hobbit) to make a Hobbit strength +3.
        EVALUATORS.put("1_304", (game, physicalId, playerName) -> {
            Skirmish skirmish = game.getGameState().getSkirmish();
            if (skirmish == null)
                return false;

            final PhysicalCard fellowshipCharacter = skirmish.getFellowshipCharacter();
            if (fellowshipCharacter == null)
                return false;

            if (!fellowshipCharacter.getBlueprint().getRace().equals(Race.HOBBIT)) {
                return false;
            }

            int bonus = 3;

            return simpleSkirmishPumpDoesAnything(game, playerName, bonus, 0);
        });

        // Sorry About Everything - Fellowship: Exert a Hobbit companion to remove a burden.
        EVALUATORS.put("1_312", (game, physicalId, playerName) -> game.getGameState().getBurdens() > 0);

        // Drums in the Deep - Skirmish: Make a [moria] Orc strength +2 (or +4 if skirmishing a Dwarf).
        EVALUATORS.put("1_168", (game, physicalId, playerName) -> {
            Skirmish skirmish = game.getGameState().getSkirmish();
            if (skirmish == null)
                return false;

            boolean anyMatch = false;
            for (PhysicalCard physicalCard : skirmish.getShadowCharacters()) {
                if (physicalCard.getBlueprint().getRace().equals(Race.ORC) && physicalCard.getBlueprint().getCulture().equals(Culture.MORIA)) {
                    anyMatch = true;
                }
            }
            if (!anyMatch)
                return false;

            int bonus = skirmish.getFellowshipCharacter().getBlueprint().getRace().equals(Race.DWARF) ? 4 : 2;

            return simpleSkirmishPumpDoesAnything(game, playerName, 0, bonus);
        });

        // Host of Thousands - Shadow: Play a [moria] Orc from your discard pile.
        EVALUATORS.put("1_187", (game, physicalId, playerName) -> {
            // Playing from discard not playable without legal targets
            return true;
        });
    }

    private static boolean simpleSkirmishPumpDoesAnything(LotroGame game, String playerName, int fpStrengthBonus, int shadowStrengthBonus) {
        Skirmish skirmish = game.getGameState().getSkirmish();
        final PhysicalCard fellowshipCharacter = skirmish.getFellowshipCharacter();

        int fpStrength = RuleUtils.getFellowshipSkirmishStrength(game);
        int shadowStrength = RuleUtils.getShadowSkirmishStrength(game);
        int multiplier = game.getModifiersQuerying().getOverwhelmMultiplier(game, fellowshipCharacter);
        int shadowMult = 2;
        for (PhysicalCard minion : skirmish.getShadowCharacters()) {
            int mult = game.getModifiersQuerying().getOverwhelmMultiplier(game, minion);
            shadowMult = Math.max(shadowMult, mult);
        }

        ResolveSkirmishEffect.Result without = getPotentialResult(fpStrength, shadowStrength, multiplier, shadowMult);
        ResolveSkirmishEffect.Result with = getPotentialResult(fpStrength + fpStrengthBonus, shadowStrength + shadowStrengthBonus, multiplier, shadowMult);

        return skirmishEventMadeDifference(game, playerName, without, with, skirmish);
    }

    private static boolean skirmishEventMadeDifference(LotroGame game, String playerName, ResolveSkirmishEffect.Result without, ResolveSkirmishEffect.Result with, Skirmish skirmish) {
        boolean diesAnyway = (without == ResolveSkirmishEffect.Result.FELLOWSHIP_OVERWHELMED)
                && (with == ResolveSkirmishEffect.Result.FELLOWSHIP_LOSES)
                && (game.getModifiersQuerying().getVitality(game, skirmish.getFellowshipCharacter()) - RuleUtils.getShadowSkirmishDamageBonus(game) - 1 <= 0)
                && (game.getGameState().getRingBearer(playerName) != skirmish.getFellowshipCharacter())
                && (game.getModifiersQuerying().canTakeWoundsFromLosingSkirmish(game, skirmish.getFellowshipCharacter()));
        if (diesAnyway) {
            return false;
        }

        int minionMaxVitality = skirmish.getShadowCharacters().stream().mapToInt(value -> game.getModifiersQuerying().getVitality(game, value)).max().orElseThrow();
        boolean allCanTakeWounds = skirmish.getShadowCharacters().stream().allMatch(physicalCard -> game.getModifiersQuerying().canTakeWoundsFromLosingSkirmish(game, physicalCard));
        boolean minionsDieAnyway = (without == ResolveSkirmishEffect.Result.SHADOW_LOSES)
                && (with == ResolveSkirmishEffect.Result.SHADOW_OVERWHELMED)
                && (minionMaxVitality - RuleUtils.getFellowshipSkirmishDamageBonus(game) - 1 <= 0)
                && allCanTakeWounds;
        if (minionsDieAnyway) {
            return false;
        }

        return without != with;
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
}
