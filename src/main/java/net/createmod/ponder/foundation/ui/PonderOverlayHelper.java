package net.createmod.ponder.foundation.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.ui.PonderScenePreview.PreviewBounds;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

final class CaptionPlacement {
    final int x;
    final int y;
    final SpeechRenderer.SpeechPointing pointing;

    CaptionPlacement(int x, int y, SpeechRenderer.SpeechPointing pointing) {
        this.x = x;
        this.y = y;
        this.pointing = pointing;
    }
}

final class GuiOverlayPlacement {
    final PonderScene.OverlayEvent overlayEvent;
    final SpeechRenderer.Point targetPoint;
    final boolean aboveTarget;
    final int panelX;
    final int panelY;
    final int panelWidth;
    final int panelHeight;
    final int drawX;
    final int drawY;
    final int drawWidth;
    final int drawHeight;

    GuiOverlayPlacement(PonderScene.OverlayEvent overlayEvent, SpeechRenderer.Point targetPoint, boolean aboveTarget,
        int panelX, int panelY, int panelWidth, int panelHeight, int drawX, int drawY, int drawWidth,
        int drawHeight) {
        this.overlayEvent = overlayEvent;
        this.targetPoint = targetPoint;
        this.aboveTarget = aboveTarget;
        this.panelX = panelX;
        this.panelY = panelY;
        this.panelWidth = panelWidth;
        this.panelHeight = panelHeight;
        this.drawX = drawX;
        this.drawY = drawY;
        this.drawWidth = drawWidth;
        this.drawHeight = drawHeight;
    }
}

final class GuiHighlightPlacement {
    final PonderScene.OverlayEvent overlayEvent;
    final int rectX;
    final int rectY;
    final int rectWidth;
    final int rectHeight;
    final SpeechRenderer.Point targetPoint;

    GuiHighlightPlacement(PonderScene.OverlayEvent overlayEvent, int rectX, int rectY, int rectWidth,
        int rectHeight, SpeechRenderer.Point targetPoint) {
        this.overlayEvent = overlayEvent;
        this.rectX = rectX;
        this.rectY = rectY;
        this.rectWidth = rectWidth;
        this.rectHeight = rectHeight;
        this.targetPoint = targetPoint;
    }
}

@FunctionalInterface
interface ScenePointProjector {
    @Nullable
    SpeechRenderer.Point project(PonderScene scene, PreviewBounds bounds, PreviewLayout layout, float renderTick, Vec3d point);
}

