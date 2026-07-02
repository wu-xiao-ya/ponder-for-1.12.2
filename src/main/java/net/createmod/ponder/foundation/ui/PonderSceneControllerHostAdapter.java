package net.createmod.ponder.foundation.ui;

import net.createmod.ponder.foundation.PonderScene;

final class PonderSceneControllerHostAdapter implements PonderSceneController.Host {

    private final PonderDebugScreenHostSupport support;

    PonderSceneControllerHostAdapter(PonderDebugScreenHostSupport support) {
        this.support = support;
    }

    @Override
    public PonderSceneSelectionState getSelectionState() {
        return support.getSelectionState();
    }

    @Override
    public PonderPlaybackState getPlaybackState() {
        return support.getPlaybackState();
    }

    @Override
    public int getRequestedSceneIndex() {
        return support.getRequestedSceneIndex();
    }

    @Override
    public void clearPreviewCaches() {
        support.clearPreviewCaches();
    }

    @Override
    public void setShowcaseGroupSelectorOpen(boolean open) {
        support.setShowcaseGroupSelectorOpen(open);
    }

    @Override
    public void resetPreviewCamera() {
        support.resetPreviewCamera();
    }

    @Override
    public void resetOperationScroll() {
        support.resetOperationScroll();
    }

    @Override
    public void centerOperationsOnActiveLine() {
        support.centerOperationsOnActiveLine();
    }

    @Override
    public void updateButtonState() {
        support.updateButtonState();
    }

    @Override
    public int getMaxComponentScroll() {
        return support.getMaxComponentScroll();
    }

    @Override
    public void setComponentScroll(int componentScroll) {
        support.setComponentScroll(componentScroll);
    }

    @Override
    public int getSceneEndTick(PonderScene scene) {
        return support.getSceneEndTick(scene);
    }
}
