package net.createmod.ponder.foundation.external.execute;

import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.util.math.Vec3d;

final class ExternalWorldOperationExecutor {

    private ExternalWorldOperationExecutor() {
    }

    static boolean execute(SceneBuilder scene, SceneBuildingUtil util, SceneOperation operation) {
        switch (operation.type) {
        case "set_blocks":
            scene.world().setBlocks(ExternalSelectionHelper.toSelection(util, operation),
                ExternalParseHelper.parseBlockState(operation.blockState, operation.blockMeta), true);
            ExternalOverlayEnqueuer.enqueueNbtWorldEvent(scene, util, operation, "set_blocks");
            return true;
        case "set_block":
            scene.world().setBlock(ExternalSelectionHelper.toBlockPos(util, operation.pos),
                ExternalParseHelper.parseBlockState(operation.blockState, operation.blockMeta), true);
            ExternalOverlayEnqueuer.enqueueNbtWorldEvent(scene, util, operation, "set_block");
            return true;
        case "replace_blocks":
            scene.world().replaceBlocks(ExternalSelectionHelper.toSelection(util, operation),
                ExternalParseHelper.parseBlockState(operation.blockState, operation.blockMeta), true);
            ExternalOverlayEnqueuer.enqueueNbtWorldEvent(scene, util, operation, "replace_blocks");
            return true;
        case "show_section":
            scene.world().showSection(ExternalSelectionHelper.toSelection(util, operation),
                ExternalParseHelper.parseFacing(operation.direction));
            return true;
        case "hide_section":
            scene.world().hideSection(ExternalSelectionHelper.toSelection(util, operation),
                ExternalParseHelper.parseFacing(operation.direction));
            return true;
        case "restore_blocks":
            scene.world().restoreBlocks(ExternalSelectionHelper.toSelection(util, operation));
            return true;
        case "destroy_block":
            scene.world().destroyBlock(ExternalSelectionHelper.toBlockPos(util, operation.pos));
            return true;
        case "break_progress":
            incrementBreakingProgress(scene, util, operation);
            return true;
        case "toggle_redstone_power":
            scene.world().toggleRedstonePower(ExternalSelectionHelper.toSelection(util, operation));
            return true;
        case "indicate_redstone":
            scene.effects().indicateRedstone(ExternalSelectionHelper.toBlockPos(util, operation.pos));
            return true;
        case "indicate_success":
            scene.effects().indicateSuccess(ExternalSelectionHelper.toBlockPos(util, operation.pos));
            return true;
        case "move_section":
            moveSection(scene, util, operation);
            return true;
        case "rotate_section":
            rotateSection(scene, util, operation);
            return true;
        default:
            return false;
        }
    }

    private static void incrementBreakingProgress(SceneBuilder scene, SceneBuildingUtil util,
        SceneOperation operation) {
        int repeats = Math.max(1, operation.times);
        for (int i = 0; i < repeats; i++) {
            scene.world().incrementBlockBreakingProgress(ExternalSelectionHelper.toBlockPos(util, operation.pos));
        }
    }

    private static void moveSection(SceneBuilder scene, SceneBuildingUtil util, SceneOperation operation) {
        Vec3d offset = ExternalSelectionHelper.toVec(operation.offset);
        if (offset == null) {
            throw new IllegalArgumentException("move_section operation requires offset");
        }
        scene.getScene().recordOperation("world.moveSection("
            + String.valueOf(ExternalSelectionHelper.toSelection(util, operation)) + ", " + offset + ", "
            + operation.duration + ")");
        scene.getScene().recordSectionMove(ExternalSelectionHelper.collectTargetPositions(util, operation), offset,
            operation.duration);
    }

    private static void rotateSection(SceneBuilder scene, SceneBuildingUtil util, SceneOperation operation) {
        Vec3d rotation = resolveRotation(operation);
        if (rotation == null) {
            throw new IllegalArgumentException("rotate_section operation requires rotation/degrees");
        }
        Vec3d pivot = ExternalSelectionHelper.resolveRotationPivot(util, operation);
        scene.getScene().recordOperation("world.rotateSection("
            + String.valueOf(ExternalSelectionHelper.toSelection(util, operation)) + ", " + rotation + ", pivot="
            + pivot + ", " + operation.duration + ")");
        scene.getScene().recordSectionRotation(ExternalSelectionHelper.collectTargetPositions(util, operation), pivot,
            rotation, operation.duration);
    }

    private static Vec3d resolveRotation(SceneOperation operation) {
        Vec3d vector = ExternalSelectionHelper.toVec(operation.rotation);
        if (vector != null) {
            return vector;
        }

        double x = operation.rotX;
        double y = operation.rotY;
        double z = operation.rotZ;
        if (Math.abs(x) >= 1.0E-4D || Math.abs(y) >= 1.0E-4D || Math.abs(z) >= 1.0E-4D) {
            return new Vec3d(x, y, z);
        }

        if (Math.abs(operation.degrees) >= 1.0E-4F) {
            return new Vec3d(0.0D, operation.degrees, 0.0D);
        }
        return null;
    }
}
