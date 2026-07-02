package net.createmod.ponder.foundation.external.execute;

import java.util.ArrayList;
import java.util.List;

import net.createmod.ponder.Ponder;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;

public final class ExternalSceneExecutor {

    private ExternalSceneExecutor() {
    }

    public static void applyOperation(SceneBuilder scene, SceneBuildingUtil util, SceneOperation operation) {
        classify(operation).execute(scene, util);
    }

    private static Operation classify(SceneOperation operation) {
        String type = operation.type;
        if ("gui_interaction".equals(type) || "sequence".equals(type)) {
            List<Operation> children = new ArrayList<Operation>();
            for (SceneOperation child : operation.childOperations) {
                children.add(classify(child));
            }
            return new CompoundOperation(children);
        }
        return new SimpleOperation(operation);
    }

    static void executeSimple(SceneBuilder scene, SceneBuildingUtil util, SceneOperation operation) {
        if (ExternalSceneOperationExecutor.execute(scene, operation)
            || ExternalWorldOperationExecutor.execute(scene, util, operation)
            || ExternalOverlayOperationExecutor.execute(scene, util, operation)) {
            return;
        }

        Ponder.LOGGER.warn("Unsupported external ponder operation '{}'", operation.type);
    }

}
