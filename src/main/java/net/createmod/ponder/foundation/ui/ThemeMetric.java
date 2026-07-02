package net.createmod.ponder.foundation.ui;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nullable;

enum ThemeMetric {
    CHROME_GLOW_INSET("chrome_glow_inset"),
    CHROME_GLOW_BOTTOM_INSET("chrome_glow_bottom_inset"),
    CHROME_TOP_GLOW_HEIGHT("chrome_top_glow_height"),
    CHROME_BOTTOM_GLOW_HEIGHT("chrome_bottom_glow_height"),
    CHROME_GLOW_ALPHA_SCALE("chrome_glow_alpha_scale"),
    CHROME_BORDER_ALPHA_SCALE("chrome_border_alpha_scale"),
    CHROME_VIGNETTE_ALPHA_SCALE("chrome_vignette_alpha_scale"),
    CHROME_LOGO_ALPHA_SCALE("chrome_logo_alpha_scale"),
    HEADER_MAX_WIDTH("header_max_width"),
    HEADER_HEIGHT("header_height"),
    HEADER_ALPHA_SCALE("header_alpha_scale"),
    HEADER_UNDERLINE_ALPHA_SCALE("header_underline_alpha_scale"),
    HEADER_SLOT_OUTER_ALPHA_SCALE("header_slot_outer_alpha_scale"),
    HEADER_SLOT_INNER_ALPHA_SCALE("header_slot_inner_alpha_scale"),
    GROUP_CHIP_FILL_ALPHA_SCALE("group_chip_fill_alpha_scale"),
    GROUP_CHIP_BORDER_ALPHA_SCALE("group_chip_border_alpha_scale");

    private static final Map<String, ThemeMetric> LOOKUP = createLookup();

    private final String resourceKey;

    ThemeMetric(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    String resourceKey() {
        return resourceKey;
    }

    @Nullable
    static ThemeMetric fromResourceKey(String resourceKey) {
        if (resourceKey == null) {
            return null;
        }
        return LOOKUP.get(normalize(resourceKey));
    }

    private static Map<String, ThemeMetric> createLookup() {
        Map<String, ThemeMetric> lookup = new HashMap<String, ThemeMetric>();
        for (ThemeMetric metric : values()) {
            lookup.put(metric.resourceKey, metric);
            lookup.put(metric.name().toLowerCase(Locale.ROOT), metric);
        }
        return lookup;
    }

    private static String normalize(String resourceKey) {
        return resourceKey.trim().toLowerCase(Locale.ROOT).replace('.', '_').replace('-', '_').replace(' ', '_');
    }
}
