package net.createmod.ponder.foundation.ui;

import javax.annotation.Nullable;

import net.createmod.ponder.foundation.PonderScene;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

final class ShowcaseHoverLabelComputer {

    interface Host {
        boolean isMouseOverPlaybackBar(int mouseX, int mouseY);

        boolean isMouseOverShowcaseHeaderIcon(int mouseX, int mouseY);

        boolean hasShowcaseGroupChoices();

        @Nullable
        ShowcaseGroupIconHitBox getShowcaseGroupIconAt(int mouseX, int mouseY);

        boolean isMouseOverNextUpCard(int mouseX, int mouseY);

        int estimatePlaybackTickForMouse(int mouseX);

        @Nullable
        PonderScene getSelectedScene();

        ItemStack createComponentStack(ResourceLocation componentId);

        String getPlaybackBarHoverLabel(int estimatedTick, @Nullable PonderScene scene);

        String getGroupSelectorHoverLabel();

        String getNextUpHoverLabel();
    }

    private ShowcaseHoverLabelComputer() {
    }

    @Nullable
    static String compute(int mouseX, int mouseY, Host host) {
        if (host.isMouseOverPlaybackBar(mouseX, mouseY)) {
            PonderScene scene = host.getSelectedScene();
            return host.getPlaybackBarHoverLabel(host.estimatePlaybackTickForMouse(mouseX), scene);
        }
        if (host.isMouseOverShowcaseHeaderIcon(mouseX, mouseY) && host.hasShowcaseGroupChoices()) {
            return host.getGroupSelectorHoverLabel();
        }
        ShowcaseGroupIconHitBox groupIcon = host.getShowcaseGroupIconAt(mouseX, mouseY);
        if (groupIcon != null) {
            ItemStack stack = host.createComponentStack(groupIcon.componentId);
            String label = stack.isEmpty() ? groupIcon.componentId.toString() : stack.getDisplayName();
            return groupIcon.tag == null ? label : groupIcon.tag.getTitle() + "  |  " + label;
        }
        if (host.isMouseOverNextUpCard(mouseX, mouseY)) {
            return host.getNextUpHoverLabel();
        }
        return null;
    }
}
