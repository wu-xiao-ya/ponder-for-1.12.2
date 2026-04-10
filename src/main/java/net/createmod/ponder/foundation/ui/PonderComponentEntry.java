package net.createmod.ponder.foundation.ui;

import java.util.Set;

import net.createmod.ponder.foundation.PonderTag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

final class PonderComponentEntry implements Comparable<PonderComponentEntry> {

    final ResourceLocation componentId;
    final ItemStack displayStack;
    final String label;
    final int sceneCount;
    final Set<PonderTag> tags;

    PonderComponentEntry(ResourceLocation componentId, ItemStack displayStack, String label, int sceneCount,
        Set<PonderTag> tags) {
        this.componentId = componentId;
        this.displayStack = displayStack == null ? ItemStack.EMPTY : displayStack.copy();
        this.label = label;
        this.sceneCount = sceneCount;
        this.tags = tags;
    }

    @Override
    public int compareTo(PonderComponentEntry other) {
        if (other == null) {
            return -1;
        }
        int labelCompare = label.compareToIgnoreCase(other.label);
        if (labelCompare != 0) {
            return labelCompare;
        }
        return componentId.toString().compareTo(other.componentId.toString());
    }
}
