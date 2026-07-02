package net.createmod.ponder.foundation.external.definition;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.minecraft.util.ResourceLocation;

/**
 * Top-level container for all definitions parsed from a single external JSON file.
 */
public record ExternalDefinitionSet(
    List<TagDefinition> tags,
    List<SceneDefinition> scenes,
    Map<ResourceLocation, InteractionDefinition> interactions,
    List<SharedTextDefinition> sharedTexts
) {
    public static final ExternalDefinitionSet EMPTY = new ExternalDefinitionSet(
        Collections.emptyList(), Collections.emptyList(), Collections.emptyMap(), Collections.emptyList()
    );

    public ExternalDefinitionSet {
        if (tags == null) tags = Collections.emptyList();
        if (scenes == null) scenes = Collections.emptyList();
        if (interactions == null) interactions = Collections.emptyMap();
        if (sharedTexts == null) sharedTexts = Collections.emptyList();
    }
}
