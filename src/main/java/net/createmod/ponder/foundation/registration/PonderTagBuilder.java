package net.createmod.ponder.foundation.registration;

import net.createmod.ponder.api.registration.TagBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class PonderTagBuilder implements TagBuilder {

    private final ResourceLocation id;
    private final DefaultPonderTagRegistrationHelper owner;

    private String title = "NO_TITLE";
    private String description = "NO_DESCRIPTION";
    private boolean addToIndex;
    private ResourceLocation textureIconLocation;
    private ItemStack itemIcon = ItemStack.EMPTY;
    private ItemStack mainItem = ItemStack.EMPTY;

    public PonderTagBuilder(ResourceLocation id, DefaultPonderTagRegistrationHelper owner) {
        this.id = id;
        this.owner = owner;
    }

    public ResourceLocation getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public boolean shouldAddToIndex() {
        return addToIndex;
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

    @Override
    public TagBuilder title(String title) {
        this.title = title;
        return this;
    }

    @Override
    public TagBuilder description(String description) {
        this.description = description;
        return this;
    }

    @Override
    public TagBuilder addToIndex() {
        this.addToIndex = true;
        return this;
    }

    @Override
    public TagBuilder icon(ResourceLocation location) {
        this.textureIconLocation =
            new ResourceLocation(location.getNamespace(), "textures/ponder/tag/" + location.getPath() + ".png");
        return this;
    }

    @Override
    public TagBuilder icon(String path) {
        this.textureIconLocation = new ResourceLocation(id.getNamespace(), "textures/ponder/tag/" + path + ".png");
        return this;
    }

    @Override
    public TagBuilder idAsIcon() {
        return icon(id);
    }

    @Override
    public TagBuilder item(ItemStack stack, boolean useAsIcon, boolean useAsMainItem) {
        ItemStack safeStack = stack == null ? ItemStack.EMPTY : stack.copy();
        if (useAsIcon) {
            this.itemIcon = safeStack;
        }
        if (useAsMainItem) {
            this.mainItem = safeStack.copy();
        }
        return this;
    }

    @Override
    public void register() {
        owner.finishTagRegister(this);
    }
}
