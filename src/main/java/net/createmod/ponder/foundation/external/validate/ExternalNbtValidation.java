package net.createmod.ponder.foundation.external.validate;

import com.google.gson.JsonElement;
import net.createmod.ponder.Ponder;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;

public final class ExternalNbtValidation {

    private ExternalNbtValidation() {
    }

    public static String readNbtPayload(JsonElement element, String warningContext) {
        if (element == null || element.isJsonNull()) {
            return null;
        }

        String nbtRaw;
        if (element.isJsonObject() || element.isJsonArray()) {
            nbtRaw = element.toString();
        } else if (element.isJsonPrimitive()) {
            nbtRaw = element.getAsString();
        } else {
            return null;
        }

        if (nbtRaw.trim().isEmpty()) {
            return null;
        }

        try {
            JsonToNBT.getTagFromJson(nbtRaw);
            return nbtRaw;
        } catch (NBTException exception) {
            Ponder.LOGGER.warn("Invalid NBT payload in {}: {}", warningContext, nbtRaw, exception);
            return null;
        }
    }

    public static NBTTagCompound parseTag(String rawNbt, String errorMessage) {
        if (rawNbt == null || rawNbt.trim().isEmpty()) {
            return null;
        }

        try {
            return JsonToNBT.getTagFromJson(rawNbt);
        } catch (NBTException exception) {
            throw new IllegalArgumentException(errorMessage + ": " + rawNbt, exception);
        }
    }

    public static int countLeaves(NBTBase tag) {
        if (tag == null) {
            return 0;
        }
        if (tag instanceof NBTTagCompound) {
            int total = 0;
            NBTTagCompound compound = (NBTTagCompound) tag;
            for (String key : compound.getKeySet()) {
                total += countLeaves(compound.getTag(key));
            }
            return total;
        }
        if (tag instanceof net.minecraft.nbt.NBTTagList) {
            int total = 0;
            net.minecraft.nbt.NBTTagList list = (net.minecraft.nbt.NBTTagList) tag;
            for (int i = 0; i < list.tagCount(); i++) {
                total += countLeaves(list.get(i));
            }
            return total;
        }
        return 1;
    }

    public static int countDepth(NBTBase tag) {
        if (tag == null) {
            return 0;
        }
        if (tag instanceof NBTTagCompound) {
            int maxDepth = 0;
            NBTTagCompound compound = (NBTTagCompound) tag;
            for (String key : compound.getKeySet()) {
                maxDepth = Math.max(maxDepth, countDepth(compound.getTag(key)));
            }
            return maxDepth + 1;
        }
        if (tag instanceof net.minecraft.nbt.NBTTagList) {
            int maxDepth = 0;
            net.minecraft.nbt.NBTTagList list = (net.minecraft.nbt.NBTTagList) tag;
            for (int i = 0; i < list.tagCount(); i++) {
                maxDepth = Math.max(maxDepth, countDepth(list.get(i)));
            }
            return maxDepth + 1;
        }
        return 1;
    }

    public static boolean isWithinLimits(NBTBase tag, int maxDepth, int maxLeaves) {
        if (tag == null) {
            return true;
        }
        return countDepth(tag) <= maxDepth && countLeaves(tag) <= maxLeaves;
    }

    public static void validateLimits(NBTBase tag, int maxDepth, int maxLeaves, String label) {
        if (!isWithinLimits(tag, maxDepth, maxLeaves)) {
            throw new IllegalArgumentException(label + " exceeds depth or volume limits");
        }
    }
}
