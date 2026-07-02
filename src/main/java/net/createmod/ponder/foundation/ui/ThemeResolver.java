package net.createmod.ponder.foundation.ui;

import java.util.Locale;

final class ThemeResolver {

    private ThemeResolver() {
    }

    static PonderTheme resolve(String themeId) {
        if (themeId == null) {
            return PonderThemes.showcase();
        }

        String normalizedId = themeId.trim().toLowerCase(Locale.ROOT);
        if ("debug".equals(normalizedId)) {
            return PonderThemes.debug();
        }
        if ("showcase".equals(normalizedId)) {
            return PonderThemes.showcase();
        }
        return PonderThemes.showcase();
    }
}
