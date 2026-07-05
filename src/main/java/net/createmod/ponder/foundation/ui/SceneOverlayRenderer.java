package net.createmod.ponder.foundation.ui;

import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.ui.PonderScenePreview.PreviewBounds;
import net.createmod.ponder.foundation.ui.projection.ProjectedBounds;
import net.createmod.ponder.foundation.ui.projection.SceneBounds;
import net.createmod.ponder.foundation.ui.projection.SceneProjectionContext;
import net.createmod.ponder.foundation.ui.render.RenderContext;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

final class SceneOverlayRenderer {

    private final PonderOverlayLayoutHelper overlayLayoutHelper;
    private final RenderContext draw;

    SceneOverlayRenderer(PonderOverlayLayoutHelper overlayLayoutHelper, RenderContext draw) {
        this.overlayLayoutHelper = overlayLayoutHelper;
        this.draw = draw;
    }

    void drawOutline(PonderScene scene, PreviewBounds bounds, PreviewLayout layout, float currentTick,
        PonderScene.OverlayEvent overlayEvent, float fade) {
        SceneProjectionContext projectionContext = createProjectionContext(scene, bounds, layout, currentTick);
        ProjectedBounds projected = overlayLayoutHelper.projectSceneBounds(projectionContext,
            overlayEvent.getSceneBounds());
        if (projected == null) {
            return;
        }

        int fillColor = withAlpha(overlayEvent.getColor(), fade * 42.0F);
        int edgeColor = withAlpha(blendColors(0xEDE4D4, overlayEvent.getColor(), 0.82F), fade * 232.0F);
        draw.drawBorderedRect(projected.minX(), projected.minY(), projected.maxX(), projected.maxY(), fillColor,
            edgeColor);
    }

    void drawLine(PonderScene scene, PreviewBounds bounds, PreviewLayout layout, float currentTick,
        PonderScene.OverlayEvent overlayEvent, float fade) {
        SceneProjectionContext projectionContext = createProjectionContext(scene, bounds, layout, currentTick);
        SpeechRenderer.Point start = overlayLayoutHelper.projectScenePoint(projectionContext,
            overlayEvent.getLineStart());
        SpeechRenderer.Point end = overlayLayoutHelper.projectScenePoint(projectionContext,
            overlayEvent.getLineEnd());
        if (start == null || end == null) {
            return;
        }

        int lineColor = withAlpha(overlayEvent.getColor(), fade * 235.0F);
        draw.drawLine(start.x, start.y, end.x, end.y, lineColor, overlayEvent.isLineWide() ? 3.0F : 1.8F);
    }

    void drawValueBox(PonderScene scene, PreviewBounds bounds, PreviewLayout layout, float currentTick,
        PonderScene.OverlayEvent overlayEvent, float fade) {
        Vec3d center = overlayEvent.getValueBoxCenter();
        Vec3d expand = overlayEvent.getValueBoxExpand();
        if (center == null || expand == null) {
            return;
        }

        AxisAlignedBB box = new AxisAlignedBB(center.x - expand.x, center.y - expand.y, center.z - expand.z,
            center.x + expand.x, center.y + expand.y, center.z + expand.z);
        SceneProjectionContext projectionContext = createProjectionContext(scene, bounds, layout, currentTick);
        ProjectedBounds projected = overlayLayoutHelper.projectSceneBounds(projectionContext, box);
        if (projected == null) {
            return;
        }

        int fillColor = withAlpha(overlayEvent.getColor(), fade * 56.0F);
        int edgeColor = withAlpha(blendColors(0xEDE4D4, overlayEvent.getColor(), 0.85F), fade * 228.0F);
        draw.drawBorderedRect(projected.minX(), projected.minY(), projected.maxX(), projected.maxY(), fillColor,
            edgeColor);
    }

    private SceneProjectionContext createProjectionContext(PonderScene scene, PreviewBounds bounds, PreviewLayout layout,
        float currentTick) {
        SceneBounds sceneBounds = new SceneBounds(bounds.minX, bounds.minY, bounds.minZ, bounds.maxX, bounds.maxY,
            bounds.maxZ);
        return SceneProjectionContext.of(scene, sceneBounds, layout, currentTick, overlayLayoutHelper::projectScenePoint);
    }

    private static int blendColors(int baseColor, int accentColor, float accentWeight) {
        float clampedWeight = Math.min(1.0F, Math.max(0.0F, accentWeight));
        float baseWeight = 1.0F - clampedWeight;
        int red = Math.round(((baseColor >> 16) & 0xFF) * baseWeight + ((accentColor >> 16) & 0xFF) * clampedWeight);
        int green = Math.round(((baseColor >> 8) & 0xFF) * baseWeight + ((accentColor >> 8) & 0xFF) * clampedWeight);
        int blue = Math.round((baseColor & 0xFF) * baseWeight + (accentColor & 0xFF) * clampedWeight);
        return red << 16 | green << 8 | blue;
    }

    private static int withAlpha(int color, float alpha) {
        int clampedAlpha = Math.min(255, Math.max(0, (int) alpha));
        return (clampedAlpha << 24) | (color & 0x00FFFFFF);
    }
}
