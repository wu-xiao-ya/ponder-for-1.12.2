package net.createmod.ponder.foundation.registration;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;

public class PonderComponentMatcher {

    private final ResourceLocation itemId;
    private final int metadata;
    private final boolean matchMetadata;
    private final NBTTagCompound nbtPattern;
    private final NBTTagCompound displayNbt;
    private final int specificity;

    private PonderComponentMatcher(ResourceLocation itemId, int metadata, boolean matchMetadata, NBTTagCompound nbtPattern,
        NBTTagCompound displayNbt) {
        this.itemId = itemId;
        this.metadata = metadata;
        this.matchMetadata = matchMetadata;
        this.nbtPattern = nbtPattern == null ? null : nbtPattern.copy();
        this.displayNbt = displayNbt == null ? null : displayNbt.copy();
        this.specificity = (matchMetadata ? 100 : 0) + countNbtLeaves(this.nbtPattern) * 10 + (this.nbtPattern != null ? 1000 : 0);
    }

    public static PonderComponentMatcher simple(ResourceLocation itemId) {
        return new PonderComponentMatcher(itemId, 0, false, null, null);
    }

    public static PonderComponentMatcher exact(ResourceLocation itemId, int metadata, NBTTagCompound nbtPattern,
        NBTTagCompound displayNbt) {
        return new PonderComponentMatcher(itemId, metadata, metadata >= 0, nbtPattern, displayNbt);
    }

    public ResourceLocation getItemId() {
        return itemId;
    }

    public int getSpecificity() {
        return specificity;
    }

    public boolean matches(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        ResourceLocation stackId = stack.getItem().getRegistryName();
        if (stackId == null || !itemId.equals(stackId)) {
            return false;
        }

        if (matchMetadata && stack.getMetadata() != metadata) {
            return false;
        }

        return nbtPattern == null || matchesCompound(stack.getTagCompound(), nbtPattern);
    }

    public ItemStack createDisplayStack() {
        Item item = Item.REGISTRY.getObject(itemId);
        if (item == null) {
            Block block = Block.REGISTRY.getObject(itemId);
            if (block != null) {
                item = Item.getItemFromBlock(block);
            }
        }

        if (item == null) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = new ItemStack(item, 1, matchMetadata ? metadata : 0);
        NBTTagCompound tag = displayNbt == null ? nbtPattern : displayNbt;
        if (tag != null) {
            stack.setTagCompound(tag.copy());
        }
        return stack;
    }

    private static boolean matchesCompound(NBTTagCompound actual, NBTTagCompound pattern) {
        if (pattern == null) {
            return true;
        }
        if (actual == null) {
            return false;
        }

        for (String key : pattern.getKeySet()) {
            if (!actual.hasKey(key)) {
                return false;
            }
            if (!matchesTag(actual.getTag(key), pattern.getTag(key))) {
                return false;
            }
        }

        return true;
    }

    private static boolean matchesTag(NBTBase actual, NBTBase pattern) {
        if (pattern == null) {
            return true;
        }
        if (actual == null || actual.getId() != pattern.getId()) {
            return false;
        }

        if (pattern instanceof NBTTagCompound) {
            return matchesCompound((NBTTagCompound) actual, (NBTTagCompound) pattern);
        }

        if (pattern instanceof NBTTagList) {
            NBTTagList actualList = (NBTTagList) actual;
            NBTTagList patternList = (NBTTagList) pattern;
            if (actualList.tagCount() < patternList.tagCount()) {
                return false;
            }
            for (int i = 0; i < patternList.tagCount(); i++) {
                if (!matchesTag(actualList.get(i), patternList.get(i))) {
                    return false;
                }
            }
            return true;
        }

        return actual.equals(pattern);
    }

    private static int countNbtLeaves(NBTBase tag) {
        if (tag == null) {
            return 0;
        }
        if (tag instanceof NBTTagCompound) {
            int total = 0;
            NBTTagCompound compound = (NBTTagCompound) tag;
            for (String key : compound.getKeySet()) {
                total += countNbtLeaves(compound.getTag(key));
            }
            return total;
        }
        if (tag instanceof NBTTagList) {
            int total = 0;
            NBTTagList list = (NBTTagList) tag;
            for (int i = 0; i < list.tagCount(); i++) {
                total += countNbtLeaves(list.get(i));
            }
            return total;
        }
        return 1;
    }
}
