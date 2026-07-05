package net.createmod.ponder.foundation.ui;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.PonderScene.RecordedOperation;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

final class PonderDebugScreenHostSupport implements ShowcaseHudRenderer.HoverLabelHost {

    private final PonderDebugScreen screen;
    private final PonderSceneSelectionState selectionState;
    private final PonderPlaybackState playbackState;
    private final InteractionHitCache interactionHitCache;
    private final int requestedSceneIndex;

    PonderDebugScreenHostSupport(PonderDebugScreen screen, PonderSceneSelectionState selectionState,
        PonderPlaybackState playbackState, InteractionHitCache interactionHitCache, int requestedSceneIndex) {
        this.screen = screen;
        this.selectionState = selectionState;
        this.playbackState = playbackState;
        this.interactionHitCache = interactionHitCache;
        this.requestedSceneIndex = requestedSceneIndex;
    }

    PonderSceneSelectionState getSelectionState() {
        return selectionState;
    }

    PonderPlaybackState getPlaybackState() {
        return playbackState;
    }

    int getRequestedSceneIndex() {
        return requestedSceneIndex;
    }

    void clearPreviewCaches() {
        screen.clearPreviewCaches();
    }

    @Override
    public boolean isMouseOverPlaybackBar(int mouseX, int mouseY) {
        return screen.isMouseOverPlaybackBar(mouseX, mouseY);
    }

    @Override
    public int estimatePlaybackTickForMouse(int mouseX) {
        return screen.estimatePlaybackTickForMouse(mouseX);
    }

    @Override
    public PonderScene getSelectedScene() {
        return screen.getSelectedScene();
    }

    @Override
    public ItemStack createComponentStack(ResourceLocation componentId) {
        return screen.createComponentStack(componentId);
    }

    void startPlaybackBarDragging() {
        interactionHitCache.setPlaybackBarDragging(true);
    }

    void seekPlaybackToMouse(int mouseX) {
        setPlaybackTick(screen.estimatePlaybackTickForMouse(mouseX));
    }

    void pausePlayback() {
        playbackState.stop();
    }

    void setPlaybackTick(int tick) {
        screen.setPlaybackTickFromHost(tick);
    }

    void updateButtonState() {
        screen.updateButtonState();
    }

    int getComponentIndexAt(int mouseX, int mouseY) {
        return screen.getDebugPanelBridge().getComponentIndexAt(mouseX, mouseY);
    }

    void selectComponent(int componentIndex) {
        screen.selectComponent(componentIndex, 0);
    }

    int getOperationIndexAt(int mouseX, int mouseY) {
        return screen.getDebugPanelBridge().getOperationIndexAt(mouseX, mouseY);
    }

    PreviewLayout getLastPreviewLayout() {
        return interactionHitCache.getPreviewLayout();
    }

    List<RecordedOperation> getSelectedRecordedOperations() {
        PonderScene scene = screen.getSelectedScene();
        return scene == null ? Collections.<RecordedOperation>emptyList() : scene.getRecordedOperations();
    }

    boolean isMouseOverPreview(int mouseX, int mouseY) {
        return interactionHitCache.isMouseOverPreview(mouseX, mouseY);
    }

    void startPreviewDragging(int mouseX, int mouseY) {
        screen.previewCameraState().startDragging(mouseX, mouseY);
    }

    boolean isPreviewDragging() {
        return screen.previewCameraState().isPreviewDragging();
    }

    void dragPreview(int mouseX, int mouseY) {
        screen.previewCameraState().dragTo(mouseX, mouseY);
    }

    boolean isPlaybackBarDragging() {
        return interactionHitCache.isPlaybackBarDragging();
    }

    void stopPlaybackBarDragging() {
        interactionHitCache.setPlaybackBarDragging(false);
    }

    void stopPreviewDragging() {
        screen.previewCameraState().stopDragging();
    }

    boolean isShiftKeyDown() {
        return screen.isShiftKeyDown();
    }

    void reloadRegisteredComponents() {
        selectionState.loadComponents(PonderDebugScreen.getRegisteredComponents());
        selectionState.setSelectedComponentIndex(MathHelper.clamp(selectionState.getSelectedComponentIndex(), 0,
            Math.max(0, selectionState.getComponentIds().size() - 1)));
    }

