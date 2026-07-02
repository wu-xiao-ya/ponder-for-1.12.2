package net.createmod.ponder.foundation.external.definition;

import java.util.Collections;
import java.util.List;

import com.google.gson.JsonObject;

import net.minecraft.util.ResourceLocation;

/**
 * A reusable GUI interaction definition from external JSON.
 * Steps are stored as raw JsonObject trees; their conversion to
 * SceneOperationDefinition happens during scene parsing.
 */
public record InteractionDefinition(
    SourceInfo source,
    ResourceLocation id,
    JsonObject defaults,
    List<JsonObject> steps
) {
    public InteractionDefinition {
        if (source == null) throw new IllegalArgumentException("source must not be null");
        if (id == null) throw new IllegalArgumentException("id must not be null");
        if (defaults == null) defaults = new JsonObject();
        if (steps == null) steps = Collections.emptyList();
    }
}
