package net.createmod.ponder.foundation.ui;

import java.io.IOException;

import net.createmod.ponder.foundation.PonderScene;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.math.MathHelper;

final class PonderDebugScreenHostSupport {

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
        screen.getDebugPanelRenderer().resetOperationScroll();
    }

    void centerOperationsOnActiveLine() {
        screen.centerOperationsOnActiveLine();
    }

    void updateButtonState() {
        screen.updateButtonState();
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

    int getMaxComponentScroll() {
        return screen.getDebugPanelRenderer().getMaxComponentScroll(selectionState.getComponentIds(), screen.height);
    }

    void setComponentScroll(int componentScroll) {
        screen.getDebugPanelRenderer().setComponentScroll(componentScroll);
    }

    int getSceneEndTick(PonderScene scene) {
        return screen.getSceneEndTick(scene);
    }
}
