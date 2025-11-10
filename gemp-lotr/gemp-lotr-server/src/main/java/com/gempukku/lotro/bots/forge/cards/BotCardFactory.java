package com.gempukku.lotro.bots.forge.cards;

import com.gempukku.lotro.bots.forge.cards.ability.*;
import com.gempukku.lotro.bots.forge.cards.ability2.*;
import com.gempukku.lotro.bots.forge.cards.ability2.ActivatedAbility;
import com.gempukku.lotro.bots.forge.cards.ability2.Target;
import com.gempukku.lotro.bots.forge.cards.ability2.TriggeredAbility;
import com.gempukku.lotro.bots.forge.cards.ability2.condition.Condition;
import com.gempukku.lotro.bots.forge.cards.ability2.cost.Cost;
import com.gempukku.lotro.bots.forge.cards.ability2.effect.Effect;
import com.gempukku.lotro.bots.forge.cards.ability2.trigger.Trigger;
import com.gempukku.lotro.bots.forge.cards.abstractcard.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.bots.forge.plan.PlannedBoardState;

import java.util.List;
import java.util.Set;

import static com.gempukku.lotro.bots.forge.cards.ability.Effect.*;
import static com.gempukku.lotro.bots.forge.cards.ability.Condition.*;
import static com.gempukku.lotro.bots.forge.cards.TriggerCondition.*;
import static com.gempukku.lotro.bots.forge.cards.ability.Cost.*;
import static com.gempukku.lotro.bots.forge.cards.ability.Target.*;

public class BotCardFactory {
    public static BotCard create(PhysicalCard card) {
        String blueprintId = card.getBlueprintId();
        if (blueprintId.startsWith("1_")) {
            return createSetOneCard(card);
        }
        throw new IllegalArgumentException("Card is not supported: " + card.getBlueprint().getFullName());
    }

