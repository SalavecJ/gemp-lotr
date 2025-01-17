package com.gempukku.lotro.library;

import com.gempukku.lotro.at.AbstractAtTest;
import com.gempukku.lotro.game.CardCollection;
import com.gempukku.lotro.game.DefaultAdventureLibrary;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.league.SealedLeagueProduct;
import com.gempukku.lotro.league.SealedLeagueType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;

public class FormatLibraryTests extends AbstractAtTest {

    protected static LotroFormatLibrary _formatLibrary = new LotroFormatLibrary(new DefaultAdventureLibrary(), _cardLibrary);
    protected static SealedLeagueProduct _sealedLeagueProduct = new SealedLeagueProduct();

    @ParameterizedTest(name = "{0} in LotroFormatLibrary matches SealedLeagueProduct.")
    @CsvSource(value = {
            "Fellowship Block Sealed,fotr_block",
            "Towers Block Sealed,ttt_block",
            "Towers Standard Special Sealed,ts_special",
            "King Block Sealed,movie",
            "Movie Special Sealed,movie_special",
            "War of the Ring Block Sealed,war_block",
            "Hunters Block Sealed,hunters_block",
    })
    public void SealedLeagueProductComparison(String sealedName, String formatCode) {
        var oldDef = _sealedLeagueProduct.getAllSeriesForLeague(formatCode);
        var newDef = _formatLibrary.GetSealedTemplate(sealedName);

        var format = SealedLeagueType.getLeagueType(formatCode);

        assertNotNull(oldDef);
        assertNotNull(newDef);

        assertNotNull(format);
        assertEquals(format.getFormat(), newDef.GetFormat().getCode());

        var oldList = oldDef.stream().map(CardCollection::getAll).toList();
        var newList = newDef.GetAllSeriesProducts();

        assertEquals(oldList.size(), newList.size());

        for (int i = 0; i < oldList.size(); ++i) {
            var oldItem = new ArrayList<>();
            oldList.get(i).forEach(oldItem::add);
            var newItem = newList.get(i);

            assertEquals(oldItem, newItem);
        }
    }

}