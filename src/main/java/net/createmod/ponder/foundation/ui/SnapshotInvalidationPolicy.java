package net.createmod.ponder.foundation.ui;

public enum SnapshotInvalidationPolicy {

    IMMUTABLE {
        @Override
        public int bucket(SnapshotContext context) {
            return 0;
        }
    },
    TICK_20_BUCKET {
        @Override
        public int bucket(SnapshotContext context) {
            return context == null ? 0 : context.tickBucket20();
        }
    };

    public abstract int bucket(SnapshotContext context);

}
