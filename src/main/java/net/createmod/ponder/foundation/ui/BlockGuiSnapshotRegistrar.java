package net.createmod.ponder.foundation.ui;

import java.util.Locale;

import net.createmod.ponder.Ponder;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

final class BlockGuiSnapshotRegistrar {

    private BlockGuiSnapshotRegistrar() {
    }

    static ResourceLocation registerBlockGuiSnapshot(ResourceLocation blockId, int meta, NBTTagCompound tileNbt,
        int width, int height) {
        ResourceLocation snapshotId = createSnapshotId(blockId, meta, width, height);
        PonderGuiSnapshotRegistry.register(snapshotId, Snapshot.liveRenderer(Math.max(1, width), Math.max(1, height),
            true, SandboxTriggeredBlockGuiSnapshot.getOrCreate(blockId, Math.max(0, meta), tileNbt)));
        return snapshotId;
    }

    static ResourceLocation createSnapshotId(ResourceLocation blockId, int meta, int width, int height) {
        return Ponder.asResource("gui_snapshot/block/%s/%s/%d/%dx%d".formatted(
            sanitizePath(blockId.getNamespace()),
            sanitizePath(blockId.getPath()),
            Math.max(0, meta),
            Math.max(1, width),
            Math.max(1, height)));
    }

    static String sanitizePath(String value) {
        String lower = value == null ? "unknown" : value.toLowerCase(Locale.ROOT);
        StringBuilder builder = new StringBuilder(lower.length());
        for (int i = 0; i < lower.length(); i++) {
            char c = lower.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_' || c == '-' || c == '.' || c == '/') {
                builder.append(c);
            } else {
                builder.append('_');
            }
        }
        return builder.length() == 0 ? "unknown" : builder.toString();
    }
}
