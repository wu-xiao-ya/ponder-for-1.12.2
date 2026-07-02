package net.createmod.ponder.foundation.ui;

import java.util.Objects;

import net.minecraft.util.ResourceLocation;

public sealed interface SnapshotSource permits SnapshotSource.ConstantSnapshotSource,
    SnapshotSource.ProviderSnapshotSource {

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

    record ConstantSnapshotSource(Snapshot snapshot) implements SnapshotSource {
        public ConstantSnapshotSource {
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
            return snapshotKey(snapshot);
        }
    }

    record ProviderSnapshotSource(SnapshotProvider provider) implements SnapshotSource {
        public ProviderSnapshotSource {
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

    static String snapshotKey(Snapshot snapshot) {
        if (snapshot == null) {
            return "snapshot:null";
        }

        StringBuilder builder = new StringBuilder(128);
        builder.append(snapshot.type.name()).append('|');
        builder.append(snapshot.texture == null ? "-" : snapshot.texture.toString()).append('|');
        builder.append(snapshot.u).append('|');
        builder.append(snapshot.v).append('|');
        builder.append(snapshot.regionWidth).append('|');
        builder.append(snapshot.regionHeight).append('|');
        builder.append(snapshot.textureWidth).append('|');
        builder.append(snapshot.textureHeight).append('|');
        builder.append(snapshot.displayWidth).append('|');
        builder.append(snapshot.displayHeight).append('|');
        builder.append(snapshot.framed).append('|');
        builder.append(rendererKey(snapshot.renderer));
        return builder.toString();
    }

    static String rendererKey(SnapshotRenderer renderer) {
        if (renderer == null) {
            return "-";
        }

        StringBuilder builder = new StringBuilder(128);
        builder.append(renderer.getClass().getName()).append('|');
        if (renderer instanceof EmbeddedReflectiveGuiSnapshot embeddedReflectiveGuiSnapshot) {
            builder.append(embeddedReflectiveGuiSnapshot.cacheKey());
        } else if (renderer instanceof EmbeddedReflectiveTabGuiSnapshot embeddedReflectiveTabGuiSnapshot) {
            builder.append(embeddedReflectiveTabGuiSnapshot.cacheKey());
        } else if (renderer instanceof EmbeddedGuiFurnaceSnapshot embeddedGuiFurnaceSnapshot) {
            builder.append(embeddedGuiFurnaceSnapshot.cacheKey());
        } else if (renderer instanceof SandboxTriggeredBlockGuiSnapshot sandboxTriggeredBlockGuiSnapshot) {
            builder.append(sandboxTriggeredBlockGuiSnapshot.cacheKey());
        } else {
            builder.append('@').append(Integer.toHexString(System.identityHashCode(renderer)));
        }
        return builder.toString();
    }

}
