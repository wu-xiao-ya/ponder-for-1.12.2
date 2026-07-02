package net.createmod.ponder.foundation.ui;

@FunctionalInterface
public interface SnapshotRenderer {
    void render(int x, int y, int width, int height, float currentTick, float fade);

    sealed interface GuiSnapshotRenderer extends SnapshotRenderer
        permits EmbeddedGuiFurnaceSnapshot,
                EmbeddedReflectiveGuiSnapshot,
                EmbeddedReflectiveTabGuiSnapshot,
                SandboxTriggeredBlockGuiSnapshot {
    }

}
