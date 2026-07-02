package net.createmod.ponder.foundation.ui;

import java.util.List;

import javax.annotation.Nullable;

import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.ui.overlay.OverlayPlacementEngine;
import net.createmod.ponder.foundation.ui.projection.CaptionPlacement;
import net.createmod.ponder.foundation.ui.projection.GuiHighlightPlacement;
import net.createmod.ponder.foundation.ui.projection.GuiOverlayPlacement;
import net.createmod.ponder.foundation.ui.projection.SceneProjectionContext;

public final class PonderOverlayHelper {

    private static final OverlayPlacementEngine OVERLAY_PLACEMENT_ENGINE = new OverlayPlacementEngine();

    private PonderOverlayHelper() {
    }

    // -- overlay fade helper -- //

    public static float computeOverlayFade(int startTick, int duration, float currentTick) {
        float fadeIn = MathHelper.clamp((currentTick - startTick + 1.0F) / 6.0F, 0.0F, 1.0F);
        float fadeOut = MathHelper.clamp((startTick + duration - currentTick) / 6.0F, 0.0F, 1.0F);
        return Math.min(fadeIn, fadeOut);
    }

    // -- coordinate helpers -- //

    public static int resolveManualCaptionCoordinate(int coordinate, int origin, int size, int boxSize) {
        return OVERLAY_PLACEMENT_ENGINE.resolveManualCaptionCoordinate(coordinate, origin, size, boxSize);
    }

    // -- rect intersection -- //

    public static boolean rectsIntersect(int ax, int ay, int aw, int ah, int bx, int by, int bw, int bh) {
        return ax < bx + bw && ax + aw > bx && ay < by + bh && ay + ah > by;
    }

    @Nullable
    public static SpeechRenderer.Point resolveCaptionTargetPoint(SceneProjectionContext context,
        PonderScene.OverlayEvent overlayEvent, List<GuiHighlightPlacement> guiHighlightPlacements) {
        return OVERLAY_PLACEMENT_ENGINE.resolveCaptionTargetPoint(context, overlayEvent, guiHighlightPlacements);
    }

    public static SpeechRenderer.SpeechPointing choosePointing(int boxX, int boxY, int boxWidth, int boxHeight,
        SpeechRenderer.Point targetPoint) {
        return OVERLAY_PLACEMENT_ENGINE.choosePointing(boxX, boxY, boxWidth, boxHeight, targetPoint);
    }

    // -- overlay overlap avoidance -- //

    public static CaptionPlacement avoidOverlayOverlap(int boxX, int boxY, int boxWidth, int boxHeight,
        SpeechRenderer.SpeechPointing pointing, int previewY, int maxBoxY, List<GuiOverlayPlacement> placements) {
        return OVERLAY_PLACEMENT_ENGINE.avoidOverlayOverlap(boxX, boxY, boxWidth, boxHeight, pointing, previewY,
            maxBoxY, placements);
    }

    // -- overlay placement computation -- //

    public static List<GuiOverlayPlacement> computeActiveGuiOverlayPlacements(SceneProjectionContext context) {
        return OVERLAY_PLACEMENT_ENGINE.computeActiveGuiOverlayPlacements(context);
    }

    // -- highlight placement computation -- //

    public static List<GuiHighlightPlacement> computeActiveGuiHighlightPlacements(SceneProjectionContext context,
        List<GuiOverlayPlacement> guiPlacements) {
        return OVERLAY_PLACEMENT_ENGINE.computeActiveGuiHighlightPlacements(context, guiPlacements);
    }
}
