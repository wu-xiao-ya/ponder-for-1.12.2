package net.createmod.ponder.foundation.ui;

import net.minecraft.util.math.MathHelper;

final class PonderPreviewCameraState {

    private float previewYaw = -45.0F;
    private float previewPitch = 28.0F;
    private float previewZoom = 1.0F;
    private boolean previewDragging;
    private int previewDragMouseX;
    private int previewDragMouseY;
    private int showcaseFadeTicks;

    float getPreviewYaw() {
        return previewYaw;
    }

    float getPreviewPitch() {
        return previewPitch;
    }

    float getPreviewZoom() {
        return previewZoom;
    }

    void adjustPreviewZoom(float delta) {
        previewZoom = MathHelper.clamp(previewZoom + delta, 0.45F, 2.4F);
    }

    boolean isPreviewDragging() {
        return previewDragging;
    }

    void startDragging(int mouseX, int mouseY) {
        previewDragging = true;
        previewDragMouseX = mouseX;
        previewDragMouseY = mouseY;
    }

    void dragTo(int mouseX, int mouseY) {
        int deltaX = mouseX - previewDragMouseX;
        int deltaY = mouseY - previewDragMouseY;
        previewYaw = MathHelper.wrapDegrees(previewYaw + deltaX * 0.75F);
        previewPitch = MathHelper.clamp(previewPitch + deltaY * 0.6F, -75.0F, 75.0F);
        previewDragMouseX = mouseX;
        previewDragMouseY = mouseY;
    }

    void stopDragging() {
        previewDragging = false;
    }

    void reset(boolean showcaseMode) {
        this.previewYaw = -45.0F;
        this.previewPitch = 26.0F;
        this.previewZoom = showcaseMode ? 1.24F : 1.0F;
        this.previewDragging = false;
        this.showcaseFadeTicks = 0;
    }

    void tickShowcaseFade() {
        if (showcaseFadeTicks < 20) {
            showcaseFadeTicks++;
        }
    }

    float getShowcaseFade(float partialTicks) {
        return MathHelper.clamp((showcaseFadeTicks + partialTicks) / 20.0F, 0.0F, 1.0F);
    }
}