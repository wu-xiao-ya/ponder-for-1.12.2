package net.createmod.ponder.foundation.ui;

final class ShowcaseMouseController {

    interface Host {
        boolean isMouseOverPreview(int mouseX, int mouseY);

        boolean isShiftKeyDown();

        void stepPlaybackTick(int delta);

        void adjustPreviewZoom(float delta);

        boolean isMouseOverPlaybackBar(int mouseX, int mouseY);

        void startPlaybackBarDragging();

        void seekPlaybackToMouse(int mouseX);

        void pausePlayback();

        void updateButtonState();

        ShowcaseGroupIconHitBox getShowcaseGroupIconAt(int mouseX, int mouseY);

        void closeShowcaseGroupSelector();

        void selectComponent(ShowcaseGroupIconHitBox icon);

        boolean isMouseOverNextUpCard(int mouseX, int mouseY);

        void selectNextScene();

        boolean isMouseOverShowcaseHeaderIcon(int mouseX, int mouseY);

        boolean hasShowcaseGroupChoices();

        void toggleShowcaseGroupSelector();

        boolean isShowcaseGroupSelectorOpen();

        boolean isMouseInsideShowcaseGroupPopup(int mouseX, int mouseY);

        void startPreviewDragging(int mouseX, int mouseY);

        boolean isPlaybackBarDragging();

        void stopPlaybackBarDragging();

        boolean isPreviewDragging();

        void dragPreview(int mouseX, int mouseY);

        void stopPreviewDragging();
    }

    private final Host host;

    ShowcaseMouseController(Host host) {
        this.host = host;
    }

    boolean handleWheel(int mouseX, int mouseY, int wheel) {
        if (!host.isMouseOverPreview(mouseX, mouseY)) {
            return true;
        }
        int delta = wheel > 0 ? -1 : 1;
        if (host.isShiftKeyDown()) {
            host.stepPlaybackTick(delta < 0 ? -1 : 1);
        } else {
            host.adjustPreviewZoom(wheel > 0 ? 0.12F : -0.12F);
        }
        return true;
    }

    boolean handleLeftClick(int mouseX, int mouseY) {
        if (host.isMouseOverPlaybackBar(mouseX, mouseY)) {
            host.startPlaybackBarDragging();
            host.seekPlaybackToMouse(mouseX);
            host.pausePlayback();
            host.updateButtonState();
            return true;
        }

        ShowcaseGroupIconHitBox groupIcon = host.getShowcaseGroupIconAt(mouseX, mouseY);
        if (groupIcon != null) {
            host.closeShowcaseGroupSelector();
            host.selectComponent(groupIcon);
            return true;
        }

        if (host.isMouseOverNextUpCard(mouseX, mouseY)) {
            host.selectNextScene();
            host.updateButtonState();
            return true;
        }

        if (host.isMouseOverShowcaseHeaderIcon(mouseX, mouseY) && host.hasShowcaseGroupChoices()) {
            host.toggleShowcaseGroupSelector();
            return true;
        }

        if (host.isShowcaseGroupSelectorOpen() && !host.isMouseInsideShowcaseGroupPopup(mouseX, mouseY)) {
            host.closeShowcaseGroupSelector();
        }

        if (host.isMouseOverPreview(mouseX, mouseY)) {
            host.startPreviewDragging(mouseX, mouseY);
        }
        return true;
    }

    boolean handleDrag(int mouseX, int mouseY, int clickedMouseButton) {
        if (host.isPlaybackBarDragging() && clickedMouseButton == 0) {
            host.seekPlaybackToMouse(mouseX);
            host.pausePlayback();
            host.updateButtonState();
            return true;
        }

        if (!host.isPreviewDragging() || clickedMouseButton != 0 || !host.isMouseOverPreview(mouseX, mouseY)) {
            return false;
        }

        host.dragPreview(mouseX, mouseY);
        return true;
    }

    boolean handleRelease(int state) {
        if (state != 0) {
            return false;
        }
        host.stopPreviewDragging();
        host.stopPlaybackBarDragging();
        return true;
    }
}
