package com.gempukku.lotro.game.state;

import com.gempukku.lotro.cards.build.bot.BotCardFactory;
import com.gempukku.lotro.cards.build.bot.ability2.ActivatedAbility;
import com.gempukku.lotro.cards.build.bot.ability2.TriggeredAbility;
import com.gempukku.lotro.cards.build.bot.ability2.effect.EffectWithTarget;
import com.gempukku.lotro.cards.build.bot.abstractcard.BotCard;
import com.gempukku.lotro.cards.build.bot.abstractcard.BotObjectAttachableCard;
import com.gempukku.lotro.cards.build.bot.ability2.effect.Effect;
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

    // Assignment tracking: FP character -> Set of minions assigned to them
    private final Map<BotCard, Set<BotCard>> assignments = new HashMap<>();

    // Assignment phase state tracking
    private boolean assignmentPhasePlayActionsCompleted = false; // True when both players have passed
    private boolean fpAssignmentCompleted = false; // True when FP has finished assigning

    // Skirmish phase state tracking
    private BotCard currentSkirmish = null; // Currently active FP character's skirmish

    private final Map<String, Integer> playerPosition = new HashMap<>();
    private final Map<String, Integer> playerThreats = new HashMap<>();

    private int twilight;
    private int movesMade;
    private int ruleOfFourCount = 0;

    public PlannedBoardState(LotroGame game) {
        phase = game.getGameState().getCurrentPhase();
        currentPlayer = game.getGameState().getCurrentPlayerId();
        movesMade = game.getGameState().getMoveCount();

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

        // Assignments
        game.getGameState().getAssignments().forEach(assignment -> {
            BotCard fpCharacter = findBotCard(assignment.getFellowshipCharacter());
            if (fpCharacter != null) {
                Set<BotCard> assignedMinions = new HashSet<>();
                assignment.getShadowCharacters().forEach(minion -> {
                    BotCard botMinion = findBotCard(minion);
                    if (botMinion != null) {
                        assignedMinions.add(botMinion);
                    }
                });
                if (!assignedMinions.isEmpty()) {
                    assignments.put(fpCharacter, assignedMinions);
                }
            }
        });

        // Stats
        twilight = game.getGameState().getTwilightPool();
        players.forEach(player -> {
            playerPosition.put(player, game.getGameState().getPlayerPosition(player));
            playerThreats.put(player, game.getGameState().getPlayerThreats(player));
        });
    }

    public PlannedBoardState(PlannedBoardState other) {
        this.phase = other.phase;
        this.currentPlayer = other.currentPlayer;
        this.players.addAll(other.players);
        this.ringBearers.putAll(other.ringBearers);

        other.adventureDecks.forEach((k, v) -> this.adventureDecks.put(k, new ArrayList<>(v)));
        other.decks.forEach((k, v) -> this.decks.put(k, new ArrayList<>(v)));
        other.hands.forEach((k, v) -> this.hands.put(k, new ArrayList<>(v)));
        other.revealedHands.forEach((k, v) -> this.revealedHands.put(k, new ArrayList<>(v)));
        other.discards.forEach((k, v) -> this.discards.put(k, new ArrayList<>(v)));
        other.deadPiles.forEach((k, v) -> this.deadPiles.put(k, new ArrayList<>(v)));

        other.inPlayFpCards.forEach((k, v) -> this.inPlayFpCards.put(k, new ArrayList<>(v)));
        other.inPlayShadowCards.forEach((k, v) -> this.inPlayShadowCards.put(k, new ArrayList<>(v)));
        this.inPlaySites.addAll(other.inPlaySites);

        other.attachedCards.forEach((k, v) -> this.attachedCards.put(k, new HashSet<>(v)));
        other.cardTokens.forEach((k, v) -> this.cardTokens.put(k, new HashMap<>(v)));

        other.assignments.forEach((k, v) -> this.assignments.put(k, new HashSet<>(v)));

        this.currentSkirmish = other.currentSkirmish;

        this.assignmentPhasePlayActionsCompleted = other.assignmentPhasePlayActionsCompleted;
        this.fpAssignmentCompleted = other.fpAssignmentCompleted;

        this.twilight = other.twilight;
        this.ruleOfFourCount = other.ruleOfFourCount;
        this.movesMade = other.movesMade;
        this.playerPosition.putAll(other.playerPosition);
        this.playerThreats.putAll(other.playerThreats);
    }

    /*
        ALTER BOARD STATE
     */
    public void moveToNextPhase() {
        ruleOfFourCount = 0;
        assignmentPhasePlayActionsCompleted = false;
        fpAssignmentCompleted = false;
        if (phase == Phase.FELLOWSHIP) {
            phase = Phase.SHADOW;
        } else if (phase == Phase.SHADOW) {
            phase = Phase.MANEUVER;
        } else if (phase == Phase.MANEUVER) {
            phase = Phase.ARCHERY;
        } else if (phase == Phase.ARCHERY) {
            phase = Phase.ASSIGNMENT;
        } else if (phase == Phase.ASSIGNMENT) {
            phase = Phase.SKIRMISH;
        } else if (phase == Phase.SKIRMISH) {
            phase = Phase.REGROUP;
        } else {
            throw new IllegalStateException("Do not know how to move to next phase from " + phase);
        }
    }

    /**
     * Assigns a minion to an FP character.
     * Can be called multiple times to assign multiple minions to the same character (for Shadow player stacking).
     */
    public void assignMinion(BotCard minion, BotCard fpCharacter) {
        assignments.computeIfAbsent(fpCharacter, k -> new HashSet<>()).add(minion);
    }

    /**
     * Marks that both players have passed in assignment phase and we're now in the "assigning minions" phase.
     */
    public void setAssignmentPhasePlayActionsCompleted(boolean completed) {
        this.assignmentPhasePlayActionsCompleted = completed;
    }

    /**
     * Returns true if both players have passed and we're in the "assigning minions" phase.
     */
    public boolean isAssignmentPhasePlayActionsCompleted() {
        return assignmentPhasePlayActionsCompleted;
    }

    /**
     * Marks that FP has completed their assignment (they assign first, 1-on-1).
     */
    public void setFpAssignmentCompleted(boolean completed) {
        this.fpAssignmentCompleted = completed;
    }

    /**
     * Returns true if FP has completed their assignment.
     */
    public boolean isFpAssignmentCompleted() {
        return fpAssignmentCompleted;
    }
    /**
     * Sets the current skirmish being resolved (the FP character in the skirmish).
     */
    public void setCurrentSkirmish(BotCard fpCharacter) {
        if (!assignments.containsKey(fpCharacter)) {
            throw new IllegalStateException("Cannot set current skirmish: no minions assigned to " + fpCharacter.getSelf().getBlueprint().getFullName());
        }
        this.currentSkirmish = fpCharacter;
    }

    /**
     * Gets the current skirmish being resolved (the FP character in the skirmish).
     */
    public BotCard getCurrentSkirmish() {
        return currentSkirmish;
    }

    /**
     * Resolves the current skirmish by comparing strengths and dealing damage.
     * Throws an exception if no skirmish is currently active.
     */
    public void resolveCurrentSkirmish() {
        if (currentSkirmish == null) {
            throw new IllegalStateException("Cannot resolve skirmish: no current skirmish is set");
        }
        // - Calculate FP character strength
        int fpStrength = getStrength(currentSkirmish);
        // - Calculate total minion strength
        int shadowStrength = assignments.get(currentSkirmish).stream().mapToInt(this::getStrength).sum();
        // - Compare strengths
        if (fpStrength > shadowStrength) {
            // FP wins
            if (fpStrength >= shadowStrength * 2) {
                // Overwhelm
                assignments.get(currentSkirmish).forEach(this::kill);
            } else {
                int damage = 1 + getBonusDamage(currentSkirmish);
                assignments.get(currentSkirmish).forEach(minion -> wound(minion, damage));
            }
        } else {
            // Shadow wins
            if (shadowStrength >= fpStrength * 2) {
                // Overwhelm
                kill(currentSkirmish);
            } else {
                int damage = 1 + assignments.get(currentSkirmish).stream().mapToInt(this::getBonusDamage).sum();
                wound(currentSkirmish, damage);
            }
        }

        // Skirmish resolved, clear current skirmish
        assignments.remove(currentSkirmish);
        currentSkirmish = null;
    }

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

    public void wound(BotCard botCard, int amount) {
        int vitality = getVitality(botCard);

        if (amount >= vitality) {
            kill(botCard);
        } else {
            exert(botCard, amount);
        }
    }

    public void kill(BotCard botCard) {
        if (botCard.getSelf().getBlueprint().getSide().equals(Side.SHADOW)) {
            discardFromPlay(botCard);
        } else if (botCard.getSelf().getBlueprint().getSide().equals(Side.FREE_PEOPLE)) {
            inPlayFpCards.get(botCard.getSelf().getOwner()).remove(botCard);
            deadPiles.get(botCard.getSelf().getOwner()).add(botCard);
        } else {
            throw new IllegalStateException("Unknown side for card: " + botCard.getSelf().getBlueprint().getFullName());
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

    private void playCardInternal(BotCard botCard, BotCard target, int twilightModifier, CardZone zone) {
        // Check if it's a minion and calculate roaming cost
        if (botCard.getSelf().getBlueprint().getCardType() == CardType.MINION) {
            int currentSiteNumber = getCurrentSite().getSelf().getBlueprint().getSiteNumber();
            int minionSiteNumber = botCard.getSelf().getBlueprint().getSiteNumber();
            boolean roaming = minionSiteNumber > currentSiteNumber;
            twilightModifier += roaming ? 2 : 0;
        }

        boolean isFreePeoples = botCard.getSelf().getBlueprint().getSide() == Side.FREE_PEOPLE;

        int totalCost = botCard.getSelf().getBlueprint().getTwilightCost() + twilightModifier;
        if (totalCost < 0) {
            totalCost = 0;
        }

        if (!botCard.canBePlayed(this)) {
            throw new IllegalStateException("Cannot be played now: " + botCard.getSelf().getBlueprint().getFullName());
        }
        if (botCard instanceof BotObjectAttachableCard attachableCard && (target == null || !attachableCard.isValidBearer(target, this))) {
            throw new IllegalStateException("Invalid target for attachment: " + botCard.getSelf().getBlueprint().getFullName());
        }
        if (!isCardInZone(botCard, zone)) {
            throw new IllegalStateException("Card not in expected zone to be played: " + botCard.getSelf().getBlueprint().getFullName());
        }

        removeCardFromZone(botCard, zone);

        if (botCard.getSelf().getBlueprint().getCardType().equals(CardType.EVENT)) {
            if (target == null) {
                botCard.getEventAbility().resolveAbility(botCard.getSelf().getOwner(), this);
            } else {
                botCard.getEventAbility().resolveAbilityOnTarget(botCard.getSelf().getOwner(), this, target);
            }
            discards.get(botCard.getSelf().getOwner()).add(botCard);
        } else if (isFreePeoples) {
            inPlayFpCards.get(botCard.getSelf().getOwner()).add(botCard);
        } else {
            inPlayShadowCards.get(botCard.getSelf().getOwner()).add(botCard);
        }

        if (botCard instanceof BotObjectAttachableCard) {
            attachedCards.computeIfAbsent(target, k -> new HashSet<>()).add(botCard);
        }

        payTwilight(totalCost, isFreePeoples);
        cardTokens.put(botCard, new HashMap<>());
    }

    private void payTwilight(int cost, boolean isFreePeoples) {
        if (isFreePeoples) {
            twilight += cost;
        } else {
            if (twilight < cost) {
                throw new IllegalStateException("Cannot pay twilight of " + cost + " when twilight pool is " + twilight);
            }
            twilight -= cost;
        }
    }

    private enum CardZone {
        HAND, DISCARD
    }

    private void removeCardFromZone(BotCard botCard, CardZone source) {
        if (source == CardZone.HAND) {
            hands.get(botCard.getSelf().getOwner()).remove(botCard);
            revealedHands.get(botCard.getSelf().getOwner()).remove(botCard);
        } else {
            discards.get(botCard.getSelf().getOwner()).remove(botCard);
        }
    }

    private boolean isCardInZone(BotCard botCard, CardZone zone) {
        if (zone == CardZone.HAND) {
            return hands.get(botCard.getSelf().getOwner()).contains(botCard);
        } else if (zone == CardZone.DISCARD) {
            return discards.get(botCard.getSelf().getOwner()).contains(botCard);
        } else {
            throw new IllegalStateException("Unknown zone: " + zone);
        }
    }

    public void playCard(BotCard botCard) {
        playCard(botCard, 0);
    }

    public void playCard(BotCard botCard, int twilightModifier) {
        playCard(botCard, null, twilightModifier);
    }

    public void playCard(BotCard botCard, BotCard target) {
        playCard(botCard, target, 0);
    }

    public void playCard(BotCard botCard, BotCard target, int twilightModifier) {
        playCardInternal(botCard, target, twilightModifier, CardZone.HAND);
    }

    public void playCardFromDiscard(BotCard botCard) {
        playCardFromDiscard(botCard, 0);
    }

    public void playCardFromDiscard(BotCard botCard, int twilightModifier) {
        playCardFromDiscard(botCard, null, twilightModifier);
    }

    public void playCardFromDiscard(BotCard botCard, BotCard target) {
        playCardFromDiscard(botCard, target, 0);
    }

    public void playCardFromDiscard(BotCard botCard, BotCard target, int twilightModifier) {
        playCardInternal(botCard, target, twilightModifier, CardZone.DISCARD);
    }

    public void activateAbility(BotCard botCard, Class<? extends Effect> effectClass, String player) {
        ActivatedAbility ability = botCard.getActivatedAbility(effectClass);
        if (ability == null) {
            throw new IllegalStateException("Card does not have ability for effect class: " + effectClass.getSimpleName());
        }
        ability.resolveAbility(player, this);
    }

    public void activateAbilityOnTarget(BotCard botCard, Class<? extends Effect> effectClass, String player, BotCard target) {
        ActivatedAbility ability = botCard.getActivatedAbility(effectClass);
        if (ability == null) {
            throw new IllegalStateException("Card does not have ability for effect class: " + effectClass.getSimpleName());
        }
        if (!(ability.getEffect() instanceof EffectWithTarget)) {
            throw new IllegalStateException("Ability does not require target: " + effectClass.getSimpleName());
        }
        ability.resolveAbilityOnTarget(player, this, target);
    }

    public void activateTriggeredAbility(BotCard botCard, String player) {
        TriggeredAbility ability = botCard.getTriggeredAbility();
        if (ability == null) {
            throw new IllegalStateException("Card does not have triggered ability: " + botCard.getSelf().getBlueprint().getFullName());
        }
        ability.resolveAbility(player, this);
    }

    public void activateTriggeredAbilityOnTarget(BotCard botCard, String player, BotCard target) {
        TriggeredAbility ability = botCard.getTriggeredAbility();
        if (ability == null) {
            throw new IllegalStateException("Card does not have triggered ability: " + botCard.getSelf().getBlueprint().getFullName());
        }
        if (!(ability.getEffect() instanceof EffectWithTarget)) {
            throw new IllegalStateException("Triggered ability does not require target: " + botCard.getSelf().getBlueprint().getFullName());
        }
        ability.resolveAbilityOnTarget(player, this, target);
    }

    public void activateAbilityWithCostTarget(BotCard botCard, Class<? extends Effect> effectClass, String player, BotCard costTarget) {
        ActivatedAbility ability = botCard.getActivatedAbility(effectClass);
        if (ability == null) {
            throw new IllegalStateException("Card does not have ability for effect class: " + effectClass.getSimpleName());
        }
        ability.resolveAbilityWithCostTarget(player, this, costTarget);
    }

    public void activateAbilityOnTargetWithCostTarget(BotCard botCard, Class<? extends Effect> effectClass, String player, BotCard effectTarget, BotCard costTarget) {
        ActivatedAbility ability = botCard.getActivatedAbility(effectClass);
        if (ability == null) {
            throw new IllegalStateException("Card does not have ability for effect class: " + effectClass.getSimpleName());
        }
        if (!(ability.getEffect() instanceof EffectWithTarget)) {
            throw new IllegalStateException("Ability does not require effect target: " + effectClass.getSimpleName());
        }
        ability.resolveAbilityOnTargetWithCostTarget(player, this, effectTarget, costTarget);
    }

    public void activateTriggeredAbilityWithCostTarget(BotCard botCard, String player, BotCard costTarget) {
        TriggeredAbility ability = botCard.getTriggeredAbility();
        if (ability == null) {
            throw new IllegalStateException("Card does not have triggered ability: " + botCard.getSelf().getBlueprint().getFullName());
        }
        ability.resolveAbilityWithCostTarget(player, this, costTarget);
    }

    public void activateTriggeredAbilityOnTargetWithCostTarget(BotCard botCard, String player, BotCard effectTarget, BotCard costTarget) {
        TriggeredAbility ability = botCard.getTriggeredAbility();
        if (ability == null) {
            throw new IllegalStateException("Card does not have triggered ability: " + botCard.getSelf().getBlueprint().getFullName());
        }
        if (!(ability.getEffect() instanceof EffectWithTarget)) {
            throw new IllegalStateException("Triggered ability does not require effect target: " + botCard.getSelf().getBlueprint().getFullName());
        }
        ability.resolveAbilityOnTargetWithCostTarget(player, this, effectTarget, costTarget);
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
    public BotCard getCardById(int id) {
        AtomicBoolean found = new AtomicBoolean(false);
        final BotCard[] result = new BotCard[1];
        Stream.of(inPlayFpCards.values(), inPlayShadowCards.values()).forEach(lists -> lists.forEach(botCards -> botCards.forEach(botCard -> {
            if (botCard.getSelf().getCardId() == id) {
                result[0] = botCard;
                found.set(true);
            }
        })));
        if (found.get()) {
            return result[0];
        }

        for (String player : players) {
            for (BotCard botCard : hands.get(player)) {
                if (botCard.getSelf().getCardId() == id) {
                    return botCard;
                }
            }
            for (BotCard botCard : discards.get(player)) {
                if (botCard.getSelf().getCardId() == id) {
                    return botCard;
                }
            }
            for (BotCard botCard : deadPiles.get(player)) {
                if (botCard.getSelf().getCardId() == id) {
                    return botCard;
                }
            }
            for (BotCard botCard : decks.get(player)) {
                if (botCard.getSelf().getCardId() == id) {
                    return botCard;
                }
            }
        }

        throw new IllegalStateException("Could not find card with id: " + id);
    }

    public boolean allCardsInHandRevealed(String player) {
        return new HashSet<>(revealedHands.get(player)).containsAll(hands.get(player));
    }

    public List<BotCard> getRevealedCardsFromHand(String player) {
        return revealedHands.get(player);
    }

    public String getCurrentFpPlayer() {
        return currentPlayer;
    }

    public String getCurrentShadowPlayer() {
        return getOpponent(currentPlayer);
    }

    public Phase getCurrentPhase() {
        return phase;
    }

    public int getMovesMade() {
        return movesMade;
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

    public int getRuleOfFourReminder() {
        return 4 - ruleOfFourCount;
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

    public List<BotCard> getShadowCardsInPlay(String owner) {
        return inPlayShadowCards.get(owner);
    }

    public int getBurdens() {
        return getTokenCount(ringBearers.get(currentPlayer), Token.BURDEN);
    }

    public int getResistance() {
        return ringBearers.get(currentPlayer).getSelf().getBlueprint().getResistance();
    }

    public int getWounds(BotCard botCard) {
        return getTokenCount(botCard, Token.WOUND);
    }

    public List<BotCard> getRingBearers() {
        return new ArrayList<>(ringBearers.values());
    }

    public BotCard getRingBearer(String player) {
        return ringBearers.get(player);
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

    public int getBonusDamage(BotCard botCard) {
        // TODO effects
        return botCard.getSelf().getBlueprint().getKeywordCount(Keyword.DAMAGE);
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

    /**
     * Gets the current assignment map.
     * @return Map of FP character -> Set of minions assigned to them
     */
    public Map<BotCard, Set<BotCard>> getAssignments() {
        return new HashMap<>(assignments);
    }

    /**
     * Gets all minions that have been assigned to any FP character.
     * @return Set of assigned minions
     */
    public Set<BotCard> getAssignedMinions() {
        Set<BotCard> assignedMinions = new HashSet<>();
        assignments.values().forEach(assignedMinions::addAll);
        return assignedMinions;
    }

    /**
     * Gets all unassigned minions currently in play.
     * @return List of unassigned minions
     */
    public List<BotCard> getUnassignedMinions() {
        Set<BotCard> assigned = getAssignedMinions();
        return getShadowCardsInPlay(getCurrentShadowPlayer()).stream()
                .filter(card -> CardType.MINION.equals(card.getSelf().getBlueprint().getCardType()))
                .filter(minion -> !assigned.contains(minion))
                .toList();
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

    private BotCard findBotCard(PhysicalCard physicalCard) {
        return Stream.of(inPlayFpCards.values(), inPlayShadowCards.values())
                .flatMap((Function<Collection<List<BotCard>>, Stream<BotCard>>) lists -> lists.stream().flatMap(List::stream))
                .filter(b -> b.getSelf().equals(physicalCard))
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlannedBoardState that = (PlannedBoardState) o;

        // Compare simple fields
        if (twilight != that.twilight) return false;
        if (ruleOfFourCount != that.ruleOfFourCount) return false;
        if (movesMade != that.movesMade) return false;
        if (assignmentPhasePlayActionsCompleted != that.assignmentPhasePlayActionsCompleted) return false;
        if (fpAssignmentCompleted != that.fpAssignmentCompleted) return false;
        if (phase != that.phase) return false;
        if (!Objects.equals(currentPlayer, that.currentPlayer)) return false;
        if (!Objects.equals(players, that.players)) return false;
        if (!Objects.equals(playerPosition, that.playerPosition)) return false;
        if (!Objects.equals(playerThreats, that.playerThreats)) return false;

        // Compare current skirmish by full name
        if (!compareCurrentSkirmishByFullName(that)) return false;

        // Compare ring bearers by full name
        if (!compareRingBearersByFullName(that)) return false;

        // Compare card collections by full name
        if (!compareCardMapsByFullName(adventureDecks, that.adventureDecks)) return false;
        if (!compareCardMapsByFullName(decks, that.decks)) return false;
        if (!compareCardMapsByFullName(hands, that.hands)) return false;
        if (!compareCardMapsByFullName(revealedHands, that.revealedHands)) return false;
        if (!compareCardMapsByFullName(discards, that.discards)) return false;
        if (!compareCardMapsByFullName(deadPiles, that.deadPiles)) return false;
        if (!compareCardMapsByFullName(inPlayFpCards, that.inPlayFpCards)) return false;
        if (!compareCardMapsByFullName(inPlayShadowCards, that.inPlayShadowCards)) return false;
        if (!compareCardSetByFullName(inPlaySites, that.inPlaySites)) return false;

        // Compare attached cards by full name
        if (!compareAttachedCardsByFullName(that)) return false;

        // Compare card tokens by full name
        if (!compareCardTokensByFullName(that)) return false;

        // Compare assignments by full name
        if (!compareAssignmentsByFullName(that)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(phase, currentPlayer, players, twilight, ruleOfFourCount, movesMade, assignmentPhasePlayActionsCompleted, fpAssignmentCompleted, playerPosition, playerThreats);
        result = 31 * result + (currentSkirmish != null ? getCardFullName(currentSkirmish).hashCode() : 0);
        result = 31 * result + hashCardMapByFullName(adventureDecks);
        result = 31 * result + hashCardMapByFullName(decks);
        result = 31 * result + hashCardMapByFullName(hands);
        result = 31 * result + hashCardMapByFullName(revealedHands);
        result = 31 * result + hashCardMapByFullName(discards);
        result = 31 * result + hashCardMapByFullName(deadPiles);
        result = 31 * result + hashCardMapByFullName(inPlayFpCards);
        result = 31 * result + hashCardMapByFullName(inPlayShadowCards);
        result = 31 * result + hashCardSetByFullName(inPlaySites);
        result = 31 * result + hashAssignmentsByFullName();
        return result;
    }

    private boolean compareRingBearersByFullName(PlannedBoardState that) {
        if (ringBearers.size() != that.ringBearers.size()) return false;
        for (Map.Entry<String, BotCard> entry : ringBearers.entrySet()) {
            BotCard thatCard = that.ringBearers.get(entry.getKey());
            if (thatCard == null) return false;
            if (!getCardFullName(entry.getValue()).equals(getCardFullName(thatCard))) return false;
            if (!entry.getValue().getSelf().getOwner().equals(thatCard.getSelf().getOwner())) return false;
        }
        return true;
    }

    private boolean compareCurrentSkirmishByFullName(PlannedBoardState that) {
        if (currentSkirmish == null && that.currentSkirmish == null) {
            return true;
        }
        if (currentSkirmish == null || that.currentSkirmish == null) {
            return false;
        }
        return getCardFullName(currentSkirmish).equals(getCardFullName(that.currentSkirmish))
                && currentSkirmish.getSelf().getOwner().equals(that.currentSkirmish.getSelf().getOwner());
    }

    private boolean compareCardMapsByFullName(Map<String, List<BotCard>> map1, Map<String, List<BotCard>> map2) {
        if (map1.size() != map2.size()) return false;
        for (Map.Entry<String, List<BotCard>> entry : map1.entrySet()) {
            List<BotCard> list2 = map2.get(entry.getKey());
            if (list2 == null) return false;
            if (!compareCardListsByFullName(entry.getValue(), list2)) return false;
        }
        return true;
    }

    private boolean compareCardSetByFullName(Set<BotCard> set1, Set<BotCard> set2) {
        if (set1.size() != set2.size()) return false;
        List<String> names1 = set1.stream()
                .map(this::getCardFullName)
                .sorted()
                .toList();
        List<String> names2 = set2.stream()
                .map(this::getCardFullName)
                .sorted()
                .toList();
        return names1.equals(names2);
    }

    private boolean compareCardListsByFullName(List<BotCard> list1, List<BotCard> list2) {
        if (list1.size() != list2.size()) return false;
        List<String> names1 = list1.stream()
                .map(card -> getCardFullName(card) + "|" + card.getSelf().getOwner())
                .sorted()
                .toList();
        List<String> names2 = list2.stream()
                .map(card -> getCardFullName(card) + "|" + card.getSelf().getOwner())
                .sorted()
                .toList();
        return names1.equals(names2);
    }

    private boolean compareAttachedCardsByFullName(PlannedBoardState that) {
        if (attachedCards.size() != that.attachedCards.size()) return false;

        // Convert to list of bearer->attachments mappings for comparison
        // We use lists instead of maps to handle multiple cards with the same name
        List<String> thisAttachments = new ArrayList<>();
        for (Map.Entry<BotCard, Set<BotCard>> entry : attachedCards.entrySet()) {
            String bearerName = getCardFullName(entry.getKey()) + "|" + entry.getKey().getSelf().getOwner();
            List<String> attachedNames = entry.getValue().stream()
                    .map(card -> getCardFullName(card) + "|" + card.getSelf().getOwner())
                    .sorted()
                    .toList();
            // Create a signature for this bearer and its attachments
            thisAttachments.add(bearerName + "=" + String.join(",", attachedNames));
        }
        Collections.sort(thisAttachments);

        List<String> thatAttachments = new ArrayList<>();
        for (Map.Entry<BotCard, Set<BotCard>> entry : that.attachedCards.entrySet()) {
            String bearerName = getCardFullName(entry.getKey()) + "|" + entry.getKey().getSelf().getOwner();
            List<String> attachedNames = entry.getValue().stream()
                    .map(card -> getCardFullName(card) + "|" + card.getSelf().getOwner())
                    .sorted()
                    .toList();
            thatAttachments.add(bearerName + "=" + String.join(",", attachedNames));
        }
        Collections.sort(thatAttachments);

        return thisAttachments.equals(thatAttachments);
    }

    private boolean compareCardTokensByFullName(PlannedBoardState that) {
        if (cardTokens.size() != that.cardTokens.size()) return false;

        // Convert to list of card->tokens mappings for comparison
        List<String> thisTokens = new ArrayList<>();
        for (Map.Entry<BotCard, Map<Token, Integer>> entry : cardTokens.entrySet()) {
            String cardName = getCardFullName(entry.getKey()) + "|" + entry.getKey().getSelf().getOwner();
            List<String> tokenList = entry.getValue().entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(e -> e.getKey() + ":" + e.getValue())
                    .toList();
            thisTokens.add(cardName + "=" + String.join(",", tokenList));
        }
        Collections.sort(thisTokens);

        List<String> thatTokens = new ArrayList<>();
        for (Map.Entry<BotCard, Map<Token, Integer>> entry : that.cardTokens.entrySet()) {
            String cardName = getCardFullName(entry.getKey()) + "|" + entry.getKey().getSelf().getOwner();
            List<String> tokenList = entry.getValue().entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(e -> e.getKey() + ":" + e.getValue())
                    .toList();
            thatTokens.add(cardName + "=" + String.join(",", tokenList));
        }
        Collections.sort(thatTokens);

        return thisTokens.equals(thatTokens);
    }

    private boolean compareAssignmentsByFullName(PlannedBoardState that) {
        if (assignments.size() != that.assignments.size()) return false;

        // Convert to list of fpCharacter->minions mappings for comparison
        List<String> thisAssignments = new ArrayList<>();
        for (Map.Entry<BotCard, Set<BotCard>> entry : assignments.entrySet()) {
            String fpCharacterName = getCardFullName(entry.getKey()) + "|" + entry.getKey().getSelf().getOwner();
            List<String> minionNames = entry.getValue().stream()
                    .map(card -> getCardFullName(card) + "|" + card.getSelf().getOwner())
                    .sorted()
                    .toList();
            // Create a signature for this FP character and assigned minions
            thisAssignments.add(fpCharacterName + "=" + String.join(",", minionNames));
        }
        Collections.sort(thisAssignments);

        List<String> thatAssignments = new ArrayList<>();
        for (Map.Entry<BotCard, Set<BotCard>> entry : that.assignments.entrySet()) {
            String fpCharacterName = getCardFullName(entry.getKey()) + "|" + entry.getKey().getSelf().getOwner();
            List<String> minionNames = entry.getValue().stream()
                    .map(card -> getCardFullName(card) + "|" + card.getSelf().getOwner())
                    .sorted()
                    .toList();
            thatAssignments.add(fpCharacterName + "=" + String.join(",", minionNames));
        }
        Collections.sort(thatAssignments);

        return thisAssignments.equals(thatAssignments);
    }

    private String getCardFullName(BotCard card) {
        return card.getSelf().getBlueprint().getFullName();
    }

    private int hashCardMapByFullName(Map<String, List<BotCard>> map) {
        int hash = 0;
        for (Map.Entry<String, List<BotCard>> entry : map.entrySet()) {
            hash += entry.getKey().hashCode();
            for (BotCard card : entry.getValue()) {
                hash += getCardFullName(card).hashCode() + card.getSelf().getOwner().hashCode();
            }
        }
        return hash;
    }

    private int hashCardSetByFullName(Set<BotCard> set) {
        int hash = 0;
        for (BotCard card : set) {
            hash += getCardFullName(card).hashCode();
        }
        return hash;
    }

    private int hashAssignmentsByFullName() {
        int hash = 0;
        for (Map.Entry<BotCard, Set<BotCard>> entry : assignments.entrySet()) {
            hash += getCardFullName(entry.getKey()).hashCode() + entry.getKey().getSelf().getOwner().hashCode();
            for (BotCard minion : entry.getValue()) {
                hash += getCardFullName(minion).hashCode() + minion.getSelf().getOwner().hashCode();
            }
        }
        return hash;
    }
}
