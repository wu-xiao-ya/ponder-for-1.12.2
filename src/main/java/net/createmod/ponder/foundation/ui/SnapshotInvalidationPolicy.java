package net.createmod.ponder.foundation.ui;

public enum SnapshotInvalidationPolicy {

    IMMUTABLE {
        @Override
        public int cacheBucket(SnapshotContext context) {
            return 0;
        }
    },
    TICK_20_BUCKET {
        @Override
        public int cacheBucket(SnapshotContext context) {
            return context == null ? 0 : context.tickBucket20();
        }
    };

    public final int bucket(SnapshotContext context) {
        return cacheBucket(context);
    }

    public abstract int cacheBucket(SnapshotContext context);

    public final boolean isCacheStable() {
        return this == IMMUTABLE;
    }

}
