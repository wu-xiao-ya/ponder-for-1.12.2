package net.createmod.ponder.foundation.ui;

import java.util.List;

import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.ui.projection.SceneBounds;
import net.createmod.ponder.foundation.ui.projection.GuiHighlightPlacement;
import net.createmod.ponder.foundation.ui.projection.GuiOverlayPlacement;
import net.createmod.ponder.foundation.ui.projection.ScenePointProjector;
import net.createmod.ponder.foundation.ui.projection.SceneProjectionContext;
import net.createmod.ponder.foundation.ui.render.GLStateGuard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

public final class GuiOverlayRenderer {

    private final Minecraft mc;

    public GuiOverlayRenderer(Minecraft mc) {
        this.mc = mc;
    }

    public void drawGuiTextureOverlays(PonderScene scene, PreviewLayout layout, float currentTick, float fade,
        ScenePointProjector projector) {
        if (scene == null || layout == null || projector == null || fade <= 0.0F) {
            return;
        }

        PonderScenePreview.PreviewBounds previewBounds = PonderScenePreview.computeBounds(scene);
        SceneBounds sceneBounds = new SceneBounds(previewBounds.minX, previewBounds.minY, previewBounds.minZ,
            previewBounds.maxX, previewBounds.maxY, previewBounds.maxZ);
        SceneProjectionContext context =
            SceneProjectionContext.of(scene, sceneBounds, layout, currentTick, projector);
        List<GuiOverlayPlacement> guiPlacements = PonderOverlayHelper.computeActiveGuiOverlayPlacements(context);
        for (GuiOverlayPlacement placement : guiPlacements) {
            PonderScene.OverlayEvent overlayEvent = placement.overlayEvent();
            float overlayFade = PonderOverlayHelper.computeOverlayFade(
                overlayEvent.getTick(), overlayEvent.getDuration(), currentTick) * fade;
            if (overlayFade <= 0.0F) {
                continue;
            }

            if (overlayEvent.isFramed()) {
                drawGuiTexturePanel(placement.panelX(), placement.panelY(), placement.panelWidth(), placement.panelHeight(),
                    overlayEvent.getColor(), overlayFade);
            }

            if (overlayEvent.isFramed() && overlayEvent.isConnectorVisible() && placement.targetPoint() != null) {
                int startX = placement.panelX() + placement.panelWidth() / 2;
                int startY = placement.aboveTarget() ? placement.panelY() + placement.panelHeight() : placement.panelY();
                drawCaptionConnector(startX, startY, placement.targetPoint().x, placement.targetPoint().y,
                    overlayEvent.getColor(), overlayFade * 0.85F);
            }

            Snapshot snapshot = overlayEvent.getGuiSnapshotId() == null ? null
                : PonderGuiSnapshotRegistry.get(overlayEvent.getGuiSnapshotId(), currentTick);
            if (snapshot != null && snapshot.renderer != null) {
                GlStateManager.enableTexture2D();
                GlStateManager.enableBlend();
                GlStateManager.enableAlpha();
                try (GLStateGuard colorGuard = GLStateGuard.color(1.0F, 1.0F, 1.0F, overlayFade)) {
                    snapshot.renderer.render(placement.drawX(), placement.drawY(), placement.drawWidth(), placement.drawHeight(),
                        currentTick, overlayFade);
                }
            } else {
                mc.getTextureManager().bindTexture(overlayEvent.getTextureLocation());
                GlStateManager.enableBlend();
                GlStateManager.enableAlpha();
                try (GLStateGuard colorGuard = GLStateGuard.color(1.0F, 1.0F, 1.0F, overlayFade)) {
                    if (overlayEvent.isStretchTexture()) {
                        drawStretchGuiTexture(placement, overlayEvent);
                    } else {
                        CompatGuiScreen.drawScaledCustomSizeModalRect(placement.drawX(), placement.drawY(),
                            overlayEvent.getTextureU(), overlayEvent.getTextureV(),
                            overlayEvent.getRegionWidth(), overlayEvent.getRegionHeight(),
                            placement.drawWidth(), placement.drawHeight(),
                            overlayEvent.getTextureWidth(), overlayEvent.getTextureHeight());
                    }
                }
            }
        }

        List<GuiHighlightPlacement> highlightPlacements =
            PonderOverlayHelper.computeActiveGuiHighlightPlacements(context, guiPlacements);
        for (GuiHighlightPlacement placement : highlightPlacements) {
            PonderScene.OverlayEvent overlayEvent = placement.overlayEvent();
            float overlayFade = PonderOverlayHelper.computeOverlayFade(
                overlayEvent.getTick(), overlayEvent.getDuration(), currentTick) * fade;
            if (overlayFade <= 0.0F) {
                continue;
            }
            drawGuiHighlight(placement.rectX(), placement.rectY(), placement.rectWidth(), placement.rectHeight(),
                overlayEvent.getColor(), overlayFade);
        }
    }

