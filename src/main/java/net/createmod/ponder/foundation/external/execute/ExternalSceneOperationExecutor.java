package net.createmod.ponder.foundation.external.execute;

import net.createmod.ponder.api.scene.SceneBuilder;

final class ExternalSceneOperationExecutor {

    private ExternalSceneOperationExecutor() {
    }

    static boolean execute(SceneBuilder scene, SceneOperation operation) {
        switch (operation.type) {
        case "base_plate":
            scene.configureBasePlate(operation.xOffset, operation.zOffset, operation.size);
            return true;
        case "show_base_plate":
            scene.showBasePlate();
            return true;
        case "scene_scale":
            scene.scaleSceneView(operation.factor);
            return true;
        case "scene_offset_y":
            scene.setSceneOffsetY(operation.offsetY);
            return true;
        case "remove_shadow":
            scene.removeShadow();
            return true;
        case "next_up":
            scene.setNextUpEnabled(operation.enabled);
            return true;
        case "idle":
            scene.idle(operation.ticks);
            return true;
        case "keyframe":
            scene.addKeyframe();
            return true;
        case "lazy_keyframe":
            scene.addLazyKeyframe();
            return true;
        case "mark_finished":
            scene.markAsFinished();
            return true;
        case "rotate_camera_y":
            scene.rotateCameraY(operation.degrees);
            return true;
        default:
            return false;
        }
    }
}
