package net.createmod.ponder.foundation.ui;

import net.createmod.ponder.foundation.PonderTag;
import net.minecraft.util.ResourceLocation;

final class ShowcaseGroupIconHitBox {
    final ResourceLocation componentId;
    final PonderTag tag;
    final int x;
    final int y;
    final int width;
    final int height;

    ShowcaseGroupIconHitBox(ResourceLocation componentId, PonderTag tag, int x, int y, int width, int height) {
        this.componentId = componentId;
        this.tag = tag;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
}
