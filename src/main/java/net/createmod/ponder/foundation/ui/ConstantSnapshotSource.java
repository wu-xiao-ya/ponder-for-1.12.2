package net.createmod.ponder.foundation.ui;

import java.util.Objects;

record ConstantSnapshotSource(Snapshot snapshot) implements SnapshotSource {
    ConstantSnapshotSource {
        Objects.requireNonNull(snapshot);
    }

    @Override
    public Snapshot resolve(SnapshotContext context) {
        return snapshot;
    }

    @Override
    public SnapshotInvalidationPolicy invalidationPolicy() {
        return SnapshotInvalidationPolicy.IMMUTABLE;
    }

    @Override
    public String sourceKey() {
        return SnapshotSource.snapshotKey(snapshot);
    }
}
