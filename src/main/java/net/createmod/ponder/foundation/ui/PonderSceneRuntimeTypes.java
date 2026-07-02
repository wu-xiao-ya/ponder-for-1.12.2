package net.createmod.ponder.foundation.ui;

import java.util.List;
import java.util.Map;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

final class PonderSceneRuntimeTypes {

    private PonderSceneRuntimeTypes() {
    }

    sealed interface TransformStep permits MoveStep, RotateStep {
    }

    static record MoveStep(Vec3d offset) implements TransformStep {
    }

    static record RotateStep(Vec3d pivot, Vec3d rotation) implements TransformStep {
    }

    static final class RuntimeBlockState {
        final BlockPos pos;
        IBlockState originalState;
        IBlockState currentState;
        boolean visible;
        float fade;
        float fadeOffsetX;
        float fadeOffsetY;
        float fadeOffsetZ;
        int breakingProgress;
        String stateDescription = "visible";
        String tileNbt;
        final List<TransformStep> transforms;
        double renderCenterX;
        double renderCenterY;
        double renderCenterZ;

        RuntimeBlockState(BlockPos pos, List<TransformStep> transforms) {
            this.pos = pos;
            this.transforms = transforms;
        }
    }

    static record RuntimeState(
        Map<BlockPos, RuntimeBlockState> blocksByPosition,
        Map<Long, PonderScenePreview.PreviewCellState> cellsByColumn,
        int visibleBlocks,
        int columnsWithBlocks
    ) {
    }
}
