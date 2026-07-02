package net.createmod.ponder.foundation.external.parse;

import static net.createmod.ponder.foundation.external.validate.ExternalJsonValidation.parseLocation;

import net.minecraft.util.ResourceLocation;

/**
 * Factory for building component identifiers ({@link ResourceLocation}) from
 * item metadata and NBT fingerprints, used by the external JSON scene parser.
 */
public final class ComponentIdFactory {

    private ComponentIdFactory() {}

    /**
     * Builds a {@link ResourceLocation} that uniquely identifies a schematic
     * component. When an explicit componentKey is given it is used directly;
     * otherwise the identifier is derived from the item id, meta, and NBT
     * content hash.
     */
    public static ResourceLocation buildComponentId(ResourceLocation componentItem, String explicitComponentKey,
        int componentMeta, String componentNbt) {
        if (explicitComponentKey != null && !explicitComponentKey.trim().isEmpty()) {
            return parseLocation(explicitComponentKey, componentItem.getNamespace());
        }

        if (componentMeta < 0 && (componentNbt == null || componentNbt.trim().isEmpty())) {
            return componentItem;
        }

        StringBuilder path = new StringBuilder(componentItem.getPath());
        if (componentMeta >= 0) {
            path.append("__m").append(componentMeta);
        }
        if (componentNbt != null && !componentNbt.trim().isEmpty()) {
            path.append("__n").append(Integer.toHexString(componentNbt.hashCode()));
        }
        return new ResourceLocation(componentItem.getNamespace(), path.toString());
    }
}
