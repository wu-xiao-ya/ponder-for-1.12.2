package net.createmod.ponder.foundation.ui;

import javax.annotation.Nullable;

import net.createmod.ponder.foundation.PonderScene;

final class PonderDebugHudTextProvider implements PonderTheme.HudTextProvider {

    private final PonderDebugScreen screen;

    PonderDebugHudTextProvider(PonderDebugScreen screen) {
        this.screen = screen;
    }

    @Override
    public String getPlaybackBarHoverLabel(int estimatedTick, @Nullable PonderScene scene) {
        return screen.getHoverHintForPlaybackBar(estimatedTick, scene);
    }

    @Override
    public String getGroupSelectorHoverLabel() {
        return screen.getHoverHintForGroupSelector();
    }

    @Override
    public String getNextUpHoverLabel() {
        return screen.getHoverHintForNextUp();
    }

    @Override
    public String getSceneShortLabel(int sceneIndex) {
        return "场景 " + sceneIndex;
    }

    @Override
    public String getNextUpLabel() {
        return "接下来";
    }
}
