package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.effects.choose.ChooseOpponentEffect;
import com.gempukku.lotro.logic.timing.Effect;
import org.json.simple.JSONObject;

public class ChooseOpponent implements EffectAppenderProducer {
	@Override
	public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
		FieldUtils.validateAllowedFields(effectObject, "memorize");

		final String memorize = FieldUtils.getString(effectObject.get("memorize"), "memorize");

		return new DelayedAppender() {
			@Override
			protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
				return new ChooseOpponentEffect(actionContext.getPerformingPlayer()) {
					@Override
					protected void opponentChosen(String opponentId) {
						actionContext.setValueToMemory(memorize, opponentId);
					}
				};
			}
		};
	}

}