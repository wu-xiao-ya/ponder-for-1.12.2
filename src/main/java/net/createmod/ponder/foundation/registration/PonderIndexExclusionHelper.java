package net.createmod.ponder.foundation.registration;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import net.createmod.ponder.api.registration.IndexExclusionHelper;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

public class PonderIndexExclusionHelper implements IndexExclusionHelper {

    private final List<Predicate<ResourceLocation>> exclusions = new ArrayList<Predicate<ResourceLocation>>();

    public List<Predicate<ResourceLocation>> getExclusions() {
        return exclusions;
    }

    @Override
    public IndexExclusionHelper exclude(final ResourceLocation componentId) {
        exclusions.add(new Predicate<ResourceLocation>() {
            @Override
            public boolean test(ResourceLocation location) {
                return componentId.equals(location);
            }
        });
        return this;
    }

    @Override
    public IndexExclusionHelper exclude(Item item) {
        return exclude(item.getRegistryName());
    }

    @Override
    public IndexExclusionHelper exclude(Block block) {
        return exclude(block.getRegistryName());
    }

    @Override
    public IndexExclusionHelper exclude(Predicate<ResourceLocation> predicate) {
        exclusions.add(predicate);
        return this;
    }
}
