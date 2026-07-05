package net.createmod.ponder.foundation.ui.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

import org.lwjgl.opengl.GL11;

final class ScissorMath {

    private ScissorMath() {
    }

    static void apply(Minecraft minecraft, int x, int y, int width, int height) {
        ScaledResolution resolution = new ScaledResolution(minecraft);
        int scaleFactor = resolution.getScaleFactor();
        GL11.glScissor(x * scaleFactor, minecraft.displayHeight - (y + height) * scaleFactor, width * scaleFactor,
            height * scaleFactor);
    }
}
