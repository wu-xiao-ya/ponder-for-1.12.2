package net.createmod.ponder.foundation.ui;

import java.util.List;

import net.minecraft.util.math.MathHelper;

final class OverlayDrawContextAdapter implements DrawContext {

    private final PonderDebugScreen screen;

    OverlayDrawContextAdapter(PonderDebugScreen screen) {
        this.screen = screen;
    }

    @Override
    public void fillRect(int left, int top, int right, int bottom, int color) {
        CompatGuiScreen.drawRect(left, top, right, bottom, color);
    }

    @Override
    public void drawString(String text, int x, int y, int color) {
        screen.drawString(screen.fontRenderer, text, x, y, color);
    }

    @Override
    public void drawLineSegment(int startX, int startY, int endX, int endY, int color, float width) {
        screen.drawLineSegment(startX, startY, endX, endY, color, width);
    }

    @Override
    public int withAlpha(int color, float alpha) {
        int alphaChannel = MathHelper.clamp((int) alpha, 0, 255);
        return alphaChannel << 24 | (color & 0x00FFFFFF);
    }

    @Override
    public int blendColors(int baseColor, int accentColor, float accentWeight) {
        float clampedWeight = MathHelper.clamp(accentWeight, 0.0F, 1.0F);
        float baseWeight = 1.0F - clampedWeight;
        int red = Math.round(((baseColor >> 16) & 0xFF) * baseWeight + ((accentColor >> 16) & 0xFF) * clampedWeight);
        int green = Math.round(((baseColor >> 8) & 0xFF) * baseWeight + ((accentColor >> 8) & 0xFF) * clampedWeight);
        int blue = Math.round((baseColor & 0xFF) * baseWeight + (accentColor & 0xFF) * clampedWeight);
        return red << 16 | green << 8 | blue;
    }

    @Override
    public void drawHoveringText(List<String> textLines, int x, int y) {
        screen.drawHoveringText(textLines, x, y);
    }

    @Override
    public int getStringWidth(String text) {
        return screen.fontRenderer.getStringWidth(text);
    }

    @Override
    public void drawCenteredString(String text, int centerX, int y, int color) {
        screen.drawCenteredString(screen.fontRenderer, text, centerX, y, color);
    }
}
