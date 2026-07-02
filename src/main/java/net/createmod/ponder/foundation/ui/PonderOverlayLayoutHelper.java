package net.createmod.ponder.foundation.ui;

import javax.annotation.Nullable;

import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.ui.PonderScenePreview.PreviewBounds;
import net.createmod.ponder.foundation.ui.projection.SceneBounds;
import net.createmod.ponder.foundation.ui.projection.SceneProjectionContext;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class PonderOverlayLayoutHelper {

    public static final class ProjectedBounds {
        public final int minX;
        public final int minY;
        public final int maxX;
        public final int maxY;

        public ProjectedBounds(int minX, int minY, int maxX, int maxY) {
            this.minX = minX;
            this.minY = minY;
            this.maxX = maxX;
            this.maxY = maxY;
        }
    }

    private final boolean showcaseMode;
    private final PonderPreviewCameraState previewCameraState;

    public PonderOverlayLayoutHelper(boolean showcaseMode, PonderPreviewCameraState previewCameraState) {
        this.showcaseMode = showcaseMode;
        this.previewCameraState = previewCameraState;
    }

    public static Vec3d rotateY(Vec3d vec, float degrees) {
        double radians = Math.toRadians(degrees);
        double cosine = Math.cos(radians);
        double sine = Math.sin(radians);
        double x = vec.x * cosine + vec.z * sine;
        double z = vec.z * cosine - vec.x * sine;
        return new Vec3d(x, vec.y, z);
    }

    public static Vec3d rotateX(Vec3d vec, float degrees) {
        double radians = Math.toRadians(degrees);
        double cosine = Math.cos(radians);
        double sine = Math.sin(radians);
        double y = vec.y * cosine - vec.z * sine;
        double z = vec.y * sine + vec.z * cosine;
        return new Vec3d(vec.x, y, z);
    }

    public float getPreviewAnchorY(PreviewLayout layout) {
        return layout.originY + layout.height * (showcaseMode ? 0.85F : 0.82F);
    }

    public float computePreviewScale(PonderScene scene, PreviewBounds bounds, PreviewLayout layout) {
        int sizeX = bounds.maxX - bounds.minX + 1;
        int sizeY = bounds.maxY - bounds.minY + 1;
        int sizeZ = bounds.maxZ - bounds.minZ + 1;
        float projectedWidth = sizeX + sizeZ * 0.85F;
        float projectedHeight = sizeY + (sizeX + sizeZ) * 0.45F;
        float widthInset = showcaseMode ? 22.0F : 12.0F;
        float heightInset = showcaseMode ? 42.0F : 12.0F;
        float widthScale = (layout.width - widthInset) / Math.max(2.0F, projectedWidth);
        float heightScale = (layout.height - heightInset) / Math.max(2.0F, projectedHeight);
        float baseScale = Math.max(6.0F, Math.min(widthScale, heightScale));
        if (showcaseMode) {
            baseScale *= 1.28F;
        }
        return baseScale * previewCameraState.getPreviewZoom() * Math.max(0.35F, scene.getScaleFactor());
    }

    @Nullable
    public SpeechRenderer.Point projectScenePoint(SceneProjectionContext context, Vec3d point) {
        if (context == null) {
            return null;
        }
        SceneBounds sceneBounds = context.sceneBounds();
        PreviewBounds bounds = new PreviewBounds(sceneBounds.minX(), sceneBounds.maxX(), sceneBounds.minZ(),
            sceneBounds.maxZ(), sceneBounds.maxY(), sceneBounds.minY());
        return projectScenePoint(context.scene(), bounds, context.layout(), context.renderTick(), point);
    }

    @Nullable
    public SpeechRenderer.Point projectScenePoint(PonderScene scene, PreviewBounds bounds, PreviewLayout layout,
        float renderTick, Vec3d point) {
        if (point == null) {
            return null;
        }

        float scale = computePreviewScale(scene, bounds, layout);
        float centerX = (bounds.minX + bounds.maxX + 1) / 2.0F;
        float centerY = (bounds.minY + bounds.maxY + 1) / 2.0F + scene.getSceneOffsetY();
        float centerZ = (bounds.minZ + bounds.maxZ + 1) / 2.0F;
        float animatedYaw = previewCameraState.getPreviewYaw() + PonderSceneRuntime.getCameraYaw(scene, renderTick);
        float pitch = previewCameraState.getPreviewPitch();

        Vec3d relative = point.subtract(centerX, centerY, centerZ);
        Vec3d rotatedYaw = rotateY(relative, animatedYaw);
        Vec3d rotated = rotateX(rotatedYaw, pitch);

        float screenX = layout.originX + layout.width / 2.0F + (float) (rotated.x * scale);
        float screenY = getPreviewAnchorY(layout) - (float) (rotated.y * scale);
        return new SpeechRenderer.Point(Math.round(screenX), Math.round(screenY));
    }

    @Nullable
    public ProjectedBounds projectSceneBounds(PonderScene scene, PreviewBounds bounds, PreviewLayout layout,
        float renderTick, AxisAlignedBB sceneBounds) {
        if (sceneBounds == null) {
            return null;
        }

        Vec3d[] corners = new Vec3d[] {
            new Vec3d(sceneBounds.minX, sceneBounds.minY, sceneBounds.minZ),
            new Vec3d(sceneBounds.minX, sceneBounds.minY, sceneBounds.maxZ),
            new Vec3d(sceneBounds.minX, sceneBounds.maxY, sceneBounds.minZ),
            new Vec3d(sceneBounds.minX, sceneBounds.maxY, sceneBounds.maxZ),
            new Vec3d(sceneBounds.maxX, sceneBounds.minY, sceneBounds.minZ),
            new Vec3d(sceneBounds.maxX, sceneBounds.minY, sceneBounds.maxZ),
            new Vec3d(sceneBounds.maxX, sceneBounds.maxY, sceneBounds.minZ),
            new Vec3d(sceneBounds.maxX, sceneBounds.maxY, sceneBounds.maxZ)
        };

        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (Vec3d corner : corners) {
            SpeechRenderer.Point projected = projectScenePoint(scene, bounds, layout, renderTick, corner);
            if (projected == null) {
                continue;
            }
            minX = Math.min(minX, projected.x);
            minY = Math.min(minY, projected.y);
            maxX = Math.max(maxX, projected.x);
            maxY = Math.max(maxY, projected.y);
        }

        if (minX == Integer.MAX_VALUE) {
            return null;
        }

        minX = MathHelper.clamp(minX - 1, layout.originX, layout.originX + layout.width - 1);
        minY = MathHelper.clamp(minY - 1, layout.originY, layout.originY + layout.height - 1);
        maxX = MathHelper.clamp(maxX + 1, minX + 2, layout.originX + layout.width);
        maxY = MathHelper.clamp(maxY + 1, minY + 2, layout.originY + layout.height);
        return new ProjectedBounds(minX, minY, maxX, maxY);
    }
}
