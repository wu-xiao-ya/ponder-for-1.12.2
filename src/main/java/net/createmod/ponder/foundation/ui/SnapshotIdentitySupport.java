package net.createmod.ponder.foundation.ui;

final class SnapshotIdentitySupport {
    private SnapshotIdentitySupport() {
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
