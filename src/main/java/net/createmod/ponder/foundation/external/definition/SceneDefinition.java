package net.createmod.ponder.foundation.external.definition;

import java.util.Collections;
import java.util.List;

import net.minecraft.util.ResourceLocation;

/**
 * A complete external ponder scene definition.
 */
public record SceneDefinition(
    SourceInfo source,
    List<ComponentDefinition> components,
    String schematic,
    String sceneId,
    String title,
    List<ResourceLocation> tags,
    List<ResourceLocation> componentTags,
    List<String> orderBefore,
    List<String> orderAfter,
    List<SceneOperationDefinition> operations
) {
    public SceneDefinition {
        if (source == null) throw new IllegalArgumentException("source must not be null");
        if (components == null) components = Collections.emptyList();
        if (tags == null) tags = Collections.emptyList();
        if (componentTags == null) componentTags = Collections.emptyList();
        if (orderBefore == null) orderBefore = Collections.emptyList();
        if (orderAfter == null) orderAfter = Collections.emptyList();
        if (operations == null) operations = Collections.emptyList();
        if (schematic == null || schematic.isBlank()) throw new IllegalArgumentException("schematic must not be blank");
        if (sceneId == null || sceneId.isBlank()) throw new IllegalArgumentException("sceneId must not be blank");
        if (title == null) title = sceneId;
    }
}
