package net.createmod.ponder.foundation.external.register;

import java.util.ArrayList;
import java.util.List;

import net.createmod.ponder.api.scene.PonderStoryBoard;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.foundation.external.definition.SceneDefinition;
import net.createmod.ponder.foundation.external.definition.SceneOperationDefinition;
import net.createmod.ponder.foundation.external.execute.ExternalSceneExecutor;
import net.createmod.ponder.foundation.external.execute.SceneOperation;

public final class ExternalStoryBoardBuilder {

    private ExternalStoryBoardBuilder() {
    }

    public static PonderStoryBoard buildStoryBoard(final SceneDefinition definition) {
        return new PonderStoryBoard() {
            @Override
            public void program(SceneBuilder scene, SceneBuildingUtil util) {
                scene.title(definition.sceneId(), definition.title());
                for (SceneOperation operation : convertOperations(definition.operations())) {
                    ExternalSceneExecutor.applyOperation(scene, util, operation);
                }
            }
        };
    }

    public static List<SceneOperation> convertOperations(List<SceneOperationDefinition> definitions) {
        List<SceneOperation> operations = new ArrayList<SceneOperation>();
        for (SceneOperationDefinition definition : definitions) {
            operations.add(SceneOperation.fromDefinition(definition));
        }
        return operations;
    }
}
