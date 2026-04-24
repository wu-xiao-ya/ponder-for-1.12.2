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
import net.createmod.ponder.Ponder;
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
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
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
import org.lwjgl.opengl.GL11;

public class PonderDebugScreen extends CompatGuiScreen {

    private static final ResourceLocation SHOWCASE_WIDGETS_TEXTURE =
        Ponder.asResource("textures/gui/widgets.png");
    private static final ResourceLocation SHOWCASE_LOGO_TEXTURE =
        Ponder.asResource("textures/gui/logo.png");
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
    private static final int SHOWCASE_GROUP_POPUP_COLUMNS = 6;
    private static final int SHOWCASE_GROUP_POPUP_ICON_SIZE = 20;
    private static final int SHOWCASE_GROUP_POPUP_ICON_SPACING = 4;
    private static final int PREVIEW_FULL_BRIGHT = 0x00F000F0;
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
    private static final Method BLOCKSTATE_ACTUAL_STATE = resolveBlockStateMethod();

    @Nullable
    private final GuiScreen parentScreen;
    private final ResourceLocation requestedComponentId;
    private final int requestedSceneIndex;
    private final boolean showcaseMode;

    private List<ResourceLocation> componentIds = Collections.emptyList();
    private List<PonderScene> compiledScenes = Collections.emptyList();
    private int selectedComponentIndex;
    private int selectedSceneIndex;
    private int componentScroll;
    private int operationScroll;
    private int playbackTick;
    protected boolean playing;

    protected GuiButton prevSceneButton;
    protected GuiButton nextSceneButton;
    protected GuiButton playPauseButton;
    protected GuiButton startButton;
    protected GuiButton endButton;
    protected GuiButton debugButton;
    protected PreviewLayout lastPreviewLayout;
    private float previewYaw = -45.0F;
    private float previewPitch = 28.0F;
    private float previewZoom = 1.0F;
    private boolean previewDragging;
    private int previewDragMouseX;
    private int previewDragMouseY;
    private int showcaseFadeTicks;
    private final Map<String, TileEntity> tileEntityPreviewCache = new LinkedHashMap<String, TileEntity>();
    private List<ShowcaseGroupIcon> lastShowcaseGroupIcons = Collections.emptyList();
    private boolean showcaseGroupSelectorOpen;
    private int lastShowcaseHeaderIconX;
    private int lastShowcaseHeaderIconY;
    private int lastShowcaseHeaderIconSize;
    private int lastShowcaseGroupPopupX;
    private int lastShowcaseGroupPopupY;
    private int lastShowcaseGroupPopupWidth;
    private int lastShowcaseGroupPopupHeight;
    private int lastPlaybackBarX;
    private int lastPlaybackBarY;
    private int lastPlaybackBarWidth;
    private int lastPlaybackBarHeight;
    private boolean playbackBarDragging;
    private int lastNextUpCardX;
    private int lastNextUpCardY;
    private int lastNextUpCardWidth;
    private int lastNextUpCardHeight;

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
        return new PonderDebugScreen(getSelectedComponentId(), selectedSceneIndex, this);
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

    protected void setShowcaseHeaderIconBounds(int x, int y, int size) {
        lastShowcaseHeaderIconX = x;
        lastShowcaseHeaderIconY = y;
        lastShowcaseHeaderIconSize = size;
    }

    protected void resetShowcaseTransientState() {
        lastPreviewLayout = null;
        lastShowcaseGroupIcons = Collections.emptyList();
        lastShowcaseGroupPopupWidth = 0;
        lastShowcaseGroupPopupHeight = 0;
        lastShowcaseHeaderIconSize = 0;
        lastNextUpCardWidth = 0;
        lastNextUpCardHeight = 0;
    }

    protected float getShowcaseFade(float partialTicks) {
        return MathHelper.clamp((showcaseFadeTicks + partialTicks) / 20.0F, 0.0F, 1.0F);
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
        playing = true;
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

    @Override
    public void initGui() {
        super.initGui();
        resetPreviewCamera();
        componentIds = getRegisteredComponents();
        selectedComponentIndex = resolveInitialComponentIndex();
        reloadCurrentComponent(false);

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
            selectScene(selectedSceneIndex - 1);
        } else if (button.id == BUTTON_NEXT_SCENE) {
            selectScene(selectedSceneIndex + 1);
        } else if (button.id == BUTTON_START) {
            setPlaybackTick(0);
            playing = showcaseMode;
        } else if (button.id == BUTTON_PLAY_PAUSE) {
            playing = !playing;
        } else if (button.id == BUTTON_END) {
            PonderScene scene = getSelectedScene();
            setPlaybackTick(scene == null ? 0 : getSceneEndTick(scene));
            playing = false;
        } else if (button.id == BUTTON_DEBUG) {
            mc.displayGuiScreen(createDebugScreenFromShowcase());
            return;
        } else if (button.id == BUTTON_RELOAD) {
            PonderIndex.reload();
            componentIds = getRegisteredComponents();
            selectedComponentIndex = MathHelper.clamp(selectedComponentIndex, 0, Math.max(0, componentIds.size() - 1));
            reloadCurrentComponent(true);
        } else if (button.id == BUTTON_CLOSE) {
            mc.displayGuiScreen(parentScreen);
        }
        updateButtonState();
    }

