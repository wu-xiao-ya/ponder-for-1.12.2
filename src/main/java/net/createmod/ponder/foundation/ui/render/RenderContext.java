package net.createmod.ponder.foundation.ui.render;

import java.util.List;

import net.minecraft.item.ItemStack;

public interface RenderContext {

    AutoCloseable push();

    AutoCloseable scissor(int x, int y, int width, int height);

    void translate(float x, float y, float z);

    void scale(float x, float y, float z);

    void rotate(float angle, float x, float y, float z);

    void fillRect(int left, int top, int right, int bottom, int color);

    default void drawBorderedRect(int left, int top, int right, int bottom, int fillColor, int borderColor) {
        fillRect(left, top, right, bottom, fillColor);
        fillRect(left, top, right, top + 1, borderColor);
        fillRect(left, bottom - 1, right, bottom, borderColor);
        fillRect(left, top, left + 1, bottom, borderColor);
        fillRect(right - 1, top, right, bottom, borderColor);
    }

    default void drawCrossMarker(int centerX, int centerY, int armRadius, int centerRadius, int accentColor,
        int fillColor) {
        fillRect(centerX - armRadius, centerY - 1, centerX + armRadius + 1, centerY + 1, accentColor);
        fillRect(centerX - 1, centerY - armRadius, centerX + 1, centerY + armRadius + 1, accentColor);
        fillRect(centerX - centerRadius, centerY - centerRadius, centerX + centerRadius + 1,
            centerY + centerRadius + 1, fillColor);
    }

    void fillGradientRect(int left, int top, int right, int bottom, int startColor, int endColor);

    void fillTexturedRect(int x, int y, int textureX, int textureY, int width, int height);

    void drawLine(int startX, int startY, int endX, int endY, int color, float width);

    void renderItem(ItemStack stack, int x, int y);

    void renderText(String text, int x, int y, int color);

    void renderCenteredText(String text, int centerX, int y, int color);

    void drawHoveringText(List<String> textLines, int x, int y);
}
