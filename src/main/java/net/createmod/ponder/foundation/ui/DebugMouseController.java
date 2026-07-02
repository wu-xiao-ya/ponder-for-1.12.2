package net.createmod.ponder.foundation.ui;

import java.util.List;

import net.createmod.ponder.foundation.PonderScene;

final class DebugMouseController {

    interface Host {
        boolean isMouseOverPlaybackBar(int mouseX, int mouseY);

        void startPlaybackBarDragging();

        void seekPlaybackToMouse(int mouseX);

        void pausePlayback();

        void updateButtonState();

        int getComponentIndexAt(int mouseX, int mouseY);

        void selectComponent(int componentIndex);

        int getOperationIndexAt(int mouseX, int mouseY);

        List<PonderScene.RecordedOperation> getSelectedRecordedOperations();

        void setPlaybackTick(int tick);

        boolean isMouseOverPreview(int mouseX, int mouseY);

        void startPreviewDragging(int mouseX, int mouseY);

        boolean isPreviewDragging();

        void dragPreview(int mouseX, int mouseY);

        boolean isPlaybackBarDragging();

        void stopPlaybackBarDragging();

        void stopPreviewDragging();

        boolean isShiftKeyDown();

        void stepPlaybackTick(int delta);

        void adjustPreviewZoom(float delta);

        boolean isMouseOverComponentList(int mouseX, int mouseY);

        void scrollComponents(int delta);

        boolean isMouseOverOperations(int mouseX, int mouseY);

        void scrollOperations(int delta);
    }

    private final Host host;

    DebugMouseController(Host host) {
        this.host = host;
    }

    boolean handleWheel(int mouseX, int mouseY, int wheel) {
        int delta = wheel > 0 ? -1 : 1;

        if (host.isMouseOverComponentList(mouseX, mouseY)) {
            host.scrollComponents(delta);
            return true;
        }
        if (host.isMouseOverOperations(mouseX, mouseY)) {
            host.scrollOperations(delta * 3);
            return true;
        }
        if (host.isMouseOverPreview(mouseX, mouseY)) {
            if (host.isShiftKeyDown()) {
                host.stepPlaybackTick(delta < 0 ? -1 : 1);
            } else {
                host.adjustPreviewZoom(wheel > 0 ? 0.12F : -0.12F);
            }
            return true;
        }
        return false;
    }

    boolean handleLeftClick(int mouseX, int mouseY) {
        if (host.isMouseOverPlaybackBar(mouseX, mouseY)) {
            host.startPlaybackBarDragging();
            host.seekPlaybackToMouse(mouseX);
            host.pausePlayback();
            host.updateButtonState();
            return true;
        }

        int componentIndex = host.getComponentIndexAt(mouseX, mouseY);
        if (componentIndex >= 0) {
            host.selectComponent(componentIndex);
            return true;
        }

        int operationIndex = host.getOperationIndexAt(mouseX, mouseY);
        if (operationIndex >= 0) {
            List<PonderScene.RecordedOperation> operations = host.getSelectedRecordedOperations();
            host.setPlaybackTick(operations.get(operationIndex).getTick());
            host.pausePlayback();
            host.updateButtonState();
            return true;
        }

        if (host.isMouseOverPreview(mouseX, mouseY)) {
            host.startPreviewDragging(mouseX, mouseY);
            return true;
        }
        return false;
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
