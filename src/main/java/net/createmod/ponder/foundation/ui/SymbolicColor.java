package net.createmod.ponder.foundation.ui;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nullable;

enum SymbolicColor {
    CHROME_GLOW("chrome_glow"),
    CHROME_BORDER_TOP("chrome_border_top"),
    CHROME_BORDER_BOTTOM("chrome_border_bottom"),
    CHROME_BORDER_SIDE("chrome_border_side"),
    CHROME_VIGNETTE("chrome_vignette"),
    HEADER_FILL("header_fill"),
    HEADER_RIGHT_EDGE("header_right_edge"),
    HEADER_UNDERLINE("header_underline"),
    HEADER_SLOT_OUTER("header_slot_outer"),
    HEADER_SLOT_INNER("header_slot_inner"),
    HEADER_EYEBROW("header_eyebrow"),
    HEADER_TITLE("header_title"),
    HEADER_SUBTITLE("header_subtitle"),
    HEADER_INDEX("header_index"),
    GROUP_CHIP_FILL("group_chip_fill"),
    GROUP_CHIP_BORDER("group_chip_border"),
    GROUP_CHIP_TEXT("group_chip_text");

    private static final Map<String, SymbolicColor> LOOKUP = createLookup();

    private final String resourceKey;

    SymbolicColor(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    String resourceKey() {
        return resourceKey;
    }

    @Nullable
    static SymbolicColor fromResourceKey(String resourceKey) {
        if (resourceKey == null) {
            return null;
        }
        return LOOKUP.get(normalize(resourceKey));
    }

    private static Map<String, SymbolicColor> createLookup() {
        Map<String, SymbolicColor> lookup = new HashMap<String, SymbolicColor>();
        for (SymbolicColor color : values()) {
            lookup.put(color.resourceKey, color);
            lookup.put(color.name().toLowerCase(Locale.ROOT), color);
        }
        return lookup;
    }

    private static String normalize(String resourceKey) {
        return resourceKey.trim().toLowerCase(Locale.ROOT).replace('.', '_').replace('-', '_').replace(' ', '_');
    }
}
