package com.gempukku.lotro.bots.forge.cards;

import com.gempukku.lotro.bots.forge.cards.ability2.*;
import com.gempukku.lotro.bots.forge.cards.ability2.ActivatedAbility;
import com.gempukku.lotro.bots.forge.cards.ability2.Target;
import com.gempukku.lotro.bots.forge.cards.ability2.TriggeredAbility;
import com.gempukku.lotro.bots.forge.cards.ability2.cost.Cost;
import com.gempukku.lotro.bots.forge.cards.ability2.effect.Effect;
import com.gempukku.lotro.bots.forge.cards.abstractcard.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

import java.util.List;
import java.util.function.Predicate;

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
        // 1_2 The Ruling Ring - <b>Response:</b> If bearer is about to take a wound in a skirmish, he wears The One Ring until the regroup phase.<br>While wearing The One Ring, each time the Ring-bearer is about to take a wound during a skirmish, add a burden instead.
        if (card.getBlueprintId().equals("1_2")) {
            return new BotOneRingCard(card) {
                @Override
                public TriggeredAbility getTriggeredAbility() {
                    return new TriggeredAbilityBuilder()
                            .optional(true)
                            .effect(Effect.wearRing())
                            // Use when RB is exhausted and does not have 1 resistance left
                            .goodToUseFunction((player, game) ->
                                    game.getModifiersQuerying().getVitality(game, game.getGameState().getRingBearer(player)) == 1
                                            && game.getModifiersQuerying().getResistance(game, game.getGameState().getRingBearer(player)) > 1)
                            .build();
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
                public List<ActivatedAbility> getActivatedAbilities() {
                    return List.of(
                            new ActivatedAbilityBuilder()
                                    .phase(Phase.FELLOWSHIP)
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
                // Nothing to add, abilities are not used, they just 'work'
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
                public List<ActivatedAbility> getActivatedAbilities() {
                    return List.of(
                            new ActivatedAbilityBuilder()
                                    .phase(Phase.FELLOWSHIP)
                                    .cost(Cost.exertSelf(this.getSelf()))
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
                protected boolean canBearThis(DefaultLotroGame game, PhysicalCard target) {
                    return Race.MAN.equals(target.getBlueprint().getRace());
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
                public List<ActivatedAbility> getActivatedAbilities() {
                    return List.of(
                            new ActivatedAbilityBuilder()
                                    .phase(Phase.FELLOWSHIP)
                                    .cost(Cost.discardSelf(this.self))
                                    .effect(Effect.heal(CardType.COMPANION))
                                    .build()
                            //TODO the other ability
//                            new ActivatedAbilityBuilder()
//                                    .phase(Phase.FELLOWSHIP)
//                                    .cost(Cost.discardSelf(this.self))
//                                    .effect(Effect.discardFromPlay(Target.and(Target.and(
//                                                    Target.side(Side.SHADOW),
//                                                    Target.cardType(CardType.CONDITION)),
//                                            Target.attachedTo(CardType.COMPANION))))
//                                    .build()
                    );
                }

                @Override
                protected boolean canBearThis(DefaultLotroGame game, PhysicalCard target) {
                    return Culture.GONDOR.equals(target.getBlueprint().getCulture())
                            && Race.MAN.equals(target.getBlueprint().getRace());
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
                public List<ActivatedAbility> getActivatedAbilities() {
                    return List.of(
                            new ActivatedAbilityBuilder()
                                    .phase(Phase.SKIRMISH)
                                    .cost(Cost.exertSelf(this.self))
                                    .effect(Effect.modifyStrength(Race.HOBBIT, 3))
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
                protected boolean canBearThis(DefaultLotroGame game, PhysicalCard target) {
                    return Race.MAN.equals(target.getBlueprint().getRace());
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
                public EventAbility getEventAbility() {
                    throw new IllegalStateException("Regroup events not yet implemented");
                }
            };
        }
        // 1_107 Great Shield - Bearer must be a Man.<br>The minion archery total is -1.
        else if (card.getBlueprintId().equals("1_107")) {
            return new BotObjectAttachableCard(card) {
                @Override
                protected boolean canBearThis(DefaultLotroGame game, PhysicalCard target) {
                    return Race.MAN.equals(target.getBlueprint().getRace());
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
                protected boolean canBearThis(DefaultLotroGame game, PhysicalCard target) {
                    return game.getModifiersQuerying().hasKeyword(game, target, Keyword.RANGER)
                            && game.getGameState().getAttachedCards(target).stream().noneMatch(card1 -> card1.getBlueprint().getTitle().equals(self.getBlueprint().getTitle()));
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
                public EventAbility getEventAbility() {
                    throw new IllegalStateException("Skirmish events not yet implemented");
                }
            };
        }
        // 1_117 Swordsman of the Northern Kingdom - Skirmish: Make a ranger strength +2 (or +4 when skirmishing a roaming minion).
        else if (card.getBlueprintId().equals("1_117")) {
            return new BotEventCard(card) {
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

            };
        }
        // 1_142
        // 1_143
        // 1_144
        // 1_145 Uruk Brood - <b>Damage +1</b>.<br><b>Skirmish:</b> Remove (2) to make this minion strength +1 for each other Uruk-hai you spot.
        else if (card.getBlueprintId().equals("1_145")) {
            return new BotMinionCard(card) {

            };
        }
        // 1_146
        // 1_147
        // 1_148
        // 1_149
        // 1_150 Uruk Rager - <b>Damage +1</b>.<br>Each time this minion wins a skirmish, the Free Peoples player must discard the top 2 cards of his or her draw deck.
        else if (card.getBlueprintId().equals("1_150")) {
            return new BotMinionCard(card) {

            };
        }
        // 1_151 Uruk Savage - <b>Damage +1</b>. <helper>(When this minion wins a skirmish, add 1 extra wound to the defender.)</helper>
        else if (card.getBlueprintId().equals("1_151")) {
            return new BotMinionCard(card) {

            };
        }
        // 1_152 Uruk Shaman - <b>Damage +1</b>.<br><b>Maneuver:</b> Remove (2) to heal an Uruk-hai.
        else if (card.getBlueprintId().equals("1_152")) {
            return new BotMinionCard(card) {

            };
        }
        // 1_153 Uruk Slayer - <b>Damage +1</b>.<br><b>Skirmish:</b> Remove (1) to make this minion strength +1 (limit +3).
        else if (card.getBlueprintId().equals("1_153")) {
            return new BotMinionCard(card) {

            };
        }
        // 1_154 Uruk Soldier - <b>Damage +1</b>.<br>When you play this minion, you may make the Free Peoples player discard the top card of his draw deck.
        else if (card.getBlueprintId().equals("1_154")) {
            return new BotMinionCard(card) {
                @Override
                public TriggeredAbility getTriggeredAbility() {
                    return new TriggeredAbilityBuilder()
                            .optional(true)
                            .effect(Effect.millOpponent(1))
                            .goodToUseFunction((s, game) -> true) // Always good to trigger
                            .build();
                }
            };
        }
        // 1_155
        // 1_156
        // 1_157 Uruk-hai Armory - Plays to your support area.<br>While you can spot an Uruk-hai, the fellowship archery total is -1.
        else if (card.getBlueprintId().equals("1_157")) {
            return new BotObjectSupportAreaCard(card) {

            };
        }
        // 1_158 Uruk-hai Raiding Party - <b>Damage +1</b>. <helper>(When this minion wins a skirmish, add 1 extra wound to the defender.)</helper>
        else if (card.getBlueprintId().equals("1_158")) {
            return new BotMinionCard(card) {

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

            };
        }
        // 1_177 Goblin Patrol Troop
        else if (card.getBlueprintId().equals("1_177")) {
            return new BotMinionCard(card) {

            };
        }
        // 1_178 Goblin Runner - When you play this minion, you may add (2).
        else if (card.getBlueprintId().equals("1_178")) {
            return new BotMinionCard(card) {
                @Override
                public TriggeredAbility getTriggeredAbility() {
                    return new TriggeredAbilityBuilder()
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
                public TriggeredAbility getTriggeredAbility() {
                    return new TriggeredAbilityBuilder()
                            .optional(true)
                            .effect(Effect.playPossessionFromDiscardOn(
                                    Target.possessionClass(PossessionClass.HAND_WEAPON).or(Target.possessionClass(PossessionClass.RANGED_WEAPON)),
                                    Target.culture(Culture.MORIA).and(Target.race(Race.ORC))))
                            .goodToUseFunction((s, game) -> true) // Always good to trigger
                            .build();
                }
            };
        }
        // 1_180 Goblin Scimitar - Bearer must be a [moria] Orc.<br>When you play this possession, you may draw a card.
        else if (card.getBlueprintId().equals("1_180")) {
            return new BotObjectAttachableCard(card) {
                @Override
                protected boolean canBearThis(DefaultLotroGame game, PhysicalCard target) {
                    return Culture.MORIA.equals(target.getBlueprint().getCulture())
                            && Race.ORC.equals(target.getBlueprint().getRace());
                }

                @Override
                protected BotTargetingMode getAttachTargetingMode() {
                    return BotTargetingMode.BASIC_SHADOW_WEAPON_TARGETING;
                }

                @Override
                public TriggeredAbility getTriggeredAbility() {
                    return new TriggeredAbilityBuilder()
                            .optional(true)
                            .effect(Effect.drawCard(1))
                            .goodToUseFunction((s, game) -> true) // Always good to trigger
                            .build();
                }
            };

        }
        // 1_181 Goblin Sneak - When you play this minion, you may place a [moria] Orc from your discard pile beneath your draw deck.
        else if (card.getBlueprintId().equals("1_181")) {
            return new BotMinionCard(card) {
                @Override
                public TriggeredAbility getTriggeredAbility() {
                    return new TriggeredAbilityBuilder()
                            .optional(true)
                            .effect(Effect.putFromDiscardToBottomOfDeck(Target.culture(Culture.MORIA).and(Target.race(Race.ORC))))
                            .goodToUseFunction((player, game) -> {
                                int moriaOrcsInDiscard = (int) game.getGameState().getDiscard(player).stream()
                                        .filter(card2 -> card2.getBlueprint().getCulture() == Culture.MORIA
                                                && card2.getBlueprint().getRace() == Race.ORC)
                                        .count();
                                return moriaOrcsInDiscard > 2; // Keep orcs in discard for Host of Thousands
                            })
                            .build();
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
                public EventAbility getEventAbility() {
                    return new EventAbilityBuilder()
                            .effect(Effect.playMinionFromDiscard(Target.culture(Culture.MORIA).and(Target.race(Race.ORC))))
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
                public TriggeredAbility getTriggeredAbility() {
                    return new TriggeredAbilityBuilder()
                            .optional(false) // json card implementation sets this as non-optional trigger even though the text says spot, which is optional
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
                public List<ActivatedAbility> getActivatedAbilities() {
                    return List.of(
                            new ActivatedAbilityBuilder()
                                    .phase(Phase.SHADOW)
                                    .cost(Cost.discardFromHand(3))
                                    .effect(Effect.playMinionFromDiscard(Target.culture(Culture.MORIA).and(Target.race(Race.ORC))))
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

            };
        }
        // 1_287
        // 1_288
        // 1_289
        // 1_290 Frodo, Son of Drogo - <b>Ring-bearer (resistance 10).</b><br><b>Fellowship:</b> Exert another companion who has the Frodo signet to heal Frodo.
        else if (card.getBlueprintId().equals("1_290")) {
            return new BotCompanionCard(card) {
                @Override
                public List<ActivatedAbility> getActivatedAbilities() {
                    return List.of(
                            new ActivatedAbilityBuilder()
                                    .phase(Phase.FELLOWSHIP)
                                    .cost(Cost.exert(Target.and(Target.and(
                                                    Target.cardType(CardType.COMPANION),
                                                    Target.signet(Signet.FRODO)),
                                            Target.not(Target.self(this.getSelf()))
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
                protected boolean canBearThis(DefaultLotroGame game, PhysicalCard target) {
                    return Race.HOBBIT.equals(target.getBlueprint().getRace());
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
                public List<ActivatedAbility> getActivatedAbilities() {
                    return List.of(
                            new ActivatedAbilityBuilder()
                                    .phase(Phase.FELLOWSHIP)
                                    .cost(Cost.exertSelf(this.getSelf()))
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
                public List<ActivatedAbility> getActivatedAbilities() {
                    return List.of(
                            new ActivatedAbilityBuilder()
                                    .phase(Phase.FELLOWSHIP)
                                    .cost(Cost.exertSelf(this.getSelf()))
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

            };
        }
        // 1_328
        // 1_329
        // 1_330
        // 1_331 Ettenmoors - <b>Plains</b>. <b>Skirmish:</b> Exert your companion or minion to make that character strength +2.
        else if (card.getBlueprintId().equals("1_331")) {
            return new BotSiteCard(card) {

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

            };
        }
        // 1_338
        // 1_339
        // 1_340 Rivendell Terrace - <b>Sanctuary</b>. <b>Fellowship:</b> Play a Man to draw a card.
        else if (card.getBlueprintId().equals("1_340")) {
            return new BotSiteCard(card) {
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
                public List<ActivatedAbility> getActivatedAbilities() {
                    return List.of(
                            new ActivatedAbilityBuilder()
                                    .phase(Phase.SHADOW)
                                    .cost(Cost.removeTwilight(1))
                                    .effect(Effect.playPossessionFromDiscardOn(
                                            Target.side(Side.SHADOW).and(Target.possessionClass(PossessionClass.HAND_WEAPON).or(Target.possessionClass(PossessionClass.RANGED_WEAPON))),
                                            botCard -> true
                                    ))
                                    .build()
                    );
                }
            };
        }
        // 1_346 Moria Lake - <b>Marsh</b>. When the fellowship moves to Moria Lake, Frodo or 2 other companions must exert.
        else if (card.getBlueprintId().equals("1_346")) {
            return new BotSiteCard(card) {

            };
        }
        // 1_347
        // 1_348
        // 1_349 The Bridge of Khazad-dûm - <b>Underground</b>. <b>Shadow:</b> Play The Balrog from your draw deck or hand; The Balrog's twilight cost is -6.
        else if (card.getBlueprintId().equals("1_349")) {
            return new BotSiteCard(card) {

            };
        }
        // 1_350 Dimrill Dale - <b>Sanctuary</b>. The twilight cost of the first [moria] Orc played each Shadow phase is -2.
        else if (card.getBlueprintId().equals("1_350")) {
            return new BotSiteCard(card) {

            };
        }
        // 1_351 Galadriel's Glade - <b>Sanctuary</b>. <b>Fellowship:</b> Exert an Elf to look at an opponent's hand.
        else if (card.getBlueprintId().equals("1_351")) {
            return new BotSiteCard(card) {
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

            };
        }
        // 1_354
        // 1_355 Silverlode Banks - <b>River</b>. When the fellowship moves to Silverlode Banks without a ranger, every companion must exert.
        else if (card.getBlueprintId().equals("1_355")) {
            return new BotSiteCard(card) {

            };
        }
        // 1_356
        // 1_357
        // 1_358 Pillars of the Kings - <b>River</b>. <b>Fellowship:</b> Discard a [gondor] card from hand to heal a [gondor] companion.
        else if (card.getBlueprintId().equals("1_358")) {
            return new BotSiteCard(card) {
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
                public List<ActivatedAbility> getActivatedAbilities() {
                    return List.of(
                            new ActivatedAbilityBuilder()
                                    .phase(Phase.SHADOW)
                                    .effect(Effect.preventFellowshipFromMoving())
                                    .build()
                    );
                }
            };
        }
        // 1_360 Emyn Muil - <b>Maneuver:</b> Exert your minion to make that minion <b>fierce</b> until the regroup phase.
        else if (card.getBlueprintId().equals("1_360")) {
            return new BotSiteCard(card) {

            };
        }
        // 1_361 Slopes of Amon Hen - The twilight cost of each [isengard] minion is -1.
        else if (card.getBlueprintId().equals("1_361")) {
            return new BotSiteCard(card) {

            };
        }
        // 1_362
        // 1_363
        // 1_364 Gandalf, The Grey Wizard - <b>Fellowship:</b> Exert Gandalf to play a companion who has the Gandalf signet. The twilight cost of that companion is -2.
        else if (card.getBlueprintId().equals("1_364")) {
            return new BotCompanionCard(card) {
                @Override
                public List<ActivatedAbility> getActivatedAbilities() {
                    return List.of(
                            new ActivatedAbilityBuilder()
                                    .phase(Phase.FELLOWSHIP)
                                    .cost(Cost.exertSelf(this.getSelf()))
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
                public TriggeredAbility getTriggeredAbility() {
                    return new TriggeredAbilityBuilder()
                            .optional(true)
                            .effect(Effect.heal(
                                    Target.cardType(CardType.COMPANION).and(
                                            Target.signet(Signet.ARAGORN)).and(
                                            Target.not(Target.self(this.getSelf())))
                            ))
                            // Use when something can be healed
                            .goodToUseFunction((player, game) -> game.getGameState().getInPlay().stream().anyMatch((Predicate<PhysicalCard>) physicalCard ->
                                    physicalCard.getOwner().equals(player)
                                    && CardType.COMPANION == physicalCard.getBlueprint().getCardType()
                                    && Signet.ARAGORN == physicalCard.getBlueprint().getSignet()
                                    && game.getGameState().getWounds(physicalCard) > 0
                                    && !physicalCard.getBlueprint().getFullName().equals("Aragorn, King in Exile")))
                            .build();
                }
            };
        }
        throw new IllegalArgumentException("Card is not supported: " + card.getBlueprint().getFullName());
    }
}