    // -- overlay drawing primitives -- //

    private void drawStretchGuiTexture(GuiOverlayPlacement placement, PonderScene.OverlayEvent overlayEvent) {
        int logicalWidth = Math.max(1, overlayEvent.getRegionWidth());
        int logicalHeight = Math.max(1, overlayEvent.getRegionHeight());
        int textureWidth = Math.max(1, overlayEvent.getTextureWidth());
        int textureHeight = Math.max(1, overlayEvent.getTextureHeight());
        int border = Math.min(overlayEvent.getStretchBorder(), Math.min(textureWidth / 2, textureHeight / 2));
        border = Math.max(1, border);

        int drawBorderX = Math.max(1, Math.round(border * (placement.drawWidth() / (float) logicalWidth)));
        int drawBorderY = Math.max(1, Math.round(border * (placement.drawHeight() / (float) logicalHeight)));
        drawBorderX = Math.min(drawBorderX, Math.max(1, placement.drawWidth() / 2));
        drawBorderY = Math.min(drawBorderY, Math.max(1, placement.drawHeight() / 2));

        int centerSourceWidth = Math.max(1, textureWidth - border * 2);
        int centerSourceHeight = Math.max(1, textureHeight - border * 2);
        int centerDrawWidth = Math.max(0, placement.drawWidth() - drawBorderX * 2);
        int centerDrawHeight = Math.max(0, placement.drawHeight() - drawBorderY * 2);
        int sourceRightU = overlayEvent.getTextureU() + textureWidth - border;
        int sourceBottomV = overlayEvent.getTextureV() + textureHeight - border;

        CompatGuiScreen.drawScaledCustomSizeModalRect(placement.drawX(), placement.drawY(),
            overlayEvent.getTextureU(), overlayEvent.getTextureV(), border, border,
            drawBorderX, drawBorderY, textureWidth, textureHeight);
        CompatGuiScreen.drawScaledCustomSizeModalRect(placement.drawX() + placement.drawWidth() - drawBorderX, placement.drawY(),
            sourceRightU, overlayEvent.getTextureV(), border, border,
            drawBorderX, drawBorderY, textureWidth, textureHeight);
        CompatGuiScreen.drawScaledCustomSizeModalRect(placement.drawX(), placement.drawY() + placement.drawHeight() - drawBorderY,
            overlayEvent.getTextureU(), sourceBottomV, border, border,
            drawBorderX, drawBorderY, textureWidth, textureHeight);
        CompatGuiScreen.drawScaledCustomSizeModalRect(placement.drawX() + placement.drawWidth() - drawBorderX,
            placement.drawY() + placement.drawHeight() - drawBorderY,
            sourceRightU, sourceBottomV, border, border,
            drawBorderX, drawBorderY, textureWidth, textureHeight);

        if (centerDrawWidth > 0) {
            CompatGuiScreen.drawScaledCustomSizeModalRect(placement.drawX() + drawBorderX, placement.drawY(),
                overlayEvent.getTextureU() + border, overlayEvent.getTextureV(),
                centerSourceWidth, border, centerDrawWidth, drawBorderY, textureWidth, textureHeight);
            CompatGuiScreen.drawScaledCustomSizeModalRect(placement.drawX() + drawBorderX,
                placement.drawY() + placement.drawHeight() - drawBorderY,
                overlayEvent.getTextureU() + border, sourceBottomV,
                centerSourceWidth, border, centerDrawWidth, drawBorderY, textureWidth, textureHeight);
        }
        if (centerDrawHeight > 0) {
            CompatGuiScreen.drawScaledCustomSizeModalRect(placement.drawX(), placement.drawY() + drawBorderY,
                overlayEvent.getTextureU(), overlayEvent.getTextureV() + border,
                border, centerSourceHeight, drawBorderX, centerDrawHeight, textureWidth, textureHeight);
            CompatGuiScreen.drawScaledCustomSizeModalRect(placement.drawX() + placement.drawWidth() - drawBorderX,
                placement.drawY() + drawBorderY,
                sourceRightU, overlayEvent.getTextureV() + border,
                border, centerSourceHeight, drawBorderX, centerDrawHeight, textureWidth, textureHeight);
        }
        if (centerDrawWidth > 0 && centerDrawHeight > 0) {
            CompatGuiScreen.drawScaledCustomSizeModalRect(placement.drawX() + drawBorderX, placement.drawY() + drawBorderY,
                overlayEvent.getTextureU() + border, overlayEvent.getTextureV() + border,
                centerSourceWidth, centerSourceHeight, centerDrawWidth, centerDrawHeight, textureWidth, textureHeight);
        }
    }