public final class PonderOverlayHelper {

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
        return coordinate >= 0 ? origin + coordinate : origin + size - boxSize + coordinate;
    }

    // -- rect intersection -- //

    public static boolean rectsIntersect(int ax, int ay, int aw, int ah, int bx, int by, int bw, int bh) {
        return ax < bx + bw && ax + aw > bx && ay < by + bh && ay + ah > by;
    }

    public static SpeechRenderer.Point resolveCaptionTargetPoint(PonderScene scene, PreviewLayout layout, float currentTick,
        PonderScene.OverlayEvent overlayEvent, List<GuiHighlightPlacement> guiHighlightPlacements,
        ScenePointProjector projector) {
        if (scene == null || overlayEvent == null || layout == null) {
            return null;
        }

        if (overlayEvent.getType() == PonderScene.OverlayEventType.GUI_HIGHLIGHT) {
            for (GuiHighlightPlacement placement : guiHighlightPlacements) {
                if (placement.overlayEvent == overlayEvent) {
                    return placement.targetPoint;
                }
            }
        }

        return overlayEvent.getPointAt() == null ? null
            : projector.project(scene, PonderScenePreview.computeBounds(scene), layout, currentTick,
                overlayEvent.getPointAt());
    }

    public static SpeechRenderer.SpeechPointing choosePointing(int boxX, int boxY, int boxWidth, int boxHeight, SpeechRenderer.Point targetPoint) {
        if (targetPoint == null) {
            return SpeechRenderer.SpeechPointing.NONE;
        }

        int centerX = boxX + boxWidth / 2;
        int centerY = boxY + boxHeight / 2;
        int deltaX = targetPoint.x - centerX;
        int deltaY = targetPoint.y - centerY;
        if (Math.abs(deltaX) > Math.abs(deltaY)) {
            return deltaX > 0 ? SpeechRenderer.SpeechPointing.RIGHT : SpeechRenderer.SpeechPointing.LEFT;
        }
        return deltaY > 0 ? SpeechRenderer.SpeechPointing.DOWN : SpeechRenderer.SpeechPointing.UP;
    }

    // -- overlay overlap avoidance -- //

    public static CaptionPlacement avoidOverlayOverlap(int boxX, int boxY, int boxWidth, int boxHeight,
        SpeechRenderer.SpeechPointing pointing, int previewY, int maxBoxY, List<GuiOverlayPlacement> placements) {
        if (placements == null || placements.isEmpty()) {
            return new CaptionPlacement(boxX, boxY, pointing);
        }

        int left = Integer.MAX_VALUE;
        int top = Integer.MAX_VALUE;
        int right = Integer.MIN_VALUE;
        int bottom = Integer.MIN_VALUE;
        boolean intersects = false;
        for (GuiOverlayPlacement placement : placements) {
            left = Math.min(left, placement.panelX);
            top = Math.min(top, placement.panelY);
            right = Math.max(right, placement.panelX + placement.panelWidth);
            bottom = Math.max(bottom, placement.panelY + placement.panelHeight);
            if (rectsIntersect(boxX, boxY, boxWidth, boxHeight, placement.panelX, placement.panelY, placement.panelWidth,
                placement.panelHeight)) {
                intersects = true;
            }
        }

        if (!intersects) {
            return new CaptionPlacement(boxX, boxY, pointing);
        }

        int aboveY = top - boxHeight - 12;
        int belowY = bottom + 12;
        boolean canAbove = aboveY >= previewY + 12;
        boolean canBelow = belowY <= maxBoxY;
        if (canAbove && (!canBelow || Math.abs(boxY - aboveY) <= Math.abs(boxY - belowY))) {
            return new CaptionPlacement(boxX, aboveY, SpeechRenderer.SpeechPointing.DOWN);
        }
        if (canBelow) {
            return new CaptionPlacement(boxX, belowY, SpeechRenderer.SpeechPointing.UP);
        }

        int clampedY = MathHelper.clamp(boxY, previewY + 12, maxBoxY);
        return new CaptionPlacement(boxX, clampedY, pointing);
    }

    // -- overlay placement computation -- //

    public static List<GuiOverlayPlacement> computeActiveGuiOverlayPlacements(PonderScene scene, PreviewLayout layout,
        float currentTick, ScenePointProjector projector) {
        if (scene == null || layout == null) {
            return Collections.emptyList();
        }

        PreviewBounds bounds = PonderScenePreview.computeBounds(scene);
        List<GuiOverlayPlacement> placements = new ArrayList<GuiOverlayPlacement>();
        Map<String, GuiOverlayPlacement> placementsById = new LinkedHashMap<String, GuiOverlayPlacement>();
        for (PonderScene.OverlayEvent overlayEvent : scene.getOverlayEvents()) {
            if (overlayEvent.getType() != PonderScene.OverlayEventType.GUI_TEXTURE) {
                continue;
            }
            if (overlayEvent.getTick() > currentTick
                || currentTick > overlayEvent.getTick() + overlayEvent.getDuration()
                || (overlayEvent.getTextureLocation() == null && overlayEvent.getGuiSnapshotId() == null)) {
                continue;
            }

            Snapshot snapshot = overlayEvent.getGuiSnapshotId() == null ? null
                : PonderGuiSnapshotRegistry.get(overlayEvent.getGuiSnapshotId(), currentTick);
            if (snapshot != null) {
                overlayEvent.applySnapshot(snapshot, overlayEvent.getOffsetX(), overlayEvent.getOffsetY());
            }
            if (overlayEvent.getTextureLocation() == null && (snapshot == null || snapshot.renderer == null)) {
                continue;
            }

            GuiOverlayPlacement placement =
                computeGuiOverlayPlacement(scene, bounds, layout, currentTick, overlayEvent, placementsById, projector);
            if (placement != null) {
                placements.add(placement);
                String overlayId = placement.overlayEvent.getOverlayId();
                if (overlayId != null && !overlayId.trim().isEmpty()) {
                    placementsById.put(overlayId, placement);
                }
            }
        }
        return placements;
    }

    private static GuiOverlayPlacement computeGuiOverlayPlacement(PonderScene scene, PreviewBounds bounds, PreviewLayout layout,
        float currentTick, PonderScene.OverlayEvent overlayEvent, Map<String, GuiOverlayPlacement> placementsById,
        ScenePointProjector projector) {
        int logicalWidth = Math.max(1, overlayEvent.getRegionWidth());
        int logicalHeight = Math.max(1, overlayEvent.getRegionHeight());
        int drawWidth = Math.max(1, overlayEvent.getDisplayWidth());
        int drawHeight = Math.max(1, overlayEvent.getDisplayHeight());
        int padding = overlayEvent.isFramed() ? 4 : 0;
        int panelWidth = drawWidth + padding * 2;
        int panelHeight = drawHeight + padding * 2;
        SpeechRenderer.Point targetPoint = overlayEvent.getPointAt() == null ? null
            : projector.project(scene, bounds, layout, currentTick, overlayEvent.getPointAt());
        boolean aboveTarget = true;

        String parentOverlayId = overlayEvent.getParentOverlayId();
        if (parentOverlayId != null && !parentOverlayId.trim().isEmpty()) {
            GuiOverlayPlacement parentPlacement = placementsById.get(parentOverlayId);
            if (parentPlacement == null) {
                return null;
            }

            float parentScaleX =
                parentPlacement.drawWidth / (float) Math.max(1, parentPlacement.overlayEvent.getRegionWidth());
            float parentScaleY =
                parentPlacement.drawHeight / (float) Math.max(1, parentPlacement.overlayEvent.getRegionHeight());
            if (overlayEvent.isScaleToParent()) {
                drawWidth = Math.max(1, Math.round(logicalWidth * parentScaleX));
                drawHeight = Math.max(1, Math.round(logicalHeight * parentScaleY));
            }
            panelWidth = drawWidth + padding * 2;
            panelHeight = drawHeight + padding * 2;

            int drawX = parentPlacement.drawX + Math.round(overlayEvent.getGuiX() * parentScaleX)
                + overlayEvent.getOffsetX();
            int drawY = parentPlacement.drawY + Math.round(overlayEvent.getGuiY() * parentScaleY)
                + overlayEvent.getOffsetY();
            int panelX = MathHelper.clamp(drawX - padding, layout.originX + 2,
                layout.originX + layout.width - panelWidth - 2);
            int panelY = MathHelper.clamp(drawY - padding, layout.originY + 2,
                layout.originY + layout.height - panelHeight - 2);
            int finalDrawX = panelX + padding;
            int finalDrawY = panelY + padding;
            SpeechRenderer.Point finalTargetPoint = new SpeechRenderer.Point(finalDrawX + drawWidth / 2, finalDrawY + drawHeight / 2);
            return new GuiOverlayPlacement(overlayEvent, finalTargetPoint, false, panelX, panelY, panelWidth,
                panelHeight, finalDrawX, finalDrawY, drawWidth, drawHeight);
        }

        int panelX = layout.originX + (layout.width - panelWidth) / 2 + overlayEvent.getOffsetX();
        int panelY = overlayEvent.getIndependentY() >= 0
            ? layout.originY + 8 + overlayEvent.getIndependentY() * 2 + overlayEvent.getOffsetY()
            : layout.originY + 8 + overlayEvent.getOffsetY();

        float fitScale = Math.min(1.0F,
            Math.min((layout.width - 8.0F) / Math.max(1.0F, panelWidth),
                (layout.height - 8.0F) / Math.max(1.0F, panelHeight)));
        if (fitScale < 1.0F) {
            drawWidth = Math.max(1, Math.round(drawWidth * fitScale));
            drawHeight = Math.max(1, Math.round(drawHeight * fitScale));
            panelWidth = drawWidth + padding * 2;
            panelHeight = drawHeight + padding * 2;
        }

        if (targetPoint != null) {
            panelX = targetPoint.x - panelWidth / 2 + overlayEvent.getOffsetX();
            panelY = targetPoint.y - panelHeight - 18 + overlayEvent.getOffsetY();
            if (overlayEvent.isPlaceNearTarget() && panelY < layout.originY + 6) {
                panelY = targetPoint.y + 14 + overlayEvent.getOffsetY();
                aboveTarget = false;
            }
        }

        panelX = MathHelper.clamp(panelX, layout.originX + 4,
            layout.originX + layout.width - panelWidth - 4);
        panelY = MathHelper.clamp(panelY, layout.originY + 4,
            layout.originY + layout.height - panelHeight - 4);
        return new GuiOverlayPlacement(overlayEvent, targetPoint, aboveTarget, panelX, panelY, panelWidth, panelHeight,
            panelX + padding, panelY + padding, drawWidth, drawHeight);
    }

    // -- highlight placement computation -- //

    public static List<GuiHighlightPlacement> computeActiveGuiHighlightPlacements(PonderScene scene, PreviewLayout layout,
        float currentTick, List<GuiOverlayPlacement> guiPlacements) {
        if (scene == null || layout == null || guiPlacements == null || guiPlacements.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, GuiOverlayPlacement> guiPlacementById = new LinkedHashMap<String, GuiOverlayPlacement>();
        for (GuiOverlayPlacement placement : guiPlacements) {
            String overlayId = placement.overlayEvent.getOverlayId();
            if (overlayId != null && !overlayId.trim().isEmpty()) {
                guiPlacementById.put(overlayId, placement);
            }
        }
        if (guiPlacementById.isEmpty()) {
            return Collections.emptyList();
        }

        List<GuiHighlightPlacement> placements = new ArrayList<GuiHighlightPlacement>();
        for (PonderScene.OverlayEvent overlayEvent : scene.getOverlayEvents()) {
            if (overlayEvent.getType() != PonderScene.OverlayEventType.GUI_HIGHLIGHT) {
                continue;
            }
            if (overlayEvent.getTick() > currentTick
                || currentTick > overlayEvent.getTick() + overlayEvent.getDuration()) {
                continue;
            }

            GuiOverlayPlacement parentPlacement = guiPlacementById.get(overlayEvent.getParentOverlayId());
            if (parentPlacement == null) {
                continue;
            }

            GuiHighlightPlacement placement = computeGuiHighlightPlacement(parentPlacement, overlayEvent);
            if (placement != null) {
                placements.add(placement);
            }
        }
        return placements;
    }

    private static GuiHighlightPlacement computeGuiHighlightPlacement(GuiOverlayPlacement parentPlacement,
        PonderScene.OverlayEvent overlayEvent) {
        float scaleX = parentPlacement.drawWidth / (float) Math.max(1, parentPlacement.overlayEvent.getRegionWidth());
        float scaleY = parentPlacement.drawHeight / (float) Math.max(1, parentPlacement.overlayEvent.getRegionHeight());
        int rectX = parentPlacement.drawX + Math.round(overlayEvent.getGuiX() * scaleX);
        int rectY = parentPlacement.drawY + Math.round(overlayEvent.getGuiY() * scaleY);
        int rectWidth = Math.max(2, Math.round(overlayEvent.getGuiWidth() * scaleX));
        int rectHeight = Math.max(2, Math.round(overlayEvent.getGuiHeight() * scaleY));
        int maxX = parentPlacement.drawX + parentPlacement.drawWidth;
        int maxY = parentPlacement.drawY + parentPlacement.drawHeight;
        rectWidth = Math.min(rectWidth, maxX - rectX);
        rectHeight = Math.min(rectHeight, maxY - rectY);
        if (rectWidth < 2 || rectHeight < 2) {
            return null;
        }
        SpeechRenderer.Point targetPoint = new SpeechRenderer.Point(rectX + rectWidth / 2, rectY + rectHeight / 2);
        return new GuiHighlightPlacement(overlayEvent, rectX, rectY, rectWidth, rectHeight, targetPoint);
    }
}
