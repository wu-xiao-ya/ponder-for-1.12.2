package net.createmod.ponder.foundation.external.execute;

import java.util.List;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;

public record CompoundOperation(List<Operation> children) implements Operation {
    @Override
    public void execute(SceneBuilder scene, SceneBuildingUtil util) {
        for (Operation child : children) {
            child.execute(scene, util);
        }
    }
}