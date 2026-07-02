package net.createmod.ponder.foundation.ui;

import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.ui.PonderScenePreview.PreviewBounds;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

final class ParticleOverlayRenderer {

    private final PonderOverlayLayoutHelper overlayLayoutHelper;
    private final DrawContext draw;

    ParticleOverlayRenderer(PonderOverlayLayoutHelper overlayLayoutHelper, DrawContext draw) {
        this.overlayLayoutHelper = overlayLayoutHelper;
        this.draw = draw;
    }

    void drawParticleEffects(PonderScene scene, PreviewBounds bounds, PreviewLayout layout,
        float currentTick, float fade) {
        for (PonderScene.ParticleEffectEvent event : scene.getParticleEvents()) {
            if (event.getTick() > currentTick || currentTick > event.getTick() + event.getCycles()) {
                continue;
            }

            float eventFade = PonderOverlayHelper.computeOverlayFade(event.getTick(), Math.max(1, event.getCycles()),
                currentTick) * fade;
            if (eventFade <= 0.0F) {
                continue;
            }

            float progress = Math.max(0.0F, currentTick - event.getTick());
            int particleCount = MathHelper.clamp(Math.round(event.getAmountPerCycle()), 1, 12);
            int color = getParticleColor(event.getParticleName());
            int alphaColor = draw.withAlpha(color, eventFade * 220.0F);
            double spread = event.isWithinBlockSpace() ? 0.55D : 0.28D;

            for (int i = 0; i < particleCount; i++) {
                double angle = Math.toRadians((event.getTick() * 37 + i * (360.0D / particleCount)) % 360);
                double radius = spread * (0.35D + (i % 3) * 0.25D);
                Vec3d baseOffset = new Vec3d(Math.cos(angle) * radius, ((i % 4) - 1.5D) * 0.08D,
                    Math.sin(angle) * radius);
                Vec3d motionOffset = event.getMotion().scale(progress);
                Vec3d point = event.getLocation().add(baseOffset).add(motionOffset);
                SpeechRenderer.Point projected =
                    overlayLayoutHelper.projectScenePoint(scene, bounds, layout, currentTick, point);
                if (projected == null) {
                    continue;
                }

                int size = i % 3 == 0 ? 2 : 1;
                draw.fillRect(projected.x - size, projected.y - size, projected.x + size + 1, projected.y + size + 1,
                    alphaColor);
            }
        }
    }

    private static int getParticleColor(String particleName) {
        if (particleName == null) {
            return 0xF2D28C;
        }
        int separator = particleName.indexOf(':');
        if (separator >= 0 && particleName.regionMatches(true, 0, "REDSTONE", 0, "REDSTONE".length())) {
            String hex = particleName.substring(separator + 1).trim();
            try {
                return Integer.parseInt(hex, 16) & 0xFFFFFF;
            } catch (NumberFormatException ignored) {
            }
        }
        String upper = particleName.toUpperCase();
        if (upper.contains("FLAME") || upper.contains("LAVA")) {
            return 0xFFB347;
        }
        if (upper.contains("PORTAL")) {
            return 0xB084F5;
        }
        if (upper.contains("BUBBLE")) {
            return 0x8FD3FF;
        }
        if (upper.contains("REDSTONE")) {
            return 0xFF6B6B;
        }
        return 0xF2D28C;
    }
}
