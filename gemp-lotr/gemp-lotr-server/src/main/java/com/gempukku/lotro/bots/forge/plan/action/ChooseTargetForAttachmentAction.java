package com.gempukku.lotro.bots.forge.plan.action;

import com.gempukku.lotro.bots.forge.cards.abstractcard.BotCard;
import com.gempukku.lotro.bots.forge.cards.abstractcard.BotObjectAttachableCard;
import com.gempukku.lotro.bots.forge.plan.PlannedBoardState;

public class ChooseTargetForAttachmentAction extends ChooseTargetAction {
    private final BotObjectAttachableCard attachment;

    public ChooseTargetForAttachmentAction(BotCard target, BotObjectAttachableCard attachment) {
        super(target);
        this.attachment = attachment;
    }

    public BotObjectAttachableCard getAttachment() {
        return attachment;
    }

    @Override
    public String toString() {
        return "Action: Choose " + getTarget().getSelf().getBlueprint().getFullName() + " as a bearer for " + attachment.getSelf().getBlueprint().getFullName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChooseTargetForAttachmentAction that = (ChooseTargetForAttachmentAction) o;
        return getTarget().getSelf().getCardId() == that.getTarget().getSelf().getCardId()
                && attachment.getSelf().getCardId() == that.attachment.getSelf().getCardId();
    }

    @Override
    public int hashCode() {
        int result = Integer.hashCode(getTarget().getSelf().getCardId());
        result = 31 * result + Integer.hashCode(attachment.getSelf().getCardId());
        return result;
    }
}