    void reloadCurrentComponent(boolean preserveSceneIndex) {
        screen.reloadCurrentComponentFromHost(preserveSceneIndex);
    }

    void selectScene(int sceneIndex) {
        screen.selectSceneFromHost(sceneIndex);
    }

    void stepPlaybackTick(int delta) {
        screen.stepPlaybackTickFromHost(delta);
    }

    void seekToStart() {
        screen.seekToStartFromHost();
    }

    void seekToEnd() {
        screen.seekToEndFromHost();
    }

    void setShowcaseGroupSelectorOpen(boolean open) {
        interactionHitCache.setShowcaseGroupSelectorOpen(open);
    }

    void resetPreviewCamera() {
        screen.resetPreviewCamera();
    }

    void resetOperationScroll() {
        screen.getDebugPanelBridge().resetOperationScroll();
    }

    void centerOperationsOnActiveLine() {
        screen.getDebugPanelBridge().centerOperationsOnActiveLine();
    }

    boolean allowDebugShortcutFromShowcase() {
        return screen.allowDebugShortcutFromShowcase();
    }

    GuiScreen createDebugScreenFromShowcase() {
        return screen.createDebugScreenFromShowcase();
    }

    void openScreen(GuiScreen screenToOpen) {
        screen.openScreenFromHost(screenToOpen);
    }

    void delegateKeyTyped(char typedChar, int keyCode) throws IOException {
        screen.delegateKeyTypedFromHost(typedChar, keyCode);
    }

    void adjustPreviewZoom(float delta) {
        screen.previewCameraState().adjustPreviewZoom(delta);
    }

    boolean isMouseOverComponentList(int mouseX, int mouseY) {
        return screen.getDebugPanelBridge().isMouseOverComponentList(mouseX, mouseY);
    }

    void scrollComponents(int delta) {
        screen.getDebugPanelBridge().scrollComponents(delta);
    }

    boolean isMouseOverOperations(int mouseX, int mouseY) {
        return screen.getDebugPanelBridge().isMouseOverOperations(mouseX, mouseY);
    }

    void scrollOperations(int delta) {
        screen.getDebugPanelBridge().scrollOperations(delta);
    }

    @Override
    public ShowcaseGroupIconHitBox getShowcaseGroupIconAt(int mouseX, int mouseY) {
        return interactionHitCache.getShowcaseGroupIconAt(mouseX, mouseY);
    }

    void closeShowcaseGroupSelector() {
        interactionHitCache.setShowcaseGroupSelectorOpen(false);
    }

    void selectComponent(ShowcaseGroupIconHitBox icon) {
        screen.selectComponent(icon.componentId, 0);
    }

    @Override
    public boolean isMouseOverNextUpCard(int mouseX, int mouseY) {
        return screen.isMouseOverNextUpCard(mouseX, mouseY);
    }

    void selectNextScene() {
        screen.selectSceneFromHost(selectionState.getSelectedSceneIndex() + 1);
    }

    @Override
    public boolean isMouseOverShowcaseHeaderIcon(int mouseX, int mouseY) {
        return screen.isMouseOverShowcaseHeaderIcon(mouseX, mouseY);
    }

    @Override
    public boolean hasShowcaseGroupChoices() {
        return screen.hasShowcaseGroupChoices();
    }

    void toggleShowcaseGroupSelector() {
        interactionHitCache.setShowcaseGroupSelectorOpen(!interactionHitCache.isShowcaseGroupSelectorOpen());
    }

    boolean isShowcaseGroupSelectorOpen() {
        return interactionHitCache.isShowcaseGroupSelectorOpen();
    }

    boolean isMouseInsideShowcaseGroupPopup(int mouseX, int mouseY) {
        return interactionHitCache.isMouseInsideShowcaseGroupPopup(mouseX, mouseY);
    }

    int getMaxComponentScroll() {
        return screen.getDebugPanelBridge().getMaxComponentScroll();
    }

    void setComponentScroll(int componentScroll) {
        screen.getDebugPanelBridge().setComponentScroll(componentScroll);
    }

    int getSceneEndTick(PonderScene scene) {
        return screen.getSceneEndTick(scene);
    }
}
