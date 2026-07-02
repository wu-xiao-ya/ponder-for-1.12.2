package net.createmod.ponder.foundation.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

final class ShowcaseChromeRenderer {

    static final class Theme {
        private final int glowInset;
        private final int glowBottomInset;
        private final int topGlowHeight;
        private final int bottomGlowHeight;
        private final float glowAlphaScale;
        private final float borderAlphaScale;
        private final float vignetteAlphaScale;
        private final int glowColor;
        private final int topBorderColor;
        private final int bottomBorderColor;
        private final int sideBorderColor;
        private final int vignetteColor;
        private final ResourceLocation logoTexture;
        private final float logoAlphaScale;

        Theme(int glowInset, int glowBottomInset, int topGlowHeight, int bottomGlowHeight, float glowAlphaScale,
            float borderAlphaScale, float vignetteAlphaScale, int glowColor, int topBorderColor, int bottomBorderColor,
            int sideBorderColor, int vignetteColor, ResourceLocation logoTexture, float logoAlphaScale) {
            this.glowInset = glowInset;
            this.glowBottomInset = glowBottomInset;
            this.topGlowHeight = topGlowHeight;
            this.bottomGlowHeight = bottomGlowHeight;
            this.glowAlphaScale = glowAlphaScale;
            this.borderAlphaScale = borderAlphaScale;
            this.vignetteAlphaScale = vignetteAlphaScale;
            this.glowColor = glowColor;
            this.topBorderColor = topBorderColor;
            this.bottomBorderColor = bottomBorderColor;
            this.sideBorderColor = sideBorderColor;
            this.vignetteColor = vignetteColor;
            this.logoTexture = logoTexture;
            this.logoAlphaScale = logoAlphaScale;
        }
    }

    private final Minecraft minecraft;
    private final Theme theme;

    ShowcaseChromeRenderer(Minecraft minecraft, Theme theme) {
        this.minecraft = minecraft;
        this.theme = theme;
    }

    void drawBackdrop(CompatGuiScreen host, int previewX, int previewY, int previewWidth, int previewHeight, float fade) {
        int glowAlpha = (int) (fade * theme.glowAlphaScale) << 24;
        int borderAlpha = (int) (fade * theme.borderAlphaScale) << 24;
        int vignetteAlpha = (int) (fade * theme.vignetteAlphaScale) << 24;

        host.drawGradientRect(previewX - theme.glowInset, previewY - theme.glowInset,
            previewX + previewWidth + theme.glowInset, previewY + previewHeight + theme.glowBottomInset,
            glowAlpha | theme.glowColor, 0x00000000);
        host.drawRect(previewX, previewY, previewX + previewWidth, previewY + 1, borderAlpha | theme.topBorderColor);
        host.drawRect(previewX, previewY + previewHeight - 1, previewX + previewWidth, previewY + previewHeight,
            borderAlpha | theme.bottomBorderColor);
        host.drawRect(previewX, previewY, previewX + 1, previewY + previewHeight, borderAlpha | theme.sideBorderColor);
        host.drawRect(previewX + previewWidth - 1, previewY, previewX + previewWidth, previewY + previewHeight,
            borderAlpha | theme.bottomBorderColor);
        host.drawGradientRect(previewX, previewY, previewX + previewWidth, previewY + theme.topGlowHeight,
            vignetteAlpha | theme.vignetteColor, 0x00000000);
        host.drawGradientRect(previewX, previewY + previewHeight - theme.bottomGlowHeight, previewX + previewWidth,
            previewY + previewHeight, 0x00000000, vignetteAlpha | theme.vignetteColor);
    }

    void drawLogo(CompatGuiScreen host, int x, int y, float fade) {
        minecraft.getTextureManager().bindTexture(theme.logoTexture);
        GlStateManager.enableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, MathHelper.clamp(fade * theme.logoAlphaScale, 0.0F, 1.0F));
        host.drawTexturedModalRect(x, y, 0, 0, 32, 32);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
