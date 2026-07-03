package net.createmod.ponder.foundation.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;

final class SnapshotGuiLayoutHelper {

    private SnapshotGuiLayoutHelper() {
    }

    static void refreshGuiReferences(GuiScreen gui, Minecraft mc, int width, int height) {
        SnapshotGuiReflectionHelper.setPossibleField(gui, "mc", mc);
        SnapshotGuiReflectionHelper.setPossibleField(gui, "field_146297_k", mc);
        SnapshotGuiReflectionHelper.setPossibleField(gui, "fontRenderer", mc.fontRenderer);
        SnapshotGuiReflectionHelper.setPossibleField(gui, "field_146289_q", mc.fontRenderer);
        SnapshotGuiReflectionHelper.setPossibleField(gui, "itemRender", mc.getRenderItem());
        SnapshotGuiReflectionHelper.setPossibleField(gui, "field_146296_j", mc.getRenderItem());
        SnapshotGuiReflectionHelper.setPossibleField(gui, "width", Integer.valueOf(width));
        SnapshotGuiReflectionHelper.setPossibleField(gui, "field_146294_l", Integer.valueOf(width));
        SnapshotGuiReflectionHelper.setPossibleField(gui, "height", Integer.valueOf(height));
        SnapshotGuiReflectionHelper.setPossibleField(gui, "field_146295_m", Integer.valueOf(height));
    }

    static void setGuiContainerPosition(GuiScreen gui, int x, int y) {
        SnapshotGuiReflectionHelper.setPossibleField(gui, "guiLeft", Integer.valueOf(x));
        SnapshotGuiReflectionHelper.setPossibleField(gui, "field_147003_i", Integer.valueOf(x));
        SnapshotGuiReflectionHelper.setPossibleField(gui, "guiTop", Integer.valueOf(y));
        SnapshotGuiReflectionHelper.setPossibleField(gui, "field_147009_r", Integer.valueOf(y));
    }

    static void enableSnapshotScissor(Minecraft mc, int x, int y, int width, int height) {
        ScaledResolution resolution = new ScaledResolution(mc);
        int scaleFactor = resolution.getScaleFactor();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(x * scaleFactor, mc.displayHeight - (y + height) * scaleFactor, width * scaleFactor,
            height * scaleFactor);
    }
}
