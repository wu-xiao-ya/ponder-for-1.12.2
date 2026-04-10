package net.createmod.ponder.api;

public enum PonderPalette {

    WHITE(0xEEEEEE),
    BLACK(0x221111),

    RED(0xFF5D6C),
    GREEN(0x8CBA51),
    BLUE(0x5F6CAF),

    SLOW(0x22FF22),
    MEDIUM(0x0084FF),
    FAST(0xFF55FF),

    INPUT(0x7FCDE0),
    OUTPUT(0xDDC166);

    private final int color;

    PonderPalette(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }
}
