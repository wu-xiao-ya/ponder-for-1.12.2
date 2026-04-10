package net.createmod.ponder.foundation.ui;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import net.createmod.ponder.foundation.PonderScene;

final class PonderScenePreview {

    private PonderScenePreview() {
    }

    static PreviewBounds computeBounds(PonderScene scene) {
        return PonderSceneRuntime.computeBounds(scene);
    }

    static PreviewState buildState(PonderScene scene, int tick) {
        PonderSceneRuntime.RuntimeState runtimeState = PonderSceneRuntime.buildState(scene, tick);
        return new PreviewState(runtimeState.cellsByColumn, runtimeState.visibleBlocks, runtimeState.columnsWithBlocks);
    }

    static int colorForState(String stateDescription) {
        if (stateDescription == null || stateDescription.isEmpty()) {
            return 0x9AA0A6;
        }

        String lower = stateDescription.toLowerCase(Locale.ROOT);
        if (lower.contains("gold")) {
            return 0xD1A93B;
        }
        if (lower.contains("quartz")) {
            return 0xD8D6CF;
        }
        if (lower.contains("crafting_table")) {
            return 0x8B633F;
        }
        if (lower.contains("destroy")) {
            return 0xAA5555;
        }
        if (lower.contains("visible") || lower.contains("restored")) {
            return 0x74A57F;
        }

        int hash = Math.abs(stateDescription.hashCode());
        int red = 72 + hash % 120;
        int green = 72 + hash / 7 % 120;
        int blue = 72 + hash / 29 % 120;
        return red << 16 | green << 8 | blue;
    }

    static int tintColor(int rgb, int y) {
        int boost = Math.min(70, y * 18);
        int red = Math.min(255, (rgb >> 16 & 0xFF) + boost);
        int green = Math.min(255, (rgb >> 8 & 0xFF) + boost);
        int blue = Math.min(255, (rgb & 0xFF) + boost);
        return red << 16 | green << 8 | blue;
    }

    static boolean isBasePlateCell(PonderScene scene, int x, int z) {
        return x >= scene.getBasePlateOffsetX()
            && x < scene.getBasePlateOffsetX() + scene.getBasePlateSize()
            && z >= scene.getBasePlateOffsetZ()
            && z < scene.getBasePlateOffsetZ() + scene.getBasePlateSize();
    }

    static long columnKey(int x, int z) {
        return ((long) x << 32) ^ (z & 0xFFFFFFFFL);
    }

    static final class PreviewCellState {
        int x;
        int z;
        int topY = Integer.MIN_VALUE;
        int visibleCount;
        int breakingProgress;
        String stateDescription = "visible";
    }

    static final class PreviewBounds {
        final int minX;
        final int maxX;
        final int minZ;
        final int maxZ;
        final int maxY;
        final int minY;

        PreviewBounds(int minX, int maxX, int minZ, int maxZ, int maxY, int minY) {
            this.minX = minX;
            this.maxX = maxX;
            this.minZ = minZ;
            this.maxZ = maxZ;
            this.maxY = maxY;
            this.minY = minY;
        }
    }

    static final class PreviewState {
        final Map<Long, PreviewCellState> cellsByColumn;
        final int visibleBlocks;
        final int columnsWithBlocks;

        PreviewState(Map<Long, PreviewCellState> cellsByColumn, int visibleBlocks, int columnsWithBlocks) {
            this.cellsByColumn = cellsByColumn;
            this.visibleBlocks = visibleBlocks;
            this.columnsWithBlocks = columnsWithBlocks;
        }
    }
}
