package net.createmod.ponder.foundation.external.validate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.util.ResourceLocation;

public final class ExternalJsonValidation {

    private ExternalJsonValidation() {
    }

    public static boolean hasField(JsonObject object, String key) {
        return object != null && object.has(key) && !object.get(key).isJsonNull();
    }

    public static JsonElement getElement(JsonObject object, JsonObject fallback, String key) {
        if (hasField(object, key)) {
            return object.get(key);
        }
        return hasField(fallback, key) ? fallback.get(key) : null;
    }

    public static JsonElement firstPresent(JsonObject object, String primaryKey, String secondaryKey) {
        if (hasField(object, primaryKey)) {
            return object.get(primaryKey);
        }
        return hasField(object, secondaryKey) ? object.get(secondaryKey) : null;
    }

    public static String readRequiredString(JsonObject object, String key) {
        String value = readOptionalString(object, key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing required json string field '" + key + "'");
        }
        return value;
    }

    public static String readRequiredString(JsonObject object, JsonObject fallback, String key) {
        String value = readOptionalString(object, fallback, key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing required json string field '" + key + "'");
        }
        return value;
    }

    public static String readOptionalString(JsonObject object, String key) {
        return object.has(key) && object.get(key).isJsonPrimitive() ? object.get(key).getAsString() : null;
    }

    public static String readOptionalString(JsonObject object, JsonObject fallback, String key) {
        String primary = readOptionalString(object, key);
        if (primary != null) {
            return primary;
        }
        return fallback == null ? null : readOptionalString(fallback, key);
    }

    public static String readString(JsonObject object, String key, String fallback) {
        String value = readOptionalString(object, key);
        return value == null ? fallback : value;
    }

    public static int readInt(JsonObject object, String key, int fallback) {
        return object.has(key) && object.get(key).isJsonPrimitive() ? object.get(key).getAsInt() : fallback;
    }

    public static float readFloat(JsonObject object, String key, float fallback) {
        return object.has(key) && object.get(key).isJsonPrimitive() ? object.get(key).getAsFloat() : fallback;
    }

    public static boolean readBoolean(JsonObject object, String key, boolean fallback) {
        return object.has(key) && object.get(key).isJsonPrimitive() ? object.get(key).getAsBoolean() : fallback;
    }

    public static List<String> parseStrings(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return Collections.emptyList();
        }

        List<String> strings = new ArrayList<String>();
        if (element.isJsonArray()) {
            for (JsonElement arrayElement : element.getAsJsonArray()) {
                if (arrayElement.isJsonPrimitive()) {
                    strings.add(arrayElement.getAsString());
                }
            }
        } else if (element.isJsonPrimitive()) {
            strings.add(element.getAsString());
        }
        return strings;
    }

    public static List<ResourceLocation> parseLocations(JsonElement element, String defaultNamespace) {
        if (element == null || element.isJsonNull()) {
            return Collections.emptyList();
        }

        List<ResourceLocation> locations = new ArrayList<ResourceLocation>();
        if (element.isJsonArray()) {
            for (JsonElement arrayElement : element.getAsJsonArray()) {
                if (arrayElement.isJsonPrimitive()) {
                    locations.add(parseLocation(arrayElement.getAsString(), defaultNamespace));
                }
            }
        } else if (element.isJsonPrimitive()) {
            locations.add(parseLocation(element.getAsString(), defaultNamespace));
        }
        return locations;
    }

    public static int[] parseIntArray(JsonElement element, int expectedSize) {
        if (element == null || element.isJsonNull() || !element.isJsonArray()) {
            return null;
        }

        JsonArray array = element.getAsJsonArray();
        if (array.size() < expectedSize) {
            return null;
        }

        int[] values = new int[expectedSize];
        for (int i = 0; i < expectedSize; i++) {
            values[i] = array.get(i).getAsInt();
        }
        return values;
    }

    public static double[] parseDoubleArray(JsonElement element, int expectedSize) {
        if (element == null || element.isJsonNull() || !element.isJsonArray()) {
            return null;
        }

        JsonArray array = element.getAsJsonArray();
        if (array.size() < expectedSize) {
            return null;
        }

        double[] values = new double[expectedSize];
        for (int i = 0; i < expectedSize; i++) {
            values[i] = array.get(i).getAsDouble();
        }
        return values;
    }

    public static JsonObject copyJsonObject(JsonObject source) {
        if (source == null) {
            return new JsonObject();
        }
        return new JsonParser().parse(source.toString()).getAsJsonObject();
    }

    public static String normalizeSceneId(String sceneId, String fallback) {
        if (sceneId == null || sceneId.trim().isEmpty()) {
            return fallback;
        }

        String trimmed = sceneId.trim();
        int separatorIndex = trimmed.indexOf(':');
        return separatorIndex >= 0 ? trimmed.substring(separatorIndex + 1) : trimmed;
    }

    public static String stripJsonExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex >= 0 ? fileName.substring(0, dotIndex) : fileName;
    }

    public static ResourceLocation parseLocation(String value, String defaultNamespace) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Resource location cannot be empty");
        }

        String trimmed = value.trim();
        if (trimmed.indexOf(':') >= 0) {
            return new ResourceLocation(trimmed);
        }
        return new ResourceLocation(defaultNamespace, trimmed);
    }

    public static ResourceLocation parseTextureLocation(String value, String defaultNamespace) {
        ResourceLocation location = parseLocation(value, defaultNamespace);
        String path = location.getPath();
        if (!path.startsWith("textures/")) {
            path = "textures/" + path;
        }
        if (!path.endsWith(".png")) {
            path = path + ".png";
        }
        return new ResourceLocation(location.getNamespace(), path);
    }
}
