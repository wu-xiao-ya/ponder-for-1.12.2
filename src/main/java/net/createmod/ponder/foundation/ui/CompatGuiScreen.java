package net.createmod.ponder.foundation.ui;

import java.util.List;

import net.createmod.ponder.foundation.ui.render.GLStateGuard;
import net.createmod.ponder.foundation.ui.render.ScissorStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

import org.lwjgl.opengl.GL11;

abstract class CompatGuiScreen extends GuiScreen implements DrawContext {

    // Runtime-obfuscated jars resolve inherited Gui fields against this subclass.
    // Keep local shadow fields so bytecode compiled against this class can resolve
    // GUI state without depending on parent-field owner remapping.
    public Minecraft mc;
    protected RenderItem itemRender;
    public int width;
    public int height;
    protected List<GuiButton> buttonList;
    protected List<GuiLabel> labelList;
    public boolean allowUserInput;
    protected FontRenderer fontRenderer;
    protected GuiButton selectedButton;
    protected boolean keyHandled;
    protected boolean mouseHandled;
    protected float zLevel;
    private ScissorStack renderScissorStack;

    protected CompatGuiScreen() {
        super();
        syncCompatFields();
    }

    @Override
    public AutoCloseable push() {
        return GLStateGuard.matrix();
    }

    @Override
    public AutoCloseable scissor(int x, int y, int width, int height) {
        if (mc == null) {
            return () -> {
            };
        }
        return getRenderScissorStack().push(x, y, width, height);
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
        drawRect(left, top, right, bottom, color);
    }

    @Override
    public void fillGradientRect(int left, int top, int right, int bottom, int startColor, int endColor) {
        drawGradientRect(left, top, right, bottom, startColor, endColor, this.zLevel);
    }

    @Override
    public void fillTexturedRect(int x, int y, int textureX, int textureY, int width, int height) {
        drawTexturedModalRect(x, y, textureX, textureY, width, height, this.zLevel);
    }

    @Override
    public void drawLine(int startX, int startY, int endX, int endY, int color, float width) {
        drawLineSegment(startX, startY, endX, endY, color, width);
    }

    @Override
    public void renderItem(ItemStack stack, int x, int y) {
        renderItemStack(stack, x, y);
    }

    @Override
    public void renderText(String text, int x, int y, int color) {
        drawString(text, x, y, color);
    }

    @Override
    public void renderCenteredText(String text, int centerX, int y, int color) {
        drawCenteredString(text, centerX, y, color);
    }

    @Override
    public void initGui() {
        super.initGui();
        syncCompatFields();
    }

    public void drawGradientRect(int left, int top, int right, int bottom, int startColor, int endColor) {
        drawGradientRect(left, top, right, bottom, startColor, endColor, this.zLevel);
    }

    public static void drawGradientRect(int left, int top, int right, int bottom, int startColor, int endColor,
        float zLevel) {
        float startAlpha = (float) (startColor >> 24 & 255) / 255.0F;
        float startRed = (float) (startColor >> 16 & 255) / 255.0F;
        float startGreen = (float) (startColor >> 8 & 255) / 255.0F;
        float startBlue = (float) (startColor & 255) / 255.0F;
        float endAlpha = (float) (endColor >> 24 & 255) / 255.0F;
        float endRed = (float) (endColor >> 16 & 255) / 255.0F;
        float endGreen = (float) (endColor >> 8 & 255) / 255.0F;
        float endBlue = (float) (endColor & 255) / 255.0F;

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO);
        GlStateManager.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(right, top, zLevel).color(startRed, startGreen, startBlue, startAlpha).endVertex();
        buffer.pos(left, top, zLevel).color(startRed, startGreen, startBlue, startAlpha).endVertex();
        buffer.pos(left, bottom, zLevel).color(endRed, endGreen, endBlue, endAlpha).endVertex();
        buffer.pos(right, bottom, zLevel).color(endRed, endGreen, endBlue, endAlpha).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    public static void drawRect(int left, int top, int right, int bottom, int color) {
        Gui.drawRect(left, top, right, bottom, color);
    }

    public void drawCenteredString(FontRenderer fontRenderer, String text, int x, int y, int color) {
        if (fontRenderer == null || text == null) {
            return;
        }
        fontRenderer.drawStringWithShadow(text, x - fontRenderer.getStringWidth(text) / 2.0F, y, color);
    }

    public void drawString(FontRenderer fontRenderer, String text, int x, int y, int color) {
        if (fontRenderer == null || text == null) {
            return;
        }
        fontRenderer.drawStringWithShadow(text, x, y, color);
    }

