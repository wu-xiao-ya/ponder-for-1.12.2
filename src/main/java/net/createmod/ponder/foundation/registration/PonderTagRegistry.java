package net.createmod.ponder.foundation.registration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.createmod.ponder.Ponder;
import net.createmod.ponder.api.registration.TagRegistryAccess;
import net.createmod.ponder.foundation.PonderTag;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class PonderTagRegistry implements TagRegistryAccess {

    private final Map<ResourceLocation, Set<ResourceLocation>> componentTagMap =
        new LinkedHashMap<ResourceLocation, Set<ResourceLocation>>();
    private final Map<ResourceLocation, PonderTag> registeredTags = new LinkedHashMap<ResourceLocation, PonderTag>();
    private final List<PonderTag> listedTags = new ArrayList<PonderTag>();
    private final PonderTag missing = new PonderTag(Ponder.asResource("not_registered"), null,
        new ItemStack(Blocks.BARRIER), new ItemStack(Blocks.BARRIER));

    private boolean allowRegistration = true;

    public PonderTagRegistry(PonderLocalization localization) {
    }

    public void clearRegistry() {
        componentTagMap.clear();
        registeredTags.clear();
        listedTags.clear();
        allowRegistration = true;
    }

    public void registerTag(PonderTag tag) {
        if (!allowRegistration) {
            throw new IllegalStateException("Registration phase has already ended");
        }
        registeredTags.put(tag.getId(), tag);
    }

    public void listTag(PonderTag tag) {
        if (!allowRegistration) {
            throw new IllegalStateException("Registration phase has already ended");
        }
        for (int i = 0; i < listedTags.size(); i++) {
            if (listedTags.get(i).getId().equals(tag.getId())) {
                listedTags.set(i, tag);
                return;
            }
        }
        listedTags.add(tag);
    }

    public void addTagToComponent(ResourceLocation tag, ResourceLocation component) {
        if (!allowRegistration) {
            throw new IllegalStateException("Registration phase has already ended");
        }
        Set<ResourceLocation> tags = componentTagMap.get(component);
        if (tags == null) {
            tags = new LinkedHashSet<ResourceLocation>();
            componentTagMap.put(component, tags);
        }
        tags.add(tag);
    }

    public int getListedTagCount() {
        return listedTags.size();
    }

    @Override
    public PonderTag getRegisteredTag(ResourceLocation tagLocation) {
        PonderTag tag = registeredTags.get(tagLocation);
        return tag != null ? tag : missing;
    }

    @Override
    public List<PonderTag> getListedTags() {
        return Collections.unmodifiableList(listedTags);
    }

    @Override
    public Set<PonderTag> getTags(ResourceLocation componentId) {
        Set<ResourceLocation> tagIds = componentTagMap.get(componentId);
        if (tagIds == null || tagIds.isEmpty()) {
            return Collections.emptySet();
        }

        Set<PonderTag> tags = new LinkedHashSet<PonderTag>();
        for (ResourceLocation tagId : tagIds) {
            tags.add(getRegisteredTag(tagId));
        }
        return Collections.unmodifiableSet(tags);
    }

    @Override
    public Set<ResourceLocation> getItems(ResourceLocation tag) {
        Set<ResourceLocation> components = new LinkedHashSet<ResourceLocation>();
        for (Map.Entry<ResourceLocation, Set<ResourceLocation>> entry : componentTagMap.entrySet()) {
            if (entry.getValue().contains(tag)) {
                components.add(entry.getKey());
            }
        }
        return Collections.unmodifiableSet(components);
    }

    @Override
    public Set<ResourceLocation> getItems(PonderTag tag) {
        return getItems(tag.getId());
    }
}
