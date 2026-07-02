package net.createmod.ponder.foundation.ui;

import net.createmod.ponder.foundation.PonderScene;
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
