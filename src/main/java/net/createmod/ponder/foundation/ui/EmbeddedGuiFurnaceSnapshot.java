package net.createmod.ponder.foundation.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiFurnace;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.tileentity.TileEntityFurnace;
import org.lwjgl.opengl.GL11;

final class EmbeddedGuiFurnaceSnapshot implements PonderGuiSnapshotRegistry.SnapshotRenderer {

    static final EmbeddedGuiFurnaceSnapshot INSTANCE = new EmbeddedGuiFurnaceSnapshot();

    private final SnapshotFurnaceInventory furnaceInventory;
    private InventoryPlayer guiInventory;
    private EmbeddedGuiFurnace gui;

    private EmbeddedGuiFurnaceSnapshot() {
        this.furnaceInventory = new SnapshotFurnaceInventory();
    }

    @Override
    public void render(int x, int y, int width, int height, float currentTick, float fade) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.player == null) {
            return;
        }

        InventoryPlayer playerInventory = PlayerInventoryResolver.getSnapshot(mc.player);
        if (playerInventory == null) {
            return;
        }
        if (gui == null || guiInventory != playerInventory) {
            guiInventory = playerInventory;
            gui = new EmbeddedGuiFurnace(playerInventory, furnaceInventory);
        }

        furnaceInventory.updateForTick(currentTick);
        gui.renderEmbedded(mc, x, y, currentTick);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static final class SnapshotFurnaceInventory extends TileEntityFurnace {
        private SnapshotFurnaceInventory() {
            setCustomInventoryName("Furnace");
        }

        private void updateForTick(float currentTick) {
            int cycle = ((int) currentTick) % 200;
            int burnTime = cycle < 160 ? 160 - cycle : 0;
            int currentBurn = 160;
            int cookTime = cycle % 100;
            int totalCook = 100;

            setField(0, burnTime);
            setField(1, currentBurn);
            setField(2, cookTime);
            setField(3, totalCook);
        }
    }

    private static final class EmbeddedGuiFurnace extends GuiFurnace {
        private EmbeddedGuiFurnace(InventoryPlayer playerInv, TileEntityFurnace furnaceInv) {
            super(playerInv, furnaceInv);
        }

        @Override
        public void initGui() {
        }

        @Override
        protected void renderHoveredToolTip(int mouseX, int mouseY) {
        }

        @Override
        public void drawDefaultBackground() {
        }

        private void renderEmbedded(Minecraft mc, int x, int y, float partialTicks) {
            this.mc = mc;
            this.itemRender = mc.getRenderItem();
            this.fontRenderer = mc.fontRenderer;
            this.width = mc.displayWidth;
            this.height = mc.displayHeight;
            this.guiLeft = x;
            this.guiTop = y;
            enableSnapshotScissor(mc, x, y, this.xSize, this.ySize);
            try {
                this.drawScreen(-1000, -1000, partialTicks);
            } finally {
                GL11.glDisable(GL11.GL_SCISSOR_TEST);
            }
        }

        private void enableSnapshotScissor(Minecraft mc, int x, int y, int width, int height) {
            ScaledResolution resolution = new ScaledResolution(mc);
            int scaleFactor = resolution.getScaleFactor();
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            GL11.glScissor(x * scaleFactor, mc.displayHeight - (y + height) * scaleFactor, width * scaleFactor,
                height * scaleFactor);
        }
    }
}
