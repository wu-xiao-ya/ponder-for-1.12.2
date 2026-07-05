package net.createmod.ponder.foundation.ui.render;

import java.nio.FloatBuffer;
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
        boolean previousTextureEnabled = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
        GlStateManager.disableTexture2D();
        return new GLStateGuard(() -> restoreCapability(previousTextureEnabled, GlStateManager::enableTexture2D,
            GlStateManager::disableTexture2D));
    }

    public static GLStateGuard cullDisabled() {
        boolean previousCullEnabled = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        GlStateManager.disableCull();
        return new GLStateGuard(() -> restoreCapability(previousCullEnabled, GlStateManager::enableCull,
            GlStateManager::disableCull));
    }

    public static GLStateGuard blendEnabled() {
        boolean previousBlendEnabled = GL11.glIsEnabled(GL11.GL_BLEND);
        GlStateManager.enableBlend();
        return new GLStateGuard(() -> restoreCapability(previousBlendEnabled, GlStateManager::enableBlend,
            GlStateManager::disableBlend));
    }

    public static GLStateGuard alphaDisabled() {
        boolean previousAlphaEnabled = GL11.glIsEnabled(GL11.GL_ALPHA_TEST);
        GlStateManager.disableAlpha();
        return new GLStateGuard(() -> restoreCapability(previousAlphaEnabled, GlStateManager::enableAlpha,
            GlStateManager::disableAlpha));
    }

    public static GLStateGuard smoothShade() {
        int previousShadeModel = GL11.glGetInteger(GL11.GL_SHADE_MODEL);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        return new GLStateGuard(() -> GlStateManager.shadeModel(previousShadeModel));
    }

    public static GLStateGuard lineWidth(float width) {
        float previousLineWidth = GL11.glGetFloat(GL11.GL_LINE_WIDTH);
        GL11.glLineWidth(width);
        return new GLStateGuard(() -> GL11.glLineWidth(previousLineWidth));
    }

    public static GLStateGuard color(float red, float green, float blue, float alpha) {
        FloatBuffer previousColor = BufferUtils.createFloatBuffer(4);
        GL11.glGetFloat(GL11.GL_CURRENT_COLOR, previousColor);
        GlStateManager.color(red, green, blue, alpha);
        return new GLStateGuard(() -> GlStateManager.color(
                previousColor.get(0),
                previousColor.get(1),
                previousColor.get(2),
                previousColor.get(3)));
    }

    public static GLStateGuard color(int color) {
        float alpha = (float) (color >> 24 & 255) / 255.0F;
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;
        return color(red, green, blue, alpha);
    }

    public static GLStateGuard guiItemLighting() {
        ItemLightingState previousLighting = ItemLightingState.capture();
        RenderHelper.enableGUIStandardItemLighting();
        return new GLStateGuard(previousLighting::restore);
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

    private static void restoreCapability(boolean previousEnabled, Runnable enableAction, Runnable disableAction) {
        if (previousEnabled) {
            enableAction.run();
        } else {
            disableAction.run();
        }
    }

    private static FloatBuffer captureFloat(int value) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(4);
        GL11.glGetFloat(value, buffer);
        return buffer;
    }

    private static FloatBuffer captureLight(int light, int value) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(4);
        GL11.glGetLight(light, value, buffer);
        return buffer;
    }

    private record LightState(FloatBuffer position, FloatBuffer ambient, FloatBuffer diffuse) {

        static LightState capture(int light) {
            return new LightState(captureLight(light, GL11.GL_POSITION), captureLight(light, GL11.GL_AMBIENT),
                captureLight(light, GL11.GL_DIFFUSE));
        }

        void restore(int light) {
            GL11.glLight(light, GL11.GL_POSITION, position);
            GL11.glLight(light, GL11.GL_AMBIENT, ambient);
            GL11.glLight(light, GL11.GL_DIFFUSE, diffuse);
        }
    }

    private record ItemLightingState(boolean lightingEnabled, boolean light0Enabled, boolean light1Enabled,
        boolean colorMaterialEnabled, int shadeModel, FloatBuffer lightModelAmbient, LightState light0,
        LightState light1) {

        static ItemLightingState capture() {
            return new ItemLightingState(GL11.glIsEnabled(GL11.GL_LIGHTING), GL11.glIsEnabled(GL11.GL_LIGHT0),
                GL11.glIsEnabled(GL11.GL_LIGHT1), GL11.glIsEnabled(GL11.GL_COLOR_MATERIAL),
                GL11.glGetInteger(GL11.GL_SHADE_MODEL), captureFloat(GL11.GL_LIGHT_MODEL_AMBIENT),
                LightState.capture(GL11.GL_LIGHT0), LightState.capture(GL11.GL_LIGHT1));
        }

        void restore() {
            light0.restore(GL11.GL_LIGHT0);
            light1.restore(GL11.GL_LIGHT1);
            GL11.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT, lightModelAmbient);
            GlStateManager.shadeModel(shadeModel);
            restoreCapability(light0Enabled, () -> GL11.glEnable(GL11.GL_LIGHT0),
                () -> GL11.glDisable(GL11.GL_LIGHT0));
            restoreCapability(light1Enabled, () -> GL11.glEnable(GL11.GL_LIGHT1),
                () -> GL11.glDisable(GL11.GL_LIGHT1));
            restoreCapability(colorMaterialEnabled, () -> GL11.glEnable(GL11.GL_COLOR_MATERIAL),
                () -> GL11.glDisable(GL11.GL_COLOR_MATERIAL));
            restoreCapability(lightingEnabled, GlStateManager::enableLighting, GlStateManager::disableLighting);
        }
    }
}
