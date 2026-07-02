package net.createmod.ponder.foundation.external.parse;

import static net.createmod.ponder.foundation.external.validate.ExternalJsonValidation.getElement;
import static net.createmod.ponder.foundation.external.validate.ExternalJsonValidation.hasField;
import static net.createmod.ponder.foundation.external.validate.ExternalJsonValidation.parseLocation;
import static net.createmod.ponder.foundation.external.validate.ExternalJsonValidation.readInt;
import static net.createmod.ponder.foundation.external.validate.ExternalJsonValidation.readOptionalString;
import static net.createmod.ponder.foundation.external.validate.ExternalJsonValidation.readRequiredString;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.createmod.ponder.foundation.external.definition.ComponentDefinition;
import net.createmod.ponder.foundation.external.validate.ExternalNbtValidation;
import net.createmod.ponder.foundation.registration.PonderComponentMatcher;
import net.minecraft.util.ResourceLocation;

public final class ComponentDefinitionFactory {

    private ComponentDefinitionFactory() {
    }

    public static ComponentDefinition parseComponentObject(JsonObject object, JsonObject defaults) {
        ResourceLocation componentItem = parseLocation(readRequiredString(object, defaults, "component"), "minecraft");
        int componentMeta = hasField(object, "componentMeta") ? readInt(object, "componentMeta", 0)
            : hasField(defaults, "componentMeta") ? readInt(defaults, "componentMeta", 0) : -1;
        String componentNbt = readNbtPayload(getElement(object, defaults, "componentNbt"));
        String componentDisplayNbt = readNbtPayload(getElement(object, defaults, "componentDisplayNbt"));
        return new ComponentDefinition(
            ComponentIdFactory.buildComponentId(componentItem, readOptionalString(object, defaults, "componentKey"),
                componentMeta, componentNbt),
            componentItem,
            componentMeta,
            componentNbt,
            componentDisplayNbt,
            PonderComponentMatcher.exact(componentItem, componentMeta, parseMatcherNbt(componentNbt),
                parseMatcherNbt(componentDisplayNbt))
        );
    }

    private static String readNbtPayload(JsonElement element) {
        return ExternalNbtValidation.readNbtPayload(element, "external ponder scene json");
    }

    private static net.minecraft.nbt.NBTTagCompound parseMatcherNbt(String rawNbt) {
        return ExternalNbtValidation.parseTag(rawNbt, "Invalid componentNbt payload");
    }
}
