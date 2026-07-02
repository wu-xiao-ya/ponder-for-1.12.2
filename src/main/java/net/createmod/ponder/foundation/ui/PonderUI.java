package net.createmod.ponder.foundation.ui;

import java.util.List;

import javax.annotation.Nullable;

import net.createmod.ponder.foundation.PonderScene;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class PonderUI extends PonderDebugScreen {

    private static final String UI_LANG_PREFIX = "ponder.ui.";
    private ShowcaseChromeRenderer showcaseChromeRenderer;
    private ShowcaseHudRenderer showcaseHudRenderer;
    private ShowcaseRenderer showcaseRenderer;

    private PonderUI(@Nullable ResourceLocation componentId, int sceneIndex, @Nullable GuiScreen parentScreen) {
        super(componentId, sceneIndex, true, parentScreen);
    }

    private ShowcaseChromeRenderer getShowcaseChromeRenderer() {
        if (showcaseChromeRenderer == null) {
            showcaseChromeRenderer = new ShowcaseChromeRenderer(mc, new ShowcaseChromeRenderer.Theme(
                24, 28, 56, 68, 68.0F, 56.0F, 116.0F, 0x1B2430, 0xE6E0D2, 0x2B323A, 0x56606A, 0x05080C,
                new ResourceLocation("ponder", "textures/gui/logo.png"), 0.78F));
        }
        return showcaseChromeRenderer;
    }

    private ShowcaseHudRenderer getShowcaseHudRenderer() {
        if (showcaseHudRenderer == null) {
            showcaseHudRenderer = new ShowcaseHudRenderer(createShowcaseHudHost(), fontRenderer, createShowcaseHudTheme());
        }
        return showcaseHudRenderer;
    }

    private ShowcaseRenderer getShowcaseRenderer() {
        if (showcaseRenderer == null) {
            showcaseRenderer = new ShowcaseRenderer(mc, fontRenderer, createShowcaseRendererHost(),
                createShowcaseRendererTheme());
        }
        return showcaseRenderer;
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
    protected ShowcaseHudRenderer.Theme createShowcaseHudTheme() {
        return new ShowcaseHudRenderer.Theme() {
            @Override
            public String getPlaybackBarHoverLabel(int estimatedTick, @Nullable PonderScene scene) {
                return getHoverHintForPlaybackBar(estimatedTick, scene);
            }

            @Override
            public String getGroupSelectorHoverLabel() {
                return getHoverHintForGroupSelector();
            }

            @Override
            public String getNextUpHoverLabel() {
                return getHoverHintForNextUp();
            }

            @Override
            public String getSceneShortLabel(int sceneIndex) {
                return tr("showcase.scene_short", Integer.valueOf(sceneIndex));
            }

            @Override
            public String getNextUpLabel() {
                return tr("showcase.next_up");
            }
        };
    }

    @Override
    protected ShowcaseRenderer.Theme createShowcaseRendererTheme() {
        return new ShowcaseRenderer.Theme(340, 44, 188.0F, 0x111822, 0x00000000, 96.0F, 0xD7DFE7, 188.0F, 0x10151B,
            64.0F, 0xE7E0D1, 0xB8C3CC, 0xF6F2EA, 0xAEB8C1, 0xFFD7DFE7, 188.0F, 0x1A2028, 156.0F, 0xD7DFE7, 0xF2EFE7,
            (componentStack, componentId) -> componentStack.isEmpty() ? tr("showcase.title")
                : tr("showcase.eyebrow_component", componentStack.getItem().getRegistryName()),
            groupState -> tr("hint.group"));
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
        updateButtonState();
    }

    @Override
    protected GuiScreen createDebugScreenFromShowcase() {
        return new PonderDebugScreen(selectionState().getSelectedComponentId(), selectionState().getSelectedSceneIndex(), this);
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
        interactionHitCache().resetShowcase();

        PreviewLayout showcaseLayout = getShowcasePreviewLayout();
        int previewX = showcaseLayout.originX;
        int previewY = showcaseLayout.originY;
        int previewWidth = showcaseLayout.width;
        int previewHeight = showcaseLayout.height;
        int headerX = previewX + 32;
        int headerY = previewY + 16;
        float fade = previewCameraState().getShowcaseFade(partialTicks);

        drawGradientRect(0, 0, width, height, 0xF40A1016, 0xFF020406);

        PonderScene scene = selectionState().getSelectedScene();
        ResourceLocation componentId = selectionState().getSelectedComponentId();
        ItemStack componentStack = createComponentStack(componentId);
        String stackLabel = componentStack.isEmpty() ? tr("showcase.missing_item") : componentStack.getDisplayName();
        String sceneTitle = scene == null ? getShowcaseEmptyTitle() : scene.getTitle();
        String title = fontRenderer.trimStringToWidth(sceneTitle, 240);
        String subtitle = componentId == null ? getShowcaseNoSceneSubtitle() : stackLabel;
        float renderTick = scene == null ? 0.0F : playbackState().getRenderTick(getSceneEndTick(scene), partialTicks);

        drawScenePreview(previewX, previewY, previewWidth, previewHeight, partialTicks);
        drawShowcaseBackdrop(previewX, previewY, previewWidth, previewHeight, fade);
        drawGuiTextureOverlays(scene, showcaseLayout, renderTick, fade);
        drawSceneSpaceOverlays(scene, showcaseLayout, renderTick, fade);
        drawShowcaseHeader(headerX, headerY, previewWidth - 64, componentStack, title, subtitle, fade);
        drawShowcaseGroupPopup(headerX - 2, headerY + 48, previewWidth - 88, fade);
        drawShowcaseLogo(previewX + previewWidth - 48, previewY + 14, fade);

        int playbackWidth = Math.min(320, previewWidth - 170);
        int playbackX = previewX + (previewWidth - playbackWidth) / 2;
        int playbackY = previewY + previewHeight - 28;
        drawPlaybackBar(playbackX, playbackY, playbackWidth, 3);
        drawShowcaseCaption(scene, previewX, previewY, previewWidth, previewHeight, renderTick, fade);
        drawNextUpCard(scene, fade);
        drawUserFooter();
    }

    @Override
    protected void drawShowcaseBackdrop(int previewX, int previewY, int previewWidth, int previewHeight, float fade) {
        getShowcaseChromeRenderer().drawBackdrop(this, previewX, previewY, previewWidth, previewHeight, fade);
    }

    @Override
    protected void drawShowcaseLogo(int x, int y, float fade) {
        getShowcaseChromeRenderer().drawLogo(this, x, y, fade);
    }

    @Override
    protected void drawShowcaseHeader(int x, int y, int width, ItemStack componentStack, String title, String subtitle,
        float fade) {
        ShowcaseRenderer.HeaderLayout header =
            getShowcaseRenderer().drawHeader(x, y, width, componentStack, selectionState().getSelectedComponentId(),
                title, subtitle, fade, selectionState().getSelectedSceneIndex(),
                selectionState().getCompiledSceneCount());
        interactionHitCache().setShowcaseHeaderIconBounds(header.iconX, header.iconY, header.iconSize);
    }

    @Override
    protected void drawPlaybackBar(int x, int y, int width, int height) {
        interactionHitCache().setPlaybackBarBounds(x, y, width, height);
        PonderScene scene = selectionState().getSelectedScene();
        int selectedSceneIndex = selectionState().getSelectedSceneIndex();
        int compiledSceneCount = selectionState().getCompiledSceneCount();
        int playbackTickValue = playbackState().getPlaybackTick();
        boolean playbackRunning = playbackState().isPlaying();
        int sceneEndTick = scene == null ? 0 : getSceneEndTick(scene);
        getShowcaseHudRenderer().drawPlaybackBar(scene, selectedSceneIndex, compiledSceneCount, playbackTickValue,
            playbackRunning, sceneEndTick, true, x, y, width, height);
    }

    @Override
    protected void drawNextUpCard(PonderScene scene, float fade) {
        ShowcaseHudRenderer.NextUpCardBounds nextUpCard = getShowcaseHudRenderer().drawNextUpCardIfNeeded(scene,
            selectionState().getSelectedSceneIndex(), selectionState().getCompiledSceneCount(),
            selectionState().getCompiledScenes(), playbackState().getPlaybackTick(),
            scene == null ? 0 : getSceneEndTick(scene), nextSceneButton, fade, true);
        if (nextUpCard == null) {
            interactionHitCache().setNextUpCardBounds(0, 0, 0, 0);
        } else {
            interactionHitCache().setNextUpCardBounds(nextUpCard.x, nextUpCard.y, nextUpCard.width,
                nextUpCard.height);
        }
    }

    @Override
    protected void drawShowcaseHoverHints(@Nullable String hoverLabel) {
        getShowcaseHudRenderer().drawHoverHints(width, height - 12, hoverLabel, buttonList.toArray(new GuiButton[0]));
    }

   private void drawUserFooter() {
        PonderScene scene = selectionState().getSelectedScene();
        int selectedSceneIndex = selectionState().getSelectedSceneIndex();
        int compiledSceneCount = selectionState().getCompiledSceneCount();
        int playbackTickValue = playbackState().getPlaybackTick();
        boolean playbackRunning = playbackState().isPlaying();
        int sceneEndTick = scene == null ? 0 : getSceneEndTick(scene);
        String left = scene == null ? tr("footer.no_scene")
            : tr("footer.scene", Integer.valueOf(selectedSceneIndex + 1),
                Integer.valueOf(Math.max(1, compiledSceneCount)));
        String middle = scene == null ? "T+0"
            : tr("footer.tick", Integer.valueOf(playbackTickValue), Integer.valueOf(sceneEndTick),
                tr(playbackRunning ? "footer.playing" : "footer.paused"));
        String right = tr("footer.help");
        int y = height - 32;
        drawRect(18, y - 4, width - 18, y + 12, 0x66101518);
        drawString(fontRenderer, left, 28, y, 0xD7DFE7);
        drawCenteredString(fontRenderer, middle, width / 2, y, 0xD7DFE7);
        drawString(fontRenderer, right, width - 28 - fontRenderer.getStringWidth(right), y, 0xAEB8C1);
    }
}
