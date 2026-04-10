package net.createmod.ponder.api.element;

import net.minecraft.item.ItemStack;

public interface InputElementBuilder {

    InputElementBuilder withItem(ItemStack stack);

    InputElementBuilder leftClick();

    InputElementBuilder rightClick();

    InputElementBuilder scroll();

    InputElementBuilder showing(Object icon);

    InputElementBuilder whileSneaking();

    InputElementBuilder whileCTRL();
}
