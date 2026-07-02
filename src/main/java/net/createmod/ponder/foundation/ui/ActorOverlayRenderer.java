package net.createmod.ponder.foundation.ui;

import java.util.List;
import net.createmod.ponder.foundation.PonderScene;

public final class ActorOverlayRenderer {

    private final DrawContext draw;

    public ActorOverlayRenderer(DrawContext draw) {
        this.draw = draw;
    }

    public void drawActorOverlay(List<ActorOverlayItem> items, float fade) {
        for (ActorOverlayItem actor : items) {
            int baseColor = actor.baseColor;
            float actorFade = actor.actorFade;

            if (actor.kind == PonderScene.ActorKind.ITEM && actor.itemStack != null && !actor.itemStack.isEmpty()) {
                draw.renderItemStack(actor.itemStack, actor.targetX - 8, actor.targetY - 8);
            }

            int border = draw.withAlpha(baseColor, fade * actorFade * 235.0F);
            int fill = draw.withAlpha(baseColor, fade * actorFade * 72.0F);
            int boxSize = actor.kind == PonderScene.ActorKind.BIRB ? 8 : 10;

            if (actor.kind != PonderScene.ActorKind.ITEM) {
                draw.drawBorderedRect(actor.targetX - boxSize / 2, actor.targetY - boxSize / 2,
                    actor.targetX + boxSize / 2 + 1, actor.targetY + boxSize / 2 + 1, fill, border);
            }

            double yawRadians = Math.toRadians(actor.yawDegrees);
            int lineX = actor.targetX + (int) Math.round(Math.sin(yawRadians) * (boxSize + 2));
            int lineY = actor.targetY - (int) Math.round(Math.cos(yawRadians) * (boxSize + 2));
            draw.drawLineSegment(actor.targetX, actor.targetY, lineX, lineY, border, 1.5F);

            draw.drawString(actor.label, actor.targetX + boxSize / 2 + 4, actor.targetY - 4, 0xF2F5F8);
        }
    }
}
