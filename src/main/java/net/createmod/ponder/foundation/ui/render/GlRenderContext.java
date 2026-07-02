package net.createmod.ponder.foundation.ui.render;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.client.config.GuiUtils;

import org.lwjgl.opengl.GL11;

public final class GlRenderContext implements RenderContext {

    private final Minecraft minecraft;
    private final FontRenderer fontRenderer;
    private final RenderItem itemRenderer;
    private final ScissorStack scissorStack;
    private final int screenWidth;
    private final int screenHeight;
    private float zLevel;

    public GlRenderContext(Minecraft minecraft, FontRenderer fontRenderer, int screenWidth, int screenHeight) {
        this.minecraft = minecraft;
        this.fontRenderer = fontRenderer;
        this.itemRenderer = minecraft.getRenderItem();
        this.scissorStack = new ScissorStack(minecraft);
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    public void setZLevel(float zLevel) {
        this.zLevel = zLevel;
    }

    @Override
    public AutoCloseable push() {
        return GLStateGuard.matrix();
    }

    @Override
    public AutoCloseable scissor(int x, int y, int width, int height) {
        return scissorStack.push(x, y, width, height);
    }

    @Override
    public void translate(float x, float y, float z) {
        GlStateManager.translate(x, y, z);
    }

    @Override
    public void scale(float x, float y, float z) {
        GlStateManager.scale(x, y, z);
    }

    @Override
    public void rotate(float angle, float x, float y, float z) {
        GlStateManager.rotate(angle, x, y, z);
    }

    @Override
    public void fillRect(int left, int top, int right, int bottom, int color) {
        Gui.drawRect(left, top, right, bottom, color);
    }

    @Override
    public void fillGradientRect(int left, int top, int right, int bottom, int startColor, int endColor) {
        float startAlpha = (float) (startColor >> 24 & 255) / 255.0F;
        float startRed = (float) (startColor >> 16 & 255) / 255.0F;
        float startGreen = (float) (startColor >> 8 & 255) / 255.0F;
        float startBlue = (float) (startColor & 255) / 255.0F;
        float endAlpha = (float) (endColor >> 24 & 255) / 255.0F;
        float endRed = (float) (endColor >> 16 & 255) / 255.0F;
        float endGreen = (float) (endColor >> 8 & 255) / 255.0F;
        float endBlue = (float) (endColor & 255) / 255.0F;

        try (GLStateGuard texture = GLStateGuard.textureDisabled();
             GLStateGuard blend = GLStateGuard.blendEnabled();
             GLStateGuard alpha = GLStateGuard.alphaDisabled();
             GLStateGuard shade = GLStateGuard.smoothShade()) {
            BufferBuilder buffer = Tessellator.getInstance().getBuffer();
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            buffer.pos(right, top, zLevel).color(startRed, startGreen, startBlue, startAlpha).endVertex();
            buffer.pos(left, top, zLevel).color(startRed, startGreen, startBlue, startAlpha).endVertex();
            buffer.pos(left, bottom, zLevel).color(endRed, endGreen, endBlue, endAlpha).endVertex();
            buffer.pos(right, bottom, zLevel).color(endRed, endGreen, endBlue, endAlpha).endVertex();
            Tessellator.getInstance().draw();
        }
    }

    @Override
    public void fillTexturedRect(int x, int y, int textureX, int textureY, int width, int height) {
        float uScale = 0.00390625F;
        float vScale = 0.00390625F;
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(x, y + height, zLevel).tex(textureX * uScale, (textureY + height) * vScale).endVertex();
        buffer.pos(x + width, y + height, zLevel).tex((textureX + width) * uScale,
            (textureY + height) * vScale).endVertex();
        buffer.pos(x + width, y, zLevel).tex((textureX + width) * uScale, textureY * vScale).endVertex();
        buffer.pos(x, y, zLevel).tex(textureX * uScale, textureY * vScale).endVertex();
        Tessellator.getInstance().draw();
    }

    @Override
    public void drawLine(int startX, int startY, int endX, int endY, int color, float width) {
        float alpha = (float) (color >> 24 & 255) / 255.0F;
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;
        try (GLStateGuard texture = GLStateGuard.textureDisabled();
             GLStateGuard blend = GLStateGuard.blendEnabled();
             GLStateGuard alphaState = GLStateGuard.alphaDisabled();
             GLStateGuard lineWidth = GLStateGuard.lineWidth(width)) {
            GlStateManager.color(red, green, blue, alpha);
            GL11.glBegin(GL11.GL_LINES);
            GL11.glVertex2f(startX + 0.5F, startY + 0.5F);
            GL11.glVertex2f(endX + 0.5F, endY + 0.5F);
            GL11.glEnd();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    @Override
    public void renderItem(ItemStack stack, int x, int y) {
        itemRenderer.renderItemAndEffectIntoGUI(stack, x, y);
    }

    @Override
    public void renderText(String text, int x, int y, int color) {
        if (text != null) {
            fontRenderer.drawStringWithShadow(text, x, y, color);
        }
    }

    @Override
    public void renderCenteredText(String text, int centerX, int y, int color) {
        if (text != null) {
            fontRenderer.drawStringWithShadow(text, centerX - fontRenderer.getStringWidth(text) / 2.0F, y, color);
        }
    }

    @Override
    public void drawHoveringText(List<String> textLines, int x, int y) {
        if (textLines != null && !textLines.isEmpty()) {
            GuiUtils.drawHoveringText(textLines, x, y, screenWidth, screenHeight, -1, fontRenderer);
        }
    }
}
