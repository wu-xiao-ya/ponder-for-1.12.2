package net.createmod.ponder.foundation.ui;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.foundation.PonderIndex;
import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.Vec3iAccessor;
import net.createmod.ponder.foundation.PonderScene.RecordedOperation;
import net.createmod.ponder.foundation.PonderScene.WorldEvent;
import net.createmod.ponder.foundation.PonderTag;
import net.createmod.ponder.foundation.ui.PonderScenePreview.PreviewBounds;
import net.createmod.ponder.foundation.ui.PonderScenePreview.PreviewState;
import net.createmod.ponder.api.PonderPalette;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class PonderDebugScreen extends CompatGuiScreen {

    private static final ResourceLocation SHOWCASE_WIDGETS_TEXTURE =
        new ResourceLocation("ponder", "textures/gui/widgets.png");
    private static final ResourceLocation SHOWCASE_LOGO_TEXTURE =
        new ResourceLocation("ponder", "textures/gui/logo.png");
    private static final int OUTER_MARGIN = 12;
    private static final int LINE_HEIGHT = 12;
    private static final int LEFT_PANEL_WIDTH = 170;
    private static final int HEADER_HEIGHT = 186;
    private static final int PREVIEW_PANEL_WIDTH = 190;
    private static final int PREVIEW_PANEL_HEIGHT = 142;
    private static final int SHOWCASE_MARGIN = 18;
    private static final int SHOWCASE_BAR_WIDTH = 320;
    private static final int SHOWCASE_BAR_BOTTOM_OFFSET = 14;
    private static final int SHOWCASE_CAPTION_SIDE_CLEARANCE = 16;
    private static final int SHOWCASE_CAPTION_TOP_CLEARANCE = 74;
    private static final int SHOWCASE_CAPTION_BOTTOM_CLEARANCE = 132;
    protected static final int SHOWCASE_ICON_SIZE = 20;
    protected static final int SHOWCASE_ICON_U_LEFT = 0;
    protected static final int SHOWCASE_ICON_U_CLOSE = 16;
    protected static final int SHOWCASE_ICON_U_RIGHT = 32;
    protected static final int SHOWCASE_ICON_U_REPLAY = 64;
    protected static final int SHOWCASE_ICON_V = 32;
    protected static final int BUTTON_PREV_SCENE = 1;
    protected static final int BUTTON_NEXT_SCENE = 2;
    protected static final int BUTTON_PLAY_PAUSE = 3;
    protected static final int BUTTON_RELOAD = 4;
    protected static final int BUTTON_CLOSE = 5;
    protected static final int BUTTON_START = 6;
    protected static final int BUTTON_END = 7;
    protected static final int BUTTON_DEBUG = 8;

    @Nullable
    private final GuiScreen parentScreen;
    private final ResourceLocation requestedComponentId;
    private final int requestedSceneIndex;
    private final boolean showcaseMode;
    private final PonderSceneSelectionState selectionState;

    private final PonderPreviewCameraState previewCameraState = new PonderPreviewCameraState();
    private final PonderPlaybackState playbackState = new PonderPlaybackState();
    private final InteractionHitCache interactionHitCache = new InteractionHitCache();
    private final DebugKeyboardController debugKeyboardController;
    private final ShowcaseKeyboardController showcaseKeyboardController;
    private final DebugMouseController debugMouseController;
    private final ShowcaseMouseController showcaseMouseController;
    private final PonderSceneController sceneController;
    private final PonderOverlayLayoutHelper overlayLayoutHelper;
    @Nullable
    private ScenePreviewRenderer scenePreviewRenderer;
    @Nullable
    private ShowcaseCaptionRenderer showcaseCaptionRenderer;
    @Nullable
    private ShowcaseRenderer fallbackShowcaseRenderer;
    @Nullable
    private ShowcaseHudRenderer fallbackShowcaseHudRenderer;
    @Nullable
    private ShowcaseChromeRenderer fallbackShowcaseChromeRenderer;
    @Nullable
    private DebugPanelRenderer debugPanelRenderer;
    @Nullable
    private GuiOverlayRenderer guiOverlayRenderer;
    @Nullable
    private ActorOverlayRenderer actorOverlayRenderer;
    @Nullable
    private ShowcaseHudRenderer.HoverLabelHost showcaseHoverLabelHost;
    @Nullable
    private ShowcaseHudRenderer.Theme showcaseHudTheme;

    private ShowcaseHudRenderer.Theme getShowcaseHudTheme() {
        if (showcaseHudTheme == null) {
            showcaseHudTheme = createShowcaseHudTheme();
        }
        return showcaseHudTheme;
    }
    @Nullable
    private SceneOverlayRenderer sceneOverlayRenderer;
    @Nullable
    private ControlsOverlayRenderer controlsOverlayRenderer;
    @Nullable
    private ParticleOverlayRenderer particleOverlayRenderer;
    @Nullable
    private PoiOverlayRenderer poiOverlayRenderer;

    protected GuiButton prevSceneButton;
    protected GuiButton nextSceneButton;
    protected GuiButton playPauseButton;
    protected GuiButton startButton;
    protected GuiButton endButton;
    protected GuiButton debugButton;

    public PonderDebugScreen(ResourceLocation componentId, int sceneIndex) {
        this(componentId, sceneIndex, false, null);
    }

    public PonderDebugScreen(ResourceLocation componentId, int sceneIndex, @Nullable GuiScreen parentScreen) {
        this(componentId, sceneIndex, false, parentScreen);
    }

    protected PonderDebugScreen(ResourceLocation componentId, int sceneIndex, boolean showcaseMode,
        @Nullable GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
        this.requestedComponentId = componentId;
        this.requestedSceneIndex = Math.max(0, sceneIndex);
        this.showcaseMode = showcaseMode;
        this.selectionState = new PonderSceneSelectionState(componentId, sceneIndex, showcaseMode);
        this.overlayLayoutHelper = new PonderOverlayLayoutHelper(showcaseMode, previewCameraState);
        this.debugKeyboardController = new DebugKeyboardController(createDebugKeyboardHost());
        this.showcaseKeyboardController = new ShowcaseKeyboardController(createShowcaseKeyboardHost());
        this.debugMouseController = new DebugMouseController(createDebugMouseHost());
        this.showcaseMouseController = new ShowcaseMouseController(createShowcaseMouseHost());
        this.sceneController = new PonderSceneController(createSceneControllerHost());
    }

    public static PonderDebugScreen showcase(ResourceLocation componentId, int sceneIndex) {
        return new PonderDebugScreen(componentId, sceneIndex, true, null);
    }

    public static PonderDebugScreen showcase(ResourceLocation componentId, int sceneIndex,
        @Nullable GuiScreen parentScreen) {
        return new PonderDebugScreen(componentId, sceneIndex, true, parentScreen);
    }

    protected String getStandaloneTitle() {
        return "Ponder Debug Viewer";
    }

    protected boolean allowDebugShortcutFromShowcase() {
        return true;
    }

    protected GuiScreen createDebugScreenFromShowcase() {
        return new PonderDebugScreen(getSelectedComponentId(), selectionState.getSelectedSceneIndex(), this);
    }

    protected String getNoSceneLabel() {
        return "No Scene";
    }

    protected String getNoVisibleBlocksLabel() {
        return "No visible blocks at this tick";
    }

    protected String getNoVisibleBlocksHint() {
        return "Press Play or step the timeline";
    }

    protected String getShowcaseEmptyTitle() {
        return "\u601D\u7D22\u5C55\u793A";
    }

    protected String getShowcaseNoSceneSubtitle() {
        return "\u6CA1\u6709\u53EF\u7528\u7684\u601D\u7D22\u573A\u666F";
    }

    protected String getHoverHintForPlaybackBar(int estimatedTick, @Nullable PonderScene scene) {
        return scene == null ? "Drag timeline" : "Drag timeline | T+" + estimatedTick + " / " + getSceneEndTick(scene);
    }

    protected String getHoverHintForGroupSelector() {
        return "\u70B9\u51FB\u56FE\u6807\u9009\u62E9\u540C\u7EC4\u673A\u5668";
    }

    protected String getHoverHintForNextUp() {
        return "Open next scene";
    }

    protected boolean usePlayPauseButtonInShowcase() {
        return false;
    }

    protected void initShowcaseButtons() {
        int buttonY = height - SHOWCASE_MARGIN - 30;
        int centerX = width / 2;
        int prevX = usePlayPauseButtonInShowcase() ? centerX - 86 : centerX - 58;
        int closeX = usePlayPauseButtonInShowcase() ? centerX - 38 : centerX - 10;
        int playPauseX = centerX - 27;
        int nextX = centerX + 38;
        int replayX = centerX + 86;

        prevSceneButton = new GuiButton(BUTTON_PREV_SCENE, prevX, buttonY, SHOWCASE_ICON_SIZE, SHOWCASE_ICON_SIZE, "<");
        GuiButton closeButton = new GuiButton(BUTTON_CLOSE, closeX, buttonY, SHOWCASE_ICON_SIZE, SHOWCASE_ICON_SIZE, "X");
        nextSceneButton = new GuiButton(BUTTON_NEXT_SCENE, nextX, buttonY, SHOWCASE_ICON_SIZE, SHOWCASE_ICON_SIZE, ">");
        startButton = new GuiButton(BUTTON_START, replayX, buttonY, SHOWCASE_ICON_SIZE, SHOWCASE_ICON_SIZE, "R");
        addCompatButton(prevSceneButton);
        addCompatButton(closeButton);
        if (usePlayPauseButtonInShowcase()) {
            playPauseButton = new GuiButton(BUTTON_PLAY_PAUSE, playPauseX, buttonY, 54, 20, "Pause");
            addCompatButton(playPauseButton);
        } else {
            playPauseButton = null;
        }
        addCompatButton(nextSceneButton);
        addCompatButton(startButton);
        playbackState.setPlaying(true);
    }

    public static List<ResourceLocation> getRegisteredComponents() {
        Set<ResourceLocation> components = new LinkedHashSet<ResourceLocation>();
        Collection<Map.Entry<ResourceLocation, net.createmod.ponder.api.registration.StoryBoardEntry>> entries =
            PonderIndex.getSceneAccess().getRegisteredEntries();
        for (Map.Entry<ResourceLocation, net.createmod.ponder.api.registration.StoryBoardEntry> entry : entries) {
            components.add(entry.getKey());
        }

        List<ResourceLocation> list = new ArrayList<ResourceLocation>(components);
        Collections.sort(list, new Comparator<ResourceLocation>() {
            @Override
            public int compare(ResourceLocation left, ResourceLocation right) {
                return left.toString().compareTo(right.toString());
            }
        });
        return list;
    }

    public static List<String> getRegisteredComponentsAsStrings() {
        List<ResourceLocation> components = getRegisteredComponents();
        List<String> strings = new ArrayList<String>(components.size());
        for (ResourceLocation component : components) {
            strings.add(component.toString());
        }
        return strings;
    }

    private void reloadRegistryAndCurrentComponent() {
        selectionState.loadComponents(getRegisteredComponents());
        selectionState.setSelectedComponentIndex(MathHelper.clamp(selectionState.getSelectedComponentIndex(), 0,
            Math.max(0, selectionState.getComponentIds().size() - 1)));
        sceneController.reloadCurrentComponent(true);
    }

    private PonderSceneSelectionState getSelectionState() {
        return selectionState;
    }

    protected PonderSceneSelectionState selectionState() {
        return selectionState;
    }

    protected PonderPlaybackState playbackState() {
        return playbackState;
    }

    protected InteractionHitCache interactionHitCache() {
        return interactionHitCache;
    }

    protected PonderPreviewCameraState previewCameraState() {
        return previewCameraState;
    }

    protected DebugKeyboardController.Host createDebugKeyboardHost() {
        return new DebugKeyboardController.Host() {
            @Override
            public PonderSceneSelectionState getSelectionState() {
                return PonderDebugScreen.this.getSelectionState();
            }

            @Override
            public PonderPlaybackState getPlaybackState() {
                return playbackState;
            }

            @Override
            public void reloadRegisteredComponents() {
                selectionState.loadComponents(getRegisteredComponents());
                selectionState.setSelectedComponentIndex(MathHelper.clamp(selectionState.getSelectedComponentIndex(), 0,
                    Math.max(0, selectionState.getComponentIds().size() - 1)));
            }

            @Override
            public void reloadCurrentComponent(boolean preserveSceneIndex) {
                sceneController.reloadCurrentComponent(preserveSceneIndex);
            }

            @Override
            public void selectScene(int sceneIndex) {
                sceneController.selectScene(sceneIndex);
            }

            @Override
            public void stepPlaybackTick(int delta) {
                sceneController.stepPlaybackTick(delta);
            }

            @Override
            public void seekToStart() {
                sceneController.seekToStart(false);
            }

            @Override
            public void seekToEnd() {
                sceneController.seekToEnd();
            }

            @Override
            public void updateButtonState() {
                PonderDebugScreen.this.updateButtonState();
            }

            @Override
            public void delegateKeyTyped(char typedChar, int keyCode) throws IOException {
                PonderDebugScreen.super.keyTyped(typedChar, keyCode);
            }
        };
    }

    protected ShowcaseKeyboardController.Host createShowcaseKeyboardHost() {
        return new ShowcaseKeyboardController.Host() {
            @Override
            public PonderSceneSelectionState getSelectionState() {
                return PonderDebugScreen.this.getSelectionState();
            }

            @Override
            public PonderPlaybackState getPlaybackState() {
                return playbackState;
            }

            @Override
            public void reloadRegisteredComponents() {
                selectionState.loadComponents(getRegisteredComponents());
                selectionState.setSelectedComponentIndex(MathHelper.clamp(selectionState.getSelectedComponentIndex(), 0,
                    Math.max(0, selectionState.getComponentIds().size() - 1)));
            }

            @Override
            public void reloadCurrentComponent(boolean preserveSceneIndex) {
                sceneController.reloadCurrentComponent(preserveSceneIndex);
            }

            @Override
            public void selectScene(int sceneIndex) {
                sceneController.selectScene(sceneIndex);
            }

            @Override
            public void stepPlaybackTick(int delta) {
                sceneController.stepPlaybackTick(delta);
            }

            @Override
            public void updateButtonState() {
                PonderDebugScreen.this.updateButtonState();
            }

            @Override
            public boolean allowDebugShortcutFromShowcase() {
                return PonderDebugScreen.this.allowDebugShortcutFromShowcase();
            }

            @Override
            public GuiScreen createDebugScreenFromShowcase() {
                return PonderDebugScreen.this.createDebugScreenFromShowcase();
            }

            @Override
            public void openScreen(GuiScreen screen) {
                mc.displayGuiScreen(screen);
            }

            @Override
            public void delegateKeyTyped(char typedChar, int keyCode) throws IOException {
                PonderDebugScreen.super.keyTyped(typedChar, keyCode);
            }
        };
    }

    protected DebugMouseController.Host createDebugMouseHost() {
        return new DebugMouseController.Host() {
            @Override
            public boolean isMouseOverPlaybackBar(int mouseX, int mouseY) {
                return PonderDebugScreen.this.isMouseOverPlaybackBar(mouseX, mouseY);
            }

            @Override
            public void startPlaybackBarDragging() {
                interactionHitCache.setPlaybackBarDragging(true);
            }

            @Override
            public void seekPlaybackToMouse(int mouseX) {
                PonderDebugScreen.this.seekPlaybackToMouse(mouseX);
            }

            @Override
            public void pausePlayback() {
                playbackState.stop();
            }

            @Override
            public void updateButtonState() {
                PonderDebugScreen.this.updateButtonState();
            }

            @Override
            public int getComponentIndexAt(int mouseX, int mouseY) {
                return PonderDebugScreen.this.getComponentIndexAt(mouseX, mouseY);
            }

            @Override
            public void selectComponent(int componentIndex) {
                sceneController.selectComponent(componentIndex, 0);
            }

            @Override
            public int getOperationIndexAt(int mouseX, int mouseY) {
                return getDebugPanelRenderer().getOperationIndexAt(mouseX, mouseY, height);
            }

            @Override
            public List<PonderScene.RecordedOperation> getSelectedRecordedOperations() {
                return PonderDebugScreen.this.getSelectedRecordedOperations();
            }

            @Override
            public void setPlaybackTick(int tick) {
                sceneController.setPlaybackTick(tick);
            }

            @Override
            public boolean isMouseOverPreview(int mouseX, int mouseY) {
                return PonderDebugScreen.this.isMouseOverPreview(mouseX, mouseY);
            }

            @Override
            public void startPreviewDragging(int mouseX, int mouseY) {
                previewCameraState.startDragging(mouseX, mouseY);
            }

            @Override
            public boolean isPreviewDragging() {
                return previewCameraState.isPreviewDragging();
            }

            @Override
            public void dragPreview(int mouseX, int mouseY) {
                previewCameraState.dragTo(mouseX, mouseY);
            }

            @Override
            public boolean isPlaybackBarDragging() {
                return interactionHitCache.isPlaybackBarDragging();
            }

            @Override
            public void stopPlaybackBarDragging() {
                interactionHitCache.setPlaybackBarDragging(false);
            }

            @Override
            public void stopPreviewDragging() {
                previewCameraState.stopDragging();
            }

            @Override
            public boolean isShiftKeyDown() {
                return PonderDebugScreen.this.isShiftKeyDown();
            }

            @Override
            public void stepPlaybackTick(int delta) {
                sceneController.stepPlaybackTick(delta);
            }

            @Override
            public void adjustPreviewZoom(float delta) {
                previewCameraState.adjustPreviewZoom(delta);
            }

            @Override
            public boolean isMouseOverComponentList(int mouseX, int mouseY) {
                return PonderDebugScreen.this.isMouseOverComponentList(mouseX, mouseY);
            }

            @Override
            public void scrollComponents(int delta) {
                getDebugPanelRenderer().scrollComponents(delta, selectionState.getComponentIds(), height);
            }

            @Override
            public boolean isMouseOverOperations(int mouseX, int mouseY) {
                return PonderDebugScreen.this.isMouseOverOperations(mouseX, mouseY);
            }

            @Override
            public void scrollOperations(int delta) {
                getDebugPanelRenderer().scrollOperations(delta, height);
            }
        };
    }

    protected ShowcaseMouseController.Host createShowcaseMouseHost() {
        return new ShowcaseMouseController.Host() {
            @Override
            public boolean isMouseOverPreview(int mouseX, int mouseY) {
                return PonderDebugScreen.this.isMouseOverPreview(mouseX, mouseY);
            }

            @Override
            public boolean isShiftKeyDown() {
                return PonderDebugScreen.this.isShiftKeyDown();
            }

            @Override
            public void stepPlaybackTick(int delta) {
                sceneController.stepPlaybackTick(delta);
            }

            @Override
            public void adjustPreviewZoom(float delta) {
                previewCameraState.adjustPreviewZoom(delta);
            }

            @Override
            public boolean isMouseOverPlaybackBar(int mouseX, int mouseY) {
                return PonderDebugScreen.this.isMouseOverPlaybackBar(mouseX, mouseY);
            }

            @Override
            public void startPlaybackBarDragging() {
                interactionHitCache.setPlaybackBarDragging(true);
            }

            @Override
            public void seekPlaybackToMouse(int mouseX) {
                PonderDebugScreen.this.seekPlaybackToMouse(mouseX);
            }

            @Override
            public void pausePlayback() {
                playbackState.stop();
            }

            @Override
            public void updateButtonState() {
                PonderDebugScreen.this.updateButtonState();
            }

            @Override
            public ShowcaseGroupIconHitBox getShowcaseGroupIconAt(int mouseX, int mouseY) {
                return interactionHitCache.getShowcaseGroupIconAt(mouseX, mouseY);
            }

            @Override
            public void closeShowcaseGroupSelector() {
                interactionHitCache.setShowcaseGroupSelectorOpen(false);
            }

            @Override
            public void selectComponent(ShowcaseGroupIconHitBox icon) {
                PonderDebugScreen.this.selectComponent(icon.componentId, 0);
            }

            @Override
            public boolean isMouseOverNextUpCard(int mouseX, int mouseY) {
                return PonderDebugScreen.this.isMouseOverNextUpCard(mouseX, mouseY);
            }

            @Override
            public void selectNextScene() {
                sceneController.selectScene(selectionState.getSelectedSceneIndex() + 1);
            }

            @Override
            public boolean isMouseOverShowcaseHeaderIcon(int mouseX, int mouseY) {
                return PonderDebugScreen.this.isMouseOverShowcaseHeaderIcon(mouseX, mouseY);
            }

            @Override
            public boolean hasShowcaseGroupChoices() {
                return PonderDebugScreen.this.hasShowcaseGroupChoices();
            }

            @Override
            public void toggleShowcaseGroupSelector() {
                interactionHitCache.setShowcaseGroupSelectorOpen(!interactionHitCache.isShowcaseGroupSelectorOpen());
            }

            @Override
            public boolean isShowcaseGroupSelectorOpen() {
                return interactionHitCache.isShowcaseGroupSelectorOpen();
            }

            @Override
            public boolean isMouseInsideShowcaseGroupPopup(int mouseX, int mouseY) {
                return interactionHitCache.isMouseInsideShowcaseGroupPopup(mouseX, mouseY);
            }

            @Override
            public void startPreviewDragging(int mouseX, int mouseY) {
                previewCameraState.startDragging(mouseX, mouseY);
            }

            @Override
            public boolean isPlaybackBarDragging() {
                return interactionHitCache.isPlaybackBarDragging();
            }

            @Override
            public void stopPlaybackBarDragging() {
                interactionHitCache.setPlaybackBarDragging(false);
            }

            @Override
            public boolean isPreviewDragging() {
                return previewCameraState.isPreviewDragging();
            }

            @Override
            public void dragPreview(int mouseX, int mouseY) {
                previewCameraState.dragTo(mouseX, mouseY);
            }

            @Override
            public void stopPreviewDragging() {
                previewCameraState.stopDragging();
            }
        };
    }


    protected PonderSceneController.Host createSceneControllerHost() {
        return new PonderSceneController.Host() {
            @Override
            public PonderSceneSelectionState getSelectionState() {
                return selectionState;
            }

            @Override
            public PonderPlaybackState getPlaybackState() {
                return playbackState;
            }

            @Override
            public int getRequestedSceneIndex() {
                return requestedSceneIndex;
            }

            @Override
            public void clearPreviewCaches() {
                if (scenePreviewRenderer != null) {
                    scenePreviewRenderer.clearTileEntityPreviewCache();
                }
            }

            @Override
            public void setShowcaseGroupSelectorOpen(boolean open) {
                interactionHitCache.setShowcaseGroupSelectorOpen(open);
            }

            @Override
            public void resetPreviewCamera() {
                PonderDebugScreen.this.resetPreviewCamera();
            }

            @Override
            public void resetOperationScroll() {
                getDebugPanelRenderer().resetOperationScroll();
            }

            @Override
            public void centerOperationsOnActiveLine() {
                PonderDebugScreen.this.centerOperationsOnActiveLine();
            }

            @Override
            public void updateButtonState() {
                PonderDebugScreen.this.updateButtonState();
            }

            @Override
            public int getMaxComponentScroll() {
                return getDebugPanelRenderer().getMaxComponentScroll(selectionState.getComponentIds(), height);
            }

            @Override
            public void setComponentScroll(int componentScroll) {
                getDebugPanelRenderer().setComponentScroll(componentScroll);
            }

            @Override
            public int getSceneEndTick(PonderScene scene) {
                return PonderDebugScreen.this.getSceneEndTick(scene);
            }
        };
    }

    @Override
    public void initGui() {
        super.initGui();
        this.scenePreviewRenderer = new ScenePreviewRenderer(mc, showcaseMode, 0x00F000F0,
            PonderPreviewRenderHelper.resolveBlockStateMethod());
        resetPreviewCamera();
        selectionState.loadComponents(getRegisteredComponents());
        selectionState.setSelectedComponentIndex(resolveInitialComponentIndex());
        sceneController.reloadCurrentComponent(false);

        clearCompatButtons();
        if (showcaseMode) {
            initShowcaseButtons();
        } else {
            int buttonY = height - OUTER_MARGIN - 20;
            int rightPanelX = OUTER_MARGIN + LEFT_PANEL_WIDTH + OUTER_MARGIN;
            int availableWidth = width - rightPanelX - OUTER_MARGIN;

            prevSceneButton = new GuiButton(BUTTON_PREV_SCENE, rightPanelX, buttonY, 60, 20, "< Scene");
            nextSceneButton = new GuiButton(BUTTON_NEXT_SCENE, rightPanelX + 64, buttonY, 60, 20, "Scene >");
            startButton = new GuiButton(BUTTON_START, rightPanelX + 128, buttonY, 42, 20, "|<");
            playPauseButton = new GuiButton(BUTTON_PLAY_PAUSE, rightPanelX + 174, buttonY, 54, 20, "Play");
            endButton = new GuiButton(BUTTON_END, rightPanelX + 232, buttonY, 42, 20, ">|");
            addCompatButton(prevSceneButton);
            addCompatButton(nextSceneButton);
            addCompatButton(startButton);
            addCompatButton(playPauseButton);
            addCompatButton(endButton);
            addCompatButton(new GuiButton(BUTTON_RELOAD, rightPanelX + 278, buttonY, 60, 20, "Reload"));
            addCompatButton(new GuiButton(BUTTON_CLOSE, rightPanelX + availableWidth - 70, buttonY, 70, 20, "Done"));
        }

        updateButtonState();
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == BUTTON_PREV_SCENE) {
            sceneController.selectScene(selectionState.getSelectedSceneIndex() - 1);
        } else if (button.id == BUTTON_NEXT_SCENE) {
            sceneController.selectScene(selectionState.getSelectedSceneIndex() + 1);
        } else if (button.id == BUTTON_START) {
            sceneController.seekToStart(showcaseMode);
        } else if (button.id == BUTTON_PLAY_PAUSE) {
            sceneController.togglePlayback();
        } else if (button.id == BUTTON_END) {
            sceneController.seekToEnd();
        } else if (button.id == BUTTON_DEBUG) {
            mc.displayGuiScreen(createDebugScreenFromShowcase());
            return;
        } else if (button.id == BUTTON_RELOAD) {
            reloadRegistryAndCurrentComponent();
        } else if (button.id == BUTTON_CLOSE) {
            mc.displayGuiScreen(parentScreen);
        }
        updateButtonState();
    }

    @Override
    public void updateScreen() {
        if (showcaseMode) {
            previewCameraState.tickShowcaseFade();
        }

        if (!playbackState.isPlaying()) {
            return;
        }

        PonderScene scene = getSelectedScene();
        if (scene == null) {
            playbackState.setPlaying(false);
            updateButtonState();
            return;
        }

        int maxTick = getSceneEndTick(scene);
        if (playbackState.getPlaybackTick() < maxTick) {
            sceneController.advanceTick();
        } else if (showcaseMode) {
            sceneController.advanceTick();
        } else {
            playbackState.setPlaying(false);
            updateButtonState();
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();

        int wheel = Mouse.getEventDWheel();
        if (wheel == 0) {
            return;
        }

        int mouseX = Mouse.getEventX() * width / mc.displayWidth;
        int mouseY = height - Mouse.getEventY() * height / mc.displayHeight - 1;

        if (showcaseMode) {
            showcaseMouseController.handleWheel(mouseX, mouseY, wheel);
            return;
        }

        debugMouseController.handleWheel(mouseX, mouseY, wheel);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (mouseButton != 0) {
            return;
        }

        if (showcaseMode) {
            showcaseMouseController.handleLeftClick(mouseX, mouseY);
            return;
        }

        debugMouseController.handleLeftClick(mouseX, mouseY);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);

        if (showcaseMode) {
            showcaseMouseController.handleDrag(mouseX, mouseY, clickedMouseButton);
            return;
        }
        debugMouseController.handleDrag(mouseX, mouseY, clickedMouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        if (showcaseMode) {
            showcaseMouseController.handleRelease(state);
            return;
        }
        debugMouseController.handleRelease(state);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(parentScreen);
            return;
        }

        if (showcaseMode) {
            if (showcaseKeyboardController.handle(typedChar, keyCode)) {
                return;
            }
            return;
        }
        debugKeyboardController.handle(typedChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    protected void drawShowcaseScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        interactionHitCache.resetShowcase();

        PreviewLayout showcaseLayout = getShowcasePreviewLayout();
        int previewX = showcaseLayout.originX;
        int previewY = showcaseLayout.originY;
        int previewWidth = showcaseLayout.width;
        int previewHeight = showcaseLayout.height;
        int headerX = previewX + 40;
        int headerY = previewY + 18;
        float fade = previewCameraState.getShowcaseFade(partialTicks);

        drawGradientRect(0, 0, width, height, 0xF306090D, 0xFF010205);

        PonderScene scene = getSelectedScene();
        ResourceLocation componentId = getSelectedComponentId();
        ItemStack componentStack = createComponentStack(componentId);
        String stackLabel = componentStack.isEmpty() ? "\u672A\u627E\u5230\u5BF9\u5E94\u7269\u54C1" : componentStack.getDisplayName();
        String sceneTitle = scene == null ? getShowcaseEmptyTitle() : scene.getTitle();
        String title = fontRenderer.trimStringToWidth(sceneTitle, 240);
        String subtitle = componentId == null ? getShowcaseNoSceneSubtitle() : stackLabel;

        float renderTick = getRenderTick(scene, partialTicks);
        drawScenePreview(previewX, previewY, previewWidth, previewHeight, partialTicks);
        drawShowcaseBackdrop(previewX, previewY, previewWidth, previewHeight, fade);
        drawGuiTextureOverlays(scene, interactionHitCache.getPreviewLayout(), renderTick, fade);
        drawSceneSpaceOverlays(scene, interactionHitCache.getPreviewLayout(), renderTick, fade);
        drawShowcaseHeader(headerX, headerY, previewWidth - 80, componentStack, title, subtitle, fade);
        drawShowcaseGroupPopup(headerX - 2, headerY + 48, previewWidth - 96, fade);
        drawShowcaseLogo(previewX + previewWidth - 46, previewY + 12, fade);
        int playbackWidth = Math.min(SHOWCASE_BAR_WIDTH, previewWidth - 150);
        int playbackX = previewX + (previewWidth - playbackWidth) / 2;
        int playbackY = previewY + previewHeight - SHOWCASE_BAR_BOTTOM_OFFSET;
        drawPlaybackBar(playbackX, playbackY, playbackWidth, 3);
        drawShowcaseCaption(scene, previewX, previewY, previewWidth, previewHeight, renderTick, fade);
        drawNextUpCard(scene, fade);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (showcaseMode) {
            drawShowcaseScreen(mouseX, mouseY, partialTicks);
            super.drawScreen(mouseX, mouseY, partialTicks);
            drawShowcaseHoverHints(computeShowcaseHoverLabel(mouseX, mouseY));
            return;
        }

        drawDefaultBackground();
        interactionHitCache.resetDebug();

        int leftPanelX = OUTER_MARGIN;
        int leftPanelY = OUTER_MARGIN;
        int leftPanelBottom = height - OUTER_MARGIN - 28;
        int rightPanelX = leftPanelX + LEFT_PANEL_WIDTH + OUTER_MARGIN;
        int rightPanelY = OUTER_MARGIN;
        int rightPanelWidth = width - rightPanelX - OUTER_MARGIN;
        int rightPanelBottom = height - OUTER_MARGIN - 28;

        drawRect(leftPanelX, leftPanelY, leftPanelX + LEFT_PANEL_WIDTH, leftPanelBottom, 0xAA101015);
        drawRect(rightPanelX, rightPanelY, rightPanelX + rightPanelWidth, rightPanelBottom, 0xAA101015);

        drawCenteredString(fontRenderer, getStandaloneTitle(), width / 2, 6, 0xFFFFFF);
        drawString(fontRenderer, "Components", leftPanelX + 6, leftPanelY + 6, 0xFFEEDD);
        drawString(fontRenderer, "Scene", rightPanelX + 6, rightPanelY + 6, 0xFFEEDD);

        drawComponentList(mouseX, mouseY, leftPanelX, leftPanelY + 20, leftPanelBottom);
        int previewPanelX = rightPanelX + rightPanelWidth - PREVIEW_PANEL_WIDTH - 8;
        int summaryWidth = Math.max(150, previewPanelX - rightPanelX - 16);
        drawSceneSummary(rightPanelX + 6, rightPanelY + 20, summaryWidth);
        PonderScene selectedScene = getSelectedScene();
        float renderTick = getRenderTick(selectedScene, partialTicks);
        drawScenePreview(previewPanelX, rightPanelY + 18, PREVIEW_PANEL_WIDTH, PREVIEW_PANEL_HEIGHT, partialTicks);
        drawGuiTextureOverlays(selectedScene, interactionHitCache.getPreviewLayout(), renderTick, 1.0F);
        drawSceneSpaceOverlays(selectedScene, interactionHitCache.getPreviewLayout(), renderTick, 1.0F);
        drawOperationList(mouseX, mouseY, rightPanelX, rightPanelY + HEADER_HEIGHT, rightPanelWidth, rightPanelBottom);

        drawString(fontRenderer, "Space play/pause  Up/Down +/-1t  PgUp/PgDn +/-20t  Left/Right scene  Home/End jump",
            rightPanelX + 6, rightPanelBottom - 12, 0xA0A0A0);

        super.drawScreen(mouseX, mouseY, partialTicks);
        drawTooltips(mouseX, mouseY);
    }

    protected PreviewLayout getShowcasePreviewLayout() {
        return new PreviewLayout(SHOWCASE_MARGIN, SHOWCASE_MARGIN, width - SHOWCASE_MARGIN * 2,
            height - SHOWCASE_MARGIN * 2);
    }

private void drawComponentList(int mouseX, int mouseY, int x, int startY, int bottom) {
        getDebugPanelRenderer().drawComponentList(mouseX, mouseY, x, startY, bottom, LINE_HEIGHT, LEFT_PANEL_WIDTH,
            new DebugDrawContext() {
                @Override
                public void fillRect(int left, int top, int right, int bottom, int color) {
                    PonderDebugScreen.this.drawRect(left, top, right, bottom, color);
                }

                @Override
                public void drawString(String text, int drawX, int drawY, int color) {
                    PonderDebugScreen.this.drawString(fontRenderer, text, drawX, drawY, color);
                }
            });
    }

    private void drawSceneSummary(int x, int y, int width) {
        PonderScene scene = getSelectedScene();
        getDebugPanelRenderer().drawSceneSummary(x, y, width, playbackState.getPlaybackTick(),
            playbackState.isPlaying(), scene == null ? 0 : getSceneEndTick(scene), new DebugDrawContext() {
                @Override
                public void fillRect(int left, int top, int right, int bottom, int color) {
                    PonderDebugScreen.this.drawRect(left, top, right, bottom, color);
                }

                @Override
                public void drawString(String text, int drawX, int drawY, int color) {
                    PonderDebugScreen.this.drawString(fontRenderer, text, drawX, drawY, color);
                }
            });
    }

    protected void drawScenePreview(int x, int y, int width, int height, float partialTicks) {
        if (showcaseMode) {
            interactionHitCache.setPreviewLayout(new PreviewLayout(x + 1, y + 1, width - 2, height - 2));
        } else {
            drawRect(x, y, x + width, y + height, 0x66192026);
            drawString(fontRenderer, "3D Preview", x + 6, y + 6, 0xFFEEDD);

            int viewportX = x + 6;
            int viewportY = y + 18;
            int viewportWidth = width - 12;
            int viewportHeight = height - 38;
            drawRect(viewportX, viewportY, viewportX + viewportWidth, viewportY + viewportHeight, 0xCC0B1016);
            drawRect(viewportX, viewportY, viewportX + viewportWidth, viewportY + 1, 0x55F0E6D2);
            drawRect(viewportX, viewportY + viewportHeight - 1, viewportX + viewportWidth, viewportY + viewportHeight,
                0x33202020);
            interactionHitCache.setPreviewLayout(new PreviewLayout(viewportX + 1, viewportY + 1, viewportWidth - 2, viewportHeight - 2));
        }

        PonderScene scene = getSelectedScene();
        if (scene == null) {
            drawCenteredString(fontRenderer, getNoSceneLabel(),
                x + width / 2, y + height / 2 - 4, 0xA0A0A0);
            return;
        }

        PreviewBounds bounds = PonderScenePreview.computeBounds(scene);
        float renderTick = getRenderTick(scene, partialTicks);
        PonderSceneRuntimeTypes.RuntimeState runtimeState = PonderSceneRuntime.buildState(scene, renderTick);

        if (runtimeState.visibleBlocks() > 0) {
            PreviewLayout layout = interactionHitCache.getPreviewLayout();
            if (scenePreviewRenderer != null && layout != null) {
                scenePreviewRenderer.renderPreview(scene, bounds, runtimeState, layout, renderTick,
                    previewCameraState, overlayLayoutHelper);
            }
        } else {
            drawCenteredString(fontRenderer, getNoVisibleBlocksLabel(), x + width / 2, y + height / 2 - 6, 0xA0A0A0);
            drawCenteredString(fontRenderer,
                getNoVisibleBlocksHint(), x + width / 2, y + height / 2 + 8, 0x707070);
        }

        if (!showcaseMode) {
            String stateLine = "Blocks " + runtimeState.visibleBlocks() + "  zoom "
                + Math.round(previewCameraState.getPreviewZoom() * 100.0F) + "%  drag orbit";
            drawString(fontRenderer, fontRenderer.trimStringToWidth(stateLine, width - 12), x + 6, y + height - 18,
                0xC0C0C0);
            drawString(fontRenderer, "wheel zoom  Shift+wheel tick", x + 6, y + height - 8, 0xA0A0A0);
        }
    }

    protected void drawPlaybackBar(int x, int y, int width, int height) {
        interactionHitCache.setPlaybackBarBounds(x, y, width, height);

        PonderScene scene = getSelectedScene();
        int maxTick = scene == null ? 0 : Math.max(1, getSceneEndTick(scene));
        float progress = scene == null ? 0.0F : playbackState.getPlaybackTick() / (float) maxTick;
        int filled = MathHelper.clamp((int) (progress * width), 0, width);

        if (showcaseMode) {
            drawRect(x, y, x + width, y + height, 0x44101518);
            drawRect(x, y, x + filled, y + height, 0xFFD9D1BF);
            if (filled > 0) {
                drawRect(x + filled - 1, y - 1, x + filled, y + height + 1, 0xFFFDFBF4);
            }

            if (scene != null) {
                String leftLabel = "\u573A\u666F " + (selectionState.getSelectedSceneIndex() + 1) + " / " + selectionState.getCompiledSceneCount();
                String rightLabel = playbackState.getPlaybackTick() + " / " + getSceneEndTick(scene);
                drawString(fontRenderer, leftLabel, x, y - 11, 0xC4CFD7);
                drawString(fontRenderer, rightLabel, x + width - fontRenderer.getStringWidth(rightLabel), y - 11,
                    0xC4CFD7);
            }
            return;
        }

        drawRect(x, y, x + width, y + height, 0x44202020);
        drawRect(x, y, x + filled, y + height, 0xCC6AB8FF);
        if (filled > 0) {
            drawRect(x + filled - 1, y, x + filled, y + height, 0xFFEFF8FF);
        }

        String label = scene == null ? "No scene"
            : "T+" + playbackState.getPlaybackTick() + " / " + getSceneEndTick(scene)
                + (playbackState.isPlaying() ? "   looping" : "   paused");
        drawCenteredString(fontRenderer, label, x + width / 2, y - 11, 0xD8E4E8);
    }

    protected void drawShowcaseHeader(int x, int y, int width, ItemStack componentStack, String title, String subtitle,
        float fade) {
        ShowcaseRenderer.HeaderLayout header = getFallbackShowcaseRenderer().drawHeader(x, y, width, componentStack,
            getSelectedComponentId(), title, subtitle, fade, selectionState.getSelectedSceneIndex(),
            selectionState.getCompiledSceneCount());
        interactionHitCache.setShowcaseHeaderIconBounds(header.iconX, header.iconY, header.iconSize);
    }

    protected ShowcaseHudRenderer.Host createShowcaseHudHost() {
        return new ShowcaseHudRenderer.Host() {
            @Override
            public void drawRect(int left, int top, int right, int bottom, int color) {
                PonderDebugScreen.this.drawRect(left, top, right, bottom, color);
            }

            @Override
            public void drawGradientRect(int left, int top, int right, int bottom, int startColor, int endColor) {
                PonderDebugScreen.this.drawGradientRect(left, top, right, bottom, startColor, endColor);
            }

            @Override
            public void drawString(FontRenderer fontRenderer, String text, int x, int y, int color) {
                PonderDebugScreen.this.drawString(fontRenderer, text, x, y, color);
            }

            @Override
            public void drawCenteredString(FontRenderer fontRenderer, String text, int x, int y, int color) {
                PonderDebugScreen.this.drawCenteredString(fontRenderer, text, x, y, color);
            }

        };
    }

    protected ShowcaseRenderer.Host createShowcaseRendererHost() {
        return new ShowcaseRenderer.Host() {
            @Override
            public void drawRect(int left, int top, int right, int bottom, int color) {
                PonderDebugScreen.this.drawRect(left, top, right, bottom, color);
            }

            @Override
            public void drawGradientRect(int left, int top, int right, int bottom, int startColor, int endColor) {
                PonderDebugScreen.this.drawGradientRect(left, top, right, bottom, startColor, endColor);
            }

            @Override
            public void drawString(FontRenderer fontRenderer, String text, int x, int y, int color) {
                PonderDebugScreen.this.drawString(fontRenderer, text, x, y, color);
            }

            @Override
            public ShowcaseGroupModel getShowcaseGroupState() {
                return PonderDebugScreen.this.getShowcaseGroupState();
            }

            @Override
            public ResourceLocation getSelectedComponentId() {
                return PonderDebugScreen.this.getSelectedComponentId();
            }

            @Override
            public ItemStack createComponentStack(ResourceLocation componentId) {
                return PonderDebugScreen.this.createComponentStack(componentId);
            }

        };
    }

    protected ShowcaseCaptionRenderer.Host createShowcaseCaptionHost() {
        return new ShowcaseCaptionRenderer.Host() {
            @Override
            public PonderScene.OverlayEvent getShowcaseCaption(PonderScene scene, float currentTick) {
                ShowcaseCaption caption = PonderDebugScreen.this.getShowcaseCaption(scene, currentTick);
                return caption == null ? null : caption.overlayEvent;
            }

            @Override
            public float computeCaptionFade(PonderScene.OverlayEvent overlayEvent, float currentTick) {
                return overlayEvent == null ? 0.0F
                    : PonderOverlayHelper.computeOverlayFade(overlayEvent.getTick(), overlayEvent.getDuration(),
                        currentTick);
            }

            @Override
            public PreviewLayout getLastPreviewLayout() {
                return interactionHitCache.getPreviewLayout();
            }

            @Override
            public void drawSpeechBox(int boxX, int boxY, int boxWidth, int boxHeight,
                SpeechRenderer.SpeechPointing pointing, int accentColor, float fade) {
                PonderDebugScreen.this.drawSpeechBox(boxX, boxY, boxWidth, boxHeight, pointing, accentColor, fade);
            }

            @Override
            public SpeechRenderer.Point getSpeechPointerTip(int boxX, int boxY, int boxWidth, int boxHeight,
                SpeechRenderer.SpeechPointing pointing) {
                return PonderDebugScreen.this.getSpeechPointerTip(boxX, boxY, boxWidth, boxHeight, pointing);
            }

            @Override
            public void drawCaptionConnector(int startX, int startY, int endX, int endY, int color, float fade) {
                PonderDebugScreen.this.drawCaptionConnector(startX, startY, endX, endY, color, fade);
            }

            @Override
            public void drawCenteredStringNoShadow(String text, int centerX, int y, int color) {
                PonderDebugScreen.this.drawCenteredStringNoShadow(text, centerX, y, color);
            }
        };
    }

    private ShowcaseCaptionRenderer getShowcaseCaptionRenderer() {
        if (showcaseCaptionRenderer == null) {
            showcaseCaptionRenderer = new ShowcaseCaptionRenderer(fontRenderer, createShowcaseCaptionHost(),
                overlayLayoutHelper::projectScenePoint);
        }
        return showcaseCaptionRenderer;
    }

    protected ShowcaseRenderer.Theme createShowcaseRendererTheme() {
        return new ShowcaseRenderer.Theme(326, 42, 185.0F, 0x141920, 0x0E1218, 120.0F, 0xD5CCB8, 188.0F, 0x10151B,
            64.0F, 0xE7E0D1, 0xB8C3CC, 0xF6F2EA, 0xAEB8C1, 0xFFC8D0D8, 188.0F, 0x1A2028, 156.0F, 0xD5CCB8, 0xF2EFE7,
            (componentStack, componentId) -> "\u601D\u7D22", groupState -> "\u5206\u7EC4\u9009\u62E9  " + groupState.tag.getTitle());
    }

    private ShowcaseRenderer getFallbackShowcaseRenderer() {
        if (fallbackShowcaseRenderer == null) {
            fallbackShowcaseRenderer = new ShowcaseRenderer(mc, fontRenderer, createShowcaseRendererHost(),
                createShowcaseRendererTheme());
        }
        return fallbackShowcaseRenderer;
    }

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
                return "场景 " + sceneIndex;
            }

            @Override
            public String getNextUpLabel() {
                return "接下来";
            }
        };
    }

    private ShowcaseHudRenderer getFallbackShowcaseHudRenderer() {
        if (fallbackShowcaseHudRenderer == null) {
            fallbackShowcaseHudRenderer = new ShowcaseHudRenderer(createShowcaseHudHost(), fontRenderer,
                createShowcaseHudTheme());
        }
        return fallbackShowcaseHudRenderer;
    }

    protected ShowcaseChromeRenderer.Theme createShowcaseChromeTheme() {
        return new ShowcaseChromeRenderer.Theme(20, 26, 52, 60, 52.0F, 44.0F, 92.0F, 0x17202A, 0xF1E8D4, 0x2C3138,
            0x4D545E, 0x030508, SHOWCASE_LOGO_TEXTURE, 0.72F);
    }

    private ShowcaseChromeRenderer getFallbackShowcaseChromeRenderer() {
        if (fallbackShowcaseChromeRenderer == null) {
            fallbackShowcaseChromeRenderer = new ShowcaseChromeRenderer(mc, createShowcaseChromeTheme());
        }
        return fallbackShowcaseChromeRenderer;
    }

    private DebugPanelRenderer getDebugPanelRenderer() {
        if (debugPanelRenderer == null) {
            debugPanelRenderer = new DebugPanelRenderer(fontRenderer, selectionState, this::formatWorldEvent);
        }
        return debugPanelRenderer;
    }

    private GuiOverlayRenderer getGuiOverlayRenderer() {
        if (guiOverlayRenderer == null) {
            guiOverlayRenderer = new GuiOverlayRenderer(mc);
        }
        return guiOverlayRenderer;
    }

    private ActorOverlayRenderer getActorOverlayRenderer() {
        if (actorOverlayRenderer == null) {
            actorOverlayRenderer = new ActorOverlayRenderer(new DrawContext() {
                @Override
                public void fillRect(int left, int top, int right, int bottom, int color) {
                    PonderDebugScreen.this.drawRect(left, top, right, bottom, color);
                }

                @Override
                public void drawString(String text, int x, int y, int color) {
                    PonderDebugScreen.this.drawString(fontRenderer, text, x, y, color);
                }

                @Override
                public void drawLineSegment(int startX, int startY, int endX, int endY, int color, float width) {
                    PonderDebugScreen.this.drawLineSegment(startX, startY, endX, endY, color, width);
                }

                @Override
                public int withAlpha(int color, float alpha) {
                    return PonderDebugScreen.this.withAlpha(color, alpha);
                }

                @Override
                public int blendColors(int baseColor, int accentColor, float accentWeight) {
                    return PonderDebugScreen.this.blendColors(baseColor, accentColor, accentWeight);
                }

                @Override
                public void renderItemStack(ItemStack stack, int x, int y) {
                    net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
                    mc.getRenderItem().renderItemAndEffectIntoGUI(stack, x, y);
                    net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();
                    GlStateManager.disableLighting();
                }

                @Override
                public void drawHoveringText(List<String> textLines, int x, int y) {
                    PonderDebugScreen.this.drawHoveringText(textLines, x, y);
                }

                @Override
                public int getStringWidth(String text) {
                    return fontRenderer.getStringWidth(text);
                }

                @Override
                public void drawCenteredString(String text, int centerX, int y, int color) {
                    PonderDebugScreen.this.drawCenteredString(fontRenderer, text, centerX, y, color);
                }
            });
        }
        return actorOverlayRenderer;
    }

    private SceneOverlayRenderer getSceneOverlayRenderer() {
        if (sceneOverlayRenderer == null) {
            sceneOverlayRenderer = new SceneOverlayRenderer(overlayLayoutHelper, new DrawContext() {
                @Override
                public void fillRect(int left, int top, int right, int bottom, int color) {
                    PonderDebugScreen.this.drawRect(left, top, right, bottom, color);
                }

                @Override
                public void drawString(String text, int x, int y, int color) {
                    PonderDebugScreen.this.drawString(fontRenderer, text, x, y, color);
                }

                @Override
                public void drawLineSegment(int startX, int startY, int endX, int endY, int color, float width) {
                    PonderDebugScreen.this.drawLineSegment(startX, startY, endX, endY, color, width);
                }

                @Override
                public int withAlpha(int color, float alpha) {
                    return PonderDebugScreen.this.withAlpha(color, alpha);
                }

                @Override
                public int blendColors(int baseColor, int accentColor, float accentWeight) {
                    return PonderDebugScreen.this.blendColors(baseColor, accentColor, accentWeight);
                }

                @Override
                public void renderItemStack(ItemStack stack, int x, int y) {
                    net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
                    mc.getRenderItem().renderItemAndEffectIntoGUI(stack, x, y);
                    net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();
                    GlStateManager.disableLighting();
                }

                @Override
                public void drawHoveringText(List<String> textLines, int x, int y) {
                    PonderDebugScreen.this.drawHoveringText(textLines, x, y);
                }

                @Override
                public int getStringWidth(String text) {
                    return fontRenderer.getStringWidth(text);
                }

                @Override
                public void drawCenteredString(String text, int centerX, int y, int color) {
                    PonderDebugScreen.this.drawCenteredString(fontRenderer, text, centerX, y, color);
                }
            });
        }
        return sceneOverlayRenderer;
    }

    private ControlsOverlayRenderer getControlsOverlayRenderer() {
        if (controlsOverlayRenderer == null) {
            controlsOverlayRenderer = new ControlsOverlayRenderer(overlayLayoutHelper, new DrawContext() {
                @Override
                public void fillRect(int left, int top, int right, int bottom, int color) {
                    PonderDebugScreen.this.drawRect(left, top, right, bottom, color);
                }

                @Override
                public void drawString(String text, int x, int y, int color) {
                    PonderDebugScreen.this.drawString(fontRenderer, text, x, y, color);
                }

                @Override
                public void drawLineSegment(int startX, int startY, int endX, int endY, int color, float width) {
                    PonderDebugScreen.this.drawLineSegment(startX, startY, endX, endY, color, width);
                }

                @Override
                public int withAlpha(int color, float alpha) {
                    return PonderDebugScreen.this.withAlpha(color, alpha);
                }

                @Override
                public int blendColors(int baseColor, int accentColor, float accentWeight) {
                    return PonderDebugScreen.this.blendColors(baseColor, accentColor, accentWeight);
                }

                @Override
                public void renderItemStack(ItemStack stack, int x, int y) {
                    net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
                    mc.getRenderItem().renderItemAndEffectIntoGUI(stack, x, y);
                    net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();
                    GlStateManager.disableLighting();
                }

                @Override
                public void drawHoveringText(List<String> textLines, int x, int y) {
                    PonderDebugScreen.this.drawHoveringText(textLines, x, y);
                }

                @Override
                public int getStringWidth(String text) {
                    return fontRenderer.getStringWidth(text);
                }

                @Override
                public void drawCenteredString(String text, int centerX, int y, int color) {
                    PonderDebugScreen.this.drawCenteredString(fontRenderer, text, centerX, y, color);
                }
            });
        }
        return controlsOverlayRenderer;
    }

    private ParticleOverlayRenderer getParticleOverlayRenderer() {
        if (particleOverlayRenderer == null) {
            particleOverlayRenderer = new ParticleOverlayRenderer(overlayLayoutHelper, new DrawContext() {
                @Override
                public void fillRect(int left, int top, int right, int bottom, int color) {
                    PonderDebugScreen.this.drawRect(left, top, right, bottom, color);
                }

                @Override
                public void drawString(String text, int x, int y, int color) {
                    PonderDebugScreen.this.drawString(fontRenderer, text, x, y, color);
                }

                @Override
                public void drawLineSegment(int startX, int startY, int endX, int endY, int color, float width) {
                    PonderDebugScreen.this.drawLineSegment(startX, startY, endX, endY, color, width);
                }

                @Override
                public int withAlpha(int color, float alpha) {
                    return PonderDebugScreen.this.withAlpha(color, alpha);
                }

                @Override
                public int blendColors(int baseColor, int accentColor, float accentWeight) {
                    return PonderDebugScreen.this.blendColors(baseColor, accentColor, accentWeight);
                }

                @Override
                public void renderItemStack(ItemStack stack, int x, int y) {
                    net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
                    mc.getRenderItem().renderItemAndEffectIntoGUI(stack, x, y);
                    net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();
                    GlStateManager.disableLighting();
                }

                @Override
                public void drawHoveringText(List<String> textLines, int x, int y) {
                    PonderDebugScreen.this.drawHoveringText(textLines, x, y);
                }

                @Override
                public int getStringWidth(String text) {
                    return fontRenderer.getStringWidth(text);
                }

                @Override
                public void drawCenteredString(String text, int centerX, int y, int color) {
                    PonderDebugScreen.this.drawCenteredString(fontRenderer, text, centerX, y, color);
                }
            });
        }
        return particleOverlayRenderer;
    }

    private PoiOverlayRenderer getPoiOverlayRenderer() {
        if (poiOverlayRenderer == null) {
            poiOverlayRenderer = new PoiOverlayRenderer(overlayLayoutHelper, new DrawContext() {
                @Override
                public void fillRect(int left, int top, int right, int bottom, int color) {
                    PonderDebugScreen.this.drawRect(left, top, right, bottom, color);
                }

                @Override
                public void drawString(String text, int x, int y, int color) {
                    PonderDebugScreen.this.drawString(fontRenderer, text, x, y, color);
                }

                @Override
                public void drawLineSegment(int startX, int startY, int endX, int endY, int color, float width) {
                    PonderDebugScreen.this.drawLineSegment(startX, startY, endX, endY, color, width);
                }

                @Override
                public int withAlpha(int color, float alpha) {
                    return PonderDebugScreen.this.withAlpha(color, alpha);
                }

                @Override
                public int blendColors(int baseColor, int accentColor, float accentWeight) {
                    return PonderDebugScreen.this.blendColors(baseColor, accentColor, accentWeight);
                }

                @Override
                public void renderItemStack(ItemStack stack, int x, int y) {
                    net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
                    mc.getRenderItem().renderItemAndEffectIntoGUI(stack, x, y);
                    net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();
                    GlStateManager.disableLighting();
                }

                @Override
                public void drawHoveringText(List<String> textLines, int x, int y) {
                    PonderDebugScreen.this.drawHoveringText(textLines, x, y);
                }

                @Override
                public int getStringWidth(String text) {
                    return fontRenderer.getStringWidth(text);
                }

                @Override
                public void drawCenteredString(String text, int centerX, int y, int color) {
                    PonderDebugScreen.this.drawCenteredString(fontRenderer, text, centerX, y, color);
                }
            });
        }
        return poiOverlayRenderer;
    }

    private ShowcaseHudRenderer.HoverLabelHost getShowcaseHoverLabelHost() {
        if (showcaseHoverLabelHost == null) {
            showcaseHoverLabelHost = new ShowcaseHudRenderer.HoverLabelHost() {
                @Override
                public boolean isMouseOverPlaybackBar(int mouseX, int mouseY) {
                    return PonderDebugScreen.this.isMouseOverPlaybackBar(mouseX, mouseY);
                }

                @Override
                public boolean isMouseOverShowcaseHeaderIcon(int mouseX, int mouseY) {
                    return PonderDebugScreen.this.isMouseOverShowcaseHeaderIcon(mouseX, mouseY);
                }

                @Override
                public boolean hasShowcaseGroupChoices() {
                    return PonderDebugScreen.this.hasShowcaseGroupChoices();
                }

                @Override
                public ShowcaseGroupIconHitBox getShowcaseGroupIconAt(int mouseX, int mouseY) {
                    return interactionHitCache.getShowcaseGroupIconAt(mouseX, mouseY);
                }

                @Override
                public boolean isMouseOverNextUpCard(int mouseX, int mouseY) {
                    return PonderDebugScreen.this.isMouseOverNextUpCard(mouseX, mouseY);
                }

                @Override
                public int estimatePlaybackTickForMouse(int mouseX) {
                    return PonderDebugScreen.this.estimatePlaybackTickForMouse(mouseX);
                }

                @Override
                public PonderScene getSelectedScene() {
                    return PonderDebugScreen.this.getSelectedScene();
                }

                @Override
                public ItemStack createComponentStack(ResourceLocation componentId) {
                    return PonderDebugScreen.this.createComponentStack(componentId);
                }
            };
        }
        return showcaseHoverLabelHost;
    }

    protected void drawShowcaseGroupPopup(int x, int y, int maxWidth, float fade) {
        ShowcaseRenderer.GroupPopupResult popup = getFallbackShowcaseRenderer().drawGroupPopup(x, y, maxWidth,
            interactionHitCache.getShowcaseHeaderIconX(), fade, interactionHitCache.isShowcaseGroupSelectorOpen(),
            getSelectedComponentId());
        if (popup == null) {
            if (!interactionHitCache.isShowcaseGroupSelectorOpen()) {
                interactionHitCache.setShowcaseGroupPopupState(Collections.<ShowcaseGroupIconHitBox>emptyList(), 0, 0, 0,
                    0);
            }
            return;
        }
        interactionHitCache.setShowcaseGroupPopupState(popup.icons, popup.boxX, popup.boxY, popup.boxWidth,
            popup.boxHeight);
    }

    protected void drawShowcaseCaption(PonderScene scene, int previewX, int previewY, int previewWidth, int previewHeight,
        float currentTick, float fade) {
        getShowcaseCaptionRenderer().drawCaption(scene, previewX, previewY, previewWidth, previewHeight, currentTick,
            fade);
    }

    protected void drawGuiTextureOverlays(PonderScene scene, PreviewLayout layout, float currentTick, float fade) {
        getGuiOverlayRenderer().drawGuiTextureOverlays(scene, layout, currentTick, fade,
            overlayLayoutHelper::projectScenePoint);
    }

    protected void drawSceneSpaceOverlays(PonderScene scene, PreviewLayout layout, float currentTick, float fade) {
        if (scene == null || layout == null || fade <= 0.0F) {
            return;
        }

        PreviewBounds bounds = PonderScenePreview.computeBounds(scene);
        for (PonderScene.OverlayEvent overlayEvent : scene.getOverlayEvents()) {
            if (overlayEvent.getTick() > currentTick
                || currentTick > overlayEvent.getTick() + overlayEvent.getDuration()) {
                continue;
            }

            float overlayFade = PonderOverlayHelper.computeOverlayFade(overlayEvent.getTick(),
                overlayEvent.getDuration(), currentTick)
                * fade;
            if (overlayFade <= 0.0F) {
                continue;
            }

            if (overlayEvent.getType() == PonderScene.OverlayEventType.SCENE_OUTLINE) {
                getSceneOverlayRenderer().drawOutline(scene, bounds, layout, currentTick, overlayEvent, overlayFade);
            } else if (overlayEvent.getType() == PonderScene.OverlayEventType.SCENE_LINE) {
                getSceneOverlayRenderer().drawLine(scene, bounds, layout, currentTick, overlayEvent, overlayFade);
            } else if (overlayEvent.getType() == PonderScene.OverlayEventType.VALUE_BOX) {
                getSceneOverlayRenderer().drawValueBox(scene, bounds, layout, currentTick, overlayEvent, overlayFade);
            } else if (overlayEvent.getType() == PonderScene.OverlayEventType.CONTROLS) {
                getControlsOverlayRenderer().drawControlsOverlay(scene, bounds, layout, currentTick, overlayEvent, overlayFade);
            }
        }

        drawActorOverlay(scene, bounds, layout, currentTick, fade);
        getParticleOverlayRenderer().drawParticleEffects(scene, bounds, layout, currentTick, fade);
        getPoiOverlayRenderer().draw(scene, bounds, layout, currentTick, fade);
    }

    private void drawActorOverlay(PonderScene scene, PreviewBounds bounds, PreviewLayout layout, float currentTick,
        float fade) {
        List<PonderSceneRuntime.ActorRuntimeState> actors = PonderSceneRuntime.buildActorStates(scene, currentTick);
        List<ActorOverlayItem> items = ActorOverlayBuilder.buildItems(scene, bounds, layout, currentTick, actors,
            overlayLayoutHelper::projectScenePoint);
        getActorOverlayRenderer().drawActorOverlay(items, fade);
    }

    private int resolveManualCaptionCoordinate(int coordinate, int origin, int size, int boxSize) {
        return coordinate >= 0 ? origin + coordinate : origin + size - boxSize + coordinate;
    }

    protected void drawNextUpCard(PonderScene scene, float fade) {
        ShowcaseHudRenderer.NextUpCardBounds nextUpCard = getFallbackShowcaseHudRenderer().drawNextUpCardIfNeeded(scene,
            selectionState.getSelectedSceneIndex(), selectionState.getCompiledSceneCount(),
            selectionState.getCompiledScenes(), playbackState.getPlaybackTick(),
            scene == null ? 0 : getSceneEndTick(scene), nextSceneButton, fade, false);
        if (nextUpCard == null) {
            interactionHitCache.setNextUpCardBounds(0, 0, 0, 0);
        } else {
            interactionHitCache.setNextUpCardBounds(nextUpCard.x, nextUpCard.y, nextUpCard.width,
                nextUpCard.height);
        }
    }

    protected void drawShowcaseBackdrop(int previewX, int previewY, int previewWidth, int previewHeight, float fade) {
        getFallbackShowcaseChromeRenderer().drawBackdrop(this, previewX, previewY, previewWidth, previewHeight, fade);
    }

    protected void drawShowcaseLogo(int x, int y, float fade) {
        getFallbackShowcaseChromeRenderer().drawLogo(this, x, y, fade);
    }

    protected void drawShowcaseHoverHints(@Nullable String hoverLabel) {
        getFallbackShowcaseHudRenderer().drawHoverHints(width, height - 12, hoverLabel,
            buttonList.toArray(new GuiButton[0]));
    }

    @Nullable
    protected String computeShowcaseHoverLabel(int mouseX, int mouseY) {
        return ShowcaseHudRenderer.computeHoverLabel(mouseX, mouseY, getShowcaseHoverLabelHost(), getShowcaseHudTheme());
    }

    private void drawSpeechBox(int boxX, int boxY, int boxWidth, int boxHeight,
        SpeechRenderer.SpeechPointing pointing, int accentColor,
        float fade) {
        SpeechRenderer.drawSpeechBox(boxX, boxY, boxWidth, boxHeight, pointing, accentColor, fade);
    }

    private void drawCenteredStringNoShadow(String text, int centerX, int y, int color) {
        SpeechRenderer.drawCenteredStringNoShadow(fontRenderer, text, centerX, y, color);
    }

    private int blendColors(int baseColor, int accentColor, float accentWeight) {
        float clampedWeight = MathHelper.clamp(accentWeight, 0.0F, 1.0F);
        float baseWeight = 1.0F - clampedWeight;
        int red = Math.round(((baseColor >> 16) & 0xFF) * baseWeight + ((accentColor >> 16) & 0xFF) * clampedWeight);
        int green = Math.round(((baseColor >> 8) & 0xFF) * baseWeight + ((accentColor >> 8) & 0xFF) * clampedWeight);
        int blue = Math.round((baseColor & 0xFF) * baseWeight + (accentColor & 0xFF) * clampedWeight);
        return red << 16 | green << 8 | blue;
    }

    private int withAlpha(int color, float alpha) {
        int alphaChannel = MathHelper.clamp((int) alpha, 0, 255);
        return alphaChannel << 24 | (color & 0x00FFFFFF);
    }

    private SpeechRenderer.Point getSpeechPointerTip(int boxX, int boxY, int boxWidth, int boxHeight,
        SpeechRenderer.SpeechPointing pointing) {
        return SpeechRenderer.getSpeechPointerTip(boxX, boxY, boxWidth, boxHeight, pointing);
    }

    private void drawCaptionConnector(int startX, int startY, int endX, int endY, int color, float fade) {
        SpeechRenderer.drawCaptionConnector(startX, startY, endX, endY, color, fade);
    }

    private void drawLineSegment(int startX, int startY, int endX, int endY, int color, float width) {
        SpeechRenderer.drawLineSegment(startX, startY, endX, endY, color, width);
    }

    private ShowcaseCaption getShowcaseCaption(PonderScene scene, float currentTick) {
        if (scene == null) {
            return null;
        }
        ShowcaseCaption activeCaption = null;
        for (PonderScene.OverlayEvent overlayEvent : scene.getOverlayEvents()) {
            if ((overlayEvent.getType() == PonderScene.OverlayEventType.TEXT
                || overlayEvent.getType() == PonderScene.OverlayEventType.GUI_HIGHLIGHT)
                && overlayEvent.getTick() <= currentTick
                && currentTick <= overlayEvent.getTick() + overlayEvent.getDuration()
                && overlayEvent.getText() != null
                && !overlayEvent.getText().isEmpty()) {
                activeCaption = new ShowcaseCaption(overlayEvent);
            }
        }
        return activeCaption;
    }

    private float computeCaptionFade(ShowcaseCaption caption, float currentTick) {
        return caption == null || caption.overlayEvent == null ? 0.0F
            : PonderOverlayHelper.computeOverlayFade(caption.overlayEvent.getTick(),
                caption.overlayEvent.getDuration(), currentTick);
    }

    protected ShowcaseGroupModel getShowcaseGroupState() {
        return ShowcaseGroupHelper.computeFor(getSelectedComponentId());
    }

    protected ItemStack createComponentStack(ResourceLocation componentId) {
        return componentId == null ? ItemStack.EMPTY : PonderIndex.getSceneAccess().getDisplayStack(componentId);
    }

    private void drawOperationList(int mouseX, int mouseY, int x, int startY, int width, int bottom) {
        getDebugPanelRenderer().drawOperationList(mouseX, mouseY, x, startY, width, bottom, LINE_HEIGHT,
            playbackState.getPlaybackTick(), new DebugDrawContext() {
                @Override
                public void fillRect(int left, int top, int right, int bottom, int color) {
                    PonderDebugScreen.this.drawRect(left, top, right, bottom, color);
                }

                @Override
                public void drawString(String text, int drawX, int drawY, int color) {
                    PonderDebugScreen.this.drawString(fontRenderer, text, drawX, drawY, color);
                }
            });
    }

    private void drawTooltips(int mouseX, int mouseY) {
        getDebugPanelRenderer().drawTooltips(mouseX, mouseY, selectionState.getComponentIds(),
            OUTER_MARGIN,
            OUTER_MARGIN, LEFT_PANEL_WIDTH, OUTER_MARGIN + LEFT_PANEL_WIDTH + OUTER_MARGIN, OUTER_MARGIN,
            width - (OUTER_MARGIN + LEFT_PANEL_WIDTH + OUTER_MARGIN) - OUTER_MARGIN, LINE_HEIGHT,
            (textLines, hoverX, hoverY) -> drawHoveringText(textLines, hoverX, hoverY));
    }

    private int resolveInitialComponentIndex() {
        if (selectionState.getComponentIds().isEmpty()) {
            return 0;
        }

        if (requestedComponentId != null) {
            for (int i = 0; i < selectionState.getComponentIds().size(); i++) {
                if (requestedComponentId.equals(selectionState.getComponentIds().get(i))) {
                    return i;
                }
            }
        }

        return 0;
    }
    private void selectComponent(ResourceLocation componentId, int preferredSceneIndex) {
        if (componentId == null) {
            return;
        }
        for (int index = 0; index < selectionState.getComponentIds().size(); index++) {
            if (componentId.equals(selectionState.getComponentIds().get(index))) {
                sceneController.selectComponent(index, preferredSceneIndex);
                return;
            }
        }
    }

    protected void updateButtonState() {
        boolean hasScenes = selectionState.getCompiledSceneCount() > 0;
        if (prevSceneButton != null) {
            prevSceneButton.enabled = hasScenes && selectionState.getSelectedSceneIndex() > 0;
        }
        if (nextSceneButton != null) {
            nextSceneButton.enabled = hasScenes
                && selectionState.getSelectedSceneIndex() + 1 < selectionState.getCompiledSceneCount();
        }
        if (startButton != null) {
            startButton.enabled = hasScenes && (showcaseMode || playbackState.getPlaybackTick() > 0);
        }
        if (playPauseButton != null) {
            playPauseButton.enabled = hasScenes;
            playPauseButton.displayString = playbackState.isPlaying() ? "Pause" : "Play";
        }
        if (endButton != null) {
            endButton.enabled = hasScenes && playbackState.getPlaybackTick() < getSceneEndTick(getSelectedScene());
        }
        if (debugButton != null) {
            debugButton.enabled = hasScenes;
        }
    }

    protected ResourceLocation getSelectedComponentId() {
        return selectionState.getSelectedComponentId();
    }

    protected PonderScene getSelectedScene() {
        return selectionState.getSelectedScene();
    }

    private List<RecordedOperation> getSelectedRecordedOperations() {
        PonderScene scene = getSelectedScene();
        return scene == null ? Collections.<RecordedOperation>emptyList() : scene.getRecordedOperations();
    }

    protected int getSceneEndTick(PonderScene scene) {
        int maxTick = scene.getTotalIdleTicks();
        for (RecordedOperation operation : scene.getRecordedOperations()) {
            maxTick = Math.max(maxTick, operation.getTick());
        }
        for (WorldEvent event : scene.getWorldEvents()) {
            maxTick = Math.max(maxTick, event.getTick());
            if (event.getType() == PonderScene.WorldEventType.SHOW_SECTION
                || event.getType() == PonderScene.WorldEventType.HIDE_SECTION) {
                maxTick = Math.max(maxTick, event.getTick() + PonderSceneRuntime.SECTION_FADE_TICKS);
            }
        }
        for (PonderScene.CameraEvent event : scene.getCameraEvents()) {
            maxTick = Math.max(maxTick, event.getTick() + PonderSceneRuntime.CAMERA_ROTATE_TICKS);
        }
        return maxTick;
    }

    protected float getRenderTick(PonderScene scene, float partialTicks) {
        if (scene == null) {
            return 0.0F;
        }
        return playbackState.getRenderTick(getSceneEndTick(scene), partialTicks);
    }

    private int getActiveOperationIndex(List<RecordedOperation> operations, int currentTick) {
        return getDebugPanelRenderer().getActiveOperationIndex(operations, currentTick);
    }

    private RecordedOperation getActiveOperation(List<RecordedOperation> operations, int currentTick) {
        int activeIndex = getActiveOperationIndex(operations, currentTick);
        return activeIndex >= 0 && activeIndex < operations.size() ? operations.get(activeIndex) : null;
    }

    private WorldEvent getLatestWorldEvent(PonderScene scene, int currentTick) {
        WorldEvent latest = null;
        for (WorldEvent event : scene.getWorldEvents()) {
            if (event.getTick() > currentTick) {
                break;
            }
            latest = event;
        }
        return latest;
    }

    private String formatWorldEvent(WorldEvent event) {
        StringBuilder builder = new StringBuilder();
        builder.append("T+")
            .append(event.getTick())
            .append(' ')
            .append(event.getType().name().toLowerCase().replace('_', ' '))
            .append(" x")
            .append(event.getPositions().size());
        if (event.getDirection() != null) {
            builder.append(" @ ").append(event.getDirection().name().toLowerCase());
        }
        if (event.getStateDescription() != null && !event.getStateDescription().isEmpty()) {
            builder.append(" -> ").append(event.getStateDescription());
        }
        return builder.toString();
    }

    private void centerOperationsOnActiveLine() {
        if (getSelectedRecordedOperations().isEmpty()) {
            getDebugPanelRenderer().resetOperationScroll();
            return;
        }
        getDebugPanelRenderer().setOperationScroll(
            getDebugPanelRenderer().computeCenteredOperationScroll(playbackState.getPlaybackTick(), height));
    }

    private int getMaxComponentScroll() {
        return getDebugPanelRenderer().getMaxComponentScroll(selectionState.getComponentIds(), height);
    }

    private int getComponentIndexAt(int mouseX, int mouseY) {
        return getDebugPanelRenderer().getComponentIndexAt(mouseX, mouseY, height);
    }

    protected boolean hasShowcaseGroupChoices() {
        return ShowcaseGroupHelper.hasChoices(getShowcaseGroupState());
    }

    protected boolean isMouseOverPlaybackBar(int mouseX, int mouseY) {
        return interactionHitCache.isMouseOverPlaybackBar(mouseX, mouseY);
    }

    protected boolean isMouseOverNextUpCard(int mouseX, int mouseY) {
        InteractionHitCache.NextUpCardBounds bounds = interactionHitCache.getNextUpCardBounds();
        return bounds.width() > 0 && mouseX >= bounds.x() && mouseX <= bounds.x() + bounds.width()
            && mouseY >= bounds.y() && mouseY <= bounds.y() + bounds.height() + 8;
    }

    private void seekPlaybackToMouse(int mouseX) {
        sceneController.setPlaybackTick(estimatePlaybackTickForMouse(mouseX));
    }

    protected int estimatePlaybackTickForMouse(int mouseX) {
        PonderScene scene = getSelectedScene();
        if (scene == null) {
            return 0;
        }

        int maxTick = Math.max(1, getSceneEndTick(scene));
        return interactionHitCache.estimatePlaybackTickForMouse(mouseX, interactionHitCache.getPlaybackBarBounds(),
            maxTick);
    }

    protected boolean isMouseOverShowcaseHeaderIcon(int mouseX, int mouseY) {
        InteractionHitCache.ShowcaseHeaderIconBounds bounds = interactionHitCache.getShowcaseHeaderIconBounds();
        return bounds.size() > 0 && mouseX >= bounds.x() && mouseX <= bounds.x() + bounds.size()
            && mouseY >= bounds.y() && mouseY <= bounds.y() + bounds.size();
    }

    private int getOperationIndexAt(int mouseX, int mouseY) {
        return getDebugPanelRenderer().getOperationIndexAt(mouseX, mouseY, height);
    }

    private boolean isMouseOverComponentList(int mouseX, int mouseY) {
        return getDebugPanelRenderer().isMouseOverComponentList(mouseX, mouseY, height);
    }

    private boolean isMouseOverOperations(int mouseX, int mouseY) {
        return getDebugPanelRenderer().isMouseOverOperations(mouseX, mouseY, height);
    }

    private boolean isMouseOverPreview(int mouseX, int mouseY) {
        return interactionHitCache.isMouseOverPreview(mouseX, mouseY);
    }

    private boolean isWithin(int mouseX, int mouseY, int minX, int minY, int maxX, int maxY) {
        return mouseX >= minX && mouseX <= maxX && mouseY >= minY && mouseY <= maxY;
    }

    private String formatTags(Collection<PonderTag> tags) {
        if (tags.isEmpty()) {
            return "[]";
        }

        StringBuilder builder = new StringBuilder("[");
        boolean first = true;
        for (PonderTag tag : tags) {
            if (!first) {
                builder.append(", ");
            }
            first = false;
            builder.append(tag.getId());
        }
        builder.append(']');
        return builder.toString();
    }

    private void resetPreviewCamera() {
        previewCameraState.reset(showcaseMode);
    }

    private static final class ShowcaseCaption {
        private final PonderScene.OverlayEvent overlayEvent;

        private ShowcaseCaption(PonderScene.OverlayEvent overlayEvent) {
            this.overlayEvent = overlayEvent;
        }
    }

}
