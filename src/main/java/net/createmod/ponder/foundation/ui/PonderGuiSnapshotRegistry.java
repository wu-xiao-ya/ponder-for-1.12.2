package net.createmod.ponder.foundation.ui;

import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public final class PonderGuiSnapshotRegistry {

    private static final Map<ResourceLocation, SnapshotSource> SNAPSHOT_SOURCES = new LinkedHashMap<>();
    private static final Map<SnapshotCacheKey, Snapshot> SNAPSHOT_CACHE = new LinkedHashMap<>();

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
        if (id == null) {
            return null;
        }

        SnapshotSource source;
        SnapshotContext resolvedContext;
        SnapshotCacheKey cacheKey;
        synchronized (PonderGuiSnapshotRegistry.class) {
            source = SNAPSHOT_SOURCES.get(id);
            if (source == null) {
                return null;
            }
            resolvedContext = context == null ? SnapshotContext.of(0.0F) : context;
            cacheKey = source.cacheKey(id, resolvedContext);
            Snapshot cached = SNAPSHOT_CACHE.get(cacheKey);
            if (cached != null) {
                return cached;
            }
        }

        Snapshot resolved = source.resolve(resolvedContext);
        if (resolved == null) {
            return null;
        }

        synchronized (PonderGuiSnapshotRegistry.class) {
            if (SNAPSHOT_SOURCES.get(id) != source) {
                return null;
            }
            Snapshot cached = SNAPSHOT_CACHE.get(cacheKey);
            if (cached != null) {
                return cached;
            }
            SNAPSHOT_CACHE.put(cacheKey, resolved);
            return resolved;
        }
    }

    public static synchronized void registerSource(ResourceLocation id, SnapshotSource source) {
        if (id == null || source == null) {
            return;
        }
        SNAPSHOT_SOURCES.put(id, source);
        SNAPSHOT_CACHE.entrySet().removeIf(entry -> id.equals(entry.getKey().id()));
    }

    public static synchronized void clearCache() {
        SNAPSHOT_CACHE.clear();
    }

    public static synchronized void clear() {
        SNAPSHOT_SOURCES.clear();
        SNAPSHOT_CACHE.clear();
    }

    public static synchronized void rebuild() {
        clear();
        registerDefaults();
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
