package net.createmod.ponder.foundation.ui;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.lang.reflect.Method;

import javax.annotation.Nullable;

import net.createmod.ponder.Ponder;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.event.GuiOpenEvent;
import org.lwjgl.opengl.GL11;

public final class SandboxTriggeredBlockGuiSnapshot implements PonderGuiSnapshotRegistry.SnapshotRenderer {

    private static final BlockPos SANDBOX_POS = new BlockPos(1000000, 200, 1000000);
    private static final int CAPTURE_TIMEOUT_TICKS = 40;
    private static final List<SandboxTriggeredBlockGuiSnapshot> INSTANCES =
        new ArrayList<SandboxTriggeredBlockGuiSnapshot>();
    private static final Map<String, SandboxTriggeredBlockGuiSnapshot> BY_BLOCK =
        new LinkedHashMap<String, SandboxTriggeredBlockGuiSnapshot>();

    public static final SandboxTriggeredBlockGuiSnapshot MINECRAFT_FURNACE =
        getOrCreate(new ResourceLocation("minecraft", "furnace"), 3);
    public static final SandboxTriggeredBlockGuiSnapshot THERMAL_FURNACE =
        getOrCreate(new ResourceLocation("thermalexpansion", "machine"), 0);

    private final ResourceLocation blockId;
    private final int meta;
    @Nullable
    private final NBTTagCompound configuredTileNbt;

    @Nullable
    private GuiScreen capturedGui;
    private boolean captureInProgress;
    private boolean captureArmed;
    private int captureTimeout;
    @Nullable
    private ClientBlockState originalClientState;
    @Nullable
    private ServerBlockState originalServerState;
    @Nullable
    private NBTTagCompound sandboxTileNbt;

    public SandboxTriggeredBlockGuiSnapshot(ResourceLocation blockId, int meta) {
        this(blockId, meta, null);
    }

    public SandboxTriggeredBlockGuiSnapshot(ResourceLocation blockId, int meta, @Nullable NBTTagCompound configuredTileNbt) {
        this.blockId = blockId;
        this.meta = meta;
        this.configuredTileNbt = configuredTileNbt == null ? null : configuredTileNbt.copy();
        INSTANCES.add(this);
    }

    public static synchronized SandboxTriggeredBlockGuiSnapshot getOrCreate(ResourceLocation blockId, int meta) {
        return getOrCreate(blockId, meta, null);
    }

    public static synchronized SandboxTriggeredBlockGuiSnapshot getOrCreate(ResourceLocation blockId, int meta,
        @Nullable NBTTagCompound configuredTileNbt) {
        String key = blockId + "#" + meta + "#" + (configuredTileNbt == null ? "" : configuredTileNbt.toString());
        SandboxTriggeredBlockGuiSnapshot existing = BY_BLOCK.get(key);
        if (existing != null) {
            return existing;
        }
        SandboxTriggeredBlockGuiSnapshot snapshot = new SandboxTriggeredBlockGuiSnapshot(blockId, meta,
            configuredTileNbt);
        BY_BLOCK.put(key, snapshot);
        return snapshot;
    }

