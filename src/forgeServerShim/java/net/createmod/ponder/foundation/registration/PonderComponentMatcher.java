package net.createmod.ponder.foundation.registration;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class PonderComponentMatcher {

    private final ResourceLocation itemId;

    private final int metadata;


    private PonderComponentMatcher(ResourceLocation itemId, int metadata) {
        this.itemId = itemId;
        this.metadata = metadata;
    }

    public static PonderComponentMatcher simple(ResourceLocation itemId) {
        return new PonderComponentMatcher(itemId, 0);
    }

    public static PonderComponentMatcher exact(ResourceLocation itemId, int metadata, NBTTagCompound nbtPattern,
        NBTTagCompound displayNbt) {
        return new PonderComponentMatcher(itemId, metadata);
    }

    public ResourceLocation getItemId() {
        return itemId;
    }

    public int getSpecificity() {
        return metadata >= 0 ? 1 : 0;
    }

    public boolean matches(ItemStack stack) {
        return false;
    }
}
