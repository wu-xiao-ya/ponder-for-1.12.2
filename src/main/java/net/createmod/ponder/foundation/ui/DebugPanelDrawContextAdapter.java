package net.createmod.ponder.foundation.ui;

import java.util.List;

import net.createmod.ponder.foundation.ui.render.RenderContext;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;

final class DebugPanelDrawContextAdapter implements DebugDrawContext, RenderContext {

    private final PonderDebugScreen screen;
    private final FontRenderer fontRenderer;

    DebugPanelDrawContextAdapter(PonderDebugScreen screen, FontRenderer fontRenderer) {
        this.screen = screen;
        this.fontRenderer = fontRenderer;
    }

    @Override
    public void fillRect(int left, int top, int right, int bottom, int color) {
        screen.drawRect(left, top, right, bottom, color);
    }

    @Override
    public void drawString(String text, int x, int y, int color) {
        screen.drawString(fontRenderer, text, x, y, color);
    }

    @Override
    public AutoCloseable push() {
        return () -> {
        };
    }

    @Override
    public AutoCloseable scissor(int x, int y, int width, int height) {
        return () -> {
        };
    }

    @Override
    public void translate(float x, float y, float z) {
    }

    @Override
    public void scale(float x, float y, float z) {
    }

    @Override
    public void rotate(float angle, float x, float y, float z) {
    }

    @Override
    public void fillGradientRect(int left, int top, int right, int bottom, int startColor, int endColor) {
        fillRect(left, top, right, bottom, startColor);
    }

    @Override
    public void fillTexturedRect(int x, int y, int textureX, int textureY, int width, int height) {
    }

    @Override
    public void drawLine(int startX, int startY, int endX, int endY, int color, float width) {
    }

    @Override
    public void renderItem(ItemStack stack, int x, int y) {
    }

    @Override
    public void renderText(String text, int x, int y, int color) {
        drawString(text, x, y, color);
    }

    @Override
    public void renderCenteredText(String text, int centerX, int y, int color) {
        screen.drawCenteredString(fontRenderer, text, centerX, y, color);
    }

    @Override
    public void drawHoveringText(List<String> textLines, int x, int y) {
        screen.drawHoveringText(textLines, x, y);
    }
}
