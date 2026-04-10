package net.createmod.ponder.foundation.ui;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import net.createmod.ponder.Ponder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

final class EmbeddedReflectiveGuiSnapshot implements PonderGuiSnapshotRegistry.SnapshotRenderer {

    private final String guiClassName;
    private final String tileClassName;
    private SnapshotContext cachedContext;
    private boolean disabled;

    EmbeddedReflectiveGuiSnapshot(String guiClassName, String tileClassName) {
        this.guiClassName = guiClassName;
        this.tileClassName = tileClassName;
    }

    @Override
    public void render(int x, int y, int width, int height, float currentTick, float fade) {
        Minecraft mc = Minecraft.getMinecraft();
        if (disabled || mc == null || mc.player == null) {
            return;
        }

        try {
            SnapshotContext context = getOrCreateContext(mc);
            if (context == null) {
                return;
            }
            attachWorld(context.tile);
            seedInventory(context.tile);
            seedFields(context.tile, currentTick);
            renderGui(mc, context, x, y, width, height, currentTick);
        } catch (Throwable throwable) {
            cachedContext = null;
            disabled = true;
            Ponder.LOGGER.error("Failed to render embedded gui snapshot {} / {}", guiClassName, tileClassName,
                throwable);
        }
    }

    private SnapshotContext getOrCreateContext(Minecraft mc) throws Exception {
        InventoryPlayer playerInventory = PlayerInventoryResolver.getSnapshot(mc.player);
        if (playerInventory == null) {
            return null;
        }

        if (cachedContext != null) {
            if (cachedContext.isCompatible(playerInventory)) {
                return cachedContext;
            }
            cachedContext = null;
        }

        TileEntity tile = createTile();
        if (tile == null) {
            disabled = true;
            return null;
        }

        attachWorld(tile);
        ThermalMachinePreviewHelper.prepare(tile);
        seedInventory(tile);

        GuiScreen gui = createGui(playerInventory, tile);
        if (gui == null) {
            disabled = true;
            return null;
        }

        cachedContext = new SnapshotContext(tile, gui, playerInventory);
        return cachedContext;
    }

    private TileEntity createTile() throws Exception {
        Class<?> tileClass = Class.forName(tileClassName);
        Constructor<?> ctor = tileClass.getDeclaredConstructor();
        ctor.setAccessible(true);
        Object tile = ctor.newInstance();
        return tile instanceof TileEntity ? (TileEntity) tile : null;
    }

    private GuiScreen createGui(InventoryPlayer playerInventory, TileEntity tile) throws Exception {
        GuiScreen fromTile = createGuiFromTile(playerInventory, tile);
        if (fromTile != null) {
            return fromTile;
        }

        Class<?> guiClass = Class.forName(guiClassName);
        List<Constructor<?>> candidates = new ArrayList<Constructor<?>>();
        for (Constructor<?> ctor : guiClass.getDeclaredConstructors()) {
            candidates.add(ctor);
        }

        GuiScreen gui = tryCreateGui(candidates, playerInventory, tile);
        if (gui != null) {
            return gui;
        }

        for (Constructor<?> ctor : guiClass.getConstructors()) {
            candidates.add(ctor);
        }

        gui = tryCreateGui(candidates, playerInventory, tile);
        if (gui != null) {
            return gui;
        }

        throw new NoSuchMethodException("No supported GUI constructor found for " + guiClassName);
    }

    private GuiScreen createGuiFromTile(InventoryPlayer playerInventory, TileEntity tile) {
        try {
            Method method = tile.getClass().getMethod("getGuiClient", InventoryPlayer.class);
            Object gui = method.invoke(tile, playerInventory);
            return gui instanceof GuiScreen ? (GuiScreen) gui : null;
        } catch (Throwable ignored) {
        }
        try {
            Method method = tile.getClass().getMethod("func_180556_a", InventoryPlayer.class);
            Object gui = method.invoke(tile, playerInventory);
            return gui instanceof GuiScreen ? (GuiScreen) gui : null;
        } catch (Throwable ignored) {
        }
        return null;
    }

    private GuiScreen tryCreateGui(List<Constructor<?>> constructors, InventoryPlayer playerInventory, TileEntity tile)
        throws Exception {
        for (Constructor<?> ctor : constructors) {
            Class<?>[] parameterTypes = ctor.getParameterTypes();
            Object[] arguments = buildArguments(parameterTypes, playerInventory, tile);
            if (arguments == null) {
                continue;
            }
            ctor.setAccessible(true);
            Object gui = ctor.newInstance(arguments);
            if (gui instanceof GuiScreen) {
                return (GuiScreen) gui;
            }
        }
        return null;
    }

    private Object[] buildArguments(Class<?>[] parameterTypes, InventoryPlayer playerInventory, TileEntity tile) {
        if (parameterTypes.length == 2 && isInventoryParameter(parameterTypes[0]) && isTileParameter(parameterTypes[1], tile)) {
            return new Object[] { playerInventory, tile };
        }
        if (parameterTypes.length == 2 && isTileParameter(parameterTypes[0], tile) && isInventoryParameter(parameterTypes[1])) {
            return new Object[] { tile, playerInventory };
        }
        if (parameterTypes.length == 1 && isTileParameter(parameterTypes[0], tile)) {
            return new Object[] { tile };
        }
        if (parameterTypes.length == 1 && isInventoryParameter(parameterTypes[0])) {
            return new Object[] { playerInventory };
        }
        return null;
    }