    @Override
    public void updateScreen() {
        if (showcaseMode && showcaseFadeTicks < 20) {
            showcaseFadeTicks++;
        }

        if (!playing) {
            return;
        }

        PonderScene scene = getSelectedScene();
        if (scene == null) {
            playing = false;
            updateButtonState();
            return;
        }

        int maxTick = getSceneEndTick(scene);
        if (playbackTick < maxTick) {
            setPlaybackTick(playbackTick + 1);
        } else if (showcaseMode) {
            setPlaybackTick(0);
        } else {
            playing = false;
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
        int delta = wheel > 0 ? -1 : 1;

        if (showcaseMode) {
            if (isMouseOverPreview(mouseX, mouseY)) {
                if (isShiftKeyDown()) {
                    stepPlaybackTick(delta < 0 ? -1 : 1);
                } else {
                    float zoomStep = wheel > 0 ? 0.12F : -0.12F;
                    previewZoom = MathHelper.clamp(previewZoom + zoomStep, 0.45F, 2.4F);
                }
            }
            return;
        }

        if (isMouseOverComponentList(mouseX, mouseY)) {
            componentScroll = MathHelper.clamp(componentScroll + delta, 0, getMaxComponentScroll());
        } else if (isMouseOverOperations(mouseX, mouseY)) {
            operationScroll = MathHelper.clamp(operationScroll + delta * 3, 0, getMaxOperationScroll());
        } else if (isMouseOverPreview(mouseX, mouseY)) {
            if (isShiftKeyDown()) {
                stepPlaybackTick(delta < 0 ? -1 : 1);
            } else {
                float zoomStep = wheel > 0 ? 0.12F : -0.12F;
                previewZoom = MathHelper.clamp(previewZoom + zoomStep, 0.45F, 2.4F);
            }
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (mouseButton != 0) {
            return;
        }

        if (isMouseOverPlaybackBar(mouseX, mouseY)) {
            playbackBarDragging = true;
            seekPlaybackToMouse(mouseX);
            playing = false;
            updateButtonState();
            return;
        }

        if (showcaseMode) {
            ShowcaseGroupIcon groupIcon = getShowcaseGroupIconAt(mouseX, mouseY);
            if (groupIcon != null) {
                showcaseGroupSelectorOpen = false;
                selectComponent(groupIcon.componentId, 0);
                return;
            }
            if (isMouseOverNextUpCard(mouseX, mouseY)) {
                selectScene(selectedSceneIndex + 1);
                updateButtonState();
                return;
            }
            if (isMouseOverShowcaseHeaderIcon(mouseX, mouseY) && hasShowcaseGroupChoices()) {
                showcaseGroupSelectorOpen = !showcaseGroupSelectorOpen;
                return;
            }
            if (showcaseGroupSelectorOpen && !isWithin(mouseX, mouseY, lastShowcaseGroupPopupX, lastShowcaseGroupPopupY,
                lastShowcaseGroupPopupX + lastShowcaseGroupPopupWidth, lastShowcaseGroupPopupY + lastShowcaseGroupPopupHeight)) {
                showcaseGroupSelectorOpen = false;
            }
            if (isMouseOverPreview(mouseX, mouseY)) {
                previewDragging = true;
                previewDragMouseX = mouseX;
                previewDragMouseY = mouseY;
            }
            return;
        }

        int componentIndex = getComponentIndexAt(mouseX, mouseY);
        if (componentIndex >= 0) {
            selectComponent(componentIndex, 0);
            return;
        }

        int operationIndex = getOperationIndexAt(mouseX, mouseY);
        if (operationIndex >= 0) {
            List<RecordedOperation> operations = getSelectedRecordedOperations();
            setPlaybackTick(operations.get(operationIndex).getTick());
            playing = false;
            updateButtonState();
            return;
        }

        if (isMouseOverPreview(mouseX, mouseY)) {
            previewDragging = true;
            previewDragMouseX = mouseX;
            previewDragMouseY = mouseY;
        }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);

        if (playbackBarDragging && clickedMouseButton == 0) {
            seekPlaybackToMouse(mouseX);
            playing = false;
            updateButtonState();
            return;
        }

        if (!previewDragging || clickedMouseButton != 0 || !isMouseOverPreview(mouseX, mouseY)) {
            return;
        }

        int deltaX = mouseX - previewDragMouseX;
        int deltaY = mouseY - previewDragMouseY;
        previewYaw = MathHelper.wrapDegrees(previewYaw + deltaX * 0.75F);
        previewPitch = MathHelper.clamp(previewPitch + deltaY * 0.6F, -75.0F, 75.0F);
        previewDragMouseX = mouseX;
        previewDragMouseY = mouseY;
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        if (state == 0) {
            previewDragging = false;
            playbackBarDragging = false;
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(parentScreen);
            return;
        }

        if (showcaseMode) {
            if (keyCode == Keyboard.KEY_SPACE) {
                playing = !playing;
                updateButtonState();
                return;
            }

            if (keyCode == Keyboard.KEY_R) {
                PonderIndex.reload();
                componentIds = getRegisteredComponents();
                selectedComponentIndex = MathHelper.clamp(selectedComponentIndex, 0,
                    Math.max(0, componentIds.size() - 1));
                reloadCurrentComponent(true);
                updateButtonState();
                return;
            }

            if (keyCode == Keyboard.KEY_LEFT) {
                selectScene(selectedSceneIndex - 1);
                updateButtonState();
                return;
            }

            if (keyCode == Keyboard.KEY_RIGHT) {
                selectScene(selectedSceneIndex + 1);
                updateButtonState();
                return;
            }

            if (keyCode == Keyboard.KEY_UP) {
                stepPlaybackTick(-1);
                return;
            }

            if (keyCode == Keyboard.KEY_DOWN) {
                stepPlaybackTick(1);
                return;
            }

            if (keyCode == Keyboard.KEY_D && allowDebugShortcutFromShowcase()) {
                mc.displayGuiScreen(createDebugScreenFromShowcase());
                return;
            }

            super.keyTyped(typedChar, keyCode);
            return;
        }

        if (keyCode == Keyboard.KEY_SPACE) {
            playing = !playing;
            updateButtonState();
            return;
        }

        if (keyCode == Keyboard.KEY_R) {
            PonderIndex.reload();
            componentIds = getRegisteredComponents();
            selectedComponentIndex = MathHelper.clamp(selectedComponentIndex, 0, Math.max(0, componentIds.size() - 1));
            reloadCurrentComponent(true);
            updateButtonState();
            return;
        }

        if (keyCode == Keyboard.KEY_LEFT) {
            selectScene(selectedSceneIndex - 1);
            updateButtonState();
            return;
        }

        if (keyCode == Keyboard.KEY_RIGHT) {
            selectScene(selectedSceneIndex + 1);
            updateButtonState();
            return;
        }

        if (keyCode == Keyboard.KEY_UP) {
            stepPlaybackTick(-1);
            return;
        }

        if (keyCode == Keyboard.KEY_DOWN) {
            stepPlaybackTick(1);
            return;
        }

        if (keyCode == Keyboard.KEY_PRIOR) {
            stepPlaybackTick(-20);
            return;
        }

        if (keyCode == Keyboard.KEY_NEXT) {
            stepPlaybackTick(20);
            return;
        }

        if (keyCode == Keyboard.KEY_HOME) {
            setPlaybackTick(0);
            playing = false;
            updateButtonState();
            return;
        }

        if (keyCode == Keyboard.KEY_END) {
            PonderScene scene = getSelectedScene();
            setPlaybackTick(scene == null ? 0 : getSceneEndTick(scene));
            playing = false;
            updateButtonState();
            return;
        }

        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    protected void drawShowcaseScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        lastPreviewLayout = null;
        lastShowcaseGroupIcons = Collections.emptyList();
        lastShowcaseGroupPopupWidth = 0;
        lastShowcaseGroupPopupHeight = 0;
        lastShowcaseHeaderIconSize = 0;
        lastNextUpCardWidth = 0;
        lastNextUpCardHeight = 0;

        PreviewLayout showcaseLayout = getShowcasePreviewLayout();
        int previewX = showcaseLayout.originX;
        int previewY = showcaseLayout.originY;
        int previewWidth = showcaseLayout.width;
        int previewHeight = showcaseLayout.height;
        int headerX = previewX + 40;
        int headerY = previewY + 18;
        float fade = MathHelper.clamp((showcaseFadeTicks + partialTicks) / 20.0F, 0.0F, 1.0F);

        drawGradientRect(0, 0, width, height, 0xF306090D, 0xFF010205);

        PonderScene scene = getSelectedScene();
        ResourceLocation componentId = getSelectedComponentId();
        ItemStack componentStack = createComponentStack(componentId);
        String stackLabel = componentStack.isEmpty() ? "\u672A\u627E\u5230\u5BF9\u5E94\u7269\u54C1" : componentStack.getDisplayName();
        String sceneTitle = scene == null ? getShowcaseEmptyTitle() : scene.getTitle();
        String title = fontRenderer.trimStringToWidth(sceneTitle, 240);
        String subtitle = componentId == null ? getShowcaseNoSceneSubtitle() : stackLabel;

        drawScenePreview(previewX, previewY, previewWidth, previewHeight, partialTicks);
        drawShowcaseBackdrop(previewX, previewY, previewWidth, previewHeight, fade);
        drawGuiTextureOverlays(scene, lastPreviewLayout, getRenderTick(scene, partialTicks), fade);
        drawSceneSpaceOverlays(scene, lastPreviewLayout, getRenderTick(scene, partialTicks), fade);
        drawShowcaseHeader(headerX, headerY, previewWidth - 80, componentStack, title, subtitle, fade);
        drawShowcaseGroupPopup(headerX - 2, headerY + 48, previewWidth - 96, fade);
        drawShowcaseLogo(previewX + previewWidth - 46, previewY + 12, fade);
        int playbackWidth = Math.min(SHOWCASE_BAR_WIDTH, previewWidth - 150);
        int playbackX = previewX + (previewWidth - playbackWidth) / 2;
        int playbackY = previewY + previewHeight - SHOWCASE_BAR_BOTTOM_OFFSET;
        drawPlaybackBar(playbackX, playbackY, playbackWidth, 3);
        drawShowcaseCaption(scene, previewX, previewY, previewWidth, previewHeight, getRenderTick(scene, partialTicks),
            fade);
        drawNextUpCard(scene, fade);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (showcaseMode) {
            drawShowcaseScreen(mouseX, mouseY, partialTicks);
            super.drawScreen(mouseX, mouseY, partialTicks);
            drawShowcaseHoverHints(mouseX, mouseY);
            return;
        }

        drawDefaultBackground();
        lastPreviewLayout = null;
        lastNextUpCardWidth = 0;
        lastNextUpCardHeight = 0;

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
        drawScenePreview(previewPanelX, rightPanelY + 18, PREVIEW_PANEL_WIDTH, PREVIEW_PANEL_HEIGHT, partialTicks);
        drawGuiTextureOverlays(getSelectedScene(), lastPreviewLayout, getRenderTick(getSelectedScene(), partialTicks), 1.0F);
        drawSceneSpaceOverlays(getSelectedScene(), lastPreviewLayout, getRenderTick(getSelectedScene(), partialTicks),
            1.0F);
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
        int visibleLines = Math.max(1, (bottom - startY - 6) / LINE_HEIGHT);
        int maxIndex = Math.min(componentIds.size(), componentScroll + visibleLines);

        for (int index = componentScroll; index < maxIndex; index++) {
            int y = startY + (index - componentScroll) * LINE_HEIGHT;
            boolean selected = index == selectedComponentIndex;
            boolean hovered = isWithin(mouseX, mouseY, x + 4, y - 1, x + LEFT_PANEL_WIDTH - 4, y + LINE_HEIGHT - 1);
            int background = selected ? 0xCC304060 : hovered ? 0x66303030 : 0x33202020;
            drawRect(x + 4, y - 1, x + LEFT_PANEL_WIDTH - 4, y + LINE_HEIGHT - 1, background);

            String label = fontRenderer.trimStringToWidth(componentIds.get(index).toString(), LEFT_PANEL_WIDTH - 12);
            drawString(fontRenderer, label, x + 8, y + 1, selected ? 0xFFFFFF : 0xD0D0D0);
        }
    }

    private void drawSceneSummary(int x, int y, int width) {
        PonderScene scene = getSelectedScene();
        ResourceLocation componentId = getSelectedComponentId();

        if (componentId == null) {
            drawString(fontRenderer, "No Ponder components registered.", x + 6, y, 0xFF8888);
            return;
        }

        List<String> lines = new ArrayList<String>();
        lines.add("Component: " + componentId);

        if (scene == null) {
            lines.add("Scene: none");
            lines.add("Tip: use /ponder reload if you just changed registrations.");
        } else {
            PreviewBounds bounds = PonderScenePreview.computeBounds(scene);
            PreviewState previewState = PonderScenePreview.buildState(scene, playbackTick);
            RecordedOperation activeOperation = getActiveOperation(scene.getRecordedOperations(), playbackTick);
            WorldEvent latestWorldEvent = getLatestWorldEvent(scene, playbackTick);
            lines.add("Scene: " + (selectedSceneIndex + 1) + "/" + compiledScenes.size() + "  id=" + scene.getSceneId());
            lines.add("Title: " + scene.getTitle());
            lines.add("Schematic: " + scene.getSchematicLocation());
            lines.add("Ticks: " + playbackTick + "/" + getSceneEndTick(scene) + "  status=" + (playing ? "playing" : "paused"));
            lines.add("Active op: " + (activeOperation == null ? "none yet" : activeOperation.getDescription()));
            lines.add("Latest world: " + (latestWorldEvent == null ? "none yet" : formatWorldEvent(latestWorldEvent)));
            lines.add("Ops: " + scene.getRecordedOperations().size() + "  worldEvents=" + scene.getWorldEvents().size());
            lines.add("Visible blocks: " + previewState.visibleBlocks + "  columns=" + previewState.columnsWithBlocks);
            lines.add("Bounds: x " + bounds.minX + ".." + bounds.maxX + "  z " + bounds.minZ + ".." + bounds.maxZ
                + "  y " + bounds.minY + ".." + bounds.maxY);
            lines.add("Camera Y: " + Math.round(PonderSceneRuntime.getCameraYaw(scene, playbackTick)) + " / "
                + Math.round(scene.getAccumulatedCameraYaw()) + "  scale=" + scene.getScaleFactor());
            lines.add("Keyframes: " + scene.getKeyframeCount() + "  lazy=" + scene.getLazyKeyframeCount()
                + "  finished=" + scene.isFinished());
            lines.add("Tags: " + formatTags(PonderIndex.getTagAccess().getTags(componentId)));
        }

        for (int i = 0; i < lines.size(); i++) {
            drawString(fontRenderer, fontRenderer.trimStringToWidth(lines.get(i), width - 12), x + 6, y + i * 12,
                0xE0E0E0);
        }
    }

    protected void drawScenePreview(int x, int y, int width, int height, float partialTicks) {
        if (showcaseMode) {
            lastPreviewLayout = new PreviewLayout(x + 1, y + 1, width - 2, height - 2);
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
            lastPreviewLayout = new PreviewLayout(viewportX + 1, viewportY + 1, viewportWidth - 2, viewportHeight - 2);
        }

        PonderScene scene = getSelectedScene();
        if (scene == null) {
            drawCenteredString(fontRenderer, showcaseMode ? "\u6CA1\u6709\u53EF\u64AD\u653E\u7684\u573A\u666F" : getNoSceneLabel(),
                x + width / 2, y + height / 2 - 4, 0xA0A0A0);
            return;
        }

        PreviewBounds bounds = PonderScenePreview.computeBounds(scene);
        float renderTick = getRenderTick(scene, partialTicks);
        PonderSceneRuntime.RuntimeState runtimeState = PonderSceneRuntime.buildState(scene, renderTick);

        if (runtimeState.visibleBlocks > 0) {
            renderScenePreview(scene, bounds, runtimeState, lastPreviewLayout, renderTick);
        } else {
            drawCenteredString(fontRenderer,
                showcaseMode ? "\u5F53\u524D\u65F6\u95F4\u70B9\u6CA1\u6709\u53EF\u89C1\u65B9\u5757" : getNoVisibleBlocksLabel(),
                x + width / 2, y + height / 2 - 6, 0xA0A0A0);
            drawCenteredString(fontRenderer,
                showcaseMode ? "\u64AD\u653E\u5230\u540E\u7EED\u65F6\u95F4\u540E\u4F1A\u51FA\u73B0\u5C55\u793A\u5185\u5BB9"
                    : getNoVisibleBlocksHint(), x + width / 2, y + height / 2 + 8, 0x707070);
        }

        if (!showcaseMode) {
            String stateLine = "Blocks " + runtimeState.visibleBlocks + "  zoom "
                + Math.round(previewZoom * 100.0F) + "%  drag orbit";
            drawString(fontRenderer, fontRenderer.trimStringToWidth(stateLine, width - 12), x + 6, y + height - 18,
                0xC0C0C0);
            drawString(fontRenderer, "wheel zoom  Shift+wheel tick", x + 6, y + height - 8, 0xA0A0A0);
        }
    }

    protected void drawPlaybackBar(int x, int y, int width, int height) {
        lastPlaybackBarX = x;
        lastPlaybackBarY = y;
        lastPlaybackBarWidth = width;
        lastPlaybackBarHeight = height;

        PonderScene scene = getSelectedScene();
        int maxTick = scene == null ? 0 : Math.max(1, getSceneEndTick(scene));
        float progress = scene == null ? 0.0F : playbackTick / (float) maxTick;
        int filled = MathHelper.clamp((int) (progress * width), 0, width);

        if (showcaseMode) {
            drawRect(x, y, x + width, y + height, 0x44101518);
            drawRect(x, y, x + filled, y + height, 0xFFD9D1BF);
            if (filled > 0) {
                drawRect(x + filled - 1, y - 1, x + filled, y + height + 1, 0xFFFDFBF4);
            }

            if (scene != null) {
                String leftLabel = "\u573A\u666F " + (selectedSceneIndex + 1) + " / " + compiledScenes.size();
                String rightLabel = playbackTick + " / " + getSceneEndTick(scene);
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
            : "T+" + playbackTick + " / " + getSceneEndTick(scene) + (playing ? "   looping" : "   paused");
        drawCenteredString(fontRenderer, label, x + width / 2, y - 11, 0xD8E4E8);
    }

    protected void drawShowcaseHeader(int x, int y, int width, ItemStack componentStack, String title, String subtitle,
        float fade) {
        int boxWidth = Math.min(326, width);
        int boxHeight = 42;
        int alpha = (int) (fade * 185.0F) << 24;
        drawGradientRect(x, y, x + boxWidth, y + boxHeight, alpha | 0x141920, 0x00000000);
        drawGradientRect(x + boxWidth - 26, y, x + boxWidth, y + boxHeight, 0x00000000, alpha | 0x0E1218);
        drawRect(x + 34, y + boxHeight - 1, x + boxWidth - 12, y + boxHeight, ((int) (fade * 120.0F) << 24) | 0xD5CCB8);
        drawRect(x - 2, y + 4, x + 28, y + 34, alpha | 0x10151B);
        drawRect(x - 1, y + 5, x + 27, y + 33, ((int) (fade * 64.0F) << 24) | 0xE7E0D1);
        lastShowcaseHeaderIconX = x - 1;
        lastShowcaseHeaderIconY = y + 5;
        lastShowcaseHeaderIconSize = 28;

        if (!componentStack.isEmpty()) {
            RenderHelper.enableGUIStandardItemLighting();
            mc.getRenderItem().renderItemAndEffectIntoGUI(componentStack, x + 5, y + 11);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableLighting();
        }

        ShowcaseGroupState groupState = getShowcaseGroupState();
        if (groupState != null && groupState.components.size() > 1) {
            int chipX = x + 16;
            int chipY = y + 22;
            int chipFill = ((int) (fade * 188.0F) << 24) | 0x1A2028;
            int chipBorder = ((int) (fade * 156.0F) << 24) | 0xD5CCB8;
            drawRect(chipX, chipY, chipX + 12, chipY + 10, chipBorder);
            drawRect(chipX + 1, chipY + 1, chipX + 11, chipY + 9, chipFill);
            drawCenteredStringNoShadow(String.valueOf(groupState.components.size()), chipX + 6, chipY + 2, 0xF2EFE7);
        }

        String hint = "\u601D\u7D22";
        int sceneCountColor = ((int) (fade * 255.0F) << 24) | 0xC8D0D8;
        drawString(fontRenderer, hint, x + 36, y + 5, 0xB8C3CC);
        drawString(fontRenderer, fontRenderer.trimStringToWidth(title, boxWidth - 90), x + 36, y + 17, 0xF6F2EA);
        drawString(fontRenderer, fontRenderer.trimStringToWidth(subtitle, boxWidth - 90), x + 36, y + 29, 0xAEB8C1);
        if (!compiledScenes.isEmpty()) {
            String sceneIndex = (selectedSceneIndex + 1) + " / " + compiledScenes.size();
            drawString(fontRenderer, sceneIndex, x + boxWidth - 12 - fontRenderer.getStringWidth(sceneIndex), y + 6,
                sceneCountColor);
        }
    }

    protected void drawShowcaseGroupPopup(int x, int y, int maxWidth, float fade) {
        ShowcaseGroupState groupState = getShowcaseGroupState();
        if (groupState == null || groupState.components.size() <= 1 || !showcaseGroupSelectorOpen || maxWidth < 120) {
            if (!showcaseGroupSelectorOpen) {
                lastShowcaseGroupIcons = Collections.emptyList();
            }
            return;
        }

        String label = "\u5206\u7EC4\u9009\u62E9  " + groupState.tag.getTitle();
        int columns = Math.min(groupState.components.size(), Math.max(1,
            Math.min(SHOWCASE_GROUP_POPUP_COLUMNS,
                (maxWidth - 20) / (SHOWCASE_GROUP_POPUP_ICON_SIZE + SHOWCASE_GROUP_POPUP_ICON_SPACING))));
        int rows = Math.max(1, (int) Math.ceil(groupState.components.size() / (float) columns));
        int boxWidth = 16 + columns * SHOWCASE_GROUP_POPUP_ICON_SIZE
            + Math.max(0, columns - 1) * SHOWCASE_GROUP_POPUP_ICON_SPACING;
        int boxHeight = 28 + rows * SHOWCASE_GROUP_POPUP_ICON_SIZE
            + Math.max(0, rows - 1) * SHOWCASE_GROUP_POPUP_ICON_SPACING + 10;
        int boxX = MathHelper.clamp(lastShowcaseHeaderIconX - 6, x, x + Math.max(0, maxWidth - boxWidth));
        int boxY = y;
        int alpha = (int) (fade * 164.0F) << 24;
        drawGradientRect(boxX, boxY, boxX + boxWidth, boxY + boxHeight, alpha | 0x12181E, alpha | 0x0A0E14);
        drawRect(boxX, boxY, boxX + boxWidth, boxY + 1, ((int) (fade * 120.0F) << 24) | 0xE7E0D1);
        drawRect(boxX, boxY + boxHeight - 1, boxX + boxWidth, boxY + boxHeight,
            ((int) (fade * 96.0F) << 24) | 0xD5CCB8);
        drawRect(boxX, boxY, boxX + 1, boxY + boxHeight, ((int) (fade * 90.0F) << 24) | 0xE7E0D1);
        drawRect(boxX + boxWidth - 1, boxY, boxX + boxWidth, boxY + boxHeight, ((int) (fade * 84.0F) << 24) | 0x2B323A);
        drawString(fontRenderer, fontRenderer.trimStringToWidth(label, boxWidth - 16), boxX + 8, boxY + 8, 0xC8D0D8);

        List<ShowcaseGroupIcon> icons = new ArrayList<ShowcaseGroupIcon>();
        int startX = boxX + 8;
        int startY = boxY + 22;
        for (int index = 0; index < groupState.components.size(); index++) {
            ResourceLocation componentId = groupState.components.get(index);
            ItemStack stack = createComponentStack(componentId);
            int iconX = startX + (index % columns) * (SHOWCASE_GROUP_POPUP_ICON_SIZE + SHOWCASE_GROUP_POPUP_ICON_SPACING);
            int iconY = startY + (index / columns) * (SHOWCASE_GROUP_POPUP_ICON_SIZE + SHOWCASE_GROUP_POPUP_ICON_SPACING);
            boolean selected = componentId.equals(getSelectedComponentId());
            int borderColor = selected ? ((int) (fade * 255.0F) << 24) | 0xE7E0D1 : ((int) (fade * 140.0F) << 24) | 0x596570;
            int fillColor = selected ? ((int) (fade * 188.0F) << 24) | 0x1A2028 : ((int) (fade * 120.0F) << 24) | 0x10151B;
            drawRect(iconX, iconY, iconX + SHOWCASE_GROUP_POPUP_ICON_SIZE, iconY + SHOWCASE_GROUP_POPUP_ICON_SIZE, borderColor);
            drawRect(iconX + 1, iconY + 1, iconX + SHOWCASE_GROUP_POPUP_ICON_SIZE - 1,
                iconY + SHOWCASE_GROUP_POPUP_ICON_SIZE - 1, fillColor);

            if (!stack.isEmpty()) {
                RenderHelper.enableGUIStandardItemLighting();
                mc.getRenderItem().renderItemAndEffectIntoGUI(stack, iconX + 2, iconY + 2);
                RenderHelper.disableStandardItemLighting();
                GlStateManager.disableLighting();
            }
            icons.add(new ShowcaseGroupIcon(componentId, groupState.tag, iconX, iconY, SHOWCASE_GROUP_POPUP_ICON_SIZE,
                SHOWCASE_GROUP_POPUP_ICON_SIZE));
        }

        lastShowcaseGroupIcons = icons;
        lastShowcaseGroupPopupX = boxX;
        lastShowcaseGroupPopupY = boxY;
        lastShowcaseGroupPopupWidth = boxWidth;
        lastShowcaseGroupPopupHeight = boxHeight;
    }

    protected void drawShowcaseCaption(PonderScene scene, int previewX, int previewY, int previewWidth, int previewHeight,
        float currentTick, float fade) {
        ShowcaseCaption caption = getShowcaseCaption(scene, currentTick);
        if (caption == null || caption.overlayEvent == null || caption.overlayEvent.getText() == null
            || caption.overlayEvent.getText().isEmpty()) {
            return;
        }

        PonderScene.OverlayEvent overlayEvent = caption.overlayEvent;
        float captionFade = computeCaptionFade(caption, currentTick) * fade;
        if (captionFade <= 0.0F) {
            return;
        }

        int maxTextWidth = Math.min(250, previewWidth - 92);
        List<String> lines = fontRenderer.listFormattedStringToWidth(overlayEvent.getText(), maxTextWidth);
        int longestLine = 0;
        for (String line : lines) {
            longestLine = Math.max(longestLine, fontRenderer.getStringWidth(line));
        }

        int boxWidth = Math.max(106, longestLine + 22);
        int boxHeight = 12 + lines.size() * 10;
        List<GuiOverlayPlacement> guiPlacements = getActiveGuiOverlayPlacements(scene, lastPreviewLayout, currentTick);
        List<GuiHighlightPlacement> guiHighlightPlacements =
            getActiveGuiHighlightPlacements(scene, lastPreviewLayout, currentTick, guiPlacements);
        Point targetPoint =
            resolveCaptionTargetPoint(scene, currentTick, overlayEvent, guiHighlightPlacements);
        int boxX;
        int boxY;
        SpeechPointing pointing = SpeechPointing.NONE;
        boolean manualX = overlayEvent.getCaptionX() != Integer.MIN_VALUE;
        boolean manualY = overlayEvent.getCaptionY() != Integer.MIN_VALUE;
        boolean manualPlacement = manualX || manualY;
        int maxBoxX = Math.max(previewX + SHOWCASE_CAPTION_SIDE_CLEARANCE,
            previewX + previewWidth - boxWidth - SHOWCASE_CAPTION_SIDE_CLEARANCE);
        int bottomClearance = targetPoint == null ? SHOWCASE_CAPTION_BOTTOM_CLEARANCE : 116;
        int minBoxY = previewY + SHOWCASE_CAPTION_TOP_CLEARANCE;
        int autoMaxBoxY = Math.max(minBoxY,
            previewY + previewHeight - boxHeight - bottomClearance);
        int manualMaxBoxY = Math.max(minBoxY, previewY + previewHeight - boxHeight - 12);

        if (targetPoint != null) {
            int preferredX = targetPoint.x - boxWidth / 2;
            boolean preferAbove = targetPoint.y >= previewY + Math.round(previewHeight * 0.58F);
            int preferredAboveY = targetPoint.y - boxHeight - (preferAbove ? 20 : 14);
            int preferredBelowY = targetPoint.y + (preferAbove ? 18 : 14);
            boolean canPlaceAbove = preferredAboveY >= minBoxY;
            boolean canPlaceBelow = !preferAbove && preferredBelowY <= autoMaxBoxY;

            boxX = MathHelper.clamp(preferredX, previewX + SHOWCASE_CAPTION_SIDE_CLEARANCE, maxBoxX);
            if ((preferAbove && canPlaceAbove) || !canPlaceBelow) {
                boxY = MathHelper.clamp(preferredAboveY, minBoxY, autoMaxBoxY);
                pointing = SpeechPointing.DOWN;
            } else {
                boxY = MathHelper.clamp(preferredBelowY, minBoxY, autoMaxBoxY);
                pointing = SpeechPointing.UP;
            }
        } else {
            boxX = previewX + (previewWidth - boxWidth) / 2;
            int preferredY = overlayEvent.getIndependentY() >= 0
                ? previewY + 16 + overlayEvent.getIndependentY() * 2
                : autoMaxBoxY;
            boxY = MathHelper.clamp(preferredY, minBoxY, autoMaxBoxY);
        }

        if (manualX) {
            boxX = resolveManualCaptionCoordinate(overlayEvent.getCaptionX(), previewX, previewWidth, boxWidth);
        }
        if (manualY) {
            boxY = resolveManualCaptionCoordinate(overlayEvent.getCaptionY(), previewY, previewHeight, boxHeight);
        }
        boxX += overlayEvent.getCaptionOffsetX();
        boxY += overlayEvent.getCaptionOffsetY();

        int maxBoxY = manualPlacement ? manualMaxBoxY : autoMaxBoxY;
        boxX = MathHelper.clamp(boxX, previewX + SHOWCASE_CAPTION_SIDE_CLEARANCE, maxBoxX);
        boxY = MathHelper.clamp(boxY, minBoxY, maxBoxY);

        if (!manualPlacement) {
            int slide = (int) ((1.0F - captionFade) * 8.0F);
            boxY = MathHelper.clamp(boxY + slide, minBoxY, maxBoxY);
            CaptionPlacement adjustedPlacement =
                avoidGuiOverlayOverlap(boxX, boxY, boxWidth, boxHeight, pointing, previewY, maxBoxY, guiPlacements);
            boxX = adjustedPlacement.x;
            boxY = adjustedPlacement.y;
            pointing = adjustedPlacement.pointing;
        }

        if (!overlayEvent.isConnectorVisible() || targetPoint == null) {
            pointing = SpeechPointing.NONE;
        } else if (manualPlacement) {
            pointing = choosePointing(boxX, boxY, boxWidth, boxHeight, targetPoint);
        }

        drawSpeechBox(boxX, boxY, boxWidth, boxHeight, pointing, overlayEvent.getColor(), captionFade);
        Point pointerTip = getSpeechPointerTip(boxX, boxY, boxWidth, boxHeight, pointing);
        if (targetPoint != null && pointerTip != null) {
            drawCaptionConnector(pointerTip.x, pointerTip.y, targetPoint.x, targetPoint.y, overlayEvent.getColor(),
                captionFade);
        }

        for (int i = 0; i < lines.size(); i++) {
            drawCenteredStringNoShadow(lines.get(i), boxX + boxWidth / 2, boxY + 6 + i * 10, 0xF2F5F8);
        }
    }

    protected void drawGuiTextureOverlays(PonderScene scene, PreviewLayout layout, float currentTick, float fade) {
        if (scene == null || layout == null || fade <= 0.0F) {
            return;
        }

        List<GuiOverlayPlacement> guiPlacements = getActiveGuiOverlayPlacements(scene, layout, currentTick);
        for (GuiOverlayPlacement placement : guiPlacements) {
            PonderScene.OverlayEvent overlayEvent = placement.overlayEvent;
            float overlayFade = computeOverlayFade(overlayEvent.getTick(), overlayEvent.getDuration(), currentTick)
                * fade;
            if (overlayFade <= 0.0F) {
                continue;
            }

            if (overlayEvent.isFramed()) {
                drawGuiTexturePanel(placement.panelX, placement.panelY, placement.panelWidth, placement.panelHeight,
                    overlayEvent.getColor(), overlayFade);
            }

            if (overlayEvent.isFramed() && overlayEvent.isConnectorVisible() && placement.targetPoint != null) {
                int startX = placement.panelX + placement.panelWidth / 2;
                int startY = placement.aboveTarget ? placement.panelY + placement.panelHeight : placement.panelY;
                drawCaptionConnector(startX, startY, placement.targetPoint.x, placement.targetPoint.y,
                    overlayEvent.getColor(), overlayFade * 0.85F);
            }

            PonderGuiSnapshotRegistry.Snapshot snapshot = overlayEvent.getGuiSnapshotId() == null ? null
                : PonderGuiSnapshotRegistry.get(overlayEvent.getGuiSnapshotId(), currentTick);
            if (snapshot != null && snapshot.renderer != null) {
                GlStateManager.enableTexture2D();
                GlStateManager.enableBlend();
                GlStateManager.enableAlpha();
                GlStateManager.color(1.0F, 1.0F, 1.0F, overlayFade);
                snapshot.renderer.render(placement.drawX, placement.drawY, placement.drawWidth, placement.drawHeight,
                    currentTick, overlayFade);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            } else {
                mc.getTextureManager().bindTexture(overlayEvent.getTextureLocation());
                GlStateManager.enableBlend();
                GlStateManager.enableAlpha();
                GlStateManager.color(1.0F, 1.0F, 1.0F, overlayFade);
                if (overlayEvent.isStretchTexture()) {
                    drawStretchGuiTexture(placement, overlayEvent);
                } else {
                    drawScaledCustomSizeModalRect(placement.drawX, placement.drawY, overlayEvent.getTextureU(),
                        overlayEvent.getTextureV(), overlayEvent.getRegionWidth(), overlayEvent.getRegionHeight(),
                        placement.drawWidth, placement.drawHeight, overlayEvent.getTextureWidth(),
                        overlayEvent.getTextureHeight());
                }
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            }
        }

        for (GuiHighlightPlacement placement : getActiveGuiHighlightPlacements(scene, layout, currentTick, guiPlacements)) {
            PonderScene.OverlayEvent overlayEvent = placement.overlayEvent;
            float overlayFade = computeOverlayFade(overlayEvent.getTick(), overlayEvent.getDuration(), currentTick)
                * fade;
            if (overlayFade <= 0.0F) {
                continue;
            }
            drawGuiHighlight(placement.rectX, placement.rectY, placement.rectWidth, placement.rectHeight,
                overlayEvent.getColor(), overlayFade);
        }
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

            float overlayFade = computeOverlayFade(overlayEvent.getTick(), overlayEvent.getDuration(), currentTick)
                * fade;
            if (overlayFade <= 0.0F) {
                continue;
            }

            if (overlayEvent.getType() == PonderScene.OverlayEventType.SCENE_OUTLINE) {
                drawSceneOutlineOverlay(scene, bounds, layout, currentTick, overlayEvent, overlayFade);
            } else if (overlayEvent.getType() == PonderScene.OverlayEventType.SCENE_LINE) {
                drawSceneLineOverlay(scene, bounds, layout, currentTick, overlayEvent, overlayFade);
            } else if (overlayEvent.getType() == PonderScene.OverlayEventType.VALUE_BOX) {
                drawValueBoxOverlay(scene, bounds, layout, currentTick, overlayEvent, overlayFade);
            } else if (overlayEvent.getType() == PonderScene.OverlayEventType.CONTROLS) {
                drawControlsOverlay(scene, bounds, layout, currentTick, overlayEvent, overlayFade);
            }
        }

        drawActorOverlay(scene, bounds, layout, currentTick, fade);
        drawParticleEffectsOverlay(scene, bounds, layout, currentTick, fade);
        drawPointOfInterestOverlay(scene, bounds, layout, currentTick, fade);
    }

    private void drawSceneOutlineOverlay(PonderScene scene, PreviewBounds bounds, PreviewLayout layout, float currentTick,
        PonderScene.OverlayEvent overlayEvent, float fade) {
        ProjectedBounds projected = projectSceneBounds(scene, bounds, layout, currentTick, overlayEvent.getSceneBounds());
        if (projected == null) {
            return;
        }

        int fillColor = withAlpha(overlayEvent.getColor(), fade * 42.0F);
        int edgeColor = withAlpha(blendColors(0xEDE4D4, overlayEvent.getColor(), 0.82F), fade * 232.0F);
        drawRect(projected.minX, projected.minY, projected.maxX, projected.maxY, fillColor);
        drawRect(projected.minX, projected.minY, projected.maxX, projected.minY + 1, edgeColor);
        drawRect(projected.minX, projected.maxY - 1, projected.maxX, projected.maxY, edgeColor);
        drawRect(projected.minX, projected.minY, projected.minX + 1, projected.maxY, edgeColor);
        drawRect(projected.maxX - 1, projected.minY, projected.maxX, projected.maxY, edgeColor);
    }

    private void drawSceneLineOverlay(PonderScene scene, PreviewBounds bounds, PreviewLayout layout, float currentTick,
        PonderScene.OverlayEvent overlayEvent, float fade) {
        Point start = projectScenePoint(scene, bounds, layout, currentTick, overlayEvent.getLineStart());
        Point end = projectScenePoint(scene, bounds, layout, currentTick, overlayEvent.getLineEnd());
        if (start == null || end == null) {
            return;
        }

        int lineColor = withAlpha(overlayEvent.getColor(), fade * 235.0F);
        drawLineSegment(start.x, start.y, end.x, end.y, lineColor, overlayEvent.isLineWide() ? 3.0F : 1.8F);
    }

    private void drawValueBoxOverlay(PonderScene scene, PreviewBounds bounds, PreviewLayout layout, float currentTick,
        PonderScene.OverlayEvent overlayEvent, float fade) {
        Vec3d center = overlayEvent.getValueBoxCenter();
        Vec3d expand = overlayEvent.getValueBoxExpand();
        if (center == null || expand == null) {
            return;
        }
        AxisAlignedBB box = new AxisAlignedBB(center.x - expand.x, center.y - expand.y, center.z - expand.z,
            center.x + expand.x, center.y + expand.y, center.z + expand.z);
        ProjectedBounds projected = projectSceneBounds(scene, bounds, layout, currentTick, box);
        if (projected == null) {
            return;
        }

        int fillColor = withAlpha(overlayEvent.getColor(), fade * 56.0F);
        int edgeColor = withAlpha(blendColors(0xEDE4D4, overlayEvent.getColor(), 0.85F), fade * 228.0F);
        drawRect(projected.minX, projected.minY, projected.maxX, projected.maxY, fillColor);
        drawRect(projected.minX, projected.minY, projected.maxX, projected.minY + 1, edgeColor);
        drawRect(projected.minX, projected.maxY - 1, projected.maxX, projected.maxY, edgeColor);
        drawRect(projected.minX, projected.minY, projected.minX + 1, projected.maxY, edgeColor);
        drawRect(projected.maxX - 1, projected.minY, projected.maxX, projected.maxY, edgeColor);
    }

    private void drawControlsOverlay(PonderScene scene, PreviewBounds bounds, PreviewLayout layout, float currentTick,
        PonderScene.OverlayEvent overlayEvent, float fade) {
        Point target = projectScenePoint(scene, bounds, layout, currentTick, overlayEvent.getPointAt());
        if (target == null) {
            return;
        }

        List<String> actionTokens = new ArrayList<String>();
        if (overlayEvent.isControlLeftClick()) {
            actionTokens.add("LMB");
        }
        if (overlayEvent.isControlRightClick()) {
            actionTokens.add("RMB");
        }
        if (overlayEvent.isControlScroll()) {
            actionTokens.add("SCROLL");
        }

        List<String> modifierTokens = new ArrayList<String>();
        if (overlayEvent.isControlSneaking()) {
            modifierTokens.add("Sneak");
        }
        if (overlayEvent.isControlCtrl()) {
            modifierTokens.add("CTRL");
        }

        Pointing direction = overlayEvent.getControlDirection();
        String directionToken = direction == null ? "INPUT" : getPointingLabel(direction);

        ItemStack stack = overlayEvent.getControlItem();
        int itemWidth = stack.isEmpty() ? 0 : 20;
        int actionsWidth = measureTokenRow(actionTokens);
        int modifiersWidth = measureTokenRow(modifierTokens);
        int directionWidth = measureTokenRow(Collections.singletonList(directionToken));
        int contentWidth = Math.max(Math.max(actionsWidth, modifiersWidth), directionWidth) + itemWidth;
        int boxWidth = Math.max(66, contentWidth + 16);
        int boxHeight = 30 + (modifierTokens.isEmpty() ? 0 : 14);
        int boxX = MathHelper.clamp(target.x - boxWidth / 2, layout.originX + 4,
            layout.originX + layout.width - boxWidth - 4);
        int boxY = target.y - boxHeight - 18;
        SpeechPointing pointing = SpeechPointing.DOWN;
        if (boxY < layout.originY + 4) {
            boxY = target.y + 14;
            pointing = SpeechPointing.UP;
        }

        drawSpeechBox(boxX, boxY, boxWidth, boxHeight, pointing, overlayEvent.getColor(), fade);
        Point tip = getSpeechPointerTip(boxX, boxY, boxWidth, boxHeight, pointing);
        if (tip != null) {
            drawCaptionConnector(tip.x, tip.y, target.x, target.y, overlayEvent.getColor(), fade * 0.9F);
        }

        int rowX = boxX + 7;
        if (!stack.isEmpty()) {
            RenderHelper.enableGUIStandardItemLighting();
            mc.getRenderItem().renderItemAndEffectIntoGUI(stack, boxX + 6, boxY + 4);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableLighting();
            rowX += 20;
        }

        int topY = boxY + 5;
        drawTokenRow(actionTokens.isEmpty() ? Collections.singletonList("ACT") : actionTokens, rowX, topY,
            overlayEvent.getColor(), fade, true);
        drawTokenRow(Collections.singletonList(directionToken), rowX, topY + 12, overlayEvent.getColor(), fade, false);
        if (!modifierTokens.isEmpty()) {
            drawTokenRow(modifierTokens, rowX, topY + 24, overlayEvent.getColor(), fade, false);
        }
    }

    private int measureTokenRow(List<String> tokens) {
        if (tokens == null || tokens.isEmpty()) {
            return 0;
        }
        int width = 0;
        for (String token : tokens) {
            width += Math.max(16, fontRenderer.getStringWidth(token) + 8);
        }
        width += Math.max(0, tokens.size() - 1) * 4;
        return width;
    }

    private void drawTokenRow(List<String> tokens, int x, int y, int accentColor, float fade, boolean emphasized) {
        int cursor = x;
        for (String token : tokens) {
            int chipWidth = Math.max(16, fontRenderer.getStringWidth(token) + 8);
            int fill = withAlpha(emphasized ? blendColors(0x18212B, accentColor, 0.66F) : 0x18212B,
                fade * (emphasized ? 126.0F : 96.0F));
            int edge = withAlpha(blendColors(0xEDE4D4, accentColor, emphasized ? 0.85F : 0.45F),
                fade * (emphasized ? 220.0F : 164.0F));
            drawRect(cursor, y, cursor + chipWidth, y + 10, fill);
            drawRect(cursor, y, cursor + chipWidth, y + 1, edge);
            drawRect(cursor, y + 9, cursor + chipWidth, y + 10, edge);
            drawRect(cursor, y, cursor + 1, y + 10, edge);
            drawRect(cursor + chipWidth - 1, y, cursor + chipWidth, y + 10, edge);
            drawCenteredString(fontRenderer, token, cursor + chipWidth / 2, y + 1, 0xF2F5F8);
            cursor += chipWidth + 4;
        }
    }

    private String getPointingLabel(Pointing pointing) {
        if (pointing == null) {
            return "INPUT";
        }
        switch (pointing) {
            case UP:
                return "UP";
            case DOWN:
                return "DOWN";
            case LEFT:
                return "LEFT";
            case RIGHT:
                return "RIGHT";
            default:
                return pointing.name();
        }
    }

    private void drawPointOfInterestOverlay(PonderScene scene, PreviewBounds bounds, PreviewLayout layout, float currentTick,
        float fade) {
        PonderScene.PoiEvent poiEvent = getActivePoiEvent(scene, currentTick);
        if (poiEvent == null) {
            return;
        }

        Point target = projectScenePoint(scene, bounds, layout, currentTick, poiEvent.getLocation());
        if (target == null) {
            return;
        }

        int accent = withAlpha(PonderPalette.RED.getColor(), fade * 235.0F);
        int fill = withAlpha(PonderPalette.RED.getColor(), fade * 72.0F);
        drawRect(target.x - 5, target.y - 1, target.x + 6, target.y + 1, accent);
        drawRect(target.x - 1, target.y - 5, target.x + 1, target.y + 6, accent);
        drawRect(target.x - 2, target.y - 2, target.x + 3, target.y + 3, fill);
        drawString(fontRenderer, "POI", target.x + 8, target.y - 4, 0xF2F5F8);
    }

    private void drawParticleEffectsOverlay(PonderScene scene, PreviewBounds bounds, PreviewLayout layout,
        float currentTick, float fade) {
        for (PonderScene.ParticleEffectEvent event : scene.getParticleEvents()) {
            if (event.getTick() > currentTick || currentTick > event.getTick() + event.getCycles()) {
                continue;
            }

            float eventFade = computeOverlayFade(event.getTick(), Math.max(1, event.getCycles()), currentTick) * fade;
            if (eventFade <= 0.0F) {
                continue;
            }

            float progress = Math.max(0.0F, currentTick - event.getTick());
            int particleCount = MathHelper.clamp(Math.round(event.getAmountPerCycle()), 1, 12);
            int color = getParticleColor(event.getParticleName());
            int alphaColor = withAlpha(color, eventFade * 220.0F);
            double spread = event.isWithinBlockSpace() ? 0.55D : 0.28D;

            for (int i = 0; i < particleCount; i++) {
                double angle = Math.toRadians((event.getTick() * 37 + i * (360.0D / particleCount)) % 360);
                double radius = spread * (0.35D + (i % 3) * 0.25D);
                Vec3d baseOffset = new Vec3d(Math.cos(angle) * radius, ((i % 4) - 1.5D) * 0.08D,
                    Math.sin(angle) * radius);
                Vec3d motionOffset = event.getMotion().scale(progress);
                Vec3d point = event.getLocation().add(baseOffset).add(motionOffset);
                Point projected = projectScenePoint(scene, bounds, layout, currentTick, point);
                if (projected == null) {
                    continue;
                }

                int size = i % 3 == 0 ? 2 : 1;
                drawRect(projected.x - size, projected.y - size, projected.x + size + 1, projected.y + size + 1,
                    alphaColor);
            }
        }
    }

    private void drawActorOverlay(PonderScene scene, PreviewBounds bounds, PreviewLayout layout, float currentTick,
        float fade) {
        List<ActorRuntimeState> actors = getActorRuntimeStates(scene, currentTick);
        for (ActorRuntimeState actor : actors) {
            if (!actor.visible || actor.fade <= 0.0F) {
                continue;
            }

            Point target = projectScenePoint(scene, bounds, layout, currentTick, actor.position);
            if (target == null) {
                continue;
            }

            int baseColor = getActorBaseColor(actor);
            if (actor.kind == PonderScene.ActorKind.ITEM && actor.itemStack != null && !actor.itemStack.isEmpty()) {
                net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
                mc.getRenderItem().renderItemAndEffectIntoGUI(actor.itemStack, target.x - 8, target.y - 8);
                net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();
                GlStateManager.disableLighting();
            }
            int border = withAlpha(baseColor, fade * actor.fade * 235.0F);
            int fill = withAlpha(baseColor, fade * actor.fade * 72.0F);
            int boxSize = actor.kind == PonderScene.ActorKind.BIRB ? 8 : 10;
            if (actor.kind != PonderScene.ActorKind.ITEM) {
                drawRect(target.x - boxSize / 2, target.y - boxSize / 2, target.x + boxSize / 2 + 1,
                    target.y + boxSize / 2 + 1, fill);
                drawRect(target.x - boxSize / 2, target.y - boxSize / 2, target.x + boxSize / 2 + 1,
                    target.y - boxSize / 2 + 1, border);
                drawRect(target.x - boxSize / 2, target.y + boxSize / 2, target.x + boxSize / 2 + 1,
                    target.y + boxSize / 2 + 1, border);
                drawRect(target.x - boxSize / 2, target.y - boxSize / 2, target.x - boxSize / 2 + 1,
                    target.y + boxSize / 2 + 1, border);
                drawRect(target.x + boxSize / 2, target.y - boxSize / 2, target.x + boxSize / 2 + 1,
                    target.y + boxSize / 2 + 1, border);
            }

            double yawDegrees = actor.kind == PonderScene.ActorKind.CART ? actor.cartYaw : actor.rotation.y;
            double yawRadians = Math.toRadians(yawDegrees);
            int lineX = target.x + (int) Math.round(Math.sin(yawRadians) * (boxSize + 2));
            int lineY = target.y - (int) Math.round(Math.cos(yawRadians) * (boxSize + 2));
            drawLineSegment(target.x, target.y, lineX, lineY, border, 1.5F);

            String label = getActorLabel(actor);
            drawString(fontRenderer, label, target.x + boxSize / 2 + 4, target.y - 4, 0xF2F5F8);
        }
    }

    private void renderActorPreviews(PonderScene scene, float currentTick) {
        List<ActorRuntimeState> actors = getActorRuntimeStates(scene, currentTick);
        if (actors.isEmpty()) {
            return;
        }

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableCull();
        for (ActorRuntimeState actor : actors) {
            if (!actor.visible || actor.fade <= 0.0F) {
                continue;
            }
            renderActorPreview(actor, currentTick);
        }
        GlStateManager.enableCull();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void renderActorPreview(ActorRuntimeState actor, float currentTick) {
        float alpha = MathHelper.clamp(actor.fade, 0.0F, 1.0F);
        int color = getActorBaseColor(actor);
        float red = ((color >> 16) & 0xFF) / 255.0F;
        float green = ((color >> 8) & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;
        float bobOffset = getActorBobOffset(actor, currentTick);
        float yaw = actor.kind == PonderScene.ActorKind.CART ? actor.cartYaw : (float) actor.rotation.y;
        yaw += getActorYawOffset(actor, currentTick);

        GlStateManager.pushMatrix();
        GlStateManager.translate(actor.position.x, actor.position.y + bobOffset, actor.position.z);
        GlStateManager.rotate(yaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate((float) actor.rotation.x, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate((float) actor.rotation.z, 0.0F, 0.0F, 1.0F);

        if (actor.kind == PonderScene.ActorKind.BIRB) {
            drawBirbBody(actor, red, green, blue, alpha, currentTick);
            drawActorHeading(alpha);
        } else if (actor.kind == PonderScene.ActorKind.ITEM) {
            drawItemBody(red, green, blue, alpha, currentTick);
            drawActorHeading(alpha * 0.7F);
        } else {
            drawActorPrism(red, green, blue, alpha, 0.30F, 0.16F, 0.18F);
            drawCartWheels(alpha);
            drawActorHeading(alpha);
        }

        GlStateManager.popMatrix();
    }

    private void drawActorHeading(float alpha) {
        int lineColor = withAlpha(0xF2F5F8, alpha * 235.0F);
        float red = ((lineColor >> 16) & 0xFF) / 255.0F;
        float green = ((lineColor >> 8) & 0xFF) / 255.0F;
        float blue = (lineColor & 0xFF) / 255.0F;
        float lineAlpha = ((lineColor >>> 24) & 0xFF) / 255.0F;

        GlStateManager.color(red, green, blue, lineAlpha);
        GL11.glLineWidth(2.0F);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3f(0.0F, 0.05F, 0.0F);
        GL11.glVertex3f(0.0F, 0.05F, -0.38F);
        GL11.glEnd();
        GL11.glLineWidth(1.0F);
    }

    private void drawBirbBody(ActorRuntimeState actor, float red, float green, float blue, float alpha,
        float currentTick) {
        if (isBirbDancePose(actor)) {
            drawActorPrism(red, green, blue, alpha, 0.16F, 0.28F, 0.16F);
            float wing = 0.04F + Math.abs(MathHelper.sin(currentTick * 0.35F)) * 0.05F;
            drawActorPrism(red * 0.92F, green * 0.92F, blue, alpha * 0.92F, wing, 0.12F, 0.04F, -0.18F, 0.08F, 0.0F);
            drawActorPrism(red * 0.92F, green * 0.92F, blue, alpha * 0.92F, wing, 0.12F, 0.04F, 0.18F, 0.08F, 0.0F);
            return;
        }
        if (isBirbCursorPose(actor)) {
            drawActorPrism(red, green, blue, alpha, 0.13F, 0.34F, 0.13F);
            drawActorPrism(red * 0.85F, green * 0.95F, blue, alpha * 0.9F, 0.04F, 0.10F, 0.10F, 0.0F, 0.26F, -0.14F);
            return;
        }
        drawActorPrism(red, green, blue, alpha, 0.14F, 0.34F, 0.14F);
    }

    private void drawItemBody(float red, float green, float blue, float alpha, float currentTick) {
        float sway = Math.abs(MathHelper.sin(currentTick * 0.25F)) * 0.02F;
        drawActorPrism(red, green, blue, alpha, 0.12F + sway, 0.06F, 0.12F + sway);
    }

    private void drawCartWheels(float alpha) {
        int wheelColor = withAlpha(0x2B2B2B, alpha * 255.0F);
        float red = ((wheelColor >> 16) & 0xFF) / 255.0F;
        float green = ((wheelColor >> 8) & 0xFF) / 255.0F;
        float blue = (wheelColor & 0xFF) / 255.0F;
        float wheelAlpha = ((wheelColor >>> 24) & 0xFF) / 255.0F;
        float[][] wheels = new float[][] {
            {-0.22F, -0.12F},
            {0.22F, -0.12F},
            {-0.22F, 0.12F},
            {0.22F, 0.12F}
        };
        for (float[] wheel : wheels) {
            drawActorPrism(red, green, blue, wheelAlpha, 0.05F, 0.05F, 0.05F, wheel[0], -0.12F, wheel[1]);
        }
    }

    private void drawActorPrism(float red, float green, float blue, float alpha, float halfX, float height,
        float halfZ) {
        drawActorPrism(red, green, blue, alpha, halfX, height, halfZ, 0.0F, 0.0F, 0.0F);
    }

    private void drawActorPrism(float red, float green, float blue, float alpha, float halfX, float height,
        float halfZ, float offsetX, float offsetY, float offsetZ) {
        float x1 = offsetX - halfX;
        float x2 = offsetX + halfX;
        float y1 = offsetY;
        float y2 = offsetY + height;
        float z1 = offsetZ - halfZ;
        float z2 = offsetZ + halfZ;

        GlStateManager.color(red, green, blue, alpha);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3f(x1, y1, z1);
        GL11.glVertex3f(x2, y1, z1);
        GL11.glVertex3f(x2, y2, z1);
        GL11.glVertex3f(x1, y2, z1);

        GL11.glVertex3f(x1, y1, z2);
        GL11.glVertex3f(x2, y1, z2);
        GL11.glVertex3f(x2, y2, z2);
        GL11.glVertex3f(x1, y2, z2);

        GL11.glVertex3f(x1, y1, z1);
        GL11.glVertex3f(x1, y1, z2);
        GL11.glVertex3f(x1, y2, z2);
        GL11.glVertex3f(x1, y2, z1);

        GL11.glVertex3f(x2, y1, z1);
        GL11.glVertex3f(x2, y1, z2);
        GL11.glVertex3f(x2, y2, z2);
        GL11.glVertex3f(x2, y2, z1);

        GL11.glVertex3f(x1, y2, z1);
        GL11.glVertex3f(x2, y2, z1);
        GL11.glVertex3f(x2, y2, z2);
        GL11.glVertex3f(x1, y2, z2);

        GL11.glVertex3f(x1, y1, z1);
        GL11.glVertex3f(x2, y1, z1);
        GL11.glVertex3f(x2, y1, z2);
        GL11.glVertex3f(x1, y1, z2);
        GL11.glEnd();
    }

    private List<GuiOverlayPlacement> getActiveGuiOverlayPlacements(PonderScene scene, PreviewLayout layout,
        float currentTick) {
        if (scene == null || layout == null) {
            return Collections.emptyList();
        }

        PreviewBounds bounds = PonderScenePreview.computeBounds(scene);
        List<GuiOverlayPlacement> placements = new ArrayList<GuiOverlayPlacement>();
        Map<String, GuiOverlayPlacement> placementsById = new LinkedHashMap<String, GuiOverlayPlacement>();
        for (PonderScene.OverlayEvent overlayEvent : scene.getOverlayEvents()) {
            if (overlayEvent.getType() != PonderScene.OverlayEventType.GUI_TEXTURE) {
                continue;
            }
            if (overlayEvent.getTick() > currentTick
                || currentTick > overlayEvent.getTick() + overlayEvent.getDuration()
                || (overlayEvent.getTextureLocation() == null && overlayEvent.getGuiSnapshotId() == null)) {
                continue;
            }

            PonderGuiSnapshotRegistry.Snapshot snapshot = overlayEvent.getGuiSnapshotId() == null ? null
                : PonderGuiSnapshotRegistry.get(overlayEvent.getGuiSnapshotId(), currentTick);
            if (snapshot != null) {
                overlayEvent.applySnapshot(snapshot, overlayEvent.getOffsetX(), overlayEvent.getOffsetY());
            }
            if (overlayEvent.getTextureLocation() == null && (snapshot == null || snapshot.renderer == null)) {
                continue;
            }

            GuiOverlayPlacement placement =
                computeGuiOverlayPlacement(scene, bounds, layout, currentTick, overlayEvent, placementsById);
            if (placement != null) {
                placements.add(placement);
                String overlayId = placement.overlayEvent.getOverlayId();
                if (overlayId != null && !overlayId.trim().isEmpty()) {
                    placementsById.put(overlayId, placement);
                }
            }
        }
        return placements;
    }

    private GuiOverlayPlacement computeGuiOverlayPlacement(PonderScene scene, PreviewBounds bounds, PreviewLayout layout,
        float currentTick, PonderScene.OverlayEvent overlayEvent, Map<String, GuiOverlayPlacement> placementsById) {
        int logicalWidth = Math.max(1, overlayEvent.getRegionWidth());
        int logicalHeight = Math.max(1, overlayEvent.getRegionHeight());
        int drawWidth = Math.max(1, overlayEvent.getDisplayWidth());
        int drawHeight = Math.max(1, overlayEvent.getDisplayHeight());
        int padding = overlayEvent.isFramed() ? 4 : 0;
        int panelWidth = drawWidth + padding * 2;
        int panelHeight = drawHeight + padding * 2;
        Point targetPoint = overlayEvent.getPointAt() == null ? null
            : projectScenePoint(scene, bounds, layout, currentTick, overlayEvent.getPointAt());
        boolean aboveTarget = true;

        String parentOverlayId = overlayEvent.getParentOverlayId();
        if (parentOverlayId != null && !parentOverlayId.trim().isEmpty()) {
            GuiOverlayPlacement parentPlacement = placementsById.get(parentOverlayId);
            if (parentPlacement == null) {
                return null;
            }

            float parentScaleX =
                parentPlacement.drawWidth / (float) Math.max(1, parentPlacement.overlayEvent.getRegionWidth());
            float parentScaleY =
                parentPlacement.drawHeight / (float) Math.max(1, parentPlacement.overlayEvent.getRegionHeight());
            if (overlayEvent.isScaleToParent()) {
                drawWidth = Math.max(1, Math.round(logicalWidth * parentScaleX));
                drawHeight = Math.max(1, Math.round(logicalHeight * parentScaleY));
            }
            panelWidth = drawWidth + padding * 2;
            panelHeight = drawHeight + padding * 2;

            int drawX = parentPlacement.drawX + Math.round(overlayEvent.getGuiX() * parentScaleX)
                + overlayEvent.getOffsetX();
            int drawY = parentPlacement.drawY + Math.round(overlayEvent.getGuiY() * parentScaleY)
                + overlayEvent.getOffsetY();
            int panelX = MathHelper.clamp(drawX - padding, layout.originX + 2,
                layout.originX + layout.width - panelWidth - 2);
            int panelY = MathHelper.clamp(drawY - padding, layout.originY + 2,
                layout.originY + layout.height - panelHeight - 2);
            int finalDrawX = panelX + padding;
            int finalDrawY = panelY + padding;
            Point finalTargetPoint = new Point(finalDrawX + drawWidth / 2, finalDrawY + drawHeight / 2);
            return new GuiOverlayPlacement(overlayEvent, finalTargetPoint, false, panelX, panelY, panelWidth,
                panelHeight, finalDrawX, finalDrawY, drawWidth, drawHeight);
        }

        int panelX = layout.originX + (layout.width - panelWidth) / 2 + overlayEvent.getOffsetX();
        int panelY = overlayEvent.getIndependentY() >= 0
            ? layout.originY + 8 + overlayEvent.getIndependentY() * 2 + overlayEvent.getOffsetY()
            : layout.originY + 8 + overlayEvent.getOffsetY();

        float fitScale = Math.min(1.0F,
            Math.min((layout.width - 8.0F) / Math.max(1.0F, panelWidth),
                (layout.height - 8.0F) / Math.max(1.0F, panelHeight)));
        if (fitScale < 1.0F) {
            drawWidth = Math.max(1, Math.round(drawWidth * fitScale));
            drawHeight = Math.max(1, Math.round(drawHeight * fitScale));
            panelWidth = drawWidth + padding * 2;
            panelHeight = drawHeight + padding * 2;
        }

        if (targetPoint != null) {
            panelX = targetPoint.x - panelWidth / 2 + overlayEvent.getOffsetX();
            panelY = targetPoint.y - panelHeight - 18 + overlayEvent.getOffsetY();
            if (overlayEvent.isPlaceNearTarget() && panelY < layout.originY + 6) {
                panelY = targetPoint.y + 14 + overlayEvent.getOffsetY();
                aboveTarget = false;
            }
        }

        panelX = MathHelper.clamp(panelX, layout.originX + 4,
            layout.originX + layout.width - panelWidth - 4);
        panelY = MathHelper.clamp(panelY, layout.originY + 4,
            layout.originY + layout.height - panelHeight - 4);
        return new GuiOverlayPlacement(overlayEvent, targetPoint, aboveTarget, panelX, panelY, panelWidth, panelHeight,
            panelX + padding, panelY + padding, drawWidth, drawHeight);
    }

    private void drawStretchGuiTexture(GuiOverlayPlacement placement, PonderScene.OverlayEvent overlayEvent) {
        int logicalWidth = Math.max(1, overlayEvent.getRegionWidth());
        int logicalHeight = Math.max(1, overlayEvent.getRegionHeight());
        int textureWidth = Math.max(1, overlayEvent.getTextureWidth());
        int textureHeight = Math.max(1, overlayEvent.getTextureHeight());
        int border = Math.min(overlayEvent.getStretchBorder(), Math.min(textureWidth / 2, textureHeight / 2));
        border = Math.max(1, border);

        int drawBorderX = Math.max(1, Math.round(border * (placement.drawWidth / (float) logicalWidth)));
        int drawBorderY = Math.max(1, Math.round(border * (placement.drawHeight / (float) logicalHeight)));
        drawBorderX = Math.min(drawBorderX, Math.max(1, placement.drawWidth / 2));
        drawBorderY = Math.min(drawBorderY, Math.max(1, placement.drawHeight / 2));

        int centerSourceWidth = Math.max(1, textureWidth - border * 2);
        int centerSourceHeight = Math.max(1, textureHeight - border * 2);
        int centerDrawWidth = Math.max(0, placement.drawWidth - drawBorderX * 2);
        int centerDrawHeight = Math.max(0, placement.drawHeight - drawBorderY * 2);
        int sourceRightU = overlayEvent.getTextureU() + textureWidth - border;
        int sourceBottomV = overlayEvent.getTextureV() + textureHeight - border;

        drawScaledCustomSizeModalRect(placement.drawX, placement.drawY, overlayEvent.getTextureU(),
            overlayEvent.getTextureV(), border, border, drawBorderX, drawBorderY, textureWidth, textureHeight);
        drawScaledCustomSizeModalRect(placement.drawX + placement.drawWidth - drawBorderX, placement.drawY, sourceRightU,
            overlayEvent.getTextureV(), border, border, drawBorderX, drawBorderY, textureWidth, textureHeight);
        drawScaledCustomSizeModalRect(placement.drawX, placement.drawY + placement.drawHeight - drawBorderY,
            overlayEvent.getTextureU(), sourceBottomV, border, border, drawBorderX, drawBorderY, textureWidth,
            textureHeight);
        drawScaledCustomSizeModalRect(placement.drawX + placement.drawWidth - drawBorderX,
            placement.drawY + placement.drawHeight - drawBorderY, sourceRightU, sourceBottomV, border, border,
            drawBorderX, drawBorderY, textureWidth, textureHeight);

        if (centerDrawWidth > 0) {
            drawScaledCustomSizeModalRect(placement.drawX + drawBorderX, placement.drawY, overlayEvent.getTextureU() + border,
                overlayEvent.getTextureV(), centerSourceWidth, border, centerDrawWidth, drawBorderY, textureWidth,
                textureHeight);
            drawScaledCustomSizeModalRect(placement.drawX + drawBorderX,
                placement.drawY + placement.drawHeight - drawBorderY, overlayEvent.getTextureU() + border, sourceBottomV,
                centerSourceWidth, border, centerDrawWidth, drawBorderY, textureWidth, textureHeight);
        }
        if (centerDrawHeight > 0) {
            drawScaledCustomSizeModalRect(placement.drawX, placement.drawY + drawBorderY, overlayEvent.getTextureU(),
                overlayEvent.getTextureV() + border, border, centerSourceHeight, drawBorderX, centerDrawHeight,
                textureWidth, textureHeight);
            drawScaledCustomSizeModalRect(placement.drawX + placement.drawWidth - drawBorderX,
                placement.drawY + drawBorderY, sourceRightU, overlayEvent.getTextureV() + border, border,
                centerSourceHeight, drawBorderX, centerDrawHeight, textureWidth, textureHeight);
        }
        if (centerDrawWidth > 0 && centerDrawHeight > 0) {
            drawScaledCustomSizeModalRect(placement.drawX + drawBorderX, placement.drawY + drawBorderY,
                overlayEvent.getTextureU() + border, overlayEvent.getTextureV() + border, centerSourceWidth,
                centerSourceHeight, centerDrawWidth, centerDrawHeight, textureWidth, textureHeight);
        }
    }

    private List<GuiHighlightPlacement> getActiveGuiHighlightPlacements(PonderScene scene, PreviewLayout layout,
        float currentTick, List<GuiOverlayPlacement> guiPlacements) {
        if (scene == null || layout == null || guiPlacements == null || guiPlacements.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, GuiOverlayPlacement> guiPlacementById = new LinkedHashMap<String, GuiOverlayPlacement>();
        for (GuiOverlayPlacement placement : guiPlacements) {
            String overlayId = placement.overlayEvent.getOverlayId();
            if (overlayId != null && !overlayId.trim().isEmpty()) {
                guiPlacementById.put(overlayId, placement);
            }
        }
        if (guiPlacementById.isEmpty()) {
            return Collections.emptyList();
        }

        List<GuiHighlightPlacement> placements = new ArrayList<GuiHighlightPlacement>();
        for (PonderScene.OverlayEvent overlayEvent : scene.getOverlayEvents()) {
            if (overlayEvent.getType() != PonderScene.OverlayEventType.GUI_HIGHLIGHT) {
                continue;
            }
            if (overlayEvent.getTick() > currentTick
                || currentTick > overlayEvent.getTick() + overlayEvent.getDuration()) {
                continue;
            }

            GuiOverlayPlacement parentPlacement = guiPlacementById.get(overlayEvent.getParentOverlayId());
            if (parentPlacement == null) {
                continue;
            }

            GuiHighlightPlacement placement = computeGuiHighlightPlacement(parentPlacement, overlayEvent);
            if (placement != null) {
                placements.add(placement);
            }
        }
        return placements;
    }

    private GuiHighlightPlacement computeGuiHighlightPlacement(GuiOverlayPlacement parentPlacement,
        PonderScene.OverlayEvent overlayEvent) {
        float scaleX = parentPlacement.drawWidth / (float) Math.max(1, parentPlacement.overlayEvent.getRegionWidth());
        float scaleY = parentPlacement.drawHeight / (float) Math.max(1, parentPlacement.overlayEvent.getRegionHeight());
        int rectX = parentPlacement.drawX + Math.round(overlayEvent.getGuiX() * scaleX);
        int rectY = parentPlacement.drawY + Math.round(overlayEvent.getGuiY() * scaleY);
        int rectWidth = Math.max(2, Math.round(overlayEvent.getGuiWidth() * scaleX));
        int rectHeight = Math.max(2, Math.round(overlayEvent.getGuiHeight() * scaleY));
        int maxX = parentPlacement.drawX + parentPlacement.drawWidth;
        int maxY = parentPlacement.drawY + parentPlacement.drawHeight;

        if (rectX >= maxX || rectY >= maxY) {
            return null;
        }

        rectWidth = Math.min(rectWidth, maxX - rectX);
        rectHeight = Math.min(rectHeight, maxY - rectY);
        if (rectWidth <= 0 || rectHeight <= 0) {
            return null;
        }

        Point targetPoint = new Point(rectX + rectWidth / 2, rectY + rectHeight / 2);
        return new GuiHighlightPlacement(overlayEvent, rectX, rectY, rectWidth, rectHeight, targetPoint);
    }

    private CaptionPlacement avoidGuiOverlayOverlap(int boxX, int boxY, int boxWidth, int boxHeight,
        SpeechPointing pointing, int previewY, int maxBoxY, List<GuiOverlayPlacement> placements) {
        if (placements == null || placements.isEmpty()) {
            return new CaptionPlacement(boxX, boxY, pointing);
        }

        int left = Integer.MAX_VALUE;
        int top = Integer.MAX_VALUE;
        int right = Integer.MIN_VALUE;
        int bottom = Integer.MIN_VALUE;
        boolean intersects = false;
        for (GuiOverlayPlacement placement : placements) {
            left = Math.min(left, placement.panelX);
            top = Math.min(top, placement.panelY);
            right = Math.max(right, placement.panelX + placement.panelWidth);
            bottom = Math.max(bottom, placement.panelY + placement.panelHeight);
            if (rectsIntersect(boxX, boxY, boxWidth, boxHeight, placement.panelX, placement.panelY, placement.panelWidth,
                placement.panelHeight)) {
                intersects = true;
            }
        }

        if (!intersects) {
            return new CaptionPlacement(boxX, boxY, pointing);
        }

        int aboveY = top - boxHeight - 12;
        int belowY = bottom + 12;
        boolean canAbove = aboveY >= previewY + 12;
        boolean canBelow = belowY <= maxBoxY;
        if (canAbove && (!canBelow || Math.abs(boxY - aboveY) <= Math.abs(boxY - belowY))) {
            return new CaptionPlacement(boxX, aboveY, SpeechPointing.DOWN);
        }
        if (canBelow) {
            return new CaptionPlacement(boxX, belowY, SpeechPointing.UP);
        }

        int clampedY = MathHelper.clamp(boxY, previewY + 12, maxBoxY);
        return new CaptionPlacement(boxX, clampedY, pointing);
    }

    private Point resolveCaptionTargetPoint(PonderScene scene, float currentTick, PonderScene.OverlayEvent overlayEvent,
        List<GuiHighlightPlacement> guiHighlightPlacements) {
        if (scene == null || overlayEvent == null || lastPreviewLayout == null) {
            return null;
        }

        if (overlayEvent.getType() == PonderScene.OverlayEventType.GUI_HIGHLIGHT) {
            for (GuiHighlightPlacement placement : guiHighlightPlacements) {
                if (placement.overlayEvent == overlayEvent) {
                    return placement.targetPoint;
                }
            }
        }

        return overlayEvent.getPointAt() == null ? null
            : projectScenePoint(scene, PonderScenePreview.computeBounds(scene), lastPreviewLayout, currentTick,
                overlayEvent.getPointAt());
    }

    private int resolveManualCaptionCoordinate(int coordinate, int origin, int size, int boxSize) {
        return coordinate >= 0 ? origin + coordinate : origin + size - boxSize + coordinate;
    }

    private SpeechPointing choosePointing(int boxX, int boxY, int boxWidth, int boxHeight, Point targetPoint) {
        if (targetPoint == null) {
            return SpeechPointing.NONE;
        }

        int centerX = boxX + boxWidth / 2;
        int centerY = boxY + boxHeight / 2;
        int deltaX = targetPoint.x - centerX;
        int deltaY = targetPoint.y - centerY;
        if (Math.abs(deltaX) > Math.abs(deltaY)) {
            return deltaX > 0 ? SpeechPointing.RIGHT : SpeechPointing.LEFT;
        }
        return deltaY > 0 ? SpeechPointing.DOWN : SpeechPointing.UP;
    }

    private boolean rectsIntersect(int ax, int ay, int aw, int ah, int bx, int by, int bw, int bh) {
        return ax < bx + bw && ax + aw > bx && ay < by + bh && ay + ah > by;
    }

    protected void drawNextUpCard(PonderScene scene, float fade) {
        lastNextUpCardWidth = 0;
        lastNextUpCardHeight = 0;
        if (scene == null || selectedSceneIndex + 1 >= compiledScenes.size()) {
            return;
        }

        int maxTick = Math.max(1, getSceneEndTick(scene));
        if (playbackTick < (int) (maxTick * 0.7F)) {
            return;
        }

        PonderScene nextScene = compiledScenes.get(selectedSceneIndex + 1);
        if (nextSceneButton == null) {
            return;
        }

        String nextUpLabel = "\u63A5\u4E0B\u6765";
        String nextTitle = fontRenderer.trimStringToWidth(nextScene.getTitle(), 150);
        int boxWidth = Math.max(96, Math.max(fontRenderer.getStringWidth(nextUpLabel), fontRenderer.getStringWidth(nextTitle)) + 22);
        int anchorX = nextSceneButton.x + nextSceneButton.width / 2;
        int anchorY = nextSceneButton.y - 8;
        int boxX = anchorX - boxWidth / 2;
        int boxY = anchorY - 34;
        lastNextUpCardX = boxX;
        lastNextUpCardY = boxY;
        lastNextUpCardWidth = boxWidth;
        lastNextUpCardHeight = 26;
        drawSpeechBox(boxX, boxY, boxWidth, 26, SpeechPointing.DOWN, 0xD5CCB8, fade);
        drawCenteredStringNoShadow(nextUpLabel, boxX + boxWidth / 2, boxY + 6, 0xAEB8C1);
        drawCenteredStringNoShadow(nextTitle, boxX + boxWidth / 2, boxY + 16, 0xF3EFE7);
    }

    protected void drawShowcaseBackdrop(int previewX, int previewY, int previewWidth, int previewHeight, float fade) {
        int glowAlpha = (int) (fade * 52.0F) << 24;
        int borderAlpha = (int) (fade * 44.0F) << 24;
        int vignetteAlpha = (int) (fade * 92.0F) << 24;
        drawGradientRect(previewX - 20, previewY - 20, previewX + previewWidth + 20, previewY + previewHeight + 26,
            glowAlpha | 0x17202A, 0x00000000);
        drawRect(previewX, previewY, previewX + previewWidth, previewY + 1, borderAlpha | 0xF1E8D4);
        drawRect(previewX, previewY + previewHeight - 1, previewX + previewWidth, previewY + previewHeight,
            borderAlpha | 0x2C3138);
        drawRect(previewX, previewY, previewX + 1, previewY + previewHeight, borderAlpha | 0x4D545E);
        drawRect(previewX + previewWidth - 1, previewY, previewX + previewWidth, previewY + previewHeight,
            borderAlpha | 0x2C3138);
        drawGradientRect(previewX, previewY, previewX + previewWidth, previewY + 52, vignetteAlpha | 0x030508,
            0x00000000);
        drawGradientRect(previewX, previewY + previewHeight - 60, previewX + previewWidth, previewY + previewHeight,
            0x00000000, vignetteAlpha | 0x030508);
    }

    protected void drawShowcaseLogo(int x, int y, float fade) {
        mc.getTextureManager().bindTexture(SHOWCASE_LOGO_TEXTURE);
        GlStateManager.enableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, MathHelper.clamp(fade * 0.72F, 0.0F, 1.0F));
        drawTexturedModalRect(x, y, 0, 0, 32, 32);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void drawGuiTexturePanel(int x, int y, int width, int height, int accentColor, float fade) {
        int fillTop = withAlpha(0x121920, fade * 205.0F);
        int fillBottom = withAlpha(0x090D12, fade * 205.0F);
        int neutralBorder = withAlpha(0xE7E0D0, fade * 165.0F);
        int shadowBorder = withAlpha(0x2F343B, fade * 96.0F);
        int accentBorder = withAlpha(blendColors(0xD5CCB8, accentColor, 0.72F), fade * 164.0F);
        int innerAccent = withAlpha(blendColors(0xEEE6D4, accentColor, 0.34F), fade * 124.0F);

        drawGradientRect(x, y, x + width, y + height, fillTop, fillBottom);
        drawHorizontalGradientRect(x, y, x + width, y + 1, neutralBorder, accentBorder);
        drawHorizontalGradientRect(x, y + height - 1, x + width, y + height, shadowBorder, accentBorder);
        drawGradientRect(x, y, x + 1, y + height, neutralBorder, shadowBorder);
        drawGradientRect(x + width - 1, y, x + width, y + height, accentBorder, shadowBorder);
        drawHorizontalGradientRect(x + 2, y + 2, x + width - 2, y + 4, neutralBorder, innerAccent);
    }

    private void drawGuiHighlight(int x, int y, int width, int height, int accentColor, float fade) {
        int glowColor = withAlpha(blendColors(0x2A3643, accentColor, 0.72F), fade * 52.0F);
        int fillColor = withAlpha(blendColors(0x18212B, accentColor, 0.66F), fade * 86.0F);
        int edgeColor = withAlpha(blendColors(0xEDE4D4, accentColor, 0.80F), fade * 228.0F);
        int accentEdge = withAlpha(blendColors(0xC2B18F, accentColor, 0.90F), fade * 180.0F);

        drawRect(x - 2, y - 2, x + width + 2, y + height + 2, glowColor);
        drawRect(x, y, x + width, y + height, fillColor);
        drawHorizontalGradientRect(x, y, x + width, y + 1, edgeColor, accentEdge);
        drawHorizontalGradientRect(x, y + height - 1, x + width, y + height, accentEdge, edgeColor);
        drawGradientRect(x, y, x + 1, y + height, edgeColor, accentEdge);
        drawGradientRect(x + width - 1, y, x + width, y + height, accentEdge, edgeColor);
    }

    protected void drawShowcaseHoverHints(int mouseX, int mouseY) {
        if (isMouseOverPlaybackBar(mouseX, mouseY)) {
            PonderScene scene = getSelectedScene();
            String label = scene == null ? "拖动时间轴" : "拖动时间轴  |  T+" + estimatePlaybackTickForMouse(mouseX) + " / " + getSceneEndTick(scene);
            drawCenteredString(fontRenderer, label, width / 2, height - 12, 0xB8C3CC);
            return;
        }
        if (isMouseOverShowcaseHeaderIcon(mouseX, mouseY) && hasShowcaseGroupChoices()) {
            drawCenteredString(fontRenderer, "\u70B9\u51FB\u56FE\u6807\u9009\u62E9\u540C\u7EC4\u673A\u5668", width / 2, height - 12,
                0xB8C3CC);
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
            drawCenteredString(fontRenderer, "点击切换到下一幕", width / 2, height - 12, 0xB8C3CC);
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

    private void drawSpeechBox(int boxX, int boxY, int boxWidth, int boxHeight, SpeechPointing pointing, int accentColor,
        float fade) {
        int fillTop = withAlpha(0x151A20, fade * 205.0F);
        int fillBottom = withAlpha(0x090C10, fade * 205.0F);
        int neutralBorder = withAlpha(0xE7E0D0, fade * 170.0F);
        int shadowBorder = withAlpha(0x2F343B, fade * 96.0F);
        int accentBorder = withAlpha(blendColors(0xD5CCB8, accentColor, 0.72F), fade * 168.0F);
        int accentEdge = withAlpha(blendColors(0xBDAE93, accentColor, 0.85F), fade * 148.0F);
        int innerAccent = withAlpha(blendColors(0xE1D7C4, accentColor, 0.38F), fade * 132.0F);

        drawGradientRect(boxX, boxY, boxX + boxWidth, boxY + boxHeight, fillTop, fillBottom);
        drawHorizontalGradientRect(boxX, boxY, boxX + boxWidth, boxY + 1, neutralBorder, accentBorder);
        drawHorizontalGradientRect(boxX, boxY + boxHeight - 1, boxX + boxWidth, boxY + boxHeight, shadowBorder,
            accentEdge);
        drawGradientRect(boxX, boxY, boxX + 1, boxY + boxHeight, neutralBorder, shadowBorder);
        drawGradientRect(boxX + boxWidth - 1, boxY, boxX + boxWidth, boxY + boxHeight, accentBorder, accentEdge);
        drawHorizontalGradientRect(boxX + 2, boxY + 2, boxX + boxWidth - 2, boxY + 4, neutralBorder, innerAccent);

        if (pointing == SpeechPointing.NONE) {
            return;
        }

        int divotX = boxX + boxWidth / 2 - 4;
        int divotY = boxY + boxHeight - 1;
        int rotation = 0;
        if (pointing == SpeechPointing.UP) {
            divotY = boxY - 7;
            rotation = 180;
        } else if (pointing == SpeechPointing.LEFT) {
            divotX = boxX - 7;
            divotY = boxY + boxHeight / 2 - 4;
            rotation = 90;
        } else if (pointing == SpeechPointing.RIGHT) {
            divotX = boxX + boxWidth - 1;
            divotY = boxY + boxHeight / 2 - 4;
            rotation = 270;
        }

        drawSpeechDivot(divotX, divotY, rotation, accentColor, fade);
    }

    private void drawCenteredStringNoShadow(String text, int centerX, int y, int color) {
        if (text == null || text.isEmpty()) {
            return;
        }
        fontRenderer.drawString(text, centerX - fontRenderer.getStringWidth(text) / 2.0F, y, color, false);
    }

    private void drawSpeechDivot(int x, int y, int rotation, int accentColor, float fade) {
        int fillColor = ((int) (fade * 205.0F) << 24) | 0x10161D;
        int edgeColor = ((int) (fade * 160.0F) << 24) | 0xE7E0D0;
        int accent = withAlpha(blendColors(0xD5CCB8, accentColor, 0.75F), fade * 148.0F);
        if (rotation == 180) {
            drawSpeechTriangleDown(x + 4, y + 8, fillColor, edgeColor, accent);
        } else if (rotation == 90) {
            drawSpeechTriangleLeft(x, y + 4, fillColor, edgeColor, accent);
        } else if (rotation == 270) {
            drawSpeechTriangleRight(x + 8, y + 4, fillColor, edgeColor, accent);
        } else {
            drawSpeechTriangleUp(x + 4, y, fillColor, edgeColor, accent);
        }
    }

    private void drawSpeechTriangleDown(int tipX, int tipY, int fillColor, int edgeColor, int accentColor) {
        drawFilledTriangle(tipX - 7, tipY - 7, tipX + 7, tipY - 7, tipX, tipY, fillColor);
        drawLineSegment(tipX - 7, tipY - 7, tipX, tipY, edgeColor, 1.5F);
        drawLineSegment(tipX + 7, tipY - 7, tipX, tipY, accentColor, 1.5F);
    }

    private void drawSpeechTriangleUp(int tipX, int tipY, int fillColor, int edgeColor, int accentColor) {
        drawFilledTriangle(tipX - 7, tipY + 7, tipX + 7, tipY + 7, tipX, tipY, fillColor);
        drawLineSegment(tipX - 7, tipY + 7, tipX, tipY, edgeColor, 1.5F);
        drawLineSegment(tipX + 7, tipY + 7, tipX, tipY, accentColor, 1.5F);
    }

    private void drawSpeechTriangleLeft(int tipX, int tipY, int fillColor, int edgeColor, int accentColor) {
        drawFilledTriangle(tipX + 7, tipY - 7, tipX + 7, tipY + 7, tipX, tipY, fillColor);
        drawLineSegment(tipX + 7, tipY - 7, tipX, tipY, edgeColor, 1.5F);
        drawLineSegment(tipX + 7, tipY + 7, tipX, tipY, accentColor, 1.5F);
    }

    private void drawSpeechTriangleRight(int tipX, int tipY, int fillColor, int edgeColor, int accentColor) {
        drawFilledTriangle(tipX - 7, tipY - 7, tipX - 7, tipY + 7, tipX, tipY, fillColor);
        drawLineSegment(tipX - 7, tipY - 7, tipX, tipY, edgeColor, 1.5F);
        drawLineSegment(tipX - 7, tipY + 7, tipX, tipY, accentColor, 1.5F);
    }

    private void drawFilledTriangle(int x1, int y1, int x2, int y2, int x3, int y3, int color) {
        float alpha = ((color >>> 24) & 0xFF) / 255.0F;
        float red = ((color >>> 16) & 0xFF) / 255.0F;
        float green = ((color >>> 8) & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.color(red, green, blue, alpha);
        GL11.glBegin(GL11.GL_TRIANGLES);
        GL11.glVertex2f(x1 + 0.5F, y1 + 0.5F);
        GL11.glVertex2f(x2 + 0.5F, y2 + 0.5F);
        GL11.glVertex2f(x3 + 0.5F, y3 + 0.5F);
        GL11.glEnd();
        GlStateManager.enableTexture2D();
        GlStateManager.enableAlpha();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void drawHorizontalGradientRect(int left, int top, int right, int bottom, int leftColor, int rightColor) {
        float leftAlpha = ((leftColor >>> 24) & 0xFF) / 255.0F;
        float leftRed = ((leftColor >>> 16) & 0xFF) / 255.0F;
        float leftGreen = ((leftColor >>> 8) & 0xFF) / 255.0F;
        float leftBlue = (leftColor & 0xFF) / 255.0F;
        float rightAlpha = ((rightColor >>> 24) & 0xFF) / 255.0F;
        float rightRed = ((rightColor >>> 16) & 0xFF) / 255.0F;
        float rightGreen = ((rightColor >>> 8) & 0xFF) / 255.0F;
        float rightBlue = (rightColor & 0xFF) / 255.0F;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(right, top, 0.0D).color(rightRed, rightGreen, rightBlue, rightAlpha).endVertex();
        buffer.pos(left, top, 0.0D).color(leftRed, leftGreen, leftBlue, leftAlpha).endVertex();
        buffer.pos(left, bottom, 0.0D).color(leftRed, leftGreen, leftBlue, leftAlpha).endVertex();
        buffer.pos(right, bottom, 0.0D).color(rightRed, rightGreen, rightBlue, rightAlpha).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableTexture2D();
        GlStateManager.enableAlpha();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
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

    private Point getSpeechPointerTip(int boxX, int boxY, int boxWidth, int boxHeight, SpeechPointing pointing) {
        if (pointing == SpeechPointing.NONE) {
            return null;
        }
        if (pointing == SpeechPointing.UP) {
            return new Point(boxX + boxWidth / 2, boxY - 7);
        }
        if (pointing == SpeechPointing.LEFT) {
            return new Point(boxX - 7, boxY + boxHeight / 2);
        }
        if (pointing == SpeechPointing.RIGHT) {
            return new Point(boxX + boxWidth + 7, boxY + boxHeight / 2);
        }
        return new Point(boxX + boxWidth / 2, boxY + boxHeight + 7);
    }

    private void drawCaptionConnector(int startX, int startY, int endX, int endY, int color, float fade) {
        int alphaColor = ((int) (fade * 255.0F) << 24) | (color & 0x00FFFFFF);
        drawLineSegment(startX, startY, endX, endY, alphaColor, 2.0F);
        drawRect(startX - 1, startY - 1, startX + 2, startY + 2, alphaColor);
    }

    private void drawLineSegment(int startX, int startY, int endX, int endY, int color, float width) {
        float alpha = ((color >>> 24) & 0xFF) / 255.0F;
        float red = ((color >>> 16) & 0xFF) / 255.0F;
        float green = ((color >>> 8) & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.color(red, green, blue, alpha);
        GL11.glLineWidth(width);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex2f(startX + 0.5F, startY + 0.5F);
        GL11.glVertex2f(endX + 0.5F, endY + 0.5F);
        GL11.glEnd();
        GL11.glLineWidth(1.0F);
        GlStateManager.enableTexture2D();
        GlStateManager.enableAlpha();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private Point projectScenePoint(PonderScene scene, PreviewBounds bounds, PreviewLayout layout, float renderTick,
        Vec3d point) {
        if (point == null) {
            return null;
        }

        float scale = computePreviewScale(scene, bounds, layout);
        float centerX = (bounds.minX + bounds.maxX + 1) / 2.0F;
        float centerY = (bounds.minY + bounds.maxY + 1) / 2.0F + scene.getSceneOffsetY();
        float centerZ = (bounds.minZ + bounds.maxZ + 1) / 2.0F;
        float yaw = previewYaw + PonderSceneRuntime.getCameraYaw(scene, renderTick);
        float pitch = previewPitch;

        Vec3d relative = point.subtract(centerX, centerY, centerZ);
        Vec3d rotatedYaw = rotateY(relative, yaw);
        Vec3d rotated = rotateX(rotatedYaw, pitch);

        float screenX = layout.originX + layout.width / 2.0F + (float) (rotated.x * scale);
        float screenY = getPreviewAnchorY(layout) - (float) (rotated.y * scale);
        return new Point(Math.round(screenX), Math.round(screenY));
    }

    private ProjectedBounds projectSceneBounds(PonderScene scene, PreviewBounds bounds, PreviewLayout layout,
        float renderTick, AxisAlignedBB sceneBounds) {
        if (sceneBounds == null) {
            return null;
        }

        Vec3d[] corners = new Vec3d[] {
            new Vec3d(sceneBounds.minX, sceneBounds.minY, sceneBounds.minZ),
            new Vec3d(sceneBounds.minX, sceneBounds.minY, sceneBounds.maxZ),
            new Vec3d(sceneBounds.minX, sceneBounds.maxY, sceneBounds.minZ),
            new Vec3d(sceneBounds.minX, sceneBounds.maxY, sceneBounds.maxZ),
            new Vec3d(sceneBounds.maxX, sceneBounds.minY, sceneBounds.minZ),
            new Vec3d(sceneBounds.maxX, sceneBounds.minY, sceneBounds.maxZ),
            new Vec3d(sceneBounds.maxX, sceneBounds.maxY, sceneBounds.minZ),
            new Vec3d(sceneBounds.maxX, sceneBounds.maxY, sceneBounds.maxZ)
        };

        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (Vec3d corner : corners) {
            Point projected = projectScenePoint(scene, bounds, layout, renderTick, corner);
            if (projected == null) {
                continue;
            }
            minX = Math.min(minX, projected.x);
            minY = Math.min(minY, projected.y);
            maxX = Math.max(maxX, projected.x);
            maxY = Math.max(maxY, projected.y);
        }

        if (minX == Integer.MAX_VALUE) {
            return null;
        }

        minX = MathHelper.clamp(minX - 1, layout.originX, layout.originX + layout.width - 1);
        minY = MathHelper.clamp(minY - 1, layout.originY, layout.originY + layout.height - 1);
        maxX = MathHelper.clamp(maxX + 1, minX + 2, layout.originX + layout.width);
        maxY = MathHelper.clamp(maxY + 1, minY + 2, layout.originY + layout.height);
        return new ProjectedBounds(minX, minY, maxX, maxY);
    }

    private PonderScene.PoiEvent getActivePoiEvent(PonderScene scene, float currentTick) {
        if (scene == null) {
            return null;
        }
        PonderScene.PoiEvent active = null;
        for (PonderScene.PoiEvent event : scene.getPointOfInterestEvents()) {
            if (event.getTick() <= currentTick) {
                active = event;
            } else {
                break;
            }
        }
        return active;
    }

    private int getParticleColor(String particleName) {
        if (particleName == null) {
            return 0xF2D28C;
        }
        int separator = particleName.indexOf(':');
        if (separator >= 0 && particleName.regionMatches(true, 0, "REDSTONE", 0, "REDSTONE".length())) {
            String hex = particleName.substring(separator + 1).trim();
            try {
                return Integer.parseInt(hex, 16) & 0xFFFFFF;
            } catch (NumberFormatException ignored) {
            }
        }
        String upper = particleName.toUpperCase();
        if (upper.contains("FLAME") || upper.contains("LAVA")) {
            return 0xFFB347;
        }
        if (upper.contains("PORTAL")) {
            return 0xB084F5;
        }
        if (upper.contains("BUBBLE")) {
            return 0x8FD3FF;
        }
        if (upper.contains("REDSTONE")) {
            return 0xFF6B6B;
        }
        return 0xF2D28C;
    }

    private int getActorBaseColor(ActorRuntimeState actor) {
        if (actor.kind == PonderScene.ActorKind.CART) {
            return 0xD9C27A;
        }
        if (actor.kind == PonderScene.ActorKind.ITEM) {
            return 0xE0D6A8;
        }
        if (isBirbDancePose(actor)) {
            return 0xE58ACF;
        }
        if (isBirbCursorPose(actor)) {
            return 0x7FCDE0;
        }
        if (isBirbPoiPose(actor)) {
            return 0x82D173;
        }
        return 0xA8D89A;
    }

    private String getActorLabel(ActorRuntimeState actor) {
        if (actor.kind == PonderScene.ActorKind.CART) {
            return "CART";
        }
        if (actor.kind == PonderScene.ActorKind.ITEM) {
            return actor.displayName == null || actor.displayName.isEmpty() ? "ITEM" : actor.displayName;
        }
        if (isBirbDancePose(actor)) {
            return "BIRB Dance";
        }
        if (isBirbCursorPose(actor)) {
            return "BIRB Cursor";
        }
        if (isBirbPoiPose(actor)) {
            return "BIRB POI";
        }
        return actor.poseName == null || actor.poseName.isEmpty() ? "BIRB" : "BIRB " + actor.poseName;
    }

    private float getActorBobOffset(ActorRuntimeState actor, float currentTick) {
        if (actor.kind == PonderScene.ActorKind.CART) {
            return MathHelper.sin(currentTick * 0.15F + actor.actorId) * 0.01F;
        }
        if (actor.kind == PonderScene.ActorKind.ITEM) {
            return Math.abs(MathHelper.sin(currentTick * 0.22F + actor.actorId * 0.4F)) * 0.05F;
        }
        if (isBirbDancePose(actor)) {
            return Math.abs(MathHelper.sin(currentTick * 0.35F + actor.actorId * 0.5F)) * 0.10F;
        }
        return Math.abs(MathHelper.sin(currentTick * 0.18F + actor.actorId * 0.35F)) * 0.04F;
    }

    private float getActorYawOffset(ActorRuntimeState actor, float currentTick) {
        if (actor.kind != PonderScene.ActorKind.BIRB) {
            return 0.0F;
        }
        if (isBirbCursorPose(actor)) {
            return MathHelper.sin(currentTick * 0.30F + actor.actorId) * 18.0F;
        }
        if (isBirbDancePose(actor)) {
            return MathHelper.sin(currentTick * 0.45F + actor.actorId) * 10.0F;
        }
        return 0.0F;
    }

    private boolean isBirbDancePose(ActorRuntimeState actor) {
        return actor.poseName != null && actor.poseName.contains("DancePose");
    }

    private boolean isBirbCursorPose(ActorRuntimeState actor) {
        return actor.poseName != null && actor.poseName.contains("FaceCursorPose");
    }

    private boolean isBirbPoiPose(ActorRuntimeState actor) {
        return actor.poseName != null && actor.poseName.contains("FacePointOfInterestPose");
    }

    private List<ActorRuntimeState> getActorRuntimeStates(PonderScene scene, float currentTick) {
        if (scene == null) {
            return Collections.emptyList();
        }

        Map<Integer, ActorRuntimeState> actors = new LinkedHashMap<Integer, ActorRuntimeState>();
        for (PonderScene.ActorEvent event : scene.getActorEvents()) {
            if (event.getTick() > currentTick) {
                break;
            }

            ActorRuntimeState actor = actors.get(Integer.valueOf(event.getActorId()));
            if (event.getType() == PonderScene.ActorEventType.SPAWN) {
                actor = new ActorRuntimeState(event.getActorId(), event.getActorKind(),
                    event.getLocation() == null ? Vec3d.ZERO : event.getLocation());
                actor.poseName = event.getPoseName();
                actor.cartYaw = event.getAngle();
                actor.displayName = event.getDisplayName();
                actor.itemStack = event.getItemStack().copy();
                float progress = actorProgress(event, currentTick);
                actor.fade = progress;
                actor.visible = progress > 0.0F;
                actors.put(Integer.valueOf(event.getActorId()), actor);
                continue;
            }

            if (actor == null) {
                continue;
            }

            if (event.getType() == PonderScene.ActorEventType.MOVE && event.getOffset() != null) {
                actor.position = actor.position.add(event.getOffset().scale(actorProgress(event, currentTick)));
            } else if (event.getType() == PonderScene.ActorEventType.ROTATE) {
                if (event.getRotation() != null) {
                    actor.rotation = actor.rotation.add(event.getRotation().scale(actorProgress(event, currentTick)));
                } else {
                    actor.cartYaw += event.getAngle() * actorProgress(event, currentTick);
                }
            } else if (event.getType() == PonderScene.ActorEventType.POSE) {
                actor.poseName = event.getPoseName();
            } else if (event.getType() == PonderScene.ActorEventType.HIDE) {
                float progress = actorProgress(event, currentTick);
                actor.fade = 1.0F - progress;
                actor.visible = actor.fade > 0.0F;
            }
        }

        return new ArrayList<ActorRuntimeState>(actors.values());
    }

    private float actorProgress(PonderScene.ActorEvent event, float currentTick) {
        int duration = Math.max(1, event.getDuration());
        return MathHelper.clamp((currentTick - event.getTick() + 1.0F) / duration, 0.0F, 1.0F);
    }

    private Vec3d rotateY(Vec3d vec, float degrees) {
        double radians = Math.toRadians(degrees);
        double cosine = Math.cos(radians);
        double sine = Math.sin(radians);
        double x = vec.x * cosine + vec.z * sine;
        double z = vec.z * cosine - vec.x * sine;
        return new Vec3d(x, vec.y, z);
    }

    private Vec3d rotateX(Vec3d vec, float degrees) {
        double radians = Math.toRadians(degrees);
        double cosine = Math.cos(radians);
        double sine = Math.sin(radians);
        double y = vec.y * cosine - vec.z * sine;
        double z = vec.y * sine + vec.z * cosine;
        return new Vec3d(vec.x, y, z);
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
            : computeOverlayFade(caption.overlayEvent.getTick(), caption.overlayEvent.getDuration(), currentTick);
    }

    private float computeOverlayFade(int startTick, int duration, float currentTick) {
        float fadeIn = MathHelper.clamp((currentTick - startTick + 1.0F) / 6.0F, 0.0F, 1.0F);
        float fadeOut = MathHelper.clamp((startTick + duration - currentTick) / 6.0F, 0.0F, 1.0F);
        return Math.min(fadeIn, fadeOut);
    }

    protected ShowcaseGroupState getShowcaseGroupState() {
        ResourceLocation componentId = getSelectedComponentId();
        if (componentId == null) {
            return null;
        }

        PonderTag bestTag = null;
        List<ResourceLocation> bestComponents = Collections.emptyList();
        for (PonderTag tag : PonderIndex.getTagAccess().getTags(componentId)) {
            if (!isShowcaseGroupCandidate(tag)) {
                continue;
            }

            Set<ResourceLocation> groupedComponents = PonderIndex.getTagAccess().getItems(tag);
            if (groupedComponents.size() <= 1) {
                continue;
            }

            List<ResourceLocation> sortedComponents = new ArrayList<ResourceLocation>(groupedComponents);
            Collections.sort(sortedComponents, new Comparator<ResourceLocation>() {
                @Override
                public int compare(ResourceLocation left, ResourceLocation right) {
                    boolean leftSelected = left.equals(getSelectedComponentId());
                    boolean rightSelected = right.equals(getSelectedComponentId());
                    if (leftSelected != rightSelected) {
                        return leftSelected ? -1 : 1;
                    }
                    return left.toString().compareTo(right.toString());
                }
            });

            if (bestTag == null || sortedComponents.size() > bestComponents.size()) {
                bestTag = tag;
                bestComponents = sortedComponents;
            }
        }

        return bestTag == null ? null : new ShowcaseGroupState(bestTag, bestComponents);
    }

    private boolean isShowcaseGroupCandidate(PonderTag tag) {
        if (tag == null || tag.getId() == null) {
            return false;
        }
        ResourceLocation id = tag.getId();
        return !PonderTag.Highlight.ALL.equals(id)
            && !(Ponder.MOD_ID.equals(id.getNamespace()) && "not_registered".equals(id.getPath()));
    }

    protected ItemStack createComponentStack(ResourceLocation componentId) {
        return componentId == null ? ItemStack.EMPTY : PonderIndex.getSceneAccess().getDisplayStack(componentId);
    }

    private void drawOperationList(int mouseX, int mouseY, int x, int startY, int width, int bottom) {
        List<RecordedOperation> operations = getSelectedRecordedOperations();
        drawString(fontRenderer, "Timeline", x + 6, startY - 14, 0xFFEEDD);

        if (operations.isEmpty()) {
            drawString(fontRenderer, "No recorded operations for the current scene.", x + 6, startY, 0xA0A0A0);
            return;
        }

        int visibleLines = Math.max(1, (bottom - startY - 18) / LINE_HEIGHT);
        int maxIndex = Math.min(operations.size(), operationScroll + visibleLines);
        int activeIndex = getActiveOperationIndex(operations, playbackTick);

        for (int index = operationScroll; index < maxIndex; index++) {
            RecordedOperation operation = operations.get(index);
            int y = startY + (index - operationScroll) * LINE_HEIGHT;
            boolean hovered = isWithin(mouseX, mouseY, x + 4, y - 1, x + width - 4, y + LINE_HEIGHT - 1);
            boolean active = index == activeIndex;
            boolean reached = operation.getTick() <= playbackTick;
            int background = active ? 0xCC6F5A1E : reached ? 0x663A4A28 : hovered ? 0x55303030 : 0x33202020;
            drawRect(x + 4, y - 1, x + width - 4, y + LINE_HEIGHT - 1, background);

            String label = "T+" + operation.getTick() + " " + operation.getDescription();
            drawString(fontRenderer, fontRenderer.trimStringToWidth(label, width - 12), x + 8, y + 1,
                active ? 0xFFFFFF : reached ? 0xE5F0D0 : 0xD0D0D0);
        }
    }

    private void drawTooltips(int mouseX, int mouseY) {
        int componentIndex = getComponentIndexAt(mouseX, mouseY);
        if (componentIndex >= 0) {
            drawHoveringText(Collections.singletonList(componentIds.get(componentIndex).toString()), mouseX, mouseY);
            return;
        }

        int operationIndex = getOperationIndexAt(mouseX, mouseY);
        if (operationIndex >= 0) {
            RecordedOperation operation = getSelectedRecordedOperations().get(operationIndex);
            drawHoveringText(Collections.singletonList("T+" + operation.getTick() + " " + operation.getDescription()),
                mouseX, mouseY);
        }
    }

    private int resolveInitialComponentIndex() {
        if (componentIds.isEmpty()) {
            return 0;
        }

        if (requestedComponentId != null) {
            for (int i = 0; i < componentIds.size(); i++) {
                if (requestedComponentId.equals(componentIds.get(i))) {
                    return i;
                }
            }
        }

        return 0;
    }

    private void reloadCurrentComponent(boolean preserveSceneIndex) {
        reloadCurrentComponentAt(preserveSceneIndex ? selectedSceneIndex : requestedSceneIndex);
    }

    private void reloadCurrentComponentAt(int preferredSceneIndex) {
        ResourceLocation componentId = getSelectedComponentId();
        compiledScenes = componentId == null ? Collections.<PonderScene>emptyList() : PonderIndex.getSceneAccess().compile(componentId);
        tileEntityPreviewCache.clear();
        selectedSceneIndex = MathHelper.clamp(preferredSceneIndex, 0, Math.max(0, compiledScenes.size() - 1));
        showcaseGroupSelectorOpen = false;

        playbackTick = 0;
        operationScroll = 0;
        playing = showcaseMode;
        resetPreviewCamera();
        centerOperationsOnActiveLine();
    }

    private void selectComponent(int componentIndex, int preferredSceneIndex) {
        if (componentIds.isEmpty()) {
            return;
        }
        selectedComponentIndex = MathHelper.clamp(componentIndex, 0, componentIds.size() - 1);
        componentScroll = MathHelper.clamp(componentScroll, 0, getMaxComponentScroll());
        reloadCurrentComponentAt(preferredSceneIndex);
        updateButtonState();
    }

    private void selectComponent(ResourceLocation componentId, int preferredSceneIndex) {
        if (componentId == null) {
            return;
        }
        for (int index = 0; index < componentIds.size(); index++) {
            if (componentId.equals(componentIds.get(index))) {
                selectComponent(index, preferredSceneIndex);
                return;
            }
        }
    }

    private void selectScene(int sceneIndex) {
        if (compiledScenes.isEmpty()) {
            selectedSceneIndex = 0;
            playbackTick = 0;
            operationScroll = 0;
            playing = false;
            tileEntityPreviewCache.clear();
            return;
        }

        selectedSceneIndex = MathHelper.clamp(sceneIndex, 0, compiledScenes.size() - 1);
        playbackTick = 0;
        operationScroll = 0;
        playing = showcaseMode;
        tileEntityPreviewCache.clear();
        showcaseGroupSelectorOpen = false;
        resetPreviewCamera();
        centerOperationsOnActiveLine();
    }

    private void stepPlaybackTick(int delta) {
        PonderScene scene = getSelectedScene();
        if (scene == null) {
            return;
        }
        setPlaybackTick(playbackTick + delta);
        playing = false;
        updateButtonState();
    }

    private void setPlaybackTick(int tick) {
        PonderScene scene = getSelectedScene();
        int maxTick = scene == null ? 0 : getSceneEndTick(scene);
        playbackTick = MathHelper.clamp(tick, 0, maxTick);
        centerOperationsOnActiveLine();
    }

    protected void updateButtonState() {
        boolean hasScenes = !compiledScenes.isEmpty();
        if (prevSceneButton != null) {
            prevSceneButton.enabled = hasScenes && selectedSceneIndex > 0;
        }
        if (nextSceneButton != null) {
            nextSceneButton.enabled = hasScenes && selectedSceneIndex + 1 < compiledScenes.size();
        }
        if (startButton != null) {
            startButton.enabled = hasScenes && (showcaseMode || playbackTick > 0);
        }
        if (playPauseButton != null) {
            playPauseButton.enabled = hasScenes;
            playPauseButton.displayString = playing ? "Pause" : "Play";
        }
        if (endButton != null) {
            endButton.enabled = hasScenes && playbackTick < getSceneEndTick(getSelectedScene());
        }
        if (debugButton != null) {
            debugButton.enabled = hasScenes;
        }
    }

    protected ResourceLocation getSelectedComponentId() {
        if (componentIds.isEmpty()) {
            return null;
        }
        return componentIds.get(MathHelper.clamp(selectedComponentIndex, 0, componentIds.size() - 1));
    }

    protected int getCompiledSceneCount() {
        return compiledScenes.size();
    }

    protected int getPlaybackTickValue() {
        return playbackTick;
    }

    protected boolean isPlaybackRunning() {
        return playing;
    }

    @Nullable
    protected PonderScene getNextScene() {
        return selectedSceneIndex + 1 < compiledScenes.size() ? compiledScenes.get(selectedSceneIndex + 1) : null;
    }

    protected void setNextUpCardBounds(int x, int y, int width, int height) {
        lastNextUpCardX = x;
        lastNextUpCardY = y;
        lastNextUpCardWidth = width;
        lastNextUpCardHeight = height;
    }

    protected int getSelectedSceneIndex() {
        return selectedSceneIndex;
    }

    protected PonderScene getSelectedScene() {
        if (compiledScenes.isEmpty()) {
            return null;
        }
        return compiledScenes.get(MathHelper.clamp(selectedSceneIndex, 0, compiledScenes.size() - 1));
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
        if (!playing) {
            return playbackTick;
        }
        return Math.min(getSceneEndTick(scene), playbackTick + Math.max(0.0F, partialTicks));
    }

    private int getActiveOperationIndex(List<RecordedOperation> operations, int currentTick) {
        int activeIndex = -1;
        for (int i = 0; i < operations.size(); i++) {
            if (operations.get(i).getTick() <= currentTick) {
                activeIndex = i;
            } else {
                break;
            }
        }
        return activeIndex;
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
        List<RecordedOperation> operations = getSelectedRecordedOperations();
        if (operations.isEmpty()) {
            operationScroll = 0;
            return;
        }

        int activeIndex = Math.max(0, getActiveOperationIndex(operations, playbackTick));
        int visibleLines = getVisibleOperationLines();
        operationScroll = MathHelper.clamp(activeIndex - visibleLines / 2, 0, getMaxOperationScroll());
    }

    private int getVisibleOperationLines() {
        int operationsTop = OUTER_MARGIN + HEADER_HEIGHT;
        int operationsBottom = height - OUTER_MARGIN - 46;
        return Math.max(1, (operationsBottom - operationsTop) / LINE_HEIGHT);
    }

    private int getMaxOperationScroll() {
        return Math.max(0, getSelectedRecordedOperations().size() - getVisibleOperationLines());
    }

    private int getMaxComponentScroll() {
        int componentTop = OUTER_MARGIN + 20;
        int componentBottom = height - OUTER_MARGIN - 28;
        int visible = Math.max(1, (componentBottom - componentTop - 6) / LINE_HEIGHT);
        return Math.max(0, componentIds.size() - visible);
    }

    private int getComponentIndexAt(int mouseX, int mouseY) {
        if (!isMouseOverComponentList(mouseX, mouseY)) {
            return -1;
        }

        int relativeY = mouseY - (OUTER_MARGIN + 20);
        int index = componentScroll + relativeY / LINE_HEIGHT;
        return index >= 0 && index < componentIds.size() ? index : -1;
    }

    protected ShowcaseGroupIcon getShowcaseGroupIconAt(int mouseX, int mouseY) {
        for (ShowcaseGroupIcon icon : lastShowcaseGroupIcons) {
            if (isWithin(mouseX, mouseY, icon.x, icon.y, icon.x + icon.width, icon.y + icon.height)) {
                return icon;
            }
        }
        return null;
    }

    protected boolean hasShowcaseGroupChoices() {
        ShowcaseGroupState groupState = getShowcaseGroupState();
        return groupState != null && groupState.components.size() > 1;
    }

    protected boolean isMouseOverPlaybackBar(int mouseX, int mouseY) {
        return lastPlaybackBarWidth > 0 && isWithin(mouseX, mouseY, lastPlaybackBarX, lastPlaybackBarY - 3,
            lastPlaybackBarX + lastPlaybackBarWidth, lastPlaybackBarY + lastPlaybackBarHeight + 4);
    }

    protected boolean isMouseOverNextUpCard(int mouseX, int mouseY) {
        return lastNextUpCardWidth > 0 && isWithin(mouseX, mouseY, lastNextUpCardX, lastNextUpCardY,
            lastNextUpCardX + lastNextUpCardWidth, lastNextUpCardY + lastNextUpCardHeight + 8);
    }

    private void seekPlaybackToMouse(int mouseX) {
        setPlaybackTick(estimatePlaybackTickForMouse(mouseX));
    }

    protected int estimatePlaybackTickForMouse(int mouseX) {
        PonderScene scene = getSelectedScene();
        if (scene == null || lastPlaybackBarWidth <= 0) {
            return 0;
        }

        int maxTick = Math.max(1, getSceneEndTick(scene));
        float progress = (mouseX - lastPlaybackBarX) / (float) Math.max(1, lastPlaybackBarWidth);
        progress = MathHelper.clamp(progress, 0.0F, 1.0F);
        return Math.round(progress * maxTick);
    }

    protected boolean isMouseOverShowcaseHeaderIcon(int mouseX, int mouseY) {
        return lastShowcaseHeaderIconSize > 0
            && isWithin(mouseX, mouseY, lastShowcaseHeaderIconX, lastShowcaseHeaderIconY,
                lastShowcaseHeaderIconX + lastShowcaseHeaderIconSize, lastShowcaseHeaderIconY + lastShowcaseHeaderIconSize);
    }

    private int getOperationIndexAt(int mouseX, int mouseY) {
        if (!isMouseOverOperations(mouseX, mouseY)) {
            return -1;
        }

        int relativeY = mouseY - (OUTER_MARGIN + HEADER_HEIGHT);
        int index = operationScroll + relativeY / LINE_HEIGHT;
        List<RecordedOperation> operations = getSelectedRecordedOperations();
        return index >= 0 && index < operations.size() ? index : -1;
    }

    private boolean isMouseOverComponentList(int mouseX, int mouseY) {
        return isWithin(mouseX, mouseY, OUTER_MARGIN + 4, OUTER_MARGIN + 20, OUTER_MARGIN + LEFT_PANEL_WIDTH - 4,
            height - OUTER_MARGIN - 28);
    }

    private boolean isMouseOverOperations(int mouseX, int mouseY) {
        int x = OUTER_MARGIN + LEFT_PANEL_WIDTH + OUTER_MARGIN;
        return isWithin(mouseX, mouseY, x + 4, OUTER_MARGIN + HEADER_HEIGHT, width - OUTER_MARGIN - 4,
            height - OUTER_MARGIN - 28);
    }

    private boolean isMouseOverPreview(int mouseX, int mouseY) {
        return lastPreviewLayout != null
            && isWithin(mouseX, mouseY, lastPreviewLayout.originX, lastPreviewLayout.originY,
                lastPreviewLayout.originX + lastPreviewLayout.width,
                lastPreviewLayout.originY + lastPreviewLayout.height);
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
        previewYaw = -45.0F;
        previewPitch = 26.0F;
        previewZoom = showcaseMode ? 1.24F : 1.0F;
        previewDragging = false;
        showcaseFadeTicks = 0;
    }

    private float getPreviewAnchorY(PreviewLayout layout) {
        return layout.originY + layout.height * (showcaseMode ? 0.85F : 0.82F);
    }

    private void renderScenePreview(PonderScene scene, PreviewBounds bounds, PonderSceneRuntime.RuntimeState runtimeState,
        PreviewLayout layout, float renderTick) {
        enablePreviewScissor(layout);

        int previousAmbientOcclusion = mc != null && mc.gameSettings != null ? mc.gameSettings.ambientOcclusion : 0;
        if (mc != null && mc.gameSettings != null) {
            // Force bright, readable previews instead of inheriting the user's world AO setting.
            mc.gameSettings.ambientOcclusion = 0;
        }

        try {
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT);
        GlStateManager.pushMatrix();
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        RenderHelper.enableGUIStandardItemLighting();
        mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        float viewportCenterX = layout.originX + layout.width / 2.0F;
        float viewportAnchorY = getPreviewAnchorY(layout);
        float scale = computePreviewScale(scene, bounds, layout);
        float centerX = (bounds.minX + bounds.maxX + 1) / 2.0F;
        float centerY = (bounds.minY + bounds.maxY + 1) / 2.0F;
        float centerZ = (bounds.minZ + bounds.maxZ + 1) / 2.0F;
        float animatedYaw = previewYaw + PonderSceneRuntime.getCameraYaw(scene, renderTick);

        GlStateManager.translate(viewportCenterX, viewportAnchorY, 190.0F);
        GlStateManager.scale(scale, -scale, scale);
        GlStateManager.rotate(previewPitch, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(animatedYaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(-centerX, -centerY - scene.getSceneOffsetY(), -centerZ);

        drawSceneShadow(bounds);

        BlockRendererDispatcher dispatcher = mc.getBlockRendererDispatcher();
        PreviewBlockAccess previewWorld = new PreviewBlockAccess(runtimeState);
        List<PonderSceneRuntime.RuntimeBlockState> visibleBlocks =
            new ArrayList<PonderSceneRuntime.RuntimeBlockState>(runtimeState.blocksByPosition.values());
        Collections.sort(visibleBlocks, new Comparator<PonderSceneRuntime.RuntimeBlockState>() {
            @Override
            public int compare(PonderSceneRuntime.RuntimeBlockState left, PonderSceneRuntime.RuntimeBlockState right) {
                int yCompare = Double.compare(left.renderCenterY, right.renderCenterY);
                if (yCompare != 0) {
                    return yCompare;
                }
                int zCompare = Double.compare(left.renderCenterZ, right.renderCenterZ);
                if (zCompare != 0) {
                    return zCompare;
                }
                return Double.compare(left.renderCenterX, right.renderCenterX);
            }
        });

        if (mc.entityRenderer != null) {
            mc.entityRenderer.enableLightmap();
        }
        setPreviewLightmap();

        for (PonderSceneRuntime.RuntimeBlockState block : visibleBlocks) {
            if (!block.visible || block.fade <= 0.0F) {
                continue;
            }

            IBlockState state = block.currentState;
            if (state == null) {
                continue;
            }

            renderBlockModelPreview(dispatcher, previewWorld, block, state);
            renderTileEntityPreview(block, state);
        }

        renderActorPreviews(scene, renderTick);

        if (mc.entityRenderer != null) {
            mc.entityRenderer.disableLightmap();
        }

        RenderHelper.disableStandardItemLighting();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.disableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();
        GlStateManager.disableDepth();
        disablePreviewScissor();
        } finally {
            if (mc != null && mc.gameSettings != null) {
                mc.gameSettings.ambientOcclusion = previousAmbientOcclusion;
            }
        }
    }

    private float computePreviewScale(PonderScene scene, PreviewBounds bounds, PreviewLayout layout) {
        int sizeX = bounds.maxX - bounds.minX + 1;
        int sizeY = bounds.maxY - bounds.minY + 1;
        int sizeZ = bounds.maxZ - bounds.minZ + 1;
        float projectedWidth = sizeX + sizeZ * 0.85F;
        float projectedHeight = sizeY + (sizeX + sizeZ) * 0.45F;
        float widthInset = showcaseMode ? 22.0F : 12.0F;
        float heightInset = showcaseMode ? 42.0F : 12.0F;
        float widthScale = (layout.width - widthInset) / Math.max(2.0F, projectedWidth);
        float heightScale = (layout.height - heightInset) / Math.max(2.0F, projectedHeight);
        float baseScale = Math.max(6.0F, Math.min(widthScale, heightScale));
        if (showcaseMode) {
            baseScale *= 1.28F;
        }
        return baseScale * previewZoom * Math.max(0.35F, scene.getScaleFactor());
    }

    private float computeBlockBrightness(PonderSceneRuntime.RuntimeBlockState block) {
        float base = showcaseMode ? 0.84F : 0.74F;
        float range = showcaseMode ? 0.22F : 0.28F;
        return MathHelper.clamp(base + block.fade * range, 0.68F, 1.06F);
    }

    private void renderBlockModelPreview(BlockRendererDispatcher dispatcher, PreviewBlockAccess previewWorld,
        PonderSceneRuntime.RuntimeBlockState block, IBlockState state) {
        if (state == null) {
            return;
        }

        IBlockState renderState = state;
        try {
            renderState = getActualStateCompat(state, previewWorld, block.pos);
        } catch (RuntimeException ignored) {
            renderState = state;
        }

        GlStateManager.pushMatrix();
        PonderSceneRuntime.applyRenderTransforms(block);
        float brightness = Math.min(1.0F, computeBlockBrightness(block));
        float alpha = MathHelper.clamp(0.28F + block.fade * 0.72F, 0.0F, 1.0F);
        GlStateManager.color(brightness, brightness, brightness, alpha);
        setPreviewLightmap();

        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        try {
            dispatcher.renderBlock(renderState, block.pos, previewWorld, buffer);
        } catch (RuntimeException ignored) {
            // Keep previews alive when third-party block models fail during tessellation.
        }
        Tessellator.getInstance().draw();

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }

    private static Method resolveBlockStateMethod() {
        try {
            Method method = IBlockState.class.getMethod("getActualState", IBlockAccess.class, BlockPos.class);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException ignored) {
        }

        try {
            Method method = IBlockState.class.getMethod("func_185899_b", IBlockAccess.class, BlockPos.class);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException ignored) {
        }

        return null;
    }

    private static IBlockState getActualStateCompat(IBlockState state, IBlockAccess world, BlockPos pos) {
        if (state == null || BLOCKSTATE_ACTUAL_STATE == null) {
            return state;
        }

        try {
            Object resolved = BLOCKSTATE_ACTUAL_STATE.invoke(state, world, pos);
            return resolved instanceof IBlockState ? (IBlockState) resolved : state;
        } catch (Exception ignored) {
            return state;
        }
    }

    @SuppressWarnings("unchecked")
    private void renderTileEntityPreview(PonderSceneRuntime.RuntimeBlockState block, IBlockState state) {
        if (state == null || mc == null || mc.world == null || !state.getBlock().hasTileEntity(state)) {
            return;
        }

        TileEntity tileEntity = getOrCreatePreviewTileEntity(block, state);
        if (tileEntity == null) {
            return;
        }

        try {
            tileEntity.setWorld(mc.world);
            tileEntity.setPos(block.pos);
            TileEntitySpecialRenderer<TileEntity> renderer = TileEntityRendererDispatcher.instance.getRenderer(tileEntity);
            if (renderer == null) {
                return;
            }

            GlStateManager.pushMatrix();
            PonderSceneRuntime.applyRenderTransforms(block);
            GlStateManager.translate(Vec3iAccessor.x(block.pos), Vec3iAccessor.y(block.pos), Vec3iAccessor.z(block.pos));
            setPreviewLightmap();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            renderer.render(tileEntity, 0.0D, 0.0D, 0.0D, 0.0F, -1, 1.0F);
            GlStateManager.popMatrix();
        } catch (RuntimeException ignored) {
            // Keep preview resilient even when third-party tile renderers throw.
        }
    }

    private TileEntity getOrCreatePreviewTileEntity(PonderSceneRuntime.RuntimeBlockState block, IBlockState state) {
        String cacheKey = block.pos.toLong() + "|" + state.toString() + "|"
            + (block.tileNbt == null ? "" : block.tileNbt);
        TileEntity cached = tileEntityPreviewCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        try {
            TileEntity created = state.getBlock().createTileEntity(mc.world, state);
            if (created == null) {
                return null;
            }
            created.setWorld(mc.world);
            created.setPos(block.pos);
            if (block.tileNbt != null && !block.tileNbt.trim().isEmpty()) {
                applyTileNbt(created, block.tileNbt);
            }
            tileEntityPreviewCache.put(cacheKey, created);
            return created;
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private void setPreviewLightmap() {
        int sky = PREVIEW_FULL_BRIGHT & 0xFFFF;
        int block = PREVIEW_FULL_BRIGHT >> 16 & 0xFFFF;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, sky, block);
    }

    private final class PreviewBlockAccess implements IBlockAccess {

        private final PonderSceneRuntime.RuntimeState runtimeState;

        private PreviewBlockAccess(PonderSceneRuntime.RuntimeState runtimeState) {
            this.runtimeState = runtimeState;
        }

        @Override
        public TileEntity getTileEntity(BlockPos pos) {
            PonderSceneRuntime.RuntimeBlockState block = runtimeState.blocksByPosition.get(pos);
            if (block == null || block.currentState == null) {
                return null;
            }
            if (!block.currentState.getBlock().hasTileEntity(block.currentState)
                && (block.tileNbt == null || block.tileNbt.isEmpty())) {
                return null;
            }
            return getOrCreatePreviewTileEntity(block, block.currentState);
        }

        @Override
        public int getCombinedLight(BlockPos pos, int lightValue) {
            return PREVIEW_FULL_BRIGHT;
        }

        @Override
        public IBlockState getBlockState(BlockPos pos) {
            PonderSceneRuntime.RuntimeBlockState block = runtimeState.blocksByPosition.get(pos);
            if (block == null || !block.visible || block.currentState == null) {
                return Blocks.AIR.getDefaultState();
            }
            return block.currentState;
        }

        @Override
        public boolean isAirBlock(BlockPos pos) {
            return getBlockState(pos).getBlock() == Blocks.AIR;
        }

        @Override
        public Biome getBiome(BlockPos pos) {
            return Biomes.PLAINS;
        }

        @Override
        public int getStrongPower(BlockPos pos, EnumFacing direction) {
            return getBlockState(pos).getStrongPower(this, pos, direction);
        }

        @Override
        public WorldType getWorldType() {
            return WorldType.FLAT;
        }

        @Override
        public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
            IBlockState state = getBlockState(pos);
            if (state == null || state.getBlock() == Blocks.AIR) {
                return _default;
            }
            return state.isSideSolid(this, pos, side);
        }
    }

    private void applyTileNbt(TileEntity tileEntity, String nbtRaw) {
        try {
            NBTTagCompound compound = JsonToNBT.getTagFromJson(nbtRaw);
            compound.setInteger("x", Vec3iAccessor.x(tileEntity.getPos()));
            compound.setInteger("y", Vec3iAccessor.y(tileEntity.getPos()));
            compound.setInteger("z", Vec3iAccessor.z(tileEntity.getPos()));
            tileEntity.readFromNBT(compound);
        } catch (NBTException ignored) {
            // Invalid external payload should not break rendering.
        } catch (RuntimeException ignored) {
            // Some TileEntities can reject partial payloads; keep preview alive.
        }
    }

    private void drawSceneShadow(PreviewBounds bounds) {
        float minX = bounds.minX - 0.35F;
        float maxX = bounds.maxX + 1.35F;
        float minZ = bounds.minZ - 0.35F;
        float maxZ = bounds.maxZ + 1.35F;
        float y = bounds.minY + 0.01F;

        GlStateManager.disableTexture2D();
        GlStateManager.color(0.0F, 0.0F, 0.0F, showcaseMode ? 0.12F : 0.06F);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3f(minX, y, minZ);
        GL11.glVertex3f(maxX, y, minZ);
        GL11.glVertex3f(maxX, y, maxZ);
        GL11.glVertex3f(minX, y, maxZ);
        GL11.glEnd();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void enablePreviewScissor(PreviewLayout layout) {
        ScaledResolution resolution = new ScaledResolution(mc);
        int scaleFactor = resolution.getScaleFactor();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(layout.originX * scaleFactor,
            mc.displayHeight - (layout.originY + layout.height) * scaleFactor, layout.width * scaleFactor,
            layout.height * scaleFactor);
    }

    private void disablePreviewScissor() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    private static final class ShowcaseCaption {
        private final PonderScene.OverlayEvent overlayEvent;

        private ShowcaseCaption(PonderScene.OverlayEvent overlayEvent) {
            this.overlayEvent = overlayEvent;
        }
    }

    protected static final class ShowcaseGroupState {
        protected final PonderTag tag;
        protected final List<ResourceLocation> components;

        private ShowcaseGroupState(PonderTag tag, List<ResourceLocation> components) {
            this.tag = tag;
            this.components = components;
        }
    }

    protected static final class ShowcaseGroupIcon {
        protected final ResourceLocation componentId;
        protected final PonderTag tag;
        protected final int x;
        protected final int y;
        protected final int width;
        protected final int height;

        private ShowcaseGroupIcon(ResourceLocation componentId, PonderTag tag, int x, int y, int width, int height) {
            this.componentId = componentId;
            this.tag = tag;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }

    private static final class Point {
        private final int x;
        private final int y;

        private Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    private static final class CaptionPlacement {
        private final int x;
        private final int y;
        private final SpeechPointing pointing;

        private CaptionPlacement(int x, int y, SpeechPointing pointing) {
            this.x = x;
            this.y = y;
            this.pointing = pointing;
        }
    }

    private static final class GuiOverlayPlacement {
        private final PonderScene.OverlayEvent overlayEvent;
        private final Point targetPoint;
        private final boolean aboveTarget;
        private final int panelX;
        private final int panelY;
        private final int panelWidth;
        private final int panelHeight;
        private final int drawX;
        private final int drawY;
        private final int drawWidth;
        private final int drawHeight;

        private GuiOverlayPlacement(PonderScene.OverlayEvent overlayEvent, Point targetPoint, boolean aboveTarget,
            int panelX, int panelY, int panelWidth, int panelHeight, int drawX, int drawY, int drawWidth,
            int drawHeight) {
            this.overlayEvent = overlayEvent;
            this.targetPoint = targetPoint;
            this.aboveTarget = aboveTarget;
            this.panelX = panelX;
            this.panelY = panelY;
            this.panelWidth = panelWidth;
            this.panelHeight = panelHeight;
            this.drawX = drawX;
            this.drawY = drawY;
            this.drawWidth = drawWidth;
            this.drawHeight = drawHeight;
        }
    }

    private static final class GuiHighlightPlacement {
        private final PonderScene.OverlayEvent overlayEvent;
        private final int rectX;
        private final int rectY;
        private final int rectWidth;
        private final int rectHeight;
        private final Point targetPoint;

        private GuiHighlightPlacement(PonderScene.OverlayEvent overlayEvent, int rectX, int rectY, int rectWidth,
            int rectHeight, Point targetPoint) {
            this.overlayEvent = overlayEvent;
            this.rectX = rectX;
            this.rectY = rectY;
            this.rectWidth = rectWidth;
            this.rectHeight = rectHeight;
            this.targetPoint = targetPoint;
        }
    }

    protected static final class PreviewLayout {
        protected final int originX;
        protected final int originY;
        protected final int width;
        protected final int height;

        private PreviewLayout(int originX, int originY, int width, int height) {
            this.originX = originX;
            this.originY = originY;
            this.width = width;
            this.height = height;
        }
    }

    private static final class ProjectedBounds {
        private final int minX;
        private final int minY;
        private final int maxX;
        private final int maxY;

        private ProjectedBounds(int minX, int minY, int maxX, int maxY) {
            this.minX = minX;
            this.minY = minY;
            this.maxX = maxX;
            this.maxY = maxY;
        }
    }

    private static final class ActorRuntimeState {
        private final int actorId;
        private final PonderScene.ActorKind kind;
        private Vec3d position;
        private Vec3d rotation = Vec3d.ZERO;
        private float cartYaw;
        private float fade = 1.0F;
        private boolean visible = true;
        private String poseName;
        private String displayName;
        private ItemStack itemStack = ItemStack.EMPTY;

        private ActorRuntimeState(int actorId, PonderScene.ActorKind kind, Vec3d position) {
            this.actorId = actorId;
            this.kind = kind;
            this.position = position;
        }
    }

    private enum SpeechPointing {
        NONE,
        DOWN,
        LEFT,
        RIGHT,
        UP
    }

}
