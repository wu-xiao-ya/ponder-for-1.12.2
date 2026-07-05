package net.createmod.ponder.foundation.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public final class PonderGuiSnapshotRegistry {

    private static final SnapshotRegistryStore STORE = new SnapshotRegistryStore();

    static {
        registerDefaults();
    }

    private PonderGuiSnapshotRegistry() {
    }

    public static void register(ResourceLocation id, Snapshot snapshot) {
        if (id == null || snapshot == null) {
            return;
        }
        registerSource(id, SnapshotSource.constant(snapshot));
    }

    public static void registerProvider(ResourceLocation id, SnapshotProvider provider) {
        registerSource(id, provider == null ? null : provider.asSource());
    }

    public static Snapshot get(ResourceLocation id, float currentTick) {
        return get(id, snapshotContext(currentTick));
    }

    public static Snapshot get(ResourceLocation id, SnapshotContext context) {
        return STORE.get(id, context);
    }

    public static void registerSource(ResourceLocation id, SnapshotSource source) {
        STORE.registerSource(id, source);
    }

    public static void clearCache() {
        STORE.clearCache();
    }

    public static void clear() {
        STORE.clear();
    }

    public static void rebuild() {
        STORE.rebuild(PonderGuiSnapshotRegistry::registerDefaults);
    }

    public static ResourceLocation registerBlockGuiSnapshot(ResourceLocation blockId, int meta, int width, int height) {
        return registerBlockGuiSnapshot(blockId, meta, null, width, height);
    }

    public static ResourceLocation registerBlockGuiSnapshot(ResourceLocation blockId, int meta, NBTTagCompound tileNbt,
        int width, int height) {
        return BlockGuiSnapshotRegistrar.registerBlockGuiSnapshot(blockId, meta, tileNbt, width, height);
    }

    private static void registerDefaults() {
        MinecraftGuiSnapshotRegistrar.registerDefaults();
        ThermalGuiSnapshotRegistrar.registerDefaults();
    }

    private static SnapshotContext snapshotContext(float currentTick) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.player == null) {
            return SnapshotContext.of(currentTick);
        }
        return SnapshotContext.of(currentTick, mc.player, mc.world);
    }
}
