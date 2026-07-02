package net.createmod.ponder.foundation.external.parse;

import static net.createmod.ponder.foundation.external.validate.ExternalJsonValidation.parseLocation;

import net.createmod.ponder.Ponder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;

/**
 * Parser for creating {@link ItemStack} instances from external JSON definition
 * strings (item id + optional meta + optional NBT).
 */
public final class ItemStackParser {

    private ItemStackParser() {}

    /**
     * Resolves an item id string into an {@link ItemStack}, applying optional
     * metadata and NBT. Returns {@link ItemStack#EMPTY} when the id is
     * absent or the item is unknown.
     */
    public static ItemStack parseItemStack(String itemId, int meta, String rawNbt) {
        if (itemId == null || itemId.trim().isEmpty()) {
            return ItemStack.EMPTY;
        }

        Item item = Item.REGISTRY.getObject(parseLocation(itemId, "minecraft"));
        if (item == null) {
            Ponder.LOGGER.warn("Unknown item id '{}' in external ponder tag definition", itemId);
            return ItemStack.EMPTY;
        }

        ItemStack stack = new ItemStack(item, 1, Math.max(0, meta));
        if (rawNbt != null && !rawNbt.trim().isEmpty()) {
            try {
                stack.setTagCompound(JsonToNBT.getTagFromJson(rawNbt));
            } catch (NBTException exception) {
                Ponder.LOGGER.warn("Invalid item NBT payload in external ponder tag definition: {}", rawNbt,
                    exception);
            }
        }
        return stack;
    }
}