    @Override
    public void drawString(String text, int x, int y, int color) {
        drawString(fontRenderer, text, x, y, color);
    }

    @Override
    public void drawLineSegment(int startX, int startY, int endX, int endY, int color, float width) {
        SpeechRenderer.drawLineSegment(startX, startY, endX, endY, color, width);
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
    public void renderItemStack(ItemStack stack, int x, int y) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        RenderItem renderItem = itemRender != null ? itemRender : mc == null ? null : mc.getRenderItem();
        if (renderItem == null) {
            return;
        }

        try (GLStateGuard itemLighting = GLStateGuard.itemLighting()) {
            renderItem.renderItemAndEffectIntoGUI(stack, x, y);
        }
    }

    @Override
    public int getStringWidth(String text) {
        if (fontRenderer == null || text == null) {
            return 0;
        }
        return fontRenderer.getStringWidth(text);
    }

    @Override
    public void drawCenteredString(String text, int centerX, int y, int color) {
        drawCenteredString(fontRenderer, text, centerX, y, color);
    }

    public void drawTexturedModalRect(int x, int y, int textureX, int textureY, int width, int height) {
        drawTexturedModalRect(x, y, textureX, textureY, width, height, this.zLevel);
    }

    public static void drawTexturedModalRect(int x, int y, int textureX, int textureY, int width, int height,
        float zLevel) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        float uScale = 0.00390625F;
        float vScale = 0.00390625F;
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(x, y + height, zLevel).tex(textureX * uScale, (textureY + height) * vScale).endVertex();
        buffer.pos(x + width, y + height, zLevel).tex((textureX + width) * uScale,
            (textureY + height) * vScale).endVertex();
        buffer.pos(x + width, y, zLevel).tex((textureX + width) * uScale, textureY * vScale).endVertex();
        buffer.pos(x, y, zLevel).tex(textureX * uScale, textureY * vScale).endVertex();
        tessellator.draw();
    }

    public static void drawScaledCustomSizeModalRect(int x, int y, float u, float v, int uWidth, int vHeight,
        int width, int height, float tileWidth, float tileHeight) {
        Gui.drawScaledCustomSizeModalRect(x, y, u, v, uWidth, vHeight, width, height, tileWidth, tileHeight);
    }

    public void drawDefaultBackground() {
        if (mc != null && mc.world != null) {
            drawGradientRect(0, 0, width, height, 0xC0101010, 0xD0101010);
        } else {
            drawRect(0, 0, width, height, 0xFF101010);
        }
        MinecraftForge.EVENT_BUS.post(new GuiScreenEvent.BackgroundDrawnEvent(this));
    }

    public void drawHoveringText(List<String> textLines, int x, int y) {
        if (textLines == null || textLines.isEmpty() || fontRenderer == null) {
            return;
        }
        GuiUtils.drawHoveringText(textLines, x, y, width, height, -1, fontRenderer);
    }

    @Override
    public void setWorldAndResolution(Minecraft mc, int width, int height) {
        super.setWorldAndResolution(mc, width, height);
        syncCompatFields();
    }

    @Override
    public void setGuiSize(int width, int height) {
        super.setGuiSize(width, height);
        syncCompatFields();
    }

    protected void syncCompatFields() {
        this.mc = super.mc;
        this.itemRender = super.itemRender;
        this.width = super.width;
        this.height = super.height;
        this.buttonList = super.buttonList;
        this.labelList = super.labelList;
        this.allowUserInput = super.allowUserInput;
        this.fontRenderer = super.fontRenderer;
        this.selectedButton = super.selectedButton;
        this.keyHandled = super.keyHandled;
        this.mouseHandled = super.mouseHandled;
        // GuiScreen itself does not declare zLevel on 1.12 runtime; keep a local depth slot.
        this.zLevel = 0.0F;
    }

    private ScissorStack getRenderScissorStack() {
        if (renderScissorStack == null) {
            renderScissorStack = new ScissorStack(mc);
        }
        return renderScissorStack;
    }

    protected void clearCompatButtons() {
        syncCompatFields();
        if (super.buttonList != null) {
            super.buttonList.clear();
        }
        if (this.buttonList != null && this.buttonList != super.buttonList) {
            this.buttonList.clear();
        }
        this.buttonList = super.buttonList;
    }

    protected <T extends GuiButton> T addCompatButton(T button) {
        syncCompatFields();
        if (super.buttonList != null) {
            super.buttonList.add(button);
        }
        if (this.buttonList != null && this.buttonList != super.buttonList) {
            this.buttonList.add(button);
        }
        this.buttonList = super.buttonList;
        return button;
    }
}
