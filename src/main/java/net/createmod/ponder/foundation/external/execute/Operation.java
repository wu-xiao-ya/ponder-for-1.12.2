package net.createmod.ponder.foundation.external.execute;

import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;

public sealed interface Operation permits SimpleOperation, CompoundOperation {
    void execute(SceneBuilder scene, SceneBuildingUtil util);
}