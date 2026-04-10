package net.createmod.catnip.math;

public enum Pointing {
    UP(0),
    LEFT(270),
    DOWN(180),
    RIGHT(90);

    private final int xRotation;

    Pointing(int xRotation) {
        this.xRotation = xRotation;
    }

    public int getXRotation() {
        return xRotation;
    }
}
