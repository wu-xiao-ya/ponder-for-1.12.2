package net.createmod.ponder.foundation.ui;

import java.util.List;

import net.createmod.ponder.foundation.ui.render.GLStateGuard;
import net.createmod.ponder.foundation.ui.render.RenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

interface DebugDrawContext {
    void fillRect(int left, int top, int right, int bottom, int color);
    void drawString(String text, int x, int y, int color);
}

public interface DrawContext extends DebugDrawContext, RenderContext {
    void drawLineSegment(int startX, int startY, int endX, int endY, int color, float width);
    int withAlpha(int color, float alpha);
    int blendColors(int baseColor, int accentColor, float accentWeight);
    void renderItemStack(ItemStack stack, int x, int y);
    void drawHoveringText(List<String> textLines, int x, int y);
    int getStringWidth(String text);
    void drawCenteredString(String text, int centerX, int y, int color);

    @Override
    default AutoCloseable push() {
        return GLStateGuard.matrix();
    }

    @Override
    default AutoCloseable scissor(int x, int y, int width, int height) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null) {
            return () -> {
            };
        }

        ScaledResolution resolution = new ScaledResolution(minecraft);
        int scaleFactor = resolution.getScaleFactor();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(x * scaleFactor, minecraft.displayHeight - (y + height) * scaleFactor, width * scaleFactor,
            height * scaleFactor);
        return () -> GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    @Override
    default void translate(float x, float y, float z) {
        GlStateManager.translate(x, y, z);
    }

    @Override
    default void scale(float x, float y, float z) {
        GlStateManager.scale(x, y, z);
    }

    @Override
    default void rotate(float angle, float x, float y, float z) {
        GlStateManager.rotate(angle, x, y, z);
    }

    @Override
    default void fillRect(int left, int top, int right, int bottom, int color) {
        CompatGuiScreen.drawRect(left, top, right, bottom, color);
    }

    @Override
    default void fillGradientRect(int left, int top, int right, int bottom, int startColor, int endColor) {
        CompatGuiScreen.drawGradientRect(left, top, right, bottom, startColor, endColor, 0.0F);
    }

    @Override
    default void fillTexturedRect(int x, int y, int textureX, int textureY, int width, int height) {
        CompatGuiScreen.drawTexturedModalRect(x, y, textureX, textureY, width, height, 0.0F);
    }

    @Override
    default void drawLine(int startX, int startY, int endX, int endY, int color, float width) {
        drawLineSegment(startX, startY, endX, endY, color, width);
    }

    @Override
    default void renderItem(ItemStack stack, int x, int y) {
        renderItemStack(stack, x, y);
    }

    @Override
    default void renderText(String text, int x, int y, int color) {
        drawString(text, x, y, color);
    }

    @Override
    default void renderCenteredText(String text, int centerX, int y, int color) {
        drawCenteredString(text, centerX, y, color);
    }
}
