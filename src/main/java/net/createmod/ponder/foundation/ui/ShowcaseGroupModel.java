package net.createmod.ponder.foundation.ui;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import net.createmod.ponder.foundation.PonderTag;
import net.minecraft.util.ResourceLocation;

final class ShowcaseGroupModel {
    final PonderTag tag;
    final List<ResourceLocation> componentIds;

    ShowcaseGroupModel(PonderTag tag, List<ResourceLocation> componentIds) {
        this.tag = tag;
        this.componentIds = componentIds;
    }

    PonderTag getTag() {
        return tag;
    }

    List<ResourceLocation> getComponentIds() {
        return Collections.unmodifiableList(componentIds);
    }

    int size() {
        return componentIds.size();
    }

    boolean isEmpty() {
        return componentIds.isEmpty();
    }

    @Nullable
    ResourceLocation get(int index) {
        return index >= 0 && index < componentIds.size() ? componentIds.get(index) : null;
    }

    boolean contains(ResourceLocation componentId) {
        return componentId != null && componentIds.contains(componentId);
    }

    int indexOf(ResourceLocation componentId) {
        return componentId == null ? -1 : componentIds.indexOf(componentId);
    }
}
