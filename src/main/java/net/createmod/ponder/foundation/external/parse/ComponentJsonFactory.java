package net.createmod.ponder.foundation.external.parse;

import com.google.gson.JsonObject;

/**
 * Small factory for building JSON object shells during component parsing.
 */
public final class ComponentJsonFactory {

    private ComponentJsonFactory() {}

    /**
     * Wraps a bare component string into a { "component": "..." } object
     * so it can be processed by {@link ExternalPonderSceneParser#parseComponentObject}.
     */
    public static JsonObject buildPrimitiveComponentObject(String component) {
        JsonObject object = new JsonObject();
        object.addProperty("component", component);
        return object;
    }
}
