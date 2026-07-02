package net.createmod.ponder.foundation.ui.projection;

public record ProjectedBounds(int minX, int minY, int maxX, int maxY) {

    public int width() {
        return Math.max(0, maxX - minX);
    }

    public int height() {
        return Math.max(0, maxY - minY);
    }
}
