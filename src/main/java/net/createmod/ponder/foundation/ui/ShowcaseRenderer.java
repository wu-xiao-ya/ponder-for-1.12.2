package net.createmod.ponder.foundation.ui;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.createmod.ponder.foundation.ui.render.GLStateGuard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

final class ShowcaseRenderer {

    interface Host {
        void drawRect(int left, int top, int right, int bottom, int color);

        void drawGradientRect(int left, int top, int right, int bottom, int startColor, int endColor);

        void drawString(FontRenderer fontRenderer, String text, int x, int y, int color);

        @Nullable
        ShowcaseGroupModel getShowcaseGroupState();

        ResourceLocation getSelectedComponentId();

        ItemStack createComponentStack(ResourceLocation componentId);
    }

    static final class Theme {
        private final int headerMaxWidth;
        private final int headerHeight;
        private final float headerAlphaScale;
        private final int headerFillColor;
        private final int headerRightGradientColor;
        private final float headerUnderlineAlphaScale;
        private final int headerUnderlineColor;
        private final float headerSlotOuterAlphaScale;
        private final int headerSlotOuterColor;
        private final float headerSlotInnerAlphaScale;
        private final int headerSlotInnerColor;
        private final int headerEyebrowColor;
        private final int headerTitleColor;
        private final int headerSubtitleColor;
        private final int headerSceneIndexColor;
        private final float groupChipFillAlphaScale;
        private final int groupChipFillColor;
        private final float groupChipBorderAlphaScale;
        private final int groupChipBorderColor;
        private final int groupChipTextColor;
        private final HeaderEyebrowFactory eyebrowFactory;
        private final GroupPopupLabelFactory popupLabelFactory;

        Theme(int headerMaxWidth, int headerHeight, float headerAlphaScale, int headerFillColor,
            int headerRightGradientColor, float headerUnderlineAlphaScale, int headerUnderlineColor,
            float headerSlotOuterAlphaScale, int headerSlotOuterColor, float headerSlotInnerAlphaScale,
            int headerSlotInnerColor, int headerEyebrowColor, int headerTitleColor, int headerSubtitleColor,
            int headerSceneIndexColor, float groupChipFillAlphaScale, int groupChipFillColor,
            float groupChipBorderAlphaScale, int groupChipBorderColor, int groupChipTextColor,
            HeaderEyebrowFactory eyebrowFactory, GroupPopupLabelFactory popupLabelFactory) {
            this.headerMaxWidth = headerMaxWidth;
            this.headerHeight = headerHeight;
            this.headerAlphaScale = headerAlphaScale;
            this.headerFillColor = headerFillColor;
            this.headerRightGradientColor = headerRightGradientColor;
            this.headerUnderlineAlphaScale = headerUnderlineAlphaScale;
            this.headerUnderlineColor = headerUnderlineColor;
            this.headerSlotOuterAlphaScale = headerSlotOuterAlphaScale;
            this.headerSlotOuterColor = headerSlotOuterColor;
            this.headerSlotInnerAlphaScale = headerSlotInnerAlphaScale;
            this.headerSlotInnerColor = headerSlotInnerColor;
            this.headerEyebrowColor = headerEyebrowColor;
            this.headerTitleColor = headerTitleColor;
            this.headerSubtitleColor = headerSubtitleColor;
            this.headerSceneIndexColor = headerSceneIndexColor;
            this.groupChipFillAlphaScale = groupChipFillAlphaScale;
            this.groupChipFillColor = groupChipFillColor;
            this.groupChipBorderAlphaScale = groupChipBorderAlphaScale;
            this.groupChipBorderColor = groupChipBorderColor;
            this.groupChipTextColor = groupChipTextColor;
            this.eyebrowFactory = eyebrowFactory;
            this.popupLabelFactory = popupLabelFactory;
        }
    }

    interface HeaderEyebrowFactory {
        String create(ItemStack componentStack, @Nullable ResourceLocation componentId);
    }

    interface GroupPopupLabelFactory {
        String create(ShowcaseGroupModel groupState);
    }

    private final Minecraft minecraft;
    private final FontRenderer fontRenderer;
    private final Host host;
    private final Theme theme;

