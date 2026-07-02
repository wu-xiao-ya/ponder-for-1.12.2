package net.createmod.ponder.foundation.ui;

import java.io.IOException;

final class DebugKeyboardHostAdapter implements DebugKeyboardController.Host {

    private final PonderDebugScreenHostSupport support;

    DebugKeyboardHostAdapter(PonderDebugScreenHostSupport support) {
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
    public void reloadRegisteredComponents() {
        support.reloadRegisteredComponents();
    }

    @Override
    public void reloadCurrentComponent(boolean preserveSceneIndex) {
        support.reloadCurrentComponent(preserveSceneIndex);
    }

    @Override
    public void selectScene(int sceneIndex) {
        support.selectScene(sceneIndex);
    }

    @Override
    public void stepPlaybackTick(int delta) {
        support.stepPlaybackTick(delta);
    }

    @Override
    public void seekToStart() {
        support.seekToStart();
    }

    @Override
    public void seekToEnd() {
        support.seekToEnd();
    }

    @Override
    public void updateButtonState() {
        support.updateButtonState();
    }

    @Override
    public void delegateKeyTyped(char typedChar, int keyCode) throws IOException {
        support.delegateKeyTyped(typedChar, keyCode);
    }
}