    private void drawGuiTexturePanel(int x, int y, int width, int height, int accentColor, float fade) {
        int fillTop = withAlpha(0x121920, fade * 205.0F);
        int fillBottom = withAlpha(0x090D12, fade * 205.0F);
        int neutralBorder = withAlpha(0xE7E0D0, fade * 165.0F);
        int shadowBorder = withAlpha(0x2F343B, fade * 96.0F);
        int accentBorder = withAlpha(blendColors(0xD5CCB8, accentColor, 0.72F), fade * 164.0F);
        int innerAccent = withAlpha(blendColors(0xEEE6D4, accentColor, 0.34F), fade * 124.0F);

        CompatGuiScreen.drawGradientRect(x, y, x + width, y + height, fillTop, fillBottom, 0.0F);
        drawHorizontalGradientRect(x, y, x + width, y + 1, neutralBorder, accentBorder);
        drawHorizontalGradientRect(x, y + height - 1, x + width, y + height, shadowBorder, accentBorder);
        CompatGuiScreen.drawGradientRect(x, y, x + 1, y + height, neutralBorder, shadowBorder, 0.0F);
        CompatGuiScreen.drawGradientRect(x + width - 1, y, x + width, y + height, accentBorder, shadowBorder, 0.0F);
        drawHorizontalGradientRect(x + 2, y + 2, x + width - 2, y + 4, neutralBorder, innerAccent);
    }

    private void drawGuiHighlight(int x, int y, int width, int height, int accentColor, float fade) {
        int glowColor = withAlpha(blendColors(0x2A3643, accentColor, 0.72F), fade * 52.0F);
        int fillColor = withAlpha(blendColors(0x18212B, accentColor, 0.66F), fade * 86.0F);
        int edgeColor = withAlpha(blendColors(0xEDE4D4, accentColor, 0.80F), fade * 228.0F);
        int accentEdge = withAlpha(blendColors(0xC2B18F, accentColor, 0.90F), fade * 180.0F);

        CompatGuiScreen.drawRect(x - 2, y - 2, x + width + 2, y + height + 2, glowColor);
        CompatGuiScreen.drawRect(x, y, x + width, y + height, fillColor);
        drawHorizontalGradientRect(x, y, x + width, y + 1, edgeColor, accentEdge);
        drawHorizontalGradientRect(x, y + height - 1, x + width, y + height, accentEdge, edgeColor);
        CompatGuiScreen.drawGradientRect(x, y, x + 1, y + height, edgeColor, accentEdge, 0.0F);
        CompatGuiScreen.drawGradientRect(x + width - 1, y, x + width, y + height, accentEdge, edgeColor, 0.0F);
    }

