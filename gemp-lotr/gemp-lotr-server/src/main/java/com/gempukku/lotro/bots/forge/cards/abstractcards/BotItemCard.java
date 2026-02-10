package com.gempukku.lotro.bots.forge.cards.abstractcards;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;
import com.gempukku.lotro.logic.timing.RuleUtils;

import java.util.Comparator;
import java.util.List;
import java.util.function.ToIntFunction;

public abstract class BotItemCard extends BotCard {
    public BotItemCard(PhysicalCard physicalCard) {
        super(physicalCard);
    }

    public BotCard chooseTargetToAttachTo(DefaultLotroGame game, List<BotCard> possibleTargets) {
        return AttachmentStrategy.chooseTarget(game, possibleTargets, getAttachmentStrategy());
    }

    public abstract boolean canBearThis(DefaultLotroGame game, BotCard target);

    public abstract boolean playsToSupportArea();

    protected abstract AttachmentStrategy getAttachmentStrategy();

    protected enum AttachmentStrategy {
        COMPANION_NOT_DYING("Companion least likely to die"),
        COMPANION_HIGH_STRENGTH("Companion with the highest strength"),
        COMPANION_LOW_STRENGTH("Companion with the lowest strength"),
        BASIC_SHADOW_WEAPON_TARGETING("Basic shadow weapon targeting");


        private final String humanReadable;

        AttachmentStrategy(String humanReadable) {
            this.humanReadable = humanReadable;
        }

        protected static BotCard chooseTarget(DefaultLotroGame game, List<BotCard> possibleTargets, AttachmentStrategy strategy) {
            return switch (strategy) {
                case COMPANION_NOT_DYING -> getCompanionLeastLikelyToDie(game, possibleTargets);
                case COMPANION_HIGH_STRENGTH -> getCompanionWithHighestStrength(game, possibleTargets);
                case COMPANION_LOW_STRENGTH -> getCompanionWithLowestStrength(game, possibleTargets);
                case BASIC_SHADOW_WEAPON_TARGETING -> getBasicShadowWeaponTarget(game, possibleTargets);
                default -> throw new IllegalArgumentException("Unknown attachment strategy: " + strategy);
            };
        }

        private static BotCard getBasicShadowWeaponTarget(DefaultLotroGame game, List<BotCard> possibleTargets) {
            int minionInPlay = Math.toIntExact(game.getGameState().getActiveCards().stream().filter(physicalCard -> CardType.MINION == physicalCard.getBlueprint().getCardType()).count());

            int companionsInPlay = Math.toIntExact(game.getGameState().getActiveCards().stream().filter(physicalCard -> CardType.COMPANION == physicalCard.getBlueprint().getCardType()).count());
            int alliesInPlay = Math.toIntExact(game.getGameState().getActiveCards().stream()
                    .filter(physicalCard ->
                            CardType.ALLY == physicalCard.getBlueprint().getCardType()
                                    && RuleUtils.isAllyAtHome(physicalCard, game.getGameState().getCurrentSiteNumber(), game.getGameState().getCurrentSiteBlock()))
                    .count());
            int fpThatCanSkirmish = companionsInPlay + alliesInPlay;

            boolean swarm = minionInPlay > fpThatCanSkirmish;

            if (swarm) {
                return getCompanionWithLowestStrength(game, possibleTargets);
            } else {
                return getCompanionWithHighestStrength(game, possibleTargets);
            }
        }

        private static BotCard getCompanionWithLowestStrength(DefaultLotroGame game, List<BotCard> possibleTargets) {

            return possibleTargets.stream()
                    .max(Comparator
                            .comparingInt((ToIntFunction<BotCard>) card -> card.getPhysicalCard().getBlueprint().getCardType() == CardType.COMPANION ? 1 : 0)
                            .thenComparingInt(card -> - game.getModifiersQuerying().getStrength(game, card.getPhysicalCard()))
                            .thenComparingInt(card -> game.getModifiersQuerying().getVitality(game, card.getPhysicalCard())))
                    .orElseThrow();
        }

        private static BotCard getCompanionWithHighestStrength(DefaultLotroGame game, List<BotCard> possibleTargets) {
            return possibleTargets.stream()
                    .max(Comparator
                            .comparingInt((ToIntFunction<BotCard>) card -> card.getPhysicalCard().getBlueprint().getCardType() == CardType.COMPANION ? 1 : 0)
                            .thenComparingInt(card -> game.getModifiersQuerying().getStrength(game, card.getPhysicalCard()))
                            .thenComparingInt(card -> game.getModifiersQuerying().getVitality(game, card.getPhysicalCard())))
                    .orElseThrow();
        }

        private static BotCard getCompanionLeastLikelyToDie(DefaultLotroGame game, List<BotCard> possibleTargets) {
            return possibleTargets.stream()
                    .max(Comparator
                            .comparingInt((ToIntFunction<BotCard>) card -> card.getPhysicalCard().getBlueprint().getCardType() == CardType.COMPANION ? 1 : 0)
                            .thenComparingInt(card -> game.getGameState().getRingBearers().contains(card.getPhysicalCard()) ? 1 : 0)
                            .thenComparingInt(card -> game.getModifiersQuerying().getVitality(game, card.getPhysicalCard()) > 1 ? 1 : 0)
                            .thenComparingInt(card -> game.getModifiersQuerying().getStrength(game, card.getPhysicalCard()))
                            .thenComparingInt(card -> game.getModifiersQuerying().getVitality(game, card.getPhysicalCard())))
                    .orElseThrow();
        }
    }

}
