package net.createmod.ponder.foundation.ui;

import net.minecraft.util.ResourceLocation;

final class PonderThemes {

    private static final ResourceLocation LOGO_TEXTURE = new ResourceLocation("ponder", "textures/gui/logo.png");
    private static final PonderTheme SHOWCASE = createShowcaseTheme();
    private static final PonderTheme DEBUG = createDebugTheme();

    private PonderThemes() {
    }

    static PonderTheme showcase() {
        return SHOWCASE;
    }

    static PonderTheme debug() {
        return DEBUG;
    }

    static ShowcaseChromeRenderer.Theme showcaseChromeTheme() {
        return SHOWCASE.createShowcaseChromeTheme();
    }

    static ShowcaseRenderer.Theme showcaseRendererTheme(ShowcaseRenderer.HeaderEyebrowFactory eyebrowFactory,
        ShowcaseRenderer.GroupPopupLabelFactory popupLabelFactory) {
        return SHOWCASE.createShowcaseRendererTheme(eyebrowFactory, popupLabelFactory);
    }

    static ShowcaseHudRenderer.Theme showcaseHudTheme(PonderTheme.HudTextProvider textProvider) {
        return SHOWCASE.createShowcaseHudTheme(textProvider);
    }

    static ShowcaseChromeRenderer.Theme debugChromeTheme() {
        return DEBUG.createShowcaseChromeTheme();
    }

    static ShowcaseRenderer.Theme debugRendererTheme(ShowcaseRenderer.HeaderEyebrowFactory eyebrowFactory,
        ShowcaseRenderer.GroupPopupLabelFactory popupLabelFactory) {
        return DEBUG.createShowcaseRendererTheme(eyebrowFactory, popupLabelFactory);
    }

    static ShowcaseHudRenderer.Theme debugHudTheme(PonderTheme.HudTextProvider textProvider) {
        return DEBUG.createShowcaseHudTheme(textProvider);
    }

    private static PonderTheme createShowcaseTheme() {
        return PonderTheme.builder("showcase")
            .metric(ThemeMetric.CHROME_GLOW_INSET, 24.0F)
            .metric(ThemeMetric.CHROME_GLOW_BOTTOM_INSET, 28.0F)
            .metric(ThemeMetric.CHROME_TOP_GLOW_HEIGHT, 56.0F)
            .metric(ThemeMetric.CHROME_BOTTOM_GLOW_HEIGHT, 68.0F)
            .metric(ThemeMetric.CHROME_GLOW_ALPHA_SCALE, 68.0F)
            .metric(ThemeMetric.CHROME_BORDER_ALPHA_SCALE, 56.0F)
            .metric(ThemeMetric.CHROME_VIGNETTE_ALPHA_SCALE, 116.0F)
            .metric(ThemeMetric.CHROME_LOGO_ALPHA_SCALE, 0.78F)
            .metric(ThemeMetric.HEADER_MAX_WIDTH, 340.0F)
            .metric(ThemeMetric.HEADER_HEIGHT, 44.0F)
            .metric(ThemeMetric.HEADER_ALPHA_SCALE, 188.0F)
            .metric(ThemeMetric.HEADER_UNDERLINE_ALPHA_SCALE, 96.0F)
            .metric(ThemeMetric.HEADER_SLOT_OUTER_ALPHA_SCALE, 188.0F)
            .metric(ThemeMetric.HEADER_SLOT_INNER_ALPHA_SCALE, 64.0F)
            .metric(ThemeMetric.GROUP_CHIP_FILL_ALPHA_SCALE, 188.0F)
            .metric(ThemeMetric.GROUP_CHIP_BORDER_ALPHA_SCALE, 156.0F)
            .color(SymbolicColor.CHROME_GLOW, 0x1B2430)
            .color(SymbolicColor.CHROME_BORDER_TOP, 0xE6E0D2)
            .color(SymbolicColor.CHROME_BORDER_BOTTOM, 0x2B323A)
            .color(SymbolicColor.CHROME_BORDER_SIDE, 0x56606A)
            .color(SymbolicColor.CHROME_VIGNETTE, 0x05080C)
            .color(SymbolicColor.HEADER_FILL, 0x111822)
            .color(SymbolicColor.HEADER_RIGHT_EDGE, 0x00000000)
            .color(SymbolicColor.HEADER_UNDERLINE, 0xD7DFE7)
            .color(SymbolicColor.HEADER_SLOT_OUTER, 0x10151B)
            .color(SymbolicColor.HEADER_SLOT_INNER, 0xE7E0D1)
            .color(SymbolicColor.HEADER_EYEBROW, 0xB8C3CC)
            .color(SymbolicColor.HEADER_TITLE, 0xF6F2EA)
            .color(SymbolicColor.HEADER_SUBTITLE, 0xAEB8C1)
            .color(SymbolicColor.HEADER_INDEX, 0xFFD7DFE7)
            .color(SymbolicColor.GROUP_CHIP_FILL, 0x1A2028)
            .color(SymbolicColor.GROUP_CHIP_BORDER, 0xD7DFE7)
            .color(SymbolicColor.GROUP_CHIP_TEXT, 0xF2EFE7)
            .logoTexture(LOGO_TEXTURE)
            .build();
    }

