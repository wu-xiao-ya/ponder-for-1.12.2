package net.createmod.ponder.foundation.ui;

import net.minecraft.client.gui.FontRenderer;

final class DebugPanelDrawContextAdapter implements DebugDrawContext {

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
}
