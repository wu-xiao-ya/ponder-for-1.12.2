package net.createmod.ponder.foundation.ui;

import java.util.List;

import net.createmod.ponder.foundation.PonderScene;

final class DebugMouseHostAdapter implements DebugMouseController.Host {

    private final PonderDebugScreenHostSupport support;

    DebugMouseHostAdapter(PonderDebugScreenHostSupport support) {
        this.support = support;
    }

    @Override
    public boolean isMouseOverPlaybackBar(int mouseX, int mouseY) {
        return support.isMouseOverPlaybackBar(mouseX, mouseY);
    }

    @Override
    public void startPlaybackBarDragging() {
        support.startPlaybackBarDragging();
    }

    @Override
    public void seekPlaybackToMouse(int mouseX) {
        support.seekPlaybackToMouse(mouseX);
    }

    @Override
    public void pausePlayback() {
        support.pausePlayback();
    }

    @Override
    public void updateButtonState() {
        support.updateButtonState();
    }

    @Override
    public int getComponentIndexAt(int mouseX, int mouseY) {
        return support.getComponentIndexAt(mouseX, mouseY);
    }

    @Override
    public void selectComponent(int componentIndex) {
        support.selectComponent(componentIndex);
    }

    @Override
    public int getOperationIndexAt(int mouseX, int mouseY) {
        return support.getOperationIndexAt(mouseX, mouseY);
    }

    @Override
    public List<PonderScene.RecordedOperation> getSelectedRecordedOperations() {
        return support.getSelectedRecordedOperations();
    }

    @Override
    public void setPlaybackTick(int tick) {
        support.setPlaybackTick(tick);
    }

    @Override
    public boolean isMouseOverPreview(int mouseX, int mouseY) {
        return support.isMouseOverPreview(mouseX, mouseY);
    }

    @Override
    public void startPreviewDragging(int mouseX, int mouseY) {
        support.startPreviewDragging(mouseX, mouseY);
    }

    @Override
    public boolean isPreviewDragging() {
        return support.isPreviewDragging();
    }

    @Override
    public void dragPreview(int mouseX, int mouseY) {
        support.dragPreview(mouseX, mouseY);
    }

    @Override
    public boolean isPlaybackBarDragging() {
        return support.isPlaybackBarDragging();
    }

    @Override
    public void stopPlaybackBarDragging() {
        support.stopPlaybackBarDragging();
    }

    @Override
    public void stopPreviewDragging() {
        support.stopPreviewDragging();
    }

    @Override
    public boolean isShiftKeyDown() {
        return support.isShiftKeyDown();
    }

    @Override
    public void stepPlaybackTick(int delta) {
        support.stepPlaybackTick(delta);
    }

    @Override
    public void adjustPreviewZoom(float delta) {
        support.adjustPreviewZoom(delta);
    }

    @Override
    public boolean isMouseOverComponentList(int mouseX, int mouseY) {
        return support.isMouseOverComponentList(mouseX, mouseY);
    }

    @Override
    public void scrollComponents(int delta) {
        support.scrollComponents(delta);
    }

    @Override
    public boolean isMouseOverOperations(int mouseX, int mouseY) {
        return support.isMouseOverOperations(mouseX, mouseY);
    }

    @Override
    public void scrollOperations(int delta) {
        support.scrollOperations(delta);
    }
}
