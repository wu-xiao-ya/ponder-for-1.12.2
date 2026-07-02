package net.createmod.ponder.foundation.ui;

import java.util.Collections;
import java.util.List;

import net.createmod.ponder.foundation.PonderIndex;
import net.createmod.ponder.foundation.PonderScene;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

final class PonderSceneSelectionState {

    private final ResourceLocation requestedComponentId;
    private final int requestedSceneIndex;
    private final boolean showcaseMode;

    private List<ResourceLocation> componentIds = Collections.emptyList();
    private List<PonderScene> compiledScenes = Collections.emptyList();
    private int selectedComponentIndex;
    private int selectedSceneIndex;
    private int componentScroll;

    PonderSceneSelectionState(ResourceLocation requestedComponentId, int requestedSceneIndex, boolean showcaseMode) {
        this.requestedComponentId = requestedComponentId;
        this.requestedSceneIndex = Math.max(0, requestedSceneIndex);
        this.showcaseMode = showcaseMode;
    }

    void loadComponents(List<ResourceLocation> componentIds) {
        this.componentIds = componentIds == null ? Collections.<ResourceLocation>emptyList() : componentIds;
        this.selectedComponentIndex = resolveInitialComponentIndex(this.componentIds);
        this.componentScroll = 0;
    }

    int resolveInitialComponentIndex(List<ResourceLocation> componentIds) {
        if (requestedComponentId != null && componentIds != null) {
            int index = componentIds.indexOf(requestedComponentId);
            if (index >= 0) {
                return index;
            }
        }
        return 0;
    }

    void setCompiledScenes(List<PonderScene> compiledScenes) {
        this.compiledScenes = compiledScenes == null ? Collections.<PonderScene>emptyList() : compiledScenes;
        this.selectedSceneIndex = MathHelper.clamp(requestedSceneIndex, 0, Math.max(0, this.compiledScenes.size() - 1));
    }

    List<ResourceLocation> getComponentIds() {
        return componentIds;
    }

    List<PonderScene> getCompiledScenes() {
        return compiledScenes;
    }

    int getSelectedComponentIndex() {
        return selectedComponentIndex;
    }

    int getRequestedSceneIndex() {
        return requestedSceneIndex;
    }

    void setSelectedComponentIndex(int selectedComponentIndex) {
        this.selectedComponentIndex = MathHelper.clamp(selectedComponentIndex, 0, Math.max(0, componentIds.size() - 1));
    }

    int getSelectedSceneIndex() {
        return selectedSceneIndex;
    }

    void setSelectedSceneIndex(int selectedSceneIndex) {
        this.selectedSceneIndex = MathHelper.clamp(selectedSceneIndex, 0, Math.max(0, compiledScenes.size() - 1));
    }

    int getComponentScroll() {
        return componentScroll;
    }

    void setComponentScroll(int componentScroll) {
        this.componentScroll = Math.max(0, componentScroll);
    }

    boolean isShowcaseMode() {
        return showcaseMode;
    }

    ResourceLocation getSelectedComponentId() {
        if (componentIds.isEmpty() || selectedComponentIndex < 0 || selectedComponentIndex >= componentIds.size()) {
            return requestedComponentId;
        }
        return componentIds.get(selectedComponentIndex);
    }

    PonderScene getSelectedScene() {
        if (compiledScenes.isEmpty() || selectedSceneIndex < 0 || selectedSceneIndex >= compiledScenes.size()) {
            return null;
        }
        return compiledScenes.get(selectedSceneIndex);
    }

    int getCompiledSceneCount() {
        return compiledScenes.size();
    }
}
