package net.createmod.ponder.foundation.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import net.createmod.ponder.Ponder;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.event.GuiOpenEvent;

public final class SandboxTriggeredBlockGuiSnapshot implements SnapshotRenderer.GuiSnapshotRenderer {

    static final BlockPos SANDBOX_POS = new BlockPos(1000000, 200, 1000000);
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
    private final String cacheKey;
    private final SandboxGuiCaptureSession captureSession = new SandboxGuiCaptureSession(this);

    @Nullable
    private GuiScreen capturedGui;
    @Nullable
    private final NBTTagCompound requestedTileNbt;

    public SandboxTriggeredBlockGuiSnapshot(ResourceLocation blockId, int meta) {
        this(blockId, meta, null, buildCacheKey(blockId, meta, null));
    }

    private SandboxTriggeredBlockGuiSnapshot(ResourceLocation blockId, int meta, @Nullable NBTTagCompound requestedTileNbt,
        String cacheKey) {
        this.blockId = blockId;
        this.meta = meta;
        this.cacheKey = cacheKey;
        this.requestedTileNbt = requestedTileNbt == null ? null : requestedTileNbt.copy();
        INSTANCES.add(this);
    }

    public static synchronized SandboxTriggeredBlockGuiSnapshot getOrCreate(ResourceLocation blockId, int meta) {
        return getOrCreate(blockId, meta, null);
    }

    public static synchronized SandboxTriggeredBlockGuiSnapshot getOrCreate(ResourceLocation blockId, int meta,
        @Nullable NBTTagCompound tileNbt) {
        String key = buildCacheKey(blockId, meta, tileNbt);
        SandboxTriggeredBlockGuiSnapshot existing = BY_BLOCK.get(key);
        if (existing != null) {
            return existing;
        }
        SandboxTriggeredBlockGuiSnapshot snapshot = new SandboxTriggeredBlockGuiSnapshot(blockId, meta, tileNbt, key);
        BY_BLOCK.put(key, snapshot);
        return snapshot;
    }

    String cacheKey() {
        return cacheKey;
    }

    @Override
    public void render(int x, int y, int width, int height, float currentTick, float fade) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.player == null || mc.world == null) {
            return;
        }

        if (capturedGui == null && !captureSession.isCaptureInProgress()) {
            captureSession.beginCapture(mc);
        }

        if (capturedGui == null) {
            return;
        }

        renderCapturedGui(mc, x, y, width, height, currentTick);
    }

    public static void onClientTickAll() {
        for (SandboxTriggeredBlockGuiSnapshot instance : INSTANCES) {
            instance.captureSession.onClientTick();
        }
    }

    public static void onGuiOpenAll(GuiOpenEvent event) {
        for (SandboxTriggeredBlockGuiSnapshot instance : INSTANCES) {
            instance.captureSession.onGuiOpen(event);
            if (event.getGui() == null) {
                return;
            }
        }
    }

    private void renderCapturedGui(Minecraft mc, int x, int y, int width, int height, float currentTick) {
        GuiScreen gui = capturedGui;
        int guiWidth = x * 2 + width;
        int guiHeight = y * 2 + height;
        gui.setWorldAndResolution(mc, guiWidth, guiHeight);
        SnapshotGuiLayoutHelper.refreshGuiReferences(gui, mc, guiWidth, guiHeight);
        SnapshotGuiLayoutHelper.setGuiContainerPosition(gui, x, y);

        SnapshotGuiLayoutHelper.renderSnapshotFrame(mc, x, y, width, height, () -> {
            gui.drawScreen(-1000, -1000, currentTick);
        });
    }

    boolean hasCapturedGui() {
        return capturedGui != null;
    }

    void setCapturedGui(@Nullable GuiScreen capturedGui) {
        this.capturedGui = capturedGui;
    }

    void logCaptureTimeout() {
        Ponder.LOGGER.warn("Sandbox gui capture timed out for {}", blockId);
    }

    ClientBlockState rememberClientState(Minecraft mc) {
        IBlockState state = mc.world.getBlockState(SANDBOX_POS);
        TileEntity tile = mc.world.getTileEntity(SANDBOX_POS);
        NBTTagCompound nbt = tile == null ? null : tile.writeToNBT(new NBTTagCompound());
        return new ClientBlockState(state, nbt);
    }

    void injectClientSandbox(Minecraft mc, @Nullable NBTTagCompound sandboxTileNbt) {
        IBlockState state = createSandboxState();
        if (state == null) {
            return;
        }
        mc.world.setBlockState(SANDBOX_POS, state, 3);
        applySandboxTileNbt(mc.world, state, sandboxTileNbt);
    }

    @Nullable
    ServerBlockState prepareServerSandbox(IntegratedServer server, UUID playerId,
        SandboxGuiCaptureSession captureSession) {
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
            NBTTagCompound sandboxTileNbt = sandboxTile.writeToNBT(new NBTTagCompound());
            captureSession.setSandboxTileNbt(sandboxTileNbt);
            if (requestedTileNbt != null) {
                mergeNbt(sandboxTileNbt, requestedTileNbt);
                sandboxTile.readFromNBT(sandboxTileNbt);
                captureSession.setSandboxTileNbt(sandboxTile.writeToNBT(new NBTTagCompound()));
            }
        } else {
            captureSession.setSandboxTileNbt(null);
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
        SnapshotTileSeedHelper.clearInventorySlots(tile, 16);
        if (tile.getClass().getName().startsWith("cofh.thermalexpansion.block.machine.")) {
            SnapshotTileSeedHelper.seedThermalMachineFields(tile, 0.0F);
        }
    }

    void triggerServerOpen(IntegratedServer server, final UUID playerId) {
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

    private void mergeNbt(NBTTagCompound target, NBTTagCompound source) {
        for (String key : source.getKeySet()) {
            target.setTag(key, source.getTag(key).copy());
        }
    }

    private static String buildCacheKey(ResourceLocation blockId, int meta, @Nullable NBTTagCompound tileNbt) {
        return blockId + "#" + meta + "#" + stableNbtKey(tileNbt);
    }

    private static String stableNbtKey(@Nullable NBTTagCompound tileNbt) {
        return tileNbt == null ? "-" : stableTagKey(tileNbt);
    }

    private static String stableTagKey(NBTBase tag) {
        if (tag instanceof NBTTagCompound compound) {
            List<String> keys = new ArrayList<String>(compound.getKeySet());
            Collections.sort(keys);
            StringBuilder builder = new StringBuilder();
            builder.append('{');
            for (String key : keys) {
                builder.append(key).append(':').append(stableTagKey(compound.getTag(key))).append(';');
            }
            builder.append('}');
            return builder.toString();
        }
        if (tag instanceof NBTTagList list) {
            StringBuilder builder = new StringBuilder();
            builder.append('[');
            for (int index = 0; index < list.tagCount(); index++) {
                builder.append(stableTagKey(list.get(index))).append(';');
            }
            builder.append(']');
            return builder.toString();
        }
        return tag.getId() + ":" + tag.toString();
    }

    record ClientBlockState(IBlockState state, @Nullable NBTTagCompound nbt) {
        ClientBlockState {
            nbt = nbt != null ? nbt.copy() : null;
        }
    }

    record ServerBlockState(int dimension, IBlockState state, @Nullable NBTTagCompound nbt) {
        ServerBlockState {
            nbt = nbt != null ? nbt.copy() : null;
        }
    }
}
