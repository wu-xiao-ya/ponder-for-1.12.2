package net.createmod.ponder.foundation.ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.util.ResourceLocation;

final class ThemeResolver {

    private static final String THEME_RESOURCE_PATH = "assets/ponder/ponder_themes.json";
    private static final ThemePack THEME_PACK = loadThemePack();

    private ThemeResolver() {
    }

    static PonderTheme resolve(String themeId) {
        String normalizedId = normalizeThemeId(themeId);
        PonderTheme preset = PonderThemes.preset(normalizedId);
        ThemeResourceTheme resourceTheme = THEME_PACK.theme(normalizedId);
        if (resourceTheme == null) {
            return preset;
        }
        return resourceTheme.applyTo(preset);
    }

    private static String normalizeThemeId(String themeId) {
        if (themeId == null || themeId.trim().isEmpty()) {
            return "showcase";
        }
        return themeId.trim().toLowerCase(Locale.ROOT);
    }

    private static ThemePack loadThemePack() {
        InputStream stream = ThemeResolver.class.getClassLoader().getResourceAsStream(THEME_RESOURCE_PATH);
        if (stream == null) {
            return ThemePack.empty();
        }

        try (InputStream input = stream; InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
            JsonElement rootElement = new JsonParser().parse(reader);
            if (!rootElement.isJsonObject()) {
                return ThemePack.empty();
            }
            return ThemePack.parse(rootElement.getAsJsonObject());
        } catch (IOException | RuntimeException e) {
            return ThemePack.empty();
        }
    }

    private static JsonObject themeRoot(JsonObject root) {
        if (root != null && root.has("themes") && root.get("themes").isJsonObject()) {
            return root.getAsJsonObject("themes");
        }
        return root;
    }

    private static Integer parseColorValue(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return null;
        }
        try {
            if (element.isJsonPrimitive()) {
                if (element.getAsJsonPrimitive().isNumber()) {
                    return Integer.valueOf(element.getAsInt());
                }
                if (element.getAsJsonPrimitive().isString()) {
                    return Integer.valueOf(parseColorString(element.getAsString()));
                }
            }
        } catch (RuntimeException e) {
            return null;
        }
        return null;
    }

    private static int parseColorString(String rawColor) {
        String trimmed = rawColor == null ? "" : rawColor.trim();
        if (trimmed.startsWith("#")) {
            trimmed = trimmed.substring(1);
        }
        if (trimmed.startsWith("0x") || trimmed.startsWith("0X")) {
            trimmed = trimmed.substring(2);
        }
        return (int) Long.parseLong(trimmed, 16);
    }

    private static Float parseMetricValue(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return null;
        }
        try {
            if (element.isJsonPrimitive()) {
                if (element.getAsJsonPrimitive().isNumber()) {
                    return Float.valueOf(element.getAsFloat());
                }
                if (element.getAsJsonPrimitive().isString()) {
                    return Float.valueOf(Float.parseFloat(element.getAsString().trim()));
                }
            }
        } catch (RuntimeException e) {
            return null;
        }
        return null;
    }

    private static ResourceLocation parseResourceLocation(String rawLocation) {
        if (rawLocation == null) {
            return null;
        }

        String trimmed = rawLocation.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        try {
            return new ResourceLocation(trimmed);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static final class ThemePack {
        private static final ThemePack EMPTY = new ThemePack(Collections.<String, ThemeResourceTheme>emptyMap());

        private final Map<String, ThemeResourceTheme> themes;

        private ThemePack(Map<String, ThemeResourceTheme> themes) {
            this.themes = themes;
        }

        static ThemePack empty() {
            return EMPTY;
        }

        static ThemePack parse(JsonObject root) {
            if (root == null) {
                return empty();
            }

            if (root.has("schema_version") && root.get("schema_version").isJsonPrimitive()) {
                int schemaVersion = root.get("schema_version").getAsInt();
                if (schemaVersion != 1) {
                    return empty();
                }
            }

            JsonObject themeObjectRoot = themeRoot(root);
            if (themeObjectRoot == null) {
                return empty();
            }

            Map<String, ThemeResourceTheme> parsedThemes = new LinkedHashMap<String, ThemeResourceTheme>();
            for (Map.Entry<String, JsonElement> entry : themeObjectRoot.entrySet()) {
                if (entry.getValue() != null && entry.getValue().isJsonObject()) {
                    ThemeResourceTheme theme = ThemeResourceTheme.parse(entry.getKey(), entry.getValue().getAsJsonObject());
                    parsedThemes.put(theme.id, theme);
                }
            }
            return new ThemePack(Collections.unmodifiableMap(parsedThemes));
        }

        ThemeResourceTheme theme(String themeId) {
            return themes.get(themeId);
        }
    }

    private static final class ThemeResourceTheme {
        private final String id;
        private final EnumMap<SymbolicColor, Integer> colors;
        private final EnumMap<ThemeMetric, Float> metrics;
        private final ResourceLocation logoTexture;

        private ThemeResourceTheme(String id, EnumMap<SymbolicColor, Integer> colors,
            EnumMap<ThemeMetric, Float> metrics, ResourceLocation logoTexture) {
            this.id = id;
            this.colors = colors;
            this.metrics = metrics;
            this.logoTexture = logoTexture;
        }

        private static ThemeResourceTheme parse(String id, JsonObject object) {
            EnumMap<SymbolicColor, Integer> colors = new EnumMap<SymbolicColor, Integer>(SymbolicColor.class);
            JsonObject colorObject = object.has("colors") && object.get("colors").isJsonObject()
                ? object.getAsJsonObject("colors")
                : null;
            if (colorObject != null) {
                for (Map.Entry<String, JsonElement> entry : colorObject.entrySet()) {
                    SymbolicColor color = SymbolicColor.fromResourceKey(entry.getKey());
                    Integer value = parseColorValue(entry.getValue());
                    if (color != null && value != null) {
                        colors.put(color, value);
                    }
                }
            }

            EnumMap<ThemeMetric, Float> metrics = new EnumMap<ThemeMetric, Float>(ThemeMetric.class);
            JsonObject metricObject = object.has("metrics") && object.get("metrics").isJsonObject()
                ? object.getAsJsonObject("metrics")
                : null;
            if (metricObject != null) {
                for (Map.Entry<String, JsonElement> entry : metricObject.entrySet()) {
                    ThemeMetric metric = ThemeMetric.fromResourceKey(entry.getKey());
                    Float value = parseMetricValue(entry.getValue());
                    if (metric != null && value != null) {
                        metrics.put(metric, value);
                    }
                }
            }

            ResourceLocation logoTexture = null;
            if (object.has("logo_texture") && object.get("logo_texture").isJsonPrimitive()) {
                logoTexture = parseResourceLocation(object.get("logo_texture").getAsString());
            }

            return new ThemeResourceTheme(id, colors, metrics, logoTexture);
        }

        private PonderTheme applyTo(PonderTheme preset) {
            PonderTheme.Builder builder = PonderTheme.builder(preset);
            for (Map.Entry<SymbolicColor, Integer> entry : colors.entrySet()) {
                builder.color(entry.getKey(), entry.getValue().intValue());
            }
            for (Map.Entry<ThemeMetric, Float> entry : metrics.entrySet()) {
                builder.metric(entry.getKey(), entry.getValue().floatValue());
            }
            if (logoTexture != null) {
                builder.logoTexture(logoTexture);
            }
            return builder.build();
        }
    }
}
