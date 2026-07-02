package net.createmod.ponder.foundation.ui;

import java.util.EnumMap;
import java.util.Objects;

import javax.annotation.Nullable;

import net.createmod.ponder.foundation.PonderScene;
import net.minecraft.util.ResourceLocation;

final class PonderTheme {

    interface HudTextProvider {
        String getPlaybackBarHoverLabel(int estimatedTick, @Nullable PonderScene scene);

        String getGroupSelectorHoverLabel();

        String getNextUpHoverLabel();

        String getSceneShortLabel(int sceneIndex);

        String getNextUpLabel();
    }

    static Builder builder(String id) {
        return new Builder(id);
    }

    private final String id;
    private final EnumMap<SymbolicColor, Integer> colors;
    private final EnumMap<ThemeMetric, Float> metrics;
    private final ResourceLocation logoTexture;

    private PonderTheme(String id, EnumMap<SymbolicColor, Integer> colors, EnumMap<ThemeMetric, Float> metrics,
        ResourceLocation logoTexture) {
        this.id = id;
        this.colors = colors;
        this.metrics = metrics;
        this.logoTexture = logoTexture;
    }

    String id() {
        return id;
    }

    ResourceLocation logoTexture() {
        return logoTexture;
    }

    int color(SymbolicColor color) {
        Integer value = colors.get(color);
        if (value == null) {
            throw new IllegalStateException("Missing symbolic color " + color + " for theme " + id);
        }
        return value.intValue();
    }

    float metric(ThemeMetric metric) {
        Float value = metrics.get(metric);
        if (value == null) {
            throw new IllegalStateException("Missing theme metric " + metric + " for theme " + id);
        }
        return value.floatValue();
    }

    int metricInt(ThemeMetric metric) {
        return (int) metric(metric);
    }

    ShowcaseChromeRenderer.Theme createShowcaseChromeTheme() {
        return new ShowcaseChromeRenderer.Theme(metricInt(ThemeMetric.CHROME_GLOW_INSET),
            metricInt(ThemeMetric.CHROME_GLOW_BOTTOM_INSET), metricInt(ThemeMetric.CHROME_TOP_GLOW_HEIGHT),
            metricInt(ThemeMetric.CHROME_BOTTOM_GLOW_HEIGHT), metric(ThemeMetric.CHROME_GLOW_ALPHA_SCALE),
            metric(ThemeMetric.CHROME_BORDER_ALPHA_SCALE), metric(ThemeMetric.CHROME_VIGNETTE_ALPHA_SCALE),
            color(SymbolicColor.CHROME_GLOW), color(SymbolicColor.CHROME_BORDER_TOP),
            color(SymbolicColor.CHROME_BORDER_BOTTOM), color(SymbolicColor.CHROME_BORDER_SIDE),
            color(SymbolicColor.CHROME_VIGNETTE), logoTexture, metric(ThemeMetric.CHROME_LOGO_ALPHA_SCALE));
    }

    ShowcaseRenderer.Theme createShowcaseRendererTheme(ShowcaseRenderer.HeaderEyebrowFactory eyebrowFactory,
        ShowcaseRenderer.GroupPopupLabelFactory popupLabelFactory) {
        return new ShowcaseRenderer.Theme(metricInt(ThemeMetric.HEADER_MAX_WIDTH), metricInt(ThemeMetric.HEADER_HEIGHT),
            metric(ThemeMetric.HEADER_ALPHA_SCALE), color(SymbolicColor.HEADER_FILL),
            color(SymbolicColor.HEADER_RIGHT_EDGE), metric(ThemeMetric.HEADER_UNDERLINE_ALPHA_SCALE),
            color(SymbolicColor.HEADER_UNDERLINE), metric(ThemeMetric.HEADER_SLOT_OUTER_ALPHA_SCALE),
            color(SymbolicColor.HEADER_SLOT_OUTER), metric(ThemeMetric.HEADER_SLOT_INNER_ALPHA_SCALE),
            color(SymbolicColor.HEADER_SLOT_INNER), color(SymbolicColor.HEADER_EYEBROW),
            color(SymbolicColor.HEADER_TITLE), color(SymbolicColor.HEADER_SUBTITLE),
            color(SymbolicColor.HEADER_INDEX), metric(ThemeMetric.GROUP_CHIP_FILL_ALPHA_SCALE),
            color(SymbolicColor.GROUP_CHIP_FILL), metric(ThemeMetric.GROUP_CHIP_BORDER_ALPHA_SCALE),
            color(SymbolicColor.GROUP_CHIP_BORDER), color(SymbolicColor.GROUP_CHIP_TEXT), eyebrowFactory,
            popupLabelFactory);
    }

    ShowcaseHudRenderer.Theme createShowcaseHudTheme(final HudTextProvider textProvider) {
        final HudTextProvider provider = Objects.requireNonNull(textProvider, "textProvider");
        return new ShowcaseHudRenderer.Theme() {
            @Override
            public String getPlaybackBarHoverLabel(int estimatedTick, @Nullable PonderScene scene) {
                return provider.getPlaybackBarHoverLabel(estimatedTick, scene);
            }

            @Override
            public String getGroupSelectorHoverLabel() {
                return provider.getGroupSelectorHoverLabel();
            }

            @Override
            public String getNextUpHoverLabel() {
                return provider.getNextUpHoverLabel();
            }

            @Override
            public String getSceneShortLabel(int sceneIndex) {
                return provider.getSceneShortLabel(sceneIndex);
            }

            @Override
            public String getNextUpLabel() {
                return provider.getNextUpLabel();
            }
        };
    }

    static final class Builder {
        private final String id;
        private final EnumMap<SymbolicColor, Integer> colors = new EnumMap<>(SymbolicColor.class);
        private final EnumMap<ThemeMetric, Float> metrics = new EnumMap<>(ThemeMetric.class);
        private ResourceLocation logoTexture;

        private Builder(String id) {
            this.id = Objects.requireNonNull(id, "id");
        }

        Builder color(SymbolicColor color, int value) {
            colors.put(Objects.requireNonNull(color, "color"), Integer.valueOf(value));
            return this;
        }

        Builder metric(ThemeMetric metric, float value) {
            metrics.put(Objects.requireNonNull(metric, "metric"), Float.valueOf(value));
            return this;
        }

        Builder logoTexture(ResourceLocation logoTexture) {
            this.logoTexture = Objects.requireNonNull(logoTexture, "logoTexture");
            return this;
        }

        PonderTheme build() {
            if (logoTexture == null) {
                throw new IllegalStateException("Missing logo texture for theme " + id);
            }
            return new PonderTheme(id, new EnumMap<>(colors), new EnumMap<>(metrics), logoTexture);
        }
    }
}
