package net.createmod.ponder.foundation.ui;

import net.createmod.ponder.foundation.PonderScene;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

final class ShowcaseHudHoverLabelHostAdapter implements ShowcaseHudRenderer.HoverLabelHost {

    private final PonderDebugScreenHostSupport support;

    ShowcaseHudHoverLabelHostAdapter(PonderDebugScreenHostSupport support) {
        this.support = support;
    }

    @Override
    public boolean isMouseOverPlaybackBar(int mouseX, int mouseY) {
        return support.isMouseOverPlaybackBar(mouseX, mouseY);
    }

    @Override
    public boolean isMouseOverShowcaseHeaderIcon(int mouseX, int mouseY) {
        return support.isMouseOverShowcaseHeaderIcon(mouseX, mouseY);
    }

    @Override
    public boolean hasShowcaseGroupChoices() {
        return support.hasShowcaseGroupChoices();
    }

    @Override
    public ShowcaseGroupIconHitBox getShowcaseGroupIconAt(int mouseX, int mouseY) {
        return support.getShowcaseGroupIconAt(mouseX, mouseY);
    }

    @Override
    public boolean isMouseOverNextUpCard(int mouseX, int mouseY) {
        return support.isMouseOverNextUpCard(mouseX, mouseY);
    }

    @Override
    public int estimatePlaybackTickForMouse(int mouseX) {
        return support.estimatePlaybackTickForMouse(mouseX);
    }

    @Override
    public PonderScene getSelectedScene() {
        return support.getSelectedScene();
    }

    @Override
    public ItemStack createComponentStack(ResourceLocation componentId) {
        return support.createComponentStack(componentId);
    }
}
