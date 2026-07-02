package net.createmod.ponder.foundation.ui.projection;

import javax.annotation.Nullable;

import net.createmod.ponder.foundation.ui.SpeechRenderer;
import net.minecraft.util.math.Vec3d;

@FunctionalInterface
public interface ScenePointProjector {

    @Nullable
    SpeechRenderer.Point project(SceneProjectionContext context, Vec3d point);
}
