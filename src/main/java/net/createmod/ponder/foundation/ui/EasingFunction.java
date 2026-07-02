package net.createmod.ponder.foundation.ui;

@FunctionalInterface
interface EasingFunction {

    EasingFunction LINEAR = progress -> progress;
    EasingFunction EASE_OUT_QUAD = progress -> {
        float inverse = 1.0F - progress;
        return 1.0F - inverse * inverse;
    };

    float apply(float progress);
}
