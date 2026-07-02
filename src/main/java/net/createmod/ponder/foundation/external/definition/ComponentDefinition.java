package net.createmod.ponder.foundation.external.definition;

import net.createmod.ponder.foundation.registration.PonderComponentMatcher;
import net.minecraft.util.ResourceLocation;

/**
 * Binds a schematic component (item/block) to a scene.
 * Stores both the raw lookup data and the resolved matcher for runtime use.
 */
public record ComponentDefinition(
    ResourceLocation componentId,
    ResourceLocation componentItem,
    int componentMeta,
    String componentNbt,
    String componentDisplayNbt,
    PonderComponentMatcher componentMatcher
) {
    public ComponentDefinition {
        if (componentId == null) throw new IllegalArgumentException("componentId must not be null");
        if (componentItem == null) throw new IllegalArgumentException("componentItem must not be null");
    }
}
