package net.createmod.ponder.foundation.ui;

import java.util.List;

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

abstract class CompatGuiScreen extends GuiScreen {

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

    protected CompatGuiScreen() {
        super();
        syncCompatFields();
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
