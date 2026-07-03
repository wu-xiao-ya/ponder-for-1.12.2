package net.createmod.ponder.foundation.ui;

import java.util.Collections;
import java.util.List;

import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.PonderScene.RecordedOperation;

final class DebugPanelBridge {

    private final PonderDebugScreen screen;
    private final PonderSceneSelectionState selectionState;

    DebugPanelBridge(PonderDebugScreen screen, PonderSceneSelectionState selectionState) {
        this.screen = screen;
        this.selectionState = selectionState;
    }

    int getComponentIndexAt(int mouseX, int mouseY) {
        return screen.getDebugPanelRenderer().getComponentIndexAt(mouseX, mouseY, screen.height);
    }

    int getOperationIndexAt(int mouseX, int mouseY) {
        return screen.getDebugPanelRenderer().getOperationIndexAt(mouseX, mouseY, screen.height);
    }

    boolean isMouseOverComponentList(int mouseX, int mouseY) {
        return screen.getDebugPanelRenderer().isMouseOverComponentList(mouseX, mouseY, screen.height);
    }

    boolean isMouseOverOperations(int mouseX, int mouseY) {
        return screen.getDebugPanelRenderer().isMouseOverOperations(mouseX, mouseY, screen.height);
    }

    void scrollComponents(int delta) {
        screen.getDebugPanelRenderer().scrollComponents(delta, selectionState.getComponentIds(), screen.height);
    }

    void scrollOperations(int delta) {
        screen.getDebugPanelRenderer().scrollOperations(delta, screen.height);
    }

    void resetOperationScroll() {
        screen.getDebugPanelRenderer().resetOperationScroll();
    }

    void centerOperationsOnActiveLine() {
        if (getSelectedRecordedOperations().isEmpty()) {
            resetOperationScroll();
            return;
        }
        setOperationScroll(screen.getDebugPanelRenderer()
            .computeCenteredOperationScroll(screen.playbackState().getPlaybackTick(), screen.height));
    }

    int getMaxComponentScroll() {
        return screen.getDebugPanelRenderer().getMaxComponentScroll(selectionState.getComponentIds(), screen.height);
    }

    void setComponentScroll(int componentScroll) {
        screen.getDebugPanelRenderer().setComponentScroll(componentScroll);
    }

    void setOperationScroll(int operationScroll) {
        screen.getDebugPanelRenderer().setOperationScroll(operationScroll);
    }

    private List<RecordedOperation> getSelectedRecordedOperations() {
        PonderScene scene = selectionState.getSelectedScene();
        return scene == null ? Collections.<RecordedOperation>emptyList() : scene.getRecordedOperations();
    }
}
