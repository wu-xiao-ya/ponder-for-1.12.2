package net.createmod.ponder.foundation.ui;

import java.util.List;

import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.ui.render.RenderContext;

public final class ActorOverlayRenderer {

    private final RenderContext draw;

    public ActorOverlayRenderer(RenderContext draw) {
        this.draw = draw;
    }

    public void drawActorOverlay(List<ActorOverlayItem> items, float fade) {
        for (ActorOverlayItem actor : items) {
            int baseColor = actor.baseColor;
            float actorFade = actor.actorFade;

            if (actor.kind == PonderScene.ActorKind.ITEM && actor.itemStack != null && !actor.itemStack.isEmpty()) {
                draw.renderItem(actor.itemStack, actor.targetX - 8, actor.targetY - 8);
            }

            int border = withAlpha(baseColor, fade * actorFade * 235.0F);
            int fill = withAlpha(baseColor, fade * actorFade * 72.0F);
            int boxSize = actor.kind == PonderScene.ActorKind.BIRB ? 8 : 10;

            if (actor.kind != PonderScene.ActorKind.ITEM) {
                draw.drawBorderedRect(actor.targetX - boxSize / 2, actor.targetY - boxSize / 2,
                    actor.targetX + boxSize / 2 + 1, actor.targetY + boxSize / 2 + 1, fill, border);
            }

            double yawRadians = Math.toRadians(actor.yawDegrees);
            int lineX = actor.targetX + (int) Math.round(Math.sin(yawRadians) * (boxSize + 2));
            int lineY = actor.targetY - (int) Math.round(Math.cos(yawRadians) * (boxSize + 2));
            draw.drawLine(actor.targetX, actor.targetY, lineX, lineY, border, 1.5F);

            draw.renderText(actor.label, actor.targetX + boxSize / 2 + 4, actor.targetY - 4, 0xF2F5F8);
        }
    }

    private static int withAlpha(int color, float alpha) {
        int clampedAlpha = Math.min(255, Math.max(0, (int) alpha));
        return (clampedAlpha << 24) | (color & 0x00FFFFFF);
    }
}
