package net.createmod.ponder.foundation.external.parse;

import static net.createmod.ponder.foundation.external.compat.ExternalPonderSceneCompat.resolveRotation;
import static net.createmod.ponder.foundation.external.validate.ExternalJsonValidation.parseDoubleArray;
import static net.createmod.ponder.foundation.external.validate.ExternalJsonValidation.parseIntArray;
import static net.createmod.ponder.foundation.external.validate.ExternalJsonValidation.readBoolean;
import static net.createmod.ponder.foundation.external.validate.ExternalJsonValidation.readFloat;
import static net.createmod.ponder.foundation.external.validate.ExternalJsonValidation.readInt;
import static net.createmod.ponder.foundation.external.validate.ExternalJsonValidation.readOptionalString;
import static net.createmod.ponder.foundation.external.validate.ExternalJsonValidation.readString;

import java.util.Collections;
import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.createmod.ponder.foundation.external.definition.SceneOperationDefinition;
import net.createmod.ponder.foundation.external.validate.ExternalNbtValidation;

public final class SceneOperationDefinitionFactory {

    private SceneOperationDefinitionFactory() {
    }

    public static SceneOperationDefinition createSimple(String type, JsonObject object) {
        return create(type, object, readBoolean(object, "enabled", true), Collections.emptyList());
    }

    public static SceneOperationDefinition createDisabledCompound(String type, JsonObject object) {
        return create(type, object, false, Collections.emptyList());
    }

    public static SceneOperationDefinition createCompound(String type, JsonObject object,
        List<SceneOperationDefinition> childOperations) {
        return create(type, object, true, childOperations);
    }

    private static SceneOperationDefinition create(String type, JsonObject object, boolean enabled,
        List<SceneOperationDefinition> childOperations) {
        return new SceneOperationDefinition(
            type,
            readInt(object, "xOffset", 0),
            readInt(object, "zOffset", 0),
            readInt(object, "size", 5),
            readInt(object, "duration", 40),
            readInt(object, "ticks", 1),
            readInt(object, "times", 1),
            object.has("independentY") ? readInt(object, "independentY", 0) : Integer.MIN_VALUE,
            readFloat(object, "factor", 1.0F),
            readFloat(object, "degrees", 0.0F),
            readFloat(object, "offsetY", 0.0F),
            enabled,
            readString(object, "text", ""),
            readString(object, "color", "WHITE"),
            readOptionalString(object, "id"),
            readOptionalString(object, "gui"),
            readOptionalString(object, "parentGui"),
            readOptionalString(object, "blockGui"),
            readOptionalString(object, "state"),
            object.has("meta") ? readInt(object, "meta", 0) : -1,
            readNbtPayload(object.get("nbt")),
            readOptionalString(object, "direction"),
            object.has("captionX") ? readInt(object, "captionX", 0) : Integer.MIN_VALUE,
            object.has("captionY") ? readInt(object, "captionY", 0) : Integer.MIN_VALUE,
            readInt(object, "captionOffsetX", 0),
            readInt(object, "captionOffsetY", 0),
            readBoolean(object, "connector", true),
            readBoolean(object, "placeNearTarget", false),
            readString(object, "pointMode", "top"),
            readOptionalString(object, "snapshot"),
            readOptionalString(object, "texture"),
            readInt(object, "u", 0),
            readInt(object, "v", 0),
            object.has("regionWidth") ? readInt(object, "regionWidth", 0) : -1,
            object.has("regionHeight") ? readInt(object, "regionHeight", 0) : -1,
            object.has("displayWidth") ? readInt(object, "displayWidth", 0) : -1,
            object.has("displayHeight") ? readInt(object, "displayHeight", 0) : -1,
            readInt(object, "textureWidth", 256),
            readInt(object, "textureHeight", 256),
            readInt(object, "offsetX", 0),
            readBoolean(object, "framed", true),
            readBoolean(object, "scaleToParent", readOptionalString(object, "parentGui") != null),
            readBoolean(object, "stretch", false),
            readInt(object, "stretchBorder", 4),
            readInt(object, "guiX", 0),
            readInt(object, "guiY", 0),
            readInt(object, "guiWidth", -1),
            readInt(object, "guiHeight", -1),
            parseIntArray(object.get("pos"), 3),
            parseIntArray(object.get("from"), 3),
            parseIntArray(object.get("to"), 3),
            parseDoubleArray(object.get("offset"), 3),
            parseDoubleArray(resolveRotation(object), 3),
            parseDoubleArray(object.get("pivot"), 3),
            parseDoubleArray(object.get("pointAt"), 3),
            readFloat(object, "rotX", 0.0F),
            readFloat(object, "rotY", 0.0F),
            readFloat(object, "rotZ", 0.0F),
            childOperations
        );
    }

    private static String readNbtPayload(JsonElement element) {
        return ExternalNbtValidation.readNbtPayload(element, "external ponder scene json");
    }
}
