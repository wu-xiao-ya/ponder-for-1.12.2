package net.createmod.ponder.foundation.external.validate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class ExternalSelectionValidation {

    private ExternalSelectionValidation() {
    }

    public static List<BlockPos> collectTargetPositions(SceneBuildingUtil util, int[] pos, int[] from, int[] to) {
        if (pos != null) {
            return Collections.singletonList(util.grid().at(pos[0], pos[1], pos[2]));
        }

        if (from == null || to == null) {
            return Collections.emptyList();
        }

        int minX = Math.min(from[0], to[0]);
        int maxX = Math.max(from[0], to[0]);
        int minY = Math.min(from[1], to[1]);
        int maxY = Math.max(from[1], to[1]);
        int minZ = Math.min(from[2], to[2]);
        int maxZ = Math.max(from[2], to[2]);
        List<BlockPos> positions = new ArrayList<BlockPos>();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    positions.add(util.grid().at(x, y, z));
                }
            }
        }
        return positions;
    }

    public static Vec3d resolveSelectionCenter(SceneBuildingUtil util, int[] pos, int[] from, int[] to) {
        if (pos != null) {
            return util.vector().centerOf(pos[0], pos[1], pos[2]);
        }

        if (from == null || to == null) {
            return null;
        }

        int minX = Math.min(from[0], to[0]);
        int maxX = Math.max(from[0], to[0]);
        int minY = Math.min(from[1], to[1]);
        int maxY = Math.max(from[1], to[1]);
        int minZ = Math.min(from[2], to[2]);
        int maxZ = Math.max(from[2], to[2]);
        int midX = (minX + maxX) / 2;
        int midY = (minY + maxY) / 2;
        int midZ = (minZ + maxZ) / 2;
        return util.vector().centerOf(midX, midY, midZ);
    }

    public static Vec3d resolveRotationPivot(SceneBuildingUtil util, int[] pos, int[] from, int[] to) {
        if (pos != null) {
            return util.vector().centerOf(pos[0], pos[1], pos[2]);
        }

        if (from == null || to == null) {
            return new Vec3d(0.5D, 0.5D, 0.5D);
        }

        int minX = Math.min(from[0], to[0]);
        int maxX = Math.max(from[0], to[0]);
        int minY = Math.min(from[1], to[1]);
        int maxY = Math.max(from[1], to[1]);
        int minZ = Math.min(from[2], to[2]);
        int maxZ = Math.max(from[2], to[2]);
        return new Vec3d((minX + maxX + 1) / 2.0D, (minY + maxY + 1) / 2.0D, (minZ + maxZ + 1) / 2.0D);
    }

    public static Pointing resolvePointMode(String pointMode) {
        if (pointMode == null || pointMode.trim().isEmpty()) {
            return Pointing.UP;
        }
        try {
            return Pointing.valueOf(pointMode.trim().toUpperCase());
        } catch (IllegalArgumentException ignored) {
            return Pointing.UP;
        }
    }
}