    ShowcaseRenderer(Minecraft minecraft, FontRenderer fontRenderer, Host host, Theme theme) {
        this.minecraft = minecraft;
        this.fontRenderer = fontRenderer;
        this.host = host;
        this.theme = theme;
    }

    HeaderLayout drawHeader(int x, int y, int width, ItemStack componentStack, @Nullable ResourceLocation componentId, String title,
        String subtitle, float fade, int selectedSceneIndex, int compiledSceneCount) {
        int boxWidth = Math.min(theme.headerMaxWidth, width);
        int boxHeight = theme.headerHeight;
        int alpha = (int) (fade * theme.headerAlphaScale) << 24;
        host.drawGradientRect(x, y, x + boxWidth, y + boxHeight, alpha | theme.headerFillColor, 0x00000000);
        if (theme.headerRightGradientColor != 0) {
            host.drawGradientRect(x + boxWidth - 26, y, x + boxWidth, y + boxHeight, 0x00000000,
                alpha | theme.headerRightGradientColor);
        }
        host.drawRect(x + 34, y + boxHeight - 1, x + boxWidth - 12, y + boxHeight,
            ((int) (fade * theme.headerUnderlineAlphaScale) << 24) | theme.headerUnderlineColor);
        host.drawRect(x - 2, y + 4, x + 28, y + 34,
            ((int) (fade * theme.headerSlotOuterAlphaScale) << 24) | theme.headerSlotOuterColor);
        host.drawRect(x - 1, y + 5, x + 27, y + 33,
            ((int) (fade * theme.headerSlotInnerAlphaScale) << 24) | theme.headerSlotInnerColor);

        if (!componentStack.isEmpty()) {
            try (GLStateGuard itemLighting = GLStateGuard.itemLighting()) {
                minecraft.getRenderItem().renderItemAndEffectIntoGUI(componentStack, x + 5, y + 11);
            }
        }

        ShowcaseGroupModel groupState = host.getShowcaseGroupState();
        if (groupState != null && groupState.componentIds.size() > 1) {
            int chipX = x + 16;
            int chipY = y + 22;
            host.drawRect(chipX, chipY, chipX + 12, chipY + 10,
                ((int) (fade * theme.groupChipBorderAlphaScale) << 24) | theme.groupChipBorderColor);
            host.drawRect(chipX + 1, chipY + 1, chipX + 11, chipY + 9,
                ((int) (fade * theme.groupChipFillAlphaScale) << 24) | theme.groupChipFillColor);
            drawCenteredStringNoShadow(String.valueOf(groupState.componentIds.size()), chipX + 6, chipY + 2,
                theme.groupChipTextColor);
        }

        host.drawString(fontRenderer, theme.eyebrowFactory.create(componentStack, componentId), x + 36, y + 5,
            theme.headerEyebrowColor);
        host.drawString(fontRenderer, fontRenderer.trimStringToWidth(title, boxWidth - 90), x + 36, y + 17,
            theme.headerTitleColor);
        host.drawString(fontRenderer, fontRenderer.trimStringToWidth(subtitle, boxWidth - 90), x + 36, y + 29,
            theme.headerSubtitleColor);
        if (compiledSceneCount > 0) {
            String sceneIndex = (selectedSceneIndex + 1) + " / " + compiledSceneCount;
            host.drawString(fontRenderer, sceneIndex, x + boxWidth - 12 - fontRenderer.getStringWidth(sceneIndex), y + 6,
                theme.headerSceneIndexColor);
        }
        return new HeaderLayout(x - 1, y + 5, 28);
    }

