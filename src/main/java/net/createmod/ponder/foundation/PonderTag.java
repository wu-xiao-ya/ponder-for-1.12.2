package net.createmod.ponder.foundation;

import java.util.Objects;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class PonderTag {

    public static final class Highlight {
        public static final ResourceLocation ALL = new ResourceLocation("ponder", "_all");

        private Highlight() {
        }
    }

    private final ResourceLocation id;
    private final ResourceLocation textureIconLocation;
    private final ItemStack itemIcon;
    private final ItemStack mainItem;

    public PonderTag(ResourceLocation id, ResourceLocation textureIconLocation, ItemStack itemIcon, ItemStack mainItem) {
        this.id = id;
        this.textureIconLocation = textureIconLocation;
        this.itemIcon = itemIcon == null ? ItemStack.EMPTY : itemIcon.copy();
        this.mainItem = mainItem == null ? ItemStack.EMPTY : mainItem.copy();
    }

    public ResourceLocation getId() {
        return id;
    }

    public ResourceLocation getTextureIconLocation() {
        return textureIconLocation;
    }

    public ItemStack getItemIcon() {
        return itemIcon.copy();
    }

    public ItemStack getMainItem() {
        return mainItem.copy();
    }

    public String getTitle() {
        return PonderIndex.getLangAccess().getTagName(id);
    }

    public String getDescription() {
        return PonderIndex.getLangAccess().getTagDescription(id);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof PonderTag)) {
            return false;
        }
        PonderTag that = (PonderTag) other;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
