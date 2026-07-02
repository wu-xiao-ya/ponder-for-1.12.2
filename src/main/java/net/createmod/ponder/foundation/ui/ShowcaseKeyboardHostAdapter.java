package net.createmod.ponder.foundation.ui;

import java.io.IOException;

import net.minecraft.client.gui.GuiScreen;

final class ShowcaseKeyboardHostAdapter implements ShowcaseKeyboardController.Host {

    private final PonderDebugScreenHostSupport support;

    ShowcaseKeyboardHostAdapter(PonderDebugScreenHostSupport support) {
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
    public void updateButtonState() {
        support.updateButtonState();
    }

    @Override
    public boolean allowDebugShortcutFromShowcase() {
        return support.allowDebugShortcutFromShowcase();
    }

    @Override
    public GuiScreen createDebugScreenFromShowcase() {
        return support.createDebugScreenFromShowcase();
    }

    @Override
    public void openScreen(GuiScreen screen) {
        support.openScreen(screen);
    }

    @Override
    public void delegateKeyTyped(char typedChar, int keyCode) throws IOException {
        support.delegateKeyTyped(typedChar, keyCode);
    }
}
