package net.createmod.ponder.foundation.ui;

import java.io.IOException;

import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Keyboard;

final class ShowcaseKeyboardController {

    interface Host {
        PonderSceneSelectionState getSelectionState();

        PonderPlaybackState getPlaybackState();

        void reloadRegisteredComponents();

        void reloadCurrentComponent(boolean preserveSceneIndex);

        void selectScene(int sceneIndex);

        void stepPlaybackTick(int delta);

        void updateButtonState();

        boolean allowDebugShortcutFromShowcase();

        GuiScreen createDebugScreenFromShowcase();

        void openScreen(GuiScreen screen);

        void delegateKeyTyped(char typedChar, int keyCode) throws IOException;
    }

    private final Host host;

    ShowcaseKeyboardController(Host host) {
        this.host = host;
    }

    boolean handle(char typedChar, int keyCode) throws IOException {
        PonderSceneSelectionState selectionState = host.getSelectionState();
        PonderPlaybackState playbackState = host.getPlaybackState();

        if (keyCode == Keyboard.KEY_SPACE) {
            playbackState.togglePlaying();
            host.updateButtonState();
            return true;
        }

        if (keyCode == Keyboard.KEY_R) {
            PonderIndex.reload();
            host.reloadRegisteredComponents();
            host.reloadCurrentComponent(true);
            host.updateButtonState();
            return true;
        }

        if (keyCode == Keyboard.KEY_LEFT) {
            host.selectScene(selectionState.getSelectedSceneIndex() - 1);
            host.updateButtonState();
            return true;
        }

        if (keyCode == Keyboard.KEY_RIGHT) {
            host.selectScene(selectionState.getSelectedSceneIndex() + 1);
            host.updateButtonState();
            return true;
        }

        if (keyCode == Keyboard.KEY_UP) {
            host.stepPlaybackTick(-1);
            return true;
        }

        if (keyCode == Keyboard.KEY_DOWN) {
            host.stepPlaybackTick(1);
            return true;
        }

        if (keyCode == Keyboard.KEY_D && host.allowDebugShortcutFromShowcase()) {
            host.openScreen(host.createDebugScreenFromShowcase());
            return true;
        }

        host.delegateKeyTyped(typedChar, keyCode);
        return true;
    }

    static void clampSelectionState(PonderSceneSelectionState selectionState) {
        selectionState.setSelectedComponentIndex(MathHelper.clamp(selectionState.getSelectedComponentIndex(), 0,
            Math.max(0, selectionState.getComponentIds().size() - 1)));
    }
}
