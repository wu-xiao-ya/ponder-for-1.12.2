package net.createmod.ponder.foundation.ui.render;

import java.nio.IntBuffer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;

import org.lwjgl.BufferUtils;
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

    public static GLStateGuard noop() {
        return new GLStateGuard(() -> {
        });
    }

    public static GLStateGuard scissor(Minecraft minecraft, int x, int y, int width, int height) {
        if (minecraft == null) {
            return noop();
        }

        boolean previousScissorEnabled = GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);
        IntBuffer previousScissorBox = BufferUtils.createIntBuffer(4);
        GL11.glGetInteger(GL11.GL_SCISSOR_BOX, previousScissorBox);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        ScissorMath.apply(minecraft, x, y, width, height);
        return new GLStateGuard(() -> {
            int previousX = previousScissorBox.get(0);
            int previousY = previousScissorBox.get(1);
            int previousWidth = previousScissorBox.get(2);
            int previousHeight = previousScissorBox.get(3);
            GL11.glScissor(previousX, previousY, previousWidth, previousHeight);
            if (previousScissorEnabled) {
                GL11.glEnable(GL11.GL_SCISSOR_TEST);
            } else {
                GL11.glDisable(GL11.GL_SCISSOR_TEST);
            }
        });
    }

    public static GLStateGuard textureDisabled() {
        GlStateManager.disableTexture2D();
        return new GLStateGuard(GlStateManager::enableTexture2D);
    }

    public static GLStateGuard cullDisabled() {
        GlStateManager.disableCull();
        return new GLStateGuard(GlStateManager::enableCull);
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

    public static GLStateGuard color(float red, float green, float blue, float alpha) {
        GlStateManager.color(red, green, blue, alpha);
        return new GLStateGuard(() -> GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F));
    }

    public static GLStateGuard color(int color) {
        float alpha = (float) (color >> 24 & 255) / 255.0F;
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;
        return color(red, green, blue, alpha);
    }

    public static GLStateGuard guiItemLighting() {
        RenderHelper.enableGUIStandardItemLighting();
        return new GLStateGuard(() -> {
            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableLighting();
        });
    }

    public static GLStateGuard itemLighting() {
        return guiItemLighting();
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
