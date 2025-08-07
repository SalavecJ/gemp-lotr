package com.gempukku.lotro.cards.evaluation;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filter;
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

    public static boolean doesAnythingIfUsed(LotroGame game, int physicalId, String playerName, LotroCardBlueprint blueprint) {
        if (EVALUATORS.containsKey(blueprint.getId())) {
            return EVALUATORS.get(blueprint.getId()).doesAnythingIfUsed(game, physicalId, playerName);
        } else {
            throw new IllegalArgumentException("Unknown blueprint id: " + blueprint.getId());
        }
    }

    private static void addSetOneCards() {
        addSetOneSites();
        addSetOneRings();
        addSetOneEvents();
        addSetOneAllies();
        addSetOneCompanions();
        addSetOneMinions();
        addSetOneConditions();
        addSetOnePossessions();
    }

    private static void addSetOnePossessions() {
        // Goblin Scimitar - Bearer must be a [moria] Orc.<br>When you play this possession, you may draw a card.
        EVALUATORS.put("1_180", new AbstractCardEvaluator() {
            @Override
            public boolean doesAnythingIfUsed(LotroGame game, int physicalId, String playerName) {
                return true;
            }
        });

        // Athelas - Bearer must be a [gondor] Man.<br><b>Fellowship:</b> Discard this possession to heal a companion or to remove a Shadow condition from a companion.
        EVALUATORS.put("1_94", new AbstractCardEvaluator() {
            @Override
            public boolean doesAnythingIfUsed(LotroGame game, int physicalId, String playerName) {
                return game.getGameState().getInPlay().stream().anyMatch((Predicate<PhysicalCard>) physicalCard -> {
                    if (!physicalCard.getBlueprint().getCardType().equals(CardType.COMPANION) || !physicalCard.getOwner().equals(playerName))
                        return false;

                    boolean shadowConditionAttached = game.getGameState().getAttachedCards(physicalCard).stream().anyMatch(attachedCard -> Side.SHADOW.equals(attachedCard.getBlueprint().getSide()) && attachedCard.getBlueprint().getCardType().equals(CardType.CONDITION));

                    return (game.getGameState().getWounds(physicalCard) > 0
                            && game.getModifiersQuerying().canBeHealed(game, physicalCard))
                            || shadowConditionAttached;
                });
            }
        });
    }

    private static void addSetOneConditions() {
        // Saruman's Ambition - Plays to your support area.<br>The twilight cost of your [isengard] events is -1.<br><b>Skirmish:</b> Discard this condition to make an Uruk-hai strength +2.
        EVALUATORS.put("1_133", new AbstractCardEvaluator() {
            @Override
            public boolean doesAnythingIfUsed(LotroGame game, int physicalId, String playerName) {
                Skirmish skirmish = game.getGameState().getSkirmish();
                if (skirmish == null)
                    return false;

                boolean anyMatch = false;
                for (PhysicalCard physicalCard : skirmish.getShadowCharacters()) {
                    if (physicalCard.getBlueprint().getRace().equals(Race.URUK_HAI)) {
                        anyMatch = true;
                    }
                }
                if (!anyMatch)
                    return false;

                int bonus = 2;

                return simpleSkirmishPumpDoesAnything(game, playerName, 0, bonus);
            }
        });

        // They Are Coming - Plays to your support area.<br><b>Shadow:</b> Discard 3 cards from hand to play a [moria] Orc from your discard pile.
        EVALUATORS.put("1_196", new AbstractCardEvaluator() {
            @Override
            public boolean doesAnythingIfUsed(LotroGame game, int physicalId, String playerName) {
                return !game.getModifiersQuerying().hasFlagActive(game, ModifierFlag.CANT_PLAY_FROM_DISCARD_OR_DECK);
            }
        });
    }

    private static void addSetOneMinions() {
        // Uruk Brood - <b>Damage +1</b>.<br><b>Skirmish:</b> Remove (2) to make this minion strength +1 for each other Uruk-hai you spot.
        EVALUATORS.put("1_145", new AbstractCardEvaluator() {
            @Override
            public boolean doesAnythingIfUsed(LotroGame game, int physicalId, String playerName) {
                Skirmish skirmish = game.getGameState().getSkirmish();
                if (skirmish == null)
                    return false;

                boolean skirmishing = skirmish.getShadowCharacters().stream().anyMatch(physicalCard -> physicalCard.getCardId() == physicalId);
                if (!skirmishing)
                    return false;

                int possibleUses = game.getGameState().getTwilightPool() / 2;
                int uruksToSpot = Math.toIntExact(game.getGameState().getInPlay().stream().filter((Predicate<PhysicalCard>) physicalCard -> Race.URUK_HAI.equals(physicalCard.getBlueprint().getRace()) && physicalCard.getCardId() != physicalId).count());
                int bonus = possibleUses * uruksToSpot;

                return simpleSkirmishPumpDoesAnything(game, playerName, 0, bonus);
            }
        });

        // Uruk Slayer - <b>Damage +1</b>.<br><b>Skirmish:</b> Remove (1) to make this minion strength +1 (limit +3).
        EVALUATORS.put("1_153", new AbstractCardEvaluator() {
            @Override
            public boolean doesAnythingIfUsed(LotroGame game, int physicalId, String playerName) {
                Skirmish skirmish = game.getGameState().getSkirmish();
                if (skirmish == null)
                    return false;

                boolean skirmishing = skirmish.getShadowCharacters().stream().anyMatch(physicalCard -> physicalCard.getCardId() == physicalId);
                if (!skirmishing)
                    return false;

                int possibleUses = Math.max(3, game.getGameState().getTwilightPool());

                return simpleSkirmishPumpDoesAnything(game, playerName, 0, possibleUses);
            }
        });

        // Uruk Shaman - <b>Damage +1</b>.<br><b>Maneuver:</b> Remove (2) to heal an Uruk-hai.
        EVALUATORS.put("1_152", new AbstractCardEvaluator() {
            @Override
            public boolean doesAnythingIfUsed(LotroGame game, int physicalId, String playerName) {
                return game.getGameState().getInPlay().stream().anyMatch((Predicate<PhysicalCard>) physicalCard -> game.getGameState().getWounds(physicalCard) > 0 && Race.URUK_HAI.equals(physicalCard.getBlueprint().getRace()));
            }
        });

        // Goblin Sneak - When you play this minion, you may place a [moria] Orc from your discard pile beneath your draw deck.
        EVALUATORS.put("1_181", new AbstractCardEvaluator() {
            @Override
            public boolean doesAnythingIfUsed(LotroGame game, int physicalId, String playerName) {
                return game.getGameState().getDiscard(playerName).stream().anyMatch((Predicate<PhysicalCard>) physicalCard -> Culture.MORIA.equals(physicalCard.getBlueprint().getCulture()) && Race.ORC.equals(physicalCard.getBlueprint().getRace()));
            }
        });

        // Uruk Soldier - <b>Damage +1</b>.<br>When you play this minion, you may make the Free Peoples player discard the top card of his draw deck.
        EVALUATORS.put("1_154", new AbstractCardEvaluator() {
            @Override
            public boolean doesAnythingIfUsed(LotroGame game, int physicalId, String playerName) {
                return true;
            }
        });

        // Goblin Scavengers - When you play this minion, you may play a weapon from your discard pile on your [moria] Orc.
        EVALUATORS.put("1_179", new AbstractCardEvaluator() {
            @Override
            public boolean doesAnythingIfUsed(LotroGame game, int physicalId, String playerName) {
                return game.getGameState().getDiscard(playerName).stream().anyMatch((Predicate<PhysicalCard>) physicalCard -> {
                    boolean isWeapon = physicalCard.getBlueprint().getPossessionClasses() != null
                            && (physicalCard.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON)
                            || physicalCard.getBlueprint().getPossessionClasses().contains(PossessionClass.RANGED_WEAPON));
                    if (!isWeapon)
                        return false;

                    if (game.getGameState().getTwilightPool() < 1 + physicalCard.getBlueprint().getTwilightCost())
                        return false;

                    Filter attachFilter = RuleUtils.getFullValidTargetFilter(playerName, game, physicalCard);
                    return game.getGameState().getInPlay().stream().anyMatch((Predicate<PhysicalCard>) physicalCard1 -> attachFilter.accepts(game, physicalCard1)
                            && Race.ORC.equals(physicalCard1.getBlueprint().getRace()) && Culture.MORIA.equals(physicalCard1.getBlueprint().getCulture()));
                });
            }
        });
    }

    private static void addSetOneCompanions() {
        // Aragorn, King in Exile - <b>Ranger</b>.<br>At the start of each of your turns, you may heal another companion who has the Aragorn signet.
        EVALUATORS.put("1_365", new AbstractCardEvaluator() {
            @Override
            public boolean doesAnythingIfUsed(LotroGame game, int physicalId, String playerName) {
                return game.getGameState().getInPlay().stream().anyMatch((Predicate<PhysicalCard>) physicalCard -> physicalCard.getOwner().equals(playerName)
                        && physicalCard.getBlueprint().getCardType().equals(CardType.COMPANION)
                        && Signet.ARAGORN.equals(physicalCard.getBlueprint().getSignet())
                        && game.getModifiersQuerying().canBeHealed(game, physicalCard));
            }
        });

        // Frodo, Son of Drogo - <b>Ring-bearer (resistance 10).</b><br><b>Fellowship:</b> Exert another companion who has the Frodo signet to heal Frodo.
        EVALUATORS.put("1_290", new AbstractCardEvaluator() {
            @Override
            public boolean doesAnythingIfUsed(LotroGame game, int physicalId, String playerName) {
                PhysicalCard thisCard = game.getGameState().getPhysicalCard(physicalId);
                return game.getGameState().getWounds(thisCard) > 0
                        && game.getModifiersQuerying().canBeHealed(game, thisCard);
            }
        });

        // Boromir, Son of Denethor - <b>Skirmish:</b> Exert Boromir to make a Hobbit strength +3.
        EVALUATORS.put("1_97", new AbstractCardEvaluator() {
            @Override
            public boolean doesAnythingIfUsed(LotroGame game, int physicalId, String playerName) {
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
            }
        });

        // Gimli, Dwarf of Erebor - <b>Damage +1</b>.<br><b>Fellowship:</b> If the twilight pool has fewer than 2 twilight tokens, add (2) to place a card from hand beneath your draw deck.
        EVALUATORS.put("1_12", new AbstractCardEvaluator() {
            @Override
            public boolean doesAnythingIfUsed(LotroGame game, int physicalId, String playerName) {
                return !game.getGameState().getHand(playerName).isEmpty();
            }
        });

        // Sam, Son of Hamfast - <b>Fellowship:</b> Exert Sam to remove a burden.<br><b>Response:</b> If Frodo dies, make Sam the <b>Ring-bearer (resistance 5)</b>.
        EVALUATORS.put("1_311", new AbstractCardEvaluator() {
            @Override
            public boolean doesAnythingIfUsed(LotroGame game, int physicalId, String playerName) {
                if (game.getGameState().getCurrentPhase().equals(Phase.FELLOWSHIP)) {
                    return game.getGameState().getPlayerBurdens(playerName) > 0;
                }
                // Frodo dies
                return true;
            }
        });

        // Gandalf, The Grey Wizard - <b>Fellowship:</b> Exert Gandalf to play a companion who has the Gandalf signet. The twilight cost of that companion is -2.
        EVALUATORS.put("1_364", new AbstractCardEvaluator() {
            @Override
            public boolean doesAnythingIfUsed(LotroGame game, int physicalId, String playerName) {
                return game.getGameState().getHand(playerName).stream().anyMatch((Predicate<PhysicalCard>) physicalCard -> physicalCard.getBlueprint().getCardType().equals(CardType.COMPANION) && Signet.GANDALF.equals(physicalCard.getBlueprint().getSignet()));
            }
        });
    }

    private static void addSetOneSites() {
        // The Bridge of Khazad-dûm - <b>Underground</b>. <b>Shadow:</b> Play The Balrog from your draw deck or hand; The Balrog's twilight cost is -6.
        EVALUATORS.put("1_349", new AbstractCardEvaluator() {
            @Override
            public boolean doesAnythingIfUsed(LotroGame game, int physicalId, String playerName) {
                int maxCost = game.getGameState().getTwilightPool() + 6;
                PhysicalCard balrogInHand = game.getGameState().getHand(playerName).stream().filter((Predicate<PhysicalCard>) physicalCard -> physicalCard.getBlueprint().getTitle().equals("The Balrog")).findFirst().orElse(null);
                PhysicalCard balrogInDeck = game.getGameState().getDeck(playerName).stream().filter((Predicate<PhysicalCard>) physicalCard -> physicalCard.getBlueprint().getTitle().equals("The Balrog")).findFirst().orElse(null);
                boolean balrogInPlay = game.getGameState().getInPlay().stream().anyMatch((Predicate<PhysicalCard>) physicalCard -> physicalCard.getBlueprint().getTitle().equals("The Balrog"));

                return !balrogInPlay
                        && ((balrogInHand != null && balrogInHand.getBlueprint().getTwilightCost() <= maxCost)
                        || (balrogInDeck != null && balrogInDeck.getBlueprint().getTwilightCost() <= maxCost));
            }
        });

        // Ettenmoors - <b>Plains</b>. <b>Skirmish:</b> Exert your companion or minion to make that character strength +2.
        EVALUATORS.put("1_331", new AbstractCardEvaluator() {
            @Override
            public boolean doesAnythingIfUsed(LotroGame game, int physicalId, String playerName) {
                PhysicalCard ettenmoors = game.getGameState().getPhysicalCard(physicalId);
                int bonus = 2;

                Skirmish skirmish = game.getGameState().getSkirmish();
                if (skirmish == null)
                    return false;

                final PhysicalCard fellowshipCharacter = skirmish.getFellowshipCharacter();
                if (fellowshipCharacter == null || skirmish.getShadowCharacters() == null || skirmish.getShadowCharacters().isEmpty())
                    return false;

                boolean usefulForFp = game.getModifiersQuerying().canBeExerted(game, ettenmoors, fellowshipCharacter) && simpleSkirmishPumpDoesAnything(game, playerName, bonus, 0);
                boolean usefulForShadow = skirmish.getShadowCharacters().stream().anyMatch(physicalCard -> game.getModifiersQuerying().canBeExerted(game, ettenmoors, physicalCard)) && simpleSkirmishPumpDoesAnything(game, playerName, 0, bonus);

                return (usefulForFp && game.getGameState().getCurrentPlayerId().equals(playerName))
                        || (usefulForShadow && !game.getGameState().getCurrentPlayerId().equals(playerName));
            }
        });

        // Westfarthing - <b>Fellowship:</b> Exert a Hobbit to play a companion or ally; that character's twilight cost is -1.
        EVALUATORS.put("1_326", new AbstractCardEvaluator() {
            @Override
            public boolean doesAnythingIfUsed(LotroGame game, int physicalId, String playerName) {
                return true;
            }
        });

        // Rivendell Terrace - <b>Sanctuary</b>. <b>Fellowship:</b> Play a Man to draw a card.
        EVALUATORS.put("1_340", new AbstractCardEvaluator() {
            @Override
            public boolean doesAnythingIfUsed(LotroGame game, int physicalId, String playerName) {
                return game.getModifiersQuerying().canDrawCardAndIncrementForRuleOfFour(game, playerName);
            }
        });

        // Mithril Mine - <b>Underground</b>. <b>Shadow:</b> Remove (1) to play a Shadow weapon from your discard pile.
        EVALUATORS.put("1_345", new AbstractCardEvaluator() {
            @Override
            public boolean doesAnythingIfUsed(LotroGame game, int physicalId, String playerName) {
                return game.getGameState().getDiscard(playerName).stream().anyMatch((Predicate<PhysicalCard>) physicalCard -> {
                    boolean isWeapon = physicalCard.getBlueprint().getPossessionClasses() != null
                            && (physicalCard.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON)
                            || physicalCard.getBlueprint().getPossessionClasses().contains(PossessionClass.RANGED_WEAPON));
                    if (!isWeapon)
                        return false;

                    if (game.getGameState().getTwilightPool() < 1 + physicalCard.getBlueprint().getTwilightCost())
                        return false;

                    Filter attachFilter = RuleUtils.getFullValidTargetFilter(playerName, game, physicalCard);
                    return game.getGameState().getInPlay().stream().anyMatch((Predicate<PhysicalCard>) physicalCard1 -> attachFilter.accepts(game, physicalCard1));
                });
            }
        });

        // Galadriel's Glade - <b>Sanctuary</b>. <b>Fellowship:</b> Exert an Elf to look at an opponent's hand.
        EVALUATORS.put("1_351", new AbstractCardEvaluator() {
            @Override
            public boolean doesAnythingIfUsed(LotroGame game, int physicalId, String playerName) {
                String oppName = game.getGameState().getPlayerNames().stream().filter(s -> !s.equals(playerName)).findFirst().orElseThrow();
                return game.getModifiersQuerying().canLookOrRevealCardsInHand(game, oppName, playerName);
            }
        });

        // Pillars of the Kings - <b>River</b>. <b>Fellowship:</b> Discard a [gondor] card from hand to heal a [gondor] companion.
        EVALUATORS.put("1_358", new AbstractCardEvaluator() {
            @Override
            public boolean doesAnythingIfUsed(LotroGame game, int physicalId, String playerName) {
                return game.getGameState().getInPlay().stream().anyMatch((Predicate<PhysicalCard>) physicalCard -> physicalCard.getOwner().equals(playerName)
                        && physicalCard.getBlueprint().getCardType().equals(CardType.COMPANION)
                        && physicalCard.getBlueprint().getCulture().equals(Culture.GONDOR)
                        && game.getModifiersQuerying().canBeHealed(game, physicalCard));
            }
        });

        // Emyn Muil - <b>Maneuver:</b> Exert your minion to make that minion <b>fierce</b> until the regroup phase.
        EVALUATORS.put("1_360", new AbstractCardEvaluator() {
            @Override
            public boolean doesAnythingIfUsed(LotroGame game, int physicalId, String playerName) {
                return game.getGameState().getInPlay().stream().anyMatch((Predicate<PhysicalCard>) physicalCard -> physicalCard.getBlueprint().getCardType().equals(CardType.MINION)
                        && game.getModifiersQuerying().canBeExerted(game, game.getGameState().getPhysicalCard(physicalId), physicalCard)
                        && game.getModifiersQuerying().getVitality(game, physicalCard) > 1
                        && !game.getModifiersQuerying().hasKeyword(game, physicalCard, Keyword.FIERCE));
            }
        });

        // Shores of Nen Hithoel - <b>River</b>. <b>Shadow:</b> Spot 5 Orc minions to prevent the fellowship from moving again this turn.
        EVALUATORS.put("1_359", new AbstractCardEvaluator() {
            @Override
            public boolean doesAnythingIfUsed(LotroGame game, int physicalId, String playerName) {
                return  !game.getModifiersQuerying().hasFlagActive(game, ModifierFlag.CANT_MOVE);
            }
        });
    }

    private static void addSetOneAllies() {
        // Rosie Cotton, Hobbiton Lass - Sam is strength +1.<br><b>Fellowship:</b> Exert Rosie Cotton to heal Sam.
        EVALUATORS.put("1_309", new AbstractCardEvaluator() {
            @Override
            public boolean doesAnythingIfUsed(LotroGame game, int physicalId, String playerName) {
                return game.getGameState().getInPlay().stream().anyMatch((Predicate<PhysicalCard>) physicalCard ->
                        physicalCard.getBlueprint().getTitle().equals("Sam")
                        && physicalCard.getOwner().equals(playerName)
                        && game.getGameState().getWounds(physicalCard) > 0
                        && game.getModifiersQuerying().canBeHealed(game, physicalCard));
            }
        });

        // Barliman Butterbur, Prancing Pony Proprietor - <b>Fellowship:</b> Exert Barliman Butterbur to take a [gandalf] event into hand from your discard pile.
        EVALUATORS.put("1_70", new AbstractCardEvaluator() {
            @Override
            public boolean doesAnythingIfUsed(LotroGame game, int physicalId, String playerName) {
                return game.getModifiersQuerying().canDrawCardAndIncrementForRuleOfFour(game, playerName)
                        && game.getGameState().getDiscard(playerName).stream().anyMatch((Predicate<PhysicalCard>) physicalCard -> Culture.GANDALF.equals(physicalCard.getBlueprint().getCulture()) && physicalCard.getBlueprint().getCardType().equals(CardType.EVENT));
            }
        });

        // Bounder - <b>Skirmish:</b> Exert this ally to prevent a Hobbit from being overwhelmed unless that Hobbit's strength is tripled.
        EVALUATORS.put("1_286", new AbstractCardEvaluator() {
            @Override
            public boolean doesAnythingIfUsed(LotroGame game, int physicalId, String playerName) {
                Skirmish skirmish = game.getGameState().getSkirmish();
                if (skirmish == null)
                    return false;

                final PhysicalCard fellowshipCharacter = skirmish.getFellowshipCharacter();
                if (fellowshipCharacter == null)
                    return false;

                if (!fellowshipCharacter.getBlueprint().getRace().equals(Race.HOBBIT)) {
                    return false;
                }

                int fpStrength = RuleUtils.getFellowshipSkirmishStrength(game);
                int shadowStrength = RuleUtils.getShadowSkirmishStrength(game);
                int multiplier = game.getModifiersQuerying().getOverwhelmMultiplier(game, fellowshipCharacter);
                int shadowMult = 2;
                for (PhysicalCard minion : skirmish.getShadowCharacters()) {
                    int mult = game.getModifiersQuerying().getOverwhelmMultiplier(game, minion);
                    shadowMult = Math.max(shadowMult, mult);
                }

                ResolveSkirmishEffect.Result without = getPotentialResult(fpStrength, shadowStrength, multiplier, shadowMult);
                ResolveSkirmishEffect.Result with = getPotentialResult(fpStrength, shadowStrength, Math.max(multiplier, 3), shadowMult);

                return skirmishActionMadeDifference(game, playerName, without, with, skirmish);
            }
        });
    }

    private static void addSetOneRings() {
        // The Ruling Ring - <b>Response:</b> If bearer is about to take a wound in a skirmish, he wears The One Ring until the regroup phase.<br>While wearing The One Ring, each time the Ring-bearer is about to take a wound during a skirmish, add a burden instead.
        EVALUATORS.put("1_2", new AbstractCardEvaluator() {
            @Override
            public boolean doesAnythingIfUsed(LotroGame game, int physicalId, String playerName) {
                return true;
            }
        });
    }

    private static void addSetOneEvents() {
        // Eregion's Trails - Maneuver: Exert a ranger to make each roaming minion strength -3 until the regroup phase.
        EVALUATORS.put("1_104", new AbstractCardEvaluator() {
            @Override
            public boolean doesAnythingIfPlayed(LotroGame game, int physicalId, String playerName) {
                for (PhysicalCard physicalCard : game.getGameState().getInPlay()) {
                    if (game.getModifiersQuerying().hasKeyword(game, physicalCard, Keyword.ROAMING)) {
                        return true;
                    }
                }
                return false;
            }
        });

        // Gondor's Vengeance - Regroup: Exert a ranger companion to discard a minion.
        EVALUATORS.put("1_106", new AbstractCardEvaluator() {
            @Override
            public boolean doesAnythingIfPlayed(LotroGame game, int physicalId, String playerName) {
                return !game.getModifiersQuerying().hasFlagActive(game, ModifierFlag.CANT_MOVE)
                        && game.getGameState().getMoveCount() < RuleUtils.calculateMoveLimit(game)
                        && game.getGameState().getInPlay().stream().anyMatch((Predicate<PhysicalCard>) physicalCard -> physicalCard.getBlueprint().getCardType() == CardType.MINION);
            }
        });

        // Pathfinder - Fellowship or Regroup: Spot a ranger to play the fellowship's next site (replacing opponent's site if necessary).
        EVALUATORS.put("1_110", new AbstractCardEvaluator() {
            @Override
            public boolean doesAnythingIfPlayed(LotroGame game, int physicalId, String playerName) {
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
            }
        });

        // Swordarm of the White Tower - Skirmish: Make a [gondor] companion strength +2 (or +4 if he or she is defender +1).
        EVALUATORS.put("1_116", new AbstractCardEvaluator() {
            @Override
            public boolean doesAnythingIfPlayed(LotroGame game, int physicalId, String playerName) {
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
            }
        });

        // Swordsman of the Northern Kingdom - Skirmish: Make a ranger strength +2 (or +4 when skirmishing a roaming minion).
        EVALUATORS.put("1_117", new AbstractCardEvaluator() {
            @Override
            public boolean doesAnythingIfPlayed(LotroGame game, int physicalId, String playerName) {
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
            }
        });

        // Hobbit Intuition - Stealth. Skirmish: At sites 1 to 4, cancel a skirmish involving a Hobbit. At any other site, make a Hobbit strength +3.
        EVALUATORS.put("1_296", new AbstractCardEvaluator() {
            @Override
            public boolean doesAnythingIfPlayed(LotroGame game, int physicalId, String playerName) {
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

                return skirmishActionMadeDifference(game, playerName, without, with, skirmish) || (canCancel && (without.equals(ResolveSkirmishEffect.Result.FELLOWSHIP_LOSES) || without.equals(ResolveSkirmishEffect.Result.FELLOWSHIP_OVERWHELMED)));
            }
        });

        // Bred for Battle - Skirmish: Exert an Uruk-hai to make it strength +3.
        EVALUATORS.put("1_121", new AbstractCardEvaluator() {
            @Override
            public boolean doesAnythingIfPlayed(LotroGame game, int physicalId, String playerName) {
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
            }
        });

        // Their Halls of Stone - Skirmish: Make a Dwarf strength +2 (or +4 if at an underground site).
        EVALUATORS.put("1_26", new AbstractCardEvaluator() {
            @Override
            public boolean doesAnythingIfPlayed(LotroGame game, int physicalId, String playerName) {
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
            }
        });

        // Defiance - Skirmish: Make an Elf strength +2 (or +4 if skirmishing a Nazgûl).
        EVALUATORS.put("1_37", new AbstractCardEvaluator() {
            @Override
            public boolean doesAnythingIfPlayed(LotroGame game, int physicalId, String playerName) {
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
            }
        });

        // Intimidate - Spell. Response: If a companion is about to take a wound, spot Gandalf to prevent that wound.
        EVALUATORS.put("1_76", new AbstractCardEvaluator() {
            @Override
            public boolean doesAnythingIfPlayed(LotroGame game, int physicalId, String playerName) {
                // Simplified, not counting for more damage at the same time
                return true;
            }
        });

        // Mysterious Wizard - Spell. Skirmish: Make Gandalf strength +2 (or +4 if there are 4 or fewer burdens on the Ring-bearer).
        EVALUATORS.put("1_78", new AbstractCardEvaluator() {
            @Override
            public boolean doesAnythingIfPlayed(LotroGame game, int physicalId, String playerName) {
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
            }
        });

        //Sleep, Caradhras - Spell. Fellowship: Exert Gandalf to discard every condition.
        EVALUATORS.put("1_84", new AbstractCardEvaluator() {
            @Override
            public boolean doesAnythingIfPlayed(LotroGame game, int physicalId, String playerName) {
                return game.getGameState().getInPlay().stream().anyMatch((Predicate<PhysicalCard>) physicalCard -> {
                    LotroCardBlueprint blueprint = physicalCard.getBlueprint();
                    return !physicalCard.getOwner().equals(playerName) && Side.SHADOW.equals(blueprint.getSide()) && blueprint.getCardType().equals(CardType.CONDITION);
                });
            }
        });

        // Treachery Deeper Than You Know - Spell Fellowship: Spot Gandalf to reveal an opponent's hand.
        EVALUATORS.put("1_86", new AbstractCardEvaluator() {
            @Override
            public boolean doesAnythingIfPlayed(LotroGame game, int physicalId, String playerName) {
                // Bots do not use it now (4th of August 2025), but let's assume having a look is good
                // Simplified, not taking into account double reveal
                String oppName = game.getGameState().getPlayerNames().stream().filter(s -> !s.equals(playerName)).findFirst().orElseThrow();
                return game.getModifiersQuerying().canLookOrRevealCardsInHand(game, oppName, playerName);
            }
        });

        // Noble Intentions - Skirmish: Exert a companion (except a Hobbit) to make a Hobbit strength +3.
        EVALUATORS.put("1_304", new AbstractCardEvaluator() {
            @Override
            public boolean doesAnythingIfPlayed(LotroGame game, int physicalId, String playerName) {
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
            }
        });

        // Sorry About Everything - Fellowship: Exert a Hobbit companion to remove a burden.
        EVALUATORS.put("1_312", new AbstractCardEvaluator() {
            @Override
            public boolean doesAnythingIfPlayed(LotroGame game, int physicalId, String playerName) {
                return game.getGameState().getBurdens() > 0;
            }
        });

        // Drums in the Deep - Skirmish: Make a [moria] Orc strength +2 (or +4 if skirmishing a Dwarf).
        EVALUATORS.put("1_168", new AbstractCardEvaluator() {
            @Override
            public boolean doesAnythingIfPlayed(LotroGame game, int physicalId, String playerName) {
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
            }
        });

        // Host of Thousands - Shadow: Play a [moria] Orc from your discard pile.
        EVALUATORS.put("1_187", new AbstractCardEvaluator() {
            @Override
            public boolean doesAnythingIfPlayed(LotroGame game, int physicalId, String playerName) {
                return !game.getModifiersQuerying().hasFlagActive(game, ModifierFlag.CANT_PLAY_FROM_DISCARD_OR_DECK);
            }
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

        return skirmishActionMadeDifference(game, playerName, without, with, skirmish);
    }

    private static boolean skirmishActionMadeDifference(LotroGame game, String playerName, ResolveSkirmishEffect.Result without, ResolveSkirmishEffect.Result with, Skirmish skirmish) {
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
