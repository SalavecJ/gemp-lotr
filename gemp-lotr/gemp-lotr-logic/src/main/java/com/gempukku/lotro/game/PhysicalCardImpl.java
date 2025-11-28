package com.gempukku.lotro.game;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.ModifierHook;

import java.util.LinkedList;
import java.util.List;

public class PhysicalCardImpl implements PhysicalCard {
    private int _cardId;
    private final String _blueprintId;
    private final String _owner;
    private String _cardController;
    private Zone _zone;
    private Zone _playedFromZone;
    private final LotroCardBlueprint _blueprint;

    private PhysicalCardImpl _attachedTo;
    private PhysicalCardImpl _stackedOn;

    private List<ModifierHook> _modifierHooks;
    private List<ModifierHook> _modifierHooksStacked;
    private List<ModifierHook> _modifierHooksInDiscard;
    private List<ModifierHook> _modifierHooksControlledSite;
    private List<ModifierHook> _modifierHooksPermanentSite;

    private WhileInZoneData _whileInZoneData;

    private Integer _siteNumber;

    private final LotroGame game;

    public PhysicalCardImpl(int cardId, String blueprintId, String owner, LotroCardBlueprint blueprint, LotroGame game) {
        _cardId = cardId;
        _blueprintId = blueprintId;
        _owner = owner;
        _blueprint = blueprint;
        this.game = game;
    }

    public PhysicalCardImpl(int cardId, String blueprintId, String owner, LotroCardBlueprint blueprint) {
        _cardId = cardId;
        _blueprintId = blueprintId;
        _owner = owner;
        _blueprint = blueprint;
        this.game = null;
    }

    /**
     * Copy constructor for creating a deep copy of a PhysicalCard.
     * This is used for game tree search without replaying decisions.
     * Note: Modifier hooks are NOT copied as they will be recreated when needed.
     * Attachment and stacking relationships are NOT set here - they must be fixed up
     * after all cards are copied to maintain proper references.
     *
     * @param original The original PhysicalCardImpl to copy
     * @param newGame The new game instance this card will belong to
     */
    public PhysicalCardImpl(PhysicalCardImpl original, LotroGame newGame) {
        _cardId = original._cardId;
        _blueprintId = original._blueprintId;
        _owner = original._owner;
        _cardController = original._cardController;
        _zone = original._zone;
        _playedFromZone = original._playedFromZone;
        _blueprint = original._blueprint;

        // Note: _attachedTo and _stackedOn will be set after all cards are copied
        // to maintain proper references between copied cards
        _attachedTo = null;
        _stackedOn = null;

        // Modifier hooks are NOT copied - they will be recreated when the card
        // starts affecting the game in the copied state
        _modifierHooks = null;
        _modifierHooksStacked = null;
        _modifierHooksInDiscard = null;
        _modifierHooksControlledSite = null;
        _modifierHooksPermanentSite = null;

        // Copy while in zone data if present
        _whileInZoneData = original._whileInZoneData;

        _siteNumber = original._siteNumber;

        // Set the game reference to the new game instance
        this.game = newGame;
    }

    public void setCardId(int cardId) {
        _cardId = cardId;
    }

    @Override
    public String getBlueprintId() {
        return _blueprintId;
    }

    public void setZone(Zone zone) {
        _zone = zone;
    }

    @Override
    public Zone getZone() {
        return _zone;
    }

    @Override
    public void setPlayedFromZone(Zone zone) {
        _playedFromZone = zone;
    }
    @Override
    public Zone getPlayedFromZone() {
        return _playedFromZone;
    }

    @Override
    public String getOwner() {
        return _owner;
    }

    public void setCardController(String siteController) {
        _cardController = siteController;
    }

    @Override
    public String getCardController() {
        return _cardController;
    }