    @Override
    public void render(int x, int y, int width, int height, float currentTick, float fade) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.player == null || mc.world == null) {
            return;
        }

        if (capturedGui == null && !captureInProgress) {
            beginCapture(mc);
        }

        if (capturedGui == null) {
            return;
        }

        renderCapturedGui(mc, x, y, width, height, currentTick);
    }

    public static void onClientTickAll() {
        for (SandboxTriggeredBlockGuiSnapshot instance : INSTANCES) {
            instance.onClientTick();
        }
    }

    public static void onGuiOpenAll(GuiOpenEvent event) {
        for (SandboxTriggeredBlockGuiSnapshot instance : INSTANCES) {
            instance.onGuiOpen(event);
            if (event.getGui() == null) {
                return;
            }
        }
    }

    private void onClientTick() {
        if (!captureInProgress) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        if (capturedGui == null && mc != null && mc.currentScreen != null
            && !(mc.currentScreen instanceof PonderUI)
            && !(mc.currentScreen instanceof PonderDebugScreen)
            && !(mc.currentScreen instanceof PonderIndexScreen)
            && !(mc.currentScreen instanceof PonderTagScreen)) {
            capturedGui = mc.currentScreen;
            mc.displayGuiScreen(null);
            cleanupCapture(mc);
            return;
        }

        if (--captureTimeout <= 0) {
            Ponder.LOGGER.warn("Sandbox gui capture timed out for {}", blockId);
            cleanupCapture(mc);
        }
    }

    private void onGuiOpen(GuiOpenEvent event) {
        if (!captureInProgress || !captureArmed) {
            return;
        }

        GuiScreen gui = event.getGui();
        if (gui == null) {
            return;
        }
        if (gui instanceof PonderUI || gui instanceof PonderDebugScreen
            || gui instanceof PonderIndexScreen || gui instanceof PonderTagScreen) {
            return;
        }

        capturedGui = gui;
        captureArmed = false;
        event.setGui(null);
        cleanupCapture(Minecraft.getMinecraft());
    }

    private void beginCapture(Minecraft mc) {
        IntegratedServer server = mc.getIntegratedServer();
        if (server == null || mc.player == null || mc.world == null) {
            return;
        }

        captureInProgress = true;
        captureArmed = false;
        captureTimeout = CAPTURE_TIMEOUT_TICKS;
        capturedGui = null;
        sandboxTileNbt = null;
        originalClientState = rememberClientState(mc);
        originalServerState = prepareServerSandbox(server, mc.player.getUniqueID());
        injectClientSandbox(mc);
        captureArmed = true;
        triggerServerOpen(server, mc.player.getUniqueID());
    }

    private void renderCapturedGui(Minecraft mc, int x, int y, int width, int height, float currentTick) {
        GuiScreen gui = capturedGui;
        int guiWidth = x * 2 + width;
        int guiHeight = y * 2 + height;
        gui.setWorldAndResolution(mc, guiWidth, guiHeight);
        refreshGuiReferences(gui, mc, guiWidth, guiHeight);
        setGuiContainerPosition(gui, x, y);

        net.minecraft.client.renderer.GlStateManager.pushMatrix();
        net.minecraft.client.renderer.GlStateManager.enableBlend();
        net.minecraft.client.renderer.GlStateManager.enableAlpha();
        net.minecraft.client.renderer.GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        enableSnapshotScissor(mc, x, y, width, height);
        try {
            gui.drawScreen(-1000, -1000, currentTick);
            net.minecraft.client.renderer.GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        } finally {
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
            net.minecraft.client.renderer.GlStateManager.popMatrix();
        }
    }

    private ClientBlockState rememberClientState(Minecraft mc) {
        IBlockState state = mc.world.getBlockState(SANDBOX_POS);
        TileEntity tile = mc.world.getTileEntity(SANDBOX_POS);
        NBTTagCompound nbt = tile == null ? null : tile.writeToNBT(new NBTTagCompound());
        return new ClientBlockState(state, nbt);
    }

    private void injectClientSandbox(Minecraft mc) {
        IBlockState state = createSandboxState();
        if (state == null) {
            return;
        }
        mc.world.setBlockState(SANDBOX_POS, state, 3);
        applySandboxTileNbt(mc.world, state, sandboxTileNbt);
    }

    @Nullable
    private ServerBlockState prepareServerSandbox(IntegratedServer server, UUID playerId) {
        EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(playerId);
        if (player == null) {
            return null;
        }
        WorldServer world = player.getServerWorld();
        IBlockState state = world.getBlockState(SANDBOX_POS);
        TileEntity tile = world.getTileEntity(SANDBOX_POS);
        NBTTagCompound nbt = tile == null ? null : tile.writeToNBT(new NBTTagCompound());

        IBlockState sandboxState = createSandboxState();
        if (sandboxState == null) {
            return null;
        }
        world.setBlockState(SANDBOX_POS, sandboxState, 3);
        TileEntity sandboxTile = world.getTileEntity(SANDBOX_POS);
        if (sandboxTile != null) {
            seedSandboxTile(sandboxTile);
            sandboxTileNbt = sandboxTile.writeToNBT(new NBTTagCompound());
        } else {
            sandboxTileNbt = null;
        }
        return new ServerBlockState(world.provider.getDimension(), state, nbt);
    }

    @Nullable
    private IBlockState createSandboxState() {
        Block block = Block.REGISTRY.getObject(blockId);
        if (block == null) {
            Ponder.LOGGER.warn("Sandbox gui capture could not find block {}", blockId);
            return null;
        }
        try {
            return block.getStateFromMeta(meta);
        } catch (Throwable ignored) {
            return block.getDefaultState();
        }
    }

    private void seedSandboxTile(TileEntity tile) {
        ThermalMachinePreviewHelper.prepare(tile);
        clearInventorySlots(tile, 16);
        if (tile.getClass().getName().startsWith("cofh.thermalexpansion.block.machine.")) {
            seedFields(tile, 0.0F);
        }
        applyConfiguredTileNbt(tile);
    }

    private void applyConfiguredTileNbt(TileEntity tile) {
        if (configuredTileNbt == null) {
            return;
        }
        try {
            NBTTagCompound merged = tile.writeToNBT(new NBTTagCompound());
            for (String key : configuredTileNbt.getKeySet()) {
                merged.setTag(key, configuredTileNbt.getTag(key).copy());
            }
            merged.setString("id", TileEntity.getKey(tile.getClass()).toString());
            merged.setInteger("x", SANDBOX_POS.getX());
            merged.setInteger("y", SANDBOX_POS.getY());
            merged.setInteger("z", SANDBOX_POS.getZ());
            tile.readFromNBT(merged);
            tile.setPos(SANDBOX_POS);
            if (Minecraft.getMinecraft() != null && Minecraft.getMinecraft().world != null) {
                tile.setWorld(Minecraft.getMinecraft().world);
            }
            tile.markDirty();
        } catch (Throwable throwable) {
            Ponder.LOGGER.warn("Failed to apply configured sandbox tile NBT for {}", blockId, throwable);
        }
    }

    private void triggerServerOpen(IntegratedServer server, final UUID playerId) {
        server.addScheduledTask(new Runnable() {
            @Override
            public void run() {
                EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(playerId);
                if (player == null) {
                    return;
                }
                WorldServer world = player.getServerWorld();
                IBlockState state = world.getBlockState(SANDBOX_POS);
                try {
                    state.getBlock().onBlockActivated(world, SANDBOX_POS, state, player, EnumHand.MAIN_HAND,
                        EnumFacing.UP, 0.5F, 0.5F, 0.5F);
                } catch (Throwable throwable) {
                    Ponder.LOGGER.warn("Sandbox gui trigger failed for {}", blockId, throwable);
                }
            }
        });
    }

    private void cleanupCapture(@Nullable Minecraft mc) {
        captureInProgress = false;
        captureArmed = false;
        captureTimeout = 0;

        if (mc != null && mc.player != null) {
            try {
                mc.player.closeScreen();
            } catch (Throwable ignored) {
            }
        }

        restoreClientSandbox(mc);
        restoreServerSandbox(mc == null ? null : mc.getIntegratedServer(),
            mc == null || mc.player == null ? null : mc.player.getUniqueID());
    }

    private void restoreClientSandbox(@Nullable Minecraft mc) {
        if (mc == null || mc.world == null || originalClientState == null) {
            return;
        }
        mc.world.setBlockState(SANDBOX_POS, originalClientState.state, 3);
        if (originalClientState.nbt == null) {
            mc.world.removeTileEntity(SANDBOX_POS);
        } else {
            TileEntity tile = TileEntity.create(mc.world, originalClientState.nbt);
            if (tile != null) {
                tile.setPos(SANDBOX_POS);
                tile.setWorld(mc.world);
                mc.world.setTileEntity(SANDBOX_POS, tile);
            }
        }
        originalClientState = null;
        sandboxTileNbt = null;
    }

    private void restoreServerSandbox(@Nullable IntegratedServer server, @Nullable UUID playerId) {
        if (server == null || playerId == null || originalServerState == null) {
            return;
        }
        final ServerBlockState restore = originalServerState;
        originalServerState = null;
        server.addScheduledTask(new Runnable() {
            @Override
            public void run() {
                EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(playerId);
                if (player == null) {
                    return;
                }
                try {
                    player.closeScreen();
                    player.closeContainer();
                } catch (Throwable ignored) {
                }
                WorldServer world = server.getWorld(restore.dimension);
                if (world == null) {
                    return;
                }
                world.setBlockState(SANDBOX_POS, restore.state, 3);
                if (restore.nbt == null) {
                    world.removeTileEntity(SANDBOX_POS);
                } else {
                    TileEntity tile = TileEntity.create(world, restore.nbt);
                    if (tile != null) {
                        tile.setPos(SANDBOX_POS);
                        tile.setWorld(world);
                        world.setTileEntity(SANDBOX_POS, tile);
                    }
                }
            }
        });
    }

    private void applySandboxTileNbt(World world, IBlockState state, @Nullable NBTTagCompound nbt) {
        if (world == null || state == null) {
            return;
        }
        if (nbt != null) {
            TileEntity tile = TileEntity.create(world, nbt);
            if (tile != null) {
                tile.setPos(SANDBOX_POS);
                tile.setWorld(world);
                world.setTileEntity(SANDBOX_POS, tile);
                return;
            }
        }

        TileEntity tile = world.getTileEntity(SANDBOX_POS);
        if (tile != null) {
            seedSandboxTile(tile);
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

    private void clearInventorySlots(TileEntity tile, int count) {
        for (int slot = 0; slot < count; slot++) {
            try {
                Method setSlot = tile.getClass().getMethod("setInventorySlotContents", int.class, net.minecraft.item.ItemStack.class);
                setSlot.invoke(tile, Integer.valueOf(slot), net.minecraft.item.ItemStack.EMPTY);
                continue;
            } catch (Throwable ignored) {
            }
            try {
                Method setSlot = tile.getClass().getMethod("func_70299_a", int.class, net.minecraft.item.ItemStack.class);
                setSlot.invoke(tile, Integer.valueOf(slot), net.minecraft.item.ItemStack.EMPTY);
            } catch (Throwable ignored) {
            }
        }
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

    private void setPossibleField(Object target, String fieldName, Object value) {
        Class<?> current = target.getClass();
        while (current != null) {
            try {
                java.lang.reflect.Field field = current.getDeclaredField(fieldName);
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

    private static final class ClientBlockState {
        private final IBlockState state;
        @Nullable
        private final NBTTagCompound nbt;

        private ClientBlockState(IBlockState state, @Nullable NBTTagCompound nbt) {
            this.state = state;
            this.nbt = nbt == null ? null : nbt.copy();
        }
    }

    private static final class ServerBlockState {
        private final int dimension;
        private final IBlockState state;
        @Nullable
        private final NBTTagCompound nbt;

        private ServerBlockState(int dimension, IBlockState state, @Nullable NBTTagCompound nbt) {
            this.dimension = dimension;
            this.state = state;
            this.nbt = nbt == null ? null : nbt.copy();
        }
    }
}
