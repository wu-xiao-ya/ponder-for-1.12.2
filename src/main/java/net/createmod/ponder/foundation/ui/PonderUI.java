package net.createmod.ponder.foundation.ui;

import java.util.List;

import javax.annotation.Nullable;

import net.createmod.ponder.Ponder;
import net.createmod.ponder.foundation.PonderScene;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class PonderUI extends PonderDebugScreen {

    private static final String UI_LANG_PREFIX = "ponder.ui.";

    private PonderUI(@Nullable ResourceLocation componentId, int sceneIndex, @Nullable GuiScreen parentScreen) {
        super(componentId, sceneIndex, true, parentScreen);
    }

    public static PonderUI showcase(@Nullable ResourceLocation componentId, int sceneIndex) {
        return new PonderUI(componentId, sceneIndex, null);
    }

    public static PonderUI showcase(@Nullable ResourceLocation componentId, int sceneIndex,
        @Nullable GuiScreen parentScreen) {
        return new PonderUI(componentId, sceneIndex, parentScreen);
    }

    private String tr(String key) {
        return I18n.format(UI_LANG_PREFIX + key);
    }

    private String tr(String key, Object... args) {
        return I18n.format(UI_LANG_PREFIX + key, args);
    }

    @Override
    protected String getStandaloneTitle() {
        return tr("showcase.title");
    }

    @Override
    protected boolean allowDebugShortcutFromShowcase() {
        return false;
    }

    @Override
    protected boolean usePlayPauseButtonInShowcase() {
        return true;
    }

    @Override
    protected void initShowcaseButtons() {
        int buttonY = height - 54;
        int centerX = width / 2;

        prevSceneButton = new GuiButton(BUTTON_PREV_SCENE, centerX - 118, buttonY, SHOWCASE_ICON_SIZE,
            SHOWCASE_ICON_SIZE, "<");
        GuiButton closeButton = new GuiButton(BUTTON_CLOSE, centerX - 70, buttonY, SHOWCASE_ICON_SIZE,
            SHOWCASE_ICON_SIZE, "X");
        playPauseButton = new GuiButton(BUTTON_PLAY_PAUSE, centerX - 22, buttonY, 44, 20, tr("button.pause"));
        nextSceneButton = new GuiButton(BUTTON_NEXT_SCENE, centerX + 30, buttonY, SHOWCASE_ICON_SIZE,
            SHOWCASE_ICON_SIZE, ">");
        startButton = new GuiButton(BUTTON_START, centerX + 78, buttonY, SHOWCASE_ICON_SIZE,
            SHOWCASE_ICON_SIZE, "R");

        addCompatButton(prevSceneButton);
        addCompatButton(closeButton);
        addCompatButton(playPauseButton);
        addCompatButton(nextSceneButton);
        addCompatButton(startButton);
        playing = true;
    }

    @Override
    protected GuiScreen createDebugScreenFromShowcase() {
        return new PonderDebugScreen(getSelectedComponentId(), getSelectedSceneIndex(), this);
    }

    @Override
    protected String getNoSceneLabel() {
        return tr("showcase.no_scene");
    }

    @Override
    protected String getNoVisibleBlocksLabel() {
        return tr("showcase.no_visible");
    }

    @Override
    protected String getNoVisibleBlocksHint() {
        return tr("showcase.no_visible_hint");
    }

    @Override
    protected String getShowcaseEmptyTitle() {
        return tr("showcase.empty_title");
    }

    @Override
    protected String getShowcaseNoSceneSubtitle() {
        return tr("showcase.no_scene_subtitle");
    }

    @Override
    protected String getHoverHintForPlaybackBar(int estimatedTick, @Nullable PonderScene scene) {
        return scene == null ? tr("hint.scrub")
            : tr("hint.scrub_tick", Integer.valueOf(estimatedTick), Integer.valueOf(getSceneEndTick(scene)));
    }

    @Override
    protected String getHoverHintForGroupSelector() {
        return tr("hint.group");
    }

    @Override
    protected String getHoverHintForNextUp() {
        return tr("hint.next");
    }

    @Override
    protected void drawShowcaseScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        resetShowcaseTransientState();

        PreviewLayout showcaseLayout = getShowcasePreviewLayout();
        int previewX = showcaseLayout.originX;
        int previewY = showcaseLayout.originY;
        int previewWidth = showcaseLayout.width;
        int previewHeight = showcaseLayout.height;
        int headerX = previewX + 32;
        int headerY = previewY + 16;
        float fade = getShowcaseFade(partialTicks);

        drawGradientRect(0, 0, width, height, 0xF40A1016, 0xFF020406);

        PonderScene scene = getSelectedScene();
        ResourceLocation componentId = getSelectedComponentId();
        ItemStack componentStack = createComponentStack(componentId);
        String stackLabel = componentStack.isEmpty() ? tr("showcase.missing_item") : componentStack.getDisplayName();
        String sceneTitle = scene == null ? getShowcaseEmptyTitle() : scene.getTitle();
        String title = fontRenderer.trimStringToWidth(sceneTitle, 240);
        String subtitle = componentId == null ? getShowcaseNoSceneSubtitle() : stackLabel;

        drawScenePreview(previewX, previewY, previewWidth, previewHeight, partialTicks);
        drawShowcaseBackdrop(previewX, previewY, previewWidth, previewHeight, fade);
        drawGuiTextureOverlays(scene, lastPreviewLayout, getRenderTick(scene, partialTicks), fade);
        drawSceneSpaceOverlays(scene, lastPreviewLayout, getRenderTick(scene, partialTicks), fade);
        drawShowcaseHeader(headerX, headerY, previewWidth - 64, componentStack, title, subtitle, fade);
        drawShowcaseGroupPopup(headerX - 2, headerY + 48, previewWidth - 88, fade);
        drawShowcaseLogo(previewX + previewWidth - 48, previewY + 14, fade);

        int playbackWidth = Math.min(320, previewWidth - 170);
        int playbackX = previewX + (previewWidth - playbackWidth) / 2;
        int playbackY = previewY + previewHeight - 28;
        drawPlaybackBar(playbackX, playbackY, playbackWidth, 3);
        drawShowcaseCaption(scene, previewX, previewY, previewWidth, previewHeight, getRenderTick(scene, partialTicks),
            fade);
        drawNextUpCard(scene, fade);
        drawUserFooter();
    }

    @Override
    protected void drawShowcaseBackdrop(int previewX, int previewY, int previewWidth, int previewHeight, float fade) {
        int glowAlpha = (int) (fade * 68.0F) << 24;
        int borderAlpha = (int) (fade * 56.0F) << 24;
        int vignetteAlpha = (int) (fade * 116.0F) << 24;
        drawGradientRect(previewX - 24, previewY - 24, previewX + previewWidth + 24, previewY + previewHeight + 28,
            glowAlpha | 0x1B2430, 0x00000000);
        drawRect(previewX, previewY, previewX + previewWidth, previewY + 1, borderAlpha | 0xE6E0D2);
        drawRect(previewX, previewY + previewHeight - 1, previewX + previewWidth, previewY + previewHeight,
            borderAlpha | 0x2B323A);
        drawRect(previewX, previewY, previewX + 1, previewY + previewHeight, borderAlpha | 0x56606A);
        drawRect(previewX + previewWidth - 1, previewY, previewX + previewWidth, previewY + previewHeight,
            borderAlpha | 0x2B323A);
        drawGradientRect(previewX, previewY, previewX + previewWidth, previewY + 56, vignetteAlpha | 0x05080C,
            0x00000000);
        drawGradientRect(previewX, previewY + previewHeight - 68, previewX + previewWidth, previewY + previewHeight,
            0x00000000, vignetteAlpha | 0x05080C);
    }

    @Override
    protected void drawShowcaseLogo(int x, int y, float fade) {
        mc.getTextureManager().bindTexture(Ponder.asResource("textures/gui/logo.png"));
        net.minecraft.client.renderer.GlStateManager.enableBlend();
        net.minecraft.client.renderer.GlStateManager.color(1.0F, 1.0F, 1.0F, Math.min(1.0F, fade * 0.78F));
        drawTexturedModalRect(x, y, 0, 0, 32, 32);
        net.minecraft.client.renderer.GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    protected void drawShowcaseHeader(int x, int y, int width, ItemStack componentStack, String title, String subtitle,
        float fade) {
        int boxWidth = Math.min(340, width);
        int boxHeight = 44;
        int alpha = (int) (fade * 188.0F) << 24;
        drawGradientRect(x, y, x + boxWidth, y + boxHeight, alpha | 0x111822, 0x00000000);
        drawRect(x + 36, y + boxHeight - 1, x + boxWidth - 10, y + boxHeight, ((int) (fade * 96.0F) << 24) | 0xD7DFE7);
        drawRect(x - 2, y + 4, x + 28, y + 34, alpha | 0x10151B);
        drawRect(x - 1, y + 5, x + 27, y + 33, ((int) (fade * 64.0F) << 24) | 0xE7E0D1);
        setShowcaseHeaderIconBounds(x - 1, y + 5, 28);

        if (!componentStack.isEmpty()) {
            net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
            mc.getRenderItem().renderItemAndEffectIntoGUI(componentStack, x + 5, y + 11);
            net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();
            net.minecraft.client.renderer.GlStateManager.disableLighting();
        }

        ShowcaseGroupState groupState = getShowcaseGroupState();
        if (groupState != null && groupState.components.size() > 1) {
            int chipX = x + 16;
            int chipY = y + 22;
            int chipFill = ((int) (fade * 188.0F) << 24) | 0x1A2028;
            int chipBorder = ((int) (fade * 156.0F) << 24) | 0xD7DFE7;
            drawRect(chipX, chipY, chipX + 12, chipY + 10, chipBorder);
            drawRect(chipX + 1, chipY + 1, chipX + 11, chipY + 9, chipFill);
            drawCenteredString(fontRenderer, String.valueOf(groupState.components.size()), chipX + 6, chipY + 1, 0xF2EFE7);
        }

        String eyebrow = componentStack.isEmpty() ? tr("showcase.title")
            : tr("showcase.eyebrow_component", componentStack.getItem().getRegistryName());
        drawString(fontRenderer, fontRenderer.trimStringToWidth(eyebrow, boxWidth - 90), x + 36, y + 5, 0xB8C3CC);
        drawString(fontRenderer, fontRenderer.trimStringToWidth(title, boxWidth - 90), x + 36, y + 17, 0xF6F2EA);
        drawString(fontRenderer, fontRenderer.trimStringToWidth(subtitle, boxWidth - 90), x + 36, y + 29, 0xAEB8C1);

        if (getCompiledSceneCount() > 0) {
            String sceneIndex = (getSelectedSceneIndex() + 1) + " / " + getCompiledSceneCount();
            drawString(fontRenderer, sceneIndex, x + boxWidth - 12 - fontRenderer.getStringWidth(sceneIndex), y + 6,
                0xD7DFE7);
        }
    }

    @Override
    protected void drawPlaybackBar(int x, int y, int width, int height) {
        PonderScene scene = getSelectedScene();
        int maxTick = scene == null ? 0 : Math.max(1, getSceneEndTick(scene));
        float progress = scene == null ? 0.0F : getPlaybackTickValue() / (float) maxTick;
        int filled = net.minecraft.util.math.MathHelper.clamp((int) (progress * width), 0, width);

        drawRect(x, y, x + width, y + height, 0x44121820);
        drawRect(x, y, x + filled, y + height, 0xFFD7DFE7);
        if (filled > 0) {
            drawRect(x + filled - 1, y - 1, x + filled, y + height + 1, 0xFFFDFBF4);
        }

        String leftLabel = scene == null ? tr("showcase.none_short")
            : tr("showcase.scene_short", Integer.valueOf(getSelectedSceneIndex() + 1));
        String rightLabel = scene == null ? "T+0" : "T+" + getPlaybackTickValue() + " / " + getSceneEndTick(scene);
        drawString(fontRenderer, leftLabel, x, y - 11, 0xC4CFD7);
        drawString(fontRenderer, rightLabel, x + width - fontRenderer.getStringWidth(rightLabel), y - 11, 0xC4CFD7);
    }

    @Override
    protected void drawNextUpCard(PonderScene scene, float fade) {
        setNextUpCardBounds(0, 0, 0, 0);
        PonderScene nextScene = getNextScene();
        if (scene == null || nextScene == null || nextSceneButton == null) {
            return;
        }

        int maxTick = Math.max(1, getSceneEndTick(scene));
        if (getPlaybackTickValue() < (int) (maxTick * 0.7F)) {
            return;
        }

        String nextUpLabel = tr("showcase.next_up");
        String nextTitle = fontRenderer.trimStringToWidth(nextScene.getTitle(), 170);
        int boxWidth = Math.max(108,
            Math.max(fontRenderer.getStringWidth(nextUpLabel), fontRenderer.getStringWidth(nextTitle)) + 26);
        int anchorX = nextSceneButton.x + nextSceneButton.width / 2;
        int anchorY = nextSceneButton.y - 8;
        int boxX = anchorX - boxWidth / 2;
        int boxY = anchorY - 36;
        setNextUpCardBounds(boxX, boxY, boxWidth, 28);

        int fillTop = ((int) (fade * 188.0F) << 24) | 0x111822;
        int fillBottom = ((int) (fade * 188.0F) << 24) | 0x090D12;
        int border = ((int) (fade * 148.0F) << 24) | 0xD7DFE7;
        drawGradientRect(boxX, boxY, boxX + boxWidth, boxY + 28, fillTop, fillBottom);
        drawRect(boxX, boxY, boxX + boxWidth, boxY + 1, border);
        drawRect(boxX, boxY + 27, boxX + boxWidth, boxY + 28, border);
        drawRect(boxX, boxY, boxX + 1, boxY + 28, border);
        drawRect(boxX + boxWidth - 1, boxY, boxX + boxWidth, boxY + 28, border);
        drawCenteredString(fontRenderer, nextUpLabel, boxX + boxWidth / 2, boxY + 6, 0xAEB8C1);
        drawCenteredString(fontRenderer, nextTitle, boxX + boxWidth / 2, boxY + 17, 0xF3EFE7);
    }

    @Override
    protected void drawShowcaseHoverHints(int mouseX, int mouseY) {
        if (isMouseOverPlaybackBar(mouseX, mouseY)) {
            drawCenteredString(fontRenderer, getHoverHintForPlaybackBar(estimatePlaybackTickForMouse(mouseX),
                getSelectedScene()), width / 2, height - 12, 0xB8C3CC);
            return;
        }
        if (isMouseOverShowcaseHeaderIcon(mouseX, mouseY) && hasShowcaseGroupChoices()) {
            drawCenteredString(fontRenderer, getHoverHintForGroupSelector(), width / 2, height - 12, 0xB8C3CC);
            return;
        }
        ShowcaseGroupIcon groupIcon = getShowcaseGroupIconAt(mouseX, mouseY);
        if (groupIcon != null) {
            ItemStack stack = createComponentStack(groupIcon.componentId);
            String label = stack.isEmpty() ? groupIcon.componentId.toString() : stack.getDisplayName();
            String groupLabel = groupIcon.tag == null ? label : groupIcon.tag.getTitle() + "  |  " + label;
            drawCenteredString(fontRenderer, groupLabel, width / 2, height - 12, 0xB8C3CC);
            return;
        }
        if (isMouseOverNextUpCard(mouseX, mouseY)) {
            drawCenteredString(fontRenderer, getHoverHintForNextUp(), width / 2, height - 12, 0xB8C3CC);
            return;
        }

        GuiButton hoveredButton = null;
        for (GuiButton button : buttonList) {
            if (button.visible && button.isMouseOver()) {
                hoveredButton = button;
                break;
            }
        }

        if (hoveredButton == null || hoveredButton.displayString == null || hoveredButton.displayString.isEmpty()) {
            return;
        }

        drawCenteredString(fontRenderer, hoveredButton.displayString, width / 2, height - 12, 0xB8C3CC);
    }

    private void drawUserFooter() {
        PonderScene scene = getSelectedScene();
        String left = scene == null ? tr("footer.no_scene")
            : tr("footer.scene", Integer.valueOf(getSelectedSceneIndex() + 1),
                Integer.valueOf(Math.max(1, getCompiledSceneCount())));
        String middle = scene == null ? "T+0"
            : tr("footer.tick", Integer.valueOf(getPlaybackTickValue()), Integer.valueOf(getSceneEndTick(scene)),
                tr(isPlaybackRunning() ? "footer.playing" : "footer.paused"));
        String right = tr("footer.help");
        int y = height - 32;
        drawRect(18, y - 4, width - 18, y + 12, 0x66101518);
        drawString(fontRenderer, left, 28, y, 0xD7DFE7);
        drawCenteredString(fontRenderer, middle, width / 2, y, 0xD7DFE7);
        drawString(fontRenderer, right, width - 28 - fontRenderer.getStringWidth(right), y, 0xAEB8C1);
    }

}
