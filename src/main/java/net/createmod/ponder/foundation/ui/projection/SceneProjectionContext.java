package net.createmod.ponder.foundation.ui.projection;

import java.util.Objects;

import javax.annotation.Nullable;

import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.ui.PreviewLayout;
import net.createmod.ponder.foundation.ui.SpeechRenderer;
import net.minecraft.util.math.Vec3d;

public record SceneProjectionContext(
    PonderScene scene,
    SceneBounds sceneBounds,
    PreviewLayout layout,
    float renderTick,
    ScenePointProjector projector) {

    public SceneProjectionContext {
        Objects.requireNonNull(scene);
        Objects.requireNonNull(sceneBounds);
        Objects.requireNonNull(layout);
        Objects.requireNonNull(projector);
    }

    @Nullable
    public SpeechRenderer.Point project(Vec3d point) {
        return projector.project(this, point);
    }
}
