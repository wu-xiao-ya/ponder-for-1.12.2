package net.createmod.ponder.foundation.ui;

import net.minecraft.util.math.MathHelper;

record AnimationSpec(int durationTicks, EasingFunction easing) {

    static AnimationSpec linear(int durationTicks) {
        return new AnimationSpec(durationTicks, EasingFunction.LINEAR);
    }

    static AnimationSpec easeOutQuad(int durationTicks) {
        return new AnimationSpec(durationTicks, EasingFunction.EASE_OUT_QUAD);
    }

    float progress(float elapsedTicks) {
        if (durationTicks <= 0) {
            return 1.0F;
        }
        float normalized = MathHelper.clamp((elapsedTicks + 1.0F) / durationTicks, 0.0F, 1.0F);
        return easing.apply(normalized);
    }
}
