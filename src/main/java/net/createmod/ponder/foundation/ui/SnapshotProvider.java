package net.createmod.ponder.foundation.ui;

import java.util.Objects;

@FunctionalInterface
public interface SnapshotProvider {
    Snapshot provide(float currentTick);

    static SnapshotProvider constant(Snapshot snapshot) {
        Objects.requireNonNull(snapshot);
        return currentTick -> snapshot;
    }

}
