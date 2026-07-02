package net.createmod.ponder.foundation.ui.projection;

public record SceneBounds(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {

    public int sizeX() {
        return Math.max(0, maxX - minX + 1);
    }

    public int sizeY() {
        return Math.max(0, maxY - minY + 1);
    }

    public int sizeZ() {
        return Math.max(0, maxZ - minZ + 1);
    }
}
