package net.createmod.ponder.foundation.external.compat;

import static net.createmod.ponder.foundation.external.validate.ExternalJsonValidation.copyJsonObject;
import static net.createmod.ponder.foundation.external.validate.ExternalJsonValidation.firstPresent;
import static net.createmod.ponder.foundation.external.validate.ExternalJsonValidation.hasField;

import java.util.Map;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public final class ExternalPonderSceneCompat {

    private static final Set<String> INTERACTION_BASE_KEYS = Set.of(
        "type", "interaction", "steps", "operations", "defaults", "id"
    );

    private ExternalPonderSceneCompat() {}

    // --- root-level definition aliases ---

    public static JsonElement resolveSharedTexts(JsonObject root) {
        if (hasField(root, "sharedTexts")) return root.get("sharedTexts");
        if (hasField(root, "sharedText")) return root.get("sharedText");
        return null;
    }

    public static JsonElement resolveInteractionDefinitions(JsonObject root) {
        if (hasField(root, "interactionDefinitions")) return root.get("interactionDefinitions");
        if (hasField(root, "guiInteractions")) return root.get("guiInteractions");
        return null;
    }

    public static JsonArray resolveSceneOperations(JsonObject object) {
        return object.has("operations") && object.get("operations").isJsonArray()
            ? object.getAsJsonArray("operations")
            : new JsonArray();
    }

    // --- interaction-step-level aliases (old field "operations") ---

    public static JsonElement resolveInteractionSteps(JsonObject object) {
        if (hasField(object, "steps")) return object.get("steps");
        if (hasField(object, "operations")) return object.get("operations");
        return null;
    }

    public static JsonArray resolveInteractionStepsArray(JsonObject object) {
        JsonElement stepsElement = resolveInteractionSteps(object);
        if (stepsElement == null || !stepsElement.isJsonArray()) {
            return new JsonArray();
        }
        return stepsElement.getAsJsonArray();
    }

    public static boolean isInteractionBaseKey(String key) {
        return INTERACTION_BASE_KEYS.contains(key);
    }

    public static JsonObject extractInteractionInvocationDefaults(JsonObject object) {
        JsonObject defaults = new JsonObject();
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            if (isInteractionBaseKey(entry.getKey())) {
                continue;
            }
            defaults.add(entry.getKey(), entry.getValue());
        }
        return defaults;
    }

    public static JsonObject mergeJsonObjects(JsonObject defaults, JsonObject overrides) {
        JsonObject merged = copyJsonObject(defaults);
        if (overrides == null) {
            return merged;
        }
        for (Map.Entry<String, JsonElement> entry : overrides.entrySet()) {
            merged.add(entry.getKey(), entry.getValue());
        }
        return merged;
    }

    public static JsonObject applyInteractionScope(JsonObject stepObject, String scopeId) {
        JsonObject scoped = copyJsonObject(stepObject);
        prefixScopedField(scoped, "id", scopeId);
        prefixScopedField(scoped, "gui", scopeId);
        prefixScopedField(scoped, "parentGui", scopeId);
        return scoped;
    }

    // --- per-operation field aliases ---

    public static JsonElement resolveRotation(JsonObject object) {
        return firstPresent(object, "rotation", "rot");
    }

    public static JsonElement resolveComponentTags(JsonObject object) {
        return firstPresent(object, "componentTags", "groups");
    }

    private static void prefixScopedField(JsonObject object, String key, String scopeId) {
        if (!hasField(object, key) || !object.get(key).isJsonPrimitive()) {
            return;
        }
        String value = object.get(key).getAsString();
        if (value == null || value.trim().isEmpty()) {
            return;
        }
        object.addProperty(key, scopeId + "." + value.trim());
    }
}