    private void drawCaptionConnector(int startX, int startY, int endX, int endY, int color, float fade) {
        int alphaColor = ((int) (fade * 255.0F) << 24) | (color & 0x00FFFFFF);
        drawLineSegment(startX, startY, endX, endY, alphaColor, 2.0F);
        CompatGuiScreen.drawRect(startX - 1, startY - 1, startX + 2, startY + 2, alphaColor);
    }

    private void drawLineSegment(int startX, int startY, int endX, int endY, int color, float width) {
        float alpha = ((color >>> 24) & 0xFF) / 255.0F;
        float red = ((color >>> 16) & 0xFF) / 255.0F;
        float green = ((color >>> 8) & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;

        GlStateManager.enableBlend();
        try (GLStateGuard textureDisabled = GLStateGuard.textureDisabled();
             GLStateGuard alphaDisabled = GLStateGuard.alphaDisabled();
             GLStateGuard lineWidth = GLStateGuard.lineWidth(width);
             GLStateGuard colorGuard = GLStateGuard.color(red, green, blue, alpha)) {
            GL11.glBegin(GL11.GL_LINES);
            GL11.glVertex2f(startX + 0.5F, startY + 0.5F);
            GL11.glVertex2f(endX + 0.5F, endY + 0.5F);
            GL11.glEnd();
        }
    }

    private void drawHorizontalGradientRect(int left, int top, int right, int bottom, int leftColor, int rightColor) {
        float leftAlpha = ((leftColor >>> 24) & 0xFF) / 255.0F;
        float leftRed = ((leftColor >>> 16) & 0xFF) / 255.0F;
        float leftGreen = ((leftColor >>> 8) & 0xFF) / 255.0F;
        float leftBlue = (leftColor & 0xFF) / 255.0F;
        float rightAlpha = ((rightColor >>> 24) & 0xFF) / 255.0F;
        float rightRed = ((rightColor >>> 16) & 0xFF) / 255.0F;
        float rightGreen = ((rightColor >>> 8) & 0xFF) / 255.0F;
        float rightBlue = (rightColor & 0xFF) / 255.0F;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        GlStateManager.enableBlend();
        try (GLStateGuard textureDisabled = GLStateGuard.textureDisabled();
             GLStateGuard alphaDisabled = GLStateGuard.alphaDisabled();
             GLStateGuard smoothShade = GLStateGuard.smoothShade();
             GLStateGuard colorGuard = GLStateGuard.color(1.0F, 1.0F, 1.0F, 1.0F)) {
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            buffer.pos(right, top, 0.0D).color(rightRed, rightGreen, rightBlue, rightAlpha).endVertex();
            buffer.pos(left, top, 0.0D).color(leftRed, leftGreen, leftBlue, leftAlpha).endVertex();
            buffer.pos(left, bottom, 0.0D).color(leftRed, leftGreen, leftBlue, leftAlpha).endVertex();
            buffer.pos(right, bottom, 0.0D).color(rightRed, rightGreen, rightBlue, rightAlpha).endVertex();
            tessellator.draw();
        }
    }

    private static int blendColors(int baseColor, int accentColor, float accentWeight) {
        float clampedWeight = MathHelper.clamp(accentWeight, 0.0F, 1.0F);
        float baseWeight = 1.0F - clampedWeight;
        int red = Math.round(((baseColor >> 16) & 0xFF) * baseWeight + ((accentColor >> 16) & 0xFF) * clampedWeight);
        int green = Math.round(((baseColor >> 8) & 0xFF) * baseWeight + ((accentColor >> 8) & 0xFF) * clampedWeight);
        int blue = Math.round((baseColor & 0xFF) * baseWeight + (accentColor & 0xFF) * clampedWeight);
        return red << 16 | green << 8 | blue;
    }

    private static int withAlpha(int color, float alpha) {
        int alphaChannel = MathHelper.clamp((int) alpha, 0, 255);
        return alphaChannel << 24 | (color & 0x00FFFFFF);
    }
}
