package net.createmod.ponder.foundation.ui;

import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.event.GuiOpenEvent;

final class SandboxGuiCaptureSession {

    private static final int CAPTURE_TIMEOUT_TICKS = 40;

    private final SandboxTriggeredBlockGuiSnapshot owner;

    private boolean captureInProgress;
    private boolean captureArmed;
    private int captureTimeout;
    @Nullable
    private SandboxTriggeredBlockGuiSnapshot.ClientBlockState originalClientState;
    @Nullable
    private SandboxTriggeredBlockGuiSnapshot.ServerBlockState originalServerState;
    @Nullable
    private NBTTagCompound sandboxTileNbt;

    SandboxGuiCaptureSession(SandboxTriggeredBlockGuiSnapshot owner) {
        this.owner = owner;
    }

    boolean isCaptureInProgress() {
        return captureInProgress;
    }

    void onClientTick() {
        if (!captureInProgress) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        if (!owner.hasCapturedGui() && mc != null && mc.currentScreen != null
            && !(mc.currentScreen instanceof PonderUI)
            && !(mc.currentScreen instanceof PonderDebugScreen)
            && !(mc.currentScreen instanceof PonderIndexScreen)
            && !(mc.currentScreen instanceof PonderTagScreen)) {
            owner.setCapturedGui(mc.currentScreen);
            mc.displayGuiScreen(null);
            cleanupCapture(mc);
            return;
        }

        if (--captureTimeout <= 0) {
            owner.logCaptureTimeout();
            cleanupCapture(mc);
        }
    }

    void onGuiOpen(GuiOpenEvent event) {
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

        owner.setCapturedGui(gui);
        captureArmed = false;
        event.setGui(null);
        cleanupCapture(Minecraft.getMinecraft());
    }

    void beginCapture(Minecraft mc) {
        IntegratedServer server = mc.getIntegratedServer();
        if (server == null || mc.player == null || mc.world == null) {
            return;
        }

        captureInProgress = true;
        captureArmed = false;
        captureTimeout = CAPTURE_TIMEOUT_TICKS;
        owner.setCapturedGui(null);
        sandboxTileNbt = null;
        originalClientState = owner.rememberClientState(mc);
        originalServerState = owner.prepareServerSandbox(server, mc.player.getUniqueID(), this);
        owner.injectClientSandbox(mc, this.sandboxTileNbt);
        captureArmed = true;
        owner.triggerServerOpen(server, mc.player.getUniqueID());
    }

    void cleanupCapture(@Nullable Minecraft mc) {
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

    void setSandboxTileNbt(@Nullable NBTTagCompound sandboxTileNbt) {
        this.sandboxTileNbt = sandboxTileNbt;
    }

    private void restoreClientSandbox(@Nullable Minecraft mc) {
        if (mc == null || mc.world == null || originalClientState == null) {
            return;
        }
        mc.world.setBlockState(SandboxTriggeredBlockGuiSnapshot.SANDBOX_POS, originalClientState.state(), 3);
        if (originalClientState.nbt() == null) {
            mc.world.removeTileEntity(SandboxTriggeredBlockGuiSnapshot.SANDBOX_POS);
        } else {
            TileEntity tile = TileEntity.create(mc.world, originalClientState.nbt());
            if (tile != null) {
                tile.setPos(SandboxTriggeredBlockGuiSnapshot.SANDBOX_POS);
                tile.setWorld(mc.world);
                mc.world.setTileEntity(SandboxTriggeredBlockGuiSnapshot.SANDBOX_POS, tile);
            }
        }
        originalClientState = null;
        sandboxTileNbt = null;
    }

    private void restoreServerSandbox(@Nullable IntegratedServer server, @Nullable UUID playerId) {
        if (server == null || playerId == null || originalServerState == null) {
            return;
        }
        SandboxTriggeredBlockGuiSnapshot.ServerBlockState restore = originalServerState;
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
                WorldServer world = server.getWorld(restore.dimension());
                if (world == null) {
                    return;
                }
                world.setBlockState(SandboxTriggeredBlockGuiSnapshot.SANDBOX_POS, restore.state(), 3);
                if (restore.nbt() == null) {
                    world.removeTileEntity(SandboxTriggeredBlockGuiSnapshot.SANDBOX_POS);
                } else {
                    TileEntity tile = TileEntity.create(world, restore.nbt());
                    if (tile != null) {
                        tile.setPos(SandboxTriggeredBlockGuiSnapshot.SANDBOX_POS);
                        tile.setWorld(world);
                        world.setTileEntity(SandboxTriggeredBlockGuiSnapshot.SANDBOX_POS, tile);
                    }
                }
            }
        });
    }
}
