package net.createmod.ponder.foundation.external.execute;

import java.util.List;

import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.createmod.ponder.foundation.external.validate.ExternalSelectionValidation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

final class ExternalSelectionHelper {

    private static final int MAX_EXTERNAL_CUBOID_VOLUME = 4096;
    private static final int MAX_EXTERNAL_CUBOID_EDGE = 64;

    private ExternalSelectionHelper() {
    }

    static Selection toSelection(SceneBuildingUtil util, SceneOperation operation) {
        if (operation.from != null && operation.to != null) {
            validateCuboidBounds(Math.min(operation.from[0], operation.to[0]), Math.max(operation.from[0], operation.to[0]),
                Math.min(operation.from[1], operation.to[1]), Math.max(operation.from[1], operation.to[1]),
                Math.min(operation.from[2], operation.to[2]), Math.max(operation.from[2], operation.to[2]));
            return util.select().fromTo(operation.from[0], operation.from[1], operation.from[2], operation.to[0],
                operation.to[1], operation.to[2]);
        }
        if (operation.pos != null) {
            return util.select().position(operation.pos[0], operation.pos[1], operation.pos[2]);
        }
        throw new IllegalArgumentException("Selection operation requires either from/to or pos");
    }

    static BlockPos toBlockPos(SceneBuildingUtil util, int[] pos) {
        if (pos == null) {
            throw new IllegalArgumentException("Block position is required");
        }
        return util.grid().at(pos[0], pos[1], pos[2]);
    }

    static Vec3d toVec(SceneBuildingUtil util, SceneOperation operation) {
        if (operation.pointAt == null) {
            return null;
        }

        if ("center".equalsIgnoreCase(operation.pointMode)) {
            return util.vector().centerOf((int) Math.round(operation.pointAt[0]), (int) Math.round(operation.pointAt[1]),
                (int) Math.round(operation.pointAt[2]));
        }
        if ("vector".equalsIgnoreCase(operation.pointMode)) {
            return util.vector().of(operation.pointAt[0], operation.pointAt[1], operation.pointAt[2]);
        }
        return util.vector().topOf((int) Math.round(operation.pointAt[0]), (int) Math.round(operation.pointAt[1]),
            (int) Math.round(operation.pointAt[2]));
    }

    static Vec3d toVec(double[] values) {
        if (values == null || values.length < 3) {
            return null;
        }
        return new Vec3d(values[0], values[1], values[2]);
    }

    static Vec3d resolveOverlayAnchor(SceneBuildingUtil util, SceneOperation operation) {
        if (operation.pointAt != null) {
            return toVec(util, operation);
        }
        if (operation.from != null && operation.to != null) {
            validateCuboidBounds(
                Math.min(operation.from[0], operation.to[0]), Math.max(operation.from[0], operation.to[0]),
                Math.min(operation.from[1], operation.to[1]), Math.max(operation.from[1], operation.to[1]),
                Math.min(operation.from[2], operation.to[2]), Math.max(operation.from[2], operation.to[2]));
        }
        return ExternalSelectionValidation.resolveSelectionCenter(util, operation.pos, operation.from, operation.to);
    }

    static Vec3d resolveRotationPivot(SceneBuildingUtil util, SceneOperation operation) {
        Vec3d explicit = toVec(operation.pivot);
        if (explicit != null) {
            return explicit;
        }
        if (operation.from != null && operation.to != null) {
            validateCuboidBounds(
                Math.min(operation.from[0], operation.to[0]), Math.max(operation.from[0], operation.to[0]),
                Math.min(operation.from[1], operation.to[1]), Math.max(operation.from[1], operation.to[1]),
                Math.min(operation.from[2], operation.to[2]), Math.max(operation.from[2], operation.to[2]));
        }
        return ExternalSelectionValidation.resolveRotationPivot(util, operation.pos, operation.from, operation.to);
    }

    static List<BlockPos> collectTargetPositions(SceneBuildingUtil util, SceneOperation operation) {
        if (operation.from != null && operation.to != null) {
            validateCuboidBounds(
                Math.min(operation.from[0], operation.to[0]), Math.max(operation.from[0], operation.to[0]),
                Math.min(operation.from[1], operation.to[1]), Math.max(operation.from[1], operation.to[1]),
                Math.min(operation.from[2], operation.to[2]), Math.max(operation.from[2], operation.to[2]));
        }
        return ExternalSelectionValidation.collectTargetPositions(util, operation.pos, operation.from, operation.to);
    }

    private static void validateCuboidBounds(int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
        long sizeX = (long) maxX - (long) minX + 1L;
        long sizeY = (long) maxY - (long) minY + 1L;
        long sizeZ = (long) maxZ - (long) minZ + 1L;
        if (sizeX <= 0L || sizeY <= 0L || sizeZ <= 0L) {
            throw new IllegalArgumentException("External ponder cuboid bounds overflow");
        }
        if (sizeX > MAX_EXTERNAL_CUBOID_EDGE || sizeY > MAX_EXTERNAL_CUBOID_EDGE
            || sizeZ > MAX_EXTERNAL_CUBOID_EDGE) {
            throw new IllegalArgumentException("External ponder cuboid edge exceeds limit " + MAX_EXTERNAL_CUBOID_EDGE);
        }
        long volume = sizeX * sizeY * sizeZ;
        if (volume > MAX_EXTERNAL_CUBOID_VOLUME) {
            throw new IllegalArgumentException("External ponder cuboid volume " + volume + " exceeds limit "
                + MAX_EXTERNAL_CUBOID_VOLUME);
        }
    }
}
