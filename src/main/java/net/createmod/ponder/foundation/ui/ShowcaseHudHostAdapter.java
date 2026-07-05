package net.createmod.ponder.foundation.ui;

import net.minecraft.client.gui.FontRenderer;

final class ShowcaseHudHostAdapter implements ShowcaseHudRenderer.Host {

    private final PonderDebugScreen screen;

    ShowcaseHudHostAdapter(PonderDebugScreen screen) {
        this.screen = screen;
    }

    @Override
    public void drawRect(int left, int top, int right, int bottom, int color) {
        screen.drawRect(left, top, right, bottom, color);
    }

    @Override
    public void drawGradientRect(int left, int top, int right, int bottom, int startColor, int endColor) {
        screen.drawGradientRect(left, top, right, bottom, startColor, endColor);
    }

    @Override
    public void drawString(FontRenderer fontRenderer, String text, int x, int y, int color) {
        screen.drawString(fontRenderer, text, x, y, color);
    }

    @Override
    public void drawCenteredString(FontRenderer fontRenderer, String text, int x, int y, int color) {
        screen.drawCenteredString(fontRenderer, text, x, y, color);
    }
}
