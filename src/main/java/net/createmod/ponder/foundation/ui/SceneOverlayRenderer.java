package net.createmod.ponder.foundation.ui;

import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.ui.PonderScenePreview.PreviewBounds;
import net.createmod.ponder.foundation.ui.projection.ProjectedBounds;
import net.createmod.ponder.foundation.ui.projection.SceneBounds;
import net.createmod.ponder.foundation.ui.projection.SceneProjectionContext;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

final class SceneOverlayRenderer {

    private final PonderOverlayLayoutHelper overlayLayoutHelper;
    private final DrawContext draw;

    SceneOverlayRenderer(PonderOverlayLayoutHelper overlayLayoutHelper, DrawContext draw) {
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

        int fillColor = draw.withAlpha(overlayEvent.getColor(), fade * 42.0F);
        int edgeColor = draw.withAlpha(draw.blendColors(0xEDE4D4, overlayEvent.getColor(), 0.82F), fade * 232.0F);
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

        int lineColor = draw.withAlpha(overlayEvent.getColor(), fade * 235.0F);
        draw.drawLineSegment(start.x, start.y, end.x, end.y, lineColor, overlayEvent.isLineWide() ? 3.0F : 1.8F);
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

        int fillColor = draw.withAlpha(overlayEvent.getColor(), fade * 56.0F);
        int edgeColor = draw.withAlpha(draw.blendColors(0xEDE4D4, overlayEvent.getColor(), 0.85F), fade * 228.0F);
        draw.drawBorderedRect(projected.minX(), projected.minY(), projected.maxX(), projected.maxY(), fillColor,
            edgeColor);
    }

    private SceneProjectionContext createProjectionContext(PonderScene scene, PreviewBounds bounds, PreviewLayout layout,
        float currentTick) {
        SceneBounds sceneBounds = new SceneBounds(bounds.minX, bounds.minY, bounds.minZ, bounds.maxX, bounds.maxY,
            bounds.maxZ);
        return SceneProjectionContext.of(scene, sceneBounds, layout, currentTick, overlayLayoutHelper::projectScenePoint);
    }
}
