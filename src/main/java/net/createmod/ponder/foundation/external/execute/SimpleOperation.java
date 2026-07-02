package net.createmod.ponder.foundation.external.execute;

import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;

public record SimpleOperation(SceneOperation delegate) implements Operation {
    @Override
    public void execute(SceneBuilder scene, SceneBuildingUtil util) {
        ExternalSceneExecutor.executeSimple(scene, util, delegate);
    }
}