package net.createmod.ponder.foundation;

import java.util.Objects;

import net.createmod.ponder.Ponder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class PonderTag {

    public static final class Highlight {
        public static final ResourceLocation ALL = Ponder.asResource("_all");

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
        this.itemIcon = itemIcon == null ? new ItemStack((Item) null) : itemIcon;
        this.mainItem = mainItem == null ? new ItemStack((Item) null) : mainItem;
    }

    public ResourceLocation getId() {
        return id;
    }

    public ResourceLocation getTextureIconLocation() {
        return textureIconLocation;
    }

    public ItemStack getItemIcon() {
        return itemIcon;
    }

    public ItemStack getMainItem() {
        return mainItem;
    }

    public String getTitle() {
        return id == null ? "" : id.toString();
    }

    public String getDescription() {
        return "";
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
