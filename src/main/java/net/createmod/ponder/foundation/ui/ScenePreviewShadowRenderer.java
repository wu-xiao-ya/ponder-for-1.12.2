package net.createmod.ponder.foundation.ui;

import net.createmod.ponder.foundation.ui.PonderScenePreview.PreviewBounds;
import net.createmod.ponder.foundation.ui.render.GLStateGuard;
import org.lwjgl.opengl.GL11;

final class ScenePreviewShadowRenderer {

    private static final float MIN_PADDING = 0.35F;
    private static final float MAX_PADDING = 1.35F;
    private static final float Y_OFFSET = 0.01F;
    private static final float SHOWCASE_ALPHA = 0.12F;
    private static final float PREVIEW_ALPHA = 0.06F;

    private ScenePreviewShadowRenderer() {
    }

    static void draw(PreviewBounds bounds, boolean showcaseMode) {
        float minX = bounds.minX - MIN_PADDING;
        float maxX = bounds.maxX + MAX_PADDING;
        float minZ = bounds.minZ - MIN_PADDING;
        float maxZ = bounds.maxZ + MAX_PADDING;
        float y = bounds.minY + Y_OFFSET;

        try (GLStateGuard textureGuard = GLStateGuard.textureDisabled();
            GLStateGuard colorGuard = GLStateGuard.color(0.0F, 0.0F, 0.0F,
                showcaseMode ? SHOWCASE_ALPHA : PREVIEW_ALPHA)) {
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glVertex3f(minX, y, minZ);
            GL11.glVertex3f(maxX, y, minZ);
            GL11.glVertex3f(maxX, y, maxZ);
            GL11.glVertex3f(minX, y, maxZ);
            GL11.glEnd();
        }
    }
}
