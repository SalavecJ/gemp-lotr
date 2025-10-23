package com.gempukku.lotro.game.state;

import com.gempukku.lotro.cards.build.bot.BotCardFactory;
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
    private final Map<String, List<BotCard>> revealedHands = new HashMap<>();
    private final Map<String, List<BotCard>> discards = new HashMap<>();
    private final Map<String, List<BotCard>> deadPiles = new HashMap<>();

    private final Map<String, List<BotCard>> inPlayFpCards = new HashMap<>();
    private final Map<String, List<BotCard>> inPlayShadowCards = new HashMap<>();
    private final Set<BotCard> inPlaySites = new HashSet<>();

    private final Map<BotCard, Set<BotCard>> attachedCards = new HashMap<>();
    private final Map<BotCard, Map<Token, Integer>> cardTokens = new HashMap<>();

    private int twilight;
    private int ruleOfFourCount = 0;

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
            revealedHands.put(player, new ArrayList<>());
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
    public void drawCard(String player) {
        try {
            if (ruleOfFourLimitOk()) {
                BotCard card = decks.get(player).removeFirst();
                hands.get(player).add(card);
                ruleOfFourCount++;
            }
        } catch (NoSuchElementException ignored) {

        }
    }

    public void addTwilight(int amount) {
        twilight += amount;
    }

    public void moveFromDiscardIntoHand(BotCard botCard) {
        if (ruleOfFourLimitOk()) {
            discards.get(botCard.getSelf().getOwner()).remove(botCard);
            hands.get(botCard.getSelf().getOwner()).add(botCard);
            ruleOfFourCount++;
        }
    }

    public void moveFromHandToBottomOfDeck(BotCard botCard) {
        hands.get(botCard.getSelf().getOwner()).remove(botCard);
        decks.get(botCard.getSelf().getOwner()).addLast(botCard);
    }

    public void discardFromHand(BotCard botCard) {
        hands.get(botCard.getSelf().getOwner()).remove(botCard);
        discards.get(botCard.getSelf().getOwner()).add(botCard);
    }

    public void removeBurden(int amount) {
        BotCard ringBearer = ringBearers.get(currentPlayer);
        int burdensPlaced = getTokenCount(ringBearer, Token.BURDEN);
        int toBeRemoved = Math.min(amount, burdensPlaced);
        if (toBeRemoved > 0) {
            cardTokens.get(ringBearer).put(Token.BURDEN, burdensPlaced - toBeRemoved);
        }
    }

    public void heal(BotCard botCard) {
        heal(botCard, 1);
    }

    public void heal(BotCard botCard, int amount) {
        int realAmount = Math.min(amount, getWounds(botCard));

        if (cardTokens.get(botCard).containsKey(Token.WOUND)) {
            cardTokens.get(botCard).put(Token.WOUND, cardTokens.get(botCard).get(Token.WOUND) - realAmount);
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
            adventureDecks.get(getOpponent(ownerOfEffect)).add(nextSite);
            inPlaySites.add(nextSiteInAdventureDeck);
            adventureDecks.get(ownerOfEffect).remove(nextSiteInAdventureDeck);
        }
    }

    private void playFpCard(BotCard botCard) {
        playFpCard(botCard, 0);
    }

    private void playFpCard(BotCard botCard, int twilightModifier) {
        inPlayFpCards.get(botCard.getSelf().getOwner()).add(botCard);
        hands.get(botCard.getSelf().getOwner()).remove(botCard);
        revealedHands.get(botCard.getSelf().getOwner()).remove(botCard);
        twilight += (botCard.getSelf().getBlueprint().getTwilightCost() + twilightModifier);
        cardTokens.put(botCard, new HashMap<>());
    }

    private void playShadowCard(BotCard botCard) {
        if (twilight < botCard.getSelf().getBlueprint().getTwilightCost()) {
            throw new IllegalStateException("Cannot pay twilight for event " + botCard.getSelf().getBlueprint().getFullName());
        }
        inPlayShadowCards.get(botCard.getSelf().getOwner()).add(botCard);
        hands.get(botCard.getSelf().getOwner()).remove(botCard);
        revealedHands.get(botCard.getSelf().getOwner()).remove(botCard);
        twilight -= botCard.getSelf().getBlueprint().getTwilightCost();
        cardTokens.put(botCard, new HashMap<>());
    }

    public void playCompanion(BotCompanionCard botCard) {
        playFpCard(botCard);
    }

    public void playToFpSupportArea(BotCard botCard) {
        playFpCard(botCard);
    }

    public void playCompanion(BotCompanionCard botCard, int twilightModifier) {
        playFpCard(botCard, twilightModifier);
    }

    public void playToFpSupportArea(BotCard botCard, int twilightModifier) {
        playFpCard(botCard, twilightModifier);
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

        botCard.getEventAbility().resolveAbility(botCard.getSelf().getOwner(), this);

        hands.get(botCard.getSelf().getOwner()).remove(botCard);
        revealedHands.get(botCard.getSelf().getOwner()).remove(botCard);
        discards.get(botCard.getSelf().getOwner()).add(botCard);
    }

    public void healByDiscard(BotCard discardedCard) {
        AtomicBoolean healed = new AtomicBoolean(false);
        inPlayFpCards.values().forEach(lists -> lists.forEach(botCard -> {
            if (discardedCard.getSelf().getBlueprint().getTitle().equals(botCard.getSelf().getBlueprint().getTitle())
                    && discardedCard.getSelf().getOwner().equals(botCard.getSelf().getOwner())) {
                heal(botCard);
                hands.get(botCard.getSelf().getOwner()).remove(discardedCard);
                discards.get(botCard.getSelf().getOwner()).add(discardedCard);
                revealedHands.get(botCard.getSelf().getOwner()).remove(botCard);
                healed.set(true);
            }
        }));
        if (!healed.get()) {
            throw new IllegalStateException("Could not find card to heal: " + discardedCard.getSelf().getBlueprint().getFullName());
        }
    }

    public void revealHand(String player) {
        for (BotCard botCard : hands.get(player)) {
            if (!revealedHands.get(player).contains(botCard))
                revealedHands.get(player).add(botCard);
        }
    }

    /*
        GET INFO
     */
    public boolean allCardsInHandRevealed(String player) {
        return new HashSet<>(revealedHands.get(player)).containsAll(hands.get(player));
    }

    public List<BotCard> getRevealedCardsFromHand(String player) {
        return revealedHands.get(player);
    }

    public String getCurrentFpPlayer() {
        return currentPlayer;
    }

    public Phase getCurrentPhase() {
        return phase;
    }

    public int getCurrentPlayerPosition() {
        return playerPosition.get(currentPlayer);
    }

    public BotCard getCurrentSite() {
        for (BotCard inPlaySite : inPlaySites) {
            if (inPlaySite.getSelf().getBlueprint().getSiteNumber() == getCurrentPlayerPosition()) {
                return inPlaySite;
            }
        }
        throw new IllegalStateException("Current site could not be found");
    }

    public boolean ruleOfFourLimitOk() {
        if (phase.equals(Phase.FELLOWSHIP)) {
            return ruleOfFourCount < 4;
        } else {
            return true;
        }
    }

    public BotCard getTopCardOfDeck(String player) {
        return decks.get(player).getFirst();
    }

    public int getTwilight() {
        return twilight;
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

    public List<BotCard> getActiveCards() {
        return new ArrayList<>(Stream.concat(inPlayFpCards.get(currentPlayer).stream(), inPlayShadowCards.get(getOpponent(currentPlayer)).stream()).toList());
    }

    public List<BotCard> getDiscard(String player) {
        return new ArrayList<>(discards.get(player));
    }

    public List<BotCard> getDeadPile(String player) {
        return new ArrayList<>(deadPiles.get(player));
    }

    public List<BotCard> getHand(String player) {
        return new ArrayList<>(hands.get(player));
    }

    public List<BotCard> getSitesInPlay() {
        return new ArrayList<>(inPlaySites);
    }

    public List<BotCard> getAdventureDeck(String player) {
        return new ArrayList<>(adventureDecks.get(player));
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

    public boolean canExert(String owner, Race race, CardType cardType) {
        return canExert(owner, card -> cardType.equals(card.getSelf().getBlueprint().getCardType()) && race.equals(card.getSelf().getBlueprint().getRace()));
    }




    public String getOpponent(String player) {
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
