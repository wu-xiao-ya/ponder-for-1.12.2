package net.createmod.ponder.foundation.ui;

import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.ui.PonderScenePreview.PreviewBounds;

final class PoiOverlayRenderer {

    private final PonderOverlayLayoutHelper overlayLayoutHelper;
    private final DrawContext draw;

    PoiOverlayRenderer(PonderOverlayLayoutHelper overlayLayoutHelper, DrawContext draw) {
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

        int accent = draw.withAlpha(PonderPalette.RED.getColor(), fade * 235.0F);
        int fill = draw.withAlpha(PonderPalette.RED.getColor(), fade * 72.0F);
        draw.drawCrossMarker(target.x, target.y, 5, 2, accent, fill);
        draw.drawString("POI", target.x + 8, target.y - 4, 0xF2F5F8);
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
}