    private static BotCard createSetOneCard(PhysicalCard card) {
        // 1_1 Isildur's Bane - <b>Response:</b> If bearer is about to take a wound, he wears The One Ring until the regroup phase.<br>While wearing The One Ring, each time the Ring-bearer is about to take a wound, add 2 burdens instead.
        if (card.getBlueprintId().equals("1_1")) {
            return new BotOneRingCard(card) {
                @Override
                public List<BotAbility> getAbilities() {
                    return List.of(
                            AbilityBuilder
                                    .triggered(ABOUT_TO_TAKE_A_WOUND)
                                    .optional(true)
                                    .effect(wearRingUntilRegroup())
                                    .build(),
                            AbilityBuilder
                                    .continuous()
                                    .condition(wearingRing())
                                    .effect(preventWoundToBearerForBurden(2))
                                    .build()
                    );
                }
            };
        }
        // 1_2 The Ruling Ring - <b>Response:</b> If bearer is about to take a wound in a skirmish, he wears The One Ring until the regroup phase.<br>While wearing The One Ring, each time the Ring-bearer is about to take a wound during a skirmish, add a burden instead.
        else if (card.getBlueprintId().equals("1_2")) {
            return new BotOneRingCard(card) {
                @Override
                public List<BotAbility> getAbilities() {
                    return List.of(
                            AbilityBuilder
                                    .triggered(ABOUT_TO_TAKE_A_WOUND_IN_SKIRMISH)
                                    .optional(true)
                                    .effect(wearRingUntilRegroup())
                                    .build(),
                            AbilityBuilder
                                    .continuous()
                                    .condition(wearingRing())
                                    .condition(phaseIs(Phase.SKIRMISH))
                                    .effect(preventWoundToBearerForBurden(1))
                                    .build()
                    );
                }
            };
        }
        // 1_3
        // 1_4
        // 1_5
        // 1_6
        // 1_7
        // 1_8
        // 1_9
        // 1_10
        // 1_11
        // 1_12 Gimli, Dwarf of Erebor - <b>Damage +1</b>.<br><b>Fellowship:</b> If the twilight pool has fewer than 2 twilight tokens, add (2) to place a card from hand beneath your draw deck.
        else if (card.getBlueprintId().equals("1_12")) {
            return new BotCompanionCard(card) {
                @Override
                public List<BotAbility> getAbilities() {
                    return List.of(
                            AbilityBuilder
                                    .activated()
                                    .condition(phaseIs(Phase.FELLOWSHIP))
                                    .condition(twilightLessThan(2))
                                    .cost(addTwilight(2))
                                    .effect(putCardFromHandToBottomOfDeck())
                                    .target(any())
                                    .build()
                    );
                }

                @Override
                public List<ActivatedAbility> getActivatedAbilities() {
                    return List.of(
                            new ActivatedAbilityBuilder()
                                    .phase(Phase.FELLOWSHIP)
                                    .condition(Condition.twilightLessThan(2))
                                    .cost(Cost.addTwilight(2))
                                    .effect(Effect.putFromHandToBottomOfDeck())
                                    .build());
                }
            };
        }
        // 1_13
        // 1_14
        // 1_15
        // 1_16
        // 1_17
        // 1_18
        // 1_19
        // 1_20
        // 1_21
        // 1_22
        // 1_23
        // 1_24
        // 1_25
        // 1_26 Their Halls of Stone - Skirmish: Make a Dwarf strength +2 (or +4 if at an underground site).
        else if (card.getBlueprintId().equals("1_26")) {
            return new BotEventCard(card) {
                @Override
                public List<BotAbility> getAbilities() {
                    return List.of(
                            AbilityBuilder
                                    .onPlay()
                                    .effect(
                                            conditional(
                                                    isAt(Keyword.UNDERGROUND),
                                                    modifyStrength(4),
                                                    modifyStrength(2)
                                            )
                                    )
                                    .target(race(Race.DWARF))
                                    .build()
                    );
                }

                @Override
                public EventAbility getEventAbility() {
                    throw new IllegalStateException("Skirmish events not yet implemented");
                }
            };
        }
        // 1_27
        // 1_28
        // 1_29
        // 1_30
        // 1_31
        // 1_32
        // 1_33
        // 1_34
        // 1_35
        // 1_36
        // 1_37 Defiance - Skirmish: Make an Elf strength +2 (or +4 if skirmishing a Nazgûl).
        else if (card.getBlueprintId().equals("1_37")) {
            return new BotEventCard(card) {
                @Override
                public List<BotAbility> getAbilities() {
                    return List.of(
                            AbilityBuilder
                                    .onPlay()
                                    .effect(
                                            conditional(
                                                    skirmishingWith(Race.NAZGUL),
                                                    modifyStrength(4),
                                                    modifyStrength(2)
                                            )
                                    )
                                    .target(race(Race.ELF))
                                    .build()
                    );
                }

                @Override
                public EventAbility getEventAbility() {
                    throw new IllegalStateException("Skirmish events not yet implemented");
                }
            };
        }
        // 1_38
        // 1_39
        // 1_40
        // 1_41
        // 1_42
        // 1_43
        // 1_44
        // 1_45
        // 1_46
        // 1_47
        // 1_48
        // 1_49
        // 1_50
        // 1_51 Legolas, Prince of Mirkwood - <b>Archer</b>.<br>While skirmishing a Nazgûl, Legolas is strength +3.
        else if (card.getBlueprintId().equals("1_51")) {
            return new BotCompanionCard(card) {
                @Override
                public List<BotAbility> getAbilities() {
                    return List.of(
                            AbilityBuilder
                                    .continuous()
                                    .condition(skirmishingWith(Race.NAZGUL))
                                    .effect(modifyStrength(3))
                                    .target(self(self))
                                    .build()
                    );
                }
            };
        }
        // 1_52
        // 1_53
        // 1_54
        // 1_55
        // 1_56
        // 1_57
        // 1_58
        // 1_59
        // 1_60
        // 1_61
        // 1_62
        // 1_63
        // 1_64
        // 1_65
        // 1_66
        // 1_67
        // 1_68
        // 1_69
        // 1_70 Barliman Butterbur, Prancing Pony Proprietor - <b>Fellowship:</b> Exert Barliman Butterbur to take a [gandalf] event into hand from your discard pile.
        else if (card.getBlueprintId().equals("1_70")) {
            return new BotAllyCard(card) {
                @Override
                public List<BotAbility> getAbilities() {
                    return List.of(
                            AbilityBuilder
                                    .activated()
                                    .condition(phaseIs(Phase.FELLOWSHIP))
                                    .cost(exertSelf())
                                    .effect(putCardFromDiscardIntoHand())
                                    .target(and(
                                            culture(Culture.GANDALF),
                                            cardType(CardType.EVENT)))
                                    .build()
                    );
                }

                @Override
                public List<ActivatedAbility> getActivatedAbilities() {
                    return List.of(
                            new ActivatedAbilityBuilder()
                                    .phase(Phase.FELLOWSHIP)
                                    .cost(Cost.exertSelf(this))
                                    .effect(Effect.takeIntoHandFromDiscard(Target.and(
                                            Target.culture(Culture.GANDALF),
                                            Target.cardType(CardType.EVENT)
                                    )))
                                    .build());
                }
            };
        }
        // 1_71
        // 1_72
        // 1_73
        // 1_74
        // 1_75
        // 1_76 Intimidate - Spell. Response: If a companion is about to take a wound, spot Gandalf to prevent that wound.
        else if (card.getBlueprintId().equals("1_76")) {
            return new BotResponseEventCard(card) {
                @Override
                public List<BotAbility> getAbilities() {
                    return List.of(
                            AbilityBuilder
                                    .onPlay()
                                    .effect(preventNextWound())
                                    .target(cardType(CardType.COMPANION))
                                    .build()
                    );
                }

                @Override
                public Set<TriggerCondition> getTriggerConditions() {
                    return Set.of(TriggerCondition.COMPANION_ABOUT_TO_TAKE_A_WOUND);
                }

                @Override
                public Condition getCondition() {
                    return Condition.spot("Gandalf");
                }

                @Override
                public EventAbility getEventAbility() {
                    throw new IllegalStateException("Response events not yet implemented");
                }
            };
        }
        // 1_77
        // 1_78 Mysterious Wizard - Spell. Skirmish: Make Gandalf strength +2 (or +4 if there are 4 or fewer burdens on the Ring-bearer).
        else if (card.getBlueprintId().equals("1_78")) {
            return new BotEventCard(card) {
                @Override
                public List<BotAbility> getAbilities() {
                    return List.of(
                            AbilityBuilder
                                    .onPlay()
                                    .effect(
                                            conditional(
                                                    burdensLessThan(5),
                                                    modifyStrength(4),
                                                    modifyStrength(2)
                                            )
                                    )
                                    .target(title("Gandalf"))
                                    .build()
                    );
                }

                @Override
                public EventAbility getEventAbility() {
                    throw new IllegalStateException("Skirmish events not yet implemented");
                }
            };
        }
        // 1_79
        // 1_80
        // 1_81
        // 1_82
        // 1_83
        // 1_84 Sleep, Caradhras - Spell. Fellowship: Exert Gandalf to discard every condition.
        else if (card.getBlueprintId().equals("1_84")) {
            return new BotEventCard(card) {
                @Override
                public List<BotAbility> getAbilities() {
                    return List.of(
                            AbilityBuilder
                                    .onPlay()
                                    .cost(exert("Gandalf"))
                                    .effect(discardFromPlay(ALL))
                                    .target(cardType(CardType.CONDITION))
                                    .build()
                    );
                }

                @Override
                public EventAbility getEventAbility() {
                    return new EventAbilityBuilder()
                            .cost(Cost.exert("Gandalf"))
                            .effect(Effect.discardFromPlayAll(CardType.CONDITION))
                            .build();
                }
            };
        }
        // 1_85
        // 1_86 Treachery Deeper Than You Know - Spell Fellowship: Spot Gandalf to reveal an opponent's hand.
        else if (card.getBlueprintId().equals("1_86")) {
            return new BotEventCard(card) {
                @Override
                public List<BotAbility> getAbilities() {
                    return List.of(
                            AbilityBuilder
                                    .onPlay()
                                    .effect(revealOpponentsHand())
                                    .build()
                    );
                }

                @Override
                public Condition getCondition() {
                    return Condition.spot("Gandalf");
                }

                @Override
                public EventAbility getEventAbility() {
                    return new EventAbilityBuilder()
                            .effect(Effect.revealOpponentsHand())
                            .build();
                }
            };
        }
        // 1_87
        // 1_88
        // 1_89
        // 1_90
        // 1_91
        // 1_92 Armor - Bearer must be a Man.<br>Bearer takes no more than 1 wound during each skirmish phase.
        else if (card.getBlueprintId().equals("1_92")) {
            return new BotObjectAttachableCard(card) {
                @Override
                public List<BotAbility> getAbilities() {
                    return List.of(
                            AbilityBuilder
                                    .continuous()
                                    .effect(limitWoundsTakenPerPhase(Phase.SKIRMISH, 1))
                                    .target(bearer(self))
                                    .build()
                    );
                }

                @Override
                protected boolean canBearThis(PlannedBoardState plannedBoardState, BotCard target) {
                    return Race.MAN.equals(target.getSelf().getBlueprint().getRace());
                }

                @Override
                public BotTargetingMode getAttachTargetingMode() {
                    return BotTargetingMode.COMPANION_LOW_STRENGTH;
                }
            };
        }
        // 1_93
        // 1_94 Athelas - Bearer must be a [gondor] Man.<br><b>Fellowship:</b> Discard this possession to heal a companion or to remove a Shadow condition from a companion.
        else if (card.getBlueprintId().equals("1_94")) {
            return new BotObjectAttachableCard(card) {
                @Override
                public List<BotAbility> getAbilities() {
                    return List.of(
                            AbilityBuilder
                                    .activated()
                                    .condition(phaseIs(Phase.FELLOWSHIP))
                                    .cost(discardSelf())
                                    .effect(heal())
                                    .target(cardType(CardType.COMPANION))
                                    .build(),
                            AbilityBuilder
                                    .activated()
                                    .condition(phaseIs(Phase.FELLOWSHIP))
                                    .cost(discardSelf())
                                    .effect(discardFromPlay())
                                    .target(and(and(
                                            side(Side.SHADOW),
                                            cardType(CardType.CONDITION)),
                                            attachedTo(CardType.COMPANION)))
                                    .build()
                    );
                }

                @Override
                public List<ActivatedAbility> getActivatedAbilities() {
                    return List.of(
                            new ActivatedAbilityBuilder()
                                    .phase(Phase.FELLOWSHIP)
                                    .cost(Cost.discardSelf(this))
                                    .effect(Effect.heal(CardType.COMPANION))
                                    .build(),
                            new ActivatedAbilityBuilder()
                                    .phase(Phase.FELLOWSHIP)
                                    .cost(Cost.discardSelf(this))
                                    .effect(Effect.discardFromPlay(Target.and(Target.and(
                                                    Target.side(Side.SHADOW),
                                                    Target.cardType(CardType.CONDITION)),
                                            Target.attachedTo(CardType.COMPANION))))
                                    .build()
                    );
                }

                @Override
                protected boolean canBearThis(PlannedBoardState plannedBoardState, BotCard target) {
                    return Culture.GONDOR.equals(target.getSelf().getBlueprint().getCulture())
                            && Race.MAN.equals(target.getSelf().getBlueprint().getRace());
                }

                @Override
                public BotTargetingMode getAttachTargetingMode() {
                    return BotTargetingMode.COMPANION_NOT_DYING;
                }
            };
        }
        // 1_95
        // 1_96
        // 1_97 Boromir, Son of Denethor - <b>Skirmish:</b> Exert Boromir to make a Hobbit strength +3.
        else if (card.getBlueprintId().equals("1_97")) {
            return new BotCompanionCard(card) {
                @Override
                public List<BotAbility> getAbilities() {
                    return List.of(
                            AbilityBuilder
                                    .activated()
                                    .condition(phaseIs(Phase.SKIRMISH))
                                    .cost(exertSelf())
                                    .effect(modifyStrength(3))
                                    .target(race(Race.HOBBIT))
                                    .build()
                    );
                }
            };
        }
        // 1_98
        // 1_99
        // 1_100
        // 1_101 Coat of Mail - Bearer must be a Man.<br>Bearer may not be overwhelmed unless his or her strength is tripled.
        else if (card.getBlueprintId().equals("1_101")) {
            return new BotObjectAttachableCard(card) {
                @Override
                public List<BotAbility> getAbilities() {
                    return List.of(
                            AbilityBuilder
                                    .continuous()
                                    .effect(setOverwhelmMultiplier(3))
                                    .target(bearer(self))
                                    .build());
                }

                @Override
                protected boolean canBearThis(PlannedBoardState plannedBoardState, BotCard target) {
                    return Race.MAN.equals(target.getSelf().getBlueprint().getRace());
                }

                @Override
                public BotTargetingMode getAttachTargetingMode() {
                    return BotTargetingMode.COMPANION_LOW_STRENGTH;
                }
            };
        }
        // 1_102
        // 1_103
        // 1_104 Eregion's Trails - Maneuver: Exert a ranger to make each roaming minion strength -3 until the regroup phase.
        else if (card.getBlueprintId().equals("1_104")) {
            return new BotEventCard(card) {
                @Override
                public List<BotAbility> getAbilities() {
                    return List.of(
                            AbilityBuilder
                                    .onPlay()
                                    .cost(exertRanger())
                                    .effect(modifyStrengthUntilRegroup(-3, ALL))
                                    .target(and(
                                            cardType(CardType.MINION),
                                            keyword(Keyword.ROAMING)
                                    ))
                                    .build()
                    );
                }

                @Override
                public EventAbility getEventAbility() {
                    throw new IllegalStateException("Maneuver events not yet implemented");
                }
            };
        }
        // 1_105
        // 1_106 Gondor's Vengeance - Regroup: Exert a ranger companion to discard a minion.
        else if (card.getBlueprintId().equals("1_106")) {
            return new BotEventCard(card) {
                @Override
                public List<BotAbility> getAbilities() {
                    return List.of(
                            AbilityBuilder
                                    .onPlay()
                                    .cost(exertRanger())
                                    .effect(discardFromPlay())
                                    .target(cardType(CardType.MINION))
                                    .build()
                    );
                }

                @Override
                public EventAbility getEventAbility() {
                    throw new IllegalStateException("Regroup events not yet implemented");
                }
            };
        }
        // 1_107 Great Shield - Bearer must be a Man.<br>The minion archery total is -1.
        else if (card.getBlueprintId().equals("1_107")) {
            return new BotObjectAttachableCard(card) {
                @Override
                public List<BotAbility> getAbilities() {
                    return List.of(
                            AbilityBuilder
                                    .continuous()
                                    .effect(modifyShadowArchery(-1))
                                    .build()
                    );
                }

                @Override
                protected boolean canBearThis(PlannedBoardState plannedBoardState, BotCard target) {
                    return Race.MAN.equals(target.getSelf().getBlueprint().getRace());
                }

                @Override
                public BotTargetingMode getAttachTargetingMode() {
                    return BotTargetingMode.COMPANION_NOT_DYING;
                }
            };
        }
        // 1_108  No Stranger to the Shadows - Bearer must be a ranger. Limit 1 per ranger.<br>Each site's Shadow number is -1.
        else if (card.getBlueprintId().equals("1_108")) {
            return new BotObjectAttachableCard(card) {
                @Override
                public List<BotAbility> getAbilities() {
                    return List.of(
                            AbilityBuilder
                                    .continuous()
                                    .effect(modifySiteShadowNumber(-1))
                                    .build()
                    );
                }

                @Override
                protected boolean canBearThis(PlannedBoardState plannedBoardState, BotCard target) {
                    return target.getSelf().getBlueprint().hasKeyword(Keyword.RANGER)
                            && plannedBoardState.getAttachedCards(target).stream().noneMatch(card1 -> card1.getSelf().getBlueprint().getTitle().equals(self.getBlueprint().getTitle()));
                }

                @Override
                public BotTargetingMode getAttachTargetingMode() {
                    return BotTargetingMode.COMPANION_NOT_DYING;
                }
            };
        }
        // 1_109
        // 1_110 Pathfinder - Fellowship or Regroup: Spot a ranger to play the fellowship's next site (replacing opponent's site if necessary).
        else if (card.getBlueprintId().equals("1_110")) {
            return new BotEventCard(card) {
                @Override
                public List<BotAbility> getAbilities() {
                    return List.of(
                            AbilityBuilder
                                    .onPlay()
                                    .effect(playNextSite())
                                    .build()
                    );
                }

                @Override
                public Condition getCondition() {
                    return Condition.spot(Keyword.RANGER);
                }

                @Override
                public EventAbility getEventAbility() {
                    return new EventAbilityBuilder()
                            .effect(Effect.playFellowshipsNextSite())
                            .build();
                }
            };
        }
        // 1_111
        // 1_112
        // 1_113
        // 1_114
        // 1_115
        // 1_116 Swordarm of the White Tower - Skirmish: Make a [gondor] companion strength +2 (or +4 if he or she is defender +1).
        else if (card.getBlueprintId().equals("1_116")) {
            return new BotEventCard(card) {
                @Override
                public List<BotAbility> getAbilities() {
                    return List.of(
                            AbilityBuilder
                                    .onPlay()
                                    .effect(
                                            conditional(
                                                    hasKeyword(Keyword.DEFENDER),
                                                    modifyStrength(4),
                                                    modifyStrength(2)
                                            )
                                    )
                                    .target(and(
                                            culture(Culture.GONDOR),
                                            cardType(CardType.COMPANION)
                                    ))
                                    .build()
                    );
                }

                @Override
                public EventAbility getEventAbility() {
                    throw new IllegalStateException("Skirmish events not yet implemented");
                }
            };
        }
        // 1_117 Swordsman of the Northern Kingdom - Skirmish: Make a ranger strength +2 (or +4 when skirmishing a roaming minion).
        else if (card.getBlueprintId().equals("1_117")) {
            return new BotEventCard(card) {
                @Override
                public List<BotAbility> getAbilities() {
                    return List.of(
                            AbilityBuilder
                                    .onPlay()
                                    .effect(
                                            conditional(
                                                    skirmishingWith(Keyword.ROAMING),
                                                    modifyStrength(4),
                                                    modifyStrength(2)
                                            )
                                    )
                                    .target(keyword(Keyword.RANGER))
                                    .build()
                    );
                }

                @Override
                public EventAbility getEventAbility() {
                    throw new IllegalStateException("Skirmish events not yet implemented");
                }
            };
        }
        // 1_118
        // 1_119
        // 1_120
        // 1_121 Bred for Battle - Skirmish: Exert an Uruk-hai to make it strength +3.
        else if (card.getBlueprintId().equals("1_121")) {
            return new BotEventCard(card) {
                @Override
                public List<BotAbility> getAbilities() {
                    return List.of(
                            AbilityBuilder
                                    .onPlay()
                                    .cost(exertTarget())
                                    .effect(modifyStrength(3))
                                    .target(race(Race.URUK_HAI))
                                    .build()
                    );
                }

                @Override
                public EventAbility getEventAbility() {
                    throw new IllegalStateException("Skirmish events not yet implemented");
                }
            };
        }
        // 1_122
        // 1_123
        // 1_124
        // 1_125
        // 1_126
        // 1_127
        // 1_128
        // 1_129
        // 1_130
        // 1_131
        // 1_132
        // 1_133 Saruman's Ambition - Plays to your support area.<br>The twilight cost of your [isengard] events is -1.<br><b>Skirmish:</b> Discard this condition to make an Uruk-hai strength +2.
        else if (card.getBlueprintId().equals("1_133")) {
            return new BotObjectSupportAreaCard(card) {
                @Override
                public List<BotAbility> getAbilities() {
                    return List.of(
                            AbilityBuilder
                                    .continuous()
                                    .effect(modifyTwilightCost(-1))
                                    .target(and(
                                            culture(Culture.ISENGARD),
                                            cardType(CardType.EVENT)
                                    ))
                                    .build(),
                            AbilityBuilder
                                    .activated()
                                    .condition(phaseIs(Phase.SKIRMISH))
                                    .cost(discardSelf())
                                    .effect(modifyStrength(2))
                                    .target(race(Race.URUK_HAI))
                                    .build()
                    );
                }
            };
        }
        // 1_134
        // 1_135
        // 1_136
        // 1_137
        // 1_138
        // 1_139
        // 1_140
        // 1_141 Their Arrows Enrage - To play, spot Saruman or an Uruk-hai. Plays to your support area.<br>Each archer companion and archer ally is strength -1.
        else if (card.getBlueprintId().equals("1_141")) {
            return new BotObjectSupportAreaCard(card) {
                @Override
                public List<BotAbility> getAbilities() {
                    return List.of(
                            AbilityBuilder
                                    .continuous()
                                    .effect(modifyStrength(-1))
                                    .target(and(
                                            keyword(Keyword.ARCHER),
                                            or(
                                                    cardType(CardType.COMPANION),
                                                    cardType(CardType.ALLY)
                                            )
                                    ))
                                    .build()
                    );
                }

                @Override
                public Condition getCondition() {
                    return Condition.spot(Target.or(
                            Target.race(Race.URUK_HAI),
                            Target.title("Saruman")));
                }
            };
        }
        // 1_142
        // 1_143
        // 1_144
        // 1_145 Uruk Brood - <b>Damage +1</b>.<br><b>Skirmish:</b> Remove (2) to make this minion strength +1 for each other Uruk-hai you spot.
        else if (card.getBlueprintId().equals("1_145")) {
            return new BotMinionCard(card) {
                @Override
                public List<BotAbility> getAbilities() {
                    return List.of(
                            AbilityBuilder
                                    .activated()
                                    .condition(phaseIs(Phase.SKIRMISH))
                                    .cost(removeTwilight(2))
                                    .effect(modifyStrengthPerSpotted(1, Race.URUK_HAI, false))
                                    .target(self(self))
                                    .build()
                    );
                }
            };
        }
        // 1_146
        // 1_147
        // 1_148
        // 1_149
        // 1_150 Uruk Rager - <b>Damage +1</b>.<br>Each time this minion wins a skirmish, the Free Peoples player must discard the top 2 cards of his or her draw deck.
        else if (card.getBlueprintId().equals("1_150")) {
            return new BotMinionCard(card) {
                @Override
                public List<BotAbility> getAbilities() {
                    return List.of(
                            AbilityBuilder
                                    .triggered(WINS_A_SKIRMISH)
                                    .optional(false)
                                    .effect(freePeoplesPlayerMillsCards(2))
                                    .build()
                    );
                }
            };
        }
        // 1_151 Uruk Savage - <b>Damage +1</b>. <helper>(When this minion wins a skirmish, add 1 extra wound to the defender.)</helper>
        else if (card.getBlueprintId().equals("1_151")) {
            return new BotMinionCard(card) {
                @Override
                public List<BotAbility> getAbilities() {
                    return List.of();
                }
            };
        }
        // 1_152 Uruk Shaman - <b>Damage +1</b>.<br><b>Maneuver:</b> Remove (2) to heal an Uruk-hai.
        else if (card.getBlueprintId().equals("1_152")) {
            return new BotMinionCard(card) {
                @Override
                public List<BotAbility> getAbilities() {
                    return List.of(
                            AbilityBuilder
                                    .activated()
                                    .condition(phaseIs(Phase.MANEUVER))
                                    .cost(removeTwilight(2))
                                    .effect(heal())
                                    .target(race(Race.URUK_HAI))
                                    .build()
                    );
                }
            };
        }
        // 1_153 Uruk Slayer - <b>Damage +1</b>.<br><b>Skirmish:</b> Remove (1) to make this minion strength +1 (limit +3).
        else if (card.getBlueprintId().equals("1_153")) {
            return new BotMinionCard(card) {
                @Override
                public List<BotAbility> getAbilities() {
                    return List.of(
                            AbilityBuilder
                                    .activated()
                                    .condition(phaseIs(Phase.SKIRMISH))
                                    .cost(removeTwilight(1))
                                    .effect(modifyStrengthWithLimit(1, 3))
                                    .target(self(self))
                                    .build()
                    );
                }
            };
        }
        // 1_154 Uruk Soldier - <b>Damage +1</b>.<br>When you play this minion, you may make the Free Peoples player discard the top card of his draw deck.
        else if (card.getBlueprintId().equals("1_154")) {
            return new BotMinionCard(card) {
                @Override
                public List<BotAbility> getAbilities() {
                    return List.of(
                            AbilityBuilder
                                    .onPlay()
                                    .optional(true)
                                    .effect(freePeoplesPlayerMillsCards(1))
                                    .build()
                    );
                }
            };
        }
        // 1_155
        // 1_156
        // 1_157 Uruk-hai Armory - Plays to your support area.<br>While you can spot an Uruk-hai, the fellowship archery total is -1.
        else if (card.getBlueprintId().equals("1_157")) {
            return new BotObjectSupportAreaCard(card) {
                @Override
                public List<BotAbility> getAbilities() {
                    return List.of(
                            AbilityBuilder
                                    .continuous()
                                    .condition(spot(race(Race.URUK_HAI)))
                                    .effect(modifyFpArchery(-1))
                                    .build()
                    );
                }
            };
        }
        // 1_158 Uruk-hai Raiding Party - <b>Damage +1</b>. <helper>(When this minion wins a skirmish, add 1 extra wound to the defender.)</helper>
        else if (card.getBlueprintId().equals("1_158")) {
            return new BotMinionCard(card) {
                @Override
                public List<BotAbility> getAbilities() {
                    return List.of();
                }
            };
        }
        // 1_159
        // 1_160
        // 1_161
        // 1_162
        // 1_163
        // 1_164
        // 1_165
        // 1_166
        // 1_167
        // 1_168 Drums in the Deep - Skirmish: Make a [moria] Orc strength +2 (or +4 if skirmishing a Dwarf).
        else if (card.getBlueprintId().equals("1_168")) {
            return new BotEventCard(card) {
                @Override
                public List<BotAbility> getAbilities() {
                    return List.of(
                            AbilityBuilder
                                    .onPlay()
                                    .effect(
                                            conditional(
                                                    skirmishingWith(Race.DWARF),
                                                    modifyStrength(4),
                                                    modifyStrength(2)
                                            )
                                    )
                                    .target(and(
                                            culture(Culture.MORIA),
                                            race(Race.ORC)
                                    ))
                                    .build()
                    );
                }

                @Override
                public EventAbility getEventAbility() {
                    throw new IllegalStateException("Skirmish events not yet implemented");
                }
            };
        }
        // 1_169
        // 1_170
        // 1_171
        // 1_172
        // 1_173
        // 1_174
        // 1_175
        // 1_176 Goblin Marksman - <b>Archer</b>. <helper>(Add 1 to the minion archery total.)</helper>
        else if (card.getBlueprintId().equals("1_176")) {
            return new BotMinionCard(card) {
                @Override
                public List<BotAbility> getAbilities() {
                    return List.of();
                }
            };
        }
        // 1_177 Goblin Patrol Troop
        else if (card.getBlueprintId().equals("1_177")) {
            return new BotMinionCard(card) {
                @Override
                public List<BotAbility> getAbilities() {
                    return List.of();
                }
            };
        }
        // 1_178 Goblin Runner - When you play this minion, you may add (2).
        else if (card.getBlueprintId().equals("1_178")) {
            return new BotMinionCard(card) {
                @Override
                public List<BotAbility> getAbilities() {
                    return List.of(
                            AbilityBuilder
                                    .onPlay()
                                    .optional(true)
                                    .effect(addTwilight(2))
                                    .build()
                    );
                }

                @Override
                public TriggeredAbility getTriggeredAbility() {
                    return new TriggeredAbilityBuilder()
                            .trigger(Trigger.WHEN_PLAYED)
                            .optional(false) // json card implementation sets this as non-optional trigger even though the text says "you may"
                            .effect(Effect.addTwilight(2))
                            .build();
                }
            };
        }
        // 1_179 Goblin Scavengers - When you play this minion, you may play a weapon from your discard pile on your [moria] Orc.
        else if (card.getBlueprintId().equals("1_179")) {
            return new BotMinionCard(card) {
                @Override
                public List<BotAbility> getAbilities() {
                    return List.of(
                            AbilityBuilder
                                    .onPlay()
                                    .optional(true)
                                    .effect(playItemFromDiscardOn(Culture.MORIA, Race.ORC))
                                    .target(or(
                                            possessionClass(PossessionClass.HAND_WEAPON),
                                            possessionClass(PossessionClass.RANGED_WEAPON)
                                    ))
                                    .build()
                    );
                }
            };
        }
        // 1_180 Goblin Scimitar - Bearer must be a [moria] Orc.<br>When you play this possession, you may draw a card.
        else if (card.getBlueprintId().equals("1_180")) {
            return new BotObjectAttachableCard(card) {
                @Override
                public List<BotAbility> getAbilities() {
                    return List.of(
                            AbilityBuilder
                                    .onPlay()
                                    .optional(true)
                                    .effect(drawCards(1))
                                    .build()
                    );
                }

                @Override
                protected boolean canBearThis(PlannedBoardState plannedBoardState, BotCard target) {
                    return Culture.MORIA.equals(target.getSelf().getBlueprint().getCulture())
                            && Race.ORC.equals(target.getSelf().getBlueprint().getRace());
                }

                @Override
                public BotTargetingMode getAttachTargetingMode() {
                    return BotTargetingMode.HIGH_STRENGTH;
                }

                @Override
                public TriggeredAbility getTriggeredAbility() {
                    return new TriggeredAbilityBuilder()
                            .trigger(Trigger.WHEN_PLAYED)
                            .optional(true)
                            .effect(Effect.drawCard(1))
                            .build();
                }
            };

        }
        // 1_181 Goblin Sneak - When you play this minion, you may place a [moria] Orc from your discard pile beneath your draw deck.
        else if (card.getBlueprintId().equals("1_181")) {
            return new BotMinionCard(card) {
                @Override
                public List<BotAbility> getAbilities() {
                    return List.of(
                            AbilityBuilder
                                    .onPlay()
                                    .optional(true)
                                    .effect(putCardFromDiscardToBottomOfDeck())
                                    .target(and(
                                            culture(Culture.MORIA),
                                            race(Race.ORC)
                                    ))
                                    .build()
                    );
                }
            };
        }
        // 1_182
        // 1_183
        // 1_184
        // 1_185
        // 1_186
        // 1_187 Host of Thousands - Shadow: Play a [moria] Orc from your discard pile.
        else if (card.getBlueprintId().equals("1_187")) {
            return new BotEventCard(card) {
                @Override
                public List<BotAbility> getAbilities() {
                    return List.of(
                            AbilityBuilder
                                    .onPlay()
                                    .effect(playFromDiscard())
                                    .target(and(
                                            culture(Culture.MORIA),
                                            race(Race.ORC)
                                    ))
                                    .build()
                    );
                }

                @Override
                public Condition getCondition() {
                    return Condition.spotPlayableInDiscard(Culture.MORIA, Race.ORC);
                }

                @Override
                public EventAbility getEventAbility() {
                    return new EventAbilityBuilder()
                            .effect(Effect.playFromDiscard(Target.culture(Culture.MORIA).and(Target.race(Race.ORC))))
                            .build();
                }
            };
        }
        // 1_188
        // 1_189
        // 1_190
        // 1_191 Moria Scout -  When you play this minion, spot an Elf to add (2).
        else if (card.getBlueprintId().equals("1_191")) {
            return new BotMinionCard(card) {
                @Override
                public List<BotAbility> getAbilities() {
                    return List.of(
                            AbilityBuilder
                                    .onPlay()
                                    .optional(true)
                                    .condition(spot(race(Race.ELF)))
                                    .effect(addTwilight(2))
                                    .build()
                    );
                }

                @Override
                public TriggeredAbility getTriggeredAbility() {
                    return new TriggeredAbilityBuilder()
                            .trigger(Trigger.WHEN_PLAYED)
                            .optional(false) // json card implementation sets this as non-optional trigger even though the text says spot, which is optional
                            .condition(Condition.spot(Race.ELF))
                            .effect(Effect.addTwilight(2))
                            .build();
                }
            };
        }
        // 1_192
        // 1_193
        // 1_194
        // 1_195
        // 1_196 They Are Coming - Plays to your support area.<br><b>Shadow:</b> Discard 3 cards from hand to play a [moria] Orc from your discard pile.
        else if (card.getBlueprintId().equals("1_196")) {
            return new BotObjectSupportAreaCard(card) {
                @Override
                public List<BotAbility> getAbilities() {
                    return List.of(
                            AbilityBuilder
                                    .activated()
                                    .condition(phaseIs(Phase.SHADOW))
                                    .cost(discardFromHand(3))
                                    .effect(playFromDiscard())
                                    .target(and(
                                            culture(Culture.MORIA),
                                            race(Race.ORC)
                                    ))
                                    .build()
                    );
                }
            };
        }
        // 1_197
        // 1_198
        // 1_199
        // 1_200
        // 1_201
        // 1_202
        // 1_203
        // 1_204
        // 1_205
        // 1_206
        // 1_207
        // 1_208
        // 1_209
        // 1_210
        // 1_211
        // 1_212
        // 1_213
        // 1_214
        // 1_215
        // 1_216
        // 1_217
        // 1_218
        // 1_219
        // 1_220
        // 1_221
        // 1_222
        // 1_223
        // 1_224
        // 1_225
        // 1_226
        // 1_227
        // 1_228
        // 1_229
        // 1_230
        // 1_231
        // 1_232
        // 1_233
        // 1_234
        // 1_235
        // 1_236
        // 1_237
        // 1_238
        // 1_239
        // 1_240
        // 1_241
        // 1_242
        // 1_243
        // 1_244
        // 1_245
        // 1_246
        // 1_247
        // 1_248
        // 1_249
        // 1_250
        // 1_251
        // 1_252
        // 1_253
        // 1_254
        // 1_255
        // 1_256
        // 1_257
        // 1_258
        // 1_259
        // 1_260
        // 1_261
        // 1_262
        // 1_263
        // 1_264
        // 1_265
        // 1_266
        // 1_267
        // 1_268
        // 1_269
        // 1_270
        // 1_271
        // 1_272
        // 1_273
        // 1_274
        // 1_275
        // 1_276
        // 1_277
        // 1_278
        // 1_279
        // 1_280
        // 1_281
        // 1_282
        // 1_283
        // 1_284
        // 1_285
        // 1_286 Bounder - <b>Skirmish:</b> Exert this ally to prevent a Hobbit from being overwhelmed unless that Hobbit's strength is tripled.
        else if (card.getBlueprintId().equals("1_286")) {
            return new BotAllyCard(card) {
                @Override
                public List<BotAbility> getAbilities() {
                    return List.of(
                            AbilityBuilder
                                    .activated()
                                    .condition(phaseIs(Phase.SKIRMISH))
                                    .cost(exertSelf())
                                    .effect(setOverwhelmMultiplier(3))
                                    .target(race(Race.HOBBIT))
                                    .build()
                    );
                }
            };
        }
        // 1_287
        // 1_288
        // 1_289
        // 1_290 Frodo, Son of Drogo - <b>Ring-bearer (resistance 10).</b><br><b>Fellowship:</b> Exert another companion who has the Frodo signet to heal Frodo.
        else if (card.getBlueprintId().equals("1_290")) {
            return new BotCompanionCard(card) {
                @Override
                public List<BotAbility> getAbilities() {
                    return List.of(
                            AbilityBuilder
                                    .activated()
                                    .condition(phaseIs(Phase.FELLOWSHIP))
                                    .cost(exert(and(and(
                                            cardType(CardType.COMPANION),
                                            signet(Signet.FRODO)),
                                            not(self(self))
                                    )))
                                    .effect(heal())
                                    .target(self(self))
                                    .build()
                    );
                }

                @Override
                public List<ActivatedAbility> getActivatedAbilities() {
                    return List.of(
                            new ActivatedAbilityBuilder()
                                    .phase(Phase.FELLOWSHIP)
                                    .cost(Cost.exert(Target.and(Target.and(
                                                    Target.cardType(CardType.COMPANION),
                                                    Target.signet(Signet.FRODO)),
                                            Target.not(Target.self(this))
                                    )))
                                    .effect(Effect.heal("Frodo"))
                                    .build()
                    );
                }
            };
        }
        // 1_291
        // 1_292
        // 1_293
        // 1_294
        // 1_295
        // 1_296 Hobbit Intuition - Stealth. Skirmish: At sites 1 to 4, cancel a skirmish involving a Hobbit. At any other site, make a Hobbit strength +3.
        else if (card.getBlueprintId().equals("1_296")) {
            return new BotEventCard(card) {
                @Override
                public List<BotAbility> getAbilities() {
                    return List.of(
                            AbilityBuilder
                                    .onPlay()
                                    .effect(
                                            conditional(
                                                    isAt(1, 4, SitesBlock.FELLOWSHIP),
                                                    cancelSkirmishingInvolving(Race.HOBBIT),
                                                    modifyStrength(3)
                                            )
                                    )
                                    .target(race(Race.HOBBIT))
                                    .build()
                    );
                }

                @Override
                public EventAbility getEventAbility() {
                    throw new IllegalStateException("Skirmish events not yet implemented");
                }
            };
        }
        // 1_297
        // 1_298
        // 1_299 Hobbit Sword - Bearer must be a Hobbit.
        else if (card.getBlueprintId().equals("1_299")) {
            return new BotObjectAttachableCard(card) {
                @Override
                public List<BotAbility> getAbilities() {
                    return List.of();
                }

                @Override
                protected boolean canBearThis(PlannedBoardState plannedBoardState, BotCard target) {
                    return Race.HOBBIT.equals(target.getSelf().getBlueprint().getRace());
                }

                @Override
                public BotTargetingMode getAttachTargetingMode() {
                    return BotTargetingMode.COMPANION_HIGH_STRENGTH;
                }
            };
        }
        // 1_300
        // 1_301
        // 1_302
        // 1_303
        // 1_304 Noble Intentions - Skirmish: Exert a companion (except a Hobbit) to make a Hobbit strength +3.
        else if (card.getBlueprintId().equals("1_304")) {
            return new BotEventCard(card) {
                @Override
                public List<BotAbility> getAbilities() {
                    return List.of(
                            AbilityBuilder
                                    .onPlay()
                                    .cost(exert(and(
                                            cardType(CardType.COMPANION),
                                            not(race(Race.HOBBIT))
                                    )))
                                    .effect(modifyStrength(3))
                                    .target(race(Race.HOBBIT))
                                    .build()
                    );
                }

                @Override
                public EventAbility getEventAbility() {
                    throw new IllegalStateException("Skirmish events not yet implemented");
                }
            };
        }
        // 1_305
        // 1_306
        // 1_307
        // 1_308
        // 1_309 Rosie Cotton, Hobbiton Lass - Sam is strength +1.<br><b>Fellowship:</b> Exert Rosie Cotton to heal Sam.
        else if (card.getBlueprintId().equals("1_309")) {
            return new BotAllyCard(card) {
                @Override
                public List<BotAbility> getAbilities() {
                    return List.of(
                            AbilityBuilder
                                    .continuous()
                                    .effect(modifyStrength(1))
                                    .target(title("Sam"))
                                    .build(),
                            AbilityBuilder
                                    .activated()
                                    .condition(phaseIs(Phase.FELLOWSHIP))
                                    .cost(exertSelf())
                                    .effect(heal())
                                    .target(title("Sam"))
                                    .build()
                    );
                }

                @Override
                public List<ActivatedAbility> getActivatedAbilities() {
                    return List.of(
                            new ActivatedAbilityBuilder()
                                    .phase(Phase.FELLOWSHIP)
                                    .cost(Cost.exertSelf(this))
                                    .effect(Effect.heal("Sam"))
                                    .build()
                    );
                }
            };
        }
        // 1_310
        // 1_311 Sam, Son of Hamfast - <b>Fellowship:</b> Exert Sam to remove a burden.<br><b>Response:</b> If Frodo dies, make Sam the <b>Ring-bearer (resistance 5)</b>.
        else if (card.getBlueprintId().equals("1_311")) {
            return new BotCompanionCard(card) {
                @Override
                public List<BotAbility> getAbilities() {
                    return List.of(
                            AbilityBuilder
                                    .activated()
                                    .condition(phaseIs(Phase.FELLOWSHIP))
                                    .cost(exertSelf())
                                    .effect(removeBurden())
                                    .build(),
                            AbilityBuilder
                                    .triggered(FRODO_DIES)
                                    .optional(true)
                                    .effect(makeSamRingBearer())
                                    .build()
                    );
                }

                @Override
                public List<ActivatedAbility> getActivatedAbilities() {
                    return List.of(
                            new ActivatedAbilityBuilder()
                                    .phase(Phase.FELLOWSHIP)
                                    .cost(Cost.exertSelf(this))
                                    .effect(Effect.removeBurden())
                                    .build()
                    );
                }
            };
        }
        // 1_312 Sorry About Everything - Fellowship: Exert a Hobbit companion to remove a burden.
        else if (card.getBlueprintId().equals("1_312")) {
            return new BotEventCard(card) {
                @Override
                public List<BotAbility> getAbilities() {
                    return List.of(
                            AbilityBuilder
                                    .onPlay()
                                    .cost(exert(and(
                                            race(Race.HOBBIT),
                                            cardType(CardType.COMPANION)
                                    )))
                                    .effect(removeBurden())
                                    .build()
                    );
                }

                @Override
                public EventAbility getEventAbility() {
                    return new EventAbilityBuilder()
                            .cost(Cost.exert(Target.and(
                                    Target.race(Race.HOBBIT),
                                    Target.cardType(CardType.COMPANION)
                            )))
                            .effect(Effect.removeBurden())
                            .build();
                }
            };
        }
        // 1_313
        // 1_314
        // 1_315
        // 1_316
        // 1_317
        // 1_318
        // 1_319
        // 1_320 East Road - Each companion's twilight cost is +2.
        else if (card.getBlueprintId().equals("1_320")) {
            return new BotSiteCard(card) {
                @Override
                public List<BotAbility> getAbilities() {
                    return List.of(
                            AbilityBuilder
                                    .continuous()
                                    .effect(modifyTwilightCost(2))
                                    .target(cardType(CardType.COMPANION))
                                    .build()
                    );
                }
            };
        }
        // 1_321
        // 1_322
        // 1_323
        // 1_324
        // 1_325
        // 1_326 Westfarthing - <b>Fellowship:</b> Exert a Hobbit to play a companion or ally; that character's twilight cost is -1.
        else if (card.getBlueprintId().equals("1_326")) {
            return new BotSiteCard(card) {
                @Override
                public List<BotAbility> getAbilities() {
                    return List.of(
                            AbilityBuilder
                                    .activated()
                                    .condition(phaseIs(Phase.FELLOWSHIP))
                                    .cost(exert(race(Race.HOBBIT)))
                                    .effect(playWithTwilightCostModification(-1))
                                    .target(or(
                                            cardType(CardType.COMPANION),
                                            cardType(CardType.ALLY)
                                    ))
                                    .build()
                    );
                }

                @Override
                public List<ActivatedAbility> getActivatedAbilities() {
                    return List.of(
                            new ActivatedAbilityBuilder()
                                    .phase(Phase.FELLOWSHIP)
                                    .cost(Cost.exert(Race.HOBBIT))
                                    .effect(Effect.playWithBonusTwilightModification(Target.cardType(CardType.COMPANION).or(Target.cardType(CardType.ALLY)), -1))
                                    .build()
                    );
                }
            };
        }
        // 1_327 Bree Gate - While you can spot a ranger, the move limit is +1 for this turn.
        else if (card.getBlueprintId().equals("1_327")) {
            return new BotSiteCard(card) {
                @Override
                public List<BotAbility> getAbilities() {
                    return List.of(
                            AbilityBuilder
                                    .continuous()
                                    .condition(spot(keyword(Keyword.RANGER)))
                                    .effect(modifyMoveLimit(1))
                                    .build()
                    );
                }
            };
        }
        // 1_328
        // 1_329
        // 1_330
        // 1_331 Ettenmoors - <b>Plains</b>. <b>Skirmish:</b> Exert your companion or minion to make that character strength +2.
        else if (card.getBlueprintId().equals("1_331")) {
            return new BotSiteCard(card) {
                @Override
                public List<BotAbility> getAbilities() {
                    return List.of(
                            AbilityBuilder
                                    .activated()
                                    .condition(phaseIs(Phase.SKIRMISH))
                                    .cost(exertTarget())
                                    .effect(modifyStrength(2))
                                    .target(or(
                                            cardType(CardType.COMPANION),
                                            cardType(CardType.MINION)
                                    ))
                                    .build()
                    );
                }
            };
        }
        // 1_332
        // 1_333
        // 1_334
        // 1_335
        // 1_336
        // 1_337 Council Courtyard - <b>Sanctuary</b>. When the fellowship moves from Council Courtyard, remove (2).
        else if (card.getBlueprintId().equals("1_337")) {
            return new BotSiteCard(card) {
                @Override
                public List<BotAbility> getAbilities() {
                    return List.of(
                            AbilityBuilder
                                    .triggered(MOVES_FROM_THIS_SITE)
                                    .optional(false)
                                    .effect(removeTwilight(2))
                                    .build()
                    );
                }
            };
        }
        // 1_338
        // 1_339
        // 1_340 Rivendell Terrace - <b>Sanctuary</b>. <b>Fellowship:</b> Play a Man to draw a card.
        else if (card.getBlueprintId().equals("1_340")) {
            return new BotSiteCard(card) {
                @Override
                public List<BotAbility> getAbilities() {
                    return List.of(
                            AbilityBuilder
                                    .activated()
                                    .condition(phaseIs(Phase.FELLOWSHIP))
                                    .effect(playWithBonusEffect(drawCards(1)))
                                    .target(race(Race.MAN))
                                    .build()
                    );
                }

                @Override
                public List<ActivatedAbility> getActivatedAbilities() {
                    return List.of(
                            new ActivatedAbilityBuilder()
                                    .phase(Phase.FELLOWSHIP)
                                    .effect(Effect.playWithBonusDraw(Race.MAN))
                                    .build()
                    );
                }
            };
        }
        // 1_341
        // 1_342
        // 1_343
        // 1_344
        // 1_345 Mithril Mine - <b>Underground</b>. <b>Shadow:</b> Remove (1) to play a Shadow weapon from your discard pile.
        else if (card.getBlueprintId().equals("1_345")) {
            return new BotSiteCard(card) {
                @Override
                public List<BotAbility> getAbilities() {
                    return List.of(
                            AbilityBuilder
                                    .activated()
                                    .condition(phaseIs(Phase.SHADOW))
                                    .cost(removeTwilight(1))
                                    .effect(playFromDiscard())
                                    .target(and(
                                            side(Side.SHADOW),
                                            or(
                                                    possessionClass(PossessionClass.HAND_WEAPON),
                                                    possessionClass(PossessionClass.RANGED_WEAPON)
                                            )
                                    ))
                                    .build()
                    );
                }
            };
        }
        // 1_346 Moria Lake - <b>Marsh</b>. When the fellowship moves to Moria Lake, Frodo or 2 other companions must exert.
        else if (card.getBlueprintId().equals("1_346")) {
            return new BotSiteCard(card) {
                @Override
                public List<BotAbility> getAbilities() {
                    return List.of(
                            AbilityBuilder
                                    .triggered(MOVES_TO_THIS_SITE)
                                    .optional(false)
                                    .effect(choice(
                                            exert(1),
                                            exert(1, 2)
                                    ))
                                    .targetForOptionA(title("Frodo"))
                                    .targetForOptionB(not(title("Frodo")))
                                    .build()
                    );
                }
            };
        }
        // 1_347
        // 1_348
        // 1_349 The Bridge of Khazad-dûm - <b>Underground</b>. <b>Shadow:</b> Play The Balrog from your draw deck or hand; The Balrog's twilight cost is -6.
        else if (card.getBlueprintId().equals("1_349")) {
            return new BotSiteCard(card) {
                @Override
                public List<BotAbility> getAbilities() {
                    return List.of(
                            AbilityBuilder
                                    .activated()
                                    .condition(phaseIs(Phase.SHADOW))
                                    .effect(choice(
                                            playWithTwilightCostModification(-6),
                                            playFromDeckWithTwilightCostModification(-6)
                                    ))
                                    .targetForOptionA(title("The Balrog"))
                                    .targetForOptionB(title("The Balrog"))
                                    .build()
                    );
                }
            };
            }
            // 1_350 Dimrill Dale - <b>Sanctuary</b>. The twilight cost of the first [moria] Orc played each Shadow phase is -2.
            else if (card.getBlueprintId().equals("1_350")) {
                return new BotSiteCard(card) {
                    @Override
                    public List<BotAbility> getAbilities() {
                        return List.of(
                                AbilityBuilder
                                        .continuous()
                                        // Not an exact match
                                        .condition(cannotSpot(and(culture(Culture.MORIA), race(Race.ORC))))
                                        .effect(modifyTwilightCost(-2))
                                        .target(and(
                                                culture(Culture.MORIA),
                                                race(Race.ORC)
                                        ))
                                        .build()
                        );
                    }
                };
            }
        // 1_351 Galadriel's Glade - <b>Sanctuary</b>. <b>Fellowship:</b> Exert an Elf to look at an opponent's hand.
        else if (card.getBlueprintId().equals("1_351")) {
            return new BotSiteCard(card) {
                @Override
                public List<BotAbility> getAbilities() {
                    return List.of(
                            AbilityBuilder
                                    .activated()
                                    .condition(phaseIs(Phase.FELLOWSHIP))
                                    .cost(exert(race(Race.ELF)))
                                    .effect(revealOpponentsHand())
                                    .build()
                    );
                }

                @Override
                public List<ActivatedAbility> getActivatedAbilities() {
                    return List.of(
                            new ActivatedAbilityBuilder()
                                    .phase(Phase.FELLOWSHIP)
                                    .cost(Cost.exert(Race.ELF))
                                    .effect(Effect.revealOpponentsHand())
                                    .build()
                    );
                }
            };
        }
        // 1_352
        // 1_353 Anduin Confluence -  <b>River</b>. When the fellowship moves to Anduin Confluence, discard every ally.
        else if (card.getBlueprintId().equals("1_353")) {
            return new BotSiteCard(card) {
                @Override
                public List<BotAbility> getAbilities() {
                    return List.of(
                            AbilityBuilder
                                    .triggered(MOVES_TO_THIS_SITE)
                                    .effect(discardFromPlay(ALL))
                                    .target(cardType(CardType.ALLY))
                                    .build()
                    );
                }
            };
        }
        // 1_354
        // 1_355 Silverlode Banks - <b>River</b>. When the fellowship moves to Silverlode Banks without a ranger, every companion must exert.
        else if (card.getBlueprintId().equals("1_355")) {
            return new BotSiteCard(card) {
                @Override
                public List<BotAbility> getAbilities() {
                    return List.of(
                            AbilityBuilder
                                    .triggered(MOVES_TO_THIS_SITE)
                                    .optional(false)
                                    .condition(cannotSpot(keyword(Keyword.RANGER)))
                                    .effect(exert(1, ALL))
                                    .target(cardType(CardType.COMPANION))
                                    .build()
                    );
                }
            };
        }
        // 1_356
        // 1_357
        // 1_358 Pillars of the Kings - <b>River</b>. <b>Fellowship:</b> Discard a [gondor] card from hand to heal a [gondor] companion.
        else if (card.getBlueprintId().equals("1_358")) {
            return new BotSiteCard(card) {
                @Override
                public List<BotAbility> getAbilities() {
                    return List.of(
                            AbilityBuilder
                                    .activated()
                                    .condition(phaseIs(Phase.FELLOWSHIP))
                                    .cost(discardFromHand(culture(Culture.GONDOR)))
                                    .effect(heal())
                                    .target(and(
                                            culture(Culture.GONDOR),
                                            cardType(CardType.COMPANION)
                                    ))
                                    .build()
                    );
                }

                @Override
                public List<ActivatedAbility> getActivatedAbilities() {
                    return List.of(
                            new ActivatedAbilityBuilder()
                                    .phase(Phase.FELLOWSHIP)
                                    .cost(Cost.discardFromHand(Target.culture(Culture.GONDOR)))
                                    .effect(Effect.heal(Target.culture(Culture.GONDOR).and(Target.cardType(CardType.COMPANION))))
                                    .build()
                    );
                }
            };
        }
        // 1_359 Shores of Nen Hithoel - <b>River</b>. <b>Shadow:</b> Spot 5 Orc minions to prevent the fellowship from moving again this turn.
        else if (card.getBlueprintId().equals("1_359")) {
            return new BotSiteCard(card) {
                @Override
                public List<BotAbility> getAbilities() {
                    return List.of(
                            AbilityBuilder
                                    .activated()
                                    .condition(phaseIs(Phase.SHADOW))
                                    .condition(spot(race(Race.ORC), 5))
                                    .effect(modifyMoveLimit(Integer.MIN_VALUE))
                                    .build()
                    );
                }
            };
        }
        // 1_360 Emyn Muil - <b>Maneuver:</b> Exert your minion to make that minion <b>fierce</b> until the regroup phase.
        else if (card.getBlueprintId().equals("1_360")) {
            return new BotSiteCard(card) {
                @Override
                public List<BotAbility> getAbilities() {
                    return List.of(
                            AbilityBuilder
                                    .activated()
                                    .condition(phaseIs(Phase.MANEUVER))
                                    .cost(exertTarget())
                                    .effect(addKeywordUntilRegroup(Keyword.FIERCE))
                                    .target(cardType(CardType.MINION))
                                    .build());
                }
            };
        }
        // 1_361 Slopes of Amon Hen - The twilight cost of each [isengard] minion is -1.
        else if (card.getBlueprintId().equals("1_361")) {
            return new BotSiteCard(card) {
                @Override
                public List<BotAbility> getAbilities() {
                    return List.of(
                            AbilityBuilder
                                    .continuous()
                                    .effect(modifyTwilightCost(-1))
                                    .target(and(
                                            culture(Culture.ISENGARD),
                                            cardType(CardType.MINION)
                                    ))
                                    .build()
                    );
                }
            };
        }
        // 1_362
        // 1_363
        // 1_364 Gandalf, The Grey Wizard - <b>Fellowship:</b> Exert Gandalf to play a companion who has the Gandalf signet. The twilight cost of that companion is -2.
        else if (card.getBlueprintId().equals("1_364")) {
            return new BotCompanionCard(card) {
                @Override
                public List<BotAbility> getAbilities() {
                    return List.of(
                            AbilityBuilder
                                    .activated()
                                    .condition(phaseIs(Phase.FELLOWSHIP))
                                    .cost(exertSelf())
                                    .effect(playWithTwilightCostModification(-2))
                                    .target(and(
                                            cardType(CardType.COMPANION),
                                            signet(Signet.GANDALF)
                                    ))
                                    .build()
                    );
                }

                @Override
                public List<ActivatedAbility> getActivatedAbilities() {
                    return List.of(
                            new ActivatedAbilityBuilder()
                                    .phase(Phase.FELLOWSHIP)
                                    .cost(Cost.exertSelf(this))
                                    .effect(Effect.playWithBonusTwilightModification(Target.cardType(CardType.COMPANION).and(Target.signet(Signet.GANDALF)), -2))
                                    .build()
                    );
                }
            };
        }
        // 1_365 Aragorn, King in Exile - <b>Ranger</b>.<br>At the start of each of your turns, you may heal another companion who has the Aragorn signet.
        else if (card.getBlueprintId().equals("1_365")) {
            return new BotCompanionCard(card) {
                @Override
                public List<BotAbility> getAbilities() {
                    return List.of(
                            AbilityBuilder
                                    .triggered(START_OF_TURN)
                                    .optional(true)
                                    .effect(heal())
                                    .target(and(and(
                                            cardType(CardType.COMPANION),
                                            signet(Signet.ARAGORN)),
                                            not(self(self)))
                                    )
                                    .build()
                    );
                }

                @Override
                public TriggeredAbility getTriggeredAbility() {
                    return new TriggeredAbilityBuilder()
                            .trigger(Trigger.START_OF_TURN)
                            .optional(true)
                            .effect(Effect.heal(
                                    Target.cardType(CardType.COMPANION).and(
                                            Target.signet(Signet.ARAGORN)).and(
                                            Target.not(Target.self(this)))
                            ))
                            .build();
                }
            };
        }
        throw new IllegalArgumentException("Card is not supported: " + card.getBlueprint().getFullName());
    }
}
