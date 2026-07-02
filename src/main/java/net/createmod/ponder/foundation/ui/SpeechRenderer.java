package net.createmod.ponder.foundation.ui;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

public final class SpeechRenderer {

    private SpeechRenderer() {}

    public enum SpeechPointing {
        NONE, DOWN, LEFT, RIGHT, UP
    }

    public static final class Point {
        public final int x;
        public final int y;
        public Point(int x, int y) { this.x = x; this.y = y; }
    }

    public static void drawSpeechBox(int boxX, int boxY, int boxWidth, int boxHeight,
                                     SpeechPointing pointing, int accentColor, float fade) {
        int fillTop = withAlpha(0x151A20, fade * 205.0F);
        int fillBottom = withAlpha(0x090C10, fade * 205.0F);
        int neutralBorder = withAlpha(0xE7E0D0, fade * 170.0F);
        int shadowBorder = withAlpha(0x2F343B, fade * 96.0F);
        int accentBorder = withAlpha(blendColors(0xD5CCB8, accentColor, 0.72F), fade * 168.0F);
        int accentEdge = withAlpha(blendColors(0xBDAE93, accentColor, 0.85F), fade * 148.0F);
        int innerAccent = withAlpha(blendColors(0xE1D7C4, accentColor, 0.38F), fade * 132.0F);

        drawVerticalGradientRect(boxX, boxY, boxX + boxWidth, boxY + boxHeight, fillTop, fillBottom);
        drawHorizontalGradientRect(boxX, boxY, boxX + boxWidth, boxY + 1, neutralBorder, accentBorder);
        drawHorizontalGradientRect(boxX, boxY + boxHeight - 1, boxX + boxWidth, boxY + boxHeight, shadowBorder, accentEdge);
        drawVerticalGradientRect(boxX, boxY, boxX + 1, boxY + boxHeight, neutralBorder, shadowBorder);
        drawVerticalGradientRect(boxX + boxWidth - 1, boxY, boxX + boxWidth, boxY + boxHeight, accentBorder, accentEdge);
        drawHorizontalGradientRect(boxX + 2, boxY + 2, boxX + boxWidth - 2, boxY + 4, neutralBorder, innerAccent);

        if (pointing == SpeechPointing.NONE) return;

        int divotX = boxX + boxWidth / 2 - 4;
        int divotY = boxY + boxHeight - 1;
        int rotation = 0;
        if (pointing == SpeechPointing.UP) {
            divotY = boxY - 7; rotation = 180;
        } else if (pointing == SpeechPointing.LEFT) {
            divotX = boxX - 7; divotY = boxY + boxHeight / 2 - 4; rotation = 90;
        } else if (pointing == SpeechPointing.RIGHT) {
            divotX = boxX + boxWidth - 1; divotY = boxY + boxHeight / 2 - 4; rotation = 270;
        }
        drawSpeechDivot(divotX, divotY, rotation, accentColor, fade);
    }

    public static Point getSpeechPointerTip(int boxX, int boxY, int boxWidth, int boxHeight,
                                            SpeechPointing pointing) {
        if (pointing == SpeechPointing.NONE) return null;
        if (pointing == SpeechPointing.UP) return new Point(boxX + boxWidth / 2, boxY - 7);
        if (pointing == SpeechPointing.LEFT) return new Point(boxX - 7, boxY + boxHeight / 2);
        if (pointing == SpeechPointing.RIGHT) return new Point(boxX + boxWidth + 7, boxY + boxHeight / 2);
        return new Point(boxX + boxWidth / 2, boxY + boxHeight + 7);
    }

    public static void drawCaptionConnector(int startX, int startY, int endX, int endY, int color, float fade) {
        int alphaColor = ((int) (fade * 255.0F) << 24) | (color & 0x00FFFFFF);
        drawLineSegment(startX, startY, endX, endY, alphaColor, 2.0F);
        Gui.drawRect(startX - 1, startY - 1, startX + 2, startY + 2, alphaColor);
    }

    public static void drawCenteredStringNoShadow(FontRenderer fontRenderer, String text, int centerX, int y, int color) {
        if (text == null || text.isEmpty()) {
            return;
        }
        fontRenderer.drawString(text, centerX - fontRenderer.getStringWidth(text) / 2.0F, y, color, false);
    }

