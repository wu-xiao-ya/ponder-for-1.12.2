package net.createmod.ponder.foundation.ui;

import java.util.Objects;

import net.minecraft.util.ResourceLocation;

public record SnapshotCacheKey(ResourceLocation id, int tickBucket, String contextKey, String sourceKey) {

    public SnapshotCacheKey {
        Objects.requireNonNull(id);
        contextKey = contextKey == null ? "" : contextKey;
        sourceKey = sourceKey == null ? "" : sourceKey;
    }

    public SnapshotCacheKey(ResourceLocation id, int tickBucket, int dimension, String sourceKey) {
        this(id, tickBucket, Integer.toString(dimension), sourceKey);
    }

    public static SnapshotCacheKey of(ResourceLocation id, SnapshotContext context, SnapshotInvalidationPolicy policy,
        String sourceKey) {
        SnapshotContext resolvedContext = context == null ? SnapshotContext.of(0.0F) : context;
        SnapshotInvalidationPolicy resolvedPolicy = policy == null ? SnapshotInvalidationPolicy.IMMUTABLE : policy;
        return new SnapshotCacheKey(id, resolvedPolicy.cacheBucket(resolvedContext), resolvedContext.cacheScopeKey(),
            sourceKey);
    }

    public int dimension() {
        int separatorIndex = contextKey.indexOf('|');
        String value = separatorIndex < 0 ? contextKey : contextKey.substring(0, separatorIndex);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

}
