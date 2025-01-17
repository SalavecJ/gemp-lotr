package com.gempukku.lotro.collection;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.db.CollectionDAO;
import com.gempukku.lotro.db.PlayerDAO;
import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.game.*;
import com.gempukku.lotro.packs.PacksStorage;
import com.gempukku.lotro.packs.ProductLibrary;
import com.google.common.collect.Iterables;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class CollectionsManager {
    private final ReentrantReadWriteLock _readWriteLock = new ReentrantReadWriteLock();

    private final PlayerDAO _playerDAO;
    private final CollectionDAO _collectionDAO;
    private final TransferDAO _transferDAO;
    private final LotroCardBlueprintLibrary lotroCardBlueprintLibrary;

    public CollectionsManager(PlayerDAO playerDAO, CollectionDAO collectionDAO, TransferDAO transferDAO, final LotroCardBlueprintLibrary lotroCardBlueprintLibrary) {
        _playerDAO = playerDAO;
        _collectionDAO = collectionDAO;
        _transferDAO = transferDAO;
        this.lotroCardBlueprintLibrary = lotroCardBlueprintLibrary;
    }

    public CardCollection getDefaultCollection() {
        return new CardCollection() {
            @Override
            public int getCurrency() {
                return 0;
            }

            @Override
            public Iterable<Item> getAll() {
                return lotroCardBlueprintLibrary.getBaseCards().entrySet().stream().map(cardBlueprintEntry -> {
                    String blueprintId = cardBlueprintEntry.getKey();
                    int count = getCount(cardBlueprintEntry.getValue());
                    return Item.createItem(blueprintId, count);
                }).collect(Collectors.toList());
            }

            @Override
            public int getItemCount(String blueprintId) {
                final String baseBlueprintId = lotroCardBlueprintLibrary.getBaseBlueprintId(blueprintId);
                if (baseBlueprintId.equals(blueprintId)) {
                    try {
                        return getCount(lotroCardBlueprintLibrary.getLotroCardBlueprint(blueprintId));
                    } catch (CardNotFoundException exp) {
                        return 0;
                    }
                }
                return 0;
            }

            private int getCount(LotroCardBlueprint blueprint) {
                final CardType cardType = blueprint.getCardType();
                if (cardType == CardType.SITE || cardType == CardType.THE_ONE_RING)
                    return 1;
                return 4;
            }

            @Override
            public Map<String, Object> getExtraInformation() {
                return Collections.emptyMap();
            }
        };
    }

    public CardCollection getPlayerCollection(String playerName, String collectionType) {
        return getPlayerCollection(_playerDAO.getPlayer(playerName), collectionType);
    }

    public CardCollection getPlayerCollection(Player player, String collectionType) {
        _readWriteLock.readLock().lock();
        try {
            if (collectionType.contains("+"))
                return createSumCollection(player, collectionType.split("\\+"));

            if (collectionType.equals("default"))
                return getDefaultCollection();

            final CardCollection collection = _collectionDAO.getPlayerCollection(player.getId(), collectionType);

            if (collection == null && (collectionType.equals(CollectionType.MY_CARDS.getCode()) || collectionType.equals(CollectionType.TROPHY.getCode())))
                return new DefaultCardCollection();

            return collection;
        } catch (SQLException | IOException exp) {
            throw new RuntimeException("Unable to get player collection", exp);
        } finally {
            _readWriteLock.readLock().unlock();
        }
    }

    private CardCollection createSumCollection(Player player, String[] collectionTypes) {
        List<CardCollection> collections = new LinkedList<>();
        for (String collectionType : collectionTypes)
            collections.add(getPlayerCollection(player, collectionType));

        return new SumCardCollection(collections);
    }

    private void setPlayerCollection(Player player, String collectionType, CardCollection cardCollection) {
        if (collectionType.contains("+"))
            throw new IllegalArgumentException("Invalid collection type: " + collectionType);
        try {
            _collectionDAO.setPlayerCollection(player.getId(), collectionType, cardCollection);
        } catch (SQLException | IOException exp) {
            throw new RuntimeException("Unable to store player collection", exp);
        }
    }

    public void addPlayerCollection(boolean notifyPlayer, String reason, String player, CollectionType collectionType, CardCollection cardCollection) {
        addPlayerCollection(notifyPlayer, reason, _playerDAO.getPlayer(player), collectionType, cardCollection);
    }

    public void addPlayerCollection(boolean notifyPlayer, String reason, Player player, CollectionType collectionType, CardCollection cardCollection) {
        if (collectionType.getCode().contains("+"))
            throw new IllegalArgumentException("Invalid collection type: " + collectionType);

        _readWriteLock.writeLock().lock();
        try {
            setPlayerCollection(player, collectionType.getCode(), cardCollection);
            _transferDAO.addTransferTo(notifyPlayer, player.getName(), reason, collectionType.getFullName(), cardCollection.getCurrency(), cardCollection);
        } finally {
            _readWriteLock.writeLock().unlock();
        }
    }

    public Map<Player, CardCollection> getPlayersCollection(String collectionType) {
        if (collectionType.contains("+"))
            throw new IllegalArgumentException("Invalid collection type: " + collectionType);

        _readWriteLock.readLock().lock();
        try {
            final Map<Integer, CardCollection> playerCollectionsByType = _collectionDAO.getPlayerCollectionsByType(collectionType);

            Map<Player, CardCollection> result = new HashMap<>();
            for (Map.Entry<Integer, CardCollection> playerCollection : playerCollectionsByType.entrySet())
                result.put(_playerDAO.getPlayer(playerCollection.getKey()), playerCollection.getValue());

            return result;
        } catch (SQLException | IOException exp) {
            throw new RuntimeException("Unable to get players collection", exp);
        } finally {
            _readWriteLock.readLock().unlock();
        }
    }

    public CardCollection openPackInPlayerCollection(Player player, CollectionType collectionType, String selection, ProductLibrary productLibrary, String packId) {
        _readWriteLock.writeLock().lock();
        try {
            final CardCollection playerCollection = getPlayerCollection(player, collectionType.getCode());
            if (playerCollection == null)
                return null;
            MutableCardCollection mutableCardCollection = new DefaultCardCollection(playerCollection);

            final CardCollection packContents = mutableCardCollection.openPack(packId, selection, productLibrary);
            if (packContents != null) {
                setPlayerCollection(player, collectionType.getCode(), mutableCardCollection);

                String reason = "Opened pack";
                _transferDAO.addTransferFrom(player.getName(), reason, collectionType.getFullName(), 0, cardCollectionFromBlueprintId(1, packId));
                _transferDAO.addTransferTo(true, player.getName(), reason, collectionType.getFullName(), packContents.getCurrency(), packContents);
            }
            return packContents;
        } finally {
            _readWriteLock.writeLock().unlock();
        }
    }

    private CardCollection cardCollectionFromBlueprintId(int count, String blueprintId) {
        DefaultCardCollection result = new DefaultCardCollection();
        result.addItem(blueprintId, count);
        return result;
    }

    public void addItemsToPlayerCollection(boolean notifyPlayer, String reason, Player player, CollectionType collectionType, Iterable<CardCollection.Item> items, Map<String, Object> extraInformation) {
        _readWriteLock.writeLock().lock();
        try {
            final CardCollection playerCollection = getPlayerCollection(player, collectionType.getCode());
            if (playerCollection != null) {
                MutableCardCollection mutableCardCollection = new DefaultCardCollection(playerCollection);
                MutableCardCollection addedCards = new DefaultCardCollection();
                for (CardCollection.Item item : items) {
                    mutableCardCollection.addItem(item.getBlueprintId(), item.getCount());
                    addedCards.addItem(item.getBlueprintId(), item.getCount());
                }

                if (extraInformation != null) {
                    Map<String, Object> resultExtraInformation = new HashMap<>(playerCollection.getExtraInformation());
                    resultExtraInformation.putAll(extraInformation);
                    mutableCardCollection.setExtraInformation(resultExtraInformation);
                }

                setPlayerCollection(player, collectionType.getCode(), mutableCardCollection);
                _transferDAO.addTransferTo(notifyPlayer, player.getName(), reason, collectionType.getFullName(), 0, addedCards);
            }
        } finally {
            _readWriteLock.writeLock().unlock();
        }
    }

    public void addItemsToPlayerCollection(boolean notifyPlayer, String reason, Player player, CollectionType collectionType, Iterable<CardCollection.Item> items) {
        addItemsToPlayerCollection(notifyPlayer, reason, player, collectionType, items, null);
    }

    public void addItemsToPlayerCollection(boolean notifyPlayer, String reason, String player, CollectionType collectionType, Iterable<CardCollection.Item> items) {
        addItemsToPlayerCollection(notifyPlayer, reason, _playerDAO.getPlayer(player), collectionType, items);
    }

    public boolean tradeCards(Player player, CollectionType collectionType, String removeBlueprintId, int removeCount, String addBlueprintId, int addCount, int currencyCost) {
        _readWriteLock.writeLock().lock();
        try {
            final CardCollection playerCollection = getPlayerCollection(player, collectionType.getCode());
            if (playerCollection != null) {
                MutableCardCollection mutableCardCollection = new DefaultCardCollection(playerCollection);
                if (!mutableCardCollection.removeItem(removeBlueprintId, removeCount))
                    return false;
                if (!mutableCardCollection.removeCurrency(currencyCost))
                    return false;
                mutableCardCollection.addItem(addBlueprintId, addCount);

                setPlayerCollection(player, collectionType.getCode(), mutableCardCollection);

                DefaultCardCollection newCards = new DefaultCardCollection();
                newCards.addItem(addBlueprintId, addCount);

                String reason = "Trading items";
                _transferDAO.addTransferFrom(player.getName(), reason, collectionType.getFullName(), currencyCost, cardCollectionFromBlueprintId(removeCount, removeBlueprintId));
                _transferDAO.addTransferTo(true, player.getName(), reason, collectionType.getFullName(), 0, cardCollectionFromBlueprintId(addCount, addBlueprintId));

                return true;
            }
            return false;
        } finally {
            _readWriteLock.writeLock().unlock();
        }
    }

    public boolean buyCardToPlayerCollection(Player player, CollectionType collectionType, String blueprintId, int currency) {
        _readWriteLock.writeLock().lock();
        try {
            final CardCollection playerCollection = getPlayerCollection(player, collectionType.getCode());
            if (playerCollection != null) {
                MutableCardCollection mutableCardCollection = new DefaultCardCollection(playerCollection);
                if (!mutableCardCollection.removeCurrency(currency))
                    return false;
                mutableCardCollection.addItem(blueprintId, 1);

                setPlayerCollection(player, collectionType.getCode(), mutableCardCollection);

                String reason = "Items bought";
                _transferDAO.addTransferFrom(player.getName(), reason, collectionType.getFullName(), currency, new DefaultCardCollection());
                _transferDAO.addTransferTo(true, player.getName(), reason, collectionType.getFullName(), 0, cardCollectionFromBlueprintId(1, blueprintId));

                return true;
            }
            return false;
        } finally {
            _readWriteLock.writeLock().unlock();
        }
    }

    public boolean sellCardInPlayerCollection(Player player, CollectionType collectionType, String blueprintId, int currency) {
        _readWriteLock.writeLock().lock();
        try {
            final CardCollection playerCollection = getPlayerCollection(player, collectionType.getCode());
            if (playerCollection != null) {
                MutableCardCollection mutableCardCollection = new DefaultCardCollection(playerCollection);
                if (!mutableCardCollection.removeItem(blueprintId, 1))
                    return false;
                mutableCardCollection.addCurrency(currency);

                setPlayerCollection(player, collectionType.getCode(), mutableCardCollection);

                _transferDAO.addTransferFrom(player.getName(), "Selling items", collectionType.getFullName(), 0, cardCollectionFromBlueprintId(1, blueprintId));
                _transferDAO.addTransferTo(false, player.getName(), "Selling items", collectionType.getFullName(), currency, new DefaultCardCollection());

                return true;
            }
            return false;
        } finally {
            _readWriteLock.writeLock().unlock();
        }
    }

    public void addCurrencyToPlayerCollection(boolean notifyPlayer, String reason, String player, CollectionType collectionType, int currency) {
        addCurrencyToPlayerCollection(notifyPlayer, reason, _playerDAO.getPlayer(player), collectionType, currency);
    }

    public void addCurrencyToPlayerCollection(boolean notifyPlayer, String reason, Player player, CollectionType collectionType, int currency) {
        if (currency > 0) {
            _readWriteLock.writeLock().lock();
            try {
                final CardCollection playerCollection = getPlayerCollection(player, collectionType.getCode());
                if (playerCollection != null) {
                    MutableCardCollection mutableCardCollection = new DefaultCardCollection(playerCollection);
                    mutableCardCollection.addCurrency(currency);

                    setPlayerCollection(player, collectionType.getCode(), mutableCardCollection);

                    DefaultCardCollection newCurrency = new DefaultCardCollection();
                    newCurrency.addCurrency(currency);

                    _transferDAO.addTransferTo(notifyPlayer, player.getName(), reason, collectionType.getFullName(), currency, new DefaultCardCollection());
                }
            } finally {
                _readWriteLock.writeLock().unlock();
            }
        }
    }

    public boolean removeCurrencyFromPlayerCollection(String reason, Player player, CollectionType collectionType, int currency) {
        _readWriteLock.writeLock().lock();
        try {
            final CardCollection playerCollection = getPlayerCollection(player, collectionType.getCode());
            if (playerCollection != null) {
                MutableCardCollection mutableCardCollection = new DefaultCardCollection(playerCollection);
                if (mutableCardCollection.removeCurrency(currency)) {
                    setPlayerCollection(player, collectionType.getCode(), mutableCardCollection);

                    _transferDAO.addTransferFrom(player.getName(), reason, collectionType.getFullName(), currency, new DefaultCardCollection());

                    return true;
                }
            }
            return false;
        } finally {
            _readWriteLock.writeLock().unlock();
        }
    }
}