    private static PonderTheme createDebugTheme() {
        return PonderTheme.builder("debug")
            .metric(ThemeMetric.CHROME_GLOW_INSET, 20.0F)
            .metric(ThemeMetric.CHROME_GLOW_BOTTOM_INSET, 26.0F)
            .metric(ThemeMetric.CHROME_TOP_GLOW_HEIGHT, 52.0F)
            .metric(ThemeMetric.CHROME_BOTTOM_GLOW_HEIGHT, 60.0F)
            .metric(ThemeMetric.CHROME_GLOW_ALPHA_SCALE, 52.0F)
            .metric(ThemeMetric.CHROME_BORDER_ALPHA_SCALE, 44.0F)
            .metric(ThemeMetric.CHROME_VIGNETTE_ALPHA_SCALE, 92.0F)
            .metric(ThemeMetric.CHROME_LOGO_ALPHA_SCALE, 0.72F)
            .metric(ThemeMetric.HEADER_MAX_WIDTH, 326.0F)
            .metric(ThemeMetric.HEADER_HEIGHT, 42.0F)
            .metric(ThemeMetric.HEADER_ALPHA_SCALE, 185.0F)
            .metric(ThemeMetric.HEADER_UNDERLINE_ALPHA_SCALE, 120.0F)
            .metric(ThemeMetric.HEADER_SLOT_OUTER_ALPHA_SCALE, 188.0F)
            .metric(ThemeMetric.HEADER_SLOT_INNER_ALPHA_SCALE, 64.0F)
            .metric(ThemeMetric.GROUP_CHIP_FILL_ALPHA_SCALE, 188.0F)
            .metric(ThemeMetric.GROUP_CHIP_BORDER_ALPHA_SCALE, 156.0F)
            .color(SymbolicColor.CHROME_GLOW, 0x17202A)
            .color(SymbolicColor.CHROME_BORDER_TOP, 0xF1E8D4)
            .color(SymbolicColor.CHROME_BORDER_BOTTOM, 0x2C3138)
            .color(SymbolicColor.CHROME_BORDER_SIDE, 0x4D545E)
            .color(SymbolicColor.CHROME_VIGNETTE, 0x030508)
            .color(SymbolicColor.HEADER_FILL, 0x141920)
            .color(SymbolicColor.HEADER_RIGHT_EDGE, 0x0E1218)
            .color(SymbolicColor.HEADER_UNDERLINE, 0xD5CCB8)
            .color(SymbolicColor.HEADER_SLOT_OUTER, 0x10151B)
            .color(SymbolicColor.HEADER_SLOT_INNER, 0xE7E0D1)
            .color(SymbolicColor.HEADER_EYEBROW, 0xB8C3CC)
            .color(SymbolicColor.HEADER_TITLE, 0xF6F2EA)
            .color(SymbolicColor.HEADER_SUBTITLE, 0xAEB8C1)
            .color(SymbolicColor.HEADER_INDEX, 0xFFC8D0D8)
            .color(SymbolicColor.GROUP_CHIP_FILL, 0x1A2028)
            .color(SymbolicColor.GROUP_CHIP_BORDER, 0xD5CCB8)
            .color(SymbolicColor.GROUP_CHIP_TEXT, 0xF2EFE7)
            .logoTexture(LOGO_TEXTURE)
            .build();
    }
}
