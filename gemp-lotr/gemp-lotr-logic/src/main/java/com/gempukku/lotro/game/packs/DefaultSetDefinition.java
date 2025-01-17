package com.gempukku.lotro.game.packs;

import java.util.*;

public class DefaultSetDefinition implements SetDefinition {
    private final List<String> _tengwarCards = new LinkedList<>();
    private final Map<String, List<String>> _rarityList = new HashMap<>();
    private final Map<String, String> _cardsRarity = new LinkedHashMap<>();
    private final String _setId;
    private final String _setName;
    private final Set<String> _flags;

    public DefaultSetDefinition(String setId, String setName, Set<String> flags) {
        _setId = setId;
        _setName = setName;
        _flags = flags;
    }

    public void addCard(String blueprintId, String rarity) {
        _cardsRarity.put(blueprintId, rarity);
        List<String> cardsOfRarity = _rarityList.get(rarity);
        if (cardsOfRarity == null) {
            cardsOfRarity = new LinkedList<>();
            _rarityList.put(rarity, cardsOfRarity);
        }
        cardsOfRarity.add(blueprintId);
    }

    public void addTengwarCard(String blueprintId) {
        _tengwarCards.add(blueprintId);
    }

    @Override
    public String getSetName() {
        return _setName;
    }

    @Override
    public String getSetId() {
        return _setId;
    }

    @Override
    public boolean hasFlag(String flag) {
        return _flags.contains(flag);
    }

    @Override
    public List<String> getCardsOfRarity(String rarity) {
        final List<String> list = _rarityList.get(rarity);
        if (list == null)
            return Collections.emptyList();
        return Collections.unmodifiableList(list);
    }

    @Override
    public List<String> getTengwarCards() {
        if (_tengwarCards == null)
            return Collections.emptyList();
        return Collections.unmodifiableList(_tengwarCards);
    }

    @Override
    public String getCardRarity(String cardId) {
        return _cardsRarity.get(cardId);
    }

    @Override
    public Set<String> getAllCards() {
        return Collections.unmodifiableSet(_cardsRarity.keySet());
    }
}
