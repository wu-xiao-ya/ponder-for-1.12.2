package net.createmod.ponder.foundation.ui;

final class ShowcaseMouseHostAdapter implements ShowcaseMouseController.Host {

    private final PonderDebugScreenHostSupport support;

    ShowcaseMouseHostAdapter(PonderDebugScreenHostSupport support) {
        this.support = support;
    }

    @Override
    public boolean isMouseOverPreview(int mouseX, int mouseY) {
        return support.isMouseOverPreview(mouseX, mouseY);
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
    public ShowcaseGroupIconHitBox getShowcaseGroupIconAt(int mouseX, int mouseY) {
        return support.getShowcaseGroupIconAt(mouseX, mouseY);
    }

    @Override
    public void closeShowcaseGroupSelector() {
        support.closeShowcaseGroupSelector();
    }

    @Override
    public void selectComponent(ShowcaseGroupIconHitBox icon) {
        support.selectComponent(icon);
    }

    @Override
    public boolean isMouseOverNextUpCard(int mouseX, int mouseY) {
        return support.isMouseOverNextUpCard(mouseX, mouseY);
    }

    @Override
    public void selectNextScene() {
        support.selectNextScene();
    }

    @Override
    public boolean isMouseOverShowcaseHeaderIcon(int mouseX, int mouseY) {
        return support.isMouseOverShowcaseHeaderIcon(mouseX, mouseY);
    }

    @Override
    public boolean hasShowcaseGroupChoices() {
        return support.hasShowcaseGroupChoices();
    }

    @Override
    public void toggleShowcaseGroupSelector() {
        support.toggleShowcaseGroupSelector();
    }

    @Override
    public boolean isShowcaseGroupSelectorOpen() {
        return support.isShowcaseGroupSelectorOpen();
    }

    @Override
    public boolean isMouseInsideShowcaseGroupPopup(int mouseX, int mouseY) {
        return support.isMouseInsideShowcaseGroupPopup(mouseX, mouseY);
    }

    @Override
    public void startPreviewDragging(int mouseX, int mouseY) {
        support.startPreviewDragging(mouseX, mouseY);
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
    public boolean isPreviewDragging() {
        return support.isPreviewDragging();
    }

    @Override
    public void dragPreview(int mouseX, int mouseY) {
        support.dragPreview(mouseX, mouseY);
    }

    @Override
    public void stopPreviewDragging() {
        support.stopPreviewDragging();
    }
}
