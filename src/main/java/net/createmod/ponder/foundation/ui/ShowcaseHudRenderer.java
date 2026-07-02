package net.createmod.ponder.foundation.ui;

import javax.annotation.Nullable;

import net.createmod.ponder.foundation.PonderScene;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.math.MathHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

final class ShowcaseHudRenderer {

    interface Canvas {
        void drawRect(int left, int top, int right, int bottom, int color);

        void drawGradientRect(int left, int top, int right, int bottom, int startColor, int endColor);

        void drawString(FontRenderer fontRenderer, String text, int x, int y, int color);

        void drawCenteredString(FontRenderer fontRenderer, String text, int x, int y, int color);
    }

    interface Host extends Canvas {
    }

    interface Theme {
        String getPlaybackBarHoverLabel(int estimatedTick, @Nullable PonderScene scene);

        String getGroupSelectorHoverLabel();

        String getNextUpHoverLabel();

        String getSceneShortLabel(int sceneIndex);

        String getNextUpLabel();
    }

    interface HoverLabelHost {
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
    }

    private final Canvas canvas;
    private final Host host;
    private final FontRenderer fontRenderer;
    private final Theme theme;

    ShowcaseHudRenderer(Host host, FontRenderer fontRenderer, Theme theme) {
        this.canvas = host;
        this.host = host;
        this.fontRenderer = fontRenderer;
        this.theme = theme;
    }

    void drawPlaybackBar(PonderScene scene, int selectedSceneIndex, int compiledSceneCount, int playbackTick, boolean playing,
        int sceneEndTick, boolean showcaseMode, int x, int y, int width, int height) {
        int maxTick = scene == null ? 0 : Math.max(1, sceneEndTick);
        float progress = scene == null ? 0.0F : playbackTick / (float) maxTick;
        int filled = MathHelper.clamp((int) (progress * width), 0, width);

        if (showcaseMode) {
            canvas.drawRect(x, y, x + width, y + height, 0x44101518);
            canvas.drawRect(x, y, x + filled, y + height, 0xFFD9D1BF);
            if (filled > 0) {
                canvas.drawRect(x + filled - 1, y - 1, x + filled, y + height + 1, 0xFFFDFBF4);
            }

            if (scene != null) {
                String leftLabel = theme.getSceneShortLabel(selectedSceneIndex + 1) + " / " + compiledSceneCount;
                String rightLabel = playbackTick + " / " + sceneEndTick;
                canvas.drawString(fontRenderer, leftLabel, x, y - 11, 0xC4CFD7);
                canvas.drawString(fontRenderer, rightLabel, x + width - fontRenderer.getStringWidth(rightLabel), y - 11, 0xC4CFD7);
            }
            return;
        }

        canvas.drawRect(x, y, x + width, y + height, 0x44202020);
        canvas.drawRect(x, y, x + filled, y + height, 0xCC6AB8FF);
        if (filled > 0) {
            canvas.drawRect(x + filled - 1, y, x + filled, y + height, 0xFFEFF8FF);
        }

        String label = scene == null ? "No scene" : "T+" + playbackTick + " / " + sceneEndTick + (playing ? "   looping" : "   paused");
        canvas.drawCenteredString(fontRenderer, label, x + width / 2, y - 11, 0xD8E4E8);
    }

