package net.createmod.ponder.api.registration;

import java.util.function.Predicate;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

public interface IndexExclusionHelper {

    IndexExclusionHelper exclude(ResourceLocation componentId);

    IndexExclusionHelper exclude(Item item);

    IndexExclusionHelper exclude(Block block);

    IndexExclusionHelper exclude(Predicate<ResourceLocation> predicate);
}
