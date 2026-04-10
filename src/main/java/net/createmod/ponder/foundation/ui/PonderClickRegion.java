package net.createmod.ponder.foundation.ui;

final class PonderClickRegion<T> {

    final T value;
    final int x;
    final int y;
    final int width;
    final int height;

    PonderClickRegion(T value, int x, int y, int width, int height) {
        this.value = value;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    boolean contains(int mouseX, int mouseY) {
        return AbstractPonderBrowserScreen.isWithin(mouseX, mouseY, x, y, width, height);
    }
}
