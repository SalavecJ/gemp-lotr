package com.gempukku.lotro.bots.forge.plan.action;

import com.gempukku.lotro.bots.forge.cards.abstractcard.BotCard;

public class ChooseTargetForAttachmentAction extends ChooseTargetAction {
    private final BotCard attachment;

    public ChooseTargetForAttachmentAction(BotCard target, BotCard attachment) {
        super(target);
        this.attachment = attachment;
    }

    public BotCard getAttachment() {
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
