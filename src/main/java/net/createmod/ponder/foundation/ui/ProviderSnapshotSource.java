package net.createmod.ponder.foundation.ui;

import java.util.Objects;

record ProviderSnapshotSource(SnapshotProvider provider) implements SnapshotSource {
    ProviderSnapshotSource {
        Objects.requireNonNull(provider);
    }

    @Override
    public Snapshot resolve(SnapshotContext context) {
        return provider.provide(context == null ? 0.0F : context.currentTick());
    }

    @Override
    public SnapshotInvalidationPolicy invalidationPolicy() {
        return SnapshotInvalidationPolicy.TICK_20_BUCKET;
    }

    @Override
    public String sourceKey() {
        return provider.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(provider));
    }
}
