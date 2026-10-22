package com.gempukku.lotro.bots.v2.decision.option;

import com.gempukku.lotro.game.PhysicalCard;

public class RequiredResponseOption extends Option {
    private final PhysicalCard source;

    public RequiredResponseOption(PhysicalCard source, String text, int actionIndex) {
        //Text example: Required trigger from <div class='cardHint' value='1_150'>Uruk Rager</div>
        super(text.replaceAll("<[^>]*>", ""), String.valueOf(actionIndex));
        this.source = source;
    }

    public PhysicalCard getSource() {
        return source;
    }
}
