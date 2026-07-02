package net.createmod.ponder.foundation.ui;

import java.util.Collections;
import java.util.List;

import net.createmod.ponder.foundation.PonderIndex;
import net.createmod.ponder.foundation.PonderScene;
import net.minecraft.util.math.MathHelper;

final class PonderSceneController {

    interface Host {
        PonderSceneSelectionState getSelectionState();

        PonderPlaybackState getPlaybackState();

        int getRequestedSceneIndex();

        void clearPreviewCaches();

        void setShowcaseGroupSelectorOpen(boolean open);

        void resetPreviewCamera();

        void resetOperationScroll();

        void centerOperationsOnActiveLine();

        void updateButtonState();

        int getMaxComponentScroll();

        void setComponentScroll(int componentScroll);

        int getSceneEndTick(PonderScene scene);
    }

    private final Host host;

    PonderSceneController(Host host) {
        this.host = host;
    }

    void reloadCurrentComponent(boolean preserveSceneIndex) {
        PonderSceneSelectionState selectionState = host.getSelectionState();
        reloadCurrentComponentAt(preserveSceneIndex ? selectionState.getSelectedSceneIndex() : host.getRequestedSceneIndex());
    }

    void reloadCurrentComponentAt(int preferredSceneIndex) {
        PonderSceneSelectionState selectionState = host.getSelectionState();
        PonderPlaybackState playbackState = host.getPlaybackState();
        net.minecraft.util.ResourceLocation componentId = selectionState.getSelectedComponentId();
        List<PonderScene> scenes = componentId == null
            ? Collections.<PonderScene>emptyList()
            : PonderIndex.getSceneAccess().compile(componentId);
        selectionState.setCompiledScenes(scenes);
        if (scenes.isEmpty()) {
            selectionState.setSelectedSceneIndex(0);
            playbackState.resetToStart(false);
            host.clearPreviewCaches();
            host.setShowcaseGroupSelectorOpen(false);
            host.resetOperationScroll();
            host.resetPreviewCamera();
            host.centerOperationsOnActiveLine();
            return;
        }
        selectionState.setSelectedSceneIndex(MathHelper.clamp(preferredSceneIndex, 0,
            selectionState.getCompiledSceneCount() - 1));
        host.clearPreviewCaches();
        host.setShowcaseGroupSelectorOpen(false);
        host.resetOperationScroll();
        playbackState.resetToStart(selectionState.isShowcaseMode());
        host.resetPreviewCamera();
        host.centerOperationsOnActiveLine();
    }

    void selectComponent(int componentIndex, int preferredSceneIndex) {
        PonderSceneSelectionState selectionState = host.getSelectionState();
        if (selectionState.getComponentIds().isEmpty()) {
            return;
        }
        selectionState.setSelectedComponentIndex(MathHelper.clamp(componentIndex, 0,
            Math.max(0, selectionState.getComponentIds().size() - 1)));
        host.setComponentScroll(MathHelper.clamp(host.getSelectionState().getComponentScroll(), 0, host.getMaxComponentScroll()));
        reloadCurrentComponentAt(preferredSceneIndex);
        host.updateButtonState();
    }

    void selectScene(int sceneIndex) {
        PonderSceneSelectionState selectionState = host.getSelectionState();
        PonderPlaybackState playbackState = host.getPlaybackState();
        if (selectionState.getCompiledScenes().isEmpty()) {
            selectionState.setSelectedSceneIndex(0);
            playbackState.resetToStart(false);
            host.resetOperationScroll();
            host.clearPreviewCaches();
            host.setShowcaseGroupSelectorOpen(false);
            host.resetPreviewCamera();
            host.centerOperationsOnActiveLine();
            return;
        }

        selectionState.setSelectedSceneIndex(MathHelper.clamp(sceneIndex, 0,
            Math.max(0, selectionState.getCompiledSceneCount() - 1)));
        playbackState.resetToStart(selectionState.isShowcaseMode());
        host.resetOperationScroll();
        host.clearPreviewCaches();
        host.setShowcaseGroupSelectorOpen(false);
        host.resetPreviewCamera();
        host.centerOperationsOnActiveLine();
    }

    void stepPlaybackTick(int delta) {
        PonderPlaybackState playbackState = host.getPlaybackState();
        PonderScene scene = host.getSelectionState().getSelectedScene();
        if (scene == null) {
            return;
        }
        setPlaybackTick(playbackState.getPlaybackTick() + delta);
        playbackState.stop();
        host.updateButtonState();
    }

    void setPlaybackTick(int tick) {
        PonderScene scene = host.getSelectionState().getSelectedScene();
        int maxTick = scene == null ? 0 : host.getSceneEndTick(scene);
        host.getPlaybackState().setPlaybackTick(MathHelper.clamp(tick, 0, maxTick));
        host.centerOperationsOnActiveLine();
    }

    void pausePlayback() {
        host.getPlaybackState().stop();
        host.updateButtonState();
    }

    void togglePlayback() {
        host.getPlaybackState().togglePlaying();
        host.updateButtonState();
    }

    void seekToStart(boolean keepPlaying) {
        setPlaybackTick(0);
        host.getPlaybackState().resetToStart(keepPlaying && host.getSelectionState().isShowcaseMode());
        host.updateButtonState();
    }

    void seekToEnd() {
        PonderScene scene = host.getSelectionState().getSelectedScene();
        setPlaybackTick(scene == null ? 0 : host.getSceneEndTick(scene));
        host.getPlaybackState().stop();
        host.updateButtonState();
    }

    void advanceTick() {
        PonderSceneSelectionState selectionState = host.getSelectionState();
        PonderPlaybackState playbackState = host.getPlaybackState();
        if (!playbackState.isPlaying()) {
            return;
        }

        PonderScene scene = selectionState.getSelectedScene();
        if (scene == null) {
            playbackState.stop();
            host.updateButtonState();
            return;
        }

        int maxTick = host.getSceneEndTick(scene);
        if (playbackState.getPlaybackTick() < maxTick) {
            setPlaybackTick(playbackState.getPlaybackTick() + 1);
            host.updateButtonState();
        } else if (selectionState.isShowcaseMode()) {
            setPlaybackTick(0);
            host.updateButtonState();
        } else {
            playbackState.stop();
            host.updateButtonState();
        }
    }
}
