package net.createmod.ponder.foundation.ui;

import net.createmod.ponder.Ponder;
import net.minecraft.util.ResourceLocation;

final class MinecraftGuiSnapshotRegistrar {

    private MinecraftGuiSnapshotRegistrar() {
    }

    static void registerDefaults() {
        Snapshot furnace = Snapshot.fullTexture(
            new ResourceLocation("minecraft", "textures/gui/container/furnace.png"), 176, 166);
        PonderGuiSnapshotRegistry.register(Ponder.asResource("gui_snapshot/minecraft_furnace"), furnace);
        PonderGuiSnapshotRegistry.register(Ponder.asResource("gui_snapshot/minecraft_furnace_live"),
            Snapshot.liveRenderer(176, 166, true, EmbeddedGuiFurnaceSnapshot.INSTANCE));
        PonderGuiSnapshotRegistry.register(Ponder.asResource("gui_snapshot/minecraft_furnace_sandbox_live"),
            Snapshot.liveRenderer(176, 166, true, SandboxTriggeredBlockGuiSnapshot.MINECRAFT_FURNACE));
    }
}
