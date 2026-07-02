package net.createmod.ponder.foundation.external.definition;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

/**
 * A ponder tag definition from external JSON.
 */
public record TagDefinition(
    SourceInfo source,
    ResourceLocation id,
    String title,
    String description,
    String icon,
    ItemStack itemStack,
    boolean useItemAsIcon,
    boolean useItemAsMainItem,
    boolean addToIndex
) {
    public TagDefinition {
        if (source == null) throw new IllegalArgumentException("source must not be null");
        if (id == null) throw new IllegalArgumentException("id must not be null");
        if (title == null) title = id.toString();
        if (description == null) description = "";
        if (itemStack == null) itemStack = ItemStack.EMPTY;
    }
}
