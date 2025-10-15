package com.gempukku.lotro.game.state;

import com.gempukku.lotro.cards.build.bot.BotCardFactory;
import com.gempukku.lotro.cards.build.bot.BotTargetingMode;
import com.gempukku.lotro.cards.build.bot.ability.AbilityProperty;
import com.gempukku.lotro.cards.build.bot.ability.ActivatedAbility;
import com.gempukku.lotro.cards.build.bot.ability.BotAbility;
import com.gempukku.lotro.cards.build.bot.ability.TriggeredAbility;
import com.gempukku.lotro.cards.build.bot.abstractcard.BotCard;
import com.gempukku.lotro.cards.build.bot.abstractcard.BotCompanionCard;
import com.gempukku.lotro.cards.build.bot.abstractcard.BotEventCard;
import com.gempukku.lotro.cards.build.bot.abstractcard.BotObjectAttachableCard;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.PhysicalCard;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class PlannedBoardState {
    private Phase phase;
    private String currentPlayer;
    private final List<String> players = new ArrayList<>();
    private final Map<String, BotCard> ringBearers = new HashMap<>();

    private final Map<String, List<BotCard>> adventureDecks = new HashMap<>();
    private final Map<String, List<BotCard>> decks = new HashMap<>();
    private final Map<String, List<BotCard>> hands = new HashMap<>();
    private final Map<String, List<BotCard>> discards = new HashMap<>();
    private final Map<String, List<BotCard>> deadPiles = new HashMap<>();

    private final Map<String, List<BotCard>> inPlayFpCards = new HashMap<>();
    private final Map<String, List<BotCard>> inPlayShadowCards = new HashMap<>();
    private final Set<BotCard> inPlaySites = new HashSet<>();

    private final Map<BotCard, Set<BotCard>> attachedCards = new HashMap<>();
    private final Map<BotCard, Map<Token, Integer>> cardTokens = new HashMap<>();

    private int twilight;

    private final Map<String, Integer> playerPosition = new HashMap<>();
    private final Map<String, Integer> playerThreats = new HashMap<>();

    public PlannedBoardState(LotroGame game) {
        phase = game.getGameState().getCurrentPhase();
        currentPlayer = game.getGameState().getCurrentPlayerId();

        // Player names
        players.addAll(game.getGameState().getPlayerOrder().getAllPlayers());

        // Other zones
        players.forEach(player -> {
            inPlayFpCards.put(player, new ArrayList<>());
            inPlayShadowCards.put(player, new ArrayList<>());
            adventureDecks.put(player, new ArrayList<>());
            game.getGameState().getAdventureDeck(player).forEach(siteCard -> adventureDecks.get(player).add(BotCardFactory.create(siteCard)));
            decks.put(player, new ArrayList<>());
            game.getGameState().getDeck(player).forEach(cardInDeck -> decks.get(player).add(BotCardFactory.create(cardInDeck)));
            hands.put(player, new ArrayList<>());
            game.getGameState().getHand(player).forEach(cardInHand -> hands.get(player).add(BotCardFactory.create(cardInHand)));
            discards.put(player, new ArrayList<>());
            game.getGameState().getDiscard(player).forEach(cardInDiscard -> discards.get(player).add(BotCardFactory.create(cardInDiscard)));
            deadPiles.put(player, new ArrayList<>());
            game.getGameState().getDeadPile(player).forEach(deadCard -> deadPiles.get(player).add(BotCardFactory.create(deadCard)));
        });

        // Cards in play
        game.getGameState().getInPlay()
                .forEach((Consumer<PhysicalCard>) card -> {
                    BotCard botCard = BotCardFactory.create(card);
                    if (CardType.SITE.equals(botCard.getSelf().getBlueprint().getCardType())) {
                        inPlaySites.add(botCard);
                    } else if (Side.FREE_PEOPLE.equals(botCard.getSelf().getBlueprint().getSide())
                            || CardType.THE_ONE_RING.equals(botCard.getSelf().getBlueprint().getCardType())) {
                        inPlayFpCards.computeIfAbsent(botCard.getSelf().getOwner(), s -> new ArrayList<>()).add(botCard);
                        if (game.getGameState().getRingBearers().contains(card)) {
                            ringBearers.put(botCard.getSelf().getOwner(), botCard);
                        }
                    } else if (Side.SHADOW.equals(botCard.getSelf().getBlueprint().getSide())) {
                        inPlayShadowCards.computeIfAbsent(botCard.getSelf().getOwner(), s -> new ArrayList<>()).add(botCard);
                    } else {
                        throw new IllegalStateException("Do not know what to do with card in play: " + botCard.getSelf().getBlueprintId() + " - " + botCard.getSelf().getBlueprint().getFullName());
                    }
                });

        // Attach cards
        Stream.of(inPlayFpCards.values(), inPlayShadowCards.values()).forEach(lists -> lists.forEach(botCards -> botCards.forEach(botCard -> {
            PhysicalCard attachedTo = botCard.getSelf().getAttachedTo();
            if (attachedTo != null) {
                BotCard bearer = findBearer(attachedTo);
                if (bearer != null) {
                    attachedCards.computeIfAbsent(bearer, k -> new HashSet<>()).add(botCard);
                }
            }
        })));

        // Wounds and burdens
        Stream.of(inPlayFpCards.values(), inPlayShadowCards.values()).forEach(lists -> lists.forEach(botCards -> botCards.forEach(botCard -> {
            cardTokens.put(botCard, new HashMap<>());
            int wounds = game.getGameState().getTokenCount(botCard.getSelf(), Token.WOUND);
            int burdens = game.getGameState().getTokenCount(botCard.getSelf(), Token.BURDEN);
            if (wounds != 0) {
                cardTokens.get(botCard).put(Token.WOUND, wounds);
            }
            if (burdens != 0) {
                cardTokens.get(botCard).put(Token.BURDEN, burdens);
            }
        })));

        // Stats
        twilight = game.getGameState().getTwilightPool();
        players.forEach(player -> {
            playerPosition.put(player, game.getGameState().getPlayerPosition(player));
            playerThreats.put(player, game.getGameState().getPlayerThreats(player));
        });
    }

    /*
        ALTER BOARD STATE
     */
    public void removeBurden(int amount) {
        BotCard ringBearer = ringBearers.get(currentPlayer);
        int burdensPlaced = getTokenCount(ringBearer, Token.BURDEN);
        int toBeRemoved = Math.min(amount, burdensPlaced);
        if (toBeRemoved > 0) {
            cardTokens.get(ringBearer).put(Token.BURDEN, burdensPlaced - toBeRemoved);
        }
    }

    public void exert(BotCard botCard) {
        exert(botCard, 1);
    }

    public void exert(BotCard botCard, int amount) {
        int realAmount = Math.min(amount, getVitality(botCard) - 1);

        if (cardTokens.get(botCard).containsKey(Token.WOUND)) {
            cardTokens.get(botCard).put(Token.WOUND, cardTokens.get(botCard).get(Token.WOUND) + realAmount);
        } else {
            cardTokens.get(botCard).put(Token.WOUND, realAmount);
        }
    }

    public void discardFromPlay(BotCard botCard) {
        String owner = botCard.getSelf().getOwner();
        boolean isFpCard = botCard.getSelf().getBlueprint().getSide().equals(Side.FREE_PEOPLE);

        if (isFpCard ? !inPlayFpCards.get(owner).contains(botCard) : !inPlayShadowCards.get(owner).contains(botCard)) {
            throw new IllegalStateException("Card " + botCard.getSelf().getBlueprint().getFullName() + " not in play");
        }

        if (isFpCard) {
            inPlayFpCards.get(botCard.getSelf().getOwner()).remove(botCard);
        } else {
            inPlayShadowCards.get(botCard.getSelf().getOwner()).remove(botCard);
        }
        discards.get(owner).add(botCard);
    }

    public void playFellowshipsNextSite(String ownerOfEffect) {
        int current = getCurrentPlayerPosition();
        int next = current + 1;

        if (next > 9) {
            // no next site
            return;
        }

        BotCard nextSite = getSitesInPlay().stream().filter(botCard -> botCard.getSelf().getBlueprint().getSiteNumber() == next).findFirst().orElse(null);
        BotCard nextSiteInAdventureDeck = getAdventureDeck(ownerOfEffect).stream().filter(botCard -> botCard.getSelf().getBlueprint().getSiteNumber() == next).findFirst().orElse(null);

        if (nextSiteInAdventureDeck != null) {
            inPlaySites.remove(nextSite);
            inPlaySites.add(nextSiteInAdventureDeck);
            adventureDecks.get(nextSite.getSelf().getOwner()).add(nextSite);
        }
    }

    private void playFpCard(BotCard botCard) {
        inPlayFpCards.get(botCard.getSelf().getOwner()).add(botCard);
        hands.get(botCard.getSelf().getOwner()).remove(botCard);
        twilight += botCard.getSelf().getBlueprint().getTwilightCost();
        cardTokens.put(botCard, new HashMap<>());
    }

    private void playShadowCard(BotCard botCard) {
        if (twilight < botCard.getSelf().getBlueprint().getTwilightCost()) {
            throw new IllegalStateException("Cannot pay twilight for event " + botCard.getSelf().getBlueprint().getFullName());
        }
        inPlayShadowCards.get(botCard.getSelf().getOwner()).add(botCard);
        hands.get(botCard.getSelf().getOwner()).remove(botCard);
        twilight -= botCard.getSelf().getBlueprint().getTwilightCost();
        cardTokens.put(botCard, new HashMap<>());
    }

    public void playCompanion(BotCompanionCard botCard) {
        playFpCard(botCard);
    }

    public void playToFpSupportArea(BotCard botCard) {
        playFpCard(botCard);
    }

    public void playOnBearer(BotObjectAttachableCard botCard, BotCard bearer) {
        boolean fp = botCard.getSelf().getBlueprint().getSide().equals(Side.FREE_PEOPLE);
        if (fp) {
            playFpCard(botCard);
            attachedCards.computeIfAbsent(bearer, k -> new HashSet<>()).add(botCard);
        } else {
            playShadowCard(botCard);
            attachedCards.computeIfAbsent(bearer, k -> new HashSet<>()).add(botCard);
        }
    }

    public void playEvent(BotEventCard botCard) {
        if (botCard.getSelf().getBlueprint().getSide().equals(Side.FREE_PEOPLE)) {
            twilight += botCard.getSelf().getBlueprint().getTwilightCost();
        } else {
            if (twilight < botCard.getSelf().getBlueprint().getTwilightCost()) {
                throw new IllegalStateException("Cannot pay twilight for event " + botCard.getSelf().getBlueprint().getFullName());
            }
            twilight -= botCard.getSelf().getBlueprint().getTwilightCost();
        }

        botCard.getEventAbility().resolveAbility(botCard, this);

        hands.get(botCard.getSelf().getOwner()).remove(botCard);
        discards.get(botCard.getSelf().getOwner()).add(botCard);
    }

    public void useActivatedAbility(BotCard botCard, ActivatedAbility activatedAbility) {
        resolveAbilityProperty(botCard, activatedAbility.getEffect());
        for (AbilityProperty cost : activatedAbility.getCosts()) {
            resolveAbilityProperty(botCard, cost);
        }
    }

    private void resolveAbilityProperty(BotCard source, AbilityProperty abilityProperty) {
        String player = source.getSelf().getOwner();
        Side side = source.getSelf().getBlueprint().getSide();
        switch (abilityProperty.getType()) {
            case DISCARD_FROM_PLAY -> {
                List<BotCard> potentialTargets = getCardsEffectCanTarget(abilityProperty, player, side);
                int numberOfTargets = abilityProperty.getParam("numberOfTargets", Integer.class);
                if (numberOfTargets >= potentialTargets.size()) {
                    for (BotCard potentialTarget : potentialTargets) {
                        inPlayFpCards.forEach((s, botCards) -> botCards.remove(potentialTarget));
                        inPlayShadowCards.forEach((s, botCards) -> botCards.remove(potentialTarget));
                    }
                } else {
                    throw new IllegalStateException("Choosing targets for discarding not implemented");
                }
            }
            case EXERT -> {
                List<BotCard> potentialTargets = getCardsEffectCanTarget(abilityProperty, player, side);
                if (potentialTargets.size() > 1) {
                    throw new IllegalStateException("Cannot resolve exert action if target can be chosen");
                } else if (potentialTargets.size() == 1 ){
                    BotCard toBeExerted = potentialTargets.getFirst();
                    int amount = Math.min(abilityProperty.getParam("amount", Integer.class), getVitality(toBeExerted) - 1);
                    if (cardTokens.get(toBeExerted).containsKey(Token.WOUND)) {
                        cardTokens.get(toBeExerted).put(Token.WOUND, cardTokens.get(toBeExerted).get(Token.WOUND) + amount);
                    } else {
                        cardTokens.get(toBeExerted).put(Token.WOUND, amount);
                    }
                }
            }
            case EXERT_SELF -> {
                int amount = Math.min(abilityProperty.getParam("amount", Integer.class), getVitality(source) - 1);
                if (cardTokens.get(source).containsKey(Token.WOUND)) {
                    cardTokens.get(source).put(Token.WOUND, cardTokens.get(source).get(Token.WOUND) + amount);
                } else {
                    cardTokens.get(source).put(Token.WOUND, amount);
                }
            }
            case HEAL -> {
                List<BotCard> potentialTargets = getCardsEffectCanTarget(abilityProperty, player, side);
                if (!potentialTargets.isEmpty()) {
                    BotCard toBeHealed = BotTargetingMode.HEAL.chooseTarget(this, potentialTargets, false);
                    int amount = Math.min(abilityProperty.getParam("amount", Integer.class), getTokenCount(toBeHealed, Token.WOUND));
                    if (amount > 0) {
                        cardTokens.get(toBeHealed).put(Token.WOUND, cardTokens.get(toBeHealed).get(Token.WOUND) - amount);
                    }
                }
            }
            case REMOVE_BURDEN -> {
                int amount = abilityProperty.getParam("amount", Integer.class);
                BotCard ringBearer = side.equals(Side.FREE_PEOPLE) ? ringBearers.get(player) : ringBearers.get(getOpponent(player));
                int burdensPlaced = getTokenCount(ringBearer, Token.BURDEN);
                int toBeRemoved = Math.min(amount, burdensPlaced);
                if (toBeRemoved > 0) {
                    cardTokens.get(ringBearer).put(Token.BURDEN, burdensPlaced - toBeRemoved);
                }
            }
            case DISCARD_SELF -> {
                inPlayFpCards.get(source.getSelf().getOwner()).remove(source);
                inPlayShadowCards.get(source.getSelf().getOwner()).remove(source);
            }
            default -> throw new IllegalStateException("Cannot resolve ability property of " + abilityProperty.getType());
        }
    }

    public void healByDiscard(BotCard discardedCard) {
        AtomicBoolean healed = new AtomicBoolean(false);
        inPlayFpCards.values().forEach(lists -> lists.forEach(botCard -> {
            if (discardedCard.getSelf().getBlueprint().getTitle().equals(botCard.getSelf().getBlueprint().getTitle())
                    && discardedCard.getSelf().getOwner().equals(botCard.getSelf().getOwner())) {
                cardTokens.get(botCard).put(Token.WOUND, cardTokens.get(botCard).get(Token.WOUND) - 1);
                hands.get(botCard.getSelf().getOwner()).remove(discardedCard);
                healed.set(true);
            }
        }));
        if (!healed.get()) {
            throw new IllegalStateException("Could not find card to heal: " + discardedCard.getSelf().getBlueprint().getFullName());
        }
    }

    public boolean sameTitleInPlayOrInDeadPile(String title, String player) {
        AtomicBoolean sameTitleInPlay = new AtomicBoolean(false);
        Stream.of(inPlayFpCards.values(), inPlayShadowCards.values()).forEach(lists -> lists.forEach(botCards -> botCards.forEach(botCard -> {
            if (player.equals(botCard.getSelf().getOwner())) {
                boolean sameTitle = botCard.getSelf().getBlueprint().getTitle().equals(title);
                if (sameTitle) {
                    sameTitleInPlay.set(true);
                }
            }
        })));
        if (sameTitleInPlay.get()) {
            return true;
        }

        for (BotCard deadCard : deadPiles.get(player)) {
            if (deadCard.getSelf().getBlueprint().getTitle().equals(title)) {
                return true;
            }
        }

        return false;
    }

    /*
        GET INFO
     */
    public String getCurrentFpPlayer() {
        return currentPlayer;
    }

    public Phase getCurrentPhase() {
        return phase;
    }

    public int getCurrentPlayerPosition() {
        return playerPosition.get(currentPlayer);
    }

    public int ruleOfNineRemainder(String player) {
        int companionsInPlay = Math.toIntExact(inPlayFpCards.get(player).stream().filter(botCard -> CardType.COMPANION.equals(botCard.getSelf().getBlueprint().getCardType())).count());
        int companionsDead = Math.toIntExact(deadPiles.get(player).stream().filter(botCard -> CardType.COMPANION.equals(botCard.getSelf().getBlueprint().getCardType())).count());

        return 9 - (companionsInPlay + companionsDead);
    }

    public boolean hasFreeSlotForThis(BotCard target, Set<PossessionClass> classes) {
        for (PossessionClass possessionClass : classes) {
            for (Map.Entry<BotCard, Set<BotCard>> entry : attachedCards.entrySet()) {
                if (entry.getKey().getSelf().equals(target.getSelf())) {
                    for (BotCard attachedCard : entry.getValue()) {
                        if (attachedCard.getSelf().getBlueprint().getPossessionClasses() != null
                                && attachedCard.getSelf().getBlueprint().getPossessionClasses().contains(possessionClass)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public List<BotCard> getAttachedCards(BotCard card) {
        for (Map.Entry<BotCard, Set<BotCard>> entry : attachedCards.entrySet()) {
            if (entry.getKey().getSelf().equals(card.getSelf())) {
                return new ArrayList<>(entry.getValue());
            }
        }
        return new ArrayList<>();
    }

    public List<BotCard> getFpCardsInPlay(String owner) {
        return inPlayFpCards.get(owner);
    }

    public int getBurdens() {
        return getTokenCount(ringBearers.get(currentPlayer), Token.BURDEN);
    }

    public int getWounds(BotCard botCard) {
        return getTokenCount(botCard, Token.WOUND);
    }

    public List<BotCard> getRingBearers() {
        return new ArrayList<>(ringBearers.values());
    }

    public int getStrength(BotCard botCard) {
        int tbr = botCard.getSelf().getBlueprint().getStrength();
        // TODO effects
        for (BotCard attachedCard : getAttachedCards(botCard)) {
            tbr += attachedCard.getSelf().getBlueprint().getStrength();
        }
        return tbr;
    }

    public int getVitality(BotCard botCard) {
        // TODO effects
        return botCard.getSelf().getBlueprint().getVitality() - getWounds(botCard);
    }

    public List<BotCard> getCardsEffectCanTarget(AbilityProperty effect, String player, Side side) {
        Predicate<PhysicalCard> targetingPredicate = effect.getTargetPredicate();
        if (targetingPredicate == null) {
            return new ArrayList<>();
        }
        if (side.equals(Side.FREE_PEOPLE)) {
            return Stream.concat(inPlayFpCards.get(player).stream(), inPlayShadowCards.get(getOpponent(player)).stream())
                    .filter(botCard -> targetingPredicate.test(botCard.getSelf())).toList();
        } else if (side.equals(Side.SHADOW)) {
            return Stream.concat(inPlayShadowCards.get(player).stream(), inPlayFpCards.get(getOpponent(player)).stream())
                    .filter(botCard -> targetingPredicate.test(botCard.getSelf())).toList();
        }
        throw new IllegalStateException("Could not determine targets for side " + side);
    }

    public List<BotCard> getActiveCards() {
        return new ArrayList<>(Stream.concat(inPlayFpCards.get(currentPlayer).stream(), inPlayShadowCards.get(getOpponent(currentPlayer)).stream()).toList());
    }

    public List<BotCard> getDiscard(String player) {
        return new ArrayList<>(discards.get(player));
    }

    public List<BotCard> getDeadPile(String player) {
        return new ArrayList<>(deadPiles.get(player));
    }

    public List<BotCard> getSitesInPlay() {
        return new ArrayList<>(inPlaySites);
    }

    public List<BotCard> getAdventureDeck(String player) {
        return new ArrayList<>(adventureDecks.get(player));
    }

    public boolean canPayAllCosts(BotCard source, BotAbility ability) {
        if (ability instanceof TriggeredAbility ta) {
            for (AbilityProperty cost : ta.getCosts()) {
                if (!canPayCost(source, cost)) {
                    return false;
                }
            }
            return true;
        } else if (ability instanceof ActivatedAbility aa) {
            for (AbilityProperty cost : aa.getCosts()) {
                if (!canPayCost(source, cost)) {
                    return false;
                }
            }
            return true;
        } else {
            throw new IllegalStateException("Cannot determine if costs can be payed for this ability");
        }
    }

    private boolean canPayCost(BotCard source, AbilityProperty cost) {
        if (cost.getType().equals(AbilityProperty.Type.EXERT)) {
            List<BotCard> potentialTargets = getCardsEffectCanTarget(cost, source.getSelf().getOwner(), source.getSelf().getBlueprint().getSide());
            int amount = cost.getParam("amount", Integer.class);
            return potentialTargets.stream().anyMatch(botCard -> getVitality(botCard) > amount);
        } else if (cost.getType().equals(AbilityProperty.Type.EXERT_SELF)) {
            int amount = cost.getParam("amount", Integer.class);
            int vitality = getVitality(source);
            return vitality > amount;
        } else if (cost.getType().equals(AbilityProperty.Type.DISCARD_SELF)) {
            return inPlayFpCards.get(source.getSelf().getOwner()).contains(source)
                    || inPlayShadowCards.get(source.getSelf().getOwner()).contains(source);
        }
        throw new IllegalStateException("Cannot determine if costs can be payed for type " + cost.getType());
    }

    public double getValueIfUsed(BotCard source, BotAbility ability) {
        double value = 0.0;
        if (ability instanceof TriggeredAbility ta) {
            value += getValueIfUsed(source, ta.getEffect());
            for (AbilityProperty cost : ta.getCosts()) {
                value += getValueIfUsed(source, cost);
            }
            return value;
        } else if (ability instanceof ActivatedAbility aa) {
            value += getValueIfUsed(source, aa.getEffect());
            for (AbilityProperty cost : aa.getCosts()) {
                value += getValueIfUsed(source, cost);
            }
            return value;
        }
        throw new IllegalStateException("Cannot compute value of this ability");
    }

    private double getValueIfUsed(BotCard source, AbilityProperty abilityProperty) {
        String player = source.getSelf().getOwner();
        Side side = source.getSelf().getBlueprint().getSide();
        return switch (abilityProperty.getType()) {
            case DISCARD_FROM_PLAY -> {
                List<BotCard> potentialTargets = getCardsEffectCanTarget(abilityProperty, player, side);
                int numberOfTargets = abilityProperty.getParam("numberOfTargets", Integer.class);
                if (numberOfTargets >= potentialTargets.size()) {
                    double value = 0.0;
                    for (BotCard potentialTarget : potentialTargets) {
                        value += potentialTarget.getSelf().getOwner().equals(player) ? -1.1: 1.1;
                    }
                    yield value;
                } else {
                    throw new IllegalStateException("Choosing targets for discarding not implemented");
                }

            }
            case EXERT -> {
                List<BotCard> potentialTargets = getCardsEffectCanTarget(abilityProperty, player, side);
                if (potentialTargets.isEmpty()) {
                    yield 0.0;
                } else if (potentialTargets.size() > 1) {
                    throw new IllegalStateException("Cannot compute exert value if target can be chosen");
                } else { // potentialTargets.size() == 1
                    BotCard toBeExerted = potentialTargets.getFirst();
                    int amount = Math.min(abilityProperty.getParam("amount", Integer.class), getVitality(toBeExerted) - 1);
                    double value = amount;
                    if (toBeExerted.getSelf().getBlueprint().getCardType().equals(CardType.ALLY)) {
                        value /= 2.0;
                    }
                    if (toBeExerted.getSelf().getBlueprint().getCardType().equals(CardType.COMPANION)
                            && getVitality(toBeExerted) - amount == 1) {
                        value += 0.5;
                    }
                    yield toBeExerted.getSelf().getOwner().equals(player) ? -value : value;
                }
            }
            case EXERT_SELF -> {
                int amount = Math.min(abilityProperty.getParam("amount", Integer.class), getVitality(source) - 1);
                double value = amount;
                if (source.getSelf().getBlueprint().getCardType().equals(CardType.ALLY)) {
                    value /= 2.0;
                }
                if (source.getSelf().getBlueprint().getCardType().equals(CardType.COMPANION)
                        && getVitality(source) - amount == 1) {
                    value += 0.5;
                }
                yield -value;
            }
            case HEAL -> {
                List<BotCard> potentialTargets = getCardsEffectCanTarget(abilityProperty, player, side);
                if (potentialTargets.isEmpty()) {
                    yield 0.0;
                } else {
                    BotCard toBeHealed = BotTargetingMode.HEAL.chooseTarget(this, potentialTargets, false);
                    double value = Math.min(abilityProperty.getParam("amount", Integer.class), getTokenCount(toBeHealed, Token.WOUND));
                    if (toBeHealed.getSelf().getBlueprint().getCardType().equals(CardType.ALLY)) {
                        value /= 2.0;
                    }
                    if (toBeHealed.getSelf().getBlueprint().getCardType().equals(CardType.COMPANION)
                            && getVitality(toBeHealed) == 1) {
                        value += 0.5;
                    }
                    yield toBeHealed.getSelf().getOwner().equals(player) ? value : -value;
                }
            }
            case REMOVE_BURDEN -> {
                int amount = abilityProperty.getParam("amount", Integer.class);
                BotCard ringBearer = side.equals(Side.FREE_PEOPLE) ? ringBearers.get(player) : ringBearers.get(getOpponent(player));
                int burdensPlaced = getTokenCount(ringBearer, Token.BURDEN);
                int toBeRemoved = Math.min(amount, burdensPlaced);
                double valueOfRemovedBurden = 0.9 + ((double) burdensPlaced / 10); // more burdens placed, better to remove
                yield toBeRemoved * valueOfRemovedBurden;
            }
            case DISCARD_SELF -> -1; // should depend on the card being discarded
            default -> throw new IllegalStateException("Cannot compute value for ability property of " + abilityProperty.getType());
        };
    }

    /*
        SPOT CHECKS
     */
    public boolean canSpot(String owner, Predicate<BotCard> predicate) {
        return Stream.concat(
                        inPlayFpCards.values().stream().flatMap(List::stream),
                        inPlayShadowCards.values().stream().flatMap(List::stream))
                .filter(card -> card.getSelf().getOwner().equals(owner))
                .anyMatch(predicate);

    }

    public boolean canSpot(String owner, String title) {
        return canSpot(owner, card -> card.getSelf().getBlueprint().getTitle().equals(title));
    }

    public boolean canSpot(String owner, Race race) {
        return canSpot(owner, card -> race.equals(card.getSelf().getBlueprint().getRace()));
    }

    public boolean canSpot(String owner, CardType cardType) {
        return canSpot(owner, card -> cardType.equals(card.getSelf().getBlueprint().getCardType()));
    }

    public boolean canSpotRanger(String owner) {
        return canSpot(owner, card -> card.getSelf().getBlueprint().hasKeyword(Keyword.RANGER));
    }

    public boolean canSpot(String owner, Race race, CardType cardType) {
        return canSpot(owner, card -> cardType.equals(card.getSelf().getBlueprint().getCardType()) && race.equals(card.getSelf().getBlueprint().getRace()));
    }

    /*
        EXERT CHECKS
     */
    public boolean canExert(String owner, Predicate<BotCard> predicate) {
        return Stream.concat(
                        inPlayFpCards.values().stream().flatMap(List::stream),
                        inPlayShadowCards.values().stream().flatMap(List::stream))
                .filter(this::hasVitalityToExert)
                .filter(card -> card.getSelf().getOwner().equals(owner))
                .anyMatch(predicate);
    }

    public boolean canExert(String owner, String title) {
        return canExert(owner, card -> card.getSelf().getBlueprint().getTitle().equals(title));
    }

    public boolean canExert(String owner, Race race) {
        return canExert(owner, card -> race.equals(card.getSelf().getBlueprint().getRace()));
    }

    public boolean canExertRanger(String owner) {
        return canExert(owner, card -> card.getSelf().getBlueprint().hasKeyword(Keyword.RANGER));
    }

    public boolean canExert(String owner, Race race, CardType cardType) {
        return canExert(owner, card -> cardType.equals(card.getSelf().getBlueprint().getCardType()) && race.equals(card.getSelf().getBlueprint().getRace()));
    }

    /*
        DISCARD CHECKS
     */
    public boolean isInDiscard(String owner, Predicate<BotCard> predicate) {
        return discards.get(owner).stream()
                .anyMatch(predicate);
    }

    public boolean isInDiscard(String owner, Culture culture, Race race) {
        return  isInDiscard(owner, card -> culture.equals(card.getSelf().getBlueprint().getCulture()) && race.equals(card.getSelf().getBlueprint().getRace()));
    }




    private String getOpponent(String player) {
        for (String s : players) {
            if (!s.equals(player)) {
                return s;
            }
        }

        throw new IllegalStateException("Could not find opponent name");
    }

    private BotCard findBearer(PhysicalCard attachedTo) {
        return Stream.of(inPlayFpCards.values(), inPlayShadowCards.values())
                .flatMap((Function<Collection<List<BotCard>>, Stream<BotCard>>) lists -> lists.stream().flatMap(List::stream))
                .filter(b -> b.getSelf().equals(attachedTo))
                .findFirst()
                .orElse(null);
    }

    private int getTokenCount(BotCard card, Token token) {
        Map<Token, Integer> tokens = cardTokens.get(card);
        if (tokens == null)
            return 0;
        Integer count = tokens.get(token);
        if (count == null)
            return 0;
        return count;
    }

    private boolean hasVitalityToExert(BotCard botCard) {
        int printedVitality = botCard.getSelf().getBlueprint().getVitality();
        int wounds = getWounds(botCard);
        return  printedVitality - wounds > 1;
    }
}