    @Nullable
    GroupPopupResult drawGroupPopup(int x, int y, int maxWidth, int anchorIconX, float fade, boolean selectorOpen,
        @Nullable ResourceLocation selectedComponentId) {
        ShowcaseGroupModel groupState = host.getShowcaseGroupState();
        if (groupState == null || groupState.componentIds.size() <= 1 || !selectorOpen || maxWidth < 120) {
            return null;
        }

        String label = theme.popupLabelFactory.create(groupState);
        int columns = Math.min(groupState.componentIds.size(), Math.max(1,
            Math.min(6, (maxWidth - 20) / (20 + 4))));
        int rows = Math.max(1, (int) Math.ceil(groupState.componentIds.size() / (float) columns));
        int boxWidth = 16 + columns * 20 + Math.max(0, columns - 1) * 4;
        int boxHeight = 28 + rows * 20 + Math.max(0, rows - 1) * 4 + 10;
        int boxX = MathHelper.clamp(anchorIconX - 6, x, x + Math.max(0, maxWidth - boxWidth));
        int boxY = y;
        int alpha = (int) (fade * 164.0F) << 24;
        host.drawGradientRect(boxX, boxY, boxX + boxWidth, boxY + boxHeight, alpha | 0x12181E, alpha | 0x0A0E14);
        host.drawRect(boxX, boxY, boxX + boxWidth, boxY + 1, ((int) (fade * 120.0F) << 24) | 0xE7E0D1);
        host.drawRect(boxX, boxY + boxHeight - 1, boxX + boxWidth, boxY + boxHeight, ((int) (fade * 96.0F) << 24) | 0xD5CCB8);
        host.drawRect(boxX, boxY, boxX + 1, boxY + boxHeight, ((int) (fade * 90.0F) << 24) | 0xE7E0D1);
        host.drawRect(boxX + boxWidth - 1, boxY, boxX + boxWidth, boxY + boxHeight, ((int) (fade * 84.0F) << 24) | 0x2B323A);
        host.drawString(fontRenderer, fontRenderer.trimStringToWidth(label, boxWidth - 16), boxX + 8, boxY + 8, 0xC8D0D8);

        List<ShowcaseGroupIconHitBox> icons = new ArrayList<ShowcaseGroupIconHitBox>();
        int startX = boxX + 8;
        int startY = boxY + 22;
        for (int index = 0; index < groupState.componentIds.size(); index++) {
            ResourceLocation componentId = groupState.componentIds.get(index);
            ItemStack stack = host.createComponentStack(componentId);
            int iconX = startX + (index % columns) * 24;
            int iconY = startY + (index / columns) * 24;
            boolean selected = componentId.equals(selectedComponentId);
            int borderColor = selected ? ((int) (fade * 255.0F) << 24) | 0xE7E0D1 : ((int) (fade * 140.0F) << 24) | 0x596570;
            int fillColor = selected ? ((int) (fade * 188.0F) << 24) | 0x1A2028 : ((int) (fade * 120.0F) << 24) | 0x10151B;
            host.drawRect(iconX, iconY, iconX + 20, iconY + 20, borderColor);
            host.drawRect(iconX + 1, iconY + 1, iconX + 19, iconY + 19, fillColor);

            if (!stack.isEmpty()) {
                try (GLStateGuard itemLighting = GLStateGuard.itemLighting()) {
                    minecraft.getRenderItem().renderItemAndEffectIntoGUI(stack, iconX + 2, iconY + 2);
                }
            }
            icons.add(new ShowcaseGroupIconHitBox(componentId, groupState.tag, iconX, iconY, 20, 20));
        }

        return new GroupPopupResult(icons, boxX, boxY, boxWidth, boxHeight);
    }

    private void drawCenteredStringNoShadow(String text, int centerX, int y, int color) {
        if (text == null || text.isEmpty()) {
            return;
        }
        fontRenderer.drawString(text, centerX - fontRenderer.getStringWidth(text) / 2.0F, y, color, false);
    }

    static final class GroupPopupResult {
        final List<ShowcaseGroupIconHitBox> icons;
        final int boxX;
        final int boxY;
        final int boxWidth;
        final int boxHeight;

        GroupPopupResult(List<ShowcaseGroupIconHitBox> icons, int boxX, int boxY, int boxWidth, int boxHeight) {
            this.icons = icons;
            this.boxX = boxX;
            this.boxY = boxY;
            this.boxWidth = boxWidth;
            this.boxHeight = boxHeight;
        }
    }

    static final class HeaderLayout {
        final int iconX;
        final int iconY;
        final int iconSize;

        HeaderLayout(int iconX, int iconY, int iconSize) {
            this.iconX = iconX;
            this.iconY = iconY;
            this.iconSize = iconSize;
        }
    }
}
