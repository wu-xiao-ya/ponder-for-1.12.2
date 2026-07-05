package net.createmod.ponder.foundation.ui;

import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.ui.PonderScenePreview.PreviewBounds;
import net.createmod.ponder.foundation.ui.render.RenderContext;

final class PoiOverlayRenderer {

    private final PonderOverlayLayoutHelper overlayLayoutHelper;
    private final RenderContext draw;

    PoiOverlayRenderer(PonderOverlayLayoutHelper overlayLayoutHelper, RenderContext draw) {
        this.overlayLayoutHelper = overlayLayoutHelper;
        this.draw = draw;
    }

    void draw(PonderScene scene, PreviewBounds bounds, PreviewLayout layout, float currentTick, float fade) {
        PonderScene.PoiEvent poiEvent = getActivePoiEvent(scene, currentTick);
        if (poiEvent == null) {
            return;
        }

        SpeechRenderer.Point target =
            overlayLayoutHelper.projectScenePoint(scene, bounds, layout, currentTick, poiEvent.getLocation());
        if (target == null) {
            return;
        }

        int accent = withAlpha(PonderPalette.RED.getColor(), fade * 235.0F);
        int fill = withAlpha(PonderPalette.RED.getColor(), fade * 72.0F);
        draw.drawCrossMarker(target.x, target.y, 5, 2, accent, fill);
        draw.renderText("POI", target.x + 8, target.y - 4, 0xF2F5F8);
    }

    private static PonderScene.PoiEvent getActivePoiEvent(PonderScene scene, float currentTick) {
        if (scene == null) {
            return null;
        }
        PonderScene.PoiEvent active = null;
        for (PonderScene.PoiEvent event : scene.getPointOfInterestEvents()) {
            if (event.getTick() <= currentTick) {
                active = event;
            } else {
                break;
            }
        }
        return active;
    }

    private static int withAlpha(int color, float alpha) {
        int clampedAlpha = Math.min(255, Math.max(0, (int) alpha));
        return (clampedAlpha << 24) | (color & 0x00FFFFFF);
    }
}