    public void startAffectingGame(LotroGame game) {
        List<? extends Modifier> modifiers = _blueprint.getInPlayModifiers(game, this);
        if (modifiers != null) {
            _modifierHooks = new LinkedList<>();
            for (Modifier modifier : modifiers)
                _modifierHooks.add(game.getModifiersEnvironment().addAlwaysOnModifier(modifier));
        }
    }

    public void stopAffectingGame() {
        if (_modifierHooks != null) {
            for (ModifierHook modifierHook : _modifierHooks)
                modifierHook.stop();
            _modifierHooks = null;
        }
    }

    public void startAffectingGameStacked(LotroGame game) {
        List<? extends Modifier> modifiers = _blueprint.getStackedOnModifiers(game, this);
        if (modifiers != null) {
            _modifierHooksStacked = new LinkedList<>();
            for (Modifier modifier : modifiers)
                _modifierHooksStacked.add(game.getModifiersEnvironment().addAlwaysOnModifier(modifier));
        }
    }

    public void stopAffectingGameStacked() {
        if (_modifierHooksStacked != null) {
            for (ModifierHook modifierHook : _modifierHooksStacked)
                modifierHook.stop();
            _modifierHooksStacked = null;
        }
    }

    public void startAffectingGameInDiscard(LotroGame game) {
        List<? extends Modifier> modifiers = _blueprint.getInDiscardModifiers(game, this);
        if (modifiers != null) {
            _modifierHooksInDiscard = new LinkedList<>();
            for (Modifier modifier : modifiers)
                _modifierHooksInDiscard.add(game.getModifiersEnvironment().addAlwaysOnModifier(modifier));
        }
    }

    public void stopAffectingGameInDiscard() {
        if (_modifierHooksInDiscard != null) {
            for (ModifierHook modifierHook : _modifierHooksInDiscard)
                modifierHook.stop();
            _modifierHooksInDiscard = null;
        }
    }

    public void startAffectingGamePermanentSite(LotroGame game) {
        List<? extends Modifier> modifiers = _blueprint.getPermanentSiteModifiers(game, this);
        if (modifiers != null) {
            _modifierHooksPermanentSite = new LinkedList<>();
            for (Modifier modifier : modifiers)
                _modifierHooksPermanentSite.add(game.getModifiersEnvironment().addAlwaysOnModifier(modifier));
        }
    }

    public void stopAffectingGameControlledSite() {
        if (_modifierHooksControlledSite != null) {
            for (ModifierHook modifierHook : _modifierHooksControlledSite)
                modifierHook.stop();
            _modifierHooksControlledSite = null;
        }
    }

    public void startAffectingGameControlledSite(LotroGame game) {
        List<? extends Modifier> modifiers = _blueprint.getControlledSiteModifiers(game, this);
        if (modifiers != null) {
            _modifierHooksControlledSite = new LinkedList<>();
            for (Modifier modifier : modifiers)
                _modifierHooksControlledSite.add(game.getModifiersEnvironment().addAlwaysOnModifier(modifier));
        }
    }

    @Override
    public int getCardId() {
        return _cardId;
    }

    @Override
    public LotroCardBlueprint getBlueprint() {
        return _blueprint;
    }

    public void attachTo(PhysicalCardImpl physicalCard) {
        _attachedTo = physicalCard;
    }

    @Override
    public PhysicalCard getAttachedTo() {
        return _attachedTo;
    }

    public void stackOn(PhysicalCardImpl physicalCard) {
        _stackedOn = physicalCard;
    }

    @Override
    public PhysicalCard getStackedOn() {
        return _stackedOn;
    }

    @Override
    public WhileInZoneData getWhileInZoneData() {
        return _whileInZoneData;
    }

    @Override
    public void setWhileInZoneData(WhileInZoneData whileInZoneData) {
        _whileInZoneData = whileInZoneData;
    }

    @Override
    public Integer getSiteNumber() {
        return _siteNumber;
    }

    @Override
    public void setSiteNumber(Integer number) {
        _siteNumber = number;
    }

    @Override
    public LotroGame getGame() {
        return game;
    }
}
