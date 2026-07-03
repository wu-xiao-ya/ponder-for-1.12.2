package net.createmod.ponder.foundation.ui;

import net.minecraft.util.ResourceLocation;

public sealed interface SnapshotSource permits ConstantSnapshotSource, ProviderSnapshotSource {

    Snapshot resolve(SnapshotContext context);

    SnapshotInvalidationPolicy invalidationPolicy();

    String sourceKey();

    default SnapshotCacheKey cacheKey(ResourceLocation id, SnapshotContext context) {
        return SnapshotCacheKey.of(id, context, invalidationPolicy(), sourceKey());
    }

    static SnapshotSource constant(Snapshot snapshot) {
        return new ConstantSnapshotSource(snapshot);
    }

    static SnapshotSource adapt(SnapshotProvider provider) {
        return new ProviderSnapshotSource(provider);
    }

    static String snapshotKey(Snapshot snapshot) {
        return SnapshotIdentitySupport.snapshotKey(snapshot);
    }

    static String rendererKey(SnapshotRenderer renderer) {
        return SnapshotIdentitySupport.rendererKey(renderer);
    }

}
