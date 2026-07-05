package net.createmod.ponder.foundation.external.parse;

import static net.createmod.ponder.foundation.external.validate.ExternalJsonValidation.copyJsonObject;
import static net.createmod.ponder.foundation.external.compat.ExternalPonderSceneCompat.*;
import static net.createmod.ponder.foundation.external.validate.ExternalJsonValidation.getElement;
import static net.createmod.ponder.foundation.external.validate.ExternalJsonValidation.hasField;
import static net.createmod.ponder.foundation.external.validate.ExternalJsonValidation.normalizeSceneId;
import static net.createmod.ponder.foundation.external.validate.ExternalJsonValidation.parseDoubleArray;
import static net.createmod.ponder.foundation.external.validate.ExternalJsonValidation.parseIntArray;
import static net.createmod.ponder.foundation.external.validate.ExternalJsonValidation.parseLocation;
import static net.createmod.ponder.foundation.external.validate.ExternalJsonValidation.parseLocations;
import static net.createmod.ponder.foundation.external.validate.ExternalJsonValidation.parseStrings;
import static net.createmod.ponder.foundation.external.validate.ExternalJsonValidation.readBoolean;
import static net.createmod.ponder.foundation.external.validate.ExternalJsonValidation.readFloat;
import static net.createmod.ponder.foundation.external.validate.ExternalJsonValidation.readInt;
import static net.createmod.ponder.foundation.external.validate.ExternalJsonValidation.readOptionalString;
import static net.createmod.ponder.foundation.external.validate.ExternalJsonValidation.readRequiredString;
import static net.createmod.ponder.foundation.external.validate.ExternalJsonValidation.readString;
import static net.createmod.ponder.foundation.external.validate.ExternalJsonValidation.stripJsonExtension;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import net.createmod.ponder.Ponder;
import net.createmod.ponder.foundation.external.definition.ComponentDefinition;
import net.createmod.ponder.foundation.external.definition.ExternalDefinitionSet;
import net.createmod.ponder.foundation.external.definition.InteractionDefinition;
import net.createmod.ponder.foundation.external.definition.SceneDefinition;
import net.createmod.ponder.foundation.external.definition.SceneOperationDefinition;
import net.createmod.ponder.foundation.external.definition.SharedTextDefinition;
import net.createmod.ponder.foundation.external.definition.SourceInfo;
import net.createmod.ponder.foundation.external.definition.TagDefinition;
import net.createmod.ponder.foundation.external.scan.ExternalPonderSceneScanner;
import net.createmod.ponder.foundation.external.scan.ExternalScanResult;
import net.createmod.ponder.foundation.external.validate.ExternalNbtValidation;
import net.minecraft.util.ResourceLocation;

public final class ExternalPonderSceneParser {

    private ExternalPonderSceneParser() {
    }

    public static ExternalDefinitionSet loadDefinitions() {
        List<TagDefinition> tags = new ArrayList<TagDefinition>();
        List<SceneDefinition> scenes = new ArrayList<SceneDefinition>();
        Map<ResourceLocation, InteractionDefinition> interactions =
            new LinkedHashMap<ResourceLocation, InteractionDefinition>();
        List<SharedTextDefinition> sharedTexts = new ArrayList<SharedTextDefinition>();

        ExternalScanResult scanResult = ExternalPonderSceneScanner.collectScanResult();
        for (File file : scanResult.files()) {
            ExternalDefinitionSet source = parseDefinitionFile(file);
            tags.addAll(source.tags());
            scenes.addAll(source.scenes());
            sharedTexts.addAll(source.sharedTexts());
            for (Map.Entry<ResourceLocation, InteractionDefinition> entry : source.interactions().entrySet()) {
                if (interactions.containsKey(entry.getKey())) {
                    Ponder.LOGGER.warn("Skipping duplicate external ponder interaction definition '{}'",
                        entry.getKey());
                    continue;
                }
                interactions.put(entry.getKey(), entry.getValue());
            }
        }

        return new ExternalDefinitionSet(tags, scenes, interactions, sharedTexts);
    }

