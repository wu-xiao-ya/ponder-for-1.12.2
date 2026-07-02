package net.createmod.ponder.foundation.ui;

import net.minecraft.item.ItemStack;
import java.util.List;

interface DebugDrawContext {
    void fillRect(int left, int top, int right, int bottom, int color);
    void drawString(String text, int x, int y, int color);
}

public interface DrawContext extends DebugDrawContext {
    void drawLineSegment(int startX, int startY, int endX, int endY, int color, float width);
    int withAlpha(int color, float alpha);
    int blendColors(int baseColor, int accentColor, float accentWeight);
    void renderItemStack(ItemStack stack, int x, int y);
    void drawHoveringText(List<String> textLines, int x, int y);
    int getStringWidth(String text);
    void drawCenteredString(String text, int centerX, int y, int color);
}
