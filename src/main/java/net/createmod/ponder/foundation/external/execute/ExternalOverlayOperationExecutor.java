package net.createmod.ponder.foundation.external.execute;

import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;

final class ExternalOverlayOperationExecutor {

    private ExternalOverlayOperationExecutor() {
    }

    static boolean execute(SceneBuilder scene, SceneBuildingUtil util, SceneOperation operation) {
        switch (operation.type) {
        case "text":
            ExternalOverlayEnqueuer.enqueueTextOverlay(scene, util, operation,
                "overlay.showText(" + operation.duration + ")");
            return true;
        case "outline_text":
            ExternalOverlayEnqueuer.enqueueTextOverlay(scene, util, operation,
                "overlay.showOutlineWithText(" + String.valueOf(ExternalSelectionHelper.toSelection(util, operation))
                    + ", " + operation.duration + ")");
            return true;
        case "gui_texture":
            ExternalOverlayEnqueuer.enqueueGuiTextureOverlay(scene, util, operation);
            return true;
        case "gui_snapshot":
            ExternalOverlayEnqueuer.enqueueGuiSnapshotOverlay(scene, util, operation);
            return true;
        case "block_gui":
        case "machine_gui":
            ExternalOverlayEnqueuer.enqueueBlockGuiOverlay(scene, util, operation);
            return true;
        case "gui_outline_text":
            ExternalOverlayEnqueuer.enqueueGuiHighlightOverlay(scene, util, operation);
            return true;
        default:
            return false;
        }
    }
}
