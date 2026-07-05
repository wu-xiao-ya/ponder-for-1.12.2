package net.createmod.ponder.foundation.ui;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import net.minecraft.util.ResourceLocation;

final class SnapshotRegistryStore {

    private final Object lock = new Object();
    private final Map<ResourceLocation, SnapshotSource> snapshotSources = new LinkedHashMap<>();
    private final Map<SnapshotCacheKey, Snapshot> snapshotCache = new LinkedHashMap<>();

    Snapshot get(ResourceLocation id, SnapshotContext context) {
        if (id == null) {
            return null;
        }

        SnapshotSource source;
        SnapshotContext resolvedContext;
        SnapshotCacheKey cacheKey;
        synchronized (lock) {
            source = snapshotSources.get(id);
            if (source == null) {
                return null;
            }
            resolvedContext = context == null ? SnapshotContext.of(0.0F) : context;
            cacheKey = source.cacheKey(id, resolvedContext);
            Snapshot cached = snapshotCache.get(cacheKey);
            if (cached != null) {
                return cached;
            }
        }

        Snapshot resolved = source.resolve(resolvedContext);
        if (resolved == null) {
            return null;
        }

        synchronized (lock) {
            if (snapshotSources.get(id) != source) {
                return null;
            }
            Snapshot cached = snapshotCache.get(cacheKey);
            if (cached != null) {
                return cached;
            }
            snapshotCache.put(cacheKey, resolved);
            return resolved;
        }
    }

    void registerSource(ResourceLocation id, SnapshotSource source) {
        if (id == null || source == null) {
            return;
        }
        synchronized (lock) {
            snapshotSources.put(id, source);
            snapshotCache.entrySet().removeIf(entry -> id.equals(entry.getKey().id()));
        }
    }

    void clearCache() {
        synchronized (lock) {
            snapshotCache.clear();
        }
    }

    void clear() {
        synchronized (lock) {
            snapshotSources.clear();
            snapshotCache.clear();
        }
    }

    void rebuild(Runnable defaultRegistrations) {
        Objects.requireNonNull(defaultRegistrations);
        synchronized (lock) {
            snapshotSources.clear();
            snapshotCache.clear();
            defaultRegistrations.run();
        }
    }
}
