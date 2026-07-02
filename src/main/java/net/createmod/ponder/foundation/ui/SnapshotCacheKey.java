package net.createmod.ponder.foundation.ui;

import java.util.Objects;

import net.minecraft.util.ResourceLocation;

public record SnapshotCacheKey(ResourceLocation id, int tickBucket, int dimension, String sourceKey) {

    public SnapshotCacheKey {
        Objects.requireNonNull(id);
        sourceKey = sourceKey == null ? "" : sourceKey;
    }

    public static SnapshotCacheKey of(ResourceLocation id, SnapshotContext context, SnapshotInvalidationPolicy policy,
        String sourceKey) {
        int dimension = context == null ? 0 : context.dimension();
        return new SnapshotCacheKey(id, policy.bucket(context), dimension, sourceKey);
    }

}
