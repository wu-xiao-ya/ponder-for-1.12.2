package net.createmod.ponder.foundation.ui;

import javax.annotation.Nullable;

import net.minecraft.util.ResourceLocation;

public final class Snapshot {

    public enum RenderCapability {
        TEXTURE_BOUND,
        RENDERER_DRIVEN
    }

    public enum SnapshotType {
        FULL_TEXTURE(RenderCapability.TEXTURE_BOUND),
        LIVE_RENDERER(RenderCapability.RENDERER_DRIVEN);

        private final RenderCapability renderCapability;

        SnapshotType(RenderCapability renderCapability) {
            this.renderCapability = renderCapability;
        }

        public RenderCapability renderCapability() {
            return renderCapability;
        }
    }

    public final SnapshotType type;
    @Nullable
    public final ResourceLocation texture;
    public final int u;
    public final int v;
    public final int regionWidth;
    public final int regionHeight;
    public final int textureWidth;
    public final int textureHeight;
    public final int displayWidth;
    public final int displayHeight;
    public final boolean framed;
    @Nullable
    public final SnapshotRenderer renderer;

    public Snapshot(@Nullable ResourceLocation texture, int u, int v, int regionWidth, int regionHeight,
        int textureWidth, int textureHeight, int displayWidth, int displayHeight,
        boolean framed, @Nullable SnapshotRenderer renderer) {
        this.type = renderer != null ? SnapshotType.LIVE_RENDERER : SnapshotType.FULL_TEXTURE;
        this.texture = texture;
        this.u = u;
        this.v = v;
        this.regionWidth = regionWidth;
        this.regionHeight = regionHeight;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.displayWidth = displayWidth;
        this.displayHeight = displayHeight;
        this.framed = framed;
        this.renderer = renderer;
    }

    public static Snapshot fullTexture(ResourceLocation texture, int width, int height) {
        return new Snapshot(texture, 0, 0, width, height, width, height, width, height, true, null);
    }

    public static Snapshot liveRenderer(int width, int height, boolean framed, SnapshotRenderer renderer) {
        return new Snapshot(null, 0, 0, width, height, width, height, width, height, framed, renderer);
    }

    public RenderCapability renderCapability() {
        return type.renderCapability;
    }

    public boolean hasRenderer() {
        return renderer != null;
    }

    public boolean hasTexture() {
        return texture != null;
    }

}