    private boolean isInventoryParameter(Class<?> parameterType) {
        return parameterType.isAssignableFrom(InventoryPlayer.class)
            || parameterType.isAssignableFrom(IInventory.class);
    }

    private boolean isTileParameter(Class<?> parameterType, TileEntity tile) {
        return parameterType.isInstance(tile)
            || parameterType.isAssignableFrom(TileEntity.class)
            || parameterType.isAssignableFrom(tile.getClass());
    }

    private void renderGui(Minecraft mc, SnapshotContext context, int x, int y, int width, int height, float currentTick)
        throws Exception {
        GuiScreen gui = context.gui;
        int guiWidth = x * 2 + width;
        int guiHeight = y * 2 + height;
        if (context.screenWidth != guiWidth || context.screenHeight != guiHeight) {
            gui.setWorldAndResolution(mc, guiWidth, guiHeight);
            context.screenWidth = guiWidth;
            context.screenHeight = guiHeight;
        } else {
            refreshGuiReferences(gui, mc, guiWidth, guiHeight);
        }

        setGuiContainerPosition(gui, x, y);

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        enableSnapshotScissor(mc, x, y, width, height);
        try {
            gui.drawScreen(-1000, -1000, currentTick);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        } finally {
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
            GlStateManager.popMatrix();
        }
    }

    private void refreshGuiReferences(GuiScreen gui, Minecraft mc, int width, int height) {
        setPossibleField(gui, "mc", mc);
        setPossibleField(gui, "field_146297_k", mc);
        setPossibleField(gui, "fontRenderer", mc.fontRenderer);
        setPossibleField(gui, "field_146289_q", mc.fontRenderer);
        setPossibleField(gui, "itemRender", mc.getRenderItem());
        setPossibleField(gui, "field_146296_j", mc.getRenderItem());
        setPossibleField(gui, "width", Integer.valueOf(width));
        setPossibleField(gui, "field_146294_l", Integer.valueOf(width));
        setPossibleField(gui, "height", Integer.valueOf(height));
        setPossibleField(gui, "field_146295_m", Integer.valueOf(height));
    }

    private void setGuiContainerPosition(GuiScreen gui, int x, int y) {
        setPossibleField(gui, "guiLeft", Integer.valueOf(x));
        setPossibleField(gui, "field_147003_i", Integer.valueOf(x));
        setPossibleField(gui, "guiTop", Integer.valueOf(y));
        setPossibleField(gui, "field_147009_r", Integer.valueOf(y));
    }

    private void enableSnapshotScissor(Minecraft mc, int x, int y, int width, int height) {
        ScaledResolution resolution = new ScaledResolution(mc);
        int scaleFactor = resolution.getScaleFactor();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(x * scaleFactor, mc.displayHeight - (y + height) * scaleFactor, width * scaleFactor,
            height * scaleFactor);
    }

    private void seedInventory(TileEntity tile) {
        for (int slot = 0; slot < 12; slot++) {
            clearInventorySlot(tile, slot);
        }
    }

    private void attachWorld(TileEntity tile) {
        try {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc != null && mc.world != null) {
                tile.setWorld(mc.world);
            }
            tile.setPos(BlockPos.ORIGIN);
        } catch (Throwable ignored) {
        }
    }

    private void seedFields(TileEntity tile, float currentTick) {
        int cycle = ((int) currentTick) % 160;
        int energy = 16000 - cycle * 50;
        int progress = cycle % 100;
        trySetField(tile, 0, Math.max(0, energy));
        trySetField(tile, 1, 16000);
        trySetField(tile, 2, progress);
        trySetField(tile, 3, 100);
    }

    private void trySetField(TileEntity tile, int fieldId, int value) {
        try {
            Method setField = tile.getClass().getMethod("setField", int.class, int.class);
            setField.invoke(tile, Integer.valueOf(fieldId), Integer.valueOf(value));
            return;
        } catch (Throwable ignored) {
        }
        try {
            Method setField = tile.getClass().getMethod("func_174885_b", int.class, int.class);
            setField.invoke(tile, Integer.valueOf(fieldId), Integer.valueOf(value));
        } catch (Throwable ignored) {
        }
    }

    private void clearInventorySlot(TileEntity tile, int slot) {
        try {
            Method setSlot = tile.getClass().getMethod("setInventorySlotContents", int.class, ItemStack.class);
            setSlot.invoke(tile, Integer.valueOf(slot), ItemStack.EMPTY);
            return;
        } catch (Throwable ignored) {
        }
        try {
            Method setSlot = tile.getClass().getMethod("func_70299_a", int.class, ItemStack.class);
            setSlot.invoke(tile, Integer.valueOf(slot), ItemStack.EMPTY);
        } catch (Throwable ignored) {
        }
    }

    private void setPossibleField(Object target, String fieldName, Object value) {
        Class<?> current = target.getClass();
        while (current != null) {
            try {
                Field field = current.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(target, value);
                return;
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            } catch (Throwable ignored) {
                return;
            }
        }
    }

    private static final class SnapshotContext {
        private final TileEntity tile;
        private final GuiScreen gui;
        private final InventoryPlayer playerInventory;
        private int screenWidth = Integer.MIN_VALUE;
        private int screenHeight = Integer.MIN_VALUE;

        private SnapshotContext(TileEntity tile, GuiScreen gui, InventoryPlayer playerInventory) {
            this.tile = tile;
            this.gui = gui;
            this.playerInventory = playerInventory;
        }

        private boolean isCompatible(InventoryPlayer currentInventory) {
            return currentInventory != null && currentInventory == playerInventory;
        }
    }
}