    public static void drawLineSegment(int startX, int startY, int endX, int endY, int color, float width) {
        float a = ((color >>> 24) & 0xFF) / 255.0F;
        float r = ((color >>> 16) & 0xFF) / 255.0F;
        float g = ((color >>> 8) & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.color(r, g, b, a);
        GL11.glLineWidth(width);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex2f(startX + 0.5F, startY + 0.5F);
        GL11.glVertex2f(endX + 0.5F, endY + 0.5F);
        GL11.glEnd();
        GL11.glLineWidth(1.0F);
        GlStateManager.enableTexture2D();
        GlStateManager.enableAlpha();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    // --- internal helpers ---

    private static int blendColors(int base, int accent, float weight) {
        float w = MathHelper.clamp(weight, 0.0F, 1.0F);
        float bw = 1.0F - w;
        int r = Math.round(((base >> 16) & 0xFF) * bw + ((accent >> 16) & 0xFF) * w);
        int g = Math.round(((base >> 8) & 0xFF) * bw + ((accent >> 8) & 0xFF) * w);
        int bl = Math.round((base & 0xFF) * bw + (accent & 0xFF) * w);
        return r << 16 | g << 8 | bl;
    }

    private static int withAlpha(int color, float alpha) {
        return MathHelper.clamp((int) alpha, 0, 255) << 24 | (color & 0x00FFFFFF);
    }

    private static void drawVerticalGradientRect(int left, int top, int right, int bottom,
                                                  int startColor, int endColor) {
        float sa = ((startColor >>> 24) & 0xFF) / 255.0F;
        float sr = ((startColor >>> 16) & 0xFF) / 255.0F;
        float sg = ((startColor >>> 8) & 0xFF) / 255.0F;
        float sb = (startColor & 0xFF) / 255.0F;
        float ea = ((endColor >>> 24) & 0xFF) / 255.0F;
        float er = ((endColor >>> 16) & 0xFF) / 255.0F;
        float eg = ((endColor >>> 8) & 0xFF) / 255.0F;
        float eb = (endColor & 0xFF) / 255.0F;

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        Tessellator t = Tessellator.getInstance();
        BufferBuilder buf = t.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buf.pos(right, top, 0.0D).color(sr, sg, sb, sa).endVertex();
        buf.pos(left, top, 0.0D).color(sr, sg, sb, sa).endVertex();
        buf.pos(left, bottom, 0.0D).color(er, eg, eb, ea).endVertex();
        buf.pos(right, bottom, 0.0D).color(er, eg, eb, ea).endVertex();
        t.draw();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableTexture2D();
        GlStateManager.enableAlpha();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void drawHorizontalGradientRect(int left, int top, int right, int bottom,
                                                    int leftColor, int rightColor) {
        float la = ((leftColor >>> 24) & 0xFF) / 255.0F;
        float lr = ((leftColor >>> 16) & 0xFF) / 255.0F;
        float lg = ((leftColor >>> 8) & 0xFF) / 255.0F;
        float lb = (leftColor & 0xFF) / 255.0F;
        float ra = ((rightColor >>> 24) & 0xFF) / 255.0F;
        float rr = ((rightColor >>> 16) & 0xFF) / 255.0F;
        float rg = ((rightColor >>> 8) & 0xFF) / 255.0F;
        float rb = (rightColor & 0xFF) / 255.0F;

        Tessellator t = Tessellator.getInstance();
        BufferBuilder buf = t.getBuffer();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buf.pos(right, top, 0.0D).color(rr, rg, rb, ra).endVertex();
        buf.pos(left, top, 0.0D).color(lr, lg, lb, la).endVertex();
        buf.pos(left, bottom, 0.0D).color(lr, lg, lb, la).endVertex();
        buf.pos(right, bottom, 0.0D).color(rr, rg, rb, ra).endVertex();
        t.draw();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableTexture2D();
        GlStateManager.enableAlpha();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void drawFilledTriangle(int x1, int y1, int x2, int y2, int x3, int y3, int color) {
        float a = ((color >>> 24) & 0xFF) / 255.0F;
        float r = ((color >>> 16) & 0xFF) / 255.0F;
        float g = ((color >>> 8) & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.color(r, g, b, a);
        GL11.glBegin(GL11.GL_TRIANGLES);
        GL11.glVertex2f(x1 + 0.5F, y1 + 0.5F);
        GL11.glVertex2f(x2 + 0.5F, y2 + 0.5F);
        GL11.glVertex2f(x3 + 0.5F, y3 + 0.5F);
        GL11.glEnd();
        GlStateManager.enableTexture2D();
        GlStateManager.enableAlpha();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void drawSpeechDivot(int x, int y, int rotation, int accentColor, float fade) {
        int fill = ((int)(fade * 205.0F) << 24) | 0x10161D;
        int edge = ((int)(fade * 160.0F) << 24) | 0xE7E0D0;
        int accent = withAlpha(blendColors(0xD5CCB8, accentColor, 0.75F), fade * 148.0F);
        if (rotation == 180) drawSpeechTriangleDown(x + 4, y + 8, fill, edge, accent);
        else if (rotation == 90) drawSpeechTriangleLeft(x, y + 4, fill, edge, accent);
        else if (rotation == 270) drawSpeechTriangleRight(x + 8, y + 4, fill, edge, accent);
        else drawSpeechTriangleUp(x + 4, y, fill, edge, accent);
    }

    private static void drawSpeechTriangleDown(int tx, int ty, int fill, int edge, int accent) {
        drawFilledTriangle(tx - 7, ty - 7, tx + 7, ty - 7, tx, ty, fill);
        drawLineSegment(tx - 7, ty - 7, tx, ty, edge, 1.5F);
        drawLineSegment(tx + 7, ty - 7, tx, ty, accent, 1.5F);
    }
    private static void drawSpeechTriangleUp(int tx, int ty, int fill, int edge, int accent) {
        drawFilledTriangle(tx - 7, ty + 7, tx + 7, ty + 7, tx, ty, fill);
        drawLineSegment(tx - 7, ty + 7, tx, ty, edge, 1.5F);
        drawLineSegment(tx + 7, ty + 7, tx, ty, accent, 1.5F);
    }
    private static void drawSpeechTriangleLeft(int tx, int ty, int fill, int edge, int accent) {
        drawFilledTriangle(tx + 7, ty - 7, tx + 7, ty + 7, tx, ty, fill);
        drawLineSegment(tx + 7, ty - 7, tx, ty, edge, 1.5F);
        drawLineSegment(tx + 7, ty + 7, tx, ty, accent, 1.5F);
    }
    private static void drawSpeechTriangleRight(int tx, int ty, int fill, int edge, int accent) {
        drawFilledTriangle(tx - 7, ty - 7, tx - 7, ty + 7, tx, ty, fill);
        drawLineSegment(tx - 7, ty - 7, tx, ty, edge, 1.5F);
        drawLineSegment(tx - 7, ty + 7, tx, ty, accent, 1.5F);
    }
}
