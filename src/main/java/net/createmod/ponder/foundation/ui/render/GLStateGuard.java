package net.createmod.ponder.foundation.ui.render;

import net.minecraft.client.renderer.GlStateManager;

import org.lwjgl.opengl.GL11;

public final class GLStateGuard implements AutoCloseable {

    private final Runnable onClose;
    private boolean closed;

    private GLStateGuard(Runnable onClose) {
        this.onClose = onClose;
    }

    public static GLStateGuard matrix() {
        GlStateManager.pushMatrix();
        return new GLStateGuard(GlStateManager::popMatrix);
    }

    public static GLStateGuard textureDisabled() {
        GlStateManager.disableTexture2D();
        return new GLStateGuard(GlStateManager::enableTexture2D);
    }

    public static GLStateGuard blendEnabled() {
        GlStateManager.enableBlend();
        return new GLStateGuard(GlStateManager::disableBlend);
    }

    public static GLStateGuard alphaDisabled() {
        GlStateManager.disableAlpha();
        return new GLStateGuard(GlStateManager::enableAlpha);
    }

    public static GLStateGuard smoothShade() {
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        return new GLStateGuard(() -> GlStateManager.shadeModel(GL11.GL_FLAT));
    }

    public static GLStateGuard lineWidth(float width) {
        GL11.glLineWidth(width);
        return new GLStateGuard(() -> GL11.glLineWidth(1.0F));
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        onClose.run();
    }
}