    @Nullable
    NextUpCardBounds drawNextUpCard(@Nullable PonderScene scene, @Nullable PonderScene nextScene, int playbackTick, int sceneEndTick,
        @Nullable GuiButton nextSceneButton, float fade, boolean themed) {
        if (scene == null || nextScene == null || nextSceneButton == null) {
            return null;
        }

        int maxTick = Math.max(1, sceneEndTick);
        if (playbackTick < (int) (maxTick * 0.7F)) {
            return null;
        }

        String nextUpLabel = theme.getNextUpLabel();
        String nextTitle = fontRenderer.trimStringToWidth(nextScene.getTitle(), themed ? 170 : 150);
        int boxWidth = themed
            ? Math.max(108, Math.max(fontRenderer.getStringWidth(nextUpLabel), fontRenderer.getStringWidth(nextTitle)) + 26)
            : Math.max(96, Math.max(fontRenderer.getStringWidth(nextUpLabel), fontRenderer.getStringWidth(nextTitle)) + 22);
        int anchorX = nextSceneButton.x + nextSceneButton.width / 2;
        int anchorY = nextSceneButton.y - 8;
        int boxX = anchorX - boxWidth / 2;
        int boxY = themed ? anchorY - 36 : anchorY - 34;
        int boxHeight = themed ? 28 : 26;

        if (themed) {
            int fillTop = ((int) (fade * 188.0F) << 24) | 0x111822;
            int fillBottom = ((int) (fade * 188.0F) << 24) | 0x090D12;
            int border = ((int) (fade * 148.0F) << 24) | 0xD7DFE7;
            canvas.drawGradientRect(boxX, boxY, boxX + boxWidth, boxY + boxHeight, fillTop, fillBottom);
            canvas.drawRect(boxX, boxY, boxX + boxWidth, boxY + 1, border);
            canvas.drawRect(boxX, boxY + boxHeight - 1, boxX + boxWidth, boxY + boxHeight, border);
            canvas.drawRect(boxX, boxY, boxX + 1, boxY + boxHeight, border);
            canvas.drawRect(boxX + boxWidth - 1, boxY, boxX + boxWidth, boxY + boxHeight, border);
            canvas.drawCenteredString(fontRenderer, nextUpLabel, boxX + boxWidth / 2, boxY + 6, 0xAEB8C1);
            canvas.drawCenteredString(fontRenderer, nextTitle, boxX + boxWidth / 2, boxY + 17, 0xF3EFE7);
            return new NextUpCardBounds(boxX, boxY, boxWidth, boxHeight);
        }

        canvas.drawRect(boxX, boxY, boxX + boxWidth, boxY + boxHeight, ((int) (fade * 188.0F) << 24) | 0x111822);
        canvas.drawCenteredString(fontRenderer, nextUpLabel, boxX + boxWidth / 2, boxY + 6, 0xAEB8C1);
        canvas.drawCenteredString(fontRenderer, nextTitle, boxX + boxWidth / 2, boxY + 16, 0xF3EFE7);
       return new NextUpCardBounds(boxX, boxY, boxWidth, boxHeight);
   }

    @Nullable
    NextUpCardBounds drawNextUpCardIfNeeded(@Nullable PonderScene scene, int selectedSceneIndex, int compiledSceneCount,
        @Nullable java.util.List<PonderScene> compiledScenes, int playbackTick, int sceneEndTick,
        @Nullable GuiButton nextSceneButton, float fade, boolean themed) {
        if (scene == null || compiledScenes == null || selectedSceneIndex + 1 >= compiledSceneCount) {
            return null;
        }

        PonderScene nextScene = compiledScenes.get(selectedSceneIndex + 1);
        return drawNextUpCard(scene, nextScene, playbackTick, sceneEndTick, nextSceneButton, fade, themed);
    }

    boolean drawHoverHints(int screenWidth, int textY, @Nullable String interactionHoverLabel,
        @Nullable GuiButton[] buttons) {
        if (interactionHoverLabel != null && !interactionHoverLabel.isEmpty()) {
            canvas.drawCenteredString(fontRenderer, interactionHoverLabel, screenWidth / 2, textY, 0xB8C3CC);
            return true;
        }

        if (buttons == null) {
            return false;
        }
        for (GuiButton button : buttons) {
            if (button != null && button.visible && button.isMouseOver() && button.displayString != null
                && !button.displayString.isEmpty()) {
                canvas.drawCenteredString(fontRenderer, button.displayString, screenWidth / 2, textY, 0xB8C3CC);
                return true;
            }
        }
        return false;
    }

    @Nullable
    static String computeHoverLabel(int mouseX, int mouseY, HoverLabelHost host, Theme theme) {
        if (host.isMouseOverPlaybackBar(mouseX, mouseY)) {
            PonderScene scene = host.getSelectedScene();
            return theme.getPlaybackBarHoverLabel(host.estimatePlaybackTickForMouse(mouseX), scene);
        }
        if (host.isMouseOverShowcaseHeaderIcon(mouseX, mouseY) && host.hasShowcaseGroupChoices()) {
            return theme.getGroupSelectorHoverLabel();
        }
        ShowcaseGroupIconHitBox groupIcon = host.getShowcaseGroupIconAt(mouseX, mouseY);
        if (groupIcon != null) {
            ItemStack stack = host.createComponentStack(groupIcon.componentId);
            String label = stack.isEmpty() ? groupIcon.componentId.toString() : stack.getDisplayName();
            return groupIcon.tag == null ? label : groupIcon.tag.getTitle() + "  |  " + label;
        }
        if (host.isMouseOverNextUpCard(mouseX, mouseY)) {
            return theme.getNextUpHoverLabel();
        }
        return null;
    }

    static final class NextUpCardBounds {
        final int x;
        final int y;
        final int width;
        final int height;

        NextUpCardBounds(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }
}