    public static ExternalDefinitionSet parseDefinitionFile(File file) {
        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            JsonElement root = new JsonParser().parse(reader);
            List<TagDefinition> tags = new ArrayList<TagDefinition>();
            List<SceneDefinition> scenes = new ArrayList<SceneDefinition>();
            Map<ResourceLocation, InteractionDefinition> interactions =
                new LinkedHashMap<ResourceLocation, InteractionDefinition>();
            List<SharedTextDefinition> sharedTexts = new ArrayList<SharedTextDefinition>();

            if (root.isJsonObject()) {
                JsonObject rootObject = root.getAsJsonObject();
                String rootNamespace = readString(rootObject, "namespace", "ponder");
                tags.addAll(parseTagDefinitions(rootObject, file, rootNamespace));
                interactions.putAll(parseInteractionDefinitions(rootObject, file, rootNamespace));
                sharedTexts.addAll(parseSharedTextDefinitions(rootObject, file, rootNamespace));
                if (rootObject.has("scenes") && rootObject.get("scenes").isJsonArray()) {
                    JsonArray sceneArray = rootObject.getAsJsonArray("scenes");
                    for (JsonElement element : sceneArray) {
                        if (element.isJsonObject()) {
                            scenes.add(parseSceneObject(element.getAsJsonObject(), file, rootNamespace, interactions));
                        }
                    }
                } else if (looksLikeSceneObject(rootObject)) {
                    scenes.add(parseSceneObject(rootObject, file, rootNamespace, interactions));
                }
            } else if (root.isJsonArray()) {
                for (JsonElement element : root.getAsJsonArray()) {
                    if (element.isJsonObject()) {
                        scenes.add(parseSceneObject(element.getAsJsonObject(), file, "ponder", interactions));
                    }
                }
            } else {
                throw new JsonParseException("Root element must be an object or array");
            }

            return new ExternalDefinitionSet(tags, scenes, interactions, sharedTexts);
        } catch (RuntimeException | IOException exception) {
            Ponder.LOGGER.error("Failed to parse external ponder scene file {}", file, exception);
            return ExternalDefinitionSet.EMPTY;
        }
    }

    private static boolean looksLikeSceneObject(JsonObject object) {
        return object.has("component") || object.has("components") || object.has("schematic")
            || object.has("operations");
    }

    private static List<TagDefinition> parseTagDefinitions(JsonObject rootObject, File file, String defaultNamespace) {
        if (!rootObject.has("tagDefinitions") || !rootObject.get("tagDefinitions").isJsonArray()) {
            return Collections.emptyList();
        }

        List<TagDefinition> definitions = new ArrayList<TagDefinition>();
        for (JsonElement element : rootObject.getAsJsonArray("tagDefinitions")) {
            if (!element.isJsonObject()) {
                continue;
            }
            definitions.add(parseTagDefinition(element.getAsJsonObject(), file, defaultNamespace));
        }
        return definitions;
    }

    private static List<SharedTextDefinition> parseSharedTextDefinitions(JsonObject rootObject, File file,
        String defaultNamespace) {
        JsonElement definitionsElement = resolveSharedTexts(rootObject);
        if (definitionsElement == null || definitionsElement.isJsonNull()) {
            return Collections.emptyList();
        }

        List<SharedTextDefinition> definitions = new ArrayList<SharedTextDefinition>();
        if (definitionsElement.isJsonArray()) {
            for (JsonElement element : definitionsElement.getAsJsonArray()) {
                if (!element.isJsonObject()) {
                    continue;
                }
                SharedTextDefinition definition =
                    parseSharedTextDefinition(element.getAsJsonObject(), file, defaultNamespace, null);
                if (definition != null) {
                    definitions.add(definition);
                }
            }
            return definitions;
        }

        if (!definitionsElement.isJsonObject()) {
            return Collections.emptyList();
        }

        JsonObject object = definitionsElement.getAsJsonObject();
        if (hasField(object, "key") || hasField(object, "id") || hasField(object, "text")) {
            SharedTextDefinition definition = parseSharedTextDefinition(object, file, defaultNamespace, null);
            return definition == null ? Collections.emptyList() : Collections.singletonList(definition);
        }

        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            SharedTextDefinition definition =
                parseSharedTextDefinition(entry.getValue(), file, defaultNamespace, entry.getKey());
            if (definition != null) {
                definitions.add(definition);
            }
        }
        return definitions;
    }

    private static SharedTextDefinition parseSharedTextDefinition(JsonElement element, File file, String defaultNamespace,
        String fallbackKey) {
        if (element == null || element.isJsonNull()) {
            return null;
        }
        if (element.isJsonPrimitive()) {
            return new SharedTextDefinition(SourceInfo.of(file, defaultNamespace), fallbackKey, element.getAsString(),
                "WHITE", Integer.MIN_VALUE, 0, 0, true, false);
        }
        if (!element.isJsonObject()) {
            return null;
        }
        return parseSharedTextDefinition(element.getAsJsonObject(), file, defaultNamespace, fallbackKey);
    }

    private static SharedTextDefinition parseSharedTextDefinition(JsonObject object, File file, String defaultNamespace,
        String fallbackKey) {
        String namespace = readString(object, "namespace", defaultNamespace);
        String key = fallbackKey;
        if (key == null || key.trim().isEmpty()) {
            key = hasField(object, "key") ? readOptionalString(object, "key") : readOptionalString(object, "id");
        }
        if (key == null || key.trim().isEmpty()) {
            return null;
        }

        return new SharedTextDefinition(
            SourceInfo.of(file, namespace),
            key.trim(),
            readString(object, "text", ""),
            readString(object, "color", "WHITE"),
            object.has("independentY") ? readInt(object, "independentY", 0) : Integer.MIN_VALUE,
            readInt(object, "captionOffsetX", 0),
            readInt(object, "captionOffsetY", 0),
            readBoolean(object, "connector", true),
            readBoolean(object, "placeNearTarget", false)
        );
    }

    private static Map<ResourceLocation, InteractionDefinition> parseInteractionDefinitions(JsonObject rootObject,
        File file, String defaultNamespace) {
        JsonElement definitionsElement = resolveInteractionDefinitions(rootObject);
        if (definitionsElement == null || definitionsElement.isJsonNull()) {
            return Collections.emptyMap();
        }

        Map<ResourceLocation, InteractionDefinition> definitions =
            new LinkedHashMap<ResourceLocation, InteractionDefinition>();
        if (definitionsElement.isJsonArray()) {
            for (JsonElement element : definitionsElement.getAsJsonArray()) {
                if (!element.isJsonObject()) {
                    continue;
                }
                InteractionDefinition definition =
                    parseInteractionDefinition(element.getAsJsonObject(), null, file, defaultNamespace);
                if (definition != null && !definitions.containsKey(definition.id())) {
                    definitions.put(definition.id(), definition);
                }
            }
            return definitions;
        }

        if (definitionsElement.isJsonObject()) {
            JsonObject object = definitionsElement.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                if (!entry.getValue().isJsonObject()) {
                    continue;
                }
                InteractionDefinition definition =
                    parseInteractionDefinition(entry.getValue().getAsJsonObject(), entry.getKey(), file,
                        defaultNamespace);
                if (definition != null && !definitions.containsKey(definition.id())) {
                    definitions.put(definition.id(), definition);
                }
            }
        }
        return definitions;
    }

    private static InteractionDefinition parseInteractionDefinition(JsonObject object, String fallbackId, File file,
        String defaultNamespace) {
        JsonArray stepsArray = resolveInteractionStepsArray(object);
        if (stepsArray.size() == 0) {
            return null;
        }

        String namespace = readString(object, "namespace", defaultNamespace);
        String interactionId = fallbackId == null ? readRequiredString(object, "id") : readString(object, "id", fallbackId);
        JsonObject defaults = hasField(object, "defaults") && object.get("defaults").isJsonObject()
            ? copyJsonObject(object.getAsJsonObject("defaults"))
            : new JsonObject();
        List<JsonObject> steps = new ArrayList<JsonObject>();
        for (JsonElement step : stepsArray) {
            if (step.isJsonObject()) {
                steps.add(copyJsonObject(step.getAsJsonObject()));
            }
        }

        return new InteractionDefinition(
            SourceInfo.of(file, namespace),
            parseLocation(interactionId, namespace),
            defaults,
            steps
        );
    }

    private static TagDefinition parseTagDefinition(JsonObject object, File file, String defaultNamespace) {
        String namespace = readString(object, "namespace", defaultNamespace);
        ResourceLocation id = parseLocation(readRequiredString(object, "id"), namespace);
        return new TagDefinition(
            SourceInfo.of(file, namespace),
            id,
            readString(object, "title", id.toString()),
            readString(object, "description", ""),
            readOptionalString(object, "icon"),
            ItemStackParser.parseItemStack(readOptionalString(object, "item"),
                object.has("meta") ? readInt(object, "meta", 0) : 0, readNbtPayload(object.get("nbt"))),
            readBoolean(object, "useItemAsIcon", true),
            readBoolean(object, "useItemAsMainItem", true),
            readBoolean(object, "addToIndex", true)
        );
    }

    private static String readNbtPayload(JsonElement element) {
        return ExternalNbtValidation.readNbtPayload(element, "external ponder scene json");
    }

    private static SceneDefinition parseSceneObject(JsonObject object, File file, String defaultNamespace,
        Map<ResourceLocation, InteractionDefinition> interactions) {
        String namespace = readString(object, "namespace", defaultNamespace);
        List<SceneOperationDefinition> operations = new ArrayList<SceneOperationDefinition>();
        JsonArray operationArray = resolveSceneOperations(object);
        for (JsonElement operationElement : operationArray) {
            if (operationElement.isJsonObject()) {
                operations.add(parseOperation(operationElement.getAsJsonObject(), namespace, interactions));
            }
        }

        return new SceneDefinition(
            SourceInfo.of(file, namespace),
            parseComponents(object, namespace),
            readRequiredString(object, "schematic"),
            normalizeSceneId(readOptionalString(object, "sceneId"), stripJsonExtension(file.getName())),
            readString(object, "title", normalizeSceneId(readOptionalString(object, "sceneId"),
                stripJsonExtension(file.getName()))),
            parseLocations(object.get("tags"), namespace),
            parseLocations(resolveComponentTags(object), namespace),
            parseStrings(object.get("orderBefore")),
            parseStrings(object.get("orderAfter")),
            operations
        );
    }

    private static List<ComponentDefinition> parseComponents(JsonObject object, String namespace) {
        List<ComponentDefinition> definitions = new ArrayList<ComponentDefinition>();
        if (object.has("components") && object.get("components").isJsonArray()) {
            for (JsonElement element : object.getAsJsonArray("components")) {
                if (element.isJsonObject()) {
                    definitions.add(ComponentDefinitionFactory.parseComponentObject(element.getAsJsonObject(), object));
                } else if (element.isJsonPrimitive()) {
                    definitions.add(ComponentDefinitionFactory.parseComponentObject(
                        ComponentJsonFactory.buildPrimitiveComponentObject(element.getAsString()), object));
                }
            }
        } else {
            definitions.add(ComponentDefinitionFactory.parseComponentObject(object, null));
        }
        return definitions;
    }

    private static SceneOperationDefinition parseOperation(JsonObject object, String defaultNamespace,
        Map<ResourceLocation, InteractionDefinition> interactions) {
        return parseOperation(object, defaultNamespace, interactions, new LinkedHashSet<ResourceLocation>());
    }

    private static SceneOperationDefinition parseOperation(JsonObject object, String defaultNamespace,
        Map<ResourceLocation, InteractionDefinition> interactions, Set<ResourceLocation> activeInteractions) {
        String type = readRequiredString(object, "type").toLowerCase(Locale.ROOT);
        if ("gui_interaction".equals(type) || "sequence".equals(type)) {
            return parseInteractionOperation(type, object, defaultNamespace, interactions, activeInteractions);
        }
        return SceneOperationDefinitionFactory.createSimple(type, object);
    }

    private static SceneOperationDefinition parseInteractionOperation(String type, JsonObject object,
        String defaultNamespace, Map<ResourceLocation, InteractionDefinition> interactions,
        Set<ResourceLocation> activeInteractions) {
        if (!readBoolean(object, "enabled", true)) {
            return SceneOperationDefinitionFactory.createDisabledCompound(type, object);
        }

        JsonArray inlineSteps = resolveInteractionStepsArray(object);
        InteractionDefinition referencedDefinition = null;
        String interactionName = readOptionalString(object, "interaction");
        if (interactionName != null && !interactionName.trim().isEmpty()) {
            ResourceLocation interactionId = parseLocation(interactionName, defaultNamespace);
            referencedDefinition = interactions.get(interactionId);
            if (referencedDefinition == null) {
                throw new IllegalArgumentException("Unknown gui interaction definition: " + interactionId);
            }
            if (!activeInteractions.add(interactionId)) {
                throw new IllegalArgumentException("Recursive gui interaction definition detected: " + interactionId);
            }
        }

        try {
            JsonObject mergedDefaults = referencedDefinition == null ? new JsonObject()
                : copyJsonObject(referencedDefinition.defaults());
            if (hasField(object, "defaults") && object.get("defaults").isJsonObject()) {
                mergedDefaults = mergeJsonObjects(mergedDefaults, object.getAsJsonObject("defaults"));
            }
            mergedDefaults = mergeJsonObjects(mergedDefaults, extractInteractionInvocationDefaults(object));

            List<SceneOperationDefinition> childOperations = new ArrayList<SceneOperationDefinition>();
            List<JsonObject> rawSteps = new ArrayList<JsonObject>();
            if (referencedDefinition != null) {
                rawSteps.addAll(referencedDefinition.steps());
            }
            for (JsonElement step : inlineSteps) {
                if (step.isJsonObject()) {
                    rawSteps.add(step.getAsJsonObject());
                }
            }

            String stepNamespace = referencedDefinition == null ? defaultNamespace : referencedDefinition.source().namespace();
            String scopeId = readOptionalString(object, "id");
            for (JsonObject rawStep : rawSteps) {
                JsonObject mergedStep = mergeJsonObjects(mergedDefaults, rawStep);
                if (scopeId != null && !scopeId.trim().isEmpty()) {
                    mergedStep = applyInteractionScope(mergedStep, scopeId.trim());
                }
                childOperations.add(parseOperation(mergedStep, stepNamespace, interactions, activeInteractions));
            }

            return SceneOperationDefinitionFactory.createCompound(type, object, childOperations);
        } finally {
            if (referencedDefinition != null) {
                activeInteractions.remove(referencedDefinition.id());
            }
        }
    }
}
